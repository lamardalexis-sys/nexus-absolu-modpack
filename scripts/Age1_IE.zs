// ============================================
// NEXUS ABSOLU -- Age1_IE.zs
// Immersive Engineering (salle 11x11+)
// ============================================

// === COKE OVEN -- pas modifie (3x3x3 multibloc, gate naturel par taille) ===

// === BLAST BRICK -- SUPPRIME ===
// Le Blast Furnace IE ne sert qu'a faire du steel.
// Notre steel vient de l'EnderIO Alloy Smelter (Age1_EnderIO.zs).
// Bug connu IE: removeRecipe() ne fonctionne pas (IE charge apres CT).
// Solution: empecher la construction du multibloc en supprimant le brick.
import mods.jei.JEI.removeAndHide as rh_ie;
rh_ie(<immersiveengineering:stone_decoration:1>);
// Aussi cacher le Blast Furnace Preheater (inutile sans BF)
rh_ie(<immersiveengineering:metal_device1:7>);

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
