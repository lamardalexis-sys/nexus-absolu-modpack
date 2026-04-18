"""
Textures items upgrades (16x16) style Nexus Absolu.

4 items :
 - upgrade_rf_converter  : core rouge avec eclair jaune (RF)
 - upgrade_io_expansion  : core bleu avec fleche bi-directionnelle
 - upgrade_speed_booster : core orange avec chevrons >>
 - upgrade_efficiency    : core vert avec symbole leaf/sparkle

Base commune : carte / jeton avec bordure violette (signature Nexus),
fond sombre, et symbole colore au centre.
"""
from PIL import Image, ImageDraw
import os

OUT = "mod-source/src/main/resources/assets/nexusabsolu/textures/items"
os.makedirs(OUT, exist_ok=True)

# Palette commune
BG_DARK = (20, 10, 35, 255)
BG_MID = (40, 20, 70, 255)
BORDER = (107, 63, 160, 255)       # violet Nexus
BORDER_LIGHT = (160, 100, 220, 255)

# Accents par type
PALETTES = {
    "rf_converter":  {
        "core":    (200, 50, 50, 255),
        "bright":  (255, 200, 60, 255),
        "extra":   (255, 255, 120, 255),
    },
    "io_expansion": {
        "core":    (50, 120, 200, 255),
        "bright":  (80, 180, 255, 255),
        "extra":   (180, 220, 255, 255),
    },
    "speed_booster": {
        "core":    (220, 100, 20, 255),
        "bright":  (255, 180, 60, 255),
        "extra":   (255, 230, 140, 255),
    },
    "efficiency": {
        "core":    (40, 180, 80, 255),
        "bright":  (100, 240, 140, 255),
        "extra":   (200, 255, 220, 255),
    },
}


def draw_card_base(d):
    """Fond de carte violet : jeton 16x16 avec bordure."""
    # Bordure exterieure
    d.rectangle([0, 0, 15, 15], outline=BORDER)
    # Coins arrondis (vire les 4 pixels d'angle)
    d.rectangle([0, 0, 0, 0], fill=(0, 0, 0, 0))
    d.rectangle([15, 0, 15, 0], fill=(0, 0, 0, 0))
    d.rectangle([0, 15, 0, 15], fill=(0, 0, 0, 0))
    d.rectangle([15, 15, 15, 15], fill=(0, 0, 0, 0))
    # Bordure interne highlight
    for i in range(1, 15):
        if i == 1 or i == 14:
            d.point((i, 1), fill=BORDER_LIGHT)
            d.point((1, i), fill=BORDER_LIGHT)
    # Fond
    d.rectangle([2, 2, 13, 13], fill=BG_DARK)
    # Gradient subtil (haut plus clair)
    d.rectangle([2, 2, 13, 4], fill=BG_MID)


def draw_rf_icon(d, p):
    """Eclair jaune sur cercle rouge."""
    # Cercle rouge
    for y in range(4, 12):
        for x in range(4, 12):
            dx, dy = x - 7, y - 7
            if dx*dx + dy*dy <= 14:
                d.point((x, y), fill=p["core"])
    # Eclair (zigzag)
    lightning = [
        (8, 4), (7, 5), (8, 5), (7, 6), (8, 6),
        (6, 7), (7, 7), (8, 7),
        (7, 8), (8, 8), (9, 8),
        (8, 9), (9, 9),
        (8, 10), (9, 10),
    ]
    for px, py in lightning:
        d.point((px, py), fill=p["bright"])
    # Glow
    d.point((8, 5), fill=p["extra"])
    d.point((8, 9), fill=p["extra"])


def draw_io_icon(d, p):
    """Double fleche bleu (bidirectionnelle)."""
    # Cercle bleu
    for y in range(4, 12):
        for x in range(4, 12):
            dx, dy = x - 7, y - 7
            if dx*dx + dy*dy <= 14:
                d.point((x, y), fill=p["core"])
    # Fleche gauche <<
    left = [(5, 7), (5, 8), (6, 6), (6, 9), (7, 5), (7, 10)]
    # Fleche droite >>
    right = [(10, 7), (10, 8), (9, 6), (9, 9), (8, 5), (8, 10)]
    for pts, col in [(left, p["bright"]), (right, p["bright"])]:
        for px, py in pts:
            d.point((px, py), fill=col)
    # Ligne centrale
    d.rectangle([6, 7, 9, 8], fill=p["extra"])


def draw_speed_icon(d, p):
    """Triple chevron >>> oranges."""
    # Cercle orange
    for y in range(4, 12):
        for x in range(4, 12):
            dx, dy = x - 7, y - 7
            if dx*dx + dy*dy <= 14:
                d.point((x, y), fill=p["core"])
    # 3 chevrons qui pointent a droite
    chevrons = [
        # Chevron 1 (plus a gauche)
        (5, 6), (5, 9), (6, 7), (6, 8),
        # Chevron 2
        (7, 6), (7, 9), (8, 7), (8, 8),
        # Chevron 3 (plus a droite)
        (9, 6), (9, 9), (10, 7), (10, 8),
    ]
    for px, py in chevrons:
        d.point((px, py), fill=p["bright"])
    # Point brillants
    d.point((6, 7), fill=p["extra"])
    d.point((8, 7), fill=p["extra"])
    d.point((10, 7), fill=p["extra"])


def draw_efficiency_icon(d, p):
    """Feuille / sparkle vert (eco)."""
    # Cercle vert
    for y in range(4, 12):
        for x in range(4, 12):
            dx, dy = x - 7, y - 7
            if dx*dx + dy*dy <= 14:
                d.point((x, y), fill=p["core"])
    # Feuille stylisee (losange + tige)
    leaf = [
        (7, 4), (7, 5), (8, 5), (7, 6), (8, 6), (6, 6),
        (7, 7), (8, 7), (9, 7), (6, 7),
        (7, 8), (8, 8), (9, 8),
        (7, 9), (8, 9),
        (8, 10),  # tige
    ]
    for px, py in leaf:
        d.point((px, py), fill=p["bright"])
    # Veine centrale
    d.point((7, 6), fill=p["extra"])
    d.point((8, 7), fill=p["extra"])
    d.point((8, 8), fill=p["extra"])


# Generation
DRAWERS = {
    "rf_converter":  draw_rf_icon,
    "io_expansion":  draw_io_icon,
    "speed_booster": draw_speed_icon,
    "efficiency":    draw_efficiency_icon,
}

for name, palette in PALETTES.items():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    draw_card_base(d)
    DRAWERS[name](d, palette)
    img.save(f"{OUT}/upgrade_{name}.png")
    print(f"[OK] upgrade_{name}.png")

print("\n4 upgrade textures generated.")
