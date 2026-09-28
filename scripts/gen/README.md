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
- `font_widths.json` — largeurs de la police **unicode** (le Carnet n'a pas
  `use_blocky_font`), extraites par `extract_font_widths.py`.
- `BreakPoints.java` — coupures de ligne du vrai `java.text.BreakIterator`.
  Il faut un JDK : `PATH`, `JAVA_HOME` ou `NEXUS_JAVA_HOME`. Java 8 de
  préférence, celui du jeu.
- `split_patchouli_pages.py` — coupe les pages trop longues. Les pages de
  suite n'ont pas de titre et portent `"nexus_suite": {sep, reprise}`, que
  Patchouli ignore et qui sert à recoller exactement.
- `fix_patchouli_closers.py` — Patchouli n'a **pas** de balises fermantes de
  style : `$(/l)` ferme un lien, `$(/o)` et `$(/li)` n'existent pas. Seul
  `$()` remet le style à zéro.
- `../check/check_patchouli_overflow.py` — le contrôle.

Budgets : 14 lignes en page 0 d'une entrée (le nom de l'entrée est dessiné,
pas le `title` de la page), 16 avec titre, 17 sans, 4 sous un multibloc.

## Quand on régénère des pages

    python3 scripts/gen/split_patchouli_pages.py --merge-only
    python3 scripts/gen/gen_patchouli_layouts.py      # ou tout autre générateur
    python3 scripts/gen/split_patchouli_pages.py
    python3 scripts/check/check_patchouli_overflow.py

Le `--merge-only` n'est pas optionnel : les générateurs repèrent leurs pages
à leur contenu, et sur une page coupée ils n'en trouveraient qu'une moitié.

Vérifier une fois la table de police contre le vrai jar :

    python3 scripts/gen/extract_font_widths.py --verify <minecraft-1.12.2.jar>
