"""
v1.0.355 : Generation fresques 4K DMT v2 - style Alex Grey / Android Jones / Amaringo

Concept (selon refs uploadees par user) :
  - Densite MAXIMUM (tout l'ecran rempli, pas de zones plates)
  - Couleurs neon UV-light sur fond presque noir
  - Geometrie sacree (fleur de vie, hexagones, etoiles 6 branches)
  - Symetrie kaleidoscope (8-fold ou 12-fold)
  - Contours brillants (outline neon + glow)
  - Multi-echelles (details a toutes les distances)
  - Recursion fractale (mandala dans mandala dans mandala)

Pipeline :
  1. Hex lattice dense (motifs sur grille hexagonale qui couvre tout)
  2. Anneaux concentriques avec petales contours neon
  3. Fleur de vie overlay (sacred geometry)
  4. Mandala fractal central (3 niveaux recursifs)
  5. All-seeing eye central
  6. Postprocess : UV glow neon + saturation extreme

Usage :
  python scripts-tools/generate_fractal_fresques_v2.py [palette_idx 0-3]
"""

import os
import sys
import math
import time
import gc

try:
    from PIL import Image, ImageDraw, ImageFilter, ImageChops, ImageEnhance
    import numpy as np
except ImportError:
    print("ERREUR : pip install Pillow numpy")
    sys.exit(1)

SIZE = 4096
CX = SIZE // 2
CY = SIZE // 2

# === PALETTES NEON UV-LIGHT ===
# Chaque palette a 8 couleurs ultra-saturees + un fond presque noir
PALETTES = {
    0: {  # NEON MAGENTA
        "name": "neon_magenta",
        "colors": [
            (255, 0, 255),    # magenta pur
            (0, 255, 255),    # cyan pur
            (255, 20, 147),   # deep pink
            (138, 43, 226),   # blue violet
            (255, 215, 0),    # gold
            (50, 255, 50),    # lime acid
            (255, 105, 180),  # hot pink
            (75, 0, 200),     # deep violet
        ],
        "bg": (5, 0, 12),
        "outline": (255, 255, 255),
    },
    1: {  # NEON LIME-CYAN (Alex Grey style)
        "name": "neon_lime_cyan",
        "colors": [
            (50, 255, 50),    # lime acid
            (0, 255, 255),    # cyan pur
            (255, 215, 0),    # gold
            (138, 43, 226),   # blue violet
            (255, 0, 200),    # neon pink
            (0, 191, 255),    # sky blue
            (255, 100, 0),    # orange neon
            (200, 255, 0),    # yellow-green
        ],
        "bg": (0, 8, 5),
        "outline": (255, 255, 255),
    },
    2: {  # COSMIC PURPLE
        "name": "cosmic_purple",
        "colors": [
            (138, 43, 226),   # blue violet
            (255, 0, 255),    # magenta
            (0, 255, 200),    # turquoise
            (255, 20, 147),   # deep pink
            (148, 0, 211),    # purple
            (50, 255, 50),    # lime
            (255, 215, 0),    # gold
            (200, 0, 255),    # violet pur
        ],
        "bg": (8, 0, 16),
        "outline": (220, 220, 255),
    },
    3: {  # FIRE NEON
        "name": "fire_neon",
        "colors": [
            (255, 30, 0),     # red neon
            (255, 215, 0),    # gold
            (255, 0, 200),    # magenta
            (255, 100, 0),    # orange
            (138, 43, 226),   # violet
            (0, 255, 255),    # cyan
            (255, 255, 50),   # yellow
            (200, 0, 255),    # purple
        ],
        "bg": (12, 2, 0),
        "outline": (255, 240, 200),
    },
}


# === HELPERS ===

def smoothstep(t):
    return t * t * (3.0 - 2.0 * t)


def mix_color(c1, c2, t):
    """Interpolation lineaire entre 2 couleurs RGB."""
    return tuple(int(c1[i] * (1 - t) + c2[i] * t) for i in range(3))


def draw_polygon_outlined(draw, pts, fill_color, outline_color, outline_w=3):
    """Dessine un polygone rempli avec contour neon brillant."""
    draw.polygon(pts, fill=fill_color)
    # Outline en plusieurs passes pour effet glow
    draw.line(pts + [pts[0]], fill=outline_color, width=outline_w)


# === COUCHE 1 : HEX LATTICE DENSE ===

def add_hex_lattice(img, palette):
    """Grille hexagonale dense de petits motifs (etoiles 6 branches)
    qui couvre TOUT l'ecran pour avoir un fond ultra-detaille."""
    d = ImageDraw.Draw(img, "RGBA")
    
    hex_size = 110  # Distance entre centres hexagones
    hex_radius = 35  # Rayon des etoiles
    
    # Grille hexagonale : alterner les rangees
    rows = SIZE // hex_size + 2
    cols = SIZE // hex_size + 2
    
    for row in range(-1, rows):
        offset_x = (hex_size // 2) if row % 2 == 1 else 0
        for col in range(-1, cols):
            cx = col * hex_size + offset_x
            cy = row * hex_size
            
            # Couleur depend de la position (gradient radial depuis le centre)
            dx = cx - CX
            dy = cy - CY
            dist = math.sqrt(dx * dx + dy * dy)
            color_idx = int((dist / 200.0 + (col + row) / 7.0)) % len(palette["colors"])
            color = palette["colors"][color_idx]
            
            # Etoile 6 branches
            draw_six_pointed_star(d, cx, cy, hex_radius, color, palette["outline"])


def draw_six_pointed_star(d, cx, cy, radius, fill_color, outline_color):
    """Dessine une etoile a 6 branches (Hexagram)."""
    pts_outer = []
    pts_inner = []
    for i in range(6):
        angle = i * math.pi / 3 - math.pi / 2
        pts_outer.append((cx + math.cos(angle) * radius,
                          cy + math.sin(angle) * radius))
        angle_inner = angle + math.pi / 6
        pts_inner.append((cx + math.cos(angle_inner) * radius * 0.45,
                          cy + math.sin(angle_inner) * radius * 0.45))
    
    # Construire le polygone etoile (alterner outer/inner)
    pts = []
    for i in range(6):
        pts.append(pts_outer[i])
        pts.append(pts_inner[i])
    
    d.polygon(pts, fill=fill_color, outline=outline_color)


# === COUCHE 2 : ANNEAUX CONCENTRIQUES AVEC PETALES NEON ===

def add_concentric_petal_rings(img, palette):
    """Anneaux concentriques avec petales en amande, contours neon."""
    d = ImageDraw.Draw(img, "RGBA")
    
    n_rings = 14
    max_r = int(SIZE * 0.49)
    min_r = int(SIZE * 0.06)
    
    for ring in range(n_rings):
        # Logarithmique : plus de detail vers le centre
        ratio = ring / (n_rings - 1)
        r_outer = int(max_r * math.pow(min_r / max_r, ratio))
        r_inner = int(max_r * math.pow(min_r / max_r,
                                        min(1.0, ratio + 1.0 / (n_rings - 1))))
        
        # Plus de petales pour les anneaux exterieurs (densite constante)
        n_petals = max(24, int(2 * math.pi * r_outer / 80))
        rotation = (ring * 11) * math.pi / 180.0
        
        for petal in range(n_petals):
            a0 = (petal / n_petals) * 2 * math.pi + rotation
            a1 = ((petal + 1) / n_petals) * 2 * math.pi + rotation
            a_mid = (a0 + a1) / 2
            
            # Petale en forme d'amande pointue (plus DMT que rectangle)
            # 4 points : pointe ext, base droite, pointe int, base gauche
            r_mid = (r_outer + r_inner) / 2
            width_factor = 0.4
            
            pts = [
                (CX + math.cos(a_mid) * r_outer,
                 CY + math.sin(a_mid) * r_outer),  # pointe ext
                (CX + math.cos(a_mid + (a1 - a_mid) * width_factor) * r_mid,
                 CY + math.sin(a_mid + (a1 - a_mid) * width_factor) * r_mid),
                (CX + math.cos(a_mid) * r_inner,
                 CY + math.sin(a_mid) * r_inner),  # pointe int
                (CX + math.cos(a_mid - (a_mid - a0) * width_factor) * r_mid,
                 CY + math.sin(a_mid - (a_mid - a0) * width_factor) * r_mid),
            ]
            
            color = palette["colors"][(petal + ring * 3) % len(palette["colors"])]
            d.polygon(pts, fill=color, outline=palette["outline"])
    
    # Anneaux delimitateurs blancs fins pour effet "lignes neon"
    for ring in range(n_rings):
        ratio = ring / (n_rings - 1)
        r = int(max_r * math.pow(min_r / max_r, ratio))
        d.ellipse([CX - r, CY - r, CX + r, CY + r],
                  outline=palette["outline"], width=2)


# === COUCHE 3 : FLEUR DE VIE OVERLAY ===

def add_flower_of_life(img, palette):
    """Pattern Fleur de Vie : cercles entrelaces en grille hexagonale.
    v1.0.355b : radius 220 -> 380 (3x moins de ronds, plus subtil)"""
    d = ImageDraw.Draw(img, "RGBA")
    
    radius = 380  # ETAIT 220 - cercles plus grands = moins denses
    spacing = radius  # Hexagonal packing
    
    rows = SIZE // (int(spacing * math.sqrt(3) / 2)) + 2
    cols = SIZE // spacing + 2
    
    for row in range(-1, rows):
        offset_x = spacing // 2 if row % 2 == 1 else 0
        cy = int(row * spacing * math.sqrt(3) / 2)
        for col in range(-1, cols):
            cx = col * spacing + offset_x
            color_idx = (col + row * 2) % len(palette["colors"])
            color = palette["colors"][color_idx]
            # Cercle outlined (pas remplit) pour effet entrelace
            # v1.0.355b : line plus fine 4 -> 3 pour effet plus subtil
            d.ellipse([cx - radius, cy - radius, cx + radius, cy + radius],
                      outline=color, width=3)


# === COUCHE 4 : MANDALA FRACTAL CENTRAL (RECURSIF 3 NIVEAUX) ===

def draw_fractal_mandala(d, palette, cx, cy, radius, level, rotation_offset=0):
    """Mandala recursif : a chaque niveau on dessine un mandala plus petit
    au centre. Niveau 0 = base, level 1 = mandala dans le centre, etc."""
    if radius < 30:
        return
    
    # Cercle de fond avec contour
    color_bg = palette["colors"][level % len(palette["colors"])]
    d.ellipse([cx - radius, cy - radius, cx + radius, cy + radius],
              fill=None, outline=color_bg, width=4)
    
    # Petales / branches
    n_branches = [12, 8, 6][level % 3]
    inner_r = radius * 0.4
    
    for i in range(n_branches):
        angle = (i / n_branches) * 2 * math.pi + rotation_offset
        x_tip = cx + math.cos(angle) * radius * 0.95
        y_tip = cy + math.sin(angle) * radius * 0.95
        x_l = cx + math.cos(angle - math.pi / n_branches * 0.4) * inner_r
        y_l = cy + math.sin(angle - math.pi / n_branches * 0.4) * inner_r
        x_r = cx + math.cos(angle + math.pi / n_branches * 0.4) * inner_r
        y_r = cy + math.sin(angle + math.pi / n_branches * 0.4) * inner_r
        
        color = palette["colors"][(i + level * 2) % len(palette["colors"])]
        d.polygon([(cx, cy), (x_l, y_l), (x_tip, y_tip), (x_r, y_r)],
                  fill=color, outline=palette["outline"])
    
    # RECURSION : mandala plus petit au centre
    if level < 3:
        new_radius = int(radius * 0.4)
        draw_fractal_mandala(d, palette, cx, cy, new_radius, level + 1,
                             rotation_offset + math.pi / 8)


def add_central_fractal(img, palette):
    """Ajoute le mandala fractal central."""
    d = ImageDraw.Draw(img, "RGBA")
    central_radius = int(SIZE * 0.06)
    draw_fractal_mandala(d, palette, CX, CY, central_radius, 0,
                         rotation_offset=math.pi / 12)


# === COUCHE 5 : ALL-SEEING EYE CENTRAL ===

def add_all_seeing_eye(img, palette):
    """Oeil au centre comme image 8 (Eye of Providence)."""
    d = ImageDraw.Draw(img, "RGBA")
    
    eye_size = 60
    
    # Iris bleu/vert
    iris_color = (50, 255, 200) if palette["name"] != "fire_neon" else (255, 100, 50)
    d.ellipse([CX - eye_size, CY - eye_size, CX + eye_size, CY + eye_size],
              fill=iris_color, outline=palette["outline"], width=3)
    
    # Pupille noire
    pupil_size = eye_size // 3
    d.ellipse([CX - pupil_size, CY - pupil_size,
               CX + pupil_size, CY + pupil_size], fill=(0, 0, 0))
    
    # Reflet blanc dans la pupille
    refl_size = pupil_size // 4
    d.ellipse([CX - refl_size + pupil_size // 2,
               CY - refl_size - pupil_size // 2,
               CX + refl_size + pupil_size // 2,
               CY + refl_size - pupil_size // 2],
              fill=(255, 255, 255))


# === COUCHE 6 : RADIAL BURSTS NEON ===

def add_radial_bursts(img, palette):
    """Lignes radiales fines partant du centre."""
    d = ImageDraw.Draw(img, "RGBA")
    
    for n_lines, length, width in [(48, int(SIZE * 0.45), 2),
                                    (24, int(SIZE * 0.50), 3),
                                    (12, int(SIZE * 0.55), 4)]:
        offset = math.pi / n_lines * 0.5
        for i in range(n_lines):
            angle = (i / n_lines) * 2 * math.pi + offset
            x_inner = CX + math.cos(angle) * int(SIZE * 0.10)
            y_inner = CY + math.sin(angle) * int(SIZE * 0.10)
            x_outer = CX + math.cos(angle) * length
            y_outer = CY + math.sin(angle) * length
            color = palette["colors"][i % len(palette["colors"])]
            d.line([(x_inner, y_inner), (x_outer, y_outer)],
                   fill=color, width=width)


# === POSTPROCESS : UV GLOW EFFECT ===

def apply_uv_glow(img, intensity=0.7):
    """Simule un effet UV blacklight avec glow neon."""
    # 1. Glow large (diffusion lumineuse)
    glow_large = img.filter(ImageFilter.GaussianBlur(radius=20))
    enhancer = ImageEnhance.Color(glow_large)
    glow_large = enhancer.enhance(1.5)
    
    # 2. Glow proche (sharp neon edges)
    glow_close = img.filter(ImageFilter.GaussianBlur(radius=4))
    
    # 3. Composite : original + glows en lighter blend
    img = ImageChops.lighter(img, glow_close)
    img = ImageChops.lighter(img, glow_large)
    
    return img


def boost_saturation(img, factor=1.4):
    enhancer = ImageEnhance.Color(img)
    return enhancer.enhance(factor)


def boost_contrast(img, factor=1.15):
    enhancer = ImageEnhance.Contrast(img)
    return enhancer.enhance(factor)


# === PIPELINE PRINCIPAL ===

def generate_dmt_fresque(palette_idx=0, output_path=None):
    if palette_idx not in PALETTES:
        raise ValueError(f"Palette {palette_idx} inconnue. Valeurs : 0,1,2,3")
    palette = PALETTES[palette_idx]
    
    if output_path is None:
        output_path = f"fresque_v2_{palette['name']}.png"
    
    print(f"=== Fresque DMT 4K palette {palette_idx} ({palette['name']}) ===")
    t0 = time.time()
    
    # Image base avec fond noir
    img = Image.new("RGB", (SIZE, SIZE), palette["bg"])
    
    # COUCHE 1 : Hex lattice dense (fond rempli)
    print("  [1/7] Hex lattice dense (etoiles 6 branches)...")
    add_hex_lattice(img, palette)
    
    # COUCHE 2 : Anneaux concentriques avec petales neon
    print("  [2/7] 14 anneaux concentriques avec petales...")
    add_concentric_petal_rings(img, palette)
    
    # COUCHE 3 : Fleur de vie overlay
    print("  [3/7] Fleur de vie sacred geometry...")
    add_flower_of_life(img, palette)
    
    # COUCHE 4 : Mandala fractal central recursif
    print("  [4/7] Mandala fractal central (3 niveaux recursifs)...")
    add_central_fractal(img, palette)
    
    # COUCHE 5 : Radial bursts neon
    print("  [5/7] Radial bursts neon...")
    add_radial_bursts(img, palette)
    
    # COUCHE 6 : All-seeing eye central
    print("  [6/7] All-seeing eye central...")
    add_all_seeing_eye(img, palette)
    
    # POSTPROCESS : UV glow + saturation
    print("  [7/7] UV glow + saturation extreme...")
    gc.collect()
    img = apply_uv_glow(img, intensity=0.7)
    img = boost_saturation(img, factor=1.4)
    img = boost_contrast(img, factor=1.15)
    
    # Save
    print(f"  Saving {output_path}...")
    img.save(output_path, "PNG", optimize=True)
    
    sz = os.path.getsize(output_path) / 1024 / 1024
    elapsed = time.time() - t0
    print(f"=== TERMINE ({sz:.1f} MB, {elapsed:.1f}s) ===")
    return output_path


def main():
    args = sys.argv[1:]
    
    # Output directory : dans le mod pour que ce soit charge automatiquement
    out_dir = "mod-source/src/main/resources/assets/nexusabsolu/textures/manifold/trip"
    os.makedirs(out_dir, exist_ok=True)
    
    if "--all" in args or "-a" in args:
        # Genere toutes les 4 palettes
        print(f"=== Generation 4 fresques DMT 4K (~2 min) ===\n")
        for i in range(4):
            out_path = os.path.join(out_dir, f"fresque_{i}.png")
            generate_dmt_fresque(i, out_path)
            print()
        print(f"=== TOUTES TERMINEES ===")
        print(f"4 fresques dans : {out_dir}/")
        print(f"Maintenant : bash mod-source/build.sh")
        return
    
    # Genere une seule palette
    if args:
        try:
            palette_idx = int(args[0])
        except ValueError:
            print(f"Usage : python {sys.argv[0]} [palette_idx 0-3 | --all]")
            sys.exit(1)
    else:
        palette_idx = 0
    
    out_path = os.path.join(out_dir, f"fresque_{palette_idx}.png")
    out = generate_dmt_fresque(palette_idx, out_path)
    print(f"\nFresque generee : {out}")
    print(f"\nPour generer toutes les 4 palettes d'un coup :")
    print(f"  python {sys.argv[0]} --all")


if __name__ == "__main__":
    main()
