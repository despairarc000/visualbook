#!/usr/bin/env python3
"""Minimal deterministic context router for VisualBook. Python 3.9+; stdlib only."""
from __future__ import annotations
import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CONFIG = Path(__file__).with_name("routes.json")

def load():
    return json.loads(CONFIG.read_text(encoding="utf-8"))

def choose_routes(config, query):
    query = query.casefold().strip()
    if query in config["routes"]:
        return [query]
    scores = []
    for key, item in config["routes"].items():
        score = sum(len(term) for term in item["terms"] if term.casefold() in query)
        if score:
            scores.append((-score, key))
    return [k for _, k in sorted(scores)[:2]] if scores else [config["default"]]

def context(config, query):
    ids = choose_routes(config, query)
    paths = []
    for path in config["base"]:
        if path not in paths: paths.append(path)
    for key in ids:
        for path in config["routes"][key]["read"]:
            if path not in paths: paths.append(path)
    return {"topic": query, "routes": ids, "paths": paths,
            "scope": [config["routes"][k]["scope"] for k in ids]}

def doctor(config):
    issues = []
    required = {"base", "default", "routes"}
    if not required.issubset(config):
        issues.append("Missing root fields: " + str(required - set(config)))
        return issues
    if config["default"] not in config["routes"]:
        issues.append("Default route missing")
    if not isinstance(config["base"], list):
        issues.append("base must be a list")
    all_paths = config.get("base", [])[:]
    for route, item in config["routes"].items():
        if not item.get("terms") or not item.get("read") or not item.get("scope"):
            issues.append("Route incomplete: " + route)
        if len(item.get("read", [])) != len(set(item.get("read", []))):
            issues.append("Duplicate read path in " + route)
        all_paths.extend(item.get("read", []))
    for path in set(all_paths):
        resolved = (ROOT / path).resolve()
        if not resolved.is_relative_to(ROOT) or not resolved.is_file():
            issues.append("Missing or escaping route path: " + path)
    return issues

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="command", required=True)
    route = sub.add_parser("route")
    route.add_argument("task", nargs="+")
    route.add_argument("--json", action="store_true")
    sub.add_parser("topics")
    sub.add_parser("doctor")
    args = parser.parse_args()
    config = load()
    if args.command == "topics":
        for key, item in config["routes"].items():
            print(f"{key}: {item['scope']}")
        return 0
    if args.command == "doctor":
        issues = doctor(config)
        for issue in issues: print("ERROR:", issue, file=sys.stderr)
        print(f"ICM doctor: {len(config['routes'])} routes; {len(issues)} issue(s)")
        return 1 if issues else 0
    info = context(config, " ".join(args.task))
    if args.json:
        print(json.dumps(info, indent=2, ensure_ascii=False))
    else:
        print("VisualBook task:", info["topic"])
        print("Route(s):", ", ".join(info["routes"]))
        for idx, path in enumerate(info["paths"], start=1):
            print(f"{idx:2}. {path}")
        print("Scope:", "; ".join(info["scope"]))
    return 0

if __name__ == "__main__":
    sys.exit(main())
