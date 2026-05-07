#!/usr/bin/env python3
"""
Generate Portail Sortie Actif texture (16x16).

Purple swirly portal texture, evokes nether portal but Voss-themed.
"""
import math
import random
from PIL import Image
import os

random.seed(2145)

W, H = 16, 16

# Color gradient: dark purple -> bright magenta -> white core
def color_at_radius(r):
    """r in [0, 1]"""
    if r > 1.0:
        r = 1.0
    if r < 0.15:
        return (200, 130, 240, 255)   # bright magenta core
    elif r < 0.35:
        return (140, 60, 200, 255)
    elif r < 0.55:
        return (90, 30, 150, 255)
    elif r < 0.75:
        return (50, 15, 100, 255)
    else:
        return (25, 5, 60, 255)

img = Image.new("RGBA", (W, H), (15, 5, 35, 255))
pixels = img.load()

cx, cy = (W - 1) / 2.0, (H - 1) / 2.0
max_r = math.sqrt(cx ** 2 + cy ** 2)

# Swirl: distance + angle distortion
for x in range(W):
    for y in range(H):
        dx = x - cx
        dy = y - cy
        r = math.sqrt(dx * dx + dy * dy) / max_r
        ang = math.atan2(dy, dx)
        # Swirl distortion: add angular noise based on radius
        swirl = r + 0.2 * math.sin(ang * 4 + r * 6)
        # Add a little randomness so it doesn't look too geometric
        swirl += (random.random() - 0.5) * 0.08
        pixels[x, y] = color_at_radius(max(0.0, min(1.0, swirl)))

out = "mod-source/src/main/resources/assets/nexusabsolu/textures/blocks/portail_sortie_actif.png"
os.makedirs(os.path.dirname(out), exist_ok=True)
img.save(out)
print(f"Wrote {out}")
