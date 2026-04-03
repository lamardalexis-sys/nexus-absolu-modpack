// ============================================
// NEXUS ABSOLU -- Age1_EnderIO.zs
// EnderIO inter-mod recipes (salle 9x9-11x11)
// ============================================

// === ALLOY SMELTER -- la machine a acier ===
recipes.remove(<enderio:block_alloy_smelter>);
recipes.addShaped("nexus_alloy_smelter", <enderio:block_alloy_smelter>,
    [[<ore:ingotInvar>, <minecraft:iron_ingot>, <ore:ingotInvar>],
     [<minecraft:furnace>, <enderio:item_material>, <minecraft:furnace>],
     [<ore:gearInvar>, <exnihilocreatio:block_barrel0>, <ore:gearInvar>]]);

// === REMOVE toutes les autres recettes de steel ===
mods.immersiveengineering.BlastFurnace.removeRecipe(<thermalfoundation:material:160>);
mods.immersiveengineering.BlastFurnace.removeRecipe(<immersiveengineering:metal:8>);

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

// === REMOVE steel from Induction Smelter (steel only via Alloy Smelter) ===
mods.thermalexpansion.InductionSmelter.removeRecipe(<minecraft:iron_ingot>, <minecraft:sand>);
mods.thermalexpansion.InductionSmelter.removeRecipe(<minecraft:sand>, <minecraft:iron_ingot>);
mods.thermalexpansion.InductionSmelter.removeRecipe(<minecraft:iron_ingot>, <thermalfoundation:material:32>);
mods.thermalexpansion.InductionSmelter.removeRecipe(<thermalfoundation:material:32>, <minecraft:iron_ingot>);
mods.thermalexpansion.InductionSmelter.removeRecipe(<minecraft:iron_ingot>, <minecraft:coal>);
mods.thermalexpansion.InductionSmelter.removeRecipe(<minecraft:coal>, <minecraft:iron_ingot>);


// === REMOVE default steel recipes from Alloy Smelter ===
// Only our custom recipe (coal coke + iron + wall dust) should work
mods.enderio.AlloySmelter.removeRecipe(<thermalfoundation:material:160>);
mods.enderio.AlloySmelter.removeRecipe(<immersiveengineering:metal:8>);
mods.enderio.AlloySmelter.removeRecipe(<enderio:item_alloy_ingot:7>);

// Re-add ONLY our custom recipe
mods.enderio.AlloySmelter.addRecipe(<thermalfoundation:material:160>, [<immersiveengineering:material:6>, <minecraft:iron_ingot>, <nexusabsolu:wall_dust>], 5000);

