# -*- coding: utf-8 -*-
"""Recale les dimensions citees dans les textes du Carnet sur les
machinery JSON reels. Relancer apres toute modification des patterns."""
import json,glob,os,re

dims={}; blocs={}
for p in glob.glob('config/modularmachinery/machinery/*.json'):
    d=json.load(open(p)); n=os.path.basename(p)[:-5]
    xs={q['x'] for q in d['parts']}|{0}; ys={q['y'] for q in d['parts']}|{0}; zs={q['z'] for q in d['parts']}|{0}
    dims[n]="%dx%dx%d"%(max(xs)-min(xs)+1,max(ys)-min(ys)+1,max(zs)-min(zs)+1)
    blocs[n]=len(d['parts'])+1

# nom affiche dans le Carnet -> machine MM
ALIAS={'MB-DESA':'vacuum_chamber','MB-HDS':'hds_tower','MB-OSMOSE':'osmose_inverse',
'MB-CK':'ck_cell','MB-FLUORITE':'fluorite_cell','MB-FOUR-ELEC':'electric_furnace',
'MB-HALL':'hall_heroult_cell','MB-KROLL':'kroll_reactor','MB-AQUA-REGIA':'aqua_regia_cell',
'MB-GAMMA-FORGE':'gamma_forge','MB-LIT-CHAMBER':'lit_chamber','MB-HABER':'haber_reactor',
'MB-OSTWALD':'ostwald_tower','MB-CONTACT':'contact_tower','MB-CRACKER':'thermal_cracker',
'MB-CUMENE':'cumene_reactor','MB-AROMATIC':'aromatic_reactor','MB-FERMENTER':'fermenter',
'MB-SOXHLET':'soxhlet_extractor','MB-CYCLISATEUR':'cyclisateur_stellaire',
'MB-EVAPORATOR':'evaporator','MB-ALAMBIC':'alambic_manaic','MB-MANA-ENCHANTER':'mana_enchanter',
'Mana Enchanter':'mana_enchanter','Reacteur Kroll':'kroll_reactor','Kroll Reactor':'kroll_reactor',
'Tour Contact':'contact_tower','Contact Tower':'contact_tower','Bio-Reacteur':'bioreacteur',
'Bio-Reactor':'bioreacteur','Forge Gamma':'gamma_forge','Gamma Forge':'gamma_forge',
'Cyclisateur Stellaire':'cyclisateur_stellaire','Stellar Cyclizer':'cyclisateur_stellaire',
'M1':'melangeur_cryogenique','Melangeur Cryogenique':'melangeur_cryogenique'}
NAMES=sorted(set(list(ALIAS)+list(dims)), key=len, reverse=True)

DIM=re.compile(r'\b\d+x\d+x\d+\b')
tot=0; files=0
for p in sorted(glob.glob('mod-source/src/main/resources/assets/nexusabsolu/patchouli_books/**/*.json',recursive=True)):
    txt=open(p,encoding='utf-8').read(); orig=txt
    out=[]; last=0
    for m in DIM.finditer(txt):
        # cherche le nom de machine le plus proche a gauche (200 car.)
        ctx=txt[max(0,m.start()-200):m.start()]
        found=None
        for nm in NAMES:
            if nm in ctx:
                cand=ALIAS.get(nm,nm)
                if cand in dims and (found is None or ctx.rfind(nm)>ctx.rfind(found[0])):
                    found=(nm,cand)
        if not found: continue
        real=dims[found[1]]
        if m.group(0)!=real:
            out.append((m.start(),m.end(),real)); tot+=1
    for s,e,r in reversed(out): txt=txt[:s]+r+txt[e:]
    # nb de blocs cites
    for nm,mc in ALIAS.items():
        txt=re.sub(r'(%s[^)]{0,40}?)(\d+) blocs'%re.escape(nm),
                   lambda M,mc=mc: M.group(1)+str(blocs[mc])+' blocs', txt)
    if txt!=orig:
        open(p,'w',encoding='utf-8').write(txt); files+=1
print("dimensions recalees : %d, dans %d fichiers"%(tot,files))
