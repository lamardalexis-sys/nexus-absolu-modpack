# QUESTS_PLAN_UPDATED.md — État réel des quêtes (v1.0.122)
> Mis à jour après session 10+. Remplace les sections Age 1 de QUESTS_PLAN.md

---

## AGE 1 — "La Mécanique Brute" (39 quêtes)

### Branche principale (colonne centrale, vertical)

```
Q47  "On respire . . ."         prereqs=[40]     (0,0)
Q48  ". . . encore plus"        prereqs=[47]     (0,50)
Q49  "c'est juste 100 . . ."   prereqs=[48]     (0,100)
Q50  "Ca chauffe"               prereqs=[49]     (0,150)
Q51  "Les arteres"              prereqs=[50]     (0,200)
Q52  "Toujours des murs"        prereqs=[51]     (0,250)
Q55  "La fusion"                prereqs=[53,54]  (0,350)
Q68  "L'upgrade"                prereqs=[55]     (0,400)
```

### Branche gauche (métallurgie avancée)

```
Q56  "Le coke"                  prereqs=[52]     (-120,250)
Q58  "Le bois traite"           prereqs=[56]     (-120,300)
Q131 "La fonderie d'alliages"   prereqs=[58]     (-120,350)
Q132 "Le premier acier"         prereqs=[131]    (-120,400)
Q61  "L'acier"                  prereqs=[132]    (-120,450)
Q133 "La Pioche Steelium"       prereqs=[61]     (-120,500)
Q62  "Le Compose B"             prereqs=[133]    (-120,550)
```

### Branche droite (atelier + scavenging)

```
Q57  "L'atelier ressuscite"     prereqs=[48]     (120,50)
Q65  "Les murs de la machine"   prereqs=[57]     (180,50)
Q66  "Les tunnels"              prereqs=[65]     (240,50)
Q127 "La source d'eau"          prereqs=[50]     (60,150)
Q128 "Les veines d'eau"         prereqs=[127,51] (60,200)
Q67  "L'assistant mecanique"    prereqs=[51,50]  (180,150)
Q70  "La reconstructrice"       prereqs=[51]     (180,200)
Q71  "Les cristaux"             prereqs=[70]     (180,250)
Q81  "L'armure des verts"       prereqs=[71]     (240,250)
Q93  "Les pioches specialisees" prereqs=[67,71]  (180,300)
Q130 "Les pioches rares"        prereqs=[93]     (180,350)
```

### Split et convergence (double branche)

```
Q53  "Le doubleur"              prereqs=[52]     (-60,300)
Q54  "Le four intelligent"      prereqs=[52]     (60,300)
```

### Endgame Age 1 (colonne centrale puis serpentin)

```
Q63  "Le Vossium evolue"        prereqs=[62,46]  (-60,550)
Q80  "L'alliage sombre"         prereqs=[61]     (-60,505)
Q136 "Le Condenseur Tier 2"     prereqs=[63,80]  (0,505)
Q137 "La Deuxieme Fissure"      prereqs=[136]    (0,585)
Q138 "j'ai merde . . ."         prereqs=[137]    (0,665)
```

### Serpentin final (U inversé vers la droite)

```
Q139 "La clé c'est la réussite" prereqs=[138]    (60,665)   ← Clé 9x9
Q140 "La chimie du vivant"      prereqs=[139]    (140,665)  ← 16x Bio Fuel (Crusher)
Q141 "Le plastique du Dr. Voss" prereqs=[140]    (220,665)  ← 4x HDPE Sheet
         ↓
Q142 "Le catalyseur ultime"     prereqs=[141]    (220,745)  ← Catalyseur Critique
Q143 "L'injection liquide"      prereqs=[142]    (140,745)  ← Fluid Input Hatch
Q144 "La Neuvième Dimension"    prereqs=[143]    (60,745)   ← CM x9 = FIN AGE 1
```

Layout serpentin :
```
Q138 → Q139 → Q140 → Q141
                        ↓
       Q144 ← Q143 ← Q142
```

---

## PROBLÈMES CONNUS QUÊTES

- Q134/Q135 : orphelines, référencées mais jamais définies — à résoudre
- Q96 "Fin de l'Age 1" : pas de prerequisite gate — devrait pointer vers Q144
- Age 2 (Q97-Q126) : certaines référencent `contenttweaker:` au lieu de `nexusabsolu:` — audit nécessaire

---

## RECETTES MODIFIÉES CETTE SESSION

### Clé d'Extension 9x9 (Age1_Signalhee.zs)
Advanced Crafting Table 5x5 (tier 0) :
```
[    ][    ][    ][    ][    ]
[    ][Vos3][Diar][Lava][GrC ]
[Vos3][    ][Vos3][    ][K7x7]
[    ][Vos3][    ][    ][CMx7]
[    ][    ][    ][    ][    ]
```

### Catalyseur Critique (Age0_Keys.zs)
```
[Vossium III][HDPE Sheet][Vossium III]
[HDPE Sheet][Catalyseur Volatile][HDPE Sheet]
[Vossium III][HDPE Sheet][Vossium III]
```

### Machines Mekanism (Age1_Mekanism.zs)
- PRC : Dark Steel + Machine Frame + Enrichment Chamber + Adv Circuits + Gas Tanks + Dynamic Tank
- Dynamic Tank : Dark Steel en croix + Bucket
- Enrichment Chamber : Invarium + Basic Circuits + Enori Crystal + Machine Frame
- Crusher : Redstone + Adv Circuits + Lava Buckets + Machine Frame

### Black Iron Ingot (Age2_Magic.zs)
Coal x8 autour de Iron Ingot (au lieu de black dye)

---

## NOUVEAUX BLOCS CETTE SESSION

- `nexusabsolu:fluid_input` — Fluid Input Hatch (tank 16000mB, accepte diarrhée par pipes/buckets)
- `nexusabsolu:nexus_wall_t2` — Nexus Wall Tier 2 (portail structure)
- `nexusabsolu:ecran_controle` — Écran de Contrôle (master du Portail Voss, TileEntity)

---

## PORTAIL VOSS — Structure 9 couches

### Couche 0 (y=-5) : Base 7x7
```
W2  W2  W2  EIn W2  W2  W2
W2  W2  V3  V3  V3  W2  W2
W2  V3  W2  V3  W2  V3  W2
W2  V3  V3  V4  V3  V3  LIn
W2  V3  W2  V3  W2  V3  W2
W2  W2  V3  V3  V3  W2  W2
W2  W2  W2  W2  W2  W2  W2
```

### Couche 1 (y=-4) : Murs + Bassin lave + Composé D
```
.   CD  W   W   W   CD  .
CD  W   L   L   L   W   .
.   W   L   V4  L   W   .
.   W   L   L   L   W   .
.   CD  W   W   W   CD  .
```

### Couche 2 (y=-3) : Colonne 3x3
```
W  W  W
W  W2 W
W  W  W
```

### Couches 3-4 (y=-2, y=-1) : Piliers jumeaux + ZONE PORTAIL
```
W2 [portail] W2
```

### Couche 5 (y=0) : Piliers + Écran de Contrôle
```
W2  EC  W2
```

### Couche 6 (y=+1) : Traverse horizontale
```
W2  W  W  W  W2
```

### Couche 7 (y=+2) : Cornes
```
W  W  .  .  .  W  W
```

### Couche 8 (y=+3) : Pointes
```
W  .  .  .  .  .  W
```

Légende : W=nexus_wall, W2=nexus_wall_t2, V3=vossium_iii_block, V4=vossium_iv_block, EIn=energy_input, LIn=fluid_input, CD=compose_block_d, L=lava, EC=ecran_controle

Activation : 1,000,000 RF + 10,000 mB diarrhée liquide → portail entre piliers (couches 3-4) + téléportation overworld + placement CM x9
