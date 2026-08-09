# Moteur de machines Nexus — design proposé

État : **proposition, non implémentée**. À valider avant écriture de code.
Base : `main`, mod 1.0.357. Remplace Modular Machinery en totalité.

---

## 1. Ce que l'inventaire a révélé

J'ai passé les 30 `machinery/*.json` et les 68 `recipes/*.json` au crible avec un
script. Trois faits changent la taille du chantier.

### Il n'y a que 3 formes de multibloc, pas 30

Les 648 entrées `parts` du pack se réduisent à **trois gabarits strictement
identiques**, au bloc près. Légende : `D` = casing décoratif, `A` = casing ou
port au choix, `I` = port item, `F` = port fluide, `E` = port énergie,
`.` = contrôleur.

**Gabarit COMPACT — 3×1×3, 9 blocs — 12 machines**
```
y=-1     D A D
         A A A
         D A D
```
`alloy_furnace, aqua_regia_cell, ck_cell, electric_furnace, fermenter,
iron_centrifuge, lit_chamber, mana_enchanter, osmose_inverse, transformer,
tritium_breeder, vacuum_chamber`

**Gabarit STANDARD — 3×3×3, 26 blocs — 14 machines**
```
y=+1     D I D        y=0      D A D        y=-1     D F D
         I I I                 A . A                 F E F
         D I D                 D A D                 D F D
```
`alambic_manaic, aromatic_reactor, contact_tower, cryo_distillateur,
cumene_reactor, evaporator, fluorite_cell, gamma_forge, haber_reactor,
hall_heroult_cell, hds_tower, kroll_reactor, ostwald_tower, thermal_cracker`

**Gabarit SPECTACLE — 3×5×3, 44 blocs — 4 machines**
```
y=+3  D I D    y=+2  D A D    y=+1  D A D    y=0  D A D    y=-1  D F D
      I I I          A A A          A A A         A . A          F E F
      D I D          D A D          D A D         D A D          D F D
```
`bioreacteur, cyclisateur_stellaire, melangeur_cryogenique, soxhlet_extractor`

Conséquence : le moteur n'a pas besoin d'un validateur de pattern arbitraire.
Il a besoin de **trois gabarits paramétrés** et d'une rotation à 4 sens. La
charte de tailles du §3 du brief est déjà respectée par la donnée existante.

### Il n'y a que 5 rôles de blocs, aucun ID en dur

Aucun `parts` du pack ne cite un bloc directement : tout passe par cinq
variables (`casings_decorative`, `casings_all`, `casings_item`,
`casings_fluid`, `casings_energy`) qui résolvent vers 7 blocs
`modularmachinery:`. Zéro bloc vanilla, zéro bloc tiers, zéro bloc
`nexusabsolu:` dans les structures.

Donc : la migration des patterns ne demande **aucune traduction de blocs**.
Il faut créer nos 5 blocs équivalents, et le pattern se transpose tel quel.

### Il n'y a que 3 types de requirement, jamais de NBT

354 requirements au total : `fluid` (156), `item` (130), `energy` (68).
Jamais de `nbt`, jamais de `tag`, jamais de `chance` sur un input.
`chance` n'apparaît que sur 17 outputs, tous dans `iron_centrifuge`.

Le moteur de recettes n'a donc à couvrir que : item (avec meta et oredict),
fluide, énergie in, énergie out, durée, et `chance` en sortie.

---

## 2. Ce qui manque dans le socle actuel

Les briques existent, mais trois manquent et une est sous-dimensionnée.

| Brique | État | Verdict |
|---|---|---|
| `TileCondenseurT2.checkStructure()` | fonctionnel, 4 rotations, tableau `STRUCTURE` | **modèle à généraliser** |
| `TileItemInput` | 4 slots, **limite 1 item par slot** | insuffisant : les recettes vont jusqu'à `amount: 32` |
| `TileItemOutput` | 1 slot | insuffisant : jusqu'à 9 outputs (`centrifuge_grass`) |
| `TileFluidInput` | 1 tank | OK en principe, à généraliser |
| `TileEnergyInput` | OK | à généraliser |
| **`TileFluidOutput`** | **absent** | 40+ recettes en produisent — bloquant |
| **`TileEnergyOutput`** | **absent** | `transformer` en a besoin — bloquant |
| `BlockMachineController` | 27 instances, décoratif, pas de TE, pas de FACING | à réécrire |

---

## 3. Architecture proposée

Un paquet neuf, `com.nexusabsolu.mod.machines`, qui ne touche à rien
d'existant tant qu'il n'est pas validé en jeu.

```
machines/
  MachineDefinition.java    id, nom localise, gabarit, couleur, tier
  MachineArchetype.java     enum COMPACT / STANDARD / SPECTACLE + le tableau de roles
  MachineRole.java          enum CASING / ANY / PORT_ITEM / PORT_FLUID / PORT_ENERGY
  StructureMatcher.java     validation 4 rotations + localisation des ports
  MachineRegistry.java      id -> definition, 30 entrees
  recipe/
    MachineRecipe.java      inputs/outputs items+fluides, rf/t, duree, conditions
    IngredientSpec.java     item | oredict | item@meta, amount, chance
    RecipeRegistry.java     indexe par machine, 68 entrees
    RecipeCondition.java    dimension / altitude / meteo / heure
  tile/
    TileMachineController.java   formation, NBT, sync client, boucle de craft
    TilePortBase.java            master + role, socle des 6 ports
  block/
    BlockMachineController.java  FACING + FORMED, TE, clic droit -> GUI
    BlockMachineCasing.java
    BlockPort{Item,Fluid,Energy}{In,Out}.java
  gui/  ContainerMachine + GuiMachine, une seule texture 256x256 dynamique
  jei/  MachineCategory generique, le controleur en catalyseur
```

**Le contrôleur appartient au mod.** C'est le point qui manquait avec MM :
`BlockMachineController` porte sa propre `TileEntity`, valide sa propre
structure, et n'est jamais remplacé par quoi que ce soit.

### La boucle du contrôleur

Reprise directe de `TileCondenseurT2.update()`, généralisée :

1. revalidation de structure toutes les 20 ticks
2. si formé : agrégation des ports trouvés (les positions sont connues par le
   gabarit, pas par une recherche)
3. `RecipeRegistry.find(machineId, inputs)` → recette ou `null`
4. vérification énergie, fluides, place en sortie, conditions d'environnement
5. progression tick par tick, consommation RF/t, sync client tous les 10 ticks
6. à `processTime >= recipeTime` : consomme les entrées, produit les sorties

### Les conditions d'environnement — l'ampoule peut disparaître

MM 1.11.1 n'avait que 5 types de requirement, d'où le contournement par
`ItemAmpouleNuitStellaire`. Notre moteur n'a pas cette limite :
`RecipeCondition` réévalue dimension / altitude / ciel dégagé / heure **à
chaque tick de craft**, ce qui restaure exactement la mise en scène prévue par
la quête 13 de l'Âge 3. L'ampoule redevient facultative.

---

## 4. Les pièges relevés dans la donnée, à corriger au passage

1. `power_transformer.json` déclare `registryname: "transformer"` — le nom de
   fichier ment. À figer sur `transformer`.
2. `cryotheum_recycle.json` consomme `thermalfoundation:material` **sans
   `meta`** : matche donc le meta 0, presque sûrement pas l'ingrédient voulu.
   Bug réel, préexistant.
3. Trois syntaxes d'items coexistent : `mod:item`, `mod:item@meta`, champ
   `meta` séparé, et `ore:NomOreDict`. Le convertisseur doit gérer les quatre.
4. `astralsorcery.liquidstarlight` : seul nom de fluide avec un point.
5. `alloy_smelter_furnaces.adapter.json` importe **toutes les recettes de four
   vanilla** avec ×3 entrées, ×3 sorties, durée ×40. Pas de traduction
   automatique possible — à réimplémenter comme un cas spécial du moteur.
6. `requires-blueprint` absent sur 3 machines : à figer explicitement.
7. `color` absent partout : la couleur par machine est à inventer.

---

## 5. Ordre de travail

| Phase | Contenu | Test de sortie |
|---|---|---|
| 1 | `MachineDefinition`, `MachineArchetype`, `StructureMatcher`, `TileMachineController`, les 5 blocs de structure, **1 machine témoin** | poser le contrôleur, monter la structure, elle passe « formée » |
| 2 | `MachineRecipe`, `RecipeRegistry`, convertisseur des 68 JSON | la machine témoin consomme et produit |
| 3 | `ContainerMachine` + `GuiMachine`, une texture dynamique | clic droit → écran avec jauges |
| 4 | catégorie JEI générique | R sur un item montre la recette |
| 5 | recettes de craft des 30 contrôleurs | craftables dans l'ordre des 14 vagues |
| 6 | wiki `docs/wiki/`, généré par script | une page par machine, patterns à jour |

Rien n'est supprimé de Modular Machinery avant que la phase 4 soit validée en
jeu. Les deux moteurs cohabitent le temps de la bascule.

---

## 6. Décisions prises

| Question | Décision |
|---|---|
| Source de vérité des définitions | **JSON dans le jar**, `assets/nexusabsolu/machines/`, lu par le Java et par les scripts Python. Pas dans `config/` : ça fait partie du mod, le joueur n'a pas à l'éditer. |
| Blocs de structure | **1 casing universel + 6 ports**, mêmes blocs pour les 30 machines. Le contrôleur porte l'identité visuelle. |
| Ampoule de Nuit Stellaire | **Supprimée** en phase 2. Les quatre conditions reviennent dans la recette et sont réévaluées à chaque tick — la mise en scène de la quête 13 est restaurée. |

### Conventions figées

**Coordonnées d'un pattern.** `layers` va du bas vers le haut, y strictement
croissant. Dans une couche, `rows[0]` est à z = -1. Dans une rangée, le
caractère 0 est à x = -1. Le symbole `C` marque le contrôleur, définit
l'origine, et doit apparaître exactement une fois. Les quatre rotations autour
de Y sont essayées à la validation.

**Legende partagée.** `machines/legend.json` associe chaque symbole à un rôle
et à la liste des blocs qui l'acceptent. Les blocs `modularmachinery:` y
figurent pendant la bascule, pour que les structures déjà montées et les 52
pages du Carnet Voss restent vraies. Ils sortiront de la liste quand MM
sortira du pack.

**Index.** Un jar ne permet pas de lister un dossier : `machines/index.json`
est la seule énumération. Une machine absente de l'index n'existe pas.

### Note pour la phase 3 — GUI

Le GUI générique ne doit pas inventer un style. Il reprend celui des 8 écrans
existants : `GuiCondenseurT2` en premier, qui est déjà un multibloc, puis
`GuiAtelier` et `GuiMachineKRDA`. Même cadre, mêmes tons, mêmes conventions de
slots, même traitement des jauges et de la barre de progression, même position
du panneau d'inventaire. La texture 256×256 générique se dessine dans la
continuité de `gui_condenseur_t2.png`.

Lire `gui/util/` avant d'écrire le moindre widget : il y a peut-être déjà de
quoi faire les jauges de fluide et les barres d'énergie.

Deux pièges déjà payés sur ces GUI :

- `drawRect` altère l'état de couleur GL. Toujours rappeler `bindTexture()`
  puis `GlStateManager.color(1,1,1,1)` avant le `drawTexturedModalRect`
  suivant, sinon les textures sortent teintées.
- `GuiCondenseurT2.java` contient des caractères non-ASCII dans des chaînes.
  Ne pas les recopier : le javac Windows est en Cp1252.
