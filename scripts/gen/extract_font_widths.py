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

Police normale (--ascii) : les titres, noms d'entree et noms de multiblock
sont dessines hors du texte, avec la police normale (ascii.png), pas la
police unicode. FontRenderer.readFontTexture : grille 16x16 de cases de
8 px ; largeur = derniere colonne non transparente + 2 ; espace = 4.
Seuls les caracteres de la table CHARSET de FontRenderer sont dans ascii.png,
les autres retombent sur la police unicode.

    python3 scripts/gen/extract_font_widths.py --ascii <minecraft-1.12.2.jar | ascii.png>

Sortie : scripts/gen/font_widths_ascii.json. Il faut Pillow (pip install pillow).
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
OUT_ASCII = os.path.join(HERE, 'font_widths_ascii.json')
ENTRY_ASCII = 'assets/minecraft/textures/font/ascii.png'
# FontRenderer.getCharWidth (1.12.2) : position du caractere dans ascii.png
CHARSET = (u"\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130"
           u"\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000"
           u" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000"
           u"\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5"
           u"\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192"
           u"\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb"
           u"\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510"
           u"\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567"
           u"\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580"
           u"\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229"
           u"\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000")


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


def build_ascii(path):
    import io
    from PIL import Image
    if zipfile.is_zipfile(path):
        with zipfile.ZipFile(path) as z:
            raw = z.read(ENTRY_ASCII)
    else:
        with open(path, 'rb') as f:
            raw = f.read()
    img = Image.open(io.BytesIO(raw)).convert('RGBA')
    if img.size != (128, 128) or len(CHARSET) != 256:
        raise SystemExit('ascii.png 128x128 attendu, lu %s' % (img.size,))
    alpha = img.getchannel('A').load()
    widths = {}
    for i, c in enumerate(CHARSET):
        if c == '\0':
            continue
        if c == ' ':
            widths[c] = 4
            continue
        col, row = i % 16, i // 16
        last = -1
        for x in range(7, -1, -1):
            if any(alpha[col * 8 + x, row * 8 + y] for y in range(8)):
                last = x
                break
        widths[c] = last + 2
    return {
        'source': 'minecraft 1.12.2 ' + ENTRY_ASCII,
        'sha1': hashlib.sha1(raw).hexdigest(),
        'rule': 'derniere colonne opaque + 2 ; espace = 4 ; hors table -> police unicode',
        'widths': widths,
    }


def main(argv):
    args = [a for a in argv if not a.startswith('--')]
    if '--ascii' in argv and len(args) == 1:
        data = build_ascii(args[0])
        with open(OUT_ASCII, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=1, sort_keys=True)
            f.write('\n')
        print('%s ecrit (sha1 %s)' % (os.path.relpath(OUT_ASCII), data['sha1']))
        return 0
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
