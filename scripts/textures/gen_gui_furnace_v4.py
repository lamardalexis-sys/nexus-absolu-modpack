"""
gui_furnace.png v4 - ajoute 2 onglets en haut du GUI (Config I/O + Upgrades).

Layout dans le PNG 256x256:
 - Main GUI principal (0, 0, 176, 166) - identique v1.0.184
 - 2 Onglets en haut (qui depassent au-dessus du GUI):
   * Onglet Main (actif par defaut) : icone fourneau, x=6,   y=-24, 30x25
   * Onglet Config I/O              : icone face,    x=40,  y=-24, 30x25
   * Onglet Upgrades                : icone *,       x=74,  y=-24, 30x25
 - Les onglets sont dessines dans le PNG a (176, 0) = area libre
   et le code les blitte au-dessus du GUI

Onglets dimensions: 30x28 chaque, layout dans sprite sheet:
 - inactive (x=176, y=0, 30x28) : onglet "plie" (un peu en retrait)
 - active   (x=176, y=28, 30x28) : onglet "sorti" (surlignage)

Contenu des onglets (icone simple dessinee):
 - Icone FURNACE : petit rectangle noir + flamme orange
 - Icone CONFIG  : mini cube avec faces
 - Icone UPGRADE : asterisque ou fleche up
"""
from PIL import Image, ImageDraw
import os

OUT = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui"
os.makedirs(OUT, exist_ok=True)

# === Palette (identique v3 pour coherence) ===
BG_MAIN = (26, 13, 46, 255)
FRAME_OUT = (10, 5, 20, 255)
FRAME_MID = (58, 30, 100, 255)
FRAME_LIGHT = (107, 63, 160, 255)
SLOT_INSIDE = (16, 8, 32, 255)
TUBE_INSIDE = (8, 4, 16, 255)
SEPARATOR = (74, 42, 110, 255)

# Tab colors
TAB_INACTIVE_FILL = (40, 20, 70, 255)    # plus sombre (retrait)
TAB_ACTIVE_FILL = BG_MAIN                # meme couleur que le fond = "continu"
TAB_HIGHLIGHT = (140, 80, 200, 255)

W, H = 176, 166
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


def draw_tube_vertical(x, y, w, h):
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


# ==========================================================================
# MAIN PANEL (identique v1.0.184)
# ==========================================================================
d.rectangle([0, 0, W - 1, H - 1], fill=BG_MAIN)

# Cadre exterieur
d.rectangle([0, 0, W - 1, 0], fill=FRAME_LIGHT)
d.rectangle([0, 0, 0, H - 1], fill=FRAME_LIGHT)
d.rectangle([0, H - 1, W - 1, H - 1], fill=FRAME_OUT)
d.rectangle([W - 1, 0, W - 1, H - 1], fill=FRAME_OUT)

# Zone machine
d.rectangle([4, 4, W - 5, 78], fill=FRAME_MID)
d.rectangle([5, 5, W - 6, 77], fill=BG_MAIN)

# Slots
draw_slot(55, 16)   # INPUT
draw_slot(55, 52)   # FUEL
draw_tube_vertical(79, 37, 12, 13)
draw_progress_zone(94, 38, 20, 10)

# Fleche silhouette
for ix in range(8):
    d.point((96 + ix, 42), fill=SEPARATOR)
    d.point((96 + ix, 43), fill=SEPARATOR)
    d.point((96 + ix, 44), fill=SEPARATOR)
arrow_tip = [(104, 40), (105, 40), (104, 41), (105, 41), (106, 41),
             (104, 45), (105, 45), (104, 46), (105, 46), (106, 46)]
for px, py in arrow_tip:
    d.point((px, py), fill=SEPARATOR)
d.point((107, 42), fill=SEPARATOR)
d.point((107, 43), fill=SEPARATOR)
d.point((107, 44), fill=SEPARATOR)

draw_output_slot(116, 30)  # OUTPUT 26x26
draw_tube_vertical(152, 15, 6, 58)  # RF tube

# 4 upgrade slots
for i in range(4):
    draw_slot(161, 15 + i * 18)

# Separateur
for ix in range(6, W - 6):
    d.point((ix, 80), fill=SEPARATOR)
    d.point((ix, 81), fill=FRAME_OUT)

# Zone inventaire
d.rectangle([4, 83, W - 5, H - 5], fill=FRAME_MID)
d.rectangle([5, 84, W - 6, H - 6], fill=BG_MAIN)

for row in range(3):
    for col in range(9):
        draw_inv_slot(7 + col * 18, 93 + row * 18)
for col in range(9):
    draw_inv_slot(7 + col * 18, 151)


# ==========================================================================
# ONGLETS en haut - sprite sheet a (W, 0)
# Layout: 2 etats x 3 types = 6 sprites 30x28
# Positions dans le PNG :
#  (176, 0) inactive tabs row (3 icones)
#  (176, 28) active tabs row (3 icones)
# ==========================================================================

def draw_tab(x, y, active, icon_type):
    """
    Dessine un onglet 30x28.
    icon_type : 'main' (furnace), 'config' (face cube), 'upgrade' (star)
    """
    fill = TAB_ACTIVE_FILL if active else TAB_INACTIVE_FILL

    # Forme de l'onglet : rectangle arrondi stylise, avec un decalage si inactive
    y_offset = 0 if active else 3  # inactive = plus bas (pas sorti)
    w, h = 30, 25

    # Corps
    d.rectangle([x, y + y_offset, x + w - 1, y + y_offset + h - 1], fill=fill)

    # Bord superieur et gauche clair
    d.rectangle([x, y + y_offset, x + w - 1, y + y_offset], fill=FRAME_LIGHT)
    d.rectangle([x, y + y_offset, x, y + y_offset + h - 1], fill=FRAME_LIGHT)

    # Bord droit sombre
    d.rectangle([x + w - 1, y + y_offset, x + w - 1, y + y_offset + h - 1], fill=FRAME_OUT)

    # Bord bas selon etat:
    # - active : pas de bord bas (fusionne avec GUI)
    # - inactive : bord bas sombre
    if not active:
        d.rectangle([x, y + y_offset + h - 1, x + w - 1, y + y_offset + h - 1], fill=FRAME_OUT)
    else:
        # Accent clair au bord haut pour montrer que c'est actif
        d.rectangle([x + 2, y + y_offset + 1, x + w - 3, y + y_offset + 1], fill=TAB_HIGHLIGHT)

    # Icone au centre
    icon_x = x + w // 2
    icon_y = y + y_offset + h // 2

    if icon_type == 'main':
        # Mini fourneau : rectangle noir + petite flamme orange
        d.rectangle([icon_x - 6, icon_y - 5, icon_x + 5, icon_y + 5], fill=FRAME_OUT)
        d.rectangle([icon_x - 4, icon_y - 3, icon_x + 3, icon_y + 3], fill=(20, 10, 8, 255))
        # Flamme
        flame = [(icon_x - 2, icon_y + 2), (icon_x - 1, icon_y + 2), (icon_x, icon_y + 2),
                 (icon_x - 1, icon_y + 1), (icon_x, icon_y), (icon_x - 1, icon_y - 1)]
        for px, py in flame:
            d.point((px, py), fill=(220, 100, 20, 255))

    elif icon_type == 'config':
        # Cube 3D stylise : 3 faces visibles
        # Face avant (rectangle)
        d.rectangle([icon_x - 5, icon_y - 3, icon_x + 1, icon_y + 4], fill=(50, 180, 100, 255))
        d.rectangle([icon_x - 5, icon_y - 3, icon_x + 1, icon_y - 3], fill=(100, 240, 150, 255))
        # Face dessus (parallelogramme stylise)
        top_pts = [(icon_x - 5, icon_y - 3), (icon_x - 3, icon_y - 6), (icon_x + 3, icon_y - 6), (icon_x + 1, icon_y - 3)]
        d.polygon(top_pts, fill=(80, 220, 130, 255))
        # Face droite
        right_pts = [(icon_x + 1, icon_y - 3), (icon_x + 3, icon_y - 6), (icon_x + 3, icon_y + 1), (icon_x + 1, icon_y + 4)]
        d.polygon(right_pts, fill=(30, 140, 70, 255))

    elif icon_type == 'upgrade':
        # Asterisque/etoile
        # Barre verticale
        d.rectangle([icon_x - 1, icon_y - 5, icon_x, icon_y + 5], fill=TAB_HIGHLIGHT)
        # Barre horizontale
        d.rectangle([icon_x - 5, icon_y - 1, icon_x + 5, icon_y], fill=TAB_HIGHLIGHT)
        # Barres diagonales
        for i in range(-3, 4):
            d.point((icon_x + i, icon_y + i), fill=TAB_HIGHLIGHT)
            d.point((icon_x + i, icon_y - i), fill=TAB_HIGHLIGHT)


# Dessine les 6 sprites d'onglets dans la zone a droite du GUI principal
# Row 0 (y=0) = inactive
# Row 1 (y=28) = active
draw_tab(176, 0,  False, 'main')     # main inactive
draw_tab(176, 28, True,  'main')     # main active
draw_tab(206, 0,  False, 'config')   # config inactive
draw_tab(206, 28, True,  'config')   # config active
# (upgrade sera en dessous, mais pas assez de place horizontal - on le met a x=176, y=56)
draw_tab(176, 56, False, 'upgrade')  # upgrade inactive
draw_tab(206, 56, True,  'upgrade')  # upgrade active

# Save
canvas.save(f"{OUT}/gui_furnace.png")
print(f"[OK] gui_furnace.png v4 - with 3 tabs sprites")
