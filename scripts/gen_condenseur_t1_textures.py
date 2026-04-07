#!/usr/bin/env python3
"""
Generate Condenseur T1 formed-block textures.

Theme: 'Le Bricolage de Voss' - industrial-vintage proto-reactor,
hand-soldered in a prison cell. Riveted metal plates, exposed cables,
worn paint, voss-7 markings, faint violet energy bleeding through.

Output: PNG files in mod-source/.../textures/blocks/
Resolution: 32x32 (animated: 32x192 = 6 frames)
"""

import os
from PIL import Image, ImageDraw

# ========= PALETTE =========
DARK     = (32, 32, 36, 255)        # base metal shadow
METAL    = (58, 58, 66, 255)        # base metal
METAL_LT = (84, 84, 94, 255)        # metal highlight
METAL_XL = (118, 118, 128, 255)     # metal extra light edge
RIVET_D  = (20, 20, 24, 255)        # rivet shadow
RIVET_L  = (140, 140, 150, 255)     # rivet highlight

VOSS_DK  = (74, 38, 110, 255)       # voss violet dark
VOSS_MD  = (122, 64, 178, 255)      # voss violet mid
VOSS_LT  = (170, 110, 220, 255)     # voss violet bright
VOSS_GLOW= (200, 150, 240, 255)     # voss violet glow

WARN_Y   = (212, 176, 64, 255)      # warning yellow
WARN_K   = (24, 24, 24, 255)        # warning black
GREEN    = (70, 180, 88, 255)       # status ok
GREEN_LT = (140, 230, 150, 255)
RED      = (196, 64, 56, 255)       # redstone red
RED_LT   = (232, 92, 84, 255)
RED_DK   = (110, 24, 20, 255)

GLASS_BG = (60, 50, 80, 96)         # transparent violet glass
GLASS_LT = (140, 110, 180, 140)

# ========= HELPERS =========
def new_img(w=32, h=32):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))

def fill_rect(img, x1, y1, x2, y2, color):
    d = ImageDraw.Draw(img)
    d.rectangle([x1, y1, x2, y2], fill=color)

def pixel(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)

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
    # Top highlight band
    fill_rect(img, 1, 0, w-2, 0, METAL_LT)
    # Bottom shadow band
    fill_rect(img, 1, h-1, w-2, h-1, DARK)
    # Side darkening
    fill_rect(img, 0, 0, 0, h-1, DARK)
    fill_rect(img, w-1, 0, w-1, h-1, DARK)
    # Edge highlights
    pixel(img, 1, 1, METAL_XL)
    pixel(img, w-2, 1, METAL_XL)

def warn_stripes(img, x1, y1, x2, y2):
    """Diagonal yellow/black warning stripes."""
    for y in range(y1, y2+1):
        for x in range(x1, x2+1):
            if ((x + y) // 2) % 2 == 0:
                pixel(img, x, y, WARN_Y)
            else:
                pixel(img, x, y, WARN_K)

def vline(img, x, y1, y2, color):
    for y in range(y1, y2+1):
        pixel(img, x, y, color)

def hline(img, x1, x2, y, color):
    for x in range(x1, x2+1):
        pixel(img, x, y, color)

# ========= TEXTURES =========

def make_side():
    """
    Generic riveted metal panel - used for: walls bas (sides),
    master (E/S sides), top_wall (sides). Must work everywhere.
    
    Layout (32x32):
    - Riveted metal base
    - Plate "01" stencil top-right
    - Vertical cable left side
    - Status LED bottom-right
    - Yellow/black warning band on bottom edge
    """
    img = new_img()
    metal_base(img)
    
    # 4 corner rivets
    riveted_corners(img)
    # Edge rivets (mid-edges)
    rivet(img, 14, 2)
    rivet(img, 14, 28)
    rivet(img, 2, 14)
    rivet(img, 28, 14)
    
    # Vertical cable (left side, runs top to bottom)
    vline(img, 6, 5, 26, DARK)
    vline(img, 7, 5, 26, RIVET_L)
    vline(img, 8, 5, 26, DARK)
    # Cable clips (3 along the way)
    for cy in [9, 16, 23]:
        fill_rect(img, 5, cy, 9, cy+1, METAL_XL)
        pixel(img, 5, cy, RIVET_D)
        pixel(img, 9, cy, RIVET_D)
    
    # Stencil plate "01" top-right
    fill_rect(img, 16, 5, 27, 11, DARK)
    fill_rect(img, 16, 5, 27, 5, RIVET_L)  # top edge
    # "0" 
    pixel(img, 18, 7, WARN_Y); pixel(img, 19, 7, WARN_Y); pixel(img, 20, 7, WARN_Y)
    pixel(img, 18, 8, WARN_Y); pixel(img, 20, 8, WARN_Y)
    pixel(img, 18, 9, WARN_Y); pixel(img, 19, 9, WARN_Y); pixel(img, 20, 9, WARN_Y)
    # "1"
    pixel(img, 23, 7, WARN_Y); pixel(img, 24, 7, WARN_Y)
    pixel(img, 24, 8, WARN_Y)
    pixel(img, 23, 9, WARN_Y); pixel(img, 24, 9, WARN_Y); pixel(img, 25, 9, WARN_Y)
    
    # Status LED bottom-right (green = OK)
    fill_rect(img, 22, 22, 25, 25, RIVET_D)
    pixel(img, 23, 23, GREEN_LT)
    pixel(img, 24, 23, GREEN)
    pixel(img, 23, 24, GREEN)
    pixel(img, 24, 24, GREEN)
    
    # Subtle violet bleed in seams (bottom-center, faint)
    pixel(img, 14, 19, VOSS_DK)
    pixel(img, 15, 19, VOSS_MD)
    pixel(img, 16, 19, VOSS_DK)
    pixel(img, 15, 20, VOSS_DK)
    
    return img

def make_top():
    """
    Top face of bottom-row blocks (walls bas).
    Plate metal seen from above.
    """
    img = new_img()
    metal_base(img)
    riveted_corners(img)
    
    # Center seam/joint (cross pattern showing 4 plates joined)
    hline(img, 1, 30, 15, DARK)
    hline(img, 1, 30, 16, RIVET_L)
    vline(img, 15, 1, 30, DARK)
    vline(img, 16, 1, 30, RIVET_L)
    
    # 4 quadrant rivets
    rivet(img, 7, 7)
    rivet(img, 22, 7)
    rivet(img, 7, 22)
    rivet(img, 22, 22)
    
    # Faint voss glow at center
    pixel(img, 14, 14, VOSS_DK)
    pixel(img, 17, 17, VOSS_DK)
    pixel(img, 14, 17, VOSS_DK)
    pixel(img, 17, 14, VOSS_DK)
    
    return img

def make_bottom():
    """Bottom face - rarely seen, simple but consistent."""
    img = new_img()
    fill_rect(img, 0, 0, 31, 31, DARK)
    fill_rect(img, 1, 1, 30, 30, METAL)
    
    # 4 corner rivets
    riveted_corners(img, inset=3)
    
    # Center cross seam
    hline(img, 2, 29, 15, DARK)
    vline(img, 15, 2, 29, DARK)
    
    return img

def make_front_frame(phase):
    """
    Master front face - 1 frame of 6.
    Layout:
    - Top: VOSS-7 logo plate stenciled
    - Center: large CRT screen with animated pulsing gauge
    - Bottom: 2 mechanical buttons + warning stripe
    - 4 corner rivets
    """
    img = new_img()
    metal_base(img)
    
    # Outer frame rivets (6 total)
    riveted_corners(img)
    rivet(img, 14, 1)
    rivet(img, 14, 29)
    
    # Top plate "VOSS-7" stencil
    fill_rect(img, 4, 3, 27, 8, RIVET_D)
    fill_rect(img, 4, 3, 27, 3, RIVET_L)
    # V
    pixel(img, 6, 5, VOSS_LT); pixel(img, 6, 6, VOSS_LT)
    pixel(img, 7, 7, VOSS_LT); pixel(img, 8, 6, VOSS_LT); pixel(img, 8, 5, VOSS_LT)
    # O
    pixel(img, 10, 5, VOSS_LT); pixel(img, 11, 5, VOSS_LT)
    pixel(img, 10, 6, VOSS_LT); pixel(img, 11, 6, VOSS_LT)
    pixel(img, 10, 7, VOSS_LT); pixel(img, 11, 7, VOSS_LT)
    # S
    pixel(img, 13, 5, VOSS_LT); pixel(img, 14, 5, VOSS_LT)
    pixel(img, 13, 6, VOSS_LT); pixel(img, 14, 6, VOSS_LT)
    pixel(img, 14, 7, VOSS_LT)
    # S
    pixel(img, 16, 5, VOSS_LT); pixel(img, 17, 5, VOSS_LT)
    pixel(img, 16, 6, VOSS_LT); pixel(img, 17, 6, VOSS_LT)
    pixel(img, 17, 7, VOSS_LT)
    # -
    pixel(img, 19, 6, WARN_Y); pixel(img, 20, 6, WARN_Y)
    # 7
    pixel(img, 22, 5, WARN_Y); pixel(img, 23, 5, WARN_Y); pixel(img, 24, 5, WARN_Y)
    pixel(img, 24, 6, WARN_Y)
    pixel(img, 23, 7, WARN_Y)
    
    # CRT screen (center, ~14x12)
    fill_rect(img, 7, 11, 24, 22, RIVET_D)
    # Screen bezel highlight
    fill_rect(img, 7, 11, 24, 11, METAL_LT)
    fill_rect(img, 7, 22, 24, 22, DARK)
    # Screen background (very dark violet)
    fill_rect(img, 8, 12, 23, 21, (16, 8, 28, 255))
    
    # Animated pulse: 6 phases - radial pulse from center
    cx, cy = 15, 16
    # Phase 0..5 -> different glow radii / brightness
    glow_levels = [
        # phase 0: minimal
        [(cx, cy, VOSS_LT)],
        # phase 1: small +
        [(cx, cy, VOSS_GLOW), (cx-1, cy, VOSS_LT), (cx+1, cy, VOSS_LT),
         (cx, cy-1, VOSS_LT), (cx, cy+1, VOSS_LT)],
        # phase 2: bigger
        [(cx, cy, VOSS_GLOW), (cx-1, cy, VOSS_GLOW), (cx+1, cy, VOSS_GLOW),
         (cx, cy-1, VOSS_GLOW), (cx, cy+1, VOSS_GLOW),
         (cx-2, cy, VOSS_LT), (cx+2, cy, VOSS_LT),
         (cx, cy-2, VOSS_LT), (cx, cy+2, VOSS_LT),
         (cx-1, cy-1, VOSS_LT), (cx+1, cy-1, VOSS_LT),
         (cx-1, cy+1, VOSS_LT), (cx+1, cy+1, VOSS_LT)],
        # phase 3: peak
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
        # phase 4: receding
        [(cx, cy, VOSS_GLOW), (cx-1, cy, VOSS_LT), (cx+1, cy, VOSS_LT),
         (cx, cy-1, VOSS_LT), (cx, cy+1, VOSS_LT),
         (cx-2, cy, VOSS_DK), (cx+2, cy, VOSS_DK),
         (cx, cy-2, VOSS_DK), (cx, cy+2, VOSS_DK)],
        # phase 5: minimal again
        [(cx, cy, VOSS_LT), (cx-1, cy, VOSS_DK), (cx+1, cy, VOSS_DK)],
    ]
    for px, py, col in glow_levels[phase]:
        pixel(img, px, py, col)
    
    # Static screen scanlines (every 3 rows, very subtle)
    for y in range(13, 21, 3):
        for x in range(8, 24):
            cur = img.getpixel((x, y))
            r, g, b, a = cur
            img.putpixel((x, y), (max(0, r-4), max(0, g-4), max(0, b-4), a))
    
    # Bottom: 2 mechanical buttons (red caps) + warning band
    # Button 1
    fill_rect(img, 6, 25, 9, 28, RIVET_D)
    pixel(img, 7, 26, RED_LT)
    pixel(img, 8, 26, RED)
    pixel(img, 7, 27, RED)
    pixel(img, 8, 27, RED_DK)
    # Button 2
    fill_rect(img, 22, 25, 25, 28, RIVET_D)
    pixel(img, 23, 26, RED_LT)
    pixel(img, 24, 26, RED)
    pixel(img, 23, 27, RED)
    pixel(img, 24, 27, RED_DK)
    
    # Warning stripes between buttons
    warn_stripes(img, 11, 26, 20, 27)
    
    return img

def make_front_animated():
    """6 frames stacked vertically -> 32x192."""
    sheet = Image.new("RGBA", (32, 192), (0, 0, 0, 0))
    for i in range(6):
        f = make_front_frame(i)
        sheet.paste(f, (0, i * 32))
    return sheet

def make_energy_frame(phase):
    """
    Energy input - keeps the 'redstone block' character.
    Red lightning bolt animated, surrounded by metal frame with rivets
    and 'RF' plate.
    """
    img = new_img()
    
    # Base: dark metal with red tint (suggests redstone bleeding through)
    fill_rect(img, 0, 0, 31, 31, DARK)
    fill_rect(img, 1, 1, 30, 30, METAL)
    # Red glow seeping in from center
    fill_rect(img, 9, 9, 22, 22, RED_DK)
    fill_rect(img, 10, 10, 21, 21, (140, 30, 24, 255))
    
    riveted_corners(img)
    
    # Top "RF" plate
    fill_rect(img, 11, 3, 20, 8, RIVET_D)
    fill_rect(img, 11, 3, 20, 3, RIVET_L)
    # R
    pixel(img, 13, 5, RED_LT); pixel(img, 14, 5, RED_LT)
    pixel(img, 13, 6, RED_LT); pixel(img, 15, 6, RED_LT)
    pixel(img, 13, 7, RED_LT); pixel(img, 15, 7, RED_LT)
    # F
    pixel(img, 17, 5, RED_LT); pixel(img, 18, 5, RED_LT); pixel(img, 19, 5, RED_LT)
    pixel(img, 17, 6, RED_LT); pixel(img, 18, 6, RED_LT)
    pixel(img, 17, 7, RED_LT)
    
    # Lightning bolt - animated brightness based on phase
    # Brightness modulation
    bolt_colors = [
        (RED_DK, RED),                    # 0: dim
        (RED, RED_LT),                    # 1
        (RED_LT, (255, 130, 120, 255)),   # 2: bright
        (RED_LT, (255, 160, 150, 255)),   # 3: peak
        (RED_LT, (255, 130, 120, 255)),   # 4
        (RED, RED_LT),                    # 5
    ]
    main, glow = bolt_colors[phase]
    
    # Lightning bolt shape (centered ~14-18 x 11-22)
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
    # Glow halo
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
    
    # Bottom warning stripe
    warn_stripes(img, 4, 27, 27, 28)
    
    return img

def make_energy_animated():
    sheet = Image.new("RGBA", (32, 192), (0, 0, 0, 0))
    for i in range(6):
        f = make_energy_frame(i)
        sheet.paste(f, (0, i * 32))
    return sheet

def make_glass():
    """
    Industrial porthole glass - violet tinted, with metal cross frame.
    Used for top glass blocks (positions 4-6).
    """
    img = new_img()
    
    # Glass tint background (semi-transparent violet)
    fill_rect(img, 1, 1, 30, 30, GLASS_BG)
    
    # Outer frame (riveted metal)
    fill_rect(img, 0, 0, 31, 1, METAL)
    fill_rect(img, 0, 30, 31, 31, DARK)
    fill_rect(img, 0, 0, 1, 31, METAL)
    fill_rect(img, 30, 0, 31, 31, DARK)
    # Frame highlights
    pixel(img, 0, 0, METAL_XL)
    pixel(img, 31, 0, METAL_LT)
    pixel(img, 0, 31, RIVET_D)
    pixel(img, 31, 31, RIVET_D)
    
    # 4 corner rivets (just inside the frame)
    rivet(img, 3, 3)
    rivet(img, 27, 3)
    rivet(img, 3, 27)
    rivet(img, 27, 27)
    
    # Cross armature (vertical + horizontal beams, 2px thick)
    fill_rect(img, 14, 2, 17, 29, METAL)
    fill_rect(img, 2, 14, 29, 17, METAL)
    # Highlight on top of beams
    fill_rect(img, 14, 2, 17, 2, METAL_LT)
    fill_rect(img, 2, 14, 29, 14, METAL_LT)
    # Shadow under beams
    fill_rect(img, 14, 17, 17, 17, DARK)
    fill_rect(img, 2, 17, 29, 17, DARK)
    
    # Center boss (where beams cross)
    fill_rect(img, 13, 13, 18, 18, METAL_LT)
    fill_rect(img, 14, 14, 17, 17, VOSS_DK)
    pixel(img, 15, 15, VOSS_LT)
    pixel(img, 16, 15, VOSS_LT)
    pixel(img, 15, 16, VOSS_LT)
    pixel(img, 16, 16, VOSS_GLOW)
    
    # Light reflections on glass quadrants (subtle)
    pixel(img, 6, 6, GLASS_LT)
    pixel(img, 7, 6, GLASS_LT)
    pixel(img, 6, 7, GLASS_LT)
    pixel(img, 24, 24, GLASS_LT)
    pixel(img, 25, 24, GLASS_LT)
    pixel(img, 24, 25, GLASS_LT)
    
    return img

def make_top_wall_top():
    """
    Top face of the top_wall block (position 7).
    The 'roof' of the structure - has an exhaust valve in the center.
    """
    img = new_img()
    metal_base(img)
    riveted_corners(img)
    
    # Outer plate seam
    fill_rect(img, 1, 1, 30, 1, METAL_LT)
    
    # Central exhaust valve (circular)
    # Outer ring
    fill_rect(img, 10, 10, 21, 21, RIVET_D)
    fill_rect(img, 11, 10, 20, 10, METAL_LT)  # top edge
    fill_rect(img, 11, 21, 20, 21, DARK)      # bottom edge
    fill_rect(img, 10, 11, 10, 20, METAL_LT)  # left edge
    fill_rect(img, 21, 11, 21, 20, DARK)      # right edge
    # Inner darker
    fill_rect(img, 12, 12, 19, 19, (16, 8, 24, 255))
    # Grille bars (3 horizontal slits)
    hline(img, 13, 18, 14, METAL)
    hline(img, 13, 18, 16, METAL)
    hline(img, 13, 18, 18, METAL)
    # Faint violet glow inside
    pixel(img, 15, 15, VOSS_DK)
    pixel(img, 16, 16, VOSS_DK)
    pixel(img, 15, 17, VOSS_DK)
    
    # 4 valve mount bolts
    rivet(img, 9, 9)
    rivet(img, 21, 9)
    rivet(img, 9, 21)
    rivet(img, 21, 21)
    
    # "EXHAUST" text - too small in 32px, just an arrow up at top
    pixel(img, 15, 4, WARN_Y); pixel(img, 16, 4, WARN_Y)
    pixel(img, 14, 5, WARN_Y); pixel(img, 17, 5, WARN_Y)
    pixel(img, 13, 6, WARN_Y); pixel(img, 18, 6, WARN_Y)
    pixel(img, 15, 5, WARN_Y); pixel(img, 16, 5, WARN_Y)
    pixel(img, 15, 6, WARN_Y); pixel(img, 16, 6, WARN_Y)
    pixel(img, 15, 7, WARN_Y); pixel(img, 16, 7, WARN_Y)
    
    # Down arrow at bottom (also EXHAUST direction indicator)
    pixel(img, 15, 24, WARN_Y); pixel(img, 16, 24, WARN_Y)
    pixel(img, 15, 25, WARN_Y); pixel(img, 16, 25, WARN_Y)
    pixel(img, 15, 26, WARN_Y); pixel(img, 16, 26, WARN_Y)
    pixel(img, 14, 26, WARN_Y); pixel(img, 17, 26, WARN_Y)
    pixel(img, 13, 27, WARN_Y); pixel(img, 18, 27, WARN_Y)
    pixel(img, 15, 27, WARN_Y); pixel(img, 16, 27, WARN_Y)
    
    return img

# ========= MAIN =========

def main():
    out_dir = "mod-source/src/main/resources/assets/nexusabsolu/textures/blocks"
    os.makedirs(out_dir, exist_ok=True)
    
    files = {
        "condenseur_formed_side.png":         make_side(),
        "condenseur_formed_top.png":          make_top(),
        "condenseur_formed_bottom.png":       make_bottom(),
        "condenseur_formed_front.png":        make_front_animated(),
        "condenseur_formed_energy.png":       make_energy_animated(),
        "condenseur_formed_glass.png":        make_glass(),
        "condenseur_formed_top_wall_top.png": make_top_wall_top(),
    }
    
    for name, img in files.items():
        path = os.path.join(out_dir, name)
        img.save(path)
        print(f"  wrote {img.size[0]:3d}x{img.size[1]:3d}  {path}")
    
    print(f"\n{len(files)} textures generated.")

if __name__ == "__main__":
    main()
