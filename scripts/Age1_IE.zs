// ============================================
// NEXUS ABSOLU -- Age1_IE.zs
// Immersive Engineering (salle 11x11+)
// IE est surtout pas modifie car ses multiblocs sont le reward
// ============================================

// === COKE OVEN -- pas modifie (3x3x3 multibloc, gate naturel par taille) ===
// Le joueur doit etre dans une salle 9x9+ pour le construire

// === BLAST FURNACE -- Blast Brick: vossium + invar + brick ===
recipes.remove(<immersiveengineering:stone_decoration:1>);
recipes.addShaped("nexus_blast_brick", <immersiveengineering:stone_decoration:1> * 4,
    [[<ore:ingotInvar>, <minecraft:brick>, <ore:ingotInvar>],
     [<minecraft:brick>, <contenttweaker:vossium_ingot>, <minecraft:brick>],
     [<ore:ingotInvar>, <minecraft:brick>, <ore:ingotInvar>]]);

// === GARDEN CLOCHE -- necessite Thermal + Ex Nihilo ===
// La Cloche permet le farming auto en compact -- tres utile
recipes.remove(<immersiveengineering:metal_device1:13>);
recipes.addShaped("nexus_garden_cloche", <immersiveengineering:metal_device1:13>,
    [[<minecraft:glass>, null, <minecraft:glass>],
     [<minecraft:glass>, <ore:ingotCopper>, <minecraft:glass>],
     [<ore:ingotIron>, <immersiveengineering:material:8>, <ore:ingotIron>]]);

// === ENGINEER'S WORKBENCH -- pas modifie ===

// === WIRE CONNECTORS -- pas modifies ===
// Le joueur decouvre le systeme d'energie IE naturellement

print("[Nexus Absolu] Age1_IE.zs loaded");

// === REMOVE ALL Compact Machine miniaturization recipes ===
// Tout se craft avec le Condenseur du Dr. Voss, pas le multibloc Miniaturization
mods.compactmachines3.Miniaturization.removeRecipe(<compactmachines3:wallbreakable>);
mods.compactmachines3.Miniaturization.removeRecipe(<compactmachines3:tunneltool>);
mods.compactmachines3.Miniaturization.removeRecipe(<compactmachines3:machine:0>);
mods.compactmachines3.Miniaturization.removeRecipe(<compactmachines3:machine:1>);
mods.compactmachines3.Miniaturization.removeRecipe(<compactmachines3:machine:2>);
mods.compactmachines3.Miniaturization.removeRecipe(<compactmachines3:machine:3>);
mods.compactmachines3.Miniaturization.removeRecipe(<compactmachines3:machine:4>);
mods.compactmachines3.Miniaturization.removeRecipe(<compactmachines3:machine:5>);
mods.compactmachines3.Miniaturization.removeRecipe(<compactmachines3:personalshrinker>);
mods.compactmachines3.Miniaturization.removeRecipe(<compactmachines3:fieldprojector>);

// Supprime aussi le craft du Field Projector pour bloquer la methode
recipes.remove(<compactmachines3:fieldprojector>);


// Supprime les crafts vanilla des machines compact aussi
recipes.remove(<compactmachines3:machine:0>);
recipes.remove(<compactmachines3:machine:1>);
recipes.remove(<compactmachines3:machine:2>);
recipes.remove(<compactmachines3:machine:3>);
recipes.remove(<compactmachines3:machine:4>);
recipes.remove(<compactmachines3:machine:5>);
recipes.remove(<compactmachines3:personalshrinker>);


// === HIDE Miniaturization category from JEI ===
mods.jei.JEI.hideCategory("compactmachines3.miniaturization");
mods.jei.JEI.hideCategory("compactmachines3:miniaturization");
mods.jei.JEI.hideCategory("compactmachines3.multiblock_miniaturization");
mods.jei.JEI.hideCategory("compactmachines3:multiblock_miniaturization");
mods.jei.JEI.hideCategory("Multiblock Miniaturization");
mods.jei.JEI.hideCategory("miniaturization");
mods.jei.JEI.hide(<compactmachines3:fieldprojector>);

