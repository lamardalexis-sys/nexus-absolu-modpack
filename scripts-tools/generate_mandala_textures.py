#!/usr/bin/env python3
"""
Genere 4 textures fractales mandala 512x512 ULTRA SATURE pour overlay DMT.

Differences avec v1 :
- Fond noir (vs transparent) → contraste max
- Pas de blur → contours nets
- Alpha 255 (pas de transparence intermediaire)
- Geometrie sacree : hexagones, triangles imbriques, sub-mandalas niveaux 2-3
- Symetrie radiale 12x ou 16x (vs 8x avant)

Output : assets/nexusabsolu/textures/gui/manifold/mandala_1..4.png
"""
import os
import math
from PIL import Image, ImageDraw

SIZE = 512
CENTER = SIZE // 2

# Palette DMT 8 couleurs ULTRA NEON
PALETTE = [
    (255, 0, 200),    # magenta neon
    (255, 100, 0),    # orange neon
    (255, 220, 0),    # or neon
    (140, 255, 0),    # lime neon
    (0, 255, 200),    # turquoise neon
    (0, 220, 255),    # cyan neon
    (180, 50, 255),   # violet neon
    (255, 0, 100)     # rose neon
]

OUT_DIR = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui/manifold"
os.makedirs(OUT_DIR, exist_ok=True)


def regular_polygon(cx, cy, radius, n, rotation=0):
    return [
        (cx + math.cos(rotation + 2 * math.pi * i / n) * radius,
         cy + math.sin(rotation + 2 * math.pi * i / n) * radius)
        for i in range(n)
    ]


def draw_petal_polygon(draw, n_petals, r_in, r_out, rotation, palette_offset, alpha=255):
    for i in range(n_petals):
        a1 = rotation + (i / n_petals) * 2 * math.pi
        a2 = rotation + ((i + 1) / n_petals) * 2 * math.pi
        pts = []
        steps = 24
        for s in range(steps + 1):
            f = s / steps
            a = a1 + (a2 - a1) * f
            mod = math.sin(f * math.pi)
            r = r_in + (r_out - r_in) * mod
            pts.append((CENTER + math.cos(a) * r, CENTER + math.sin(a) * r))
        for s in range(steps + 1):
            f = s / steps
            a = a2 - (a2 - a1) * f
            r = r_in
            pts.append((CENTER + math.cos(a) * r, CENTER + math.sin(a) * r))
        color = PALETTE[(i + palette_offset) % len(PALETTE)] + (alpha,)
        draw.polygon(pts, fill=color)


def draw_radiating_triangles(draw, n, r_in, r_out, rotation, palette_offset, alpha=255):
    half_width = math.pi / n * 0.4
    for i in range(n):
        a = rotation + (i / n) * 2 * math.pi
        p1 = (CENTER + math.cos(a) * r_in, CENTER + math.sin(a) * r_in)
        p2 = (CENTER + math.cos(a - half_width) * r_out,
              CENTER + math.sin(a - half_width) * r_out)
        p3 = (CENTER + math.cos(a + half_width) * r_out,
              CENTER + math.sin(a + half_width) * r_out)
        color = PALETTE[(i + palette_offset) % len(PALETTE)] + (alpha,)
        draw.polygon([p1, p2, p3], fill=color)


def draw_polygon_outline(draw, n, radius, rotation, color, width):
    pts = regular_polygon(CENTER, CENTER, radius, n, rotation)
    pts.append(pts[0])
    for i in range(len(pts) - 1):
        draw.line([pts[i], pts[i + 1]], fill=color + (255,), width=width)


def draw_sacred_geometry(draw, max_r, palette_offset):
    for i, n in enumerate([6, 8, 12, 16]):
        for j in range(2):
            rotation = (j * math.pi / n) + i * 0.1
            radius = max_r * (0.3 + i * 0.18)
            color = PALETTE[(i + j + palette_offset) % len(PALETTE)]
            draw_polygon_outline(draw, n, radius, rotation, color, 2)


def draw_sub_mandalas(draw, ring_radius, count, palette_offset):
    for i in range(count):
        a = (i / count) * 2 * math.pi
        cx = CENTER + math.cos(a) * ring_radius
        cy = CENTER + math.sin(a) * ring_radius
        for j in range(6):
            ja = (j / 6) * 2 * math.pi + a
            ex = cx + math.cos(ja) * 12
            ey = cy + math.sin(ja) * 12
            color = PALETTE[(i + j + palette_offset) % len(PALETTE)]
            draw.ellipse(
                [(ex - 6, ey - 6), (ex + 6, ey + 6)],
                fill=color + (255,)
            )
        center_color = PALETTE[(i + palette_offset + 4) % len(PALETTE)]
        draw.ellipse(
            [(cx - 5, cy - 5), (cx + 5, cy + 5)],
            fill=center_color + (255,)
        )


def draw_concentric_arcs(draw, n_arcs, max_r, palette_offset, alpha=200):
    for i in range(n_arcs, 0, -1):
        r = (i / n_arcs) * max_r
        color = PALETTE[(i + palette_offset) % len(PALETTE)] + (alpha,)
        draw.ellipse(
            [(CENTER - r, CENTER - r), (CENTER + r, CENTER + r)],
            outline=color,
            width=3
        )


def make_mandala(seed, n_petals, n_rings, palette_offset):
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 255))
    draw = ImageDraw.Draw(img, "RGBA")

    max_r = SIZE // 2 - 5

    draw_petal_polygon(draw, n_petals, max_r * 0.55, max_r * 0.98,
                       rotation=0,
                       palette_offset=palette_offset, alpha=255)

    draw_petal_polygon(draw, n_petals * 2, max_r * 0.25, max_r * 0.55,
                       rotation=math.pi / (n_petals * 2),
                       palette_offset=palette_offset + 3, alpha=255)

    draw_radiating_triangles(draw, n_petals * 3, max_r * 0.15, max_r * 0.95,
                             rotation=math.pi / (n_petals * 6),
                             palette_offset=palette_offset + 5, alpha=180)

    draw_sacred_geometry(draw, max_r * 0.9, palette_offset + 1)

    draw_sub_mandalas(draw, max_r * 0.7, n_petals,
                      palette_offset=palette_offset + 2)

    draw_concentric_arcs(draw, n_rings, max_r * 0.92,
                         palette_offset=palette_offset + 1, alpha=200)

    for i in range(8, 0, -1):
        r = max_r * 0.10 * (i / 8)
        color = PALETTE[(i + palette_offset + 4) % len(PALETTE)]
        draw.ellipse(
            [(CENTER - r, CENTER - r), (CENTER + r, CENTER + r)],
            fill=color + (255,)
        )

    draw.ellipse(
        [(CENTER - 4, CENTER - 4), (CENTER + 4, CENTER + 4)],
        fill=(255, 255, 255, 255)
    )

    return img


configs = [
    {"seed": 42,  "n_petals": 8,  "n_rings": 16, "palette_offset": 0},
    {"seed": 123, "n_petals": 12, "n_rings": 12, "palette_offset": 2},
    {"seed": 456, "n_petals": 6,  "n_rings": 20, "palette_offset": 4},
    {"seed": 789, "n_petals": 16, "n_rings": 14, "palette_offset": 6}
]

for i, cfg in enumerate(configs, 1):
    img = make_mandala(**cfg)
    out_path = os.path.join(OUT_DIR, f"mandala_{i}.png")
    img.save(out_path, "PNG")
    print(f"  Generated: {out_path}")

print(f"\n4 textures generees dans {OUT_DIR}")
