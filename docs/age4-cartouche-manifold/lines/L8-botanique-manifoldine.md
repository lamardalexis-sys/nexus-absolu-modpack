# L8 — Botanique + Manifoldine ⭐

> Tags: #ligne #botania #astral-sorcery #manifoldine #climax
> Objectif : produire **Essence Chromatique** (16 champis) + **Cristaux de Manifoldine** (Mycélium irradié → Cyclisation Stellaire)
> Théorèmes : II (Organique), III (Stellaire), V (Brisure)

---

## 📥 Inputs

- **16 champis Botania** (8 doux + 8 sombres, élevés en Mycélium)
- **HCl + H₂SO₄** (de [[L6-acides-ammoniaque]])
- **Mana** (~1M Mana via Mana Pool / Endoflame / Spreaders)
- **Liquid Starlight** (Astral Sorcery, Lens Cluster + Marble Collector)
- **Mycélium Activé** (de [[L5-nucleaire]] — block transmuté)
- **Acétone, Tryptamide-M, Méthanol, Éthanol** (de [[L7-organique-acetone]])
- **Ammoniaque NH₃** (de L6)
- **Eau Lourde D₂O** (de [[L2-hydro-eau]])
- **Argon** (de [[L3-electrolyse-cryo]] — atmosphère cyclisation)
- **H₃PO₄** (de [[L4-pyrometallurgie]] — stabilisation finale)

## 📤 Outputs

| Produit | Qté/cycle | Destination |
|---------|-----------|-------------|
| **Essence Chromatique** ⭐ | 1000mB | M2 Bio-Réacteur (composé final) |
| **Cristaux de Manifoldine** ⭐ | 1 | Composé ε (Solution Manifoldine Active) |

---

## 🔧 Étapes — Sous-pipeline A : Botania (Théorème II)

### L8.A.1 — Élevage des 16 champis
Les 16 champis colorés Botania doivent être farmés en **Champignonnière Mycélium** (Garden of Glass-style).
- **Astuce** : utiliser Botania `Tinted Daisy` pour produire les 16 couleurs facilement.
- Ou farm naturel sur mycélium (lent).

### L8.A.2 — Broyage en poudres pigmentaires
Mortier d'Apothecary (Botania natif) → 16 poudres distinctes.
```zenscript
// Recipe par champi (×16 — pattern itéré)
mods.botania.Apothecary.addRecipe(<item:nexusabsolu:pigment_red>, [<item:botania:mushroom:0>]);  // Champi rouge
// ... idem pour 15 autres couleurs
```

### L8.A.3 — Traitement acide différencié
- 8 champis "doux" (rouge, orange, jaune, rose, vert clair, cyan, lime, magenta) → traitement HCl léger → 8 **Extraits Doux**
- 8 champis "sombres" (noir, violet, marron, gris foncé, vert foncé, bleu foncé, gris clair, bleu clair) → traitement H₂SO₄ chaud → 8 **Extraits Sombres**

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("extract_sweet", "alchemy_chamber", 100, 0)
  .addItemInput(<item:nexusabsolu:pigment_red>)  // ×8 recettes
  .addFluidInput(<liquid:hcl> * 50)
  .addFluidOutput(<liquid:extract_sweet_red> * 100)
  .build();

mods.modularmachinery.RecipeBuilder.newBuilder("extract_dark", "alchemy_chamber", 200, 0)
  .addItemInput(<item:nexusabsolu:pigment_black>)  // ×8 recettes
  .addFluidInput(<liquid:h2so4> * 80)
  .addEnergyPerTickInput(5000)  // chauffe
  .addFluidOutput(<liquid:extract_dark_black> * 100)
  .build();
```

### L8.A.4 — Alambic Manaïque (multibloc MB-ALAMBIC) ⭐
Layout 5x4x5 avec **Mana Pool** au centre + 8 Mana Spreaders convergents pointés vers le centre.
- **Input** : 16 extraits dosés (8 doux + 8 sombres)
- **Mana** : consomme 200k Mana
- **Output** : sépare **Mana Lié** (récupérable) + **Matrice Pigmentaire** (item, contient les 16 fréquences)

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("alembic_manaic", "manaic_alembic", 1200, 0)
  .addFluidInput(<liquid:extract_sweet_red> * 100)
  .addFluidInput(<liquid:extract_sweet_orange> * 100)
  // ... 14 autres
  .addItemInput(<item:botania:manaresource:5> * 4)  // proxy Mana via mana resource
  .addEnergyPerTickInput(2000)
  .addItemOutput(<item:nexusabsolu:matrix_pigmentary> * 1)
  .addItemOutput(<item:nexusabsolu:mana_bound_capsule> * 4)  // récup
  .build();
```

→ **Note technique** : intégrer le mana via `ManaPool` checking nécessite KubeJS event listener pour vraiment consommer du mana. Sinon proxy via item Mana Diamond / Mana Steel.

### L8.A.5 — Recombinaison via Runic Altar (Botania natif modifié)
**Matrice Pigmentaire** + **6 Runes Botania de base** (Air, Terre, Feu, Eau, Lumière, Ténèbres) → **Cristal Chromatique Brut**.

```zenscript
mods.botania.RuneAltar.addRecipe(
  <item:nexusabsolu:cristal_chromatic_raw>,
  100000,  // mana
  [<item:nexusabsolu:matrix_pigmentary>,
   <item:botania:rune:0>, <item:botania:rune:1>, <item:botania:rune:2>,
   <item:botania:rune:3>, <item:botania:rune:4>, <item:botania:rune:5>]
);
```

### L8.A.6 — Charge Manaïque finale (Mana Enchanter custom)
Cristal Brut + **1M Mana** (channeling via Mana Spreaders configurés) → **Essence Chromatique** liquide.

```zenscript
// Block custom Mana Enchanter ou via Botania Brewery + custom recipe
mods.modularmachinery.RecipeBuilder.newBuilder("essence_chromatic_charge", "mana_enchanter", 600, 0)
  .addItemInput(<item:nexusabsolu:cristal_chromatic_raw>)
  .addItemInput(<item:botania:manaresource:8> * 4)  // 4× Mana Pearl = ~1M Mana
  .addEnergyPerTickInput(10000)
  .addFluidOutput(<liquid:essence_chromatic> * 1000)
  .build();
```

---

## 🔧 Étapes — Sous-pipeline B : Liquid Starlight (Théorème III)

### L8.B — Capture du Starlight
Setup Astral Sorcery natif : Lens Cluster pointé vers **Marble Collector Crystal** la nuit.
- Pas de modification mod nécessaire.
- Production passive : ~100mB Liquid Starlight / nuit / Collector tier 1.
- Pour la cartouche, prévoir 1000-2000mB (10-20 nuits ou plusieurs collectors).

→ Voir Astral Sorcery Patchouli book joueur.

---

## 🔧 Étapes — Sous-pipeline C : Manifoldine (Théorème V) ⭐⭐⭐

### L8.C.1 — Préparation Mycélium Activé
Le block `nexusabsolu:mycelium_active` est produit en L5.9 (par exposition à un fission reactor NC pendant 1 nuit). Récolté ici.

```zenscript
// Récolte
furnace.addRecipe(<item:nexusabsolu:spores_active> * 4, <block:nexusabsolu:mycelium_active>, 0.5);
// ou Mekanism Crusher
mods.mekanism.crusher.addRecipe(<block:nexusabsolu:mycelium_active>, <item:nexusabsolu:spores_active> * 8);
```

### L8.C.2 — Extraction Soxhlet (multibloc MB-SOXHLET) ⭐
Layout 3x5x3 (vertical avec reflux). Acétone + NH₃ extraient les composés azotés des spores.
- **Inputs** : 8 Spores Activées + Acétone (L7) + Ammoniaque (L6) + Méthanol (L7) éluant
- **Process** : 3 minutes en jeu (3600 ticks) à 60°C, reflux

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("manifoldine_extraction_purification", "soxhlet_extractor", 3600, 0)
  .addItemInput(<item:nexusabsolu:spores_active> * 8)
  .addFluidInput(<liquid:acetone> * 200)
  .addFluidInput(<liquid:ammoniaque> * 100)
  .addFluidInput(<liquid:methanol> * 100)
  .addEnergyPerTickInput(8000)
  .addFluidOutput(<liquid:manifoldine_extract_purified> * 200)
  .addFluidOutput(<liquid:acetone> * 150)  // partial recovery
  .build();
```

→ **Note** : extraction et purification fusionnées dans un seul process (économie de complexité).

### L8.C.3 — Cyclisateur Stellaire (multibloc MB-CYCLO) ⭐⭐⭐
Layout 5x6x5, **DOIT être à ciel ouvert la nuit**. Au-dessus : Astral Sorcery Collector Crystal (alimentation Starlight directe).
- **Inputs** : Extrait Purifié + Tryptamide-M (L7) + Liquid Starlight + Eau Lourde (L2) + Argon (L3) atmosphère
- **Conditions** : LA NUIT (KubeJS check : `world.isNight()`) + ciel ouvert (line of sight test) + 5 min process

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("manifoldine_cyclization", "cyclisateur_stellaire", 6000, 0)
  .addFluidInput(<liquid:manifoldine_extract_purified> * 200)
  .addItemInput(<item:nexusabsolu:tryptamide_m_capsule> * 1)
  .addFluidInput(<liquid:astralsorcery.liquidstarlight> * 500)
  .addFluidInput(<liquid:mekanism.heavywater> * 100)
  .addFluidInput(<liquid:argon> * 200)
  .addEnergyPerTickInput(15000)
  .addFluidOutput(<liquid:manifoldine_brute> * 100)
  .build();
```

→ **Validation conditions** : implémenté via Modular Machinery `requirements` JSON :
```json
"requirements": [
  { "type": "modularmachinery:dimension", "dimension": [0] },
  { "type": "modularmachinery:position", "y_min": 60 },
  { "type": "modularmachinery:weather", "weather": "clear" },
  { "type": "modularmachinery:time", "time_min": 13000, "time_max": 23000 }
]
```

### L8.C.4 — Stabilisation finale (Cristallisation)
Manifoldine Brute + H₃PO₄ (L4) → **Phosphate de Manifoldine** stable. Cristallisation par évaporation contrôlée.

```zenscript
mods.modularmachinery.RecipeBuilder.newBuilder("manifoldine_stabilization", "evaporator", 1200, 0)
  .addFluidInput(<liquid:manifoldine_brute> * 100)
  .addFluidInput(<liquid:h3po4> * 50)
  .addEnergyPerTickInput(3000)
  .addItemOutput(<item:nexusabsolu:cristal_manifoldine> * 1)  // ⭐ ITEM FINAL L8
  .addFluidOutput(<liquid:tridistilled_water> * 100)  // bonus eau pure recyclée
  .build();
```

---

## 🏗️ Multiblocs custom

- **MB-ALAMBIC** (5x4x5, Alambic Manaïque + 8 Mana Spreaders convergents)
- **MB-SOXHLET** (3x5x3, Extracteur Soxhlet vertical)
- **MB-CYCLO** (5x6x5, Cyclisateur Stellaire ciel ouvert nuit) ⭐⭐⭐
- **MB-EVAPORATOR** (réutilisé de L6)
- **MB-MANA-ENCHANTER** (3x3x3, charge manaïque finale)

## 🧪 Fluides custom

```zenscript
val extract_sweet_red = VanillaFactory.createFluid("extract_sweet_red", 0xEF5350); extract_sweet_red.luminosity = 4; extract_sweet_red.register();
// ... × 16 fluides extracts (8 sweet + 8 dark)
val essence_chromatic = VanillaFactory.createFluid("essence_chromatic", 0xFFFFFF); essence_chromatic.luminosity = 12; essence_chromatic.register();
// ⚠️ Essence Chromatique : effet "rainbow" via NBT custom et tinting
val manifoldine_extract_purified = VanillaFactory.createFluid("manifoldine_extract_purified", 0x7CB342); manifoldine_extract_purified.luminosity = 6; manifoldine_extract_purified.register();
val manifoldine_brute = VanillaFactory.createFluid("manifoldine_brute", 0x9C27B0); manifoldine_brute.luminosity = 14; manifoldine_brute.register();
```

## 🪨 Items custom

- `nexusabsolu:pigment_red`, `nexusabsolu:pigment_blue`, ... (×16)
- `nexusabsolu:matrix_pigmentary`
- `nexusabsolu:mana_bound_capsule` (récupération mana)
- `nexusabsolu:cristal_chromatic_raw`
- `nexusabsolu:spores_active` (récolte Mycélium Activé)
- `nexusabsolu:cristal_manifoldine` ⭐⭐⭐ (output final L8)

## ⚡ Bilan énergie L8

- Alambic Manaïque : 2k × 1200 = 2.4M RF + 200k Mana
- Soxhlet extraction : 8k × 3600 = 28.8M RF / cycle
- Cyclisateur Stellaire : 15k × 6000 = 90M RF / cycle ⭐ (LE plus gros consommateur)
- Stabilisation : 3k × 1200 = 3.6M RF / cycle
- **Total / Cristal Manifoldine** : ~125M RF + masse de Mana + 500mB Liquid Starlight
- **Pic L8 quand Cyclisateur tourne** : ~25k RF/t

## ⚠️ Importance critique

L8 est l'**aval final magique**. Sans elle :
- Pas d'Essence Chromatique → pas de fusion magique-chimique dans M2
- Pas de Cristal Manifoldine → pas de composé ε → pas de Cartouche

→ Quête finale "Phase 5 : Le Vivant et l'Étoile" doit être un climax émotionnel + visuel (cinématique cyclisation sous étoiles).

## 🔗 Voir aussi

- [[../01-lore-integration#Théorème II]]
- [[../01-lore-integration#Théorème III]]
- [[../01-lore-integration#Théorème V]]
- [[L5-nucleaire]] — produit Mycélium Activé (input critique)
- [[L7-organique-acetone]] — produit 4 inputs (Acétone, Tryptamide-M, Méthanol, Éthanol)
