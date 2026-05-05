#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Genere les 4 variantes de textures tunnel pour le Cartouche Manifold
(Stage 4 Hyperspace) - VERSION MEGASTRUCTURES COSMIQUES.

Concept : galaxie civilisee pleine de structures architecturales geantes
(Dyson sphere, Halo Ringworld, Death Star, pyramides, citadelles gothiques,
stations orbitales, cubes Borg, cylindres Rama). Wireframe blueprint style.

Architecture :
  - 4 variantes : structure dominante differente par variante
  - 15+ structures background dispersees (densite ULTRA)
  - 1024x1024, fond transparent, palette VOSS SATURATED

4 variantes (theme dominant) :
  A : DYSON SPHERE        - Dyson sphere centrale geante + ringworld au-dessus
  B : HALO RINGWORLD      - 2 anneaux geants + cylindres orbitaux
  C : CITADELLES GOTHIQUES - cluster de tours + pyramides
  D : DEATH STAR FLEET    - cluster de Death Stars + cubes Borg

Chaque variante : 15 structures background aleatoires + 3-4 grosses foreground
+ coeur lumineux blanc central (point de fuite).

Le shader Java existant (renderHyperspace3D + 3 couches parallax 2D + crossfade
A-B-C-D 50% + acceleration progressive) anime ces textures.

Pour regenerer : python3 scripts-tools/generate_tunnel_v2.py
"""
import os
import math
import random
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


# ============================================================
# Megastructures
# ============================================================

def draw_dyson_sphere(draw, cx, cy, radius, color_idx=3, alpha=180):
    """Sphere Dyson : cercle + grille latitudes/longitudes."""
    color = VOSS[color_idx % 8]
    rgba = color + (alpha,)
    draw.ellipse([cx-radius, cy-radius, cx+radius, cy+radius],
                 outline=rgba, width=3*SS)
    # Latitudes
    for i in range(-3, 4):
        lat_y = cy + i * radius * 0.22
        h_factor = math.cos(i * math.pi / 8)
        lat_w = radius * abs(h_factor)
        if lat_w > 4:
            draw.ellipse([cx-lat_w, lat_y-radius*0.02,
                          cx+lat_w, lat_y+radius*0.02],
                         outline=rgba, width=2*SS)
    # Longitudes
    for n in range(6):
        a = n * math.pi / 6
        long_w = radius * abs(math.cos(a))
        if long_w > 4:
            draw.ellipse([cx-long_w, cy-radius, cx+long_w, cy+radius],
                         outline=color + (alpha//2,), width=SS)


def draw_ringworld(draw, cx, cy, radius, color_idx=5, alpha=200):
    """Halo / Ringworld : grand anneau avec subdivisions internes."""
    color = VOSS[color_idx % 8]
    rgba = color + (alpha,)
    ring_h = radius * 0.25
    draw.ellipse([cx-radius, cy-ring_h, cx+radius, cy+ring_h],
                 outline=rgba, width=4*SS)
    draw.ellipse([cx-radius*0.92, cy-ring_h*0.92,
                  cx+radius*0.92, cy+ring_h*0.92],
                 outline=rgba, width=3*SS)
    # Segments
    n_seg = 36
    for i in range(n_seg):
        a = i * 2 * math.pi / n_seg
        x_out = cx + math.cos(a) * radius
        y_out = cy + math.sin(a) * ring_h
        x_in = cx + math.cos(a) * radius * 0.92
        y_in = cy + math.sin(a) * ring_h * 0.92
        draw.line([x_in, y_in, x_out, y_out], fill=rgba, width=SS)


def draw_cube_borg(draw, cx, cy, size, color_idx=1, alpha=180):
    """Cube 3D wireframe avec subdivisions internes."""
    color = VOSS[color_idx % 8]
    rgba = color + (alpha,)
    s = size / 2
    offset = s * 0.4
    front = [(cx-s, cy-s), (cx+s, cy-s), (cx+s, cy+s), (cx-s, cy+s)]
    back = [(p[0]+offset, p[1]-offset) for p in front]
    for i in range(4):
        draw.line([front[i], front[(i+1) % 4]], fill=rgba, width=2*SS)
        draw.line([back[i], back[(i+1) % 4]], fill=rgba, width=2*SS)
        draw.line([front[i], back[i]], fill=rgba, width=2*SS)
    # Grid interne face avant
    for i in range(1, 4):
        f = i / 4
        x1 = cx-s + f*2*s
        draw.line([(x1, cy-s), (x1, cy+s)], fill=color + (alpha//3,), width=SS)
        y1 = cy-s + f*2*s
        draw.line([(cx-s, y1), (cx+s, y1)], fill=color + (alpha//3,), width=SS)


def draw_citadel_tower(draw, cx, cy_base, height, width, color_idx=6, alpha=180):
    """Tour citadelle gothique avec spire."""
    color = VOSS[color_idx % 8]
    rgba = color + (alpha,)
    half_w = width / 2
    cy_top = cy_base - height
    draw.rectangle([cx-half_w, cy_top + height*0.2,
                    cx+half_w, cy_base], outline=rgba, width=2*SS)
    pts = [(cx-half_w, cy_top + height*0.25), (cx, cy_top),
           (cx+half_w, cy_top + height*0.25)]
    draw.polygon(pts, outline=rgba, width=2*SS)
    for i in range(1, 6):
        y = cy_base - (height * 0.7) * i / 6
        draw.line([(cx-half_w, y), (cx+half_w, y)],
                  fill=color + (alpha//2,), width=SS)


def draw_pyramid(draw, cx, cy_base, size, color_idx=2, alpha=170):
    """Pyramide 3D wireframe."""
    color = VOSS[color_idx % 8]
    rgba = color + (alpha,)
    s = size / 2
    cy_top = cy_base - size
    base_pts = [
        (cx-s, cy_base + s*0.2),
        (cx+s, cy_base + s*0.2),
        (cx+s*1.2, cy_base - s*0.1),
        (cx-s*1.2, cy_base - s*0.1)
    ]
    for p in base_pts:
        draw.line([p, (cx, cy_top)], fill=rgba, width=2*SS)
    for i in range(4):
        draw.line([base_pts[i], base_pts[(i+1)%4]], fill=rgba, width=2*SS)
    for i in range(1, 5):
        f = i / 5
        s_at = s * (1 - f)
        y_at = cy_base - size * f
        draw.line([(cx-s_at, y_at), (cx+s_at, y_at)],
                  fill=color + (alpha//2,), width=SS)


def draw_death_star(draw, cx, cy, radius, color_idx=0, alpha=180):
    """Sphere segmentee Death Star avec equateur + super-rayon."""
    color = VOSS[color_idx % 8]
    rgba = color + (alpha,)
    draw.ellipse([cx-radius, cy-radius, cx+radius, cy+radius],
                 outline=rgba, width=3*SS)
    draw.line([(cx-radius, cy), (cx+radius, cy)], fill=rgba, width=3*SS)
    for i in range(1, 4):
        r = radius * (1 - i*0.2)
        draw.arc([cx-r, cy-radius, cx+r, cy+radius-r*0.3],
                 0, 180, fill=color + (alpha//2,), width=SS)
    # Super-rayon
    super_r = radius * 0.18
    super_x = cx - radius * 0.45
    super_y = cy - radius * 0.4
    draw.ellipse([super_x-super_r, super_y-super_r,
                  super_x+super_r, super_y+super_r],
                 outline=rgba, width=2*SS)


def draw_orbital_station(draw, cx, cy, size, color_idx=4, alpha=170):
    """Station orbitale : croix + 4 modules + anneau central."""
    color = VOSS[color_idx % 8]
    rgba = color + (alpha,)
    draw.line([(cx-size, cy), (cx+size, cy)], fill=rgba, width=2*SS)
    draw.line([(cx, cy-size), (cx, cy+size)], fill=rgba, width=2*SS)
    mod_r = size * 0.2
    for dx, dy in [(-size, 0), (size, 0), (0, -size), (0, size)]:
        draw.ellipse([cx+dx-mod_r, cy+dy-mod_r, cx+dx+mod_r, cy+dy+mod_r],
                     outline=rgba, width=2*SS)
    draw.ellipse([cx-size*0.4, cy-size*0.4, cx+size*0.4, cy+size*0.4],
                 outline=rgba, width=2*SS)


def draw_orbital_cylinder(draw, cx, cy, length, radius, angle=0,
                           color_idx=2, alpha=160):
    """Cylindre orbital style Rama (cylindre 3D wireframe)."""
    color = VOSS[color_idx % 8]
    rgba = color + (alpha,)
    cos_a, sin_a = math.cos(angle), math.sin(angle)
    x1 = cx - cos_a * length / 2
    y1 = cy - sin_a * length / 2
    x2 = cx + cos_a * length / 2
    y2 = cy + sin_a * length / 2
    perp_x, perp_y = -sin_a, cos_a
    for offset_f in [-0.8, -0.4, 0, 0.4, 0.8]:
        offset = radius * offset_f
        x_a = x1 + perp_x * offset
        y_a = y1 + perp_y * offset
        x_b = x2 + perp_x * offset
        y_b = y2 + perp_y * offset
        draw.line([(x_a, y_a), (x_b, y_b)], fill=rgba, width=SS)
    for ex, ey in [(x1, y1), (x2, y2)]:
        draw.ellipse([ex-radius*0.3, ey-radius, ex+radius*0.3, ey+radius],
                     outline=rgba, width=SS)


# ============================================================
# Dispatcher + populate background
# ============================================================

TYPES = ['cube', 'tower', 'cylinder', 'pyramid', 'death_star', 'station', 'small_dyson']


def place_random_structure(draw, type_name, x, y, sz, color_idx, alpha):
    if type_name == 'cube':
        draw_cube_borg(draw, x, y, sz, color_idx=color_idx, alpha=alpha)
    elif type_name == 'tower':
        draw_citadel_tower(draw, x, y, sz*1.5, sz*0.5,
                           color_idx=color_idx, alpha=alpha)
    elif type_name == 'cylinder':
        draw_orbital_cylinder(draw, x, y, sz*2.5, sz*0.4,
                              angle=random.uniform(0, math.pi),
                              color_idx=color_idx, alpha=alpha)
    elif type_name == 'pyramid':
        draw_pyramid(draw, x, y, sz, color_idx=color_idx, alpha=alpha)
    elif type_name == 'death_star':
        draw_death_star(draw, x, y, sz, color_idx=color_idx, alpha=alpha)
    elif type_name == 'station':
        draw_orbital_station(draw, x, y, sz, color_idx=color_idx, alpha=alpha)
    elif type_name == 'small_dyson':
        draw_dyson_sphere(draw, x, y, sz, color_idx=color_idx, alpha=alpha)


def populate_background(draw, n=15, seed=42, exclude_center=True):
    """Disperse n structures aleatoires dans le canvas."""
    random.seed(seed)
    for _ in range(n):
        type_name = random.choice(TYPES)
        x = random.randint(int(DRAW*0.05), int(DRAW*0.95))
        y = random.randint(int(DRAW*0.05), int(DRAW*0.95))
        # Optionnel : eviter le centre pour pas surcharger le focal
        if exclude_center:
            dist_from_center = math.hypot(x - CX, y - CY)
            if dist_from_center < DRAW * 0.15:
                # Repousse vers l'exterieur
                if dist_from_center == 0:
                    x = int(DRAW * 0.7)
                else:
                    factor = (DRAW * 0.15) / dist_from_center
                    x = int(CX + (x - CX) * factor)
                    y = int(CY + (y - CY) * factor)
        sz = random.randint(int(DRAW*0.04), int(DRAW*0.10))
        alpha = random.randint(60, 130)
        color_idx = random.randint(0, 7)
        place_random_structure(draw, type_name, x, y, sz, color_idx, alpha)


def draw_central_core(draw, cx, cy):
    """Coeur lumineux central (point de fuite)."""
    r = int(DRAW * 0.025)
    draw.ellipse([cx-r, cy-r, cx+r, cy+r], fill=VOSS[7] + (240,))
    # Halo subtil
    r2 = int(DRAW * 0.05)
    draw.ellipse([cx-r2, cy-r2, cx+r2, cy+r2],
                 outline=VOSS[7] + (80,), width=2*SS)


# ============================================================
# 4 variantes avec structures dominantes differentes
# ============================================================

def make_variant_A():
    """DYSON SPHERE - sphere geante centrale + ringworld + 15 background."""
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    d = ImageDraw.Draw(img, 'RGBA')
    populate_background(d, n=15, seed=42)
    # Foreground principal
    draw_ringworld(d, CX, int(DRAW*0.25), int(DRAW*0.42), color_idx=5, alpha=220)
    draw_dyson_sphere(d, CX, CY, int(DRAW*0.22), color_idx=3, alpha=220)
    draw_central_core(d, CX, CY)
    return img.resize((SIZE, SIZE), Image.LANCZOS)


def make_variant_B():
    """HALO RINGWORLD - 2 anneaux geants + cylindres orbitaux + 15 background."""
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    d = ImageDraw.Draw(img, 'RGBA')
    populate_background(d, n=15, seed=84)
    # 2 ringworlds entrelacés
    draw_ringworld(d, CX, int(DRAW*0.30), int(DRAW*0.45), color_idx=4, alpha=220)
    draw_ringworld(d, CX, int(DRAW*0.55), int(DRAW*0.40), color_idx=6, alpha=220)
    # 3 cylindres orbitaux geants
    for i, (offx, offy, ang) in enumerate([
        (-0.2, 0.0, math.pi/4),
        (0.25, -0.1, -math.pi/3),
        (0.0, 0.2, math.pi/6),
    ]):
        draw_orbital_cylinder(d, int(CX + offx*DRAW), int(CY + offy*DRAW),
                              int(DRAW*0.25), int(DRAW*0.04), angle=ang,
                              color_idx=2 + i, alpha=200)
    draw_central_core(d, CX, CY)
    return img.resize((SIZE, SIZE), Image.LANCZOS)


def make_variant_C():
    """CITADELLES GOTHIQUES - cluster de tours + pyramides + 15 background."""
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    d = ImageDraw.Draw(img, 'RGBA')
    populate_background(d, n=15, seed=126)
    # 5 grandes tours citadelles
    tower_positions = [
        (0.20, 0.85, 0.35, 6),
        (0.40, 0.85, 0.30, 1),
        (0.55, 0.80, 0.40, 2),
        (0.70, 0.85, 0.32, 5),
        (0.85, 0.80, 0.28, 7),
    ]
    for x_f, y_f, h_f, color_idx in tower_positions:
        h = int(DRAW * h_f)
        draw_citadel_tower(d, int(DRAW * x_f), int(DRAW * y_f),
                           h, h*0.18, color_idx=color_idx, alpha=210)
    # 2 grandes pyramides
    draw_pyramid(d, int(DRAW*0.30), int(DRAW*0.45), int(DRAW*0.15),
                 color_idx=2, alpha=200)
    draw_pyramid(d, int(DRAW*0.75), int(DRAW*0.40), int(DRAW*0.18),
                 color_idx=6, alpha=200)
    draw_central_core(d, CX, CY)
    return img.resize((SIZE, SIZE), Image.LANCZOS)


def make_variant_D():
    """DEATH STAR FLEET - cluster de Death Stars + cubes Borg + 15 background."""
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    d = ImageDraw.Draw(img, 'RGBA')
    populate_background(d, n=15, seed=168)
    # 4 Death Stars de tailles variees
    death_star_positions = [
        (CX, CY, int(DRAW*0.18), 0),
        (int(DRAW*0.25), int(DRAW*0.30), int(DRAW*0.10), 1),
        (int(DRAW*0.75), int(DRAW*0.70), int(DRAW*0.12), 2),
        (int(DRAW*0.20), int(DRAW*0.75), int(DRAW*0.08), 5),
    ]
    for x, y, r, color_idx in death_star_positions:
        draw_death_star(d, x, y, r, color_idx=color_idx, alpha=210)
    # 3 cubes Borg geants
    cube_positions = [
        (int(DRAW*0.80), int(DRAW*0.20), int(DRAW*0.16), 4),
        (int(DRAW*0.85), int(DRAW*0.85), int(DRAW*0.13), 6),
        (int(DRAW*0.15), int(DRAW*0.50), int(DRAW*0.11), 3),
    ]
    for x, y, sz, color_idx in cube_positions:
        draw_cube_borg(d, x, y, sz, color_idx=color_idx, alpha=210)
    draw_central_core(d, CX, CY)
    return img.resize((SIZE, SIZE), Image.LANCZOS)


# ============================================================
# Mockup 3D
# ============================================================

def make_mockup_3d(tile, output_path):
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


# ============================================================
# MAIN
# ============================================================

if __name__ == "__main__":
    os.makedirs(OUT_DIR, exist_ok=True)

    variants = {
        "a": ("DYSON SPHERE + RINGWORLD", make_variant_A),
        "b": ("HALO RINGWORLDS + CYLINDRES", make_variant_B),
        "c": ("CITADELLES GOTHIQUES + PYRAMIDES", make_variant_C),
        "d": ("DEATH STAR FLEET + CUBES BORG", make_variant_D),
    }

    print("=== Generation 4 textures tunnel MEGASTRUCTURES ===\n")

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
