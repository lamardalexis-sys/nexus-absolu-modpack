#!/usr/bin/env python3
"""
Generate the Condenseur T2 GUI background texture.

New layout (220x220 inside a 256x256 PNG):
- Fluid bar (DIA) moved to the LEFT
- More breathing room between all elements
- Player inventory centered horizontally
- Hotbar at the bottom, well separated

Layout (coordinates in px relative to the GUI top-left):

  (0,0)                                              (220,0)
    +----------------------------------------------+
    | [title bar]                                  |  y=5
    |                                              |
    | [DIA  ] [slots 2x2] [PROG] [Output]          |  y=26-80
    | [bar  ] [  CM CM ]  [ ▼ ] [  []  ]          |
    | [     ] [  Cl Ca ]  [   ]                    |
    |                                              |
    | [==== Energy bar horizontal =========]       |  y=92
    |                                              |
    | 0.0M / 1.0M RF          STATUS               |  y=104
    |                                              |
    | +----------------------------------------+   |
    | | Player inventory 3x9                   |   |  y=130
    | |                                        |   |
    | |                                        |   |
    | +----------------------------------------+   |
    |                                              |
    | +----------------------------------------+   |
    | | Hotbar 1x9                             |   |  y=192
    | +----------------------------------------+   |
    +----------------------------------------------+
                      (220,220)

Inventory is centered: 9 * 18 = 162 wide, (220 - 162) / 2 = 29 px left margin.
"""

import os
from PIL import Image, ImageDraw

OUT_PATH = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui/gui_condenseur_t2.png"

# GUI dimensions
GW, GH = 220, 220

# Colors (cohérent avec le style T2 violet sombre)
BG_DARK  = (20, 10, 32, 255)       # Almost black with violet tint
BG_MID   = (36, 18, 56, 255)       # Slightly lighter violet
BG_LT    = (56, 28, 84, 255)       # Mid violet
FRAME    = (120, 60, 180, 255)     # Bright violet frame
FRAME_LT = (170, 100, 220, 255)    # Highlighted violet
FRAME_DK = (80, 30, 130, 255)      # Dark violet
SLOT_BG  = (12, 6, 20, 255)        # Slot background
SLOT_BORDER = (88, 44, 140, 255)   # Slot border
CORNER   = (212, 176, 64, 255)     # Yellow corner accents
GRID     = (100, 50, 160, 200)     # Grid overlay (semi-transparent)


def fill(img, x1, y1, x2, y2, color):
    ImageDraw.Draw(img).rectangle([x1, y1, x2, y2], fill=color)

def rect_border(img, x1, y1, x2, y2, color, width=1):
    d = ImageDraw.Draw(img)
    for i in range(width):
        d.rectangle([x1+i, y1+i, x2-i, y2-i], outline=color)

def px(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)

def draw_slot(img, x, y, w=18, h=18):
    """Standard Minecraft-style 18x18 slot with dark recessed look."""
    # Outer dark border (top/left darker, bottom/right lighter for depth)
    fill(img, x, y, x + w - 1, y, BG_DARK)          # top
    fill(img, x, y, x, y + h - 1, BG_DARK)          # left
    fill(img, x + w - 1, y, x + w - 1, y + h - 1, FRAME_LT)  # right
    fill(img, x, y + h - 1, x + w - 1, y + h - 1, FRAME_LT)  # bottom
    # Inside (1px padding)
    fill(img, x + 1, y + 1, x + w - 2, y + h - 2, SLOT_BG)

def draw_slot_grid(img, x, y, cols, rows):
    """Draw a grid of slots at (x, y), cols wide, rows tall, 18px each."""
    for row in range(rows):
        for col in range(cols):
            draw_slot(img, x + col * 18, y + row * 18)

def draw_panel(img, x1, y1, x2, y2):
    """Inset panel with frame and darker background."""
    # Outer frame
    rect_border(img, x1, y1, x2, y2, FRAME)
    # Inner highlight
    rect_border(img, x1 + 1, y1 + 1, x2 - 1, y2 - 1, FRAME_DK)
    # Dark inside
    fill(img, x1 + 2, y1 + 2, x2 - 2, y2 - 2, BG_DARK)

def draw_vertical_bar_recess(img, x, y, w, h):
    """Vertical bar recessed area (for fluid or progress)."""
    # Outer black frame
    rect_border(img, x - 1, y - 1, x + w, y + h, (0, 0, 0, 255))
    # Inner
    fill(img, x, y, x + w - 1, y + h - 1, BG_DARK)

def draw_horizontal_bar_recess(img, x, y, w, h):
    """Horizontal bar recessed area (for energy)."""
    rect_border(img, x - 1, y - 1, x + w, y + h, (0, 0, 0, 255))
    fill(img, x, y, x + w - 1, y + h - 1, BG_DARK)


def main():
    # Base image: 256x256 transparent PNG
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))

    # === MAIN GUI BACKGROUND (220x220 top-left) ===
    # Dark violet base
    fill(img, 0, 0, GW - 1, GH - 1, BG_MID)

    # Outer frame (3 layers for depth)
    rect_border(img, 0, 0, GW - 1, GH - 1, BG_DARK)
    rect_border(img, 1, 1, GW - 2, GH - 2, FRAME)
    rect_border(img, 2, 2, GW - 3, GH - 3, FRAME_LT)
    rect_border(img, 3, 3, GW - 4, GH - 4, FRAME_DK)
    # Inner fill a bit darker
    fill(img, 4, 4, GW - 5, GH - 5, BG_MID)

    # Yellow corner accents (VOSS-style)
    for (cx, cy) in [(4, 4), (GW - 6, 4), (4, GH - 6), (GW - 6, GH - 6)]:
        fill(img, cx, cy, cx + 1, cy + 1, CORNER)

    # === TITLE BAR (separator under title) ===
    # Title is drawn by the GUI Java code with the font, we just draw a
    # subtle separator line at y=18 to delimit the title area.
    fill(img, 6, 18, GW - 7, 18, FRAME_DK)
    fill(img, 6, 19, GW - 7, 19, BG_DARK)

    # === FLUID BAR (DIA) - LEFT SIDE ===
    # Positioned at x=10..22, y=26..88 (vertical, 12 wide, 62 tall)
    draw_vertical_bar_recess(img, 10, 26, 12, 62)

    # === INPUT SLOTS 2x2 grid (shifted right to leave room for fluid bar) ===
    # 2 columns x 2 rows = 4 slots of 18x18 with 2px gap between them
    # Slot 0: CM1, Slot 1: CM2, Slot 2: Key, Slot 3: Catalyst
    # Top row (CMs):
    draw_slot(img, 36, 30)   # CM 1
    draw_slot(img, 56, 30)   # CM 2
    # Bottom row (Key, Catalyst):
    draw_slot(img, 36, 52)   # Key
    draw_slot(img, 56, 52)   # Catalyst

    # === PROGRESS BAR (vertical, center) ===
    # Positioned at x=94..112, y=28..84
    draw_vertical_bar_recess(img, 94, 28, 18, 56)

    # === ARROW from progress bar to output (visual hint) ===
    # A small decorative arrow pointing right around y=50
    for i in range(6):
        fill(img, 118 + i, 46 + i, 118 + i, 46 + i, FRAME_LT)
        fill(img, 118 + i, 55 - i, 118 + i, 55 - i, FRAME_LT)

    # === OUTPUT SLOT (right side, larger visual emphasis) ===
    # Output slot at x=150, y=42 (single 18x18 slot)
    draw_slot(img, 150, 42)
    # Decorative border around output to emphasize it
    rect_border(img, 145, 37, 173, 65, FRAME_DK)

    # === ENERGY BAR (horizontal, lower-mid area) ===
    # x=10..210, y=96..104 (horizontal, 200 wide, 8 tall)
    draw_horizontal_bar_recess(img, 10, 96, 200, 8)

    # === STATUS LINE SEPARATOR ===
    fill(img, 6, 118, GW - 7, 118, FRAME_DK)
    fill(img, 6, 119, GW - 7, 119, BG_DARK)

    # === PLAYER INVENTORY AREA (centered, 9 cols x 3 rows) ===
    # Inventory: 9*18=162 wide, start x = (220-162)/2 = 29
    # Row 1 y=130, Row 2 y=148, Row 3 y=166
    inv_x = 29
    inv_y = 130
    draw_slot_grid(img, inv_x, inv_y, 9, 3)

    # === HOTBAR (1 row, centered) ===
    # Hotbar at y=192 (22 px gap under inventory)
    hotbar_y = 192
    draw_slot_grid(img, inv_x, hotbar_y, 9, 1)

    # === DECORATIVE: small corner rivets inside the main panel ===
    for (rx, ry) in [(8, 8), (GW - 10, 8), (8, GH - 10), (GW - 10, GH - 10)]:
        fill(img, rx, ry, rx + 1, ry + 1, FRAME_LT)

    img.save(OUT_PATH)
    print(f"Wrote {img.size[0]}x{img.size[1]}  {OUT_PATH}")


if __name__ == "__main__":
    main()
