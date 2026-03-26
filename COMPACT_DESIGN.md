# COMPACT_DESIGN.md — Design du gameplay Compact Machine (Âge 0-2)
> Style Compact Claustrophobia, adapté Nexus Absolu. 30-50h dans les boîtes.

---

## TIMING GLOBAL

```
Pack complet : 300-500h (objectif ~400h)

COMPACT MACHINES (Âge 0-2) : ~40h (10%)
  3x3x3 → 5x5x5 :  ~8h   (lent, manuel, survie pure)
  5x5x5 → 7x7x7 :  ~7h   (premiers outils, bonsai, farming)
  7x7x7 → 9x9x9 :  ~6h   (Tinkers Smeltery, premiers alliages, accélération)
  9x9x9 → 11x11  :  ~6h   (Thermal, EnderIO, RF — ça accélère)
  11x11 → 13x13  :  ~5h   (IE, alliages avancés, automation)
  13x13 + magie   :  ~8h   (Botania, Astral, Blood Magic, 3 Fragments)

OVERWORLD (Âge 3+) : ~360h (90%)
  Âge 3 : ~80h    (Mekanism, PneumaticCraft, AE2, Nether)
  Âge 4 : ~60h    (AE2 complet, OpenComputers, End)
  Âge 5 : ~50h    (NuclearCraft, Extreme Reactors, Lune)
  Âge 6 : ~45h    (Espace complet, RFTools Dims)
  Âge 7 : ~40h    (Draconic, Blood Magic max, TF)
  Âge 8 : ~45h    (Avaritia, Extended Crafting endgame)
  Âge 9 : ~40h    (Nexus Absolu, quêtes WTF)
```

---

## PHILOSOPHIE 3x3→7x7 (les 15 premières heures)

### Ce que fait Compact Claustrophobia :
- Scavenge murs au poing → dirt dust, gravel dust
- Pioches spécifiques → grits spécifiques (1 type par pioche)
- Decay Generator (NuclearCraft) → premier RF depuis radioactivité
- Modularium = ingrédient central pour construire les Compact Machines
- Récursivité : 5x5 nécessite 4x 3x3 dans la recette
- Lent, méthodique, satisfaisant

### Ce que NOUS faisons (différences) :
- Scavenge MAIS aussi Ex Nihilo tamis (deux systèmes en parallèle)
- Pioches Tinkers (pas vanilla) → grits + drops Ex Nihilo
- PAS de Decay Generator (on garde ça pour NuclearCraft Âge 5)
- Nos clés custom au lieu de Modularium
- PAS de récursivité (les clés se craftent, pas besoin de 4x petites machines)
- Crop Dusting (poop) = composant central
- Bonsai Trees = source de bois (pas de vrais arbres dans la boîte)
- Lore du Dr. Voss intégré dès le début

### Les 3 piliers du early game compact :
1. **SCAVENGING** — taper les murs = seule source de minerais
2. **FARMING** — Bonsai + Pam's + Crop Dusting = nourriture + composants
3. **CRAFTING MANUEL** — pas de machines RF avant la 7x7

---

## DÉTAIL PAR SALLE

### ═══ SALLE 3x3x3 — "Le Réveil" (~3h) ═══

**Espace :** 27 blocs. MINUSCULE. Le joueur peut à peine bouger.

**Coffre de départ contient :**
```
1x Livre du Dr. Voss (Patchouli — guide book)
1x Quest Book (BetterQuesting)
4x Pain (nourriture de base)
1x Watering Can (arrosoir, si mod dispo, sinon crouch pour accélérer)
```

**Ce que le joueur fait :**

Étape 1 — Scavenge au poing (~30 min)
```
Main nue + mur Compact Machine → 
  70% : Dirt Dust (contenttweaker:wall_dust)
  25% : Gravel Dust (exnihilocreatio:block_dust)
  5%  : Flint

4x Dirt Dust → 1x Dirt Block
4x Gravel Dust → 1x Gravel Block

Le joueur est blessé à chaque coup (0.5 cœur).
Il doit manger du pain entre les sessions de scavenging.
C'est LENT et INTENTIONNEL — tu sens que t'es enfermé.
```

Étape 2 — Dirt et premières graines (~30 min)
```
Place la Dirt → clic droit main vide = scavenge la dirt
  Drops : graines de blé, carotte, pomme de terre, betterave
  Rare : Bonsai Sapling (oak)

Place plus de Dirt → crée un mini farm (2-3 blocs de terre)
Plante les graines → nourriture basique
Accroupis longtemps → Crop Dusting → Poop
```

Étape 3 — Bois et table de craft (~30 min)
```
Bonsai Sapling + Dirt → Bonsai Tree (1 bloc)
Bonsai Tree → Bonsai Cuttings → Wood Planks
Wood Planks → Crafting Table, Sticks

CRAFT : Wooden Pickaxe (vanilla)
```

Étape 4 — Premiers grits au pickaxe (~1h)
```
Wooden Pickaxe + mur →
  50% : Dirt Dust
  30% : Gravel Dust
  15% : Cobblestone Fragment (4→1 cobble)
  5%  : Clay Ball

Stone Pickaxe (cobble + sticks) + mur →
  40% : Cobblestone Fragment
  30% : Iron Grit (contenttweaker:iron_grit)
  20% : Copper Grit (contenttweaker:copper_grit)
  10% : Tin Grit (contenttweaker:tin_grit)

Le joueur accumule des grits. Pas encore de quoi les fondre.
Patience. C'est le jeu.
```

Étape 5 — Craft de la clé 5x5 (~1h)
```
Pour crafter la clé 5x5, il faut :
  - 4x Iron Grit (fondus en fourneau improvisé ? NON — pas de four)
  - SOLUTION : Poop + Iron Grit + Dirt = Crude Iron Nugget ?
  
  ALTERNATIVE MEILLEURE : 
  La clé 5x5 ne nécessite PAS de métal fondu.
  Elle nécessite des matériaux BRUTS de la 3x3.

  Recette clé 5x5 :
  [cobble] [iron_grit] [cobble]
  [copper_grit] [poop] [tin_grit]
  [cobble] [iron_grit] [cobble]

  Le poop comme composant central = Crop Dusting a un rôle !
  "Le Dr. Voss a noté que les matières organiques catalysent
  les réactions dimensionnelles. Oui, même celles-là." — Carnet n°2

OBTENIR LA CLÉ → clic droit sur le mur → salle s'agrandit à 5x5x5
(mécanisme technique : la clé craft une Compact Machine 5x5 + PSD)
```

**Quêtes 3x3 : 8 quêtes**
```
□ Ouvrir le coffre et lire le livre
□ Scavenge un mur au poing → dirt dust
□ Crafter un bloc de Dirt
□ Obtenir des graines et planter
□ Premier Bonsai Tree → bois
□ Table de craft
□ Première pioche → premiers grits
□ Crafter la clé 5x5 → expansion !
```

---

### ═══ SALLE 5x5x5 — "L'Espoir" (~4h) ═══

**Espace :** 125 blocs. Le joueur peut ENFIN construire un peu.

**Nouveautés qui deviennent possibles :**
- Ex Nihilo : Barrel (tonneau = composting, eau)
- Ex Nihilo : Sieve (tamis = minerais depuis gravel/sand/dust)
- Farming élargi (plus de place pour les cultures)
- Crop Dusting en masse (plus d'animaux possibles)

**Progression :**

Étape 6 — Ex Nihilo setup (~1h)
```
Barrel (tonneau Ex Nihilo) :
  Compost : feuilles, graines, saplings → Dirt
  Eau : place sous la pluie... MAIS pas de pluie en compact
  SOLUTION : recette custom eau → Barrel + leaves = Water over time
  OU : le barrel produit de l'eau depuis le composting

Sieve (tamis) :
  CRAFT : planks + sticks (vanilla)
  Mesh : String (depuis bonsai? ou crafting)
  
  Gravel dans le tamis →
    Iron Ore Piece (Ex Nihilo) → 4 = 1 Iron Ore
    Gold Ore Piece → 4 = 1 Gold Ore  
    Diamond fragment (rare)
    Lapis, Redstone, Coal

  Sand dans le tamis (mesh fer) →
    Copper, Tin, Silver, Nickel, Lead pieces

  Dust dans le tamis (mesh diamant) →
    Osmium, Platinum (rare), Certus Quartz (très rare)
```

Étape 7 — Premier four (~30 min)
```
Cobblestone → Four vanilla
MAIS : on BLOQUE le four vanilla via CraftTweaker
  recipes.remove(<minecraft:furnace>);

ALTERNATIVE : Tinkers Smeltery ? NON — trop gros pour 5x5
SOLUTION : Four Ex Nihilo (Unfired Crucible) ou un four custom

MEILLEURE SOLUTION : 
  Le four vanilla n'est PAS bloqué mais il est LENT (x0.25 vitesse)
  via config ou CraftTweaker fuel time
  Le joueur peut fondre ses grits → lingots, mais c'est LENT
  
  Iron Grit → furnace (lent) → Iron Nugget (pas un lingot entier!)
  4x Iron Nugget → 1x Iron Ingot
  
  Ça force le joueur à accumuler beaucoup de grits.
  Le tamis Ex Nihilo donne des Ore Pieces, qui sont plus efficaces.
  4x Iron Ore Piece → 1x Iron Ore → furnace → 1x Iron Ingot

Message : les grits sont MOINS efficaces que le tamis.
Le scavenging des murs = survie, le tamis = progression.
```

Étape 8 — Premiers outils Tinkers (~1h)
```
Pattern Chest + Stencil Table + Part Builder → Tinkers tables
  (ça rentre dans un 5x5)

Premiers outils Tinkers :
  Pickaxe (stone head + wood handle) → meilleur que vanilla
  Tool Leveling → l'outil gagne en XP

PAS encore de Smeltery (trop gros pour 5x5)
```

Étape 9 — Farming expansion (~1h)
```
Plus de Dirt → plus de farm
Pam's HarvestCraft :
  Jardin = placé sur Dirt → récolte variée
  Cuisiner des plats → Spice of Life Carrot → +cœurs

Crop Dusting :
  Plus d'animaux (chickens) → plus de poop
  Poop = composant pour engrais
  Poop + Bonemeal = Super Fertilizer (custom)

Bonsai Trees :
  Plusieurs Bonsai → bois + saplings + fruit
  Dark Oak Bonsai → pommes
  Birch Bonsai → sticks bonus
```

Étape 10 — Clé 7x7 (~30 min)
```
Recette clé 7x7 (nécessite métaux fondus cette fois) :
  [iron] [bronze_ingot] [iron]
  [copper] [compact_machine_wall] [tin]
  [iron] [bronze_ingot] [iron]

Bronze = 3 Copper + 1 Tin dans le four ? NON — 
  SOLUTION : Bronze Blend (custom?) = 3 Copper Grit + 1 Tin Grit
  Bronze Blend → furnace → Bronze Ingot (lent mais possible)
  
  OU : on utilise la Tinkers Smeltery dès le 5x5 (mini version?)
  PROBLÈME : Smeltery Controller + faucet + casting = minimum 7 blocs de base
  Dans un 5x5 c'est TIGHT mais possible si le joueur empile
```

**Quêtes 5x5 : 10 quêtes**
```
□ Explorer ta nouvelle salle !
□ Ex Nihilo : crafter un Barrel
□ Ex Nihilo : crafter un Sieve + Mesh
□ Tamiser du Gravel → premiers Iron Ore Pieces
□ Fondre tes premiers lingots (four vanilla lent)
□ Tinkers : Pattern Chest + Tool Station
□ Craft un outil Tinkers
□ Pam's : cuisiner 3 plats différents
□ Spice of Life : manger 10 aliments différents → cœurs
□ Crafter la clé 7x7 → expansion !
```

---

### ═══ SALLE 7x7x7 — "La Fonderie" (~5h) ═══

**Espace :** 343 blocs. ENFIN assez pour une Smeltery Tinkers !

**C'est ici que ça BASCULE :** Le joueur passe du mode survie manuelle
au mode "début d'industrie". La Smeltery change tout.

**Nouveautés :**
- Tinkers Smeltery complète (3x3 base minimum)
- Alliages dans la Smeltery (Bronze, Electrum, etc.)
- Construct's Armory (armures Tinkers)
- Soul Shards (début mob farming)
- Outils Tinkers avancés (pioches fer/bronze → meilleurs grits)

**Progression :**

Étape 11 — Smeltery (~1h)
```
Smeltery minimum :
  1x Smeltery Controller
  1x Seared Tank (fuel = lave)
  3x Seared Bricks (structure)
  1x Seared Faucet
  1x Casting Table ou Casting Basin

  Lave : Cobblestone dans Crucible Ex Nihilo → lave
  OU : Magma Crucible (PAS ENCORE — pas de RF)
  OU : Seared Tank accepte la lave → mais comment obtenir la lave?
  
  SOLUTION CC-style :
  Ex Nihilo Crucible (placed over fire/lava source) :
  Cobblestone/Netherrack → Lava (lent)
  Le joueur a besoin de Netherrack (grit spécial?) ou juste cobblestone
  
  MIEUX : Cobblestone dans Ex Nihilo Crucible (heat from torch below) → Lava
  La lave alimente la Smeltery

Smeltery fonctionnelle → TOUT CHANGE :
  Iron Grit → Smeltery → Molten Iron → Ingots (2:1 ratio — mieux que le four)
  Copper Grit → Molten Copper
  Tin Grit → Molten Tin
  
  ALLIAGES dans la Smeltery :
  3 Copper + 1 Tin = Bronze (4 lingots)
  1 Gold + 1 Silver = Electrum (2 lingots) → mais Gold et Silver rares
```

Étape 12 — Outils Tinkers améliorés (~1h)
```
Pioche Bronze + mur →
  30% : Silver Grit
  30% : Nickel Grit  
  30% : Lead Grit
  10% : Gold Grit

Pioche Fer + mur →
  40% : Gold Grit
  30% : Redstone
  20% : Lapis
  10% : Diamond fragment

Tool Leveling : les outils gagnent des modifiers en les utilisant
Modifiers : Haste, Luck, Fortified, etc.
```

Étape 13 — Soul Shards + mob farming (~1h)
```
Soul Shards :
  Craft le Soul Cage (nécessite Soul Sand → grit spécial?)
  
  PROBLÈME : Soul Sand = Nether normalement
  SOLUTION : Soul Sand Grit — pioche diamant Tinkers + mur
  OU : Ex Nihilo config — sieve dust → Soul Sand
  
  Tuer des mobs (spawns In Control dans compact machine)
  50 kills → Soul Shard tier 1 → Soul Cage → mob spawner
  
  Mobs utiles :
  Zombie → Rotten Flesh → Compost
  Skeleton → Bones → Bonemeal → farming
  Chicken → Eggs, Feathers, Poop (Crop Dusting)
  Enderman → Ender Pearl (très rare en compact)
```

Étape 14 — Storage + organisation (~1h)
```
Storage Drawers :
  Basic Drawer → 1 type, beaucoup de quantité
  Compacting Drawer → auto-compress (nuggets → ingots → blocks)
  Drawer Controller → accès centralisé

C'est CRUCIAL en compact — chaque bloc compte.
Storage Drawers = meilleur rapport stockage/bloc du jeu.
```

Étape 15 — Préparation sortie 7x7 (~1h)
```
Accumuler :
  - Stack+ de fer, cuivre, étain, bronze
  - Premiers diamants (tamis dust)
  - Premiers Ender Pearls (Enderman Soul Shard ou loot)
  - Nourriture variée → +cœurs Spice of Life

Clé 9x9 :
  [bronze] [electrum] [bronze]
  [iron_block] [diamond] [iron_block]
  [bronze] [electrum] [bronze]

Plus cher — nécessite Electrum (Gold + Silver) et Diamond.
```

**Quêtes 7x7 : 12 quêtes**
```
□ Smeltery : crafter le Controller
□ Smeltery : produire de la lave (Ex Nihilo Crucible)
□ Smeltery : fondre du fer
□ Premier alliage : Bronze
□ Outil Tinkers en Bronze
□ Ex Nihilo : tamiser avec mesh fer → nouveaux minerais
□ Soul Shards : tuer 50 mobs
□ Soul Cage : premier mob spawner automatique
□ Storage Drawers : organiser ta salle
□ Construct's Armory : première pièce d'armure
□ Lore : "Note de Voss #5 — La Smeltery"
□ Clé 9x9 → expansion !
```

---

### ═══ SALLE 9x9x9 — "La Machine" (~6h) ═══

**ICI ÇA ACCÉLÈRE.** Le joueur a de la place pour du RF.

**Nouveautés :**
- Thermal Expansion (PREMIER RF !)
- Steam Dynamo → Redstone Furnace → Pulverizer
- EnderIO Conduits (transport dans 1 bloc)
- Actually Additions basics
- Angel Blocks (ExU2) — placer en l'air !

**Le RF change TOUT :**
```
Steam Dynamo (charbon) → 40 RF/t
Redstone Furnace → cuisson instantanée vs four vanilla lent
Pulverizer → ore DOUBLING (tamis + pulverizer = x2)
EnderIO conduits → transport items/RF dans 1 seul bloc

Le joueur passe de "je fonds mes grits un par un dans un four lent"
à "ma machine broie et fond 10x plus vite".

C'est LE moment de breakthrough que CC fait bien :
"Enfin, la technologie fait le boulot pour moi."
```

---

### ═══ SALLE 11x11 — "L'Industrie" (~5h) ═══

**Thermal Expansion complet + EnderIO + IE basics**

- Induction Smelter → alliages (Invar, Signalum, etc.)
- EnderIO Alloy Smelter → Electrical Steel, Dark Steel
- IE Coke Oven (3x3x3, tight dans un 11x11 mais OK)
- IE Blast Furnace (3x3x3, acier)
- Simply Jetpacks tier 1 (vol limité)
- Dark Utilities Vector Plates (automatisation items)
- Automation basique (hoppers → machines → drawers)

---

### ═══ SALLE 13x13 — "L'Évasion" (~8h) ═══

**Magie + 3 Fragments + lab_key**

- Botania (Mana Pool + Endoflame + Terrasteel)
- Astral Sorcery (nécessite config ciel JED)
- Blood Magic (Altar T1-T3)
- Mystical Agriculture tier 1-2
- Extended Crafting table 3x3 (pour les Fragments)
- Les 3 Fragments (Mécanique + Organique + Stellaire)
- Lab Key → SORTIE

---

## SYSTÈME DE SCAVENGING CUSTOM

### Drops par outil (CraftTweaker event handler)

```
MAIN NUE (ne casse pas le mur, le frappe) :
  70% Dirt Dust → 4 = 1 Dirt
  25% Gravel Dust → 4 = 1 Gravel
  5%  Flint
  DÉGÂTS : 0.5 cœur par coup

PIOCHE BOIS :
  40% Cobblestone Fragment → 4 = 1 Cobblestone
  30% Dirt Dust
  20% Gravel Dust
  10% Clay Ball

PIOCHE PIERRE :
  35% Iron Grit
  30% Copper Grit
  25% Tin Grit
  10% Coal

PIOCHE FER :
  30% Silver Grit
  30% Nickel Grit
  25% Lead Grit
  15% Lapis Lazuli

PIOCHE BRONZE (Tinkers) :
  35% Gold Grit
  30% Redstone
  20% Osmium Grit (rare!)
  15% Quartz

PIOCHE DIAMANT :
  30% Diamond Fragment → 4 = 1 Diamond
  25% Emerald Fragment → 4 = 1 Emerald
  25% Ender Pearl Fragment → 8 = 1 Ender Pearl
  20% Obsidian Fragment → 4 = 1 Obsidian
```

### Grits → Lingots (ratios)

```
FOUR VANILLA (lent, disponible dès 5x5) :
  1 Grit → 1 Nugget (pas un lingot !)
  9 Nuggets → 1 Ingot
  Donc : 9 Grits → 1 Ingot (très inefficace)

SMELTERY TINKERS (disponible dès 7x7) :
  1 Grit → 1 Nugget dans la Smeltery (même ratio mais plus rapide)
  MAIS : 4 Grits en même temps → 1 Ingot (mieux en batch)

EX NIHILO TAMIS (disponible dès 5x5) :
  Ore Pieces → 4 = 1 Ore → furnace/smeltery → 1 Ingot
  C'est PLUS efficace que les grits
  → Le tamis est la voie principale, les grits sont le complément

PULVERIZER (disponible dès 9x9 avec RF) :
  1 Ore → 2 Dust → 2 Ingots
  Le Pulverizer rend le tamis 2x plus efficace
```

---

## ITEMS CUSTOM SUPPLÉMENTAIRES NÉCESSAIRES

```
DÉJÀ CRÉÉS :
  wall_dust, iron_grit, copper_grit, tin_grit, silver_grit,
  nickel_grit, lead_grit, gold_grit, osmium_grit
  compact_key_5x5/7x7/9x9/11x11/13x13, lab_key

À CRÉER (ContentTweaker) :
  cobblestone_fragment    (4 → 1 cobblestone)
  diamond_fragment        (4 → 1 diamond)
  emerald_fragment        (4 → 1 emerald)
  ender_pearl_fragment    (8 → 1 ender pearl)
  obsidian_fragment       (4 → 1 obsidian)
  soul_sand_dust          (4 → 1 soul sand)
  netherrack_fragment     (4 → 1 netherrack)
  super_fertilizer        (poop + bonemeal → accélère crops x5)
```

---

## CONFIG NÉCESSAIRES

### In Control — Mobs en Compact Machine
```json
[
  {
    "dimension": [144, 145, 146, 147, 148, 149],
    "comment": "Compact Machine dimensions",
    "mob": ["zombie", "skeleton", "spider", "creeper", "enderman"],
    "result": "allow",
    "maxcount": 5,
    "comment2": "Peu de mobs, juste assez pour Soul Shards"
  },
  {
    "dimension": [144, 145, 146, 147, 148, 149],
    "mob": ["chicken", "cow", "pig", "sheep"],
    "result": "allow",
    "maxcount": 3,
    "comment": "Animaux passifs pour Crop Dusting + nourriture"
  }
]
```

### Four vanilla — Ralenti
```zenscript
// Le four vanilla est 4x plus lent qu'un four normal
// Implémenté via fuel time ou furnace speed config
// Les grits donnent des Nuggets, pas des Ingots
```

### JustEnoughDimensions — Ciel en Compact Machine
```json
{
  "dimensionId": 144,
  "skyLight": true,
  "hasSkyLight": true,
  "comment": "Permet Astral Sorcery dans la salle 13x13"
}
```

---

*Document créé le 26 Mars 2026*
