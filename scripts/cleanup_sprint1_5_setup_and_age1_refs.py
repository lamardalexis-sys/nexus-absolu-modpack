#!/usr/bin/env python3
"""
cleanup_sprint1_5_setup_and_age1_refs.py — Sprint 1.76 fix.

Two things in one atomic pass (safer than two separate commits):

1. FIX Age 1 broken prereqs from fix_qid_duplicates.py (Sprint 1.75).
   Root cause: the fix script translated in-chain prereqs ONLY on the 21 renamed
   storage keys. It missed 2 OUTSIDE quests whose prereqs point to the old
   qids 132 and 133:

     qid 61 "L'acier"       pre=[132] -> was "Le premier acier" (Age 1 water/steel)
                                         now points to "Factory Multiplicateur" (Mek)  BROKEN
     qid 62 "Le Compose B"  pre=[133] -> was "La Pioche Steelium" (Age 1)
                                         now points to "Logistical Sorter" (Mek)       BROKEN

   Fix: translate their prereqs to the new canonical Age 1 qids (1104 and 1106).

2. DELETE the 5 Sprint 1.5 setup quests that Alexis identified as redundant.
   The Age 1 final reward already gives bed/crafting_table/furnace/chest/iron_pickaxe,
   so re-requiring them in the Age 2 intro is pointless filler.

     qid 101 "Un Lit"                -> delete
     qid 102 "Table de Craft"        -> delete
     qid 103 "Four"                  -> delete
     qid 104 "Premier Coffre"        -> delete
     qid 105 "Pioche de Fer"         -> delete (was the convergence point)

   Migration: qid 2000 "Le Quartz Bleu" had pre=[105]. It now points to qid 100
   "Les Pages de Voss (3/3)" (the final lore checkbox), which was the OTHER
   entry branch of the Sprint 1.5 intro. Lore chain is now the sole prereq.

   Also: the 5 layout entries for qid 101-105 are removed from lines.json
   (Age 2 line 2:10) since they no longer exist in the DB.

Post-cleanup Sprint 1.5 structure (12 quests intro, down from 17):

  97  Wake           (pre=149 from Age 1 exit)
   |
  98  Lore 1/3
   |
  99  Lore 2/3
   |
  100 Lore 3/3
   |
  2000 Certus       (was pre=[105], now pre=[100])
   |
  2001 Grindstone
   |
  2002 Meteor Compass
   |
  2003 First Press  (ANY of 4 AE2 presses)
   |
  2004 V2 in Hand   (pre=[2003, 100])   <- wait, lore 100 is already upstream via 2000
   |                                        so effective prereq is [2003]
  2005 Sculk One
   |
  2006 Grabber
   |
  2007 Crossroads
   |
  106 Premiere Cellule

The script is idempotent: if already applied, each step detects its own
post-fix state and no-ops.
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
AGE1 = ROOT / "quests-source" / "age1.json"
AGE2 = ROOT / "quests-source" / "age2.json"
LINES = ROOT / "quests-source" / "lines.json"


def load(p: Path) -> dict:
    with open(p, encoding="utf-8") as f:
        return json.load(f)


def save(p: Path, data: dict) -> None:
    with open(p, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)


# ---------------------------------------------------------------------------
# Step 1: Fix the 2 Age 1 broken prereqs
# ---------------------------------------------------------------------------

AGE1_PREREQ_FIXES = [
    # (storage_key, old_prereq_value, new_prereq_value, expected_qid, reason)
    ("61:10", 132, 1104, 61, "L'acier -> Le premier acier (renamed from 132)"),
    ("62:10", 133, 1106, 62, "Le Compose B -> La Pioche Steelium (renamed from 133)"),
]


def fix_age1_prereqs(age1: dict) -> int:
    quests = age1["quests"]
    fixed = 0
    already = 0
    for sk, old_p, new_p, expected_qid, reason in AGE1_PREREQ_FIXES:
        entry = quests.get(sk)
        if entry is None:
            raise KeyError(f"[age1] {sk} missing — abort")
        qid = entry.get("questID:3", int(sk.split(":")[0]))
        if qid != expected_qid:
            raise ValueError(f"[age1] {sk} expected qid {expected_qid}, got {qid}")
        pre = entry.get("preRequisites:11", [])
        if old_p in pre:
            new_pre = [new_p if x == old_p else x for x in pre]
            entry["preRequisites:11"] = new_pre
            fixed += 1
            print(f"  [age1] qid {qid} '{reason}': pre {pre} -> {new_pre}")
        elif new_p in pre:
            already += 1
            print(f"  [age1] qid {qid} already fixed (idempotent)")
        else:
            raise RuntimeError(
                f"[age1] qid {qid} pre={pre} does not contain {old_p} or {new_p} — manual inspection needed"
            )
    return fixed + already


# ---------------------------------------------------------------------------
# Step 2: Delete 5 Sprint 1.5 setup quests
# ---------------------------------------------------------------------------

SETUP_QUESTS_TO_DELETE = [
    # (storage_key, expected_qid, name)
    ("153:10", 101, "Un Lit"),
    ("154:10", 102, "Table de Craft"),
    ("155:10", 103, "Four"),
    ("156:10", 104, "Premier Coffre"),
    ("157:10", 105, "Pioche de Fer"),
]

# qid 2000 "Le Quartz Bleu" had pre=[105]. Re-point to qid 100 "Les Pages de Voss (3/3)".
CERTUS_PREREQ_MIGRATION = {
    "sk": "229:10",
    "expected_qid": 2000,
    "old_pre": [105],
    "new_pre": [100],
}


def delete_setup_quests(age2: dict) -> int:
    quests = age2["quests"]
    deleted = 0
    already_deleted = 0
    for sk, expected_qid, name in SETUP_QUESTS_TO_DELETE:
        if sk in quests:
            cur_qid = quests[sk].get("questID:3", int(sk.split(":")[0]))
            if cur_qid != expected_qid:
                raise ValueError(
                    f"[age2] {sk} expected qid {expected_qid}, got {cur_qid} — abort"
                )
            del quests[sk]
            deleted += 1
            print(f"  [age2] deleted {sk} qid {expected_qid} '{name}'")
        else:
            already_deleted += 1
            print(f"  [age2] {sk} qid {expected_qid} already deleted (idempotent)")
    return deleted + already_deleted


def migrate_certus_prereq(age2: dict) -> None:
    quests = age2["quests"]
    sk = CERTUS_PREREQ_MIGRATION["sk"]
    expected_qid = CERTUS_PREREQ_MIGRATION["expected_qid"]
    old_pre = CERTUS_PREREQ_MIGRATION["old_pre"]
    new_pre = CERTUS_PREREQ_MIGRATION["new_pre"]

    entry = quests.get(sk)
    if entry is None:
        raise KeyError(f"[age2] {sk} missing — abort")
    cur_qid = entry.get("questID:3", int(sk.split(":")[0]))
    if cur_qid != expected_qid:
        raise ValueError(f"[age2] {sk} expected qid {expected_qid}, got {cur_qid}")

    cur_pre = entry.get("preRequisites:11", [])
    if cur_pre == old_pre:
        entry["preRequisites:11"] = new_pre
        print(f"  [age2] qid {expected_qid} Certus: pre {old_pre} -> {new_pre}")
    elif cur_pre == new_pre:
        print(f"  [age2] qid {expected_qid} Certus prereq already migrated (idempotent)")
    else:
        raise RuntimeError(
            f"[age2] qid {expected_qid} pre={cur_pre} is neither old nor new — manual check"
        )


# ---------------------------------------------------------------------------
# Step 3: Clean lines.json Age 2 layout entries for deleted quests
# ---------------------------------------------------------------------------


def clean_layout(lines_data: dict) -> int:
    age2_line = lines_data["questLines:9"]["2:10"]
    layout = age2_line["quests:9"]

    deleted_qids = {101, 102, 103, 104, 105}
    keys_to_remove = []
    for lk, lentry in layout.items():
        if lentry.get("id:3") in deleted_qids:
            keys_to_remove.append(lk)

    for lk in keys_to_remove:
        qid = layout[lk]["id:3"]
        del layout[lk]
        print(f"  [lines] removed Age 2 layout entry {lk} (qid {qid})")

    return len(keys_to_remove)


# ---------------------------------------------------------------------------
# Apply
# ---------------------------------------------------------------------------


def apply() -> None:
    age1 = load(AGE1)
    age2 = load(AGE2)
    lines_data = load(LINES)

    print("--- Step 1: fix Age 1 broken prereqs from Sprint 1.75 fix ---")
    n = fix_age1_prereqs(age1)
    print(f"  ({n} entries touched)")

    print("--- Step 2: delete 5 Sprint 1.5 setup quests ---")
    n = delete_setup_quests(age2)
    print(f"  ({n} entries deleted)")

    print("--- Step 3: migrate qid 2000 Certus prereq ---")
    migrate_certus_prereq(age2)

    print("--- Step 4: clean Age 2 line layout entries ---")
    n = clean_layout(lines_data)
    print(f"  ({n} layout entries removed)")

    save(AGE1, age1)
    save(AGE2, age2)
    save(LINES, lines_data)
    print("--- wrote age1.json, age2.json, lines.json ---")


if __name__ == "__main__":
    apply()
