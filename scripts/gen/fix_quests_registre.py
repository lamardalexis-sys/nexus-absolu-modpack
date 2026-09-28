# -*- coding: utf-8 -*-
"""Corrige les quetes rendues impossibles par un identifiant ou un prerequis faux.

Chaque correction ci-dessous a ete verifiee contre le registre reel du pack
(config/AppliedEnergistics2/items.csv, dump fait en jeu : le nom affiche
correspond exactement). Voir scripts/check/check_quests.py.

Ce que ca corrige :
  - Q97 "Tu es sorti", racine de l'Age 2, attendait Q149, retiree le 13/04
    (commit 52d8365) : elle attend maintenant Q1118 "Le Portail Voss", la fin
    de l'Age 1. L'Age 2 etait verrouille pour toujours.
  - Pieces AE2 ecrites avec des noms qui n'existent pas en 1.12 : ce sont des
    metas de appliedenergistics2:part.
  - Mystical Agriculture, Environmental Tech : noms errones.
  - Taches retrieval sans NBT exige mais avec ignoreNBT=0 : elles refusaient
    tout item portant un NBT, meme sans rapport (ItemComparison.CompareNBTTag).
    Passees a ignoreNBT=1 : strictement moins exigeant.
  - Lignes sans lineID/order : l'ordre des onglets dependait du tri interne de
    BQ. Ordre fixe : Age 0, Age 1, Age 2, Age 3, Coding. Les lineID donnes sont
    ceux que BQ attribuait deja (0, 2, 4) : aucune progression ne bouge.

Decisions de design (28/09) :
  - Q141 : le Tesseract n'existe pas en Thermal Expansion 5.5.7 et Q142 l'exige.
    Remplace par le Quantum Entangloporter de Mekanism, meme role.
  - Q2005 : le Sculk Tendril vient d'un mod absent, et le Grabber Voss exige
    par Q2006 n'a aucune recette. Q2005 devient une etape narrative (case a
    cocher) qui donne le Grabber.

Idempotent.   python3 scripts/gen/fix_quests_registre.py
"""
import collections
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DQ = os.path.join(ROOT, 'config', 'betterquesting', 'DefaultQuests.json')

# ancien id -> (nouvel id, meta). Noms affiches verifies dans items.csv.
RENAME = {
    'appliedenergistics2:smart_cable': ('appliedenergistics2:part', 56),        # ME Smart Cable - Fluix
    'appliedenergistics2:terminal': ('appliedenergistics2:part', 380),          # ME Terminal
    'appliedenergistics2:crafting_terminal': ('appliedenergistics2:part', 360), # ME Crafting Terminal
    'appliedenergistics2:quartz_fiber': ('appliedenergistics2:part', 140),      # Quartz Fiber
    'appliedenergistics2:import_bus': ('appliedenergistics2:part', 240),        # ME Import Bus
    'appliedenergistics2:export_bus': ('appliedenergistics2:part', 260),        # ME Export Bus
    'appliedenergistics2:me_p2_p_tunnel': ('appliedenergistics2:part', 460),    # P2P Tunnel - ME
    'appliedenergistics2:storage_bus': ('appliedenergistics2:part', 220),       # ME Storage Bus
    'appliedenergistics2:quartz_grindstone': ('appliedenergistics2:grindstone', 0),
    'mysticalagriculture:inferium_seeds': ('mysticalagriculture:tier1_inferium_seeds', 0),
    'mysticalagriculture:inferium_essence': ('mysticalagriculture:crafting', 0),  # Inferium Essence
    'environmentaltech:void_ore_miner_controller1': ('environmentaltech:void_ore_miner_cont_1', 0),
    'environmentaltech:void_ore_miner_controller2': ('environmentaltech:void_ore_miner_cont_2', 0),
    'environmentaltech:structure_frame1': ('environmentaltech:structure_frame_1', 0),
    'environmentaltech:structure_frame2': ('environmentaltech:structure_frame_2', 0),
}

PREREQ = {97: (149, 1118)}   # quete : (prerequis mort, remplacant)

# ---- Decisions du 28/09 (Alexis) ----------------------------------------
# Q141 : le Tesseract n'existe pas en TE 5.5.7 -> Quantum Entangloporter de
#        Mekanism (mekanism:machineblock3:0, verifie dans items.csv), meme role.
RENAME['thermalexpansion:tesseract'] = ('mekanism:machineblock3', 0)
Q141_NAME = '§l§3Quantum Entangloporter'
Q141_DESC = ("§7§oBonus : teleportation de ressources§r\n\n"
             "§7L'§6Entangloporter§r§7 de Mekanism envoie items,\n"
             "§7energie et fluides a travers les dimensions,\n"
             "§7SANS cable. Deux blocs, une meme frequence.\n\n"
             "§7Indispensable pour relier tes bases lointaines.\n\n"
             "§e§lObjectif : §7Craft 1x Quantum Entangloporter")

# Q2005 : le Sculk Tendril vient d'un mod absent, et le Grabber Voss n'a aucune
#         recette. La quete devient une etape narrative (case a cocher) qui
#         donne le Grabber : Q2006 se valide en le recevant. La commande
#         gamestage d'origine est gardee pour une future recette.
Q2005_NAME = '§l§6Le Signal'
Q2005_ICON = {'id:8': 'minecraft:noteblock', 'Count:3': 1, 'Damage:2': 0, 'OreDict:8': ''}
Q2005_DESC = ("§7Pour retrouver le §dSac du Sujet 46§r§7, il faut\n"
              "§7d'abord capter son signal.\n\n"
              "§7Descends sous la couche 20, ton carnet en main.\n"
              "§7Le signal y est plus net. Note ce que tu\n"
              "§7entends, puis remonte tout de suite.\n\n"
              "§8§o\"46 emettait encore. Je n'ai jamais su\n"
              "§8quoi.\"§r\n\n"
              "§e§lObjectif : §7Coche quand tu as capte le signal\n"
              "§e§lRecompense : §7Le Grabber Voss")
GRABBER = collections.OrderedDict([('id:8', 'nexusabsolu:grabber_voss'), ('Count:3', 1),
                                   ('Damage:2', 0), ('OreDict:8', '')])
Q2006_OLD = ("§7Avec ton tendril de sculk, tu peux assembler\n"
             "§7une replique du §dSac du Sujet 46§r§7.")
Q2006_NEW = ("§7Le signal t'a mene au §dGrabber§r§7 de Voss :\n"
             "§7une replique du §dSac du Sujet 46§r§7.")


def apply_decisions(byid):
    q = byid[141]['properties:10']['betterquesting:10']
    q['name:8'], q['desc:8'] = Q141_NAME, Q141_DESC

    q2005 = byid[2005]
    p = q2005['properties:10']['betterquesting:10']
    p['name:8'], p['desc:8'], p['icon:10'] = Q2005_NAME, Q2005_DESC, collections.OrderedDict(Q2005_ICON)
    q2005['tasks:9'] = collections.OrderedDict(
        [('0:10', collections.OrderedDict([('index:3', 0), ('taskID:8', 'bq_standard:checkbox')]))])
    items = q2005['rewards:9']['0:10']['rewards:9']
    block = items[0] if isinstance(items, list) else items
    if not any(v.get('id:8') == 'nexusabsolu:grabber_voss' for v in block.values()):
        block['%d:10' % len(block)] = GRABBER

    p = byid[2006]['properties:10']['betterquesting:10']
    p['desc:8'] = p['desc:8'].replace(Q2006_OLD, Q2006_NEW)

# nom de ligne (debut) : (lineID, order)
LINES = [('§l§5Age 0', 0, 0), ('§l§6Age 1', 1, 1), ('§l§bAge 2', 2, 2),
         ('§l§5Age 3', 4, 3), ('§l§aCoding', 3, 4)]


def walk_items(node):
    """Tous les BigItemStack d'une quete (icone, taches, recompenses)."""
    if isinstance(node, dict):
        if 'id:8' in node and 'Damage:2' in node:
            yield node
        for v in node.values():
            yield from walk_items(v)
    elif isinstance(node, list):
        for v in node:
            yield from walk_items(v)


def entries(block):
    if isinstance(block, dict):
        return list(block.values())
    return [w for v in block for w in (v.values() if isinstance(v, dict) and 'taskID:8' not in v else [v])]


def main():
    with open(DQ, encoding='utf-8') as f:
        raw = f.read()
    d = json.loads(raw, object_pairs_hook=collections.OrderedDict)
    n_ids = n_nbt = n_pre = 0
    for q in d['questDatabase:9'].values():
        for it in walk_items(q):
            if it['id:8'] in RENAME:
                it['id:8'], it['Damage:2'] = RENAME[it['id:8']]
                n_ids += 1
        qid = q['questID:3']
        if qid in PREREQ:
            old, new = PREREQ[qid]
            pre = q['preRequisites:11']
            if old in pre:
                pre[pre.index(old)] = new
                n_pre += 1
        for t in entries(q.get('tasks:9', {})):
            if t.get('taskID:8') != 'bq_standard:retrieval':
                continue
            items = list(walk_items(t.get('requiredItems:9', {})))
            if t.get('ignoreNBT:1', 0) == 0 and not any('tag:10' in it for it in items):
                t['ignoreNBT:1'] = 1
                n_nbt += 1
    apply_decisions({q['questID:3']: q for q in d['questDatabase:9'].values()})
    for l in d['questLines:9'].values():
        name = l['properties:10']['betterquesting:10']['name:8']
        match = [x for x in LINES if name.startswith(x[0])]
        assert len(match) == 1, 'ligne inattendue : %s' % name
        _, lid, order = match[0]
        l['lineID:3'] = lid
        l['order:3'] = order
        # lineID et order en tete de l'entree, comme la ligne Age 1 existante
        for k in ('properties:10', 'quests:9'):
            l.move_to_end(k)
    out = json.dumps(d, ensure_ascii=False, indent=2) + ('\n' if raw.endswith('\n') else '')
    if out != raw:
        with open(DQ, 'w', encoding='utf-8') as f:
            f.write(out)
    print('%d items renommes, %d prerequis, %d taches ignoreNBT, 5 lignes ordonnees'
          % (n_ids, n_pre, n_nbt))
    return 0


if __name__ == '__main__':
    sys.exit(main())
