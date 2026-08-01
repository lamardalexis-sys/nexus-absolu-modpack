# -*- coding: utf-8 -*-
"""Silhouettes 32x32. Chaque fonction rend (grille, carte_de_faces).
faces : 'T' dessus, 'F' face, 'R' cote droit, '.' vide."""

def blank(): return [['.']*32 for _ in range(32)]

def _mk(g):  return [''.join(r) for r in g]

# ---------------------------------------------------------------- LINGOT
def ingot():
    """Lingot Thermal Foundation, releve au pixel puis porte en 32x32.

    La carte ci-dessous EST celle de ingot_copper, classee en 4 niveaux :
      O = contour et ombre profonde (luminance ~69 sur 219)
      C = face eclairee            (~195)
      m = ton moyen                (~125)
      d = face sombre              (~95)

    Ce que je n'avais pas compris avant : chez TF le volume ne vient pas
    d'un dessus + une face avant, mais d'un DEGRADE EN BANDES DIAGONALES
    qui traverse tout le lingot du coin haut-gauche au coin bas-droit. Et
    le contour est ferme, 1px, sur tout le pourtour -- y compris en haut.
    """
    MAP=[
    "................................",
    "................................",
    "................................",
    "................................",
    "....................OOOO........",
    "....................OOOO........",
    "..............OOOOOOmmmmOO......",
    "..............OOOOOOmmmmOO......",
    "........OOOOOOmmCCCCCCCCmmOO....",
    "........OOOOOOmmCCCCCCCCmmOO....",
    "..OOOOOOmmCCCCCCCCCCCCCCCCmmOO..",
    "..OOOOOOmmCCCCCCCCCCCCCCCCmmOO..",
    "OOmmCCCCCCCCCCCCCCCCCCddddddddOO",
    "OOmmCCCCCCCCCCCCCCCCCCddddddddOO",
    "OOCCmmCCCCCCCCCCddddddmmmmmmmmOO",
    "OOCCmmCCCCCCCCCCddddddmmmmmmmmOO",
    "OOCCCCmmCCddddddmmmmmmmmmmddddOO",
    "OOCCCCmmCCddddddmmmmmmmmmmddddOO",
    "OOCCCCmmmmmmmmmmmmmmddddddddddOO",
    "OOCCCCmmmmmmmmmmmmmmddddddddddOO",
    "OOmmmmmmmmmmmmmmddddddddOOOOOO..",
    "OOmmmmmmmmmmmmmmddddddddOOOOOO..",
    "..OOmmmmmmddddddddOOOOOO........",
    "..OOmmmmmmddddddddOOOOOO........",
    "....OOmmmmddOOOOOO..............",
    "....OOmmmmddOOOOOO..............",
    "......OOOOOO....................",
    "......OOOOOO....................",
    "................................",
    "................................",
    "................................",
    "................................",
    ]
    g=blank(); f=blank()
    for y,row in enumerate(MAP):
        for x,ch in enumerate(row):
            if ch=='.': continue
            g[y][x]='#'
            f[y][x]=ch          # O / C / m / d : le niveau est la face
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
