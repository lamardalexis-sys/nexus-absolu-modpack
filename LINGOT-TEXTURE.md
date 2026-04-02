# LINGOT-TEXTURE.md — Comment creer des textures de lingots Minecraft

## FORME
- La forme vient du dessin d'Alexis (reference: image avec pixels rouge/bleu/cyan/vert)
- Les pixels colores dans le reference = OUTLINE (ne PAS les supprimer, ils donnent les aretes 3D)
- Les pixels INTERNES colores = lignes de separation entre faces (top/front/right)
- Ces lignes internes sont ESSENTIELLES pour le look 3D du lingot

## STRUCTURE DU LINGOT (32x32, offset Y=7)
- **Outer outline** : contour rouge/bleu = bord sombre du lingot
- **Internal edges** : cyan/vert = aretes entre les faces (TOP/FRONT/RIGHT)
- **Top face** (rows 0-5) : la plus claire, gradient bright a droite -> dark a gauche
- **Front face** (rows 6-18) : plus sombre, gradient haut -> bas
- **Right side** (rows 6-10, x >= 22) : face laterale visible, sombre

## OUTLINE DATA (32 wide x 19 tall)
```
Row  0: ....................####........
Row  1: .............#######....##.....
Row  2: .......#######............#....
Row  3: .#######...................#...
Row  4: #...........................#..
Row  5: #.#..........................#.
Row  6: #..#...................########
Row  7: #..##...........################
Row  8: #...##...##############........#
Row  9: #....##.#########..............#
Row 10: ##....####.....................#
Row 11: .#.....##......................#
Row 12: .##....##......................#
Row 13: ..##...##......................#
Row 14: ...##..##.................######
Row 15: ....##.##...........###########
Row 16: .....####....#############.....
Row 17: ......##############...........
Row 18: .......#######.................
```

## METHODE
1. Placer TOUS les '#' comme outline (couleur outline)
2. Fill entre le premier et dernier '#' de chaque row
3. Classifier: row <= 5 = Top, row 6+ x >= 22 = Right, reste = Front
4. Appliquer gradient par face
5. Ajouter details: speckles (Invarium) ou veins (Vossium)

## VEINS VOSSIUM
- Dessiner des LIGNES CONTINUES en zigzag (comme des eclairs)
- PAS des points disperses
- Chaque eclair = liste de (x,y) connectes
- Ajouter un halo glow sur les pixels voisins (blend 50% avec la couleur vein glow)

## PALETTES
### Invarium (acier bleu)
- outline: (28, 38, 55), shine: (215, 228, 245)
- top: hi(178,198,222) mid(148,170,198) lo(122,145,175)
- front: hi(98,120,150) mid(72,92,120) lo(52,68,92)
- right: hi(60,78,105) lo(42,55,78)
- speck: (48, 128, 215), chance 5.5%

### Vossium (violet + cyan)
- outline: (25, 8, 40), shine: (218, 155, 250)
- top: hi(178,108,218) mid(150,78,192) lo(125,58,165)
- front: hi(100,42,138) mid(75,28,110) lo(55,18,82)
- right: hi(62,25,90) lo(42,14,65)
- vein: (65, 245, 252), vglow: (90, 190, 210)

## NE PAS FAIRE
- Ne PAS supprimer les lignes internes (elles font le 3D)
- Ne PAS mettre des points disperses pour les veins (faire des LIGNES)
- Ne PAS remplir tout l'espace 32x32 (le lingot est centre)
- Ne PAS "nettoyer" les artefacts internes — ce sont les aretes entre faces
