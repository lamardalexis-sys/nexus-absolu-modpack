# Age 4 — Phase 5 détaillée (Q56-Q70 : Le Vivant et l'Étoile)

> Cinquième batch — la phase la plus dense narrativement et techniquement.
> 15 quêtes (questIDs 256-270). Botania + Astral Sorcery + Manifoldine.
> Pré-requis Phase 4 complete (Q55).
> Voir : `docs/age4-cartouche-manifold/lines/L8-botanique-manifoldine.md`

*"La chimie t'a amené jusqu'ici. La magie va te porter au-delà."*

---

## 📐 Layout

Phase 5 commence à `y=1270`. **15 quêtes** = 3 rangées de 5 :

```
   Q56 ── Q57 ── Q58 ── Q59 ── Q60       (Botania setup + 8 champis doux)
                                  │
   Q65 ── Q64 ── Q63 ── Q62 ── Q61       (8 champis sombres + extracts)
   │
   Q66 ── Q67 ── Q68 ── Q69 ── Q70       (Manifoldine + Compose epsilon) ──► Phase 6
```

---

## Q56 : "Botania, le retour de la magie"

- **ID** 256 | **Pos** (0, 1270) | **Pré** : Q55
- **Type** : crafting `1x botania:manaresource` (Manasteel ingot, damage=0)
- **Icon** : Manasteel ingot
- **Récompense** : 8 Manasteel + plans Mana Pool
- **Description** :

```
§7§oTu te rappelles Botania ? Tu sais, le truc magique avec les fleurs ?
§7Tu l'as croise Age 2 mais tu as pu l'eviter. Plus maintenant.

§7Theoreme II (Organique) de Voss = §lle vivant maitrise les
§7transformations qui depassent la chimie classique§r§7.

§7Tu vas avoir besoin de 1M de Mana et 16 champis differents.
§7Pour la Mana :
§7  - 64 Endoflames + Coal (passive, 200 Mana/sec sustained)
§7  - 1 Mana Pool Diluted + Spreaders Gaia
§7  - Patience (1M Mana = 5h en background)

§e§lObjectif : §71 Manasteel ingot
§e§lRecompense : §78 Manasteel + plans Mana Pool"""
```

---

## Q57 : "Setup Mana Industrial"

- **ID** 257 | **Pos** (80, 1270) | **Pré** : Q56
- **Type** : crafting `1x botania:pool` (Mana Pool, damage=0)
- **Icon** : Mana Pool
- **Récompense** : 4 Mana Pools + 2 Mana Spreaders + plans Champi
- **Description** :

```
§7§oUn Mana Pool. Capacite max : 1M Mana.

§7Setup typique pour Age 4 :
§7  - 4 Mana Pools en parallele (4M total stockable)
§7  - 64 Endoflames bouclees sur charbon AE2
§7  - 16 Mana Spreaders Gaia pointes vers les pools
§7  - 1 Tinted Daisy (couleur change chaque tick) pour generer
§7    les 16 mushrooms par chance regulierement

§7§lAlternative§r§7 : 4 Spectroliers Botania -> 8000 Mana/sec mais
§7coute 4 obsidian + 4 quartz + 1 manasteel chacune.

§e§lObjectif : §71 Mana Pool craft
§e§lRecompense : §74 Pools + 2 Spreaders + plans Champi
```

---

## Q58 : "Champis colorés — Botania Tinted Daisy"

- **ID** 258 | **Pos** (160, 1270) | **Pré** : Q57
- **Type** : retrieval `16x botania:mushroom` (avec damage 0-15 = 16 couleurs)
- **Icon** : Mushroom Botania
- **Récompense** : 1 Tinted Daisy + plans 8 doux
- **Description** :

```
§7§oBotania Mushroom. 16 couleurs (subtypes 0..15) :
§7  Rouge, Orange, Jaune, Rose, Vert, Cyan, Lime, Magenta
§7  Noir, Violet, Marron, Gris fonce, Vert fonce, Bleu fonce,
§7  Gris clair, Bleu clair

§7Tu vas farm les 16 sur Mycelium (vanilla ou Active).
§7Astuce : la §lTinted Daisy§r§7 (Botania) genere passivement
§7une couleur differente chaque tick. Setup avec hopper.

§7§l/!\\§r §7Au moins 1 de chaque pour passer cette quete.

§e§lObjectif : §716 Mushrooms (1 de chaque couleur)
§e§lRecompense : §71 Tinted Daisy + plans Pigments
```

---

## Q59 : "Pigments doux — 8 couleurs claires"

- **ID** 259 | **Pos** (240, 1270) | **Pré** : Q58
- **Type** : retrieval `8x contenttweaker:pigment_*` (existent déjà : red, orange, yellow, pink, lime, cyan, lightblue, magenta)
- **Icon** : pigment_red item (existant)
- **Récompense** : 8 pigments doux + plans Extracts
- **Description** :

```
§7§oTu broies les 8 champis "doux" (couleurs claires/chaudes) au
§7§lApothecary's Mortar§r§7 (Botania natif).

§7Recettes :
§7  Mushroom Red    -> pigment_red
§7  Mushroom Orange -> pigment_orange
§7  ...
§7  Mushroom Magenta -> pigment_magenta

§e§lObjectif : §78 pigments doux (1 de chaque)
§e§lRecompense : §78 pigments bonus + plans Extracts doux
```

---

## Q60 : "Extracts doux — traitement HCl"

- **ID** 260 | **Pos** (320, 1270) | **Pré** : Q59
- **Type** : retrieval `8x contenttweaker:extract_sweet_*` (à créer pour 8 couleurs)
- **Icon** : Le rouge en exemple
- **Récompense** : 8 extracts doux + plans Sombres
- **Description** :

```
§7§oTraitement HCl leger des 8 pigments doux dans une Alchemy Chamber
§7Modular Machinery (ou un Blood Magic Alchemy Array si tu veux
§7spice it up).

§7Recette (×8) :
§7  pigment_doux + 50mB HCl + 100 RF/t pendant 5s -> extract_sweet_<color>

§7C'est un fluide custom par couleur. 8 extracts doux qui forment
§7l'Essence Chromatique partielle (Theoreme II).

§e§lObjectif : §78 extracts doux
§e§lRecompense : §716 extracts bonus + plans Sombres
```

---

## Q61 : "Pigments sombres — 8 couleurs profondes"

- **ID** 261 | **Pos** (320, 1360) | **Pré** : Q60
- **Type** : retrieval `8x contenttweaker:pigment_*` (sombres)
- **Icon** : pigment_black
- **Récompense** : 8 pigments sombres + plans Extracts sombres
- **Description** :

```
§7§oMaintenant les 8 sombres (couleurs froides/sombres) :
§7  Mushroom Black, Purple, Brown, Gray, Green dark,
§7  Blue dark, LightGray, Blue light

§7Tu broies idem au Mortar.

§e§lObjectif : §78 pigments sombres
§e§lRecompense : §78 pigments bonus + plans Extracts sombres
```

---

## Q62 : "Extracts sombres — traitement H2SO4 chaud"

- **ID** 262 | **Pos** (240, 1360) | **Pré** : Q61
- **Type** : retrieval `8x contenttweaker:extract_dark_*`
- **Icon** : extract_dark sample
- **Récompense** : 8 extracts sombres + plans Essence Chromatique
- **Description** :

```
§7§oH2SO4 chaud + chauffe 5000 RF/t pendant 10s.
§7Plus dur que les doux. Plus puissant aussi.

§7Recette (×8) :
§7  pigment_sombre + 80mB H2SO4 + chauffe -> extract_dark_<color>

§e§lObjectif : §78 extracts sombres
§e§lRecompense : §716 extracts bonus + plans Essence Chromatique
```

---

## Q63 : "Essence Chromatique — la fusion 16 couleurs"

- **ID** 263 | **Pos** (160, 1360) | **Pré** : Q62
- **Type** : retrieval `1x bucket nexusabsolu:essence_chromatique`
- **Icon** : essence_chromatique bucket
- **Récompense** : 4 Essence + plans Astral Sorcery
- **Description** :

```
§7§oFusionne les 16 extracts (8 doux + 8 sombres) dans le
§7§lModular Machinery Chromatic Fusion Chamber§r§7.

§7Recipe :
§7  16 extracts (1 de chaque) + 1 Heavy Water + 5000 RF/t * 30s
§7  -> 1 bucket Essence Chromatique (1000mB)

§7L'Essence Chromatique = §lTheoreme II valide§r§7.
§7Voss en demande 4 buckets pour le Bio-Reacteur final.

§8§o"La couleur n'est pas une propriete passive de la matiere.
§8C'est le langage que la matiere utilise pour parler a notre
§8oeil. Et tu viens d'ecrire un dictionnaire complet." — E.V.

§e§lObjectif : §71 Essence Chromatique
§e§lRecompense : §74 Essence + plans Astral Sorcery
```

---

## Q64 : "Astral Sorcery — Liquid Starlight"

- **ID** 264 | **Pos** (80, 1360) | **Pré** : Q63
- **Type** : retrieval `4x bucket astralsorcery:liquidstarlight`
- **Icon** : Liquid Starlight bucket
- **Récompense** : 8 Liquid Starlight + plans Cyclisation
- **Description** :

```
§7§oTheoreme III (Stellaire) de Voss = §ll'energie des etoiles porte
§7une signature qui active certaines reactions chimiques§r§7.

§7Astral Sorcery te permet de capter ca :
§7  - Lens Cluster (3 Lens) + Marble Collector Crystal
§7  - Pose sous le ciel etoile, max altitude
§7  - Production : 100mB Liquid Starlight / minute / Lens

§7Tu as besoin de 4 buckets (4000mB) pour la cyclisation Manifoldine.

§e§lObjectif : §74 buckets Liquid Starlight
§e§lRecompense : §78 LS bonus + plans Cyclisation
```

---

## Q65 : "Tryptamide-M — l'amine cle"

- **ID** 265 | **Pos** (0, 1360) | **Pré** : Q64
- **Type** : retrieval `2x contenttweaker:tryptamide_m`
- **Icon** : tryptamide_m (custom à créer)
- **Récompense** : 4 tryptamide-M + plans Cristaux Manifoldine
- **Description** :

```
§7§oTryptamide-M. Amine analogue a la DMT.
§7C'est le neurochem qui interagit avec les recepteurs 5-HT2A
§7du systeme nerveux humain.

§7Synthese :
§7  Tryptophane (a faire avec Botania Living Wood) + 2 Methanol
§7  + ammoniaque (Phase 3 Q36) + Compose alpha (Phase 3 Q35)
§7  -> Tryptamide-M

§7C'est le §lcoeur neurochimique§r§7 de la cartouche.

§8§o"Sans la Tryptamide-M, le sujet 47 perçoit la simulation mais
§8ne peut pas la franchir. Avec, il devient permeable." — E.V.

§e§lObjectif : §72 Tryptamide-M
§e§lRecompense : §74 trypt + plans Cristaux Manifoldine
```

---

## Q66 : "Cristaux de Manifoldine — premiere cyclisation"

- **ID** 266 | **Pos** (0, 1450) | **Pré** : Q65
- **Type** : retrieval `1x contenttweaker:cristal_manifoldine`
- **Icon** : cristal_manifoldine (custom)
- **Récompense** : 2 cristaux + plans Compose delta
- **Description** :

```
§7§oCyclisation Stellaire. La piece centrale de la Phase 5.

§7Sous Argon (Phase 1 Q6) + sous flux Liquid Starlight (Q64) :
§7  Phenol substitue (Phase 3 Q43) + Tryptamide-M (Q65)
§7  + Carbone Au charge (Phase 3 Q44) + 6Li
§7  -> Cristal de Manifoldine (cristal violet/cyan, 800 C)

§7C'est le §lpremier compose stable§r§7 de la chaine epsilon.

§e§lObjectif : §71 Cristal de Manifoldine
§e§lRecompense : §72 cristaux + plans Compose delta
```

---

## Q67 : "Composé delta — bio-actif"

- **ID** 267 | **Pos** (80, 1450) | **Pré** : Q66
- **Type** : retrieval `2x contenttweaker:compose_delta`
- **Icon** : compose_delta (custom)
- **Récompense** : 4 compose delta + plans epsilon
- **Description** :

```
§7§oCompose delta = Cristal de Manifoldine + Heavy Water + Mycelium Active.

§7Le Mycelium Active (Phase 4 Q51) catalyse la fixation du cristal
§7dans une matrice biologique soluble. Sans lui le cristal serait
§7inerte et ne s'injecterait jamais.

§7C'est de la chimie organometallique vivante.

§e§lObjectif : §72 Compose delta
§e§lRecompense : §74 delta + plans Solution epsilon
```

---

## Q68 : "Composé epsilon — la Manifoldine active"

- **ID** 268 | **Pos** (160, 1450) | **Pré** : Q67
- **Type** : retrieval `1x bucket nexusabsolu:solution_epsilon`
- **Icon** : solution_epsilon bucket (custom)
- **Récompense** : 4 solution + plans Bio-Reacteur
- **Description** :

```
§7§oSolution epsilon = §lLA Manifoldine active§r§7.

§7Synthese finale :
§7  Compose delta + Compose alpha + Compose beta + Compose gamma3
§7  + Argon atmosphere + Eau Tridistillee
§7  -> Solution Epsilon (liquide pourpre/cyan animee)

§7§5C'est ce qui s'injectera dans les veines du joueur.§r

§7§4§lDanger§r §7 : la solution est instable. La pression interne
§7de l'ampoule iridium doit etre maintenue a 2.5 atm. En dessous,
§7la solution decoagule et devient inerte. Au-dessus, elle explose.

§e§lObjectif : §71 bucket Solution Epsilon
§e§lRecompense : §74 solution + plans Bio-Reacteur final
```

---

## Q69 : "Verification — les 5 theoremes en main"

- **ID** 269 | **Pos** (240, 1450) | **Pré** : Q68
- **Type** : checkbox (verification mentale + items en stock)
- **Icon** : Voss Codex
- **Récompense** : 1 cartouche vide (sans serum) + plans Encartouchage
- **Description** :

```
§7§oArrete-toi 30 secondes.

§7Verifie ce que tu as :
§7  ✓ Theoreme I (Conservation) : tous les composes alpha-beta
§7  ✓ Theoreme II (Organique) : Essence Chromatique 16 champis
§7  ✓ Theoreme III (Stellaire) : Liquid Starlight + Cristaux Manifoldine
§7  ✓ Theoreme IV (Sanguine) : Tryptamide-M + Compose delta
§7  ✓ Theoreme V (Brisure) : composes gamma + Solution Epsilon

§7Si tu as tout, tu es pret pour la Phase 6 finale.
§7Si tu manques un, retourne en arriere. Aucun raccourci.

§8§o"Voila, Sujet 47. Tu as les Cinq Theoremes dans tes mains.
§8Maintenant tu vas les faire fusionner." — E.V.

§e§lObjectif : §7Confirme manuellement (clique le bouton)
§e§lRecompense : §71 Cartouche Vide + plans Encartouchage
```

---

## Q70 : "Phase 5 complete — la magie est dans la machine"

- **ID** 270 | **Pos** (320, 1450) | **Pré** : Q69
- **Type** : checkbox (lit Voss Codex chap.4 — Sanguine)
- **Icon** : Voss Codex
- **Récompense** : Voss Codex + 32 pain + 8 Bouteilles XP + débloquage Phase 6
- **Description** :

```
§7§oTu es a 70/80 quetes. Tu as la Solution Epsilon.
§7Tout ce qui te manque maintenant c'est la chambre stellaire
§7multibloc et le rite final.

§7§5§lLe carnet de Voss s'ouvre sur le chapitre 4 (Theoreme IV) -
§7le plus court mais le plus brutal :§r

§8§o"Le sang humain est le seul medium qui parle a tous les niveaux
§8de la simulation. C'est la qu'il faut frapper. La cartouche est
§8finie. Ne la mets pas dans tes veines avant d'etre dans la
§8chambre stellaire. Sinon tu mourras." — E.V.

§e§lObjectif : §7Lis Voss Codex chap.4 (Sanguine) -- chapitre final
§e§lRecompense : §7Phase 6 (Convergence) debloquee + 8 Bouteilles XP
```

---

## ⚠️ Items custom à créer pour Phase 5

Existants :
- ✅ `pigment_red, _orange, _yellow, _pink, _cyan, _lightblue, _magenta, _lime, _black, _purple` (10/16 dans le ZS)
- ✅ `essence_chromatique` (fluide à vérifier)
- ✅ `cristal_manifoldine`, `compose_delta`, `solution_epsilon`, `tryptamide_m` (à vérifier)

À CRÉER si manquants :
- ❌ Pigments manquants : `pigment_brown`, `pigment_gray`, `pigment_green_dark`, `pigment_blue_dark`, `pigment_lightgray`, `pigment_blue_light`
- ❌ Extracts (16 fluides) : `extract_sweet_<color>` × 8 + `extract_dark_<color>` × 8
- ❌ `tryptamide_m` (item)
- ❌ `cristal_manifoldine` (item, glowing)
- ❌ `compose_delta` (item, glowing)
- ❌ `solution_epsilon` (fluide, animé)
- ❌ `essence_chromatique` (fluide)
- ❌ `cartouche_vide` (item) — récompense Q69

Note : 16 fluides extracts c'est lourd. Possiblement on peut les **fusionner en 2 fluides** (`extract_sweet` et `extract_dark` avec couleur dynamique via NBT) ou rester strict avec 16 fluides séparés. À décider.
