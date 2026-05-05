#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Genere les 4 variantes de textures tunnel pour le Cartouche Manifold
(Stage 4 Hyperspace) - VERSION DMT VISUAL ULTIME.

Concept : trip DMT geometrico-organique avec OEIL/IRIS COSMIQUE central,
mandala perspective qui aspire, tendrils psychic vivants, palette DMT
ultra-saturee (Alex Grey / Android Jones aesthetic).

Architecture :
  - 4 variantes : forme dominante differente (PAS QUE DES RONDS)
  - Style identique : iris central + petales radiaux + tendrils + glow
  - 1024x1024, fond transparent

4 variantes (formes geometriques de base) :
  A : ROND (cercles)          - mandala circulaire classique DMT
  B : HEXAGONAL               - iris hexagonal, petales hex, palette cyan/vert
  C : TRIANGULAIRE/ETOILE     - etoile de David + triangles aigus, palette violet/rose
  D : MIXED EXPLOSION         - mix ronds + hex + triangles + carres (PEAK)

Couleurs DMT iconiques :
  - Magenta saturé, Cyan electrique, Or pur
  - Vert acide, Violet, Rose neon, Blanc highlight

Le shader Java existant (renderHyperspace3D + 3 couches parallax 2D
+ crossfade 50% + acceleration progressive) anime ces textures.

Pour regenerer : python3 scripts-tools/generate_tunnel_v2.py
"""
import os
import math
from PIL import Image, ImageDraw, ImageFilter

SIZE = 1024
SS = 2
DRAW = SIZE * SS
CX = CY = DRAW // 2
OUT_DIR = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui/manifold"

# === PALETTE DMT ULTIME ===
DMT = {
    'magenta':    (255, 0, 200),
    'magenta_d':  (180, 0, 140),
    'cyan':       (0, 240, 255),
    'cyan_d':     (0, 160, 200),
    'or':         (255, 200, 0),
    'or_d':       (200, 140, 0),
    'vert':       (100, 255, 80),
    'vert_d':     (60, 180, 50),
    'violet':     (160, 30, 255),
    'violet_d':   (100, 20, 180),
    'rose':       (255, 100, 200),
    'blanc':      (255, 255, 255),
    'noir':       (10, 0, 20),
}


# ============================================================
# Helpers geometriques
# ============================================================

def regular_polygon(cx, cy, radius, n, rotation=0):
    return [(cx + math.cos(rotation + 2*math.pi*i/n) * radius,
             cy + math.sin(rotation + 2*math.pi*i/n) * radius)
            for i in range(n)]


def draw_polygon_layer(draw, cx, cy, radius, n_sides, rotation,
                       color, alpha=255, fill=True, width=4):
    """Dessine un polygone regulier (filled ou outline)."""
    pts = regular_polygon(cx, cy, radius, n_sides, rotation)
    rgba = color + (alpha,)
    if fill:
        draw.polygon(pts, fill=rgba)
    else:
        draw.polygon(pts, outline=rgba, width=width*SS)


# ============================================================
# Iris cosmique (central) - parametrable par n_sides
# ============================================================

def draw_psychic_iris(draw, cx, cy, max_r, n_sides=0, rotation=0):
    """
    Iris cosmique central : 13 couches concentriques avec gradient + motifs.

    n_sides : 0 = cercles ronds, 3 = triangles, 6 = hexagones, etc.
    """
    layers = [
        (1.00, DMT['magenta'],   200, 'fill'),
        (0.95, DMT['magenta_d'], 230, 'ring'),
        (0.85, DMT['or'],        180, 'fill'),
        (0.80, DMT['or_d'],      255, 'ring'),
        (0.70, DMT['cyan'],      200, 'fill'),
        (0.65, DMT['cyan_d'],    255, 'ring'),
        (0.55, DMT['violet'],    220, 'fill'),
        (0.45, DMT['rose'],      230, 'fill'),
        (0.35, DMT['vert'],      240, 'fill'),
        (0.25, DMT['or'],        250, 'fill'),
        (0.15, DMT['cyan'],      255, 'fill'),
        (0.08, DMT['blanc'],     255, 'fill'),
        (0.04, DMT['noir'],      255, 'fill'),  # pupille
    ]
    for i, (r_f, color, alpha, t) in enumerate(layers):
        r = int(max_r * r_f)
        # Rotation alternée par couche pour effet de "swirl"
        rot = rotation + i * math.pi / 24
        if n_sides == 0:
            # Cercle classique
            if t == 'fill':
                draw.ellipse([cx-r, cy-r, cx+r, cy+r], fill=color + (alpha,))
            else:
                draw.ellipse([cx-r, cy-r, cx+r, cy+r],
                             outline=color + (alpha,), width=4*SS)
        else:
            # Polygone
            draw_polygon_layer(draw, cx, cy, r, n_sides, rot, color, alpha,
                               fill=(t == 'fill'), width=4)


# ============================================================
# Petales radiaux (geometrie + organique)
# ============================================================

def draw_radial_petals(draw, cx, cy, r_in, r_out, n_petals, color,
                        alpha=200, rotation=0, shape='petal'):
    """
    Petales radials qui pulsent vers l'exterieur.

    shape : 'petal' (forme petale courbe), 'triangle' (aigu pointu),
            'rectangle' (rayons droits), 'diamond' (losange)
    """
    rgba = color + (alpha,)
    for i in range(n_petals):
        a = rotation + i * 2 * math.pi / n_petals
        a_next = rotation + (i + 1) * 2 * math.pi / n_petals
        if shape == 'petal':
            # Forme petale courbe (mid-point bombe)
            pts = []
            for t in [0, 0.5, 1]:
                r_at = r_in + (r_out - r_in) * (1 - abs(t - 0.5) * 2)
                offset_a = (t - 0.5) * (a_next - a) * 0.7
                pts.append((cx + math.cos(a + offset_a) * r_at,
                            cy + math.sin(a + offset_a) * r_at))
            pts.append((cx + math.cos(a) * r_in, cy + math.sin(a) * r_in))
            draw.polygon(pts, fill=rgba)
        elif shape == 'triangle':
            # Triangle aigu pointant vers l'exterieur
            mid = (a + a_next) / 2
            tip = (cx + math.cos(mid) * r_out, cy + math.sin(mid) * r_out)
            base1 = (cx + math.cos(a) * r_in, cy + math.sin(a) * r_in)
            base2 = (cx + math.cos(a_next) * r_in, cy + math.sin(a_next) * r_in)
            draw.polygon([base1, tip, base2], fill=rgba)
        elif shape == 'rectangle':
            # Rayon rectangulaire droit (baton de lumiere)
            mid = (a + a_next) / 2
            half_width = (a_next - a) * 0.3
            pts = [
                (cx + math.cos(mid - half_width) * r_in,
                 cy + math.sin(mid - half_width) * r_in),
                (cx + math.cos(mid - half_width) * r_out,
                 cy + math.sin(mid - half_width) * r_out),
                (cx + math.cos(mid + half_width) * r_out,
                 cy + math.sin(mid + half_width) * r_out),
                (cx + math.cos(mid + half_width) * r_in,
                 cy + math.sin(mid + half_width) * r_in),
            ]
            draw.polygon(pts, fill=rgba)
        elif shape == 'diamond':
            # Losange aux pointes interieur/exterieur
            mid = (a + a_next) / 2
            r_mid = (r_in + r_out) / 2
            tip_in = (cx + math.cos(mid) * r_in, cy + math.sin(mid) * r_in)
            tip_out = (cx + math.cos(mid) * r_out, cy + math.sin(mid) * r_out)
            side1 = (cx + math.cos(a) * r_mid, cy + math.sin(a) * r_mid)
            side2 = (cx + math.cos(a_next) * r_mid, cy + math.sin(a_next) * r_mid)
            draw.polygon([tip_in, side1, tip_out, side2], fill=rgba)


def draw_concentric_mandala_rings(draw, cx, cy, max_r, palette_seq,
                                    n_rings=6, shape='petal'):
    """Anneaux de mandala concentriques type tunnel perspective."""
    for i in range(n_rings):
        f = i / n_rings
        r_out = max_r * (1 - f * 0.85)
        r_in = max_r * (1 - (f + 1) / (n_rings + 1) * 0.85)
        n_petals = 24 + i * 6
        rotation = i * math.pi / 12
        color = palette_seq[i % len(palette_seq)]
        alpha = int(180 - i * 15)
        draw_radial_petals(draw, cx, cy, r_in, r_out, n_petals, color, alpha,
                           rotation, shape=shape)


# ============================================================
# Tendrils organiques (psy curves)
# ============================================================

def draw_psy_tendrils(draw, cx, cy, max_r, n_tendrils=16, palette=None):
    """Courbes organiques qui ondulent du centre vers l'exterieur."""
    if palette is None:
        palette = [DMT['vert'], DMT['cyan'], DMT['rose'], DMT['or']]
    for i in range(n_tendrils):
        base_angle = i * 2 * math.pi / n_tendrils
        n_pts = 50
        pts = []
        for j in range(n_pts):
            t = j / (n_pts - 1)
            r = max_r * 0.15 + t * max_r * 0.85
            wobble = math.sin(t * math.pi * 4 + i * 0.5) * 0.15
            angle = base_angle + wobble + t * math.pi * 0.3
            pts.append((cx + math.cos(angle) * r, cy + math.sin(angle) * r))
        color = palette[i % len(palette)]
        for k in range(len(pts) - 1):
            alpha = int(80 + (k / len(pts)) * 100)
            draw.line([pts[k], pts[k+1]], fill=color + (alpha,), width=3*SS)


# ============================================================
# Sacred geometry overlays (formes specifiques aux variantes)
# ============================================================

def draw_flower_of_life(draw, cx, cy, base_r, color, alpha=180):
    """7 cercles imbriques (fleur de vie)."""
    rgba = color + (alpha,)
    centers = [(cx, cy)]
    for i in range(6):
        a = i * math.pi / 3
        centers.append((cx + math.cos(a) * base_r, cy + math.sin(a) * base_r))
    for ccx, ccy in centers:
        draw.ellipse([ccx-base_r, ccy-base_r, ccx+base_r, ccy+base_r],
                     outline=rgba, width=3*SS)


def draw_hexagonal_lattice(draw, cx, cy, max_r, color, alpha=160):
    """Reseau hexagonal sacre."""
    rgba = color + (alpha,)
    for i in range(3):
        r = max_r * (0.30 + i * 0.20)
        pts = regular_polygon(cx, cy, r, 6, i * math.pi/12)
        draw.polygon(pts, outline=rgba, width=3*SS)


def draw_star_of_david(draw, cx, cy, max_r, color, alpha=200):
    """2 triangles superposes."""
    rgba = color + (alpha,)
    pts1 = regular_polygon(cx, cy, max_r, 3, 0)
    pts2 = regular_polygon(cx, cy, max_r, 3, math.pi/3)
    draw.polygon(pts1, outline=rgba, width=4*SS)
    draw.polygon(pts2, outline=rgba, width=4*SS)


def draw_octagram(draw, cx, cy, max_r, color, alpha=200):
    """Etoile a 8 branches."""
    rgba = color + (alpha,)
    pts1 = regular_polygon(cx, cy, max_r, 4, 0)
    pts2 = regular_polygon(cx, cy, max_r, 4, math.pi/4)
    draw.polygon(pts1, outline=rgba, width=4*SS)
    draw.polygon(pts2, outline=rgba, width=4*SS)


# ============================================================
# 4 variantes DMT - formes differentes
# ============================================================

def make_variant_A():
    """ROND - mandala circulaire DMT classique (oeil cosmique)."""
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))

    # COUCHE 1 : Background mandala diffus avec glow
    bg = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    bg_d = ImageDraw.Draw(bg, 'RGBA')
    palette = [DMT['magenta'], DMT['or'], DMT['cyan'],
               DMT['violet'], DMT['rose'], DMT['vert']]
    draw_concentric_mandala_rings(bg_d, CX, CY, DRAW * 0.48, palette,
                                   n_rings=6, shape='petal')
    bg_glow = bg.filter(ImageFilter.GaussianBlur(radius=40))
    img = Image.alpha_composite(img, bg_glow)
    img = Image.alpha_composite(img, bg)

    # COUCHE 2 : Tendrils organiques
    tendrils = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    t_d = ImageDraw.Draw(tendrils, 'RGBA')
    draw_psy_tendrils(t_d, CX, CY, DRAW * 0.48)
    img = Image.alpha_composite(img, tendrils)

    # COUCHE 3 : Sacred overlay (fleur de vie)
    sacred = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    s_d = ImageDraw.Draw(sacred, 'RGBA')
    draw_flower_of_life(s_d, CX, CY, DRAW * 0.13, DMT['or'], 160)
    img = Image.alpha_composite(img, sacred)

    # COUCHE 4 : Iris central rond + glow
    iris = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    iris_d = ImageDraw.Draw(iris, 'RGBA')
    draw_psychic_iris(iris_d, CX, CY, DRAW * 0.20, n_sides=0)
    iris_glow = iris.filter(ImageFilter.GaussianBlur(radius=25))
    img = Image.alpha_composite(img, iris_glow)
    img = Image.alpha_composite(img, iris)

    return img.resize((SIZE, SIZE), Image.LANCZOS)


def make_variant_B():
    """HEXAGONAL - iris hex, petales triangulaires, palette cyan/vert."""
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))

    # COUCHE 1 : Background avec petales triangulaires
    bg = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    bg_d = ImageDraw.Draw(bg, 'RGBA')
    palette = [DMT['cyan'], DMT['vert'], DMT['or'],
               DMT['cyan_d'], DMT['vert_d'], DMT['blanc']]
    draw_concentric_mandala_rings(bg_d, CX, CY, DRAW * 0.48, palette,
                                   n_rings=6, shape='triangle')
    bg_glow = bg.filter(ImageFilter.GaussianBlur(radius=40))
    img = Image.alpha_composite(img, bg_glow)
    img = Image.alpha_composite(img, bg)

    # COUCHE 2 : Tendrils
    tendrils = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    t_d = ImageDraw.Draw(tendrils, 'RGBA')
    draw_psy_tendrils(t_d, CX, CY, DRAW * 0.48,
                      palette=[DMT['cyan'], DMT['vert'], DMT['or']])
    img = Image.alpha_composite(img, tendrils)

    # COUCHE 3 : Reseau hexagonal sacre
    sacred = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    s_d = ImageDraw.Draw(sacred, 'RGBA')
    draw_hexagonal_lattice(s_d, CX, CY, DRAW * 0.45, DMT['cyan_d'], 180)
    img = Image.alpha_composite(img, sacred)

    # COUCHE 4 : Iris HEXAGONAL central + glow
    iris = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    iris_d = ImageDraw.Draw(iris, 'RGBA')
    draw_psychic_iris(iris_d, CX, CY, DRAW * 0.20, n_sides=6)
    iris_glow = iris.filter(ImageFilter.GaussianBlur(radius=25))
    img = Image.alpha_composite(img, iris_glow)
    img = Image.alpha_composite(img, iris)

    return img.resize((SIZE, SIZE), Image.LANCZOS)


def make_variant_C():
    """TRIANGULAIRE - etoile David, petales diamants, palette violet/rose."""
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))

    # COUCHE 1 : Background avec rayons diamants
    bg = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    bg_d = ImageDraw.Draw(bg, 'RGBA')
    palette = [DMT['violet'], DMT['rose'], DMT['or'],
               DMT['magenta'], DMT['violet_d'], DMT['blanc']]
    draw_concentric_mandala_rings(bg_d, CX, CY, DRAW * 0.48, palette,
                                   n_rings=6, shape='diamond')
    bg_glow = bg.filter(ImageFilter.GaussianBlur(radius=40))
    img = Image.alpha_composite(img, bg_glow)
    img = Image.alpha_composite(img, bg)

    # COUCHE 2 : Tendrils
    tendrils = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    t_d = ImageDraw.Draw(tendrils, 'RGBA')
    draw_psy_tendrils(t_d, CX, CY, DRAW * 0.48,
                      palette=[DMT['violet'], DMT['rose'], DMT['or'], DMT['magenta']])
    img = Image.alpha_composite(img, tendrils)

    # COUCHE 3 : 3 etoiles de David imbriquees
    sacred = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    s_d = ImageDraw.Draw(sacred, 'RGBA')
    for i, scale in enumerate([0.45, 0.30, 0.18]):
        color = [DMT['or'], DMT['rose'], DMT['blanc']][i]
        draw_star_of_david(s_d, CX, CY, DRAW * scale, color, 200 - i*40)
    img = Image.alpha_composite(img, sacred)

    # COUCHE 4 : Iris TRIANGULAIRE central (3 cotes) + glow
    iris = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    iris_d = ImageDraw.Draw(iris, 'RGBA')
    draw_psychic_iris(iris_d, CX, CY, DRAW * 0.20, n_sides=3)
    iris_glow = iris.filter(ImageFilter.GaussianBlur(radius=25))
    img = Image.alpha_composite(img, iris_glow)
    img = Image.alpha_composite(img, iris)

    return img.resize((SIZE, SIZE), Image.LANCZOS)


def make_variant_D():
    """MIXED EXPLOSION - mix de toutes formes (PEAK)."""
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))

    # COUCHE 1 : Background petales rectangles (rayons droits = energie)
    bg = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    bg_d = ImageDraw.Draw(bg, 'RGBA')
    palette = [DMT['magenta'], DMT['cyan'], DMT['or'], DMT['violet'],
               DMT['rose'], DMT['vert'], DMT['blanc']]
    draw_concentric_mandala_rings(bg_d, CX, CY, DRAW * 0.48, palette,
                                   n_rings=8, shape='rectangle')
    bg_glow = bg.filter(ImageFilter.GaussianBlur(radius=50))
    img = Image.alpha_composite(img, bg_glow)
    img = Image.alpha_composite(img, bg)

    # COUCHE 2 : Tendrils intenses
    tendrils = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    t_d = ImageDraw.Draw(tendrils, 'RGBA')
    draw_psy_tendrils(t_d, CX, CY, DRAW * 0.48, n_tendrils=24,
                      palette=[DMT['magenta'], DMT['cyan'], DMT['or'],
                               DMT['vert'], DMT['rose']])
    img = Image.alpha_composite(img, tendrils)

    # COUCHE 3 : MULTIPLES sacred overlays
    sacred = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    s_d = ImageDraw.Draw(sacred, 'RGBA')
    draw_octagram(s_d, CX, CY, DRAW * 0.42, DMT['cyan'], 180)
    draw_star_of_david(s_d, CX, CY, DRAW * 0.32, DMT['rose'], 160)
    draw_hexagonal_lattice(s_d, CX, CY, DRAW * 0.40, DMT['or'], 160)
    draw_flower_of_life(s_d, CX, CY, DRAW * 0.10, DMT['blanc'], 200)
    img = Image.alpha_composite(img, sacred)

    # COUCHE 4 : Iris OCTOGONAL central (8 cotes) + double glow
    iris = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    iris_d = ImageDraw.Draw(iris, 'RGBA')
    draw_psychic_iris(iris_d, CX, CY, DRAW * 0.22, n_sides=8)
    iris_glow1 = iris.filter(ImageFilter.GaussianBlur(radius=15))
    iris_glow2 = iris.filter(ImageFilter.GaussianBlur(radius=40))
    img = Image.alpha_composite(img, iris_glow2)
    img = Image.alpha_composite(img, iris_glow1)
    img = Image.alpha_composite(img, iris)

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
        "a": ("ROND - mandala circulaire DMT", make_variant_A),
        "b": ("HEXAGONAL - cyan/vert", make_variant_B),
        "c": ("TRIANGULAIRE - violet/rose", make_variant_C),
        "d": ("MIXED EXPLOSION - PEAK", make_variant_D),
    }

    print("=== Generation 4 textures tunnel DMT ULTIME ===\n")

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
