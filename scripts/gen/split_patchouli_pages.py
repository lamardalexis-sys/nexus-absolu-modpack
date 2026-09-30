# -*- coding: utf-8 -*-
"""Recoupe les pages du Carnet Voss qui sortent du parchemin.

Patchouli ne pagine pas : un texte trop long deborde sous la page. Ce script
coupe chaque page trop longue et insere des pages de suite juste apres,
avec le meme moteur de mise en page que le controle (patchouli_layout.py).

Ou couper, par ordre de preference :
  1. entre deux paragraphes      $(br2)  $(p)  $(2br)
  2. a un retour a la ligne      $(br)
  3. avant un element de liste   $(li)
  4. entre deux phrases
  5. entre deux mots, en dernier recours
On garde le niveau le plus haut qui remplit au moins la moitie de la page ;
sinon la coupe qui la remplit le plus.

Une page de suite n'a pas de titre et porte un champ "nexus_suite" :
  {"sep": <separateur retire a la coupe>, "reprise": <styles rouverts>}
Patchouli l'ignore (Gson brut, ClientBookRegistry). Le script s'en sert pour
recoller exactement : page + sep + (suite sans sa reprise). La reprise
rouvre la couleur et le style actifs au point de coupe -- chaque page
repart de zero dans Patchouli.

Le script recolle toujours avant de recouper : il est idempotent.

    python3 scripts/gen/split_patchouli_pages.py              # recolle puis recoupe
    python3 scripts/gen/split_patchouli_pages.py --merge-only # recolle seulement
    python3 scripts/gen/split_patchouli_pages.py --dry-run    # n'ecrit rien

--merge-only avant de relancer un generateur : ils reperent leurs pages par
leur contenu, et sur une page coupee ils n'en trouveraient qu'une moitie.
"""
import glob
import json
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import patchouli_layout as P  # noqa: E402

BOOK = 'mod-source/src/main/resources/assets/nexusabsolu/patchouli_books/voss_codex'
MARK = 'nexus_suite'
CMD = re.compile(r'\$\([^)]*\)')
COLOR = re.compile(r'^(#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6}|[0-9a-f])$')
CODES = {'k', 'obf', 'l', 'bold', 'm', 'strike', 'o', 'italic', 'italics'}
LEVELS = ('paragraphe', 'ligne', 'liste', 'phrase', 'mot')
INHERITED = ('advancement', 'flag')     # BookPage : verrouillage de la page


class SplitError(Exception):
    pass


# ---------------------------------------------------------------- recollage

def merge_pages(pages):
    """Recolle chaque page de suite dans la page qui la precede."""
    out = []
    for page in pages:
        suite = page.get(MARK)
        if suite is None:
            out.append(page)
            continue
        if not out or out[-1].get('type') != 'text':
            raise SplitError('page de suite sans page de texte avant elle')
        text = page.get('text', '')
        reprise = suite.get('reprise', '')
        if not text.startswith(reprise):
            raise SplitError('suite modifiee a la main : la reprise %r manque' % reprise)
        out[-1] = dict(out[-1])
        out[-1]['text'] = out[-1].get('text', '') + suite.get('sep', '') + text[len(reprise):]
    return out


# ---------------------------------------------------------------- coupe

def state_at(text):
    """Couleur et style Patchouli actifs a la fin de `text` -> reprise."""
    for key, value in P.DEFAULT_MACROS.items():
        text = text.replace(key, value)
    color = None
    code = None
    for m in CMD.finditer(text):
        c = m.group(0)[2:-1]
        if c in ('', 'reset', 'clear'):
            color = None
            code = None
        elif c == '/l':
            color = None                    # sans lien ouvert : couleur de base
        elif c == 'nocolor':
            color = '0'                     # font.getColorCode('0')
        elif COLOR.match(c):
            color = c
        elif c in CODES:
            code = c
    return ('$(%s)' % color if color else '') + ('$(%s)' % code if code else '')


def candidates(text):
    """Toutes les coupes possibles : (niveau, debut, fin, sep).

    La page garde text[:debut], la suite reprend a text[fin:], et
    text[debut:fin] == sep est ce qui disparait entre les deux.
    """
    out = []
    pos = 0
    for m in CMD.finditer(text):
        plain = text[pos:m.start()]
        _plain_cuts(out, plain, pos)
        c = m.group(0)[2:-1]
        if c in ('br2', 'p', '2br'):
            out.append((0, m.start(), m.end(), m.group(0)))
        elif c == 'br':
            out.append((1, m.start(), m.end(), m.group(0)))
        elif re.fullmatch(r'(li|list)[0-9]?', c):
            out.append((2, m.start(), m.start(), ''))
        pos = m.end()
    _plain_cuts(out, text[pos:], pos)
    return [c for c in out if 0 < c[1] and text[c[2]:].strip() and CMD.sub('', text[c[2]:]).strip()]


def _plain_cuts(out, plain, base):
    for m in re.finditer(r' ', plain):
        before = plain[:m.start()].rstrip()
        level = 3 if before.endswith(('.', '!', '?', ':', ';')) else 4
        out.append((level, base + m.start(), base + m.end(), ' '))


def split_text(font, text, top):
    """Rend [(texte, sep, reprise)] : la premiere entree garde sep=reprise=None."""
    budget = P.budget(top)
    if P.layout(font, text, top).lines_used() <= budget:
        return [(text, None, None)]
    best = {}                                   # niveau -> (lignes, coupe)
    for cut in candidates(text):
        level, start, _, _ = cut
        used = P.layout(font, text[:start], top).lines_used()
        if used > budget or used == 0:
            continue
        if level not in best or start > best[level][1][1]:
            best[level] = (used, cut)
    if not best:
        raise SplitError('aucune coupe ne tient sur la page : un mot plus large que la page ?')
    chosen = None
    for level in sorted(best):
        if best[level][0] * 2 >= budget:
            chosen = best[level][1]
            break
    if chosen is None:
        chosen = max(best.values(), key=lambda b: (b[0], -b[1][0]))[1]
    _, start, end, sep = chosen
    reprise = state_at(text[:start])
    rest = split_text(font, reprise + text[end:], P.TOP_UNTITLED)
    first = rest[0]
    rest[0] = (first[0], sep, reprise)
    return [(text[:start], None, None)] + rest


def split_pages(font, pages):
    out = []
    for page in pages:
        top = P.page_top(page, len(out))
        if page.get('type') != 'text' or top is None or 'text' not in page:
            out.append(page)
            continue
        parts = split_text(font, page['text'], top)
        head = dict(page)
        head['text'] = parts[0][0]
        out.append(head)
        for text, sep, reprise in parts[1:]:
            suite = {'type': 'text', 'text': text,
                     MARK: {'sep': sep, 'reprise': reprise}}
            for key in INHERITED:             # une suite se debloque avec sa page
                if key in page:
                    suite[key] = page[key]
            out.append(suite)
    return out


# ---------------------------------------------------------------- fichiers

def entry_files():
    return sorted(glob.glob(BOOK + '/*/entries/*.json'))


def load(path):
    with open(path, encoding='utf-8') as f:
        raw = f.read()
    return raw, json.loads(raw)


def dump(data, raw):
    return json.dumps(data, ensure_ascii=False, indent=2) + ('\n' if raw.endswith('\n') else '')


def main(argv):
    merge_only = '--merge-only' in argv
    dry = '--dry-run' in argv
    font = None if merge_only else P.book_font()
    changed = 0
    added = 0
    for path in entry_files():
        raw, data = load(path)
        before = len(data.get('pages', []))
        pages = merge_pages(data.get('pages', []))
        if not merge_only:
            try:
                pages = split_pages(font, pages)
            except (SplitError, P.LayoutError) as e:
                raise SystemExit('%s : %s' % (os.path.relpath(path), e))
        data['pages'] = pages
        added += len(pages) - before
        new = dump(data, raw)
        if new != raw:
            changed += 1
            if not dry:
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(new)
    total = sum(1 for p in entry_files() for pg in load(p)[1]['pages'] if MARK in pg) \
        if not dry else None
    verb = 'a modifier' if dry else 'modifies'
    print('%d fichiers %s, %+d pages' % (changed, verb, added))
    if total is not None:
        print('%d pages de suite dans le Carnet' % total)
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
