"""
Genere les textures "enhanced" des furnaces avec LED POWER INDICATOR :

Position : centre-haut (x=7-8, y=1-2) dans chaque frame 16x16
Taille   : 2x2 pixels
Style    :
  - IDLE (furnace enhanced mais eteint) : LED GRISE discrete, pas de halo
  - ACTIVE (furnace enhanced en cuisson) : LED CYAN brillante + halo

Textures generees par tier :
  - furnace_X_front_enhanced_idle.png     : base front.png + LED grise
  - furnace_X_front_on_enhanced.png       : base front_on.png + LED cyan + halo
  - furnace_X_top_enhanced_idle.png       : base top.png + LED grise
  - furnace_X_top_enhanced_active.png     : base top.png + LED cyan + halo
"""
from PIL import Image
import shutil, os

TIERS = ["iron", "gold", "invar", "emeradic", "vossium_iv"]
BLOCKS_DIR = "/home/claude/nexus-absolu/mod-source/src/main/resources/assets/nexusabsolu/textures/blocks"

FRAME_H = 16
LED_X = 7
LED_Y = 1
LED_W = 2
LED_H = 2

IDLE_CORE   = (102, 102, 110, 255)
IDLE_GLOW   = None
ACTIVE_CORE = (102, 255, 238, 255)
ACTIVE_GLOW = (51, 170, 180, 200)


def add_led_to_all_frames(src_path, dst_path, core_color, glow_color):
    img = Image.open(src_path).convert("RGBA")
    w, h = img.size
    num_frames = h // FRAME_H if h >= FRAME_H else 1
    pixels = img.load()
    for frame in range(num_frames):
        base_y = frame * FRAME_H
        if glow_color is not None:
            for dx in range(-1, LED_W + 1):
                for dy in range(-1, LED_H + 1):
                    x = LED_X + dx
                    y = base_y + LED_Y + dy
                    if x < 0 or x >= w or y < base_y or y >= base_y + FRAME_H:
                        continue
                    if 0 <= dx < LED_W and 0 <= dy < LED_H:
                        continue
                    ex = pixels[x, y]
                    a = glow_color[3] / 255.0
                    r = int(ex[0] * (1 - a) + glow_color[0] * a)
                    g = int(ex[1] * (1 - a) + glow_color[1] * a)
                    b = int(ex[2] * (1 - a) + glow_color[2] * a)
                    pixels[x, y] = (r, g, b, 255)
        for dx in range(LED_W):
            for dy in range(LED_H):
                pixels[LED_X + dx, base_y + LED_Y + dy] = core_color
    img.save(dst_path)


def copy_mcmeta(src, dst):
    if os.path.exists(src + ".mcmeta"):
        shutil.copy(src + ".mcmeta", dst + ".mcmeta")


for tier in TIERS:
    src_front = os.path.join(BLOCKS_DIR, f"furnace_{tier}_front.png")
    dst = os.path.join(BLOCKS_DIR, f"furnace_{tier}_front_enhanced_idle.png")
    add_led_to_all_frames(src_front, dst, IDLE_CORE, IDLE_GLOW)
    copy_mcmeta(src_front, dst)
    print(f"Created {os.path.basename(dst)}")

    src_front_on = os.path.join(BLOCKS_DIR, f"furnace_{tier}_front_on.png")
    dst = os.path.join(BLOCKS_DIR, f"furnace_{tier}_front_on_enhanced.png")
    add_led_to_all_frames(src_front_on, dst, ACTIVE_CORE, ACTIVE_GLOW)
    copy_mcmeta(src_front_on, dst)
    print(f"Created {os.path.basename(dst)}")

    src_top = os.path.join(BLOCKS_DIR, f"furnace_{tier}_top.png")
    dst = os.path.join(BLOCKS_DIR, f"furnace_{tier}_top_enhanced_idle.png")
    add_led_to_all_frames(src_top, dst, IDLE_CORE, IDLE_GLOW)
    copy_mcmeta(src_top, dst)
    print(f"Created {os.path.basename(dst)}")

    dst = os.path.join(BLOCKS_DIR, f"furnace_{tier}_top_enhanced_active.png")
    add_led_to_all_frames(src_top, dst, ACTIVE_CORE, ACTIVE_GLOW)
    copy_mcmeta(src_top, dst)
    print(f"Created {os.path.basename(dst)}")

print("Done.")
