# -*- coding: utf-8 -*-
"""Raccourcit les noms que Patchouli dessine sans retour a la ligne.

Noms d'entree, titres de page, noms de multibloc et de categorie : Patchouli
les centre d'un bloc, sans les couper. Plus larges que la page (116 px), ils
debordaient sur la marge et la page d'en face (captures en jeu du 30/09).
Mesure : scripts/check/check_patchouli_titres.py.

Legendes des multiblocs : 4 lignes sous le rendu 3D (y = 115), pas de page
de suite possible. En police normale ("use_blocky_font", plus lisible),
l'ancienne legende prenait 6 lignes : elle est raccourcie.

Idempotent, applique aux deux langues.
    python3 scripts/gen/fix_patchouli_titres.py
puis split_patchouli_pages.py et les deux checks (voir README.md).
"""
import collections
import glob
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
BOOK = os.path.join(ROOT, 'mod-source', 'src', 'main', 'resources', 'assets',
                    'nexusabsolu', 'patchouli_books', 'voss_codex')

ENTREES = {  # <= 99 px : la liste des entrees les ecrit apres l'icone
 'L1 -- Petrochimie': 'L1 Petrochimie',
 'L2 -- Hydro-Eau': 'L2 Hydro-Eau',
 'L5 -- Nucleaire': 'L5 Nucleaire',
 'Theoreme I -- Conservation Absolue': 'I. Conservation',
 'Theoreme II -- Organique': 'II. Organique',
 'Theoreme III -- Stellaire': 'III. Stellaire',
 'Theoreme IV -- Sanguine': 'IV. Sanguine',
 'Theoreme V -- Brisure Dimensionnelle': 'V. Brisure',
 'L3 -- Electrolyse + Cryo': 'L3 Electrolyse',
 'L4 -- Pyrometallurgie': 'L4 Pyrometallurgie',
 'L6 -- Acides + Ammoniaque (HUB CENTRAL)': 'L6 Acides (hub)',
 'L7 -- Organique + Acetone': 'L7 Organique',
 'L8 -- Botanique + Manifoldine': 'L8 Botanique',
 'M1 + M2 -- Les Machines Finales': 'Machines M1 + M2',
 'Ordre de montage optimal': 'Ordre de montage',
 'Synthese -- La Cartouche': 'La Cartouche',
 'Epilogue -- L\'Injection': 'Epilogue',
 'Comment lire ce guide': 'Lire ce guide',
 'Preambule du Vol. IV': 'Preambule IV',
}
# Multiblocs : le nom affiche sur le controleur en jeu (localizedname de
# config/modularmachinery/machinery), abrege quand il depasse. Tous les
# multiblocs, pas seulement ceux qui debordaient, pour un style uniforme. Les dimensions
# et les notes entre parentheses sont deja dans le texte des pages.
PAGES = {  # <= 116 px
 'MB-OSMOSE (3x2x3)': 'Osmose Inverse',
 'MB-FLUORITE (3x3x3)': 'Cellule Fluorite',
 'MB-CRACKER (3x3x3)': 'Thermal Cracker',
 'MB-SOXHLET (3x5x3)': 'Extracteur Soxhlet',
 'MB-DESA Chambre Sous Vide (3x2x3)': 'Chambre Sous Vide',
 'MB-HDS Tour Hydrodesulfuration (3x3x3)': 'Tour HDS',
 'MB-TRITIUM (3x3x3, pres reactor NC)': 'Tritium Breeder',
 'MB-CK Castner-Kellner (3x2x3)': 'Castner-Kellner',
 'MB-FOUR-ELEC HT 1500C (3x2x3)': 'Four Electrique HT',
 'MB-HALL Hall-Heroult (3x3x3)': 'Cellule Hall-Heroult',
 'MB-KROLL Reacteur Kroll (3x3x3)': 'Reacteur Kroll',
 'MB-AQUA-REGIA Eau Regale (3x2x3)': 'Cellule Eau Regale',
 'MB-GAMMA-FORGE (3x3x3)': 'Forge Gamma',
 'MB-LIT-CHAMBER Tritiure de Lithium (3x2x3)': 'Chambre Lithium-Tritium',
 'MB-HABER Hub Central (3x3x3)': 'Reacteur Haber-Bosch',
 'MB-OSTWALD Tour HNO3 (3x3x3)': 'Tour Ostwald',
 'MB-CONTACT Tour H2SO4 (3x3x3)': 'Tour Contact',
 'MB-CUMENE Acetone+Phenol (3x3x3)': 'Reacteur Cumene',
 'MB-AROMATIC Tryptamide-M (3x3x3)': 'Reacteur Aromatique',
 'MB-FERMENTER Ethanol (3x2x3)': 'Fermenteur Ethanol',
 'MB-CYCLO Cyclisateur Stellaire (3x5x3, NUIT)': 'Cyclisateur Stellaire',
 'MB-EVAPORATOR Cristal Manifoldine (3x3x3)': 'Evaporateur',
 'MB-ALAMBIC Manaique (3x3x3)': 'Alambic Manaique',
 'MB-MANA-ENCHANTER (3x2x3)': 'Mana Enchanter',
 'M1 Melangeur Cryogenique (3x5x3)': 'Melangeur M1',
 'Multiblocs L3 (3 + cryo existant)': 'Multiblocs L3',
 'MB-AROMATIC (cle pour Manifoldine)': 'MB-AROMATIC',
 'M2 Bio-Reacteur (deja en place)': 'M2 Bio-Reacteur',
 '5 multiblocs L8 (le plus de machines)': '5 multiblocs L8',
 'MB-OSTWALD + MB-CONTACT': 'OSTWALD + CONTACT',
 'MB-CRACKER multifonction': 'MB-CRACKER',
 'MB-ALAMBIC + MB-MANA-ENCHANTER': 'ALAMBIC + ENCHANTER',
 'M1 Melangeur Cryogenique': 'M1 Melangeur Cryo',
 'La nuit pese 100 grammes': 'La nuit pese 100 g',
 'Le Cyclisateur Stellaire': 'Le Cyclisateur',
 'MB-CYCLO (3x5x3) ⭐⭐⭐': 'MB-CYCLO ⭐⭐⭐',
}
CATEGORIES = {  # <= 116 px
 'Carnet Voss Vol. V -- Atlas Industriel': 'Carnet Voss Vol. V'}

LEGENDES = {  # <= 4 lignes en police normale, sous le multibloc
 '$(l)Souris$() : tourner. $(l)Shift$() : une couche a la fois. Le bloc qui clignote '
 'est le $(l)Controller$() : pose-le en dernier.':
 '$(l)Souris$() : tourner. $(l)Shift$() : une couche. Le $(l)Controller$() clignote : '
 'en dernier.',
 '$(l)Drag$() to rotate, hold $(l)shift$() for one layer at a time. The blinking block '
 'is the $(l)Controller$(): place it last.':
 '$(l)Drag$(): rotate. $(l)Shift$(): one layer. The blinking block ($(l)Controller$()) '
 'goes last.',
 "$(l)9 couches$(), de bas en haut. $(l)Shift$() en isole une. L'$(l)Ecran de Controle$() "
 "clignote : pose-le en dernier.":
 "$(l)9 couches$(). $(l)Shift$() : une couche. L'$(l)Ecran de Controle$() clignote, "
 "a poser en dernier.",
 '$(l)9 layers$(), bottom to top. $(l)Shift$(): one layer. The blinking block is the '
 '$(l)Control Screen$(): place it last.':
 '$(l)9 layers$(). $(l)Shift$(): one layer. The blinking $(l)Control Screen$() goes '
 'last.',
}

SOUS_TITRE = {'Plans et notes du Dr. Elias Voss': 'Notes du Dr. Voss'}  # <= 108 px


def rename(node, key, table):
    if node.get(key) in table:
        node[key] = table[node[key]]
        return 1
    return 0


def main():
    n = 0
    path = os.path.join(BOOK, 'book.json')
    with open(path, encoding='utf-8') as f:
        raw = f.read()
    book = json.loads(raw, object_pairs_hook=collections.OrderedDict)
    if rename(book, 'subtitle', SOUS_TITRE):
        n += 1
        with open(path, 'w', encoding='utf-8') as f:
            f.write(json.dumps(book, ensure_ascii=False, indent=2) + '\n')
    for path in sorted(glob.glob(os.path.join(BOOK, '*', '*', '**', '*.json'), recursive=True)):
        kind = os.path.relpath(path, BOOK).split(os.sep)[1]
        if kind not in ('entries', 'categories'):
            continue
        with open(path, encoding='utf-8') as f:
            raw = f.read()
        data = json.loads(raw)
        k = rename(data, 'name', ENTREES if kind == 'entries' else CATEGORIES)
        for page in data.get('pages', []):
            k += rename(page, 'title', PAGES) + rename(page, 'name', PAGES)
            if 'multiblock' in page.get('type', ''):
                k += rename(page, 'text', LEGENDES)
        if k:
            n += k
            with open(path, 'w', encoding='utf-8') as f:
                f.write(json.dumps(data, ensure_ascii=False, indent=2)
                        + ('\n' if raw.endswith('\n') else ''))
    print('%d nom(s) ou legende(s) raccourci(s)' % n)
    return 0


if __name__ == '__main__':
    sys.exit(main())
