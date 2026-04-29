// ============================================
// NEXUS ABSOLU -- Age0_Keys.zs
// Cles + Catalyseurs + Condenseur system
// Le joueur utilise le Condenseur Dimensionnel pour fusionner
// 2 Compact Machines + Cle + Catalyseur = CM plus grande
// ============================================

// === REMOVE VANILLA CM RECIPES (force Condenseur) ===
recipes.remove(<compactmachines3:machine:0>);
recipes.remove(<compactmachines3:machine:1>);
recipes.remove(<compactmachines3:machine:2>);
recipes.remove(<compactmachines3:machine:3>);
recipes.remove(<compactmachines3:machine:4>);
recipes.remove(<compactmachines3:machine:5>);

// === CONDENSEUR BLOCK RECIPE ===
// Recette dans Age0_Scavenging.zs (Machine Frame + invar + bronze)

// === CLES D'EXPANSION ===

// Cle 5x5 -- grits + poop (salle 3x3, materiaux bruts)
recipes.addShaped("compact_key_5x5", <nexusabsolu:compact_key_5x5>,
    [[<minecraft:cobblestone>, <nexusabsolu:iron_grit>, <minecraft:cobblestone>],
     [<nexusabsolu:copper_grit>, <cropdusting:poop>, <nexusabsolu:tin_grit>],
     [<minecraft:cobblestone>, <nexusabsolu:iron_grit>, <minecraft:cobblestone>]]);

// Cle 7x7 -- lingots basiques (salle 5x5)
recipes.addShaped("compact_key_7x7", <nexusabsolu:compact_key_7x7>,
    [[<ore:ingotIron>, <ore:ingotCopper>, <ore:ingotIron>],
     [<ore:ingotTin>, <minecraft:redstone>, <ore:ingotTin>],
     [<ore:ingotIron>, <ore:ingotCopper>, <ore:ingotIron>]]);

// Cle 9x9 -- REMOVED: replaced by Advanced Crafting 5x5 recipe in Age1_Signalhee.zs
// (Vossium III + Diarrhee + Blood Magic + Compose C gear + x7 key + CM x7)
recipes.remove(<nexusabsolu:compact_key_9x9>);

// Cle 11x11 -- Thermal (salle 9x9, RF requis)
recipes.addShaped("compact_key_11x11", <nexusabsolu:compact_key_11x11>,
    [[<ore:ingotInvar>, <ore:gearCopper>, <ore:ingotInvar>],
     [<ore:gearCopper>, <thermalexpansion:frame>, <ore:gearCopper>],
     [<ore:ingotInvar>, <ore:gearCopper>, <ore:ingotInvar>]]);

// Cle 13x13 -- IE + EnderIO (salle 11x11)
recipes.addShaped("compact_key_13x13", <nexusabsolu:compact_key_13x13>,
    [[<ore:ingotSteel>, <ore:ingotDarkSteel>, <ore:ingotSteel>],
     [<ore:ingotDarkSteel>, <ore:ingotElectrum>, <ore:ingotDarkSteel>],
     [<ore:ingotSteel>, <ore:ingotDarkSteel>, <ore:ingotSteel>]]);

// === CATALYSEURS DE PHASE ===
// Chaque catalyseur est de plus en plus fou

// Catalyseur Instable -- lingots cuits + cobblestone (premier catalyseur)
recipes.addShaped("catalyseur_instable", <nexusabsolu:catalyseur_instable>,
    [[<ore:cobblestone>, <minecraft:iron_ingot>, <ore:cobblestone>],
     [<thermalfoundation:material:128>, <minecraft:redstone>, <thermalfoundation:material:129>],
     [<ore:cobblestone>, <minecraft:iron_ingot>, <ore:cobblestone>]]);

// Catalyseur Volatile -- lingots + redstone + lapis
recipes.addShaped("catalyseur_volatile", <nexusabsolu:catalyseur_volatile>,
    [[<ore:ingotIron>, <minecraft:redstone>, <ore:ingotIron>],
     [<minecraft:dye:4>, <ore:ingotCopper>, <minecraft:dye:4>],
     [<ore:ingotIron>, <minecraft:redstone>, <ore:ingotIron>]]);

// Catalyseur Critique -- Vossium III + HDPE Sheet
recipes.addShaped("catalyseur_critique", <nexusabsolu:catalyseur_critique>,
    [[<nexusabsolu:vossium_iii_ingot>, <mekanism:polyethene:2>, <nexusabsolu:vossium_iii_ingot>],
     [<mekanism:polyethene:2>, <nexusabsolu:catalyseur_volatile>, <mekanism:polyethene:2>],
     [<nexusabsolu:vossium_iii_ingot>, <mekanism:polyethene:2>, <nexusabsolu:vossium_iii_ingot>]]);

// Catalyseur Resonant -- Thermal + Invar + Electrum
recipes.addShaped("catalyseur_resonant", <nexusabsolu:catalyseur_resonant>,
    [[<ore:ingotInvar>, <ore:ingotElectrum>, <ore:ingotInvar>],
     [<ore:ingotElectrum>, <thermalexpansion:frame>, <ore:ingotElectrum>],
     [<ore:ingotInvar>, <ore:ingotElectrum>, <ore:ingotInvar>]]);

// Catalyseur Absolu -- Dark Steel + Steel + Signalum
recipes.addShaped("catalyseur_absolu", <nexusabsolu:catalyseur_absolu>,
    [[<ore:ingotDarkSteel>, <ore:ingotSignalum>, <ore:ingotDarkSteel>],
     [<ore:ingotSignalum>, <ore:ingotSteel>, <ore:ingotSignalum>],
     [<ore:ingotDarkSteel>, <ore:ingotSignalum>, <ore:ingotDarkSteel>]]);

// NOTE: Les recettes du Condenseur sont dans le mod Java (CondenseurRecipes.java)
// Le joueur met 2 CM + Cle + Catalyseur dans le Condenseur -> CM plus grande

// === CLE DU LABORATOIRE (sortie vers overworld) ===
// Extended Crafting table 3x3 (tier 0)
mods.extendedcrafting.TableCrafting.addShaped(0, <nexusabsolu:lab_key>,
    [[<nexusabsolu:fragment_mecanique>, <minecraft:nether_star>, <nexusabsolu:fragment_stellaire>],
     [<ore:ingotEnderium>, <minecraft:diamond_block>, <ore:ingotEnderium>],
     [<nexusabsolu:fragment_organique>, <minecraft:nether_star>, <nexusabsolu:fragment_organique>]]);

// === 3 FRAGMENTS ===

// Fragment Mecanique -- Age 1 (Thermal + IE)
recipes.addShaped("fragment_mecanique", <nexusabsolu:fragment_mecanique>,
    [[<ore:ingotSteel>, <thermalexpansion:frame>, <ore:ingotSteel>],
     [<ore:gearInvar>, <nexusabsolu:infused_circuit>, <ore:gearInvar>],
     [<ore:ingotSteel>, <nexusabsolu:resonant_coil>, <ore:ingotSteel>]]);

recipes.addShaped("infused_circuit", <nexusabsolu:infused_circuit>,
    [[<ore:ingotElectrum>, <minecraft:redstone>, <ore:ingotElectrum>],
     [<minecraft:redstone>, <ore:ingotCopper>, <minecraft:redstone>],
     [<ore:ingotElectrum>, <minecraft:redstone>, <ore:ingotElectrum>]]);

recipes.addShaped("resonant_coil", <nexusabsolu:resonant_coil>,
    [[null, <ore:ingotInvar>, null],
     [<ore:ingotInvar>, <immersiveengineering:material:1>, <ore:ingotInvar>],
     [null, <ore:ingotInvar>, null]]);

// Fragment Organique -- Age 2 (Botania + Blood Magic)
recipes.addShaped("fragment_organique", <nexusabsolu:fragment_organique>,
    [[<botania:manaresource:0>, <bloodmagic:slate:1>, <botania:manaresource:0>],
     [<bloodmagic:slate:1>, <nexusabsolu:organic_catalyst>, <bloodmagic:slate:1>],
     [<botania:manaresource:0>, <bloodmagic:slate:1>, <botania:manaresource:0>]]);

recipes.addShaped("organic_catalyst", <nexusabsolu:organic_catalyst>,
    [[<cropdusting:poop>, <botania:manaresource:0>, <cropdusting:poop>],
     [<botania:manaresource:0>, <minecraft:golden_apple>, <botania:manaresource:0>],
     [<cropdusting:poop>, <botania:manaresource:0>, <cropdusting:poop>]]);

// Fragment Stellaire -- Age 2 (Astral Sorcery)
recipes.addShaped("fragment_stellaire", <nexusabsolu:fragment_stellaire>,
    [[<astralsorcery:itemcraftingcomponent:0>, <astralsorcery:itemcraftingcomponent:1>, <astralsorcery:itemcraftingcomponent:0>],
     [<astralsorcery:itemcraftingcomponent:1>, <minecraft:nether_star>, <astralsorcery:itemcraftingcomponent:1>],
     [<astralsorcery:itemcraftingcomponent:0>, <astralsorcery:itemcraftingcomponent:1>, <astralsorcery:itemcraftingcomponent:0>]]);

print("[Nexus Absolu] Age0_Keys.zs loaded");

// === PLONGEUR VOSS (v1.0.325) ===
// Outil custom permettant de descendre dans une Compact Machine en cliquant
// dessus, sans etre ejecte vers la sortie comme le PSD vanilla.
// Recette: composes Voss + oeil ender (composant dimensionnel) + lingot Vossium
recipes.addShaped("plongeur_voss", <nexusabsolu:plongeur_voss>,
    [[<nexusabsolu:compose_a>, <minecraft:ender_eye>, <nexusabsolu:compose_a>],
     [<ore:ingotInvar>, <nexusabsolu:vossium_ingot>, <ore:ingotInvar>],
     [null, <ore:ingotIron>, null]]);
