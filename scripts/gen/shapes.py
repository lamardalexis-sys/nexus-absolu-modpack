# -*- coding: utf-8 -*-
"""Silhouettes 32x32. Chaque fonction rend (grille, carte_de_faces).
faces : 'T' dessus, 'F' face, 'R' cote droit, '.' vide."""

def blank(): return [['.']*32 for _ in range(32)]

def _mk(g):  return [''.join(r) for r in g]

# ---------------------------------------------------------------- LINGOT
def ingot():
    """Parallelepipede 3/4 : dessus, face, cote droit. Bords durs."""
    g=blank(); f=blank()
    # dessus : losange aplati
    for y in range(6,14):
        t=(y-6)/7.0
        x0=int(6+10*t); x1=int(19+6*t)
        for x in range(x0,x1+1): g[y][x]='#'; f[y][x]='T'
    # face avant
    for y in range(14,25):
        x0=16-int(10*(1-(y-14)/11.0))-0
        for x in range(6,20): g[y][x]='#'; f[y][x]='F'
    # cote droit
    for y in range(14,23):
        for x in range(20,26-(y-14)//3):
            g[y][x]='#'; f[y][x]='R'
    return _mk(g),_mk(f)

# ---------------------------------------------------------------- POUDRE
def dust():
    g=blank(); f=blank()
    for y in range(32):
        for x in range(32):
            dx=(x-16)/12.5; dy=(y-20)/8.5
            if dx*dx+dy*dy<=1.0 and y<=27:
                g[y][x]='#'; f[y][x]='T' if y<17 else 'F'
    for x in range(7,25): g[27][x]='#'; f[27][x]='F'
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
    """Coupelle avec 6 grosses pastilles."""
    g=blank(); f=blank()
    for y in range(9,25):
        for x in range(4,28): g[y][x]='#'; f[y][x]='F'
    for x in range(6,26): g[8][x]='#'; f[8][x]='T'
    for (x,y) in ((4,9),(27,9),(4,24),(27,24)): g[y][x]='.'; f[y][x]='.'
    return _mk(g),_mk(f)

def gauze():
    g=blank(); f=blank()
    for y in range(5,27):
        for x in range(5,27): g[y][x]='#'; f[y][x]='F'
    return _mk(g),_mk(f)

# --------------------------------------------------------------- CRISTAL
def crystal():
    """Gemme a facettes nettes : pointe haute, corps hexagonal."""
    g=blank(); f=blank()
    seg=[(4,15,16),(5,14,17),(6,13,18),(7,12,19),(8,11,20),(9,10,21),
         (10,9,22),(11,9,22),(12,8,23),(13,8,23),(14,8,23),(15,8,23),
         (16,8,23),(17,9,22),(18,9,22),(19,10,21),(20,11,20),(21,12,19),
         (22,13,18),(23,14,17),(24,15,16)]
    for (y,x0,x1) in seg:
        for x in range(x0,x1+1):
            g[y][x]='#'
            # facette gauche / centre / droite
            m=(x0+x1)/2.0
            f[y][x]='T' if x<m-2 else ('R' if x>m+2 else 'F')
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
    g=blank(); f=blank()
    for y in range(10,22):
        for x in range(4,28): g[y][x]='#'; f[y][x]='F'
    for x in range(6,26): g[10][x]='#'; f[10][x]='T'
    for (x,y) in ((4,10),(4,21),(27,10),(27,21)): g[y][x]='.'; f[y][x]='.'
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
