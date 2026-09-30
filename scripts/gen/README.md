# Generateur de textures d'items — chaine Manifold

Regenere les 64 textures 32x32 des items ContentTweaker de l'Age 3.

    python3 scripts/gen/render.py resources/contenttweaker/textures/items

## Fichiers

- `shapes.py` — silhouettes 32x32 par famille. Chaque fonction rend
  `(grille, carte_de_faces)` ou les faces valent `T` (dessus), `F` (face),
  `R` (cote droit). Dix familles : ingot, dust, capsule, catalyst, gauze,
  crystal, block, casing, pool, spores.
- `palettes.py` — `(famille, couleur_de_base, couleur_d_accent)` par item.
  Couleurs choisies d'apres l'aspect reel du compose (soufre jaune,
  yellowcake pour l'uranyle, bleu phtalo pour le beta1...).
- `render.py` — applique l'ombrage. Multiplicateurs a bords durs par face
  (T 1.24 / F 0.94 / R 0.68), contour fin sur le bord exterieur uniquement,
  eclat en haut a gauche, puis les details specifiques a la famille.
- `names64.py` — noms FR et EN, a reinjecter dans les `.lang` si besoin.

## Ajouter un item

1. Une entree dans `palettes.py` avec sa famille et ses deux couleurs.
2. Relancer `render.py`.
3. Ajouter le nom dans `names64.py` et les deux `.lang`.

Une nouvelle famille = une fonction dans `shapes.py` qui rend une grille
32x32 et sa carte de faces, plus un bloc de details dans `render.py`.

## Principes

Repris de `LINGOT-TEXTURE.md` : les faces se distinguent par des bords
**durs**, pas par un degrade continu — c'est ce qui fait lire le volume a
32 pixels. Le contour reste fin (1px) sinon il mange la forme.

---

# Carnet Voss (Patchouli) — mise en page

Patchouli 1.12.2 ne pagine pas et ne tronque pas : un texte trop long est
dessiné sous la page, hors du parchemin. Ces scripts le mesurent hors jeu.

- `patchouli_layout.py` — portage de `BookTextParser` et `TextLayouter`.
  Source unique du découpage et du contrôle.
- `font_widths.json` et `font_widths_ascii.json` — largeurs des deux polices
  de Minecraft, extraites par `extract_font_widths.py` (`--ascii` pour la
  seconde). Le Carnet a `use_blocky_font` depuis le 30/09 : texte en police
  normale, plus lisible que la petite police unicode. `book_font()` suit
  `book.json`, découpage et contrôles s'adaptent seuls si on le change.
- `BreakPoints.java` — coupures de ligne du vrai `java.text.BreakIterator`.
  Il faut un JDK : `PATH`, `JAVA_HOME` ou `NEXUS_JAVA_HOME`. Java 8 de
  préférence, celui du jeu.
- `split_patchouli_pages.py` — coupe les pages trop longues. Les pages de
  suite n'ont pas de titre et portent `"nexus_suite": {sep, reprise}`, que
  Patchouli ignore et qui sert à recoller exactement.
- `fix_patchouli_closers.py` — Patchouli n'a **pas** de balises fermantes de
  style : `$(/l)` ferme un lien, `$(/o)` et `$(/li)` n'existent pas. Seul
  `$()` remet le style à zéro.
- `fix_patchouli_titres.py` — raccourcit noms d'entrée, titres de page, noms
  de multibloc et légendes. Patchouli les dessine d'un bloc, centrés, sans
  jamais les couper : trop larges, ils débordent sur la page d'en face.
- `../check/check_patchouli_overflow.py` — le contrôle de hauteur.
- `../check/check_patchouli_titres.py` — le contrôle de largeur : 116 px
  pour un en-tête (police normale, sauf si le joueur force l'unicode), 99 px pour un nom dans la
  liste des entrées (après l'icône).

Budgets : 14 lignes en page 0 d'une entrée (le nom de l'entrée est dessiné,
pas le `title` de la page), 16 avec titre, 17 sans, 4 sous un multibloc.

## Quand on régénère des pages

    python3 scripts/gen/split_patchouli_pages.py --merge-only
    python3 scripts/gen/gen_patchouli_layouts.py      # ou tout autre générateur
    python3 scripts/gen/split_patchouli_pages.py
    python3 scripts/check/check_patchouli_overflow.py
    python3 scripts/check/check_patchouli_titres.py

Le `--merge-only` n'est pas optionnel : les générateurs repèrent leurs pages
à leur contenu, et sur une page coupée ils n'en trouveraient qu'une moitié.

Vérifier une fois la table de police contre le vrai jar :

    python3 scripts/gen/extract_font_widths.py --verify <minecraft-1.12.2.jar>
    python3 scripts/gen/extract_font_widths.py --ascii <minecraft-1.12.2.jar>   # réécrit, comparer au diff
