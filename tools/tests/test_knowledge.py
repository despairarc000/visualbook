import json
import hashlib
import sys
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/"knowledge-system"/"icm"))
sys.path.insert(0,str(ROOT/"knowledge-system"/"graphify"))
import icm
import graphify
import scan_code

class KnowledgeTest(unittest.TestCase):
    def test_routes_valid(self):
        self.assertEqual(icm.doctor(icm.load()),[])
    def test_graph_valid(self):
        self.assertEqual(graphify.validate(graphify.load()),[])
    def test_critical_narration_route(self):
        p=icm.context(icm.load(),"narration pointer")["paths"]
        self.assertIn("knowledge-system/vault/design/NARRATION_AND_PLAYBACK.md",p)
    def test_critical_assets_route(self):
        p=icm.context(icm.load(),"generic character assets")["paths"]
        self.assertIn("knowledge-system/vault/design/BUILTIN_CONTENT_PLAN.md",p)
    def test_graph_trace(self):
        self.assertIn("event-reducer",graphify.impact(graphify.load(),"narration-cursor"))
    def test_snapshot_read_only_provenance(self):
        src=ROOT/"knowledge-system/vault/history/v0.2/builtin_asset_catalog_seed.json"
        j=json.loads(src.read_text(encoding="utf-8"))
        self.assertIn("catalogVersion",j)
    def test_source_graph_scanner(self):
        result=scan_code.scan()
        self.assertEqual(result['parse_errors'],[])
        self.assertTrue(any('storytool.py:compile_story' in n['id'] for n in result['nodes']))
        self.assertTrue(any(e['relation']=='defines' for e in result['edges']))
    def test_tagalog_long_story_integrity(self):
        p=ROOT/"samples/tagalog_tts_anthology.txt"
        digest=hashlib.sha256(p.read_bytes()).hexdigest()
        self.assertEqual(digest,"858860b156b2f76781199f9a5170f3b1dba8c1f59acb8b2bae27665b47833c14")
    def test_voice_corpus_has_30_cases(self):
        p=ROOT/"knowledge-system/vault/history/v0.3/research/TAGALOG_TTS_TEST_CORPUS.tsv"
        lines=p.read_text(encoding="utf-8").splitlines()
        self.assertEqual(len(lines)-1,30)
    def test_live_index(self):
        self.assertTrue((ROOT/"NAVIGATION.md").is_file())

if __name__=="__main__": unittest.main()
