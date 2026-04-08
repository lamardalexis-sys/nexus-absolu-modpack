#!/usr/bin/env python3
"""
Generate the 2 Cle de Liberte textures (16x16):
- cle_liberte.png (inactive, bronze/gray)
- cle_liberte_activee.png (activated, violet glow)
"""
import os
from PIL import Image

OUT_DIR = "mod-source/src/main/resources/assets/nexusabsolu/textures/items"
os.makedirs(OUT_DIR, exist_ok=True)

# Palettes
# Inactive: old bronze/tarnished
BRONZE_DK = (74, 46, 20, 255)
BRONZE_MD = (130, 86, 38, 255)
BRONZE_LT = (184, 132, 60, 255)
BRONZE_HL = (220, 178, 90, 255)
SHADOW    = (30, 18, 10, 255)

# Activated: violet glow
VOSS_DK   = (74, 38, 110, 255)
VOSS_MD   = (122, 64, 178, 255)
VOSS_LT   = (170, 110, 220, 255)
VOSS_HL   = (210, 160, 240, 255)
VOSS_GLOW = (240, 200, 255, 255)
VOSS_SHDW = (32, 10, 54, 255)


def make_key(activated=False):
    """Generate a 16x16 old-style key texture.

    Layout (vue de face, clé horizontale, anneau à gauche, panneton à droite):
      col:  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
    row 0:  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
    row 1:  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
    row 2:  .  #  #  #  .  .  .  .  .  .  .  .  .  .  .  .
    row 3:  #  #  .  .  #  .  .  .  .  .  .  .  .  .  .  .
    row 4:  #  .  .  .  #  .  .  .  .  .  .  .  .  .  .  .
    row 5:  #  .  .  .  #  #  #  #  #  #  #  #  #  .  .  .
    row 6:  #  .  .  .  #  .  .  .  .  .  .  .  .  .  .  .
    row 7:  #  #  .  .  #  .  .  .  .  .  #  .  #  .  .  .
    row 8:  .  #  #  #  .  .  .  .  .  .  #  #  #  .  .  .
    row 9:  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .

    With anti-aliased edges and highlights.
    """
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    
    if activated:
        dk, md, lt, hl, shdw = VOSS_DK, VOSS_MD, VOSS_LT, VOSS_HL, VOSS_SHDW
        glow = VOSS_GLOW
    else:
        dk, md, lt, hl, shdw = BRONZE_DK, BRONZE_MD, BRONZE_LT, BRONZE_HL, SHADOW
        glow = None
    
    def px(x, y, color):
        if 0 <= x < 16 and 0 <= y < 16:
            img.putpixel((x, y), color)
    
    # === Ring (anneau) on the left, positions (1..4, 2..8) with hole ===
    # Outer ring outline
    ring_outline = [
        (2, 2), (3, 2),
        (1, 3), (4, 3),
        (1, 4), (4, 4),
        (1, 5), (4, 5),
        (1, 6), (4, 6),
        (1, 7), (4, 7),
        (2, 8), (3, 8),
    ]
    for (x, y) in ring_outline:
        px(x, y, dk)
    
    # Ring outer body (highlighted pixels around the rim)
    ring_body = [
        (2, 3), (3, 3),   # top-left of ring
        (2, 4),
        (3, 7),
        (2, 7),           # bottom-right of ring
    ]
    for (x, y) in ring_body:
        px(x, y, md)
    
    # Ring highlights
    px(2, 2, lt)  # top dark edge brighter
    px(3, 2, lt)
    px(1, 5, lt)  # left edge highlight (torch-side)
    
    # Ring inner hole is already transparent (not set)
    # Let me explicitly clear inside pixels
    for (x, y) in [(2, 4), (3, 4), (2, 5), (3, 5), (2, 6), (3, 6)]:
        if (x, y) in ring_body:
            continue
        img.putpixel((x, y), (0, 0, 0, 0))
    
    # Actually redo inside: should be hollow (transparent) in the center
    for x, y in [(2, 5), (3, 5), (2, 6), (3, 6)]:
        img.putpixel((x, y), (0, 0, 0, 0))
    
    # === Shaft (tige horizontale) from x=5 to x=12, y=5 ===
    for x in range(5, 13):
        px(x, 4, dk)   # top edge
        px(x, 5, lt)   # body highlight
        px(x, 6, md)   # body shadow
    
    # === Panneton (bit) at the right end, positions (10..12, 6..8) ===
    # Forms an "L" shape sticking down
    px(10, 7, dk)
    px(10, 8, dk)
    px(11, 8, dk)
    px(12, 8, dk)
    px(12, 7, dk)
    
    # Panneton interior (lighter body)
    px(11, 7, md)
    
    # Panneton highlight
    px(10, 6, hl if activated else BRONZE_HL)  # tip highlight
    px(12, 6, dk)  # shadow under shaft
    
    # === Shadows under the whole key ===
    for x in range(5, 13):
        px(x, 7, shdw if x < 10 or x > 12 else md)
    # Shadow below the ring
    px(2, 9, shdw)
    px(3, 9, shdw)
    
    # === Tip detail / key guard (small nub between shaft and panneton) ===
    px(8, 3, md)   # upper guard
    px(8, 7, dk)   # lower guard
    
    # === Highlight on ring top ===
    px(2, 2, hl)
    
    # === Highlight on shaft (top-left of shaft) ===
    px(5, 4, hl)
    px(6, 4, hl)
    
    # === If activated: add violet glow around the key ===
    if activated and glow is not None:
        glow_positions = [
            # Sparkle around ring
            (0, 2), (0, 8), (5, 1),
            # Sparkle around panneton
            (13, 8), (9, 9), (12, 4),
            # Scattered sparkles
            (1, 1), (14, 6),
        ]
        for (x, y) in glow_positions:
            # Only draw if position is currently transparent
            cur = img.getpixel((x, y))
            if cur[3] == 0:
                px(x, y, (*glow[:3], 180))  # semi-transparent glow
        
        # Aura: very faint halo around the key (1 pixel border at low alpha)
        aura_positions = set()
        for y in range(16):
            for x in range(16):
                cur = img.getpixel((x, y))
                if cur[3] > 200:
                    # Add aura to neighbors
                    for dy in [-1, 0, 1]:
                        for dx in [-1, 0, 1]:
                            if dx == 0 and dy == 0:
                                continue
                            nx, ny = x + dx, y + dy
                            if 0 <= nx < 16 and 0 <= ny < 16:
                                neighbor = img.getpixel((nx, ny))
                                if neighbor[3] == 0:
                                    aura_positions.add((nx, ny))
        for (x, y) in aura_positions:
            img.putpixel((x, y), (*VOSS_MD[:3], 90))
    
    return img


def main():
    key_inactive = make_key(activated=False)
    key_active = make_key(activated=True)
    
    inactive_path = os.path.join(OUT_DIR, "cle_liberte.png")
    active_path = os.path.join(OUT_DIR, "cle_liberte_activee.png")
    
    key_inactive.save(inactive_path)
    key_active.save(active_path)
    
    print(f"Wrote {inactive_path}")
    print(f"Wrote {active_path}")


if __name__ == "__main__":
    main()
