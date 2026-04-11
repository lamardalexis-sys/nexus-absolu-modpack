#!/usr/bin/env python3
"""
apply_sprint1_quests.py — Sprint 1 one-shot migration.

Rewrites quests-source/age2.json, replacing the 10 existing intro quests
(storage keys 149:10..157:10, qids 97..105) in-place with the new A+B fusion
content from SPRINT1_TECHNICAL_PLAN.md (sections 9 and 8 in AGE2_INTRO_DESIGN.md).

qid 106 (storage key 158:10, "Premiere Cellule") is NOT touched. Its
preRequisites:11 remains [105], which now points to the new qid 105
"Heritage Forge" — semantic migration by content replacement.

Mod IDs are sourced from quests-source/MOD_IDS.txt (hardcoded in this script
rather than parsed at runtime, so the script is self-contained for audit).

Run:
    py scripts/apply_sprint1_quests.py
then:
    py scripts/merge_quests.py --check
    py scripts/merge_quests.py

The script is idempotent — running it twice yields the same file.
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
AGE2 = ROOT / "quests-source" / "age2.json"

# ---------------------------------------------------------------------------
# Mod IDs (from quests-source/MOD_IDS.txt, confirmed by Alexis in Sprint 1)
# ---------------------------------------------------------------------------
MOD_DEEPER = "stacked_dimensions_warden"
MOD_NETHER = "netherite"

# Items
ITEM_SCULK_TENDRIL = f"{MOD_DEEPER}:sculk_tendril"
ITEM_WARDEN_HEART = f"{MOD_DEEPER}:warden_heart"
ITEM_WARDEN = f"{MOD_DEEPER}:warden"  # entity for bq_standard:hunt
ITEM_DEEPER_PORTAL = f"{MOD_DEEPER}:deeper_dark"  # portal igniter
ITEM_ANCIENT_DEBRIS = f"{MOD_NETHER}:ancient_debris"
ITEM_NETHERITE_SCRAP = f"{MOD_NETHER}:netherite_scrap"
ITEM_NETHERITE_INGOT = f"{MOD_NETHER}:netherite_ingot"

# AE2 press damage values (meta) — Alexis-confirmed via AE2_Processors.md
AE2_PRESS_LOGIC = 15
AE2_PRESS_CALCULATION = 13
AE2_PRESS_ENGINEERING = 14
AE2_PRESS_SILICON = 19
AE2_PROC_LOGIC = 22


# ---------------------------------------------------------------------------
# Shared BQ property scaffolding (matches the existing age2.json format exactly)
# ---------------------------------------------------------------------------


def bq_props(name: str, desc: str, icon: dict, ismain: int = 1,
             snd_complete: str = "minecraft:entity.player.levelup") -> dict:
    """Build the properties:10 / betterquesting:10 block with BQ defaults."""
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
    """Build an item NBT dict matching the age2.json format."""
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
    """Build a patchouli:guide_book item pointing to a specific Nexus book."""
    return item("patchouli:guide_book", count=1, damage=0, oredict="",
                tag={"patchouli:book:8": book_id})


def task_retrieval(index: int, items: list[dict], *, consume: int = 0,
                   ignore_nbt: int = 1, partial_match: int = 1) -> dict:
    """Build a bq_standard:retrieval task with N required items."""
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
    """Build a bq_standard:checkbox task (player manually confirms)."""
    return {
        "index:3": index,
        "taskID:8": "bq_standard:checkbox",
    }


def task_hunt(index: int, target_entity: str, required: int = 1) -> dict:
    """Build a bq_standard:hunt task for killing a specific entity."""
    return {
        "index:3": index,
        "taskID:8": "bq_standard:hunt",
        "required:3": required,
        "ignoreNBT:1": 1,
        "subtypes:1": 0,
        "target:8": target_entity,
        "targetNBT:10": {},
    }


def reward_items(index: int, items: list[dict]) -> dict:
    """Build a bq_standard:item reward with N items.

    Note the BQ2 quirk: the inner rewards:9 is a LIST containing exactly 1 dict
    keyed by "N:10" (not a dict directly). This matches the existing age2.json
    format byte-for-byte.
    """
    inner = {f"{i}:10": items[i] for i in range(len(items))}
    return {
        "rewardID:8": "bq_standard:item",
        "index:3": index,
        "rewards:9": [inner],
    }


def reward_command(index: int, command: str,
                   title: str = "bq_standard.reward.command") -> dict:
    """Build a bq_standard:command reward that runs a server command as script."""
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
    """Assemble a full quest dict in the exact key order used by age2.json."""
    return {
        "questID:3": qid,
        "preRequisites:11": prereqs,
        "properties:10": properties,
        "tasks:9": {f"{i}:10": tasks[i] for i in range(len(tasks))},
        "rewards:9": {f"{i}:10": rewards[i] for i in range(len(rewards))},
    }


# ---------------------------------------------------------------------------
# The 9 new quests — content definitions
# ---------------------------------------------------------------------------


def q97_surface() -> tuple[str, dict]:
    """qid 97 — A la Surface : player exits CM9x9 into overworld for the first time."""
    desc = (
        "§7§oLa lumiere t'aveugle. Tes jambes tremblent.\n"
        "Tu sens la pluie, le vent, la terre sous tes pieds.§r\n\n"
        "§7Trois Ages dans des boites.\n"
        "§7Tu ne savais meme plus si l'exterieur existait encore.\n\n"
        "§8§o\"Jour 0 — Sujet S-47 sorti du confinement Voss-7.\n"
        "§8Aucune surveillance active depuis l'effondrement du Poste.\n"
        "§8Bienvenue dehors, enfant.\"\n— Journal automatise§r\n\n"
        "§e§lObjectif : §7Confirme ta sortie\n"
        "§e§lRecompense : §7Carnet de Voss Vol. II"
    )
    icon = patchouli_book("nexusabsolu:carnet_voss_v2")
    props = bq_props("§l§6A la Surface", desc, icon, ismain=1)
    tasks = [task_checkbox(0)]
    rewards = [
        reward_items(0, [
            patchouli_book("nexusabsolu:carnet_voss_v2"),
            item("minecraft:bread", count=4),
            item("minecraft:experience_bottle", count=2),
        ]),
        reward_command(
            1,
            "/say §5[Nexus Absolu]§r §7VAR_NAME§r sort enfin de sa boite. L'Age 2 commence.",
        ),
    ]
    return "149:10", build_quest(97, [149], props, tasks, rewards)


def q98_carnet_read() -> tuple[str, dict]:
    """qid 98 — Les Pages de Voss : player holds the carnet in hand (auto-completes)."""
    desc = (
        "§7Le Carnet Vol. II est dans ton inventaire.\n"
        "§7Ouvre-le. Lis-le. C'est la premiere fois\n"
        "§7que tu vois l'ecriture de Voss directement.\n\n"
        "§8§oEntrees :\n"
        "§8- \"Le Poste\"\n"
        "§8- \"Sujet 46\"\n"
        "§8- \"Protocole Voss-7\"§r\n\n"
        "§7Tu comprendras qu'il t'a surveille\n"
        "§7pendant trois ans. Et que tu n'etais\n"
        "§7pas le premier.\n\n"
        "§e§lObjectif : §7Garder le Carnet II sur toi\n"
        "§e§lRecompense : §7Badge Voss + Pioche Diamant"
    )
    icon = patchouli_book("nexusabsolu:carnet_voss_v2")
    props = bq_props("§l§6Les Pages de Voss", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [
        patchouli_book("nexusabsolu:carnet_voss_v2")
    ], ignore_nbt=0)]
    rewards = [
        reward_items(0, [
            item("nexusabsolu:badge_voss", count=1),
            item("minecraft:diamond_pickaxe", count=1),
            item("minecraft:experience_bottle", count=4),
        ]),
    ]
    return "150:10", build_quest(98, [97], props, tasks, rewards)


def q99_sculk_signal() -> tuple[str, dict]:
    """qid 99 — Signal Sculk : collect 4 sculk tendrils."""
    desc = (
        "§7Il y a un bruit. Pas un son, un §obruit§r§7.\n"
        "§7Une vibration dans le sol qui te suit\n"
        "§7quand tu marches. Voss a ecrit la-dessus.\n\n"
        "§8§o\"Sujet 46 emet des fragments sculk en permanence.\n"
        "§8C'est sa facon de parler. Ou de nous\n"
        "§8compter. Je ne sais plus.\"\n— Carnet Voss Vol. II, p.4§r\n\n"
        "§7Ramasse 4 §bTendrils de Sculk§r§7 — ils poussent\n"
        "§7la ou la surveillance du Sujet 46 s'etend.\n\n"
        "§e§lObjectif : §74x Sculk Tendril\n"
        "§e§lRecompense : §7Recette du Sac du Sujet 46 debloquee"
    )
    icon = item(ITEM_SCULK_TENDRIL, count=1)
    props = bq_props("§l§6Signal Sculk", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item(ITEM_SCULK_TENDRIL, count=4)])]
    rewards = [
        reward_items(0, [
            item("minecraft:iron_sword", count=1),
            item("minecraft:torch", count=16),
            item("minecraft:experience_bottle", count=4),
        ]),
        reward_command(1, "/gamestage add @p age2_grabber_recipe"),
    ]
    return "151:10", build_quest(99, [98], props, tasks, rewards)


def q100_grabber_craft() -> tuple[str, dict]:
    """qid 100 — Le Sac du Sujet 46 : craft the Grabber Voss."""
    desc = (
        "§7Le Sujet 46 portait un sac.\n"
        "§7Voss l'appelait le §eGrabber§r§7.\n"
        "§7Il stocke dans un pli spatial au lieu\n"
        "§7de ton inventaire physique.\n\n"
        "§8§o\"Le sac de 46 contient ses affaires,\n"
        "§8mais aussi ses souvenirs. Je n'ai jamais\n"
        "§8compris comment il a fait ca.\"\n"
        "§8— Carnet Voss Vol. II, p.7§r\n\n"
        "§7Les tendrils suffisent pour assembler\n"
        "§7une replique. Crafte-la.\n\n"
        "§e§lObjectif : §71x Grabber Voss\n"
        "§e§lRecompense : §7Nourriture + golden apples"
    )
    icon = item("nexusabsolu:grabber_voss", count=1)
    props = bq_props("§l§6Le Sac du Sujet 46", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item("nexusabsolu:grabber_voss", count=1)])]
    rewards = [
        reward_items(0, [
            item("minecraft:bread", count=16),
            item("minecraft:torch", count=32),
            item("minecraft:golden_apple", count=2),
            item("minecraft:experience_bottle", count=6),
        ]),
    ]
    return "152:10", build_quest(100, [99], props, tasks, rewards)


def q101_deep_descent() -> tuple[str, dict]:
    """qid 101 — La Descente : build/obtain the Deeper Dark portal igniter."""
    desc = (
        "§7Sous ton point de sortie, les grottes\n"
        "§7sont plus profondes que tu ne le pensais.\n"
        "§7Trop profondes. Trop §osilencieuses§r§7.\n\n"
        "§7Il te faut l'§dAllumeur Deeper Dark§r§7.\n"
        "§7Cherche la recette dans ton livre.\n\n"
        "§8§o\"Le Deep Dark n'est pas une grotte. C'est\n"
        "§8un fossile d'experience. Voss a ouvert une\n"
        "§8porte qu'il n'a jamais refermee.\"\n"
        "— Carnet Voss Vol. II, p.11§r\n\n"
        "§e§lObjectif : §71x Allumeur Deeper Dark\n"
        "§e§lRecompense : §7Lanterne de Voss + Vision Nocturne"
    )
    icon = item(ITEM_DEEPER_PORTAL, count=1)
    props = bq_props("§l§6La Descente", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item(ITEM_DEEPER_PORTAL, count=1)])]
    rewards = [
        reward_items(0, [
            item("nexusabsolu:lanterne_voss", count=1),
            item("minecraft:glowstone", count=4),
            item("minecraft:potion", count=2, tag={"Potion:8": "minecraft:long_night_vision"}),
        ]),
    ]
    return "153:10", build_quest(101, [100], props, tasks, rewards)


def q102_warden_kill() -> tuple[str, dict]:
    """qid 102 — Celui Qui Dort : kill the Warden (real boss fight, ATM10-style)."""
    desc = (
        "§7Il est la. Il te voit par vibration.\n"
        "§7Il ne te laissera pas repartir sans combattre.\n\n"
        "§7C'etait le Sujet 46.\n"
        "§7Maintenant c'est autre chose.\n\n"
        "§8§o\"Le Sujet 46 a survecu a l'experience\n"
        "§8du 17 mars. Mais il n'est plus vraiment\n"
        "§8le Sujet 46. Je n'ai pas le courage de\n"
        "§8le terminer moi-meme. Je suis desole.\"\n"
        "— Carnet Voss Vol. II, p.14 (derniere entree)§r\n\n"
        "§c§lObjectif : §7Tuer le Gardien\n"
        "§c§lDanger : §4EXTREME — prepare-toi\n"
        "§e§lRecompense : §74 Presses d'Inscription AE2 + Coeur du Gardien"
    )
    icon = item(ITEM_WARDEN_HEART, count=1)
    props = bq_props("§l§4Celui Qui Dort", desc, icon, ismain=1,
                     snd_complete="minecraft:entity.wither.spawn")
    tasks = [task_hunt(0, ITEM_WARDEN, required=1)]
    rewards = [
        reward_items(0, [
            # The 4 AE2 presses — skip meteorite RNG entirely
            item("appliedenergistics2:material", count=1, damage=AE2_PRESS_LOGIC),
            item("appliedenergistics2:material", count=1, damage=AE2_PRESS_CALCULATION),
            item("appliedenergistics2:material", count=1, damage=AE2_PRESS_ENGINEERING),
            item("appliedenergistics2:material", count=1, damage=AE2_PRESS_SILICON),
            # Boss trophy + lore item
            item(ITEM_WARDEN_HEART, count=1),
            item("nexusabsolu:fragment_memoire_1", count=1),
            item("minecraft:experience_bottle", count=16),
        ]),
    ]
    return "154:10", build_quest(102, [101], props, tasks, rewards)


def q103_nether_open() -> tuple[str, dict]:
    """qid 103 — La Porte de Voss : enter the Nether (proxied by soul sand retrieval)."""
    desc = (
        "§7Le Gardien est mort. L'oeil dans sa main\n"
        "§7gauche contenait des coordonnees.\n"
        "§7Pas sur l'Overworld. §cDans le Nether§r§7.\n\n"
        "§7Voss y cachait quelque chose — peut-etre\n"
        "§7lui-meme. Construis un portail. Entre.\n"
        "§7Ramasse du §6soul sand§r§7 pour prouver\n"
        "§7que tu y es alle.\n\n"
        "§8§o\"Le Nether pour moi n'est pas une dimension.\n"
        "§8C'est un cimetiere que j'ai creuse moi-meme.\"\n"
        "— Carnet Voss Vol. III, p.1§r\n\n"
        "§e§lObjectif : §71x Soul Sand\n"
        "§e§lRecompense : §7Carnet de Voss Vol. III"
    )
    icon = item("minecraft:obsidian", count=1)
    props = bq_props("§l§6La Porte de Voss", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item("minecraft:soul_sand", count=1)])]
    rewards = [
        reward_items(0, [
            patchouli_book("nexusabsolu:carnet_voss_v3"),
            item("minecraft:fire_charge", count=4),
            item("minecraft:experience_bottle", count=4),
        ]),
    ]
    return "155:10", build_quest(103, [102], props, tasks, rewards)


def q104_ancient_debris() -> tuple[str, dict]:
    """qid 104 — Le Fragment de Voss : mine 4 Ancient Debris."""
    desc = (
        "§7Au fond du Nether, des blocs noirs\n"
        "§7resistent a ta pioche de fer comme si\n"
        "§7c'etait du papier. Il te faut du diamant.\n\n"
        "§7C'est l'§4Ancient Debris§r§7 — une matiere\n"
        "§7que Voss a fait tomber dans le Nether\n"
        "§7en 2019 pendant un test de densite.\n"
        "§7Personne n'etait cense la revoir.\n\n"
        "§e§lObjectif : §74x Ancient Debris\n"
        "§e§lRecompense : §74x Netherite Scrap + 4x Or"
    )
    icon = item(ITEM_ANCIENT_DEBRIS, count=1)
    props = bq_props("§l§6Le Fragment de Voss", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item(ITEM_ANCIENT_DEBRIS, count=4)])]
    rewards = [
        reward_items(0, [
            item(ITEM_NETHERITE_SCRAP, count=4),
            item("minecraft:gold_ingot", count=4),
            item("minecraft:experience_bottle", count=8),
        ]),
    ]
    return "156:10", build_quest(104, [103], props, tasks, rewards)


def q105_netherite_forged() -> tuple[str, dict]:
    """qid 105 — Heritage Forge : forge 1 Netherite Ingot + open the 3 branches."""
    desc = (
        "§7Quatre fragments + quatre lingots d'or\n"
        "§7+ un feu assez chaud. C'est la recette\n"
        "§7de Voss pour le §4Netherite§r§7.\n\n"
        "§7Tiens. Regarde-le briller dans ta main.\n"
        "§7Tu portes maintenant quelque chose\n"
        "§7que Voss a fabrique, touche, oublie.\n\n"
        "§8§o\"Le Netherite n'est pas un alliage. C'est\n"
        "§8une memoire. Chaque lingot garde la trace\n"
        "§8de celui qui l'a forge.\"\n"
        "— Carnet Voss Vol. III, p.7§r\n\n"
        "§e§lObjectif : §71x Netherite Ingot\n"
        "§e§lRecompense : §7ME Controller + 4x Logic Processor + 3 voies ouvertes"
    )
    icon = item(ITEM_NETHERITE_INGOT, count=1)
    props = bq_props("§l§6Heritage Forge", desc, icon, ismain=1)
    tasks = [task_retrieval(0, [item(ITEM_NETHERITE_INGOT, count=1)])]
    rewards = [
        reward_items(0, [
            item("appliedenergistics2:controller", count=1),
            item("appliedenergistics2:material", count=4, damage=AE2_PROC_LOGIC),
            item("minecraft:experience_bottle", count=16),
        ]),
        reward_command(1, "/gamestage add @p age2_crossroads"),
        reward_command(
            2,
            "/say §5[Nexus Absolu]§r §7VAR_NAME§r a forge un lingot de Netherite. L'heritage de Voss passe a la generation suivante.",
        ),
    ]
    return "157:10", build_quest(105, [104], props, tasks, rewards)


# ---------------------------------------------------------------------------
# Apply
# ---------------------------------------------------------------------------


NEW_QUESTS = [
    q97_surface,
    q98_carnet_read,
    q99_sculk_signal,
    q100_grabber_craft,
    q101_deep_descent,
    q102_warden_kill,
    q103_nether_open,
    q104_ancient_debris,
    q105_netherite_forged,
]


def apply() -> None:
    if not AGE2.exists():
        raise FileNotFoundError(f"{AGE2} not found")
    with open(AGE2, encoding="utf-8") as f:
        data = json.load(f)

    if "quests" not in data:
        raise ValueError(f"{AGE2}: missing 'quests' top-level key")

    quests = data["quests"]
    replaced = []

    for fn in NEW_QUESTS:
        storage_key, new_quest = fn()
        if storage_key not in quests:
            raise KeyError(
                f"storage key {storage_key!r} missing from age2.json — expected "
                f"to replace the existing qid {new_quest['questID:3']} entry"
            )
        if quests[storage_key].get("questID:3") != new_quest["questID:3"]:
            raise ValueError(
                f"storage key {storage_key!r}: existing questID "
                f"{quests[storage_key].get('questID:3')} does not match "
                f"new questID {new_quest['questID:3']}"
            )
        quests[storage_key] = new_quest
        replaced.append((storage_key, new_quest["questID:3"]))

    # Sanity check: qid 106 (storage key 158:10) must still exist and its prereq still [105]
    if "158:10" not in quests:
        raise RuntimeError("qid 106 (key 158:10) was lost — abort")
    q106 = quests["158:10"]
    if q106.get("questID:3") != 106 or q106.get("preRequisites:11") != [105]:
        raise RuntimeError(
            f"qid 106 invariant broken: qid={q106.get('questID:3')}, "
            f"prereq={q106.get('preRequisites:11')}"
        )

    with open(AGE2, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

    print(f"[sprint1] Rewrote {len(replaced)} quests in {AGE2.relative_to(ROOT)}:")
    for sk, qid in replaced:
        print(f"  {sk} -> qid {qid}")
    print(f"  158:10 -> qid 106 (Premiere Cellule, UNTOUCHED, prereq=[105] preserved)")


if __name__ == "__main__":
    apply()
