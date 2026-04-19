"""
Generate furnace_upgrade_kit item texture (16x16).

Style : petit boitier tech/circuit board avec LED cyan au centre,
contours metalliques chromes, ambiance 'high-tech Voss'.

Layout 16x16 :
  - Fond noir transparent autour
  - Rectangle metalique gris fonce (bordure chrome brillant)
  - Circuits interieurs (lignes fines bleues/cyan)
  - LED cyan au centre (2x2 pixels, meme style que sur les furnaces)
  - Couleurs : noir, gris metal, chrome bright, cyan LED, violet accent Voss
"""
from PIL import Image, ImageDraw

W, H = 16, 16
img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
px = img.load()

# Palette
METAL_DARK = (60, 60, 70, 255)       # corps metal
METAL_LIGHT = (120, 120, 135, 255)   # highlight metal
CHROME = (200, 210, 220, 255)        # bords brillants
CIRCUIT_BG = (20, 20, 35, 255)        # fond circuit
LED_CORE = (102, 255, 238, 255)      # cyan brillant (match furnaces)
LED_GLOW = (51, 180, 200, 220)
VOSS_PURPLE = (170, 110, 255, 255)   # accent lore

# Rectangle corps du kit (2,2) a (13,13), laisse bordure transparente
# Corps interieur
for x in range(3, 13):
    for y in range(3, 13):
        px[x, y] = CIRCUIT_BG

# Bordure metalique (contour du rectangle)
# Top et bottom
for x in range(2, 14):
    px[x, 2] = CHROME
    px[x, 13] = METAL_DARK
# Gauche et droite
for y in range(2, 14):
    px[2, y] = CHROME
    px[13, y] = METAL_DARK

# Coin bas-droit sombre, coin haut-gauche bright (shading)
px[2, 2] = (220, 230, 240, 255)    # lumiere
px[13, 13] = (40, 40, 50, 255)      # ombre

# "Connecteurs" aux 4 coins du corps interieur (petits carres metalliques)
for dx, dy in [(3, 3), (12, 3), (3, 12), (12, 12)]:
    px[dx, dy] = METAL_LIGHT

# Circuits : 2 lignes horizontales cyan
px[4, 6] = LED_GLOW
px[5, 6] = LED_GLOW
px[6, 6] = LED_CORE
px[9, 6] = LED_CORE
px[10, 6] = LED_GLOW
px[11, 6] = LED_GLOW

px[4, 10] = LED_GLOW
px[5, 10] = LED_GLOW
px[6, 10] = LED_CORE
px[9, 10] = LED_CORE
px[10, 10] = LED_GLOW
px[11, 10] = LED_GLOW

# LED centrale 2x2 cyan brillant (power indicator)
for dx in range(2):
    for dy in range(2):
        px[7 + dx, 7 + dy] = LED_CORE

# Halo autour de la LED (cercle 4x4 avec cyan moins sature)
for dx, dy in [(6, 7), (6, 8), (9, 7), (9, 8), (7, 6), (8, 6), (7, 9), (8, 9)]:
    # Blend avec existing (qui est CIRCUIT_BG)
    existing = px[dx, dy]
    alpha = LED_GLOW[3] / 255.0
    r = int(existing[0] * (1 - alpha) + LED_GLOW[0] * alpha)
    g = int(existing[1] * (1 - alpha) + LED_GLOW[1] * alpha)
    b = int(existing[2] * (1 - alpha) + LED_GLOW[2] * alpha)
    px[dx, dy] = (r, g, b, 255)

# Accent violet Voss : petit trait en diagonale coin bas-gauche (signature)
px[4, 11] = VOSS_PURPLE
px[4, 12] = VOSS_PURPLE

out = "/home/claude/nexus-absolu/mod-source/src/main/resources/assets/nexusabsolu/textures/items/furnace_upgrade_kit.png"
img.save(out)
print(f"Saved {out}")
