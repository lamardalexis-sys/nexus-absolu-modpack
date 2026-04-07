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

// === STEEL — Chaine Iron Insule ===
// 1. Crafting table : 1 iron + 8 wall_dust autour -> 1 Iron Insule
recipes.addShaped("nexus_iron_insule", <nexusabsolu:iron_insule>,
    [[<nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>],
     [<nexusabsolu:wall_dust>, <minecraft:iron_ingot>, <nexusabsolu:wall_dust>],
     [<nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>]]);

// 2. NOTRE recette est definie dans config/enderio/recipes/user/user_recipes.xml
//    Iron Insule + Coal Coke -> Steel (2 inputs, pas de conflit de slots)
//    Le blocage des recettes par defaut se fait via XML EnderIO disabled="true"
//    car CT removeRecipe ne fonctionne pas (recettes chargees apres CT)

// === SAG MILL ===
recipes.remove(<enderio:block_sag_mill>);
recipes.addShaped("nexus_sag_mill", <enderio:block_sag_mill>,
    [[<ore:ingotDarkSteel>, <minecraft:flint>, <ore:ingotDarkSteel>],
     [<minecraft:flint>, <enderio:item_material>, <minecraft:flint>],
     [<ore:ingotDarkSteel>, <immersiveengineering:material:1>, <ore:ingotDarkSteel>]]);

// === ELECTRICAL STEEL via INDUCTION SMELTER ===
mods.thermalexpansion.InductionSmelter.addRecipe(
    <enderio:item_alloy_ingot:0>, <minecraft:iron_ingot>, <ore:itemSilicon>.firstItem, 4000);


// ============================================
// MACHINES ENDERIO MANQUANTES — crafts inter-mods
// ============================================

// === PAINTING MACHINE (EnderIO + AA) ===
// Fonction: peindre des blocs
recipes.remove(<enderio:block_painter>);
recipes.addShaped("nexus_painter", <enderio:block_painter>,
    [[<ore:ingotDarkSteel>, <minecraft:diamond>, <ore:ingotDarkSteel>],
     [<actuallyadditions:item_crystal:2>, <enderio:item_material>, <actuallyadditions:item_crystal:2>],
     [<ore:ingotDarkSteel>, <ore:gearIron>, <ore:ingotDarkSteel>]]);

// === CRAFTER (EnderIO + Thermal) ===
// Fonction: auto-craft (table de craft automatique)
recipes.remove(<enderio:block_crafter>);
recipes.addShaped("nexus_crafter", <enderio:block_crafter>,
    [[<ore:ingotDarkSteel>, <minecraft:crafting_table>, <ore:ingotDarkSteel>],
     [<ore:ingotDarkSteel>, <enderio:item_material>, <ore:ingotDarkSteel>],
     [<ore:gearInvar>, <thermalexpansion:frame>, <ore:gearInvar>]]);

// === WIRED CHARGER (EnderIO + Thermal) ===
// Fonction: charger des items EnderIO
recipes.remove(<enderio:block_wired_charger>);
recipes.addShaped("nexus_wired_charger", <enderio:block_wired_charger>,
    [[<ore:ingotDarkSteel>, <minecraft:redstone>, <ore:ingotDarkSteel>],
     [<enderio:item_alloy_ingot:3>, <enderio:item_material>, <enderio:item_alloy_ingot:3>],
     [<ore:ingotDarkSteel>, <minecraft:redstone_block>, <ore:ingotDarkSteel>]]);

// === CONDUITS ===
// Energy conduit: Redstone Alloy + copper wire (Thermal + IE)
recipes.remove(<enderio:item_power_conduit>);
recipes.addShaped("nexus_energy_conduit", <enderio:item_power_conduit> * 8,
    [[<enderio:item_alloy_ingot:3>, <enderio:item_alloy_ingot:3>, <enderio:item_alloy_ingot:3>],
     [<ore:ingotCopper>, <immersiveengineering:wirecoil:0>, <ore:ingotCopper>],
     [<enderio:item_alloy_ingot:3>, <enderio:item_alloy_ingot:3>, <enderio:item_alloy_ingot:3>]]);

// Item conduit: Pulsating Iron + hopper (EnderIO + vanilla)
recipes.remove(<enderio:item_item_conduit>);
recipes.addShaped("nexus_item_conduit", <enderio:item_item_conduit> * 8,
    [[<enderio:item_alloy_ingot:5>, <enderio:item_alloy_ingot:5>, <enderio:item_alloy_ingot:5>],
     [<ore:ingotIron>, <minecraft:hopper>, <ore:ingotIron>],
     [<enderio:item_alloy_ingot:5>, <enderio:item_alloy_ingot:5>, <enderio:item_alloy_ingot:5>]]);

// Fluid conduit: Vibrant Alloy + bucket (EnderIO avance)
recipes.remove(<enderio:item_liquid_conduit>);
recipes.addShaped("nexus_fluid_conduit", <enderio:item_liquid_conduit> * 8,
    [[<enderio:item_alloy_ingot:4>, <enderio:item_alloy_ingot:4>, <enderio:item_alloy_ingot:4>],
     [<minecraft:glass>, <minecraft:bucket>, <minecraft:glass>],
     [<enderio:item_alloy_ingot:4>, <enderio:item_alloy_ingot:4>, <enderio:item_alloy_ingot:4>]]);

// === VAT (EnderIO + IE) ===
// Fonction: brewing/alchimie fluides
recipes.remove(<enderio:block_vat>);
recipes.addShaped("nexus_vat", <enderio:block_vat>,
    [[<ore:ingotDarkSteel>, <minecraft:cauldron>, <ore:ingotDarkSteel>],
     [<minecraft:glass>, <enderio:item_material>, <minecraft:glass>],
     [<ore:ingotDarkSteel>, <immersiveengineering:metal_device1:1>, <ore:ingotDarkSteel>]]);

print("[Nexus Absolu] Age1_EnderIO.zs loaded");
