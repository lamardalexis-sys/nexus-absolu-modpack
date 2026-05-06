// ============================================================================
// Nexus Absolu - Age4_L5_Nucleaire.zs
// ============================================================================
// Recettes ZS pour la ligne L5 Nucleaire :
//   - Items intermediaires (uranyl_dust, bef2_dust, b2o3_dust)
//   - Plutonium239 et Thorium ingots (via NC dust pattern)
//
// Reference design : docs/age4-cartouche-manifold/lines/L5-nucleaire.md
// ============================================================================


// ============================================================================
// L5.1 - Uranyl Dust (Mekanism PRC -> simplifie shaped)
// ============================================================================
// Design : Uranium Dust + O2 + H2SO4 -> uranyl_dust + H2 (PRC)
// Implementation simplifiee : recette shaped

recipes.addShaped("nexus_uranyl_dust",
    <contenttweaker:uranyl_dust> * 1,
    [[<minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000}), <ore:dustUranium>, <minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000})],
     [<ore:dustUranium>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <ore:dustUranium>],
     [<minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000}), <ore:dustUranium>, <minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000})]]);


// ============================================================================
// L5.5 - BeF2 dust (input reduction Beryllium)
// ============================================================================
// Beryl + F2 -> BeF2 dust

recipes.addShaped("nexus_bef2_dust",
    <contenttweaker:bef2_dust> * 2,
    [[<minecraft:bucket>.withTag({FluidName: "fluorine_gas", Amount: 1000}), <ore:dustBeryllium>, <minecraft:bucket>.withTag({FluidName: "fluorine_gas", Amount: 1000})],
     [<ore:dustBeryllium>, <ore:dustBeryllium>, <ore:dustBeryllium>],
     [<minecraft:bucket>.withTag({FluidName: "fluorine_gas", Amount: 1000}), <ore:dustBeryllium>, <minecraft:bucket>.withTag({FluidName: "fluorine_gas", Amount: 1000})]]);

// Fallback : si dustBeryllium indispo, utiliser ingot beryllium
recipes.addShaped("nexus_bef2_dust_fallback",
    <contenttweaker:bef2_dust> * 1,
    [[<contenttweaker:fluorine_capsule>, <ore:ingotBeryllium>, <contenttweaker:fluorine_capsule>],
     [<ore:ingotBeryllium>, <ore:dustQuartz>, <ore:ingotBeryllium>],
     [<contenttweaker:fluorine_capsule>, <ore:ingotBeryllium>, <contenttweaker:fluorine_capsule>]]);


// ============================================================================
// L5.6 - B2O3 dust (input electrolyse Bore)
// ============================================================================
// Borax + chauffe -> B2O3 (oxyde de bore)

recipes.addShaped("nexus_b2o3_dust",
    <contenttweaker:b2o3_dust> * 2,
    [[<ore:dustBorax>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <ore:dustBorax>],
     [<ore:dustBorax>, <ore:dustBorax>, <ore:dustBorax>],
     [<ore:dustBorax>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <ore:dustBorax>]]);

// Fallback : utiliser dustBoron directement
recipes.addShaped("nexus_b2o3_dust_fallback",
    <contenttweaker:b2o3_dust> * 1,
    [[<ore:dustBoron>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <ore:dustBoron>],
     [<ore:dustBoron>, <ore:dustBoron>, <ore:dustBoron>],
     [<ore:dustBoron>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <ore:dustBoron>]]);


// ============================================================================
// L5.4 - Plutonium-239 (proxy NC ou shapeless si NC indispo)
// ============================================================================
// Design : breeder reactor NC produit Pu-239 ingot natif
// Implementation : shapeless qui consume NC plutonium239 natif si dispo

// Note : si nuclearcraft:ingot:9 (Plutonium-239) existe, on convertit en notre ingot
// Sinon, fallback shaped via uranium ingot + neutron source (proxy)

recipes.addShapeless("nexus_plutonium239_ingot",
    <contenttweaker:plutonium239_ingot> * 1,
    [<nuclearcraft:ingot:9>]);  // NC plutonium-239 natif

// Fallback : shaped recipe si NC indispo (rare)
recipes.addShaped("nexus_plutonium239_ingot_fallback",
    <contenttweaker:plutonium239_ingot> * 1,
    [[<contenttweaker:uranyl_dust>, <minecraft:redstone_block>, <contenttweaker:uranyl_dust>],
     [<minecraft:redstone_block>, <ore:ingotUranium>, <minecraft:redstone_block>],
     [<contenttweaker:uranyl_dust>, <minecraft:redstone_block>, <contenttweaker:uranyl_dust>]]);


// ============================================================================
// L5.4 - Thorium ingot (proxy NC)
// ============================================================================
recipes.addShapeless("nexus_thorium_ingot",
    <contenttweaker:thorium_ingot> * 1,
    [<nuclearcraft:ingot:5>]);  // NC thorium natif (verify ID)


print("[Nexus Absolu] Age4_L5_Nucleaire.zs loaded -- 8 recettes L5");
