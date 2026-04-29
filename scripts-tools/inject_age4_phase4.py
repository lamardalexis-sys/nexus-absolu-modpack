#!/usr/bin/env python3
"""
Injecte les 10 quetes Phase 4 (Q46-Q55 Feu Nucleaire) Age 4.

questIDs : 246-255
Layout : sous Phase 3 (y=1040 a 1130)
"""
import json
import sys
import shutil

QUEST_FILE = 'config/betterquesting/DefaultQuests.json'


def make_item(item_id, count=1, damage=0, nbt=None):
    item = {"id:8": item_id, "Count:3": count, "Damage:2": damage, "OreDict:8": ""}
    if nbt:
        item["tag:10"] = nbt
    return item


def make_oredict_item(ore, count=1):
    return {"id:8": "minecraft:iron_ingot", "Count:3": count, "Damage:2": 0, "OreDict:8": ore}


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
    return {f"{index}:10": {"index:3": index, "taskID:8": "bq_standard:checkbox"}}


def task_retrieval_item(index, item_id, count=1, nbt=None):
    return {f"{index}:10": {
        "ignoreNBT:1": 1 if nbt is None else 0,
        "partialMatch:1": 1, "autoConsume:1": 0, "groupDetect:1": 0, "consume:1": 0,
        "taskID:8": "bq_standard:retrieval", "index:3": index,
        "required:9": {"0:10": make_item(item_id, count=count, nbt=nbt)}
    }}


def task_retrieval_oredict(index, ore, count=1):
    return {f"{index}:10": {
        "ignoreNBT:1": 1, "partialMatch:1": 1, "autoConsume:1": 0,
        "groupDetect:1": 0, "consume:1": 0,
        "taskID:8": "bq_standard:retrieval", "index:3": index,
        "required:9": {"0:10": make_oredict_item(ore, count=count)}
    }}


def task_crafting(index, item_id, count=1):
    return {f"{index}:10": {
        "ignoreNBT:1": 1, "partialMatch:1": 1, "anyCrafting:1": 0,
        "taskID:8": "bq_standard:crafting", "index:3": index,
        "requiredItems:9": {"0:10": make_item(item_id, count=count)}
    }}


def task_retrieval_fluid_bucket(index, fluid_name, count=1):
    nbt = {"FluidName:8": fluid_name, "Amount:3": 1000}
    return {f"{index}:10": {
        "ignoreNBT:1": 0, "partialMatch:1": 1, "autoConsume:1": 0,
        "groupDetect:1": 0, "consume:1": 0,
        "taskID:8": "bq_standard:retrieval", "index:3": index,
        "required:9": {"0:10": make_item("forge:bucketfilled", count=count, nbt=nbt)}
    }}


def reward_items(index, items_list):
    rewards_dict = {f"{i}:10": item for i, item in enumerate(items_list)}
    return {f"{index}:10": {
        "rewardID:8": "bq_standard:item", "index:3": index, "rewards:9": rewards_dict
    }}


def reward_command(index, command):
    return {f"{index}:10": {
        "hideBlockIcon:1": 1, "rewardID:8": "bq_standard:command",
        "asScript:1": 1, "description:8": "", "viaPlayer:1": 0,
        "index:3": index, "title:8": "bq_standard.reward.command", "command:8": command
    }}


# Items recurrents
ITEM_BREAD_16 = make_item("minecraft:bread", count=16)
ITEM_XP_2 = make_item("minecraft:experience_bottle", count=2)
ITEM_XP_4 = make_item("minecraft:experience_bottle", count=4)
ICON_VOSS = make_item("patchouli:guide_book", nbt={"patchouli:book:8": "nexusabsolu:voss_codex"})


def fluid_bucket(fluid_name, count=1):
    return make_item("forge:bucketfilled", count=count,
                     nbt={"FluidName:8": fluid_name, "Amount:3": 1000})


# Items / fluides Phase 4
ICON_REACTOR = make_item("mekanism:basicblock", damage=10)  # placeholder reactor controller
ICON_UF6 = fluid_bucket("uf6_gas")
ICON_PU = make_oredict_item("ingotPlutonium")
ICON_CAPSULE_PUBE = make_item("contenttweaker:capsule_pube")
ICON_TRITIUM = fluid_bucket("tritium")
ICON_MYCELIUM_ACT = make_item("contenttweaker:mycelium_active")
ICON_GAMMA1 = make_item("contenttweaker:compose_gamma1")
ICON_GAMMA2 = make_item("contenttweaker:compose_gamma2")
ICON_GAMMA3 = make_item("contenttweaker:compose_gamma3")


phase4_quests = []

# === Q46 Reacteur fission ===
desc_q46 = """§7§oTu as 30 elements, des acides, des ethers magiques.
§7Maintenant tu vas casser le NOYAU. Sujet 47 : welcome to nuclear physics.

§7Construis un reacteur Mekanism Fission ou un Big Reactor :
§7  - Coeur : barres uranium
§7  - Moderateur : Heavy Water (D2O, Phase 1 Q13)
§7  - Refroidissement : Resonant Cells / Sodium liquide
§7  - Blindage : Lead (Phase 2 Q26)

§7Sortie : 50M+ RF/t en pic. Tu en auras besoin pour la Phase 5
§7(Liquid Starlight + Botania ne pousse pas avec rien).

§e§lObjectif : §7Crafte 1 Reactor Glass Mekanism (ou equivalent)
§e§lRecompense : §7Plans Centrifugeuse + 2 Bouteilles XP"""

phase4_quests.append(make_quest(
    qid=246, name="§l§4Q46 -- Le reacteur fission, ce monstre",
    desc=desc_q46, icon=ICON_REACTOR, prereqs=[245],
    tasks=task_crafting(0, "mekanism:reactorglass", count=1),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q47 Enrichissement UF6 ===
desc_q47 = """§7§oUF6 = uranium hexafluoride. Solide a 56 C, gazeux au-dessus.
§7C'est le seul compose d'uranium volatile, donc le seul qu'on
§7peut centrifuger pour separer U-235 de U-238.

§7Synthese :
§7  Yellowcake (U3O8, Phase 2 Q27) + 6 HF -> UF6 + UO2 + ...

§7Le HF (acide fluorhydrique) tu l'as deja (F2 + H2 -> 2 HF, Phase 1).

§7§4§lDANGER§r §7: l'UF6 attaque le verre normal. Reservoirs Lead/Iridium only.

§e§lObjectif : §78 buckets UF6 gazeux
§e§lRecompense : §7Plans enrichissement Pu + 2 Bouteilles XP"""

phase4_quests.append(make_quest(
    qid=247, name="§l§4Q47 -- L'enrichissement de l'uranium",
    desc=desc_q47, icon=ICON_UF6, prereqs=[246],
    tasks=task_retrieval_fluid_bucket(0, "uf6_gas", count=8),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q48 Plutonium ===
desc_q48 = """§7§oPu-239 = ton vrai isotope d'interet. Pourquoi ?
§7  - Section efficace fission x2 par rapport a U-235
§7  - Demi-vie 24000 ans (assez stable pour l'utiliser)
§7  - Cree dans le reacteur fission par capture neutronique :
§7    U-238 + n -> U-239 -> Np-239 -> Pu-239

§7Tu as besoin de 4 ingots de Pu pour la capsule Pu-Be (Q49).
§7Recyclage des barres usagees du reacteur Mekanism IsotopicCentrifuge.

§8§o"Le plutonium n'existe pas dans la nature. C'est un metal
§8artificiel. C'est nous qui l'avons cree. C'est le premier
§8signe que nous sommes des Createurs." — E.V.

§e§lObjectif : §74 Plutonium ingots
§e§lRecompense : §72 Pu Plates + plans capsule Be"""

phase4_quests.append(make_quest(
    qid=248, name="§l§4Q48 -- La fission et son plutonium",
    desc=desc_q48, icon=ICON_PU, prereqs=[247],
    tasks=task_retrieval_oredict(0, "ingotPlutonium", count=4),
    rewards=reward_items(0, [
        make_oredict_item("platePlutonium", count=2),
        ITEM_XP_2
    ])
))

# === Q49 Capsule Pu-Be ===
desc_q49 = """§7§oUne capsule Pu-Be = source neutronique scellee.

§7Recette :
§7  4 Pu (Q48) + 4 Be (Phase 2 Q29) + 1 capsule blindee Lead
§7  -> Capsule Pu-Be qui emet ~10E6 neutrons/sec pendant 100 ans

§7Tu vas l'utiliser pour irradier le 6Li -> Tritium (Q50).
§7Et dans le Mycelium Activator (Q51) — flux neutronique sur
§7Mycelium pour activer la magie (jonction Theoreme IV+V).

§7§4§lDANGER§r §7: tiens-la dans un Lead Tank pendant le transport.

§e§lObjectif : §71 Capsule Pu-Be synthese
§e§lRecompense : §7Plans Tritium pur + 2 Bouteilles XP"""

phase4_quests.append(make_quest(
    qid=249, name="§l§4Q49 -- Capsule Pu-Be, la source neutronique",
    desc=desc_q49, icon=ICON_CAPSULE_PUBE, prereqs=[248],
    tasks=task_retrieval_item(0, "contenttweaker:capsule_pube", count=1),
    rewards=reward_items(0, [
        make_item("contenttweaker:capsule_pube", count=2),
        ITEM_XP_2
    ])
))

# === Q50 Tritium pur ===
desc_q50 = """§7§oFin Phase 1 Q14 tu avais pre-stocke 100mB de Tritium.
§7Maintenant tu en stockes 4 buckets (4000mB).

§7Avec ta capsule Pu-Be tu peux maintenant produire industriellement :
§7  6Li + n -> 4He + Tritium + 4.8 MeV

§7Le Tritium pur sert a :
§7  - Compose gamma3 = 6LiT (lithium tritide)
§7  - Fusion D-T des reacteurs (50% des chambres reaction Phase 5)
§7  - Maintien plasma stellaire reaction Phase 6

§e§lObjectif : §74 buckets Tritium pur
§e§lRecompense : §7Plans 6LiT + 2 Bouteilles XP"""

phase4_quests.append(make_quest(
    qid=250, name="§l§4Q50 -- Tritium pur, le carburant des etoiles",
    desc=desc_q50, icon=ICON_TRITIUM, prereqs=[249],
    tasks=task_retrieval_fluid_bucket(0, "tritium", count=4),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q51 Mycelium Active ===
desc_q51 = """§7§oUne pelle de mycelium normal + 1 capsule Pu-Be 100 ms d'irradiation
§7-> Mycelium Active.

§7C'est de la science-fiction mais Voss avait vu juste : le flux
§7neutronique active la replication ARN champignonique (Botania
§7ne fonctionne pas sur du mycelium normal).

§8§o"L'irradiation neutronique a faible dose ouvre les portes du vivant.
§8Phase 5 vous demandera 16 champignons. Ils ne pousseront que
§8sur Mycelium Active." — E.V.

§7§4§lWARNING§r §7: 8 mycelium = 800ms d'irradiation. Ta capsule en aura.

§e§lObjectif : §78 Mycelium Active
§e§lRecompense : §716 mycelium bonus + plans Compose gamma"""

phase4_quests.append(make_quest(
    qid=251, name="§l§4Q51 -- Mycelium active, premier croisement",
    desc=desc_q51, icon=ICON_MYCELIUM_ACT, prereqs=[250],
    tasks=task_retrieval_item(0, "contenttweaker:mycelium_active", count=8),
    rewards=reward_items(0, [
        make_item("contenttweaker:mycelium_active", count=16),
        ITEM_XP_2
    ])
))

# === Q52 Compose gamma1 ===
desc_q52 = """§7§oCompose gamma1 = Borate de sodium dope au lithium.

§7Recette : Na2B4O7 + 2 Li + Heavy Water reaction chamber
§7-> Na2B4O7-Li dope (cristal vert luminescent)

§7Sert a moderer les neutrons rapides du reacteur Phase 4 vers
§7des neutrons thermiques (lents, plus efficaces pour fission Pu).

§e§lObjectif : §72 Compose gamma1
§e§lRecompense : §74 gamma1 + plans gamma2"""

phase4_quests.append(make_quest(
    qid=252, name="§l§4Q52 -- Compose gamma1, sodium borate",
    desc=desc_q52, icon=ICON_GAMMA1, prereqs=[251],
    tasks=task_retrieval_item(0, "contenttweaker:compose_gamma1", count=2),
    rewards=reward_items(0, [
        make_item("contenttweaker:compose_gamma1", count=4),
        ITEM_XP_2
    ])
))

# === Q53 Compose gamma2 ===
desc_q53 = """§7§oIr-191 + n -> Ir-192 + gamma. Demi-vie Ir-192 = 73.8 jours.
§7C'est ta source de rayons gamma pour activer le Compose epsilon
§7(Phase 5 cyclisation).

§7Recette :
§7  1 Ir ingot (Phase 2 Q24) + capsule Pu-Be 5s d'exposition
§7  -> Compose gamma2 (Iridium-192 active)

§7Half-life 73 jours -> tu as 2 mois Minecraft pour finir l'Age 4.
§7Apres ca decline. Pas tres grave si tu te depeches.

§e§lObjectif : §72 Compose gamma2
§e§lRecompense : §74 gamma2 + plans gamma3"""

phase4_quests.append(make_quest(
    qid=253, name="§l§4Q53 -- Compose gamma2, l'iridium active",
    desc=desc_q53, icon=ICON_GAMMA2, prereqs=[252],
    tasks=task_retrieval_item(0, "contenttweaker:compose_gamma2", count=2),
    rewards=reward_items(0, [
        make_item("contenttweaker:compose_gamma2", count=4),
        ITEM_XP_2
    ])
))

# === Q54 Compose gamma3 ===
desc_q54 = """§7§oLithium-6 Tritide (6LiT). Le compose gamma de plus haute densite
§7energetique connu.

§7Recette :
§7  6Li (separe par Mekanism IsotopicCentrifuge) + Tritium pur
§7  -> 6LiT (poudre noire, 1.04 g/cm3)

§7§5§l/!\\ ENERGIE LIBEREE §r§7 si compresse a 100 atm = 4.7 MJ/kg.
§7C'est ce qui declenche la cyclisation finale Phase 6 du Solvant
§7epsilon dans la chambre stellaire.

§e§lObjectif : §71 Compose gamma3
§e§lRecompense : §72 gamma3 + Carnet Voss chap.5 (Brisure)"""

phase4_quests.append(make_quest(
    qid=254, name="§l§4Q54 -- Compose gamma3, le 6LiT",
    desc=desc_q54, icon=ICON_GAMMA3, prereqs=[253],
    tasks=task_retrieval_item(0, "contenttweaker:compose_gamma3", count=1),
    rewards=reward_items(0, [
        make_item("contenttweaker:compose_gamma3", count=2),
        ICON_VOSS,
        ITEM_XP_4
    ])
))

# === Q55 Fin Phase 4 ===
desc_q55 = """§7§oTu as :
§7  - Reacteur fission stable (50M+ RF/t)
§7  - UF6 + Pu enrichis
§7  - Capsule Pu-Be source neutronique
§7  - Tritium pur
§7  - Mycelium Active (jonction Theoreme IV+V)
§7  - Composes gamma1, gamma2, gamma3

§7Tu as franchi le §lTheoreme V (Brisure)§r§7. Le carnet de Voss
§7s'ouvre sur le chapitre 5, le plus dangereux.

§8§o"Tu as transmute la Terre. Tu as transmute les molecules.
§8Tu as transmute les NOYAUX. Maintenant tu vas transmuter
§8le VIVANT. Et Phase 5 va te ramener vers la magie. Au sens
§8technique du terme cette fois." — E.V.

§e§lObjectif : §7Lis Voss Codex chap.5 (Brisure Nucleaire)
§e§lRecompense : §7Phase 5 (Vivant et Etoile) debloquee + 4 Bouteilles XP"""

phase4_quests.append(make_quest(
    qid=255, name="§l§4§nQ55 -- Phase 4 complete, le feu nucleaire dompte",
    desc=desc_q55, icon=ICON_VOSS, prereqs=[254],
    tasks=task_checkbox(0),
    rewards={
        **reward_items(0, [ICON_VOSS, ITEM_BREAD_16, ITEM_XP_4]),
        **reward_command(1, "/say §4[Nexus Absolu]§r §7VAR_NAME§r a domestique le feu nucleaire. Phase 5 -- Vivant et Etoile -- s'ouvre.")
    }
))


# Layout sous Phase 3 (y=1040 a y=1130)
LAYOUT = {
    246: (0, 1040),    247: (80, 1040),   248: (160, 1040),
    249: (240, 1040),  250: (320, 1040),
    251: (320, 1130),  252: (240, 1130),  253: (160, 1130),
    254: (80, 1130),   255: (0, 1130),
}


def main():
    with open(QUEST_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    quests_db = data['questDatabase:9']
    qlines = data['questLines:9']

    # Securite collisions
    existing_ids = set(int(q['questID:3']) for q in quests_db.values())
    for qid in range(246, 256):
        if qid in existing_ids:
            print(f"ERROR : questID {qid} deja existant. Abort.")
            sys.exit(1)

    max_key_idx = max(int(k.split(':')[0]) for k in quests_db.keys())
    next_key_idx = max_key_idx + 1
    print(f"Insertion {len(phase4_quests)} quetes a partir de la cle {next_key_idx}:10")

    for q in phase4_quests:
        quests_db[f"{next_key_idx}:10"] = q
        next_key_idx += 1

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

    for q in phase4_quests:
        qid = q['questID:3']
        x, y = LAYOUT[qid]
        age4_qline['quests:9'][f"{next_qline_idx}:10"] = {
            "id:3": qid, "sizeX:3": 24, "sizeY:3": 24,
            "x:3": x, "y:3": y, "posX:3": x, "posY:3": y
        }
        next_qline_idx += 1

    shutil.copy(QUEST_FILE, QUEST_FILE + '.bak.before-age4-phase4')
    with open(QUEST_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"OK : {len(phase4_quests)} quetes Age 4 Phase 4 injectees.")
    print(f"  questIDs : 246..255")


if __name__ == '__main__':
    main()
