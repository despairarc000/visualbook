#!/usr/bin/env python3
"""Graphify: validate, inspect and export explicit VisualBook architecture dependencies."""
from __future__ import annotations
import argparse
import json
import sys
from collections import deque
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA = Path(__file__).with_name("graph.json")
TYPES = {"depends_on","controls","feeds","resolves","documents","tests","blocks"}
STATUSES = {"CONFIRMED","ACCEPTED","PROVISIONAL","OPEN","DEFERRED","HISTORICAL","VERIFIED-TEST"}

def load():
    return json.loads(DATA.read_text(encoding="utf-8"))

def validate(g):
    errors = []
    ids = set()
    for n in g["nodes"]:
        k = n.get("id")
        if not k or k in ids: errors.append("duplicate/empty node: "+str(k))
        ids.add(k)
        if n.get("status") not in STATUSES: errors.append("bad status: "+str(k))
        p = n.get("path","")
        resolved = (ROOT / p).resolve()
        if not p or not resolved.is_relative_to(ROOT) or not resolved.is_file():
            errors.append("missing node file: "+str(k)+" "+str(p))
    seen = set()
    for e in g["edges"]:
        key = (e.get("from"),e.get("to"),e.get("type"))
        if key in seen: errors.append("duplicate edge: "+str(key))
        seen.add(key)
        if e.get("from") not in ids or e.get("to") not in ids:
            errors.append("dangling edge: "+str(key))
        if e.get("from")==e.get("to"): errors.append("self-edge: "+str(key))
        if e.get("type") not in TYPES: errors.append("invalid relation: "+str(key))
    return errors

def neighbors(g, node):
    nodes={n["id"]:n for n in g["nodes"]}
    if node not in nodes: raise ValueError("unknown node "+node)
    lines=[]
    for e in g["edges"]:
        if e["from"]==node or e["to"]==node:
            other=e["to"] if e["from"]==node else e["from"]
            direction="->" if e["from"]==node else "<-"
            lines.append(f"{direction:2} {other:22} [{e['type']}] {nodes[other]['path']}")
    return lines

def impact(g, start):
    ids={n["id"] for n in g["nodes"]}
    if start not in ids: raise ValueError("unknown node "+start)
    reverse={}
    for e in g["edges"]: reverse.setdefault(e["to"],[]).append(e["from"])
    discovered={start}
    q=deque([start])
    while q:
        for dependent in reverse.get(q.popleft(),[]):
            if dependent not in discovered:
                discovered.add(dependent)
                q.append(dependent)
    return sorted(discovered-{start})

def mermaid(g):
    lines=["flowchart TD"]
    for n in g["nodes"]:
        label=n["label"].replace('"',"'").replace("[","(").replace("]",")")
        lines.append('  '+n["id"].replace("-","_")+'["'+label+'"]')
    for e in g["edges"]:
        lines.append("  "+e["from"].replace("-","_")+" -->|"+e["type"]+"| "+e["to"].replace("-","_"))
    return "\n".join(lines)

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument("command",choices=["validate","neighbors","impact","mermaid"])
    p.add_argument("node",nargs="?")
    args=p.parse_args()
    g=load()
    problems=validate(g)
    if problems:
        for issue in problems: print("ERROR:",issue,file=sys.stderr)
        return 1
    if args.command=="validate":
        print(f"Graphify valid: {len(g['nodes'])} nodes, {len(g['edges'])} edges")
    elif args.command=="mermaid":
        print(mermaid(g))
    else:
        if not args.node: p.error("node id required")
        result = neighbors(g,args.node) if args.command=="neighbors" else impact(g,args.node)
        print("\n".join(result) if result else "(none)")
    return 0

if __name__=="__main__":
    sys.exit(main())
