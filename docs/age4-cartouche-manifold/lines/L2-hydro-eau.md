# L2 — Hydro-Eau

> Tags: #ligne #eau #thermal #mekanism
> Objectif : produire **Eau Tridistillée**, **Eau Lourde D₂O** et **Tritium T₂**
> Théorème : I (Conservation Absolue)

---

## 📥 Inputs

- **Eau** (Mekanism Electric Pump source infinie)
- **Énergie RF**
- **Flux neutronique** (de [[L5-nucleaire]]) — pour transmuter Li-6 en Tritium

## 📤 Outputs

| Produit | Quantité/cycle | Destination |
|---------|----------------|-------------|
| **Eau Tridistillée** | 1000mB | Tout le pipeline (L1, L3, L6, L7, L8) |
| **Eau Lourde D₂O** | 50mB | [[L5-nucleaire]] (modérateur), [[L8-botanique-manifoldine]] (cyclisation) |
| **Tritium T₂** | 10mB | [[L5-nucleaire]] (composé γ3 ⁶LiT) |

---

## 🔧 Étapes

### L2.1 — Distillation 1 (Thermal Evaporation Plant — natif)
Setup classique TF (4 Evaporation Controller + Valves + Glass + Plain Casing).
- **Input** : Water 1000mB + chaleur soleil
- **Output** : Brine ou Distilled Water (selon config) — ici on veut Distilled Water (recette modifiable via CrT)

```zenscript
// Si nécessaire, modifier output de TEP via CrT (à confirmer avec docs Thermal)
// Sinon utiliser un setup custom MM
```

Alt : Mekanism Thermal Evaporation Plant (5x4x5 multibloc Mekanism, output Brine → utiliser autre pour eau distillée pure).

### L2.2 — Filtration ionique (multibloc custom MB-FILTER ou simple Mekanism)
Setup simple : passer l'eau distillée à travers un **Bloc Résine Échangeuse** (custom) qui consomme 1 charge à chaque 1000mB filtré.

- **Bloc Résine Échangeuse** : ContentTweaker block + recipe (1 Silica Dust + 1 Sulfonic Acid Pellet)
- **Sulfonic Acid Pellet** : sub-recipe (Sulfur + Hydrogen + Oxygen via Reaction Chamber Mekanism)

Recipe d'usage (input/output via Fluidic Plenisher custom ou MM):
```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("ion_filtration", "ion_filter", 100, 0)
  .addFluidInput(<liquid:distilledwater> * 1000)
  .addItemInput(<item:nexusabsolu:resin_charge> * 1)
  .addEnergyPerTickInput(500)
  .addFluidOutput(<liquid:bidistilled_water> * 1000)
  .build();
```

### L2.3 — Osmose Inverse (multibloc custom MB-OSMOSE)
Layout 3x3x3, membrane Silicium dopé au centre.
- **Membrane Silicium Dopé** : Silicon Dust + Phosphorus + Aluminum dans Pressure Chamber PCC

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("reverse_osmosis", "osmose_inverse", 200, 0)
  .addFluidInput(<liquid:bidistilled_water> * 1000)
  .addEnergyPerTickInput(3000)  // pression élevée
  .addFluidOutput(<liquid:tridistilled_water> * 1000)
  .build();
```

→ La membrane n'est PAS sacrificielle ici (durabilité illimitée pour économie de jeu). Pour un challenge, ajouter un wear & tear via durability item input recharge tous les 100 cycles.

### L2.4 — Sous-ligne Eau Lourde (Mekanism natif)
Mekanism Electrolytic Separator avec config "Heavy Water" (gas:deuterium output) :
- 1000mB Water → 1000mB H₂ + 500mB O₂ (standard)
- Avec config Heavy Water enabled : 100mB Heavy Water par cycle d'enrichissement

→ Setup standard Mekanism, pas de custom requis. Utiliser Sea Water (Brine via TEP) en input pour meilleure ratio Deuterium.

### L2.5 — Sous-ligne Tritium (multibloc custom MB-TRITIUM)
Layout 3x3x3, doit être placé **à côté** d'un fission reactor NuclearCraft actif (récupère le flux neutronique).
- **Input** : Lithium-6 Ingot (sous-recette) + Heavy Water 100mB
- **Détection flux** : check via NuclearCraft API si reactor actif dans 5 blocs
- **Output** : Tritium 10mB par 200 ticks

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("tritium_breeding", "tritium_breeder", 200, 0)
  .addItemInput(<item:nexusabsolu:lithium6_ingot> * 1)
  .addFluidInput(<liquid:heavywater> * 100)
  .addEnergyPerTickInput(5000)  // pour le shielding
  .addFluidOutput(<liquid:tritium> * 10)
  .addItemOutput(<item:nexusabsolu:helium4_capsule> * 1)  // sous-produit He-4
  .build();
```

→ Dépendance forte avec [[L5-nucleaire]] qui doit être active.

---

## 🏗️ Multiblocs custom

- **MB-FILTER** (Filtration ionique, 3x3x3)
- **MB-OSMOSE** (Osmose Inverse, 3x3x3)
- **MB-TRITIUM** (Tritium Breeder, 3x3x3, doit être placé à côté d'un réacteur NC actif)

## 🧪 Fluides custom

```zenscript
val bidistilled_water = VanillaFactory.createFluid("bidistilled_water", 0xB3E5FC); bidistilled_water.register();
val tridistilled_water = VanillaFactory.createFluid("tridistilled_water", 0xE1F5FE); tridistilled_water.register();
val tritium = VanillaFactory.createFluid("tritium", 0xFFFF00); tritium.density = 1; tritium.gaseous = true; tritium.luminosity = 8; tritium.register();
// Heavy Water déjà fournie par Mekanism (mekanism:heavywater)
```

## 🪨 Items custom

- `nexusabsolu:resin_charge` (recharge filtre)
- `nexusabsolu:lithium6_ingot` (séparé par centrifugation Mekanism, Li-6 vs Li-7)
- `nexusabsolu:helium4_capsule` (sous-produit reproduction tritium)

## ⚡ Bilan énergie L2

- Distillation : passive (TEP soleil) ou 2k RF/t Mekanism (15M RF / 1000mB tridistillée si Mek)
- Osmose : 3k RF/t × 200 = 600k RF / 1000mB
- Tritium : 5k RF/t × 200 = 1M RF / 10mB tritium
- **Total estimé** : 2-15M RF par 1000mB de Tridistillée selon path choisi

## 🔗 Voir aussi

- [[../02-pipeline-overview]]
- [[L5-nucleaire]] — fournit le flux neutronique
- [[L8-botanique-manifoldine]] — destinataire Eau Lourde
