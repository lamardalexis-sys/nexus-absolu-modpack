// ============================================
// Age 0 — Silver Chest (Iron Chests mod)
// Recette custom Nexus Absolu
// ============================================

// Supprimer la recette vanilla du Silver Chest
recipes.remove(<ironchest:iron_chest:4>);

// Recette custom Silver Chest
recipes.addShaped("nexus_silver_chest", <ironchest:iron_chest:4>, [
    [<ore:ingotSilver>, <minecraft:chest>, <ore:ingotSilver>],
    [<minecraft:chest>, <ore:ingotSilver>, <minecraft:chest>],
    [<minecraft:iron_ingot>, <minecraft:string>, <minecraft:iron_ingot>]
]);
