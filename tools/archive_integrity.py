#!/usr/bin/env python3
"""Compare committed historical source files against original v0.3 SHA-256 manifest.

Default: report differences but return 0 (transcription drift is evidence, not runnable-code failure).
--strict: require bit-for-bit fidelity on all listed files.
"""
import argparse
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "knowledge-system/vault/history/v0.3"
MANIFEST = BASE / "CHECKSUMS_AND_RESULTS.json"

def check():
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    verified = []
    mismatches = []
    for relative, expected in sorted(manifest.get("originalSourceSha256", {}).items()):
        path = BASE / relative
        actual = hashlib.sha256(path.read_bytes()).hexdigest() if path.is_file() else None
        if actual == expected:
            verified.append(relative)
        else:
            mismatches.append((relative, expected, actual))
    return verified, mismatches

if __name__ == "__main__":
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--strict", action="store_true")
    args = ap.parse_args()
    ok, bad = check()
    print(f"Historical v0.3 SHA-256: {len(ok)} byte-identical, {len(bad)} divergent/missing")
    for path, expected, actual in bad:
        print(f"  DRIFT {path}: expected {expected}; observed {actual or 'MISSING'}")
    if args.strict and bad:
        raise SystemExit(1)
