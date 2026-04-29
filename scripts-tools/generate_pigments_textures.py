#!/usr/bin/env python3
"""
Genere les textures + models + lang pour les pigments Botania (16) +
cristal_manifoldine + matrix_pigmentary (Age 4 Phase 5).

Items existants dans Age4_Manifold_Content.zs sans texture :
  pigment_{red, orange, yellow, pink, lime, cyan, lightblue, magenta,
            black, purple, brown, gray, green, blue, lightgray, white}
  matrix_pigmentary
  cristal_manifoldine
"""
import os
import json
import math
import random
from PIL import Image

OUT_TEX_ITEMS = "resources/contenttweaker/textures/items"
OUT_MODEL_ITEMS = "resources/contenttweaker/models/item"
OUT_LANG = "resources/contenttweaker/lang"

for d in [OUT_TEX_ITEMS, OUT_MODEL_ITEMS, OUT_LANG]:
    os.makedirs(d, exist_ok=True)


def make_pigment_32(color, name="pigment", seed=42):
    """Pigment = poudre fine 32x32 dans une fiole/plat avec gradient lumineux."""
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    px = img.load()
    rng = random.Random(seed)

    # Pigment etale en cercle (poudre dans un plat)
    cx, cy = 16, 18
    radius = 11

    outline = tuple(max(0, c - 80) for c in color[:3]) + (255,)
    main = (*color, 255)
    hi = tuple(min(255, c + 50) for c in color[:3]) + (255,)
    deep = tuple(max(0, c - 30) for c in color[:3]) + (255,)

    for y in range(32):
        for x in range(32):
            dx = x - cx
            dy = y - cy
            dist = math.sqrt(dx*dx + dy*dy)
            if dist <= radius:
                if dist >= radius - 1:
                    px[x, y] = outline
                elif dist <= radius - 1 and dist > radius - 3:
                    # Anneau exterieur sombre
                    px[x, y] = deep
                elif dy < -2:
                    # Highlight haut
                    px[x, y] = hi
                else:
                    # Centre avec speckles fins
                    if rng.random() < 0.15:
                        px[x, y] = hi
                    elif rng.random() < 0.10:
                        px[x, y] = deep
                    else:
                        n = rng.randint(-10, 10)
                        px[x, y] = (
                            max(0, min(255, color[0] + n)),
                            max(0, min(255, color[1] + n)),
                            max(0, min(255, color[2] + n)),
                            255
                        )
    return img


def make_cristal_32(seed=42):
    """Cristal de Manifoldine : forme losange/diamant violet+cyan avec glow."""
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    px = img.load()

    # Couleurs
    outline = (40, 10, 60, 255)
    deep_violet = (90, 30, 140, 255)
    violet = (140, 60, 200, 255)
    bright = (180, 100, 230, 255)
    cyan_acc = (100, 220, 230, 255)
    white = (240, 240, 255, 255)

    # Forme diamant : pointe haut (16, 4), milieu (16, 16), pointe bas (16, 28)
    # Largeur max au milieu : 8 px de chaque cote (24 px total, x=4 a x=28 trop large)
    # On fait plus serre : largeur max 6 px

    # Dessin par lignes horizontales
    diamond_data = []
    for y in range(4, 29):
        if y <= 16:
            # Haut : largeur croissante
            half_w = (y - 4) // 2
        else:
            # Bas : largeur decroissante
            half_w = (28 - y) // 2

        if half_w == 0 and y not in [4, 28]:
            half_w = 1

        for x in range(16 - half_w, 16 + half_w + 1):
            # Position relative pour gradient
            v_pos = (y - 4) / 24.0  # 0..1
            h_pos = (x - 16) / max(1, half_w)  # -1..1

            # Bord = outline
            if x == 16 - half_w or x == 16 + half_w or y == 4 or y == 28:
                px[x, y] = outline
            elif abs(h_pos) > 0.7:
                px[x, y] = deep_violet
            elif abs(h_pos) > 0.3:
                px[x, y] = violet
            else:
                # Centre
                if v_pos < 0.3:
                    px[x, y] = bright
                elif v_pos < 0.5:
                    px[x, y] = cyan_acc
                else:
                    px[x, y] = violet

    # Reflet blanc en haut a gauche
    px[15, 7] = white
    px[15, 8] = white
    px[14, 9] = white

    # Glow halo cyan/violet
    cy = 16
    for y in range(32):
        for x in range(32):
            if px[x, y][3] == 0:
                # Distance au plus proche pixel colore
                min_dist = 999
                for dy in [-3, -2, -1, 0, 1, 2, 3]:
                    for dx in [-3, -2, -1, 0, 1, 2, 3]:
                        nx, ny = x + dx, y + dy
                        if 0 <= nx < 32 and 0 <= ny < 32 and px[nx, ny][3] == 255:
                            d = abs(dx) + abs(dy)
                            min_dist = min(min_dist, d)
                if min_dist < 4:
                    alpha = max(0, 80 - min_dist * 20)
                    if alpha > 0:
                        # Glow violet
                        px[x, y] = (140, 60, 200, alpha)

    return img


# ============================================================
# 16 PIGMENTS
# ============================================================

PIGMENTS = [
    ("pigment_red",       (220, 30, 30),    "Pigment Rouge",      "Red Pigment"),
    ("pigment_orange",    (255, 130, 20),   "Pigment Orange",     "Orange Pigment"),
    ("pigment_yellow",    (255, 220, 30),   "Pigment Jaune",      "Yellow Pigment"),
    ("pigment_pink",      (255, 150, 200),  "Pigment Rose",       "Pink Pigment"),
    ("pigment_lime",      (180, 240, 30),   "Pigment Lime",       "Lime Pigment"),
    ("pigment_cyan",      (30, 220, 230),   "Pigment Cyan",       "Cyan Pigment"),
    ("pigment_lightblue", (130, 200, 255),  "Pigment Bleu Clair", "Light Blue Pigment"),
    ("pigment_magenta",   (240, 60, 200),   "Pigment Magenta",    "Magenta Pigment"),
    ("pigment_black",     (35, 35, 40),     "Pigment Noir",       "Black Pigment"),
    ("pigment_purple",    (130, 50, 200),   "Pigment Violet",     "Purple Pigment"),
    ("pigment_brown",     (110, 70, 40),    "Pigment Brun",       "Brown Pigment"),
    ("pigment_gray",      (100, 100, 110),  "Pigment Gris",       "Gray Pigment"),
    ("pigment_green",     (40, 160, 60),    "Pigment Vert",       "Green Pigment"),
    ("pigment_blue",      (40, 80, 200),    "Pigment Bleu",       "Blue Pigment"),
    ("pigment_lightgray", (180, 180, 190),  "Pigment Gris Clair", "Light Gray Pigment"),
    ("pigment_white",     (240, 240, 245),  "Pigment Blanc",      "White Pigment"),
]


print(f"Generation de {len(PIGMENTS)} pigments + cristal_manifoldine + matrix_pigmentary...")
print()

lang_en = []
lang_fr = []

for name, color, name_fr, name_en in PIGMENTS:
    img = make_pigment_32(color, name=name, seed=hash(name) & 0xFFFF)
    img.save(os.path.join(OUT_TEX_ITEMS, f"{name}.png"), "PNG", optimize=True)

    # Model JSON
    model = {
        "parent": "item/generated",
        "textures": {"layer0": f"contenttweaker:items/{name}"}
    }
    with open(os.path.join(OUT_MODEL_ITEMS, f"{name}.json"), 'w') as f:
        json.dump(model, f, indent=2)

    # Lang
    lang_en.append(f"item.contenttweaker.{name}.name={name_en}")
    lang_fr.append(f"item.contenttweaker.{name}.name={name_fr}")

    print(f"  OK {name}")


# ============================================================
# CRISTAL MANIFOLDINE
# ============================================================

print()
print("Generation cristal_manifoldine...")
img = make_cristal_32(seed=2026)
img.save(os.path.join(OUT_TEX_ITEMS, "cristal_manifoldine.png"), "PNG", optimize=True)
with open(os.path.join(OUT_MODEL_ITEMS, "cristal_manifoldine.json"), 'w') as f:
    json.dump({
        "parent": "item/generated",
        "textures": {"layer0": "contenttweaker:items/cristal_manifoldine"}
    }, f, indent=2)
lang_en.append("item.contenttweaker.cristal_manifoldine.name=Manifoldine Crystal")
lang_fr.append("item.contenttweaker.cristal_manifoldine.name=Cristal de Manifoldine")
print("  OK cristal_manifoldine")


# ============================================================
# MATRIX PIGMENTARY (Essence Chromatique solide form)
# ============================================================
# Utilise le meme generateur que cristal mais avec couleurs arc-en-ciel

def make_matrix_pigmentary_32(seed=42):
    """Matrix Pigmentary : 16 pigments fusionnes en cristal arc-en-ciel."""
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    px = img.load()
    rng = random.Random(seed)

    # Forme carree avec gradient arc-en-ciel
    rainbow = [
        (255, 30, 30), (255, 130, 20), (255, 220, 30),
        (180, 240, 30), (40, 160, 60), (30, 220, 230),
        (40, 80, 200), (130, 50, 200), (240, 60, 200)
    ]

    cx, cy = 16, 16
    half = 11

    for y in range(cy - half, cy + half + 1):
        for x in range(cx - half, cx + half + 1):
            dx = abs(x - cx)
            dy = abs(y - cy)
            # Forme carree avec coins arrondis
            d = max(dx, dy)
            if d > half:
                continue

            # Outline
            if d == half or d == half - 1 and (dx + dy) % 3 == 0:
                px[x, y] = (20, 10, 30, 255)
                continue

            # Couleur depend de la position (rainbow)
            angle = math.atan2(y - cy, x - cx)
            idx = int((angle + math.pi) / (2 * math.pi) * len(rainbow)) % len(rainbow)
            color = rainbow[idx]

            # Variation
            n = rng.randint(-15, 15)
            px[x, y] = (
                max(0, min(255, color[0] + n)),
                max(0, min(255, color[1] + n)),
                max(0, min(255, color[2] + n)),
                255
            )

    # Glow exterieur cyan
    for y in range(32):
        for x in range(32):
            if px[x, y][3] == 0:
                dx = abs(x - cx)
                dy = abs(y - cy)
                d = max(dx, dy)
                if half < d <= half + 2:
                    alpha = max(0, 80 - (d - half) * 30)
                    px[x, y] = (200, 200, 255, alpha)

    return img


print()
print("Generation matrix_pigmentary...")
img = make_matrix_pigmentary_32(seed=2026)
img.save(os.path.join(OUT_TEX_ITEMS, "matrix_pigmentary.png"), "PNG", optimize=True)
with open(os.path.join(OUT_MODEL_ITEMS, "matrix_pigmentary.json"), 'w') as f:
    json.dump({
        "parent": "item/generated",
        "textures": {"layer0": "contenttweaker:items/matrix_pigmentary"}
    }, f, indent=2)
lang_en.append("item.contenttweaker.matrix_pigmentary.name=Pigmentary Matrix")
lang_fr.append("item.contenttweaker.matrix_pigmentary.name=Matrice Pigmentaire")
print("  OK matrix_pigmentary")


# ============================================================
# Append lang
# ============================================================

def append_lang(lang_file, entries):
    existing = set()
    if os.path.exists(lang_file):
        with open(lang_file, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line and '=' in line and not line.startswith('#'):
                    key = line.split('=')[0]
                    existing.add(key)

    new_entries = [e for e in entries if e.split('=')[0] not in existing]
    if new_entries:
        with open(lang_file, 'a', encoding='utf-8') as f:
            f.write("\n# === Pigments Botania + cristal_manifoldine ===\n")
            for entry in new_entries:
                f.write(entry + "\n")
        print(f"\n  + {len(new_entries)} entries dans {lang_file}")


append_lang(os.path.join(OUT_LANG, "en_us.lang"), lang_en)
append_lang(os.path.join(OUT_LANG, "fr_fr.lang"), lang_fr)

print()
print(f"Total : {len(PIGMENTS)} pigments + cristal_manifoldine + matrix_pigmentary = {len(PIGMENTS) + 2} items")
