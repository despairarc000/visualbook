#!/usr/bin/env python3
"""VisualBook IR 0.4-draft bridge / validation proof. No Android audio or art.

Intentionally bridges the preserved 0.3 parser; DOES NOT change historical snapshots.
This file is a disposable prototype used to test the canonical IR contract.
"""
from __future__ import annotations

import argparse
import hashlib
import importlib.util
import json
import sys
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[3]
LEGACY = ROOT / "knowledge-system/vault/history/v0.3/prototype/storytool.py"
IR_VERSION = "0.4-draft"
PALETTE = ("slate", "amber", "teal", "violet", "coral", "blue")


def _legacy_compile(source: str, mode: str) -> dict:
    spec = importlib.util.spec_from_file_location("visualbook_archived_storytool", LEGACY)
    if not spec or not spec.loader:
        raise RuntimeError("Missing historical parser: " + str(LEGACY))
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod.compile_story(source, mode=mode)


def fail(code: str, message: str) -> dict:
    return {"code": code, "severity": "error", "message": message}


def compile_ir(source: str, mode: str = "script") -> dict[str, Any]:
    """Return {ok, ir | None, diagnostics}. Do not emit apparently valid IR on errors."""
    if mode not in {"txt", "script"}:
        raise ValueError("mode must be txt or script")
    old = _legacy_compile(source, mode)
    diagnostics = list(old["diagnostics"])
    if any(d["severity"] == "error" for d in diagnostics):
        return {"ok": False, "ir": None, "diagnostics": diagnostics}
    if len(old["scenes"]) == 0:
        return {"ok": False, "ir": None, "diagnostics": diagnostics + [fail("IR001", "No scenes")]}

    characters = []
    cast_idx = 0
    for c in old["characters"]:
        narrator = c["id"] == "n"
        appearance = "none" if narrator else "generic.silhouette"
        if not narrator:
            accent = PALETTE[cast_idx % len(PALETTE)]
            cast_idx += 1
        else:
            accent = "none"
        characters.append({
            "id": c["id"], "display_name": c["name"], "sex_hint": c["sex"],
            "implicit": c["implicit"], "appearance": {
                "logical_id": appearance,
                "accent": accent,
                "fallback": "procedural.silhouette" if not narrator else "none"
            },
            "voice_intent": {"language": "und", "profile": "narrator" if narrator else "character",
                             "required_for_visual_playback": False}
        })

    scenes = []
    for i, old_scene in enumerate(old["scenes"], 1):
        if not old_scene["blocks"]:
            diagnostics.append(fail("IR002", "Empty scene requires future explicit silent-beat semantics"))
            continue
        blocks = []
        for block in old_scene["blocks"]:
            utterances = [
                {"id": f"{block['id']}.u{j:04d}", "text": u["text"]}
                for j, u in enumerate(block["segments"], 1)
            ]
            blocks.append({
                "id": block["id"], "speaker_id": block["speaker"],
                "source_text": block["text"], "spoken_text": block["spoken_text"],
                "source_line": block["line"], "utterances": utterances
            })
        cues = [{
            "id": cue["id"],
            "anchor": {"block_id": cue["anchor"]["block"], "edge": cue["anchor"]["edge"]},
            "action": {"type": cue["action"], "character_id": cue["character"], "value": cue["value"]}
        } for cue in old_scene["cues"]]
        scenes.append({
            "visit_id": f"scene.{i:06d}",
            "logical_id": old_scene["id"],
            "background": {"role": "background", "logical_id": old_scene["background"],
                           "fallback_id": "bg.neutral", "missing_policy": "procedural-neutral"},
            "blocks": blocks, "cues": cues
        })

    if any(d["severity"] == "error" for d in diagnostics):
        return {"ok": False, "ir": None, "diagnostics": diagnostics}
    normalized_source = source.lstrip("\ufeff").replace("\r\n", "\n").replace("\r", "\n")
    sha = hashlib.sha256(normalized_source.encode("utf-8")).hexdigest()
    ir = {
        "format": "visualbook.story-ir", "ir_version": IR_VERSION,
        "story": {"id": "story." + sha[:20]},
        "source": {"sha256": sha, "input_mode": mode, "encoding": "UTF-8",
                   "compiler": "v0.4-bridge-from-v0.3"},
        "capabilities": {"required": ["story.speech", "visual.generic-defaults"],
                         "optional": ["visual.expression"]},
        "defaults": {"visual_style": "storybook-flat-v1",
                     "background_id": "bg.neutral", "music_policy": "silent",
                     "ambience_policy": "silent", "subtitles": "available"},
        "characters": characters,
        "chapters": [{"id": "chapter.0001", "scenes": scenes}]
    }
    errors = validate_ir(ir)
    if errors:
        return {"ok": False, "ir": None, "diagnostics": diagnostics + errors}
    return {"ok": True, "ir": ir, "diagnostics": diagnostics}


def validate_ir(ir: dict[str, Any]) -> list[dict]:
    """Additional cross-reference/semantic checks beyond JSON schema shape."""
    out = []
    try:
        if ir["format"] != "visualbook.story-ir" or ir["ir_version"] != IR_VERSION:
            return [fail("IR100", "Unsupported IR envelope/version")]
        chars = ir["characters"]
        char_ids = [c["id"] for c in chars]
        if len(char_ids) != len(set(char_ids)):
            out.append(fail("IR101", "Duplicate character ID"))
        seen_scene, seen_block, seen_utterance, seen_cue = set(), set(), set(), set()
        for chapter in ir["chapters"]:
            for sc in chapter["scenes"]:
                sid = sc["visit_id"]
                if sid in seen_scene: out.append(fail("IR102", "Duplicate scene visit ID: " + sid))
                seen_scene.add(sid)
                if sc["background"]["role"] != "background" or not sc["background"]["logical_id"].startswith("bg."):
                    out.append(fail("IR103", "Background role or logical ID invalid"))
                local_blocks = set()
                for b in sc["blocks"]:
                    bid = b["id"]
                    if bid in seen_block: out.append(fail("IR104", "Duplicate block ID: " + bid))
                    seen_block.add(bid);local_blocks.add(bid)
                    if b["speaker_id"] not in char_ids: out.append(fail("IR105", "Unknown speaker"))
                    if not b["utterances"]: out.append(fail("IR106", "Empty utterance list"))
                    for u in b["utterances"]:
                        if u["id"] in seen_utterance: out.append(fail("IR107", "Duplicate utterance ID"))
                        seen_utterance.add(u["id"])
                        if not u["text"].strip(): out.append(fail("IR108", "Empty spoken utterance"))
                    if " ".join(u["text"] for u in b["utterances"]) != b["spoken_text"]:
                        out.append(fail("IR109", "Utterance concatenation changed spoken text"))
                for cue in sc["cues"]:
                    if cue["id"] in seen_cue: out.append(fail("IR110", "Duplicate cue ID"))
                    seen_cue.add(cue["id"])
                    if cue["anchor"]["block_id"] not in local_blocks:
                        out.append(fail("IR111", "Cue references block in another scene"))
                    if cue["anchor"]["edge"] not in {"start", "end"}:
                        out.append(fail("IR112", "Invalid cue anchor edge"))
                    a = cue["action"]
                    if a["type"] != "expression" or a["character_id"] not in char_ids:
                        out.append(fail("IR113", "Unsupported action or missing actor"))
    except (KeyError, TypeError, AttributeError) as exc:
        out.append(fail("IR199", "Malformed IR: " + type(exc).__name__))
    return out


def event_plan(ir: dict[str, Any]) -> list[dict[str, Any]]:
    """Ordered *logical* boundary plan. No invented seconds or media playback."""
    errors = validate_ir(ir)
    if errors:
        raise ValueError(errors)
    result = []
    for chapter in ir["chapters"]:
        for scene in chapter["scenes"]:
            by_anchor = {}
            for cue in scene["cues"]:
                a = cue["anchor"]
                by_anchor.setdefault((a["block_id"], a["edge"]), []).append(cue)
            for block in scene["blocks"]:
                for cue in by_anchor.get((block["id"], "start"), []):
                    result.append({"event": "cue", "scene_visit_id": scene["visit_id"],
                                   "cue": cue["id"], "anchor": "block.start"})
                for u in block["utterances"]:
                    result.append({"event": "audible.start", "scene_visit_id": scene["visit_id"],
                                   "utterance_id": u["id"], "speaker_id": block["speaker_id"]})
                    result.append({"event": "audible.end", "scene_visit_id": scene["visit_id"],
                                   "utterance_id": u["id"], "speaker_id": block["speaker_id"]})
                for cue in by_anchor.get((block["id"], "end"), []):
                    result.append({"event": "cue", "scene_visit_id": scene["visit_id"],
                                   "cue": cue["id"], "anchor": "block.end"})
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("--mode", choices=["auto", "script", "txt"], default="auto")
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    mode = ("txt" if args.source.suffix == ".txt" else "script") if args.mode == "auto" else args.mode
    result = compile_ir(args.source.read_text(encoding="utf-8"), mode=mode)
    payload = json.dumps(result, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.write_text(payload, encoding="utf-8")
    else:
        print(payload, end="")
    return 0 if result["ok"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
