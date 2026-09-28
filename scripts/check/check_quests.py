# -*- coding: utf-8 -*-
"""Controle de config/betterquesting/DefaultQuests.json.

Verifie ce qui rend une quete impossible sans que BetterQuesting le dise :
  - prerequis vers une quete inexistante (la quete reste verrouillee pour
    toujours, QuestInstance.isUnlocked) ;
  - item de tache, d'icone ou de recompense absent du registre (BQ le remplace
    par un placeholder : la tache ne peut plus etre validee) ;
  - meta inconnue pour un item dont on connait les sous-items ;
  - entree de ligne vers une quete absente, quete dans aucune ligne ;
  - ligne sans lineID/order (ordre des onglets imprevisible) ;
  - tache retrieval sans ses cles (une cle absente vaut 0 dans BQ).

Registre des items = union de deux sources du pack, sans jar :
  - config/AppliedEnergistics2/items.csv : dump AE2 fait en jeu (id + meta) ;
  - world-template/level.dat et backups/*.zip : registre FML "minecraft:items"
    (id seul).
Les items contenttweaker: et nexusabsolu: recents peuvent manquer des deux :
ils sont signales en avertissement, pas en erreur.

    python3 scripts/check/check_quests.py          # erreurs et resume
    python3 scripts/check/check_quests.py -v       # avec les avertissements
    python3 scripts/check/check_quests.py --line 3 # une seule ligne (lineID)

Code de sortie 1 s'il y a une erreur dans le perimetre controle.
"""
import collections
import glob
import gzip
import io
import json
import os
import re
import struct
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DQ = os.path.join(ROOT, 'config', 'betterquesting', 'DefaultQuests.json')
CSV = os.path.join(ROOT, 'config', 'AppliedEnergistics2', 'items.csv')
SOFT = ('contenttweaker', 'nexusabsolu')
RETRIEVAL_KEYS = ('consume:1', 'autoConsume:1', 'groupDetect:1', 'ignoreNBT:1', 'partialMatch:1')


# ------------------------------------------------------------ NBT (level.dat)

def _nbt(buf, t):
    """Lit une valeur NBT de type t. Renvoie (valeur, reste)."""
    if t == 1:
        return struct.unpack('>b', buf.read(1))[0]
    if t == 2:
        return struct.unpack('>h', buf.read(2))[0]
    if t == 3:
        return struct.unpack('>i', buf.read(4))[0]
    if t == 4:
        return struct.unpack('>q', buf.read(8))[0]
    if t == 5:
        return struct.unpack('>f', buf.read(4))[0]
    if t == 6:
        return struct.unpack('>d', buf.read(8))[0]
    if t == 7:
        n = struct.unpack('>i', buf.read(4))[0]
        return buf.read(n)
    if t == 8:
        n = struct.unpack('>H', buf.read(2))[0]
        return buf.read(n).decode('utf-8', 'replace')
    if t == 9:
        et = buf.read(1)[0]
        n = struct.unpack('>i', buf.read(4))[0]
        return [_nbt(buf, et) for _ in range(n)]
    if t == 10:
        out = {}
        while True:
            ct = buf.read(1)[0]
            if ct == 0:
                return out
            name = _nbt(buf, 8)
            out[name] = _nbt(buf, ct)
    if t == 11:
        n = struct.unpack('>i', buf.read(4))[0]
        return list(struct.unpack('>%di' % n, buf.read(4 * n)))
    if t == 12:
        n = struct.unpack('>i', buf.read(4))[0]
        return list(struct.unpack('>%dq' % n, buf.read(8 * n)))
    raise ValueError('type NBT %d' % t)


def _registry_from_level(raw):
    buf = io.BytesIO(gzip.decompress(raw))
    buf.read(1)
    _nbt(buf, 8)
    root = _nbt(buf, 10)
    reg = root.get('FML', {}).get('Registries', {}).get('minecraft:items', {})
    return {e.get('K') for e in reg.get('ids', [])}


def level_dat_items():
    """Noms du registre FML des items : world-template (le plus recent) et la
    derniere sauvegarde de backups/."""
    names = set()
    tpl = os.path.join(ROOT, 'world-template', 'level.dat')
    if os.path.exists(tpl):
        with open(tpl, 'rb') as f:
            names |= _registry_from_level(f.read())
    for z in sorted(glob.glob(os.path.join(ROOT, 'backups', '*.zip')))[-1:]:
        with zipfile.ZipFile(z) as zf:
            ld = [n for n in zf.namelist() if re.search(r'(^|[\\/])[^\\/]+[\\/]level\.dat$', n)
                  and 'DIM' not in n]
            for n in ld[:1]:
                names |= _registry_from_level(zf.read(n))
    return names


def registry():
    metas = collections.defaultdict(set)
    if os.path.exists(CSV):
        with open(CSV, encoding='utf-8', errors='replace') as f:
            next(f)
            for line in f:
                key = line.split(',', 1)[0].strip()
                m = re.match(r'^([^:]+:[^:]+)(?::(\d+))?$', key)
                if m:
                    metas[m.group(1)].add(int(m.group(2) or 0))
    # Le dump AE2 (avril) est plus recent que la sauvegarde (mars) : pour un mod
    # qu'il connait, il fait foi (un module retire depuis, comme EnderIO-machines,
    # est encore dans level.dat). La sauvegarde ne sert que pour les mods absents
    # du dump, comme ComputerCraft.
    csv_mods = {n.split(':')[0] for n in metas}
    names = set(metas) | {n for n in level_dat_items() if n.split(':')[0] not in csv_mods}
    return names, metas


# ------------------------------------------------------------ controle

def _entries(block):
    """Elements d'une liste NBT-JSON, ecrite en objet {"0:10":...} ou en liste
    JSON (certaines quetes du pack contiennent [ {"0:10":..., "1:10":...} ])."""
    if isinstance(block, dict):
        for v in block.values():
            yield v
    elif isinstance(block, list):
        for v in block:
            if isinstance(v, dict) and 'id:8' not in v:
                for w in v.values():
                    yield w
            else:
                yield v


def iter_items(q):
    p = q['properties:10']['betterquesting:10']
    if 'icon:10' in p:
        yield 'icone', p['icon:10']
    for t in _entries(q.get('tasks:9', {})):
        for it in _entries(t.get('requiredItems:9', {})):
            yield 'tache', it
    for r in _entries(q.get('rewards:9', {})):
        for key in ('rewards:9', 'choices:9'):
            for it in _entries(r.get(key, {})):
                yield 'recompense', it


def main(argv):
    verbose = '-v' in argv
    only_line = None
    if '--line' in argv:
        only_line = int(argv[argv.index('--line') + 1])
    with open(DQ, encoding='utf-8') as f:
        d = json.load(f)
    names, metas = registry()
    byid = {}
    errors, warns = [], []
    for v in d['questDatabase:9'].values():
        qid = v['questID:3']
        if qid in byid:
            errors.append((qid, 'questID en double'))
        byid[qid] = v
    in_line = collections.defaultdict(list)
    for l in d['questLines:9'].values():
        name = l['properties:10']['betterquesting:10']['name:8']
        if 'lineID:3' not in l or 'order:3' not in l:
            errors.append((None, 'ligne sans lineID/order : %s' % name))
        for e in l['quests:9'].values():
            in_line[e['id:3']].append(l.get('lineID:3'))
            if e['id:3'] not in byid:
                errors.append((e['id:3'], 'entree de la ligne %s vers une quete absente' % name))

    def scope(qid):
        return only_line is None or only_line in in_line.get(qid, [])

    for qid, q in sorted(byid.items()):
        if not scope(qid):
            continue
        qname = q['properties:10']['betterquesting:10'].get('name:8', '')
        if qid not in in_line:
            warns.append((qid, 'dans aucune ligne : %s' % qname))
        for p in q.get('preRequisites:11', []):
            if p not in byid:
                errors.append((qid, 'prerequis %d inexistant : quete bloquee pour toujours (%s)'
                               % (p, qname)))
        for t in _entries(q.get('tasks:9', {})):
            if t.get('taskID:8') == 'bq_standard:retrieval':
                missing = [k for k in RETRIEVAL_KEYS if k not in t]
                if missing:
                    warns.append((qid, 'retrieval sans %s (vaut 0)' % ', '.join(missing)))
        for where, it in iter_items(q):
            iid = it.get('id:8', '')
            meta = it.get('Damage:2', 0)
            if iid not in names:
                bucket = warns if iid.split(':')[0] in SOFT else errors
                bucket.append((qid, '%s : item %s absent du registre (%s)' % (where, iid, qname)))
            elif iid in metas and meta not in (32767, -1) and meta not in metas[iid] \
                    and len(metas[iid]) > 1:
                warns.append((qid, '%s : %s meta %d absente du dump AE2 (metas connues %s)'
                              % (where, iid, meta, sorted(metas[iid])[:12])))
    for qid, msg in errors:
        print('  ERREUR  Q%s  %s' % (qid, msg))
    if verbose:
        for qid, msg in warns:
            print('  attention  Q%s  %s' % (qid, msg))
    print('\n%d quetes, registre %d items, %d erreur(s), %d avertissement(s)%s'
          % (len(byid), len(names), len(errors), len(warns),
             '' if only_line is None else ' (ligne %d)' % only_line))
    return 1 if errors else 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
