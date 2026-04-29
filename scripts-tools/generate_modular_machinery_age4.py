#!/usr/bin/env python3
"""
Genere les JSONs Modular Machinery pour Age 4 :
  - cryo_distillateur (7x5x2 socle + 3x3x11 tour)
  - bioreacteur (5x5x3 compact)

Specs validees par Alexis :
  - Cryo : socle 7x5 sur 2 couches + tour 3x3 hauteur 11 + 2 gros compresseurs
  - Bio-reacteur : 5x5x3 (au lieu de 7x7x7 trop gros)
"""
import json
import os

OUT_DIR = "config/modularmachinery/machinery"
os.makedirs(OUT_DIR, exist_ok=True)


def part(x, y, z, elements):
    return {"x": x, "y": y, "z": z, "elements": elements}


# ============================================================
# CRYO-DISTILLATEUR ATMOSPHERIQUE
# ============================================================
# Socle Y=-1 et Y=0 : 7x5 (X:-3..3, Z:-2..2)
# Tour Y=1..9 : 3x3 (X:-1..1, Z:-1..1)
# Sommet Y=10 : 3x3 couvercle decoratif
# Total : 35 + 35 + 8x10 + 9 = 159 blocs (controller compte pour 1)

cryo_parts = []

# Socle Y=-1 : 7x5 plein casings (35 blocs - controller = 34)
# Controller est implicite a (0,0,0), donc Y=-1 z=0 x=0 = OK ce n'est pas le controller
for x in range(-3, 4):
    for z in range(-2, 3):
        cryo_parts.append(part(x, -1, z, "casings_all"))

# Socle Y=0 : 7x5 avec compresseurs + ventilations
# Le controller est implicite a (0,0,0), donc on saute (0,0,0)
for x in range(-3, 4):
    for z in range(-2, 3):
        if x == 0 and z == 0:
            continue  # controller
        # Compresseurs 2 gros aux positions (-2,-1) et (2,-1) (front + back symetrique)
        if (x, z) in [(-2, -1), (2, -1), (-2, 1), (2, 1)]:
            cryo_parts.append(part(x, 0, z, "minecraft:iron_block"))
        # Ventilations entree air : (0, -1), (-1, 0), (1, 0), (0, 1)
        elif (x, z) in [(0, -1), (-1, 0), (1, 0), (0, 1)]:
            cryo_parts.append(part(x, 0, z,
                "modularmachinery:blockfluidinputhatch"))
        else:
            cryo_parts.append(part(x, 0, z, "casings_all"))

# Tour Y=1..9 : 3x3 (X:-1..1, Z:-1..1) avec niveaux alternes casing/glass
# Niveaux pairs (Y=2,4,6,8) : verre sur les 4 cotes du milieu
# Niveaux impairs (Y=1,3,5,7,9) : casings pleins sur le pourtour
for y in range(1, 10):
    for x in range(-1, 2):
        for z in range(-1, 2):
            # Centre creux a tous les Y
            if x == 0 and z == 0:
                # Y=9 centre = sortie fluides
                if y == 9:
                    cryo_parts.append(part(x, y, z,
                        "modularmachinery:blockfluidoutputhatch"))
                continue
            # Niveaux pairs : glass sur les 4 cotes du milieu (pas les coins)
            if y % 2 == 0:
                if (x, z) in [(0, -1), (0, 1), (-1, 0), (1, 0)]:
                    cryo_parts.append(part(x, y, z, "minecraft:glass"))
                else:
                    cryo_parts.append(part(x, y, z, "casings_all"))
            else:
                cryo_parts.append(part(x, y, z, "casings_all"))

# Sommet Y=10 : 3x3 couvercle
for x in range(-1, 2):
    for z in range(-1, 2):
        cryo_parts.append(part(x, 10, z, "casings_decorative"))

cryo_machine = {
    "registryname": "cryo_distillateur",
    "localizedname": "Cryo-Distillateur Atmospherique",
    "parts": cryo_parts
}

with open(os.path.join(OUT_DIR, "cryo_distillateur.json"), 'w') as f:
    json.dump(cryo_machine, f, indent=2)

print(f"OK : cryo_distillateur.json ({len(cryo_parts)} parts)")


# ============================================================
# BIO-REACTEUR (compact 5x5x3)
# ============================================================
# Base Y=-1 : 5x5 plein casings (25 blocs)
# Mur Y=0 : 5x5 anneau (interieur creux 3x3 vide), controller au centre
# Mur Y=1 : 5x5 anneau (interieur creux 3x3 vide)
# Toit Y=2 : 5x5 plein
# Total : 25 + 16 + 16 + 25 = 82 blocs (controller compte pour 1)

bio_parts = []

# Base Y=-1 : 5x5 plein
for x in range(-2, 3):
    for z in range(-2, 3):
        bio_parts.append(part(x, -1, z, "casings_all"))

# Mur Y=0 : 5x5 anneau (controller au centre, on skip 0,0,0)
for x in range(-2, 3):
    for z in range(-2, 3):
        if x == 0 and z == 0:
            continue  # controller
        # Anneau exterieur seulement
        if x == -2 or x == 2 or z == -2 or z == 2:
            # 4 inputs/outputs aux milieux des cotes
            if (x, z) == (-2, 0):
                bio_parts.append(part(x, 0, z,
                    "modularmachinery:blockfluidinputhatch"))
            elif (x, z) == (2, 0):
                bio_parts.append(part(x, 0, z,
                    "modularmachinery:blockfluidoutputhatch"))
            elif (x, z) == (0, -2):
                bio_parts.append(part(x, 0, z,
                    "modularmachinery:blockinputbus"))
            elif (x, z) == (0, 2):
                bio_parts.append(part(x, 0, z,
                    "modularmachinery:blockoutputbus"))
            else:
                bio_parts.append(part(x, 0, z, "casings_all"))
        # Interieur (3x3 sauf controller) = vide

# Mur Y=1 : 5x5 anneau, glass sur les 4 cotes du milieu pour visualisation
for x in range(-2, 3):
    for z in range(-2, 3):
        if x == -2 or x == 2 or z == -2 or z == 2:
            # Verre sur les 4 cotes du milieu (vue interieur)
            if (x, z) in [(0, -2), (0, 2), (-2, 0), (2, 0)]:
                bio_parts.append(part(x, 1, z, "minecraft:glass"))
            # Energy input sur un coin
            elif (x, z) == (-2, -2):
                bio_parts.append(part(x, 1, z,
                    "modularmachinery:blockenergyinputhatch"))
            else:
                bio_parts.append(part(x, 1, z, "casings_all"))

# Toit Y=2 : 5x5 plein decoratif
for x in range(-2, 3):
    for z in range(-2, 3):
        bio_parts.append(part(x, 2, z, "casings_decorative"))

bio_machine = {
    "registryname": "bioreacteur",
    "localizedname": "Bio-Reacteur Manifold",
    "parts": bio_parts
}

with open(os.path.join(OUT_DIR, "bioreacteur.json"), 'w') as f:
    json.dump(bio_machine, f, indent=2)

print(f"OK : bioreacteur.json ({len(bio_parts)} parts)")
print()
print("Resume :")
print(f"  Cryo-distillateur : 7x5x2 socle + 3x3x11 tour = {len(cryo_parts) + 1} blocs")
print(f"  Bio-reacteur       : 5x5x3 compact = {len(bio_parts) + 1} blocs")
