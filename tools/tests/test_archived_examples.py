import json
import sys
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[2]
BASE=ROOT/"knowledge-system/vault/history/v0.3"
sys.path.insert(0,str(BASE/"prototype"))
from storytool import compile_story

class ArchivedGoldenExamples(unittest.TestCase):
    def test_minimal(self):
        source=(BASE/"examples/minimal.story.md").read_text(encoding="utf-8")
        expected=json.loads((BASE/"examples/minimal.compiled.json").read_text(encoding="utf-8"))
        self.assertEqual(compile_story(source),expected)

    def test_juan_pedro(self):
        source=(BASE/"examples/juan_pedro.story.md").read_text(encoding="utf-8")
        expected=json.loads((BASE/"examples/juan_pedro.compiled.json").read_text(encoding="utf-8"))
        self.assertEqual(compile_story(source),expected)

if __name__=="__main__": unittest.main()
