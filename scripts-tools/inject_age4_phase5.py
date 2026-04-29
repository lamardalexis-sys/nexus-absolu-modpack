#!/usr/bin/env python3
"""
Injecte les 15 quetes Phase 5 (Q56-Q70 Vivant et Etoile) Age 4.

questIDs : 256-270
Layout : sous Phase 4 (y=1270 a 1450)
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


def make_quest(qid, name, desc, icon, prereqs, tasks, rewards):
    return {
        "questID:3": qid, "preRequisites:11": prereqs,
        "properties:10": {"betterquesting:10": {
            "issilent:1": 0, "snd_complete:8": "minecraft:entity.player.levelup",
            "lockedprogress:1": 1, "partySingleReward:8": "false",
            "tasklogic:8": "AND", "repeattime:3": -1, "visibility:8": "ALWAYS",
            "simultaneous:1": 0, "globalshare:1": 0, "questlogic:8": "AND",
            "partysinglereward:1": 0, "snd_update:8": "minecraft:entity.player.levelup",
            "autoclaim:1": 0, "ismain:1": 1, "repeat_relative:1": 1,
            "icon:10": icon, "name:8": name, "desc:8": desc
        }},
        "tasks:9": tasks, "rewards:9": rewards
    }


def task_checkbox(index=0):
    return {f"{index}:10": {"index:3": index, "taskID:8": "bq_standard:checkbox"}}


def task_retrieval_item(index, item_id, count=1, damage=0, nbt=None):
    return {f"{index}:10": {
        "ignoreNBT:1": 1 if nbt is None else 0,
        "partialMatch:1": 1, "autoConsume:1": 0, "groupDetect:1": 0, "consume:1": 0,
        "taskID:8": "bq_standard:retrieval", "index:3": index,
        "required:9": {"0:10": make_item(item_id, count=count, damage=damage, nbt=nbt)}
    }}


def task_retrieval_multi(index, items_list):
    """Retrieval avec plusieurs items requis."""
    required = {f"{i}:10": it for i, it in enumerate(items_list)}
    return {f"{index}:10": {
        "ignoreNBT:1": 1, "partialMatch:1": 1, "autoConsume:1": 0,
        "groupDetect:1": 0, "consume:1": 0,
        "taskID:8": "bq_standard:retrieval", "index:3": index,
        "required:9": required
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


# Helpers
ITEM_BREAD_16 = make_item("minecraft:bread", count=16)
ITEM_BREAD_32 = make_item("minecraft:bread", count=32)
ITEM_XP_2 = make_item("minecraft:experience_bottle", count=2)
ITEM_XP_4 = make_item("minecraft:experience_bottle", count=4)
ITEM_XP_8 = make_item("minecraft:experience_bottle", count=8)
ICON_VOSS = make_item("patchouli:guide_book", nbt={"patchouli:book:8": "nexusabsolu:voss_codex"})


def fluid_bucket(fluid_name, count=1):
    return make_item("forge:bucketfilled", count=count,
                     nbt={"FluidName:8": fluid_name, "Amount:3": 1000})


# Items Phase 5
ICON_MANASTEEL = make_item("botania:manaresource", damage=0)
ICON_MANAPOOL = make_item("botania:pool", damage=0)
ICON_MUSHROOM = make_item("botania:mushroom", damage=0)
ICON_PIGMENT_RED = make_item("contenttweaker:pigment_red")
ICON_PIGMENT_BLACK = make_item("contenttweaker:pigment_black")
ICON_EXTRACT_SWEET = fluid_bucket("extract_sweet_red")
ICON_EXTRACT_DARK = fluid_bucket("extract_dark_black")
ICON_ESSENCE = fluid_bucket("essence_chromatique")
ICON_LIQUIDSTAR = fluid_bucket("astralsorcery.liquidstarlight")
ICON_TRYPT = make_item("contenttweaker:tryptamide_m")
ICON_CRISTAL = make_item("contenttweaker:cristal_manifoldine")
ICON_DELTA = make_item("contenttweaker:compose_delta")
ICON_EPSILON = fluid_bucket("solution_epsilon")
ICON_CARTOUCHE_VIDE = make_item("contenttweaker:cartouche_vide")


phase5_quests = []

# === Q56 Botania retour magie ===
desc_q56 = """§7§oTu te rappelles Botania ? Tu sais, le truc magique avec les fleurs ?
§7Tu l'as croise Age 2 mais tu as pu l'eviter. Plus maintenant.

§7Theoreme II (Organique) de Voss = §lle vivant maitrise les
§7transformations qui depassent la chimie classique§r§7.

§7Tu vas avoir besoin de 1M de Mana et 16 champis differents.
§7Pour la Mana :
§7  - 64 Endoflames + Coal (passive, 200 Mana/sec sustained)
§7  - 1 Mana Pool Diluted + Spreaders Gaia
§7  - Patience (1M Mana = 5h en background)

§e§lObjectif : §71 Manasteel ingot
§e§lRecompense : §78 Manasteel + plans Mana Pool"""

phase5_quests.append(make_quest(
    qid=256, name="§l§dQ56 -- Botania, le retour de la magie",
    desc=desc_q56, icon=ICON_MANASTEEL, prereqs=[255],
    tasks=task_crafting(0, "botania:manaresource", count=1),
    rewards=reward_items(0, [
        make_item("botania:manaresource", count=8),
        ITEM_XP_2
    ])
))

# === Q57 Mana Pool ===
desc_q57 = """§7§oUn Mana Pool. Capacite max : 1M Mana.

§7Setup typique pour Age 4 :
§7  - 4 Mana Pools en parallele (4M total stockable)
§7  - 64 Endoflames bouclees sur charbon AE2
§7  - 16 Mana Spreaders Gaia pointes vers les pools
§7  - 1 Tinted Daisy (couleur change chaque tick) pour generer
§7    les 16 mushrooms par chance regulierement

§7§lAlternative§r§7 : 4 Spectroliers Botania -> 8000 Mana/sec mais
§7coute 4 obsidian + 4 quartz + 1 manasteel chacune.

§e§lObjectif : §71 Mana Pool craft
§e§lRecompense : §74 Pools + 2 Spreaders + plans Champi"""

phase5_quests.append(make_quest(
    qid=257, name="§l§dQ57 -- Setup Mana Industrial",
    desc=desc_q57, icon=ICON_MANAPOOL, prereqs=[256],
    tasks=task_crafting(0, "botania:pool", count=1),
    rewards=reward_items(0, [
        make_item("botania:pool", count=4),
        make_item("botania:spreader", count=2),
        ITEM_XP_2
    ])
))

# === Q58 16 mushrooms ===
desc_q58 = """§7§oBotania Mushroom. 16 couleurs (subtypes 0..15) :
§7  Rouge, Orange, Jaune, Rose, Vert, Cyan, Lime, Magenta
§7  Noir, Violet, Marron, Gris fonce, Vert fonce, Bleu fonce,
§7  Gris clair, Bleu clair

§7Tu vas farm les 16 sur Mycelium (vanilla ou Active).
§7Astuce : la §lTinted Daisy§r§7 (Botania) genere passivement
§7une couleur differente chaque tick. Setup avec hopper.

§7§l/!\\§r §7Au moins 1 de chaque pour passer cette quete.

§e§lObjectif : §716 Mushrooms (1 de chaque couleur)
§e§lRecompense : §71 Tinted Daisy + plans Pigments"""

# 16 mushroom subtypes (damage 0-15)
mushroom_items = [make_item("botania:mushroom", count=1, damage=d) for d in range(16)]

phase5_quests.append(make_quest(
    qid=258, name="§l§dQ58 -- Champis colores, Botania Tinted Daisy",
    desc=desc_q58, icon=ICON_MUSHROOM, prereqs=[257],
    tasks=task_retrieval_multi(0, mushroom_items),
    rewards=reward_items(0, [
        make_item("botania:specialflower", count=1, nbt={"type:8": "tintedDaisy"}),
        ITEM_XP_2
    ])
))

# === Q59 8 pigments doux ===
desc_q59 = """§7§oTu broies les 8 champis "doux" (couleurs claires/chaudes) au
§7§lApothecary's Mortar§r§7 (Botania natif).

§7Recettes :
§7  Mushroom Red    -> pigment_red
§7  Mushroom Orange -> pigment_orange
§7  ...
§7  Mushroom Magenta -> pigment_magenta

§e§lObjectif : §78 pigments doux (1 de chaque)
§e§lRecompense : §78 pigments bonus + plans Extracts doux"""

# 8 pigments doux : red, orange, yellow, pink, lime, cyan, lightblue, magenta
pigments_doux = [
    make_item(f"contenttweaker:pigment_{c}", count=1)
    for c in ["red", "orange", "yellow", "pink", "lime", "cyan", "lightblue", "magenta"]
]

phase5_quests.append(make_quest(
    qid=259, name="§l§dQ59 -- Pigments doux, 8 couleurs claires",
    desc=desc_q59, icon=ICON_PIGMENT_RED, prereqs=[258],
    tasks=task_retrieval_multi(0, pigments_doux),
    rewards=reward_items(0, [
        make_item("contenttweaker:pigment_red", count=4),
        make_item("contenttweaker:pigment_yellow", count=4),
        ITEM_XP_2
    ])
))

# === Q60 8 extracts doux ===
desc_q60 = """§7§oTraitement HCl leger des 8 pigments doux dans une Alchemy Chamber
§7Modular Machinery (ou un Blood Magic Alchemy Array si tu veux
§7spice it up).

§7Recette (×8) :
§7  pigment_doux + 50mB HCl + 100 RF/t pendant 5s -> extract_sweet_<color>

§7C'est un fluide custom par couleur. 8 extracts doux qui forment
§7l'Essence Chromatique partielle (Theoreme II).

§e§lObjectif : §78 extracts doux (1 bucket de chaque)
§e§lRecompense : §7Plans Sombres + 2 Bouteilles XP"""

extracts_doux = [
    fluid_bucket(f"extract_sweet_{c}", count=1)
    for c in ["red", "orange", "yellow", "pink", "lime", "cyan", "lightblue", "magenta"]
]

phase5_quests.append(make_quest(
    qid=260, name="§l§dQ60 -- Extracts doux, traitement HCl",
    desc=desc_q60, icon=ICON_EXTRACT_SWEET, prereqs=[259],
    tasks=task_retrieval_multi(0, extracts_doux),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q61 8 pigments sombres ===
desc_q61 = """§7§oMaintenant les 8 sombres (couleurs froides/sombres) :
§7  Mushroom Black, Purple, Brown, Gray, Green dark,
§7  Blue dark, LightGray, Blue light

§7Tu broies idem au Mortar.

§e§lObjectif : §78 pigments sombres
§e§lRecompense : §78 pigments bonus + plans Extracts sombres"""

# Pigments sombres existants : black, purple, brown, gray, green, blue,
# + on assume green_dark = green, blue_dark = blue, lightgray = ?, blue_light = ?
# Pour rester simple : on utilise les pigments existants (10/16 trouves dans le ZS)
pigments_sombres = [
    make_item(f"contenttweaker:pigment_{c}", count=1)
    for c in ["black", "purple", "brown", "gray", "green", "blue"]
]
# Compense avec 2 doux pour atteindre 8 (alternative simple)
pigments_sombres += [
    make_item("contenttweaker:pigment_red", count=1),
    make_item("contenttweaker:pigment_lightblue", count=1)
]

phase5_quests.append(make_quest(
    qid=261, name="§l§dQ61 -- Pigments sombres, 8 couleurs profondes",
    desc=desc_q61, icon=ICON_PIGMENT_BLACK, prereqs=[260],
    tasks=task_retrieval_multi(0, pigments_sombres),
    rewards=reward_items(0, [
        make_item("contenttweaker:pigment_black", count=4),
        make_item("contenttweaker:pigment_purple", count=4),
        ITEM_XP_2
    ])
))

# === Q62 8 extracts sombres ===
desc_q62 = """§7§oH2SO4 chaud + chauffe 5000 RF/t pendant 10s.
§7Plus dur que les doux. Plus puissant aussi.

§7Recette (×8) :
§7  pigment_sombre + 80mB H2SO4 + chauffe -> extract_dark_<color>

§e§lObjectif : §78 extracts sombres
§e§lRecompense : §7Plans Essence Chromatique + 2 Bouteilles XP"""

# 8 extracts dark : black, purple, brown, gray, green, blue, lightgray, white
extracts_sombres = [
    fluid_bucket(f"extract_dark_{c}", count=1)
    for c in ["black", "purple", "brown", "gray", "green", "blue", "lightgray", "white"]
]

phase5_quests.append(make_quest(
    qid=262, name="§l§dQ62 -- Extracts sombres, traitement H2SO4 chaud",
    desc=desc_q62, icon=ICON_EXTRACT_DARK, prereqs=[261],
    tasks=task_retrieval_multi(0, extracts_sombres),
    rewards=reward_items(0, [ITEM_XP_4])
))

# === Q63 Essence Chromatique ===
desc_q63 = """§7§oFusionne les 16 extracts (8 doux + 8 sombres) dans le
§7§lModular Machinery Chromatic Fusion Chamber§r§7.

§7Recipe :
§7  16 extracts (1 de chaque) + 1 Heavy Water + 5000 RF/t * 30s
§7  -> 1 bucket Essence Chromatique (1000mB)

§7L'Essence Chromatique = §lTheoreme II valide§r§7.
§7Voss en demande 4 buckets pour le Bio-Reacteur final.

§8§o"La couleur n'est pas une propriete passive de la matiere.
§8C'est le langage que la matiere utilise pour parler a notre
§8oeil. Et tu viens d'ecrire un dictionnaire complet." — E.V.

§e§lObjectif : §71 Essence Chromatique
§e§lRecompense : §7Plans Astral Sorcery + 4 Bouteilles XP"""

phase5_quests.append(make_quest(
    qid=263, name="§l§dQ63 -- Essence Chromatique, la fusion 16 couleurs",
    desc=desc_q63, icon=ICON_ESSENCE, prereqs=[262],
    tasks=task_retrieval_fluid_bucket(0, "essence_chromatique", count=1),
    rewards=reward_items(0, [ITEM_XP_4])
))

# === Q64 Liquid Starlight ===
desc_q64 = """§7§oTheoreme III (Stellaire) de Voss = §ll'energie des etoiles porte
§7une signature qui active certaines reactions chimiques§r§7.

§7Astral Sorcery te permet de capter ca :
§7  - Lens Cluster (3 Lens) + Marble Collector Crystal
§7  - Pose sous le ciel etoile, max altitude
§7  - Production : 100mB Liquid Starlight / minute / Lens

§7Tu as besoin de 4 buckets (4000mB) pour la cyclisation Manifoldine.

§e§lObjectif : §74 buckets Liquid Starlight
§e§lRecompense : §7Plans Cyclisation + 2 Bouteilles XP"""

phase5_quests.append(make_quest(
    qid=264, name="§l§dQ64 -- Astral Sorcery, Liquid Starlight",
    desc=desc_q64, icon=ICON_LIQUIDSTAR, prereqs=[263],
    tasks=task_retrieval_fluid_bucket(0, "astralsorcery.liquidstarlight", count=4),
    rewards=reward_items(0, [ITEM_XP_2])
))

# === Q65 Tryptamide-M ===
desc_q65 = """§7§oTryptamide-M. Amine analogue a la DMT.
§7C'est le neurochem qui interagit avec les recepteurs 5-HT2A
§7du systeme nerveux humain.

§7Synthese :
§7  Tryptophane (a faire avec Botania Living Wood) + 2 Methanol
§7  + ammoniaque (Phase 3 Q36) + Compose alpha (Phase 3 Q35)
§7  -> Tryptamide-M

§7C'est le §lcoeur neurochimique§r§7 de la cartouche.

§8§o"Sans la Tryptamide-M, le sujet 47 percoit la simulation mais
§8ne peut pas la franchir. Avec, il devient permeable." — E.V.

§e§lObjectif : §72 Tryptamide-M
§e§lRecompense : §74 trypt + plans Cristaux Manifoldine"""

phase5_quests.append(make_quest(
    qid=265, name="§l§dQ65 -- Tryptamide-M, l'amine cle",
    desc=desc_q65, icon=ICON_TRYPT, prereqs=[264],
    tasks=task_retrieval_item(0, "contenttweaker:tryptamide_m", count=2),
    rewards=reward_items(0, [
        make_item("contenttweaker:tryptamide_m", count=4),
        ITEM_XP_2
    ])
))

# === Q66 Cristaux Manifoldine ===
desc_q66 = """§7§oCyclisation Stellaire. La piece centrale de la Phase 5.

§7Sous Argon (Phase 1 Q6) + sous flux Liquid Starlight (Q64) :
§7  Phenol substitue (Phase 3 Q43) + Tryptamide-M (Q65)
§7  + Carbone Au charge (Phase 3 Q44) + 6Li
§7  -> Cristal de Manifoldine (cristal violet/cyan, 800 C)

§7C'est le §lpremier compose stable§r§7 de la chaine epsilon.

§e§lObjectif : §71 Cristal de Manifoldine
§e§lRecompense : §72 cristaux + plans Compose delta"""

phase5_quests.append(make_quest(
    qid=266, name="§l§dQ66 -- Cristaux de Manifoldine, premiere cyclisation",
    desc=desc_q66, icon=ICON_CRISTAL, prereqs=[265],
    tasks=task_retrieval_item(0, "contenttweaker:cristal_manifoldine", count=1),
    rewards=reward_items(0, [
        make_item("contenttweaker:cristal_manifoldine", count=2),
        ITEM_XP_2
    ])
))

# === Q67 Compose delta ===
desc_q67 = """§7§oCompose delta = Cristal de Manifoldine + Heavy Water + Mycelium Active.

§7Le Mycelium Active (Phase 4 Q51) catalyse la fixation du cristal
§7dans une matrice biologique soluble. Sans lui le cristal serait
§7inerte et ne s'injecterait jamais.

§7C'est de la chimie organometallique vivante.

§e§lObjectif : §72 Compose delta
§e§lRecompense : §74 delta + plans Solution epsilon"""

phase5_quests.append(make_quest(
    qid=267, name="§l§dQ67 -- Compose delta, bio-actif",
    desc=desc_q67, icon=ICON_DELTA, prereqs=[266],
    tasks=task_retrieval_item(0, "contenttweaker:compose_delta", count=2),
    rewards=reward_items(0, [
        make_item("contenttweaker:compose_delta", count=4),
        ITEM_XP_2
    ])
))

# === Q68 Solution epsilon ===
desc_q68 = """§7§oSolution epsilon = §lLA Manifoldine active§r§7.

§7Synthese finale :
§7  Compose delta + Compose alpha + Compose beta + Compose gamma3
§7  + Argon atmosphere + Eau Tridistillee
§7  -> Solution Epsilon (liquide pourpre/cyan animee)

§7§5C'est ce qui s'injectera dans les veines du joueur.§r

§7§4§lDanger§r §7 : la solution est instable. La pression interne
§7de l'ampoule iridium doit etre maintenue a 2.5 atm. En dessous,
§7la solution decoagule et devient inerte. Au-dessus, elle explose.

§e§lObjectif : §71 bucket Solution Epsilon
§e§lRecompense : §7Plans Bio-Reacteur final + 4 Bouteilles XP"""

phase5_quests.append(make_quest(
    qid=268, name="§l§dQ68 -- Compose epsilon, la Manifoldine active",
    desc=desc_q68, icon=ICON_EPSILON, prereqs=[267],
    tasks=task_retrieval_fluid_bucket(0, "solution_epsilon", count=1),
    rewards=reward_items(0, [ITEM_XP_4])
))

# === Q69 Verification ===
desc_q69 = """§7§oArrete-toi 30 secondes.

§7Verifie ce que tu as :
§7  ✓ Theoreme I (Conservation) : tous les composes alpha-beta
§7  ✓ Theoreme II (Organique) : Essence Chromatique 16 champis
§7  ✓ Theoreme III (Stellaire) : Liquid Starlight + Cristaux Manifoldine
§7  ✓ Theoreme IV (Sanguine) : Tryptamide-M + Compose delta
§7  ✓ Theoreme V (Brisure) : composes gamma + Solution Epsilon

§7Si tu as tout, tu es pret pour la Phase 6 finale.
§7Si tu manques un, retourne en arriere. Aucun raccourci.

§8§o"Voila, Sujet 47. Tu as les Cinq Theoremes dans tes mains.
§8Maintenant tu vas les faire fusionner." — E.V.

§e§lObjectif : §7Confirme manuellement (clique le bouton)
§e§lRecompense : §71 Cartouche Vide + plans Encartouchage"""

phase5_quests.append(make_quest(
    qid=269, name="§l§dQ69 -- Verification, les 5 theoremes en main",
    desc=desc_q69, icon=ICON_CARTOUCHE_VIDE, prereqs=[268],
    tasks=task_checkbox(0),
    rewards=reward_items(0, [
        make_item("contenttweaker:cartouche_vide", count=1),
        ITEM_XP_4
    ])
))

# === Q70 Fin Phase 5 ===
desc_q70 = """§7§oTu es a 70/80 quetes. Tu as la Solution Epsilon.
§7Tout ce qui te manque maintenant c'est la chambre stellaire
§7multibloc et le rite final.

§7§5§lLe carnet de Voss s'ouvre sur le chapitre 4 (Theoreme IV) -
§7le plus court mais le plus brutal :§r

§8§o"Le sang humain est le seul medium qui parle a tous les niveaux
§8de la simulation. C'est la qu'il faut frapper. La cartouche est
§8finie. Ne la mets pas dans tes veines avant d'etre dans la
§8chambre stellaire. Sinon tu mourras." — E.V.

§e§lObjectif : §7Lis Voss Codex chap.4 (Sanguine) -- chapitre final
§e§lRecompense : §7Phase 6 (Convergence) debloquee + 8 Bouteilles XP"""

phase5_quests.append(make_quest(
    qid=270, name="§l§d§nQ70 -- Phase 5 complete, la magie est dans la machine",
    desc=desc_q70, icon=ICON_VOSS, prereqs=[269],
    tasks=task_checkbox(0),
    rewards={
        **reward_items(0, [ICON_VOSS, ITEM_BREAD_32, ITEM_XP_8]),
        **reward_command(1, "/say §d[Nexus Absolu]§r §7VAR_NAME§r tient la Solution Epsilon. Phase 6 -- Convergence -- s'ouvre. La cartouche se rapproche.")
    }
))


# Layout sous Phase 4 (y=1270 a y=1450)
LAYOUT = {
    256: (0, 1270),    257: (80, 1270),   258: (160, 1270),
    259: (240, 1270),  260: (320, 1270),
    261: (320, 1360),  262: (240, 1360),  263: (160, 1360),
    264: (80, 1360),   265: (0, 1360),
    266: (0, 1450),    267: (80, 1450),   268: (160, 1450),
    269: (240, 1450),  270: (320, 1450),
}


def main():
    with open(QUEST_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    quests_db = data['questDatabase:9']
    qlines = data['questLines:9']

    existing_ids = set(int(q['questID:3']) for q in quests_db.values())
    for qid in range(256, 271):
        if qid in existing_ids:
            print(f"ERROR : questID {qid} deja existant.")
            sys.exit(1)

    max_key_idx = max(int(k.split(':')[0]) for k in quests_db.keys())
    next_key_idx = max_key_idx + 1
    print(f"Insertion {len(phase5_quests)} quetes a partir de la cle {next_key_idx}:10")

    for q in phase5_quests:
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

    for q in phase5_quests:
        qid = q['questID:3']
        x, y = LAYOUT[qid]
        age4_qline['quests:9'][f"{next_qline_idx}:10"] = {
            "id:3": qid, "sizeX:3": 24, "sizeY:3": 24,
            "x:3": x, "y:3": y, "posX:3": x, "posY:3": y
        }
        next_qline_idx += 1

    shutil.copy(QUEST_FILE, QUEST_FILE + '.bak.before-age4-phase5')
    with open(QUEST_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"OK : {len(phase5_quests)} quetes Age 4 Phase 5 injectees.")
    print(f"  questIDs : 256..270")


if __name__ == '__main__':
    main()
