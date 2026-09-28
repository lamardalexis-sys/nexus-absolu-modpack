# -*- coding: utf-8 -*-
"""Controle : aucune page du Carnet Voss ne sort du parchemin.

Meme moteur de mise en page que le decoupage (scripts/gen/patchouli_layout.py),
portage de Patchouli 1.12.2 avec les largeurs de la police unicode.

Controle :
  - texte des pages "text" (14 lignes en page 0, 16 avec titre, 17 sans)
  - texte des pages "multiblock" (4 lignes sous le rendu 3D)
  - pages de suite bien formees (reprise presente, page de texte avant)
  - aucune fausse balise fermante $(/l) $(/o) $(/li), pages, landing_text et
    descriptions de categories (voir fix_patchouli_closers.py)

    python3 scripts/check/check_patchouli_overflow.py        # resume
    python3 scripts/check/check_patchouli_overflow.py -v     # lignes des pages en faute

Code de sortie 1 s'il y a une erreur.
"""
import glob
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, os.path.join(ROOT, 'scripts', 'gen'))
import patchouli_layout as P  # noqa: E402

BOOK = os.path.join(ROOT, 'mod-source/src/main/resources/assets/nexusabsolu/'
                          'patchouli_books/voss_codex')
MARK = 'nexus_suite'
FAKE_CLOSER = re.compile(r'\$\(/(l|o|li)\)')


def main(argv):
    verbose = '-v' in argv
    try:
        font = P.Font()
    except P.LayoutError as e:
        print('ERREUR', e)
        return 1
    errors = []
    pages = 0
    suites = 0
    for path in sorted(glob.glob(BOOK + '/*/entries/*.json')):
        rel = os.path.relpath(path, BOOK)
        with open(path, encoding='utf-8') as f:
            data = json.load(f)
        prev = None
        for i, page in enumerate(data.get('pages', [])):
            where = '%s p%d' % (rel, i)
            suite = page.get(MARK)
            if suite is not None:
                suites += 1
                if prev is None or prev.get('type') != 'text':
                    errors.append((where, 'page de suite sans page de texte avant', None))
                if not page.get('text', '').startswith(suite.get('reprise', '')):
                    errors.append((where, 'reprise absente : suite modifiee a la main ?', None))
                if page.get('title'):
                    errors.append((where, 'une page de suite ne doit pas avoir de titre', None))
            prev = page
            text = page.get('text')
            top = P.page_top(page, i)
            if text is None or top is None:
                continue
            pages += 1
            if FAKE_CLOSER.search(text):
                errors.append((where, 'fausse balise fermante %s' % FAKE_CLOSER.search(text).group(0), None))
            try:
                lay = P.layout(font, text, top)
            except P.LayoutError as e:
                errors.append((where, str(e), None))
                continue
            used, budget = lay.lines_used(), P.budget(top)
            if used > budget:
                errors.append((where, '%s : %d lignes pour %d' % (page['type'], used, budget), lay))
    for path in [os.path.join(BOOK, 'book.json')] + sorted(glob.glob(BOOK + '/*/categories/*.json')):
        with open(path, encoding='utf-8') as f:
            data = json.load(f)
        for key in ('landing_text', 'description'):
            m = FAKE_CLOSER.search(data.get(key, ''))
            if m:
                errors.append(('%s %s' % (os.path.relpath(path, BOOK), key),
                               'fausse balise fermante %s' % m.group(0), None))
    for where, msg, lay in errors:
        print('  ERREUR  %s -- %s' % (where, msg))
        if verbose and lay is not None:
            limit = lay.top + P.budget(lay.top) * P.LINE
            for y, line in lay.lines():
                print('      %s %4d |%s' % ('>>' if y >= limit else '  ', y, line))
    print('\n%d pages mesurees (police %s), %d pages de suite, %d erreur(s)'
          % (pages, font.sha1[:12], suites, len(errors)))
    return 1 if errors else 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
