#!/usr/bin/env python3
"""
Genere 2 textures supplementaires pour l'overlay DMT :

1. tunnel_tile.png — 256x256 SEAMLESS (les bords se raccordent)
   Pattern dense filigrane dore-multicolore qui se tile parfaitement.
   Affiche en 3x3 avec scale qui pulse → effet tunnel zoom in/out.

2. entity.png — 256x256 silhouette centrale
   Forme triangulaire-organique avec filigrane dore + 2 yeux roses.
   Inspire image 1 (entite centrale).

Output : assets/nexusabsolu/textures/gui/manifold/
"""
import os
import math
import random
from PIL import Image, ImageDraw

OUT_DIR = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui/manifold"
os.makedirs(OUT_DIR, exist_ok=True)


# ============================================================================
# TUNNEL TILE — 256x256 seamless
# ============================================================================
# Strategy : symmetrie 4-fold (le quad TL est mirror'd vers TR/BL/BR) → seamless
# par construction. Pattern tres dense : grilles imbriques + diamants + traits.

TILE_SIZE = 256
HALF = TILE_SIZE // 2

# Palette tunnel : tons cyan/jaune/rouge/dore (image 2 ref)
TUNNEL_PALETTE = [
    (0, 200, 255),    # cyan
    (255, 230, 50),   # jaune
    (255, 50, 50),    # rouge
    (255, 215, 0),    # or
    (0, 255, 220),    # turquoise
    (100, 255, 100),  # vert
]


def draw_tunnel_quad(draw, palette_offset=0):
    """Dessine UN quad (haut-gauche) 128x128, sera mirror'd."""
    random.seed(42 + palette_offset)

    # Grille de base : carres concentriques offset depuis (0,0)
    for i in range(8, 0, -1):
        size = HALF * (i / 8)
        color = TUNNEL_PALETTE[(i + palette_offset) % len(TUNNEL_PALETTE)]
        # Carre vide (outline)
        draw.rectangle(
            [0, 0, size, size],
            outline=color + (255,),
            width=2
        )

    # Diamants (rotated rectangles) qui se chevauchent
    for i in range(6, 0, -1):
        radius = HALF * (i / 6) * 0.7
        # Pointe haut/bas/gauche/droite depuis (0,0) vers exterieur
        pts = [
            (radius, 0),
            (HALF * 0.6 + radius * 0.3, radius),
            (radius, HALF * 0.6 + radius * 0.3),
            (0, radius)
        ]
        color = TUNNEL_PALETTE[(i + 2 + palette_offset) % len(TUNNEL_PALETTE)]
        draw.polygon(pts, outline=color + (255,))

    # Diagonales decoratives (filigrane)
    for i in range(6):
        offset = i * 20
        color = TUNNEL_PALETTE[(i + 4 + palette_offset) % len(TUNNEL_PALETTE)]
        draw.line(
            [(0, offset), (HALF, offset + HALF * 0.3)],
            fill=color + (180,),
            width=1
        )
        draw.line(
            [(offset, 0), (offset + HALF * 0.3, HALF)],
            fill=color + (180,),
            width=1
        )

    # Petits cercles "yeux" repartis
    for cx, cy, r in [(HALF * 0.3, HALF * 0.3, 8),
                       (HALF * 0.7, HALF * 0.5, 6),
                       (HALF * 0.5, HALF * 0.8, 7),
                       (HALF * 0.2, HALF * 0.7, 5)]:
        color = TUNNEL_PALETTE[int(cx + cy + palette_offset) % len(TUNNEL_PALETTE)]
        draw.ellipse(
            [(cx - r, cy - r), (cx + r, cy + r)],
            fill=color + (255,)
        )
        # Pupille
        draw.ellipse(
            [(cx - r * 0.4, cy - r * 0.4), (cx + r * 0.4, cy + r * 0.4)],
            fill=(0, 0, 0, 255)
        )

    # Petits triangles decoratifs
    for i in range(8):
        angle = (i / 8) * 2 * math.pi
        cx = HALF * 0.5 + math.cos(angle) * HALF * 0.35
        cy = HALF * 0.5 + math.sin(angle) * HALF * 0.35
        s = 8
        color = TUNNEL_PALETTE[(i + palette_offset) % len(TUNNEL_PALETTE)]
        draw.polygon([
            (cx, cy - s),
            (cx + s, cy + s),
            (cx - s, cy + s)
        ], fill=color + (255,))


def make_tunnel_tile():
    """Construit la texture seamless 256x256 par symmetrie 4-fold."""
    # Quad haut-gauche
    quad = Image.new("RGBA", (HALF, HALF), (0, 0, 0, 255))
    draw = ImageDraw.Draw(quad, "RGBA")
    draw_tunnel_quad(draw, palette_offset=0)

    # Compose la texture finale par mirror
    tile = Image.new("RGBA", (TILE_SIZE, TILE_SIZE), (0, 0, 0, 255))
    # Top-Left : original
    tile.paste(quad, (0, 0))
    # Top-Right : flip horizontal
    tile.paste(quad.transpose(Image.FLIP_LEFT_RIGHT), (HALF, 0))
    # Bottom-Left : flip vertical
    tile.paste(quad.transpose(Image.FLIP_TOP_BOTTOM), (0, HALF))
    # Bottom-Right : flip both
    tile.paste(
        quad.transpose(Image.FLIP_LEFT_RIGHT).transpose(Image.FLIP_TOP_BOTTOM),
        (HALF, HALF)
    )
    return tile


tile = make_tunnel_tile()
tile_path = os.path.join(OUT_DIR, "tunnel_tile.png")
tile.save(tile_path, "PNG")
print(f"  Generated: {tile_path}")


# ============================================================================
# ENTITY TEXTURE — 256x256 entite centrale
# ============================================================================
# Forme : ovale-triangulaire (goutte d'eau) avec filigrane interne dore + yeux

ENTITY_SIZE = 256
EH = ENTITY_SIZE // 2

ENTITY_GOLD = (255, 215, 0)
ENTITY_GOLD_DARK = (180, 130, 0)
ENTITY_PINK = (255, 50, 200)
ENTITY_BLACK = (10, 5, 15)


def make_entity():
    img = Image.new("RGBA", (ENTITY_SIZE, ENTITY_SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img, "RGBA")

    # === Silhouette : goutte d'eau pointing up ===
    # Ovale principal
    body_top = 30
    body_bottom = ENTITY_SIZE - 10
    body_left = 50
    body_right = ENTITY_SIZE - 50

    # On cree la goutte par 2 demi-ellipses :
    # - Dome haut (pointu)
    # - Dome bas (rond)
    # Approximation : polygone avec arcs

    # Rempli noir/sombre sur l'interieur
    draw.ellipse([(body_left, body_top + 30), (body_right, body_bottom)],
                 fill=ENTITY_BLACK + (255,))
    # Dome pointu en haut (triangle arrondi)
    draw.polygon([
        (EH, body_top),
        (body_right, body_top + 60),
        (body_left, body_top + 60)
    ], fill=ENTITY_BLACK + (255,))

    # === Filigrane dore — lignes décoratives ===
    # Triangles imbriques au centre
    for i in range(6):
        size = 60 - i * 8
        cx = EH
        cy = EH
        pts = [
            (cx, cy - size),
            (cx + size * 0.87, cy + size * 0.5),
            (cx - size * 0.87, cy + size * 0.5)
        ]
        draw.polygon(pts, outline=ENTITY_GOLD + (255,))

    # Lignes radiales depuis le centre
    for i in range(16):
        angle = (i / 16) * 2 * math.pi
        x_end = EH + math.cos(angle) * 70
        y_end = EH + math.sin(angle) * 70
        draw.line([(EH, EH), (x_end, y_end)],
                  fill=ENTITY_GOLD_DARK + (200,), width=1)

    # Cercles concentriques dans le corps
    for i in range(5, 0, -1):
        r = 30 + i * 8
        draw.ellipse(
            [(EH - r, EH - r), (EH + r, EH + r)],
            outline=ENTITY_GOLD + (180,),
            width=2
        )

    # Petits diamants repartis sur le corps (filigrane)
    for cx_d, cy_d, s in [
        (EH - 30, EH - 50, 5),
        (EH + 30, EH - 50, 5),
        (EH - 50, EH + 20, 4),
        (EH + 50, EH + 20, 4),
        (EH, EH + 60, 6),
        (EH, EH - 80, 4),
        (EH - 70, EH + 60, 4),
        (EH + 70, EH + 60, 4),
    ]:
        draw.polygon([
            (cx_d, cy_d - s),
            (cx_d + s, cy_d),
            (cx_d, cy_d + s),
            (cx_d - s, cy_d)
        ], fill=ENTITY_GOLD + (255,))

    # === LES YEUX (signature DMT entity) ===
    eye_y = EH - 30
    eye_left_x = EH - 28
    eye_right_x = EH + 28
    eye_radius_x = 18
    eye_radius_y = 12

    # Eil gauche — gradient rose vers magenta
    for i in range(5, 0, -1):
        f = i / 5
        rx = eye_radius_x * f
        ry = eye_radius_y * f
        a = int(255 * f)
        # gradient color
        r = int(255)
        g = int(50 + (1 - f) * 50)
        b = int(200 + (1 - f) * 30)
        draw.ellipse(
            [(eye_left_x - rx, eye_y - ry), (eye_left_x + rx, eye_y + ry)],
            fill=(r, g, b, a)
        )
    # Eil droit (mirror)
    for i in range(5, 0, -1):
        f = i / 5
        rx = eye_radius_x * f
        ry = eye_radius_y * f
        a = int(255 * f)
        r = int(255)
        g = int(50 + (1 - f) * 50)
        b = int(200 + (1 - f) * 30)
        draw.ellipse(
            [(eye_right_x - rx, eye_y - ry), (eye_right_x + rx, eye_y + ry)],
            fill=(r, g, b, a)
        )

    # Glow blanc au centre des yeux (highlight)
    draw.ellipse(
        [(eye_left_x - 4, eye_y - 3), (eye_left_x + 4, eye_y + 3)],
        fill=(255, 255, 255, 220)
    )
    draw.ellipse(
        [(eye_right_x - 4, eye_y - 3), (eye_right_x + 4, eye_y + 3)],
        fill=(255, 255, 255, 220)
    )

    # === Outline doré final (silhouette) ===
    # Contour pointu haut
    draw.line([(EH, body_top), (body_right, body_top + 60)],
              fill=ENTITY_GOLD + (255,), width=3)
    draw.line([(EH, body_top), (body_left, body_top + 60)],
              fill=ENTITY_GOLD + (255,), width=3)
    # Contour ovale bas
    draw.arc(
        [(body_left, body_top + 30), (body_right, body_bottom)],
        start=0, end=180,
        fill=ENTITY_GOLD + (255,),
        width=3
    )
    # Closure horizontal
    draw.line([(body_left, body_top + 60), (body_left + 5, body_top + 80)],
              fill=ENTITY_GOLD + (255,), width=3)
    draw.line([(body_right, body_top + 60), (body_right - 5, body_top + 80)],
              fill=ENTITY_GOLD + (255,), width=3)

    return img


entity = make_entity()
entity_path = os.path.join(OUT_DIR, "entity.png")
entity.save(entity_path, "PNG")
print(f"  Generated: {entity_path}")

print(f"\n2 textures generees")
