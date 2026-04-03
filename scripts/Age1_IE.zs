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

// === REMOVE Compact Machine miniaturization recipes ===
// Walls and tunnels are now made in the Atelier du Dr. Voss
mods.compactmachines3.Miniaturization.removeRecipe(<compactmachines3:wallbreakable>);
mods.compactmachines3.Miniaturization.removeRecipe(<compactmachines3:tunneltool>);

