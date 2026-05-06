// ============================================================================
// Nexus Absolu - Age4_L6_Acides.zs
// ============================================================================
// Recettes ZS pour la ligne L6 Acides-Ammoniaque :
//   - Catalyseurs (fe_k2o_catalyst, platinum_gauze, v2o5_catalyst)
//   - sulfur_pure (TODO : recyclage H2S via Claus, recette TEMP fallback)
//
// Reference design : docs/age4-cartouche-manifold/lines/L6-acides-ammoniaque.md
//
// Note : la recette TEMP de l'ammoniaque dans Age4_L8_Manifoldine.zs reste
// active comme FALLBACK (recettes complementaires pas en conflit).
// ============================================================================


// ============================================================================
// L6.1 - Fe-K2O Catalyst (Haber-Bosch)
// ============================================================================
// Iron + K2O dopant. Durabilite 100 cycles.
recipes.addShaped("nexus_fe_k2o_catalyst",
    <contenttweaker:fe_k2o_catalyst> * 1,
    [[<ore:dustIron>, <minecraft:dye:15>, <ore:dustIron>],  // dye:15 = bonemeal (proxy K)
     [<minecraft:dye:15>, <ore:ingotIron>, <minecraft:dye:15>],
     [<ore:dustIron>, <minecraft:dye:15>, <ore:dustIron>]]);


// ============================================================================
// L6.2 - Platinum Gauze (Ostwald)
// ============================================================================
// Plate platinum tisse en grille. Durabilite 200 cycles.
recipes.addShaped("nexus_platinum_gauze",
    <contenttweaker:platinum_gauze> * 1,
    [[<ore:platePlatinum>, <ore:nuggetPlatinum>, <ore:platePlatinum>],
     [<ore:nuggetPlatinum>, <ore:gemDiamond>, <ore:nuggetPlatinum>],
     [<ore:platePlatinum>, <ore:nuggetPlatinum>, <ore:platePlatinum>]]);

// Fallback : platinum_pure_99 from L4
recipes.addShaped("nexus_platinum_gauze_fallback",
    <contenttweaker:platinum_gauze> * 1,
    [[<contenttweaker:platinum_pure_99>, <ore:nuggetIridium>, <contenttweaker:platinum_pure_99>],
     [<ore:nuggetIridium>, <ore:gemDiamond>, <ore:nuggetIridium>],
     [<contenttweaker:platinum_pure_99>, <ore:nuggetIridium>, <contenttweaker:platinum_pure_99>]]);


// ============================================================================
// L6.3 - V2O5 Catalyst (Contact - H2SO4)
// ============================================================================
// Vanadium pentoxide. Durabilite 150 cycles.
recipes.addShaped("nexus_v2o5_catalyst",
    <contenttweaker:v2o5_catalyst> * 1,
    [[<ore:dustVanadium>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <ore:dustVanadium>],
     [<minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <ore:dustVanadium>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000})],
     [<ore:dustVanadium>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <ore:dustVanadium>]]);

// Fallback : si pas dustVanadium, utiliser titanium dust + oxygen
recipes.addShaped("nexus_v2o5_catalyst_fallback",
    <contenttweaker:v2o5_catalyst> * 1,
    [[<ore:dustTitanium>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <ore:dustTitanium>],
     [<minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <minecraft:diamond>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000})],
     [<ore:dustTitanium>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <ore:dustTitanium>]]);


// ============================================================================
// L6.X - Sulfur Pure fallback recipe
// ============================================================================
// La vraie source = Claus process (recipe JSON contact_tower).
// Fallback shaped si pas de H2S dispo : sulfur dust vanille
recipes.addShaped("nexus_sulfur_pure_fallback",
    <contenttweaker:sulfur_pure> * 4,
    [[<ore:dustSulfur>, <ore:dustSulfur>, <ore:dustSulfur>],
     [<ore:dustSulfur>, <minecraft:redstone>, <ore:dustSulfur>],
     [<ore:dustSulfur>, <ore:dustSulfur>, <ore:dustSulfur>]]);


print("[Nexus Absolu] Age4_L6_Acides.zs loaded -- 7 recettes catalyseurs L6");
