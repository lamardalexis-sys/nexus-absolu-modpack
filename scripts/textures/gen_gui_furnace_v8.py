"""
gui_furnace.png v8 - meilleur contraste RF bar (tube VIDE plus noir).

v7 : tube couleur TUBE_INSIDE (8,4,16) - trop pale
v8 : tube couleur pure noir (0,0,0) - aucune confusion quand vide
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
TUBE_INSIDE_DARK = (0, 0, 0, 255)    # NOIR PUR pour bien distinguer "vide"
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
    """Tube VIDE - fond noir pur pour bien distinguer du remplissage."""
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=FRAME_OUT)
    d.rectangle([x + 1, y + 1, x + w - 2, y + h - 2], fill=TUBE_INSIDE_DARK)


def draw_progress_zone(x, y, w, h):
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=TUBE_INSIDE_DARK)
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
draw_slot(40, 18)   # INPUT
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

draw_output_slot(100, 20)
draw_slot(40, 50)  # FUEL

# Flamme indicator
draw_progress_zone(68, 55, 24, 8)
flame_pts = [(73, 57), (73, 58), (74, 56), (74, 57), (74, 58), (74, 59),
             (75, 56), (75, 57), (75, 58), (75, 59), (75, 60),
             (76, 57), (76, 58), (76, 59)]
for px, py in flame_pts:
    d.point((px, py), fill=SEPARATOR)

# BARRE RF VERTICALE a droite (NOIR pur a l'interieur)
draw_tube(140, 12, 10, 72)

# SEPARATEUR
for ix in range(6, W - 6):
    d.point((ix, 90), fill=SEPARATOR)
    d.point((ix, 91), fill=FRAME_OUT)

# ZONE INVENTAIRE
d.rectangle([4, 93, W - 5, H - 5], fill=FRAME_MID)
d.rectangle([5, 94, W - 6, H - 6], fill=BG_MAIN)
for row in range(3):
    for col in range(9):
        draw_inv_slot(7 + col * 18, 103 + row * 18)
for col in range(9):
    draw_inv_slot(7 + col * 18, 161)

# ONGLETS
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
print(f"[OK] gui_furnace.png v8 (tube RF VIDE en noir pur)")
