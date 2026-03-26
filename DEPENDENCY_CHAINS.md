# DEPENDENCY_CHAINS.md — Chaînes de dépendances globales
> Vue d'ensemble de TOUT ce qui dépend de quoi. Chaque recette modifiée a des conséquences.
> Ce document est la CARTE MÈRE du modpack. On le consulte AVANT d'écrire un script.

---

## PHILOSOPHIE

**Règle 1 :** Avant de modifier une recette, vérifier TOUTES ses dépendances en aval.
**Règle 2 :** Un item modifié ne doit JAMAIS créer un deadlock (impossible à crafter).
**Règle 3 :** Chaque recette modifiée nécessite au moins 2 mods mais MAX 3-4 (pas 7).
**Règle 4 :** Moins hard qu'E2E = on utilise des circuits basiques/avancés, pas ultimate partout.

---

## 1. L'ARBRE DE DÉPENDANCES — Vue globale

```
NIVEAU 0 — Matières premières (disponibles naturellement)
│
├── Cobblestone, Dirt, Gravel, Sand (Compact Machine murs / Overworld)
├── Minerais : Iron, Copper, Tin, Silver, Nickel, Lead, Gold, Osmium
├── Gemmes : Diamond, Emerald, Lapis, Redstone, Quartz
├── Bois (Bonsai Trees / arbres normaux)
├── Mobs (drops: Ender Pearls, Blaze Rods, etc.)
└── Fluides : Eau, Lave

NIVEAU 1 — Matériaux transformés de base
│
├── Lingots basiques (Smeltery Tinkers / Furnace)
├── Acier (IE Blast Furnace) ← GATE: Âge 1+
├── Bronze (Smeltery: 3 Copper + 1 Tin)
├── Electrum (Smeltery ou Induction Smelter: Gold + Silver)
├── Invar (Induction Smelter: 2 Iron + 1 Nickel)
├── Constantan (Induction Smelter: Copper + Nickel)
├── Silicon (Pulverizer → Quartz, ou EnderIO grind)
└── Coal Coke (IE Coke Oven: Coal → Coal Coke + Creosote)

NIVEAU 2 — Composants intermédiaires
│
├── THERMAL
│   ├── Machine Frame (thermalexpansion:frame) ← Tin + Iron + Glass
│   ├── Servo (thermalfoundation:material:512-515) ← Iron→Signalum
│   ├── Gears (ore:gearCopper, gearIron, etc.)
│   ├── Plates (ore:plateIron, plateSteel, etc.)
│   ├── Signalum (Copper 3 + Silver 1 + Redstone 10) ← Âge 2+
│   ├── Lumium (Tin 3 + Silver 1 + Glowstone 4) ← Nether requis
│   └── Enderium (Tin 2 + Silver 1 + Lead 1 + Ender Pearl 4) ← End Pearls
│
├── ENDERIO
│   ├── Simple Machine Chassis (enderio:item_material) ← Iron + Bars
│   ├── Electrical Steel (Iron + Silicon + Coal) ← Alloy Smelter OU Induction
│   ├── Energetic Alloy (Gold + Redstone + Glowstone) ← Nether requis
│   ├── Vibrant Alloy (Energetic + Ender Pearl) ← End Pearls
│   ├── Dark Steel (Iron + Coal + Obsidian)
│   ├── Pulsating Iron (Iron + Ender Pearl) ← Ender Pearls
│   └── Soularium (Gold + Soul Sand) ← Nether requis
│
├── IMMERSIVE ENGINEERING
│   ├── Treated Wood (planks + Creosote Oil)
│   ├── Iron Mechanical Component ← Iron + planks
│   ├── Steel Mechanical Component ← Steel + planks
│   ├── Coke Brick ← Clay + Sand + brick
│   ├── Blast Brick ← Nether Brick + Brick
│   ├── HV Capacitor ← Steel + Lead + Electrum
│   └── Engineering Block ← Steel + Iron + Redstone
│
├── MEKANISM
│   ├── Steel Casing (mekanism:basicblock:8) ← Steel + Osmium + Glass
│   ├── Basic Circuit ← Alloy Infused + Osmium
│   ├── Advanced Circuit ← Basic + Alloy Reinforced + Infused Alloy
│   ├── Elite Circuit ← Advanced + Alloy Atomic + Reinforced Alloy
│   ├── Ultimate Circuit ← Elite + Atomic Alloy
│   ├── Infused Alloy (Metallurgic Infuser: Iron + Redstone)
│   ├── Reinforced Alloy (Metallurgic Infuser: Infused + Diamond)
│   ├── Atomic Alloy (Metallurgic Infuser: Reinforced + Obsidian)
│   ├── Basic Gas Tank ← Osmium + glass
│   ├── Advanced/Elite/Ultimate Gas Tank ← upgrade chain
│   ├── Basic Fluid Tank ← idem fluides
│   └── Teleportation Core ← Atomic Alloy + Gold + Lapis
│
├── AE2
│   ├── Certus Quartz (miné ou grindstone)
│   ├── Charged Certus (AE2 Charger)
│   ├── Fluix Crystal (Charged Certus + Redstone + Quartz → eau)
│   ├── Pure Crystals (seeds → growth)
│   ├── Logic Processor (Gold + Redstone + Silicon) via Inscriber
│   ├── Calculation Processor (Pure Certus + Redstone + Silicon) via Inscriber
│   ├── Engineering Processor (Diamond + Redstone + Silicon) via Inscriber
│   └── ME Controller ← Fluix + Engineering Proc + ??? (à modifier)
│
├── BOTANIA
│   ├── Manasteel (Iron + Mana Pool)
│   ├── Mana Diamond (Diamond + Mana Pool)
│   ├── Terrasteel (Iron + Mana Pearl + Mana Diamond + BEAUCOUP de Mana)
│   └── Elementium (Elven Gateway — mid-game Botania)
│
├── ASTRAL SORCERY
│   ├── Aquamarine (miné dans Clay + eau)
│   ├── Starmetal Ingot (Iron → attune sous étoile)
│   ├── Stardust (Starmetal → grind)
│   ├── Glass Lens ← Glass + Aquamarine
│   └── Celestial Crystal ← grown from Rock Crystal
│
└── BLOOD MAGIC
    ├── Blank Slate (Stone + Altar T1 + 1000 LP)
    ├── Reinforced Slate (Blank + Altar T2 + 2000 LP)
    ├── Imbued Slate (Reinforced + Altar T3 + 5000 LP)
    └── Blood Orb T1-T5

NIVEAU 3 — Machines (ce qu'on modifie)
│
├── (voir section 2 ci-dessous)
└── ...
```

---

## 2. CHAÎNE MEKANISM x1 → x5 — Dépendances COMPLÈTES

### Ce que le joueur doit avoir AVANT de commencer Mekanism :

```
PRÉ-REQUIS MEKANISM :
├── Osmium (mining Overworld) ← Âge 3
├── Acier (IE Blast Furnace) ← déjà depuis Âge 1
├── Redstone (mining ou tamis) ← déjà depuis Âge 0
├── Diamond (mining ou tamis) ← déjà depuis Âge 0
├── Obsidian ← lave + eau ← Âge 1+
├── Glass ← Smeltery ou Furnace
├── Electrical Steel (EnderIO) ← pour nos crafts custom
└── Infused Circuit (notre item custom) ← Thermal + Astral
```

### Étape par étape — Machines + dépendances :

```
═══════════════════════════════════════════════════════════════
  STEP 0 : Metallurgic Infuser (PORTE D'ENTRÉE Mekanism)
═══════════════════════════════════════════════════════════════
  Vanilla recipe : Osmium + Iron + Furnace + Redstone
  NOTRE recipe :
    Osmium (mining)
    + Steel Casing (Steel + Osmium + Glass)
    + Furnace
    + IE Heater (immersiveengineering:metal_device1:1) ← force IE
  
  Dépendances : Osmium, Acier IE, verre
  Âge minimum : 3 (Osmium = overworld mining)

═══════════════════════════════════════════════════════════════
  STEP 1 : Infused Alloy → Reinforced → Atomic
═══════════════════════════════════════════════════════════════
  Metallurgic Infuser recipes (PAS de changement) :
    Iron + Redstone dust → Infused Alloy
    Infused Alloy + Diamond → Reinforced Alloy
    Reinforced Alloy + Obsidian → Atomic Alloy
  
  NOTE : On ne modifie PAS les alloys Mekanism — c'est interne au mod.
  La gate est sur la Metallurgic Infuser elle-même.

═══════════════════════════════════════════════════════════════
  STEP 2 : Circuits Basic → Advanced → Elite → Ultimate
═══════════════════════════════════════════════════════════════
  Vanilla recipes (Metallurgic Infuser) :
    Osmium + Infused Alloy → Basic Control Circuit
    Basic + Reinforced Alloy → Advanced Circuit  
    Advanced + Atomic Alloy → Elite Circuit
    Elite + Atomic Alloy → Ultimate Circuit

  NOTRE MODIFICATION : on change les circuits pour forcer d'autres mods
    Basic Circuit = vanilla (juste Mekanism)
    Advanced Circuit = Basic + Reinforced + Electrical Steel (EnderIO)
    Elite Circuit = Advanced + Atomic + Signalum (Thermal)
    Ultimate Circuit = Elite + Starmetal (Astral) + Enderium (Thermal)

  Ça force :
    Basic = Mekanism seul ← Âge 3 début
    Advanced = + EnderIO ← Âge 3 mid
    Elite = + Thermal Signalum ← Âge 3 mid (Signalum = Nether glowstone... wait)
    Ultimate = + Astral + Enderium ← Âge 3 fin / Âge 4

  ⚠️ ATTENTION : Signalum nécessite Redstone (10) + Silver + Copper → OK
  ⚠️ ATTENTION : Lumium nécessite Glowstone → NETHER REQUIS
  ⚠️ ATTENTION : Enderium nécessite Ender Pearls → farming Endermen

═══════════════════════════════════════════════════════════════
  STEP 3 : Steel Casing (base de toutes les machines Mek)
═══════════════════════════════════════════════════════════════
  Vanilla : Osmium + Steel + Glass
  NOTRE recipe :
    Osmium + Steel (IE) + Glass + Iron Mechanical Component (IE)
  
  Force : IE + Mekanism dès le départ

═══════════════════════════════════════════════════════════════
  x2 : Enrichment Chamber
═══════════════════════════════════════════════════════════════
  Vanilla : Redstone + Iron + Circuit Basic + Steel Casing
  NOTRE recipe :
    Basic Circuit
    + Steel Casing
    + Pulverizer (Thermal) ← force d'avoir Thermal AVANT x2 Mek
    + Copper Gear (Thermal)
  
  Dépendances : Mekanism (circuit, casing) + Thermal (Pulverizer, gear)
  Âge : 3 début
  
  NOTE : Le joueur a DÉJÀ le Pulverizer Thermal depuis l'Âge 1.
  L'Enrichment Chamber utilise le Pulverizer comme composant.
  Message : "La chimie commence où la mécanique finit."

═══════════════════════════════════════════════════════════════
  x2 support : Crusher
═══════════════════════════════════════════════════════════════
  Vanilla : Redstone + Lava Bucket + Circuit Basic + Steel Casing
  NOTRE recipe :
    Basic Circuit
    + Steel Casing
    + IE Crusher component (immersiveengineering:material:8 ou 9)
    + Piston
  
  Force : IE + Mekanism

═══════════════════════════════════════════════════════════════
  x3 : Purification Chamber
═══════════════════════════════════════════════════════════════
  Vanilla : Osmium + Advanced Circuit + Enrichment Chamber
  NOTRE recipe :
    Advanced Circuit (→ EnderIO Electrical Steel)
    + Enrichment Chamber (la machine qu'il a déjà)
    + Invar Gear (Thermal)
    + Gold Block
  
  Dépendances : Mekanism + EnderIO + Thermal
  Âge : 3 mid

═══════════════════════════════════════════════════════════════
  x3 support : Electrolytic Separator (produit O2 et H2)
═══════════════════════════════════════════════════════════════
  Vanilla : Redstone + Iron + Advanced Circuit + Steel Casing
  NOTRE recipe :
    Advanced Circuit
    + Steel Casing
    + IE Fluid Pipe
    + Glass Pane (beaucoup)
  
  Nécessaire pour : O2 → Purification, H2 → fuel

═══════════════════════════════════════════════════════════════
  x4 : Chemical Injection Chamber
═══════════════════════════════════════════════════════════════
  Vanilla : Sulfur + Elite Circuit + Purification Chamber + Gold
  NOTRE recipe :
    Elite Circuit (→ Signalum Thermal)
    + Purification Chamber (machine qu'il a)
    + Gold Block
    + Sulfur (Nether ou Mekanism)
  
  Dépendances : Mekanism + Thermal (Signalum) + probablement Nether
  Âge : 3 mid-fin

  Support nécessaire pour x4 :
  ├── Chemical Oxidizer : convertit solide → gaz
  │   Recipe : Elite Circuit + Gas Tank + Fluid Tank + Steel Casing
  │   + Resonant Coil (notre item custom, Thermal+IE+EnderIO)
  │
  ├── Chemical Infuser : combine 2 gaz → 1 gaz
  │   Recipe : Elite Circuit + 2x Gas Tank + Steel Casing
  │   + Induction Smelter (Thermal) comme composant
  │
  └── Pour produire HCl :
      H2 (Electrolytic Sep) + Cl2 (Electrolytic Sep from Brine)
      → Chemical Infuser → HCl gas
      → injecté dans la Injection Chamber

═══════════════════════════════════════════════════════════════
  x5 : Chemical Dissolution Chamber
═══════════════════════════════════════════════════════════════
  Vanilla : Ultimate Circuit + Gas Tank + Steel Casing + H2SO4
  NOTRE recipe :
    Ultimate Circuit (→ Starmetal Astral + Enderium)
    + 2x Ultimate Gas Tank
    + Steel Casing
    + Bucket of Sulfuric Acid
    + Compressed Iron (PneumaticCraft) ← force PneumaticCraft
  
  Dépendances : Mekanism + Astral + Thermal + PneumaticCraft
  Âge : 3 fin (juste avant le X-77)

  Support nécessaire pour x5 :
  ├── Chemical Washer : nettoie le slurry
  │   Recipe : Ultimate Circuit + Water Source (NuclearCraft)
  │   + Ultimate Gas Tank
  │   ⚠️ E2E utilise NuclearCraft water source ici
  │   NOUS : remplacer par Aqueous Accumulator (Thermal) → pas de gate NC
  │   Car NC = Âge 5, et x5 processing = Âge 3
  │
  └── Chemical Crystallizer : cristallise le slurry propre
      Recipe : Ultimate Circuit + Ultimate Gas Tank
      + Celestial Crystal (Astral Sorcery)
      + Compressed Iron Block (PneumaticCraft)

═══════════════════════════════════════════════════════════════
```

---

## 3. CHAÎNE THERMAL — Dépendances

```
Machines Thermal (Âge 1, dans Compact Machine) :
  
  Machine Frame = Tin + Iron + Glass (vanilla-ish, accessible)
  Servo = Iron → Invar → Signalum → Resonant (upgrade path)

  Redstone Furnace     = Frame + Redstone + Bricks ← PAS modifié (première machine)
  Pulverizer           = Frame + IE component + Tinkers plate ← force IE + Tinkers
  Induction Smelter    = Frame + EnderIO redstone alloy + Bronze gear ← force EnderIO
  Sawmill              = Frame + IE Sawblade + logs ← force IE
  Magma Crucible       = Frame + Nether Brick + Invar ← vanilla-ish
  Phytogenic Insolator = Frame + Pam's component + Lumium ← force Pam's, Nether
  Compactor            = Frame + pistons + Bronze ← simple
  Fluid Transposer     = Frame + Glass + Invar ← simple

  Dynamos :
  Steam Dynamo         = Copper coil + Tin + Redstone ← PAS modifié (première énergie)
  Magmatic Dynamo      = IE component + Invar + Copper ← force IE
  Compression Dynamo   = Mekanism component + Signalum ← force Mek (Âge 3)
  Reactant Dynamo      = EnderIO component + Electrum ← force EnderIO
```

---

## 4. CHAÎNE AE2 — Dépendances

```
AE2 Âge 3 (stockage seulement) :

  Certus Quartz      = mining (naturel)
  Charged Certus     = AE2 Charger + Certus
  Fluix Crystal      = Charged Certus + Redstone + Nether Quartz → eau
  Pure Crystals      = Seeds + eau + temps

  Inscriber (pour les Processors) :
    NOTRE recipe : Steel Casing (Mek) + Pistons + Fluix + EnderIO Conduit
    → Force : Mekanism + EnderIO

  Logic Processor    = Inscriber (Gold + Redstone + Silicon)
  Calc Processor     = Inscriber (Pure Certus + Redstone + Silicon)
  Engineering Proc   = Inscriber (Diamond + Redstone + Silicon)

  ME Controller :
    NOTRE recipe : Engineering Proc + Fluix Block + Enderium (Thermal)
    + Electrical Steel (EnderIO)
    → Force : AE2 + Thermal + EnderIO

  ME Drive :
    NOTRE recipe : Engineering Proc + Fluix + Iron + Chest
    → Relativement simple (le stockage doit être accessible)

  ⚠️ GATE AUTOCRAFTING (Âge 4) :
  Molecular Assembler = Engineering Proc + Annihilation Core + Formation Core
    + Composant NuclearCraft OU composant End
    → Force le joueur à avoir End ou NC avant autocrafting

  Crafting Unit = Calc Proc + Steel Casing (Mek) + Engineering Block (IE)
    → Force Mekanism + IE
```

---

## 5. MATÉRIAUX CRITIQUES — Où les trouver et quand

| Matériau | Source | Disponible | Nécessaire pour |
|----------|--------|------------|-----------------|
| Iron | Tamis/Mining | Âge 0 | Tout |
| Copper | Tamis/Mining | Âge 0 | Thermal, IE, câbles |
| Tin | Tamis/Mining | Âge 0 | Thermal, Bronze |
| Gold | Tamis/Mining | Âge 0 | EnderIO, AE2 |
| Silver | Tamis/Mining | Âge 0 | Thermal (Electrum) |
| Nickel | Tamis/Mining | Âge 0 | Thermal (Invar) |
| Lead | Tamis/Mining | Âge 0 | Thermal, IE |
| Osmium | Mining Overworld | Âge 3 | Mekanism (TOUT) |
| Redstone | Tamis/Mining | Âge 0 | Tout |
| Diamond | Tamis/Mining | Âge 0 | Circuits, outils |
| Ender Pearl | Endermen (nuit) | Âge 3 | EnderIO, Enderium, End |
| Blaze Rod | Nether | Âge 3 mid | Mekanism, potions |
| Nether Quartz | Nether | Âge 3 mid | EnderIO, AE2 |
| Glowstone | Nether | Âge 3 mid | Lumium, Energetic Alloy |
| Soul Sand | Nether | Âge 3 mid | Soularium EnderIO |
| Silicon | Pulverizer (Quartz) | Âge 1 | EnderIO, AE2 |
| Certus Quartz | Mining Overworld | Âge 3 | AE2 |
| Starmetal | Astral Sorcery | Âge 2 | Ultimate Circuit, crafts |
| Stardust | Grind Starmetal | Âge 2 | Crafts Astral |
| Aquamarine | Mining (aquifer) | Âge 2 | Astral Sorcery |
| Manasteel | Mana Pool + Iron | Âge 2 | Crafts Botania |
| Terrasteel | Mana + complex | Âge 2 | Fragment Organique |
| Compressed Iron | PneumaticCraft | Âge 3 | x5 chain, X-77 |
| Signalum | 3Cu + 1Ag + 10Rs | Âge 2+ | Elite Circuit, Solar 5+ |
| Lumium | 3Sn + 1Ag + 4Glow | Âge 3 mid | Crafts (Nether requis) |
| Enderium | 2Sn + Ag + Pb + 4EP | Âge 3+ | Endgame Thermal, Ult. Circuit |
| End Stone | The End | Âge 4 | End Steel EnderIO |
| Draconium | End/Mining rare | Âge 7 | Draconic Evolution |

---

## 6. DEADLOCK CHECK — Vérifications critiques

### ⚠️ Points de deadlock potentiels :

**1. Signalum nécessite Redstone liquide**
- Induction Smelter : Copper + Silver + Destabilized Redstone (liquide)
- Le Destabilized Redstone vient du Magma Crucible (redstone → liquide)
- CHECK : le joueur a-t-il le Magma Crucible avant de faire du Signalum ?
- RÉPONSE : Oui, Magma Crucible = Âge 1. Signalum = Âge 2+. OK.

**2. Lumium nécessite Glowstone**
- Glowstone = Nether uniquement
- CHECK : le joueur a-t-il accès au Nether avant de faire du Lumium ?
- RÉPONSE : Nether = mid Âge 3. Lumium = Âge 3 mid. OK mais timing serré.
- ALTERNATIVE : Witches drop Glowstone → farming possible sans Nether
- DÉCISION : Lumium pas requis avant Nether. Les crafts qui utilisent Lumium arrivent APRÈS le Nether.

**3. Enderium nécessite Ender Pearls (x4 par lingot)**
- Ender Pearls = Endermen (spawn la nuit en Overworld)
- CHECK : le joueur peut-il farmer des Ender Pearls en Âge 3 ?
- RÉPONSE : Oui (Endermen spawn la nuit). Lent mais possible.
- ALTERNATIVE : Soul Shards cage d'Enderman (acquis en Âge 0-1)
- DÉCISION : Enderium = Âge 3 fin, farming possible. OK.

**4. Ultimate Circuit nécessite Starmetal**
- Starmetal = Astral Sorcery (Âge 2)
- CHECK : le joueur a-t-il gardé du Starmetal de l'Âge 2 ?
- RÉPONSE : Il devrait. Si non, il peut refaire de l'Astral en Overworld.
- DÉCISION : Mettre une quête rappel "Stocke du Starmetal pour plus tard"

**5. x5 Processing nécessite Sulfuric Acid**
- H2SO4 = Chemical Oxidizer (Sulfur → SO2) + Chemical Infuser (SO2 + O2 → SO3) + ...
- Sulfur = Nether (sulfur ore) ou Mekanism (crusher sur certains items)
- CHECK : le joueur a-t-il du Sulfur en Âge 3 ?
- RÉPONSE : Nether donne du Sulfur. Mekanism peut aussi en produire. OK.

**6. Compressed Iron (PneumaticCraft) dans x5 chain**
- Compressed Iron = Iron Ingot dans Pressure Chamber
- CHECK : le joueur a-t-il PneumaticCraft setup avant x5 ?
- RÉPONSE : Phase 2 de l'Âge 3 = PneumaticCraft. x5 = Phase 2-3. OK.

**7. AE2 Inscriber nécessite Presses**
- Les 4 Presses (Logic, Calc, Engineering, Silicon) = loot dans Meteorites
- CHECK : les Meteorites spawn-elles en Overworld ?
- RÉPONSE : Oui, AE2 génère des meteorites. Le joueur doit les trouver.
- ALTERNATIVE : Ajouter une recette pour les presses si introuvables
- DÉCISION : Garder vanilla (exploration nécessaire) + quête hint

---

## 7. RÉSUMÉ DES RECETTES À MODIFIER PAR MOD

### Thermal Expansion (~15 recettes)
- 8 machines (Furnace libre, 7 autres modifiées)
- 6 dynamos (Steam libre, 5 modifiées)
- Upgrades Hardened→Resonant (4 recettes)

### EnderIO (~10 recettes)
- Alloy Smelter, SAG Mill, Slice'n'Splice
- 2-3 alliages (Electrical Steel via Induction Smelter)
- Conduits (1-2 types modifiés)

### Immersive Engineering (~5 recettes)
- Crusher, Arc Furnace (accessible mais pas modifié lourdement)
- Excavator (modifié pour forcer Steel + composants)
- Diesel Generator

### Mekanism (~20 recettes)
- Steel Casing, Metallurgic Infuser
- 4 circuits (Basic→Ultimate)
- 8 machines processing (Enrichment→Crystallizer)
- Gas/Fluid Tanks (probablement pas modifiés)
- Gas Generator, Wind Generator

### AE2 (~10 recettes)
- Inscriber, ME Controller, ME Drive
- Crafting Unit, Molecular Assembler (Âge 4 gate)
- Storage Cells (probablement pas modifiées)

### PneumaticCraft (~5 recettes)
- Pressure Chamber, Assembly System
- Refinery, Thermopneumatic Plant

### Extended Crafting (~10 recettes)
- Tables tier 0-3
- Recettes des 9 Fragments
- Recette Nexus Absolu 9x9

### TOTAL ESTIMÉ : ~75-80 recettes modifiées dans l'Âge 3

---

*Document créé le 26 Mars 2026 — METTRE À JOUR à chaque changement de recette*
