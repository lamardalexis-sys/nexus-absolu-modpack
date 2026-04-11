#!/usr/bin/env python3
"""
merge_quests.py — Split & merge BetterQuesting DefaultQuests.json.

Default mode (merge):  combine quests-source/*.json -> config/betterquesting/DefaultQuests.json
--split                one-time migration: DefaultQuests.json -> quests-source/*
--check                validate sources, do not write output

Source layout:
    quests-source/
        _meta.json     format version + id_ranges convention (doc only)
        lines.json     full questLines:9 block (copied verbatim)
        age0.json      partition of questDatabase:9 — Age 0 (L'Eveil)
        age1.json      Age 1 (La Mecanique Brute)
        age2.json      Age 2 (Le Paradoxe Organique)
        coding.json    Coding line (OC & CC:T)

Each age file has shape { "quests": { "<storage_key>:10": { ...raw BQ entry... }, ... } }.

Partition rule (split): each DB entry is assigned to the first questLine that
references its effective questID (from questID:3, defaulting to the storage key
numeric prefix if the field is absent). Orphans with no line reference fall
back to storage-index ranges 0-46 / 47-96 / 97-142 / 143+ -> age0 / age1 / age2 / coding.

Symbolic prereqs: a quest entry may add a "_symbolic" field like "age2:condenseur_t2_built".
Another quest may then declare preRequisites:11 entries as {"$ref": "age2:condenseur_t2_built"}
which the merger resolves to the referenced numeric questID. The "_symbolic" marker and the
$ref dicts are stripped from the final output.

ID range convention (documented in _meta.json, not enforced on legacy quests):
    Age 0: [0..999], Age 1: [1000..1999], Age 2: [2000..2999], Coding: [3000..3999]
Kept deterministic so future quests land in predictable windows without disturbing
existing save-game quest progress (existing numeric questIDs are preserved as-is).

Round-trip guarantee: after --split, a plain merge produces a byte-identical
DefaultQuests.json (Windows text mode handles CRLF so json.dump matches the original).
"""

from __future__ import annotations

import argparse
import json
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC_DIR = ROOT / "quests-source"
DEST = ROOT / "config" / "betterquesting" / "DefaultQuests.json"
BACKUP = DEST.with_suffix(".json.bak")

AGE_FILES = ["age0.json", "age1.json", "age2.json", "coding.json"]
LINE_INDEX_TO_FILE = {0: "age0.json", 1: "age1.json", 2: "age2.json", 3: "coding.json"}

# Fallback buckets for DB entries that no questLine references (orphans).
# Ranges are inclusive on the storage-key numeric prefix.
ORPHAN_RANGES = [
    (0, 46, "age0.json"),
    (47, 96, "age1.json"),
    (97, 142, "age2.json"),
    (143, 10_000, "coding.json"),
]

DEFAULT_META = {
    "format": "2.0.0",
    "id_ranges": {
        "age0": [0, 999],
        "age1": [1000, 1999],
        "age2": [2000, 2999],
        "coding": [3000, 3999],
    },
    "_comment": (
        "id_ranges is a convention for NEW quests only. Existing numeric questIDs are "
        "preserved verbatim to keep save-game quest progress intact. The merge script "
        "does not enforce the ranges; use them as a guide when authoring new entries."
    ),
}


# ---------------------------------------------------------------------------
# I/O helpers
# ---------------------------------------------------------------------------


def load_json(path: Path) -> dict:
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def save_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    # Text mode: on Windows, \n is translated to \r\n automatically, matching
    # the original DefaultQuests.json byte layout.
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)


def storage_key_num(key: str) -> int:
    return int(key.split(":", 1)[0])


def effective_qid(storage_key: str, quest: dict) -> int:
    """Return the quest's effective BetterQuesting questID."""
    qid = quest.get("questID:3")
    return qid if qid is not None else storage_key_num(storage_key)


# ---------------------------------------------------------------------------
# Symbolic prereq resolution
# ---------------------------------------------------------------------------


def build_symbol_map(age_buckets: dict[str, dict]) -> dict[str, int]:
    """Map "_symbolic" name -> effective questID. Raises on duplicates."""
    sym: dict[str, int] = {}
    for fname, quests in age_buckets.items():
        for storage_key, quest in quests.items():
            name = quest.get("_symbolic")
            if not name:
                continue
            if name in sym:
                raise ValueError(
                    f"duplicate _symbolic name {name!r} (seen again in {fname}/{storage_key})"
                )
            sym[name] = effective_qid(storage_key, quest)
    return sym


def resolve_prereqs(prereqs: list, sym: dict[str, int], origin: str) -> list:
    """Resolve any {"$ref": "<name>"} entries to numeric questIDs."""
    out: list = []
    for p in prereqs:
        if isinstance(p, dict) and "$ref" in p:
            ref = p["$ref"]
            if ref not in sym:
                raise ValueError(f"{origin}: unknown symbolic prereq {ref!r}")
            out.append(sym[ref])
        else:
            out.append(p)
    return out


# ---------------------------------------------------------------------------
# Merge
# ---------------------------------------------------------------------------


def _load_sources() -> tuple[dict, dict, dict[str, dict]]:
    if not SRC_DIR.exists():
        raise FileNotFoundError(
            f"source directory {SRC_DIR} does not exist — run with --split first"
        )
    meta = load_json(SRC_DIR / "_meta.json")
    lines_wrap = load_json(SRC_DIR / "lines.json")
    lines = lines_wrap.get("questLines:9", lines_wrap)

    buckets: dict[str, dict] = {}
    for fname in AGE_FILES:
        fpath = SRC_DIR / fname
        if not fpath.exists():
            buckets[fname] = {}
            continue
        data = load_json(fpath)
        if "quests" not in data:
            raise ValueError(f"{fname}: missing top-level 'quests' key")
        buckets[fname] = data["quests"]
    return meta, lines, buckets


def _combine_quest_db(buckets: dict[str, dict], sym: dict[str, int]) -> dict:
    """Combine all age buckets into a single questDatabase dict.

    Enforces unique storage keys, resolves symbolic prereqs, strips _symbolic markers,
    and returns entries sorted by numeric storage-key prefix to match the original
    file layout.
    """
    combined: dict[str, tuple[str, dict]] = {}  # storage_key -> (origin_file, quest)
    for fname, quests in buckets.items():
        for storage_key, quest in quests.items():
            if storage_key in combined:
                prior = combined[storage_key][0]
                raise ValueError(
                    f"duplicate storage key {storage_key!r}: seen in both {prior} and {fname}"
                )
            cleaned = {k: v for k, v in quest.items() if k != "_symbolic"}
            if "preRequisites:11" in cleaned:
                cleaned["preRequisites:11"] = resolve_prereqs(
                    cleaned["preRequisites:11"], sym, origin=f"{fname}/{storage_key}"
                )
            combined[storage_key] = (fname, cleaned)

    ordered_keys = sorted(combined.keys(), key=storage_key_num)
    return {k: combined[k][1] for k in ordered_keys}


def _validate(quest_db: dict, lines: dict) -> list[str]:
    """Return a list of validation errors (empty == OK)."""
    errors: list[str] = []

    # All effective questIDs present in the combined DB (duplicates allowed: the
    # existing modpack has ~19 duplicate questIDs from historical re-indexing and
    # the merge must preserve that state).
    known_qids: set[int] = set()
    for sk, q in quest_db.items():
        known_qids.add(effective_qid(sk, q))

    # Prereq references must resolve to an existing questID.
    for sk, q in quest_db.items():
        for p in q.get("preRequisites:11", []):
            if not isinstance(p, int):
                errors.append(f"{sk}: unresolved prereq {p!r}")
                continue
            if p not in known_qids:
                errors.append(f"{sk}: broken prereq -> questID {p} missing")

    # Every questLine reference must resolve.
    for lk, line in lines.items():
        for pos_key, q in line.get("quests:9", {}).items():
            qid = q.get("id:3")
            if qid is None:
                errors.append(f"line {lk} pos {pos_key}: missing id:3")
                continue
            if qid not in known_qids:
                errors.append(f"line {lk} pos {pos_key}: references missing questID {qid}")

    return errors


def merge(check_only: bool = False) -> int:
    meta, lines, buckets = _load_sources()
    sym = build_symbol_map(buckets)
    quest_db = _combine_quest_db(buckets, sym)

    errors = _validate(quest_db, lines)
    if errors:
        print(f"[merge_quests] validation failed with {len(errors)} error(s):", file=sys.stderr)
        for e in errors:
            print(f"  - {e}", file=sys.stderr)
        return 1

    if check_only:
        print(f"[merge_quests] OK — {len(quest_db)} quests, {len(lines)} lines, {len(sym)} symbolic refs")
        return 0

    if DEST.exists():
        shutil.copy(DEST, BACKUP)
        print(f"[merge_quests] backup -> {BACKUP.relative_to(ROOT)}")

    output = {
        "format:8": meta.get("format", "2.0.0"),
        "questDatabase:9": quest_db,
        "questLines:9": lines,
    }
    save_json(DEST, output)
    print(
        f"[merge_quests] wrote {DEST.relative_to(ROOT)} "
        f"({len(quest_db)} quests, {len(lines)} lines, {len(sym)} symbolic refs)"
    )
    return 0


# ---------------------------------------------------------------------------
# Split (one-time migration)
# ---------------------------------------------------------------------------


def _assign_orphan(storage_key: str) -> str:
    n = storage_key_num(storage_key)
    for lo, hi, fname in ORPHAN_RANGES:
        if lo <= n <= hi:
            return fname
    return ORPHAN_RANGES[-1][2]


def split(force: bool = False) -> int:
    if not DEST.exists():
        print(f"[merge_quests] {DEST} not found", file=sys.stderr)
        return 1
    existing = [p for p in (SRC_DIR / f for f in AGE_FILES) if p.exists()]
    if existing and not force:
        print(
            f"[merge_quests] quests-source/ already has {len(existing)} age file(s). "
            f"Refusing to overwrite without --force (or delete quests-source/ manually).",
            file=sys.stderr,
        )
        return 1
    data = load_json(DEST)
    db = data["questDatabase:9"]
    lines = data["questLines:9"]

    # questID -> first line index that references it (stable assignment)
    qid_first_line: dict[int, int] = {}
    for line_key, line in lines.items():
        line_idx = storage_key_num(line_key)
        for q in line.get("quests:9", {}).values():
            qid = q.get("id:3")
            if qid is None:
                continue
            if qid not in qid_first_line:
                qid_first_line[qid] = line_idx

    buckets: dict[str, dict] = {fname: {} for fname in AGE_FILES}
    orphan_count = 0
    for storage_key, quest in db.items():
        qid = effective_qid(storage_key, quest)
        line_idx = qid_first_line.get(qid)
        if line_idx is not None and line_idx in LINE_INDEX_TO_FILE:
            bucket = LINE_INDEX_TO_FILE[line_idx]
        else:
            bucket = _assign_orphan(storage_key)
            orphan_count += 1
        buckets[bucket][storage_key] = quest

    SRC_DIR.mkdir(parents=True, exist_ok=True)

    meta = dict(DEFAULT_META)
    meta["format"] = data.get("format:8", DEFAULT_META["format"])
    save_json(SRC_DIR / "_meta.json", meta)
    save_json(SRC_DIR / "lines.json", {"questLines:9": lines})

    for fname, quests in buckets.items():
        sorted_quests = {
            k: quests[k] for k in sorted(quests.keys(), key=storage_key_num)
        }
        save_json(SRC_DIR / fname, {"quests": sorted_quests})

    total = sum(len(b) for b in buckets.values())
    print(f"[merge_quests] split {total} quests into {len(buckets)} files "
          f"({orphan_count} orphans fell back to storage-index ranges):")
    for fname, quests in buckets.items():
        print(f"  {fname}: {len(quests)} quests")
    print(f"  lines.json: {len(lines)} lines")
    return 0


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[1])
    group = ap.add_mutually_exclusive_group()
    group.add_argument("--split", action="store_true",
                       help="migrate DefaultQuests.json -> quests-source/ (one-time)")
    group.add_argument("--check", action="store_true",
                       help="validate sources without writing output")
    ap.add_argument("--force", action="store_true",
                    help="with --split: overwrite existing quests-source/ files")
    args = ap.parse_args()

    if args.split:
        return split(force=args.force)
    return merge(check_only=args.check)


if __name__ == "__main__":
    sys.exit(main())
