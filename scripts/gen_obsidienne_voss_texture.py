#!/usr/bin/env python3
"""
Generate Obsidienne Voss block texture (16x16).

Obsidian-like base with subtle purple Voss veins and faint glow.
Output: mod-source/src/main/resources/assets/nexusabsolu/textures/blocks/obsidienne_voss.png
"""
import random
from PIL import Image
import os

random.seed(7704)  # Reproducible

W, H = 16, 16

# Color palette
BASE_DARK = (10, 8, 18)       # Almost black, slight purple
BASE_MID = (20, 15, 35)       # Dark purple-grey
BASE_LIGHT = (35, 25, 55)     # Lighter purple-grey
VEIN_PURPLE = (80, 30, 110)   # Voss purple
VEIN_BRIGHT = (140, 60, 180)  # Bright purple highlight
HIGHLIGHT = (180, 100, 220)   # Faint glow spots

img = Image.new("RGBA", (W, H), BASE_DARK)
pixels = img.load()

# Layer 1: speckled base - random dark variations
for x in range(W):
    for y in range(H):
        r = random.random()
        if r < 0.55:
            pixels[x, y] = BASE_DARK
        elif r < 0.85:
            pixels[x, y] = BASE_MID
        else:
            pixels[x, y] = BASE_LIGHT

# Layer 2: irregular purple veins (3-4 of them, random walk)
def vein(start_x, start_y, length, color):
    x, y = start_x, start_y
    for _ in range(length):
        if 0 <= x < W and 0 <= y < H:
            pixels[x, y] = color
        # Random walk biased diagonal
        dx = random.choice([-1, 0, 0, 1])
        dy = random.choice([-1, 0, 1, 1])
        x += dx
        y += dy

vein(2, 1, 14, VEIN_PURPLE)
vein(13, 3, 12, VEIN_PURPLE)
vein(5, 14, 10, VEIN_PURPLE)
vein(9, 7, 8, VEIN_BRIGHT)

# Layer 3: faint glow highlights (3-5 isolated bright pixels)
for _ in range(4):
    x = random.randint(2, W - 3)
    y = random.randint(2, H - 3)
    pixels[x, y] = HIGHLIGHT

# Save
out = "mod-source/src/main/resources/assets/nexusabsolu/textures/blocks/obsidienne_voss.png"
os.makedirs(os.path.dirname(out), exist_ok=True)
img.save(out)
print(f"Wrote {out}")
