# -*- coding: utf-8 -*-
"""Extrait les largeurs de la police unicode de Minecraft 1.12.2.

Le Carnet Voss est rendu avec la police UNICODE, pas la police ASCII :
BookTextParser.parse() pose setUnicodeFlag(true) tant que book.json n'a pas
"use_blocky_font", et le Carnet ne l'a pas. En mode unicode,
FontRenderer.getCharWidth() lit assets/minecraft/font/glyph_sizes.bin :
un octet par point de code, colonne de debut dans le quartet haut, colonne
de fin dans le quartet bas, glyphe de 16 px dessine a demi-echelle.

    largeur = ((fin + 1) - debut) // 2 + 1      (0 si l'octet vaut 0)
    espace  = 4,  U+00A0 = 4,  paragraphe (U+00A7) = -1 (code de format)

Sortie : scripts/gen/font_widths.json, un chiffre par point de code de
U+0000 a U+2FFF (latin, ponctuation, fleches, puces). patchouli_layout.py
le lit ; au-dela de la plage il s'arrete en erreur plutot que de deviner.

Usage, au choix :

    python3 scripts/gen/extract_font_widths.py <minecraft-1.12.2.jar>
    python3 scripts/gen/extract_font_widths.py <glyph_sizes.bin>

Sur la machine de dev (CurseForge), le jar est en general dans
~/curseforge/minecraft/Install/versions/1.12.2/1.12.2.jar.
Avec --verify, compare au font_widths.json deja present au lieu d'ecrire.
"""
import hashlib
import json
import os
import sys
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, 'font_widths.json')
ENTRY = 'assets/minecraft/font/glyph_sizes.bin'
LIMIT = 0x3000


def read_table(path):
    if zipfile.is_zipfile(path):
        with zipfile.ZipFile(path) as z:
            return z.read(ENTRY)
    with open(path, 'rb') as f:
        return f.read()


def width_of(byte):
    if byte == 0:
        return 0
    start = byte >> 4
    end = (byte & 15) + 1
    return (end - start) // 2 + 1


def build(table):
    if len(table) != 65536:
        raise SystemExit('glyph_sizes.bin doit faire 65536 octets, lu : %d' % len(table))
    digits = []
    for cp in range(LIMIT):
        w = width_of(table[cp])
        if not 0 <= w <= 9:
            raise SystemExit('largeur hors plage pour U+%04X : %d' % (cp, w))
        digits.append(str(w))
    return {
        'source': 'minecraft 1.12.2 ' + ENTRY,
        'sha1': hashlib.sha1(table).hexdigest(),
        'range': 'U+0000..U+%04X' % (LIMIT - 1),
        'rule': '((low+1)-high)//2+1 ; space=4, U+00A0=4, U+00A7=-1',
        'widths': ''.join(digits),
    }


def main(argv):
    args = [a for a in argv if not a.startswith('--')]
    if len(args) != 1:
        print(__doc__)
        return 2
    data = build(read_table(args[0]))
    if '--verify' in argv:
        with open(OUT, encoding='utf-8') as f:
            have = json.load(f)
        same = have['widths'] == data['widths']
        print('sha1 lu      :', data['sha1'])
        print('sha1 du repo :', have['sha1'])
        print('largeurs identiques' if same else 'LARGEURS DIFFERENTES')
        return 0 if same else 1
    with open(OUT, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=1)
        f.write('\n')
    print('%s ecrit (sha1 %s)' % (os.path.relpath(OUT), data['sha1']))
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
