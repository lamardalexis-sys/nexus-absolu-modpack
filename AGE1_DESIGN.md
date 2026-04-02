# AGE1_DESIGN.md -- Age 1 : La Mecanique Brute
> "L'energie est la premiere liberte." -- Dr. E. Voss, Carnet n4

---

## CONTEXTE

Le joueur a fini l'Age 0 dans une CM 3x3 -> 5x5. Il entre maintenant dans une CM 5x5
avec acces a plus d'espace et a l'energie RF.

**Objectif temporel** : Age 1 doit prendre **20% de plus** que l'Age 0.
- Age 0 : ~44 quetes
- Age 1 actuel : 35 quetes -> **cible : ~53 quetes** (+18 nouvelles)

---

## 1. ETAT ACTUEL (35 quetes)

### Branches existantes
| Branche | Quetes | Mods |
|---------|--------|------|
| Thermal Expansion | Q100-Q110 (11) | Steam/Magmatic Dynamo, Pulverizer, Smelter, Transposer |
| EnderIO | Q120-Q125 (6) | Electrical/Dark Steel, Conduits, Alloy Smelter |
| Immersive Engineering | Q130-Q136 (7) | Coke Oven, Blast Furnace, Acier, Crusher |
| Actually Additions | Q140-Q142 (3) | Atomic Reconstructor, Cristaux, Coal Gen |
| Jetpacks | Q150-Q151 (2) | Simply Jetpacks 2 |
| Milestones | Q160-Q181 (6) | 1M RF, Chaine x2, Expansions, Fragment, Fin |

### Problemes identifies
- Q107 (Electrum), Q109 (Transposer), Q123 (Fluid), Q125 (Dark Steel), Q132 (Workbench), Q135 (Crusher), Q140-Q142 (AA) : rewards = XP seul (pas d'items)
- Jetpacks Q150-Q151 : checkboxes sans items specifiques
- Pas d'Atelier (prevu pour Age 1)
- Pas de Vossium-II
- Pas de quetes stockage
- Pas de progression Tinkers

---

## 2. NOUVELLES QUETES A AJOUTER (~18)

### 2a. Branche Atelier du Dr. Voss (3 quetes)
L'Atelier est debloque en Age 1 (retire de l'Age 0).

| ID | Nom | Task | Prereq | Reward |
|----|-----|------|--------|--------|
| Q111 | L'Atelier du Dr. Voss | Craft atelier_voss x1 | Q100 | 4 invar ingots |
| Q112 | Les Plans de Voss | Craft 1 item exclusif Atelier (a definir) | Q111 | item contextuel |
| Q113 | La Methode Voss | Craft 3 items differents a l'Atelier | Q112 | fioles XP |

**Items Atelier Age 1 a creer :**
- Circuit Stabilise (Invarium + Redstone a l'Atelier)
- Bobine d'Induction (Copper Coil + Invarium a l'Atelier)
- Condensateur Voss (Invarium + Capacitor a l'Atelier)

### 2b. Branche Vossium-II (3 quetes)
Progression du Vossium avec le Compose B.

| ID | Nom | Task | Prereq | Reward |
|----|-----|------|--------|--------|
| Q114 | Le Compose B | Craft compose_b x4 | Q106, Q134 | 2 compose_b |
| Q115 | Le Vossium Evolue | Craft vossium_ii x1 (Vossium + Compose B, Alloy Kiln) | Q114 | 2 vossium_ii |
| Q116 | L'Engrenage Compose B | Craft compose_gear_b x1 (Vossium-II au centre) | Q115 | fioles XP |

**Items a creer (ContentTweaker) :**
- `vossium_ii_ingot` : Lingot de Vossium-II (texture violette + plus de veines cyan)
- `compose_gear_b` : Engrenage de Compose B

**Recettes Alloy Kiln :**
- Vossium + Compose B -> Vossium-II (400 ticks)

### 2c. Branche Stockage (3 quetes)
Progression du stockage en Age 1.

| ID | Nom | Task | Prereq | Reward |
|----|-----|------|--------|--------|
| Q143 | Le Coffre de Fer | Craft iron_chest x1 | Q100 | 4 iron ingots |
| Q144 | Les Tiroirs | Craft storage_drawers:basicdrawers x4 | Q143 | 2 drawers |
| Q145 | Le Controleur | Craft storage_drawers:controller x1 | Q144 | 4 drawers |

### 2d. Branche Tinkers Construct (3 quetes)
Outils Tinkers avec les materiaux de l'Age 1.

| ID | Nom | Task | Prereq | Reward |
|----|-----|------|--------|--------|
| Q146 | La Fonderie | Craft tconstruct:smeltery_controller x1 | Q134 (acier) | 8 seared bricks |
| Q147 | L'Outil Parfait | Craft un pickaxe Tinkers avec tete acier | Q146 | materiaux Tinkers |
| Q148 | Le Marteau du Mineur | Craft un hammer Tinkers | Q147 | materiaux |

### 2e. Quetes intermediaires (3 quetes)
Combler les trous dans la progression.

| ID | Nom | Task | Prereq | Reward |
|----|-----|------|--------|--------|
| Q137 | Le Thermoelectrique | Craft thermoelectric_generator IE x1 | Q136 | 4 wire coils |
| Q126 | Le Spawner d'Ames | Craft Soul Shards spawner | Q125 (dark steel) | composants |
| Q152 | L'Armure d'Acier | Craft full set Dark Steel Armor | Q125 | fioles XP |

### 2f. Quetes de progression manquantes (3 quetes)
Milestones supplementaires pour allonger l'age.

| ID | Nom | Task | Prereq | Reward |
|----|-----|------|--------|--------|
| Q162 | 5 000 000 RF | Checkbox (atteindre 5M RF stocke) | Q160 | capacitor ameliore |
| Q163 | Automatisation Totale | Checkbox (full auto ore processing) | Q161 | 8 iron chests |
| Q172 | Le Condenseur Tier 2 | Craft condenseur 3x3x3 pour CM 7x7 | Q115 (Vossium-II), Q171 | composants |

---

## 3. ITEMS A CREER

### ContentTweaker (scripts/contenttweaker/NexusItems.zs)
```
buildItem("vossium_ii_ingot");     // Vossium + Compose B
buildItem("compose_gear_b");       // Engrenage Age 1
buildItem("circuit_stabilise");    // Atelier: Invarium + Redstone
buildItem("bobine_induction");     // Atelier: Copper + Invarium
buildItem("condensateur_voss");    // Atelier: Invarium + Capacitor
```

### Textures a creer (resources/contenttweaker/textures/items/)
- `vossium_ii_ingot.png` : comme Vossium mais veines plus denses/lumineuses
- `compose_gear_b.png` : gear violette avec accents
- `circuit_stabilise.png`, `bobine_induction.png`, `condensateur_voss.png`

### Lang (resources/contenttweaker/lang/en_us.lang)
```
item.contenttweaker.vossium_ii_ingot.name=Lingot de Vossium-II
item.contenttweaker.compose_gear_b.name=Engrenage de Compose B
item.contenttweaker.circuit_stabilise.name=Circuit Stabilise
item.contenttweaker.bobine_induction.name=Bobine d'Induction
item.contenttweaker.condensateur_voss.name=Condensateur Voss
```

---

## 4. RECETTES A CREER/MODIFIER

### Alloy Kiln (IE AlloySmelter)
```zs
// Vossium-II
mods.immersiveengineering.AlloySmelter.addRecipe(
    <contenttweaker:vossium_ii_ingot>,
    <contenttweaker:vossium_ingot>,
    <nexusabsolu:compose_b>, 400);
```

### Atelier du Dr. Voss (AtelierRecipes.java)
```java
// Ajouter dans le static block:
addRecipe(ModItems.INVARIUM_INGOT, 1, Items.REDSTONE, 4,
          ModItems.CIRCUIT_STABILISE, 1);
addRecipe(ModItems.INVARIUM_INGOT, 1, COPPER_COIL, 1,
          ModItems.BOBINE_INDUCTION, 1);
```
**Note** : Les items Atelier sont en Java (mod custom) OU en ContentTweaker.
Si ContentTweaker, il faut un autre systeme pour les recettes Atelier.
**Decision a prendre** : items CT + recettes via CraftTweaker shaped,
ou items Java + recettes Atelier natives ?

### CraftTweaker
```zs
// Compose Gear B (meme pattern que Gear A mais Vossium-II)
recipes.addShaped("nexus_compose_gear_b",
    <contenttweaker:compose_gear_b>,
    [[null, <nexusabsolu:compose_b>, null],
     [<nexusabsolu:compose_b>, <contenttweaker:vossium_ii_ingot>, <nexusabsolu:compose_b>],
     [null, <nexusabsolu:compose_b>, null]]);
```

---

## 5. REWARDS A FIXER (quetes existantes Age 1)

Quetes qui n'ont que de l'XP -> ajouter items contextuels :

| Quest | Reward actuelle | Reward proposee |
|-------|----------------|-----------------|
| Q107 (Electrum) | XP:35 | 4 electrum + fioles |
| Q109 (Fluid Transposer) | XP:45 | 4 buckets + fioles |
| Q123 (Fluid Conduit) | XP:35 | 4 fluid conduit + fioles |
| Q125 (Dark Steel) | XP:45 | 4 dark steel + fioles |
| Q132 (Workbench) | XP:40 | 4 treated wood + fioles |
| Q135 (IE Crusher) | XP:55 | 8 gravel + fioles |
| Q140 (Atomic Reconstructor) | XP:50 | 4 cristaux + fioles |
| Q141 (Cristaux AA) | XP:40 | 4 cristaux varietes + fioles |
| Q142 (Coal Gen AA) | XP:30 | 8 coal + fioles |
| Q150 (Jetpack) | XP:60 | jetpack components + fioles |
| Q151 (Jetpack ameliore) | XP:80 | fioles |
| Q181 (FIN) | XP:500 | Vossium-II + fioles + items rares |

---

## 6. LORE AGE 1

### Ton narratif
L'Age 1 passe du survival brut (Age 0) a la **maitrise de l'energie**.
Voss a ecrit : "L'energie est la premiere liberte. Sans elle, la matiere
reste inerte. Avec elle, tout est possible."

### Moments de lore cles
- **Q100 (Bienvenue)** : "Tu as quitte la boite. L'espace s'ouvre. Mais l'obscurite est la meme."
- **Q111 (Atelier)** : "Les plans de Voss etaient griffonnes sur les murs. Tu les as rassembles."
- **Q115 (Vossium-II)** : "Le Compose B ouvre la deuxieme couche. Les canaux secondaires s'activent. L'Invarium tremble."
- **Q160 (1M RF)** : "Un million d'unites. Voss avait atteint ce seuil en 3 ans. Toi, en quelques jours."
- **Q172 (Condenseur T2)** : "La dimension se plie a nouveau. Plus large cette fois. Plus stable."
- **Q181 (FIN)** : "Le Fragment Mecanique pulse dans ta main. C'est le premier des neuf."

---

## 7. LAYOUT VISUEL (a redesigner)

Le layout actuel est horizontal (gauche->droite). A refaire en **vertical** (haut->bas)
comme l'Age 0, avec branches laterales :

```
[Q100] Bienvenue
   |
   +--[Q130] Coke Oven (IE branch, left)
   |     |
   |   [Q131-Q137] IE chain
   |
[Q101] Steam Dynamo (center)
   |
[Q102] Fluxducts
   |
   +--[Q140-Q142] Actually Additions (left branch)
   +--[Q143-Q145] Storage (right branch)
   |
[Q103] Redstone Furnace ---[Q104] Pulverizer
                               |
                           [Q105] Induction Smelter
                              / \
                          [Q106]  [Q120] EnderIO branch
                            |         |
                          [Q108]   [Q121-Q125]
                            |
                          [Q110] Upgrade -> [Q150-Q151] Jetpacks
                            |
   [Q111] Atelier ----------+
      |                     |
   [Q112-Q113]           [Q160] 1M RF
                            |
   [Q114] Compose B -----[Q162] 5M RF
      |                     |
   [Q115] Vossium-II     [Q170] Expansion 11x11
      |                     |
   [Q116] Gear B         [Q171] Expansion 13x13
                            |
                          [Q172] Condenseur T2
                            |
                          [Q180] Fragment Mecanique
                            |
                          [Q181] FIN AGE 1
```

---

## 8. RESUME DES ACTIONS

### Priorite 1 — Immediat
- [ ] Fixer les rewards XP-only des 12 quetes Age 1
- [ ] Ajouter les 18 nouvelles quetes dans DefaultQuests.json
- [ ] Creer les items ContentTweaker (vossium_ii, compose_gear_b, etc.)
- [ ] Recettes CraftTweaker (Alloy Kiln, crafting)
- [ ] Redesign layout vertical

### Priorite 2 — Session suivante
- [ ] Textures pour les nouveaux items
- [ ] Recettes Atelier (necessite decision Java vs CT)
- [ ] Descriptions lore pour chaque nouvelle quete
- [ ] Playtest Age 1 complet
- [ ] Ajuster les timings si l'age est trop court/long

### Priorite 3 — Plus tard
- [ ] Compose B item/block (si pas encore fait)
- [ ] Condenseur Tier 2 multibloc (3x3x3)
- [ ] GameStages pour gater les items Age 1

---

*Document cree Session 8 — Base pour le design de l'Age 1*
