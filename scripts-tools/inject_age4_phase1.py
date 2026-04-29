#!/usr/bin/env python3
"""
Genere les 16 quetes Phase 1 (Q-INTRO + Q1-Q15) Age 4 dans
config/betterquesting/DefaultQuests.json.

Apres execution :
  - 16 quetes ajoutees, questIDs 200-215
  - 1 nouvelle questline 'Age 4 -- L'Echappee' creee
  - Prereq Q-INTRO = Q156 (VERS L'AGE 3, terminale Age 2)

Convention :
  - questIDs : 200 (Q-INTRO) -> 215 (Q15)
  - Range Age 4 reservee : 200-279 (80 quetes)
  - Layout : x=80px par quete, y=90px par rangee, serpentin

Items custom utilises (existent ou seront crees) :
  - patchouli:guide_book taggue 'nexusabsolu:carnet_voss_v4' (a creer)
  - mekanism:bucket distilled_water
  - nexusabsolu:resine_echangeuse_block (a creer)
  - nexusabsolu:cryo_distillateur_controller (a creer)
  - thermalfoundation:fluid_cryotheum
  - nexusabsolu:bucket_argon (existe via fluide custom)
  - etc.
"""
import json
import sys

QUEST_FILE = 'config/betterquesting/DefaultQuests.json'
PHASE1_QID_START = 200  # questID de Q-INTRO
AGE2_TERMINAL_QID = 156  # prereq Q-INTRO


def make_item(item_id, count=1, damage=0, nbt=None):
    """Helper item dict pour BQ."""
    item = {
        "id:8": item_id,
        "Count:3": count,
        "Damage:2": damage,
        "OreDict:8": ""
    }
    if nbt:
        item["tag:10"] = nbt
    return item


def make_quest(qid, name, desc, icon, prereqs, tasks, rewards):
    """Construit une quete BQ complete."""
    return {
        "questID:3": qid,
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
    return {str(index) + ":10": {
        "index:3": index,
        "taskID:8": "bq_standard:checkbox"
    }}


def task_retrieval(index, item_id, count=1, nbt=None):
    """Tache : recuperer N exemplaires d'un item dans l'inventaire."""
    item_dict = make_item(item_id, count=count, nbt=nbt)
    return {str(index) + ":10": {
        "ignoreNBT:1": 1 if nbt is None else 0,
        "partialMatch:1": 1,
        "autoConsume:1": 0,
        "groupDetect:1": 0,
        "consume:1": 0,
        "taskID:8": "bq_standard:retrieval",
        "index:3": index,
        "required:9": {
            "0:10": item_dict
        }
    }}


def task_crafting(index, item_id, count=1, nbt=None):
    """Tache : crafter N exemplaires d'un item."""
    item_dict = make_item(item_id, count=count, nbt=nbt)
    return {str(index) + ":10": {
        "ignoreNBT:1": 1 if nbt is None else 0,
        "partialMatch:1": 1,
        "anyCrafting:1": 0,
        "taskID:8": "bq_standard:crafting",
        "index:3": index,
        "requiredItems:9": {
            "0:10": item_dict
        }
    }}


def reward_items(index, items_list):
    """Recompense items (jusqu'a 6 items dans la liste)."""
    rewards_dict = {}
    for i, item in enumerate(items_list):
        rewards_dict[str(i) + ":10"] = item
    return {str(index) + ":10": {
        "rewardID:8": "bq_standard:item",
        "index:3": index,
        "rewards:9": rewards_dict
    }}


def reward_command(index, command, hide_icon=True):
    """Recompense commande (announce + side effects)."""
    return {str(index) + ":10": {
        "hideBlockIcon:1": 1 if hide_icon else 0,
        "rewardID:8": "bq_standard:command",
        "asScript:1": 1,
        "description:8": "",
        "viaPlayer:1": 0,
        "index:3": index,
        "title:8": "bq_standard.reward.command",
        "command:8": command
    }}


# ============================================================
# Definitions des 16 quetes
# ============================================================

# Carnet Voss V4 = patchouli book taggue
ICON_CARNET_V4 = make_item("patchouli:guide_book", nbt={
    "patchouli:book:8": "nexusabsolu:carnet_voss_v4"
})

# Bucket distilled water (Mekanism)
ICON_DISTILLED = make_item("mekanism:bucket", nbt={
    "FluidName:8": "mekanism:water",
    "Amount:3": 1000
})
TASK_DISTILLED = make_item("mekanism:bucket", count=1, nbt={
    "FluidName:8": "mekanism:water",
    "Amount:3": 1000
})

# Resine Echangeuse (custom)
ICON_RESINE = make_item("contenttweaker:resine_echangeuse_block")

# Cryo Distillateur Controller (custom)
ICON_CRYO_CTRL = make_item("contenttweaker:cryo_distillateur_controller")

# Tridistilled water (custom fluid)
ICON_TRIDIST = make_item("contenttweaker:bucket_tridistilled_water")
TASK_TRIDIST_4000 = make_item("contenttweaker:bucket_tridistilled_water", count=4)

# Argon (custom fluid)
ICON_ARGON = make_item("contenttweaker:bucket_argon")
TASK_ARGON_8 = make_item("contenttweaker:bucket_argon", count=8)

# N2 (custom fluid)
ICON_N2 = make_item("contenttweaker:bucket_n2")
TASK_N2_10 = make_item("contenttweaker:bucket_n2", count=10)

# O2 (custom fluid)
ICON_O2 = make_item("contenttweaker:bucket_o2")
TASK_O2_10 = make_item("contenttweaker:bucket_o2", count=10)

# Cryotheum bucket (Thermal)
ICON_CRYO = make_item("thermalfoundation:fluid_cryotheum")

# AE2 Controller
ICON_AE2 = make_item("appliedenergistics2:controller")

# OC Case T3
ICON_OC = make_item("opencomputers:case3")

# Heavy Water (Mekanism)
ICON_HEAVYWATER = make_item("mekanism:bucket", nbt={
    "FluidName:8": "heavywater",
    "Amount:3": 1000
})

# Tritium (custom)
ICON_TRITIUM = make_item("contenttweaker:bucket_tritium")
TASK_TRITIUM_100 = make_item("contenttweaker:bucket_tritium", count=1)  # 1 bucket = 1000mB, on demande au moins 100mB

# Bread + XP bottle
ITEM_BREAD = make_item("minecraft:bread", count=4)
ITEM_BREAD_8 = make_item("minecraft:bread", count=8)
ITEM_XP = make_item("minecraft:experience_bottle", count=1)
ITEM_XP_2 = make_item("minecraft:experience_bottle", count=2)
ITEM_XP_4 = make_item("minecraft:experience_bottle", count=4)

# Mekanism pump
ITEM_PUMP = make_item("mekanism:machineblock", count=1, damage=10)
ITEM_RESONANT_TANK = make_item("thermalexpansion:tank", count=1, damage=4)
ITEM_RESONANT_GLASS = make_item("thermalfoundation:glass", count=4, damage=4)
ITEM_AE2_STORAGE = make_item("appliedenergistics2:storage_cell_16k", count=4)
ITEM_OC_GPU = make_item("opencomputers:card3", count=1)
ITEM_LITHIUM = make_item("mekanism:dust", count=8, damage=4)


phase1_quests = []

# === Q-INTRO Tu te crois libre ? ===
desc_intro = """§7§oTu sors de ta base. Tu regardes le ciel. Tu te dis "j'ai fait un truc bien".§r

§7Sauf qu'un livre vient d'apparaitre sur ta table de craft.
§7Tu ne l'as pas pose la.
§7Personne d'autre n'est la.

§8§o"Sujet 47.
§8Tu as franchi trois paliers. Tu es sorti des Compact Machines
§8a la fin de l'Age 1. Tu as explore l'overworld pendant
§8les Ages 2 et 3. Tu te crois libre.

§8Tu te trompes.

§8Cette ligne d'horizon que tu vois — c'est un mur."§r
§8— E.V., Carnet Vol. IV — Preface

§e§lObjectif : §7Lis le carnet. Comprends que ce n'est pas fini.
§e§lRecompense : §7La liberte n'est pas pour aujourd'hui."""

phase1_quests.append(make_quest(
    qid=200,
    name="§l§5Tu te crois libre ?",
    desc=desc_intro,
    icon=ICON_CARNET_V4,
    prereqs=[AGE2_TERMINAL_QID],
    tasks=task_checkbox(0),
    rewards={
        **reward_items(0, [ICON_CARNET_V4, ITEM_BREAD, ITEM_XP]),
        **reward_command(1, "/say §5[Nexus Absolu]§r §7VAR_NAME§r ouvre le Carnet Voss Vol IV. L'Age 4 commence.")
    }
))

# === Q1 L'eau qui ment ===
desc_q1 = """§7§oVoss commence par l'eau. Evidemment.§r

§7Tu pensais qu'un seau d'eau de riviere c'etait de l'eau ?
§7§lFaux.§r §7C'est de l'eau qui ment.
§7Elle contient 2.4 millions de bacteries, 14 ions differents,
§7du calcaire, du fluor, et probablement les souvenirs
§7d'au moins trois grenouilles decedees.

§7Pour la Cartouche tu vas avoir besoin d'§lEau Tridistillee§r§7.
§7Tu vas la faire en 3 etapes.

§8§o"L'eau pure n'existe pas dans la nature, Sujet 47.
§8L'eau pure est une obsession humaine. Adopte-la." — E.V.

§e§lObjectif : §7Distille ton premier 1000mB d'eau (Thermal Evap Plant)
§e§lRecompense : §74 buckets vides + plans Filtre Ionique"""

phase1_quests.append(make_quest(
    qid=201,
    name="§l§6Q1 -- L'eau qui ment",
    desc=desc_q1,
    icon=ICON_DISTILLED,
    prereqs=[200],
    tasks=task_retrieval(0, "mekanism:bucket", count=1, nbt={"FluidName:8": "mekanism:water", "Amount:3": 1000}),
    rewards=reward_items(0, [
        make_item("minecraft:bucket", count=4),
        ITEM_XP,
        ITEM_BREAD
    ])
))

# === Q2 Filtre ionique ===
desc_q2 = """§7§oCe que ton seau d'eau distillee a encore en lui c'est des §lions§r§7.
§7Cations. Anions. Tu sais. Les particules chargees qui font
§7que ton serum exploserait au lieu de te transformer.

§7Ce qu'il te faut c'est une §lresine echangeuse d'ions§r§7.
§7Silice + acide sulfonique. Tu vas devoir synthetiser ca.
§7Et oui ca veut dire que tu as deja besoin d'acide sulfurique
§7avant meme d'avoir commence l'usine. Bienvenue dans la chimie.

§8§o"Tout depend de tout. C'est l'essence du Theoreme I." — E.V.

§e§lObjectif : §7Crafte 1 bloc de Resine Echangeuse
§e§lRecompense : §74 Resine Block + plans Osmose Inverse"""

phase1_quests.append(make_quest(
    qid=202,
    name="§l§6Q2 -- Le filtre qui sépare le bon grain de l'ivraie ionique",
    desc=desc_q2,
    icon=ICON_RESINE,
    prereqs=[201],
    tasks=task_crafting(0, "contenttweaker:resine_echangeuse_block", count=1),
    rewards=reward_items(0, [
        make_item("contenttweaker:resine_echangeuse_block", count=4),
        ITEM_XP
    ])
))

# === Q3 L'eau au cube ===
desc_q3 = """§7§oTroisieme passage. Membrane silicium dope.
§7Pression osmotique inverse. T'es en mode labo blanc maintenant.

§7Cette eau-la elle ment plus.
§7Elle est si pure qu'elle conduit MOINS l'electricite que l'air.
§7Si tu en bois t'as litteralement aucun sel dans le corps.
§7Donc bois pas. T'as pas signe pour mourir d'hyponatremie aujourd'hui.

§e§lObjectif : §7Produis 4 buckets d'Eau Tridistillee
§e§lRecompense : §78 buckets Tridist + Schema Cryo-Distillateur"""

phase1_quests.append(make_quest(
    qid=203,
    name="§l§6Q3 -- L'eau au cube",
    desc=desc_q3,
    icon=ICON_TRIDIST,
    prereqs=[202],
    tasks=task_retrieval(0, "contenttweaker:bucket_tridistilled_water", count=4),
    rewards=reward_items(0, [
        make_item("contenttweaker:bucket_tridistilled_water", count=8),
        ITEM_XP_2
    ])
))

# === Q4 L'air aussi est sale ===
desc_q4 = """§7§oL'air que tu respires c'est :
§7  - 78% azote (qui sert a rien pour respirer mais joli)
§7  - 21% oxygene (le truc utile)
§7  - 0.93% argon (le gaz noble dont t'auras besoin)
§7  - 0.04% CO2 (le truc qui rechauffe la planete)
§7  - 0.03% autres conneries

§7Pour la Cartouche il te faut isoler l'§lArgon§r§7.
§7Spoiler : tu peux pas le crafter. Tu peux que le §lcondenser a -186 deg§r§7.
§7Et tu vas avoir besoin de §lGelid Cryotheum§r§7.
§7Beaucoup de Gelid Cryotheum.

§8§o"L'argon est le mensonge le plus discret de l'atmosphere.
§8Personne ne le voit. Mais sans lui ton serum oxyde et meurt." — E.V.

§e§lObjectif : §7Construis le Cryo-Distillateur Atmospherique (3x6x3)
§e§lRecompense : §7Bucket d'Argon + 4 Cryotheum"""

phase1_quests.append(make_quest(
    qid=204,
    name="§l§6Q4 -- L'air aussi est sale",
    desc=desc_q4,
    icon=ICON_CRYO_CTRL,
    prereqs=[203],
    tasks=task_crafting(0, "contenttweaker:cryo_distillateur_controller", count=1),
    rewards=reward_items(0, [
        make_item("contenttweaker:bucket_argon", count=1),
        make_item("thermalfoundation:fluid_cryotheum", count=4),
        ITEM_XP_2
    ])
))

# === Q5 Le froid intelligent ===
desc_q5 = """§7§oFais des stocks de Cryotheum. Serieux.
§7T'en as besoin pour le Cryo-Distillateur. T'en auras besoin
§7pour le Melangeur Cryogenique. T'en auras besoin pour le
§7Bio-Reacteur Manifold final.

§7Recette : Blizz Powder + Snowball + Redstone + Niter
§7Tu connais. Tu connais.

§7Si tu connais pas c'est que t'as seche les ages 2 et 3.
§7Honte a toi. Ouvre le JEI.

§e§lObjectif : §7Stocke 32 buckets de Gelid Cryotheum
§e§lRecompense : §7Resonant Tank + 8 Cryotheum"""

phase1_quests.append(make_quest(
    qid=205,
    name="§l§6Q5 -- Le froid intelligent",
    desc=desc_q5,
    icon=ICON_CRYO,
    prereqs=[204],
    tasks=task_retrieval(0, "thermalfoundation:fluid_cryotheum", count=32),
    rewards=reward_items(0, [
        ITEM_RESONANT_TANK,
        make_item("thermalfoundation:fluid_cryotheum", count=8),
        ITEM_XP_2
    ])
))

# === Q6 Le ciel s'est ouvert ===
desc_q6 = """§7§oArgon. 0.93% de l'atmosphere. Le moins glorieux des gaz nobles.

§7Sauf que c'est le seul gaz inerte que tu peux faire
§7sans casquer 200 buckets de Cryotheum par mB.
§7L'helium serait mieux mais bon courage pour en condenser
§7a -269 deg dans Minecraft.

§7Stocke 8 buckets. Tu vas en cramer un en encartouchage final.
§7Le reste te servira pour le Kroll TiCl4 (Phase 2).

§e§lObjectif : §7Stocke 8 buckets d'Argon
§e§lRecompense : §74 Resonant Glass + plans encartouchage"""

phase1_quests.append(make_quest(
    qid=206,
    name="§l§6Q6 -- Le ciel s'est ouvert",
    desc=desc_q6,
    icon=ICON_ARGON,
    prereqs=[205],
    tasks=task_retrieval(0, "contenttweaker:bucket_argon", count=8),
    rewards=reward_items(0, [
        ITEM_RESONANT_GLASS,
        ITEM_XP_2
    ])
))

# === Q7 L'azote pour le pivot ===
desc_q7 = """§7§oN2. 78% de ton atmosphere. C'est ce qui prend le plus de place
§7dans tes poumons sans servir a respirer.

§7Tu vas l'utiliser pour le §lprocede Haber-Bosch§r§7 (Phase 3) :
§7  N2 + 3 H2 -> 2 NH3 (ammoniaque)
§7Et l'ammoniaque c'est la base de toute la chimie organique azotee.

§7Stocke 10 buckets. Phase 3 va te demander 6.

§e§lObjectif : §7Stocke 10 buckets de N2
§e§lRecompense : §7Mekanism Pump + 16 buckets vides"""

phase1_quests.append(make_quest(
    qid=207,
    name="§l§6Q7 -- L'azote pour le pivot",
    desc=desc_q7,
    icon=ICON_N2,
    prereqs=[206],
    tasks=task_retrieval(0, "contenttweaker:bucket_n2", count=10),
    rewards=reward_items(0, [
        ITEM_PUMP,
        make_item("minecraft:bucket", count=16),
        ITEM_XP_2
    ])
))

# === Q8 L'oxygene pour bruler ===
desc_q8 = """§7§oO2. Le truc qui te tient en vie. Et qui fait bruler tout ce
§7que tu mets dedans.

§7Pour la Cartouche tu vas l'utiliser pour :
§7  - Bessemer (Phase 2 fer pur)
§7  - Claus (Phase 3 soufre)
§7  - Ostwald (Phase 3 acide nitrique)

§7Stocke 10 buckets. C'est la base.

§e§lObjectif : §7Stocke 10 buckets d'O2
§e§lRecompense : §7Resonant Tank + 8 buckets vides"""

phase1_quests.append(make_quest(
    qid=208,
    name="§l§6Q8 -- L'oxygène pour brûler",
    desc=desc_q8,
    icon=ICON_O2,
    prereqs=[207],
    tasks=task_retrieval(0, "contenttweaker:bucket_o2", count=10),
    rewards=reward_items(0, [
        ITEM_RESONANT_TANK,
        make_item("minecraft:bucket", count=8),
        ITEM_XP_2
    ])
))

# === Q9 Et la lumiere fut ===
desc_q9 = """§7§o5M RF/t. Voila ton premier jalon energie.

§7Tu vas pas y arriver avec des fours a charbon. Faut soit :
§7  - 2 Big Reactors taille moyenne
§7  - 1 Mekanism Fission Reactor T1
§7  - 4 EnderIO Capacitor Banks Resonant + 16 generateurs

§7Spoiler : Phase 4 nucleaire va te demander 50M RF/t en pic.
§7Donc 5M c'est juste l'echauffement.

§8§o"L'energie est la signature des Createurs.
§8Plus tu en consommes plus tu te rapproches d'eux." — E.V.

§e§lObjectif : §7Atteins 5M RF/t (clique manuellement quand fait)
§e§lRecompense : §78 Energy Conduit + Carnet Voss Vol IV ch.1"""

phase1_quests.append(make_quest(
    qid=209,
    name="§l§6Q9 -- Et la lumière fut",
    desc=desc_q9,
    icon=make_item("enderio:item_power_conduit", count=1, damage=2),
    prereqs=[208],
    tasks=task_checkbox(0),
    rewards=reward_items(0, [
        make_item("enderio:item_power_conduit", count=8, damage=2),
        ICON_CARNET_V4,
        ITEM_XP_2
    ])
))

# === Q10 L'usine sait pomper ===
desc_q10 = """§7§oTu vas pas continuer a remplir des seaux a la main.
§7Tu te crois encore en age 1 ?

§7§lMekanism Electric Pump§r§7. Source d'eau infinie tant que t'as
§7une source d'eau dessous. Roxe pour quand tu auras besoin
§7de 30 buckets/seconde pour la pretrochimie (Phase 3).

§e§lObjectif : §7Crafte ta premiere Mekanism Electric Pump
§e§lRecompense : §74 Pumps + 8 Gas Tubes"""

phase1_quests.append(make_quest(
    qid=210,
    name="§l§6Q10 -- L'usine sait pomper",
    desc=desc_q10,
    icon=ITEM_PUMP,
    prereqs=[209],
    tasks=task_crafting(0, "mekanism:machineblock", count=1, nbt=None),  # damage 10 = electric pump, mais on accepte tout
    rewards=reward_items(0, [
        make_item("mekanism:machineblock", count=4, damage=10),
        make_item("mekanism:gastank", count=4),
        ITEM_XP_2
    ])
))

# === Q11 Tu te crois encore en age 1 ===
desc_q11 = """§7§oArrete avec tes coffres en bois. Serieusement.

§7Pour l'Age 4 il te faut un §lAE2 ME System§r§7. C'est non-negociable.
§7Tu vas avoir 50 fluides differents, 80 items custom, 16 champis,
§7et il faudra qu'AE2 trie tout ca pendant que tu reflechis a la chimie.

§7Si tu galeres avec AE2 c'est normal. Ouvre le JEI ou Patchouli AE2.
§7Le ME Controller + 8 Storage Drive 16k + 1 Pattern Provider
§7suffira pour l'Age 4 entier.

§e§lObjectif : §7Crafte un ME Controller AE2
§e§lRecompense : §7Storage 16k + Crafting CPU plans"""

phase1_quests.append(make_quest(
    qid=211,
    name="§l§6Q11 -- Tu te crois encore en âge 1",
    desc=desc_q11,
    icon=ICON_AE2,
    prereqs=[210],
    tasks=task_crafting(0, "appliedenergistics2:controller", count=1),
    rewards=reward_items(0, [
        ITEM_AE2_STORAGE,
        make_item("appliedenergistics2:smart_cable", count=16),
        ITEM_XP_2
    ])
))

# === Q12 OpenComputer t'aidera ===
desc_q12 = """§7§oQuand tu auras 8 lignes industrielles qui tournent en parallele
§7tu vas vouloir un §lecran de monitoring§r§7.

§7Tu peux ecrire un script Lua qui affiche pour chaque ligne :
§7  - Niveau des reservoirs
§7  - Production / minute
§7  - Alertes si < seuil
§7  - Nombre d'items synthetises

§7Si tu connais pas Lua c'est le moment d'apprendre.
§7Sinon tu prends CC:Tweaked qui est plus simple.

§e§lObjectif : §7Crafte un PC OC T3 ou CC Computer Advanced
§e§lRecompense : §7Hardware T3 + plans monitoring"""

phase1_quests.append(make_quest(
    qid=212,
    name="§l§6Q12 -- L'OpenComputer t'aidera",
    desc=desc_q12,
    icon=ICON_OC,
    prereqs=[211],
    tasks=task_crafting(0, "opencomputers:case3", count=1),
    rewards=reward_items(0, [
        ITEM_OC_GPU,
        make_item("opencomputers:component", count=4),
        ITEM_XP_2
    ])
))

# === Q13 L'eau lourde rampe ===
desc_q13 = """§7§oD2O. L'eau lourde. 0.015% de l'eau ordinaire.

§7Mekanism te permet d'en extraire avec :
§7  Electrolytic Separator + 1000mB H2O -> 200mB Heavy Water + 800mB Sodium

§7Tu en auras besoin pour :
§7  - Moderateur du reacteur Phase 4
§7  - Cyclisation Manifoldine Phase 5

§e§lObjectif : §7Stocke 1000mB d'Eau Lourde
§e§lRecompense : §74 Heavy Water + plans Tritium"""

phase1_quests.append(make_quest(
    qid=213,
    name="§l§6Q13 -- L'eau lourde rampe",
    desc=desc_q13,
    icon=ICON_HEAVYWATER,
    prereqs=[212],
    tasks=task_retrieval(0, "mekanism:bucket", count=1, nbt={"FluidName:8": "heavywater", "Amount:3": 1000}),
    rewards=reward_items(0, [
        make_item("mekanism:bucket", count=4, nbt={"FluidName:8": "heavywater", "Amount:3": 1000}),
        ITEM_XP_2
    ])
))

# === Q14 Le tritium et la strategie ===
desc_q14 = """§7§oTritium. T2. Hydrogene avec 2 neutrons en plus.
§7Tu vas en avoir besoin pour le compose gamma3 (6LiT) Phase 4.

§7Recette : 6Li + flux neutronique -> 4He + Tritium + 4.8 MeV

§7Donc t'as besoin du reacteur de Phase 4 pour le faire.
§7§lMais§r §7tu peux deja produire 100mB en pre-stock avec
§7une Mekanism Fusion Reactor Mini (D-T fusion side product).

§7Le but de cette quete = preparer Phase 4 a l'avance.

§e§lObjectif : §7Stocke 1 bucket de Tritium
§e§lRecompense : §78 Lithium dust + 2 Bouteilles XP"""

phase1_quests.append(make_quest(
    qid=214,
    name="§l§6Q14 -- Le tritium et la stratégie",
    desc=desc_q14,
    icon=ICON_TRITIUM,
    prereqs=[213],
    tasks=task_retrieval(0, "contenttweaker:bucket_tritium", count=1),
    rewards=reward_items(0, [
        ITEM_LITHIUM,
        ITEM_XP_2
    ])
))

# === Q15 Tu peux le faire ===
desc_q15 = """§7§oFin de Phase 1. Tu as :
§7  - Eau Tridistillee a la pelle
§7  - Argon, N2, O2 stockes
§7  - 5M RF/t stable
§7  - AE2 + OC qui ronronnent
§7  - Pre-stock D2O et Tritium

§7Ouvre le Carnet Voss Vol IV chapitre 1.
§7Tu vas y trouver le plan complet de la Phase 2 — les §lmetaux§r§7.

§8§o"Tu as construit les fondations. Maintenant tu vas chercher
§8les materiaux. Trente elements. Aucun substitut accepte." — E.V.

§e§lObjectif : §7Lis le Carnet Voss Vol IV — Chapitre 1
§e§lRecompense : §7Phase 2 debloquee + 4 Bouteilles XP"""

phase1_quests.append(make_quest(
    qid=215,
    name="§l§6§nQ15 -- Phase 1 complete",
    desc=desc_q15,
    icon=ICON_CARNET_V4,
    prereqs=[214],
    tasks=task_checkbox(0),
    rewards={
        **reward_items(0, [ICON_CARNET_V4, ITEM_BREAD_8, ITEM_XP_4]),
        **reward_command(1, "/say §6[Nexus Absolu]§r §7VAR_NAME§r a complete la Phase 1 de l'Age 4. Direction la metallurgie.")
    }
))


# ============================================================
# Layout serpentin (positions x, y) -- voir doc quetes-phase1.md
# ============================================================

LAYOUT = {
    200: (0, 0),       # Q-INTRO
    201: (0, 80),      # Q1
    202: (80, 80),     # Q2
    203: (160, 80),    # Q3
    204: (240, 80),    # Q4
    205: (320, 80),    # Q5
    206: (320, 170),   # Q6
    207: (240, 170),   # Q7
    208: (160, 170),   # Q8
    209: (80, 170),    # Q9
    210: (0, 170),     # Q10
    211: (0, 260),     # Q11
    212: (80, 260),    # Q12
    213: (160, 260),   # Q13
    214: (240, 260),   # Q14
    215: (320, 260),   # Q15
}


# ============================================================
# Injection
# ============================================================

def main():
    with open(QUEST_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    quests_db = data['questDatabase:9']
    qlines = data['questLines:9']

    # Securite : verifier qu'aucun questID 200-215 n'existe deja
    existing_ids = set(int(q['questID:3']) for q in quests_db.values())
    for qid in range(200, 216):
        if qid in existing_ids:
            print(f"ERROR : questID {qid} deja existant. Abort.")
            sys.exit(1)

    # Trouver max key index dans questDatabase
    max_key_idx = max(int(k.split(':')[0]) for k in quests_db.keys())
    next_key_idx = max_key_idx + 1
    print(f"Insertion {len(phase1_quests)} quetes a partir de la cle {next_key_idx}:10")

    # Inserer chaque quete
    for q in phase1_quests:
        quests_db[f"{next_key_idx}:10"] = q
        next_key_idx += 1

    # Trouver max key index dans questLines pour creer la nouvelle line Age 4
    max_qline_idx = max(int(k.split(':')[0]) for k in qlines.keys())
    new_qline_idx = max_qline_idx + 1

    # Construire la quest line Age 4
    new_qline = {
        "properties:10": {
            "betterquesting:10": {
                "name:8": "§l§5Age 4 — L'Echappee",
                "desc:8": (
                    "§7§oProgramme Voss-7 — Phase 4 : La Brisure§r\n\n"
                    "§7L'overworld est une seconde simulation. Voss l'a decouvert.\n"
                    "§7Pour t'en echapper il a designe la §dCartouche Manifold§r§7.\n\n"
                    "§eObjectifs : §fPipeline industriel 8 lignes,\n"
                    "§f30+ elements chimiques, 16 champis Botania,\n"
                    "§fNucleaire + Magie + Chimie convergent.\n\n"
                    "§5§l\"Tu te crois libre. Tu te trompes.\""
                ),
                "visibility:8": "NORMAL",
                "bg_image:8": "",
                "bg_size:3": 256,
                "icon:10": make_item("patchouli:guide_book", nbt={
                    "patchouli:book:8": "nexusabsolu:carnet_voss_v4"
                })
            }
        },
        "quests:9": {}
    }

    # Ajouter les quetes a la questline avec leurs positions
    for i, q in enumerate(phase1_quests):
        qid = q['questID:3']
        x, y = LAYOUT[qid]
        new_qline['quests:9'][f"{i}:10"] = {
            "id:3": qid,
            "sizeX:3": 24,
            "sizeY:3": 24,
            "x:3": x,
            "y:3": y,
            "posX:3": x,
            "posY:3": y
        }

    qlines[f"{new_qline_idx}:10"] = new_qline

    # Backup avant ecriture
    import shutil
    shutil.copy(QUEST_FILE, QUEST_FILE + '.bak.before-age4-phase1')

    # Ecriture
    with open(QUEST_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"OK : {len(phase1_quests)} quetes Age 4 Phase 1 injectees.")
    print(f"  questIDs : 200..215")
    print(f"  questline 'Age 4 -- L'Echappee' creee a la key {new_qline_idx}:10")
    print(f"  Backup : {QUEST_FILE}.bak.before-age4-phase1")


if __name__ == '__main__':
    main()
