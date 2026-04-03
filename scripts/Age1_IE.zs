// ============================================
// NEXUS ABSOLU -- Age1_IE.zs
// Immersive Engineering (salle 11x11+)
// IE est surtout pas modifie car ses multiblocs sont le reward
// ============================================

// === COKE OVEN -- pas modifie (3x3x3 multibloc, gate naturel par taille) ===
// Le joueur doit etre dans une salle 9x9+ pour le construire

// === BLAST FURNACE -- Blast Brick: blaze powder -> vossium ===
// Pas de Blaze en Compact Machine, le Vossium remplace
recipes.remove(<immersiveengineering:stone_decoration:1>);
recipes.addShaped("nexus_blast_brick", <immersiveengineering:stone_decoration:1> * 4,
    [[<minecraft:netherbrick>, <contenttweaker:vossium_ingot>],
     [<contenttweaker:vossium_ingot>, <minecraft:netherbrick>]]);

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
