#!/usr/bin/env python3
"""
Genere les 19 textures Age 4 (items + blocks) + models JSON + lang.

Conventions :
  - Items 32x32 : style "lingot" pour les composes/poudres, style "fiole"
    pour les liquides, style custom selon nature
  - Blocks 16x16 : style EnderIO biseaute (LIRE MACHINE-TEXTURE.md)
    Cadre 3 couches BEV3/BEV2/BEV1 + symbole central + cyan dot Voss
  - Models : items.json simple, blocks via blockstate
  - Lang : entries en_us + fr_fr

Items Age 4 a generer (19) :

  Phase 1 :
    resine_echangeuse_block (block 16x16)
    cryo_distillateur_controller (block 16x16, machine)

  Phase 2 :
    cryolite_dust (item 32x32, dust)
    catalyseur_como (item 32x32, dust avec speckles)
    yellowcake_dust (item 32x32, dust glowing)

  Phase 3 :
    compose_alpha (item 32x32, fiole liquide jaune)
    compose_beta (item 32x32, fiole liquide cyan)
    phenol_substitue (item 32x32, dust beige)
    carbone_actif_au (item 32x32, dust noir + dots or)

  Phase 4 :
    capsule_pube (item 32x32, capsule scellee)
    mycelium_active (item 32x32, terre violette glow)
    compose_gamma1 (item 32x32, cristal vert)
    compose_gamma2 (item 32x32, cristal cyan)
    compose_gamma3 (item 32x32, poudre noire glow)

  Phase 5 :
    tryptamide_m (item 32x32, dust violet)
    compose_delta (item 32x32, fiole pourpre)
    cartouche_vide (item 32x32, ampoule vide iridium)

  Phase 6 :
    bioreacteur_controller (block 16x16, machine premium)
    cartouche_chargee (item 32x32, cartouche pleine)

Output :
  - resources/contenttweaker/textures/items/<name>.png
  - resources/contenttweaker/textures/blocks/<name>.png
  - resources/contenttweaker/models/item/<name>.json
  - resources/contenttweaker/models/block/<name>.json (pour blocks)
  - resources/contenttweaker/blockstates/<name>.json (pour blocks)
  - resources/contenttweaker/lang/en_us.lang (entries)
  - resources/contenttweaker/lang/fr_fr.lang (entries)
"""
import os
import json
import random
import math
from PIL import Image

# Conventions MACHINE-TEXTURE.md
BORD = (10, 10, 16)
BEV1 = (24, 24, 34)
BEV2 = (46, 46, 56)
BEV3 = (58, 58, 72)
PANEL = (12, 12, 18)
PURPLE_DARK = (60, 20, 80)
PURPLE_ACCENT = (90, 32, 128)
PURPLE_BRIGHT = (122, 56, 176)
CYAN_VOSS = (0, 180, 180)
GOLD_DOT = (180, 140, 40)

OUT_TEX_ITEMS = "resources/contenttweaker/textures/items"
OUT_TEX_BLOCKS = "resources/contenttweaker/textures/blocks"
OUT_MODEL_ITEMS = "resources/contenttweaker/models/item"
OUT_MODEL_BLOCKS = "resources/contenttweaker/models/block"
OUT_BLOCKSTATES = "resources/contenttweaker/blockstates"
OUT_LANG = "resources/contenttweaker/lang"

for d in [OUT_TEX_ITEMS, OUT_TEX_BLOCKS, OUT_MODEL_ITEMS, OUT_MODEL_BLOCKS,
          OUT_BLOCKSTATES, OUT_LANG]:
    os.makedirs(d, exist_ok=True)


# ============================================================
# HELPERS
# ============================================================

def draw_bevel_frame_16(px, panel_color=PANEL, noise=2):
    """Dessine le cadre 16x16 standard Nexus Absolu sur un PixelAccess."""
    rng = random.Random(42)
    # Noise sur le fond
    for y in range(16):
        for x in range(16):
            n = rng.randint(-noise, noise)
            px[x, y] = (
                max(0, min(255, panel_color[0] + n)),
                max(0, min(255, panel_color[1] + n)),
                max(0, min(255, panel_color[2] + n + 1)),
                255
            )
    # Bordure 1px
    for i in range(16):
        px[i, 0] = (*BORD, 255)
        px[i, 15] = (*BORD, 255)
        px[0, i] = (*BORD, 255)
        px[15, i] = (*BORD, 255)
    # Bevel 3 couches
    for i in range(1, 15):
        px[i, 1] = (*BEV3, 255)
        px[1, i] = (*BEV3, 255)
        px[i, 14] = (*BEV1, 255)
        px[14, i] = (*BEV1, 255)
    for i in range(2, 14):
        px[i, 2] = (*BEV2, 255)
        px[2, i] = (*BEV2, 255)
        px[i, 13] = (*BEV2, 255)
        px[13, i] = (*BEV2, 255)
    # Coins
    px[1, 1] = (*BEV3, 255)
    px[14, 14] = (*BEV1, 255)
    px[1, 14] = (*BEV2, 255)
    px[14, 1] = (*BEV2, 255)


def make_item_dust_32(name, color_main, color_speck=None, glow=False, seed=42):
    """Cree un item 'poudre' 32x32 : tas de pixels en cercle avec speckles."""
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    px = img.load()
    rng = random.Random(seed)

    # Centre du tas + rayon
    cx, cy = 16, 18
    radius_outer = 11
    radius_inner = 8

    # Couleur outline (sombre)
    outline = tuple(max(0, c - 60) for c in color_main[:3]) + (255,)
    main = (*color_main, 255)
    hi = tuple(min(255, c + 40) for c in color_main[:3]) + (255,)

    for y in range(32):
        for x in range(32):
            dx = x - cx
            dy = y - cy
            dist = math.sqrt(dx*dx + dy*dy)
            if dist <= radius_outer:
                if dist >= radius_outer - 1:
                    # outline pixel
                    px[x, y] = outline
                elif dist <= 2 and dy < 0:
                    # highlight haut
                    px[x, y] = hi
                else:
                    # noise interieur
                    n = rng.randint(-15, 15)
                    px[x, y] = (
                        max(0, min(255, color_main[0] + n)),
                        max(0, min(255, color_main[1] + n)),
                        max(0, min(255, color_main[2] + n)),
                        255
                    )
                # Speckles
                if color_speck and rng.random() < 0.08 and dist < radius_inner:
                    px[x, y] = (*color_speck, 255)

    # Glow halo si demande
    if glow:
        glow_color = (*color_main, 80)
        for y in range(32):
            for x in range(32):
                if px[x, y][3] == 0:
                    dx = x - cx
                    dy = y - cy
                    dist = math.sqrt(dx*dx + dy*dy)
                    if radius_outer < dist <= radius_outer + 3:
                        alpha = int(80 * (1 - (dist - radius_outer) / 3))
                        px[x, y] = (*color_main, alpha)
    return img


def make_item_fiole_32(name, liquid_color, glow=False, seed=42):
    """Cree un item 'fiole/flacon' 32x32 : verre transparent + liquide."""
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    px = img.load()

    # Couleurs verre
    glass_outline = (40, 40, 50, 255)
    glass_hi = (220, 220, 230, 200)
    glass_mid = (140, 140, 160, 180)
    cork = (90, 60, 30, 255)
    cork_hi = (130, 90, 50, 255)

    # Forme fiole : col 11-19, hauteur 5-29
    # Bouchon : rows 5-7
    for x in range(13, 19):
        px[x, 5] = cork_hi
        px[x, 6] = cork
        px[x, 7] = cork
    # Goulot : rows 7-10
    for y in range(7, 11):
        px[12, y] = glass_outline
        px[13, y] = glass_hi
        px[14, y] = glass_mid
        px[15, y] = glass_mid
        px[16, y] = glass_mid
        px[17, y] = glass_mid
        px[18, y] = glass_outline
    # Corps : rows 10-29 (s'evase)
    body_top = 10
    body_bot = 28
    for y in range(body_top, body_bot + 1):
        # Largeur evase
        if y < 13:
            w = 7  # goulot s'elargit
        else:
            w = 9
        x_left = 16 - w
        x_right = 16 + w - 1

        # Bord verre
        px[x_left, y] = glass_outline
        px[x_right, y] = glass_outline

        # Interieur : liquide ou glass
        liquid_top = body_top + 3  # liquide commence un peu plus bas que le haut
        if y >= liquid_top and y < body_bot:
            for x in range(x_left + 1, x_right):
                # Liquide avec gradient (plus sombre en bas)
                f = (y - liquid_top) / max(1, (body_bot - liquid_top))
                r = int(liquid_color[0] * (1 - f * 0.3))
                g = int(liquid_color[1] * (1 - f * 0.3))
                b = int(liquid_color[2] * (1 - f * 0.3))
                px[x, y] = (r, g, b, 255)
            # Reflet glass sur cote gauche du liquide
            px[x_left + 1, y] = (
                min(255, liquid_color[0] + 60),
                min(255, liquid_color[1] + 60),
                min(255, liquid_color[2] + 60),
                255
            )
        elif y < liquid_top:
            for x in range(x_left + 1, x_right):
                px[x, y] = glass_mid

    # Fond (row 29) ferme
    for x in range(8, 25):
        if px[x, 28][3] > 0:  # si on a dessine quelque chose au-dessus
            px[x, 29] = glass_outline

    # Glow halo
    if glow:
        for y in range(32):
            for x in range(32):
                if px[x, y][3] == 0:
                    # Check si proche d'un pixel liquide
                    found = False
                    for dy in [-2, -1, 0, 1, 2]:
                        for dx in [-2, -1, 0, 1, 2]:
                            nx, ny = x + dx, y + dy
                            if 0 <= nx < 32 and 0 <= ny < 32:
                                pixel = px[nx, ny]
                                if pixel[3] == 255 and pixel[:3] == (
                                    int(liquid_color[0] * (1 - 0.3 * 0.5)),
                                    int(liquid_color[1] * (1 - 0.3 * 0.5)),
                                    int(liquid_color[2] * (1 - 0.3 * 0.5))
                                ):
                                    found = True
                                    break
                        if found: break
                    if found:
                        px[x, y] = (*liquid_color, 60)
    return img


def make_item_capsule_32(name, color, glow=True, seed=42):
    """Capsule scellee : cylindre metallique vertical avec contenu."""
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    px = img.load()

    metal_dark = (40, 40, 50, 255)
    metal_mid = (90, 90, 100, 255)
    metal_hi = (160, 160, 170, 255)

    # Capsule 12x22 (de col 10 a 21, row 5 a 26)
    for y in range(5, 27):
        # Cap haut/bas arrondi
        if y == 5 or y == 26:
            for x in range(12, 20):
                px[x, y] = metal_dark
        elif y == 6 or y == 25:
            px[10, y] = metal_dark
            px[11, y] = metal_dark
            for x in range(12, 20):
                px[x, y] = metal_mid
            px[20, y] = metal_dark
            px[21, y] = metal_dark
        else:
            # Corps cylindrique
            px[10, y] = metal_dark
            px[11, y] = metal_dark
            px[20, y] = metal_dark
            px[21, y] = metal_dark
            # Highlight gauche (lumiere venant de gauche)
            px[12, y] = metal_hi
            for x in range(13, 19):
                # Centre = couleur du contenu
                if 10 <= y <= 22:
                    px[x, y] = (*color, 255)
                else:
                    px[x, y] = metal_mid
            px[19, y] = metal_dark

    # Glow autour si demande
    if glow:
        for y in range(32):
            for x in range(32):
                if px[x, y][3] == 0:
                    # Distance au plus proche pixel coloré
                    for dy in [-3, -2, -1, 0, 1, 2, 3]:
                        for dx in [-3, -2, -1, 0, 1, 2, 3]:
                            nx, ny = x + dx, y + dy
                            if 0 <= nx < 32 and 0 <= ny < 32:
                                pixel = px[nx, ny]
                                if pixel[:3] == color:
                                    dist = abs(dx) + abs(dy)
                                    alpha = max(0, 70 - dist * 15)
                                    if alpha > 0:
                                        px[x, y] = (*color, alpha)
                                        break
                        if px[x, y][3] > 0:
                            break
    return img


def make_item_ampoule_32(name, color_glass=(220, 230, 240),
                         color_inner=None, glow=False, seed=42):
    """Ampoule iridium pour cartouche : forme oblongue verticale."""
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    px = img.load()

    iridium_dark = (60, 65, 80, 255)
    iridium_mid = (110, 115, 130, 255)
    iridium_hi = (180, 185, 200, 255)
    glass_clear = (220, 230, 240, 200)

    # Cartouche : ovale vertical centré, 14 pixels haut x 8 pixels large
    cx = 16
    cy = 16
    rx = 5  # rayon horizontal
    ry = 11  # rayon vertical

    for y in range(32):
        for x in range(32):
            # Equation ovale
            dx = x - cx
            dy = y - cy
            d = (dx * dx) / (rx * rx) + (dy * dy) / (ry * ry)
            if d <= 1.0:
                if d >= 0.85:
                    # Outline iridium
                    px[x, y] = iridium_dark
                elif d >= 0.70:
                    # Bord iridium
                    if dx < 0 and dy < 0:
                        px[x, y] = iridium_hi
                    else:
                        px[x, y] = iridium_mid
                elif d >= 0.55:
                    # Cap iridium fin
                    px[x, y] = iridium_dark
                else:
                    # Interieur : verre transparent ou liquide
                    if color_inner:
                        # Liquide avec gradient
                        f = (y - (cy - ry * 0.4)) / max(1, (ry * 0.8))
                        f = max(0, min(1, f))
                        r = int(color_inner[0] * (1 - f * 0.2))
                        g = int(color_inner[1] * (1 - f * 0.2))
                        b = int(color_inner[2] * (1 - f * 0.2))
                        px[x, y] = (r, g, b, 255)
                    else:
                        px[x, y] = glass_clear

    # Highlight glass (reflet brillant en haut-gauche)
    px[cx - 2, cy - 7] = (255, 255, 255, 220)
    px[cx - 1, cy - 7] = (255, 255, 255, 180)
    px[cx - 2, cy - 6] = (255, 255, 255, 180)

    # Glow halo si charge
    if glow and color_inner:
        for y in range(32):
            for x in range(32):
                if px[x, y][3] == 0:
                    dx = x - cx
                    dy = y - cy
                    d = (dx * dx) / (rx * rx) + (dy * dy) / (ry * ry)
                    if 1.0 < d <= 1.5:
                        alpha = int(60 * (1.5 - d) / 0.5)
                        px[x, y] = (*color_inner, alpha)
    return img


# ============================================================
# BLOCS — style EnderIO biseaute 16x16
# ============================================================

def make_block_machine_16(name, symbol_func, panel_bg=PANEL, seed=42):
    """Cree un bloc machine 16x16 standard Nexus Absolu (frame 0)."""
    img = Image.new('RGBA', (16, 16), (*panel_bg, 255))
    px = img.load()
    draw_bevel_frame_16(px, panel_color=panel_bg)
    if symbol_func:
        symbol_func(px, 0, 1)
    return img


def symbol_resine(px, frame_idx, num_frames):
    """Symbole pour resine echangeuse : grille de pixels colores
    (filtre/echangeur)."""
    # 4x4 grille de cellules dans le panneau (3,3 to 12,12)
    for ry in range(4):
        for rx in range(4):
            x = 3 + rx * 3
            y = 3 + ry * 3
            # Couleur alternee simulant les granules de resine
            if (rx + ry) % 2 == 0:
                px[x, y] = (90, 60, 200, 255)  # violet
                px[x + 1, y] = (110, 80, 220, 255)
                px[x, y + 1] = (110, 80, 220, 255)
            else:
                px[x, y] = (60, 40, 140, 255)
                px[x + 1, y] = (80, 60, 160, 255)
                px[x, y + 1] = (80, 60, 160, 255)

    # Cyan dot indicateur Voss
    px[12, 4] = (*CYAN_VOSS, 255)


def symbol_cryo_distillateur(px, frame_idx, num_frames):
    """Symbole : tube vertical avec gouttes (distillation cryogenique)."""
    # Tube central
    for y in range(4, 12):
        px[7, y] = (*BEV3, 255)
        px[8, y] = (200, 230, 255, 255)  # cryo bleu clair
        px[9, y] = (*BEV1, 255)

    # Joint haut/bas (chambres)
    for x in range(5, 12):
        px[x, 4] = (*PURPLE_DARK, 255)
        px[x, 11] = (*PURPLE_DARK, 255)

    # Goutte qui tombe
    px[8, 12] = (*CYAN_VOSS, 255)

    # Cyan dot
    px[3, 12] = (*CYAN_VOSS, 255)
    # Gold status
    px[12, 3] = (*GOLD_DOT, 255)


def symbol_bioreacteur(px, frame_idx, num_frames):
    """Symbole : centre violet pulsant + 4 ancres + entrelacs."""
    # Cercle central pulsant (violet bright)
    px[7, 7] = (*PURPLE_BRIGHT, 255)
    px[8, 7] = (*PURPLE_BRIGHT, 255)
    px[7, 8] = (*PURPLE_BRIGHT, 255)
    px[8, 8] = (*PURPLE_BRIGHT, 255)

    # Halo violet
    for dx, dy in [(6, 7), (9, 7), (7, 6), (8, 6), (7, 9), (8, 9), (6, 8), (9, 8)]:
        px[dx, dy] = (*PURPLE_ACCENT, 255)

    # 4 ancres aux coins du panneau
    for x, y in [(3, 3), (12, 3), (3, 12), (12, 12)]:
        px[x, y] = (*CYAN_VOSS, 255)

    # Lignes diagonales reliant le centre aux ancres
    px[4, 4] = (*PURPLE_DARK, 255)
    px[5, 5] = (*PURPLE_DARK, 255)
    px[10, 5] = (*PURPLE_DARK, 255)
    px[11, 4] = (*PURPLE_DARK, 255)
    px[5, 10] = (*PURPLE_DARK, 255)
    px[4, 11] = (*PURPLE_DARK, 255)
    px[11, 11] = (*PURPLE_DARK, 255)
    px[10, 10] = (*PURPLE_DARK, 255)

    # Gold status
    px[3, 4] = (*GOLD_DOT, 255)


# ============================================================
# DEFINITION DES 19 ITEMS/BLOCKS
# ============================================================

ITEMS_DEF = [
    # Phase 2 — dust simples
    ("cryolite_dust", "item", "dust", {
        "color_main": (200, 220, 255), "color_speck": (255, 255, 255),
        "name_en": "Cryolite Dust", "name_fr": "Poudre de Cryolite"
    }),
    ("catalyseur_como", "item", "dust", {
        "color_main": (60, 70, 90), "color_speck": (200, 100, 60),
        "name_en": "CoMo Catalyst", "name_fr": "Catalyseur CoMo"
    }),
    ("yellowcake_dust", "item", "dust", {
        "color_main": (220, 200, 60), "color_speck": (255, 240, 100),
        "glow": True,
        "name_en": "Yellowcake Dust", "name_fr": "Poudre de Yellowcake"
    }),

    # Phase 3 — composes alpha/beta + dust
    ("compose_alpha", "item", "fiole", {
        "liquid_color": (255, 200, 50), "glow": True,
        "name_en": "Compound Alpha", "name_fr": "Compose Alpha"
    }),
    ("compose_beta", "item", "fiole", {
        "liquid_color": (50, 200, 255), "glow": True,
        "name_en": "Compound Beta", "name_fr": "Compose Beta"
    }),
    ("phenol_substitue", "item", "dust", {
        "color_main": (220, 200, 170),
        "name_en": "Substituted Phenol", "name_fr": "Phenol Substitue"
    }),
    ("carbone_actif_au", "item", "dust", {
        "color_main": (30, 30, 35), "color_speck": (220, 180, 60),
        "name_en": "Au-loaded Activated Carbon", "name_fr": "Carbone Actif Or"
    }),

    # Phase 4 — capsules + composes gamma
    ("capsule_pube", "item", "capsule", {
        "color": (100, 255, 100), "glow": True,
        "name_en": "Pu-Be Source Capsule", "name_fr": "Capsule Pu-Be"
    }),
    ("mycelium_active", "item", "dust", {
        "color_main": (140, 80, 200), "color_speck": (200, 150, 255),
        "glow": True,
        "name_en": "Activated Mycelium", "name_fr": "Mycelium Active"
    }),
    ("compose_gamma1", "item", "dust", {
        "color_main": (100, 200, 80), "color_speck": (200, 255, 180),
        "glow": True,
        "name_en": "Compound Gamma-1", "name_fr": "Compose Gamma-1"
    }),
    ("compose_gamma2", "item", "dust", {
        "color_main": (80, 200, 230), "color_speck": (180, 255, 255),
        "glow": True,
        "name_en": "Compound Gamma-2", "name_fr": "Compose Gamma-2"
    }),
    ("compose_gamma3", "item", "dust", {
        "color_main": (15, 15, 20), "color_speck": (180, 220, 255),
        "glow": True,
        "name_en": "Compound Gamma-3 (6LiT)", "name_fr": "Compose Gamma-3 (6LiT)"
    }),

    # Phase 5
    ("tryptamide_m", "item", "dust", {
        "color_main": (180, 100, 220),
        "name_en": "Tryptamide-M", "name_fr": "Tryptamide-M"
    }),
    ("compose_delta", "item", "fiole", {
        "liquid_color": (160, 60, 200), "glow": True,
        "name_en": "Compound Delta", "name_fr": "Compose Delta"
    }),
    ("cartouche_vide", "item", "ampoule", {
        "color_inner": None,
        "name_en": "Empty Cartridge", "name_fr": "Cartouche Vide"
    }),

    # Phase 6
    ("cartouche_chargee", "item", "ampoule", {
        "color_inner": (180, 80, 230), "glow": True,
        "name_en": "Charged Cartridge", "name_fr": "Cartouche Chargee"
    }),

    # Blocks Phase 1
    ("resine_echangeuse_block", "block", "machine", {
        "symbol_func": symbol_resine,
        "name_en": "Ion Exchange Resin", "name_fr": "Resine Echangeuse"
    }),
    ("cryo_distillateur_controller", "block", "machine", {
        "symbol_func": symbol_cryo_distillateur,
        "name_en": "Cryo Distillation Controller", "name_fr": "Cryo-Distillateur (Controleur)"
    }),

    # Block Phase 6
    ("bioreacteur_controller", "block", "machine", {
        "symbol_func": symbol_bioreacteur,
        "name_en": "Bio-Reactor Controller", "name_fr": "Bio-Reacteur (Controleur)"
    }),
]


# ============================================================
# Generation
# ============================================================

print(f"Generation de {len(ITEMS_DEF)} items/blocks Age 4...")
print()

lang_en = []
lang_fr = []

for i, (name, kind, style, params) in enumerate(ITEMS_DEF):
    # Generation texture
    if kind == "item":
        if style == "dust":
            img = make_item_dust_32(
                name,
                color_main=params["color_main"],
                color_speck=params.get("color_speck"),
                glow=params.get("glow", False),
                seed=hash(name) & 0xFFFF
            )
        elif style == "fiole":
            img = make_item_fiole_32(
                name,
                liquid_color=params["liquid_color"],
                glow=params.get("glow", False),
                seed=hash(name) & 0xFFFF
            )
        elif style == "capsule":
            img = make_item_capsule_32(
                name,
                color=params["color"],
                glow=params.get("glow", True),
                seed=hash(name) & 0xFFFF
            )
        elif style == "ampoule":
            img = make_item_ampoule_32(
                name,
                color_inner=params.get("color_inner"),
                glow=params.get("glow", False),
                seed=hash(name) & 0xFFFF
            )

        out_path = os.path.join(OUT_TEX_ITEMS, f"{name}.png")
        img.save(out_path, "PNG", optimize=True)

        # Model item
        model = {
            "parent": "item/generated",
            "textures": {"layer0": f"contenttweaker:items/{name}"}
        }
        with open(os.path.join(OUT_MODEL_ITEMS, f"{name}.json"), 'w') as f:
            json.dump(model, f, indent=2)

        # Lang : item.contenttweaker.<name>.name=...
        lang_en.append(f"item.contenttweaker.{name}.name={params['name_en']}")
        lang_fr.append(f"item.contenttweaker.{name}.name={params['name_fr']}")

    elif kind == "block":
        if style == "machine":
            img = make_block_machine_16(name, params["symbol_func"])

        out_path = os.path.join(OUT_TEX_BLOCKS, f"{name}.png")
        img.save(out_path, "PNG", optimize=True)

        # Blockstate
        blockstate = {
            "variants": {"normal": {"model": f"contenttweaker:{name}"}}
        }
        with open(os.path.join(OUT_BLOCKSTATES, f"{name}.json"), 'w') as f:
            json.dump(blockstate, f, indent=2)

        # Block model
        block_model = {
            "parent": "block/cube_all",
            "textures": {"all": f"contenttweaker:blocks/{name}"}
        }
        with open(os.path.join(OUT_MODEL_BLOCKS, f"{name}.json"), 'w') as f:
            json.dump(block_model, f, indent=2)

        # Item model (block dans inventaire)
        item_model = {
            "parent": f"contenttweaker:block/{name}"
        }
        with open(os.path.join(OUT_MODEL_ITEMS, f"{name}.json"), 'w') as f:
            json.dump(item_model, f, indent=2)

        # Lang : tile.contenttweaker.<name>.name=...
        lang_en.append(f"tile.contenttweaker.{name}.name={params['name_en']}")
        lang_fr.append(f"tile.contenttweaker.{name}.name={params['name_fr']}")

    print(f"  [{i+1:2d}/{len(ITEMS_DEF)}] {name} ({kind} {style})")


# Append lang entries (ne pas overwrite, on append)
def append_lang(lang_file, entries):
    """Append lang entries au fichier existant, en evitant les doublons."""
    existing = set()
    if os.path.exists(lang_file):
        with open(lang_file, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line and '=' in line and not line.startswith('#'):
                    key = line.split('=')[0]
                    existing.add(key)

    new_entries = []
    for entry in entries:
        key = entry.split('=')[0]
        if key not in existing:
            new_entries.append(entry)

    if new_entries:
        with open(lang_file, 'a', encoding='utf-8') as f:
            f.write("\n# === Age 4 -- Phase 1-6 (auto-generated) ===\n")
            for entry in new_entries:
                f.write(entry + "\n")
        print(f"  + {len(new_entries)} entries dans {lang_file}")


append_lang(os.path.join(OUT_LANG, "en_us.lang"), lang_en)
append_lang(os.path.join(OUT_LANG, "fr_fr.lang"), lang_fr)

print()
print(f"OK : {len(ITEMS_DEF)} items/blocks generes.")
print(f"  Textures items : {OUT_TEX_ITEMS}")
print(f"  Textures blocks : {OUT_TEX_BLOCKS}")
print(f"  Models : {OUT_MODEL_ITEMS} + {OUT_MODEL_BLOCKS}")
print(f"  Blockstates : {OUT_BLOCKSTATES}")
print(f"  Lang : {OUT_LANG}/en_us.lang + fr_fr.lang")
