#!/usr/bin/env python3
"""
Phase 6 Polish - Generation de 11 textures items Phase 3+4 manquantes.

Style :
  - Composes alpha/beta/gamma1-3/delta : fiole liquide 32x32 avec couleur
    distinctive et bouchon doré
  - cartouche_chargee : variante de cartouche_manifold (pre-armement)
  - casing_titane_iridium : anneau metallique
  - tryptamide_m_capsule : capsule scellee
  - cristal_manifoldine : cristal hexagonal pourpre
  - matrix_pigmentary : matrice 4x4 multicolore

Convention : 32x32 PNG RGBA, fond transparent.
"""

from PIL import Image, ImageDraw
import os

OUT_DIR = "mod-source/src/main/resources/assets/nexusabsolu/textures/items"

def new_canvas():
    return Image.new('RGBA', (32, 32), (0, 0, 0, 0))


def draw_flask(color, glow_color=None, outline=(40, 40, 40, 255)):
    """Dessine une fiole 32x32 avec couleur de liquide donnee."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    # Cou de la fiole (rectangle haut)
    d.rectangle([12, 4, 19, 9], fill=(180, 180, 180, 255), outline=outline)
    # Bouchon dore
    d.rectangle([10, 2, 21, 5], fill=(220, 180, 50, 255), outline=outline)
    # Corps de la fiole (bulb)
    d.ellipse([6, 10, 25, 28], fill=(200, 200, 220, 230), outline=outline)
    # Liquide a l'interieur
    d.ellipse([8, 14, 23, 26], fill=color)
    # Highlight
    d.ellipse([10, 16, 14, 20], fill=(255, 255, 255, 80))
    # Glow optionnel
    if glow_color:
        for r in range(2, 5):
            d.ellipse([14-r, 18-r, 14+r, 18+r], fill=(*glow_color[:3], 30))
    return img


def draw_capsule(color_top, color_bot, has_seal=True):
    """Dessine une capsule scellee 32x32."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    # Capsule top (rectangle arrondi)
    d.rounded_rectangle([10, 4, 22, 16], radius=4, fill=color_top, outline=(40,40,40,255))
    # Capsule bottom
    d.rounded_rectangle([10, 14, 22, 28], radius=4, fill=color_bot, outline=(40,40,40,255))
    # Joint central
    d.line([10, 16, 22, 16], fill=(60,60,60,255), width=1)
    # Seal dore
    if has_seal:
        d.ellipse([14, 13, 18, 19], fill=(220, 180, 50, 255), outline=(140,100,20,255))
    return img


def draw_cristal_hex(color, glow_color):
    """Dessine un cristal hexagonal pourpre 32x32."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    cx, cy = 16, 16
    # Hexagone (6 points)
    pts = [
        (cx, cy-12), (cx+10, cy-6), (cx+10, cy+6),
        (cx, cy+12), (cx-10, cy+6), (cx-10, cy-6)
    ]
    d.polygon(pts, fill=color, outline=(20,20,20,255))
    # Lignes internes (facettes)
    for px, py in pts:
        d.line([cx, cy, px, py], fill=(*glow_color[:3], 120), width=1)
    # Highlight central
    d.ellipse([cx-3, cy-3, cx+3, cy+3], fill=(255, 255, 255, 100))
    return img


def draw_ring(metal_color, accent_color):
    """Dessine un anneau metallique 32x32 (casing)."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    # Outer circle
    d.ellipse([3, 3, 28, 28], fill=metal_color, outline=(20,20,20,255))
    # Inner hollow
    d.ellipse([10, 10, 21, 21], fill=(0, 0, 0, 0), outline=(20,20,20,255))
    # 4 accent dots (titanium fixings)
    for x, y in [(7, 16), (16, 7), (24, 16), (16, 24)]:
        d.ellipse([x-2, y-2, x+2, y+2], fill=accent_color)
    # Subtle highlight
    d.arc([3, 3, 28, 28], start=200, end=270, fill=(255,255,255,140), width=2)
    return img


def draw_matrix(colors_4x4):
    """Dessine matrix 4x4 colors (pour matrix_pigmentary)."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    # 4x4 grid de cellules de 5x5 pixels chacune, centree
    start = 6
    cell = 5
    for i in range(4):
        for j in range(4):
            x = start + j * cell
            y = start + i * cell
            color = colors_4x4[i * 4 + j]
            d.rectangle([x, y, x+cell-1, y+cell-1], fill=color, outline=(40,40,40,255))
    # Outline general
    d.rectangle([4, 4, 27, 27], fill=None, outline=(40,40,40,255), width=1)
    return img


def draw_cartouche_chargee():
    """Variante cartouche_manifold mais avec couleur intermediaire."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    # Cartridge body (rectangle vertical)
    d.rounded_rectangle([10, 4, 22, 26], radius=2, fill=(190,190,200,255), outline=(40,40,50,255))
    # Inner Quantum Glass window
    d.rectangle([12, 8, 20, 22], fill=(40,40,50,255))
    # Liquide partiellement actif (gradient pourpre/cyan en motif spirale)
    for y in range(8, 22):
        ratio = (y - 8) / 14
        r = int(120 + ratio * 60)
        g = int(80 + ratio * 100)
        b = int(180 - ratio * 50)
        a = 200
        d.rectangle([12, y, 20, y+1], fill=(r, g, b, a))
    # Bouchon (ampoule top)
    d.ellipse([13, 1, 19, 6], fill=(180,150,40,255), outline=(40,40,50,255))
    # Aiguille bottom (sertisseur)
    d.polygon([(15,26), (17,26), (16,30)], fill=(220,220,220,255), outline=(40,40,50,255))
    return img


# === GENERATE TEXTURES ===
items = {
    # Composes : fioles avec couleurs distinctes (alpha=jaune-acide, beta=cyan, 
    #                    gamma1=vert UF6, gamma2=violet PuBe, gamma3=noir LiT, delta=pourpre)
    'compose_alpha':  draw_flask((220, 200, 60, 230), glow_color=(255, 230, 100)),
    'compose_beta':   draw_flask((90, 200, 220, 230), glow_color=(150, 240, 255)),
    'compose_gamma1': draw_flask((150, 220, 100, 230), glow_color=(200, 255, 150)),
    'compose_gamma2': draw_flask((180, 100, 220, 230), glow_color=(220, 150, 255)),
    'compose_gamma3': draw_flask((40, 40, 60, 230), glow_color=(120, 80, 200)),
    'compose_delta':  draw_flask((180, 60, 180, 230), glow_color=(220, 100, 220)),
    
    # Capsules scellees
    'tryptamide_m_capsule': draw_capsule((140, 80, 200, 255), (180, 100, 220, 255)),
    
    # Cristal hexagonal
    'cristal_manifoldine': draw_cristal_hex((180, 80, 220, 230), (220, 150, 255)),
    
    # Matrix 16-color
    'matrix_pigmentary': draw_matrix([
        (220, 60, 60, 255), (240, 140, 50, 255), (240, 220, 80, 255), (240, 180, 200, 255),
        (180, 240, 120, 255), (80, 220, 220, 255), (120, 180, 240, 255), (220, 100, 220, 255),
        (40, 40, 60, 255), (140, 80, 200, 255), (140, 100, 60, 255), (220, 220, 220, 255),
        (60, 100, 60, 255), (200, 100, 80, 255), (180, 180, 100, 255), (100, 80, 140, 255),
    ]),
    
    # Anneaux metalliques
    'casing_titane_iridium': draw_ring((180, 180, 200, 255), (90, 200, 220, 255)),
    
    # Cartouche chargee (variante intermediaire avant armement)
    'cartouche_chargee': draw_cartouche_chargee(),
}

print(f"Generating {len(items)} textures in {OUT_DIR}...")
os.makedirs(OUT_DIR, exist_ok=True)
for name, img in items.items():
    path = f"{OUT_DIR}/{name}.png"
    img.save(path, 'PNG')
    print(f"  ✓ {name}.png ({os.path.getsize(path)} bytes)")

print(f"\n✓ Done. {len(items)} textures crees.")
