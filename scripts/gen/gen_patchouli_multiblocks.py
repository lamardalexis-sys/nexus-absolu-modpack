# -*- coding: utf-8 -*-
"""Genere les multiblocs Patchouli en ligne depuis les machinery MM."""
import json,glob,os,re

SYM={'casings_decorative':'C','casings_all':'A','casings_fluid':'F',
     'casings_item':'I','casings_energy':'E'}
MAP={
 "C":"modularmachinery:blockcasing",
 "A":"modularmachinery:blockcasing",
 "F":"modularmachinery:blockfluidinputhatch",
 "I":"modularmachinery:blockinputbus",
 "E":"modularmachinery:blockenergyinputhatch",
 "0":"modularmachinery:blockcontroller",
}

def build(machine):
    d=json.load(open('config/modularmachinery/machinery/%s.json'%machine))
    cells={}
    for p in d['parts']:
        cells[(p['x'],p['y'],p['z'])]=SYM.get(p.get('elements'),'C')
    cells[(0,0,0)]='0'                       # le controleur
    ys=sorted({k[1] for k in cells}, reverse=True)   # haut -> bas
    xs=sorted({k[0] for k in cells}); zs=sorted({k[2] for k in cells})
    pattern=[]
    for y in ys:
        layer=[]
        for z in zs:
            layer.append(''.join(cells.get((x,y,z),' ') for x in xs))
        pattern.append(layer)
    used=sorted({c for l in pattern for r in l for c in r if c!=' '})
    return {"pattern":pattern,"mapping":{c:MAP[c] for c in used},"symmetrical":False}

# quelle page decrit quelle machine ?
PAGES={}
for lang in ('fr_fr','en_us'):
    base='mod-source/src/main/resources/assets/nexusabsolu/patchouli_books/voss_codex/%s/entries'%lang
    for p in sorted(glob.glob(base+'/*.json')):
        d=json.load(open(p,encoding='utf-8'))
        for pg in d.get('pages',[]):
            mid=pg.get('multiblock_id','')
            if mid.startswith('nexusabsolu:'):
                PAGES.setdefault(p,[]).append(mid.split(':')[1])
mach={os.path.basename(x)[:-5] for x in glob.glob('config/modularmachinery/machinery/*.json')}
print("=== pages a reparer ===")
allm=set()
for p,ms in PAGES.items():
    for m in ms: allm.add(m)
    print("  %-58s %s"%(p.split('/')[-3]+'/'+p.split('/')[-1], ', '.join(ms)))
print("\nmachines citees : %d | connues de MM : %d | inconnues : %s"%(
    len(allm), len(allm&mach), sorted(allm-mach) or 'aucune'))
json.dump({'pages':PAGES},open('/tmp/pages.json','w'))
