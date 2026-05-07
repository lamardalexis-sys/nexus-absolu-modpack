#!/usr/bin/env python3
"""
Generate Catalyseur de Sortie texture (16x16).

A potion-flask shape with a glowing purple Voss liquid inside.
"""
import math
from PIL import Image
import os

W, H = 16, 16

# Colors
TRANSPARENT = (0, 0, 0, 0)
GLASS_DARK = (200, 220, 230, 200)
GLASS_LIGHT = (240, 250, 255, 220)
LIQUID_DEEP = (60, 20, 100, 255)
LIQUID_MID = (130, 50, 180, 255)
LIQUID_BRIGHT = (200, 130, 240, 255)
HIGHLIGHT = (255, 220, 255, 255)
CORK = (90, 50, 30, 255)
CORK_DARK = (60, 30, 15, 255)

img = Image.new("RGBA", (W, H), TRANSPARENT)
pixels = img.load()

def setp(x, y, c):
    if 0 <= x < W and 0 <= y < H:
        pixels[x, y] = c

# Bottom: round bulb (rows 6-13)
for y in range(6, 14):
    for x in range(2, 14):
        cx, cy = 7.5, 9.5
        d = math.sqrt((x - cx) ** 2 + (y - cy) ** 2)
        if d <= 5.2:
            # Liquid inside
            if d <= 4.0:
                if y >= 8:  # Liquid fills bottom of bulb
                    if d < 2.0:
                        setp(x, y, LIQUID_BRIGHT)
                    elif d < 3.0:
                        setp(x, y, LIQUID_MID)
                    else:
                        setp(x, y, LIQUID_DEEP)
                else:
                    setp(x, y, GLASS_LIGHT)
            else:
                setp(x, y, GLASS_DARK)

# Neck: rows 3-5
for y in range(3, 6):
    setp(6, y, GLASS_DARK)
    setp(7, y, GLASS_LIGHT)
    setp(8, y, GLASS_LIGHT)
    setp(9, y, GLASS_DARK)

# Cork: rows 1-2
setp(6, 1, CORK_DARK)
setp(7, 1, CORK)
setp(8, 1, CORK)
setp(9, 1, CORK_DARK)
setp(6, 2, CORK_DARK)
setp(7, 2, CORK)
setp(8, 2, CORK)
setp(9, 2, CORK_DARK)

# Add a small highlight to glass
setp(4, 8, HIGHLIGHT)
setp(5, 7, HIGHLIGHT)

# Add a glowing pixel in center of liquid
setp(7, 11, HIGHLIGHT)
setp(8, 10, HIGHLIGHT)

out = "mod-source/src/main/resources/assets/nexusabsolu/textures/items/catalyseur_sortie.png"
os.makedirs(os.path.dirname(out), exist_ok=True)
img.save(out)
print(f"Wrote {out}")
