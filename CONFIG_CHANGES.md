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
| Wyvern | 65,536 | 10,000 | 200,000,000 | 80,000 | 7 |
| Draconic | 262,144 | 50,000 | 400,000,000 | 400,000 | 7-8 |
| Chaotic | 524,288 | 200,000 | 1,500,000,000 | 1,600,000 | 8 |
| Neutronium | 8,388,608 | 1,000,000 | 80,000,000,000 | 8,000,000 | 8-9 |
| Infinity | 16,777,216 | 5,000,000 | 200,000,000,000 | 40,000,000 | 9 |

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

## 3. CONFIGS À FAIRE

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
Fichier: config/mekanism.cfg
- Vérifier que x5 processing n'est pas trop facile
- Éventuellement augmenter le coût en gaz
```

### Draconic Evolution
```
Fichier: config/brandon3055/DraconicEvolution.cfg
- Réduire la génération du Draconic Reactor si trop OP
- Ou modifier via CraftTweaker les recettes du fuel
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
