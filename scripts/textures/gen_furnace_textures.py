"""
Generateur textures Nexus Absolu pour les 5 Furnaces T1-T5.
Respecte MACHINE-TEXTURE.md : 16x16, cadre biseaute EnderIO-style,
4 frames animees avec mcmeta.

Per tier palette = accent flamme + halo sur la porte de cuisson.
Front = porte avec vitre + flamme animee.
Side = panneaux avec vents metalliques.
Top = vent + indicateur tier au centre.
"""

from PIL import Image
import random
import json
import os
import math

# === Palette commune (cadre biseaute) ===
BORD = (10, 10, 16)
BEV1 = (24, 24, 34)
BEV2 = (46, 46, 56)
BEV3 = (58, 58, 72)
PANEL = (12, 12, 18)

# === Palettes par tier (corps_clair, corps_sombre, flamme_base, flamme_bright, accent_tier) ===
TIER_PALETTES = {
    "iron": {
        "body_light":  (175, 175, 180),   # fer poli
        "body_mid":    (130, 130, 140),
        "body_dark":   (80, 80, 90),
        "flame_core":  (255, 180, 60),    # flamme orange classique
        "flame_bright":(255, 240, 140),
        "accent":      (200, 80, 60),     # chaud rouge/orange
    },
    "gold": {
        "body_light":  (240, 200, 80),    # or eclatant
        "body_mid":    (200, 160, 50),
        "body_dark":   (120, 90, 20),
        "flame_core":  (255, 200, 90),    # flamme dore
        "flame_bright":(255, 255, 180),
        "accent":      (255, 230, 120),
    },
    "invar": {
        "body_light":  (200, 210, 200),   # blanc-vert metallique
        "body_mid":    (150, 165, 150),
        "body_dark":   (90, 105, 90),
        "flame_core":  (180, 230, 255),   # flamme cyan
        "flame_bright":(220, 250, 255),
        "accent":      (100, 220, 240),   # cyan
    },
    "emeradic": {
        "body_light":  (120, 230, 140),   # cristal vert emeradic
        "body_mid":    (70, 170, 90),
        "body_dark":   (30, 90, 40),
        "flame_core":  (120, 255, 160),   # flamme vert brillant
        "flame_bright":(220, 255, 220),
        "accent":      (180, 255, 200),
    },
    "vossium_iv": {
        "body_light":  (180, 100, 220),   # violet vossium
        "body_mid":    (120, 60, 170),
        "body_dark":   (60, 20, 90),
        "flame_core":  (220, 140, 255),   # flamme violet/rose
        "flame_bright":(255, 220, 255),
        "accent":      (200, 80, 255),
    },
}


def draw_frame(palette, face, frame_idx, num_frames):
    """Cree une frame 16x16 pour une face (front/side/top) d'un tier."""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    px = img.load()

    body_light = palette["body_light"]
    body_mid = palette["body_mid"]
    body_dark = palette["body_dark"]

    # === 1. FOND : corps metallique avec noise ===
    random.seed(hash(face + "bg") & 0xFFFF)
    for y in range(16):
        for x in range(16):
            n = random.randint(-8, 8)
            base = body_mid
            px[x, y] = tuple(max(0, min(255, c + n)) for c in base) + (255,)

    # === 2. Bordure exterieure (1px tout autour) ===
    for i in range(16):
        px[i, 0]  = (*BORD, 255)
        px[i, 15] = (*BORD, 255)
        px[0, i]  = (*BORD, 255)
        px[15, i] = (*BORD, 255)

    # === 3. Bevel 3 couches (biseau EnderIO-style) ===
    for i in range(1, 15):
        px[i,  1]  = (*body_light, 255)    # top = clair
        px[1,  i]  = (*body_light, 255)    # left = clair
        px[i, 14]  = (*body_dark, 255)     # bottom = sombre
        px[14, i]  = (*body_dark, 255)     # right = sombre
    for i in range(2, 14):
        px[i,  2]  = tuple((a+b)//2 for a, b in zip(body_light, body_mid)) + (255,)
        px[2,  i]  = tuple((a+b)//2 for a, b in zip(body_light, body_mid)) + (255,)
        px[i, 13]  = tuple((a+b)//2 for a, b in zip(body_mid, body_dark)) + (255,)
        px[13, i]  = tuple((a+b)//2 for a, b in zip(body_mid, body_dark)) + (255,)
    # Coins du bevel
    px[1, 1]   = (*body_light, 255)
    px[14, 14] = (*body_dark, 255)
    px[1, 14]  = (*body_mid, 255)
    px[14, 1]  = (*body_mid, 255)

    # === 4. Contenu par face ===
    if face == "front":
        draw_front(px, palette, frame_idx, num_frames)
    elif face == "side":
        draw_side(px, palette, frame_idx, num_frames)
    elif face == "top":
        draw_top(px, palette, frame_idx, num_frames)

    return img


def draw_front(px, palette, frame_idx, num_frames):
    """Face avant : porte de four avec vitre + flamme animee."""
    flame_core = palette["flame_core"]
    flame_bright = palette["flame_bright"]

    # Cadre de porte (rectangle sombre 10x9 centre bas-centre)
    # De (3, 4) a (12, 12) inclus
    for x in range(3, 13):
        for y in range(4, 13):
            if 4 <= x <= 11 and 5 <= y <= 11:
                # Interieur sombre (= interior du four)
                px[x, y] = (15, 10, 8, 255)
            else:
                # Cadre de porte
                px[x, y] = (*BORD, 255)

    # Bevel interieur de la porte (crusty metal around flame opening)
    for x in range(3, 13):
        px[x, 4] = (*palette["body_dark"], 255)
    for y in range(4, 13):
        px[3, y] = (*palette["body_dark"], 255)

    # === FLAMME ANIMEE au centre de la porte ===
    # Position base : (5,7) a (10,11)
    # La flamme "danse" selon frame_idx

    # Base de la flamme (brulantes braises)
    for x in range(5, 11):
        px[x, 11] = (*flame_core, 255)

    # Corps de flamme
    flame_shape_frames = [
        # Frame 0 : flamme centree, moyenne
        [(6, 10), (7, 9), (8, 9), (9, 10), (7, 8), (8, 8)],
        # Frame 1 : flamme plus haute, tremble a droite
        [(6, 10), (7, 9), (8, 8), (9, 9), (10, 10), (7, 7), (8, 7)],
        # Frame 2 : flamme max, plus large
        [(5, 10), (6, 9), (7, 8), (8, 8), (9, 8), (10, 9), (7, 7), (8, 7), (8, 6)],
        # Frame 3 : flamme retombe, tremble a gauche
        [(5, 10), (6, 9), (7, 9), (8, 9), (9, 10), (6, 8), (7, 8)],
    ]
    shape = flame_shape_frames[frame_idx % len(flame_shape_frames)]
    for fx, fy in shape:
        if 4 <= fx <= 11 and 5 <= fy <= 11:
            px[fx, fy] = (*flame_core, 255)

    # Highlight bright au sommet (uniquement frame 1 et 2)
    if frame_idx in (1, 2):
        highlight_pts = [(7, 7), (8, 7)] if frame_idx == 1 else [(7, 6), (8, 6), (8, 7)]
        for hx, hy in highlight_pts:
            if 4 <= hx <= 11 and 5 <= hy <= 11:
                px[hx, hy] = (*flame_bright, 255)

    # Petit indicateur tier en haut a droite (pulse subtil)
    accent = palette["accent"]
    if frame_idx in (0, 2):
        px[12, 3] = (*accent, 255)
    else:
        px[12, 3] = tuple(max(0, c - 60) for c in accent) + (255,)


def draw_side(px, palette, frame_idx, num_frames):
    """Face laterale : panneaux metalliques avec vents."""
    body_dark = palette["body_dark"]
    body_light = palette["body_light"]

    # Vents horizontaux (3 bandes)
    for row in (5, 8, 11):
        for x in range(4, 12):
            # Fente sombre
            px[x, row] = tuple(max(0, c - 40) for c in body_dark) + (255,)
            # Highlight au-dessus de la fente
            px[x, row - 1] = (*body_light, 255)

    # Petits rivets aux coins (decoration)
    for rx, ry in [(3, 3), (12, 3), (3, 12), (12, 12)]:
        px[rx, ry] = (*BORD, 255)


def draw_top(px, palette, frame_idx, num_frames):
    """Face superieure : grille de ventilation avec accent tier au centre."""
    body_dark = palette["body_dark"]
    accent = palette["accent"]

    # Grille de ventilation (croix centrale)
    # Barre horizontale
    for x in range(4, 12):
        px[x, 7] = tuple(max(0, c - 50) for c in body_dark) + (255,)
        px[x, 8] = tuple(max(0, c - 50) for c in body_dark) + (255,)
    # Barre verticale
    for y in range(4, 12):
        px[7, y] = tuple(max(0, c - 50) for c in body_dark) + (255,)
        px[8, y] = tuple(max(0, c - 50) for c in body_dark) + (255,)

    # Centre = accent tier (pulse avec frame_idx)
    brightness = [0.4, 0.7, 1.0, 0.7][frame_idx % 4]
    accent_pulsed = tuple(int(c * brightness) for c in accent)
    px[7, 7] = (*accent_pulsed, 255)
    px[8, 7] = (*accent_pulsed, 255)
    px[7, 8] = (*accent_pulsed, 255)
    px[8, 8] = (*accent_pulsed, 255)


def save_animated(frames, path, frametime=4, interpolate=True):
    """Sauvegarde un PNG vertical (16 x 16*N) + mcmeta."""
    total_h = 16 * len(frames)
    combined = Image.new('RGBA', (16, total_h))
    for i, fr in enumerate(frames):
        combined.paste(fr, (0, i * 16))
    combined.save(path)

    meta_path = path + ".mcmeta"
    with open(meta_path, 'w') as f:
        json.dump({"animation": {"interpolate": interpolate, "frametime": frametime}}, f)


# === Generation ===
out_dir = "mod-source/src/main/resources/assets/nexusabsolu/textures/blocks"
os.makedirs(out_dir, exist_ok=True)

for tier_name, palette in TIER_PALETTES.items():
    # 4 frames par face pour front (flamme anime), top (accent pulse)
    # Side : 1 frame seulement (statique, pas besoin d'animer les vents)

    # FRONT : 4 frames animees
    frames_front = [draw_frame(palette, "front", f, 4) for f in range(4)]
    save_animated(frames_front, f"{out_dir}/furnace_{tier_name}_front.png", frametime=4)

    # TOP : 4 frames animees (pulse accent)
    frames_top = [draw_frame(palette, "top", f, 4) for f in range(4)]
    save_animated(frames_top, f"{out_dir}/furnace_{tier_name}_top.png", frametime=5)

    # SIDE : 1 frame statique (pas de .mcmeta)
    side = draw_frame(palette, "side", 0, 1)
    side_path = f"{out_dir}/furnace_{tier_name}_side.png"
    side.save(side_path)
    # Si un ancien .mcmeta existait, le retirer (side n'est plus anime)
    if os.path.exists(side_path + ".mcmeta"):
        os.remove(side_path + ".mcmeta")

    print(f"[OK] Generated furnace_{tier_name}_front (4 frames), _top (4 frames), _side (1 frame)")

print("\nAll 5 tiers done. 15 textures + 10 mcmeta files (front + top animated).")
