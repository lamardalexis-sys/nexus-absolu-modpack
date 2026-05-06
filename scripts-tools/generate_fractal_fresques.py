"""
v1.0.354 : Generation de fresques 4K mandala fractal multi-echelles pour
le tunnel infini avant l'entite PEAK.

Concept :
  - Image 4096x4096 ultra detaillee
  - Multi-echelles : anneaux concentriques + mini-mandalas recursifs au centre
  - Quand le code Java zoom de 1x a 16x sur la fresque, le joueur decouvre
    constamment de nouveaux details = effet 'tunnel infini fractal'
  - 4 fresques avec themes/palettes differentes pour variete

Algorithme :
  1. 12 anneaux concentriques avec motifs symetriques (24-fold)
  2. Mini-mandala central (recursion fractale)
  3. Bursts radiaux pour effet 'rayonnement'
  4. Postprocessing : glow Gaussian + color saturation + grain

Usage :
  python scripts-tools/generate_fractal_fresques.py [palette_idx 0-3]
  
  palette 0 : MAGENTA-CYAN  (rose magenta + cyan + or)
  palette 1 : VERDANT       (vert + or + violet)
  palette 2 : SUNSET FIRE   (rouge orange + or + magenta)
  palette 3 : COSMIC VOID   (violet profond + magenta + cyan)
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

# === CONSTANTES ===
SIZE = 4096
CX = SIZE // 2
CY = SIZE // 2

PALETTES = {
    0: {  # MAGENTA-CYAN
        "name": "magenta_cyan",
        "colors": [
            (255, 20, 147),   # deep pink
            (0, 191, 255),    # deep sky blue
            (255, 215, 0),    # gold
            (148, 0, 211),    # dark violet
            (255, 105, 180),  # hot pink
            (0, 255, 255),    # cyan
            (255, 165, 0),    # orange
            (138, 43, 226),   # blue violet
        ],
        "bg": (8, 0, 16),
    },
    1: {  # VERDANT
        "name": "verdant",
        "colors": [
            (50, 205, 50),    # lime
            (255, 215, 0),    # gold
            (138, 43, 226),   # blue violet
            (0, 139, 139),    # teal
            (255, 20, 147),   # deep pink
            (124, 252, 0),    # lawn green
            (255, 99, 71),    # tomato
            (75, 0, 130),     # indigo
        ],
        "bg": (0, 12, 8),
    },
    2: {  # SUNSET FIRE
        "name": "sunset_fire",
        "colors": [
            (255, 69, 0),     # red orange
            (255, 215, 0),    # gold
            (148, 0, 211),    # dark violet
            (255, 105, 180),  # hot pink
            (0, 191, 255),    # cyan
            (255, 165, 0),    # orange
            (255, 20, 147),   # magenta
            (138, 43, 226),   # blue violet
        ],
        "bg": (16, 4, 0),
    },
    3: {  # COSMIC VOID
        "name": "cosmic_void",
        "colors": [
            (72, 61, 139),    # dark slate blue
            (138, 43, 226),   # blue violet
            (255, 20, 147),   # magenta
            (0, 191, 255),    # cyan
            (255, 215, 0),    # gold
            (148, 0, 211),    # purple
            (255, 99, 71),    # tomato
            (50, 205, 50),    # lime
        ],
        "bg": (4, 0, 12),
    },
}


def smoothstep(t):
    return t * t * (3.0 - 2.0 * t)


def render_ring(d, palette, ring_idx, n_rings, radius_outer, radius_inner,
                rotation, n_petals=24, fractal_subdiv=False):
    """Dessine un anneau de mandala avec motifs symetriques.
    
    n_petals : nombre de petales/segments dans l'anneau
    fractal_subdiv : si True, subdivise les petales pour plus de detail (effet fractal)
    """
    color_main = palette["colors"][ring_idx % len(palette["colors"])]
    color_alt = palette["colors"][(ring_idx + 3) % len(palette["colors"])]
    
    # Base : un anneau plein avec couleur main
    for petal in range(n_petals):
        a0 = (petal / n_petals) * 2 * math.pi + rotation
        a1 = ((petal + 1) / n_petals) * 2 * math.pi + rotation
        # Pinte le petale comme un quad polygone
        # Diviser en quad outer/inner
        x0_o = CX + math.cos(a0) * radius_outer
        y0_o = CY + math.sin(a0) * radius_outer
        x1_o = CX + math.cos(a1) * radius_outer
        y1_o = CY + math.sin(a1) * radius_outer
        x0_i = CX + math.cos(a0) * radius_inner
        y0_i = CY + math.sin(a0) * radius_inner
        x1_i = CX + math.cos(a1) * radius_inner
        y1_i = CY + math.sin(a1) * radius_inner
        # Couleur alternee
        color = color_main if petal % 2 == 0 else color_alt
        d.polygon([(x0_o, y0_o), (x1_o, y1_o), (x1_i, y1_i), (x0_i, y0_i)],
                  fill=color, outline=None)
    
    # Bordures fines pour delineation
    border_color = (255, 255, 255)
    d.ellipse([CX - radius_outer, CY - radius_outer,
               CX + radius_outer, CY + radius_outer],
              outline=border_color, width=2)
    d.ellipse([CX - radius_inner, CY - radius_inner,
               CX + radius_inner, CY + radius_inner],
              outline=border_color, width=2)
    
    # Si fractal_subdiv : ajouter petits cercles dans chaque petale
    if fractal_subdiv:
        for petal in range(n_petals):
            a_mid = ((petal + 0.5) / n_petals) * 2 * math.pi + rotation
            r_mid = (radius_outer + radius_inner) / 2
            cx_p = CX + math.cos(a_mid) * r_mid
            cy_p = CY + math.sin(a_mid) * r_mid
            r_circle = (radius_outer - radius_inner) * 0.3
            color_dot = palette["colors"][(petal + ring_idx) % len(palette["colors"])]
            d.ellipse([cx_p - r_circle, cy_p - r_circle,
                       cx_p + r_circle, cy_p + r_circle], fill=color_dot)


def render_radial_bursts(d, palette, n_bursts, radius_outer, radius_inner, rotation):
    """Lignes radiales depuis le centre pour effet 'rayonnement'."""
    for i in range(n_bursts):
        angle = (i / n_bursts) * 2 * math.pi + rotation
        x_inner = CX + math.cos(angle) * radius_inner
        y_inner = CY + math.sin(angle) * radius_inner
        x_outer = CX + math.cos(angle) * radius_outer
        y_outer = CY + math.sin(angle) * radius_outer
        color = palette["colors"][i % len(palette["colors"])]
        # Largeur variable
        width = 4 + (i % 3) * 2
        d.line([(x_inner, y_inner), (x_outer, y_outer)],
               fill=color, width=width)


def render_fractal_mini_mandala(d, palette, center_radius, rotation):
    """Au centre : un MINI mandala avec motifs detailles."""
    # 8 petales fins
    n_petals = 8
    color = palette["colors"][0]
    color_alt = palette["colors"][2]
    
    for i in range(n_petals):
        angle = (i / n_petals) * 2 * math.pi + rotation
        # Petale en forme d'amande
        x_tip = CX + math.cos(angle) * center_radius
        y_tip = CY + math.sin(angle) * center_radius
        # Largeur du petale
        width_angle = (math.pi * 2 / n_petals) * 0.3
        x_l = CX + math.cos(angle - width_angle) * center_radius * 0.4
        y_l = CY + math.sin(angle - width_angle) * center_radius * 0.4
        x_r = CX + math.cos(angle + width_angle) * center_radius * 0.4
        y_r = CY + math.sin(angle + width_angle) * center_radius * 0.4
        c = color if i % 2 == 0 else color_alt
        d.polygon([(CX, CY), (x_l, y_l), (x_tip, y_tip), (x_r, y_r)],
                  fill=c, outline=(255, 255, 255), width=2)
    
    # Cercle central blanc pour eye effect
    r_eye = center_radius * 0.18
    d.ellipse([CX - r_eye, CY - r_eye, CX + r_eye, CY + r_eye],
              fill=(255, 255, 255))
    r_pupil = r_eye * 0.5
    d.ellipse([CX - r_pupil, CY - r_pupil, CX + r_pupil, CY + r_pupil],
              fill=palette["colors"][1])


def add_starfield(img, n_stars=2000):
    """Ajoute des etoiles eparses pour densifier le fond."""
    d = ImageDraw.Draw(img)
    np.random.seed(42)
    xs = np.random.randint(0, SIZE, n_stars)
    ys = np.random.randint(0, SIZE, n_stars)
    sizes = np.random.choice([1, 1, 2, 3], n_stars)
    intensities = np.random.randint(150, 255, n_stars)
    for x, y, sz, intensity in zip(xs, ys, sizes, intensities):
        c = (int(intensity), int(intensity), int(intensity))
        d.ellipse([x - sz, y - sz, x + sz, y + sz], fill=c)


def postprocess_painterly(img, palette):
    """Postprocessing : glow + saturation + slight blur pour effet painterly."""
    # 1. Glow (lumiere additive)
    glow = img.filter(ImageFilter.GaussianBlur(radius=8))
    img = ImageChops.lighter(img, glow)
    
    # 2. Saturation boost
    enhancer = ImageEnhance.Color(img)
    img = enhancer.enhance(1.3)
    
    # 3. Brightness slight boost
    enhancer = ImageEnhance.Brightness(img)
    img = enhancer.enhance(1.05)
    
    return img


def generate_fractal_fresque(palette_idx=0, output_path=None):
    """Pipeline complet pour generer 1 fresque 4K."""
    if palette_idx not in PALETTES:
        raise ValueError(f"Palette {palette_idx} inconnue. Valeurs : 0,1,2,3")
    palette = PALETTES[palette_idx]
    
    if output_path is None:
        output_path = f"fresque_{palette['name']}.png"
    
    print(f"=== Generation fresque 4K palette {palette_idx} ({palette['name']}) ===")
    t0 = time.time()
    
    # 1. Image de base
    img = Image.new("RGB", (SIZE, SIZE), palette["bg"])
    
    # 2. Starfield en fond
    print("  [1/5] Starfield...")
    add_starfield(img, n_stars=3000)
    
    # 3. Multi-echelles : 12 anneaux concentriques (du plus grand au plus petit)
    print("  [2/5] 12 anneaux concentriques...")
    d = ImageDraw.Draw(img)
    
    n_rings = 12
    max_radius = int(SIZE * 0.48)
    min_radius = int(SIZE * 0.04)
    
    for ring_idx in range(n_rings):
        # Logarithmique pour avoir plus de detail vers le centre
        ratio = ring_idx / (n_rings - 1)
        # Interpolation logarithmique entre min et max
        radius_outer = int(min_radius * math.pow(max_radius / min_radius, 1.0 - ratio))
        next_ratio = (ring_idx + 1) / (n_rings - 1)
        if ring_idx == n_rings - 1:
            radius_inner = min_radius
        else:
            radius_inner = int(min_radius * math.pow(max_radius / min_radius, 1.0 - next_ratio))
        
        # Plus de petales pour les anneaux exterieurs
        n_petals = 24 + ring_idx * 4
        # Rotation variable pour chaque anneau
        rotation = (ring_idx * 7) * math.pi / 180.0
        # Subdivision fractale tous les 2 anneaux
        fractal = (ring_idx % 2 == 0)
        
        render_ring(d, palette, ring_idx, n_rings, radius_outer, radius_inner,
                    rotation, n_petals=n_petals, fractal_subdiv=fractal)
    
    # 4. Bursts radiaux longs pour effet rayonnement
    print("  [3/5] Bursts radiaux...")
    render_radial_bursts(d, palette, n_bursts=48,
                         radius_outer=int(SIZE * 0.50),
                         radius_inner=int(SIZE * 0.10),
                         rotation=0)
    render_radial_bursts(d, palette, n_bursts=24,
                         radius_outer=int(SIZE * 0.50),
                         radius_inner=int(SIZE * 0.20),
                         rotation=math.pi / 24)
    
    # 5. Mini-mandala central (effet fractal)
    print("  [4/5] Mini-mandala central...")
    render_fractal_mini_mandala(d, palette, center_radius=int(SIZE * 0.05),
                                 rotation=math.pi / 8)
    
    # 6. Postprocessing
    print("  [5/5] Postprocessing painterly...")
    del d
    gc.collect()
    img = postprocess_painterly(img, palette)
    
    # Save
    print(f"  Saving {output_path}...")
    img.save(output_path, "PNG", optimize=True)
    
    sz = os.path.getsize(output_path) / 1024 / 1024
    elapsed = time.time() - t0
    print(f"=== TERMINE ({sz:.1f} MB, {elapsed:.1f}s) ===")
    return output_path


def main():
    if len(sys.argv) > 1:
        try:
            palette_idx = int(sys.argv[1])
        except ValueError:
            print(f"Usage : python {sys.argv[0]} [palette_idx 0-3]")
            sys.exit(1)
    else:
        palette_idx = 0
    
    out = generate_fractal_fresque(palette_idx)
    print(f"\nFresque generee : {out}")
    print(f"Pour generer toutes les 4 palettes :")
    for i in range(4):
        print(f"  python {sys.argv[0]} {i}")


if __name__ == "__main__":
    main()
