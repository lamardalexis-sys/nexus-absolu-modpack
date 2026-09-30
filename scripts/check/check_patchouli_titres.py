# -*- coding: utf-8 -*-
"""Verifie la LARGEUR de tout ce que Patchouli dessine sans retour a la ligne.

check_patchouli_overflow.py mesure la hauteur du texte des pages, qui passe
a la ligne tout seul. Mais Patchouli 1.12 dessine certains textes d'un bloc,
centres, sans jamais les couper (GuiBook.drawCenteredStringNoShadow) :
s'ils sont plus larges que la page, ils debordent sur la marge ou sur la
page d'en face. C'est ce que ce script verifie :

  - nom d'entree, en tete de sa premiere page (PageText.java:47)
  - "title" des pages text / spotlight / crafting / image / relations
  - "name" des pages multiblock (PageMultiblock.java:109)
  - nom de categorie, en tete de la liste d'entrees (GuiBookEntryList.java:87)
  - sous-titre du livre, sur le bandeau de la page d'accueil
    (GuiBookLanding.drawHeader : x = 24, bandeau jusqu'a x = 132)
  - nom d'entree dans la liste de la categorie (GuiButtonEntry.java:68),
    dessine dans la police du livre, a x+12, avant la marque de lecture

Les en-tetes sont dans la police normale (ascii.png), sauf si le joueur
active Force Unicode Font : tout passe alors en unicode, plus etroite sauf
pour 'i' (3 px au lieu de 2), sans consequence sur le Carnet : largeurs
dans scripts/gen/font_widths_ascii.json (extract_font_widths.py --ascii).
La liste des entrees suit "use_blocky_font" de book.json (book_font()).
Le gras ajoute 1 px par caractere, comme FontRenderer.getStringWidth.

    python3 scripts/check/check_patchouli_titres.py [-v]
"""
import glob
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
BOOK = os.path.join(ROOT, 'mod-source', 'src', 'main', 'resources', 'assets',
                    'nexusabsolu', 'patchouli_books', 'voss_codex')
GEN = os.path.join(ROOT, 'scripts', 'gen')

PAGE_WIDTH = 116
INDEX_WIDTH = PAGE_WIDTH - 12 - 5      # icone a gauche, marque de lecture a droite
SUBTITLE_WIDTH = 132 - 24              # bandeau de -8 a 132, texte a partir de 24
# Types 1.12.2 qui dessinent un titre (ClientBookRegistry : type brut, sans
# espace de noms). crafting / smelting / spotlight sans titre dessinent le nom
# de l'item : non mesurable ici, signale a part.
TITLED = {'text', 'link', 'quest', 'spotlight', 'crafting', 'smelting', 'image',
          'relations', 'entity', 'multiblock'}
ITEM_TITLE = {'spotlight', 'crafting', 'smelting'}


sys.path.insert(0, GEN)
import patchouli_layout as P  # noqa: E402

try:
    HEADER = P.BlockyFont()     # en-tetes : police normale (sauf Force Unicode Font)
    BODY = P.book_font()        # liste des entrees : la police du livre
except P.LayoutError as e:
    raise SystemExit('ERREUR %s' % e)


def items(lang):
    base = os.path.join(BOOK, lang)
    with open(os.path.join(BOOK, 'book.json'), encoding='utf-8') as f:
        book = json.load(f)
    yield 'book.json', 'sous-titre', book.get('subtitle', ''), BODY, SUBTITLE_WIDTH
    for path in sorted(glob.glob(os.path.join(base, 'categories', '*.json'))):
        with open(path, encoding='utf-8') as f:
            d = json.load(f)
        yield path, 'categorie', d['name'], HEADER, PAGE_WIDTH
    for path in sorted(glob.glob(os.path.join(base, 'entries', '**', '*.json'), recursive=True)):
        with open(path, encoding='utf-8') as f:
            d = json.load(f)
        yield path, 'entree', d['name'], HEADER, PAGE_WIDTH
        yield path, 'index', d['name'], BODY, INDEX_WIDTH
        for n, p in enumerate(d.get('pages', [])):
            for key in ('title', 'name'):
                if p.get('type') in TITLED and p.get(key):
                    yield path, 'page %d %s' % (n, key), p[key], HEADER, PAGE_WIDTH
            if p.get('type') in ITEM_TITLE and not p.get('title'):
                yield path, 'page %d sans titre' % n, None, HEADER, PAGE_WIDTH


def main():
    verbose = '-v' in sys.argv
    bad = 0
    for lang in ('fr_fr', 'en_us'):
        for path, where, text, font, limit in items(lang):
            if text is None:
                bad += 1
                print('%s %-26s titre absent : Patchouli dessine le nom de l\'item, '
                      'donner un "title" court  (%s)' % (lang, where, os.path.basename(path)))
                continue
            w = font.string_width(text)
            if w > limit:
                bad += 1
                print('%s %-26s %3d/%d px  %s  (%s)' % (lang, where, w, limit, text,
                                                        os.path.basename(path)))
            elif verbose:
                print('ok  %-26s %3d/%d px  %s' % (where, w, limit, text))
    print('%d texte(s) plus large(s) que la page' % bad)
    return 1 if bad else 0


if __name__ == '__main__':
    sys.exit(main())
