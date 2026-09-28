# -*- coding: utf-8 -*-
"""Test des taches a NBT de la ligne Coding contre la comparaison de BetterQuesting.

Portage de betterquesting/api/utils/ItemComparison.java (BQ 1.12, 3.5.x) :
StackMatch + CompareNBTTag, pour verifier hors jeu que chaque tache accepte
l'item qu'un joueur aura vraiment en main, et refuse ce qu'elle doit refuser.
Les items "joueur" sont reconstitues d'apres le code des mods :
  - RFTools Control : ProgramCardInstance.writeToNBT / GridInstance.writeToNBT
  - ComputerCraft 1.80 : ItemPrintout.createFromTitleAndText
  - OpenComputers 1.8.9 : EEPROM.save, Loot (disquette OpenOS)

    python3 scripts/check/test_quests_coding.py
"""
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DQ = os.path.join(ROOT, 'config', 'betterquesting', 'DefaultQuests.json')
DAMAGEABLE = set()   # aucun item de la ligne n'est un outil ou une armure


# ------------------------------------------------------------ JSON-NBT de BQ

def from_bq(node):
    """"nom:type" -> nom ; les listes (:9) ecrites en objet {"0:10":...} -> list."""
    if isinstance(node, dict):
        out = {}
        for k, v in node.items():
            name, _, typ = k.rpartition(':')
            if typ == '9' and isinstance(v, dict):
                out[name] = [from_bq(x) for x in v.values()]
            else:
                out[name] = from_bq(v)
        return out
    if isinstance(node, list):
        return [from_bq(x) for x in node]
    return node


# ------------------------------------------------------------ ItemComparison

def is_empty(tag):
    return tag is None or (isinstance(tag, (dict, list)) and len(tag) == 0)


def compare(t1, t2, partial):
    """CompareNBTTag(requis, joueur, partial)."""
    if is_empty(t1) != is_empty(t2):
        return False
    if is_empty(t1):
        return True
    if isinstance(t1, dict) and isinstance(t2, dict):
        for k, v in t1.items():            # CompareNBTTagCompound
            if k not in t2 or not compare(v, t2[k], partial):
                return False
        return True
    if isinstance(t1, list) and isinstance(t2, list):
        if len(t1) > len(t2) or (not partial and len(t1) != len(t2)):
            return False
        return all(any(compare(a, b, partial) for b in t2) for a in t1)
    if isinstance(t1, (int, float)) and isinstance(t2, (int, float)):
        return float(t1) == float(t2)
    return type(t1) == type(t2) and t1 == t2


def stack_match(req, stack, nbt_check, partial):
    if req['id'] != stack['id']:
        return False
    if not (req['Damage'] == stack['Damage'] or req['id'] in DAMAGEABLE
            or req['Damage'] == 32767):
        return False
    if nbt_check and not compare(req.get('tag'), stack.get('tag'), partial):
        return False
    return True


def task_accepts(task, inventory):
    """Tache retrieval sans consommation : chaque item requis doit etre present."""
    nbt_check = not task['ignoreNBT']
    partial = bool(task['partialMatch'])
    for req in task['requiredItems']:
        have = sum(s['Count'] for s in inventory if stack_match(req, s, nbt_check, partial))
        if have < req['Count']:
            return False
    return True


# ------------------------------------------------------------ items "joueur"

def stack(iid, meta=0, count=1, tag=None):
    s = {'id': iid, 'Damage': meta, 'Count': count}
    if tag is not None:
        s['tag'] = tag
    return s


def card(*ops, name='mon programme'):
    grid = [{'x': i, 'y': 0, 'id': op, 'prim': 'r', 'pars': [{'t': 0}]}
            for i, op in enumerate(ops)]
    return stack('rftoolscontrol:program_card', tag={'grid': grid, 'name': name})


def page(title, meta=0):
    tag = {'pages': 1}
    if title is not None:
        tag['title'] = title
    for i in range(21):
        tag['line%d' % i] = ' ' * 25
        tag['colour%d' % i] = 'f' * 25
    return stack('computercraft:printout', meta, tag=tag)


def eeprom(label):
    return stack('opencomputers:storage', 0, tag={'oc:data': {
        'oc:eeprom': b'print(1)', 'oc:label': label, 'oc:readonly': 0,
        'node': {'address': 'abc'}}})


OPENOS = stack('opencomputers:storage', 1, tag={
    'oc:lootFactory': 'opencomputers:openos', 'oc:color': 2,
    'oc:data': {'oc:fs.label': 'openos'}, 'display': {'Name': 'OpenOS (Operating System)'}})
BLANK_FLOPPY = stack('opencomputers:storage', 1)
LUA_BIOS = eeprom('EEPROM (Lua BIOS)')

CASES = {
    3007: [([card('ev_timer', 'do_delay', 'do_rs')], True),
           ([card('ev_timer', 'do_delay')], False),
           ([stack('rftoolscontrol:program_card')], False)],
    3008: [([card('ev_timer', 'eval_countinv', 'do_setvar')], True),
           ([card('eval_countinv')], False)],
    3009: [([card('ev_timer', 'eval_countinv', 'test_gt_number', 'do_rs', 'do_rs')], True),
           ([card('eval_countinv', 'test_eq_number', 'do_rs')], False)],
    3010: [([card('eval_countinv', 'test_gt_number', 'do_fetchitems', 'do_pushitems')], True),
           ([card('do_fetchitems')], False)],
    3011: [([card('ev_timer', 'eval_countinv', 'do_setvar', 'do_subtract_numbers',
                  'do_setvar', 'eval_number', 'do_setvar')], True),
           ([card('ev_timer', 'eval_countinv')], False)],
    3021: [([page('Premiere page')], True),
           ([page('Premiere page', meta=2)], True),        # relie en livre
           ([page('premiere page')], False),               # casse
           ([page(None)], False)],
    3023: [([LUA_BIOS, OPENOS, stack('opencomputers:diskdrive'),
             stack('opencomputers:storage', 2)], True),
           ([LUA_BIOS, BLANK_FLOPPY, stack('opencomputers:diskdrive'),
             stack('opencomputers:storage', 2)], False),
           ([eeprom('EEPROM'), OPENOS, stack('opencomputers:diskdrive'),
             stack('opencomputers:storage', 2)], False)],
    3027: [([eeprom('Premier Flash')], True),
           ([LUA_BIOS], False)],
    3028: [([page('Rapport de Production')], True),
           ([eeprom('Rapport de Production')], True),
           ([page('Rapport de production')], False),
           ([eeprom('Rapport')], False)],
    3018: [([stack('computercraft:turtle', 1)], True),
           ([stack('computercraft:turtle_expanded', 0, tag={'leftUpgrade': 5})], True),
           ([stack('computercraft:turtle_advanced', 0, tag={'computerID': 3})], True),
           ([stack('computercraft:computer', 0)], False)],
}


def load_quests():
    with open(DQ, encoding='utf-8') as f:
        d = json.load(f)
    return {v['questID:3']: v for v in d['questDatabase:9'].values()}


def quest_accepts(q, inventory):
    logic = q['properties:10']['betterquesting:10']['tasklogic:8']
    results = []
    for t in q['tasks:9'].values():
        t = from_bq(t)
        if t['taskID'] == 'bq_standard:checkbox':
            results.append(True)       # le joueur coche
        else:
            results.append(task_accepts(t, inventory))
    return any(results) if logic == 'OR' else all(results)


def main():
    quests = load_quests()
    fails = 0
    total = 0
    for qid, cases in sorted(CASES.items()):
        for inv, expected in cases:
            total += 1
            got = quest_accepts(quests[qid], inv)
            if got != expected:
                fails += 1
                print('  ECHEC  Q%d : attendu %s, obtenu %s pour %s'
                      % (qid, expected, got, [s['id'] + ':' + str(s['Damage']) for s in inv]))
    print('%d cas, %d echec(s)' % (total, fails))
    return 1 if fails else 0


if __name__ == '__main__':
    sys.exit(main())
