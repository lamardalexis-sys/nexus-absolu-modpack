// ============================================
// NEXUS ABSOLU -- Age1_EnderIO.zs
// EnderIO inter-mod recipes (salle 9x9-11x11)
// ============================================

// === ALLOY SMELTER -- la machine a acier ===
recipes.remove(<enderio:block_alloy_smelter>);
recipes.addShaped("nexus_alloy_smelter", <enderio:block_alloy_smelter>,
    [[<ore:ingotInvar>, <minecraft:iron_ingot>, <ore:ingotInvar>],
     [<minecraft:furnace>, <enderio:item_material>, <minecraft:furnace>],
     [<ore:gearInvar>, <immersiveengineering:wooden_device0:1>, <ore:gearInvar>]]);

// === STEEL — Coal Coke + Iron + Wall Dust dans l'Alloy Smelter ===
mods.enderio.AlloySmelter.addRecipe(<thermalfoundation:material:160>, [<immersiveengineering:material:6>, <minecraft:iron_ingot>, <nexusabsolu:wall_dust>], 5000);

// === SAG MILL ===
recipes.remove(<enderio:block_sag_mill>);
recipes.addShaped("nexus_sag_mill", <enderio:block_sag_mill>,
    [[<ore:ingotDarkSteel>, <minecraft:flint>, <ore:ingotDarkSteel>],
     [<minecraft:flint>, <enderio:item_material>, <minecraft:flint>],
     [<ore:ingotDarkSteel>, <immersiveengineering:material:1>, <ore:ingotDarkSteel>]]);

// === ELECTRICAL STEEL via INDUCTION SMELTER ===
mods.thermalexpansion.InductionSmelter.addRecipe(
    <enderio:item_alloy_ingot:0>, <minecraft:iron_ingot>, <ore:itemSilicon>.firstItem, 4000);

print("[Nexus Absolu] Age1_EnderIO.zs loaded");
