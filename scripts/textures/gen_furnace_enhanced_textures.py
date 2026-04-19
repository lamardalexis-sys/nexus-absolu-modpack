"""
Genere les textures 'enhanced' des furnaces avec une LED bleue au coin
superieur droit (power indicator style).

Pour chaque tier : copie la texture de base (16x64 = 4 frames de 16x16)
et ajoute une LED 2x2 pixels a la position (13, 2) de CHAQUE frame.

La LED est un pixel cyan brillant (0x66FFEE) avec un glow autour
(couleur moins saturee).

Sources :
  - furnace_<tier>_front_on.png -> furnace_<tier>_front_on_enhanced.png
  - furnace_<tier>_top.png      -> furnace_<tier>_top_enhanced.png

Les .mcmeta sont copies tels quels (meme animation).
"""
from PIL import Image
import shutil, os

TIERS = ["iron", "gold", "invar", "emeradic", "vossium_iv"]
BLOCKS_DIR = "/home/claude/nexus-absolu/mod-source/src/main/resources/assets/nexusabsolu/textures/blocks"

# Position de la LED (coin superieur droit dans chaque frame 16x16)
# Frame = 16 pixels de large x 16 pixels de haut. LED 2x2 a (x=13, y=2).
LED_X = 13
LED_Y = 2
LED_W = 2
LED_H = 2
# Couleurs de la LED (RGBA)
LED_CORE = (102, 255, 238, 255)   # cyan brillant 0x66FFEE
LED_GLOW = (51, 153, 170, 180)    # cyan plus sombre avec alpha 180

FRAME_H = 16

def add_led_to_all_frames(src_path, dst_path):
    img = Image.open(src_path).convert("RGBA")
    w, h = img.size
    num_frames = h // FRAME_H
    pixels = img.load()
    for frame in range(num_frames):
        base_y = frame * FRAME_H
        # Glow (halo 3x3 autour de la LED, pixels qui ne sont pas la LED core)
        for dx in range(-1, LED_W + 1):
            for dy in range(-1, LED_H + 1):
                x = LED_X + dx
                y = base_y + LED_Y + dy
                if x < 0 or x >= w or y < base_y or y >= base_y + FRAME_H:
                    continue
                # Si ce pixel est dans la zone LED core, skip (on le fera apres)
                if 0 <= dx < LED_W and 0 <= dy < LED_H:
                    continue
                # Blend le glow sur le pixel existant
                existing = pixels[x, y]
                alpha = LED_GLOW[3] / 255.0
                r = int(existing[0] * (1 - alpha) + LED_GLOW[0] * alpha)
                g = int(existing[1] * (1 - alpha) + LED_GLOW[1] * alpha)
                b = int(existing[2] * (1 - alpha) + LED_GLOW[2] * alpha)
                pixels[x, y] = (r, g, b, 255)
        # LED core (2x2 pixels opaques cyan)
        for dx in range(LED_W):
            for dy in range(LED_H):
                pixels[LED_X + dx, base_y + LED_Y + dy] = LED_CORE
    img.save(dst_path)

for tier in TIERS:
    # Front ON (animated)
    src = os.path.join(BLOCKS_DIR, f"furnace_{tier}_front_on.png")
    dst = os.path.join(BLOCKS_DIR, f"furnace_{tier}_front_on_enhanced.png")
    add_led_to_all_frames(src, dst)
    # Copier le .mcmeta pour que l'animation marche pareil
    src_meta = src + ".mcmeta"
    if os.path.exists(src_meta):
        shutil.copy(src_meta, dst + ".mcmeta")
    print(f"Created {os.path.basename(dst)}")

    # TOP (animated)
    src = os.path.join(BLOCKS_DIR, f"furnace_{tier}_top.png")
    dst = os.path.join(BLOCKS_DIR, f"furnace_{tier}_top_enhanced.png")
    add_led_to_all_frames(src, dst)
    src_meta = src + ".mcmeta"
    if os.path.exists(src_meta):
        shutil.copy(src_meta, dst + ".mcmeta")
    print(f"Created {os.path.basename(dst)}")

print("Done.")
