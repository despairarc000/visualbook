"""Pure fake-audio narrative reducer for Phase-1 model contract experiments.
Does not play audio; intentionally ensures "prepared" is not a narrative trigger.
"""
from __future__ import annotations

class FakeNarrationReducer:
    def __init__(self, story_ir: dict):
        self.blocks = [b for scene in story_ir['scenes'] for b in scene['blocks']]
        self.cues = [cue for scene in story_ir['scenes'] for cue in scene['cues']]
        self.sequence = [(b['id'], seg['id']) for b in self.blocks for seg in b['segments']]
        self.generation = 1
        self.position = 0
        self.state = 'ready' if self.sequence else 'ended'
        self.expressions: dict[str,str] = {}
        self.cue_log: list[str] = []
        self.prepared: set[str] = set()

    def current(self):
        return self.sequence[self.position] if self.position < len(self.sequence) else None

    def _reconstruct(self):
        self.expressions = {}
        if self.position >= len(self.sequence):
            completed_blocks = {b['id'] for b in self.blocks}
        else:
            completed_blocks = {bid for bid,seg in self.sequence[:self.position] if all(
                xbid!=bid for xbid,_ in self.sequence[self.position:])}
        for cue in self.cues:
            anchor = cue.get('anchor',{})
            if anchor.get('edge') == 'end' and anchor.get('block') in completed_blocks and cue['action']=='expression':
                self.expressions[cue['character']] = cue['value']

    def prepare(self, segment_id: str, generation: int) -> bool:
        if generation != self.generation:
            return False
        self.prepared.add(segment_id)
        # Absolutely no story or visual progress here.
        return True

    def play(self, segment_id: str, generation: int) -> bool:
        if generation != self.generation or not self.current() or self.current()[1] != segment_id or self.state not in ('ready','buffering'):
            return False
        self.state = 'playing'
        return True

    def end(self, segment_id: str, generation: int) -> bool:
        if generation != self.generation or self.state != 'playing' or not self.current() or self.current()[1] != segment_id:
            return False
        old_block = self.current()[0]
        self.position += 1
        next_block = self.current()[0] if self.current() else None
        if old_block != next_block:
            for cue in self.cues:
                if cue['anchor']=={'block':old_block,'edge':'end'}:
                    self.cue_log.append(cue['id'])
                    if cue['action']=='expression':
                        self.expressions[cue['character']]=cue['value']
        self.state = 'ready' if self.current() else 'ended'
        return True

    def pause(self):
        if self.state == 'playing':
            self.state='paused'

    def resume(self):
        if self.state == 'paused':
            # Could be resumed at a known audio position in real player.
            self.state='playing'

    def seek_to(self, new_position: int):
        if new_position < 0 or new_position > len(self.sequence):
            raise IndexError(new_position)
        self.generation += 1
        self.position = new_position
        self.state = 'ready' if self.current() else 'ended'
        self.cue_log.clear()  # one-shot log reset; logical scene state rebuilt without emitting SFX
        self._reconstruct()
