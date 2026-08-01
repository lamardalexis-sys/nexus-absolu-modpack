# -*- coding: utf-8 -*-
import sys, random, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from PIL import Image
import shapes
from palettes import P

def cl(v): return max(0,min(255,int(v)))
def mul(c,k): return tuple(cl(v*k) for v in c)
def mix(a,b,t): return tuple(cl(a[i]+(b[i]-a[i])*t) for i in range(3))

# multiplicateurs par face -- bords durs, pas de degrade continu
# ratios mesures sur ThermalFoundation ingot_copper :
# shine 1.83 / top 1.58 / front 1.00 / side 0.60 / outline 0.45
FACE = {'T':1.58, 'F':1.00, 'R':0.60}

def render(name, fam, base, accent):
    SEED=sum(ord(c)*(i+7) for i,c in enumerate(name))
    rnd=random.Random(SEED)
    g,f = shapes.SHAPES[fam if fam in shapes.SHAPES else 'dust']()
    img=Image.new('RGBA',(32,32),(0,0,0,0)); px=img.load()
    outline = mul(base,0.45)
    shine   = mix(base,(255,255,255),0.62)

    def solid(x,y): return 0<=x<32 and 0<=y<32 and g[y][x]=='#'

    _px=[(x,y) for y in range(32) for x in range(32) if g[y][x]=='#']
    BX0=min(p[0] for p in _px); BX1=max(p[0] for p in _px)
    BY0=min(p[1] for p in _px); BY1=max(p[1] for p in _px)
    # bbox PAR FACE : un gradient calcule sur la boite globale ne parcourt
    # qu'une fraction de sa course sur une face donnee, et s'ecrase.
    FB={}
    for _f in ('T','F','R'):
        pts=[(x,y) for y in range(32) for x in range(32) if f[y][x]==_f]
        FB[_f]=(min(p[0] for p in pts),max(p[0] for p in pts),
                min(p[1] for p in pts),max(p[1] for p in pts)) if pts else (0,1,0,1)

    for y in range(32):
        for x in range(32):
            if g[y][x]!='#': continue
            face=f[y][x] if f[y][x] in FACE else 'F'
            c=mul(base,FACE[face])

            # --- details par famille (gros, lisibles a 32px) ---
            if fam=='ingot':
                fx0,fx1,fy0,fy1=FB[face]
                u=(x-fx0)/max(1,fx1-fx0)
                v=(y-fy0)/max(1,fy1-fy0)
                if face=='T':
                    c=mul(c, 1.30-0.30*v-0.12*u)      # lisere du dessus
                elif face=='F':
                    c=mul(c, 1.06-0.24*v-0.14*u)      # face avant
                else:
                    c=mul(c, 0.66-0.10*v)             # cote droit
                # ECLAT : une bande diagonale claire qui traverse la face
                # avant. C'est elle qui distingue les metaux entre eux --
                # sa largeur et son intensite suivent la couleur d'accent.
                if face=='F':
                    d=(x-fx0)-(y-fy0)*0.55
                    band=(fx1-fx0)*0.30
                    if abs(d-band)<2.2:
                        c=mix(c,shine,0.55)
                    elif abs(d-band)<3.6:
                        c=mix(c,accent,0.35)
                # grain metallique en amas 2x2, deterministe
                gx,gy=(x//2)*2,(y//2)*2
                h=(gx*73856093 ^ gy*19349663 ^ SEED)&0xFFFF
                q=h/65535.0
                if face=='T' and q<0.12: c=mix(c,accent,0.50)
                elif face=='F' and q<0.07: c=mix(c,accent,0.30)
            elif fam=='dust':
                # TF ne bruite pas pixel par pixel : la poudre est faite de
                # GRAINS groupes. On pose des amas 2x2 a positions fixes
                # (deterministe par item), pas un mouchetis aleatoire.
                gx,gy=(x//2)*2,(y//2)*2
                h=(gx*73856093 ^ gy*19349663 ^ SEED) & 0xFFFF
                q=h/65535.0
                if q<0.22:   c=mix(c,accent,0.80)     # grain clair
                elif q<0.38: c=mul(c,0.68)            # creux entre grains
                elif q<0.48: c=mul(c,1.12)            # facette exposee
            elif fam=='capsule':
                if 12<=x<=19 and 10<=y<=25:          # fenetre du contenu
                    t=(y-10)/15.0
                    c=mix(mul(accent,1.18),mul(accent,0.55),t)
                    if x<=13: c=mix(c,(255,255,255),0.30)
                elif y<8 or y>=26: c=mul(mix(base,(150,156,166),0.5),0.88)
                else: c=mix(c,(186,192,202),0.45)
            elif fam=='catalyst':
                # Pastilles EN CREUX dans la coupelle : chaque pastille a un
                # bord haut sombre (l'ombre du creux) et un corps clair.
                # 5 pastilles en quinconce, lisibles a 32px.
                inp=False
                for (cx,cy) in ((11,12),(21,12),(16,17),(11,22),(21,22)):
                    dx,dy=x-cx,y-cy
                    d2=dx*dx+dy*dy
                    if d2<=10:
                        inp=True
                        if dy<-1 and d2>4:  c=mul(base,0.48)          # ombre du creux
                        elif dy>1 and d2>4: c=mix(base,accent,0.75)   # eclat bas
                        else:               c=mix(base,accent,0.42)
                if not inp:
                    c=mul(c, 0.82 if face=='T' else 0.58)
            elif fam=='gauze':
                # Vraie trame tissee : des fils de 2px qui passent dessus /
                # dessous. Un damier 1px se lit comme du bruit a l'ecran.
                over = ((x//2) % 2) == ((y//2) % 2)
                if over:
                    c=mix(base,accent,0.55)                    # fil au-dessus
                    if y%2==0: c=mul(c,1.14)                   # arrondi du fil
                else:
                    c=mul(base,0.55)                           # fil en dessous
                    if x%2==0: c=mul(c,1.10)
                # ombre a chaque croisement
                if x%4==3 and y%4==3: c=mul(c,0.72)
            elif fam=='crystal':
                # trois facettes a bords durs, comme crystal_cinnabar
                if face=='T':   c=mix(mul(c,1.30),accent,0.45)
                elif face=='F': c=mix(c,accent,0.20)
                else:           c=mul(c,0.62)
                # arete verticale de separation des deux flancs
                xs=[i for i in range(32) if g[y][i]=='#']
                if xs and abs(x-((xs[0]+xs[-1])//2))<=1 and y>=14:
                    c=mix(c,shine,0.35)
            elif fam=='block':
                if rnd.random()<0.10: c=mul(c,0.86)
            elif fam=='casing':
                # cannelures verticales regulieres le long du cylindre
                if face!='R' and x%4==1: c=mul(c,0.70)
                if 3<=x<=5:  c=mix(c,accent,0.60)                 # collerette
                if face=='R': c=mul(c, 1.0 - 0.02*(x-23))         # embout qui fuit
                if face=='T' and y==10: c=mix(c,shine,0.40)       # arete du dessus
            elif fam=='pool':
                if face=='T' and 6<=x<=15 and 15<=y<=19: c=mix(c,shine,0.55)
                if rnd.random()<0.05: c=mix(c,accent,0.7)
            elif fam=='spores':
                if rnd.random()<0.18: c=mix(c,accent,0.7)

            px[x,y]=c+(255,)

    # --- contour ---
    # Thermal Foundation n'entoure PAS ses lingots d'une ligne uniforme :
    # le volume vient des faces. Un contour ferme sur une silhouette en
    # escalier suit chaque marche et decoupe la piece en morceaux.
    if fam=='ingot':
        # Ombre portee UNIQUEMENT sur le bord exterieur bas/droit. Tester
        # solid(x+1,y) attrape aussi l'escalier interne de l'arete du
        # trapeze et noircit la moitie du dessus.
        for y in range(32):
            for x in range(32):
                if g[y][x]!='#': continue
                bottom = not solid(x,y+1)
                right  = not solid(x+1,y)
                if bottom or right:
                    # le bord haut-gauche du dessus reste clair (arete eclairee)
                    if f[y][x]=='T' and not solid(x,y-1):
                        px[x,y]=mix(px[x,y][:3],(255,255,255),0.30)+(255,)
                    else:
                        px[x,y]=mul(px[x,y][:3],0.70)+(255,)
    else:
        for y in range(32):
            for x in range(32):
                if g[y][x]!='#': continue
                if not(solid(x-1,y) and solid(x+1,y) and solid(x,y-1) and solid(x,y+1)):
                    px[x,y]=outline+(255,)

    # --- eclat le long de l'arete superieure ---
    for y in range(32):
        for x in range(32):
            if g[y][x]!='#' or f[y][x]!='T': continue
            if not solid(x,y-1) and solid(x,y+1):
                px[x,y]=shine+(255,)
            break
    return img

def main(outdir='/tmp/tex'):
    os.makedirs(outdir,exist_ok=True)
    for name,(fam,base,acc) in P.items():
        render(name,fam,base,acc).save(os.path.join(outdir,name+'.png'))
    print("%d textures generees"%len(P))

if __name__=='__main__': main(sys.argv[1] if len(sys.argv)>1 else '/tmp/tex')
