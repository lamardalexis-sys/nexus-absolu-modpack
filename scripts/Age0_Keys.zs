// ============================================
// NEXUS ABSOLU -- Age0_Keys.zs
// Cles d'expansion + Compact Machines + Fragments
// Cle = composant central pour crafter la Compact Machine
// ============================================

// ==========================================
// CLE 5x5 -- Materiaux bruts (salle 3x3)
// ==========================================

recipes.addShaped("compact_key_5x5", <nexusabsolu:compact_key_5x5>,
    [[<minecraft:cobblestone>, <nexusabsolu:iron_grit>, <minecraft:cobblestone>],
     [<nexusabsolu:copper_grit>, <cropdusting:poop>, <nexusabsolu:tin_grit>],
     [<minecraft:cobblestone>, <nexusabsolu:iron_grit>, <minecraft:cobblestone>]]);

// ==========================================
// CLE 7x7 -- Premiers lingots (salle 5x5)
// ==========================================

recipes.addShaped("compact_key_7x7", <nexusabsolu:compact_key_7x7>,
    [[<ore:ingotIron>, <ore:ingotCopper>, <ore:ingotIron>],
     [<ore:ingotTin>, <minecraft:redstone>, <ore:ingotTin>],
     [<ore:ingotIron>, <ore:ingotCopper>, <ore:ingotIron>]]);

// ==========================================
// CLE 9x9 -- Alliages Tinkers (salle 7x7)
// ==========================================

recipes.addShaped("compact_key_9x9", <nexusabsolu:compact_key_9x9>,
    [[<ore:ingotBronze>, <ore:ingotIron>, <ore:ingotBronze>],
     [<ore:ingotIron>, <minecraft:diamond>, <ore:ingotIron>],
     [<ore:ingotBronze>, <ore:ingotIron>, <ore:ingotBronze>]]);

// ==========================================
// CLE 11x11 -- Thermal Expansion (salle 9x9)
// ==========================================

recipes.addShaped("compact_key_11x11", <nexusabsolu:compact_key_11x11>,
    [[<ore:ingotInvar>, <ore:gearCopper>, <ore:ingotInvar>],
     [<ore:gearCopper>, <thermalexpansion:frame>, <ore:gearCopper>],
     [<ore:ingotInvar>, <ore:gearCopper>, <ore:ingotInvar>]]);

// ==========================================
// CLE 13x13 -- EnderIO + IE (salle 11x11)
// ==========================================

recipes.addShaped("compact_key_13x13", <nexusabsolu:compact_key_13x13>,
    [[<ore:ingotSteel>, <ore:ingotDarkSteel>, <ore:ingotSteel>],
     [<ore:ingotDarkSteel>, <ore:ingotElectrum>, <ore:ingotDarkSteel>],
     [<ore:ingotSteel>, <ore:ingotDarkSteel>, <ore:ingotSteel>]]);

// ==========================================
// COMPACT MACHINES -- Cle + materiaux = machine
// machine:1 = Small 5x5
// machine:2 = Normal 7x7
// machine:3 = Large 9x9
// machine:4 = Giant 11x11
// machine:5 = Maximum 13x13
// ==========================================

recipes.remove(<compactmachines3:machine:1>);
recipes.remove(<compactmachines3:machine:2>);
recipes.remove(<compactmachines3:machine:3>);
recipes.remove(<compactmachines3:machine:4>);
recipes.remove(<compactmachines3:machine:5>);

recipes.addShaped("compact_machine_5x5", <compactmachines3:machine:1>,
    [[<ore:ingotIron>, <minecraft:glass>, <ore:ingotIron>],
     [<minecraft:glass>, <nexusabsolu:compact_key_5x5>, <minecraft:glass>],
     [<ore:ingotIron>, <minecraft:glass>, <ore:ingotIron>]]);

recipes.addShaped("compact_machine_7x7", <compactmachines3:machine:2>,
    [[<ore:ingotCopper>, <minecraft:redstone_block>, <ore:ingotCopper>],
     [<minecraft:redstone_block>, <nexusabsolu:compact_key_7x7>, <minecraft:redstone_block>],
     [<ore:ingotCopper>, <minecraft:redstone_block>, <ore:ingotCopper>]]);

recipes.addShaped("compact_machine_9x9", <compactmachines3:machine:3>,
    [[<ore:ingotBronze>, <minecraft:iron_block>, <ore:ingotBronze>],
     [<minecraft:iron_block>, <nexusabsolu:compact_key_9x9>, <minecraft:iron_block>],
     [<ore:ingotBronze>, <minecraft:iron_block>, <ore:ingotBronze>]]);

recipes.addShaped("compact_machine_11x11", <compactmachines3:machine:4>,
    [[<ore:ingotInvar>, <ore:blockIron>, <ore:ingotInvar>],
     [<ore:blockIron>, <nexusabsolu:compact_key_11x11>, <ore:blockIron>],
     [<ore:ingotInvar>, <ore:blockIron>, <ore:ingotInvar>]]);

recipes.addShaped("compact_machine_13x13", <compactmachines3:machine:5>,
    [[<ore:ingotSteel>, <ore:blockSteel>, <ore:ingotSteel>],
     [<ore:blockSteel>, <nexusabsolu:compact_key_13x13>, <ore:blockSteel>],
     [<ore:ingotSteel>, <ore:blockSteel>, <ore:ingotSteel>]]);

// ==========================================
// CLE DU LABORATOIRE -- Les 3 Fragments (salle 13x13)
// Extended Crafting table 3x3 (tier 0)
// ==========================================

mods.extendedcrafting.TableCrafting.addShaped(0, <nexusabsolu:lab_key>,
    [[<nexusabsolu:fragment_mecanique>, <minecraft:nether_star>, <nexusabsolu:fragment_stellaire>],
     [<ore:ingotEnderium>, <minecraft:diamond_block>, <ore:ingotEnderium>],
     [<nexusabsolu:fragment_organique>, <minecraft:nether_star>, <nexusabsolu:fragment_organique>]]);

// ==========================================
// FRAGMENT MECANIQUE -- Age 1 (Thermal + IE)
// ==========================================

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

// ==========================================
// FRAGMENT ORGANIQUE -- Age 2 (Botania + Blood Magic)
// ==========================================

recipes.addShaped("fragment_organique", <nexusabsolu:fragment_organique>,
    [[<botania:manaresource:0>, <bloodmagic:slate:1>, <botania:manaresource:0>],
     [<bloodmagic:slate:1>, <nexusabsolu:organic_catalyst>, <bloodmagic:slate:1>],
     [<botania:manaresource:0>, <bloodmagic:slate:1>, <botania:manaresource:0>]]);

recipes.addShaped("organic_catalyst", <nexusabsolu:organic_catalyst>,
    [[<cropdusting:poop>, <botania:manaresource:0>, <cropdusting:poop>],
     [<botania:manaresource:0>, <minecraft:golden_apple>, <botania:manaresource:0>],
     [<cropdusting:poop>, <botania:manaresource:0>, <cropdusting:poop>]]);

// ==========================================
// FRAGMENT STELLAIRE -- Age 2 (Astral Sorcery)
// ==========================================

recipes.addShaped("fragment_stellaire", <nexusabsolu:fragment_stellaire>,
    [[<astralsorcery:itemcraftingcomponent:0>, <astralsorcery:itemcraftingcomponent:1>, <astralsorcery:itemcraftingcomponent:0>],
     [<astralsorcery:itemcraftingcomponent:1>, <minecraft:nether_star>, <astralsorcery:itemcraftingcomponent:1>],
     [<astralsorcery:itemcraftingcomponent:0>, <astralsorcery:itemcraftingcomponent:1>, <astralsorcery:itemcraftingcomponent:0>]]);

print("[Nexus Absolu] Age0_Keys.zs loaded");
