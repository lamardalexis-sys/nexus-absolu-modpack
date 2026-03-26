# CONFIG_CHANGES.md — Changements de config planifiés
> Toutes les modifications de config nécessaires pour l'équilibrage.

---

## 1. SOLAR FLUX REBORN — panels.hlc

### Principe : courbe moins explosive, solaire = complément, pas remplacement

| Tier | Actuel RF/t | Nouveau RF/t | Capacity | Transfer | Âge |
|------|------------|-------------|----------|----------|-----|
| 1 | 1 | 4 | 10,000 | 32 | 1 |
| 2 | 8 | 16 | 50,000 | 128 | 1 |
| 3 | 32 | 48 | 200,000 | 384 | 1-2 |
| 4 | 128 | 128 | 1,000,000 | 1,024 | 2 |
| 5 | 512 | 320 | 4,000,000 | 2,560 | 3 (gate NC) |
| 6 | 2,048 | 800 | 16,000,000 | 6,400 | 4 (gate NC) |
| 7 | 8,192 | 2,000 | 40,000,000 | 16,000 | 5 |
| 8 | 32,768 | 5,000 | 80,000,000 | 40,000 | 5 |
| ──── | ────── | SAUT DRACONIC x20 | ────── | ────── | ──── |
| Wyvern | 65,536 | 100,000 | 1,000,000,000 | 800,000 | 7 |
| Draconic | 262,144 | 1,000,000 | 10,000,000,000 | 8,000,000 | 7-8 |
| Chaotic | 524,288 | 15,000,000 | 150,000,000,000 | 120,000,000 | 8 |
| Neutronium | 8,388,608 | 1,000,000,000 | 10,000,000,000,000 | 8,000,000,000 | 8-9 |
| Infinity | 16,777,216 | 55,000,000,000 | 550,000,000,000,000 | 440,000,000,000 | 9 |

Note: Draconic Energy Core T8 = 9.223 Quintillion RF
→ 3 panneaux Infinity + tout le reste (331M RF/t) = Core T8 en ~32 jours
→ La quête "L'Étoile Noire" (300M RF/t) = dernière quête de production atteignable

**Statut : ⏳ À APPLIQUER**

---

## 2. CONFIGS DÉJÀ FAITES ✅

| Config | Changement | Status |
|--------|-----------|--------|
| AE2 channels | 32/128 (x4) | ✅ |
| UniDict keepOneEntry | true + 16 métaux | ✅ |
| Astral Sorcery sky fix | weakSkyRenders | ✅ |
| Forge version check | disabled | ✅ |
| Surge disableAnimatedModels | true | ✅ |
| BetterFps preallocateMemory | true | ✅ |
| FoamFix deduplicate | true | ✅ |

---

## 3. CONFIGS ÉNERGIE — APPLIQUÉES ✅

### Extreme Reactors — config/Extreme Reactors/Extreme Reactors.cfg
```
D:powerProductionMultiplier=10.0          (était 1.0, x10)
D:reactorPowerProductionMultiplier=10.0   (était 1.0, x10)
D:turbinePowerProductionMultiplier=10.0   (était 1.0, x10)
```

### Mekanism — config/mekanism.cfg
```
D:EnergyPerFusionFuel=50000000.0    (était 5000000, x10 → Fusion Reactor x10)
D:WindGenerationMax=4800.0          (était 480, x10)
D:AdvancedSolarGeneration=3000.0    (était 300, x10)
D:SolarGeneration=500.0             (était 50, x10)
```

### NuclearCraft — config/nuclearcraft.cfg
```
D:fission_fuel_efficiency_multiplier=5.0   (était 1.0, x5 → plus de RF/t)
D:fission_fuel_time_multiplier=0.5         (était 1.0, x0.5 → fuel dure moins = plus de puissance)
```

### Environmental Tech Solar — config/environmentaltech/main.cfg
```
I:s_production_rate=512                      (était 128, x4)
I:tier_1_solar_array_max_efficiency=400      (était 100, x4)
I:tier_2_solar_array_max_efficiency=800      (était 200, x4)
I:tier_3_solar_array_max_efficiency=1600     (était 400, x4)
I:tier_4_solar_array_max_efficiency=3200     (était 800, x4)
I:tier_5_solar_array_max_efficiency=6400     (était 1600, x4)
I:tier_6_solar_array_max_efficiency=12800    (était 3200, x4)
I:6aethium_solar_cell_efficiency=1024        (était 256, x4)
```

### Environmental Tech Lunar — config/environmentaltech/etlunar/main.cfg
```
I:s_production_rate=512    (était 128, x4)
```

---

## 4. CONFIGS À FAIRE

### Spice of Life: Carrot Edition
```
Fichier: config/solcarrot.cfg (ou similaire)
maxHearts=50
```

### In Control — Spawn mobs par dimension
```
Fichier: config/incontrol/spawn.json
- Dimension Compact Machine: seulement passifs + quelques hostiles limités
- Overworld: normal + Ice&Fire dragons
- Twilight Forest: renforcé
- Galacticraft: custom (aliens?)
```

### Mekanism — Processing chain
```
- Vérifier en jeu que x5 processing n'est pas trop facile avec le fuel buff
```

### Draconic Evolution
```
Fichier: config/brandon3055/DraconicEvolution.cfg
- Reactor buff: à faire via CraftTweaker (fuel recipes) car pas de config directe
- Objectif: 15M RF/t max pour le Draconic Reactor
```

### Compact Machines 3
```
Fichier: config/compactmachines3/
- Vérifier les tailles max
- Config des tunnels (pour Astral Sorcery sky access)
```

### Blood Magic
```
Fichier: config/bloodmagic/
- Vérifier LP generation rates
- Ajuster si Well of Suffering trop OP trop tôt
```

### Mystical Agriculture
```
Fichier: config/mysticalagriculture.cfg
- Seed drop rates (réduire pour tier 5-6)
- Growth speed (ralentir pour tiers élevés)
```

### Environmental Tech — Void Miners
```
Fichier: config/environmentaltech/
- Réduire output des void miners pour ne pas bypass le mining
- Ou augmenter les tiers requis
```

---

*Document créé le 26 Mars 2026*
