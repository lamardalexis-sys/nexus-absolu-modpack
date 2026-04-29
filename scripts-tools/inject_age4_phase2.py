#!/usr/bin/env python3
"""
Genere les 15 quetes Phase 2 (Q16-Q30 metaux) Age 4 dans
config/betterquesting/DefaultQuests.json.

Ajoute aussi les quetes a la questline 'Age 4 -- L'Echappee' existante.

Convention :
  - questIDs : 216 (Q16) -> 230 (Q30)
  - Range Age 4 : 200-279 reservee
  - Layout : sous Phase 1 (y=400 a 580)
"""
import json
import sys

QUEST_FILE = 'config/betterquesting/DefaultQuests.json'


def make_item(item_id, count=1, damage=0, nbt=None):
    item = {
        "id:8": item_id,
        "Count:3": count,
        "Damage:2": damage,
        "OreDict:8": ""
    }
    if nbt:
        item["tag:10"] = nbt
    return item


def make_oredict_item(ore, count=1):
    """Pour tasks retrieval : matche tout ingot d'un metal via OreDict."""
    return {
        "id:8": "minecraft:iron_ingot",  # placeholder, OreDict prime
        "Count:3": count,
        "Damage:2": 0,
        "OreDict:8": ore
    }


def make_quest(qid, name, desc, icon, prereqs, tasks, rewards):
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
    return {f"{index}:10": {
        "index:3": index,
        "taskID:8": "bq_standard:checkbox"
    }}


def task_retrieval_oredict(index, ore, count=1):
    """Tache retrieval qui matche par OreDict."""
    return {f"{index}:10": {
        "ignoreNBT:1": 1,
        "partialMatch:1": 1,
        "autoConsume:1": 0,
        "groupDetect:1": 0,
        "consume:1": 0,
        "taskID:8": "bq_standard:retrieval",
        "index:3": index,
        "required:9": {
            "0:10": make_oredict_item(ore, count=count)
        }
    }}


def task_retrieval_item(index, item_id, count=1, nbt=None, damage=0):
    """Tache retrieval item direct."""
    return {f"{index}:10": {
        "ignoreNBT:1": 1 if nbt is None else 0,
        "partialMatch:1": 1,
        "autoConsume:1": 0,
        "groupDetect:1": 0,
        "consume:1": 0,
        "taskID:8": "bq_standard:retrieval",
        "index:3": index,
        "required:9": {
            "0:10": make_item(item_id, count=count, damage=damage, nbt=nbt)
        }
    }}


def task_retrieval_multi(index, items_list):
    """Tache retrieval avec plusieurs items requis (AND task logic)."""
    required = {}
    for i, it in enumerate(items_list):
        required[f"{i}:10"] = it
    return {f"{index}:10": {
        "ignoreNBT:1": 1,
        "partialMatch:1": 1,
        "autoConsume:1": 0,
        "groupDetect:1": 0,
        "consume:1": 0,
        "taskID:8": "bq_standard:retrieval",
        "index:3": index,
        "required:9": required
    }}


def reward_items(index, items_list):
    rewards_dict = {}
    for i, item in enumerate(items_list):
        rewards_dict[f"{i}:10"] = item
    return {f"{index}:10": {
        "rewardID:8": "bq_standard:item",
        "index:3": index,
        "rewards:9": rewards_dict
    }}


def reward_command(index, command):
    return {f"{index}:10": {
        "hideBlockIcon:1": 1,
        "rewardID:8": "bq_standard:command",
        "asScript:1": 1,
        "description:8": "",
        "viaPlayer:1": 0,
        "index:3": index,
        "title:8": "bq_standard.reward.command",
        "command:8": command
    }}


# ============================================================
# Items recurrents
# ============================================================

ITEM_BREAD_8 = make_item("minecraft:bread", count=8)
ITEM_BREAD_16 = make_item("minecraft:bread", count=16)
ITEM_XP_2 = make_item("minecraft:experience_bottle", count=2)
ITEM_XP_4 = make_item("minecraft:experience_bottle", count=4)

# Voss Codex (Patchouli book)
ICON_VOSS = make_item("patchouli:guide_book", nbt={
    "patchouli:book:8": "nexusabsolu:voss_codex"
})

# Iron / Cu / Al (vanilla / Mekanism / Thermal)
ICON_IRON = make_item("minecraft:iron_ingot")
ICON_COPPER = make_item("thermalfoundation:material", damage=128)  # Copper ingot Thermal
ICON_ALU = make_item("thermalfoundation:material", damage=132)  # Aluminium ingot Thermal
ICON_GOLD = make_item("minecraft:gold_ingot")
ICON_LEAD = make_item("thermalfoundation:material", damage=131)  # Lead Thermal
ICON_LITHIUM = make_item("mekanism:dust", damage=4)  # Lithium dust Mek

# Plates Thermal
ITEM_IRON_PLATE = make_item("thermalfoundation:material", count=32, damage=320)
ITEM_COPPER_PLATE = make_item("thermalfoundation:material", count=32, damage=64)  # 64 = plate Cu
ITEM_ALU_PLATE = make_item("thermalfoundation:material", count=16, damage=68)  # 68 = plate Al
ITEM_GOLD_PLATE = make_item("thermalfoundation:material", count=8, damage=66)  # 66 = plate Au
ITEM_LEAD_PLATE = make_item("thermalfoundation:material", count=32, damage=67)  # 67 = plate Pb

# Titanium / Tungsten / Co / Mo / Pt / Pd / Ir (modpack-specific, on utilise OreDict)
# Iridium : EnderIO ou Mekanism

# Recompenses contenttweaker
ITEM_CRYOLITE = make_item("contenttweaker:cryolite_dust", count=16)
ITEM_CATALYSEUR_COMO = make_item("contenttweaker:catalyseur_como", count=1)
ITEM_YELLOWCAKE = make_item("contenttweaker:yellowcake_dust", count=1)


# ============================================================
# Definitions des 15 quetes Phase 2
# ============================================================

phase2_quests = []

# === Q16 Le fer ment moins ===
desc_q16 = """§7§oOK. La chimie du gaz c'est fait. Maintenant les §lmetaux§r§7.

§7Spoiler : la chimie c'est rien sans les metaux.
§7Les §lcatalyseurs§r§7 c'est des metaux.
§7Les §lparois de reacteurs§r§7 c'est des metaux.
§7L'§lampoule§r§7 de la cartouche c'est de l'iridium platine.

§7On commence simple : 64 lingots de fer pur.
§7Tu vas vouloir un Pulverizer + Induction Smelter pour x2 yield.

§e§lObjectif : §764 Iron ingots
§e§lRecompense : §732 Iron Plates + 2 Bouteilles XP"""

phase2_quests.append(make_quest(
    qid=216,
    name="§l§6Q16 -- Le fer ment moins que l'eau",
    desc=desc_q16,
    icon=ICON_IRON,
    prereqs=[215],  # Q15
    tasks=task_retrieval_oredict(0, "ingotIron", count=64),
    rewards=reward_items(0, [ITEM_IRON_PLATE, ITEM_XP_2])
))

# === Q17 Le cuivre conduit ===
desc_q17 = """§7§oCu. 8.96 g/cm3. Conduit l'electricite mieux que tout sauf l'argent.

§7Tu vas en avoir besoin pour :
§7  - Les bobines des Energy Conduits
§7  - Le Bessemer (tubes anodiques)
§7  - L'electrolyse de l'eau (electrodes)

§7Les Apatites de Forestry te donneront du phosphore en sous-produit.
§7Plus tard. Pas maintenant.

§e§lObjectif : §764 Copper ingots
§e§lRecompense : §732 Copper Plates"""

phase2_quests.append(make_quest(
    qid=217,
    name="§l§6Q17 -- Le cuivre conduit",
    desc=desc_q17,
    icon=ICON_COPPER,
    prereqs=[216],
    tasks=task_retrieval_oredict(0, "ingotCopper", count=64),
    rewards=reward_items(0, [ITEM_COPPER_PLATE, ITEM_XP_2])
))

# === Q18 L'aluminium et le procede Bayer ===
desc_q18 = """§7§oAl. Le metal qui n'existait pas a l'etat pur sur Terre avant 1825.
§7Pourquoi ? Parce qu'il fallait un courant electrique pour le faire.

§7§lProcede Bayer§r§7 : Bauxite + NaOH chaud -> NaAlO2 (aluminate) + boue rouge
§7§lProcede Hall-Heroult§r§7 : NaAlO2 + Cryolite fondue + 4V -> Al + O2

§7T'as deja le NaOH (Phase 1 Q8). Manque la cryolite (Q19).

§e§lObjectif : §732 Aluminium ingots
§e§lRecompense : §716 Aluminium Plates"""

phase2_quests.append(make_quest(
    qid=218,
    name="§l§6Q18 -- L'aluminium et le procede Bayer",
    desc=desc_q18,
    icon=ICON_ALU,
    prereqs=[217],
    tasks=task_retrieval_oredict(0, "ingotAluminium", count=32),
    rewards=reward_items(0, [ITEM_ALU_PLATE, ITEM_XP_2])
))

# === Q19 La cryolite ouvre la voie ===
desc_q19 = """§7§oNa3AlF6. La cryolite. Trouvee naturellement au Groenland en quantite
§7tellement faible qu'a la fin du 20e siecle on a du la synthetiser.

§7Synthese : 6 NaF + Al(OH)3 + 3/2 O2 -> Na3AlF6 + ...
§7Tu as deja le NaOH. Tu as deja le F2 (Phase 1 Q4 -> Cryo-distillation).

§7Stocke 64 dust. Tu vas en bruler beaucoup au Hall-Heroult.

§e§lObjectif : §764 Cryolite dust
§e§lRecompense : §716 Cryolite + 2 Bouteilles XP"""

phase2_quests.append(make_quest(
    qid=219,
    name="§l§6Q19 -- La cryolite ouvre la voie",
    desc=desc_q19,
    icon=make_item("contenttweaker:cryolite_dust"),
    prereqs=[218],
    tasks=task_retrieval_item(0, "contenttweaker:cryolite_dust", count=64),
    rewards=reward_items(0, [ITEM_CRYOLITE, ITEM_XP_2])
))

# === Q20 Le titane ===
desc_q20 = """§7§oTi. Resistance/poids champion. Indispensable pour la cartouche
§7(le casing externe doit resister a 800 C cyclisation).

§7§lProcede Kroll§r§7 :
§7  TiO2 + 2 Cl2 + C -> TiCl4 + CO2  (chloruration)
§7  TiCl4 + 2 Mg -> Ti + 2 MgCl2     (reduction sous Argon)

§7Pour ca il te faut :
§7  - Cl2 (Phase 1 cryo-distillation Saumure)
§7  - Argon (Phase 1 Q6, atmosphere inerte)
§7  - Mg (a faire sur Magnesite + reduction Pidgeon)

§e§lObjectif : §716 Titanium ingots
§e§lRecompense : §78 Ti Plates + plans Tungstene"""

phase2_quests.append(make_quest(
    qid=220,
    name="§l§6Q20 -- Le titane, ce difficile",
    desc=desc_q20,
    icon=make_item("contenttweaker:cryo_distillateur_controller"),  # placeholder, vrai Ti ingot ID dependant modpack
    prereqs=[219],
    tasks=task_retrieval_oredict(0, "ingotTitanium", count=16),
    rewards=reward_items(0, [
        make_oredict_item("plateTitanium", count=8),
        ITEM_XP_2
    ])
))

# === Q21 Le tungstene ===
desc_q21 = """§7§oW (de Wolfram en allemand). Point de fusion 3422 C. Champion absolu.

§7Tu vas en avoir besoin pour :
§7  - Catalyseur CoMo (HDS petrochimie Phase 3)
§7  - Filaments des cables d'arc Hall-Heroult
§7  - Casing chambre cyclisation Manifold

§7Spoiler : tu vas en cramer beaucoup.

§e§lObjectif : §78 Tungsten ingots
§e§lRecompense : §74 W Plates + plans Cobalt"""

phase2_quests.append(make_quest(
    qid=221,
    name="§l§6Q21 -- Le tungstene, le plus dur des metaux",
    desc=desc_q21,
    icon=make_oredict_item("ingotTungsten"),
    prereqs=[220],
    tasks=task_retrieval_oredict(0, "ingotTungsten", count=8),
    rewards=reward_items(0, [
        make_oredict_item("plateTungsten", count=4),
        ITEM_XP_2
    ])
))

# === Q22 Cobalt + Mo ===
desc_q22 = """§7§oCo et Mo. Les deux pour faire le §lcatalyseur CoMo§r§7.

§7Le CoMo (cobalt-molybdene sulfure) c'est ce qui rend la
§7§lhydrodesulfuration§r§7 possible :
§7  Petrole + H2 + CoMo -> petrole desulfure + H2S

§7Sans ce truc tu peux pas raffiner ton kerosene Phase 3.
§7Et sans kerosene raffine pas de Solvant Neutre alpha.
§7Et sans Solvant Neutre alpha pas de cartouche.

§7Tu vois la chaine de dependance ?

§e§lObjectif : §78 Co + 8 Mo
§e§lRecompense : §71 Catalyseur CoMo (custom) + plans Pt/Pd"""

phase2_quests.append(make_quest(
    qid=222,
    name="§l§6Q22 -- Le cobalt et le Mo, ces deux la",
    desc=desc_q22,
    icon=make_item("contenttweaker:catalyseur_como"),
    prereqs=[221],
    tasks=task_retrieval_multi(0, [
        make_oredict_item("ingotCobalt", count=8),
        make_oredict_item("ingotMolybdenum", count=8)
    ]),
    rewards=reward_items(0, [ITEM_CATALYSEUR_COMO, ITEM_XP_2])
))

# === Q23 Pt + Pd ===
desc_q23 = """§7§oPt et Pd. Metaux nobles ultra rares.

§7Tu en auras besoin pour :
§7  - Pot d'echappement Bessemer (Pt comme catalyseur Ostwald)
§7  - Membrane de cyclisation (Pd selectif H2)
§7  - Encartouchage (electrodes inertes)

§7Tu peux les obtenir via Mekanism Tier 4 ore processing
§7(double dust + chemical washer + dissolution + crystallizer).

§e§lObjectif : §74 Pt + 4 Pd
§e§lRecompense : §72 Plates each + plans Iridium"""

phase2_quests.append(make_quest(
    qid=223,
    name="§l§6Q23 -- Platine et palladium en parallele",
    desc=desc_q23,
    icon=make_oredict_item("ingotPlatinum"),
    prereqs=[222],
    tasks=task_retrieval_multi(0, [
        make_oredict_item("ingotPlatinum", count=4),
        make_oredict_item("ingotPalladium", count=4)
    ]),
    rewards=reward_items(0, [
        make_oredict_item("platePlatinum", count=2),
        make_oredict_item("platePalladium", count=2),
        ITEM_XP_2
    ])
))

# === Q24 Iridium ===
desc_q24 = """§7§oIr. 22.56 g/cm3. Le plus dense apres l'osmium. Si tu fais tomber
§7un cube de 1 cm3 sur ton pied il pese 22 grammes mais te casse l'os.

§7L'iridium c'est l'§lampoule§r§7 de la cartouche. Il faut qu'elle
§7soit assez resistante pour ne pas reagir avec les composes
§7delta et epsilon de la Phase 5 (qui tueraient n'importe quel metal).

§7Iridium = Mekanism T4 + AE2 sieve sur Mining Dimension Crushed Ores.
§7Bonne chasse.

§e§lObjectif : §74 Iridium ingots
§e§lRecompense : §72 Ir Plates + plans Or"""

phase2_quests.append(make_quest(
    qid=224,
    name="§l§6Q24 -- L'iridium, l'element le plus dense",
    desc=desc_q24,
    icon=make_oredict_item("ingotIridium"),
    prereqs=[223],
    tasks=task_retrieval_oredict(0, "ingotIridium", count=4),
    rewards=reward_items(0, [
        make_oredict_item("plateIridium", count=2),
        ITEM_XP_2
    ])
))

# === Q25 Or ===
desc_q25 = """§7§oAu. Le metal de l'humanite. Brillant. Stable. Conducteur.

§7Pour la cartouche tu en as besoin pour :
§7  - Contacts electriques inertes
§7  - Solvant Eau Regale (3 HCl + 1 HNO3 -> dissout l'or)
§7    qu'on utilisera Phase 3 pour activer Au -> AuCl3

§e§lObjectif : §732 Gold ingots
§e§lRecompense : §78 Au Plates + plans Eau Regale"""

phase2_quests.append(make_quest(
    qid=225,
    name="§l§6Q25 -- L'or, le metal noble",
    desc=desc_q25,
    icon=ICON_GOLD,
    prereqs=[224],
    tasks=task_retrieval_oredict(0, "ingotGold", count=32),
    rewards=reward_items(0, [ITEM_GOLD_PLATE, ITEM_XP_2])
))

# === Q26 Plomb ===
desc_q26 = """§7§oPb. Le metal qui a empoisonne l'Empire Romain.
§7Densite 11.3 g/cm3. Conduit pas grand chose. Mais !
§7Il bloque les rayons gamma comme personne.

§7Tu vas en avoir besoin Phase 4 :
§7  - Blindage du reacteur fission
§7  - Conteneurs UF6 (uranium hexafluoride)
§7  - Doublure capsule Pu-Be

§7Stocke 64. La Phase 4 te demandera 200 plates en pic.

§e§lObjectif : §764 Lead ingots
§e§lRecompense : §732 Pb Plates + plans blindage"""

phase2_quests.append(make_quest(
    qid=226,
    name="§l§6Q26 -- Le plomb, ce vieux con",
    desc=desc_q26,
    icon=ICON_LEAD,
    prereqs=[225],
    tasks=task_retrieval_oredict(0, "ingotLead", count=64),
    rewards=reward_items(0, [ITEM_LEAD_PLATE, ITEM_XP_2])
))

# === Q27 Uranium ===
desc_q27 = """§7§oU. 92 protons. Element le plus lourd que tu trouves a l'etat naturel.

§7Le minerai s'appelle "yellowcake" (U3O8). On va le fluorer en UF6
§7gazeux Phase 4 pour le centrifuger et l'enrichir.

§7Pour l'instant on stocke 4 dust.
§7Le minage te tue ? Active la radiation immunity (potion).

§8§o"L'uranium est le seul element naturel qui souviens
§8de la nucleosynthese stellaire qui l'a cree." — E.V.

§e§lObjectif : §74 Uranium ingots ou dust
§e§lRecompense : §71 Yellowcake + plans UF6"""

phase2_quests.append(make_quest(
    qid=227,
    name="§l§6Q27 -- L'uranium est jaune",
    desc=desc_q27,
    icon=make_oredict_item("ingotUranium"),
    prereqs=[226],
    tasks=task_retrieval_oredict(0, "ingotUranium", count=4),
    rewards=reward_items(0, [ITEM_YELLOWCAKE, ITEM_XP_2])
))

# === Q28 Lithium ===
desc_q28 = """§7§oLi. 3 protons. L'element le plus leger qui soit solide a 25 C.

§7Tu l'as deja croise (Phase 1 Q14 prestock Tritium).
§7Maintenant tu en stockes 32 ingots pour :
§7  - Compose gamma3 = 6Li + Tritium = 6LiT
§7  - Source neutronique reacteur fission (Li-6 + n -> T + 4He)
§7  - Beryllium-Li alloys pour le pot reactif Phase 5

§e§lObjectif : §732 Lithium ingots
§e§lRecompense : §716 Li Plates + plans Tritium"""

phase2_quests.append(make_quest(
    qid=228,
    name="§l§6Q28 -- Le lithium dans la roche",
    desc=desc_q28,
    icon=ICON_LITHIUM,
    prereqs=[227],
    tasks=task_retrieval_oredict(0, "ingotLithium", count=32),
    rewards=reward_items(0, [
        make_oredict_item("plateLithium", count=16),
        ITEM_XP_2
    ])
))

# === Q29 Be + Mg ===
desc_q29 = """§7§oBe (4) et Mg (12). Group 2 du tableau periodique.
§7Tous les deux ultra-reactifs avec H2O et O2. Tu les stockes
§7sous Argon (Phase 1 Q6).

§7Pour la cartouche :
§7  - Be -> Capsule Pu-Be (source neutronique gamma2)
§7  - Mg -> Reduction Kroll TiCl4 (etape Q20 Titane)

§7Si t'as pas encore stocke 32 Mg, ton procede Kroll est a sec.

§e§lObjectif : §716 Be + 32 Mg
§e§lRecompense : §7Plates each + plans Pu-Be"""

phase2_quests.append(make_quest(
    qid=229,
    name="§l§6Q29 -- Beryllium et magnesium, les jumeaux",
    desc=desc_q29,
    icon=make_oredict_item("ingotBeryllium"),
    prereqs=[228],
    tasks=task_retrieval_multi(0, [
        make_oredict_item("ingotBeryllium", count=16),
        make_oredict_item("ingotMagnesium", count=32)
    ]),
    rewards=reward_items(0, [
        make_oredict_item("plateBeryllium", count=8),
        make_oredict_item("plateMagnesium", count=16),
        ITEM_XP_4
    ])
))

# === Q30 fin Phase 2 ===
desc_q30 = """§7§oCompte tes lingots, sujet 47.

§7Fe, Cu, Al, Ti, W, Co, Mo, Pt, Pd, Ir, Au, Pb, U, Li, Be, Mg.
§7Plus tous les natifs (Sn, Ag, Ni, Zn, Bi, Mn, Cr, V, Si, P, S, Na, Cl, F, K).

§7Tu as les 30 elements. La Phase 2 est terminee.

§8§o"Tu as transmute la Terre. Maintenant tu vas transmuter
§8les molecules. Phase 3 : la chimie organique." — E.V.

§e§lObjectif : §7Lis Voss Codex chap.2 (Pyrometallurgie)
§e§lRecompense : §7Phase 3 debloquee + 4 Bouteilles XP"""

phase2_quests.append(make_quest(
    qid=230,
    name="§l§6§nQ30 -- 30 elements, fin de Phase 2",
    desc=desc_q30,
    icon=ICON_VOSS,
    prereqs=[229],
    tasks=task_checkbox(0),
    rewards={
        **reward_items(0, [ICON_VOSS, ITEM_BREAD_16, ITEM_XP_4]),
        **reward_command(1, "/say §6[Nexus Absolu]§r §7VAR_NAME§r a complete la Phase 2 -- 30 elements reunis. Direction la chimie seche.")
    }
))


# ============================================================
# Layout (sous Phase 1, y=400 a 580)
# ============================================================

LAYOUT = {
    216: (0, 400),     # Q16
    217: (80, 400),    # Q17
    218: (160, 400),   # Q18
    219: (240, 400),   # Q19
    220: (320, 400),   # Q20
    221: (320, 490),   # Q21
    222: (240, 490),   # Q22
    223: (160, 490),   # Q23
    224: (80, 490),    # Q24
    225: (0, 490),     # Q25
    226: (0, 580),     # Q26
    227: (80, 580),    # Q27
    228: (160, 580),   # Q28
    229: (240, 580),   # Q29
    230: (320, 580),   # Q30
}


def main():
    with open(QUEST_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    quests_db = data['questDatabase:9']
    qlines = data['questLines:9']

    # Securite : verifier qu'aucun questID 216-230 n'existe deja
    existing_ids = set(int(q['questID:3']) for q in quests_db.values())
    for qid in range(216, 231):
        if qid in existing_ids:
            print(f"ERROR : questID {qid} deja existant. Abort.")
            sys.exit(1)

    # Trouver max key index
    max_key_idx = max(int(k.split(':')[0]) for k in quests_db.keys())
    next_key_idx = max_key_idx + 1
    print(f"Insertion {len(phase2_quests)} quetes a partir de la cle {next_key_idx}:10")

    for q in phase2_quests:
        quests_db[f"{next_key_idx}:10"] = q
        next_key_idx += 1

    # Trouver questline Age 4 et y ajouter les quetes
    age4_qline_key = None
    for k, ql in qlines.items():
        name = ql['properties:10']['betterquesting:10'].get('name:8', '')
        if 'Age 4' in name:
            age4_qline_key = k
            break

    if age4_qline_key is None:
        print("ERROR : questline Age 4 introuvable. Abort.")
        sys.exit(1)

    age4_qline = qlines[age4_qline_key]
    # Trouver max index dans quests:9 de la questline
    max_qline_idx = max(int(k.split(':')[0]) for k in age4_qline['quests:9'].keys())
    next_qline_idx = max_qline_idx + 1

    for q in phase2_quests:
        qid = q['questID:3']
        x, y = LAYOUT[qid]
        age4_qline['quests:9'][f"{next_qline_idx}:10"] = {
            "id:3": qid,
            "sizeX:3": 24,
            "sizeY:3": 24,
            "x:3": x,
            "y:3": y,
            "posX:3": x,
            "posY:3": y
        }
        next_qline_idx += 1

    # Backup
    import shutil
    shutil.copy(QUEST_FILE, QUEST_FILE + '.bak.before-age4-phase2')

    with open(QUEST_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"OK : {len(phase2_quests)} quetes Age 4 Phase 2 injectees.")
    print(f"  questIDs : 216..230")
    print(f"  Ajoutees a la questline 'Age 4 -- L'Echappee'")
    print(f"  Backup : {QUEST_FILE}.bak.before-age4-phase2")


if __name__ == '__main__':
    main()
