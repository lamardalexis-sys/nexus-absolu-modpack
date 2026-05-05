#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Genere les 4 variantes de textures tunnel pour le Cartouche Manifold
(Stage 4 Hyperspace) - VERSION FRACTALE PURE.

Architecture :
  - 4 variantes crescendo A -> B -> C -> D (du simple a l'ultra complexe)
  - 100% fractales geometriques (zero etoiles/nebuleuses)
  - 1024x1024, fond transparent, palette VOSS SATURATED

Crescendo dramatique :
  A : HEXAGONES        - hexagones recursifs purs, calme
  B : + SPIRALES       - + spirales arithmetiques (mouvement)
  C : + ETOILES DAVID  - + etoiles de David recursives + tessellation hex
  D : ULTRA COMPLEXE   - TOUT empile (matrice fractale infinie)

Le shader Java existant (renderHyperspace3D + 3 couches parallax 2D) anime
ces textures avec zoom, rotation vortex, acceleration progressive.

Output : assets/nexusabsolu/textures/gui/manifold/tunnel_tile_a.png ... _d.png
         + tunnel_tile.png (legacy = copie de _a)

Pour regenerer : python3 scripts-tools/generate_tunnel_v2.py
"""
import os
import math
from PIL import Image, ImageDraw

SIZE = 1024
SS = 2
DRAW = SIZE * SS
CX = CY = DRAW // 2
OUT_DIR = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui/manifold"

VOSS = [
    (80, 0, 180), (160, 30, 255), (220, 80, 255), (0, 200, 255),
    (50, 255, 255), (255, 220, 0), (255, 80, 200), (255, 255, 255),
]


def regular_polygon(cx, cy, radius, n, rotation=0):
    return [(cx + math.cos(rotation + 2*math.pi*i/n) * radius,
             cy + math.sin(rotation + 2*math.pi*i/n) * radius)
            for i in range(n)]


def fractal_polygons(draw, cx, cy, radius, n, depth, rotation, color_idx):
    if depth <= 0 or radius < 4:
        return
    pts = regular_polygon(cx, cy, radius, n, rotation)
    color = VOSS[color_idx % 8]
    alpha = int(220 - depth * 25)
    width = max(1, depth * SS)
    draw.polygon(pts, outline=color + (alpha,), width=width)
    new_radius = radius * 0.42
    for px, py in pts:
        fractal_polygons(draw, px, py, new_radius, n,
                         depth - 1, rotation + math.pi/n,
                         color_idx + 1)


def fractal_spiral(draw, cx, cy, max_r, n_arms, n_steps, color_idx):
    for arm in range(n_arms):
        base = arm * math.pi * 2 / n_arms
        for step in range(n_steps):
            t = step / n_steps
            r = 20 + t * max_r
            angle = base + t * math.pi * 4
            x = cx + math.cos(angle) * r
            y = cy + math.sin(angle) * r
            sz = max(1, int(2 + t * 8))
            color = VOSS[(color_idx + step // 8) % 8]
            alpha = int(60 + t * 180)
            draw.ellipse([x-sz, y-sz, x+sz, y+sz],
                         outline=color + (alpha,), width=2)


def hex_tessellation_hyperbolic(draw, cx, cy, base_radius, max_radius):
    n_rings = 8
    for ring in range(n_rings):
        r_at_ring = base_radius + (max_radius - base_radius) * (ring / n_rings) ** 1.5
        hex_size = base_radius * (1.2 + ring * 0.4)
        n_hex = max(6, 6 + ring * 6)
        for i in range(n_hex):
            a = i * math.pi * 2 / n_hex + ring * math.pi / n_hex
            x = cx + math.cos(a) * r_at_ring
            y = cy + math.sin(a) * r_at_ring
            pts = regular_polygon(x, y, hex_size, 6, ring * math.pi/12)
            color = VOSS[(ring + i) % 8]
            alpha = int(200 - ring * 15)
            draw.polygon(pts, outline=color + (alpha,),
                         width=max(1, 4-ring//2)*SS)


def star_of_david_recursive(draw, cx, cy, radius, depth, rotation, color_idx):
    if depth <= 0 or radius < 6:
        return
    color = VOSS[color_idx % 8]
    alpha = int(240 - depth * 20)
    width = max(1, depth * SS)
    for tri in range(2):
        pts = regular_polygon(cx, cy, radius, 3, rotation + tri * math.pi/3)
        draw.polygon(pts, outline=color + (alpha,), width=width)
    new_r = radius * 0.38
    for i in range(6):
        a = rotation + i * math.pi/3
        x = cx + math.cos(a) * radius
        y = cy + math.sin(a) * radius
        star_of_david_recursive(draw, x, y, new_r, depth - 1,
                                rotation, color_idx + 1)


def concentric_subdiv_rings(draw, cx, cy, max_r, n_rings):
    for i in range(n_rings):
        r_out = max_r * (1 - i / n_rings) ** 0.7
        r_in = max_r * (1 - (i + 1) / n_rings) ** 0.7
        n_div = 12 + i * 8
        color = VOSS[(i + 3) % 8]
        alpha = int(180 - i * 15)
        for j in range(n_div):
            a = j * math.pi * 2 / n_div
            x_out = cx + math.cos(a) * r_out
            y_out = cy + math.sin(a) * r_out
            x_in = cx + math.cos(a) * r_in
            y_in = cy + math.sin(a) * r_in
            draw.line([x_in, y_in, x_out, y_out],
                      fill=color + (alpha,), width=2*SS)
        draw.ellipse([cx-r_out, cy-r_out, cx+r_out, cy+r_out],
                     outline=color + (alpha,), width=2*SS)


def micro_mandalas_central(draw, cx, cy):
    for r_factor in [0.15, 0.10, 0.06, 0.03]:
        fractal_polygons(draw, cx, cy, DRAW * r_factor, n=12, depth=2,
                         rotation=r_factor * 10, color_idx=int(r_factor * 30))


# ============================================================
# 4 variantes crescendo
# ============================================================

def make_variant_A():
    """HEXAGONES - hexagones recursifs purs, le plus clean."""
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    d = ImageDraw.Draw(img, 'RGBA')
    fractal_polygons(d, CX, CY, DRAW * 0.42, n=6, depth=4, rotation=0, color_idx=1)
    r = DRAW * 0.04
    d.ellipse([CX-r, CY-r, CX+r, CY+r], outline=VOSS[4] + (255,), width=3*SS)
    return img.resize((SIZE, SIZE), Image.LANCZOS)


def make_variant_B():
    """+ SPIRALES - hexagones recursifs + spirales arithmetiques + octogones."""
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    d = ImageDraw.Draw(img, 'RGBA')
    fractal_spiral(d, CX, CY, max_r=DRAW * 0.45, n_arms=6, n_steps=80, color_idx=2)
    fractal_polygons(d, CX, CY, DRAW * 0.42, n=6, depth=4, rotation=0, color_idx=1)
    fractal_polygons(d, CX, CY, DRAW * 0.35, n=8, depth=3,
                     rotation=math.pi/8, color_idx=3)
    return img.resize((SIZE, SIZE), Image.LANCZOS)


def make_variant_C():
    """+ ETOILES DAVID + TESSELLATION - sacred geometry s'intensifie."""
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    d = ImageDraw.Draw(img, 'RGBA')
    hex_tessellation_hyperbolic(d, CX, CY, base_radius=80*SS,
                                max_radius=DRAW*0.45)
    fractal_spiral(d, CX, CY, max_r=DRAW * 0.45, n_arms=6, n_steps=80, color_idx=2)
    fractal_polygons(d, CX, CY, DRAW * 0.42, n=6, depth=4,
                     rotation=0, color_idx=1)
    fractal_polygons(d, CX, CY, DRAW * 0.35, n=8, depth=3,
                     rotation=math.pi/8, color_idx=3)
    star_of_david_recursive(d, CX, CY, DRAW * 0.30, depth=3,
                            rotation=0, color_idx=5)
    return img.resize((SIZE, SIZE), Image.LANCZOS)


def make_variant_D():
    """ULTRA COMPLEXE - tout empile : matrice fractale infinie."""
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    d = ImageDraw.Draw(img, 'RGBA')
    hex_tessellation_hyperbolic(d, CX, CY, base_radius=80*SS,
                                max_radius=DRAW*0.45)
    fractal_spiral(d, CX, CY, max_r=DRAW * 0.45, n_arms=6, n_steps=80, color_idx=2)
    fractal_polygons(d, CX, CY, DRAW * 0.42, n=6, depth=4,
                     rotation=0, color_idx=1)
    fractal_polygons(d, CX, CY, DRAW * 0.35, n=8, depth=3,
                     rotation=math.pi/8, color_idx=3)
    star_of_david_recursive(d, CX, CY, DRAW * 0.30, depth=3,
                            rotation=0, color_idx=5)
    concentric_subdiv_rings(d, CX, CY, DRAW * 0.45, n_rings=10)
    micro_mandalas_central(d, CX, CY)
    return img.resize((SIZE, SIZE), Image.LANCZOS)


def make_mockup_3d(tile, output_path):
    """Simule rendu 3D : 8 quads en perspective."""
    W, H = 1280, 720
    mockup = Image.new('RGB', (W, H), (5, 2, 15))
    cx, cy = W // 2, H // 2

    for layer in range(8):
        z = layer / 7
        scale = 0.15 + (1 - z) * 1.2
        alpha = int(60 + (1 - z) * 120)
        quad_w = int(W * scale)
        quad_h = int(H * scale)

        tile_resized = tile.resize((quad_w, quad_h), Image.LANCZOS)
        if alpha < 255:
            r, g, b, a = tile_resized.split()
            a = a.point(lambda p: int(p * alpha / 255))
            tile_resized = Image.merge('RGBA', (r, g, b, a))

        px = cx - quad_w // 2
        py = cy - quad_h // 2
        mockup_rgba = mockup.convert('RGBA')
        mockup_rgba.alpha_composite(tile_resized, (px, py))
        mockup = mockup_rgba.convert('RGB')

    mockup.save(output_path)


if __name__ == "__main__":
    os.makedirs(OUT_DIR, exist_ok=True)

    variants = {
        "a": ("HEXAGONES", make_variant_A),
        "b": ("+ SPIRALES", make_variant_B),
        "c": ("+ ETOILES DAVID + TESSELLATION", make_variant_C),
        "d": ("ULTRA COMPLEXE", make_variant_D),
    }

    print("=== Generation 4 textures tunnel FRACTAL crescendo ===\n")

    for letter, (label, fn) in variants.items():
        print(f"  Variant {letter.upper()} - {label}...")
        img = fn()
        out_path = os.path.join(OUT_DIR, f"tunnel_tile_{letter}.png")
        img.save(out_path, "PNG", optimize=True)

        pixels = img.load()
        corner = pixels[0, 0]
        size_kb = os.path.getsize(out_path) / 1024
        print(f"    -> {out_path} ({size_kb:.0f} KB, coin alpha={corner[3]})")

    legacy_path = os.path.join(OUT_DIR, "tunnel_tile.png")
    img_a = Image.open(os.path.join(OUT_DIR, "tunnel_tile_a.png"))
    img_a.save(legacy_path, "PNG", optimize=True)
    print(f"\n  Legacy {legacy_path} (copie de _a)")

    print("\n=== TERMINE ===")
