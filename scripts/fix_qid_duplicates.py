#!/usr/bin/env python3
"""
fix_qid_duplicates.py — one-shot cleanup for 19 duplicated questIDs.

Pre-existing tech debt: the Mekanism chain (Age 2 post-Croisee) and the Coding
chain were both authored reusing questIDs already taken by the Age 1 water/steel
chain (127-142) and the Age 2 Testament chain (151-156), respectively.

BetterQuesting loads only the first entry it encounters for each questID, so
half of the duplicated content has been ghosted (invisible in the in-game quest
book) since it was authored. This script renames the "losing" storage keys to
fresh qids in their documented range (per quests-source/_meta.json):

  Group 1 (Age 1 water chain)  -> 1100-1114  (Mekanism keeps 127-142)
  Group 2 (Coding chain)       -> 3000-3005  (Testament keeps 151-156)

Alexis picked which side is canonical (post-playtest). Mekanism is critical for
Age 2 progression. Testament is the Age 3 unlock gate. Both stay at their
original qids.

What the script does:
  1. Loads quests-source/age1.json, age2.json, and lines.json
  2. For each losing storage key:
       - Sets its questID:3 to the new qid (explicit, overriding any prior default)
       - Translates its preRequisites:11 using the in-chain prereq map
  3. Updates lines.json layout entries:
       - Age 1 line: losing-group-1 layout entries repointed to new qids
       - Coding line: losing-group-2 layout entries repointed to new qids
  4. Writes both files back

The script is idempotent: if it detects any losing sk already has its new qid,
it aborts cleanly (so re-running doesn't double-migrate).

The storage keys themselves are NOT renamed. Only the questID:3 inside each
entry changes. This means the age1.json bucket organization stays intact, and
merge_quests.py's partition-by-first-line rule is not affected.

After running:
  py scripts/merge_quests.py --check   # must pass
  py scripts/merge_quests.py           # writes DefaultQuests.json
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
AGE1 = ROOT / "quests-source" / "age1.json"
AGE2 = ROOT / "quests-source" / "age2.json"
LINES = ROOT / "quests-source" / "lines.json"


# ---------------------------------------------------------------------------
# Rename tables
# ---------------------------------------------------------------------------

# Each entry: (storage_key, old_qid, new_qid, bucket, short_name)
# short_name is only used for logging; the actual match is by (sk, old_qid).
GROUP1 = [
    ("127:10", 127, 1100, "age1", "La source d'eau"),
    ("128:10", 128, 1101, "age1", "Les veines d'eau"),
    ("126:10", 130, 1102, "age1", "Les pioches rares"),
    ("129:10", 131, 1103, "age1", "La fonderie d'alliages"),
    ("130:10", 132, 1104, "age1", "Le premier acier"),
    ("132:10", 132, 1105, "age1", "Le haut-fourneau"),
    ("131:10", 133, 1106, "age1", "La Pioche Steelium"),
    ("133:10", 133, 1107, "age1", "Le creuset d'alliages"),
    ("136:10", 136, 1108, "age1", "Le Condenseur Tier 2"),
    ("137:10", 137, 1109, "age1", "La Deuxieme Fissure"),
    ("138:10", 138, 1110, "age1", "j'ai merde . . ."),
    ("113:10", 139, 1111, "age1", "la cle c'est la reussite"),
    ("139:10", 140, 1112, "age1", "la chimie du vivant"),
    ("140:10", 141, 1113, "age1", "le plastique du Dr. Voss"),
    ("141:10", 142, 1114, "age1", "le catalyseur ultime"),
]

GROUP2 = [
    ("217:10", 151, 3000, "age2", "Turtle qui mine"),
    ("218:10", 152, 3001, "age2", "OC Computer Case"),
    ("219:10", 153, 3002, "age2", "OpenOS Floppy"),
    ("220:10", 154, 3003, "age2", "component API"),
    ("221:10", 155, 3004, "age2", "Component Proxy"),
    ("222:10", 156, 3005, "age2", "Events et signaux"),
]

# Prereq translation tables — applied ONLY to the preRequisites:11 of the
# storage keys in the losing set (not to the rest of the DB, because the
# rest of the DB's prereqs on these qids resolved to the *winning* entries
# all along, thanks to BQ's first-match rule).
#
# Internal duplicates at qid 132 and 133 are NOT in the map — no losing quest
# inside Group 1 has them as a prereq (verified by hand inspection of the chain).
GROUP1_PREREQ_MAP = {
    127: 1100,   # La source d'eau
    131: 1103,   # La fonderie d'alliages
    136: 1108,   # Le Condenseur Tier 2
    137: 1109,   # La Deuxieme Fissure
    138: 1110,   # j'ai merde
    139: 1111,   # la cle c'est la reussite
    140: 1112,   # la chimie du vivant
    141: 1113,   # le plastique du Dr. Voss
}

GROUP2_PREREQ_MAP = {
    152: 3001,   # OC Computer Case
    153: 3002,   # OpenOS Floppy
    154: 3003,   # component API
    155: 3004,   # Component Proxy
}


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------


def load(path: Path) -> dict:
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def save(path: Path, data: dict) -> None:
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)


def effective_qid(sk: str, entry: dict) -> int:
    return entry.get("questID:3", int(sk.split(":", 1)[0]))


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------


def apply() -> None:
    age1 = load(AGE1)
    age2 = load(AGE2)
    lines = load(LINES)

    buckets = {"age1": age1["quests"], "age2": age2["quests"]}

    all_renames = GROUP1 + GROUP2
    prereq_map = {**GROUP1_PREREQ_MAP, **GROUP2_PREREQ_MAP}

    # --- Idempotency check: if ANY losing sk is already at its new qid, abort ---
    already_migrated = []
    for sk, old_qid, new_qid, bucket, name in all_renames:
        entry = buckets[bucket].get(sk)
        if entry is None:
            raise KeyError(f"{sk} missing from {bucket}.json — abort")
        cur_qid = effective_qid(sk, entry)
        if cur_qid == new_qid:
            already_migrated.append((sk, name, new_qid))
        elif cur_qid != old_qid:
            raise ValueError(
                f"{sk} unexpected qid={cur_qid} (expected {old_qid}) — abort, manual inspection needed"
            )

    if already_migrated:
        if len(already_migrated) == len(all_renames):
            print(f"[fix] all {len(already_migrated)} renames already applied — no-op")
            return
        raise RuntimeError(
            f"partial migration detected: {len(already_migrated)}/{len(all_renames)} "
            f"already renamed. Manual cleanup required."
        )

    # --- Step 1: rename questID:3 and translate prereqs for each losing sk ---
    renamed = 0
    for sk, old_qid, new_qid, bucket, name in all_renames:
        entry = buckets[bucket][sk]

        # Build a new dict preserving key order (questID:3 first per BQ convention)
        new_entry = {"questID:3": new_qid}
        # Copy remaining keys in their original order (skip existing questID:3)
        for k, v in entry.items():
            if k == "questID:3":
                continue
            if k == "preRequisites:11":
                # Translate in-chain prereqs
                new_prereqs = [prereq_map.get(p, p) for p in v]
                new_entry[k] = new_prereqs
            else:
                new_entry[k] = v

        buckets[bucket][sk] = new_entry
        renamed += 1
        print(f"  [fix] {sk} qid {old_qid} -> {new_qid}  ({name})")

    print(f"[fix] renamed {renamed} entries in buckets")

    # --- Step 2: update lines.json layouts ---
    line1 = lines["questLines:9"]["1:10"]  # Age 1 line
    line3 = lines["questLines:9"]["3:10"]  # Coding line

    # Build old_qid -> new_qid for each group for faster layout lookup.
    # For the internal Age 1 dupes (qid 132/133), we pick the FIRST occurrence
    # from the GROUP1 list, which corresponds to the Age 1 line's single layout
    # entry (Le premier acier / La Pioche Steelium — the ones BQ was actually
    # showing). The other occurrence (Le haut-fourneau / Le creuset) becomes
    # an orphan with no layout entry, matching the pre-fix in-game state where
    # they were already ghosted.
    group1_layout_map: dict[int, int] = {}
    for _sk, old_qid, new_qid, _b, _n in GROUP1:
        if old_qid not in group1_layout_map:
            group1_layout_map[old_qid] = new_qid

    group2_layout_map = {old: new for _sk, old, new, _b, _n in GROUP2}

    line1_updated = 0
    for lk, lentry in line1["quests:9"].items():
        qid = lentry.get("id:3")
        if qid in group1_layout_map:
            lentry["id:3"] = group1_layout_map[qid]
            line1_updated += 1
    print(f"[fix] Age 1 line layout: {line1_updated} entries repointed")

    line3_updated = 0
    for lk, lentry in line3["quests:9"].items():
        qid = lentry.get("id:3")
        if qid in group2_layout_map:
            lentry["id:3"] = group2_layout_map[qid]
            line3_updated += 1
    print(f"[fix] Coding line layout: {line3_updated} entries repointed")

    # --- Step 3: save ---
    save(AGE1, age1)
    save(AGE2, age2)
    save(LINES, lines)
    print(f"[fix] wrote age1.json, age2.json, lines.json")

    # --- Summary of orphans ---
    orphaned = []
    # The internal dupes (Le haut-fourneau at new qid 1105, Le creuset at 1107)
    # have no layout entry. They exist in the DB but are invisible to BQ.
    for sk, old_qid, new_qid, bucket, name in GROUP1:
        if old_qid in (132, 133) and new_qid not in (group1_layout_map.get(132), group1_layout_map.get(133)):
            orphaned.append((new_qid, name))
    if orphaned:
        print(f"[fix] orphaned (in DB, no layout — same visibility as before the fix):")
        for nq, nm in orphaned:
            print(f"       qid {nq}: {nm}")


if __name__ == "__main__":
    apply()
