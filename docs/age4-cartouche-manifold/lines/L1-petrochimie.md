# L1 — Pétrochimie

> Tags: #ligne #petrochimie #pneumaticcraft #immersivepetroleum
> Objectif : produire le **Solvant Neutre α** (kérosène ultra-raffiné) + sous-produits
> Théorème : I (Conservation Absolue)

---

## 📥 Inputs

- **Crude Oil** (PneumaticCraft Pumpjack ou IP Pumpjack)
- **Eau Tridistillée** (de [[L2-hydro-eau]])
- **Hydrogène H₂** (de [[L3-electrolyse-cryo]])
- **Cobalt + Tungstène** (de [[L4-pyrometallurgie]]) — pour catalyseur CoMo

## 📤 Outputs

| Produit | Quantité/cycle | Destination |
|---------|----------------|-------------|
| **Solvant Neutre α** | 1000mB | Synthèse finale (M1) |
| Naphta léger | 500mB | [[L7-organique-acetone]] (Cumène) |
| Gaz Naturel | 200mB | [[L7-organique-acetone]] (Propylène) + [[L6-acides-ammoniaque]] (H₂) |
| H₂S | 100mB | [[L6-acides-ammoniaque]] (Claus → S) |
| Diesel | 300mB | Auto-conso génératrices |
| Bitume | 50mB | Déchet ou roads decoratifs |

---

## 🔧 Étapes

### L1.1 — Pumpjack
PneumaticCraft Pumpjack natif. Pose-le sur un puits (Plastic Mixer + Sensor pour détecter). Output : `pneumaticcraft:fluid.oil`.

### L1.2 — Désaération sous vide (multibloc custom MB-DESA)
Sépare les gaz légers (méthane, éthane) du brut.
- **Input** : Crude Oil 1000mB
- **RF** : 5000 RF/t pendant 20s (400 ticks)
- **Output** : Crude Dégazé 800mB + Gaz Naturel 200mB

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("desaeration", "vacuum_chamber", 400, 0)
  .addFluidInput(<liquid:oil> * 1000)
  .addEnergyPerTickInput(5000)
  .addFluidOutput(<liquid:crude_degazed> * 800)
  .addFluidOutput(<liquid:natural_gas> * 200)
  .build();
```

### L1.3 — Distillation (PneumaticCraft Refinery, natif)
Refinery PCC native, alimentée à des températures différentes.
- **Input** : Crude Dégazé via PCC Heat Pipes (température 100-300°C selon palier)
- **Output** : 4 fluides natifs (LPG, Gasoline, Kérosène brut, Diesel) + Bitume au fond

→ Ajuster les ratios via CraftTweaker si besoin :
```zenscript
mods.pneumaticcraft.Refinery.removeRecipe(<liquid:kerosene>);
mods.pneumaticcraft.Refinery.addRecipe(150, [<liquid:kerosene> * 600, <liquid:diesel> * 300, <liquid:bitume> * 100]);
```

### L1.4 — Hydrodésulfuration (multibloc custom MB-HDS)
Layout 3x8x3 (tour verticale)
- **Couche 0** (sol) : 4 Reinforced Iridium Casing (Mekanism) en croix + Energy Hatch
- **Couches 1-6** (tour) : Reinforced Iridium Casing autour, Quantum Glass au centre
- **Couche 7** (top) : Output Hatch fluide
- **Catalyseur** : item input slot avec **Pellets CoMo** (à crafter : 1 Cobalt Dust + 2 Tungsten Dust + sintering 800°C)

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("hds_kerosene", "hds_tower", 600, 0)
  .addFluidInput(<liquid:kerosene> * 1000)
  .addFluidInput(<liquid:hydrogen> * 200)
  .addItemInput(<item:nexusabsolu:pellets_como> * 1)
  .addEnergyPerTickInput(10000)
  .addFluidOutput(<liquid:kerosene_desulfured> * 950)
  .addFluidOutput(<liquid:h2s> * 50)
  .build();
```

→ Consomme 1 pellet CoMo par cycle (catalyseur sacrificiel).

### L1.5 — Hydrocraquage (réutilise MB-HDS)
Mêmes blocs, recette différente (catalyseur Zéolithe au lieu de CoMo).
- **Zéolithe synthétique** : 4 Silicon Dust + 2 Aluminum Dust + 1 Sodium Dust → Pressurized Reaction Chamber Mekanism (recette custom)

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("hydrocrack_kerosene", "hds_tower", 400, 0)
  .addFluidInput(<liquid:kerosene_desulfured> * 1000)
  .addFluidInput(<liquid:hydrogen> * 100)
  .addItemInput(<item:nexusabsolu:zeolite_pellet> * 1)
  .addEnergyPerTickInput(8000)
  .addFluidOutput(<liquid:kerosene_premium> * 1000)
  .build();
```

### L1.6 — Mixage final (Mekanism Fluidic Plenisher / Fluid Mixer custom)
Kérosène Premium + Eau Tridistillée + Tensioactif Phosphaté = Solvant Neutre α.

- **Tensioactif Phosphaté** : sous-recette à créer
  - 1 Phosphore (de L3) + 1 Sodium Dust (de L3) + 1 Vegetable Oil (Pam's HarvestCraft `harvestcraft:freshwateritem` puis press)
  - Crusher Mekanism custom recipe → 1 Tensioactif Phosphate dust

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("solvant_alpha", "fluid_mixer", 200, 0)
  .addFluidInput(<liquid:kerosene_premium> * 800)
  .addFluidInput(<liquid:tridistilled_water> * 200)
  .addItemInput(<item:nexusabsolu:tensioactif_phosphate> * 1)
  .addEnergyPerTickInput(2000)
  .addFluidOutput(<liquid:solvant_alpha> * 1000)
  .build();
```

---

## 🏗️ Multiblocs custom à créer

- **MB-DESA** (Désaération sous vide, 3x3x3) — voir [[../multiblocs/MB-DESA]]
- **MB-HDS** (Tour HDS/Hydrocraquage, 3x8x3) — voir [[../multiblocs/MB-HDS]]
- **MB-MIX** (Fluid Mixer custom, 3x3x3) — peut être substitué par Mekanism Chemical Infuser si plus simple

## 🧪 Fluides custom à déclarer (ContentTweaker)

```zenscript
#loader contenttweaker
import mods.contenttweaker.VanillaFactory;

val crude_degazed = VanillaFactory.createFluid("crude_degazed", 0x3D2817); crude_degazed.density = 850; crude_degazed.viscosity = 1500; crude_degazed.register();
val natural_gas = VanillaFactory.createFluid("natural_gas", 0xCFD8DC); natural_gas.density = 1; natural_gas.viscosity = 100; natural_gas.gaseous = true; natural_gas.register();
val h2s = VanillaFactory.createFluid("h2s", 0xFFEB3B); h2s.density = 2; h2s.viscosity = 100; h2s.gaseous = true; h2s.register();
val kerosene_desulfured = VanillaFactory.createFluid("kerosene_desulfured", 0xFFF59D); kerosene_desulfured.density = 800; kerosene_desulfured.viscosity = 800; kerosene_desulfured.register();
val kerosene_premium = VanillaFactory.createFluid("kerosene_premium", 0xFFEE58); kerosene_premium.density = 780; kerosene_premium.viscosity = 600; kerosene_premium.register();
val solvant_alpha = VanillaFactory.createFluid("solvant_alpha", 0x80DEEA); solvant_alpha.density = 850; solvant_alpha.viscosity = 700; solvant_alpha.register();
```

## 🪨 Items custom (catalyseurs)

- `nexusabsolu:pellets_como` (Cobalt-Molybdène — sub Tungstène)
- `nexusabsolu:zeolite_pellet`
- `nexusabsolu:tensioactif_phosphate`

## ⚡ Bilan énergie L1

- HDS : 10k RF/t × 600 ticks = 6M RF par cycle
- Hydrocrack : 8k RF/t × 400 ticks = 3.2M RF par cycle
- Désaération : 5k RF/t × 400 ticks = 2M RF par cycle
- Mixage : 2k RF/t × 200 ticks = 400k RF par cycle
- **Total / cycle complet** : ~12M RF pour 1000mB de Solvant α

## 🔗 Voir aussi

- [[../02-pipeline-overview]] — Vue ensemble
- [[L6-acides-ammoniaque]] — destinataire H₂S et Gaz Naturel
- [[L7-organique-acetone]] — destinataire Naphta
