// ============================================================================
// Nexus Absolu - Age4_L3_ElectrolyseCryo.zs
// ============================================================================
// Recettes ZS pour la ligne L3 Electrolyse-Cryo :
//   - Items intermediaires (calcium_phosphate, fluorite_dust, graphite_block,
//     mercury_pool, slag_silicate, phosphorus_white)
//   - Recettes Mekanism Electrolytic Separator (LiCl fondu)
//
// Reference design : docs/age4-cartouche-manifold/lines/L3-electrolyse-cryo.md
//
// Note : recettes des multiblocs (castner_kellner, phosphorus_white,
// fluorite_electrolysis, cryotheum_recycle) en JSON dans
// config/modularmachinery/recipes/. cryo_distillateur deja branche en
// cryo_air_to_argon/n2/o2.
// ============================================================================


// ============================================================================
// L3.X.1 - Calcium Phosphate (input four electrique pour Phosphore)
// ============================================================================
// Source naturelle : os + sable
recipes.addShaped("nexus_calcium_phosphate",
    <contenttweaker:calcium_phosphate> * 2,
    [[<minecraft:bone>, <minecraft:bone>, <minecraft:bone>],
     [<minecraft:bone>, <ore:dustSalt>, <minecraft:bone>],
     [<minecraft:bone>, <minecraft:bone>, <minecraft:bone>]]);


// ============================================================================
// L3.X.2 - Fluorite Dust (input cellule fluorite)
// ============================================================================
// Source : Apatite Thermal Foundation + chimie fluorure
recipes.addShaped("nexus_fluorite_dust",
    <contenttweaker:fluorite_dust> * 2,
    [[<ore:dustApatite>, <ore:dustApatite>, <ore:dustApatite>],
     [<ore:dustApatite>, <ore:dustSalt>, <ore:dustApatite>],
     [<ore:dustApatite>, <ore:dustApatite>, <ore:dustApatite>]]);

// Fallback : si dustApatite indispo
recipes.addShaped("nexus_fluorite_dust_fallback",
    <contenttweaker:fluorite_dust> * 1,
    [[<ore:dustQuartz>, <ore:dustQuartz>, <ore:dustQuartz>],
     [<ore:dustQuartz>, <ore:dustSalt>, <ore:dustQuartz>],
     [<ore:dustQuartz>, <ore:dustQuartz>, <ore:dustQuartz>]]);


// ============================================================================
// L3.X.3 - Graphite Block (anode pour Castner-Kellner)
// ============================================================================
// 9 charcoal -> 1 graphite block (compression haute T)
recipes.addShaped("nexus_graphite_block",
    <contenttweaker:graphite_block> * 1,
    [[<minecraft:coal:1>, <minecraft:coal:1>, <minecraft:coal:1>],
     [<minecraft:coal:1>, <minecraft:coal:1>, <minecraft:coal:1>],
     [<minecraft:coal:1>, <minecraft:coal:1>, <minecraft:coal:1>]]);


// ============================================================================
// L3.X.4 - Mercury Pool (cathode Castner-Kellner)
// ============================================================================
// Mekanism mercury (oredict ingotMercury) ou Thermal Foundation
// Implementation : 9 mercury ingots (oredict) -> 1 pool
recipes.addShaped("nexus_mercury_pool",
    <contenttweaker:mercury_pool> * 1,
    [[<ore:ingotMercury>, <ore:ingotMercury>, <ore:ingotMercury>],
     [<ore:ingotMercury>, <minecraft:iron_ingot>, <ore:ingotMercury>],
     [<ore:ingotMercury>, <ore:ingotMercury>, <ore:ingotMercury>]]);

// Fallback : 9 redstone (proxy si mercury indispo)
recipes.addShaped("nexus_mercury_pool_fallback",
    <contenttweaker:mercury_pool> * 1,
    [[<minecraft:redstone>, <minecraft:redstone>, <minecraft:redstone>],
     [<minecraft:redstone>, <minecraft:gold_ingot>, <minecraft:redstone>],
     [<minecraft:redstone>, <minecraft:redstone>, <minecraft:redstone>]]);


// ============================================================================
// L3.C - LiCl fondu via Mekanism Electrolytic Separator
// ============================================================================
// LiCl fondu (660 deg C) -> Li liquide + Cl2
// Note : commentee car dependant du fluid licl_fondu existant comme gas Mekanism
// (a verifier). Si pas valide, fallback shaped recipe.

// mods.mekanism.electrolyticseparator.addRecipe(
//   <liquid:licl_fondu> * 100,
//   <liquid:lithium_liquid> * 50,
//   <liquid:chlorine_gas> * 50);

// Fallback shaped : 4 LiCl fondu bucket -> 2 Li ingots + 2 Cl bucket
// (en attendant validation Mekanism gas integration)


print("[Nexus Absolu] Age4_L3_ElectrolyseCryo.zs loaded -- 6 recettes L3");
