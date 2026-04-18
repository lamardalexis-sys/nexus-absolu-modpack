"""
gui_furnace.png v7 - RF bar DEPLACEE en VERTICAL a DROITE.

v6 avait la RF bar horizontale en bas de zone machine (40,70 92x8).
v7 : la met en VERTICAL a droite, entre OUTPUT et le bord du GUI.

Nouveau layout zone machine:
 - INPUT  (40, 18) 18x18
 - FUEL   (40, 50) 18x18
 - PROGRESS (68, 27) 24x10 (flèche)
 - FLAME  (68, 55) 24x8 (indicateur fuel)
 - OUTPUT (100, 20) 26x26
 - RF BAR VERTICAL (135, 15) 14x72  <-- NOUVEAU

Inventaire inchange.
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

W, H = 176, 186
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


def draw_tube(x, y, w, h):
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
# Fleche silhouette
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
# Flamme indicator entre FUEL et progress
draw_progress_zone(68, 55, 24, 8)
flame_pts = [(73, 57), (73, 58), (74, 56), (74, 57), (74, 58), (74, 59),
             (75, 56), (75, 57), (75, 58), (75, 59), (75, 60),
             (76, 57), (76, 58), (76, 59)]
for px, py in flame_pts:
    d.point((px, py), fill=SEPARATOR)

# === NOUVELLE : BARRE RF VERTICALE A DROITE ===
# Positioned : (135, 12) 14x74 - tout a droite entre output et bord du GUI
# OUTPUT finit a x=125 (100+26=126), donc il y a (127..170) dispo = 43px
# On met la barre a x=140 (centrage dans l'espace dispo)
draw_tube(140, 12, 10, 72)

# Petite icone "RF" au-dessus de la barre (ou en dessous?)
# Ici on laisse vide dans la texture, le label sera dessine en foreground

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

# === ONGLETS (sprite sheet a x>=176) ===

def draw_tab(tx, ty, icon):
    d.rectangle([tx, ty, tx + 14, ty + 16], fill=FRAME_MID)
    d.rectangle([tx, ty, tx + 14, ty], fill=FRAME_LIGHT)
    d.rectangle([tx + 14, ty, tx + 14, ty + 16], fill=FRAME_OUT)
    d.rectangle([tx, ty + 16, tx + 14, ty + 16], fill=FRAME_OUT)

    cx = tx + 7
    cy = ty + 8
    if icon == 'config':
        d.rectangle([cx - 4, cy - 2, cx + 1, cy + 4], fill=(30, 230, 180, 255))
        top = [(cx - 4, cy - 2), (cx - 3, cy - 4), (cx + 3, cy - 4), (cx + 1, cy - 2)]
        d.polygon(top, fill=(100, 255, 200, 255))
        right = [(cx + 1, cy - 2), (cx + 3, cy - 4), (cx + 3, cy + 2), (cx + 1, cy + 4)]
        d.polygon(right, fill=(20, 180, 140, 255))
    elif icon == 'upgrades':
        for dy in range(-4, 5):
            d.point((cx, cy + dy), fill=(255, 220, 100, 255))
        for dx in range(-4, 5):
            d.point((cx + dx, cy), fill=(255, 220, 100, 255))
        for o in [-3, -2, -1, 1, 2, 3]:
            d.point((cx + o, cy + o), fill=(255, 200, 80, 255))
            d.point((cx + o, cy - o), fill=(255, 200, 80, 255))
        d.point((cx, cy), fill=(255, 255, 200, 255))


draw_tab(176, 0, 'config')
draw_tab(176, 17, 'upgrades')

canvas.save(f"{OUT}/gui_furnace.png")
print(f"[OK] gui_furnace.png v7 (RF bar verticale a droite)")
