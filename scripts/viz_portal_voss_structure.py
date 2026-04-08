#!/usr/bin/env python3
"""
Parse TilePortalVoss.STRUCTURE array and generate a visual layer-by-layer
schematic of the Portal Voss expected structure.

This is a diagnostic tool: it shows EXACTLY what blocks the validator
expects at each position, so the player can compare to their build.
"""

# Block type codes (must match TilePortalVoss)
W    = 0  # nexus_wall
W2   = 1  # nexus_wall_t2
V3   = 2  # vossium_iii_block
V4   = 3  # vossium_iv_block
EIN  = 4  # energy_input
LIN  = 5  # fluid_input
CD   = 6  # compose_block_d
LAVA = 7  # lava
EC   = 8  # ecran_controle (the master block, at origin 0,0,0)

# 1-char names for the grid display
NAMES = {
    W:    "w",
    W2:   "W",  # uppercase = T2
    V3:   "3",  # Vossium III
    V4:   "4",  # Vossium IV
    EIN:  "E",  # Energy
    LIN:  "L",  # Liquid
    CD:   "D",  # Compose D
    LAVA: "~",  # Lava
    EC:   "X",  # EcranControle (master)
}

LABELS = {
    W:    "w = Nexus Wall (normal)",
    W2:   "W = Nexus Wall T2 (violet anime)",
    V3:   "3 = Bloc de Vossium III",
    V4:   "4 = Bloc de Vossium IV",
    EIN:  "E = Energy Input (bloc dedie)",
    LIN:  "L = Fluid Input (bloc dedie)",
    CD:   "D = Bloc de Compose D",
    LAVA: "~ = Lava (vanilla)",
    EC:   "X = Ecran de Controle (MASTER, le bloc que tu cliques)",
}

STRUCTURE = []

def add_row(y, z, types):
    for i, t in enumerate(types):
        x = i - 3  # col 0 = x=-3
        STRUCTURE.append((x, y, z, t))

def add(x, y, z, t):
    STRUCTURE.append((x, y, z, t))

# Rebuilt from TilePortalVoss static initializer (lines 40-93)

# === LAYER 0 (y=-5): 7x7 base ===
add_row(-5, -3, [W2, W2, W2, EIN, W2, W2, W2])
add_row(-5, -2, [W2, W2, V3, V3, V3, W2, W2])
add_row(-5, -1, [W2, V3, W2, V3, W2, V3, W2])
add_row(-5,  0, [W2, V3, V3, V4, V3, V3, LIN])
add_row(-5,  1, [W2, V3, W2, V3, W2, V3, W2])
add_row(-5,  2, [W2, W2, V3, V3, V3, W2, W2])
add_row(-5,  3, [W2, W2, W2, W2, W2, W2, W2])

# === LAYER 1 (y=-4): walls + lava pool + compose D ===
add(-2, -4, -2, CD); add(-1, -4, -2, W); add(0, -4, -2, W)
add( 1, -4, -2, W);  add( 2, -4, -2, CD)
add(-2, -4, -1, W);  add(-1, -4, -1, LAVA); add(0, -4, -1, LAVA)
add( 1, -4, -1, LAVA); add(2, -4, -1, W)
add(-2, -4,  0, W);  add(-1, -4,  0, LAVA); add(0, -4,  0, V4)
add( 1, -4,  0, LAVA); add(2, -4,  0, W)
add(-2, -4,  1, W);  add(-1, -4,  1, LAVA); add(0, -4,  1, LAVA)
add( 1, -4,  1, LAVA); add(2, -4,  1, W)
add(-2, -4,  2, CD); add(-1, -4,  2, W); add(0, -4,  2, W)
add( 1, -4,  2, W);  add( 2, -4,  2, CD)

# === LAYER 2 (y=-3): 3x3 column ===
add(-1, -3, -1, W); add(0, -3, -1, W);  add(1, -3, -1, W)
add(-1, -3,  0, W); add(0, -3,  0, W2); add(1, -3,  0, W)
add(-1, -3,  1, W); add(0, -3,  1, W);  add(1, -3,  1, W)

# === LAYER 3 (y=-2): twin pillars ===
add(-1, -2, 0, W2); add(1, -2, 0, W2)

# === LAYER 4 (y=-1): twin pillars ===
add(-1, -1, 0, W2); add(1, -1, 0, W2)

# === LAYER 5 (y=0): EC + pillars (EC is master, implicit) ===
add(-1, 0, 0, W2); add(1, 0, 0, W2)

# === LAYER 6 (y=+1): horizontal bridge ===
add(-2, 1, 0, W2); add(-1, 1, 0, W); add(0, 1, 0, W)
add( 1, 1, 0, W);  add( 2, 1, 0, W2)

# === LAYER 7 (y=+2): wider horns ===
add(-3, 2, 0, W); add(-2, 2, 0, W)
add( 2, 2, 0, W); add( 3, 2, 0, W)

# === LAYER 8 (y=+3): horn tips ===
add(-3, 3, 0, W); add(3, 3, 0, W)


# === RENDER ===

def layer_grid(y):
    """Return a dict {(x, z): type} for all blocks at height y."""
    return {(x, z): t for (x, yy, z, t) in STRUCTURE if yy == y}

def render_layer(y, label, ec_here=False):
    grid = layer_grid(y)
    if not grid and not ec_here:
        return None

    # Find bounds
    xs = {x for (x, _) in grid}
    zs = {z for (_, z) in grid}
    if ec_here:
        xs.add(0); zs.add(0)
    x_min, x_max = min(xs), max(xs)
    z_min, z_max = min(zs), max(zs)

    # Extend bounds slightly for readability
    x_min -= 1; x_max += 1
    z_min -= 1; z_max += 1

    lines = [f"=== LAYER y={y:+d}  ({label}) ===", ""]

    # Column header (x values)
    header = "        "
    for x in range(x_min, x_max + 1):
        header += f"{x:+3d} "
    lines.append(header)
    lines.append("      +" + "-" * ((x_max - x_min + 1) * 4) + "+")

    for z in range(z_min, z_max + 1):
        row = f"  z={z:+2d} | "
        for x in range(x_min, x_max + 1):
            if ec_here and x == 0 and z == 0:
                char = "X"  # EC master
            elif (x, z) in grid:
                char = NAMES[grid[(x, z)]]
            else:
                char = "."  # Air / not required
            row += f" {char}  "
        row += "|"
        lines.append(row)

    lines.append("      +" + "-" * ((x_max - x_min + 1) * 4) + "+")
    return "\n".join(lines)


print("#" * 72)
print("# PORTAIL VOSS - Structure attendue par TilePortalVoss.checkStructure()")
print("#" * 72)
print()
print("LEGENDE:")
for t in sorted(NAMES.keys()):
    print(f"  {NAMES[t]}  {LABELS[t]}")
print("  .  (air / pas de bloc requis)")
print()
print("ORIENTATION:")
print("  L'Ecran de Controle (X) est le bloc MASTER a y=0 (niveau de reference).")
print("  x = axe est-ouest (negatif = ouest)")
print("  z = axe nord-sud (negatif = nord)")
print("  y = hauteur (0 = niveau de l'ecran, negatif = en dessous)")
print("  La structure supporte les 4 rotations cardinales automatiquement.")
print()
print()

# Sorted layers from lowest to highest
ys = sorted({y for (_, y, _, _) in STRUCTURE} | {0})
layer_labels = {
    -5: "BASE 7x7 - Fondation de vossium III/IV avec Energy/Fluid hatches",
    -4: "BASSIN - Piscine de lave autour du cube V4 + coins Compose D",
    -3: "COLONNE - Plateforme 3x3 en walls avec coeur W2",
    -2: "PILIERS BAS - 2 piliers W2 de chaque cote du futur portail",
    -1: "PILIERS MILIEU - 2 piliers W2 (zone du portail entre les 2)",
     0: "ECRAN DE CONTROLE + PILIERS - master au milieu, piliers W2 a cote",
     1: "PONT HORIZONTAL - passerelle de walls avec extremites W2",
     2: "CORNES EXTERIEURES - 2 walls de chaque cote, 1 bloc de plus qu'en dessous",
     3: "POINTES DES CORNES - 2 walls isoles aux extremites",
}

for y in ys:
    label = layer_labels.get(y, "???")
    ec_here = (y == 0)
    rendered = render_layer(y, label, ec_here=ec_here)
    if rendered:
        print(rendered)
        print()

# Count blocks
counts = {}
for (_, _, _, t) in STRUCTURE:
    counts[t] = counts.get(t, 0) + 1

print()
print("#" * 72)
print("# MATERIAUX NECESSAIRES (hors Ecran de Controle, pose en dernier)")
print("#" * 72)
for t in sorted(counts.keys()):
    name = LABELS[t].split(" = ")[1]
    print(f"  {counts[t]:3d} x  {name}")
total = sum(counts.values()) + 1
print(f"  ---")
print(f"  {total:3d} blocs au total (incluant l'Ecran de Controle)")
