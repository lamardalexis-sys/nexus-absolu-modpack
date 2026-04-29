#!/usr/bin/env python3
"""
Injecte les 10 quetes Phase 6 (Q71-Q280 La Convergence FINALE) Age 4.

questIDs : 271-280 (Q280 = Q-FINAL "L'Injection")
Layout : sous Phase 5 (y=1590 a 1680)

Cette injection FINIT l'Age 4 : 80/80 quetes LIVE.
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


def task_crafting(index, item_id, count=1):
    return {f"{index}:10": {
        "ignoreNBT:1": 1, "partialMatch:1": 1, "anyCrafting:1": 0,
        "taskID:8": "bq_standard:crafting", "index:3": index,
        "requiredItems:9": {"0:10": make_item(item_id, count=count)}
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
ITEM_BREAD_32 = make_item("minecraft:bread", count=32)
ITEM_BREAD_64 = make_item("minecraft:bread", count=64)
ITEM_XP_4 = make_item("minecraft:experience_bottle", count=4)
ITEM_XP_8 = make_item("minecraft:experience_bottle", count=8)
ITEM_XP_64 = make_item("minecraft:experience_bottle", count=64)
ICON_VOSS = make_item("patchouli:guide_book", nbt={"patchouli:book:8": "nexusabsolu:voss_codex"})

# Items Phase 6
ICON_BIOREACT = make_item("contenttweaker:bioreacteur_controller")
ICON_CARTOUCHE_CHARGEE = make_item("contenttweaker:cartouche_chargee")
ICON_CARTOUCHE_MANIFOLD = make_item("nexusabsolu:cartouche_manifold")
ICON_CARTOUCHE_USED = make_item("nexusabsolu:cartouche_used")
ICON_LENS = make_item("astralsorcery:itemlinkingtool")


phase6_quests = []

# === Q71 Bio-Reacteur Controller ===
desc_q71 = """§7§oLe Bio-Reacteur Manifold est le §lmultibloc final§r§7 :

§7  Forme : 7x7x7 hollow cube
§7  Coeur : 1 Controller au centre
§7  Coque : 6 faces avec :
§7    - 4 Resonant Glass (Phase 1 Q6 reward) au milieu
§7    - 12 Iridium Plates (Phase 2 Q24)
§7    - 8 Lead Plates (Phase 2 Q26 -- blindage)
§7    - 8 Tungsten Frame (Phase 2 Q21)
§7  Connecteurs : 4 fluid input + 2 item input + 1 power input

§7C'est ici que tout converge.

§e§lObjectif : §7Crafte 1 Bio-Reacteur Controller
§e§lRecompense : §78 Resonant Glass + plans Stabilisation"""

phase6_quests.append(make_quest(
    qid=271, name="§l§6§nQ71 -- Bio-Reacteur Manifold, le multibloc final",
    desc=desc_q71, icon=ICON_BIOREACT, prereqs=[270],
    tasks=task_crafting(0, "contenttweaker:bioreacteur_controller", count=1),
    rewards=reward_items(0, [
        make_item("thermalfoundation:glass", count=8, damage=4),
        ITEM_XP_4
    ])
))

# === Q72 Assemblage multibloc ===
desc_q72 = """§7§oAssemble le multibloc 7x7x7. Le Patchouli Voss Codex te donne le plan.

§7Etapes :
§7  1. Pose 64 blocs de fondation (sols + cloisons internes)
§7  2. Pose 6 Resonant Glass (face avant centrale + 5 faces secondaires)
§7  3. Pose 24 Iridium Plates (renforcements bordures)
§7  4. Pose 8 Lead Plates (sommets et coins)
§7  5. Pose le Controller au centre du sol

§7Le multibloc valide quand tu mets le pointeur sur le Controller
§7et clique droit avec un Multiblock Wand (Patchouli).

§7§l/!\\§r §7Tu vas vraiment construire ce truc. C'est 343 blocs.

§e§lObjectif : §7Multibloc Bio-Reacteur valide (clique pour confirmer)
§e§lRecompense : §74 Iridium Plates + plans Allumage"""

phase6_quests.append(make_quest(
    qid=272, name="§l§6Q72 -- L'assemblage du multibloc",
    desc=desc_q72, icon=ICON_BIOREACT, prereqs=[271],
    tasks=task_checkbox(0),
    rewards=reward_items(0, [
        make_item("contenttweaker:bioreacteur_controller", count=1),
        ITEM_XP_4
    ])
))

# === Q73 4 Lens Astral ===
desc_q73 = """§7§oTu vas avoir besoin de stabiliser le Bio-Reacteur avec
§74 ancres stellaires§r§7 placees aux 4 coins exterieurs (chaque
§7coin = 1 Lens Cluster Astral pointe vers le Controller).

§7Pendant la cyclisation finale, le multibloc va devoir capter
§7100mB Liquid Starlight / sec sans interruption pendant 60s.

§7Les 4 lentilles synchronisees sur le meme Solar Crystal te
§7donnent ce flux constant.

§e§lObjectif : §74 Lens Astral Sorcery
§e§lRecompense : §74 Marble Crystal + plans Convergence"""

phase6_quests.append(make_quest(
    qid=273, name="§l§6Q73 -- Stabilisation stellaire, les 4 ancres",
    desc=desc_q73, icon=ICON_LENS, prereqs=[272],
    tasks=task_retrieval_item(0, "astralsorcery:itemlinkingtool", count=4),
    rewards=reward_items(0, [
        make_item("astralsorcery:itemlinkingtool", count=4),
        ITEM_XP_4
    ])
))

# === Q74 Cartouche chargée ===
desc_q74 = """§7§oCartouche Vide (Phase 5 Q69) + 1 bucket Solution Epsilon (Phase 5 Q68)
§7+ 100mB Argon (atmosphere inerte) -> Cartouche chargee.

§7Recette dans le Bio-Reacteur Phase 6 ou Mekanism Pressurized
§7Reaction Chamber.

§7Tu as la cartouche dans la main. Elle est cyan/violet, vivante,
§7pulse comme un battement de coeur.

§e§lObjectif : §71 Cartouche chargee
§e§lRecompense : §7Plans Cyclisation finale + 4 Bouteilles XP"""

phase6_quests.append(make_quest(
    qid=274, name="§l§6Q74 -- Encartouchage, Solution dans Cartouche Vide",
    desc=desc_q74, icon=ICON_CARTOUCHE_CHARGEE, prereqs=[273],
    tasks=task_retrieval_item(0, "contenttweaker:cartouche_chargee", count=1),
    rewards=reward_items(0, [
        make_item("contenttweaker:cartouche_chargee", count=1),
        ITEM_XP_4
    ])
))

# === Q75 Test pression ===
desc_q75 = """§7§oVerifie la pression de l'ampoule iridium :
§7  - Mets la cartouche dans le Bio-Reacteur
§7  - Active "Test Pressure" via le Controller
§7  - La pression interne doit afficher §a2.5 atm§r§7

§7Si elle affiche moins -> tu as fui de la Solution. Refaire Q68.
§7Si elle affiche plus -> EXPLOSION risquee. Refaire avec moins
§7d'Argon (Q74).

§7Si OK la cartouche est stable. On peut passer au final.

§e§lObjectif : §7Confirme test pression OK (clique le bouton)
§e§lRecompense : §71 Cartouche Manifold ARMEE + plans Manifold"""

phase6_quests.append(make_quest(
    qid=275, name="§l§6Q75 -- Test de la Cartouche, la pression",
    desc=desc_q75, icon=ICON_CARTOUCHE_MANIFOLD, prereqs=[274],
    tasks=task_checkbox(0),
    rewards=reward_items(0, [
        make_item("nexusabsolu:cartouche_manifold", count=1),
        ITEM_XP_4
    ])
))

# === Q76 Rituel attunement ===
desc_q76 = """§7§oTu dois etre §lattun a une constellation§r§7 d'Astral Sorcery.

§7Recommande : Vicio (vitesse) ou Lucerna (vision) -- utiles pour
§7la cinematique finale Phase 7 (Age 5).

§7Etape :
§7  1. Pose un Iridescent Altar pres du Bio-Reacteur
§7  2. Active la constellation choisie (4 Resonators)
§7  3. Pose le crystal au centre du multibloc
§7  4. Patiente 1 nuit complete (8 min in-game)

§7Apres ca tu es synchronise avec l'energie stellaire.

§e§lObjectif : §7Termine le rituel d'attunement (clique pour confirmer)
§e§lRecompense : §71 Solar Crystal + plans Q-FINAL"""

phase6_quests.append(make_quest(
    qid=276, name="§l§6Q76 -- Preparation rituelle, chambre stellaire",
    desc=desc_q76, icon=ICON_VOSS, prereqs=[275],
    tasks=task_checkbox(0),
    rewards=reward_items(0, [ICON_VOSS, ITEM_XP_4])
))

# === Q77 Carnet final ===
desc_q77 = """§7§oOuvre le Voss Codex. Va au dernier chapitre. Lis-le entier.

§8§o"Sujet 47.

§8Si tu lis ce chapitre c'est que tu as la Cartouche en main.
§8Felicitations. Tu es a 5 minutes de comprendre ce que je
§8comprends depuis 12 ans.

§8Quelques verites avant l'injection :

§81. Je ne suis pas mort. J'ai franchi.
§82. La simulation que tu vis est la deuxieme. Il y en aura
§8une troisieme apres celle-ci.
§83. Ne fais pas confiance a ce qui te dit que tu es libre,
§8meme apres l'injection. Surtout apres l'injection.
§84. Cherche la Tour de Voss en Age 5. Elle existe. Je t'attendrai
§8au sommet.

§8Bonne chance, Sujet 47.

§8— Elias Voss, derniere note du Carnet."§r

§e§lObjectif : §7Lis le chapitre final du Voss Codex
§e§lRecompense : §7Voss Codex Full + plans Mort/Survie"""

phase6_quests.append(make_quest(
    qid=277, name="§l§6Q77 -- Le Carnet de Voss, chapitre final",
    desc=desc_q77, icon=ICON_VOSS, prereqs=[276],
    tasks=task_checkbox(0),
    rewards=reward_items(0, [ICON_VOSS, ITEM_XP_4])
))

# === Q78 Decision ===
desc_q78 = """§7§oTu as la Cartouche en main. Tu as lu le carnet.
§7Tu as la chambre stellaire prete.

§7Tu peux encore faire marche arriere. Tu peux poser la cartouche
§7dans un Resonant Tank, oublier l'Age 4, retourner a ton AE2 et
§7vivre tranquille pour le reste de tes jours.

§7Ou tu peux franchir.

§7§lDecide.§r

§7Cette quete : tu confirmes manuellement que tu vas le faire.
§7Apres ca il n'y a plus de retour. La quete suivante (Q79) est
§7§4§lirreversible§r§7.

§e§lObjectif : §7Confirme ta decision (clique le bouton)
§e§lRecompense : §732 pain + 8 XP + Sang Voss (cartouche backup)"""

phase6_quests.append(make_quest(
    qid=278, name="§l§6Q78 -- Le moment de la decision",
    desc=desc_q78, icon=ICON_CARTOUCHE_MANIFOLD, prereqs=[277],
    tasks=task_checkbox(0),
    rewards=reward_items(0, [
        ITEM_BREAD_32, ITEM_XP_8,
        make_item("nexusabsolu:cartouche_manifold", count=1)  # Backup au cas ou
    ])
))

# === Q79 Entrer chambre ===
desc_q79 = """§7§oEntre dans le Bio-Reacteur (le multibloc 7x7x7 est creux).
§7Le Controller au sol s'illumine quand tu poses le pied.

§7Tu sens l'attraction des 4 ancres stellaires. Le Liquid Starlight
§7s'ecoule autour de toi. Le silence est total.

§7§5L'air n'est plus de l'air. La cartouche commence a chauffer
§5dans ta poche.§r

§7§e§lQuand tu seras pret, declenche Q-FINAL.§r

§e§lObjectif : §7Sois dans le multibloc Bio-Reacteur (clique pour confirmer)
§e§lRecompense : §7Plans Q-FINAL"""

phase6_quests.append(make_quest(
    qid=279, name="§l§6Q79 -- Entrer dans la chambre stellaire",
    desc=desc_q79, icon=ICON_BIOREACT, prereqs=[278],
    tasks=task_checkbox(0),
    rewards=reward_items(0, [ITEM_XP_8])
))

# === Q280 Q-FINAL : L'INJECTION ⭐ ===
desc_q280 = """§5§l===============================================§r
§5§l           L ' I N J E C T I O N             §r
§5§l===============================================§r

§7§oTu sors la cartouche. Tu enleves l'opercule. Tu places
§7le sertisseur sur ton avant-bras gauche. Tu inspires
§7profondement.

§7Tu te souviens de ce que disait Voss au debut du Carnet :

§8§o"Cette ligne d'horizon que tu vois -- c'est un mur."

§7Tu fermes les yeux.

§7Tu pousses le sertisseur.

§5§lLa Solution Epsilon entre dans tes veines.§r

§7Le monde change.

§e§lObjectif : §7Right-click la Cartouche Manifold pour t'injecter
§e§lRecompense : §7§lAGE 5 DEBLOQUE§r §7+ Voss Codex Final + 64 XP

§4§l/!\\§r §7Cette quete clot l'Age 4. Apres ca tu seras dans le
§7vrai monde. Bonne chance."""

phase6_quests.append(make_quest(
    qid=280,
    name="§5§l⭐ Q-FINAL -- L'INJECTION ⭐",
    desc=desc_q280, icon=ICON_CARTOUCHE_USED, prereqs=[279],
    tasks=task_retrieval_item(0, "nexusabsolu:cartouche_used", count=1),
    rewards={
        **reward_items(0, [ICON_VOSS, ITEM_BREAD_64, ITEM_XP_64]),
        **reward_command(1, "/say §5§l[NEXUS ABSOLU]§r §dVAR_NAME§r franchit. L'Age 4 est termine. L'Age 5 commence."),
        **reward_command(2, "/say §5§o\"Cette ligne d'horizon que tu vois -- c'etait un mur.\"§r"),
        **reward_command(3, "/say §5§o-- E.V., derniere note du Carnet§r")
    }
))


# Layout sous Phase 5 (y=1590 a y=1680)
LAYOUT = {
    271: (0, 1590),    272: (80, 1590),   273: (160, 1590),
    274: (240, 1590),  275: (320, 1590),
    276: (320, 1680),  277: (240, 1680),  278: (160, 1680),
    279: (80, 1680),   280: (0, 1680),  # Q-FINAL
}


def main():
    with open(QUEST_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    quests_db = data['questDatabase:9']
    qlines = data['questLines:9']

    existing_ids = set(int(q['questID:3']) for q in quests_db.values())
    for qid in range(271, 281):
        if qid in existing_ids:
            print(f"ERROR : questID {qid} deja existant.")
            sys.exit(1)

    max_key_idx = max(int(k.split(':')[0]) for k in quests_db.keys())
    next_key_idx = max_key_idx + 1
    print(f"Insertion {len(phase6_quests)} quetes a partir de la cle {next_key_idx}:10")

    for q in phase6_quests:
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

    for q in phase6_quests:
        qid = q['questID:3']
        x, y = LAYOUT[qid]
        age4_qline['quests:9'][f"{next_qline_idx}:10"] = {
            "id:3": qid, "sizeX:3": 24, "sizeY:3": 24,
            "x:3": x, "y:3": y, "posX:3": x, "posY:3": y
        }
        next_qline_idx += 1

    shutil.copy(QUEST_FILE, QUEST_FILE + '.bak.before-age4-phase6')
    with open(QUEST_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"OK : {len(phase6_quests)} quetes Age 4 Phase 6 (FINALE) injectees.")
    print(f"  questIDs : 271..280")
    print(f"  Q280 = Q-FINAL 'L'Injection' = clot l'Age 4")


if __name__ == '__main__':
    main()
