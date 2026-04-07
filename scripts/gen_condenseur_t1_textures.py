#!/usr/bin/env python3
"""
Generate Condenseur T1 formed-block textures.

Theme: 'Le Premier Prototype du Dr. Voss' - rusty industrial proto-reactor,
hand-soldered in a prison cell years ago and showing its age.
The Tier 1 condenser is the player's FIRST machine, so it should look
worn and rusty - in contrast to the polished higher-tier versions.

This script:
1. Regenerates all 7 existing textures with a rust patina overlay
2. Converts 3 previously static textures to ANIMATED 6-frame versions:
   - condenseur_formed_side       (status LED blinks)
   - condenseur_formed_glass      (porthole boss pulses)
   - condenseur_formed_top_wall_top (exhaust valve glows)
3. Writes .mcmeta animation files for the newly animated textures

Pattern inspired by the T2 condenseur which has multiple animated
textures (master + wall both animated). T1 follows the same approach
but with a worn/rusty look to mark its 'first prototype' status.

Output: PNG files in mod-source/.../textures/blocks/
Resolution: 32x32 static, 32x192 (6 frames stacked) for animated
"""

import json
import os
import random
from PIL import Image, ImageDraw

# ========= PALETTE =========
DARK     = (32, 32, 36, 255)
METAL    = (58, 58, 66, 255)
METAL_LT = (84, 84, 94, 255)
METAL_XL = (118, 118, 128, 255)
RIVET_D  = (20, 20, 24, 255)
RIVET_L  = (140, 140, 150, 255)

VOSS_DK   = (74, 38, 110, 255)
VOSS_MD   = (122, 64, 178, 255)
VOSS_LT   = (170, 110, 220, 255)
VOSS_GLOW = (200, 150, 240, 255)

WARN_Y   = (212, 176, 64, 255)
WARN_K   = (24, 24, 24, 255)
GREEN    = (70, 180, 88, 255)
GREEN_LT = (140, 230, 150, 255)
RED      = (196, 64, 56, 255)
RED_LT   = (232, 92, 84, 255)
RED_DK   = (110, 24, 20, 255)

GLASS_BG = (60, 50, 80, 96)
GLASS_LT = (140, 110, 180, 140)

# RUST palette (additional, for the 'first prototype' look)
RUST_DK  = (60, 24, 8, 255)    # very dark rust shadow
RUST_MD  = (138, 62, 20, 255)  # main rust color
RUST_LT  = (184, 80, 31, 255)  # bright rust highlight
RUST_OR  = (210, 110, 50, 255) # orange rust spot
PATINA   = (74, 56, 38, 255)   # brown-olive patina


# ========= HELPERS =========
def new_img(w=32, h=32):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))

def fill_rect(img, x1, y1, x2, y2, color):
    ImageDraw.Draw(img).rectangle([x1, y1, x2, y2], fill=color)

def pixel(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)

def vline(img, x, y1, y2, color):
    for y in range(y1, y2 + 1):
        pixel(img, x, y, color)

def hline(img, x1, x2, y, color):
    for x in range(x1, x2 + 1):
        pixel(img, x, y, color)

def rivet(img, cx, cy):
    """2x2 rivet at (cx,cy) top-left."""
    pixel(img, cx,   cy,   RIVET_D)
    pixel(img, cx+1, cy,   RIVET_L)
    pixel(img, cx,   cy+1, RIVET_D)
    pixel(img, cx+1, cy+1, RIVET_D)

def riveted_corners(img, w=32, h=32, inset=2):
    rivet(img, inset,       inset)
    rivet(img, w-inset-2,   inset)
    rivet(img, inset,       h-inset-2)
    rivet(img, w-inset-2,   h-inset-2)

def metal_base(img, w=32, h=32):
    """Riveted metal panel base with subtle gradient."""
    fill_rect(img, 0, 0, w-1, h-1, METAL)
    fill_rect(img, 1, 0, w-2, 0, METAL_LT)
    fill_rect(img, 1, h-1, w-2, h-1, DARK)
    fill_rect(img, 0, 0, 0, h-1, DARK)
    fill_rect(img, w-1, 0, w-1, h-1, DARK)
    pixel(img, 1, 1, METAL_XL)
    pixel(img, w-2, 1, METAL_XL)

def warn_stripes(img, x1, y1, x2, y2):
    for y in range(y1, y2+1):
        for x in range(x1, x2+1):
            if ((x + y) // 2) % 2 == 0:
                pixel(img, x, y, WARN_Y)
            else:
                pixel(img, x, y, WARN_K)


# ========= RUST OVERLAY =========

def add_rust(img, seed=0, intensity=1.0, avoid_zone=None):
    """
    Add rust patches and streaks to a metal texture.
    Deterministic via seed so re-runs produce identical results.
    
    avoid_zone = optional (x1, y1, x2, y2) rectangle where rust must NOT be
                 applied (e.g. screens, glowing elements, lights).
    """
    rng = random.Random(seed)
    w, h = img.size
    
    def in_avoid(x, y):
        if avoid_zone is None:
            return False
        x1, y1, x2, y2 = avoid_zone
        return x1 <= x <= x2 and y1 <= y <= y2
    
    def is_metal_pixel(x, y):
        """Only put rust over metal-colored pixels (preserve details)."""
        if not (0 <= x < w and 0 <= y < h):
            return False
        p = img.getpixel((x, y))
        if p[3] == 0:  # transparent
            return False
        # Check if color is in the metal range (grayscale-ish)
        r, g, b = p[0], p[1], p[2]
        # Metal pixels have low saturation and low-mid brightness
        max_c = max(r, g, b)
        min_c = min(r, g, b)
        if max_c - min_c > 30:  # too saturated, probably a colored detail
            return False
        if max_c > 160 or max_c < 15:  # too bright (highlight) or too dark (shadow line)
            return False
        return True
    
    # 1) Random rust patches
    n_patches = int(8 * intensity)
    for _ in range(n_patches):
        cx = rng.randint(2, w - 3)
        cy = rng.randint(2, h - 3)
        if in_avoid(cx, cy):
            continue
        # Cluster of 2-5 rust pixels around (cx, cy)
        size = rng.randint(2, 5)
        for _ in range(size):
            ox = rng.randint(-1, 1)
            oy = rng.randint(-1, 1)
            x, y = cx + ox, cy + oy
            if in_avoid(x, y) or not is_metal_pixel(x, y):
                continue
            color = rng.choice([RUST_DK, RUST_MD, RUST_MD, RUST_LT, RUST_OR])
            pixel(img, x, y, color)
    
    # 2) Vertical rust streaks (drips from rivets)
    n_streaks = int(3 * intensity)
    for _ in range(n_streaks):
        sx = rng.randint(2, w - 3)
        sy_start = rng.randint(2, h // 2)
        length = rng.randint(3, 7)
        for i in range(length):
            y = sy_start + i
            if y >= h - 1 or in_avoid(sx, y):
                break
            if not is_metal_pixel(sx, y):
                break
            # Streak gradient: darker at top, lighter as it goes down
            if i < 2:
                pixel(img, sx, y, RUST_DK)
            elif i < 4:
                pixel(img, sx, y, RUST_MD)
            else:
                pixel(img, sx, y, PATINA)
    
    # 3) Patina spots (subtle olive-brown discoloration)
    n_patina = int(4 * intensity)
    for _ in range(n_patina):
        px = rng.randint(1, w - 2)
        py = rng.randint(1, h - 2)
        if in_avoid(px, py) or not is_metal_pixel(px, py):
            continue
        pixel(img, px, py, PATINA)
    
    return img


# ========= TEXTURE BUILDERS =========

def make_side_frame(phase):
    """
    Generic riveted metal panel with rust + animated status LED.
    LED blinks: green-bright in phase 0, dimmer in phase 3, off in phase 5.
    """
    img = new_img()
    metal_base(img)
    
    # 4 corner rivets + 4 mid-edge rivets
    riveted_corners(img)
    rivet(img, 14, 2)
    rivet(img, 14, 28)
    rivet(img, 2, 14)
    rivet(img, 28, 14)
    
    # Vertical cable (left side)
    vline(img, 6, 5, 26, DARK)
    vline(img, 7, 5, 26, RIVET_L)
    vline(img, 8, 5, 26, DARK)
    for cy in [9, 16, 23]:
        fill_rect(img, 5, cy, 9, cy+1, METAL_XL)
        pixel(img, 5, cy, RIVET_D)
        pixel(img, 9, cy, RIVET_D)
    
    # Stencil plate "01" top-right
    fill_rect(img, 16, 5, 27, 11, DARK)
    fill_rect(img, 16, 5, 27, 5, RIVET_L)
    pixel(img, 18, 7, WARN_Y); pixel(img, 19, 7, WARN_Y); pixel(img, 20, 7, WARN_Y)
    pixel(img, 18, 8, WARN_Y); pixel(img, 20, 8, WARN_Y)
    pixel(img, 18, 9, WARN_Y); pixel(img, 19, 9, WARN_Y); pixel(img, 20, 9, WARN_Y)
    pixel(img, 23, 7, WARN_Y); pixel(img, 24, 7, WARN_Y)
    pixel(img, 24, 8, WARN_Y)
    pixel(img, 23, 9, WARN_Y); pixel(img, 24, 9, WARN_Y); pixel(img, 25, 9, WARN_Y)
    
    # Animated status LED bottom-right
    # Phases: 0=bright on, 1=bright on, 2=mid, 3=dim, 4=off, 5=mid (cycle)
    led_states = [
        (GREEN_LT, GREEN),
        (GREEN_LT, GREEN),
        (GREEN, (40, 100, 50, 255)),
        ((40, 100, 50, 255), (24, 60, 30, 255)),
        ((24, 60, 30, 255), (16, 40, 20, 255)),
        (GREEN, (40, 100, 50, 255)),
    ]
    led_lit, led_dim = led_states[phase]
    fill_rect(img, 22, 22, 25, 25, RIVET_D)
    pixel(img, 23, 23, led_lit)
    pixel(img, 24, 23, led_dim)
    pixel(img, 23, 24, led_dim)
    pixel(img, 24, 24, led_dim)
    
    # Subtle violet bleed center-bottom
    pixel(img, 14, 19, VOSS_DK)
    pixel(img, 15, 19, VOSS_MD)
    pixel(img, 16, 19, VOSS_DK)
    pixel(img, 15, 20, VOSS_DK)
    
    # RUST OVERLAY (skip the cable, the plate, the LED, and the violet bleed)
    add_rust(img, seed=10, intensity=1.2, avoid_zone=(5, 5, 27, 26))
    # Apply rust outside the protected zone too
    add_rust(img, seed=11, intensity=0.8, avoid_zone=(15, 4, 28, 11))  # protect plate "01"
    
    return img

def make_side_animated():
    sheet = Image.new("RGBA", (32, 192), (0, 0, 0, 0))
    for i in range(6):
        sheet.paste(make_side_frame(i), (0, i * 32))
    return sheet

def make_top():
    """Top face of bottom-row blocks. Riveted plate, rusty."""
    img = new_img()
    metal_base(img)
    riveted_corners(img)
    
    # Center seam (cross pattern)
    hline(img, 1, 30, 15, DARK)
    hline(img, 1, 30, 16, RIVET_L)
    vline(img, 15, 1, 30, DARK)
    vline(img, 16, 1, 30, RIVET_L)
    
    # Quadrant rivets
    rivet(img, 7, 7)
    rivet(img, 22, 7)
    rivet(img, 7, 22)
    rivet(img, 22, 22)
    
    # Faint voss glow at center
    pixel(img, 14, 14, VOSS_DK)
    pixel(img, 17, 17, VOSS_DK)
    pixel(img, 14, 17, VOSS_DK)
    pixel(img, 17, 14, VOSS_DK)
    
    # Heavy rust on the top (it's the most exposed face)
    add_rust(img, seed=20, intensity=1.5)
    
    return img

def make_bottom():
    img = new_img()
    fill_rect(img, 0, 0, 31, 31, DARK)
    fill_rect(img, 1, 1, 30, 30, METAL)
    riveted_corners(img, inset=3)
    hline(img, 2, 29, 15, DARK)
    vline(img, 15, 2, 29, DARK)
    
    # Light rust (least exposed face)
    add_rust(img, seed=30, intensity=0.6)
    
    return img

def make_front_frame(phase):
    """Master front - VOSS-7 plate, CRT screen pulsing, 2 buttons, warning band."""
    img = new_img()
    metal_base(img)
    riveted_corners(img)
    rivet(img, 14, 1)
    rivet(img, 14, 29)
    
    # VOSS-7 stencil plate
    fill_rect(img, 4, 3, 27, 8, RIVET_D)
    fill_rect(img, 4, 3, 27, 3, RIVET_L)
    pixel(img, 6, 5, VOSS_LT); pixel(img, 6, 6, VOSS_LT)
    pixel(img, 7, 7, VOSS_LT); pixel(img, 8, 6, VOSS_LT); pixel(img, 8, 5, VOSS_LT)
    pixel(img, 10, 5, VOSS_LT); pixel(img, 11, 5, VOSS_LT)
    pixel(img, 10, 6, VOSS_LT); pixel(img, 11, 6, VOSS_LT)
    pixel(img, 10, 7, VOSS_LT); pixel(img, 11, 7, VOSS_LT)
    pixel(img, 13, 5, VOSS_LT); pixel(img, 14, 5, VOSS_LT)
    pixel(img, 13, 6, VOSS_LT); pixel(img, 14, 6, VOSS_LT)
    pixel(img, 14, 7, VOSS_LT)
    pixel(img, 16, 5, VOSS_LT); pixel(img, 17, 5, VOSS_LT)
    pixel(img, 16, 6, VOSS_LT); pixel(img, 17, 6, VOSS_LT)
    pixel(img, 17, 7, VOSS_LT)
    pixel(img, 19, 6, WARN_Y); pixel(img, 20, 6, WARN_Y)
    pixel(img, 22, 5, WARN_Y); pixel(img, 23, 5, WARN_Y); pixel(img, 24, 5, WARN_Y)
    pixel(img, 24, 6, WARN_Y)
    pixel(img, 23, 7, WARN_Y)
    
    # CRT screen
    fill_rect(img, 7, 11, 24, 22, RIVET_D)
    fill_rect(img, 7, 11, 24, 11, METAL_LT)
    fill_rect(img, 7, 22, 24, 22, DARK)
    fill_rect(img, 8, 12, 23, 21, (16, 8, 28, 255))
    
    cx, cy = 15, 16
    glow_levels = [
        [(cx, cy, VOSS_LT)],
        [(cx, cy, VOSS_GLOW), (cx-1, cy, VOSS_LT), (cx+1, cy, VOSS_LT),
         (cx, cy-1, VOSS_LT), (cx, cy+1, VOSS_LT)],
        [(cx, cy, VOSS_GLOW), (cx-1, cy, VOSS_GLOW), (cx+1, cy, VOSS_GLOW),
         (cx, cy-1, VOSS_GLOW), (cx, cy+1, VOSS_GLOW),
         (cx-2, cy, VOSS_LT), (cx+2, cy, VOSS_LT),
         (cx, cy-2, VOSS_LT), (cx, cy+2, VOSS_LT),
         (cx-1, cy-1, VOSS_LT), (cx+1, cy-1, VOSS_LT),
         (cx-1, cy+1, VOSS_LT), (cx+1, cy+1, VOSS_LT)],
        [(cx, cy, VOSS_GLOW), (cx-1, cy, VOSS_GLOW), (cx+1, cy, VOSS_GLOW),
         (cx, cy-1, VOSS_GLOW), (cx, cy+1, VOSS_GLOW),
         (cx-2, cy, VOSS_LT), (cx+2, cy, VOSS_LT),
         (cx, cy-2, VOSS_LT), (cx, cy+2, VOSS_LT),
         (cx-1, cy-1, VOSS_GLOW), (cx+1, cy-1, VOSS_GLOW),
         (cx-1, cy+1, VOSS_GLOW), (cx+1, cy+1, VOSS_GLOW),
         (cx-2, cy-1, VOSS_LT), (cx+2, cy-1, VOSS_LT),
         (cx-2, cy+1, VOSS_LT), (cx+2, cy+1, VOSS_LT),
         (cx-1, cy-2, VOSS_LT), (cx+1, cy-2, VOSS_LT),
         (cx-1, cy+2, VOSS_LT), (cx+1, cy+2, VOSS_LT),
         (cx-3, cy, VOSS_DK), (cx+3, cy, VOSS_DK),
         (cx, cy-3, VOSS_DK), (cx, cy+3, VOSS_DK)],
        [(cx, cy, VOSS_GLOW), (cx-1, cy, VOSS_LT), (cx+1, cy, VOSS_LT),
         (cx, cy-1, VOSS_LT), (cx, cy+1, VOSS_LT),
         (cx-2, cy, VOSS_DK), (cx+2, cy, VOSS_DK),
         (cx, cy-2, VOSS_DK), (cx, cy+2, VOSS_DK)],
        [(cx, cy, VOSS_LT), (cx-1, cy, VOSS_DK), (cx+1, cy, VOSS_DK)],
    ]
    for px, py, col in glow_levels[phase]:
        pixel(img, px, py, col)
    
    # Scanlines
    for y in range(13, 21, 3):
        for x in range(8, 24):
            cur = img.getpixel((x, y))
            r, g, b, a = cur
            img.putpixel((x, y), (max(0, r-4), max(0, g-4), max(0, b-4), a))
    
    # 2 mechanical buttons
    fill_rect(img, 6, 25, 9, 28, RIVET_D)
    pixel(img, 7, 26, RED_LT)
    pixel(img, 8, 26, RED)
    pixel(img, 7, 27, RED)
    pixel(img, 8, 27, RED_DK)
    fill_rect(img, 22, 25, 25, 28, RIVET_D)
    pixel(img, 23, 26, RED_LT)
    pixel(img, 24, 26, RED)
    pixel(img, 23, 27, RED)
    pixel(img, 24, 27, RED_DK)
    
    warn_stripes(img, 11, 26, 20, 27)
    
    # RUST OVERLAY (avoid the screen, the VOSS-7 plate, the buttons, the warning stripes)
    # Apply rust mostly on the upper edges and around the screen frame
    add_rust(img, seed=40, intensity=1.0, avoid_zone=(3, 2, 28, 9))   # protect VOSS-7 plate
    add_rust(img, seed=41, intensity=0.8, avoid_zone=(7, 11, 24, 22)) # protect CRT screen
    add_rust(img, seed=42, intensity=0.6, avoid_zone=(5, 24, 26, 28)) # protect buttons + warn
    
    return img

def make_front_animated():
    sheet = Image.new("RGBA", (32, 192), (0, 0, 0, 0))
    for i in range(6):
        sheet.paste(make_front_frame(i), (0, i * 32))
    return sheet

def make_energy_frame(phase):
    """Energy input - red lightning bolt animated, RF plate, warning stripe."""
    img = new_img()
    fill_rect(img, 0, 0, 31, 31, DARK)
    fill_rect(img, 1, 1, 30, 30, METAL)
    fill_rect(img, 9, 9, 22, 22, RED_DK)
    fill_rect(img, 10, 10, 21, 21, (140, 30, 24, 255))
    
    riveted_corners(img)
    
    # RF plate
    fill_rect(img, 11, 3, 20, 8, RIVET_D)
    fill_rect(img, 11, 3, 20, 3, RIVET_L)
    pixel(img, 13, 5, RED_LT); pixel(img, 14, 5, RED_LT)
    pixel(img, 13, 6, RED_LT); pixel(img, 15, 6, RED_LT)
    pixel(img, 13, 7, RED_LT); pixel(img, 15, 7, RED_LT)
    pixel(img, 17, 5, RED_LT); pixel(img, 18, 5, RED_LT); pixel(img, 19, 5, RED_LT)
    pixel(img, 17, 6, RED_LT); pixel(img, 18, 6, RED_LT)
    pixel(img, 17, 7, RED_LT)
    
    # Lightning bolt animated brightness
    bolt_colors = [
        (RED_DK, RED),
        (RED, RED_LT),
        (RED_LT, (255, 130, 120, 255)),
        (RED_LT, (255, 160, 150, 255)),
        (RED_LT, (255, 130, 120, 255)),
        (RED, RED_LT),
    ]
    main, glow = bolt_colors[phase]
    bolt_pixels = [
        (16, 11), (17, 11),
        (15, 12), (16, 12), (17, 12),
        (14, 13), (15, 13), (16, 13),
        (14, 14), (15, 14), (16, 14),
        (13, 15), (14, 15), (15, 15),
        (13, 16), (14, 16), (15, 16), (16, 16), (17, 16), (18, 16),
        (15, 17), (16, 17), (17, 17), (18, 17),
        (15, 18), (16, 18), (17, 18),
        (16, 19), (17, 19),
        (16, 20), (17, 20),
        (17, 21),
    ]
    for (px, py) in bolt_pixels:
        pixel(img, px, py, main)
    bolt_glow = [
        (14, 12), (18, 12),
        (13, 13), (17, 13),
        (12, 14), (17, 14),
        (12, 15), (16, 15),
        (12, 16), (19, 16),
        (14, 17), (19, 17),
        (14, 18), (18, 18),
        (15, 19), (18, 19),
        (15, 20), (18, 20),
        (16, 21), (18, 21),
    ]
    for (px, py) in bolt_glow:
        pixel(img, px, py, glow)
    
    warn_stripes(img, 4, 27, 27, 28)
    
    # RUST (avoid the inner red zone and the RF plate)
    add_rust(img, seed=50, intensity=0.8, avoid_zone=(8, 8, 23, 22))
    add_rust(img, seed=51, intensity=0.5, avoid_zone=(10, 2, 21, 9))  # protect RF plate
    
    return img

def make_energy_animated():
    sheet = Image.new("RGBA", (32, 192), (0, 0, 0, 0))
    for i in range(6):
        sheet.paste(make_energy_frame(i), (0, i * 32))
    return sheet

def make_glass_frame(phase):
    """
    Industrial porthole glass with cross armature.
    Animated: center boss pulses violet through 6 phases.
    """
    img = new_img()
    
    # Glass background
    fill_rect(img, 1, 1, 30, 30, GLASS_BG)
    
    # Outer frame
    fill_rect(img, 0, 0, 31, 1, METAL)
    fill_rect(img, 0, 30, 31, 31, DARK)
    fill_rect(img, 0, 0, 1, 31, METAL)
    fill_rect(img, 30, 0, 31, 31, DARK)
    pixel(img, 0, 0, METAL_XL)
    pixel(img, 31, 0, METAL_LT)
    pixel(img, 0, 31, RIVET_D)
    pixel(img, 31, 31, RIVET_D)
    
    rivet(img, 3, 3)
    rivet(img, 27, 3)
    rivet(img, 3, 27)
    rivet(img, 27, 27)
    
    # Cross armature
    fill_rect(img, 14, 2, 17, 29, METAL)
    fill_rect(img, 2, 14, 29, 17, METAL)
    fill_rect(img, 14, 2, 17, 2, METAL_LT)
    fill_rect(img, 2, 14, 29, 14, METAL_LT)
    fill_rect(img, 14, 17, 17, 17, DARK)
    fill_rect(img, 2, 17, 29, 17, DARK)
    
    # Center boss with animated pulse
    fill_rect(img, 13, 13, 18, 18, METAL_LT)
    
    # 6 boss pulse phases
    boss_phases = [
        # phase 0: dim
        [(VOSS_DK, [(14, 14), (15, 14), (16, 14), (17, 14),
                    (14, 15), (15, 15), (16, 15), (17, 15),
                    (14, 16), (15, 16), (16, 16), (17, 16),
                    (14, 17), (15, 17), (16, 17), (17, 17)])],
        # phase 1: rising
        [(VOSS_DK, [(14, 14), (17, 14), (14, 17), (17, 17)]),
         (VOSS_MD, [(15, 14), (16, 14), (14, 15), (17, 15),
                    (14, 16), (17, 16), (15, 17), (16, 17)]),
         (VOSS_LT, [(15, 15), (16, 15), (15, 16), (16, 16)])],
        # phase 2: brighter
        [(VOSS_MD, [(14, 14), (17, 14), (14, 17), (17, 17)]),
         (VOSS_LT, [(15, 14), (16, 14), (14, 15), (17, 15),
                    (14, 16), (17, 16), (15, 17), (16, 17)]),
         (VOSS_GLOW, [(15, 15), (16, 15), (15, 16), (16, 16)])],
        # phase 3: peak
        [(VOSS_LT, [(14, 14), (17, 14), (14, 17), (17, 17)]),
         (VOSS_GLOW, [(15, 14), (16, 14), (14, 15), (17, 15),
                      (14, 16), (17, 16), (15, 17), (16, 17),
                      (15, 15), (16, 15), (15, 16), (16, 16)])],
        # phase 4: receding
        [(VOSS_MD, [(14, 14), (17, 14), (14, 17), (17, 17)]),
         (VOSS_LT, [(15, 14), (16, 14), (14, 15), (17, 15),
                    (14, 16), (17, 16), (15, 17), (16, 17)]),
         (VOSS_GLOW, [(15, 15), (16, 15), (15, 16), (16, 16)])],
        # phase 5: dim again
        [(VOSS_DK, [(14, 14), (17, 14), (14, 17), (17, 17)]),
         (VOSS_MD, [(15, 14), (16, 14), (14, 15), (17, 15),
                    (14, 16), (17, 16), (15, 17), (16, 17)]),
         (VOSS_LT, [(15, 15), (16, 15), (15, 16), (16, 16)])],
    ]
    for color, coords in boss_phases[phase]:
        for (px, py) in coords:
            pixel(img, px, py, color)
    
    # Reflections
    pixel(img, 6, 6, GLASS_LT)
    pixel(img, 7, 6, GLASS_LT)
    pixel(img, 6, 7, GLASS_LT)
    pixel(img, 24, 24, GLASS_LT)
    pixel(img, 25, 24, GLASS_LT)
    pixel(img, 24, 25, GLASS_LT)
    
    # Subtle rust on the metal frame and cross (not the glass)
    # Apply rust only on the cross beams and outer frame
    add_rust(img, seed=60, intensity=0.7, avoid_zone=(2, 2, 29, 13))   # protect upper glass
    add_rust(img, seed=61, intensity=0.7, avoid_zone=(2, 18, 29, 29))  # protect lower glass
    add_rust(img, seed=62, intensity=0.5, avoid_zone=(13, 13, 18, 18)) # protect center boss
    
    return img

def make_glass_animated():
    sheet = Image.new("RGBA", (32, 192), (0, 0, 0, 0))
    for i in range(6):
        sheet.paste(make_glass_frame(i), (0, i * 32))
    return sheet

def make_top_wall_top_frame(phase):
    """
    Roof exhaust valve - animated, glow comes out of the grille.
    """
    img = new_img()
    metal_base(img)
    riveted_corners(img)
    fill_rect(img, 1, 1, 30, 1, METAL_LT)
    
    # Central exhaust valve
    fill_rect(img, 10, 10, 21, 21, RIVET_D)
    fill_rect(img, 11, 10, 20, 10, METAL_LT)
    fill_rect(img, 11, 21, 20, 21, DARK)
    fill_rect(img, 10, 11, 10, 20, METAL_LT)
    fill_rect(img, 21, 11, 21, 20, DARK)
    
    # Inner darker
    fill_rect(img, 12, 12, 19, 19, (16, 8, 24, 255))
    
    # Grille bars
    hline(img, 13, 18, 14, METAL)
    hline(img, 13, 18, 16, METAL)
    hline(img, 13, 18, 18, METAL)
    
    # Animated violet glow inside the valve (between the grille bars)
    glow_phases = [
        # phase 0: minimal
        [(VOSS_DK, [(15, 15), (16, 15), (15, 17), (16, 17)])],
        # phase 1
        [(VOSS_DK, [(15, 15), (16, 15), (15, 17), (16, 17)]),
         (VOSS_MD, [(14, 13), (17, 13), (14, 15), (17, 15), (14, 17), (17, 17)])],
        # phase 2
        [(VOSS_MD, [(15, 15), (16, 15), (15, 17), (16, 17),
                    (14, 13), (17, 13), (14, 15), (17, 15), (14, 17), (17, 17)]),
         (VOSS_LT, [(15, 13), (16, 13)])],
        # phase 3: peak
        [(VOSS_LT, [(15, 15), (16, 15), (15, 17), (16, 17),
                    (15, 13), (16, 13), (14, 13), (17, 13)]),
         (VOSS_GLOW, [(14, 15), (17, 15), (14, 17), (17, 17)])],
        # phase 4: receding
        [(VOSS_MD, [(15, 15), (16, 15), (15, 17), (16, 17),
                    (14, 13), (17, 13), (14, 15), (17, 15), (14, 17), (17, 17)]),
         (VOSS_LT, [(15, 13), (16, 13)])],
        # phase 5: dim
        [(VOSS_DK, [(15, 15), (16, 15), (15, 17), (16, 17)]),
         (VOSS_MD, [(14, 13), (17, 13)])],
    ]
    for color, coords in glow_phases[phase]:
        for (px, py) in coords:
            pixel(img, px, py, color)
    
    # 4 valve mount bolts
    rivet(img, 9, 9)
    rivet(img, 21, 9)
    rivet(img, 9, 21)
    rivet(img, 21, 21)
    
    # Up arrow at top
    pixel(img, 15, 4, WARN_Y); pixel(img, 16, 4, WARN_Y)
    pixel(img, 14, 5, WARN_Y); pixel(img, 17, 5, WARN_Y)
    pixel(img, 13, 6, WARN_Y); pixel(img, 18, 6, WARN_Y)
    pixel(img, 15, 5, WARN_Y); pixel(img, 16, 5, WARN_Y)
    pixel(img, 15, 6, WARN_Y); pixel(img, 16, 6, WARN_Y)
    pixel(img, 15, 7, WARN_Y); pixel(img, 16, 7, WARN_Y)
    
    # Down arrow at bottom
    pixel(img, 15, 24, WARN_Y); pixel(img, 16, 24, WARN_Y)
    pixel(img, 15, 25, WARN_Y); pixel(img, 16, 25, WARN_Y)
    pixel(img, 15, 26, WARN_Y); pixel(img, 16, 26, WARN_Y)
    pixel(img, 14, 26, WARN_Y); pixel(img, 17, 26, WARN_Y)
    pixel(img, 13, 27, WARN_Y); pixel(img, 18, 27, WARN_Y)
    pixel(img, 15, 27, WARN_Y); pixel(img, 16, 27, WARN_Y)
    
    # RUST on the outer plate (avoid the valve and arrows)
    add_rust(img, seed=70, intensity=1.3, avoid_zone=(9, 9, 22, 22))   # protect valve
    add_rust(img, seed=71, intensity=0.6, avoid_zone=(13, 4, 18, 7))   # protect up arrow
    add_rust(img, seed=72, intensity=0.6, avoid_zone=(13, 24, 18, 28)) # protect down arrow
    
    return img

def make_top_wall_top_animated():
    sheet = Image.new("RGBA", (32, 192), (0, 0, 0, 0))
    for i in range(6):
        sheet.paste(make_top_wall_top_frame(i), (0, i * 32))
    return sheet


# ========= MCMETA =========

MCMETA_CONTENT = {"animation": {"frametime": 3, "interpolate": True}}


# ========= MAIN =========

def main():
    out_dir = "mod-source/src/main/resources/assets/nexusabsolu/textures/blocks"
    os.makedirs(out_dir, exist_ok=True)
    
    # Static textures (with rust)
    static_files = {
        "condenseur_formed_top.png":          make_top(),
        "condenseur_formed_bottom.png":       make_bottom(),
    }
    
    # Animated textures (6 frames each, 32x192)
    animated_files = {
        "condenseur_formed_front.png":          make_front_animated(),
        "condenseur_formed_energy.png":         make_energy_animated(),
        "condenseur_formed_side.png":           make_side_animated(),
        "condenseur_formed_glass.png":          make_glass_animated(),
        "condenseur_formed_top_wall_top.png":   make_top_wall_top_animated(),
    }
    
    print("=== Static textures (with rust) ===")
    for name, img in static_files.items():
        path = os.path.join(out_dir, name)
        img.save(path)
        print(f"  {img.size[0]:3d}x{img.size[1]:3d}  {name}")
    
    print("\n=== Animated textures (6 frames, with rust) ===")
    for name, img in animated_files.items():
        path = os.path.join(out_dir, name)
        img.save(path)
        # Write/update the .mcmeta file
        meta_path = path + ".mcmeta"
        with open(meta_path, "w") as f:
            json.dump(MCMETA_CONTENT, f)
        print(f"  {img.size[0]:3d}x{img.size[1]:3d}  {name}  + .mcmeta")
    
    total = len(static_files) + len(animated_files)
    print(f"\n{total} textures generated ({len(animated_files)} animated).")

if __name__ == "__main__":
    main()
