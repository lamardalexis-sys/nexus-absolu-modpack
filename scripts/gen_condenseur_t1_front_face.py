#!/usr/bin/env python3
"""
Condenseur T1 - cohesive front face PoC.

Generates:
1. condenseur_t1_face_front.png (128x128) - the COMPLETE front facade
   of the 2x2x2 multiblock as a single coherent image
2. 4 model JSONs (POS 0, 1, 4, 5) that UV-split this texture across
   their north faces, making the multiblock front face show the
   coherent machine when assembled

Spatial layout (looking NORTH at the multiblock front):
   +-------------+-------------+
   |   POS 4     |   POS 5     |   <- top row (glass blocks)
   | (above 0)   | (above 1)   |
   +-------------+-------------+
   |   POS 0     |   POS 1     |   <- bottom row
   |  (master)   | (wall +X)   |
   +-------------+-------------+

Each block uses its quarter of the 128x128 texture:
   POS 4 -> top-left      uv [0, 0, 8, 8]      texture [0..64, 0..64]
   POS 5 -> top-right     uv [8, 0, 16, 8]     texture [64..128, 0..64]
   POS 0 -> bottom-left   uv [0, 8, 8, 16]     texture [0..64, 64..128]
   POS 1 -> bottom-right  uv [8, 8, 16, 16]    texture [64..128, 64..128]
"""

import json
import os
from PIL import Image, ImageDraw

TEX_OUT = "mod-source/src/main/resources/assets/nexusabsolu/textures/blocks"
MODEL_OUT = "mod-source/src/main/resources/assets/nexusabsolu/models/block"
BLOCKSTATE_OUT = "mod-source/src/main/resources/assets/nexusabsolu/blockstates"

# ========= PALETTE (consistent with existing textures) =========
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

WARN_Y = (212, 176, 64, 255)
WARN_K = (24, 24, 24, 255)
GREEN  = (70, 180, 88, 255)
GREEN_LT = (140, 230, 150, 255)
RED    = (196, 64, 56, 255)
RED_LT = (232, 92, 84, 255)
RED_DK = (110, 24, 20, 255)

GLASS_BG = (60, 50, 80, 96)
GLASS_LT = (140, 110, 180, 140)


# ========= HELPERS =========

def fill(img, x1, y1, x2, y2, color):
    ImageDraw.Draw(img).rectangle([x1, y1, x2, y2], fill=color)

def px(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)

def hline(img, x1, x2, y, c):
    for x in range(x1, x2 + 1):
        px(img, x, y, c)

def vline(img, x, y1, y2, c):
    for y in range(y1, y2 + 1):
        px(img, x, y, c)

def rivet(img, cx, cy):
    """4x4 rivet (we're at 4x scale of vanilla now: 64px per block)"""
    fill(img, cx, cy, cx + 1, cy + 1, RIVET_D)
    px(img, cx, cy, RIVET_L)

def riveted_corners(img, x1, y1, x2, y2, inset=3):
    rivet(img, x1 + inset,    y1 + inset)
    rivet(img, x2 - inset - 1, y1 + inset)
    rivet(img, x1 + inset,    y2 - inset - 1)
    rivet(img, x2 - inset - 1, y2 - inset - 1)


# ========= FRONT FACE TEXTURE (128x128) =========

def make_front_face():
    """
    The complete front facade of the Condenseur T1 multiblock as one
    coherent image. Layout:
    
    +---------------------+---------------------+
    |                                           |  <- TOP HALF (glass top)
    |  Crossed glass armature with central      |
    |  observation porthole (boss central       |
    |  spans the seam between POS 4 and POS 5)  |
    |                                           |
    +---------------------+---------------------+
    |                     |                     |  <- BOTTOM HALF
    |  MASTER CONSOLE     |  CONTROL PANEL      |
    |  - Big screen       |  - Plate '01'       |
    |  - VOSS-7 plate     |  - Status LEDs      |
    |  - 2 buttons        |  - Lever            |
    |  - Warning band     |  - Cable strip      |
    |  (POS 0)            |  (POS 1)            |
    +---------------------+---------------------+
    
    Note: 128x128 = 64px per sub-block (4x vanilla resolution)
    """
    img = Image.new("RGBA", (128, 128), DARK)
    
    # Whole face base metal
    fill(img, 0, 0, 127, 127, METAL)
    
    # ============ TOP HALF - Glass armature (POS 4 + POS 5) ============
    
    # Glass background tint (translucent violet)
    fill(img, 4, 4, 123, 60, GLASS_BG)
    
    # Outer frame (riveted metal around the glass region)
    fill(img, 0, 0, 127, 3, METAL)        # top edge
    fill(img, 0, 60, 127, 63, METAL)      # bottom edge of glass region
    fill(img, 0, 0, 3, 63, METAL)         # left edge
    fill(img, 124, 0, 127, 63, METAL)     # right edge
    
    # Rivets at the 4 corners + middle edges of the top frame
    for (rx, ry) in [(4, 4), (120, 4), (4, 56), (120, 56),
                     (62, 4), (62, 56), (4, 30), (120, 30)]:
        rivet(img, rx, ry)
    
    # Vertical seam between POS 4 and POS 5 (faint, just a darker line)
    vline(img, 63, 4, 60, DARK)
    vline(img, 64, 4, 60, METAL_LT)
    
    # Horizontal armature beam (across the whole top, ~6px tall)
    fill(img, 4, 28, 123, 33, METAL_LT)
    fill(img, 4, 28, 123, 28, METAL_XL)
    fill(img, 4, 33, 123, 33, DARK)
    
    # Vertical armature beam (across the whole top, ~6px wide)
    fill(img, 60, 4, 67, 60, METAL_LT)
    fill(img, 60, 4, 60, 60, METAL_XL)
    fill(img, 67, 4, 67, 60, DARK)
    
    # CENTER OBSERVATION PORTHOLE (the focal point)
    # Centered at (64, 30) which is the seam between POS 4 and POS 5
    # AND on the horizontal beam
    cx, cy = 64, 30
    # Outer ring (raised metal)
    fill(img, cx - 12, cy - 12, cx + 11, cy + 11, METAL_XL)
    fill(img, cx - 11, cy - 11, cx + 10, cy + 10, METAL_LT)
    # Inner glass (violet pulsing core)
    fill(img, cx - 9, cy - 9, cx + 8, cy + 8, VOSS_DK)
    fill(img, cx - 7, cy - 7, cx + 6, cy + 6, VOSS_MD)
    fill(img, cx - 5, cy - 5, cx + 4, cy + 4, VOSS_LT)
    fill(img, cx - 3, cy - 3, cx + 2, cy + 2, VOSS_GLOW)
    px(img, cx, cy, (255, 255, 255, 255))
    
    # 4 small mounting bolts around the porthole
    for (bx, by) in [(cx - 14, cy - 14), (cx + 12, cy - 14),
                     (cx - 14, cy + 12), (cx + 12, cy + 12)]:
        rivet(img, bx, by)
    
    # ============ BOTTOM HALF - Console (POS 0) + Panel (POS 1) ============
    
    # Big base
    fill(img, 0, 64, 127, 127, METAL)
    fill(img, 0, 64, 127, 64, METAL_XL)
    fill(img, 0, 127, 127, 127, DARK)
    fill(img, 0, 64, 0, 127, DARK)
    fill(img, 127, 64, 127, 127, DARK)
    
    # Vertical seam between POS 0 and POS 1
    vline(img, 63, 64, 127, DARK)
    vline(img, 64, 64, 127, METAL_LT)
    
    # === LEFT SIDE: MASTER CONSOLE (POS 0) ===
    
    # VOSS-7 stencil plate (top of master)
    fill(img, 6, 68, 58, 78, RIVET_D)
    fill(img, 6, 68, 58, 68, RIVET_L)
    
    # "VOSS-7" text in violet pixels
    # V
    for (lx, ly) in [(10, 71), (10, 72), (11, 73), (12, 74), (13, 73), (14, 72), (14, 71)]:
        px(img, lx, ly, VOSS_LT)
    # O
    fill(img, 17, 71, 18, 74, VOSS_LT)
    fill(img, 21, 71, 22, 74, VOSS_LT)
    px(img, 19, 71, VOSS_LT); px(img, 20, 71, VOSS_LT)
    px(img, 19, 74, VOSS_LT); px(img, 20, 74, VOSS_LT)
    # S
    for (lx, ly) in [(25, 71), (26, 71), (27, 71), (25, 72), (25, 73), (26, 73),
                     (27, 73), (27, 74), (25, 74), (26, 74)]:
        px(img, lx, ly, VOSS_LT)
    # S
    for (lx, ly) in [(30, 71), (31, 71), (32, 71), (30, 72), (30, 73), (31, 73),
                     (32, 73), (32, 74), (30, 74), (31, 74)]:
        px(img, lx, ly, VOSS_LT)
    # -
    fill(img, 35, 72, 37, 73, WARN_Y)
    # 7
    fill(img, 40, 71, 44, 72, WARN_Y)
    px(img, 43, 73, WARN_Y); px(img, 42, 74, WARN_Y); px(img, 41, 75, WARN_Y)
    
    # Big CRT screen (master, center)
    fill(img, 8, 84, 56, 110, RIVET_D)
    fill(img, 8, 84, 56, 84, METAL_LT)
    fill(img, 8, 110, 56, 110, DARK)
    # Screen background (very dark)
    fill(img, 10, 86, 54, 108, (16, 8, 28, 255))
    
    # Animated radial pulse on the screen (static frame in this PoC)
    scx, scy = 32, 97
    # Concentric rings of glow
    fill(img, scx - 11, scy - 11, scx + 11, scy + 11, VOSS_DK)
    fill(img, scx - 8, scy - 8, scx + 8, scy + 8, VOSS_MD)
    fill(img, scx - 5, scy - 5, scx + 5, scy + 5, VOSS_LT)
    fill(img, scx - 2, scy - 2, scx + 2, scy + 2, VOSS_GLOW)
    px(img, scx, scy, (255, 255, 255, 255))
    
    # Scanlines on the screen
    for y in range(88, 108, 4):
        for x in range(11, 54):
            cur = img.getpixel((x, y))
            r, g, b, a = cur
            img.putpixel((x, y), (max(0, r - 10), max(0, g - 10), max(0, b - 10), a))
    
    # 2 large mechanical buttons below the screen
    # Button left (red)
    fill(img, 12, 114, 22, 122, RIVET_D)
    fill(img, 13, 115, 21, 121, RED_DK)
    fill(img, 14, 116, 20, 120, RED)
    fill(img, 15, 117, 19, 119, RED_LT)
    # Button right (red)
    fill(img, 42, 114, 52, 122, RIVET_D)
    fill(img, 43, 115, 51, 121, RED_DK)
    fill(img, 44, 116, 50, 120, RED)
    fill(img, 45, 117, 49, 119, RED_LT)
    
    # Warning band between buttons
    for x in range(24, 41):
        for y in range(115, 122):
            if ((x + y) // 3) % 2 == 0:
                px(img, x, y, WARN_Y)
            else:
                px(img, x, y, WARN_K)
    
    # 4 corner rivets on the master section
    riveted_corners(img, 0, 64, 63, 127, inset=3)
    
    # === RIGHT SIDE: CONTROL PANEL (POS 1) ===
    
    # Plate "01" stencil top-right
    fill(img, 100, 68, 122, 78, RIVET_D)
    fill(img, 100, 68, 122, 68, RIVET_L)
    # "0"
    fill(img, 105, 71, 109, 74, WARN_Y)
    fill(img, 106, 72, 108, 73, RIVET_D)
    # "1"
    px(img, 113, 71, WARN_Y); px(img, 114, 71, WARN_Y); px(img, 115, 71, WARN_Y)
    fill(img, 114, 71, 114, 74, WARN_Y)
    px(img, 113, 74, WARN_Y); px(img, 115, 74, WARN_Y)
    
    # Vertical cable strip (left side of POS 1, near the seam)
    fill(img, 70, 80, 73, 124, DARK)
    fill(img, 71, 80, 72, 124, RIVET_L)
    fill(img, 70, 80, 73, 80, RIVET_L)
    fill(img, 70, 124, 73, 124, RIVET_L)
    # Cable clips
    for cy in [88, 100, 112]:
        fill(img, 69, cy, 74, cy + 2, METAL_XL)
        rivet(img, 69, cy)
        rivet(img, 73, cy)
    
    # 3 large status LEDs (vertically stacked, right side of POS 1)
    # LED 1 (green)
    fill(img, 110, 88, 118, 96, RIVET_D)
    fill(img, 111, 89, 117, 95, GREEN)
    fill(img, 113, 90, 116, 94, GREEN_LT)
    # LED 2 (yellow)
    fill(img, 110, 100, 118, 108, RIVET_D)
    fill(img, 111, 101, 117, 107, WARN_Y)
    # LED 3 (red)
    fill(img, 110, 112, 118, 120, RIVET_D)
    fill(img, 111, 113, 117, 119, RED)
    fill(img, 113, 114, 116, 118, RED_LT)
    
    # Lever / switch on the panel (between cable and LEDs)
    fill(img, 85, 90, 100, 96, RIVET_D)
    fill(img, 86, 91, 99, 95, METAL_LT)
    fill(img, 90, 92, 95, 94, METAL_XL)
    # Lever handle going down-right
    fill(img, 92, 96, 94, 110, METAL_LT)
    fill(img, 91, 108, 95, 112, RED)
    
    # Warning stripes at the bottom of POS 1
    for x in range(68, 124):
        for y in range(118, 122):
            if ((x + y) // 3) % 2 == 0:
                px(img, x, y, WARN_Y)
            else:
                px(img, x, y, WARN_K)
    
    # 4 corner rivets on the panel section
    riveted_corners(img, 64, 64, 127, 127, inset=3)
    
    return img


# ========= MODEL JSON GENERATORS =========

def make_split_model(uv, side_tex, top_tex, bottom_tex):
    """
    Generic block model that uses the front face texture in UV-split mode
    on its NORTH face, and falls back to other textures for the rest.
    """
    return {
        "ambientocclusion": False,
        "textures": {
            "particle": "nexusabsolu:blocks/condenseur_t1_face_front",
            "front":    "nexusabsolu:blocks/condenseur_t1_face_front",
            "side":     side_tex,
            "top":      top_tex,
            "bottom":   bottom_tex,
        },
        "elements": [
            {
                "from": [0, 0, 0],
                "to":   [16, 16, 16],
                "faces": {
                    "north": {"uv": uv, "texture": "#front", "cullface": "north"},
                    "south": {"uv": [0, 0, 16, 16], "texture": "#side", "cullface": "south"},
                    "east":  {"uv": [0, 0, 16, 16], "texture": "#side", "cullface": "east"},
                    "west":  {"uv": [0, 0, 16, 16], "texture": "#side", "cullface": "west"},
                    "up":    {"uv": [0, 0, 16, 16], "texture": "#top", "cullface": "up"},
                    "down":  {"uv": [0, 0, 16, 16], "texture": "#bottom", "cullface": "down"},
                }
            }
        ]
    }


def model_pos0_master():
    """POS 0 - master, bottom-left of front face. UV [0, 8, 8, 16]."""
    return make_split_model(
        uv=[0, 8, 8, 16],
        side_tex="nexusabsolu:blocks/condenseur_formed_side",
        top_tex="nexusabsolu:blocks/condenseur_formed_top",
        bottom_tex="nexusabsolu:blocks/condenseur_formed_bottom",
    )

def model_pos1_wall_br():
    """POS 1 - wall +X, bottom-right of front face. UV [8, 8, 16, 16]."""
    return make_split_model(
        uv=[8, 8, 16, 16],
        side_tex="nexusabsolu:blocks/condenseur_formed_side",
        top_tex="nexusabsolu:blocks/condenseur_formed_top",
        bottom_tex="nexusabsolu:blocks/condenseur_formed_bottom",
    )

def model_pos4_glass_tl():
    """POS 4 - glass above master, top-left of front face. UV [0, 0, 8, 8]."""
    return make_split_model(
        uv=[0, 0, 8, 8],
        side_tex="nexusabsolu:blocks/condenseur_formed_glass",
        top_tex="nexusabsolu:blocks/condenseur_formed_glass",
        bottom_tex="nexusabsolu:blocks/condenseur_formed_glass",
    )

def model_pos5_glass_tr():
    """POS 5 - glass above wall +X, top-right of front face. UV [8, 0, 16, 8]."""
    return make_split_model(
        uv=[8, 0, 16, 8],
        side_tex="nexusabsolu:blocks/condenseur_formed_glass",
        top_tex="nexusabsolu:blocks/condenseur_formed_glass",
        bottom_tex="nexusabsolu:blocks/condenseur_formed_glass",
    )


# ========= BLOCKSTATE =========

def make_blockstate():
    return {
        "variants": {
            "position=0": {"model": "nexusabsolu:condenseur_formed_pos0_master"},
            "position=1": {"model": "nexusabsolu:condenseur_formed_pos1_wall_br"},
            "position=2": {"model": "nexusabsolu:condenseur_formed_bottom"},
            "position=3": {"model": "nexusabsolu:condenseur_formed_energy"},
            "position=4": {"model": "nexusabsolu:condenseur_formed_pos4_glass_tl"},
            "position=5": {"model": "nexusabsolu:condenseur_formed_pos5_glass_tr"},
            "position=6": {"model": "nexusabsolu:condenseur_formed_top_glass"},
            "position=7": {"model": "nexusabsolu:condenseur_formed_top_wall"}
        }
    }


# ========= MAIN =========

def main():
    os.makedirs(TEX_OUT, exist_ok=True)
    os.makedirs(MODEL_OUT, exist_ok=True)
    os.makedirs(BLOCKSTATE_OUT, exist_ok=True)

    # 1. Generate the texture
    tex_path = os.path.join(TEX_OUT, "condenseur_t1_face_front.png")
    img = make_front_face()
    img.save(tex_path)
    print(f"  TEXTURE  {img.size[0]}x{img.size[1]}  {tex_path}")

    # 2. Generate the 4 split models
    models = {
        "condenseur_formed_pos0_master.json":   model_pos0_master(),
        "condenseur_formed_pos1_wall_br.json":  model_pos1_wall_br(),
        "condenseur_formed_pos4_glass_tl.json": model_pos4_glass_tl(),
        "condenseur_formed_pos5_glass_tr.json": model_pos5_glass_tr(),
    }
    for name, m in models.items():
        path = os.path.join(MODEL_OUT, name)
        with open(path, "w") as f:
            json.dump(m, f, indent=2)
        print(f"  MODEL              {path}")

    # 3. Update blockstate
    bs_path = os.path.join(BLOCKSTATE_OUT, "condenseur_formed.json")
    with open(bs_path, "w") as f:
        json.dump(make_blockstate(), f, indent=4)
    print(f"  BLOCKSTATE         {bs_path}")

    print(f"\nGenerated 1 texture, {len(models)} models, 1 blockstate.")


if __name__ == "__main__":
    main()
