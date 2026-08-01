# -*- coding: utf-8 -*-
"""Remplace les descriptions de couches du Carnet par une version generee
depuis les patterns Modular Machinery reels.
Relancer apres toute modification des machinery JSON."""
import json,glob,os,re,collections

LAB={'casings_decorative':'casing decoratif','casings_all':'casing ou hatch au choix',
     'casings_fluid':'hatch a fluide','casings_item':'bus a items','casings_energy':'hatch energie'}
LABEN={'casings_decorative':'decorative casing','casings_all':'casing or hatch',
     'casings_fluid':'fluid hatch','casings_item':'item bus','casings_energy':'energy hatch'}

def describe(m,lang='fr_fr'):
    d=json.load(open('config/modularmachinery/machinery/%s.json'%m))
    L=LAB if lang=='fr_fr' else LABEN
    per=collections.defaultdict(collections.Counter)
    for p in d['parts']: per[p['y']][p.get('elements','casings_all')]+=1
    xs={p['x'] for p in d['parts']}|{0}; ys={p['y'] for p in d['parts']}|{0}; zs={p['z'] for p in d['parts']}|{0}
    dims="%dx%dx%d"%(max(xs)-min(xs)+1,max(ys)-min(ys)+1,max(zs)-min(zs)+1)
    n=len(d['parts'])+1
    head=(("$(b)%s$(/b) -- %s, %d blocs controleur compris." if lang=='fr_fr'
           else "$(b)%s$(/b) -- %s, %d blocks including the controller.")
          %(d.get('localizedname',m),dims,n))
    lines=[]
    for y in sorted(per,reverse=True):
        parts=", ".join("%d %s"%(c,L[k]) for k,c in sorted(per[y].items(),key=lambda x:-x[1]))
        lines.append("$(li)Y=%+d : %s$(/li)"%(y,parts))
    ctrl=("$(li)Y=+0 : le Machine Controller au centre$(/li)" if lang=='fr_fr'
          else "$(li)Y=+0: the Machine Controller at the center$(/li)")
    if 0 not in per: lines.insert(len(lines)-1 if len(lines)>1 else 0, ctrl)
    tail=("$(br2)$(o)La page suivante montre la structure en 3D. Tourne la souris, "
          "maintiens shift pour isoler une couche.$(/o)" if lang=='fr_fr' else
          "$(br2)$(o)The next page shows the structure in 3D. Drag to rotate, hold "
          "shift to isolate one layer.$(/o)")
    return head+"$(br)"+"".join(lines)+tail

ALIAS={'MB-DESA':'vacuum_chamber','MB-HDS':'hds_tower','MB-OSMOSE':'osmose_inverse',
'MB-CK':'ck_cell','MB-FLUORITE':'fluorite_cell','MB-FOUR-ELEC':'electric_furnace',
'MB-HALL':'hall_heroult_cell','MB-KROLL':'kroll_reactor','MB-AQUA-REGIA':'aqua_regia_cell',
'MB-GAMMA-FORGE':'gamma_forge','MB-LIT-CHAMBER':'lit_chamber','MB-HABER':'haber_reactor',
'MB-OSTWALD':'ostwald_tower','MB-CONTACT':'contact_tower','MB-CRACKER':'thermal_cracker',
'MB-CUMENE':'cumene_reactor','MB-AROMATIC':'aromatic_reactor','MB-FERMENTER':'fermenter',
'MB-SOXHLET':'soxhlet_extractor','MB-CYCLO':'cyclisateur_stellaire',
'MB-CYCLISATEUR':'cyclisateur_stellaire','MB-EVAPORATOR':'evaporator',
'MB-ALAMBIC':'alambic_manaic','MB-MANA-ENCHANTER':'mana_enchanter','M1':'melangeur_cryogenique'}

Y=re.compile(r'Y=[-+]?\d')
tot=0; files=0
for lang in ('fr_fr','en_us'):
    base='mod-source/src/main/resources/assets/nexusabsolu/patchouli_books/voss_codex/%s/entries'%lang
    for p in sorted(glob.glob(base+'/*.json')):
        if 'intro_guide' in p: continue          # page de notation, a garder
        d=json.load(open(p,encoding='utf-8')); ch=False
        for pg in d.get('pages',[]):
            t=pg.get('text') or ''
            if len(Y.findall(t))<2: continue
            hay=(pg.get('title') or '')+' '+t
            best=None
            for k,v in ALIAS.items():
                i=hay.find(k)
                if i>=0 and (best is None or i<best[0]): best=(i,v)
            if not best: continue
            pg['text']=describe(best[1],lang); ch=True; tot+=1
        if ch:
            json.dump(d,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=2); files+=1
print("descriptions regenerees : %d dans %d fichiers"%(tot,files))
