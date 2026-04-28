# L6 — Acides + Ammoniaque (Hub Central)

> Tags: #ligne #acides #ammoniaque #hub-central #haber-bosch
> Objectif : produire **NH₃ (pivot)** + **5 acides forts** + **NaOH** + **Eau Régale**
> Théorème : I (Conservation Absolue)

---

## 📥 Inputs (depuis presque toutes les lignes)

- **N₂, O₂, H₂, Cl₂, F₂** (de [[L3-electrolyse-cryo]])
- **NaOH** (sous-produit Castner-Kellner L3, concentré ici)
- **H₂S** (de [[L1-petrochimie]] — recyclage)
- **Soufre élémentaire** (récupéré de H₂S via Claus)
- **Eau Tridistillée** (de [[L2-hydro-eau]])
- **Platine** (catalyseur Ostwald, de [[L4-pyrometallurgie]])
- **Fer** (catalyseur Haber-Bosch, de L4)

## 📤 Outputs (vers presque toutes les lignes)

| Produit | Qté/cycle | Destinations |
|---------|-----------|--------------|
| **NH₃ (Ammoniaque)** ⭐ pivot | 500mB | L7 (Indole), L8 (Soxhlet Manifoldine), 6.B (HNO₃) |
| **HNO₃ (Acide Nitrique)** | 200mB | L4 (Eau Régale or/platine), δ2 (Argent colloïdal) |
| **H₂SO₄ (Acide Sulfurique)** | 300mB | L4 (Bayer), L5 (Uranyle), L8 (champis sombres) |
| **HCl (Acide Chlorhydrique)** | 200mB | L4 (Eau Régale), L8 (champis doux) |
| **NaOH** (concentré) | 100mB | L4 (Bayer Aluminium) |
| **Eau Régale (Aqua Regia)** | 150mB | L4 (Métaux précieux) |

---

## 🔧 Étapes

### L6.1 — Procédé Haber-Bosch (NH₃) — multibloc MB-HABER ⭐
Layout 5x5x5, casing Reinforced Iridium + chambre haute pression. Catalyseur Fer dopé K₂O (sub-recipe).
- **Conditions** : 200 bars, 450°C, catalyseur Fe-K₂O
- **Réaction** : N₂ + 3 H₂ → 2 NH₃ (équilibre, donc faut beaucoup de pression)

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("haber_bosch", "haber_reactor", 600, 0)
  .addFluidInput(<liquid:nitrogen_liquid> * 100)
  .addFluidInput(<liquid:hydrogen> * 300)
  .addItemInput(<item:nexusabsolu:fe_k2o_catalyst> * 1)  // catalyseur réutilisable, durabilité
  .addEnergyPerTickInput(30000)  // pression
  .addFluidOutput(<liquid:ammoniaque> * 200)
  .build();
```

### L6.2 — Procédé Ostwald (HNO₃)
Multibloc MB-OSTWALD 3x4x3, plateau catalyseur Platinum gauze.
- **Phase 1** : NH₃ + O₂ + Pt 900°C → NO + H₂O
- **Phase 2** : NO + O₂ → NO₂
- **Phase 3** : 3 NO₂ + H₂O → 2 HNO₃ + NO (recyclé)

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("ostwald", "ostwald_tower", 400, 0)
  .addFluidInput(<liquid:ammoniaque> * 200)
  .addFluidInput(<liquid:oxygen> * 500)
  .addFluidInput(<liquid:tridistilled_water> * 200)
  .addItemInput(<item:nexusabsolu:platinum_gauze> * 1)  // catalyseur durable
  .addEnergyPerTickInput(20000)
  .addFluidOutput(<liquid:hno3> * 200)
  .build();
```

### L6.3 — Procédé de contact (H₂SO₄)
Multibloc MB-CONTACT 3x4x3, catalyseur V₂O₅ (sub Pt si pas de Vanadium).
- **Phase 1 (Claus en amont)** : H₂S (de L1) + O₂ → S + H₂O — récupère soufre élémentaire
- **Phase 2** : S + O₂ → SO₂
- **Phase 3** : 2 SO₂ + O₂ + V₂O₅ → 2 SO₃
- **Phase 4** : SO₃ + H₂SO₄ existant (oléum) → H₂SO₄ pur (technique réelle)

```zenscript
// Claus
mods.modularmachinery.RecipeBuilder.newBuilder("claus_process", "contact_tower", 200, 0)
  .addFluidInput(<liquid:h2s> * 200)
  .addFluidInput(<liquid:oxygen> * 100)
  .addEnergyPerTickInput(5000)
  .addItemOutput(<item:nexusabsolu:sulfur_pure> * 4)
  .addFluidOutput(<liquid:tridistilled_water> * 100)  // bonus eau pure
  .build();

// Contact
mods.modularmachinery.RecipeBuilder.newBuilder("contact_h2so4", "contact_tower", 500, 0)
  .addItemInput(<item:nexusabsolu:sulfur_pure> * 4)
  .addFluidInput(<liquid:oxygen> * 600)
  .addFluidInput(<liquid:tridistilled_water> * 100)
  .addItemInput(<item:nexusabsolu:v2o5_catalyst> * 1)
  .addEnergyPerTickInput(15000)
  .addFluidOutput(<liquid:h2so4> * 300)
  .build();
```

### L6.4 — HCl (Mekanism Chemical Infuser)
Réutilise Mekanism natif :
```zenscript
mods.mekanism.chemical_infuser.addRecipe(<gas:hydrogen> * 100, <gas:chlorine> * 100, <gas:hydrogen_chloride> * 200);
// Convertir gas → fluid via Pressurized Reaction Chamber + tridistilled water
```

### L6.5 — NaOH concentré
Sous-produit de Castner-Kellner (L3.B), concentré ici par évaporation contrôlée.
```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("naoh_concentrate", "evaporator", 200, 0)
  .addFluidInput(<liquid:naoh_solution> * 1000)  // dilué
  .addEnergyPerTickInput(5000)
  .addFluidOutput(<liquid:naoh_concentrated> * 200)
  .addFluidOutput(<liquid:tridistilled_water> * 800)  // recyclé
  .build();
```

### L6.6 — Eau Régale (Aqua Regia)
Mélange simple HCl + HNO₃ ratio 3:1. Pas de multibloc, juste un Fluid Mixer.
```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("aqua_regia_mix", "fluid_mixer", 100, 0)
  .addFluidInput(<liquid:hcl> * 300)
  .addFluidInput(<liquid:hno3> * 100)
  .addEnergyPerTickInput(500)
  .addFluidOutput(<liquid:aqua_regia> * 400)
  .build();
```

---

## 🏗️ Multiblocs custom

- **MB-HABER** (5x5x5, Procédé Haber-Bosch, le pivot)
- **MB-OSTWALD** (3x4x3, Procédé Ostwald)
- **MB-CONTACT** (3x4x3, Procédé de contact, sert aussi pour Claus)
- **MB-EVAPORATOR** (3x3x3, concentration NaOH)

## 🧪 Fluides custom

```zenscript
val ammoniaque = VanillaFactory.createFluid("ammoniaque", 0xC8E6C9); ammoniaque.density = 730; ammoniaque.viscosity = 250; ammoniaque.register();
val hno3 = VanillaFactory.createFluid("hno3", 0xFFC107); hno3.density = 1500; hno3.luminosity = 2; hno3.register();
val h2so4 = VanillaFactory.createFluid("h2so4", 0xFFEB3B); h2so4.density = 1840; h2so4.viscosity = 2500; h2so4.register();
val hcl = VanillaFactory.createFluid("hcl", 0xFFF59D); hcl.register();
val naoh_concentrated = VanillaFactory.createFluid("naoh_concentrated", 0xFFFFFF); naoh_concentrated.luminosity = 3; naoh_concentrated.register();
// aqua_regia déjà défini en L4
```

## 🪨 Items custom (catalyseurs)

- `nexusabsolu:fe_k2o_catalyst` (catalyseur Haber-Bosch — durabilité 100 cycles, recharge possible)
- `nexusabsolu:platinum_gauze` (catalyseur Ostwald — durabilité 200 cycles)
- `nexusabsolu:v2o5_catalyst` (catalyseur Contact — durabilité 150 cycles)
- `nexusabsolu:sulfur_pure` (récupéré du H₂S via Claus)

## ⚡ Bilan énergie L6

- Haber-Bosch : 30k × 600 = 18M RF / cycle (200mB NH₃)
- Ostwald : 20k × 400 = 8M RF / cycle
- Contact (Claus + H₂SO₄) : 20k × 700 = 14M RF / cycle
- HCl : Mekanism natif, faible
- **Pic L6 si tout tourne** : ~70k RF/t (modéré)

## ⚠️ Importance critique

L6 est **le hub central**. Si Haber-Bosch ne tourne pas, tout s'arrête :
- Pas de NH₃ → Pas de HNO₃ → Pas d'Eau Régale → Pas de métaux précieux purifiés
- Pas de NH₃ → Pas d'Indole (L7) → Pas de Tryptamide-M → Pas de Manifoldine
- Pas d'acides → Pas de réactif α dans M1 Mélangeur Cryogénique

→ **Quête dédiée pour Haber-Bosch** doit être un milestone majeur de la Phase 3 (voir [[../03-progression-quetes]]).

## 🔗 Voir aussi

- [[../02-pipeline-overview#Matrice d'inter-dépendance]]
- [[L1-petrochimie]] — fournit H₂S
- [[L3-electrolyse-cryo]] — fournit N₂/O₂/H₂/Cl₂
- [[L4-pyrometallurgie]] — fournit Pt/Fe (catalyseurs) et reçoit Eau Régale
