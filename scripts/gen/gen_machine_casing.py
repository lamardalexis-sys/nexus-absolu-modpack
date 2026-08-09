#!/usr/bin/env python3
"""
Genere la texture du casing structurel des multiblocs Nexus.

Methode (celle de MACHINE-TEXTURE.md, et celle qui a marche pour les 64 items) :
on ne reconstruit pas "ce qu'un casing devrait etre", on releve ce que les
textures de controleurs deja en jeu font, et on reproduit leur structure.

  1. lire une texture de controleur de reference ;
  2. en extraire la carte de luminance, la classer en 4 niveaux ;
  3. reutiliser les couleurs de ces 4 niveaux pour le cadre du casing.

Le casing est volontairement plus sobre que les controleurs : c'est de la
carcasse, elle ne doit pas voler la vedette au bloc qui porte l'identite de la
machine. Meme cadre, panneau neutre, quatre rivets.

Usage : python3 scripts/gen/gen_machine_casing.py
Sortie : mod-source/src/main/resources/assets/nexusabsolu/textures/blocks/machine_casing.png
"""

import os

from PIL import Image

# Toujours resoudre les chemins depuis l'emplacement du script : un sys.path
# ou un chemin relatif en dur fait lire un fichier et en modifier un autre.
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
BLOCKS = os.path.join(ROOT, "mod-source", "src", "main", "resources",
                     "assets", "nexusabsolu", "textures", "blocks")

REFERENCE = os.path.join(BLOCKS, "evaporator_controller.png")
OUTPUT = os.path.join(BLOCKS, "machine_casing.png")

SIZE = 32


def luminance(c):
    return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]


def extract_levels(path, count=4):
    """Les `count` couleurs dominantes de la reference, triees par luminance."""
    img = Image.open(path).convert("RGB")
    px = img.load()
    tally = {}
    for y in range(img.size[1]):
        for x in range(img.size[0]):
            c = px[x, y]
            tally[c] = tally.get(c, 0) + 1
    ranked = sorted(tally.items(), key=lambda kv: -kv[1])[: count * 3]
    ranked = sorted((c for c, _ in ranked), key=luminance)
    if len(ranked) < count:
        raise SystemExit("reference trop pauvre en couleurs : %s" % path)
    # On echantillonne regulierement dans l'echelle de luminance plutot que de
    # prendre les 4 premieres, sinon on ramasse quatre nuances voisines.
    step = (len(ranked) - 1) / float(count - 1)
    return [ranked[int(round(i * step))] for i in range(count)]


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def build(levels):
    # Les deux extremes sont releves sur la reference ; les tons intermediaires
    # sont interpoles entre eux. Les echantillonner aussi donnerait quatre
    # nuances trop voisines : le controleur passe l'essentiel de sa surface
    # dans un degrade serre, et le bevel disparaitrait.
    dark = levels[0]
    light = levels[-1]
    mid_light = mix(dark, light, 0.62)
    mid_dark = mix(dark, light, 0.20)

    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 255))
    px = img.load()

    panel = mix(dark, light, 0.32)

    for y in range(SIZE):
        for x in range(SIZE):
            edge = min(x, y, SIZE - 1 - x, SIZE - 1 - y)
            if edge < 2:
                col = dark
            elif edge < 4:
                # Bevel : lumiere en haut a gauche, ombre en bas a droite.
                top_left = (x + y) < (SIZE - 1)
                col = mid_light if top_left else mid_dark
            elif edge < 5:
                col = mix(mid_dark, panel, 0.5)
            else:
                col = panel
            px[x, y] = (col[0], col[1], col[2], 255)

    # Rivets aux quatre coins du panneau, 3x3, releve clair puis ombre.
    rivet_hi = mix(light, (255, 255, 255), 0.25)
    for rx, ry in ((8, 8), (SIZE - 11, 8), (8, SIZE - 11), (SIZE - 11, SIZE - 11)):
        for dy in range(3):
            for dx in range(3):
                if dx == 0 and dy == 0:
                    col = rivet_hi
                elif dx + dy <= 1:
                    col = light
                elif dx == 2 and dy == 2:
                    col = dark
                else:
                    col = mid_dark
                px[rx + dx, ry + dy] = col + (255,)

    # Trois fentes d'aeration horizontales, centrees. Ligne creuse sombre,
    # puis le liseret clair du bord inferieur, comme sur les casings IE.
    vent_dark = mix(dark, panel, 0.15)
    vent_edge = mix(panel, light, 0.45)
    for vy in (13, 17, 21):
        for x in range(13, SIZE - 13):
            px[x, vy] = vent_dark + (255,)
            px[x, vy + 1] = vent_edge + (255,)

    return img


def main():
    if not os.path.isfile(REFERENCE):
        raise SystemExit("reference introuvable : %s" % REFERENCE)
    levels = extract_levels(REFERENCE)
    print("niveaux releves sur %s :" % os.path.basename(REFERENCE))
    for c in levels:
        print("  rgb%s  luminance %.1f" % (c, luminance(c)))
    img = build(levels)
    img.save(OUTPUT)
    print("ecrit %s (%dx%d)" % (OUTPUT, img.size[0], img.size[1]))


if __name__ == "__main__":
    main()
