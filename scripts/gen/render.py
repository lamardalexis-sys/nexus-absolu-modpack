# -*- coding: utf-8 -*-
import sys, random, os
sys.path.insert(0,'/home/claude/gen')
from PIL import Image
import shapes
from palettes import P

def cl(v): return max(0,min(255,int(v)))
def mul(c,k): return tuple(cl(v*k) for v in c)
def mix(a,b,t): return tuple(cl(a[i]+(b[i]-a[i])*t) for i in range(3))

# multiplicateurs par face -- bords durs, pas de degrade continu
FACE = {'T':1.24, 'F':0.94, 'R':0.68}

def render(name, fam, base, accent):
    rnd=random.Random(sum(ord(c)*(i+7) for i,c in enumerate(name)))
    g,f = shapes.SHAPES[fam if fam in shapes.SHAPES else 'dust']()
    img=Image.new('RGBA',(32,32),(0,0,0,0)); px=img.load()
    outline = mul(base,0.42)
    shine   = mix(base,(255,255,255),0.62)

    def solid(x,y): return 0<=x<32 and 0<=y<32 and g[y][x]=='#'

    for y in range(32):
        for x in range(32):
            if g[y][x]!='#': continue
            face=f[y][x] if f[y][x] in FACE else 'F'
            c=mul(base,FACE[face])

            # --- details par famille (gros, lisibles a 32px) ---
            if fam=='ingot':
                if face=='T' and rnd.random()<0.10: c=mix(c,accent,0.6)
                if face=='F' and rnd.random()<0.07: c=mix(c,accent,0.45)
            elif fam=='dust':
                r=rnd.random()
                if r<0.20:   c=mix(c,accent,0.75)
                elif r<0.34: c=mul(c,0.72)
            elif fam=='capsule':
                if 12<=x<=19 and 10<=y<=25:          # fenetre du contenu
                    t=(y-10)/15.0
                    c=mix(mul(accent,1.18),mul(accent,0.55),t)
                    if x<=13: c=mix(c,(255,255,255),0.30)
                elif y<8 or y>=26: c=mul(mix(base,(150,156,166),0.5),0.88)
                else: c=mix(c,(186,192,202),0.45)
            elif fam=='catalyst':
                # 6 grosses pastilles 6x6
                inp=False
                for (px0,py0) in ((6,10),(13,10),(20,10),(6,17),(13,17),(20,17)):
                    if px0<=x<px0+6 and py0<=y<py0+6:
                        dx=x-(px0+2.5); dy=y-(py0+2.5)
                        if dx*dx+dy*dy<=7.5:
                            inp=True
                            c=mix(accent,base,0.30) if (dx<0 and dy<0) else mix(base,accent,0.45)
                if not inp: c=mul(base,0.60)
            elif fam=='gauze':
                c = mix(base,accent,0.55) if (x+y)%2==0 else mul(base,0.62)
                if x%4==0 or y%4==0: c=mul(c,0.80)
            elif fam=='crystal':
                if face=='T': c=mix(c,accent,0.55)
                elif face=='F': c=mix(c,accent,0.28)
                if 14<=x<=17 and 10<=y<=20: c=mix(c,shine,0.45)   # coeur
            elif fam=='block':
                if rnd.random()<0.10: c=mul(c,0.86)
            elif fam=='casing':
                if y in (13,14,18,19): c=mul(c,0.66)              # cannelures
                if 6<=x<=8: c=mix(c,accent,0.55)                  # bague
            elif fam=='pool':
                if face=='T' and 6<=x<=15 and 15<=y<=19: c=mix(c,shine,0.55)
                if rnd.random()<0.05: c=mix(c,accent,0.7)
            elif fam=='spores':
                if rnd.random()<0.18: c=mix(c,accent,0.7)

            px[x,y]=c+(255,)

    # --- contour fin, uniquement sur le bord exterieur ---
    for y in range(32):
        for x in range(32):
            if g[y][x]!='#': continue
            if not(solid(x-1,y) and solid(x+1,y) and solid(x,y-1) and solid(x,y+1)):
                px[x,y]=outline+(255,)

    # --- eclat en haut a gauche ---
    for y in range(32):
        for x in range(32):
            if g[y][x]=='#' and f[y][x]=='T' and solid(x,y-1) and not solid(x-1,y-1):
                px[x,y]=shine+(255,); break
    return img

def main(outdir='/tmp/tex'):
    os.makedirs(outdir,exist_ok=True)
    for name,(fam,base,acc) in P.items():
        render(name,fam,base,acc).save(os.path.join(outdir,name+'.png'))
    print("%d textures generees"%len(P))

if __name__=='__main__': main(sys.argv[1] if len(sys.argv)>1 else '/tmp/tex')
