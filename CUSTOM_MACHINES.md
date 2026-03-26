# CUSTOM_MACHINES.md — Machines et systèmes custom
> Scavenging, machines Modular Machinery, et blocs custom.

---

## 1. SYSTÈME DE SCAVENGING

### Le problème technique
Les murs de Compact Machine (compactmachines3:wall) sont incassables comme de la bedrock.
On ne peut PAS les miner directement.

### La solution : Blocs "Nexus Wall" custom

On crée un bloc custom via ContentTweaker : **nexus_wall**
- Placé dans le coffre de départ (ou déjà présent dans la salle)
- CASSABLE avec n'importe quel outil (mais lent)
- Drops différents selon la pioche utilisée
- Se RE-CRAFT facilement (4 dirt dust → 1 nexus wall)
- Le joueur en a une source infinie (dirt dust → murs → casser → grits + dirt dust)

C'est une boucle : casser le nexus_wall donne des grits + du dirt dust pour refaire des nexus_walls.

```
ContentTweaker — NexusBlocks.zs :

#loader contenttweaker

import mods.contenttweaker.VanillaFactory;
import mods.contenttweaker.Block;

val nexusWall = VanillaFactory.createBlock("nexus_wall", <blockmaterial:rock>);
nexusWall.setCreativeTab(<creativetab:nexus_absolu>);
nexusWall.setBlockHardness(3.0);  // cassable mais lent
nexusWall.setBlockResistance(5.0);
nexusWall.setToolClass("pickaxe");
nexusWall.setToolLevel(0);  // main nue OK mais très lent
nexusWall.setBlockSoundType(<soundtype:stone>);
nexusWall.register();
```

### Drops custom via CraftTweaker

```zenscript
// scripts/Scavenging.zs

import crafttweaker.event.BlockHarvestDropsEvent;

events.onBlockHarvestDrops(function(e as BlockHarvestDropsEvent) {
    if (e.block.definition.id != "contenttweaker:nexus_wall") return;
    
    // Clear default drops
    e.drops = [];
    
    // Toujours drop du dirt dust (pour refaire des nexus walls)
    e.addItem(<contenttweaker:wall_dust> * 2);
    
    val tool = e.player.currentItem;
    
    // Main nue ou pas de pioche
    if (isNull(tool) || !tool.definition.id.contains("pickaxe")) {
        e.addItem(<contenttweaker:wall_dust> * 2);  // 4 total
        return;
    }
    
    val harvestLevel = tool.getHarvestLevel("pickaxe");
    
    // Pioche bois (harvest 0)
    if (harvestLevel == 0) {
        val rand = Math.random();
        if (rand < 0.40)      e.addItem(<minecraft:cobblestone>);
        else if (rand < 0.70) e.addItem(<contenttweaker:wall_dust> * 2);
        else if (rand < 0.90) e.addItem(<minecraft:gravel>);
        else                  e.addItem(<minecraft:flint>);
    }
    // Pioche pierre (harvest 1)
    else if (harvestLevel == 1) {
        val rand = Math.random();
        if (rand < 0.35)      e.addItem(<contenttweaker:iron_grit>);
        else if (rand < 0.65) e.addItem(<contenttweaker:copper_grit>);
        else if (rand < 0.90) e.addItem(<contenttweaker:tin_grit>);
        else                  e.addItem(<minecraft:coal>);
    }
    // Pioche fer (harvest 2)
    else if (harvestLevel == 2) {
        val rand = Math.random();
        if (rand < 0.30)      e.addItem(<contenttweaker:silver_grit>);
        else if (rand < 0.60) e.addItem(<contenttweaker:nickel_grit>);
        else if (rand < 0.85) e.addItem(<contenttweaker:lead_grit>);
        else                  e.addItem(<minecraft:redstone>);
    }
    // Pioche diamant (harvest 3)
    else if (harvestLevel >= 3) {
        val rand = Math.random();
        if (rand < 0.30)      e.addItem(<contenttweaker:gold_grit>);
        else if (rand < 0.55) e.addItem(<contenttweaker:osmium_grit>);
        else if (rand < 0.75) e.addItem(<contenttweaker:diamond_fragment>);
        else if (rand < 0.90) e.addItem(<minecraft:redstone> * 3);
        else                  e.addItem(<contenttweaker:ender_pearl_fragment>);
    }
});
```

### Recette Nexus Wall (renouvelable)
```zenscript
// 4 dirt dust → 1 nexus wall
recipes.addShaped("nexus_wall", <contenttweaker:nexus_wall> * 2,
    [[<contenttweaker:wall_dust>, <contenttweaker:wall_dust>],
     [<contenttweaker:wall_dust>, <contenttweaker:wall_dust>]]);
```

### Coffre de départ (3x3)
```
16x Nexus Wall (blocs à miner)
 1x Quest Book
 1x Livre du Dr. Voss (Patchouli)
 8x Pain
 4x Graines (blé, carotte, patate, betterave)
 1x Bonsai Sapling (Oak)
```

Le joueur place les 16 Nexus Walls dans sa salle 3x3, les mine à la main,
récupère du dirt dust et des grits, refait des Nexus Walls, recommence.
C'est la boucle de base.

---

## 2. AUTO-SCAVENGING (salle 9x9+)

### Phase manuelle (3x3 → 7x7)
Le joueur mine les Nexus Walls à la main avec ses pioches.
C'est lent et intentionnel. Chaque grit compte.

### Phase automatique (9x9+)
Quand le joueur a du RF et des machines :

**Option A — Actually Additions Block Breaker**
```
Le Block Breaker (AA) mine le bloc devant lui automatiquement.
Placé devant un Nexus Wall → casse → drops → hopper/conduit → stockage
Il faut re-placer des Nexus Walls → Block Placer (AA) de l'autre côté

Setup auto :
[Block Placer] → [Nexus Wall] → [Block Breaker] → [Hopper] → [Drawer]
                      ↑                                          │
                      └──────── [Nexus Wall craft] ←─────────────┘

Le joueur craft des Nexus Walls en boucle et les place automatiquement.
L'AA Block Breaker les casse et collecte les drops.
```

**Option B — Cyclic Block Miner**
```
Cyclic a un "Block Miner" qui mine dans une zone.
Plus simple mais moins contrôlable.
```

**Option C — Machine custom Modular Machinery (L'EXCAVATEUR)**
```
Voir section 3 ci-dessous.
```

---

## 3. MACHINES CUSTOM — Modular Machinery

### Machine 1 : Le Purificateur de Voss (salle 7x7, Âge 0)

**Rôle :** Convertit les grits en lingots MIEUX que le four vanilla.
Le four vanilla fait 1 grit → 1 nugget (9 grits = 1 lingot).
Le Purificateur fait 2 grits → 1 lingot (2x plus efficace).
C'est le premier "upgrade" du joueur.

**Lore :** "Voss a conçu cette machine pour extraire chaque atome utile
des résidus de mur. Elle est primitive mais efficace."

**Structure multibloc (2x2x2) :**
```
Couche 0 (sol) :      Couche 1 (haut) :
[Casing] [Input]      [Casing] [Casing]
[Output] [Controller] [Casing] [Casing]

Blocs nécessaires :
- 1x Modular Machinery Controller (crafté avec fer + cobble + redstone)
- 1x Item Input Hatch
- 1x Item Output Hatch
- 5x Machine Casing (crafté avec fer + cobble)
```

**Taille :** 2x2x2 = rentre dans un 7x7 sans problème

**Recettes :**
```json
{
    "registryname": "voss_purifier",
    "localizedname": "Purificateur de Voss",
    "requires-blueprints": false,
    "parts": [
        {"x": 0, "y": 0, "z": 0, "elements": ["modularmachinery:blockcontroller@0"]},
        {"x": 1, "y": 0, "z": 0, "elements": ["modularmachinery:blockinputbus@0"]},
        {"x": 0, "y": 0, "z": 1, "elements": ["modularmachinery:blockoutputbus@0"]},
        {"x": 1, "y": 0, "z": 1, "elements": ["modularmachinery:blockcasing@0"]},
        {"x": 0, "y": 1, "z": 0, "elements": ["modularmachinery:blockcasing@0"]},
        {"x": 1, "y": 1, "z": 0, "elements": ["modularmachinery:blockcasing@0"]},
        {"x": 0, "y": 1, "z": 1, "elements": ["modularmachinery:blockcasing@0"]},
        {"x": 1, "y": 1, "z": 1, "elements": ["modularmachinery:blockcasing@0"]}
    ]
}

Recettes du Purificateur :
  2x Iron Grit → 1x Iron Ingot (60 ticks, pas de RF)
  2x Copper Grit → 1x Copper Ingot
  2x Tin Grit → 1x Tin Ingot
  2x Silver Grit → 1x Silver Ingot
  2x Nickel Grit → 1x Nickel Ingot
  2x Lead Grit → 1x Lead Ingot
  2x Gold Grit → 1x Gold Ingot
  2x Osmium Grit → 1x Osmium Ingot
```

**Pas de RF nécessaire !** C'est une machine "passive" alimentée par... le lore dit
"la fréquence résiduelle des murs de la Compact Machine". En vrai, le processing
time est juste long (60 ticks = 3 secondes par opération).

---

### Machine 2 : L'Excavateur de Mur (salle 9x9, Âge 1)

**Rôle :** Auto-scavenge. Consomme des Nexus Walls et produit des grits automatiquement.
Nécessite du RF. C'est le moment où le joueur passe de "taper les murs" à "la machine tape pour moi."

**Lore :** "Voss a automatisé l'extraction dès qu'il a eu accès au RF.
'Pourquoi frapper quand on peut faire vibrer ?' — Carnet n°8"

**Structure multibloc (3x2x2) :**
```
Couche 0 (sol) :          Couche 1 (haut) :
[Energy In] [Controller]  [Casing] [Casing]
[Input]     [Output]      [Casing] [Casing]

Blocs nécessaires :
- 1x Controller
- 1x Item Input Hatch (Nexus Walls entrent ici)
- 1x Item Output Hatch (grits sortent ici)
- 1x Energy Input Hatch (RF)
- 4x Machine Casing
```

**Coût RF :** 20 RF/t pendant 100 ticks = 2000 RF par opération

**Recettes :**
```
1x Nexus Wall → résultat aléatoire basé sur un "filtre" :
  Sans filtre : mélange de tous les grits (comme pioche pierre)
  Avec Iron Grit dans l'input : 100% Iron Grit x3
  Avec Copper Grit dans l'input : 100% Copper Grit x3
  etc.

Le joueur met un "template" grit + des Nexus Walls → grits ciblés.
C'est plus efficace que le scavenging manuel ET ciblé.
```

**Automation complète :**
```
EnderIO conduits ou Thermal Dynamics :
  [Chest: Nexus Walls] → [Input Hatch] → [Excavateur] → [Output Hatch] → [Drawer]
  [Steam Dynamo] → [Energy Hatch]

Le joueur craft un stack de Nexus Walls, les met dans un coffre,
et l'Excavateur les process automatiquement en grits.
```

---

### Machine 3 : Le Synthétiseur Dimensionnel (salle 9x9, Âge 1)

**Rôle :** Craft les clés d'expansion. Remplace le craft vanilla des clés.
Nécessite du RF + des items spécifiques + du temps.

**Lore :** "Chaque clé contient une fréquence dimensionnelle unique.
Le Synthétiseur aligne les molécules pour créer cette fréquence.
C'est comme accorder un instrument — sauf que l'instrument est la réalité."

**Structure multibloc (3x3x2) :**
```
Couche 0 (sol) :                Couche 1 (haut) :
[Casing]    [Input]   [Casing]  [Casing]  [Casing] [Casing]
[Energy In] [Control] [Output]  [Casing]  [Casing] [Casing]
[Casing]    [Casing]  [Casing]  [Casing]  [Casing] [Casing]
```

**Recettes des clés :**
```
Clé 5x5 :
  Input: 8x Iron Grit + 4x Copper Grit + 1x Poop
  RF: 500 RF total
  Temps: 200 ticks (10 sec)

Clé 7x7 :
  Input: 4x Iron Ingot + 4x Copper Ingot + 2x Tin Ingot + 1x Bronze Ingot
  RF: 2000 RF total
  Temps: 400 ticks (20 sec)

Clé 9x9 :
  Input: 4x Bronze Ingot + 4x Electrum Ingot + 1x Diamond
  RF: 5000 RF total
  Temps: 600 ticks (30 sec)

Clé 11x11 :
  Input: 2x Invar Ingot + 2x Signalum Ingot + 1x Infused Circuit (custom)
  RF: 20000 RF total
  Temps: 1200 ticks (60 sec)

Clé 13x13 :
  Input: 4x Dark Steel (EnderIO) + 2x Enderium Ingot + 1x Resonant Coil (custom)
  RF: 50000 RF total
  Temps: 2400 ticks (120 sec)

Lab Key :
  Input: Fragment Mécanique + Fragment Organique + Fragment Stellaire + Nether Star
  RF: 200000 RF total
  Temps: 6000 ticks (5 min)
  
  "La fréquence est trouvée. La porte s'ouvre. Respire."
```

---

### Machine 4 : Le Cœur de Données (Âge 4, Overworld)

**Rôle :** Machine massive qui craft le fragment "Cœur de Données".
Nécessite AE2 + Mekanism + beaucoup de RF.

**Structure multibloc (5x5x3) — la plus grosse machine custom**
```
Construit avec :
- ME Controller (AE2)
- ME Drive (AE2)
- Steel Casing (Mekanism)
- Machine Casing (Modular Machinery)
- Energy Input Hatch
- Item Input/Output Hatches
- Fluid Input (optionnel)
```

**Sera designée en détail quand on arrivera à l'Âge 4.**

---

## 4. ITEMS CUSTOM SUPPLÉMENTAIRES

### Blocs à créer (ContentTweaker)
```
nexus_wall         — le bloc minable qui donne les grits
```

### Items à ajouter (ContentTweaker)
```
cobblestone_fragment    — 4 → 1 cobblestone
diamond_fragment        — 4 → 1 diamond
emerald_fragment        — 4 → 1 emerald
ender_pearl_fragment    — 8 → 1 ender pearl
obsidian_fragment       — 4 → 1 obsidian
soul_sand_dust          — 4 → 1 soul sand
super_fertilizer        — poop + bonemeal → accélère crops
```

---

## 5. TIMELINE TECHNIQUE

```
Priorité 1 (avant de tester en jeu) :
  [x] ContentTweaker items (28 — déjà fait)
  [ ] ContentTweaker nexus_wall bloc
  [ ] ContentTweaker items supplémentaires (7 nouveaux)
  [ ] CraftTweaker scavenging drops (Scavenging.zs)
  [ ] CraftTweaker recette nexus_wall
  [ ] Coffre de départ (CraftTweaker ou command block)
  
Priorité 2 (machines custom) :
  [ ] Modular Machinery — Purificateur de Voss (JSON)
  [ ] Modular Machinery — Excavateur de Mur (JSON)
  [ ] Modular Machinery — Synthétiseur Dimensionnel (JSON)
  [ ] Recettes Modular Machinery pour chaque machine
  
Priorité 3 (automation) :
  [ ] Config AA Block Breaker compatible Nexus Wall
  [ ] Config EnderIO conduits pour les machines custom
  [ ] Test complet de la boucle scavenging → grits → lingots
```

---

*Document créé le 26 Mars 2026*
