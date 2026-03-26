# DIMENSIONS.md — Progression dimensionnelle Nexus Absolu
> Comment le joueur débloque chaque dimension, dans quel ordre, et pourquoi.

---

## CARTE DE PROGRESSION

```
Âge 0-2 ─── Compact Machine (dim 144+)
                │
                │ ← lab_key (3 Fragments)
                ▼
Âge 3 ──── Overworld (dim 0)
                │
                ├──→ Nether (dim -1)         ← flint & steel gatée
                │
                ├──→ Âge 4-5 : The End (dim 1)  ← Ender Eyes gatés
                │
                ├──→ Âge 6 : Lune (GC)       ← Fusée Tier 1 gatée
                │     ├──→ Mars (GC)          ← Fusée Tier 2
                │     ├──→ Extra Planets       ← Fusée Tier 3+
                │     └──→ Station orbitale    ← Adv. Rocketry
                │
                ├──→ Âge 6 : RFTools Dims     ← Dimlets gatés
                │
                └──→ Âge 7 : Twilight Forest  ← Portal (optionnel)
```

---

## DÉTAIL PAR DIMENSION

### 1. Compact Machine Dimensions (Âge 0-2)
**Accès :** Le joueur spawn dedans. C'est le début du jeu.
**Progression interne :**
```
3x3x3 → compact_key_5x5  → 5x5x5
5x5x5 → compact_key_7x7  → 7x7x7
7x7x7 → compact_key_9x9  → 9x9x9    (fin Âge 0)
9x9x9 → compact_key_11x11 → 11x11x11 (Âge 1)
11x11 → compact_key_13x13 → 13x13x13 (Âge 1-2)
13x13 → compact_key_17x17?→ 17x17    (Âge 2, si CM3 le supporte)
```

**Comment les clés marchent (CraftTweaker) :**
- Chaque clé se craft avec des ressources de l'âge en cours
- La clé + la Compact Machine au bon tier → nouvelle machine plus grande
- Le joueur place la nouvelle CM dans l'ancienne, y entre, et recommence

**Recettes des clés (planifiées) :**
```zenscript
// Clé 5x5 — Ex Nihilo + Tinkers basique
recipes.addShaped("key_5x5", <contenttweaker:compact_key_5x5>,
  [[<ore:ingotIron>, <exnihilocreatio:item_mesh:2>, <ore:ingotIron>],
   [<ore:ingotCopper>, <compactmachines3:machine:1>, <ore:ingotCopper>],
   [<ore:ingotIron>, <ore:ingotTin>, <ore:ingotIron>]]);

// Clé 7x7 — Bronze + premiers alliages
// Clé 9x9 — Invar + composant Ex Nihilo avancé
// Clé 11x11 — Thermal machines + EnderIO
// Clé 13x13 — Acier IE + Electrum + fragment
// Lab Key — 3 Fragments (Mécanique + Organique + Stellaire)
```

**La Lab Key (sortie vers Overworld) :**
```zenscript
mods.extendedcrafting.TableCrafting.addShaped(0, <contenttweaker:lab_key>,
  [[<contenttweaker:fragment_mecanique>, <minecraft:nether_star>, <contenttweaker:fragment_stellaire>],
   [<ore:ingotEnderium>, <compactmachines3:machine:5>, <ore:ingotEnderium>],
   [<contenttweaker:fragment_organique>, <minecraft:nether_star>, <contenttweaker:fragment_organique>]]);
```

**Astral Sorcery en Compact Machine :**
- Problème : AS nécessite vue du ciel
- Solution 1 : JustEnoughDimensions config pour que la dim CM ait un ciel
- Solution 2 : Compact Machine Maximum (la plus grande) a un "trou" vers le ciel
- Solution 3 : Forcer le joueur à faire AS en Âge 3 (overworld) — MOINS BON car on veut les 3 Fragments avant de sortir

**Config JED recommandée (config/justenoughdimensions/):**
```json
{
  "144": {
    "skylight": true,
    "worldProvider": "compactmachines3"
  }
}
```

---

### 2. Overworld (Âge 3+)
**Accès :** Utiliser la lab_key
**Comment ça marche techniquement :**
- Le joueur craft la lab_key dans sa salle 13x13 ou 17x17
- Il fait clic droit avec la lab_key → téléportation à l'overworld
- OU : la lab_key se combine avec un Personal Shrinking Device pour sortir
- OU : la lab_key ouvre un "tunnel" spécial dans la Compact Machine

**Ce qui est disponible :**
- Mining normal (tous les minerais, enfin !)
- Biomes O'Plenty (biomes variés)
- Recurrent Complex (structures générées)
- Ice and Fire (dragons dans le monde)
- Villages et donjons

**Ce qui est NOUVEAU par rapport aux Compact Machines :**
- Espace infini (le joueur respire enfin)
- Mobs hostiles variés (plus juste les spawns contrôlés)
- Ressources infinies (plus besoin de tamis)
- Multiblocs géants possibles (IE Crusher, Arc Furnace, etc.)

---

### 3. Nether (Âge 3)
**Accès :** Portail vanilla MAIS :
- L'obsidienne se fait normalement (lave + eau)
- Le Flint & Steel a sa recette MODIFIÉE :

```zenscript
// Flint & Steel nécessite de l'acier IE (pas du fer vanilla)
recipes.remove(<minecraft:flint_and_steel>);
recipes.addShaped("nexus_flint_steel", <minecraft:flint_and_steel>,
  [[null, <ore:ingotSteel>],
   [<minecraft:flint>, null]]);
```

**Pourquoi le Nether est nécessaire (Âge 3) :**
- Blaze Rods → Blaze Powder → nécessaire pour :
  - Brewing Stand (potions)
  - Ender Eyes (End, plus tard)
  - Mekanism Chemical Oxidizer (soufre du Nether)
- Nether Quartz → EnderIO, AE2
- Glowstone → alliages Thermal (Lumium)
- Soul Sand → EnderIO Soularium
- Magma Cream → Mekanism

**Mobs Nether configurés (In Control) :**
- Blazes, Wither Skeletons, Ghasts = normaux
- Pas de mobs extra (le Nether est déjà assez dur)

---

### 4. The End (Âge 5)
**Accès :** Ender Eyes modifiés

```zenscript
// Ender Eye nécessite composants Mekanism + Astral
recipes.remove(<minecraft:ender_eye>);
recipes.addShaped("nexus_ender_eye", <minecraft:ender_eye>,
  [[null, <astralsorcery:itemcraftingcomponent:2>, null],
   [<ore:ingotPulsatingIron>, <minecraft:ender_pearl>, <ore:ingotPulsatingIron>],
   [null, <mekanism:controlcircuit:1>, null]]);
```

**Pourquoi Âge 5 (pas avant) :**
- Le Dragon drop le Dragon Egg → nécessaire plus tard
- End Stone → composants EnderIO End Steel
- Shulker Boxes → stockage
- Chorus Fruit → craft avancés
- L'Ender Dragon est un gate boss naturel

**Gate supplémentaire possible :**
- Stronghold non généré → utiliser un item custom pour le faire spawner
- OU : le portal du End nécessite une clé custom en plus des Eyes

---

### 5. Galacticraft — Lune (Âge 6)
**Accès :** Fusée Tier 1 modifiée

La fusée vanilla Galacticraft nécessite juste du fer et de l'étain.
On la rend BEAUCOUP plus chère :

```zenscript
// NASA Workbench — recette fusée T1 modifiée
// Nécessite : Acier IE + Signalum Thermal + Osmium Mekanism + NuclearCraft composants
// (la recette exacte sera dans le NASA Workbench config)
```

**Ce que la Lune apporte :**
- Cheese (Pam's compat?)
- Meteoric Iron → matériaux Galacticraft
- Desh Ingot → nécessaire pour fusée Tier 2
- Dungeon Lune → Tier 2 Schematic

**Lore :** "Station Ω — La première étape vers le Fragment Espace-Temps"

---

### 6. Galacticraft — Mars (Âge 6)
**Accès :** Fusée Tier 2 (nécessite Desh de la Lune)
**Ce que Mars apporte :**
- Desh en quantité
- Slimeling (compagnon)
- Mars Dungeon → Tier 3 Schematic
- Matériaux pour Advanced Rocketry

---

### 7. Extra Planets (Âge 6+)
**Accès :** Fusées Tier 3+ (progressif)
**Planètes disponibles :**
- Vénus, Jupiter, Saturne, Uranus, Neptune, Pluton
- Chaque planète a des minerais uniques

**Gate logique :**
- Chaque tier de fusée nécessite le matériau de la planète précédente
- Lune → Mars → Vénus → Jupiter → etc.

**Matériau unique pour le Nexus :**
- Le Fragment Espace-Temps nécessite un item crafté EN ORBITE
- via Advanced Rocketry station orbitale

---

### 8. Advanced Rocketry — Station Orbitale (Âge 6)
**Accès :** Gate par Galacticraft (recette du Rocket Assembling Machine nécessite composants GC)

**Pourquoi c'est important :**
- La station orbitale est où le Fragment Espace-Temps est crafté
- Lore : "La Station Ω-7 du Dr. Voss"
- C'est le seul endroit en gravité zéro

```zenscript
// Le Fragment Espace-Temps ne peut être crafté qu'en orbite
// (via Modular Machinery custom ou condition de dimension)
```

---

### 9. RFTools Dimensions (Âge 6+)
**Accès :** Les Dimlets sont gatés par la progression

```zenscript
// Dimension Builder nécessite composants AE2 + NuclearCraft
recipes.remove(<rftools:dimension_builder>);
recipes.addShaped("nexus_dim_builder", <rftools:dimension_builder>,
  [[<ore:ingotEnderium>, <appliedenergistics2:material:24>, <ore:ingotEnderium>],
   [<nuclearcraft:ingot:8>, <rftools:machine_frame>, <nuclearcraft:ingot:8>],
   [<ore:ingotEnderium>, <contenttweaker:infused_circuit>, <ore:ingotEnderium>]]);
```

**Usage dans le pack :**
- Dimensions custom pour farming de ressources spécifiques
- Mining dimensions (alternative aux Void Miners)
- Quête : créer une dimension parfaite

---

### 10. Twilight Forest (Âge 7 — OPTIONNEL)
**Accès :** Portal vanilla (diamant + eau + fleurs)
- On ne gate PAS le portal TF — le joueur peut y aller quand il veut
- MAIS les boss TF sont progressifs et naturellement difficiles

**Ce que TF apporte (bonus, pas obligatoire) :**
- Ironwood, Steeleaf, Knightmetal, Fiery Ingots → matériaux uniques
- Naga Scale, Lich Trophy → trophées de boss
- Twilight Forest boss chain → contenu aventure
- Carminite → matériau rare

**Pourquoi optionnel :**
- Le Codex Transcendant (Âge 7) ne nécessite PAS de matériaux TF
- MAIS certaines recettes secondaires peuvent utiliser du Knightmetal ou du Fiery
- Le joueur qui explore TF a un avantage (meilleur loot) mais peut s'en passer

**Intégration lore :**
- "Le Twilight Forest est une dimension née du rêve collectif des âmes"
- "Le Dr. Voss y a trouvé des fragments de mémoire qu'il n'a jamais pu expliquer"
- Easter egg : un labo caché du Dr. Voss dans le Lich Tower ?

---

## RÉSUMÉ — TIMELINE DES DIMENSIONS

| Âge | Dimension | Comment y accéder | Obligatoire ? |
|-----|-----------|-------------------|---------------|
| 0-2 | Compact Machine | Spawn | ✅ |
| 0-2 | CM expansions | Clés custom (5x5→13x13) | ✅ |
| 3 | Overworld | lab_key (3 Fragments) | ✅ |
| 3 | Nether | Flint & Steel (acier IE) | ✅ |
| 5 | The End | Ender Eyes modifiés | ✅ |
| 6 | Lune (GC) | Fusée T1 (inter-mods) | ✅ (Fragment E-T) |
| 6 | Mars (GC) | Fusée T2 (Desh) | ✅ (pour T3) |
| 6+ | Extra Planets | Fusées T3+ | ⚠️ Semi |
| 6 | Station orbitale (AR) | Gate par GC | ✅ (Fragment E-T) |
| 6+ | RFTools Dims | Dim Builder (AE2+NC) | ⚠️ Semi |
| 7 | Twilight Forest | Portal vanilla | ❌ Optionnel |

---

## ITEMS DE GATE PAR DIMENSION

### Items dont la recette bloque l'accès :
```
Flint & Steel          → nécessite Acier IE (gate Nether)
Ender Eye              → nécessite Pulsating Iron + Astral + Mek (gate End)
NASA Workbench         → nécessite composants multi-mods (gate Lune)
Rocket T2              → nécessite Desh (gate Mars, autogate)
Rocket T3              → nécessite matériaux Mars (autogate)
Dimension Builder      → nécessite AE2 + NC + Enderium (gate RFTools Dims)
Rocket Assembling Mach → nécessite composants GC (gate station orbitale)
lab_key                → nécessite 3 Fragments (gate Overworld)
```

### Items qui NE gatent PAS :
```
Twilight Forest portal → diamant + eau + fleurs (vanilla, pas modifié)
Nether Portal cadre    → obsidienne (vanilla, pas modifié)
End Portal cadre       → vanilla (non modifiable, mais les Eyes sont gatés)
```

---

*Document créé le 26 Mars 2026*
