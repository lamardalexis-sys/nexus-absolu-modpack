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
