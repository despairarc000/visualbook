"""Independent JSON Schema 2020-12 structural checks for the v0.4 research IR."""
import copy
import importlib.util
import json
import unittest
from pathlib import Path

from jsonschema import Draft202012Validator

ROOT=Path(__file__).resolve().parents[4]
SCHEMA=json.loads((ROOT/"schemas/story-ir-v0.4-draft.schema.json").read_text(encoding="utf-8"))
spec=importlib.util.spec_from_file_location("visualbook_ir_proto", ROOT/"research/prototypes/v04/story_ir.py")
module=importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

class SchemaChecks(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        Draft202012Validator.check_schema(SCHEMA)
        cls.validator=Draft202012Validator(SCHEMA)

    def good_ir(self):
        result=module.compile_ir("@n Isang araw.\n@c1m Kumusta?\n@c2f Mabuti!\n@after c1 expression=happy")
        self.assertTrue(result["ok"],result["diagnostics"])
        return result["ir"]

    def test_schema_is_valid(self):
        Draft202012Validator.check_schema(SCHEMA)

    def test_compiler_output_passes_standard_validator(self):
        self.assertEqual(list(self.validator.iter_errors(self.good_ir())),[])

    def test_unknown_fields_are_rejected(self):
        ir=self.good_ir()
        ir["untrusted_control"]="execute"
        self.assertFalse(self.validator.is_valid(ir))

    def test_wrong_asset_role_is_rejected(self):
        ir=self.good_ir()
        ir["chapters"][0]["scenes"][0]["background"]["role"]="character"
        self.assertFalse(self.validator.is_valid(ir))

    def test_unsupported_expression_is_rejected(self):
        ir=self.good_ir()
        ir["chapters"][0]["scenes"][0]["cues"][0]["action"]["value"]="laser"
        self.assertFalse(self.validator.is_valid(ir))

    def test_wrong_ir_version_is_rejected(self):
        ir=self.good_ir()
        ir["ir_version"]="0.999"
        self.assertFalse(self.validator.is_valid(ir))

    def test_semantic_duplicate_actor_rejected_even_if_shape_is_valid(self):
        ir=self.good_ir()
        ir["characters"].append(copy.deepcopy(ir["characters"][1]))
        self.assertTrue(self.validator.is_valid(ir))
        self.assertIn("IR101",[x["code"] for x in module.validate_ir(ir)])

if __name__=="__main__":
    unittest.main()
