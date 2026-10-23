import sys, unittest
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]/'prototype'))
from storytool import compile_story
from fake_playback import FakeNarrationReducer

SOURCE='@c1m\nUna. Pangalawa.\n@after c2 expression=surprised\n@c2m\nAno?'
class FakePlaybackTests(unittest.TestCase):
    def setUp(self):
        self.d=FakeNarrationReducer(compile_story(SOURCE))
        self.assertEqual(len(self.d.sequence),3)
    def test_generation_is_not_playback(self):
        old=self.d.current()[1]
        self.assertTrue(self.d.prepare(old,self.d.generation))
        self.assertEqual(self.d.position,0)
        self.assertEqual(self.d.expressions,{})
    def test_end_of_first_sentence_does_not_fire_block_cue(self):
        first=self.d.current()[1]
        self.assertTrue(self.d.play(first,self.d.generation))
        self.assertTrue(self.d.end(first,self.d.generation))
        self.assertFalse(self.d.cue_log)
        second=self.d.current()[1]
        self.assertTrue(self.d.play(second,self.d.generation))
        self.assertTrue(self.d.end(second,self.d.generation))
        self.assertEqual(self.d.expressions['c2'],'surprised')
        self.assertEqual(len(self.d.cue_log),1)
    def test_seek_back_reconstructs_before_expression(self):
        for _ in range(2):
            current=self.d.current()[1]
            self.d.play(current,self.d.generation)
            self.d.end(current,self.d.generation)
        self.assertEqual(self.d.expressions['c2'],'surprised')
        self.d.seek_to(1)
        self.assertNotIn('c2',self.d.expressions)
        self.assertFalse(self.d.cue_log)
    def test_seek_forward_reconstructs_without_emitting_event(self):
        self.d.seek_to(2)
        self.assertEqual(self.d.expressions['c2'],'surprised')
        self.assertEqual(self.d.cue_log,[])
    def test_stale_utterance_callback_does_not_advance(self):
        first=self.d.current()[1]
        old_gen=self.d.generation
        self.d.seek_to(1)
        self.assertFalse(self.d.play(first,old_gen))
        self.assertFalse(self.d.end(first,old_gen))
        self.assertEqual(self.d.position,1)
    def test_paused_audio_does_not_end_segment(self):
        first=self.d.current()[1]
        self.d.play(first,self.d.generation)
        self.d.pause()
        self.assertFalse(self.d.end(first,self.d.generation))
        self.d.resume()
        self.assertTrue(self.d.end(first,self.d.generation))
    def test_wrong_segment_id_cannot_move_cursor(self):
        self.assertFalse(self.d.play('not-current',self.d.generation))
        self.assertEqual(self.d.position,0)

if __name__=='__main__': unittest.main()
