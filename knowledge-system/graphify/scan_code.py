#!/usr/bin/env python3
"""VisualBook source Graphify: deterministic Python AST file/function/import map.

This supplements the human-curated architecture graph, it does not replace it.
Code map is observational (committed source only), and never claims Android code exists.
"""
from __future__ import annotations

import argparse
import ast
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SCOPES = [
    "knowledge-system/icm",
    "knowledge-system/graphify",
    "tools",
    "knowledge-system/vault/history/v0.3/prototype",
    "knowledge-system/vault/history/v0.3/tests",
]

def source_files():
    found = {}
    for scope in SCOPES:
        for file in (ROOT / scope).rglob("*.py"):
            if "__pycache__" in file.parts:
                continue
            found[file.relative_to(ROOT).as_posix()] = file
    return sorted(found.items())

def scan():
    nodes, edges, issues = [], [], []
    files = source_files()
    for rel, file in files:
        fid = "file:" + rel
        nodes.append({"id":fid,"kind":"python_file","path":rel,"evidence":"AST parsed"})
        try:
            tree=ast.parse(file.read_text(encoding="utf-8"),filename=rel)
        except (SyntaxError,UnicodeDecodeError) as err:
            issues.append({"file":rel,"message":str(err)})
            continue
        functions={n.name for n in tree.body if isinstance(n,(ast.FunctionDef,ast.AsyncFunctionDef))}
        classes={n.name for n in tree.body if isinstance(n,ast.ClassDef)}
        for statement in tree.body:
            if isinstance(statement,(ast.FunctionDef,ast.AsyncFunctionDef,ast.ClassDef)):
                kind="class" if isinstance(statement,ast.ClassDef) else "function"
                mid=fid+":"+statement.name
                nodes.append({"id":mid,"kind":kind,"path":rel,"line":statement.lineno})
                edges.append({"from":fid,"to":mid,"relation":"defines"})
                if isinstance(statement,(ast.FunctionDef,ast.AsyncFunctionDef)):
                    for call in ast.walk(statement):
                        if isinstance(call,ast.Call) and isinstance(call.func,ast.Name) and call.func.id in functions:
                            edges.append({"from":mid,"to":fid+":"+call.func.id,"relation":"calls"})
            if isinstance(statement,ast.Import):
                for alias in statement.names:
                    edges.append({"from":fid,"to":"external:"+alias.name,"relation":"imports"})
            if isinstance(statement,ast.ImportFrom) and statement.module:
                edges.append({"from":fid,"to":"external:"+statement.module,"relation":"imports"})
    unique_external=sorted({e["to"] for e in edges if e["to"].startswith("external:")})
    for mid in unique_external:
        nodes.append({"id":mid,"kind":"import_reference","name":mid[len("external:"):]})
    edges=sorted({(e["from"],e["to"],e["relation"]) for e in edges})
    nodes.sort(key=lambda n:n["id"])
    return {"schema":"visualbook.graphify.python-ast.v1","scope":SCOPES,
            "nodes":nodes,"edges":[{"from":a,"to":b,"relation":r} for a,b,r in edges],
            "parse_errors":issues}

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument("--focus",default="")
    p.add_argument("--summary",action="store_true")
    p.add_argument("--json",action="store_true")
    args=p.parse_args()
    result=scan()
    if result["parse_errors"]:
        print("AST parse errors:",result["parse_errors"],file=sys.stderr)
        return 1
    focus=args.focus.casefold()
    if focus:
        selected={n["id"] for n in result["nodes"] if focus in n["id"].casefold()}
        result["edges"]=[e for e in result["edges"] if e["from"] in selected or e["to"] in selected]
        endpoints={v for e in result["edges"] for v in (e["from"],e["to"])}
        result["nodes"]=[n for n in result["nodes"] if n["id"] in endpoints or n["id"] in selected]
    if args.summary:
        print(f"Source map: {len(result['nodes'])} nodes, {len(result['edges'])} edges, 0 parse errors")
    else:
        print(json.dumps(result,ensure_ascii=False,indent=2))
    return 0

if __name__=="__main__":
    sys.exit(main())
