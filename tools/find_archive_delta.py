#!/usr/bin/env python3
"""Investigate single-character transcription drift against historical SHA-256.

This tool does not modify source; run only for historical audit, not as a repair
strategy for unknown or untrusted data.
"""
import hashlib
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/"knowledge-system/vault/history/v0.3"
manifest=json.loads((BASE/"CHECKSUMS_AND_RESULTS.json").read_text(encoding="utf-8"))
target=manifest["originalSourceSha256"]["prototype/storytool.py"]
path=BASE/"prototype/storytool.py"
src=path.read_text(encoding="utf-8")
alphabet=" \t\n\\'\".,:;()[]{}+-=*/!?@#0123456789"
for ch in alphabet:
    for index in range(len(src)+1):
        alt=src[:index]+ch+src[index:]
        if hashlib.sha256(alt.encode("utf-8")).hexdigest()==target:
            line=src[:index].count("\n")+1
            print(f"MATCH missing {ch!r} at 0-based index {index}, 1-based line {line}")
            print("Context:",repr(src[max(0,index-60):index+60]))
            raise SystemExit(0)
print("No single missing character from candidate alphabet matches archived digest.")
