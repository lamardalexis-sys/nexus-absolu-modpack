#!/usr/bin/env python3
"""
Génère quests-source/age4.json (BetterQuesting 1.12.2 format).
Phase 0 (intro) + Phase 1 (fondations L2/L3) = 16 quêtes.

Convention IDs : 4000-4999 (range Age 4).
Référence format : quests-source/age2.json
"""
import json
import os

# =============================================================================
# HELPERS — Construction quêtes
# =============================================================================

def make_icon(item_id, damage=0, count=1, nbt_tag=None):
    icon = {
        "id:8": item_id,
        "Count:3": count,
        "Damage:2": damage,
        "OreDict:8": ""
    }
    if nbt_tag:
        icon["tag:10"] = nbt_tag
    return icon

def make_carnet_voss_v4_icon():
    return make_icon("patchouli:guide_book", nbt_tag={
        "patchouli:book:8": "nexusabsolu:carnet_voss_v4"
    })

def make_quest(quest_id, prereqs, name, desc, icon, tasks, rewards):
    """
    Construit une quête au format BQ 1.12.2 (NBT-suffixé).
    """
    # Slot key dans le dict parent — convention age2.json : index 0,1,2,...
    return {
        "questID:3": quest_id,
        "preRequisites:11": prereqs,
        "properties:10": {
            "betterquesting:10": {
                "issilent:1": 0,
                "snd_complete:8": "minecraft:entity.player.levelup",
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
                "ismain:1": 1,
                "repeat_relative:1": 1,
                "icon:10": icon,
                "name:8": name,
                "desc:8": desc
            }
        },
        "tasks:9": tasks,
        "rewards:9": rewards
    }

def task_checkbox(index=0):
    return {
        f"{index}:10": {
            "index:3": index,
            "taskID:8": "bq_standard:checkbox"
        }
    }

def task_retrieval(items, index=0, consume=0, partial=1, ignore_nbt=1):
    """
    items = list of dicts {"id":"...", "count":N, "damage":0, "oredict":""}
    """
    required = {}
    for i, it in enumerate(items):
        required[f"{i}:10"] = {
            "id:8": it["id"],
            "Count:3": it.get("count", 1),
            "Damage:2": it.get("damage", 0),
            "OreDict:8": it.get("oredict", "")
        }
    return {
        f"{index}:10": {
            "partialMatch:1": partial,
            "autoConsume:1": 0,
            "groupDetect:1": 0,
            "ignoreNBT:1": ignore_nbt,
            "index:3": index,
            "consume:1": consume,
            "ignoreDamage:1": 0,
            "taskID:8": "bq_standard:retrieval",
            "requiredItems:9": required
        }
    }

def reward_items(items, index=0):
    """
    items = list of {"id":"...", "count":N, "damage":0}
    """
    rewards_dict = {}
    for i, it in enumerate(items):
        rewards_dict[f"{i}:10"] = {
            "id:8": it["id"],
            "Count:3": it.get("count", 1),
            "Damage:2": it.get("damage", 0),
            "OreDict:8": it.get("oredict", "")
        }
    return {
        f"{index}:10": {
            "rewardID:8": "bq_standard:item",
            "index:3": index,
            "rewards:9": [rewards_dict]
        }
    }

def reward_command(cmd, index=1):
    return {
        f"{index}:10": {
            "hideBlockIcon:1": 1,
            "rewardID:8": "bq_standard:command",
            "asScript:1": 1,
            "description:8": "",
            "viaPlayer:1": 0,
            "index:3": index,
            "title:8": "bq_standard.reward.command",
            "command:8": cmd
        }
    }

def merge(*dicts):
    out = {}
    for d in dicts:
        out.update(d)
    return out

# =============================================================================
# QUÊTES — PHASE 0 : INTRO (3 quêtes) — IDs 4000-4002
# =============================================================================

quests = []

# Q4000 — Le Grand Œuvre commence
quests.append(make_quest(
    quest_id=4000,
    prereqs=[],  # première quête de l'âge — déclenchée par fin Âge 3
    name="§l§5Le Grand Œuvre",
    desc=(
        "§7§oTu sens un truc bizarre. L'air a une odeur que t'avais "
        "jamais remarquee.§r\n\n"
        "§7Tu viens de finir l'Age 3. Tu crois que c'est fini. Tu as tort.\n"
        "§7Quelque chose t'attendait. Un livre. §dCarnet Voss Vol. IV.§r\n"
        "§7Il etait pas la y'a 5 minutes.\n\n"
        "§8§o\"Si tu lis ceci, c'est que tu es alle plus loin que les autres.\n"
        "§8Felicitations. Maintenant le vrai travail commence.\"\n"
        "§8— Note manuscrite, premiere page§r\n\n"
        "§e§lObjectif : §7Recupere le §dCarnet Voss Vol. IV§r\n"
        "§e§lRecompense : §7Le carnet + 8 pains + bouteille XP"
    ),
    icon=make_carnet_voss_v4_icon(),
    tasks=task_checkbox(),
    rewards=merge(
        reward_items([
            {"id": "patchouli:guide_book", "count": 1,
             "damage": 0},  # le carnet via NBT
            {"id": "minecraft:bread", "count": 8},
            {"id": "minecraft:experience_bottle", "count": 3}
        ]),
        reward_command(
            "/say §5[Nexus Absolu]§r §7L'Age 4 commence. Le Grand Oeuvre s'eveille.§r"
        )
    )
))

# Q4001 — Préface du Carnet
quests.append(make_quest(
    quest_id=4001,
    prereqs=[4000],
    name="§l§5La Preface",
    desc=(
        "§7Ouvre le §dCarnet Voss Vol. IV§r§7.\n"
        "§7Lis la §oPreface§r§7. Voss y explique ce que c'est qu'une\n"
        "§7§lCartouche Manifold§r§7.\n\n"
        "§7§oSpoiler : c'est un serum injectable qui fusionne 5 theoremes\n"
        "§7§oet qui fait sortir de la simulation. Bah voila.§r\n\n"
        "§8§o\"Une cartouche. Pas une potion. Pas une pilule. Une cartouche.\n"
        "§8Parce qu'on n'avale pas le reel. On l'injecte.\"\n"
        "§8— Carnet Voss Vol. IV, Preface§r\n\n"
        "§e§lObjectif : §7Lis la §oPreface§r §7du Carnet Vol. IV\n"
        "§e§lRecompense : §710 XP + 1 emerald"
    ),
    icon=make_carnet_voss_v4_icon(),
    tasks=task_checkbox(),
    rewards=reward_items([
        {"id": "minecraft:experience_bottle", "count": 2},
        {"id": "minecraft:emerald", "count": 1}
    ])
))

# Q4002 — Vue d'ensemble : 8 lignes parallèles
quests.append(make_quest(
    quest_id=4002,
    prereqs=[4001],
    name="§l§5Huit lignes en parallele",
    desc=(
        "§7Voss decrit le pipeline : §l8 lignes industrielles§r§7 qui\n"
        "§7convergent vers un seul produit final.\n\n"
        "§7§oOui, 8. Tu as bien lu. Si t'as cru que cet age allait etre\n"
        "§7§ofacile, c'est rate. Mais bon, t'es la pour ca.§r\n\n"
        "§7Liste :\n"
        "§7• L1 §8Petrochimie §7• L2 §8Hydro-Eau\n"
        "§7• L3 §8Electrolyse + Cryo §7• L4 §8Pyrometallurgie\n"
        "§7• L5 §8Nucleaire §7• L6 §8Acides + NH3 (hub)\n"
        "§7• L7 §8Organique §7• L8 §8Botanique + Manifoldine\n\n"
        "§8§o\"Si tu te demandes par ou commencer : par l'eau.\n"
        "§8L'eau alimente tout le reste.\"§r\n\n"
        "§e§lObjectif : §7Lis le chapitre §o'Vue d'ensemble'§r\n"
        "§e§lRecompense : §7Patchouli book Vol. IV + 5 emeralds"
    ),
    icon=make_carnet_voss_v4_icon(),
    tasks=task_checkbox(),
    rewards=reward_items([
        {"id": "minecraft:emerald", "count": 5},
        {"id": "minecraft:experience_bottle", "count": 5}
    ])
))

# =============================================================================
# QUÊTES — PHASE 1 : FONDATIONS (13 quêtes) — IDs 4003-4015
# =============================================================================

# Q4003 — Setup eau (Mekanism Electric Pump)
quests.append(make_quest(
    quest_id=4003,
    prereqs=[4002],
    name="§l§6L'eau, par ou tout commence",
    desc=(
        "§7Voss te dit de commencer par l'eau. Soit.\n"
        "§7Pose un §eMekanism Electric Pump§r§7 a cote d'un lac.\n\n"
        "§7§oPas un seau a la main. Un pump. On industrialise.§r\n\n"
        "§e§lObjectif : §71x Mekanism Electric Pump\n"
        "§e§lRecompense : §72x Mechanical Pipe + 16 Universal Cable"
    ),
    icon=make_icon("mekanism:machineblock2", damage=2),
    tasks=task_retrieval([
        {"id": "mekanism:machineblock2", "count": 1, "damage": 2}
    ]),
    rewards=reward_items([
        {"id": "mekanism:transmitter", "count": 2, "damage": 4},
        {"id": "mekanism:transmitter", "count": 16, "damage": 0}
    ])
))

# Q4004 — Distillation (Thermal Evaporation Plant)
quests.append(make_quest(
    quest_id=4004,
    prereqs=[4003],
    name="§l§6Distiller, c'est purifier",
    desc=(
        "§7Construis une §eThermal Evaporation Plant§r§7 (Thermal Foundation).\n"
        "§7Au lieu de Brine, on veut juste de §lleau distillee§r§7.\n\n"
        "§7§oC'est l'etape 1 sur 3. Y'a encore deux distillations apres ca.\n"
        "§7§oLe truc c'est que une eau pas pure casse 80% des reactions plus tard.§r\n\n"
        "§e§lObjectif : §7Construis la TEP (Evaporation Controller au centre)\n"
        "§e§lRecompense : §74 buckets de Distilled Water + 8 Glass"
    ),
    icon=make_icon("thermalexpansion:device", damage=3),
    tasks=task_retrieval([
        {"id": "thermalexpansion:device", "count": 1, "damage": 3}
    ]),
    rewards=reward_items([
        {"id": "minecraft:bucket", "count": 4},
        {"id": "minecraft:glass", "count": 8}
    ])
))

# Q4005 — Filtration ionique (résine + filter)
quests.append(make_quest(
    quest_id=4005,
    prereqs=[4004],
    name="§l§6Filtration ionique",
    desc=(
        "§7Crafte une §eResine Echangeuse§r§7 (resin_charge) et fais passer\n"
        "§7ton eau distillee a travers le filtre ionique.\n\n"
        "§7§oTu retires les ions metalliques. Bah ca a l'air technique\n"
        "§7§omais en pratique c'est juste : eau dans le filtre, eau bidistillee dehors.§r\n\n"
        "§8§o\"Une eau ionisee est une eau menteuse.\" — Voss§r\n\n"
        "§e§lObjectif : §72x Resin Charge + 1000mB Bidistilled Water\n"
        "§e§lRecompense : §74 Resin Charges + 1 emerald"
    ),
    icon=make_icon("nexusabsolu:resin_charge"),
    tasks=task_retrieval([
        {"id": "nexusabsolu:resin_charge", "count": 2}
    ]),
    rewards=reward_items([
        {"id": "nexusabsolu:resin_charge", "count": 4},
        {"id": "minecraft:emerald", "count": 1}
    ])
))

# Q4006 — Osmose Inverse → Eau Tridistillée
quests.append(make_quest(
    quest_id=4006,
    prereqs=[4005],
    name="§l§6Osmose inverse : la 3eme passe",
    desc=(
        "§7Construis le §eMB-OSMOSE§r§7 (3x3x3, voir §dCarnet Vol. IV §7p.12).\n"
        "§7Membrane silicium dope au centre.\n\n"
        "§7§oTu pousses l'eau a travers une membrane qui laisse passer\n"
        "§7§oque les molecules H2O. Tout le reste reste de l'autre cote.§r\n\n"
        "§7Output : §3Eau Tridistillee§r §7- l'eau qui alimente TOUT le pipeline.\n\n"
        "§e§lObjectif : §71000mB Tridistilled Water\n"
        "§e§lRecompense : §74 buckets Tridistilled + 2 emeralds"
    ),
    icon=make_icon("nexusabsolu:tridistilled_water_bucket"),  # à créer ou substitut bucket
    tasks=task_retrieval([
        # Pas de bucket custom encore — utiliser checkbox manuel
        {"id": "minecraft:bucket", "count": 1}  # placeholder
    ], partial=1),
    rewards=reward_items([
        {"id": "minecraft:bucket", "count": 4},
        {"id": "minecraft:emerald", "count": 2}
    ])
))

# Q4007 — Air Intake Block (intro L3)
quests.append(make_quest(
    quest_id=4007,
    prereqs=[4002],  # parallèle à L2 (peut être lancé après Q4002)
    name="§l§6Aspirer l'air ambiant",
    desc=(
        "§7L3 commence par un §eAir Intake Block§r§7 pose a l'air libre.\n"
        "§7Il transforme l'air en §eAir Canisters§r §7(item).\n\n"
        "§7§oOui, l'air c'est un truc qu'on industrialise. Bienvenue dans l'Age 4.§r\n\n"
        "§e§lObjectif : §71x Air Intake Block + 16 Air Canisters\n"
        "§e§lRecompense : §72 Air Intake Blocks + 1 Iron Block"
    ),
    icon=make_icon("nexusabsolu:air_intake_block"),
    tasks=task_retrieval([
        {"id": "nexusabsolu:air_intake_block", "count": 1},
        {"id": "nexusabsolu:air_canister", "count": 16}
    ]),
    rewards=reward_items([
        {"id": "nexusabsolu:air_intake_block", "count": 2},
        {"id": "minecraft:iron_block", "count": 1}
    ])
))

# Q4008 — Cryotheum farming
quests.append(make_quest(
    quest_id=4008,
    prereqs=[4007],
    name="§l§6Le Cryotheum, ton nouveau pote",
    desc=(
        "§7Pour la cryo-distillation, t'as besoin de §bGelid Cryotheum§r§7.\n"
        "§7Recette Thermal Foundation classique : Blizz Powder + Snowballs\n"
        "§7+ Niter + Redstone Dust dans la Magma Crucible.\n\n"
        "§7§oBah voila, t'auras besoin de plein de Blizz. Genre §lvraiment plein§r§o.\n"
        "§7§oFais-toi une ferme a Blizz, tu vas me remercier.§r\n\n"
        "§e§lObjectif : §71000mB Gelid Cryotheum\n"
        "§e§lRecompense : §74 Blizz Rod + 8 Snowballs"
    ),
    icon=make_icon("thermalfoundation:fluid_cryotheum"),
    tasks=task_retrieval([
        {"id": "thermalfoundation:material", "count": 1, "damage": 1024}  # Blizz Powder placeholder
    ]),
    rewards=reward_items([
        {"id": "thermalfoundation:material", "count": 4, "damage": 2049},  # Blizz Rod
        {"id": "minecraft:snowball", "count": 8}
    ])
))

# Q4009 — MB-CRYO-ATM construction
quests.append(make_quest(
    quest_id=4009,
    prereqs=[4008],
    name="§l§6Construire la Cryo-Distillation Atmospherique",
    desc=(
        "§7C'est ton §lpremier gros multibloc Age 4§r§7.\n"
        "§7Layout 3x6x3 vertical (cf §dCarnet Vol. IV §7p.18).\n\n"
        "§7Couche 0 : Reinforced Iridium + Air Intake centre\n"
        "§7Couches 1-6 : Casing + Quantum Glass\n"
        "§7Top : 3 Output Hatch (N2/Ar/O2)\n\n"
        "§7§oC'est un mastodonte energivore (2M RF/t). Si t'as pas un\n"
        "§7§ocluster Big Reactors ou un Mekanism Fusion deja, c'est mort.\n"
        "§7§oMais bon, t'as commence l'Age 4 donc c'est suppose etre dur.§r\n\n"
        "§e§lObjectif : §7Multibloc MB-CRYO-ATM forme et detecte\n"
        "§e§lRecompense : §74 Reinforced Iridium + 1 Quantum Glass + 8 emeralds"
    ),
    icon=make_icon("modularmachinery:blockcasing"),
    tasks=task_checkbox(),
    rewards=merge(
        reward_items([
            {"id": "modularmachinery:blockcasing", "count": 4},
            {"id": "minecraft:emerald", "count": 8}
        ]),
        reward_command("/gamestage add @p age4_cryo_atm_built")
    )
))

# Q4010 — Première extraction Argon ⭐
quests.append(make_quest(
    quest_id=4010,
    prereqs=[4009, 4006],  # nécessite MB-CRYO-ATM ET Tridistilled water
    name="§l§6Premiere extraction Argon",
    desc=(
        "§7Lance ton MB-CRYO-ATM. Premier cycle = 60s en jeu.\n"
        "§7Output : §57800mB N2§r§7 + §62100mB O2§r§7 + §d100mB Argon§r§7.\n\n"
        "§7§oL'Argon represente 1% de l'air. Pour la premiere fois,\n"
        "§7§ot'en as une bouteille. Bienvenue dans la chimie noble.§r\n\n"
        "§8§o\"L'Argon ne reagit avec rien. C'est pourquoi il scelle tout.\"\n"
        "§8— Voss, Carnet Vol. IV§r\n\n"
        "§e§lObjectif : §7100mB Argon (1 bucket Argon Liquid)\n"
        "§e§lRecompense : §74 emeralds + 1 diamond + 1 capsule UC argon"
    ),
    icon=make_icon("nexusabsolu:fluorine_capsule"),  # placeholder gas item
    tasks=task_checkbox(),  # à raffiner avec un fluid task quand item bucket dispo
    rewards=merge(
        reward_items([
            {"id": "minecraft:emerald", "count": 4},
            {"id": "minecraft:diamond", "count": 1}
        ]),
        reward_command(
            "/say §5[Nexus Absolu]§r §7Premier Argon extrait. Le pipeline s'eveille.§r"
        )
    )
))

# Q4011 — Castner-Kellner construction
quests.append(make_quest(
    quest_id=4011,
    prereqs=[4006],  # nécessite eau tridistillée + brine de TEP
    name="§l§6La cellule au mercure",
    desc=(
        "§7Construis le §eMB-CK§r§7 (Cellule Castner-Kellner, 5x3x3 horizontal).\n"
        "§7Cathode : Mercury Pool. Anode : 4 Graphite Blocks.\n\n"
        "§7§oC'est de l'electrolyse fondue. T'envoies de la saumure dedans,\n"
        "§7§oca te sort du sodium liquide, du chlore gazeux, du NaOH et du H2.§r\n\n"
        "§8§o\"Avec le mercure, on triche un peu sur la thermodynamique.\n"
        "§8Mais on triche bien.\" — Voss§r\n\n"
        "§e§lObjectif : §7Multibloc MB-CK + 1 cycle complet\n"
        "§e§lRecompense : §72 Graphite Blocks + 4 emeralds"
    ),
    icon=make_icon("nexusabsolu:graphite_block"),
    tasks=task_retrieval([
        {"id": "nexusabsolu:graphite_block", "count": 4},
        {"id": "nexusabsolu:mercury_pool", "count": 1}
    ]),
    rewards=reward_items([
        {"id": "nexusabsolu:graphite_block", "count": 2},
        {"id": "minecraft:emerald", "count": 4}
    ])
))

# Q4012 — Sodium liquide
quests.append(make_quest(
    quest_id=4012,
    prereqs=[4011],
    name="§l§6Sodium liquide (jaune feu)",
    desc=(
        "§7Lance Castner-Kellner. Recupere du §eSodium liquide§r§7.\n"
        "§7Tu vas en avoir besoin pour le Procede Bayer (Aluminium L4).\n\n"
        "§7§oNote : le sodium liquide brule au contact de l'eau. Genre,\n"
        "§7§oREELLEMENT brule. Stocke-le bien dans des tanks Mekanism.§r\n\n"
        "§e§lObjectif : §7100mB Sodium Liquide\n"
        "§e§lRecompense : §72 Sodium Ingots + 4 emeralds"
    ),
    icon=make_icon("nexusabsolu:sodium_ingot"),
    tasks=task_retrieval([
        {"id": "nexusabsolu:sodium_ingot", "count": 1}
    ]),
    rewards=reward_items([
        {"id": "nexusabsolu:sodium_ingot", "count": 2},
        {"id": "minecraft:emerald", "count": 4}
    ])
))

# Q4013 — NaOH concentré
quests.append(make_quest(
    quest_id=4013,
    prereqs=[4011],
    name="§l§6NaOH : la base forte",
    desc=(
        "§7Sous-produit du Castner-Kellner. Concentre-le dans le\n"
        "§eMB-EVAPORATOR§r§7 (3x3x3 simple).\n\n"
        "§7§oTu vas en avoir besoin a 3 endroits :\n"
        "§7§o• L4 (Procede Bayer pour l'aluminium)\n"
        "§7§o• L8 (extraction pigments doux/sombres)\n"
        "§7§o• Tampon de pH dans plein de reactions§r\n\n"
        "§e§lObjectif : §7500mB NaOH Concentrated\n"
        "§e§lRecompense : §72 emeralds + 1 Lapis Block"
    ),
    icon=make_icon("nexusabsolu:graphite_block"),  # placeholder
    tasks=task_checkbox(),
    rewards=reward_items([
        {"id": "minecraft:emerald", "count": 2},
        {"id": "minecraft:lapis_block", "count": 1}
    ])
))

# Q4014 — Hydrogene & Oxygene Mekanism
quests.append(make_quest(
    quest_id=4014,
    prereqs=[4006],
    name="§l§6H2 et O2 : les classiques",
    desc=(
        "§7Setup Mekanism §eElectrolytic Separator§r§7 sur de l'eau tridistillee.\n"
        "§7Output : §dHydrogene§r §7+ §6Oxygene§r §7gazeux.\n\n"
        "§7§oTu vas en cramer des tonnes. H2 pour HDS (L1) + Haber (L6).\n"
        "§7§oO2 pour Cumene (L7), Claus (L6), Bessemer (L4)... le truc c'est\n"
        "§7§oque tu peux JAMAIS en avoir trop. Stocke en Pressurized Tubes.§r\n\n"
        "§e§lObjectif : §71x Electrolytic Separator + 1000mB H2\n"
        "§e§lRecompense : §74 Pressurized Tubes + 4 Gas Tank"
    ),
    icon=make_icon("mekanism:machineblock", damage=4),  # Electrolytic Separator
    tasks=task_retrieval([
        {"id": "mekanism:machineblock", "count": 1, "damage": 4}
    ]),
    rewards=reward_items([
        {"id": "mekanism:transmitter", "count": 4, "damage": 1},
        {"id": "mekanism:gastank", "count": 4}
    ])
))

# Q4015 — Phase 1 complete (milestone)
quests.append(make_quest(
    quest_id=4015,
    prereqs=[4006, 4010, 4012, 4013, 4014],  # tous les milestones P1
    name="§l§5Fondations posees",
    desc=(
        "§7T'as l'eau, l'air, l'azote, l'oxygene, l'argon, l'hydrogene,\n"
        "§7le sodium, le chlore, le NaOH.\n\n"
        "§7§oC'est la base. Sans ca rien ne tourne. Avec ca tu peux tout faire.\n"
        "§7§oEt maintenant la vraie chimie commence : les metaux.§r\n\n"
        "§8§o\"Quand tu maitrises les gaz industriels, tu maitrises le 21eme siecle.\n"
        "§8La suite, c'est le 19eme : les metaux et le feu.\"\n"
        "§8— Voss, Carnet Vol. IV§r\n\n"
        "§e§lObjectif : §7Phase 1 complete\n"
        "§e§lRecompense : §710 emeralds + 1 diamond block + Carnet Vol. IV chap.2 unlock"
    ),
    icon=make_carnet_voss_v4_icon(),
    tasks=task_checkbox(),
    rewards=merge(
        reward_items([
            {"id": "minecraft:emerald", "count": 10},
            {"id": "minecraft:diamond_block", "count": 1}
        ]),
        reward_command(
            "/say §5[Nexus Absolu]§r §7VAR_NAME a complete la Phase 1. La metallurgie commence.§r"
        )
    )
))

# =============================================================================
# ASSEMBLAGE FINAL DU JSON
# =============================================================================

quests_dict = {}
for i, q in enumerate(quests):
    # Slot key BQ : on suit la convention age2.json (149:10, 150:10, ...)
    # On commence à un slot libre — convention age4 = 400:10 onwards
    slot = 400 + i
    quests_dict[f"{slot}:10"] = q

output = {
    "quests": quests_dict
}

# =============================================================================
# WRITE FILE
# =============================================================================

OUT = os.path.join(os.path.dirname(__file__), "../quests-source/age4.json")
OUT = os.path.abspath(OUT)

with open(OUT, "w", encoding="utf-8") as f:
    json.dump(output, f, indent=2, ensure_ascii=False)

print(f"✓ Genere {OUT}")
print(f"✓ {len(quests)} quetes (Phase 0: 3 + Phase 1: 13)")
print(f"✓ IDs : 4000-{4000 + len(quests) - 1}")
