#!/usr/bin/env python3
"""VisualBook GitHub-native vault/ICM/graph integrity doctor (no external deps)."""
from __future__ import annotations
import re
import subprocess
import sys
from pathlib import Path
from urllib.parse import unquote

ROOT = Path(__file__).resolve().parents[1]
LINK_RE = re.compile(r"!?(?:\[[^\]]*\])\(([^)]+)\)")
REQUIRED = [
    "README.md","NAVIGATION.md","IDEA_PLAN.md","IMPLEMENTATION_PLAN.md","AGENTS.md",
    "knowledge-system/vault/INDEX.md",
    "knowledge-system/vault/checkpoints/CURRENT.md",
    "knowledge-system/vault/decisions/DECISION_REGISTER.md",
    "knowledge-system/vault/history/v0.2/builtin_asset_catalog_seed.json",
    "knowledge-system/vault/history/v0.3/00_START_HERE.md",
    "knowledge-system/icm/routes.json","knowledge-system/icm/icm.py",
    "knowledge-system/graphify/graph.json","knowledge-system/graphify/graphify.py"
]

def active_markdown():
    for f in ROOT.rglob("*.md"):
        rel=f.relative_to(ROOT).as_posix()
        if "/history/" in rel or ".git/" in rel: continue
        yield f

def link_errors():
    errors=[]
    for f in active_markdown():
        content=f.read_text(encoding="utf-8")
        in_fence=False
        for line_number,line in enumerate(content.splitlines(),start=1):
            stripped=line.lstrip()
            if stripped.startswith("```") or stripped.startswith("~~~"):
                in_fence=not in_fence
                continue
            if in_fence: continue
            for match in LINK_RE.finditer(line):
                link=match.group(1).split(' "',1)[0].strip()
                if link.startswith(("#","http://","https://","mailto:","data:")): continue
                raw=unquote(link.split("#",1)[0].split("?",1)[0])
                if not raw: continue
                resolved=(f.parent / raw).resolve()
                if not resolved.is_relative_to(ROOT) or not resolved.exists():
                    errors.append(f"{f.relative_to(ROOT)}:{line_number} -> {link}")
    return errors

def main():
    errors=[]
    for path in REQUIRED:
        if not (ROOT/path).is_file(): errors.append("required file missing: "+path)
    errors.extend("broken link: "+e for e in link_errors())
    for command in [
        [sys.executable,"knowledge-system/icm/icm.py","doctor"],
        [sys.executable,"knowledge-system/graphify/graphify.py","validate"]
    ]:
        r=subprocess.run(command,cwd=ROOT,text=True,capture_output=True)
        print(r.stdout.strip())
        if r.returncode:
            errors.append("failed: "+" ".join(command)+": "+r.stderr.strip())
    for issue in errors: print("ERROR:",issue,file=sys.stderr)
    print(f"VisualBook doctor: {len(list(active_markdown()))} live MD files, {len(errors)} issue(s)")
    return 1 if errors else 0

if __name__=="__main__": sys.exit(main())
