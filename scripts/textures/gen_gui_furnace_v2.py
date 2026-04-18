"""
Textures GUI Furnace Nexus v2 - style Mekanism decompose.

3 textures distinctes :
 1. gui_furnace.png (256x256) - fond vanilla clair + zone inventaire, ultra minimaliste
 2. guislot_nexus.png (256x256) - sprite sheet 18x18 par slot type (NORMAL, INPUT, OUTPUT_LARGE 26x26, POWER, FUEL, UPGRADE)
 3. guiprogress_nexus.png (256x256) - sprite sheet flèches progress (empty 28x11 + filled 28x11 a droite)
 4. guipowerbar_nexus.png (256x256) - sprite sheet barre RF (empty 14x74 + filled 14x74 a droite)

Palette : gris vanilla MC + legers accents violet Nexus
"""
from PIL import Image, ImageDraw
import os

OUT = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui"
OUT_EL = f"{OUT}/elements"
os.makedirs(OUT_EL, exist_ok=True)

# === PALETTE STYLE MEKANISM ===
VANILLA_BG = (198, 198, 198, 255)   # fond beige/gris vanilla (inventaire MC)
VANILLA_LIGHT = (255, 255, 255, 255)
VANILLA_MID = (139, 139, 139, 255)   # bordure interne
VANILLA_DARK = (55, 55, 55, 255)     # ombre
SLOT_INNER = (139, 139, 139, 255)    # fond slot (grey plus clair)
SLOT_LIGHT = (255, 255, 255, 255)
SLOT_DARK = (55, 55, 55, 255)

# Accents Nexus (legers, utilises pour icones sur slots specialises)
NEXUS_PURPLE = (120, 60, 170, 255)
NEXUS_PURPLE_LIGHT = (180, 120, 220, 255)
FLAME_ORANGE = (220, 100, 20, 255)
FLAME_YELLOW = (255, 200, 60, 255)
POWER_RED = (200, 40, 40, 255)
POWER_YELLOW = (255, 220, 0, 255)


# ==========================================================================
# 1. GUI FURNACE BACKGROUND (style vanilla smelter)
# ==========================================================================
def gen_main_bg():
    W, H = 176, 166
    img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Fond principal beige
    d.rectangle([0, 0, W-1, H-1], fill=VANILLA_BG)

    # Bordure double Mekanism-style (clair en haut-gauche, sombre en bas-droite)
    # outer border
    d.rectangle([0, 0, W-1, 0], fill=VANILLA_LIGHT)                # top
    d.rectangle([0, 0, 0, H-1], fill=VANILLA_LIGHT)                # left
    d.rectangle([0, H-1, W-1, H-1], fill=VANILLA_DARK)             # bottom
    d.rectangle([W-1, 0, W-1, H-1], fill=VANILLA_DARK)             # right
    # inner border (3px thick effet 3D bevel)
    for i in range(1, 4):
        d.rectangle([i, i, W-1-i, i], fill=VANILLA_LIGHT if i == 1 else VANILLA_BG)
        d.rectangle([i, i, i, H-1-i], fill=VANILLA_LIGHT if i == 1 else VANILLA_BG)
        d.rectangle([i, H-1-i, W-1-i, H-1-i], fill=VANILLA_DARK if i == 1 else VANILLA_BG)
        d.rectangle([W-1-i, i, W-1-i, H-1-i], fill=VANILLA_DARK if i == 1 else VANILLA_BG)

    # Zone inventaire (y=83 a y=142 pour inv, y=148 a y=164 pour hotbar)
    # Les slots inventaire utilisent le style classique MC : juste un cadre sombre avec slots clairs
    # On dessine juste la bordure de la zone inv, les slots individuels viendront du code Java
    # NOTE: en fait Mekanism dessine DIRECTEMENT les slots dans la texture de fond pour l'inventaire
    # On fait pareil : dessine les 27+9 slots inventaire dans la texture

    def draw_inv_slot(x, y):
        # Style vanilla : dark outline + inner lighter
        d.rectangle([x, y, x+17, y+17], fill=VANILLA_DARK)
        d.rectangle([x+1, y+1, x+16, y+16], fill=SLOT_INNER)
        # Bevel subtil (haut-gauche clair, bas-droite sombre mais leger)
        d.rectangle([x+1, y+1, x+16, y+1], fill=VANILLA_DARK)
        d.rectangle([x+1, y+1, x+1, y+16], fill=VANILLA_DARK)

    # Inventaire joueur (3 lignes x 9 cols)
    for row in range(3):
        for col in range(9):
            draw_inv_slot(7 + col * 18, 83 + row * 18)
    # Hotbar
    for col in range(9):
        draw_inv_slot(7 + col * 18, 141)

    # Label separator subtle entre machine et inventaire
    # (Mekanism ne le fait pas, on garde clean)

    img.save(f"{OUT}/gui_furnace.png")
    print(f"[OK] gui_furnace.png ({W}x{H} usable, canvas 256x256)")


# ==========================================================================
# 2. SPRITE SHEET SLOTS (18x18 + 26x26 pour output large)
# ==========================================================================
def gen_slot_sheet():
    """
    Layout (x: 0-143, y: 0-53):
      NORMAL     (x=0,   y=0, 18x18)  - slot gris standard
      POWER      (x=18,  y=0, 18x18)  - slot gris + accent power icon
      INPUT      (x=36,  y=0, 18x18)  - slot gris + icone input
      FUEL       (x=54,  y=0, 18x18)  - slot gris + icone flamme
      UPGRADE    (x=72,  y=0, 18x18)  - slot avec bord violet (Nexus)
      OUTPUT_LG  (x=90,  y=0, 26x26)  - output slot double-bordure

      Overlays (row 2, y=18):
      ICON_POWER (x=36, y=18, 18x18)
      ICON_FUEL  (x=54, y=18, 18x18)
    """
    img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    def draw_slot_base(x, y, w=18, h=18, purple_frame=False):
        """Slot vanilla style : outer dark frame, inner clearer."""
        # Outer frame
        d.rectangle([x, y, x+w-1, y+h-1], fill=VANILLA_DARK)
        # Bevel sombre top-left (inverse vanilla - un slot est encaisse)
        d.rectangle([x+1, y+1, x+w-2, y+h-2], fill=SLOT_INNER)
        # Ombre subtile dans slot
        d.rectangle([x+1, y+1, x+w-2, y+1], fill=(100, 100, 100, 255))
        d.rectangle([x+1, y+1, x+1, y+h-2], fill=(100, 100, 100, 255))
        if purple_frame:
            # Accent violet en bordure externe (1px)
            d.rectangle([x, y, x+w-1, y], fill=NEXUS_PURPLE)
            d.rectangle([x, y, x, y+h-1], fill=NEXUS_PURPLE)
            d.rectangle([x, y+h-1, x+w-1, y+h-1], fill=NEXUS_PURPLE)
            d.rectangle([x+w-1, y, x+w-1, y+h-1], fill=NEXUS_PURPLE)

    # NORMAL (0,0)
    draw_slot_base(0, 0)
    # POWER (18,0)
    draw_slot_base(18, 0)
    # INPUT (36,0)
    draw_slot_base(36, 0)
    # FUEL (54,0)
    draw_slot_base(54, 0)
    # UPGRADE (72,0) avec cadre violet
    draw_slot_base(72, 0, purple_frame=True)
    # OUTPUT_LARGE (90,0) 26x26 avec double bordure
    draw_slot_base(90, 0, w=26, h=26)
    # Cadre externe supplementaire OUTPUT_LARGE
    d.rectangle([90, 0, 90+25, 0], fill=VANILLA_LIGHT)
    d.rectangle([90, 0, 90, 25], fill=VANILLA_LIGHT)
    d.rectangle([90, 25, 115, 25], fill=VANILLA_DARK)
    d.rectangle([115, 0, 115, 25], fill=VANILLA_DARK)

    # === Overlays (y=18) ===
    # Icone POWER (eclair) sur overlay POWER (36, 18)
    d.rectangle([36, 18, 53, 35], fill=(0, 0, 0, 0))  # transparent base
    # Eclair simple 7x11 au centre
    lightning = [(44, 21), (45, 21), (43, 23), (44, 23), (45, 23), (46, 23),
                 (44, 25), (45, 25), (46, 25), (43, 27), (44, 27), (45, 27)]
    for px, py in lightning:
        d.point((px, py), fill=POWER_YELLOW)

    # Icone FUEL (flamme) sur overlay FUEL (54, 18)
    d.rectangle([54, 18, 71, 35], fill=(0, 0, 0, 0))
    # Flamme stylisee 7x9
    flame_body = [(62, 30), (63, 30), (61, 28), (62, 28), (63, 28), (64, 28),
                  (62, 26), (63, 26), (62, 24), (63, 24), (62, 22)]
    flame_bright = [(62, 28), (63, 28)]
    for px, py in flame_body:
        d.point((px, py), fill=FLAME_ORANGE)
    for px, py in flame_bright:
        d.point((px, py), fill=FLAME_YELLOW)

    img.save(f"{OUT_EL}/guislot_nexus.png")
    print(f"[OK] guislot_nexus.png (sprite sheet)")


# ==========================================================================
# 3. PROGRESS BAR SPRITE SHEET (fleche horizontal 28x11)
# ==========================================================================
def gen_progress_sheet():
    """
    Layout Mekanism-like:
     - empty bar: x=0, y=0, 28x11 (silhouette)
     - filled bar: x=28, y=0, 28x11 (pleine)
     - rangee par tier: y=0 (iron gris), y=11 (gold), y=22 (invar), y=33 (emeradic), y=44 (vossium)
    """
    img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    TIER_COLORS = [
        ("iron",       (180, 180, 180, 255), (240, 240, 240, 255)),  # gris clair
        ("gold",       (220, 180, 60, 255),  (255, 230, 120, 255)),  # dore
        ("invar",      (140, 180, 150, 255), (200, 230, 210, 255)),  # vert-gris clair
        ("emeradic",   (80, 200, 100, 255),  (150, 255, 180, 255)),  # vert brillant
        ("vossium_iv", (140, 80, 200, 255),  (200, 140, 240, 255)),  # violet
    ]

    for idx, (name, color, bright) in enumerate(TIER_COLORS):
        y_base = idx * 11

        # EMPTY version (silhouette dark gris)
        # Tige de fleche (thin) row 4-6
        for ix in range(4, 23):
            d.point((ix, y_base + 4), fill=VANILLA_DARK)
            d.point((ix, y_base + 5), fill=VANILLA_DARK)
            d.point((ix, y_base + 6), fill=VANILLA_DARK)
        # Pointe de fleche (triangle droite)
        arrow_points = [(22, y_base+2), (23, y_base+2), (23, y_base+3), (24, y_base+3), (24, y_base+4),
                        (25, y_base+4), (25, y_base+5), (26, y_base+5),
                        (25, y_base+6), (24, y_base+6), (24, y_base+7), (23, y_base+7), (23, y_base+8), (22, y_base+8)]
        for px, py in arrow_points:
            if px < 28:
                d.point((px, py), fill=VANILLA_DARK)

        # FILLED version (x=28+, meme forme mais colore)
        for ix in range(4, 23):
            d.point((ix + 28, y_base + 4), fill=color)
            d.point((ix + 28, y_base + 5), fill=bright)
            d.point((ix + 28, y_base + 6), fill=color)
        for px, py in arrow_points:
            if px < 28:
                d.point((px + 28, py), fill=color)

    img.save(f"{OUT_EL}/guiprogress_nexus.png")
    print(f"[OK] guiprogress_nexus.png (sprite sheet 5 tiers)")


# ==========================================================================
# 4. POWER BAR (RF) SPRITE SHEET (vertical 14x54)
# ==========================================================================
def gen_powerbar_sheet():
    """
    Layout:
     - empty: x=0, y=0, 14x54 (tube vide sombre)
     - filled: x=14, y=0, 14x54 (tube rempli degrade rouge->jaune)
     - flame column (for fuel indicator): x=28, y=0, 14x13 empty / x=42, y=0, 14x13 filled
    """
    img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    W_BAR, H_BAR = 14, 54

    # EMPTY bar (x=0, y=0)
    d.rectangle([0, 0, W_BAR-1, H_BAR-1], fill=VANILLA_DARK)
    d.rectangle([1, 1, W_BAR-2, H_BAR-2], fill=(30, 30, 30, 255))
    # Top accent (clair)
    d.rectangle([1, 1, W_BAR-2, 1], fill=(100, 100, 100, 255))

    # FILLED bar (x=14, y=0) - degrade rouge (bas) -> jaune (haut)
    for iy in range(H_BAR):
        t = iy / H_BAR
        # Haut = jaune, bas = rouge
        r = 255
        g = int(40 + (220 - 40) * t)  # 40 (bas rouge) -> 220 (haut jaune)
        b = int(40 * (1 - t))          # 40 (bas) -> 0 (haut)
        color = (r, g, b, 255)
        d.rectangle([14 + 1, H_BAR - 1 - iy, 14 + W_BAR - 2, H_BAR - 1 - iy], fill=color)
    # Bordure filled
    d.rectangle([14, 0, 14+W_BAR-1, H_BAR-1], outline=VANILLA_DARK)

    # FLAME indicator - empty (x=28, y=0, 14x13)
    d.rectangle([28, 0, 28+13, 12], fill=VANILLA_DARK)
    d.rectangle([29, 1, 28+12, 11], fill=(30, 30, 30, 255))

    # FLAME indicator - filled (x=42, y=0, 14x13) - flamme orange/jaune
    d.rectangle([42, 0, 42+13, 12], fill=VANILLA_DARK)
    d.rectangle([43, 1, 42+12, 11], fill=(30, 30, 30, 255))
    # Flamme (petite silhouette centree)
    flame_pts = [(47, 9), (48, 9), (47, 7), (48, 7), (49, 7), (47, 5), (48, 5), (48, 3)]
    for px, py in flame_pts:
        d.point((px, py), fill=FLAME_ORANGE)
    d.point((48, 5), fill=FLAME_YELLOW)
    d.point((48, 7), fill=FLAME_YELLOW)

    img.save(f"{OUT_EL}/guipowerbar_nexus.png")
    print(f"[OK] guipowerbar_nexus.png (sprite sheet)")


# Run all
gen_main_bg()
gen_slot_sheet()
gen_progress_sheet()
gen_powerbar_sheet()
print("\nAll 4 textures regenerated (Mekanism-decomposed style).")
