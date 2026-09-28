# -*- coding: utf-8 -*-
"""Retire les fausses balises fermantes du Carnet Voss.

Patchouli 1.12.2 n'a PAS de balises fermantes pour le style
(BookTextParser.java, 1.12.2-final) :

  $(/l)   ferme un LIEN : remet la couleur d'avant le lien, mais laisse le
          gras de $(l) actif. Sans lien ouvert, il remet la couleur de base.
  $(/o)   n'existe pas : commande inconnue, ignoree. L'italique continue.
  $(/li)  n'existe pas non plus. Ignore.

Seul $() (alias $(reset), $(clear)) remet style ET couleur a zero. Le Carnet
ecrivait $(l)...$(/l) et $(o)...$(/o) comme du HTML : le gras et l'italique
debordaient sur tout le texte qui suit, jusqu'au prochain $().

Ce script remplace chaque fermant par $() suivi de ce qui doit rester actif
(couleur, puis gras ou italique englobant). Il supprime le fermant quand il
est en fin de texte ou deja suivi d'un $(). Il supprime $(/li).

Idempotent : sur un texte deja corrige il ne trouve plus rien a faire.

    python3 scripts/gen/fix_patchouli_closers.py          # corrige
    python3 scripts/gen/fix_patchouli_closers.py --check  # compte, n'ecrit rien
"""
import glob
import json
import os
import re
import sys

BOOK = 'mod-source/src/main/resources/assets/nexusabsolu/patchouli_books/voss_codex'
CMD = re.compile(r'(\$\([^)]*\))')
COLOR = re.compile(r'^(#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6}|[0-9a-f])$')


def fix_text(text):
    """Rend (texte corrige, nombre de fermants traites)."""
    toks = [t for t in CMD.split(text) if t != '']
    out = []
    color = None      # derniere commande de couleur active, ex. '#248'
    styles = []       # styles voulus, du plus ancien au plus recent : 'l', 'o'
    n = 0
    for i, tok in enumerate(toks):
        m = re.fullmatch(r'\$\(([^)]*)\)', tok)
        if not m:
            out.append(tok)
            continue
        c = m.group(1)
        if c in ('l', 'o'):
            if c in styles:
                styles.remove(c)
            styles.append(c)
            out.append(tok)
        elif c in ('/l', '/o'):
            n += 1
            want = c[1:]
            if want in styles:
                styles.remove(want)
            nxt = toks[i + 1] if i + 1 < len(toks) else ''
            if nxt == '' or nxt in ('$()', '$(reset)', '$(clear)'):
                continue                    # fin de texte, ou deja remis a zero
            out.append('$()')
            if color:
                out.append('$(%s)' % color)
            if styles:                      # Patchouli n'a qu'un style a la fois
                out.append('$(%s)' % styles[-1])
        elif c == '/li':
            n += 1
        elif c in ('', 'reset', 'clear'):
            color = None
            styles = []
            out.append(tok)
        elif COLOR.match(c):
            color = c
            out.append(tok)
        else:
            out.append(tok)
    return ''.join(out), n


def texts_of(data, path):
    """Champs de texte Patchouli d'un fichier du livre : (objet, cle)."""
    name = os.path.basename(path)
    if name == 'book.json':
        return [(data, 'landing_text')] if 'landing_text' in data else []
    if os.sep + 'categories' + os.sep in path or '/categories/' in path:
        return [(data, 'description')] if 'description' in data else []
    return [(p, 'text') for p in data.get('pages', []) if 'text' in p]


def book_files():
    return ([BOOK + '/book.json'] + sorted(glob.glob(BOOK + '/*/categories/*.json'))
            + sorted(glob.glob(BOOK + '/*/entries/*.json')))


def main(check):
    total = 0
    files = 0
    for path in book_files():
        with open(path, encoding='utf-8') as f:
            raw = f.read()
        data = json.loads(raw)
        changed = False
        for obj, key in texts_of(data, path):
            fixed, n = fix_text(obj[key])
            if n:
                total += n
                obj[key] = fixed
                changed = True
        if changed:
            files += 1
            if not check:
                with open(path, 'w', encoding='utf-8') as f:
                    json.dump(data, f, ensure_ascii=False, indent=2)
                    if raw.endswith('\n'):
                        f.write('\n')
    verb = 'a corriger' if check else 'corriges'
    print('%d fermants %s dans %d fichiers' % (total, verb, files))
    return 1 if (check and total) else 0


if __name__ == '__main__':
    sys.exit(main('--check' in sys.argv))
