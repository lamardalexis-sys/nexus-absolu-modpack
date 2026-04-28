# L7 — Organique + Acétone

> Tags: #ligne #organique #acetone #pneumaticcraft #cumene
> Objectif : produire **Acétone** (procédé Cumène) + **Tryptamide-M** (précurseur Manifoldine) + solvants auxiliaires
> Théorèmes : I (Conservation) + IV (Sanguine — pré-précurseur de l'effet bio)

---

## 📥 Inputs

- **Naphta léger** (de [[L1-petrochimie]])
- **Gaz Naturel** (méthane/éthane/propane, de L1) → cracking → Propylène
- **Ammoniaque NH₃** (de [[L6-acides-ammoniaque]])
- **H₂SO₄** (catalytique, de L6)
- **O₂** (de [[L3-electrolyse-cryo]])
- **Pd, Pt** (catalyseurs, de [[L4-pyrometallurgie]])
- **CO + H₂** (Syngas, de L1)
- **Sucre / blé** (Pam's HarvestCraft pour fermentation Éthanol)

## 📤 Outputs

| Produit | Qté/cycle | Destination |
|---------|-----------|-------------|
| **Acétone** ⭐ | 200mB | [[L8-botanique-manifoldine]] (Soxhlet Manifoldine) |
| **Phénol** | 200mB | [[L8-botanique-manifoldine]] (sub-recipe Phthalocyanine) |
| **Tryptamide-M** | 1 capsule | [[L8-botanique-manifoldine]] (Cyclisateur Stellaire) |
| **Indole** (intermédiaire) | 1 dust | [[L8-botanique-manifoldine]] (via Tryptamide-M) |
| **Méthanol** | 100mB | [[L8-botanique-manifoldine]] (élution chromatographique) |
| **Éthanol** | 100mB | Composé ε (Solution Manifoldine Active) |

---

## 🔧 Étapes

### L7.A — Procédé Cumène (Acétone — voie principale) ⭐

**Phase 1 — Reformage du Naphta** (Pressure Chamber PCC + catalyseur Pt/Re custom)
Extrait le **Benzène** du Naphta léger.
```zenscript
mods.pneumaticcraft.PressureChamber.addRecipe(
  [<liquid:naphtha> * 1000, <item:nexusabsolu:pt_re_catalyst> * 1],
  3.5,
  [<liquid:benzene> * 400, <liquid:hydrogen> * 200]  // bonus H₂
);
```

**Phase 2 — Cracking thermique du Gaz Naturel** (multibloc MB-CRACKER 3x5x3)
Extrait le **Propylène** du gaz naturel.
```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("propylene_cracking", "thermal_cracker", 300, 0)
  .addFluidInput(<liquid:natural_gas> * 1000)
  .addEnergyPerTickInput(15000)
  .addFluidOutput(<liquid:propylene> * 600)
  .addFluidOutput(<liquid:methane> * 200)
  .addFluidOutput(<liquid:hydrogen> * 200)
  .build();
```

**Phase 3 — Synthèse Cumène** (Pressure Chamber PCC + catalyseur acide solide)
Benzène + Propylène + H₂SO₄ catalytique → Cumène (isopropylbenzène).
```zenscript
mods.pneumaticcraft.PressureChamber.addRecipe(
  [<liquid:benzene> * 400, <liquid:propylene> * 400, <liquid:h2so4> * 50],
  4.5,
  [<liquid:cumene> * 800, <liquid:h2so4> * 50]  // catalyseur récupéré
);
```

**Phase 4 — Oxydation + Clivage** (multibloc MB-CUMENE 3x4x3)
Cumène + O₂ → hydroperoxyde → + H₂SO₄ → Phénol + Acétone (le fameux clivage).
```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("cumene_oxidation_cleavage", "cumene_reactor", 500, 0)
  .addFluidInput(<liquid:cumene> * 800)
  .addFluidInput(<liquid:oxygen> * 400)
  .addFluidInput(<liquid:h2so4> * 20)  // catalytique
  .addEnergyPerTickInput(10000)
  .addFluidOutput(<liquid:phenol> * 400)
  .addFluidOutput(<liquid:acetone> * 400)
  .addFluidOutput(<liquid:h2so4> * 20)  // récupéré
  .build();
```

→ **2 produits stratégiques d'un coup** : Acétone (Manifoldine) + Phénol (Phthalocyanine).

### L7.B — Voie Isopropanol (backup Acétone)
Plus simple si t'as pas envie de monter Cumène en premier.
```zenscript
// Propylène + H₂O → Isopropanol (catalyseur acide)
mods.modularmachinery.RecipeBuilder.newBuilder("ipa_synthesis", "fluid_mixer", 200, 0)
  .addFluidInput(<liquid:propylene> * 400)
  .addFluidInput(<liquid:tridistilled_water> * 400)
  .addItemInput(<item:nexusabsolu:acid_catalyst> * 1)
  .addEnergyPerTickInput(5000)
  .addFluidOutput(<liquid:isopropanol> * 400)
  .build();

// Isopropanol → Acétone (déshydrogénation Cu/Zn 400°C)
mods.modularmachinery.RecipeBuilder.newBuilder("ipa_dehydrogenation", "thermal_cracker", 200, 0)
  .addFluidInput(<liquid:isopropanol> * 400)
  .addItemInput(<item:nexusabsolu:cu_zn_catalyst> * 1)
  .addEnergyPerTickInput(8000)
  .addFluidOutput(<liquid:acetone> * 400)
  .addFluidOutput(<liquid:hydrogen> * 200)
  .build();
```

### L7.C — Synthèse Indole (multibloc MB-AROMATIC 3x3x3)
**Indole** = précurseur du Tryptamide-M. Synthèse à partir Naphta + NH₃ + catalyseur Pt/Al₂O₃.
```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("indole_synthesis", "aromatic_reactor", 600, 0)
  .addFluidInput(<liquid:naphtha> * 200)
  .addFluidInput(<liquid:ammoniaque> * 100)
  .addItemInput(<item:nexusabsolu:pt_al2o3_catalyst> * 1)
  .addEnergyPerTickInput(25000)
  .addItemOutput(<item:nexusabsolu:indole_dust> * 2)
  .build();
```

### L7.D — Synthèse Tryptamide-M ⭐ (composé fictif Manifoldine-bound)
Indole + NH₃ + Acétone + chaleur sur Pd/C → **Tryptamide-M** (composé fictif propre au lore Manifold).
```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("tryptamide_m_synthesis", "aromatic_reactor", 800, 0)
  .addItemInput(<item:nexusabsolu:indole_dust> * 2)
  .addFluidInput(<liquid:ammoniaque> * 100)
  .addFluidInput(<liquid:acetone> * 100)
  .addItemInput(<item:nexusabsolu:pd_c_catalyst> * 1)
  .addEnergyPerTickInput(30000)
  .addItemOutput(<item:nexusabsolu:tryptamide_m_capsule> * 1)
  .build();
```

### L7.E — Solvants auxiliaires

**Méthanol** (Syngas + Cu/ZnO catalyseur)
```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("methanol_synthesis", "thermal_cracker", 300, 0)
  .addFluidInput(<liquid:syngas> * 400)  // Syngas = CO + H₂, à définir
  .addItemInput(<item:nexusabsolu:cu_zno_catalyst> * 1)
  .addEnergyPerTickInput(10000)
  .addFluidOutput(<liquid:methanol> * 200)
  .build();
```

**Éthanol** (fermentation Pam's + distillation)
```zenscript
// Industrial Foregoing Bioreactor recipe custom, ou MM Fermentation
mods.modularmachinery.RecipeBuilder.newBuilder("ethanol_fermentation", "fermenter", 1000, 0)
  .addItemInput(<ore:cropWheat> * 8)  // ou autre crop Pam's
  .addFluidInput(<liquid:tridistilled_water> * 200)
  .addEnergyPerTickInput(500)
  .addFluidOutput(<liquid:ethanol> * 100)
  .build();
```

---

## 🏗️ Multiblocs custom

- **MB-CRACKER** (3x5x3, cracking thermique gaz naturel)
- **MB-CUMENE** (3x4x3, oxydation + clivage Cumène)
- **MB-AROMATIC** (3x3x3, synthèse Indole + Tryptamide-M)
- **MB-FERMENTER** (3x3x3, fermentation Éthanol — pourrait sub Industrial Foregoing Bioreactor)

## 🧪 Fluides custom

```zenscript
val naphtha = VanillaFactory.createFluid("naphtha", 0xFFE082); naphtha.register();  // pourrait sub PCC
val benzene = VanillaFactory.createFluid("benzene", 0xFFF59D); benzene.register();
val propylene = VanillaFactory.createFluid("propylene", 0xCFD8DC); propylene.gaseous = true; propylene.register();
val cumene = VanillaFactory.createFluid("cumene", 0xFFCC80); cumene.register();
val phenol = VanillaFactory.createFluid("phenol", 0xFF8A65); phenol.register();
val acetone = VanillaFactory.createFluid("acetone", 0xE1F5FE); acetone.viscosity = 320; acetone.register();
val isopropanol = VanillaFactory.createFluid("isopropanol", 0xE0F7FA); isopropanol.register();
val methanol = VanillaFactory.createFluid("methanol", 0xF1F8E9); methanol.register();
val ethanol = VanillaFactory.createFluid("ethanol", 0xFFFDE7); ethanol.register();
val syngas = VanillaFactory.createFluid("syngas", 0xB0BEC5); syngas.gaseous = true; syngas.register();
val methane = VanillaFactory.createFluid("methane", 0xECEFF1); methane.gaseous = true; methane.register();
```

## 🪨 Items custom

- `nexusabsolu:pt_re_catalyst`, `nexusabsolu:cu_zn_catalyst`, `nexusabsolu:pt_al2o3_catalyst`, `nexusabsolu:pd_c_catalyst`, `nexusabsolu:cu_zno_catalyst`, `nexusabsolu:acid_catalyst`
- `nexusabsolu:indole_dust`
- `nexusabsolu:tryptamide_m_capsule` ⭐ (précurseur Manifoldine)

## ⚡ Bilan énergie L7

- Cumène complet : 30M RF / cycle (4 phases × ~250 ticks chacune)
- Tryptamide-M : 30k × 800 = 24M RF / capsule
- Indole : 25k × 600 = 15M RF / cycle
- **Pic L7 si tout tourne** : ~80k RF/t

## 🔗 Voir aussi

- [[../02-pipeline-overview]]
- [[L1-petrochimie]] — fournit Naphta + Gaz Naturel
- [[L8-botanique-manifoldine]] — destinataire Acétone + Tryptamide-M + Méthanol + Éthanol (4 produits convergent vers L8)
