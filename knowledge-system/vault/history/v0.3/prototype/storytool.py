#!/usr/bin/env python3
"""Restricted Phase-1 story-script compiler proof-of-concept, NOT the Android runtime.
Only deliberately specified baseline directives are implemented. Pure stdlib.
"""
from __future__ import annotations
import argparse
import json
import re
import shlex
import sys
from pathlib import Path
from typing import Any

SPEAKER_RE = re.compile(r'^@(n|c([1-9][0-9]*)([mf])?)(?:\s+(.*))?$')
ID_RE = re.compile(r'^c[1-9][0-9]*$')
SCENE_RE = re.compile(r'^[a-z][a-z0-9_.-]{0,63}$')
VALID_EXPRESSIONS = {'neutral', 'happy', 'sad', 'confused', 'surprised', 'angry', 'sleepy', 'laughing', 'worried'}
SUPPORTED = {'scene', 'bg', 'after', 'before', 'character'}
ABBREVIATIONS = {'dr.', 'gng.', 'gg.', 'mr.', 'mrs.', 'ms.', 'prof.', 'atbp.', 'etc.', 'hal.', 'blg.', 'st.'}

def diag(code: str, severity: str, line: int, message: str) -> dict[str, Any]:
    return {'code': code, 'severity': severity, 'line': line, 'message': message}

def segment(text: str, cap: int = 220) -> list[str]:
    """Conservative prototype splitter; not a production Filipino sentence segmenter.
    It avoids some decimal/abbreviation boundaries; preserve full normalized content.
    """
    if cap < 20:
        raise ValueError('cap below prototype minimum')
    text = ' '.join(text.split())
    if not text:
        return []
    bounds = []
    for i, ch in enumerate(text):
        if ch not in '.!?':
            continue
        nxt = i + 1
        if nxt >= len(text):
            bounds.append(nxt)
            continue
        if text[nxt] not in ' \t“”"\'':
            continue
        if ch == '.' and i > 0:
            if i + 1 < len(text) and text[i-1].isdigit() and text[i+1].isdigit():
                continue
            prefix = text[:i+1]
            word = prefix.split()[-1].lower()
            if word in ABBREVIATIONS or re.match(r'^[A-Z]\.$', prefix.split()[-1]):
                continue
            if text[i+1:i+2] == '.':
                continue
        bounds.append(nxt)
    sentences = []
    start = 0
    for end in bounds:
        if end > start:
            s = text[start:end].strip()
            if s:
                sentences.append(s)
        start = end
    if start < len(text):
        s = text[start:].strip()
        if s:
            sentences.append(s)
    # Force finite chunks by word boundary only when needed; never drop tokens.
    results = []
    for sentence in sentences:
        if len(sentence) <= cap:
            results.append(sentence)
            continue
        current = ''
        for word in sentence.split(' '):
            if len(word) > cap:
                if current:
                    results.append(current)
                    current = ''
                results.extend(word[i:i+cap] for i in range(0, len(word), cap))
                continue
            candidate = (current + ' ' + word).strip()
            if len(candidate) > cap:
                if current:
                    results.append(current)
                current = word
            else:
                current = candidate
        if current:
            results.append(current)
    return results

def compile_story(source: str, mode: str = 'script') -> dict[str, Any]:
    if mode not in ('script','txt'):
        raise ValueError('mode must be script or txt')
    if '\x00' in source:
        return {'format': 'story-ir-prototype/0.3', 'characters': [], 'scenes': [], 'diagnostics': [diag('SEC001','error',1,'NUL characters not permitted')]}
    source = source.lstrip('\ufeff').replace('\r\n','\n').replace('\r','\n')
    characters: dict[str, dict] = {'n': {'id': 'n', 'name': 'Narrator', 'sex': None, 'implicit': True}}
    scenes: list[dict] = []
    diags: list[dict] = []
    scene: dict | None = None
    active_speaker = 'n'
    prose_buffer: list[str] = []
    buffer_start = 1
    pending_before: list[dict] = []
    last_block: dict | None = None
    current_scene_id_counter = 0

    def ensure_scene() -> dict:
        nonlocal scene, current_scene_id_counter
        if scene is None:
            current_scene_id_counter += 1
            scene = {'id':'default', 'instance': current_scene_id_counter, 'background': 'bg.neutral', 'blocks': [], 'cues': []}
            scenes.append(scene)
        return scene

    def add_character(sid: str, sex: str | None, line: int) -> None:
        if sid == 'n':
            return
        if sid not in characters:
            characters[sid] = {'id': sid, 'name': sid, 'sex': sex, 'implicit': True}
        elif sex:
            old = characters[sid]['sex']
            if old and old != sex:
                diags.append(diag('CHAR002','warning',line,f'Conflicting sex metadata for {sid}; preserving first declaration {old}'))
            elif not old:
                characters[sid]['sex'] = sex

    def flush() -> None:
        nonlocal prose_buffer, pending_before, last_block
        if not prose_buffer:
            return
        target = ensure_scene()
        text = '\n'.join(prose_buffer)
        normal = ' '.join(text.split())
        if not normal:
            prose_buffer = []
            return
        block_id = f'b{sum(len(s["blocks"]) for s in scenes)+1:05d}'
        block = {'id':block_id, 'speaker':active_speaker, 'text':text, 'spoken_text':normal,
                 'line':buffer_start, 'segments':[{'id':f'{block_id}.u{i+1}', 'text':s} for i,s in enumerate(segment(normal))]}
        target['blocks'].append(block)
        for cue in pending_before:
            cue['anchor'] = {'block': block_id, 'edge': 'start'}
            target['cues'].append(cue)
        pending_before = []
        last_block = block
        prose_buffer = []

    if mode == 'txt':
        prose = source.strip()
        if prose:
            active_speaker = 'n'
            buffer_start = 1
            # blank-line paragraphs as distinct utterance blocks
            for par in re.split(r'\n\s*\n', prose):
                prose_buffer = par.split('\n')
                flush()
        if not scenes or not any(s['blocks'] for s in scenes):
            diags.append(diag('STORY001','error',1,'Story has no spoken text'))
        return {'format':'story-ir-prototype/0.3', 'characters': list(characters.values()), 'scenes':scenes, 'diagnostics':diags}

    for line_number, line in enumerate(source.split('\n'), start=1):
        stripped = line.strip()
        if not stripped:
            flush()
            continue
        if stripped.startswith('@@'):
            if not prose_buffer:
                buffer_start = line_number
            prose_buffer.append(line.replace('@@','@',1))
            continue
        if stripped.startswith('@'):
            # Speaker markers: @c1m / @c1 / @n, optionally followed by dialogue.
            m = SPEAKER_RE.fullmatch(stripped)
            if m:
                flush()
                raw = m.group(1)
                active_speaker = 'n' if raw == 'n' else f'c{m.group(2)}'
                add_character(active_speaker, m.group(3), line_number)
                inline = m.group(4)
                if inline:
                    buffer_start = line_number
                    prose_buffer = [inline]
                    flush()
                continue
            flush()
            directive = stripped[1:].split(None,1)[0]
            rest = stripped[1+len(directive):].strip()
            if directive not in SUPPORTED:
                diags.append(diag('SYNTAX001','error',line_number,f'Unknown directive @{directive}; use @@ to speak a literal @'))
                continue
            if directive == 'scene':
                if pending_before:
                    diags.append(diag('CUE004','error',line_number,'@before cannot cross a scene boundary'))
                    pending_before.clear()
                if not SCENE_RE.fullmatch(rest):
                    diags.append(diag('SCENE001','error',line_number,'@scene expects a simple ID'))
                    continue
                current_scene_id_counter += 1
                scene = {'id':rest,'instance':current_scene_id_counter,'background':'bg.neutral','blocks':[],'cues':[]}
                scenes.append(scene)
                last_block = None
            elif directive == 'bg':
                if not SCENE_RE.fullmatch(rest):
                    diags.append(diag('ASSET001','error',line_number,'@bg expects one logical background ID'))
                else:
                    target = ensure_scene()
                    if target['blocks']:
                        diags.append(diag('SCENE002','error',line_number,'@bg after dialogue would retroactively change scene; place it before first speech block or open a new scene'))
                    else:
                        # Background source logical ID; does not imply real artwork exists.
                        target['background'] = rest if rest.startswith('bg.') else f'bg.{rest}'
            elif directive == 'character':
                try:
                    tokens = shlex.split(rest)
                    cid = tokens[0]
                    kv = dict(item.split('=',1) for item in tokens[1:])
                    if not ID_RE.fullmatch(cid) or not set(kv).issubset({'name','sex'}) or any(not s for s in kv.values()):
                        raise ValueError
                    sex = kv.get('sex')
                    if sex not in (None,'m','f'):
                        raise ValueError
                    add_character(cid, sex, line_number)
                    if 'name' in kv:
                        characters[cid]['name'] = kv['name']
                        characters[cid]['implicit'] = False
                except (ValueError, IndexError):
                    diags.append(diag('CHAR001','error',line_number,'@character expects cN name="..." [sex=m|f]'))
            elif directive in ('after','before'):
                try:
                    tokens = shlex.split(rest)
                    if len(tokens) != 2 or not ID_RE.fullmatch(tokens[0]):
                        raise ValueError
                    kv = tokens[1].split('=',1)
                    if len(kv)!=2 or kv[0]!='expression' or kv[1] not in VALID_EXPRESSIONS:
                        raise ValueError
                    cid, expr = tokens[0], kv[1]
                    add_character(cid, None, line_number)
                    cue = {'id': f'cue.l{line_number}', 'action':'expression', 'character':cid,'value':expr,'line':line_number}
                    if directive=='after':
                        if last_block is None:
                            diags.append(diag('CUE001','error',line_number,'@after has no prior speech block in scene'))
                        else:
                            cue['anchor']={'block':last_block['id'],'edge':'end'}
                            ensure_scene()['cues'].append(cue)
                    else:
                        pending_before.append(cue)
                except (ValueError, IndexError):
                    diags.append(diag('CUE002','error',line_number,f'@{directive} expects cN expression=known_expression'))
            continue
        if not prose_buffer:
            buffer_start = line_number
        prose_buffer.append(line)
    flush()
    if pending_before:
        for cue in pending_before:
            diags.append(diag('CUE003','error',cue['line'],'@before never found following speech block'))
    if not any(s['blocks'] for s in scenes):
        diags.append(diag('STORY001','error',1,'Story has no spoken text'))
    return {'format':'story-ir-prototype/0.3', 'characters':list(characters.values()), 'scenes':scenes, 'diagnostics':diags}


def main() -> int:
    cli = argparse.ArgumentParser(description=__doc__)
    cli.add_argument('path',type=Path)
    cli.add_argument('--mode',choices=('auto','txt','script'),default='auto')
    cli.add_argument('--out',type=Path)
    args=cli.parse_args()
    try:
        body=args.path.read_text(encoding='utf-8')
        mode = 'txt' if (args.mode=='auto' and args.path.suffix.lower()=='.txt') else ('script' if args.mode=='auto' else args.mode)
        result=compile_story(body,mode=mode)
        payload=json.dumps(result,ensure_ascii=False,indent=2)+'\n'
        if args.out:
            args.out.write_text(payload,encoding='utf-8')
        else:
            print(payload,end='')
        return 1 if any(d['severity']=='error' for d in result['diagnostics']) else 0
    except (OSError,UnicodeDecodeError) as exc:
        print(f'Unable to read file: {exc}',file=sys.stderr)
        return 2

if __name__=='__main__':
    raise SystemExit(main())
