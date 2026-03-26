// ============================================
// NEXUS ABSOLU -- Age1_EnderIO.zs
// EnderIO inter-mod recipes (salle 9x9-11x11)
// ============================================

// === ALLOY SMELTER -- necessite Thermal component ===
recipes.remove(<enderio:block_alloy_smelter>);
recipes.addShaped("nexus_alloy_smelter", <enderio:block_alloy_smelter>,
    [[<ore:ingotDarkSteel>, <minecraft:furnace>, <ore:ingotDarkSteel>],
     [<minecraft:furnace>, <enderio:item_material>, <minecraft:furnace>],
     [<ore:ingotDarkSteel>, <thermalexpansion:frame>, <ore:ingotDarkSteel>]]);

// === SAG MILL -- necessite IE + Tinkers ===
recipes.remove(<enderio:block_sag_mill>);
recipes.addShaped("nexus_sag_mill", <enderio:block_sag_mill>,
    [[<ore:ingotDarkSteel>, <minecraft:flint>, <ore:ingotDarkSteel>],
     [<minecraft:flint>, <enderio:item_material>, <minecraft:flint>],
     [<ore:ingotDarkSteel>, <immersiveengineering:material:1>, <ore:ingotDarkSteel>]]);

// === ELECTRICAL STEEL via INDUCTION SMELTER (force Thermal) ===
// Par defaut EnderIO fait Electrical Steel dans son Alloy Smelter
// On ajoute aussi la recette dans l'Induction Smelter Thermal
mods.thermalexpansion.InductionSmelter.addRecipe(
    <enderio:item_alloy_ingot:0>, <minecraft:iron_ingot>, <ore:itemSilicon>.firstItem, 4000);

print("[Nexus Absolu] Age1_EnderIO.zs loaded");
