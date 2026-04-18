"""
Genere la texture GUI pour les Furnaces Nexus, style cohérent avec
les autres machines Nexus Absolu (violet sombre, inspiré de Mekanism).

Layout (xSize=176, ySize=166):
  - Titre en haut
  - Slot INPUT (56, 17)
  - Slot FUEL (56, 53)
  - Barre progress horizontale 79-103 x 34-50 (fleche qui avance)
  - Flamme verticale 57, 37 (indicateur fuel)
  - Slot OUTPUT (116, 35)
  - Barre RF verticale 134, 17 -> 134, 89
  - 4 Slots upgrades verticaux (152, 17-71)
  - Inventaire joueur (27 + 9) en bas
"""

from PIL import Image, ImageDraw

# === Palette Nexus Absolu (copié de gui_diarh33.png analysé) ===
BG_DARK = (26, 16, 48, 255)        # fond principal violet très sombre
BG_MID = (42, 26, 74, 255)          # violet moyen (cadres)
BG_LIGHT = (58, 31, 94, 255)        # violet un peu plus clair
SLOT_BG = (14, 8, 24, 255)          # noir-violet pour fond de slot
SLOT_FRAME = (107, 63, 160, 255)    # violet des bordures de slot
SLOT_FRAME_DARK = (74, 42, 110, 255)
INV_SLOT_FRAME = (74, 42, 110, 255) # frame slots inventaire
TEXT_PURPLE = (221, 136, 255, 255)
BORDER_DARK = (10, 6, 18, 255)

W, H = 176, 166
# Canvas 256x256 (Minecraft scale standard pour GUI textures)
img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

# === FOND PRINCIPAL 0,0 -> W,H ===
d.rectangle([0, 0, W-1, H-1], fill=BG_DARK, outline=BORDER_DARK)

# === ZONE MACHINE (haut, 0..0 -> W, 75) ===
# Fond violet un poil plus clair sous la zone machine
d.rectangle([4, 4, W-5, 76], fill=BG_MID, outline=SLOT_FRAME_DARK)
d.rectangle([5, 5, W-6, 75], fill=BG_DARK)

# === SEPARATION machine / inventaire (ligne horizontale) ===
d.rectangle([4, 78, W-5, 80], fill=SLOT_FRAME_DARK)

# === ZONE INVENTAIRE (bas, 82 -> H) ===
d.rectangle([4, 82, W-5, H-5], fill=BG_MID, outline=SLOT_FRAME_DARK)
d.rectangle([5, 83, W-6, H-6], fill=BG_DARK)


def draw_slot_frame(draw, x, y, w=18, h=18, fc=SLOT_FRAME, bg=SLOT_BG):
    """Dessine un slot encadré (Mekanism-style)."""
    # Bordure externe
    draw.rectangle([x, y, x + w - 1, y + h - 1], fill=fc, outline=BORDER_DARK)
    # Fond interne
    draw.rectangle([x + 1, y + 1, x + w - 2, y + h - 2], fill=bg)


# === SLOT INPUT (55, 16) ===
draw_slot_frame(d, 55, 16)

# === SLOT FUEL (55, 52) ===
draw_slot_frame(d, 55, 52)

# === FLAMME fuel (verticale, 77, 37 - 13 de hauteur) ===
# Fond du "tube" de flamme
d.rectangle([78, 37, 91, 50], fill=SLOT_FRAME_DARK)
d.rectangle([79, 38, 90, 49], fill=SLOT_BG)

# === PROGRESS BAR cavity (horizontale, 79 -> 103, 34 -> 51) ===
# Simple rectangle vide que le code fillera dynamiquement
d.rectangle([93, 34, 115, 50], fill=SLOT_FRAME_DARK)
d.rectangle([94, 35, 114, 49], fill=SLOT_BG)

# === SLOT OUTPUT (115, 34) — plus grand, double-bordure Mekanism-style ===
# External frame
d.rectangle([114, 33, 134, 53], fill=SLOT_FRAME, outline=BORDER_DARK)
# Inner frame (double-bordered)
d.rectangle([115, 34, 133, 52], fill=SLOT_FRAME_DARK)
# Slot interior
d.rectangle([116, 35, 132, 51], fill=SLOT_BG)

# === RF BAR (verticale, 140 -> 148, 16 -> 70) ===
d.rectangle([139, 16, 149, 72], fill=SLOT_FRAME_DARK)
d.rectangle([140, 17, 148, 71], fill=SLOT_BG)

# === 4 SLOTS UPGRADES (à droite, verticaux, 151, 16-70) ===
for i in range(4):
    y = 16 + i * 18
    draw_slot_frame(d, 151, y)

# === INVENTAIRE JOUEUR (27 slots + 9 hotbar) ===
# 3 rangées x 9 cols, position (8, 94)
for row in range(3):
    for col in range(9):
        sx = 7 + col * 18
        sy = 93 + row * 18
        draw_slot_frame(d, sx, sy, fc=INV_SLOT_FRAME)

# Hotbar
for col in range(9):
    sx = 7 + col * 18
    sy = 151
    draw_slot_frame(d, sx, sy, fc=INV_SLOT_FRAME)

# === Détails déco : petites barres violettes décoratives ===
# Ligne séparatrice inventaire
d.rectangle([7, 80, W-8, 82], fill=SLOT_FRAME)

# === ICONES STATUS ===
# Petit chevron ">" vers flamme-progress
d.polygon([(91, 42), (94, 42), (94, 45)], fill=TEXT_PURPLE)
d.polygon([(91, 45), (94, 45), (94, 42)], fill=TEXT_PURPLE)

# Petit chevron ">" progress -> output
d.polygon([(115, 42), (118, 42), (118, 45)], fill=TEXT_PURPLE)
d.polygon([(115, 45), (118, 45), (118, 42)], fill=TEXT_PURPLE)

# Petite flamme icone au-dessus slot fuel (decoration)
# Position 60,48 (petite taille)
fp = [(61, 50), (63, 47), (65, 48), (66, 50)]
d.polygon(fp, fill=(200, 100, 40, 255))

img.save('mod-source/src/main/resources/assets/nexusabsolu/textures/gui/gui_furnace.png')
print(f"Generated gui_furnace.png (256x256, usable area {W}x{H})")
