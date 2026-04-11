# PROGRESSION_GATES_DESIGN.md — 3 minerais / 3 boss / Mega Furnace

> Design doc séparé d'`AGE2_INTRO_DESIGN.md`. Sujet : utiliser les 3 minerais du mod Nexus comme
> gates de tier "à la ATM10 mais sauce Nexus", avec la **Mega Furnace** (9 slots, vitesse ×10000)
> comme capstone d'un chapter endgame.
> Rien n'est écrit en Java ni dans `age2.json` tant qu'Alexis n'a pas choisi une direction.

---

## 1. État actuel (source Java)

Fichiers inspectés :
- `mod-source/src/main/java/com/nexusabsolu/mod/blocks/BlockNexusOre.java`
- `mod-source/src/main/java/com/nexusabsolu/mod/init/ModBlocks.java` (lignes 37-39)
- `mod-source/src/main/java/com/nexusabsolu/mod/init/ModItems.java` (lignes 96-101)
- `mod-source/src/main/java/com/nexusabsolu/mod/world/NexusOreGen.java`

### 1.1 Les 3 minerais enregistrés

| Block | Registry | Hardness | Harvest Level | XP drop | Vein | Chances/chunk | Y range | Dimensions actuelles |
|-------|----------|---------:|:-------------:|:-------:|:----:|:-------------:|:-------:|----------------------|
| `BlockNexusOre("claustrite_ore", …)` | `nexusabsolu:claustrite_ore` | 3.0 | 1 (stone+) | 2-5 | 2 | 2 | 5-30 | Advanced Rocketry (dim ≥ 100, hors 144) |
| `BlockNexusOre("vossium_ore", …)` | `nexusabsolu:vossium_ore` | 4.0 | 2 (iron+) | 3-7 | 4 | 3 | 5-40 | Galacticraft Moon (-28) + Mars (-29) |
| `BlockNexusOre("nexium_ore", …)` | `nexusabsolu:nexium_ore` | 5.0 | 3 (diamond+) | 5-10 | 3 | 2 | 10-60 | Galacticraft Venus (-31) + Asteroids (-30) |

`BlockNexusOre` hérite de `Block` (pas de `BlockOre`). Pas d'override `getItemDropped` → le bloc se drop lui-même quand miné (pas de silk touch nécessaire).

### 1.2 État des ingots/chaînes

- **Vossium** → chaîne complète : `vossium_ingot` + `vossium_ii_ingot` + `vossium_iii_ingot` + `vossium_iv_ingot` + `invarium_ingot` (intermédiaire) + `signalhee_ingot`. Vossium est **déjà utilisé à l'Age 1** (Condenseur T2 + matériaux) et à l'Age 2 (II/III/IV).
- **Nexium** → bloc minerai seul. Pas de `nexium_ingot` dans `ModItems.java`. Aucun craft ni smelt connu à ce stade.
- **Claustrite** → bloc minerai seul. Pas de `claustrite_ingot`. Aucun craft.

### 1.3 Conséquences pour le design

Deux implications importantes :

1. **Les 3 minerais sont INAJUSTABLES à l'Age 2** avec le spawn actuel, parce qu'ils nécessitent Galacticraft (Age 6 dans le plan des Ages documenté dans SKILL.md). Pour les utiliser comme gates Age 2, il **faut remapper les dimensions de spawn**.
2. **Nexium et Claustrite sont des coquilles vides** — les blocs existent, les ingots n'existent pas, les recettes n'existent pas. Il y a du travail à faire pour qu'ils deviennent utilisables en jeu.

### 1.4 Et l'idée « minerai de l'End » ?

Tu as dit « avec le minerai pris dans l'End, crafts dans des crafts dans des crafts, il faudrait faire une Mega Furnace ». Dans le code actuel, **aucun des 3 minerais ne spawne dans l'End**. Deux interprétations possibles :

- Tu penses à un **4ème minerai à ajouter** (par exemple `endrium_ore`) qui spawnerait uniquement dans l'End.
- OU tu veux **remapper un des 3 existants** vers l'End (Nexium étant le meilleur candidat parce que c'est le plus rare et le plus dur).

Les deux sont possibles, voir Section 3 options A / B / C.

---

## 2. Le modèle ATM10 appliqué — rappel

(Résumé de la Section 10 d'`AGE2_INTRO_DESIGN.md`.)

Dans ATM10, les 3 boss vanilla sont des **gates de tiers de matériaux**, pas des climax narratifs :

| Boss | Gate ATM10 vers |
|------|------------------|
| Warden | **Allthemodium** (tier 1 ATM) — première ressource ATM accessible |
| Wither | Nether Star → summon boss Cataclysm "Harbinger" |
| Ender Dragon | End → **Unobtainium** + **Vibranium** (tier 2-3 ATM) |

Les 3 ressources ATM forment une **pyramide** : Allthemodium (facile) → Vibranium (mid) → Unobtainium (endgame). Chaque étage débloque le suivant via une recette qui **mélange les trois** + du matériel de mod (AE2, Mekanism, etc.).

**Transposé à Nexus** : on remplace Allthemodium / Vibranium / Unobtainium par **Claustrite / Vossium / Nexium**. Chaque boss débloque un minerai, chaque minerai devient un étage de la pyramide, et la Mega Furnace est au sommet.

---

## 3. Trois options de re-mapping des dimensions

### Option A — Tout remapper vers vanilla (Overworld + Nether + End)

Change les dimensions de spawn dans `NexusOreGen.java` :

| Minerai | Nouveau spawn | Gated par | Age d'accès |
|---------|---------------|-----------|-------------|
| **Claustrite** | Overworld deep caves (Y<20) ou Deep Dark (Deeper in the Caves) | **Warden** (Deep Dark) | Age 2 intro |
| **Vossium** | Nether (y<50 dans le Nether) | **Wither** | Age 2 mi |
| **Nexium** | End (partout dans le main island + islands extérieures) | **Ender Dragon** | Age 2 fin / Age 3 |

**Pros**
- ✅ Alignement parfait 1:1 avec ATM10 (Warden → tier 1, Wither → tier 2, Dragon → tier 3).
- ✅ Les 3 boss ont une raison d'exister narrativement : chacun débloque un minerai.
- ✅ Aucun nouveau minerai Java à créer — on réutilise les 3 blocs existants.
- ✅ Le Mega Furnace peut demander les 3 minerais (base Claustrite, cœur Vossium, cadre Nexium).

**Cons**
- ⚠️ **Casse le thème Age 6 "Conquête du Vide"** — il n'y a plus de minerai exclusif à Galacticraft / Advanced Rocketry. Age 6 doit être repensé autour d'autres matériaux (resources spatiales spécifiques, items custom planète-par-planète, etc.).
- ⚠️ **Casse la progression existante Vossium** — Vossium est actuellement un matériau Age 1 obtenu par alliage (Invarium + Composé A). Si on fait aussi spawner Vossium dans le Nether, on a deux sources qui entrent en conflit. Il faut clarifier : soit le Vossium alloyé de l'Age 1 est un item différent du Vossium miné (`vossium_alloy_ingot` vs `vossium_ingot`), soit on supprime une des deux sources.
- ⚠️ Tous les saves en cours perdent leurs anciennes veines Galacticraft — elles ne spawnent plus.

### Option B — Ajouter un 4ème minerai "Endrium" dans l'End

Les 3 minerais actuels gardent leurs dimensions space (Age 6), on **ajoute** un nouveau bloc :

- `BlockNexusOre("endrium_ore", 6.0F, 3, 6, 12)` → spawne dans le End (dim 1), vein 3, chances 2/chunk.
- Nouveau ingot `endrium_ingot`.
- Gated par Ender Dragon (ou pas — l'End est accessible sans tuer le Dragon, mais en pratique les gens le tuent).

**Pros**
- ✅ Préserve totalement le thème Age 6 space.
- ✅ Aucun conflit avec Vossium alloy de l'Age 1.
- ✅ L'End ore devient l'ingrédient endgame spécifique de la Mega Furnace.
- ✅ Extension cohérente : on ajoute UN bloc + UN ingot, pas une refonte.

**Cons**
- ⚠️ Ne fait pas du tout du ATM10-style "gate matériau par boss". Les 3 minerais actuels restent Age 6, indépendants des boss vanilla.
- ⚠️ Warden et Wither ne gâtent aucun minerai. Ils redeviennent des boss "narratifs" sans reward matériau.
- ⚠️ Le joueur n'a AUCUN minerai Nexus avant l'Age 6 (sauf le Vossium alloyé de l'Age 1 qui existe toujours).

### Option C — Hybride : 2 minerais remappés vers vanilla + 1 minerai End (nouveau)

Compromis entre A et B.

| Minerai | Dimension | Age d'accès | Gated par |
|---------|-----------|-------------|-----------|
| **Claustrite** | Overworld deep caves (Deep Dark / Deeper in the Caves) | Age 2 intro | Warden |
| **Vossium** *(alloy, inchangé)* | Crafté via Invarium + Composé A | Age 1 | (chaîne existante) |
| **Vossium minerai brut** *(bloc actuel)* | Supprimé ou relégué à Age 6 Galacticraft en bonus | — | — |
| **Nexium** | Nether (remplace Venus/Asteroids) | Age 2 mi | Wither |
| **Endrium** (NOUVEAU) | End | Age 2 fin / Age 3 | Ender Dragon |

**Pros**
- ✅ Alignement ATM10 sur les 3 boss (Warden / Wither / Dragon → Claustrite / Nexium / Endrium).
- ✅ Le Vossium existant n'est pas touché (pas de conflit avec la chaîne Age 1 alloy).
- ✅ Chaque boss a une raison.
- ✅ Ajoute seulement UN nouveau minerai (Endrium), on remappe seulement UN existant (Nexium).
- ✅ Claustrite (qui était un bloc orphelin sans usage) devient utile.

**Cons**
- ⚠️ On perd le spawn Vossium sur Moon/Mars (il est juste alloyé, pas miné). Age 6 doit trouver autre chose comme reward minerai.
- ⚠️ Il reste quand même à créer `endrium_ingot` et tout son tooling.

**Mon pick** : **Option C**. C'est le meilleur compromis : ça respecte le "à notre sauce", ça débloque les 3 boss comme gates ATM10-style, ça ne casse PAS la chaîne Vossium existante, et ça ajoute un minimum de nouveau code Java.

---

## 4. La Mega Furnace — spécification

### 4.1 Ce que tu as demandé

- **9 slots à l'intérieur**
- **Vitesse ×10000 vs vanilla furnace**
- **Crafts dans des crafts dans des crafts** (chaîne de composants intermédiaires)
- **Utilise le minerai de l'End** (Endrium ou Nexium remappé)

### 4.2 Gameplay

**Vanilla furnace** : 1 slot input + 1 slot fuel + 1 slot output, 200 ticks (10s) par item.
**Mega Furnace** : **9 slots input + 1 slot fuel + 9 slots output**, 0,02 ticks par item (effectivement instantané).

Le "9 slots" devient une grille 3×3 d'emplacements de smelting parallèles. Tu peux mettre 9 items différents, chacun est traité en parallèle et instantanément.

Vitesse ×10000 :
- Vanilla = 200 ticks/item
- Mega Furnace = 200 / 10000 = 0,02 ticks/item
- À 20 TPS = 400 items/tick × 9 slots = **3600 items/tick total**
- En pratique : tu vides un shulker box entier (27 stacks de 64 = 1728 items) en moins de 1 tick.

**C'est absurdement cher en RF** pour équilibrer :
- 8192 RF/tick par slot actif (× 9 = 73728 RF/tick peak)
- Buffer interne 10 M RF
- Fuel = burning fuel vanilla OK, ou **RF only** (plus endgame). Je pense : RF only, ça force l'intégration AE2/Mekanism.

**UI** : 3×3 input en haut, 3×3 output en bas, barre de progression centrale style Extended Crafting Ultimate, écran énergie à droite.

**Block model** : bloc cubique métallique avec faces animées (grille rouge incandescente), TESR avec des items qui flottent dans la grille input et qui disparaissent en instant → sphère lumineuse dans le centre → réapparaissent smeltés dans la grille output. Gros TESR.

### 4.3 Recette — chaîne de crafts dans des crafts

Inspiré de Compact Claustrophobia + E2E : chaque composant nécessite une machine précédente + 2-3 mods différents. Utilisation des 3 minerais à 3 étapes distinctes.

```
ÉTAPE 1 — Noyau Fondamental
┌──────────────────────────────────────────────────────────┐
│  Input: 4× Claustrite Ingot + 4× Redstone + 1× Composé A │
│  Machine: Inscriber AE2 (débloqué via Warden kill)        │
│  Output: 1× Fundamental Core                              │
└──────────────────────────────────────────────────────────┘

ÉTAPE 2 — Catalyseur Ionique
┌──────────────────────────────────────────────────────────┐
│  Input: 2× Vossium IV Ingot + 2× Blaze Rod + 1× Fundamental Core │
│  Machine: Tinkers Smeltery ou Mekanism Infuser           │
│  Output: 1× Ionic Catalyst                                │
└──────────────────────────────────────────────────────────┘

ÉTAPE 3 — Cadre Stellaire
┌──────────────────────────────────────────────────────────┐
│  Input: 4× Nexium Ingot + 2× Ionic Catalyst + 1× Nether Star │
│         (Nether Star vient du Wither kill)                │
│  Machine: Avaritia Neutronium Compressor                  │
│  Output: 1× Stellar Frame                                 │
└──────────────────────────────────────────────────────────┘

ÉTAPE 4 — Cœur Endrium
┌──────────────────────────────────────────────────────────┐
│  Input: 8× Endrium Ingot + 1× Dragon Egg + 1× Stellar Frame │
│         (Dragon Egg vient du Dragon kill)                 │
│  Machine: Extended Crafting Ultimate Table (7x7)          │
│  Output: 1× Endrium Core                                  │
└──────────────────────────────────────────────────────────┘

ÉTAPE 5 — La Mega Furnace
┌──────────────────────────────────────────────────────────┐
│  Input: 1× Endrium Core + 9× Furnace (vanilla) +          │
│         1× AE2 Quantum Ring + 1× Draconic Core Tier 4     │
│         + 4× Vossium IV Block                             │
│  Machine: Extended Crafting Ultimate Table (9x9)          │
│  Output: 1× Mega Furnace (block)                          │
└──────────────────────────────────────────────────────────┘
```

**Mods forcés dans la chaîne** :
1. Nexus Absolu (Claustrite, Vossium, Nexium, Endrium, Composé A)
2. AE2 (Inscriber, Quantum Ring)
3. Tinkers Construct OU Mekanism (Infuser)
4. Avaritia (Neutronium Compressor)
5. Extended Crafting (Ultimate Table 7x7 puis 9x9)
6. Draconic Evolution (Core Tier 4)
7. Vanilla (9 furnaces, blaze rod, nether star, dragon egg)

**7 mods**. Philosophie Nexus respectée : "chaque mod doit interagir avec au moins 2 autres".

Les **3 boss vanilla** sont chacun sur le chemin critique :
- Warden → débloque Claustrite → Étape 1
- Wither → donne Nether Star → Étape 3
- Ender Dragon → donne Dragon Egg → Étape 4

Aucun des 3 n'est skippable.

### 4.4 Placement dans la progression des Ages

La Mega Furnace n'est **pas** un item d'Age 2. Elle nécessite :
- Avaritia (Age 8)
- Draconic Core Tier 4 (Age 7-8)
- Extended Crafting Ultimate 9x9 (Age 8-9)

Donc c'est un item **d'Age 8**. Probablement une des 9 composantes du Nexus Absolu lui-même (Age 9), ou un item-clé qui débloque l'Age 9 ("sans la Mega Furnace tu ne peux pas produire en masse les composés finaux").

Elle ne se situe **PAS** dans l'intro Age 2 qu'on refondait. Mais sa **préparation** commence à l'Age 2 (obtenir le 1er Claustrite, tuer le Warden). C'est exactement le pattern ATM10 : tu touches Allthemodium en mid-game et c'est l'escalier qui te mène à l'ATM Star de endgame.

### 4.5 Implémentation Java — esquisse

Fichiers à créer (budget ~5/5, gros travail) :

```
mod-source/src/main/java/com/nexusabsolu/mod/
├── blocks/machines/BlockMegaFurnace.java         (BlockContainer + BlockStates)
├── tiles/TileMegaFurnace.java                    (TE, smelting logic, RF handling)
├── tiles/MegaFurnaceRecipes.java                 (recipe resolver, vanilla + modded smelting)
├── gui/ContainerMegaFurnace.java                 (3x3 input + 3x3 output + fuel + RF bar)
├── gui/GuiMegaFurnace.java                       (UI rendering)
├── render/TESRMegaFurnace.java                   (item float + light sphere + smelt particle FX)
├── blocks/BlockEndriumOre.java OR extend BlockNexusOre
├── items/ItemEndriumIngot.java                   (or use ItemBase)
├── items/ItemFundamentalCore.java                (recipe component)
├── items/ItemIonicCatalyst.java
├── items/ItemStellarFrame.java
├── items/ItemEndriumCore.java
```

Plus :
- `ModBlocks.java` / `ModItems.java` : register everything
- `world/NexusOreGen.java` : add End generation branch for Endrium (dim 1)
- Recipes : CraftTweaker scripts dans `scripts/` OU JSON recipes dans `src/main/resources/assets/nexusabsolu/recipes/`

Estimation : **15-20 heures de dev Java** pour la Mega Furnace complète avec TESR + toutes les étapes.

---

## 5. Intégration avec les quêtes BetterQuesting

Une fois les items Java créés, on écrit un **nouveau chapter ou un set de quêtes cross-age** dans `quests-source/` :

Idée de structure (à débattre) :

```
quests-source/
  _meta.json
  age0.json
  age1.json
  age2.json
  age3.json     ← à créer plus tard
  ...
  age8.json     ← à créer plus tard
  mega_furnace.json  ← chapter dédié au crafting de la Mega Furnace
  lines.json    ← une nouvelle questLine "Pyramide Minerai" qui pointe vers les étapes
```

Ou alternative plus simple : pas de nouveau fichier, on ajoute les quêtes Mega Furnace directement dans `age8.json` (quand il existera) et on ajoute juste les dépendances cross-age via prereqs symboliques.

---

## 6. Questions pour toi

Avant que je code OU que j'écrive des quêtes Mega Furnace :

### 6.1 Sur les 3 minerais (remapping)
- [ ] **M-Q1** : Tu valides **Option C** (hybrid) ? Ou tu préfères A (tout remapper vanilla) ou B (ajouter Endrium sans toucher les existants) ?
- [ ] **M-Q2** : Si Option C, accord pour que Claustrite spawne dans le Deep Dark (via Deeper in the Caves, Y<-40) au lieu d'Advanced Rocketry ?
- [ ] **M-Q3** : Si Option C, accord pour que Nexium spawne dans le Nether au lieu de Venus+Asteroids ? (Ça mange une partie du thème Age 6 space)
- [ ] **M-Q4** : Tu veux garder le spawn Age 6 space pour Vossium en BONUS (on peut faire double spawn : Age 1 alloy chain + Age 6 bonus via Moon/Mars rocketry) ?

### 6.2 Sur la Mega Furnace (item)
- [ ] **MF-Q1** : 9 slots = 3×3 parallel smelting OK ? Ou tu veux 9 slots en file (1 seul item à la fois mais 9 items buffer) ?
- [ ] **MF-Q2** : Vitesse ×10000 = instantané (200 ticks / 10000 = 0.02 tick). Ça te va ou tu veux que ce soit plus modéré (ex. ×100 = 2 ticks) ?
- [ ] **MF-Q3** : Fuel RF only (endgame strict) ou RF + vanilla fuel (flexibilité) ?
- [ ] **MF-Q4** : La chaîne 5 étapes avec 7 mods — OK pour toi ou tu veux simplifier / complexifier ?
- [ ] **MF-Q5** : Placement Age 8 ou ailleurs ? Est-ce que ça devient une des 9 composantes du Nexus Absolu (Age 9) ou un item séparé ?

### 6.3 Sur la priorité
- [ ] **P-Q1** : On s'occupe de ÇA maintenant ou on finit d'abord la refonte intro Age 2 (`AGE2_INTRO_DESIGN.md` Section 9, les 6 blockers attendent toujours ta validation) ?

Mon vote : **finir l'intro Age 2 d'abord**, parce qu'elle est en cours et débloquera une vraie session de playtest. La Mega Furnace est un gros bloc de travail Java (15-20h) qui peut attendre que l'intro Age 2 soit stable.

---

## 7. Sources

- `BlockNexusOre.java`, `ModBlocks.java:37-39`, `ModItems.java:96-101`, `NexusOreGen.java` (mod-source local)
- [ATM10 Bosses Guide](https://all-themods.com/bosses/)
- [ATM10 ATM Star & Custom Items (DeepWiki)](https://deepwiki.com/AllTheMods/ATM-10/3.1-atm-star-and-custom-items)
- `AGE2_INTRO_DESIGN.md` Section 10 (étude du modèle ATM10)
