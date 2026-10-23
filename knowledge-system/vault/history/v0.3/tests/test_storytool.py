import sys, unittest
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]/'prototype'))
from storytool import compile_story, segment

class StoryToolContractTests(unittest.TestCase):
    def test_plain_txt_is_always_narrator(self):
        d=compile_story('Juan: Kumusta?\n\n@c2m Hala!', 'txt')
        self.assertEqual([b['speaker'] for s in d['scenes'] for b in s['blocks']],['n','n'])
    def test_implicit_characters(self):
        d=compile_story('@c1m\nKumusta?\n\n@c2f\nMabuti.\n')
        self.assertEqual({x['id']:x['sex'] for x in d['characters']},{'n':None,'c1':'m','c2':'f'})
    def test_sex_metadata_sticks(self):
        d=compile_story('@c1m\nUna.\n@c1\nPangalawa.')
        self.assertEqual(len(d['characters']),2)
        self.assertEqual(d['characters'][1]['sex'],'m')
    def test_conflicting_sex_issues_warning_no_overwrite(self):
        d=compile_story('@c1m\nUna.\n@c1f\nPangalawa.')
        self.assertEqual(d['characters'][1]['sex'],'m')
        self.assertIn('CHAR002',[e['code'] for e in d['diagnostics']])
    def test_block_end_after_all_sentences(self):
        d=compile_story('@c1\nUna. Pangalawa.\n@after c1 expression=happy')
        b=d['scenes'][0]['blocks'][0]
        self.assertEqual(len(b['segments']),2)
        self.assertEqual(d['scenes'][0]['cues'][0]['anchor'],{'block':b['id'],'edge':'end'})
    def test_inline_speaker(self):
        d=compile_story('@c1m Kumusta?\n@c2f Mabuti.')
        self.assertEqual([b['speaker'] for b in d['scenes'][0]['blocks']],['c1','c2'])
    def test_blank_paragraph_same_speaker(self):
        d=compile_story('@c1m\nUna.\n\nPangalawa.')
        self.assertEqual([b['speaker'] for b in d['scenes'][0]['blocks']],['c1','c1'])
    def test_literal_escape(self):
        d=compile_story('@n\n@@c1 is literally printed.')
        self.assertEqual(d['scenes'][0]['blocks'][0]['spoken_text'],'@c1 is literally printed.')
    def test_unknown_directive_error(self):
        d=compile_story('@unexpected foo\nHello.')
        self.assertIn('SYNTAX001',[e['code'] for e in d['diagnostics']])
    def test_empty_block_after_error(self):
        d=compile_story('@n\n@after c1 expression=happy')
        self.assertTrue({'CUE001','STORY001'}.issubset({e['code'] for e in d['diagnostics']}))
    def test_before_next_block(self):
        d=compile_story('@before c1 expression=happy\n@c1\nHi.')
        self.assertEqual(d['scenes'][0]['cues'][0]['anchor']['edge'],'start')
    def test_before_scene_boundary_error(self):
        d=compile_story('@before c1 expression=happy\n@scene new\nHi.')
        self.assertIn('CUE004',[e['code'] for e in d['diagnostics']])
    def test_before_end_of_file_error(self):
        d=compile_story('@n\nHello.\n@before c1 expression=happy')
        self.assertIn('CUE003',[e['code'] for e in d['diagnostics']])
    def test_character_name(self):
        d=compile_story('@character c1 name="Juan Dela Cruz" sex=m\n@c1 Hello.')
        self.assertEqual(d['characters'][1]['name'],'Juan Dela Cruz')
    def test_background_fallback_id(self):
        d=compile_story('@bg school.classroom\n@n\nHello.')
        self.assertEqual(d['scenes'][0]['background'],'bg.school.classroom')
    def test_segmentation_preserves_words(self):
        t='Dr. Juan paid 3.14 pesos. Pedro! Ayos.'
        s=segment(t)
        self.assertEqual(' '.join(s),t)
        self.assertEqual(len(s),3)
    def test_segment_cap(self):
        t='word '*300
        s=segment(t,80)
        self.assertTrue(all(len(x)<=80 for x in s))
        self.assertEqual(' '.join(s),' '.join(t.split()))
    def test_late_background_does_not_retroactively_change_scene(self):
        d=compile_story('@n\nHello.\n@bg school.classroom')
        self.assertIn('SCENE002',[e['code'] for e in d['diagnostics']])
        self.assertEqual(d['scenes'][0]['background'],'bg.neutral')
    def test_empty_plain_txt_is_error(self):
        d=compile_story('  ', 'txt')
        self.assertIn('STORY001',[e['code'] for e in d['diagnostics']])
    def test_no_nulls(self):
        d=compile_story('Hi\x00')
        self.assertIn('SEC001',[e['code'] for e in d['diagnostics']])
    def test_same_block_id_stable(self):
        src='@c1\nHi.\n\n@c2\nHey.'
        self.assertEqual(compile_story(src),compile_story(src))
    def test_no_speaker_no_scene(self):
        d=compile_story('Isang araw, sumikat ang araw.')
        self.assertEqual(d['scenes'][0]['background'],'bg.neutral')
        self.assertEqual(d['scenes'][0]['blocks'][0]['speaker'],'n')

if __name__=='__main__': unittest.main()
