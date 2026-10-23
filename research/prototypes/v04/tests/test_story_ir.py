"""New IR 0.4 prototype contract checks, not Android integration tests."""
import copy
import importlib.util
import unittest
from pathlib import Path

SOURCE = Path(__file__).resolve().parents[1] / "story_ir.py"
spec = importlib.util.spec_from_file_location("v04_story_ir", SOURCE)
mod = importlib.util.module_from_spec(spec)
spec.loader.exec_module(mod)

class CanonicalIrTests(unittest.TestCase):
    def compile_ok(self, script, mode="script"):
        r = mod.compile_ir(script, mode=mode)
        self.assertTrue(r["ok"], r["diagnostics"])
        return r["ir"]

    def test_minimal_generates_implicit_cast_and_procedural_defaults(self):
        ir = self.compile_ok("@c1m Kumusta?\n@c2f Mabuti.")
        self.assertEqual([c["id"] for c in ir["characters"]], ["n","c1","c2"])
        self.assertEqual(ir["characters"][1]["sex_hint"], "m")
        self.assertEqual(ir["characters"][2]["sex_hint"], "f")
        self.assertEqual(ir["characters"][2]["appearance"]["fallback"], "procedural.silhouette")
        self.assertEqual(ir["chapters"][0]["scenes"][0]["background"]["fallback_id"], "bg.neutral")
        self.assertEqual(ir["defaults"]["music_policy"], "silent")

    def test_plain_txt_never_interprets_marker(self):
        ir = self.compile_ok("Juan: Hello.\n\n@c1m Hehehe.", mode="txt")
        self.assertEqual([c["id"] for c in ir["characters"]], ["n"])
        self.assertIn("@c1m", ir["chapters"][0]["scenes"][0]["blocks"][1]["source_text"])

    def test_deterministic_ir_and_hash(self):
        src="@c1m Hi.\n@c1 Again."
        self.assertEqual(self.compile_ok(src), self.compile_ok(src))

    def test_CRLF_and_BOM_normalize_source_fingerprint(self):
        a=self.compile_ok("@n\nKumusta.")
        b=self.compile_ok("\ufeff@n\r\nKumusta.")
        self.assertEqual(a["source"]["sha256"], b["source"]["sha256"])

    def test_two_scene_visits_identified_uniquely(self):
        x=self.compile_ok("@scene school\n@c1 Hello.\n@scene school\n@c1 Hello again.")
        ids=[s["visit_id"] for s in x["chapters"][0]["scenes"]]
        self.assertEqual(len(ids),len(set(ids)))

    def test_last_utterance_end_anchor(self):
        ir=self.compile_ok("@c1m Una. Pangalawa.\n@after c2 expression=happy")
        tape=mod.event_plan(ir)
        self.assertEqual([x["event"] for x in tape],
                         ["audible.start","audible.end","audible.start","audible.end","cue"])
        self.assertEqual(tape[-1]["anchor"],"block.end")

    def test_before_cue_precedes_audio(self):
        ir=self.compile_ok("@before c1 expression=happy\n@c1 Hi.")
        self.assertEqual(mod.event_plan(ir)[0]["event"],"cue")

    def test_story_text_preserved_and_utterance_text_equals_normalized(self):
        ir=self.compile_ok("@c1m\n  Kumusta   ka?\nMasaya ako!")
        b=ir["chapters"][0]["scenes"][0]["blocks"][0]
        self.assertIn("\n", b["source_text"])
        self.assertEqual(" ".join(x["text"] for x in b["utterances"]),b["spoken_text"])

    def test_bad_source_fails_closed(self):
        r=mod.compile_ir("@imaginary unknown\nHi.")
        self.assertFalse(r["ok"])
        self.assertIsNone(r["ir"])

    def test_empty_scene_rejected_pending_silent_beats(self):
        r=mod.compile_ir("@scene intro\n@c1 Hello.\n@scene outro")
        self.assertFalse(r["ok"])
        self.assertIn("IR002",[x["code"] for x in r["diagnostics"]])

    def test_cross_scene_cue_is_rejected(self):
        ir=self.compile_ok("@scene a\n@c1 Hello.\n@scene b\n@c1 Bye.")
        sc=ir["chapters"][0]["scenes"]
        sc[1]["cues"]=[{"id":"bad","anchor":{"block_id":sc[0]["blocks"][0]["id"],"edge":"end"},
                         "action":{"type":"expression","character_id":"c1","value":"happy"}}]
        self.assertIn("IR111",[x["code"] for x in mod.validate_ir(ir)])

    def test_duplicate_character_validation(self):
        ir=self.compile_ok("@c1m Hello.")
        ir["characters"].append(copy.deepcopy(ir["characters"][1]))
        self.assertIn("IR101",[x["code"] for x in mod.validate_ir(ir)])

    def test_corrupt_utterance_detected(self):
        ir=self.compile_ok("@c1m Hello.")
        ir["chapters"][0]["scenes"][0]["blocks"][0]["utterances"][0]["text"]="Tampered!"
        self.assertIn("IR109",[x["code"] for x in mod.validate_ir(ir)])

    def test_no_voice_model_assumed(self):
        ir=self.compile_ok("@c1m Hi.")
        v=ir["characters"][1]["voice_intent"]
        self.assertEqual(v["language"],"und")
        self.assertNotIn("voice_model",v)

    def test_plain_prose_only_has_narrator(self):
        ir=self.compile_ok("Isang araw, sumikat ang araw.")
        self.assertEqual(ir["characters"][0]["id"],"n")
        self.assertEqual(len(ir["characters"]),1)

    def test_expression_has_valid_actor(self):
        ir=self.compile_ok("@c1m Hi.\n@after c2 expression=confused")
        cues=ir["chapters"][0]["scenes"][0]["cues"]
        self.assertEqual(cues[0]["action"]["character_id"],"c2")

if __name__ == "__main__":
    unittest.main()
