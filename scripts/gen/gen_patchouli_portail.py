# -*- coding: utf-8 -*-
"""Multibloc Patchouli du Portail Voss, genere depuis TilePortalVoss.java.
Relancer si la STRUCTURE statique du TileEntity change."""
import json,re,glob

SRC='mod-source/src/main/java/com/nexusabsolu/mod/tiles/TilePortalVoss.java'
CH={'W':'W','W2':'T','V3':'3','V4':'4','EIN':'E','LIN':'L','CD':'D','LAVA':'V'}
MAP={"W":"nexusabsolu:nexus_wall","T":"nexusabsolu:nexus_wall_t2",
     "3":"nexusabsolu:vossium_iii_block","4":"nexusabsolu:vossium_iv_block",
     "E":"nexusabsolu:energy_input","L":"nexusabsolu:fluid_input",
     "D":"nexusabsolu:compose_block_d","V":"minecraft:lava",
     "0":"nexusabsolu:ecran_controle"}

src=open(SRC,encoding='utf-8').read()
blk=src[src.index('static {'):src.index('private static void addRow')]
cells={}
for m in re.finditer(r'addRow\((-?\d+),\s*(-?\d+),\s*new int\[\]\{([^}]*)\}\)',blk):
    y,z=int(m.group(1)),int(m.group(2))
    for i,t in enumerate(x.strip() for x in m.group(3).split(',')):
        cells[(i-3,y,z)]=CH[t]
for m in re.finditer(r'add\((-?\d+),\s*(-?\d+),\s*(-?\d+),\s*(\w+)\)',blk):
    x,y,z,t=int(m.group(1)),int(m.group(2)),int(m.group(3)),m.group(4)
    cells[(x,y,z)]=CH[t]
cells[(0,0,0)]='0'                                  # Ecran de Controle

xs=range(-3,4); zs=range(-3,4); ys=sorted({k[1] for k in cells},reverse=True)
pattern=[[''.join(cells.get((x,y,z),' ') for x in xs) for z in zs] for y in ys]
used=sorted({c for l in pattern for r in l for c in r if c!=' '})
mb={"pattern":pattern,"mapping":{c:MAP[c] for c in used},"symmetrical":False}

n=0
for p in glob.glob('mod-source/src/main/resources/assets/nexusabsolu/patchouli_books/voss_codex/*/entries/portail_voss.json'):
    d=json.load(open(p,encoding='utf-8')); ch=False
    for pg in d.get('pages',[]):
        if pg.get('type')=='patchouli:multiblock':
            pg.pop('multiblock_id',None); pg['multiblock']=mb; ch=True
    if ch:
        json.dump(d,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=2); n+=1
print("Portail Voss : %d couches, %d blocs, %d symboles"%(
    len(pattern), sum(1 for v in cells.values()), len(used)))
for i,l in enumerate(pattern): print("  y=%+d %s"%(ys[i],l))
print("\n%d fichier(s) mis a jour"%n)
