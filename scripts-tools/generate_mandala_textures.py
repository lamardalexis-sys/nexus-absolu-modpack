#!/usr/bin/env python3
"""
Genere 16 textures fractales mandala 1024x1024 ULTRA SATURE pour overlay DMT.

V2 (Etape 2 visuel ultime) :
- 1024x1024 (vs 512) avec supersampling 2x puis downscale Lanczos
  -> sub-pixel anti-aliasing, contours lisses meme en mouvement rapide
- 16 frames (vs 4) -> cycle morphing 80s plein de variations sans repetition
- Couches additionnelles :
  * sub-mandalas niveau 3 (sub-mandalas dans les sub-mandalas)
  * geometric arcs (arcs partiels au lieu de cercles complets)
  * lotus deep (lotus layers superposees avec offset angulaire progressif)
- Geometrie sacree multi-densite (hexagones, octogones, dodecagones imbriques)
- Symetrie radiale variee 6/8/10/12/14/16x

Output : assets/nexusabsolu/textures/gui/manifold/mandala_1..16.png

Pour regenerer : python3 scripts-tools/generate_mandala_textures.py
Temps estime : ~2-3 min (PIL CPU-only).
"""
import os
import math
from PIL import Image, ImageDraw

SIZE = 1024
SS = 2  # supersampling factor (draw at 2x then downscale)
DRAW_SIZE = SIZE * SS
DRAW_CENTER = DRAW_SIZE // 2

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


# ============================================================
# Helpers geometriques (tous prennent cx/cy explicites)
# ============================================================

def regular_polygon(cx, cy, radius, n, rotation=0):
    return [
        (cx + math.cos(rotation + 2 * math.pi * i / n) * radius,
         cy + math.sin(rotation + 2 * math.pi * i / n) * radius)
        for i in range(n)
    ]


def draw_petal_polygon(draw, cx, cy, n_petals, r_in, r_out, rotation,
                        palette_offset, alpha=255):
    """Petales courbes formes d'un arc rentrant + arc sortant."""
    for i in range(n_petals):
        a1 = rotation + (i / n_petals) * 2 * math.pi
        a2 = rotation + ((i + 1) / n_petals) * 2 * math.pi
        pts = []
        steps = 32  # plus de steps pour le supersampling
        for s in range(steps + 1):
            f = s / steps
            a = a1 + (a2 - a1) * f
            mod = math.sin(f * math.pi)
            r = r_in + (r_out - r_in) * mod
            pts.append((cx + math.cos(a) * r, cy + math.sin(a) * r))
        for s in range(steps + 1):
            f = s / steps
            a = a2 - (a2 - a1) * f
            r = r_in
            pts.append((cx + math.cos(a) * r, cy + math.sin(a) * r))
        color = PALETTE[(i + palette_offset) % len(PALETTE)] + (alpha,)
        draw.polygon(pts, fill=color)


def draw_radiating_triangles(draw, cx, cy, n, r_in, r_out, rotation,
                              palette_offset, alpha=255):
    half_width = math.pi / n * 0.4
    for i in range(n):
        a = rotation + (i / n) * 2 * math.pi
        p1 = (cx + math.cos(a) * r_in, cy + math.sin(a) * r_in)
        p2 = (cx + math.cos(a - half_width) * r_out,
              cy + math.sin(a - half_width) * r_out)
        p3 = (cx + math.cos(a + half_width) * r_out,
              cy + math.sin(a + half_width) * r_out)
        color = PALETTE[(i + palette_offset) % len(PALETTE)] + (alpha,)
        draw.polygon([p1, p2, p3], fill=color)


def draw_polygon_outline(draw, cx, cy, n, radius, rotation, color, width):
    pts = regular_polygon(cx, cy, radius, n, rotation)
    pts.append(pts[0])
    for i in range(len(pts) - 1):
        draw.line([pts[i], pts[i + 1]], fill=color + (255,), width=width)


def draw_sacred_geometry(draw, cx, cy, max_r, palette_offset):
    """Polygones reguliers multi-densite imbriques."""
    for i, n in enumerate([6, 8, 10, 12, 14, 16]):
        for j in range(2):
            rotation = (j * math.pi / n) + i * 0.1
            radius = max_r * (0.25 + i * 0.13)
            color = PALETTE[(i + j + palette_offset) % len(PALETTE)]
            draw_polygon_outline(draw, cx, cy, n, radius, rotation, color, 4 * SS)


# ============================================================
# Sub-mandalas RECURSIFS (niveau 3)
# ============================================================

def draw_sub_mandala_atom(draw, cx, cy, size, palette_offset):
    """Niveau 3 (le plus petit) : etoile a 6 branches simple."""
    for j in range(6):
        a = (j / 6) * 2 * math.pi
        ex = cx + math.cos(a) * size
        ey = cy + math.sin(a) * size
        color = PALETTE[(j + palette_offset) % len(PALETTE)]
        r = size * 0.35
        draw.ellipse(
            [(ex - r, ey - r), (ex + r, ey + r)],
            fill=color + (255,)
        )
    # Coeur blanc
    cr = size * 0.25
    draw.ellipse(
        [(cx - cr, cy - cr), (cx + cr, cy + cr)],
        fill=(255, 255, 255, 255)
    )


def draw_sub_mandala_level2(draw, cx, cy, size, palette_offset):
    """Niveau 2 : 6 atomes en cercle + 1 centre."""
    for j in range(6):
        a = (j / 6) * 2 * math.pi
        ex = cx + math.cos(a) * size * 0.6
        ey = cy + math.sin(a) * size * 0.6
        draw_sub_mandala_atom(draw, ex, ey, size * 0.3, palette_offset + j)
    # Atome central
    draw_sub_mandala_atom(draw, cx, cy, size * 0.35, palette_offset + 4)


def draw_sub_mandalas_recursive(draw, cx_main, cy_main, ring_radius, count,
                                 palette_offset, level=2):
    """Niveau 1 : 'count' sub-mandalas (chacun de niveau 'level') sur un cercle."""
    for i in range(count):
        a = (i / count) * 2 * math.pi
        cx = cx_main + math.cos(a) * ring_radius
        cy = cy_main + math.sin(a) * ring_radius
        size = ring_radius * 0.18
        if level >= 2:
            draw_sub_mandala_level2(draw, cx, cy, size, palette_offset + i)
        else:
            draw_sub_mandala_atom(draw, cx, cy, size, palette_offset + i)


# ============================================================
# Geometric arcs (NEW dans v2)
# ============================================================

def draw_geometric_arcs(draw, cx, cy, max_r, n_arcs, palette_offset, alpha=220):
    """Arcs PARTIELS (pas cercles complets) -> effet 'brise' magnifique.

    Chaque 'ring' a un offset rotation different + un span angulaire variable
    (de 30 deg a 270 deg) -> les arcs ne se chevauchent pas exactement et
    creent un mouvement visuel.
    """
    for i in range(n_arcs, 0, -1):
        r = (i / n_arcs) * max_r
        # Offset rotation per ring -> arcs are staggered
        start_deg = (i * 23) % 360
        span_deg = 60 + (i * 47) % 210  # entre 60 et 270 deg
        end_deg = start_deg + span_deg
        color = PALETTE[(i + palette_offset) % len(PALETTE)] + (alpha,)
        bbox = [(cx - r, cy - r), (cx + r, cy + r)]
        draw.arc(bbox, start=start_deg, end=end_deg, fill=color, width=4 * SS)


def draw_concentric_arcs(draw, cx, cy, n_arcs, max_r, palette_offset, alpha=200):
    """Cercles complets concentriques (legacy)."""
    for i in range(n_arcs, 0, -1):
        r = (i / n_arcs) * max_r
        color = PALETTE[(i + palette_offset) % len(PALETTE)] + (alpha,)
        draw.ellipse(
            [(cx - r, cy - r), (cx + r, cy + r)],
            outline=color,
            width=3 * SS
        )


# ============================================================
# Lotus deep (NEW dans v2)
# ============================================================

def draw_lotus_deep(draw, cx, cy, max_r, n_layers, n_petals_base, palette_offset):
    """Lotus a N couches : chaque couche a un nombre de petales different
    (n_base, n_base*1.5, n_base*2, ...) et un offset angulaire progressif.

    Effet visuel : lotus 'qui s'ouvre' avec petales superposees a profondeur.
    """
    for layer in range(n_layers):
        # Plus on monte en profondeur, plus les petales sont fines et nombreuses
        n_petals = int(n_petals_base * (1 + layer * 0.5))
        if n_petals < 4:
            n_petals = 4
        layer_frac = layer / max(1, n_layers - 1)
        # r_in petit, r_out qui croit avec la couche
        r_in = max_r * (0.18 + layer_frac * 0.35)
        r_out = max_r * (0.45 + layer_frac * 0.50)
        # Offset angulaire pour decaler les couches entre elles
        rotation = (layer * math.pi / 13.0) + math.pi / (n_petals * 2)
        # Alpha decroissant avec la profondeur (couches profondes plus discretes)
        alpha = int(255 - layer * 30)
        if alpha < 100:
            alpha = 100
        draw_petal_polygon(draw, cx, cy, n_petals, r_in, r_out, rotation,
                           palette_offset + layer * 2, alpha=alpha)


# ============================================================
# Mandala complet
# ============================================================

def make_mandala(n_petals, n_rings, palette_offset, lotus_layers):
    """Genere 1 mandala 1024x1024 via supersampling 2x."""
    img = Image.new("RGBA", (DRAW_SIZE, DRAW_SIZE), (0, 0, 0, 255))
    draw = ImageDraw.Draw(img, "RGBA")

    cx = DRAW_CENTER
    cy = DRAW_CENTER
    max_r = DRAW_CENTER - 5 * SS

    # 1. Couche externe : petales principaux
    draw_petal_polygon(draw, cx, cy, n_petals, max_r * 0.55, max_r * 0.98,
                       rotation=0,
                       palette_offset=palette_offset, alpha=255)

    # 2. Couche moyenne : double densite de petales
    draw_petal_polygon(draw, cx, cy, n_petals * 2, max_r * 0.25, max_r * 0.55,
                       rotation=math.pi / (n_petals * 2),
                       palette_offset=palette_offset + 3, alpha=255)

    # 3. Triangles radiants (rayons de soleil)
    draw_radiating_triangles(draw, cx, cy, n_petals * 3,
                              max_r * 0.15, max_r * 0.95,
                              rotation=math.pi / (n_petals * 6),
                              palette_offset=palette_offset + 5, alpha=180)

    # 4. Geometrie sacree (polygones imbriques)
    draw_sacred_geometry(draw, cx, cy, max_r * 0.9, palette_offset + 1)

    # 5. NEW : Lotus deep (couches superposees de petales)
    draw_lotus_deep(draw, cx, cy, max_r * 0.6, lotus_layers, n_petals,
                    palette_offset + 4)

    # 6. NEW : Sub-mandalas RECURSIFS niveau 3
    draw_sub_mandalas_recursive(draw, cx, cy, max_r * 0.72, n_petals,
                                 palette_offset + 2, level=2)

    # 7. NEW : Geometric arcs (arcs partiels brises)
    draw_geometric_arcs(draw, cx, cy, max_r * 0.92, n_rings,
                        palette_offset + 1, alpha=210)

    # 8. Cercles concentriques (legacy, plus discret en alpha)
    draw_concentric_arcs(draw, cx, cy, n_rings // 2, max_r * 0.88,
                         palette_offset, alpha=140)

    # 9. Coeur central : disques concentriques + point blanc
    for i in range(8, 0, -1):
        r = max_r * 0.10 * (i / 8)
        color = PALETTE[(i + palette_offset + 4) % len(PALETTE)]
        draw.ellipse(
            [(cx - r, cy - r), (cx + r, cy + r)],
            fill=color + (255,)
        )

    cr = 4 * SS
    draw.ellipse(
        [(cx - cr, cy - cr), (cx + cr, cy + cr)],
        fill=(255, 255, 255, 255)
    )

    # Downscale supersample -> taille finale avec Lanczos (anti-aliasing)
    return img.resize((SIZE, SIZE), Image.LANCZOS)


# ============================================================
# 16 configs : 4 groupes de 4, varient n_petals et palette_offset
# ============================================================

configs = [
    # Group 1 : Petales fines (6-8) - frames 1-4
    {"n_petals": 6,  "n_rings": 16, "palette_offset": 0, "lotus_layers": 2},
    {"n_petals": 6,  "n_rings": 18, "palette_offset": 1, "lotus_layers": 3},
    {"n_petals": 8,  "n_rings": 14, "palette_offset": 2, "lotus_layers": 2},
    {"n_petals": 8,  "n_rings": 16, "palette_offset": 3, "lotus_layers": 3},
    # Group 2 : Petales moyennes (10-12) - frames 5-8
    {"n_petals": 10, "n_rings": 12, "palette_offset": 4, "lotus_layers": 2},
    {"n_petals": 10, "n_rings": 14, "palette_offset": 5, "lotus_layers": 3},
    {"n_petals": 12, "n_rings": 10, "palette_offset": 6, "lotus_layers": 3},
    {"n_petals": 12, "n_rings": 12, "palette_offset": 7, "lotus_layers": 3},
    # Group 3 : Petales denses (14-16) - frames 9-12
    {"n_petals": 14, "n_rings": 16, "palette_offset": 0, "lotus_layers": 3},
    {"n_petals": 14, "n_rings": 18, "palette_offset": 2, "lotus_layers": 4},
    {"n_petals": 16, "n_rings": 14, "palette_offset": 4, "lotus_layers": 3},
    {"n_petals": 16, "n_rings": 16, "palette_offset": 6, "lotus_layers": 4},
    # Group 4 : Hybride (lotus profond) - frames 13-16
    {"n_petals": 12, "n_rings": 20, "palette_offset": 1, "lotus_layers": 4},
    {"n_petals": 8,  "n_rings": 22, "palette_offset": 3, "lotus_layers": 4},
    {"n_petals": 16, "n_rings": 24, "palette_offset": 5, "lotus_layers": 4},
    {"n_petals": 10, "n_rings": 24, "palette_offset": 7, "lotus_layers": 4},
]

print(f"Generation de {len(configs)} mandalas {SIZE}x{SIZE} (supersampling {SS}x)")
print(f"Output : {OUT_DIR}")
print()

for i, cfg in enumerate(configs, 1):
    img = make_mandala(**cfg)
    out_path = os.path.join(OUT_DIR, f"mandala_{i}.png")
    img.save(out_path, "PNG", optimize=True)
    print(f"  [{i:2d}/{len(configs)}] {out_path} (petals={cfg['n_petals']}, "
          f"rings={cfg['n_rings']}, lotus={cfg['lotus_layers']})")

print()
print(f"{len(configs)} textures generees.")
