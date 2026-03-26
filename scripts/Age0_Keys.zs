// ============================================
// NEXUS ABSOLU — Age0_Keys.zs
// Recettes des cles d'expansion Compact Machine
// Chaque cle necessite des materiaux de l'etape en cours
// ============================================

// ==========================================
// CLE 5x5 — Materiaux bruts (salle 3x3)
// Le joueur n'a QUE des grits, du dirt dust et du poop
// Pas de four, pas de metal fondu
// ==========================================

recipes.addShaped("compact_key_5x5", <nexusabsolu:compact_key_5x5>,
    [[<minecraft:cobblestone>, <nexusabsolu:iron_grit>, <minecraft:cobblestone>],
     [<nexusabsolu:copper_grit>, <cropdusting:poop>, <nexusabsolu:tin_grit>],
     [<minecraft:cobblestone>, <nexusabsolu:iron_grit>, <minecraft:cobblestone>]]);

// ==========================================
// CLE 7x7 — Premiers lingots (salle 5x5)
// Le joueur a le four et le tamis, donc des lingots basiques
// ==========================================

recipes.addShaped("compact_key_7x7", <nexusabsolu:compact_key_7x7>,
    [[<ore:ingotIron>, <ore:ingotCopper>, <ore:ingotIron>],
     [<ore:ingotTin>, <minecraft:redstone>, <ore:ingotTin>],
     [<ore:ingotIron>, <ore:ingotCopper>, <ore:ingotIron>]]);

// ==========================================
// CLE 9x9 — Alliages Tinkers (salle 7x7)
// Le joueur a la Smeltery, il peut faire du Bronze et de l'Electrum
// ==========================================

recipes.addShaped("compact_key_9x9", <nexusabsolu:compact_key_9x9>,
    [[<ore:ingotBronze>, <ore:ingotIron>, <ore:ingotBronze>],
     [<ore:ingotIron>, <minecraft:diamond>, <ore:ingotIron>],
     [<ore:ingotBronze>, <ore:ingotIron>, <ore:ingotBronze>]]);

// ==========================================
// CLE 11x11 — Thermal Expansion (salle 9x9)
// Le joueur a du RF, des machines Thermal, de l'Invar
// ==========================================

recipes.addShaped("compact_key_11x11", <nexusabsolu:compact_key_11x11>,
    [[<ore:ingotInvar>, <ore:gearCopper>, <ore:ingotInvar>],
     [<ore:gearCopper>, <thermalexpansion:frame>, <ore:gearCopper>],
     [<ore:ingotInvar>, <ore:gearCopper>, <ore:ingotInvar>]]);

// ==========================================
// CLE 13x13 — EnderIO + IE (salle 11x11)
// Le joueur a de l'acier IE et du Dark Steel EnderIO
// ==========================================

recipes.addShaped("compact_key_13x13", <nexusabsolu:compact_key_13x13>,
    [[<ore:ingotSteel>, <ore:ingotDarkSteel>, <ore:ingotSteel>],
     [<ore:ingotDarkSteel>, <ore:ingotElectrum>, <ore:ingotDarkSteel>],
     [<ore:ingotSteel>, <ore:ingotDarkSteel>, <ore:ingotSteel>]]);

// ==========================================
// CLE DU LABORATOIRE — Les 3 Fragments (salle 13x13)
// Extended Crafting table 3x3 (tier 0)
// C'est le craft le plus important de la phase compact
// ==========================================

mods.extendedcrafting.TableCrafting.addShaped(0, <nexusabsolu:lab_key>,
    [[<nexusabsolu:fragment_mecanique>, <minecraft:nether_star>, <nexusabsolu:fragment_stellaire>],
     [<ore:ingotEnderium>, <minecraft:diamond_block>, <ore:ingotEnderium>],
     [<nexusabsolu:fragment_organique>, <minecraft:nether_star>, <nexusabsolu:fragment_organique>]]);

// ==========================================
// FRAGMENT MECANIQUE — Age 1 (salle 9x9-11x11)
// Thermal + IE + EnderIO
// ==========================================

recipes.addShaped("fragment_mecanique", <nexusabsolu:fragment_mecanique>,
    [[<ore:ingotSteel>, <thermalexpansion:frame>, <ore:ingotSteel>],
     [<ore:gearInvar>, <nexusabsolu:infused_circuit>, <ore:gearInvar>],
     [<ore:ingotSteel>, <nexusabsolu:resonant_coil>, <ore:ingotSteel>]]);

// Infused Circuit = Thermal + EnderIO
recipes.addShaped("infused_circuit", <nexusabsolu:infused_circuit>,
    [[<ore:ingotElectrum>, <minecraft:redstone>, <ore:ingotElectrum>],
     [<minecraft:redstone>, <ore:ingotCopper>, <minecraft:redstone>],
     [<ore:ingotElectrum>, <minecraft:redstone>, <ore:ingotElectrum>]]);

// Resonant Coil = IE + Thermal
recipes.addShaped("resonant_coil", <nexusabsolu:resonant_coil>,
    [[null, <ore:ingotInvar>, null],
     [<ore:ingotInvar>, <immersiveengineering:material:1>, <ore:ingotInvar>],
     [null, <ore:ingotInvar>, null]]);

// ==========================================
// FRAGMENT ORGANIQUE — Age 2 (salle 13x13)
// Botania + Blood Magic
// ==========================================

recipes.addShaped("fragment_organique", <nexusabsolu:fragment_organique>,
    [[<botania:manaresource:0>, <bloodmagic:slate:1>, <botania:manaresource:0>],
     [<bloodmagic:slate:1>, <nexusabsolu:organic_catalyst>, <bloodmagic:slate:1>],
     [<botania:manaresource:0>, <bloodmagic:slate:1>, <botania:manaresource:0>]]);

// Organic Catalyst = Botania + Pam's + Crop Dusting
recipes.addShaped("organic_catalyst", <nexusabsolu:organic_catalyst>,
    [[<cropdusting:poop>, <botania:manaresource:0>, <cropdusting:poop>],
     [<botania:manaresource:0>, <minecraft:golden_apple>, <botania:manaresource:0>],
     [<cropdusting:poop>, <botania:manaresource:0>, <cropdusting:poop>]]);

// ==========================================
// FRAGMENT STELLAIRE — Age 2 (salle 13x13)
// Astral Sorcery dominant
// ==========================================

recipes.addShaped("fragment_stellaire", <nexusabsolu:fragment_stellaire>,
    [[<astralsorcery:itemcraftingcomponent:0>, <astralsorcery:itemcraftingcomponent:1>, <astralsorcery:itemcraftingcomponent:0>],
     [<astralsorcery:itemcraftingcomponent:1>, <minecraft:nether_star>, <astralsorcery:itemcraftingcomponent:1>],
     [<astralsorcery:itemcraftingcomponent:0>, <astralsorcery:itemcraftingcomponent:1>, <astralsorcery:itemcraftingcomponent:0>]]);

print("[Nexus Absolu] Age0_Keys.zs loaded");
