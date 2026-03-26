# MOD.md — Base de données des mods Nexus Absolu
> Référence complète pour les recettes, items clés, chaînes d'automatisation et patterns inter-mods.
> Basé sur l'analyse de : Enigmatica 2 Expert, Compact Claustrophobia, ATM 10

---

## TABLE DES MATIÈRES
1. [Carte des mods par âge](#1-carte-des-mods-par-âge)
2. [Items clés par mod](#2-items-clés-par-mod)
3. [Patterns de recettes E2E](#3-patterns-de-recettes-e2e)
4. [Chaînes d'automatisation](#4-chaînes-dautomatisation)
5. [Matrice d'interconnexion des mods](#5-matrice-dinterconnexion)
6. [Recettes planifiées Nexus Absolu](#6-recettes-planifiées)
7. [Items custom nécessaires](#7-items-custom)
8. [Checklist d'automatisabilité](#8-checklist-automatisabilité)

---

## 1. CARTE DES MODS PAR ÂGE

### Âge 0 — L'Éveil (3x3→9x9 Compact Machine)
| Mod | Rôle | Déblocage |
|-----|------|-----------|
| Ex Nihilo Creatio | Source de toutes les ressources (tamis) | Départ |
| Tinkers Construct | Fonderie, outils modulaires | Après tamis |
| Bonsai Trees | Bois automatique | Après dirt |
| Pam's HarvestCraft | Nourriture variée | Après dirt+graines |
| Spice of Life Carrot | Récompense variété alimentaire (+cœurs) | Passif |
| Crop Dusting | Poop = engrais | Après animaux |
| Soul Shards Respawn | Mob farm automatique | Après kill mobs |
| Storage Drawers | Organisation | Mid Âge 0 |
| Compact Machines 3 | Le monde lui-même | Permanent |
| Construct's Armory | Armures modulaires Tinkers | Après Smeltery |

### Âge 1 — La Mécanique Brute (9x9→13x13)
| Mod | Rôle | Prérequis |
|-----|------|-----------|
| Thermal Foundation | Métaux, alliages (Invar, Electrum, Enderium) | Tamis fer/cuivre |
| Thermal Expansion | Machines RF (Pulverizer, Smelter, etc.) | TF + premier RF |
| Thermal Dynamics | Transport énergie/fluides (Fluxducts) | TE |
| EnderIO Base | Conduits multifonction, alliages | Acier + Silicium |
| EnderIO Conduits | Transport items/fluides/RF en 1 bloc | EIO Base |
| Immersive Engineering | Coke Oven, Blast Furnace, Acier | Briques |
| Actually Additions | Atomic Reconstructor, cristaux, générateurs | RF basique |
| Simply Jetpacks 2 | Vol tier 1-4 | Composants Thermal |
| Cyclic | Utilitaires variés | RF basique |

### Âge 2 — Le Paradoxe Organique (13x13→17x17)
| Mod | Rôle | Prérequis |
|-----|------|-----------|
| Botania | Mana, fleurs, Terrasteel | Fer + Mystical Flowers |
| Astral Sorcery | Starlight, cristaux, altar | Marbre + ciel (tunnel) |
| Blood Magic | Life Points, altar tiers, rituels | Diamants + fer |
| Mystical Agriculture | Graines de ressources (tier 1-6) | Inferium + Prudentium |
| Roots 2 | (si ajouté) Herbalisme | Plantes |

### Âge 3 — La Chimie du Chaos (monde ouvert)
| Mod | Rôle | Prérequis |
|-----|------|-----------|
| Mekanism | Ore processing x2→x5, gaz, fusion | Osmium + Acier |
| PneumaticCraft | Pression, drones, pétrole | Fer + compresseur |
| Industrial Foregoing | Mob farm avancé, tree farm | Plastic |

### Âge 4 — L'Intelligence des Réseaux
| Mod | Rôle | Prérequis |
|-----|------|-----------|
| AE2 (UEL) | Stockage ME, autocrafting | Certus Quartz + processeurs |
| OpenComputers | Programmation LUA | Circuits |
| XNet | Réseau logique compact | Composants |
| Modular Machinery | Machines custom multiblocs | Tous les alliages |

### Âge 5 — La Fission de l'Impossible
| Mod | Rôle | Prérequis |
|-----|------|-----------|
| NuclearCraft | Réacteurs, isotopes, fusion | Uranium, Thorium |
| Environmental Tech | Void miners, solar arrays | Litherite |
| Solar Flux Reborn | Panneaux solaires tier 1-8 | Basique→NuclearCraft |
| Extreme Reactors | Réacteurs Yellorium | Yellorium |

### Âge 6 — La Conquête du Vide
| Mod | Rôle | Prérequis |
|-----|------|-----------|
| Galacticraft | Lune, Mars, fusée | Acier + beaucoup de ressources |
| Advanced Rocketry | Stations orbitales, planètes custom | Galacticraft gate |
| Extra Planets | Planètes supplémentaires | Galacticraft |
| RFTools Dimensions | Dimensions custom | Dimlets |

### Âge 7 — La Synthèse des Mondes
| Mod | Rôle | Prérequis |
|-----|------|-----------|
| Twilight Forest | Dimension aventure, boss, matériaux | Portal |
| Blood Magic avancé | Tier 5-6, Well of Suffering | LP massif |
| Draconic Evolution | Énergie tier 1-2, outils draconiques | Draconium |
| Ice and Fire | Dragons, matériaux rares | Exploration |

### Âge 8 — L'Endgame des Dieux
| Mod | Rôle | Prérequis |
|-----|------|-----------|
| Draconic Evolution max | Tier 3, réacteur, armure | Awakened Draconium |
| Avaritia | Neutronium, Infinity Ingot | Neutron Collector |
| Extended Crafting | Tables 3x3→9x9 | Black Iron |

### Âge 9 — Le Nexus Absolu
| Mod | Rôle | Prérequis |
|-----|------|-----------|
| Extended Crafting 9x9 | Table finale | Tous les fragments |
| ContentTweaker | Items custom du Nexus | Tout le modpack |

---

## 2. ITEMS CLÉS PAR MOD

### Thermal Foundation — Métaux et alliages
```
thermalfoundation:material
  Damage 128 = Copper Ingot       | Damage 64  = Copper Dust
  Damage 129 = Tin Ingot           | Damage 65  = Tin Dust
  Damage 130 = Silver Ingot        | Damage 66  = Silver Dust
  Damage 131 = Lead Ingot          | Damage 67  = Lead Dust
  Damage 132 = Aluminum Ingot      | Damage 68  = Aluminum Dust
  Damage 133 = Nickel Ingot        | Damage 69  = Nickel Dust
  Damage 134 = Platinum Ingot      | Damage 70  = Platinum Dust
  Damage 160 = Steel Ingot         | Damage 96  = Steel Dust
  Damage 161 = Electrum Ingot      | Damage 97  = Electrum Dust
  Damage 162 = Invar Ingot         | Damage 98  = Invar Dust
  Damage 163 = Bronze Ingot        | Damage 99  = Bronze Dust
  Damage 164 = Constantan Ingot    | Damage 100 = Constantan Dust
  Damage 165 = Signalum Ingot      | Damage 101 = Signalum Dust
  Damage 166 = Lumium Ingot        | Damage 102 = Lumium Dust
  Damage 167 = Enderium Ingot      | Damage 103 = Enderium Dust

Alliages (Induction Smelter) :
  Bronze    = Copper (3) + Tin (1)
  Electrum  = Gold (1) + Silver (1)
  Invar     = Iron (2) + Nickel (1)
  Constantan = Copper (1) + Nickel (1)
  Signalum  = Copper (3) + Silver (1) + Redstone (10) [liquide]
  Lumium    = Tin (3) + Silver (1) + Glowstone (4) [liquide]
  Enderium  = Tin (2) + Silver (1) + Lead (1) + Ender Pearl (4) [liquide]

Gears : ore:gearCopper, ore:gearIron, ore:gearInvar, ore:gearSignalum, etc.
Plates : ore:plateCopper, ore:plateIron, ore:plateSteel, etc.
```

### Thermal Expansion — Machines
```
thermalexpansion:machine
  Damage 0  = Redstone Furnace     (cuisson)
  Damage 1  = Pulverizer           (broyage x2)
  Damage 2  = Sawmill              (sciage bois)
  Damage 3  = Induction Smelter    (alliages)
  Damage 4  = Phytogenic Insolator (farming)
  Damage 5  = Compactor            (compression)
  Damage 6  = Magma Crucible       (liquéfaction)
  Damage 7  = Fluid Transposer     (remplissage/vidage)
  Damage 8  = Energetic Infuser    (charge RF)
  Damage 9  = Centrifugal Separator
  Damage 10 = Sequential Fabricator (autocraft)
  Damage 14 = Fractionating Still   (distillation)

thermalexpansion:dynamo
  Damage 0 = Steam Dynamo          (charbon → RF)
  Damage 1 = Magmatic Dynamo       (lave → RF)
  Damage 2 = Compression Dynamo    (fuel → RF)
  Damage 3 = Reactant Dynamo
  Damage 4 = Enervation Dynamo     (redstone → RF)
  Damage 5 = Numismatic Dynamo     (gemmes → RF)

Frames (base des machines) :
  thermalexpansion:frame     = Machine Frame (basic)
  thermalexpansion:frame:64  = Device Frame
  thermalexpansion:frame:128 = Cell Frame (energy storage)

Upgrades :
  thermalfoundation:upgrade  Damage 0=Hardened, 1=Reinforced, 2=Signalum, 3=Resonant
```

### Mekanism — Processing chain
```
ORE PROCESSING TIERS :
  x2 = Enrichment Chamber (ore → 2 dust → smelt)
  x3 = Purification Chamber (ore + O2 → 3 clump → crush → dust → smelt)
  x4 = Chemical Injection (ore + HCl → 4 shard → purify → clump → crush → dust → smelt)
  x5 = Chemical Dissolution (ore + H2SO4 → slurry → crystallize → shard → purify → clump → crush → dust → smelt)

MACHINES CLÉS :
  mekanism:machineblock      Damage 0  = Enrichment Chamber
  mekanism:machineblock      Damage 3  = Crusher
  mekanism:machineblock      Damage 8  = Metallurgic Infuser
  mekanism:machineblock      Damage 9  = Purification Chamber
  mekanism:machineblock      Damage 12 = Digital Miner
  mekanism:machineblock2     Damage 1  = Chemical Oxidizer
  mekanism:machineblock2     Damage 2  = Chemical Infuser
  mekanism:machineblock2     Damage 3  = Chemical Injection Chamber
  mekanism:machineblock2     Damage 4  = Electrolytic Separator
  mekanism:machineblock2     Damage 5  = Precision Sawmill
  mekanism:machineblock2     Damage 6  = Chemical Dissolution Chamber
  mekanism:machineblock2     Damage 7  = Chemical Washer
  mekanism:machineblock2     Damage 8  = Chemical Crystallizer
  mekanism:machineblock2     Damage 11 = Rotary Condensentrator

MATÉRIAUX :
  Osmium Ingot = mekanism:ingot (Damage 1)
  Circuits : Basic=ore:circuitBasic, Advanced=ore:circuitAdvanced, Elite=ore:circuitElite, Ultimate=ore:circuitUltimate
  Steel Casing = mekanism:basicblock:8
  Alloys : Infused=ore:alloyInfused, Reinforced=ore:alloyReinforced, Atomic=ore:alloyAtomic
```

### EnderIO — Alliages et conduits
```
ALLIAGES (enderio:item_alloy_ingot) :
  Damage 0 = Electrical Steel    (Iron + Silicon + Coal)
  Damage 1 = Energetic Alloy     (Gold + Redstone + Glowstone)
  Damage 2 = Vibrant Alloy       (Energetic + Ender Pearl)
  Damage 3 = Redstone Alloy      (Redstone + Silicon)
  Damage 4 = Conductive Iron     (Iron + Redstone)
  Damage 5 = Pulsating Iron      (Iron + Ender Pearl)
  Damage 6 = Dark Steel          (Iron + Coal + Obsidian)
  Damage 7 = Soularium           (Gold + Soul Sand)
  Damage 8 = End Steel           (Dark Steel + Obsidian + End Stone)
  Damage 9 = Construction Alloy  (Iron + Quartz Gravel)

CONDUITS :
  enderio:item_power_conduit     (energy, 3 tiers)
  enderio:item_item_conduit      (items)
  enderio:item_liquid_conduit    (fluids, 3 tiers)
  enderio:item_redstone_conduit  (redstone)
  enderio:item_me_conduit        (AE2 channels)

MACHINES :
  enderio:block_alloy_smelter           (alliages)
  enderio:block_sag_mill                (broyage)
  enderio:block_slice_and_splice        (composants zombie)
  enderio:block_soul_binder             (âmes)
  enderio:block_vat                     (fluides spéciaux)
```

### Immersive Engineering — Multiblocs
```
MATÉRIAUX :
  immersiveengineering:material
    Damage 0  = Treated Wood Stick
    Damage 1  = Iron Mechanical Component
    Damage 2  = Steel Mechanical Component
    Damage 6  = Coal Coke
    Damage 8  = Hemp Fiber
    Damage 9  = Hemp Fabric

  immersiveengineering:treated_wood (Treated Wood Planks)
  immersiveengineering:stone_decoration
    Damage 0  = Coke Brick
    Damage 1  = Coke Oven (multibloc controller)
    Damage 2  = Blast Furnace (multibloc controller)
    Damage 3  = Coke Block (9x Coal Coke)

MULTIBLOCS :
  Coke Oven     : 3x3x3 (27 Coke Bricks) — charbon → coal coke + creosote
  Blast Furnace : 3x3x3 (27 Blast Bricks) — fer → acier
  Crusher       : 3x5x3 (trop gros pour compact machine <13x13)
  Arc Furnace   : 5x5x5 (monde ouvert uniquement)

CONNECTORS :
  immersiveengineering:connector (LV/MV/HV connectors et relays)
  immersiveengineering:wirecoil  (copper/electrum/steel wire)
```

### AE2 (Applied Energistics 2 UEL)
```
COMPOSANTS CLÉS :
  appliedenergistics2:material
    Damage 0  = Certus Quartz Crystal
    Damage 1  = Charged Certus Quartz
    Damage 7  = Engineering Processor
    Damage 10 = Fluix Crystal
    Damage 13 = Logic Processor Press
    Damage 14 = Calculation Processor Press
    Damage 15 = Engineering Processor Press
    Damage 16 = Printed Logic Circuit
    Damage 17 = Printed Calculation Circuit
    Damage 18 = Printed Engineering Circuit
    Damage 20 = Pure Certus Quartz
    Damage 22 = Logic Processor
    Damage 23 = Calculation Processor
    Damage 24 = Engineering Processor
    Damage 35 = 1k Storage Component
    Damage 36 = 4k Storage Component
    Damage 37 = 16k Storage Component
    Damage 38 = 64k Storage Component
    Damage 43 = Wireless Receiver
    Damage 44 = Wireless Booster

MACHINES :
  appliedenergistics2:controller        (ME Controller)
  appliedenergistics2:drive             (ME Drive)
  appliedenergistics2:crafting_unit     (Crafting CPU)
  appliedenergistics2:molecular_assembler (Autocrafting)
  appliedenergistics2:inscriber         (Processor crafting)
  appliedenergistics2:charger           (Charge certus quartz)
  appliedenergistics2:grindstone        (Grindstone)

CHANNELS : 32 normal (configué x4), 128 dense (configué x4)
```

### Botania
```
MATÉRIAUX :
  botania:manaresource
    Damage 0  = Manasteel Ingot
    Damage 1  = Mana Pearl
    Damage 2  = Mana Diamond
    Damage 4  = Terrasteel Ingot
    Damage 7  = Elementium Ingot
    Damage 8  = Pixie Dust
    Damage 9  = Dragonstone
    Damage 14 = Gaia Spirit
    Damage 15 = Gaia Spirit Ingot

CLÉS :
  Mana Pool (botania:pool) — centre de tout
  Runic Altar (botania:runeAltar) — runes
  Terrasteel — nécessite Mana Pool + fer + mana pearl + mana diamond
  Elementium — nécessite Elven Gateway
  Gaia Spirit — nécessite Gaia Guardian boss fight

AUTOMATISATION :
  Hopperhock (collecte) + Open Crate (input) + Mana Spreader (transfer mana)
  100% automatisable avec redstone
```

### Astral Sorcery
```
MATÉRIAUX :
  astralsorcery:itemcraftingcomponent
    Damage 0  = Aquamarine
    Damage 1  = Starmetal Ingot
    Damage 2  = Stardust
    Damage 3  = Glass Lens
    Damage 4  = Celestial Crystal

PROGRESSION :
  1. Luminous Crafting Table (crafting basique AS)
  2. Starlight Altar (tier 2)
  3. Celestial Altar (tier 3)
  4. Iridescent Altar (tier 4)
  5. Ritual Pedestal (rituels)

NOTE : Nécessite vue du ciel. En Compact Machine, il faudra un Tunnel
ou un Compact Machine spécial configuré en dimension avec ciel.
```

### Blood Magic
```
PROGRESSION :
  Tier 1 = Altar basique (2000 LP)
  Tier 2 = 8 Blood Runes (5000 LP)
  Tier 3 = 20 Blood Runes (20000 LP)
  Tier 4 = 28 Blood Runes (40000 LP)
  Tier 5 = 56 Blood Runes (80000 LP)
  Tier 6 = 84 Blood Runes (200000 LP)

ITEMS CLÉS :
  bloodmagic:blood_orb (tier 1-5)
  bloodmagic:slate (Blank, Reinforced, Imbued, Demonic, Ethereal)
  bloodmagic:sentient_sword

AUTOMATISATION LP :
  Well of Suffering ritual = LP infini (tue mobs auto)
  Incense Altar = bonus LP crafting
```

### NuclearCraft
```
COMBUSTIBLES :
  nuclearcraft:ingot (Thorium, Uranium, Plutonium)
  nuclearcraft:ingot_oxide (oxydes pour réacteur)
  nuclearcraft:fuel_thorium, fuel_uranium, fuel_plutonium

MACHINES :
  nuclearcraft:manufactory (crushing)
  nuclearcraft:isotope_separator
  nuclearcraft:decay_hastener
  nuclearcraft:fuel_reprocessor
  nuclearcraft:fission_controller_new_fixed (réacteur)
  nuclearcraft:fusion_core (fusion — endgame)

COOLANTS : eau, helium, sodium, lithium (tous automatisables)
```

### Draconic Evolution
```
TIERS :
  Wyvern (tier 1) — draconium ingot + nether star
  Draconic (tier 2) — awakened draconium + dragon heart
  Chaotic (tier 3, mod Draconic Additions)

ITEMS CLÉS :
  draconicevolution:draconium_ingot
  draconicevolution:draconic_ingot (awakened)
  draconicevolution:dragon_heart
  draconicevolution:chaos_shard

MACHINES :
  draconicevolution:fusion_crafting_core (Fusion Crafting)
  draconicevolution:energy_storage_core (stockage massif)
  draconicevolution:reactor_core (reactor — risque explosion)
```

### Avaritia
```
MATÉRIAUX :
  avaritia:resource
    Damage 1 = Diamond Lattice
    Damage 2 = Crystal Matrix Ingot
    Damage 3 = Neutronium Nugget
    Damage 4 = Neutronium Ingot
    Damage 5 = Cosmic Neutronium Ingot
    Damage 6 = Infinity Ingot

MACHINES :
  avaritia:neutron_collector (très lent, produit Neutronium)
  avaritia:compressor (compression massive)

NOTE E2E : L'Infinity Ingot se craft dans la table Extended Crafting 9x9,
PAS dans la table Avaritia (qui est supprimée).
```

---

## 3. PATTERNS DE RECETTES E2E — Ce qui marche

### Pattern 1 : "Machine A nécessite sous-produit de Machine B"
```
EXEMPLE E2E — Pulverizer :
  Recette vanilla: Machine Frame + pistons + flint
  Recette E2E:    Machine Frame + AA Grinder + Tinkers Large Plate (flint) + pistons
  → Force: Actually Additions + Tinkers Construct

POUR NEXUS ABSOLU :
  Pulverizer = Machine Frame + IE Crusher component + Tinkers Large Plate
  → Force: IE + Tinkers avant Thermal
```

### Pattern 2 : "Alliage nécessite machine d'un autre mod"
```
EXEMPLE E2E — Electrical Steel :
  Vanilla EnderIO: fer + silicium + charbon dans Alloy Smelter
  E2E: fer + silicium + charbon dans Induction Smelter Thermal
  → Force: Thermal Expansion pour accéder à EnderIO

POUR NEXUS ABSOLU :
  Electrical Steel = Induction Smelter (Iron + Silicon + Steel dust)
  → Force: Thermal + IE (acier) pour EnderIO
```

### Pattern 3 : "Cable/conduit nécessite alliage d'un autre mod"
```
EXEMPLE E2E — Mekanism cables :
  Vanilla: Steel + Osmium
  E2E: Electrical Steel (EnderIO) + composant spécifique
  → Force: EnderIO avant Mekanism conduits

POUR NEXUS ABSOLU :
  Mekanism Universal Cable = Electrical Steel + Copper Wire (IE) + Osmium
  → Force: EnderIO + IE + Mekanism
```

### Pattern 4 : "Upgrade nécessite composant magique"
```
EXEMPLE E2E — Draconic Evolution :
  Wyvern Core = Draconium + Nether Star + NuclearCraft isotope
  → Force: NuclearCraft avant Draconic

POUR NEXUS ABSOLU :
  Wyvern Core = Draconium + Nether Star + Starmetal (Astral) + Blood Shard (BM)
  → Force: Magie + Tech convergent
```

### Pattern 5 : "Fuel/énergie gate par un autre mod"
```
EXEMPLE E2E — Solar Flux :
  Tier 5+ nécessite NuclearCraft isotopes
  → Force: nucléaire avant solaire passif

POUR NEXUS ABSOLU : IDENTIQUE
  Solar Flux 5+ = panneaux + NuclearCraft Thorium cell
```

---

## 4. CHAÎNES D'AUTOMATISATION

### Chaîne Ore Processing (Âge 1 → Âge 3)
```
Âge 0:  Tamis Ex Nihilo → grits → Smeltery Tinkers → lingots (x1)
Âge 1:  Pulverizer Thermal → dust x2 → Redstone Furnace → lingots (x2)
Âge 3:  Mekanism Enrichment → dust x2 (idem mais plus rapide)
Âge 3+: Mekanism x3 = Purification + Crush + Smelt
Âge 3+: Mekanism x4 = Injection + Purification + Crush + Smelt
Âge 3+: Mekanism x5 = Dissolution + Washing + Crystallize + Inject + Purify + Crush + Smelt

Chaque tier est automatisable avec EnderIO conduits ou Mekanism pipes.
```

### Chaîne Énergie (progression)
```
Âge 0:  Aucune (manuel)
Âge 1:  Steam Dynamo (charbon) → 40 RF/t
Âge 1:  Magmatic Dynamo (lave) → 80 RF/t (nécessite Magma Crucible)
Âge 1:  IE Water Wheel → 12 RF/t (gratuit)
Âge 2:  Mana → RF via Mana Fluxfield (Botania)
Âge 3:  Mekanism Gas Generators → 400+ RF/t
Âge 3:  Immersive Petroleum → Compression Dynamo → 500 RF/t
Âge 5:  NuclearCraft Fission → 2000+ RF/t
Âge 5:  Extreme Reactors → variable
Âge 7:  Draconic Reactor → 100k+ RF/t
```

### Chaîne Vol
```
Âge 1:  Simply Jetpacks tier 1 (charbon, lent)
Âge 1:  Simply Jetpacks tier 2 (RF, correct)
Âge 2:  Simply Jetpacks tier 3-4 (rapide)
Âge 2:  Angel Ring to Bauble (vol créatif, nécessite jetpack T4 comme composant)
Âge 7:  Draconic Armor (vol + invincibilité, nécessite Angel Ring comme composant)
```

### Chaîne Stockage
```
Âge 0:  Coffres vanilla + Storage Drawers
Âge 1:  Iron Chest (upgrades)
Âge 1:  Drawer Controller (auto-sort)
Âge 4:  AE2 ME System (1k → 4k → 16k → 64k cells)
Âge 4:  AE2 Autocrafting
Âge 8:  AE2 Creative Cell (objectif endgame)
```

---

## 5. MATRICE D'INTERCONNEXION DES MODS

### Comment chaque mod DONNE à un autre
```
THERMAL → fournit alliages (Invar, Electrum, Enderium) à TOUS les mods
THERMAL → fournit machines de processing à IE, EnderIO, Mekanism
IE → fournit Acier à Thermal, Mekanism, AE2, Galacticraft
IE → fournit Coal Coke/Creosote à Thermal
EnderIO → fournit Electrical Steel à Mekanism (câbles)
EnderIO → fournit Dark Steel à Draconic (composants)
EnderIO → fournit conduits à TOUS les mods (logistique)
MEKANISM → fournit Osmium/circuits à AE2, NuclearCraft
MEKANISM → fournit gaz processing à NuclearCraft
AE2 → fournit autocrafting à TOUS les mods (automatisation)
AE2 → fournit processeurs à RFTools, Modular Machinery
NUCLEARCRAFT → fournit isotopes à Solar Flux, Draconic, Galacticraft
NUCLEARCRAFT → fournit coolants à Mekanism (fusion)
BOTANIA → fournit Mana/Terrasteel à Blood Magic, Astral
BOTANIA → fournit Elementium à Extended Crafting
ASTRAL → fournit cristaux/Starmetal à Thermal (augments), AE2, Blood Magic
BLOOD MAGIC → fournit Slates/orbs à Botania, Astral, Draconic
GALACTICRAFT → fournit matériaux spatiaux à Advanced Rocketry
DRACONIC → fournit Draconium/cores à Avaritia, Extended Crafting
AVARITIA → fournit Neutronium/Infinity à Extended Crafting (Nexus)
TWILIGHT FOREST → fournit matériaux uniques (Fiery, Knightmetal, Ironwood)
```

### Comment chaque mod REÇOIT d'un autre
```
THERMAL ← IE (acier), Tinkers (composants), AA (cristaux)
MEKANISM ← EnderIO (Electrical Steel), IE (acier), Thermal (alliages)
EnderIO ← Thermal (Induction Smelter pour alliages), IE (acier)
AE2 ← Mekanism (osmium), Astral (cristaux), EnderIO (conduits ME)
NUCLEARCRAFT ← Mekanism (processing), Thermal (machines), IE (acier)
BOTANIA ← Thermal (Mana infusion de métaux), Ex Nihilo (ressources)
ASTRAL ← Thermal (machines améliorées), Botania (mana)
BLOOD MAGIC ← Botania (Terrasteel altar), Astral (infusion)
DRACONIC ← NuclearCraft (isotopes), Blood Magic (slates), Astral (starmetal)
GALACTICRAFT ← Thermal (alliages), Mekanism (gaz), IE (acier)
AVARITIA ← Draconic (cores), NuclearCraft (neutronium), ALL mods (singularities)
```

---

## 6. RECETTES PLANIFIÉES NEXUS ABSOLU

### Principe : "Pas E2E hard, mais pas vanilla non plus"
- On modifie ~80% des recettes des machines principales
- On garde les recettes vanilla pour les items basiques (crafting table, sticks, etc.)
- Chaque machine importante nécessite 2-3 mods
- Les items de gate (clés, fragments) nécessitent des chaînes complètes

### Âge 0 — Scripts nécessaires
```zenscript
// Bloquer le four vanilla
furnace.removeAll(); // puis ré-ajouter uniquement ce qu'on veut
// ou recipes.remove(<minecraft:furnace>);

// Grits des murs → fonderie Tinkers
// wall_dust → cobble (compressé)
// iron_grit → fer dans Smeltery
// copper_grit → cuivre dans Smeltery

// Recette Compact Key 5x5
recipes.addShaped("compact_key_5x5", <contenttweaker:compact_key_5x5>,
  [[<minecraft:iron_ingot>, <ore:ingotBronze>, <minecraft:iron_ingot>],
   [<ore:ingotCopper>, <minecraft:nether_star>, <ore:ingotCopper>],
   [<minecraft:iron_ingot>, <ore:ingotBronze>, <minecraft:iron_ingot>]]);
// Note: nether_star trop cher pour Âge 0, remplacer par item custom ou rare
```

### Âge 1 — Recettes inter-mods planifiées
```zenscript
// Pulverizer = nécessite composant IE
remake("nexus_pulverizer", <thermalexpansion:machine:1>,
  [[<ore:plateIron>, <immersiveengineering:material:1>, <ore:plateIron>],
   [<ore:gearCopper>, <thermalexpansion:frame>, <ore:gearCopper>],
   [<ore:plateIron>, <thermalfoundation:material:513>, <ore:plateIron>]]);

// Induction Smelter = nécessite composant EnderIO
remake("nexus_smelter", <thermalexpansion:machine:3>,
  [[<ore:plateInvar>, <enderio:item_alloy_ingot:3>, <ore:plateInvar>],
   [<ore:gearBronze>, <thermalexpansion:frame>, <ore:gearBronze>],
   [<ore:plateInvar>, <thermalfoundation:material:513>, <ore:plateInvar>]]);

// EnderIO Alloy Smelter = nécessite Invar (Thermal)
remake("nexus_alloy_smelter", <enderio:block_alloy_smelter>,
  [[<ore:ingotInvar>, <minecraft:furnace>, <ore:ingotInvar>],
   [<ore:ingotInvar>, <enderio:item_material:2>, <ore:ingotInvar>],
   [<ore:gearIron>, <ore:ingotDarkSteel>, <ore:gearIron>]]);

// Steam Dynamo = vanilla-ish (première source RF)
// PAS de modification (accessible facilement)

// Magmatic Dynamo = nécessite Invar + composant IE
remake("nexus_magmatic", <thermalexpansion:dynamo:1>,
  [[null, <immersiveengineering:material:2>, null],
   [<ore:ingotInvar>, <ore:gearCopper>, <ore:ingotInvar>],
   [<ore:ingotInvar>, <thermalfoundation:material:513>, <ore:ingotInvar>]]);
```

### Âge 2 — Liens Tech/Magie
```zenscript
// Fragment Organique = Botania + Blood Magic
// Terrasteel + Blood Orb T3 + craft table Astral
mods.extendedcrafting.TableCrafting.addShaped(0, <contenttweaker:fragment_organique>,
  [[<botania:manaresource:4>, <bloodmagic:slate:2>, <botania:manaresource:4>],
   [<astralsorcery:itemcraftingcomponent:1>, <minecraft:nether_star>, <astralsorcery:itemcraftingcomponent:1>],
   [<botania:manaresource:4>, <bloodmagic:slate:2>, <botania:manaresource:4>]]);

// Fragment Stellaire = Astral Sorcery dominante
// Starlight infusion d'un cristal céleste + composants Botania + Thermal
```

---

## 7. ITEMS CUSTOM NÉCESSAIRES

### Déjà créés (ContentTweaker)
```
GRITS (9):        wall_dust, iron_grit, copper_grit, tin_grit, silver_grit,
                  nickel_grit, lead_grit, gold_grit, osmium_grit
CLÉS (6):         compact_key_5x5/7x7/9x9/11x11/13x13, lab_key
COMPOSANTS (3):   infused_circuit, resonant_coil, organic_catalyst
FRAGMENTS (9):    fragment_mecanique, fragment_organique, fragment_stellaire,
                  compose_x77, coeur_de_donnees, noyau_fissile,
                  fragment_espace_temps, codex_transcendant, prisme_transcendance
FINAL (1):        nexus_absolu
```

### À peut-être ajouter
```
compact_machine_dust    — drop des murs (si Ex Nihilo ne suffit pas)
purified_iron           — étape intermédiaire ore processing custom
enriched_copper         — idem
machine_core_basic      — composant partagé entre Thermal/EnderIO/Mek
machine_core_advanced   — idem tier 2
machine_core_elite      — idem tier 3
starlight_crystal       — composant Astral Sorcery custom
blood_crystal           — composant Blood Magic custom
mana_crystal            — composant Botania custom
```

---

## 8. CHECKLIST D'AUTOMATISABILITÉ

### Règle d'or : tout doit pouvoir être automatisé au moment où le joueur en a besoin

| Ressource | Source Âge 0 | Source Âge 1+ | Auto possible ? |
|-----------|-------------|---------------|-----------------|
| Cobblestone | Murs compact | Igneous Extruder | ✅ Oui |
| Fer | Tamis gravel | Pulverizer + ores | ✅ Oui |
| Cuivre | Tamis sand | Pulverizer + ores | ✅ Oui |
| Étain | Tamis sand | Pulverizer + ores | ✅ Oui |
| Or | Tamis gravel | Pulverizer + ores | ✅ Oui |
| Argent | Tamis sand (mesh fer) | Pulverizer + ores | ✅ Oui |
| Nickel | Tamis sand (mesh fer) | Pulverizer + ores | ✅ Oui |
| Plomb | Tamis sand (mesh fer) | Pulverizer + ores | ✅ Oui |
| Osmium | Tamis dust (mesh diamant) | Mekanism ores | ✅ Oui |
| Redstone | Tamis dust | Mining/Mystical Agri | ✅ Oui |
| Diamant | Tamis gravel (mesh dia) | Mining/Mystical Agri | ✅ Oui |
| Lave | Magma Crucible (cobble) | Nether/Crucible | ✅ Oui |
| Eau | Tonneau Ex Nihilo | Aqueous Accumulator | ✅ Oui |
| Bois | Bonsai Trees | Bonsai/Tree Farm | ✅ Oui |
| Mana (Botania) | Endoflame (charbon) | Kekimurus/automation | ✅ Oui |
| Starlight (AS) | Collecteur cristal | Starlight Well + ritual | ✅ Oui |
| LP (Blood Magic) | Self-sacrifice | Well of Suffering | ✅ Oui |
| RF | Steam Dynamo | Dynamos/Reactors | ✅ Oui |
| EMC/XP | Mob farm Soul Shards | Woot/Mob Duplicator | ✅ Oui |

### Goulots d'étranglement à surveiller
```
1. Astral Sorcery en Compact Machine → besoin de ciel
   SOLUTION : configurer dimension Compact Machines avec weakSkyRenders
   ou ajouter un item custom "Skylight Crystal" qui simule le ciel

2. Blood Magic altar taille → tier 3+ prend beaucoup de place
   SOLUTION : les compact machines 13x13 suffisent pour tier 4
   Tier 5-6 = monde ouvert (Âge 3+)

3. IE Crusher = gros multibloc
   SOLUTION : repousser à Âge 3 (monde ouvert)
   En compact, utiliser Pulverizer Thermal

4. Mekanism x5 processing = 7 machines en chaîne
   SOLUTION : nécessite salle 13x13 minimum, ou monde ouvert
   Quête guide la disposition

5. AE2 autocrafting = beaucoup de channels
   SOLUTION : channels x4 déjà configuré (32/128)
```

---

## 9. NOTES DE DESIGN

### "Moins hard qu'E2E" signifie concrètement :
- Les recettes basiques (outils, blocs déco, items simples) restent VANILLA
- Les MACHINES sont modifiées pour nécessiter 2-3 mods
- Les UPGRADES/TIERS SUPÉRIEURS sont modifiés plus fortement
- Les items de GATE (clés, fragments) sont les plus complexes
- Le joueur peut explorer librement mais doit toucher à tout pour progresser

### Ratio approximatif :
- 20% des recettes = vanilla (inchangées)
- 50% des recettes = légèrement modifiées (1 composant d'un autre mod ajouté)
- 25% des recettes = fortement modifiées (2-3 mods requis)
- 5% des recettes = custom total (fragments, Nexus, items uniques)

---

*Document généré le 26 Mars 2026 — Basé sur E2E, CC, ATM10*
