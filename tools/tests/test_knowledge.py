import json
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
    def test_live_index(self):
        self.assertTrue((ROOT/"NAVIGATION.md").is_file())

if __name__=="__main__": unittest.main()
