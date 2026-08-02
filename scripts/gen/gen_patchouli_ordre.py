# -*- coding: utf-8 -*-
"""Regenere l'entree 'Ordre de montage optimal' du Carnet depuis le graphe
de dependances reel des recettes Modular Machinery.
Relancer apres toute modification des recettes ou des machineries."""
import json,glob,os,collections

R={}
for p in sorted(glob.glob('config/modularmachinery/recipes/*.json')):
    d=json.load(open(p)); n=os.path.basename(p)[:-5]
    ins=[];outs=[];e=0;t=d.get('recipeTime',0)
    for r in d.get('requirements',[]):
        ty=r['type'].split(':')[-1]
        if ty=='energy' and r.get('io-type')=='input': e=r.get('energyPerTick',0); continue
        if ty not in('item','fluid'): continue
        k=(ty,r.get('item') or r.get('fluid'))
        (outs if r.get('io-type')=='output' else ins).append(k)
    R[n]=dict(m=d['machine'],ins=ins,outs=outs,e=e,t=t)
prod=collections.defaultdict(list)
for n,r in R.items():
    for k in r['outs']: prod[k].append(n)
need=set()
def walk(k,seen=None):
    seen=seen or set()
    if k in seen: return
    seen.add(k)
    for n in prod.get(k,[])[:1]:
        need.add(R[n]['m'])
        for kk in R[n]['ins']: walk(kk,seen)
walk(('item','nexusabsolu:cartouche_manifold'))
def depth(k,vis=None):
    vis=vis or set()
    if k in vis: return 0
    vis=vis|{k}
    ps=prod.get(k)
    if not ps: return 0
    return 1+max([depth(i,vis) for i in R[ps[0]]['ins']] or [0])
md=collections.defaultdict(int)
for n,r in R.items():
    if r['m'] not in need: continue
    md[r['m']]=max(md[r['m']],max([depth(k) for k in r['ins']] or [0]))
byd=collections.defaultdict(list)
for m,d in md.items(): byd[d].append(m)
waves=[sorted(byd[d]) for d in sorted(byd)]

NAME={}; BLK={}; DIMS={}
for p in glob.glob('config/modularmachinery/machinery/*.json'):
    d=json.load(open(p)); n=os.path.basename(p)[:-5]
    NAME[n]=d.get('localizedname',n); BLK[n]=len(d['parts'])+1
    xs={q['x'] for q in d['parts']}|{0}; ys={q['y'] for q in d['parts']}|{0}; zs={q['z'] for q in d['parts']}|{0}
    DIMS[n]='%dx%dx%d'%(max(xs)-min(xs)+1,max(ys)-min(ys)+1,max(zs)-min(zs)+1)
rf=collections.defaultdict(int)
for n,r in R.items():
    if r['m'] in need: rf[r['m']]+=r['e']*r['t']

def li(s): return "$(li)%s$(/li)"%s
def pages(lang):
    fr = lang=='fr_fr'
    P=[]
    tot=sum(BLK[m] for w in waves for m in w)
    P.append({"type":"text","title":"Pourquoi un ordre" if fr else "Why an order",
      "text":(("Vingt-deux machines, %d blocs a poser. Si vous vous lancez sans plan, "
        "vous construirez une machine qui attend indefiniment ce qu'une autre n'a pas "
        "encore produit.$(br2)L'ordre qui suit n'est pas un avis : il sort du graphe de "
        "dependances des recettes. Une machine d'une vague ne peut pas tourner tant que "
        "toutes celles des vagues precedentes ne tournent pas.$(br2)$(o)La vague 1 est le "
        "seul point d'entree. Ses intrants viennent de la table de craft et des autres "
        "mods, pas d'une de vos machines.$(/o)")%tot) if fr else
        (("Twenty-two machines, %d blocks to place. Start without a plan and you will "
        "build a machine that waits forever on something another one has not made yet."
        "$(br2)This order is not an opinion: it comes from the recipe dependency graph. "
        "A machine in one wave cannot run until every machine in the previous waves does."
        "$(br2)$(o)Wave 1 is the only entry point. Its inputs come from the crafting "
        "table and other mods, not from your machines.$(/o)")%tot)})
    grp=[(0,4),(4,8),(8,11),(11,14)]
    for gi,(a,b) in enumerate(grp,1):
        items=[]
        for wi in range(a,b):
            if wi>=len(waves): break
            for m in waves[wi]:
                items.append(li("$(#248)%s$() -- %s, %d blocs%s"%(
                    NAME[m], DIMS[m], BLK[m],
                    (", %.0f M RF"%(rf[m]/1e6)) if rf[m]>=1e6 else "")))
        t=("Vagues %d a %d" if fr else "Waves %d to %d")%(a+1,min(b,len(waves)))
        P.append({"type":"text","title":t,"text":"".join(items)})
    P.append({"type":"text","title":"Le moment final" if fr else "The final moment",
      "text":("Les six composes sortent du M1. La Cartouche Chargee s'assemble a la table "
        "-- c'est le seul craft de table legitime au sommet de la chaine. Puis le "
        "Bio-Reacteur : cartouche chargee, Solution Epsilon, 4000 mB de Liquid Starlight, "
        "500 mB d'argon, 60 millions de RF.$(br2)$(o)Rien de ce que vous avez construit "
        "n'etait necessaire a la production de cet objet. L'usine n'etait pas le moyen. "
        "L'usine etait la mesure.$(/o)") if fr else
        ("The six compounds come out of M1. The Charged Cartridge is assembled on a "
        "crafting table -- the only legitimate table craft at the top of the chain. Then "
        "the Bio-Reactor: charged cartridge, Epsilon Solution, 4000 mB of Liquid "
        "Starlight, 500 mB of argon, 60 million RF.$(br2)$(o)None of what you built was "
        "necessary to produce this object. The factory was not the means. The factory "
        "was the measurement.$(/o)")})
    return P

n=0
for lang in ('fr_fr','en_us'):
    p='mod-source/src/main/resources/assets/nexusabsolu/patchouli_books/voss_codex/%s/entries/ordre_montage_optimal.json'%lang
    d=json.load(open(p,encoding='utf-8'))
    d['pages']=pages(lang); json.dump(d,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=2); n+=1
print("%d vagues, %d machines, %d blocs"%(len(waves),sum(len(w) for w in waves),sum(BLK[m] for w in waves for m in w)))
for i,w in enumerate(waves,1): print("  vague %-2d : %s"%(i,', '.join(NAME[m] for m in w)))
print("\n%d fichiers regeneres"%n)
