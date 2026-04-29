#!/usr/bin/env python3
"""
v2 : 4 textures tunnel seamless 256x256 (au lieu de 1).

4 variantes pour le Stage 4 hyperspace, qui se succedent dans
renderHyperspace3D :

  tunnel_tile_a.png  (auto)  : grille hexagonale neon DMT classique
  tunnel_tile_b.png  (auto)  : fragments cristallins / eclats prismatiques
  tunnel_tile_c.png  (Voss)  : runes Voss inventees + circuits
  tunnel_tile_d.png  (Voss)  : blueprint Voss fractal + symboles dimensionnels

Toutes seamless (symmetrie 4-fold sur quad TL puis mirror).

Output :
  - assets/.../manifold/tunnel_tile_a..d.png  (4 variantes hyperspace)
  - assets/.../manifold/tunnel_tile.png       (legacy = copy de tile_a)
"""
import os
import math
import random
from PIL import Image, ImageDraw

OUT_DIR = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui/manifold"
os.makedirs(OUT_DIR, exist_ok=True)

TILE_SIZE = 256
HALF = TILE_SIZE // 2

TUNNEL_PALETTE = [
    (0, 200, 255), (255, 230, 50), (255, 50, 50),
    (255, 215, 0), (0, 255, 220), (100, 255, 100),
]

VOSS_PALETTE = [
    (255, 215, 0), (0, 255, 230), (180, 50, 255),
    (255, 100, 200), (100, 255, 200), (255, 220, 100),
]


def mirror_seamless(quad):
    tile = Image.new("RGBA", (TILE_SIZE, TILE_SIZE), (0, 0, 0, 255))
    tile.paste(quad, (0, 0))
    tile.paste(quad.transpose(Image.FLIP_LEFT_RIGHT), (HALF, 0))
    tile.paste(quad.transpose(Image.FLIP_TOP_BOTTOM), (0, HALF))
    tile.paste(
        quad.transpose(Image.FLIP_LEFT_RIGHT).transpose(Image.FLIP_TOP_BOTTOM),
        (HALF, HALF)
    )
    return tile


def draw_quad_a(draw, palette_offset=0):
    """Variante A : hexagones imbriques + diamants + yeux (auto)."""
    random.seed(42 + palette_offset)
    pal = TUNNEL_PALETTE

    for i in range(8, 0, -1):
        size = HALF * (i / 8)
        color = pal[(i + palette_offset) % len(pal)]
        n = 6
        pts = []
        for k in range(n):
            a = (k / n) * 2 * math.pi
            pts.append((max(0, math.cos(a) * size),
                        max(0, math.sin(a) * size)))
        draw.polygon(pts, outline=color + (255,))

    for i in range(6, 0, -1):
        radius = HALF * (i / 6) * 0.7
        pts = [(radius, 0), (HALF * 0.6 + radius * 0.3, radius),
               (radius, HALF * 0.6 + radius * 0.3), (0, radius)]
        color = pal[(i + 2 + palette_offset) % len(pal)]
        draw.polygon(pts, outline=color + (255,))

    for cx, cy, r in [(HALF * 0.3, HALF * 0.3, 8),
                       (HALF * 0.7, HALF * 0.5, 6),
                       (HALF * 0.5, HALF * 0.8, 7),
                       (HALF * 0.2, HALF * 0.7, 5)]:
        color = pal[int(cx + cy + palette_offset) % len(pal)]
        draw.ellipse([(cx - r, cy - r), (cx + r, cy + r)],
                     fill=color + (255,))
        draw.ellipse([(cx - r * 0.4, cy - r * 0.4),
                      (cx + r * 0.4, cy + r * 0.4)],
                     fill=(0, 0, 0, 255))


def draw_quad_b(draw, palette_offset=0):
    """Variante B : eclats cristallins prismatiques (auto)."""
    random.seed(123 + palette_offset)
    pal = TUNNEL_PALETTE

    # Eclats radiaux longs
    for i in range(12):
        a = (i / 12) * (math.pi / 2)
        a_w = math.pi / 24
        r_in = HALF * 0.05
        r_out = HALF * (0.7 + (i % 3) * 0.1)
        p1 = (math.cos(a) * r_in, math.sin(a) * r_in)
        p2 = (math.cos(a - a_w) * r_out, math.sin(a - a_w) * r_out)
        p3 = (math.cos(a + a_w) * r_out, math.sin(a + a_w) * r_out)
        color = pal[(i + palette_offset) % len(pal)]
        draw.polygon([p1, p2, p3], fill=color + (220,))

    # Triangles cristallins disperses
    for _ in range(15):
        cx = random.uniform(10, HALF - 10)
        cy = random.uniform(10, HALF - 10)
        s = random.uniform(4, 12)
        rot = random.uniform(0, math.pi * 2)
        color = pal[random.randint(0, len(pal) - 1)]
        pts = []
        for k in range(3):
            a = rot + (k / 3) * 2 * math.pi
            pts.append((cx + math.cos(a) * s, cy + math.sin(a) * s))
        draw.polygon(pts, outline=color + (255,))

    for i in range(8):
        offset = i * 16
        color = pal[(i + 3 + palette_offset) % len(pal)]
        draw.line([(0, offset), (HALF, max(0, offset - HALF * 0.2))],
                  fill=color + (140,), width=1)


def draw_voss_rune(draw, cx, cy, size, color):
    """Une rune Voss : cercle + triangle + barre asymetrique."""
    draw.ellipse([(cx - size, cy - size), (cx + size, cy + size)],
                 outline=color + (255,), width=2)
    pts = []
    for k in range(3):
        a = -math.pi / 2 + (k / 3) * 2 * math.pi
        pts.append((cx + math.cos(a) * size * 0.7,
                    cy + math.sin(a) * size * 0.7))
    draw.polygon(pts, outline=color + (255,))
    draw.ellipse([(cx - 2, cy - 2), (cx + 2, cy + 2)], fill=color + (255,))
    draw.line([(cx - size, cy - size * 0.3),
               (cx - size * 0.3, cy - size)],
              fill=color + (255,), width=2)


def draw_quad_c(draw, palette_offset=0):
    """Variante C : runes Voss + circuits PCB (Voss)."""
    random.seed(999 + palette_offset)
    pal = VOSS_PALETTE

    grid = 24
    for x in range(0, HALF, grid):
        for y in range(0, HALF, grid):
            color = pal[(x + y + palette_offset) % len(pal)]
            if (x + y) % (2 * grid) == 0:
                draw.line([(x, y + grid // 2), (x + grid, y + grid // 2)],
                          fill=color + (160,), width=1)
            else:
                draw.line([(x + grid // 2, y), (x + grid // 2, y + grid)],
                          fill=color + (160,), width=1)
            if random.random() < 0.3:
                draw.ellipse([(x + grid // 2 - 2, y + grid // 2 - 2),
                              (x + grid // 2 + 2, y + grid // 2 + 2)],
                             fill=color + (220,))

    for cx, cy, s in [(HALF * 0.3, HALF * 0.3, 18),
                       (HALF * 0.7, HALF * 0.55, 14),
                       (HALF * 0.45, HALF * 0.85, 16)]:
        color = pal[int(cx + cy + palette_offset) % len(pal)]
        draw_voss_rune(draw, cx, cy, s, color)

    color_link = pal[(2 + palette_offset) % len(pal)]
    draw.line([(HALF * 0.3, HALF * 0.3), (HALF * 0.7, HALF * 0.55)],
              fill=color_link + (180,), width=2)


def draw_quad_d(draw, palette_offset=0):
    """Variante D : blueprint fractal Voss + symboles dimensionnels (Voss)."""
    random.seed(2026 + palette_offset)
    pal = VOSS_PALETTE

    for i, scale_factor in enumerate([0.95, 0.75, 0.55, 0.35, 0.18]):
        s = HALF * scale_factor
        rot = i * 0.15
        color = pal[(i + palette_offset) % len(pal)]
        cx = HALF * 0.5
        cy = HALF * 0.5
        pts = []
        for k in range(4):
            a = rot + (k / 4) * 2 * math.pi + math.pi / 4
            pts.append((cx + math.cos(a) * s, cy + math.sin(a) * s))
        pts = [(max(0, x), max(0, y)) for (x, y) in pts]
        draw.polygon(pts, outline=color + (255,))

    for i in range(6):
        y = HALF * 0.1 + i * (HALF * 0.13)
        color = pal[(i + 2 + palette_offset) % len(pal)]
        draw.line([(HALF * 0.05, y), (HALF * 0.95, y)],
                  fill=color + (140,), width=1)
        draw.line([(HALF * 0.05, y - 3), (HALF * 0.05, y + 3)],
                  fill=color + (200,), width=1)
        draw.line([(HALF * 0.95, y - 3), (HALF * 0.95, y + 3)],
                  fill=color + (200,), width=1)

    color_sym = pal[(palette_offset) % len(pal)]
    # Nablah haut-gauche
    cx, cy = HALF * 0.15, HALF * 0.15
    draw.polygon([(cx, cy + 8), (cx - 6, cy - 4), (cx + 6, cy - 4)],
                 outline=color_sym + (255,))
    # Asterisme haut-droit
    cx, cy = HALF * 0.85, HALF * 0.15
    for k in range(6):
        a = (k / 6) * 2 * math.pi
        draw.line([(cx, cy),
                   (cx + math.cos(a) * 6, cy + math.sin(a) * 6)],
                  fill=color_sym + (255,), width=1)
    # Croix de Voss bas-gauche
    cx, cy = HALF * 0.15, HALF * 0.85
    draw.line([(cx - 6, cy), (cx + 6, cy)], fill=color_sym + (255,), width=2)
    draw.line([(cx, cy - 6), (cx, cy + 6)], fill=color_sym + (255,), width=2)
    draw.ellipse([(cx - 3, cy - 3), (cx + 3, cy + 3)],
                 outline=color_sym + (255,))
    # Losange bas-droit
    cx, cy = HALF * 0.85, HALF * 0.85
    draw.polygon([(cx, cy - 7), (cx + 7, cy), (cx, cy + 7), (cx - 7, cy)],
                 outline=color_sym + (255,))


variants = [
    ("a", draw_quad_a, 0),
    ("b", draw_quad_b, 1),
    ("c", draw_quad_c, 2),
    ("d", draw_quad_d, 3),
]

print(f"Generation 4 tunnel tiles seamless {TILE_SIZE}x{TILE_SIZE}")
print(f"Output : {OUT_DIR}")
print()

for letter, draw_fn, off in variants:
    quad = Image.new("RGBA", (HALF, HALF), (0, 0, 0, 255))
    d = ImageDraw.Draw(quad, "RGBA")
    draw_fn(d, palette_offset=off)
    tile = mirror_seamless(quad)
    out_path = os.path.join(OUT_DIR, f"tunnel_tile_{letter}.png")
    tile.save(out_path, "PNG", optimize=True)
    print(f"  [{letter}] {out_path}")

# Legacy : tunnel_tile.png = copie de tile_a
import shutil
shutil.copyfile(
    os.path.join(OUT_DIR, "tunnel_tile_a.png"),
    os.path.join(OUT_DIR, "tunnel_tile.png")
)
print(f"  [legacy] tunnel_tile.png (= tile_a)")
print()
print("4 variantes tunnel generees.")
