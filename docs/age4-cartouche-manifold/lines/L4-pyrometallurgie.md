# L4 — Pyrométallurgie

> Tags: #ligne #metaux #mekanism #immersive-engineering
> Objectif : extraire et purifier **les 30 éléments métalliques** (qualité chimique) + acide phosphorique
> Théorème : I (Conservation Absolue)

---

## 📥 Inputs

- **Minerais** (vanilla + IE Bauxite + NC Lithium/Beryl/Borax/Rutile + Mekanism Osmium)
- **NaOH** (de [[L3-electrolyse-cryo]] via L6)
- **Cl₂, F₂, Na, Mg** (de L3)
- **Eau Régale** (HCl + HNO₃ de [[L6-acides-ammoniaque]])
- **O₂** (de L3)
- **Argon** (de L3) — pour Kroll

## 📤 Outputs

| Catégorie | Métaux | Destination |
|-----------|--------|-------------|
| Métaux communs | Fe, Cu, Sn, Zn, Pb, Ni, Co, Mn | Composés β/δ (synthèse finale) |
| Aluminium pur | Al | Réactif β2 (Cobalt-Phthalocyanine sub) |
| Titane pur | Ti | Casing Cartouche (encartouchage) |
| Métaux précieux | Au, Ag, Pt, Ir, Os | Composés β2/β3, δ2 |
| Tungstène | W | Catalyseur CoMo (L1 HDS) |
| **H₃PO₄** (Acide Phosphorique) | — | [[L8-botanique-manifoldine]] (stabilisation Manifoldine) |

---

## 🔧 Étapes (5 sous-stations parallèles)

### L4.A — Métaux communs (Mekanism 5x ore processing — natif)
Setup standard : Crusher → Enrichment Chamber → Purification Chamber → Injection Chamber → Crystallizer → Smelter.
- **Pas de modification** nécessaire pour Fe, Cu, Sn, Zn, Pb, Ni, Co, Mn, Al brut, Osmium.
- Output : Ingots qualité métallurgique standard.

### L4.B — Hall-Héroult (Aluminium pur — multibloc MB-HALL)
Layout 5x4x5, casing Cryolithe-Resistant Iridium.
- **Process Bayer en amont** : Bauxite + NaOH → Alumine Al₂O₃ (Pressure Chamber PCC)
- **Cryolithe synthétique** : Na (L3) + Al + F (L3) → Na₃AlF₆ (recipe custom)

```zenscript
// Procédé Bayer (Pressure Chamber PCC)
mods.pneumaticcraft.PressureChamber.addRecipe(
  [<item:immersiveengineering:ore:1> * 4, <liquid:naoh_solution> * 1000],  // Bauxite + NaOH
  4.5, // bar
  [<item:nexusabsolu:alumina> * 3]
);

// Cryolithe synthétique
recipes.addShaped(<item:nexusabsolu:cryolithe_block> * 1, [
  [<item:nexusabsolu:sodium_ingot>, <item:nexusabsolu:fluorine_capsule>, <item:nexusabsolu:sodium_ingot>],
  [<item:nexusabsolu:fluorine_capsule>, <item:nexusabsolu:aluminum_ingot>, <item:nexusabsolu:fluorine_capsule>],
  [<item:nexusabsolu:sodium_ingot>, <item:nexusabsolu:fluorine_capsule>, <item:nexusabsolu:sodium_ingot>]
]);

// Hall-Héroult dans MB-HALL (électrolyse à 950°C)
mods.modularmachinery.RecipeBuilder.newBuilder("hall_heroult", "hall_heroult_cell", 800, 0)
  .addItemInput(<item:nexusabsolu:alumina> * 4)
  .addItemInput(<item:nexusabsolu:cryolithe_block> * 1)
  .addEnergyPerTickInput(150000)  // électrolyse fondue 950°C cher
  .addItemOutput(<item:nexusabsolu:aluminum_pure> * 4)
  .addFluidOutput(<liquid:oxygen> * 200)  // O₂ libéré
  .build();
```

### L4.C — Procédé Kroll (Titane — multibloc MB-KROLL)
Layout 4x5x4, casing Reinforced Iridium + cuve Magnésium fondu.
- **Process en 3 étapes** :
  1. Rutile (TiO₂) + Cl₂ + C → TiCl₄ liquide (chloruration)
  2. TiCl₄ + Mg fondu **sous Argon** (atmosphère inerte) → Ti éponge (réduction)
  3. Refonte sous arc électrique → Ti métal pur

```zenscript
// Étape 1 — Chloruration (Pressure Chamber PCC, 800°C)
mods.modularmachinery.RecipeBuilder.newBuilder("rutile_chlorination", "kroll_reactor", 200, 0)
  .addItemInput(<item:nuclearcraft:gem_dust:8> * 4)  // Rutile dust
  .addFluidInput(<liquid:chlorine_gas> * 400)
  .addItemInput(<item:minecraft:coal> * 2)
  .addEnergyPerTickInput(20000)
  .addFluidOutput(<liquid:ticl4> * 1000)
  .addFluidOutput(<liquid:carbon_dioxide> * 200)
  .build();

// Étape 2 — Réduction Kroll (sous Argon !)
mods.modularmachinery.RecipeBuilder.newBuilder("kroll_reduction", "kroll_reactor", 600, 0)
  .addFluidInput(<liquid:ticl4> * 1000)
  .addItemInput(<item:nexusabsolu:magnesium_ingot> * 4)
  .addFluidInput(<liquid:argon> * 100)  // atmosphère inerte
  .addEnergyPerTickInput(50000)
  .addItemOutput(<item:nexusabsolu:titanium_sponge> * 4)
  .addItemOutput(<item:nexusabsolu:magnesium_chloride> * 4)  // sous-produit (recyclable)
  .build();

// Étape 3 — Refonte arc (Mekanism Smelting Factory ou Smeltery natif)
recipes.addShapeless(<item:nexusabsolu:titanium_pure> * 1, [
  <item:nexusabsolu:titanium_sponge>
]);
// + recipe Arc Furnace IE multi-step si on veut être plus fin
```

### L4.D — Métaux précieux à l'eau régale (multibloc MB-AQUA-REGIA)
Layout 3x3x3, casing résistant acide (Quantum Glass + Iridium).
- **Eau Régale** = HCl + HNO₃ ratio 3:1 (de [[L6-acides-ammoniaque]])

```zenscript
// Dissolution sélective Or
mods.modularmachinery.RecipeBuilder.newBuilder("gold_aqua_regia", "aqua_regia_cell", 200, 0)
  .addItemInput(<item:minecraft:gold_ingot> * 1)
  .addFluidInput(<liquid:aqua_regia> * 100)
  .addEnergyPerTickInput(1000)
  .addFluidOutput(<liquid:gold_chloride_solution> * 100)
  .build();

// Précipitation avec Fer (réducteur)
mods.modularmachinery.RecipeBuilder.newBuilder("gold_precipitation", "aqua_regia_cell", 100, 0)
  .addFluidInput(<liquid:gold_chloride_solution> * 100)
  .addItemInput(<item:nexusabsolu:iron_dust_pure> * 1)
  .addItemOutput(<item:nexusabsolu:gold_pure_99> * 1)
  .addItemOutput(<item:nexusabsolu:iron_chloride_dust> * 1)
  .build();

// Idem pour Platinum, Iridium, Osmium (recettes parallèles avec différents réducteurs)
```

### L4.E — Acide Phosphorique (consolidé ici)
- P (de L3) + O₂ (de L3) → P₂O₅
- P₂O₅ + H₂O Tridistillée → H₃PO₄

```zenscript
mods.mekanism.reaction.addRecipe(
  <item:nexusabsolu:phosphorus_white> * 4,
  <gas:oxygen> * 100,
  <liquid:tridistilled_water> * 100,
  <item:nexusabsolu:p2o5_capsule> * 1,  // intermédiaire
  <gas:hydrogen> * 50,  // sous-produit
  100,  // duration
  100   // energy
);

mods.modularmachinery.RecipeBuilder.newBuilder("phosphoric_acid_synthesis", "fluid_mixer", 100, 0)
  .addItemInput(<item:nexusabsolu:p2o5_capsule> * 1)
  .addFluidInput(<liquid:tridistilled_water> * 300)
  .addFluidOutput(<liquid:h3po4> * 300)
  .build();
```

---

## 🏗️ Multiblocs custom

- **MB-HALL** (Hall-Héroult, 5x4x5)
- **MB-KROLL** (Réacteur Kroll, 4x5x4)
- **MB-AQUA-REGIA** (Cellule Eau Régale, 3x3x3)

## 🧪 Fluides custom

```zenscript
val ticl4 = VanillaFactory.createFluid("ticl4", 0xFFC107); ticl4.density = 1730; ticl4.register();
val carbon_dioxide = VanillaFactory.createFluid("carbon_dioxide", 0xBDBDBD); carbon_dioxide.gaseous = true; carbon_dioxide.register();
val aqua_regia = VanillaFactory.createFluid("aqua_regia", 0xFF9800); aqua_regia.luminosity = 4; aqua_regia.register();
val gold_chloride_solution = VanillaFactory.createFluid("gold_chloride_solution", 0xFFB300); gold_chloride_solution.register();
val h3po4 = VanillaFactory.createFluid("h3po4", 0xFFE0B2); h3po4.register();
```

## 🪨 Items custom

- `nexusabsolu:alumina`, `nexusabsolu:cryolithe_block`, `nexusabsolu:aluminum_pure`
- `nexusabsolu:titanium_sponge`, `nexusabsolu:titanium_pure`
- `nexusabsolu:magnesium_ingot`, `nexusabsolu:magnesium_chloride` (recyclable → Mg via électrolyse)
- `nexusabsolu:gold_pure_99`, `nexusabsolu:platinum_pure_99`, `nexusabsolu:iridium_pure_99`, `nexusabsolu:osmium_pure_99`
- `nexusabsolu:iron_dust_pure`, `nexusabsolu:iron_chloride_dust`
- `nexusabsolu:p2o5_capsule`
- `nexusabsolu:fluorine_capsule` (Fluor liquéfié pour usage solide)
- `nexusabsolu:sodium_ingot` (Na solidifié)

## ⚡ Bilan énergie L4

- Hall-Héroult : 150k RF/t × 800 = 120M RF / cycle (4 lingots Al)
- Kroll étape 1+2 : 70k × 800 = 56M RF / cycle (4 lingots Ti)
- Eau régale : 1k × 200 = 200k RF / cycle (1 lingot or)
- **Pic L4 si tout tourne** : ~250k RF/t

## 🔗 Voir aussi

- [[../02-pipeline-overview]]
- [[L1-petrochimie]] — destinataire Tungstène (catalyseur CoMo)
- [[L8-botanique-manifoldine]] — destinataire H₃PO₄ (stabilisation Manifoldine)
