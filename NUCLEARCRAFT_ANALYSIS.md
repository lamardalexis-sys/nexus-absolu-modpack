# NuclearCraft Overhauled — Analyse pour intégration Nexus Absolu

**Source étudiée** : `github.com/tomdodd4598/NuclearCraft` (commit 37b383e, v2o.9.3)
**Minecraft** : 1.12.2 (match parfait avec Nexus Absolu)
**License** : MIT (intégrable librement)
**Taille code** : 6267 lignes pour juste les multiblocs nuclear (~50k LOC total)

---

## Vue d'ensemble : 4 PILIERS du mod

NuclearCraft n'est pas un mod de machines éparses, c'est un écosystème en **4 piliers qui s'enchaînent** :

```
┌──────────────────┐   produit du   ┌─────────────────┐   chauffe    ┌──────────────┐
│  1. PROCESSING   │ ─ combustible→ │  2. FISSION /   │ ─ vapeur →   │  3. TURBINE  │
│  (21 machines)   │                │     FUSION      │              │  (génère RF) │
└──────────────────┘                └─────────────────┘              └──────────────┘
                                            │
                                            │ chaleur perdue
                                            ▼
                                    ┌──────────────────┐
                                    │  4. HEAT EXCHNGR │
                                    │  (recyclage RF)  │
                                    └──────────────────┘

Accessoire : RTG (générateur passif décroissant, pour early/mid-game)
```

### Pilier 1 — Processing (21 machines, toutes single-block)

Catégories de process :
| Machine | Entrée | Sortie | Rôle progression |
|---|---|---|---|
| **Manufactory** | Ore/dust | dust fin | Age 0-1 (équivalent Pulverizer) |
| **Separator** | dust mix | 2-3 dusts | Séparation minerais |
| **Electric Furnace** | ore/dust | ingot | Alternative au four vanilla, plus rapide |
| **Alloy Furnace** | 2-3 ingots | alloy | Crafting alliages (bronze, steel, HSLA) |
| **Infuser** | item + fluid | item infused | Upgrades items |
| **Melter** | block/ingot | fluid | Entrée chaîne fluides |
| **Supercooler** | fluid | cryo fluid | Liquid nitrogen, helium |
| **Electrolyzer** | fluid (eau) | 2 gas (H2, O2) | Electrolyse eau/sels |
| **Assembler** | 4-6 items | part complexe | Circuits, composants |
| **Ingot Former** | fluid | ingot | Inverse du Melter |
| **Pressurizer** | dust/ingot | plate/block | Compression |
| **Chemical Reactor** | 2 fluids | fluid + fluid | Cœur chimie radio-chimique |
| **Salt Mixer** | 2 fluids | molten salt | Préparation fuel liquid fission |
| **Crystallizer** | fluid | gem | Création cristaux uranium/thorium |
| **Enricher** | ore + substance | ore enrichi | Métallurgie spéciale |
| **Extractor** | item | item + scrap | Recyclage |
| **Centrifuge** | fluid/item | 4 outputs séparés | Enrichissement uranium (U-235 from U-238) |
| **Rock Crusher** | cobble/stone | dust minéraux | Entrée mining early |
| **Decay Hastener** | isotope unstable | isotope stable + energy | Accélère la décroissance |
| **Fuel Reprocessor** | fuel spent | 4 outputs radioactifs | Recyclage combustible |
| **Nuclear Furnace** | itempile coal/uranium | RF bas | Alternative générateur |

**Lecture progression Nexus Absolu** :
- Early Age 3 : Manufactory + Electric Furnace + Alloy Furnace (remplacent grind Tinkers)
- Mid Age 3 : Melter + Ingot Former + Pressurizer (chaîne fluide basique)
- Late Age 3 : Centrifuge + Enricher + Chemical Reactor (prep combustible fission)
- Age 4+ : Fuel Reprocessor, Decay Hastener, Supercooler (maintenance réacteur)

### Pilier 2 — Fission Reactor (CŒUR du mod)

Deux modes principaux :

**2a. Solid Fuel Fission** — `SolidFuelFissionLogic.java`
- Réacteur rectangulaire 3-24 blocs de côté
- **Fuel Cell** = TileFissionCell au centre, contient pellets (TBU, LEU-233, MOX, etc.)
- **Moderator blocks** (graphite, beryllium, heavy water) **placés entre cells** pour augmenter flux
- **Heatsinks** (water, iron, redstone, glowstone, lapis, gold, prismarine, etc.) — chacun a des règles d'adjacence (ex. "water heatsink needs ≥1 reactor cell adjacent")
- **Chaque tier de fuel** a : heat/tick, efficiency, criticality (min cells for chain reaction)
- Logic complexe : chaque cell calcule un **"fuel bunch"** = amas contigu de cells pilotés par un même controller
- Output : produit de la **chaleur** + utilise l'**efficience** pour convertir → RF (via Turbine derrière)

**2b. Molten Salt Fission** — `MoltenSaltFissionLogic.java`
- Au lieu de pellets solides, utilise des **fluides molten salt** (ex. Flibe-TBU, NaF-LEU)
- Pas de fuel cells fixes : la chaleur est dans le salt lui-même
- Nécessite **heater blocks** connectés à un réseau de **coolants fluides**
- Plus complexe, plus efficace, endgame

**Structure du code** :
```
FissionReactor       (multiblock state, world blocks)
├── FissionReactorLogic (superclasse)
│    ├── SolidFuelFissionLogic
│    └── MoltenSaltFissionLogic
├── FissionCluster       (groupes de cells connectées)
├── FissionFuelBunch     (cells adjacentes partageant reactivity)
└── FissionPlacement     (règles validation adjacence)
```

### Pilier 3 — Turbine (conversion chaleur → RF)

- Multiblock 3-24 blocs, doit être **horizontalement étendu**
- **Rotor axle** + **rotor blades** (steel, extreme, sic-sic-cmc) en section croisée
- **Dynamo coils** (Magnesium, Beryllium, Aluminium, Gold, Copper, Silver)
  aux 2 bouts du rotor, chaque coil a des règles adjacence
- Entrée : **fluide chaud** (steam HP ou autre) produit par Fission/HeatEx
- Sortie : **RF / FE** (jusqu'à des millions RF/t avec turbine maxée)
- Physique réaliste : velocity, expansion ratio, rotor area, blade tier

### Pilier 4 — Heat Exchanger (recyclage)

- Multiblock tube-réseau
- Prend **vapeur épuisée** (basse pression) sortie turbine
- Utilise **coolants** (eau, helium, nitrogen, CO2) pour récupérer chaleur
- Re-chauffe ces coolants → renvoyés vers réacteur
- Boucle fermée quasi-infinie pour endgame
- Classes : `CondenserLogic` (condensation), `HeatExchangerFlowHelper` (flow direction)

### Bonus — RTG (générateur passif early)

- Multiblock 1×1 ou plus, alimenté par un **RTG pellet** (Plutonium, Americium, Californium, Polonium)
- Génère ~10-5000 RF/t **passivement**, 24/7, pas besoin de cooling
- Pellets décroissent (durée de vie basée sur half-life réelle : Pu-238 = 87.7 ans IRL)
- **Progression Age 3 early** : passe d'un générateur passif sûr mais lent → gros réacteur fission risqué mais puissant

---

## 4 mods pour mix avec NuclearCraft (déjà dans ton pack)

### A) **Mekanism** — Purification + Turbines complémentaires
- Mek **Gas System** (hydrogen, oxygen, sulfuric acid) peut alimenter les Electrolyzer/Chemical NC
- Mek **Dynamic Tank/Induction Matrix** = stockage ×10 capacité des batteries NC
- **Mek Turbine** 3x3x3 : alternative plus petite à NC Turbine, utile avant maîtrise
- **Synergy concrète** : enrichir uranium via Mek Enrichment → pelletize via NC Pressurizer

### B) **Immersive Engineering** — Câblage haute tension + raffinage
- **IE HV Wire (LV/MV/HV)** : transport RF fiable, visuellement cohérent avec thème "vintage-industriel" NC
- **IE Diesel Generator** : utilise biodiesel → pont vers les fluides NC
- **IE Coke Oven** : charbon → creosote → entrée Chemical Reactor NC
- **Synergy concrète** : raffiner oil IE → diesel → alimenter Mek/NC early game

### C) **EnderIO** — Conduits universels + buffer
- **Energy Conduits** : meilleur networking RF que Thermal pour grosses installations NC
- **Capacitor Bank** : buffer intermediate entre Turbine NC et le reste du réseau
- **Item Conduits** avec filtres : auto-extract produits fission cell / reprocessor
- **Synergy concrète** : réseau item auto entre les 21 machines processing NC

### D) **Draconic Evolution** — Endgame stockage + tier final
- **Energy Core Tier 6-8** : stocker les GWh produits par un réacteur fission maxé
- **Reactor DE** : alternative/complémentaire au fission NC (boules draconium)
- **Awakened Draconium** : composant pour upgrade NC endgame
- **Synergy concrète** : RTG NC → recharge items Draconic passivement
  OU réacteur fission NC → pump vers Energy Core DE

---

## Proposition d'intégration Age 3 "tout doucement"

### Age 3 early (premières heures)
1. **Débloquer Manufactory + Electric Furnace + Alloy Furnace NC**
   - Remplace le crafting manuel Tinkers → workflow RF-based
   - Fourni par quête : "Dr. Voss a trouvé des plans industriels pré-apocalypse"
2. **RTG Plutonium-238** (passif ~100 RF/t)
   - Gate sur trouver du Plutonium (ore worldgen config NC, ou craft via Chemical Reactor)
   - Premier générateur stable non-basé sur fuel humain

### Age 3 mid (maîtrise chimique)
3. **Chaîne liquide** : Melter → Ingot Former → Pressurizer
4. **Centrifuge + Enricher**
   - Pour séparer U-238/U-235, première rencontre avec la radioactivité
5. **Compose D (Nexus)** = fuel TBU-ThorOxide enrichi
   - Recette custom Nexus via Chemical Reactor NC
   - Gate propre pour passer Age 4

### Age 3 late (premier réacteur)
6. **Petit réacteur fission solid-fuel** (3x3x3 minimum)
7. **Petite turbine** (4x4x4)
8. **Premier RF massif** : 50-200k RF/t → alimente enfin le Pallanutro Furnace

### Age 4 (domaine fission maîtrisé)
9. Heat Exchanger pour boucler la vapeur
10. Molten Salt Fission (progression naturelle)
11. Integration DE Reactor pour comparer

### Age 5+ (fusion)
12. NC Fusion Reactor (jumelle DE)

---

## Intégration technique : comment câbler NC à Nexus Absolu

### Recettes customisées (CraftTweaker)
- Scripts `scripts/Age3_Nuclear.zs` à créer
- Forcer usage de certains composés Nexus dans les recipes NC
- Exemple : `NC:pressurizer.addRecipe([<nexusabsolu:vossium_iv_ingot>, <liquid:molten_tbu>], <nexusabsolu:compose_d>)`

### Quêtes (BetterQuesting)
- Line nouvelle "Atomique" dans `quests-source/age3.json`
- ~30-40 quêtes : débloquer machines une par une, guider vers premier RTG, puis petit réacteur
- Milestone finaux : "Produire 1M RF" (= pouvoir alimenter Pallanutro durablement)

### Compact Machines
- NC réacteurs peuvent se construire **dans** un Compact Machine 11x11 ou 13x13
- Parfait pour isoler la radiation !
- Tu pourrais créer un nouveau CM "Réacteur" uniquement débloqué Age 3

### Radiation System (intégré NC)
- NC a son propre système de radiation : aléatoire, affecte biomes
- Peut être désactivé via config si tu le trouves trop punitif
- Ou intégré thématiquement : "le Paradoxe Organique a déjà exposé Voss à la radiation..."

---

## Points d'attention avant de commencer

### Configs à regarder
- `config/nuclearcraft/*.cfg` : énormément d'options (enable_fission, ore_gen, RF output multipliers)
- NC permet de désactiver indépendamment fission/turbine/rtg si tu veux limiter le scope
- Conversion RF↔EU↔FE ratios configurables

### Taille de la tâche
- Comprendre NC à fond = ~10-15 heures lecture code + tests in-game
- Design Age 3 nuclear line = ~5-8 heures
- Quêtes + recettes custom = ~15-20 heures selon profondeur
- **Total : ~40h pour une intégration Age 3 solide**

### Risques identifiés
1. **Complexité onboarding** : NC peut submerger un joueur. Tutorial in-game critique.
2. **Balance RF** : un réacteur minimaliste produit déjà plus que Pallanutro ne consomme → risque d'"obsoléter" le reste du pack
3. **Performance serveur** : gros réacteurs = beaucoup de tick/update. À tester sur OMGSERV.
4. **Radiation** : si mal configurée, peut tuer des joueurs en solo qui cherchent juste à explorer

---

## Prochaines étapes possibles

Plusieurs directions selon ton envie :

A. **Deep-dive sur UN pilier spécifique** (ex. je décortique le SolidFuelFissionLogic.java en détail pour comprendre les règles de placement/adjacence)

B. **Analyse des recettes** : je lis les 21 fichiers RecipeHandler et dresse le graphe complet "qui produit quoi" pour identifier les points d'ancrage Nexus

C. **Draft design document Age 3 Nuclear Line** : je rédige un vrai plan de progression (quêtes + gating + RF budget) basé sur ce qu'on a vu

D. **Explorer un des 4 mods mix en détail** pour voir les points de jonction techniques

E. **Autre chose** que tu as en tête
