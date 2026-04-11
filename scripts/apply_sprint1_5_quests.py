#!/usr/bin/env python3
"""
apply_sprint1_5_quests.py — Sprint 1.5 one-shot migration.

Supersedes Sprint 1's quest design after playtest feedback:
  "trop complexe des le debut, Warden en Q6 c'est pas ca"

Applies the 17-quest tree-structure intro from AGE2_INTRO_V2_DESIGN.md:
  - Rewrites qid 97-105 in-place with the first 9 v2 quests (Welcome + Setup + Iron Pick)
  - Adds 8 NEW quests at qid 2000-2007 for the Certus Chain + Lore Gates
  - Migrates qid 106 "Premiere Cellule" prereq from [105] to [2007]
  - Updates quests-source/lines.json (questLines:9 line 2) with the new tree layout

Design principles (extracted from ATM10 and Compact Claustrophobia):
  - One quest = one thing (retrieval on a single item, no combined actions)
  - No boss fight in the intro (the Warden is moved to a dedicated Deep Dark chapter later)
  - Rewards are tools that teach, not game-breaking skips
  - Branch-and-converge dependency graph, not a linear chain
  - Orientation before progression (4 Welcome checkboxes at the top)

The Java items (Grabber Voss, Badge, Lanterne, Fragment Memoire 1) and the Patchouli
books (Carnet Voss Vol.2 / Vol.3) shipped in v1.0.165 are 100% reused by this script —
only the BQ quest layer is replaced.

Run:
    py scripts/apply_sprint1_5_quests.py
then:
    py scripts/merge_quests.py --check
    py scripts/merge_quests.py
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
AGE2 = ROOT / "quests-source" / "age2.json"
LINES = ROOT / "quests-source" / "lines.json"

# ---------------------------------------------------------------------------
# Mod IDs (same as Sprint 1, loaded from MOD_IDS.txt intent)
# ---------------------------------------------------------------------------
MOD_DEEPER = "stacked_dimensions_warden"
ITEM_SCULK_TENDRIL = f"{MOD_DEEPER}:sculk_tendril"

# AE2 press meta values — unchanged
AE2_PRESS_LOGIC = 15
AE2_PRESS_CALCULATION = 13
AE2_PRESS_ENGINEERING = 14
AE2_PRESS_SILICON = 19


# ---------------------------------------------------------------------------
# BQ format helpers (copied from apply_sprint1_quests.py for self-containment)
# ---------------------------------------------------------------------------


def bq_props(name: str, desc: str, icon: dict, ismain: int = 1,
             snd_complete: str = "minecraft:entity.player.levelup") -> dict:
    return {
        "betterquesting:10": {
            "issilent:1": 0,
            "snd_complete:8": snd_complete,
            "lockedprogress:1": 1,
            "partySingleReward:8": "false",
            "tasklogic:8": "AND",
            "repeattime:3": -1,
            "visibility:8": "ALWAYS",
            "simultaneous:1": 0,
            "globalshare:1": 0,
            "questlogic:8": "AND",
            "partysinglereward:1": 0,
            "snd_update:8": "minecraft:entity.player.levelup",
            "autoclaim:1": 0,
            "ismain:1": ismain,
            "repeat_relative:1": 1,
            "icon:10": icon,
            "name:8": name,
            "desc:8": desc,
        }
    }


def item(id_str: str, count: int = 1, damage: int = 0, oredict: str = "",
         tag: dict | None = None) -> dict:
    out = {
        "id:8": id_str,
        "Count:3": count,
        "Damage:2": damage,
        "OreDict:8": oredict,
    }
    if tag is not None:
        out["tag:10"] = tag
    return out


def patchouli_book(book_id: str) -> dict:
    return item("patchouli:guide_book", count=1, damage=0, oredict="",
                tag={"patchouli:book:8": book_id})


def task_retrieval(index: int, items: list[dict], *, consume: int = 0,
                   ignore_nbt: int = 1, partial_match: int = 1) -> dict:
    required = {f"{i}:10": items[i] for i in range(len(items))}
    return {
        "partialMatch:1": partial_match,
        "autoConsume:1": 0,
        "groupDetect:1": 0,
        "ignoreNBT:1": ignore_nbt,
        "index:3": index,
        "consume:1": consume,
        "ignoreDamage:1": 0,
        "taskID:8": "bq_standard:retrieval",
        "requiredItems:9": required,
    }


def task_checkbox(index: int) -> dict:
    return {
        "index:3": index,
        "taskID:8": "bq_standard:checkbox",
    }


def reward_items(index: int, items: list[dict]) -> dict:
    inner = {f"{i}:10": items[i] for i in range(len(items))}
    return {
        "rewardID:8": "bq_standard:item",
        "index:3": index,
        "rewards:9": [inner],
    }


def reward_command(index: int, command: str,
                   title: str = "bq_standard.reward.command") -> dict:
    return {
        "hideBlockIcon:1": 1,
        "rewardID:8": "bq_standard:command",
        "asScript:1": 1,
        "description:8": "",
        "viaPlayer:1": 0,
        "index:3": index,
        "title:8": title,
        "command:8": command,
    }


def build_quest(qid: int, prereqs: list[int], properties: dict,
                tasks: list[dict], rewards: list[dict]) -> dict:
    return {
        "questID:3": qid,
        "preRequisites:11": prereqs,
        "properties:10": properties,
        "tasks:9": {f"{i}:10": tasks[i] for i in range(len(tasks))},
        "rewards:9": {f"{i}:10": rewards[i] for i in range(len(rewards))},
    }


# ---------------------------------------------------------------------------
# The 17 v2 quests
# ---------------------------------------------------------------------------

# ---- WELCOME (4 checkboxes, tiny rewards, orientation) ----

def q97_wake() -> tuple[str, dict]:
    """qid 97 — Tu es sorti. Orientation post-CM, carnet Vol.2 in reward."""
    desc = (
        "§7§oLa lumiere t'aveugle. Tu es sorti.§r\n\n"
        "§7Trois Ages dans des boites. Tu es enfin dehors.\n"
        "§7Respire. Regarde autour. Tu as le temps.\n\n"
        "§8§o\"Bienvenue dehors, Sujet 47.\n"
        "§8Le confinement est termine. Ce qui suit\n"
        "§8depend de toi.\" — Journal automatise§r\n\n"
        "§e§lObjectif : §7Clique pour confirmer ta sortie\n"
        "§e§lRecompense : §7Carnet de Voss Vol. II + pain"
    )
    icon = patchouli_book("nexusabsolu:carnet_voss_v2")
    props = bq_props("§l§6Tu es sorti", desc, icon, ismain=1)
    tasks = [task_checkbox(0)]
    rewards = [
        reward_items(0, [
            patchouli_book("nexusabsolu:carnet_voss_v2"),
            item("minecraft:bread", count=4),
            item("minecraft:experience_bottle", count=1),
        ]),
        reward_command(
            1,
            "/say §5[Nexus Absolu]§r §7VAR_NAME§r sort enfin de sa boite. L'Age 2 commence.",
        ),
    ]
    return "149:10", build_quest(97, [149], props, tasks, rewards)


def q98_lore_1() -> tuple[str, dict]:
    """qid 98 — Carnet Vol. II, entree Le Poste."""
    desc = (
        "§7Ouvre ton §eCarnet de Voss Vol. II§r§7.\n"
        "§7Lis la premiere entree : §o'Le Poste Voss-7'§r§7.\n\n"
        "§7Ce n'est pas une histoire lointaine. C'est\n"
        "§7la tienne. Voss t'a surveille pendant trois\n"
        "§7ans depuis une station qui n'existe plus.\n\n"
        "§e§lObjectif : §7Lis la section 'Le Poste'\n"
        "§e§lRecompense : §710 XP"
    )
    icon = patchouli_book("nexusabsolu:carnet_voss_v2")
    props = bq_props("§l§6Les Pages de Voss (1/3)", desc, icon, ismain=1)
    tasks = [task_checkbox(0)]
    rewards = [
        reward_items(0, [item("minecraft:experience_bottle", count=1)]),
    ]
    return "150:10", build_quest(98, [97], props, tasks, rewards)


def q99_lore_2() -> tuple[str, dict]:
    """qid 99 — Carnet Vol. II, entree Sujet 46."""
    desc = (
        "§7Deuxieme entree : §o'Sujet 46'§r§7.\n\n"
        "§7Tu n'etais pas le premier. Marcus a ete le\n"
        "§746eme a accepter le protocole. Il est toujours\n"
        "§7la, sous le sol, mais il n'est plus vraiment\n"
        "§7Marcus. §cNe descends pas le voir sans preparation§r§7.\n\n"
        "§e§lObjectif : §7Lis la section 'Sujet 46'\n"
        "§e§lRecompense : §710 XP"
    )
    icon = patchouli_book("nexusabsolu:carnet_voss_v2")
    props = bq_props("§l§6Les Pages de Voss (2/3)", desc, icon, ismain=1)
    tasks = [task_checkbox(0)]
    rewards = [
        reward_items(0, [item("minecraft:experience_bottle", count=1)]),
    ]
    return "151:10", build_quest(99, [98], props, tasks, rewards)


def q100_lore_3() -> tuple[str, dict]:
    """qid 100 — Carnet Vol. II, entree Protocole."""
    desc = (
        "§7Troisieme entree : §o'Protocole Voss-7'§r§7.\n\n"
        "§7Tu es la preuve que le protocole a un sens.\n"
        "§7Continue. Voss te laisse des outils. A toi\n"
        "§7de les trouver.\n\n"
        "§8§o\"Tu n'es pas un sujet. Tu es la suite.\"\n"
        "§8— E. V., derniere entree§r\n\n"
        "§e§lObjectif : §7Lis la section 'Protocole Voss-7'\n"
        "§e§lRecompense : §7Diamant Pickaxe (pour plus tard)"
    )
    icon = patchouli_book("nexusabsolu:carnet_voss_v2")
    props = bq_props("§l§6Les Pages de Voss (3/3)", desc, icon, ismain=1)
    tasks = [task_checkbox(0)]
    rewards = [
        reward_items(0, [
            item("minecraft:diamond_pickaxe", count=1),
            item("minecraft:experience_bottle", count=2),
        ]),
    ]
    return "152:10", build_quest(100, [99], props, tasks, rewards)


# ---- SETUP (4 parallel retrieval quests, all prereq on Q97) ----

def q101_bed() -> tuple[str, dict]:
    desc = (
        "§7Premiere nuit dehors. Il te faut un lit.\n"
        "§7Tu sais deja comment en faire un. Crafte.\n\n"
        "§e§lObjectif : §71x Lit\n"
        "§e§lRecompense : §720 XP"
    )
    icon = item("minecraft:red_bed", count=1)
    props = bq_props("§l§6Un Lit", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item("minecraft:bed", count=1, damage=32767)])]
    rewards = [
        reward_items(0, [item("minecraft:experience_bottle", count=2)]),
    ]
    return "153:10", build_quest(101, [97], props, tasks, rewards)


def q102_crafting_table() -> tuple[str, dict]:
    desc = (
        "§7Le Cube de Bois. Ton meilleur ami.\n"
        "§7Crafte-en un. Par securite.\n\n"
        "§e§lObjectif : §71x Table de Craft\n"
        "§e§lRecompense : §74 planches + 20 XP"
    )
    icon = item("minecraft:crafting_table", count=1)
    props = bq_props("§l§6Table de Craft", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item("minecraft:crafting_table", count=1)])]
    rewards = [
        reward_items(0, [
            item("minecraft:planks", count=4, damage=32767),
            item("minecraft:experience_bottle", count=2),
        ]),
    ]
    return "154:10", build_quest(102, [97], props, tasks, rewards)


def q103_furnace() -> tuple[str, dict]:
    desc = (
        "§7Huit blocs de cobble en cercle. Tu connais.\n"
        "§7Un four, c'est la base de toute civilisation.\n\n"
        "§e§lObjectif : §71x Four\n"
        "§e§lRecompense : §71 charbon + 20 XP"
    )
    icon = item("minecraft:furnace", count=1)
    props = bq_props("§l§6Four", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item("minecraft:furnace", count=1)])]
    rewards = [
        reward_items(0, [
            item("minecraft:coal", count=1),
            item("minecraft:experience_bottle", count=2),
        ]),
    ]
    return "155:10", build_quest(103, [97], props, tasks, rewards)


def q104_chest() -> tuple[str, dict]:
    desc = (
        "§7Un coffre. Pour ne pas tout perdre\n"
        "§7quand tu mourras. Ca arrive.\n\n"
        "§e§lObjectif : §71x Coffre\n"
        "§e§lRecompense : §74 baton + 20 XP"
    )
    icon = item("minecraft:chest", count=1)
    props = bq_props("§l§6Premier Coffre", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item("minecraft:chest", count=1)])]
    rewards = [
        reward_items(0, [
            item("minecraft:stick", count=4),
            item("minecraft:experience_bottle", count=2),
        ]),
    ]
    return "156:10", build_quest(104, [97], props, tasks, rewards)


# ---- CERTUS CHAIN (5 quests, linear, start at Q105 convergence) ----

def q105_iron_pickaxe() -> tuple[str, dict]:
    """qid 105 — Convergence point: requires ALL 4 setup quests."""
    desc = (
        "§7Une pioche de fer. Il te la faut pour\n"
        "§7casser le §bQuartz Bleu§r§7 dans les grottes.\n\n"
        "§e§lObjectif : §71x Pioche de Fer\n"
        "§e§lRecompense : §74 lingots de fer"
    )
    icon = item("minecraft:iron_pickaxe", count=1)
    props = bq_props("§l§6Pioche de Fer", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item("minecraft:iron_pickaxe", count=1, damage=32767)])]
    rewards = [
        reward_items(0, [
            item("minecraft:iron_ingot", count=4),
            item("minecraft:experience_bottle", count=4),
        ]),
    ]
    # Converges all 4 setup quests
    return "157:10", build_quest(105, [101, 102, 103, 104], props, [tasks[0]], rewards)


def q2000_certus() -> tuple[str, dict]:
    desc = (
        "§7Au fond des grottes, des cristaux bleus\n"
        "§7luisent. C'est le §bCertus Quartz§r§7.\n"
        "§7Matiere premiere de l'informatique dimensionnelle.\n\n"
        "§e§lObjectif : §716x Certus Quartz\n"
        "§e§lRecompense : §7Stone + XP"
    )
    icon = item("appliedenergistics2:material", count=1, damage=0)
    props = bq_props("§l§6Le Quartz Bleu", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [
        item("appliedenergistics2:material", count=16, damage=0)
    ])]
    rewards = [
        reward_items(0, [
            item("minecraft:stone", count=4),
            item("minecraft:experience_bottle", count=4),
        ]),
    ]
    return "229:10", build_quest(2000, [105], props, tasks, rewards)


def q2001_grindstone() -> tuple[str, dict]:
    desc = (
        "§7Avant les machines electriques, il y avait\n"
        "§7la §6Quartz Grindstone§r§7. Un broyeur manuel\n"
        "§7qui double tes minerais de base.\n\n"
        "§e§lObjectif : §71x Quartz Grindstone\n"
        "§e§lRecompense : §78 torches + XP"
    )
    icon = item("appliedenergistics2:quartz_grindstone", count=1)
    props = bq_props("§l§6La Pierre Affuteuse", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [
        item("appliedenergistics2:quartz_grindstone", count=1)
    ])]
    rewards = [
        reward_items(0, [
            item("minecraft:torch", count=8),
            item("minecraft:experience_bottle", count=4),
        ]),
    ]
    return "230:10", build_quest(2001, [2000], props, tasks, rewards)


def q2002_meteor_compass() -> tuple[str, dict]:
    desc = (
        "§7Les §ePresses d'Inscription AE2§r§7 tombent\n"
        "§7du ciel dans des meteorites.\n\n"
        "§7La §eBoussole a Meteorite§r§7 pointe vers la plus\n"
        "§7proche. Crafte-la. Suis la boussole. Creuse.\n\n"
        "§e§lObjectif : §71x Boussole a Meteorite\n"
        "§e§lRecompense : §74 pain + XP"
    )
    icon = item("appliedenergistics2:sky_compass", count=1)
    props = bq_props("§l§6Boussole a Meteorite", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [
        item("appliedenergistics2:sky_compass", count=1)
    ])]
    rewards = [
        reward_items(0, [
            item("minecraft:bread", count=4),
            item("minecraft:experience_bottle", count=4),
        ]),
    ]
    return "231:10", build_quest(2002, [2001], props, tasks, rewards)


def q2003_first_press() -> tuple[str, dict]:
    """qid 2003 — Accept ANY of the 4 AE2 presses. Silicon press as reward fallback."""
    desc = (
        "§7Trouve UNE meteorite AE2 et ramasse n'importe\n"
        "§7laquelle des 4 Presses d'Inscription.\n\n"
        "§7§oLogic, Calculation, Engineering, ou Silicon.§r\n"
        "§7Peu importe laquelle — le jeu te donnera la\n"
        "§7Silicon Press en bonus pour completer ton set.\n\n"
        "§e§lObjectif : §71x N'importe quelle AE2 Press\n"
        "§e§lRecompense : §7Silicon Press + 100 XP"
    )
    icon = item("appliedenergistics2:material", count=1, damage=AE2_PRESS_LOGIC)
    props = bq_props("§l§6Premiere Presse", desc, icon, ismain=1)
    # Task: retrieval with multiple required items (ANY match). BQ2 treats
    # multiple requiredItems as AND by default; we use partialMatch:1 to
    # allow partial completion (any ONE item matches).
    tasks = [{
        "partialMatch:1": 1,
        "autoConsume:1": 0,
        "groupDetect:1": 1,  # allow group match
        "ignoreNBT:1": 1,
        "index:3": 0,
        "consume:1": 0,
        "ignoreDamage:1": 0,
        "taskID:8": "bq_standard:retrieval",
        "requiredItems:9": {
            "0:10": item("appliedenergistics2:material", count=1, damage=AE2_PRESS_LOGIC),
            "1:10": item("appliedenergistics2:material", count=1, damage=AE2_PRESS_CALCULATION),
            "2:10": item("appliedenergistics2:material", count=1, damage=AE2_PRESS_ENGINEERING),
            "3:10": item("appliedenergistics2:material", count=1, damage=AE2_PRESS_SILICON),
        },
    }]
    rewards = [
        reward_items(0, [
            item("appliedenergistics2:material", count=1, damage=AE2_PRESS_SILICON),
            item("minecraft:experience_bottle", count=10),
        ]),
    ]
    return "232:10", build_quest(2003, [2002], props, tasks, rewards)


# ---- LORE GATES (4 quests, final run before Crossroads) ----

def q2004_v2_in_hand() -> tuple[str, dict]:
    desc = (
        "§7Tiens ton §eCarnet Vol. II§r§7 en main.\n"
        "§7Tu devrais l'avoir depuis la Q97. Si tu l'as\n"
        "§7perdu, crafte en un autre (ou demande a un admin).\n\n"
        "§e§lObjectif : §71x Carnet Voss Vol. II\n"
        "§e§lRecompense : §72 golden apples"
    )
    icon = patchouli_book("nexusabsolu:carnet_voss_v2")
    props = bq_props("§l§6Le Carnet en Main", desc, icon, ismain=1)
    # NBT-matching retrieval: must be the exact Vol.2 book
    tasks = [task_retrieval(0, [
        patchouli_book("nexusabsolu:carnet_voss_v2")
    ], ignore_nbt=0)]
    rewards = [
        reward_items(0, [
            item("minecraft:golden_apple", count=2),
            item("minecraft:experience_bottle", count=4),
        ]),
    ]
    return "233:10", build_quest(2004, [2003, 100], props, tasks, rewards)


def q2005_sculk_one() -> tuple[str, dict]:
    desc = (
        "§7Pour crafter le §dSac du Sujet 46§r§7, il te faut\n"
        "§7§6un§r§7 seul §bSculk Tendril§r§7 du Deep Dark.\n\n"
        "§cPas besoin de tuer le Warden.§r §7Descends juste\n"
        "§7assez profond pour en trouver un. Retourne en\n"
        "§7haut tout de suite apres.\n\n"
        "§e§lObjectif : §71x Sculk Tendril\n"
        "§e§lRecompense : §7Debloque la recette du Sac"
    )
    icon = item(ITEM_SCULK_TENDRIL, count=1)
    props = bq_props("§l§6Signal Sculk", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item(ITEM_SCULK_TENDRIL, count=1)])]
    rewards = [
        reward_items(0, [
            item("minecraft:torch", count=16),
            item("minecraft:experience_bottle", count=4),
        ]),
        reward_command(1, "/gamestage add @p age2_grabber_recipe"),
    ]
    return "234:10", build_quest(2005, [2004], props, tasks, rewards)


def q2006_grabber() -> tuple[str, dict]:
    desc = (
        "§7Avec ton tendril de sculk, tu peux assembler\n"
        "§7une replique du §dSac du Sujet 46§r§7.\n\n"
        "§8§o\"Le sac de 46 contient ses affaires,\n"
        "§8mais aussi ses souvenirs.\"\n"
        "§8— Carnet Voss Vol. II, p.7§r\n\n"
        "§e§lObjectif : §71x Grabber Voss\n"
        "§e§lRecompense : §7Nourriture + golden apples + XP"
    )
    icon = item("nexusabsolu:grabber_voss", count=1)
    props = bq_props("§l§6Le Sac du Sujet 46", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item("nexusabsolu:grabber_voss", count=1)])]
    rewards = [
        reward_items(0, [
            item("minecraft:bread", count=16),
            item("minecraft:golden_apple", count=2),
            item("minecraft:experience_bottle", count=6),
        ]),
    ]
    return "235:10", build_quest(2006, [2005], props, tasks, rewards)


def q2007_crossroads() -> tuple[str, dict]:
    desc = (
        "§7Tu as ton sac. Tu as tes presses AE2.\n"
        "§7Tu as le Carnet Vol. II. Tu es §lpret§r§7.\n\n"
        "§7Trois voies s'ouvrent devant toi :\n"
        "§7- §bAE2 complet§r§7 (canaux, terminal, autocraft)\n"
        "§7- §aBotania§r§7 (fleurs, mana, manasteel)\n"
        "§7- §5Mystical Agriculture§r§7 (inferium, graines)\n\n"
        "§7Choisis par ou commencer. Tu pourras tout\n"
        "§7faire dans l'ordre que tu veux.\n\n"
        "§e§lObjectif : §7Clique pour confirmer\n"
        "§e§lRecompense : §7Carnet Voss Vol. III + ME Controller"
    )
    icon = patchouli_book("nexusabsolu:carnet_voss_v3")
    props = bq_props("§l§6La Croisee", desc, icon, ismain=1)
    tasks = [task_checkbox(0)]
    rewards = [
        reward_items(0, [
            patchouli_book("nexusabsolu:carnet_voss_v3"),
            item("appliedenergistics2:controller", count=1),
            item("minecraft:experience_bottle", count=16),
        ]),
        reward_command(1, "/gamestage add @p age2_crossroads"),
        reward_command(
            2,
            "/say §5[Nexus Absolu]§r §7VAR_NAME§r atteint la Croisee. Les 3 voies de l'Age 2 sont ouvertes.",
        ),
    ]
    return "236:10", build_quest(2007, [2006], props, tasks, rewards)


# ---------------------------------------------------------------------------
# questLines line 2 layout coordinates for the 17 v2 quests
# ---------------------------------------------------------------------------

# Tree-style layout: wake at top, lore chain on left, setup row right, convergence center
LAYOUT_V2 = {
    # (qid, sizeX, sizeY, x, y, posX, posY)
    97:   (32, 32,    0,   0),   # Wake, center top, bigger
    98:   (24, 24, -180,  50),   # Lore 1 left
    99:   (24, 24, -180, 100),   # Lore 2 left
    100:  (24, 24, -180, 150),   # Lore 3 left
    101:  (24, 24,   60,  50),   # Bed
    102:  (24, 24,  120,  50),   # CT
    103:  (24, 24,  180,  50),   # Furnace
    104:  (24, 24,  240,  50),   # Chest
    105:  (24, 24,  150, 110),   # Iron Pick (converges 101-104)
    2000: (24, 24,  150, 170),   # Certus
    2001: (24, 24,  150, 220),   # Grindstone
    2002: (24, 24,  150, 270),   # Meteor Compass
    2003: (24, 24,  150, 320),   # First Press
    2004: (24, 24,  -30, 370),   # V2 in hand (merges lore+certus)
    2005: (24, 24,  -30, 420),   # Sculk One
    2006: (24, 24,  -30, 470),   # Grabber
    2007: (32, 32,  -30, 530),   # Crossroads, bigger
}


# ---------------------------------------------------------------------------
# Apply
# ---------------------------------------------------------------------------


V2_QUESTS_REWRITE = [
    q97_wake,
    q98_lore_1,
    q99_lore_2,
    q100_lore_3,
    q101_bed,
    q102_crafting_table,
    q103_furnace,
    q104_chest,
    q105_iron_pickaxe,
]

V2_QUESTS_NEW = [
    q2000_certus,
    q2001_grindstone,
    q2002_meteor_compass,
    q2003_first_press,
    q2004_v2_in_hand,
    q2005_sculk_one,
    q2006_grabber,
    q2007_crossroads,
]


def apply() -> None:
    # --- Step 1: update age2.json ---
    with open(AGE2, encoding="utf-8") as f:
        age2_data = json.load(f)
    quests = age2_data["quests"]

    # Rewrite the 9 existing qids (97-105)
    for fn in V2_QUESTS_REWRITE:
        sk, new_q = fn()
        if sk not in quests:
            raise KeyError(f"storage key {sk!r} missing, expected in rewrite pass")
        if quests[sk].get("questID:3") != new_q["questID:3"]:
            raise ValueError(
                f"{sk}: existing qid {quests[sk].get('questID:3')} != new qid {new_q['questID:3']}"
            )
        quests[sk] = new_q

    # Add the 8 new qids (2000-2007) at new storage keys (229:10..236:10)
    for fn in V2_QUESTS_NEW:
        sk, new_q = fn()
        if sk in quests:
            raise KeyError(f"storage key {sk!r} already exists, cannot add new quest")
        quests[sk] = new_q

    # Migrate qid 106 "Premiere Cellule" prereq from [105] to [2007]
    q106 = quests["158:10"]
    if q106.get("questID:3") != 106:
        raise RuntimeError(
            f"158:10 is not qid 106 anymore — aborting (got {q106.get('questID:3')})"
        )
    old_prereq = q106.get("preRequisites:11")
    if old_prereq == [105]:
        q106["preRequisites:11"] = [2007]
        print(f"[s1.5] qid 106 prereq migrated: [105] -> [2007]")
    elif old_prereq == [2007]:
        print(f"[s1.5] qid 106 prereq already migrated (idempotent)")
    else:
        raise RuntimeError(
            f"qid 106 unexpected prereq {old_prereq} — expected [105] or [2007]"
        )

    # Sort storage keys numerically for stable output
    sorted_quests = {
        k: quests[k] for k in sorted(quests.keys(), key=lambda x: int(x.split(":", 1)[0]))
    }
    age2_data["quests"] = sorted_quests

    with open(AGE2, "w", encoding="utf-8") as f:
        json.dump(age2_data, f, indent=2, ensure_ascii=False)
    print(f"[s1.5] Wrote {AGE2.relative_to(ROOT)} ({len(sorted_quests)} quests)")

    # --- Step 2: update lines.json (Age 2 line, index "2:10") ---
    with open(LINES, encoding="utf-8") as f:
        lines_data = json.load(f)
    age2_line = lines_data["questLines:9"]["2:10"]
    layout = age2_line["quests:9"]

    # Build qid -> existing layout_key (so we can update positions in-place)
    qid_to_layout_key = {}
    for lk, lentry in layout.items():
        qid_to_layout_key[lentry["id:3"]] = lk
    # Find next free layout key (numeric)
    next_layout_idx = max(int(k.split(":", 1)[0]) for k in layout.keys()) + 1

    for qid, (sx, sy, x, y) in LAYOUT_V2.items():
        new_entry = {
            "id:3": qid,
            "sizeX:3": sx,
            "sizeY:3": sy,
            "x:3": x,
            "y:3": y,
            "posX:3": x,
            "posY:3": y,
        }
        if qid in qid_to_layout_key:
            lk = qid_to_layout_key[qid]
            layout[lk] = new_entry
        else:
            lk = f"{next_layout_idx}:10"
            next_layout_idx += 1
            layout[lk] = new_entry

    # Sort layout keys numerically for stable output
    sorted_layout = {
        k: layout[k] for k in sorted(layout.keys(), key=lambda x: int(x.split(":", 1)[0]))
    }
    age2_line["quests:9"] = sorted_layout

    with open(LINES, "w", encoding="utf-8") as f:
        json.dump(lines_data, f, indent=2, ensure_ascii=False)
    print(
        f"[s1.5] Wrote {LINES.relative_to(ROOT)} "
        f"(Age 2 line now has {len(sorted_layout)} layout entries)"
    )


if __name__ == "__main__":
    apply()
