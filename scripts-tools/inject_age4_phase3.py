#!/usr/bin/env python3
"""
Injecte les 15 quetes Phase 3 (Q31-Q45 Chimie Seche) Age 4 dans BQ.

Convention :
  - questIDs : 231 (Q31) -> 245 (Q45)
  - Layout : sous Phase 2 (y=720 a 900)
"""
import json
import sys
import shutil

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


def task_retrieval_item(index, item_id, count=1, nbt=None):
    return {f"{index}:10": {
        "ignoreNBT:1": 1 if nbt is None else 0,
        "partialMatch:1": 1,
        "autoConsume:1": 0,
        "groupDetect:1": 0,
        "consume:1": 0,
        "taskID:8": "bq_standard:retrieval",
        "index:3": index,
        "required:9": {
            "0:10": make_item(item_id, count=count, nbt=nbt)
        }
    }}


def task_retrieval_fluid_bucket(index, fluid_name, mod_id, count=1):
    """Retrieve N buckets remplis du fluide donne (via NBT FluidName)."""
    nbt = {"FluidName:8": fluid_name, "Amount:3": 1000}
    return {f"{index}:10": {
        "ignoreNBT:1": 0,
        "partialMatch:1": 1,
        "autoConsume:1": 0,
        "groupDetect:1": 0,
        "consume:1": 0,
        "taskID:8": "bq_standard:retrieval",
        "index:3": index,
        "required:9": {
            "0:10": make_item(mod_id, count=count, nbt=nbt)
        }
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


# Items recurrents
ITEM_BREAD_16 = make_item("minecraft:bread", count=16)
ITEM_XP_2 = make_item("minecraft:experience_bottle", count=2)
ITEM_XP_4 = make_item("minecraft:experience_bottle", count=4)
ICON_VOSS = make_item("patchouli:guide_book", nbt={
    "patchouli:book:8": "nexusabsolu:voss_codex"
})

# Items Phase 3 specific
ICON_COMPOSE_A = make_item("contenttweaker:compose_alpha")
ICON_COMPOSE_B = make_item("contenttweaker:compose_beta")
ICON_PHENOL = make_item("contenttweaker:phenol_substitue")
ICON_CARBONE_AU = make_item("contenttweaker:carbone_actif_au")
ICON_CATALYSEUR_COMO = make_item("contenttweaker:catalyseur_como")

# Fluide buckets via Forge bucket
def fluid_bucket(fluid_name, count=1):
    return make_item("forge:bucketfilled", count=count,
                     nbt={"FluidName:8": fluid_name, "Amount:3": 1000})

ICON_CRUDE = fluid_bucket("crudeoil")
ICON_KEROSENE_DESULF = fluid_bucket("kerosene_desulfured")
ICON_KEROSENE_PREMIUM = fluid_bucket("kerosene_premium")
ICON_SOLVANT_ALPHA = fluid_bucket("solvant_alpha")
ICON_AMMONIAQUE = fluid_bucket("ammoniaque")
ICON_HNO3 = fluid_bucket("hno3")
ICON_H2SO4 = fluid_bucket("h2so4")
ICON_HCL = fluid_bucket("hcl")
ICON_ACETONE = fluid_bucket("acetone")
ICON_ETHER_ETOILE = fluid_bucket("ether_etoile")


# ============================================================
phase3_quests = []

# === Q31 Le petrole brut ===
desc_q31 = """§7§oTu commences la chimie organique avec ce que la Terre te donne de pire :
§7du §lpetrole brut§r§7. C'est un melange chaotique de 200 hydrocarbures
§7+ soufre + metaux lourds + bouts d'animaux du Cretace.

§7Avant de pouvoir l'utiliser tu vas devoir :
§7  1. Le pomper (Pumpjack IP/PneumaticCraft)
§7  2. Le degazer (separer le gaz naturel)
§7  3. Le distiller (kerosene + naphta + diesel)
§7  4. Le desulfurer (catalyseur CoMo de Phase 2 Q22)
§7  5. Le re-cracker (Cracker IP) -> Solvant Neutre alpha

§7Cette quete : juste avoir 8 buckets de crude.

§e§lObjectif : §78 buckets Crude Oil
§e§lRecompense : §7Plans Distillation + 2 Bouteilles XP"""

phase3_quests.append(make_quest(
    qid=231,
    name="§l§6Q31 -- Le petrole brut, ce melange degueulasse",
    desc=desc_q31,
    icon=ICON_CRUDE,
    prereqs=[230],
    tasks=task_retrieval_fluid_bucket(0, "crudeoil", "forge:bucketfilled", count=8),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q32 Distillation atmospherique ===
desc_q32 = """§7§oDistillation atmospherique = chauffer le crude a 350 C et separer
§7par point d'ebullition :
§7  - 30-200 C  -> naphta (essence brute)
§7  - 200-300 C -> kerosene (jet fuel)
§7  - 300-350 C -> diesel
§7  - residu lourd -> bitume

§7Tu vas vouloir le kerosene. C'est notre base pour le Solvant Neutre.

§7L'IP Distillation Tower fait ca naturellement. Sinon le Crude Oil
§7Distillation Multiblock de PneumaticCraft.

§e§lObjectif : §74 buckets Kerosene Desulfure
§e§lRecompense : §7Plans HDS + 2 Bouteilles XP"""

phase3_quests.append(make_quest(
    qid=232,
    name="§l§6Q32 -- La distillation atmospherique",
    desc=desc_q32,
    icon=ICON_KEROSENE_DESULF,
    prereqs=[231],
    tasks=task_retrieval_fluid_bucket(0, "kerosene_desulfured", "forge:bucketfilled", count=4),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q33 HDS et CoMo ===
desc_q33 = """§7§oTu as ton kerosene. Mais il contient 0.3% de soufre.
§7Pour le Solvant Neutre il faut moins de 10 ppm.

§7§lHDS§r§7 (HydroDeSulfuration) :
§7  Kerosene + H2 + CoMo a 350 C 50 atm -> Kerosene_premium + H2S

§7Le H2S sera bridge vers la Phase 6 (Procede Claus -> Soufre pur).
§7Tu connais maintenant les §lcatalyseurs§r§7.

§8§o"Sans catalyseur la chimie est lente. Avec catalyseur la chimie
§8est tactique. Voila pourquoi tu es la Sujet 47." — E.V.

§e§lObjectif : §74 buckets Kerosene Premium (desulfure < 10 ppm)
§e§lRecompense : §78 Catalyseur CoMo bonus + plans Cracker"""

phase3_quests.append(make_quest(
    qid=233,
    name="§l§6Q33 -- L'hydrodesulfuration et le CoMo",
    desc=desc_q33,
    icon=ICON_KEROSENE_PREMIUM,
    prereqs=[232],
    tasks=task_retrieval_fluid_bucket(0, "kerosene_premium", "forge:bucketfilled", count=4),
    rewards=reward_items(0, [
        make_item("contenttweaker:catalyseur_como", count=8),
        ITEM_XP_2
    ])
))

# === Q34 Cracker Solvant alpha ===
desc_q34 = """§7§oCracker le Kerosene Premium :
§7  Kerosene + chaleur 800 C + catalyseur Pt -> alcanes leger + alcenes

§7On retient les alcenes ramifies. Avec un peu d'aromatique benzene
§7on obtient le §lSolvant Neutre alpha§r§7.

§7C'est la base de l'§lampoule§r§7 chimique de la cartouche.
§7Ca dissout n'importe quoi sauf l'eau et les metaux nobles.

§e§lObjectif : §72 buckets Solvant alpha
§e§lRecompense : §7Plans Composes alpha + 2 Bouteilles XP"""

phase3_quests.append(make_quest(
    qid=234,
    name="§l§6Q34 -- Le craquage et le Solvant alpha",
    desc=desc_q34,
    icon=ICON_SOLVANT_ALPHA,
    prereqs=[233],
    tasks=task_retrieval_fluid_bucket(0, "solvant_alpha", "forge:bucketfilled", count=2),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q35 Compose alpha ===
desc_q35 = """§7§oOK ton premier compose intermediaire.

§7§lCompose alpha§r§7 = 1 Solvant alpha + 1 H2SO4 + 1 HNO3 a basse temp.
§7C'est un acide super-acide stabilise (proton tres reactif).

§7Tu vas l'utiliser pour activer le centre cyclique du compose epsilon
§7final (Phase 5).

§7Pour l'instant garde-en 4 dans un Resonant Tank dedie. Ne le mets
§7pas en contact avec l'eau ordinaire ou ca explose.

§e§lObjectif : §71 Compose alpha synthese
§e§lRecompense : §74 Compose alpha + plans Haber-Bosch"""

phase3_quests.append(make_quest(
    qid=235,
    name="§l§6Q35 -- Compose alpha, l'acide stabilise",
    desc=desc_q35,
    icon=ICON_COMPOSE_A,
    prereqs=[234],
    tasks=task_retrieval_item(0, "contenttweaker:compose_alpha", count=1),
    rewards=reward_items(0, [
        make_item("contenttweaker:compose_alpha", count=4),
        ITEM_XP_2
    ])
))

# === Q36 Haber-Bosch ===
desc_q36 = """§7§oFritz Haber. 1908. Procede industriel le plus important du 20e siecle :
§7  N2 + 3 H2 -> 2 NH3   (450 C, 200 atm, catalyseur Fe)

§7Sans Haber-Bosch, pas d'engrais. Pas d'agriculture moderne.
§7Pas de 8 milliards d'humains. C'est ce processus la qui a permis
§7a l'humanite de se multiplier x4 en un siecle.

§7T'as besoin de :
§7  - 8 N2 (Phase 1 Q7)
§7  - 24 H2 (electrolyse, depuis Phase 1)
§7  - Un Fischer Tropsch Reactor (Mekanism Pressurized Reaction Chamber)

§e§lObjectif : §78 buckets Ammoniaque
§e§lRecompense : §7Plans Ostwald + 2 Bouteilles XP"""

phase3_quests.append(make_quest(
    qid=236,
    name="§l§6Q36 -- Haber-Bosch et l'ammoniaque",
    desc=desc_q36,
    icon=ICON_AMMONIAQUE,
    prereqs=[235],
    tasks=task_retrieval_fluid_bucket(0, "ammoniaque", "forge:bucketfilled", count=8),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q37 Ostwald HNO3 ===
desc_q37 = """§7§oWilhelm Ostwald. 1902. Suite logique d'Haber-Bosch :
§7  4 NH3 + 5 O2 -> 4 NO + 6 H2O   (catalyseur Pt-Rh, 850 C)
§7  2 NO + O2 -> 2 NO2
§7  3 NO2 + H2O -> 2 HNO3 + NO

§7L'acide nitrique sert a :
§7  - Synthese Compose alpha (Q35) -- deja stocke ?
§7  - Eau Regale (3 HCl + HNO3) pour Au -> AuCl3 (Q44)
§7  - Explosifs (mais on en fait pas pour l'Age 4)

§e§lObjectif : §78 buckets HNO3
§e§lRecompense : §7Plans H2SO4 + 2 Bouteilles XP"""

phase3_quests.append(make_quest(
    qid=237,
    name="§l§6Q37 -- Ostwald et l'acide nitrique",
    desc=desc_q37,
    icon=ICON_HNO3,
    prereqs=[236],
    tasks=task_retrieval_fluid_bucket(0, "hno3", "forge:bucketfilled", count=8),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q38 H2SO4 ===
desc_q38 = """§7§oH2SO4. Le composant chimique le plus produit au monde
§7(245 millions de tonnes par an). C'est un §lindicateur§r§7
§7du PIB industriel d'un pays.

§7§lProcede de contact§r§7 :
§7  S + O2 -> SO2  (Procede Claus du H2S Phase 3 ?)
§7  2 SO2 + O2 -> 2 SO3  (catalyseur V2O5, 450 C)
§7  SO3 + H2O -> H2SO4

§7Pour la cartouche : Compose alpha, activation Compose beta,
§7cleaning catalyseur Pt entre cycles.

§e§lObjectif : §78 buckets H2SO4
§e§lRecompense : §7Plans HCl + 2 Bouteilles XP"""

phase3_quests.append(make_quest(
    qid=238,
    name="§l§6Q38 -- L'acide sulfurique, l'industrie en bouteille",
    desc=desc_q38,
    icon=ICON_H2SO4,
    prereqs=[237],
    tasks=task_retrieval_fluid_bucket(0, "h2so4", "forge:bucketfilled", count=8),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q39 HCl ===
desc_q39 = """§7§oHCl. Plus simple : H2 + Cl2 -> 2 HCl, brule a la flamme.

§7Tu en as besoin pour :
§7  - Eau Regale (3 HCl + HNO3 -> dissout Au)
§7  - Pickling des metaux (decapage avant Bessemer)
§7  - Compose beta (Phase 5 organometalliques)

§e§lObjectif : §74 buckets HCl
§e§lRecompense : §7Plans Eau Regale + 2 Bouteilles XP"""

phase3_quests.append(make_quest(
    qid=239,
    name="§l§6Q39 -- L'acide chlorhydrique",
    desc=desc_q39,
    icon=ICON_HCL,
    prereqs=[238],
    tasks=task_retrieval_fluid_bucket(0, "hcl", "forge:bucketfilled", count=4),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q40 Compose beta ===
desc_q40 = """§7§oL'§lEau Regale§r§7 (regis aqua = "eau royale" en latin) :
§7  3 HCl + HNO3 -> 2 H2O + NOCl + Cl2

§7C'est le seul truc qui dissout l'or et le platine. Donc on l'utilise
§7pour activer ces metaux nobles en chlorides solubles, qui se
§7lient ensuite aux ligands organiques.

§7§lCompose beta§r§7 = AuCl3 + benzene + tributylphosphine -> bizarre
§7complexe organometallique cyan. Mort si tu le touches a main nue.

§e§lObjectif : §71 Compose beta
§e§lRecompense : §72 Compose beta + plans Acetone"""

phase3_quests.append(make_quest(
    qid=240,
    name="§l§6Q40 -- Eau Regale et Compose beta",
    desc=desc_q40,
    icon=ICON_COMPOSE_B,
    prereqs=[239],
    tasks=task_retrieval_item(0, "contenttweaker:compose_beta", count=1),
    rewards=reward_items(0, [
        make_item("contenttweaker:compose_beta", count=2),
        ITEM_XP_2
    ])
))

# === Q41 Acetone ===
desc_q41 = """§7§o§lProcede Cumene-Hock§r§7. Inventee 1944. Produit 90% de l'acetone
§7mondiale + 95% du phenol mondial en meme temps.

§7  Benzene + Propylene -> Cumene  (catalyseur H3PO4)
§7  Cumene + O2 -> Cumene Hydroperoxide
§7  CumOH -> Acetone + Phenol  (rupture acide)

§7Phenol : pour les composes magiques Phase 5.
§7Acetone : solvant pour la Manifoldine.

§e§lObjectif : §74 buckets Acetone
§e§lRecompense : §7Plans Phenol + 2 Bouteilles XP"""

phase3_quests.append(make_quest(
    qid=241,
    name="§l§6Q41 -- Le cumene et l'acetone",
    desc=desc_q41,
    icon=ICON_ACETONE,
    prereqs=[240],
    tasks=task_retrieval_fluid_bucket(0, "acetone", "forge:bucketfilled", count=4),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q42 Ether Etoile ===
desc_q42 = """§7§oEther stabilise par exposition prolongee a la Liquid Starlight
§7(Astral Sorcery). Phase intermediaire avant Compose epsilon.

§7Recette :
§7  Acetone + Heavy Water + Liquid Starlight (Mekanism Reaction Chamber)
§7  -> Ether Etoile (1000mB output / 4000 input)

§7Spoiler : ca commence a sentir la magie. Phase 5 va etre tendue.

§e§lObjectif : §72 buckets Ether Etoile
§e§lRecompense : §7Plans Phenol substitue + 2 Bouteilles XP"""

phase3_quests.append(make_quest(
    qid=242,
    name="§l§6Q42 -- L'ether etoile",
    desc=desc_q42,
    icon=ICON_ETHER_ETOILE,
    prereqs=[241],
    tasks=task_retrieval_fluid_bucket(0, "ether_etoile", "forge:bucketfilled", count=2),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q43 Phenol substitue ===
desc_q43 = """§7§oPhenol modifie pour porter 2 groupes -OCH3 (methoxy) en para
§7(positions 2-4 du cycle benzenique).

§7Synthese : Phenol + 2 MeOH + H2SO4 catalyseur 180 C.
§7C'est tendu mais ca marche.

§7Le phenol substitue est le §lcoeur aromatique§r§7 de la Manifoldine.
§7C'est lui qui se lie au lithium dans la cyclisation finale.

§e§lObjectif : §74 phenol substitue
§e§lRecompense : §78 phenol bonus + plans Composes Voss"""

phase3_quests.append(make_quest(
    qid=243,
    name="§l§6Q43 -- Phenol substitue, le precurseur",
    desc=desc_q43,
    icon=ICON_PHENOL,
    prereqs=[242],
    tasks=task_retrieval_item(0, "contenttweaker:phenol_substitue", count=4),
    rewards=reward_items(0, [
        make_item("contenttweaker:phenol_substitue", count=8),
        ITEM_XP_2
    ])
))

# === Q44 Carbone Au ===
desc_q44 = """§7§oCharbon active impregne d'AuCl3 a 0.5 wt%.
§7Catalyseur ultra-selectif pour la cyclisation epsilon Phase 5.

§7Recette :
§7  Charbon + AuCl3 (Eau Regale) -> Charbon Au impregne
§7  Sechage 110 C 24h sous Argon

§7Stocke 8. La cyclisation Manifoldine va en avaler 4 d'un coup.

§e§lObjectif : §78 Carbone Actif Au
§e§lRecompense : §716 Carbone Au + plans Synthese Voss"""

phase3_quests.append(make_quest(
    qid=244,
    name="§l§6Q44 -- Le carbone active et l'or charge",
    desc=desc_q44,
    icon=ICON_CARBONE_AU,
    prereqs=[243],
    tasks=task_retrieval_item(0, "contenttweaker:carbone_actif_au", count=8),
    rewards=reward_items(0, [
        make_item("contenttweaker:carbone_actif_au", count=16),
        ITEM_XP_2
    ])
))

# === Q45 Fin Phase 3 ===
desc_q45 = """§7§oFin de Phase 3. Tu as :
§7  - Solvant Neutre alpha
§7  - Compose alpha (super-acide stabilise)
§7  - Compose beta (organometallique Au-PR3)
§7  - Acetone + Ether Etoile + Phenol substitue
§7  - Carbone Au charge

§7Le carnet de Voss s'ouvre sur le chapitre 3 :
§7  §6"La chimie t'a fait suer. Maintenant tu vas faire chauffer
§6  des trucs dangereux. Phase 4 : nucleaire."§r

§8§o"Le H et le N et le O et le C ne sont que des poupees russes.
§8Pour cracker la simulation il faut casser le NOYAU. Bienvenue
§8dans le Theoreme V." — E.V.

§e§lObjectif : §7Lis Voss Codex chap.3 (Chimie Seche)
§e§lRecompense : §7Phase 4 nucleaire debloquee + 4 Bouteilles XP"""

phase3_quests.append(make_quest(
    qid=245,
    name="§l§6§nQ45 -- Phase 3 complete, fin de la chimie seche",
    desc=desc_q45,
    icon=ICON_VOSS,
    prereqs=[244],
    tasks=task_checkbox(0),
    rewards={
        **reward_items(0, [ICON_VOSS, ITEM_BREAD_16, ITEM_XP_4]),
        **reward_command(1, "/say §6[Nexus Absolu]§r §7VAR_NAME§r a complete la Phase 3 -- chimie seche maitrisee. Direction le nucleaire.")
    }
))


# Layout sous Phase 2 (y=720 a y=900)
LAYOUT = {
    231: (0, 720),     # Q31
    232: (80, 720),    # Q32
    233: (160, 720),   # Q33
    234: (240, 720),   # Q34
    235: (320, 720),   # Q35
    236: (320, 810),   # Q36
    237: (240, 810),   # Q37
    238: (160, 810),   # Q38
    239: (80, 810),    # Q39
    240: (0, 810),     # Q40
    241: (0, 900),     # Q41
    242: (80, 900),    # Q42
    243: (160, 900),   # Q43
    244: (240, 900),   # Q44
    245: (320, 900),   # Q45
}


def main():
    with open(QUEST_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    quests_db = data['questDatabase:9']
    qlines = data['questLines:9']

    # Securite collisions
    existing_ids = set(int(q['questID:3']) for q in quests_db.values())
    for qid in range(231, 246):
        if qid in existing_ids:
            print(f"ERROR : questID {qid} deja existant. Abort.")
            sys.exit(1)

    max_key_idx = max(int(k.split(':')[0]) for k in quests_db.keys())
    next_key_idx = max_key_idx + 1
    print(f"Insertion {len(phase3_quests)} quetes a partir de la cle {next_key_idx}:10")

    for q in phase3_quests:
        quests_db[f"{next_key_idx}:10"] = q
        next_key_idx += 1

    # Ajouter a la questline Age 4
    age4_qline_key = None
    for k, ql in qlines.items():
        name = ql['properties:10']['betterquesting:10'].get('name:8', '')
        if 'Age 4' in name:
            age4_qline_key = k
            break

    if age4_qline_key is None:
        print("ERROR : questline Age 4 introuvable.")
        sys.exit(1)

    age4_qline = qlines[age4_qline_key]
    max_qline_idx = max(int(k.split(':')[0]) for k in age4_qline['quests:9'].keys())
    next_qline_idx = max_qline_idx + 1

    for q in phase3_quests:
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

    shutil.copy(QUEST_FILE, QUEST_FILE + '.bak.before-age4-phase3')
    with open(QUEST_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"OK : {len(phase3_quests)} quetes Age 4 Phase 3 injectees.")
    print(f"  questIDs : 231..245")
    print(f"  Ajoutees a questline Age 4 -- L'Echappee")


if __name__ == '__main__':
    main()
