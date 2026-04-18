"""
Regenerer textures furnace en 2 etats : off (statique) et on (animee).

Avant : _front.png (4 frames animees avec flamme dans tous les cas)
Apres :
  _front.png     (1 frame statique, PAS de flamme - etat eteint)
  _front_on.png  (4 frames animees avec flamme - etat allume)

Le bloc switchera entre les deux selon la propriete BURNING.
Side + Top restent identiques (pas anime, on garde _top.png a 4 frames pulse).
"""
from PIL import Image
import random
import json
import os

# Palette commune
BORD = (10, 10, 16)
BEV1 = (24, 24, 34)
BEV2 = (46, 46, 56)
BEV3 = (58, 58, 72)
PANEL = (12, 12, 18)

TIER_PALETTES = {
    "iron": {
        "body_light":  (175, 175, 180),
        "body_mid":    (130, 130, 140),
        "body_dark":   (80, 80, 90),
        "flame_core":  (255, 180, 60),
        "flame_bright":(255, 240, 140),
        "accent":      (200, 80, 60),
    },
    "gold": {
        "body_light":  (240, 200, 80),
        "body_mid":    (200, 160, 50),
        "body_dark":   (120, 90, 20),
        "flame_core":  (255, 200, 90),
        "flame_bright":(255, 255, 180),
        "accent":      (255, 230, 120),
    },
    "invar": {
        "body_light":  (200, 210, 200),
        "body_mid":    (150, 165, 150),
        "body_dark":   (90, 105, 90),
        "flame_core":  (180, 230, 255),
        "flame_bright":(220, 250, 255),
        "accent":      (100, 220, 240),
    },
    "emeradic": {
        "body_light":  (120, 230, 140),
        "body_mid":    (70, 170, 90),
        "body_dark":   (30, 90, 40),
        "flame_core":  (120, 255, 160),
        "flame_bright":(220, 255, 220),
        "accent":      (180, 255, 200),
    },
    "vossium_iv": {
        "body_light":  (180, 100, 220),
        "body_mid":    (120, 60, 170),
        "body_dark":   (60, 20, 90),
        "flame_core":  (220, 140, 255),
        "flame_bright":(255, 220, 255),
        "accent":      (200, 80, 255),
    },
}


def draw_frame_base(palette, frame_idx):
    """Cree une frame 16x16 avec juste le cadre machine, sans flamme."""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    px = img.load()
    body_light = palette["body_light"]
    body_mid = palette["body_mid"]
    body_dark = palette["body_dark"]

    # Fond metallique noisy
    random.seed(42)
    for y in range(16):
        for x in range(16):
            n = random.randint(-8, 8)
            px[x, y] = tuple(max(0, min(255, c + n)) for c in body_mid) + (255,)

    # Bordure exterieure
    for i in range(16):
        px[i, 0]  = (*BORD, 255)
        px[i, 15] = (*BORD, 255)
        px[0, i]  = (*BORD, 255)
        px[15, i] = (*BORD, 255)

    # Bevel 3D
    for i in range(1, 15):
        px[i,  1]  = (*body_light, 255)
        px[1,  i]  = (*body_light, 255)
        px[i, 14]  = (*body_dark, 255)
        px[14, i]  = (*body_dark, 255)
    for i in range(2, 14):
        px[i,  2]  = tuple((a+b)//2 for a, b in zip(body_light, body_mid)) + (255,)
        px[2,  i]  = tuple((a+b)//2 for a, b in zip(body_light, body_mid)) + (255,)
        px[i, 13]  = tuple((a+b)//2 for a, b in zip(body_mid, body_dark)) + (255,)
        px[13, i]  = tuple((a+b)//2 for a, b in zip(body_mid, body_dark)) + (255,)
    px[1, 1]   = (*body_light, 255)
    px[14, 14] = (*body_dark, 255)
    px[1, 14]  = (*body_mid, 255)
    px[14, 1]  = (*body_mid, 255)

    # Porte de fourneau (rectangle encaisse)
    for x in range(3, 13):
        for y in range(4, 13):
            if 4 <= x <= 11 and 5 <= y <= 11:
                px[x, y] = (15, 10, 8, 255)
            else:
                px[x, y] = (*BORD, 255)
    for x in range(3, 13):
        px[x, 4] = (*body_dark, 255)
    for y in range(4, 13):
        px[3, y] = (*body_dark, 255)

    # Petit indicateur en haut-droite (pour differencier les tiers meme quand off)
    accent = palette["accent"]
    if frame_idx in (0, 2):
        px[12, 3] = (*accent, 255)
    else:
        px[12, 3] = tuple(max(0, c - 60) for c in accent) + (255,)

    return img


def draw_frame_with_flame(palette, frame_idx, num_frames):
    """Frame 16x16 AVEC flamme animee dans la porte."""
    img = draw_frame_base(palette, frame_idx)
    px = img.load()
    flame_core = palette["flame_core"]
    flame_bright = palette["flame_bright"]

    # Base de flamme (braises)
    for x in range(5, 11):
        px[x, 11] = (*flame_core, 255)

    # Corps de flamme (change selon frame)
    flame_shape_frames = [
        [(6, 10), (7, 9), (8, 9), (9, 10), (7, 8), (8, 8)],
        [(6, 10), (7, 9), (8, 8), (9, 9), (10, 10), (7, 7), (8, 7)],
        [(5, 10), (6, 9), (7, 8), (8, 8), (9, 8), (10, 9), (7, 7), (8, 7), (8, 6)],
        [(5, 10), (6, 9), (7, 9), (8, 9), (9, 10), (6, 8), (7, 8)],
    ]
    shape = flame_shape_frames[frame_idx % len(flame_shape_frames)]
    for fx, fy in shape:
        if 4 <= fx <= 11 and 5 <= fy <= 11:
            px[fx, fy] = (*flame_core, 255)

    if frame_idx in (1, 2):
        highlight = [(7, 7), (8, 7)] if frame_idx == 1 else [(7, 6), (8, 6), (8, 7)]
        for hx, hy in highlight:
            if 4 <= hx <= 11 and 5 <= hy <= 11:
                px[hx, hy] = (*flame_bright, 255)

    return img


def save_static(img, path):
    """PNG 16x16, pas de mcmeta."""
    img.save(path)
    # Supprimer un eventuel ancien mcmeta
    mcmeta = path + ".mcmeta"
    if os.path.exists(mcmeta):
        os.remove(mcmeta)


def save_animated(frames, path, frametime=4):
    """PNG 16 x 16*N + mcmeta avec animation."""
    total_h = 16 * len(frames)
    combined = Image.new('RGBA', (16, total_h))
    for i, fr in enumerate(frames):
        combined.paste(fr, (0, i * 16))
    combined.save(path)
    with open(path + ".mcmeta", 'w') as f:
        json.dump({"animation": {"interpolate": True, "frametime": frametime}}, f)


out_dir = "mod-source/src/main/resources/assets/nexusabsolu/textures/blocks"
os.makedirs(out_dir, exist_ok=True)

for tier_name, palette in TIER_PALETTES.items():
    # _front.png : STATIQUE (1 frame, sans flamme) = etat eteint
    front_off = draw_frame_base(palette, 0)
    save_static(front_off, f"{out_dir}/furnace_{tier_name}_front.png")

    # _front_on.png : ANIMEE (4 frames, avec flamme) = etat allume
    frames_on = [draw_frame_with_flame(palette, f, 4) for f in range(4)]
    save_animated(frames_on, f"{out_dir}/furnace_{tier_name}_front_on.png", frametime=4)

    print(f"[OK] {tier_name}: _front.png (static off) + _front_on.png (animated on)")

print("\nFronts done. Side/Top inchangees.")
