# L5 — Nucléaire

> Tags: #ligne #nuclear #nuclearcraft #composes-gamma
> Objectif : produire **U-235, Plutonium, Thorium, Béryllium, Bore** + **3 composés γ** + **flux neutronique** pour Mycélium Activé
> Théorèmes : I (Conservation) + V (Brisure Dimensionnelle — irradiation Mycélium)

---

## 📥 Inputs

- **Minerai d'Uranium, Thorium, Béryllium (Beryl), Bore (Borax)** (NuclearCraft)
- **Fluor F₂** (de [[L3-electrolyse-cryo]])
- **Magnésium** (de [[L4-pyrometallurgie]])
- **H₂SO₄** (de [[L6-acides-ammoniaque]]) — pour solution d'uranyle
- **Tritium T₂** (de [[L2-hydro-eau]]) — pour ⁶LiT
- **Lithium-6 ingot** (séparé via Mekanism Centrifugal Separator depuis Li naturel de L3)

## 📤 Outputs

| Produit | Qté/cycle | Destination |
|---------|-----------|-------------|
| **U-235 enrichi** | 5% mass | Composé γ1 (UF₆) |
| **Plutonium-239** | 1 ingot | Composé γ2 (Pu-Be borate) |
| **Thorium** | 1 ingot | Composé γ2 (alternatif U-233) |
| **Béryllium** | 1 ingot | Composé γ2 |
| **Bore** | 1 ingot | Composé γ2 (absorbeur) |
| **γ1 — UF₆ enrichi** | 100mB | M2 Bio-Réacteur |
| **γ2 — Pu-Be Borate** | 1 capsule | M2 Bio-Réacteur |
| **γ3 — ⁶LiT (Tritiure de Lithium)** | 1 capsule | M2 Bio-Réacteur |
| **Flux neutronique** | passive | [[L8-botanique-manifoldine]] (Mycélium Activé) + [[L2-hydro-eau]] (Tritium breeding) |

---

## 🔧 Étapes

### L5.1 — Extraction Uranium (Mekanism + recipe custom)
- Uranium Ore → Crusher → Uranium Dust → Reaction Chamber avec H₂SO₄ → Solution Uranyle
```zenscript
mods.mekanism.reaction.addRecipe(
  <item:nuclearcraft:dust:23> * 1,  // Uranium Dust
  <gas:oxygen> * 50,
  <liquid:h2so4> * 200,
  <item:nexusabsolu:uranyl_dust> * 1,
  <gas:hydrogen> * 50,
  100, 200
);
```

### L5.2 — Fluoration UF₆ (Pressure Chamber PCC)
- Uranyle + F₂ → UF₆ gazeux
```zenscript
mods.pneumaticcraft.PressureChamber.addRecipe(
  [<item:nexusabsolu:uranyl_dust> * 1, <liquid:fluorine_gas> * 600],
  4.5,
  [<liquid:uf6_gas> * 100]
);
```

### L5.3 — Centrifugation isotopique (cascade NuclearCraft Centrifuges)
Setup en cascade : 6 centrifugeuses NC en série, ratio enrichissement augmente à chaque étape.
- Recettes natives NuclearCraft (Centrifuge sépare U-238/U-235)
- Output final : U-235 enrichi à 5% (qualité réacteur de puissance)

### L5.4 — Réacteur Breeder (NuclearCraft Fission Reactor configuré)
Config breeder :
- Fuel cells : MOX-239 ou Th-232 selon target
- Modérateur : Graphite Block + Eau Lourde (de L2)
- Output U-238 → Pu-239 / Th-232 → U-233
- **Bonus** : émet du **flux neutronique** dans un rayon de 5 blocs autour → utilisé par L2 (Tritium) et L8 (Mycélium Activé)

### L5.5 — Béryllium (procédé classique)
- BeF₂ (Beryl + F₂) → réduction par Mg fondu (de L4)
```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("beryllium_reduction", "kroll_reactor", 400, 0)
  .addItemInput(<item:nexusabsolu:bef2_dust> * 2)
  .addItemInput(<item:nexusabsolu:magnesium_ingot> * 1)
  .addFluidInput(<liquid:argon> * 100)
  .addEnergyPerTickInput(30000)
  .addItemOutput(<item:nexusabsolu:beryllium_pure> * 2)
  .addItemOutput(<item:nexusabsolu:magnesium_fluoride> * 1)
  .build();
```

### L5.6 — Bore (électrolyse fondue)
- Borax → B₂O₃ → électrolyse → Bore métal
```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("boron_electrolysis", "fluorite_cell", 600, 0)
  .addItemInput(<item:nexusabsolu:b2o3_dust> * 2)
  .addEnergyPerTickInput(60000)
  .addItemOutput(<item:nexusabsolu:boron_pure> * 1)
  .addFluidOutput(<liquid:oxygen> * 100)
  .build();
```

### L5.7 — Composé γ2 (Borate de Plutonium-Béryllium) ⭐
Multibloc MB-GAMMA-FORGE 3x3x3 sous Argon (atmosphère inerte).
```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("gamma2_pube_borate", "gamma_forge", 1000, 0)
  .addItemInput(<item:nexusabsolu:plutonium239_ingot> * 1)
  .addItemInput(<item:nexusabsolu:beryllium_pure> * 1)
  .addItemInput(<item:nexusabsolu:boron_pure> * 1)
  .addFluidInput(<liquid:argon> * 200)
  .addEnergyPerTickInput(40000)
  .addItemOutput(<item:nexusabsolu:gamma2_pube_borate_capsule> * 1)
  .build();
```

### L5.8 — Composé γ3 (Tritiure de Lithium ⁶LiT)
Multibloc MB-LIT-CHAMBER 3x3x3, à 250°C.
```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("gamma3_lit", "lit_chamber", 600, 0)
  .addItemInput(<item:nexusabsolu:lithium6_ingot> * 1)
  .addFluidInput(<liquid:tritium> * 50)
  .addEnergyPerTickInput(15000)
  .addItemOutput(<item:nexusabsolu:gamma3_lit_capsule> * 1)
  .build();
```

### L5.9 — Activation du Mycélium pour L8 ⭐ (Théorème V)
Critère unique : un block `minecraft:mycelium` placé dans un rayon de 5 blocs autour d'un fission reactor NC actif pendant 1 nuit Minecraft (24000 ticks) **se transforme** en `nexusabsolu:mycelium_active`.

→ Implémentation : KubeJS event listener sur `block.tick` qui vérifie la présence d'un reactor NC actif à proximité.

```javascript
// kubejs/server_scripts/age4_mycelium_activation.js
events.listen('player.tick', event => {
  // Run only every 200 ticks to save perf
  if (event.player.age % 200 !== 0) return;
  // Find mycelium blocks near the player
  // For each, check if NC reactor is active within 5 blocks
  // Track exposure time, transmute when threshold reached
});
```

→ Voir [[../effects/mycelium-activation.md]] pour code complet.

---

## 🏗️ Multiblocs custom

- **MB-GAMMA-FORGE** (3x3x3, fonderie composé γ2)
- **MB-LIT-CHAMBER** (3x3x3, chambre Tritiure de Lithium γ3)
- **MB-CASCADE-CENTRI** (cascade 6 centrifugeuses NC, infrastructure plutôt que multibloc — déjà natif NC)

## 🧪 Fluides custom

```zenscript
val uf6_gas = VanillaFactory.createFluid("uf6_gas", 0x9C27B0); uf6_gas.gaseous = true; uf6_gas.luminosity = 6; uf6_gas.register();
```

## 🪨 Items custom

- `nexusabsolu:uranyl_dust`
- `nexusabsolu:bef2_dust`, `nexusabsolu:b2o3_dust`
- `nexusabsolu:beryllium_pure`, `nexusabsolu:boron_pure`
- `nexusabsolu:plutonium239_ingot`, `nexusabsolu:thorium_ingot` (peuvent réutiliser items NC natifs)
- `nexusabsolu:magnesium_fluoride` (sous-produit, recyclable)
- `nexusabsolu:gamma1_uf6_capsule` (UF₆ stabilisé en capsule)
- `nexusabsolu:gamma2_pube_borate_capsule`
- `nexusabsolu:gamma3_lit_capsule`
- `nexusabsolu:mycelium_active` (block, transformé du mycélium standard)

## ⚡ Bilan énergie L5

- Centrifugation cascade : 200k RF/t pendant longues durées (NC standard)
- MB-GAMMA-FORGE : 40k × 1000 = 40M RF / capsule γ2
- MB-LIT-CHAMBER : 15k × 600 = 9M RF / capsule γ3
- **Pic L5 quand cascade active** : ~300k RF/t

## ⚠️ Sécurité jeu (lore + équilibrage)

- Le breeder reactor doit être **bien shielded** (Casing au moins Glowing Mushroom Block ou meilleur, NC standard)
- Le **Mycélium Activé** émet de légères particules vertes (visuelles uniquement)
- Pas de radiation player-damaging si shielding correct (NC config standard)

## 🔗 Voir aussi

- [[../02-pipeline-overview]]
- [[../01-lore-integration]] — Théorème V (Brisure)
- [[L8-botanique-manifoldine]] — Mycélium Activé → Spores → Manifoldine
- [[L2-hydro-eau]] — Tritium breeding nécessite proximité reactor
