"""
gui_furnace.png v6 - refonte complete suite feedback Alexis v1.0.188 :

Problemes v1.0.188:
 * Inventaire deborde en bas (ySize=166 trop petit pour l'hotbar)
 * Couleurs config panel trop pales
 * Upgrades + config panel tous les deux a droite, faut les separer
 * Upgrades doivent etre dans leur propre panneau

Solution v6 :
 - ySize = 186 (20px de plus, tout rentre propre)
 - Zone machine reorganisee : NO upgrades 2x2, juste IN/OUT/progress/RF
 - 2 onglets lateraux :
   * GAUCHE (x=-15, y=18) : Config I/O (icone cube colore)
   * DROITE (x=xSize-2, y=18) : Upgrades (icone *)
 - Panneaux separes : Config a gauche, Upgrades a droite

Layout main GUI (176x186) :
   [TITRE centre]                                     
   [IN]          >> [PROGRESS] >>   [OUTPUT]       
                                                     
   [FUEL]                           [RF bar vert]    
                                                     
   -------- separateur --------                      
   Inventaire                                        
   [9x3 slots]                                       
   [9 hotbar]                                        

Onglets (depassent lateralement) :
   [C] sur GAUCHE a y+18 (15x17, sprite (176, 0))
   [U] sur DROITE a y+18 (15x17, sprite (176, 17))
"""
from PIL import Image, ImageDraw
import os

OUT = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui"
os.makedirs(OUT, exist_ok=True)

BG_MAIN = (26, 13, 46, 255)
FRAME_OUT = (10, 5, 20, 255)
FRAME_MID = (58, 30, 100, 255)
FRAME_LIGHT = (107, 63, 160, 255)
SLOT_INSIDE = (16, 8, 32, 255)
TUBE_INSIDE = (8, 4, 16, 255)
SEPARATOR = (74, 42, 110, 255)

W, H = 176, 186   # Nouvelle hauteur 186
canvas = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
d = ImageDraw.Draw(canvas)


def draw_slot(x, y, size=18):
    d.rectangle([x, y, x + size - 1, y + size - 1], outline=FRAME_OUT)
    d.rectangle([x + 1, y + 1, x + size - 2, y + size - 2], fill=SLOT_INSIDE)
    d.rectangle([x, y, x + size - 1, y], fill=FRAME_LIGHT)
    d.rectangle([x, y, x, y + size - 1], fill=FRAME_LIGHT)


def draw_output_slot(x, y):
    d.rectangle([x, y, x + 25, y + 25], fill=FRAME_LIGHT)
    d.rectangle([x + 1, y + 1, x + 24, y + 24], fill=FRAME_OUT)
    d.rectangle([x + 3, y + 3, x + 22, y + 22], fill=SLOT_INSIDE)


def draw_tube_horizontal(x, y, w, h):
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=FRAME_OUT)
    d.rectangle([x + 1, y + 1, x + w - 2, y + h - 2], fill=TUBE_INSIDE)


def draw_progress_zone(x, y, w, h):
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=TUBE_INSIDE)
    d.rectangle([x, y, x + w - 1, y], fill=FRAME_OUT)
    d.rectangle([x, y + h - 1, x + w - 1, y + h - 1], fill=FRAME_OUT)


def draw_inv_slot(x, y):
    d.rectangle([x, y, x + 17, y + 17], fill=FRAME_OUT)
    d.rectangle([x + 1, y + 1, x + 16, y + 16], fill=SLOT_INSIDE)
    d.rectangle([x, y, x + 17, y], fill=FRAME_MID)
    d.rectangle([x, y, x, y + 17], fill=FRAME_MID)


# === FOND PRINCIPAL ===
d.rectangle([0, 0, W - 1, H - 1], fill=BG_MAIN)
# Bordures
d.rectangle([0, 0, W - 1, 0], fill=FRAME_LIGHT)
d.rectangle([0, 0, 0, H - 1], fill=FRAME_LIGHT)
d.rectangle([0, H - 1, W - 1, H - 1], fill=FRAME_OUT)
d.rectangle([W - 1, 0, W - 1, H - 1], fill=FRAME_OUT)

# === ZONE MACHINE (y=4 a y=88) ===
d.rectangle([4, 4, W - 5, 88], fill=FRAME_MID)
d.rectangle([5, 5, W - 6, 87], fill=BG_MAIN)

# === SLOTS MACHINE ===
# INPUT centre-gauche
draw_slot(40, 18)
# PROGRESS zone au milieu-haut
draw_progress_zone(68, 27, 24, 10)
# Fleche silhouette dans la progress zone
for ix in range(12):
    d.point((70 + ix, 31), fill=SEPARATOR)
    d.point((70 + ix, 32), fill=SEPARATOR)
    d.point((70 + ix, 33), fill=SEPARATOR)
arrow_tip = [(82, 29), (83, 29), (82, 30), (83, 30), (84, 30),
             (82, 34), (83, 34), (82, 35), (83, 35), (84, 35)]
for px, py in arrow_tip:
    d.point((px, py), fill=SEPARATOR)
d.point((85, 31), fill=SEPARATOR)
d.point((85, 32), fill=SEPARATOR)
d.point((85, 33), fill=SEPARATOR)

# OUTPUT_LARGE a droite
draw_output_slot(100, 20)

# FUEL slot sous INPUT
draw_slot(40, 50)
# Flamme silhouette entre FUEL et RF bar
draw_progress_zone(68, 55, 24, 8)
flame_pts = [(73, 57), (73, 58), (74, 56), (74, 57), (74, 58), (74, 59),
             (75, 56), (75, 57), (75, 58), (75, 59), (75, 60),
             (76, 57), (76, 58), (76, 59)]
for px, py in flame_pts:
    d.point((px, py), fill=SEPARATOR)

# RF bar horizontale en bas de zone machine
# Couvrant (40, 70) a (132, 78) = 92x8
draw_tube_horizontal(40, 70, 92, 8)

# === SEPARATEUR machine/inventaire ===
for ix in range(6, W - 6):
    d.point((ix, 90), fill=SEPARATOR)
    d.point((ix, 91), fill=FRAME_OUT)

# === ZONE INVENTAIRE (y=93 a fin) ===
d.rectangle([4, 93, W - 5, H - 5], fill=FRAME_MID)
d.rectangle([5, 94, W - 6, H - 6], fill=BG_MAIN)

# 3x9 inventaire (y=103, 121, 139)
for row in range(3):
    for col in range(9):
        draw_inv_slot(7 + col * 18, 103 + row * 18)
# Hotbar (y=161)
for col in range(9):
    draw_inv_slot(7 + col * 18, 161)

# === ONGLETS ===
# Onglet CONFIG (sprite a 176, 0, 15x17) - dessine a (GAUCHE : x=-15)
# Onglet UPGRADES (sprite a 176, 17, 15x17) - dessine a (DROITE : x=xSize-2)

def draw_tab(tx, ty, icon):
    """Dessine un onglet 15x17 dans le sprite sheet a position (tx, ty)."""
    # Corps violet
    d.rectangle([tx, ty, tx + 14, ty + 16], fill=FRAME_MID)
    # Bords
    d.rectangle([tx, ty, tx + 14, ty], fill=FRAME_LIGHT)
    d.rectangle([tx + 14, ty, tx + 14, ty + 16], fill=FRAME_OUT)
    d.rectangle([tx, ty + 16, tx + 14, ty + 16], fill=FRAME_OUT)

    cx = tx + 7
    cy = ty + 8
    if icon == 'config':
        # Cube 3D avec couleurs saturees
        # Face avant (turquoise)
        d.rectangle([cx - 4, cy - 2, cx + 1, cy + 4], fill=(30, 230, 180, 255))
        # Face haut (parallelogramme)
        top = [(cx - 4, cy - 2), (cx - 3, cy - 4), (cx + 3, cy - 4), (cx + 1, cy - 2)]
        d.polygon(top, fill=(100, 255, 200, 255))
        # Face droite
        right = [(cx + 1, cy - 2), (cx + 3, cy - 4), (cx + 3, cy + 2), (cx + 1, cy + 4)]
        d.polygon(right, fill=(20, 180, 140, 255))
    elif icon == 'upgrades':
        # Etoile brillante
        # Croix verticale + horizontale
        for dy in range(-4, 5):
            d.point((cx, cy + dy), fill=(255, 220, 100, 255))
        for dx in range(-4, 5):
            d.point((cx + dx, cy), fill=(255, 220, 100, 255))
        # Diagonales
        for o in [-3, -2, -1, 1, 2, 3]:
            d.point((cx + o, cy + o), fill=(255, 200, 80, 255))
            d.point((cx + o, cy - o), fill=(255, 200, 80, 255))
        # Centre brillant
        d.point((cx, cy), fill=(255, 255, 200, 255))


# Onglet Config (icone cube 3D colore) a (176, 0)
draw_tab(176, 0, 'config')
# Onglet Upgrades (icone etoile) a (176, 17)
draw_tab(176, 17, 'upgrades')

canvas.save(f"{OUT}/gui_furnace.png")
print(f"[OK] gui_furnace.png v6 ({W}x{H}, no upgrades in main, 2 tabs)")
