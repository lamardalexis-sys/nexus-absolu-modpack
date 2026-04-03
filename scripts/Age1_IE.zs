// ============================================
// NEXUS ABSOLU -- Age1_IE.zs
// Immersive Engineering (salle 11x11+)
// ============================================

// === COKE OVEN -- pas modifie (3x3x3 multibloc, gate naturel par taille) ===

// === BLAST BRICK -- vossium + invar + brick ===
recipes.remove(<immersiveengineering:stone_decoration:1>);
recipes.addShaped("nexus_blast_brick", <immersiveengineering:stone_decoration:1> * 4,
    [[<ore:ingotInvar>, <minecraft:brick>, <ore:ingotInvar>],
     [<minecraft:brick>, <contenttweaker:vossium_ingot>, <minecraft:brick>],
     [<ore:ingotInvar>, <minecraft:brick>, <ore:ingotInvar>]]);

// === GARDEN CLOCHE ===
recipes.remove(<immersiveengineering:metal_device1:13>);
recipes.addShaped("nexus_garden_cloche", <immersiveengineering:metal_device1:13>,
    [[<minecraft:glass>, null, <minecraft:glass>],
     [<minecraft:glass>, <ore:ingotCopper>, <minecraft:glass>],
     [<ore:ingotIron>, <immersiveengineering:material:8>, <ore:ingotIron>]]);

// === REMOVE crafting table recipes for Compact Machines ===
// CMs are made with the Condenseur, walls/tunnels with the Atelier
recipes.remove(<compactmachines3:machine:0>);
recipes.remove(<compactmachines3:machine:1>);
recipes.remove(<compactmachines3:machine:2>);
recipes.remove(<compactmachines3:machine:3>);
recipes.remove(<compactmachines3:machine:4>);
recipes.remove(<compactmachines3:machine:5>);
recipes.remove(<compactmachines3:fieldprojector>);

// Hide Field Projector from JEI
mods.jei.JEI.hide(<compactmachines3:fieldprojector>);

print("[Nexus Absolu] Age1_IE.zs loaded");
