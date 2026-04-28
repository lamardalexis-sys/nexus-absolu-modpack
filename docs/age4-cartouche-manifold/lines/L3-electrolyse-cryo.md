# L3 — Électrolyse + Cryogénie

> Tags: #ligne #electrolyse #cryogenie #argon #thermal-cryotheum
> Objectif : produire les **gaz industriels** (N₂/O₂/Ar) + **sels électrolysés** (Na/Cl/Li/P/F/H)
> Théorème : I (Conservation Absolue)

---

## 📥 Inputs

- **Air ambiant** (Air Intake Block custom)
- **Gelid Cryotheum** (Thermal Foundation, recette native)
- **Eau Tridistillée** (de [[L2-hydro-eau]])
- **Saumure / Brine** (de Mekanism TEP)

## 📤 Outputs

| Produit | Qté/cycle | Destination |
|---------|-----------|-------------|
| **N₂** (Azote) | 780mB | [[L6-acides-ammoniaque]] (Haber-Bosch NH₃) |
| **O₂** (Oxygène) | 210mB | [[L1-petrochimie]] (Claus), [[L4-pyrometallurgie]] (Bessemer), [[L6-acides-ammoniaque]] (Ostwald) |
| **Argon** | 10mB | [[L4-pyrometallurgie]] (Kroll), [[L8-botanique-manifoldine]] (cyclisation), encartouchage final |
| **Na** liquide | 50mB | [[L4-pyrometallurgie]] (Bayer), [[L6-acides-ammoniaque]] (NaOH) |
| **Cl₂** | 50mB | [[L4-pyrometallurgie]] (Kroll TiCl₄), [[L6-acides-ammoniaque]] (HCl) |
| **NaOH** | 100mB | [[L6-acides-ammoniaque]] (base forte) |
| **H₂** | 200mB | [[L1-petrochimie]] (HDS), [[L6-acides-ammoniaque]] (Haber-Bosch) |
| **Li** ingot | 1 | [[L5-nucleaire]] (Li-6 → Tritium) |
| **P** (Phosphore) | 1 | [[L4-pyrometallurgie]] (H₃PO₄), [[L1-petrochimie]] (tensioactif) |
| **F₂** (Fluor) | 50mB | [[L4-pyrometallurgie]] (Cryolithe), [[L5-nucleaire]] (UF₆, IrF₆) |

---

## 🔧 Étapes

### L3.A — Cryo-Distillation Atmosphérique (multibloc custom MB-CRYO-ATM) ⭐

Layout vertical 3x6x3 :
```
Couche 5 (top): [3 Output Hatch fluide N₂/Ar/O₂]
Couche 4: [Quantum Glass × 9] — colonne de fractionnement
Couche 3: [Iridium Casing × 8 + plateau distillation centre]
Couche 2: [Iridium Casing × 8 + plateau distillation centre]
Couche 1: [Reinforced Iridium × 9] — chambre de refroidissement
Couche 0 (sol): [Air Intake centre + Cryotheum Input + Energy Input + Casings]
```

**Process en 4 phases (1 recette MM = 60s) :**
1. Aspiration air ambiant (10 000mB)
2. Compression 200 bars (consomme RF + air)
3. Refroidissement à -196°C (consomme Cryotheum)
4. Fractionnement 3 plateaux → 3 outputs

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("cryo_air_distillation", "cryo_atmospheric", 1200, 0)
  .addItemInput(<item:nexusabsolu:air_canister> * 10)  // sub: Air Intake produit ces canisters
  .addFluidInput(<liquid:cryotheum> * 200)
  .addEnergyPerTickInput(2000000)  // 2M RF/t — c'est cher
  .addFluidOutput(<liquid:nitrogen_liquid> * 7800)
  .addFluidOutput(<liquid:oxygen> * 2100)
  .addFluidOutput(<liquid:argon> * 100)
  .addItemOutput(<item:nexusabsolu:cryotheum_used> * 1)  // refroidi → recyclable
  .build();
```

→ **Argon = 1% de l'air**. Pour 100mB d'argon, t'extrais 7800mB de N₂ et 2100mB d'O₂ en passant. Donc **un seul multibloc alimente N₂/O₂/Ar** pour toute l'usine.

### L3.B — Cellule Castner-Kellner (multibloc custom MB-CK)

Layout 5x3x3 horizontal (cellule électrolytique au mercure).
- **Cathode mercure** : bloc custom Mercury Pool (item input mercury 1L réutilisable)
- **Anode graphite** : 4 blocs Graphite Block (sub-recipe à crafter)
- **Casing** : Reinforced Iridium

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("castner_kellner", "ck_cell", 300, 0)
  .addFluidInput(<liquid:brine> * 1000)
  .addEnergyPerTickInput(50000)  // électrolyse fondue cher
  .addFluidOutput(<liquid:sodium_liquid> * 50)
  .addFluidOutput(<liquid:chlorine_gas> * 50)
  .addFluidOutput(<liquid:naoh_solution> * 100)
  .addFluidOutput(<liquid:hydrogen> * 100)  // bonus
  .build();
```

### L3.C — Cellules d'électrolyse spécialisées (Mekanism Electrolytic Separator + recettes custom)

Recettes custom via CrT pour chaque sel :

```zenscript
// Eau pure → H₂ + O₂ (vanilla Mekanism, on utilise tel quel)

// LiCl fondu → Li + Cl₂ (660°C, donc nécessite chauffage)
// On utilise Mekanism Electrolytic Separator avec input custom
mods.mekanism.electrolyticseparator.addRecipe(
  <liquid:licl_fondu> * 100,
  <liquid:lithium_liquid> * 50,
  <liquid:chlorine_gas> * 50
);

// Phosphate fondu → Phosphore (1500°C, multibloc custom alternative)
// Cf MB-FOUR-ELEC ci-dessous

// Fluorite CaF₂ → Fluor F₂ (multibloc custom)
mods.modularmachinery.RecipeBuilder.newBuilder("fluorite_electrolysis", "fluorite_cell", 600, 0)
  .addItemInput(<item:nexusabsolu:fluorite_dust> * 4)
  .addEnergyPerTickInput(80000)
  .addFluidOutput(<liquid:fluorine_gas> * 50)
  .addItemOutput(<item:nexusabsolu:calcium_dust> * 2)
  .build();
```

### L3.D — Four électrique pour Phosphore (multibloc MB-FOUR-ELEC)
Layout 5x4x5, casing Reinforced Iridium + Heating Coils Mekanism (custom item).
- **Inputs** : Phosphate de Calcium (Ca₃(PO₄)₂) + Silice + Carbone
- **Process** : 1500°C arc électrique → Phosphore Blanc + scories

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("phosphorus_white", "electric_furnace", 800, 0)
  .addItemInput(<item:nexusabsolu:calcium_phosphate> * 3)
  .addItemInput(<item:minecraft:sand> * 4)  // silice
  .addItemInput(<item:minecraft:coal> * 4)
  .addEnergyPerTickInput(100000)
  .addItemOutput(<item:nexusabsolu:phosphorus_white> * 2)
  .addItemOutput(<item:nexusabsolu:slag_silicate> * 3)
  .build();
```

### L3.E — Cryotheum cycling
Le Cryotheum utilisé est récupérable. Recyclage simple :
- 1 Cryotheum Used + 1 Blizz Powder → 1 Gelid Cryotheum (Thermal recipe modifiée via CrT)

```zenscript
mods.thermalexpansion.Compactor.addRecipe(
  <liquid:cryotheum>,
  <item:nexusabsolu:cryotheum_used>,
  <item:thermalfoundation:material:1024> // Blizz Powder
);
```

---

## 🏗️ Multiblocs custom

- **MB-CRYO-ATM** (Cryo-Distillation Atmosphérique, 3x6x3) ⭐ — voir [[../multiblocs/MB-CRYO-ATM]]
- **MB-CK** (Cellule Castner-Kellner, 5x3x3) — voir [[../multiblocs/MB-CK]]
- **MB-FOUR-ELEC** (Four électrique haute T, 5x4x5)
- **MB-FLUORITE** (Cellule fluorite, 3x3x3)

## 🧪 Fluides custom

```zenscript
val nitrogen_liquid = VanillaFactory.createFluid("nitrogen_liquid", 0xE3F2FD); nitrogen_liquid.density = 808; nitrogen_liquid.register();
val argon = VanillaFactory.createFluid("argon", 0xCE93D8); argon.density = 1400; argon.gaseous = true; argon.register();
val sodium_liquid = VanillaFactory.createFluid("sodium_liquid", 0xFFEB3B); sodium_liquid.density = 970; sodium_liquid.luminosity = 5; sodium_liquid.register();
val chlorine_gas = VanillaFactory.createFluid("chlorine_gas", 0xC8E6C9); chlorine_gas.gaseous = true; chlorine_gas.register();
val lithium_liquid = VanillaFactory.createFluid("lithium_liquid", 0xF5F5F5); lithium_liquid.density = 530; lithium_liquid.register();
val licl_fondu = VanillaFactory.createFluid("licl_fondu", 0xFFCCBC); licl_fondu.density = 1500; licl_fondu.register();
val fluorine_gas = VanillaFactory.createFluid("fluorine_gas", 0xFFF59D); fluorine_gas.gaseous = true; fluorine_gas.register();
val naoh_solution = VanillaFactory.createFluid("naoh_solution", 0xFFFFFF); naoh_solution.register();
// Oxygen et Hydrogen déjà fournis par Mekanism (mekanism:oxygen, mekanism:hydrogen)
```

## 🪨 Items custom

- `nexusabsolu:air_canister` (item produit par Air Intake Block)
- `nexusabsolu:air_intake_block` (block fonctionnel à poser à l'air libre)
- `nexusabsolu:cryotheum_used` (recyclage)
- `nexusabsolu:phosphorus_white` (pyrophorique, brille)
- `nexusabsolu:calcium_phosphate`
- `nexusabsolu:fluorite_dust`
- `nexusabsolu:slag_silicate` (déchet)
- `nexusabsolu:graphite_block`
- `nexusabsolu:mercury_pool` (consommable réutilisable)

## ⚡ Bilan énergie L3

- **MB-CRYO-ATM** : 2M RF/t × 1200 ticks = **2.4 milliards RF par cycle** (gros) → produit ~78 buckets gaz industriels d'un coup → rentabilisé sur la durée
- Castner-Kellner : 50k × 300 = 15M RF / cycle
- Four électrique : 100k × 800 = 80M RF / cycle
- Fluorite : 80k × 600 = 48M RF / cycle
- **Pic L3 quand tout tourne** : ~3M RF/t (gros consommateur)

## 🔗 Voir aussi

- [[../02-pipeline-overview]]
- [[L5-nucleaire]] — destinataire Fluor pour UF₆/IrF₆
- [[L4-pyrometallurgie]] — destinataire Na/Cl/F pour Hall-Héroult/Kroll
- [[L6-acides-ammoniaque]] — destinataire N₂/O₂/H₂/NaOH pour acides
