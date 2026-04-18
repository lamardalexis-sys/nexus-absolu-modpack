"""
gui_furnace.png - Style Nexus Absolu EXACT (copie du pattern KRDA).

Palette de KRDA (analysee depuis gui_krda.png) :
 - Fond principal : violet tres sombre (~ 26, 13, 46)
 - Cadres : violet moyen (~ 58, 30, 100)
 - Slots : fond noir-violet (~ 16, 8, 32)
 - Zone tubes : meme noir que slots
 - Bordures de slots : purple pale (~ 107, 63, 160)

Structure (200x220 pour avoir room comme KRDA) :
 - Fond violet sombre plein
 - Bordure top (une ligne claire violette)
 - Zone machine (lignes 0-80) :
   * Slot INPUT (55, 16, 18x18)
   * Slot FUEL (55, 52, 18x18)
   * Tube RF vertical (139, 15, 14x74) -- 'vide' = cadre noir
   * Tube FLAMME (79, 36, 12x13) -- indicateur fuel, dans le fond
   * Zone PROGRESS horizontale (100, 36, 56x16) -- forme de fleche tracee en gris sombre
   * Slot OUTPUT large (116, 34, 26x26)
   * 4 Slots UPGRADES (173, 15/33/51/69, 18x18)
 - Ligne separation (y=82)
 - Zone inventaire (y=85+)
   * 27 slots inventaire (3x9) y=90
   * 9 slots hotbar y=150
"""
from PIL import Image, ImageDraw
import os

OUT = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui"
os.makedirs(OUT, exist_ok=True)

# === Palette Nexus (extraite de gui_krda.png analyse pixel-par-pixel) ===
BG_MAIN = (26, 13, 46, 255)       # fond violet tres sombre
FRAME_OUT = (10, 5, 20, 255)       # contour noir violet
FRAME_MID = (58, 30, 100, 255)     # cadre violet moyen (bordures)
FRAME_LIGHT = (107, 63, 160, 255)  # violet clair (highlights, borders slots)
SLOT_INSIDE = (16, 8, 32, 255)     # interieur des slots (noir-violet)
TUBE_INSIDE = (8, 4, 16, 255)      # tube RF/flamme interieur (plus noir)
SLOT_SHADOW = (74, 42, 110, 255)   # ombre legere slots
SEPARATOR = (74, 42, 110, 255)     # ligne separatrice

W, H = 176, 166
canvas = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
d = ImageDraw.Draw(canvas)


def draw_slot(x, y, size=18, light=FRAME_LIGHT, dark=FRAME_OUT, inner=SLOT_INSIDE):
    """Dessine un slot style KRDA : cadre clair + interieur sombre."""
    # Bordure externe 1px
    d.rectangle([x, y, x + size - 1, y + size - 1], outline=dark)
    # Interieur
    d.rectangle([x + 1, y + 1, x + size - 2, y + size - 2], fill=inner)
    # Top-left = clair (bevel)
    d.rectangle([x, y, x + size - 1, y], fill=light)
    d.rectangle([x, y, x, y + size - 1], fill=light)


def draw_output_slot(x, y):
    """Slot OUTPUT 26x26 avec double-bordure Mekanism-style."""
    # Cadre externe plus gros
    d.rectangle([x, y, x + 25, y + 25], fill=FRAME_LIGHT)
    d.rectangle([x + 1, y + 1, x + 24, y + 24], fill=FRAME_OUT)
    d.rectangle([x + 3, y + 3, x + 22, y + 22], fill=SLOT_INSIDE)


def draw_tube_vertical(x, y, w, h):
    """Tube vide (barre RF)."""
    # Cadre externe
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=FRAME_OUT)
    # Interieur tube
    d.rectangle([x + 1, y + 1, x + w - 2, y + h - 2], fill=TUBE_INSIDE)


def draw_progress_zone(x, y, w, h):
    """Zone de progression (fleche en silhouette sombre)."""
    # Juste une zone claire ou on ecrira drawRect violet par-dessus
    # Pour que la fleche soit visible quand vide, on dessine une silhouette sombre
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=TUBE_INSIDE)
    # Bordure
    d.rectangle([x, y, x + w - 1, y], fill=FRAME_OUT)
    d.rectangle([x, y + h - 1, x + w - 1, y + h - 1], fill=FRAME_OUT)


# ==========================================================================
# COMPOSITION DE LA TEXTURE
# ==========================================================================

# === FOND PRINCIPAL ===
d.rectangle([0, 0, W - 1, H - 1], fill=BG_MAIN)

# === CADRE EXTERIEUR (bordure) ===
# Top clair
d.rectangle([0, 0, W - 1, 0], fill=FRAME_LIGHT)
d.rectangle([0, 0, 0, H - 1], fill=FRAME_LIGHT)
# Bottom / right sombre
d.rectangle([0, H - 1, W - 1, H - 1], fill=FRAME_OUT)
d.rectangle([W - 1, 0, W - 1, H - 1], fill=FRAME_OUT)

# === ZONE MACHINE (top panel, 0-80) ===
# Cadre interne de la zone machine
d.rectangle([4, 4, W - 5, 78], fill=FRAME_MID)
d.rectangle([5, 5, W - 6, 77], fill=BG_MAIN)

# === SLOTS MACHINE ===
# INPUT (55, 16)
draw_slot(55, 16)
# FUEL (55, 52)
draw_slot(55, 52)
# TUBE FLAMME entre input et fuel (cote droit input) — mini-tube
# Position: x=79 pour etre visible a droite du slot input
draw_tube_vertical(79, 37, 12, 13)

# ZONE PROGRESS (fleche) - entre fuel indicator et output
draw_progress_zone(94, 38, 20, 10)

# Dessine une mini silhouette de fleche sombre dans la progress zone
# (sera remplie en violet par le code Java)
# Pointe vers la droite
for ix in range(8):
    d.point((96 + ix, 42), fill=SEPARATOR)
    d.point((96 + ix, 43), fill=SEPARATOR)
    d.point((96 + ix, 44), fill=SEPARATOR)
# Pointe triangulaire
arrow_tip = [(104, 40), (105, 40), (104, 41), (105, 41), (106, 41),
             (104, 45), (105, 45), (104, 46), (105, 46), (106, 46)]
for px, py in arrow_tip:
    d.point((px, py), fill=SEPARATOR)
# Pointe finale
d.point((107, 42), fill=SEPARATOR)
d.point((107, 43), fill=SEPARATOR)
d.point((107, 44), fill=SEPARATOR)

# OUTPUT 26x26 at (116, 30)
draw_output_slot(116, 30)

# TUBE RF vertical (bar energie) (152, 15, 14x70)
draw_tube_vertical(152, 15, 6, 58)

# 4 Slots UPGRADES (161, 15/33/51/69) - a droite du tube RF
for i in range(4):
    draw_slot(161, 15 + i * 18)

# === SEPARATEUR entre machine et inventaire ===
for ix in range(6, W - 6):
    d.point((ix, 80), fill=SEPARATOR)
    d.point((ix, 81), fill=FRAME_OUT)

# === ZONE INVENTAIRE (83-166) ===
d.rectangle([4, 83, W - 5, H - 5], fill=FRAME_MID)
d.rectangle([5, 84, W - 6, H - 6], fill=BG_MAIN)

# === SLOTS INVENTAIRE (27 + 9 hotbar) ===
def draw_inv_slot(x, y):
    """Slot inventaire compact (pareil mais avec bordures legerement differentes)."""
    d.rectangle([x, y, x + 17, y + 17], fill=FRAME_OUT)
    d.rectangle([x + 1, y + 1, x + 16, y + 16], fill=SLOT_INSIDE)
    # Bevel
    d.rectangle([x, y, x + 17, y], fill=FRAME_MID)
    d.rectangle([x, y, x, y + 17], fill=FRAME_MID)

# 3x9 inventaire
for row in range(3):
    for col in range(9):
        draw_inv_slot(7 + col * 18, 93 + row * 18)
# 9 hotbar
for col in range(9):
    draw_inv_slot(7 + col * 18, 151)

# === Save ===
canvas.save(f"{OUT}/gui_furnace.png")
print(f"[OK] gui_furnace.png Nexus style (usable {W}x{H}, canvas 256x256)")
