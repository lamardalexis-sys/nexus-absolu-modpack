# -*- coding: utf-8 -*-
"""Silhouettes 32x32. Chaque fonction rend (grille, carte_de_faces).
faces : 'T' dessus, 'F' face, 'R' cote droit, '.' vide."""

def blank(): return [['.']*32 for _ in range(32)]

def _mk(g):  return [''.join(r) for r in g]

# ---------------------------------------------------------------- LINGOT
def ingot():
    """Lingot trapezoidal vu de 3/4, facon Thermal Foundation.
    Trois faces avec une vraie surface a ombrer : le dessus (trapeze), la
    face avant (bande basse) et le cote droit (bande verticale). Une
    silhouette seule ne suffit pas -- il faut que chaque face existe."""
    g=blank(); f=blank()
    TOP_Y0, TOP_Y1 = 5, 16     # trapeze du dessus
    H = 7                      # hauteur des flancs
    XR = 25                    # arete verticale droite

    # Trapeze du dessus : recule vers la droite en montant.
    edges={}
    for y in range(TOP_Y0, TOP_Y1+1):
        t=(y-TOP_Y0)/float(TOP_Y1-TOP_Y0)
        x0=int(round(15-11*t))         # 15 -> 4
        x1=int(round(XR+3-3*t))        # 28 -> 25
        edges[y]=(x0,x1)
        for x in range(x0,x1+1):
            g[y][x]='#'; f[y][x]='T'

    # Face avant : sous l'arete inferieure gauche du trapeze.
    for x in range(32):
        ys=[y for y in range(32) if f[y][x]=='T']
        if not ys: continue
        if x>XR: continue                 # au-dela, c'est le cote droit
        base=max(ys)
        for k in range(1,H+1):
            y=base+k
            if y<32 and g[y][x]!='#': g[y][x]='#'; f[y][x]='F'

    # Cote droit : bande verticale sous l'arete droite, suit sa pente.
    for y in range(TOP_Y0,TOP_Y1+1):
        x1=edges[y][1]
        for x in range(min(x1+1,31), min(x1+1,31)):
            pass
    for y in range(TOP_Y0, TOP_Y1+H+1):
        # l'arete droite descend en biais : on suit son x
        yy=min(y,TOP_Y1)
        x1=edges[yy][1]
        for x in range(XR-2, x1+1):
            if 0<=x<32 and g[y][x]!='T' and (f[y][x]!='T'):
                if g[y][x]=='.' or f[y][x]=='F':
                    g[y][x]='#'; f[y][x]='R'
    return _mk(g),_mk(f)

# ---------------------------------------------------------------- POUDRE
def dust():
    """Tas de poudre : silhouette de ThermalFoundation dust_copper portee en
    32x32. TF n'eclaire pas au hasard -- un noyau clair legerement decale en
    haut a gauche, des grains groupes autour. Faces : T noyau, F bord."""
    SIL=[
    "................................",
    "................................",
    "................................",
    "................................",
    "................................",
    "................................",
    "................................",
    "................................",
    "..............####..............",
    "..............####..............",
    "............########............",
    "............########............",
    "..........############..........",
    "..........############..........",
    "........################........",
    "........################........",
    "......####################......",
    "......####################......",
    "....########################....",
    "....########################....",
    "....########################....",
    "....########################....",
    "......####################......",
    "......####################......",
    "........################........",
    "........################........",
    "............########............",
    "............########............",
    "................................",
    "................................",
    "................................",
    "................................",
    ]
    g=[list(r) for r in SIL]; f=blank()
    for y in range(32):
        for x in range(32):
            if g[y][x]!='#': continue
            dx=(x-14)/9.0; dy=(y-17)/8.0
            f[y][x]='T' if dx*dx+dy*dy<=1.0 else 'F'
    return _mk(g),_mk(f)

# --------------------------------------------------------------- CAPSULE
def capsule():
    g=blank(); f=blank()
    for y in range(4,8):                       # capuchon
        for x in range(12,20): g[y][x]='#'; f[y][x]='T'
    for y in range(8,27):                      # corps
        for x in range(10,22): g[y][x]='#'; f[y][x]='F'
    for y in range(27,29):                     # socle
        for x in range(11,21): g[y][x]='#'; f[y][x]='F'
    for (x,y) in ((10,8),(21,8),(10,26),(21,26)): g[y][x]='.'; f[y][x]='.'
    return _mk(g),_mk(f)

# ------------------------------------------------------------- CATALYSEUR
def catalyst():
    """Coupelle de catalyseur vue de 3/4 : plateau ovale incline avec un
    rebord. Les pastilles sont dessinees dans render.py. Faces : T plateau,
    F rebord avant."""
    g=blank(); f=blank()
    # plateau ovale
    for y in range(32):
        for x in range(32):
            dx=(x-16)/12.0; dy=(y-15)/7.0
            if dx*dx+dy*dy<=1.0: g[y][x]='#'; f[y][x]='T'
    # rebord avant, epaisseur du recipient
    for x in range(32):
        ys=[y for y in range(32) if g[y][x]=='#']
        if not ys: continue
        for k in range(1,5):
            y=ys[-1]+k
            if y<32: g[y][x]='#'; f[y][x]='F'
    return _mk(g),_mk(f)

def gauze():
    g=blank(); f=blank()
    for y in range(5,27):
        for x in range(5,27): g[y][x]='#'; f[y][x]='F'
    return _mk(g),_mk(f)

# --------------------------------------------------------------- CRISTAL
def crystal():
    """Gemme : silhouette de ThermalFoundation crystal_cinnabar portee en
    32x32. Trois facettes nettes -- pointe claire, flanc gauche moyen,
    flanc droit sombre."""
    SIL=[
    "................................",
    "................................",
    "................................",
    "................................",
    "................####............",
    "................####............",
    "..............########..........",
    "..............########..........",
    "............##########..........",
    "............##########..........",
    "..........##############........",
    "..........##############........",
    "........################........",
    "........################........",
    "......####################......",
    "......####################......",
    "....######################......",
    "....######################......",
    "....######################......",
    "....######################......",
    "....######################......",
    "....######################......",
    "......##################........",
    "......##################........",
    "........##############..........",
    "........##############..........",
    "..........##########............",
    "..........##########............",
    "................................",
    "................................",
    "................................",
    "................................",
    ]
    g=[list(r) for r in SIL]; f=blank()
    for y in range(32):
        xs=[x for x in range(32) if g[y][x]=='#']
        if not xs: continue
        x0,x1=xs[0],xs[-1]; m=(x0+x1)/2.0
        for x in xs:
            if y<14:        f[y][x]='T'
            elif x<m-1:     f[y][x]='F'
            else:           f[y][x]='R'
    return _mk(g),_mk(f)

# ------------------------------------------------------------------ BLOC
def block():
    """Cube isometrique : losange dessus + deux faces laterales."""
    g=blank(); f=blank()
    CX,CY,W,HT,HB = 16,11,13,7,9
    def ytop(x):
        return CY + (x-(CX-W))*HT/float(W) if x<=CX else CY + ((CX+W)-x)*HT/float(W)
    for y in range(32):
        for x in range(32):
            dx=abs(x-CX)/float(W); dy=abs(y-CY)/float(HT)
            if dx+dy<=1.0: g[y][x]='#'; f[y][x]='T'
    for x in range(CX-W,CX+W+1):
        y0=int(round(ytop(x)))
        for y in range(y0, y0+HB):
            if 0<=y<32 and g[y][x]=='.':
                g[y][x]='#'; f[y][x]='F' if x<CX else 'R'
    return _mk(g),_mk(f)

# ----------------------------------------------------------------- COQUE
def casing():
    """Coque de cartouche : cylindre couche vu de 3/4, avec un embout conique
    a droite et une collerette a gauche. Faces : T dessus du cylindre,
    F flanc, R embout."""
    g=blank(); f=blank()
    # corps cylindrique, y de 9 a 22
    for y in range(9,23):
        for x in range(5,24):
            g[y][x]='#'
            f[y][x]='T' if y<13 else 'F'
    # collerette gauche, plus haute
    for y in range(7,25):
        for x in range(3,6):
            g[y][x]='#'; f[y][x]='T' if y<13 else 'F'
    # embout conique droit
    for i in range(6):
        y0,y1=9+i,22-i
        x=24+i
        if x>29: break
        for y in range(y0,y1+1):
            g[y][x]='#'; f[y][x]='R'
    # arrondi du corps
    for (x,y) in ((5,9),(5,22),(23,9),(23,22),(3,7),(5,7),(3,24),(5,24)):
        if 0<=x<32 and 0<=y<32: g[y][x]='.'; f[y][x]='.'
    return _mk(g),_mk(f)

def pool():
    g=blank(); f=blank()
    for y in range(32):
        for x in range(32):
            dx=(x-16)/13.0; dy=(y-20)/6.5
            if dx*dx+dy*dy<=1.0: g[y][x]='#'; f[y][x]='T' if y<19 else 'F'
    return _mk(g),_mk(f)

def spores():
    g=blank(); f=blank()
    for (cx,cy,r) in ((12,13,5),(21,14,4),(11,22,4),(21,22,5),(16,18,5)):
        for y in range(32):
            for x in range(32):
                if (x-cx)**2+(y-cy)**2<=r*r:
                    g[y][x]='#'; f[y][x]='T' if y<cy else 'F'
    return _mk(g),_mk(f)

SHAPES=dict(ingot=ingot,dust=dust,capsule=capsule,catalyst=catalyst,gauze=gauze,
            crystal=crystal,block=block,casing=casing,pool=pool,spores=spores)
