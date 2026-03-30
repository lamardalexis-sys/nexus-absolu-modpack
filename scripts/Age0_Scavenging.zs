// =============================================
// AGE 0 — Scavenging & Early Resources
// Nexus Absolu — rechargeable avec /ct reload
// =============================================

// === WALL DUST CONVERSIONS ===
// 4x wall_dust -> 1 cobblestone
recipes.addShapeless("nexus_walldust_to_cobble",
    <minecraft:cobblestone>,
    [<nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>]);

// 2x wall_dust -> 1 gravel
recipes.addShapeless("nexus_walldust_to_gravel",
    <minecraft:gravel>,
    [<nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>]);

// 2x cobblestone_fragment -> 1 cobblestone
recipes.addShapeless("nexus_fragment_to_cobble",
    <minecraft:cobblestone>,
    [<nexusabsolu:cobblestone_fragment>, <nexusabsolu:cobblestone_fragment>]);

// === GRITS -> FURNACE -> INGOTS ===
furnace.addRecipe(<minecraft:iron_ingot>, <nexusabsolu:iron_grit>);
furnace.addRecipe(<thermalfoundation:material:128>, <nexusabsolu:copper_grit>);  // copper ingot
furnace.addRecipe(<thermalfoundation:material:129>, <nexusabsolu:tin_grit>);     // tin ingot
furnace.addRecipe(<thermalfoundation:material:130>, <nexusabsolu:silver_grit>);  // silver ingot
furnace.addRecipe(<thermalfoundation:material:133>, <nexusabsolu:nickel_grit>);  // nickel ingot
furnace.addRecipe(<thermalfoundation:material:131>, <nexusabsolu:lead_grit>);    // lead ingot
furnace.addRecipe(<minecraft:gold_ingot>, <nexusabsolu:gold_grit>);
furnace.addRecipe(<mekanism:ingot:1>, <nexusabsolu:osmium_grit>);               // osmium ingot

// === EARLY TOOLS (backup crafting table recipes) ===
// Si l'Atelier bug, le joueur peut crafter sur la table
recipes.addShaped("nexus_pioche_frag_backup",
    <nexusabsolu:pioche_fragmentee>,
    [[<ore:plankWood>, <ore:plankWood>],
     [null, <minecraft:stick>],
     [null, <minecraft:stick>]]);

recipes.addShaped("nexus_pioche_renf_backup",
    <nexusabsolu:pioche_renforcee>,
    [[<minecraft:iron_nugget>, <nexusabsolu:wall_dust>, <minecraft:iron_nugget>],
     [null, <minecraft:stick>, null],
     [null, <minecraft:stick>, null]]);

// === NEXUS WALL RECIPE (re-craft from wall_dust) ===
recipes.addShaped("nexus_wall_craft",
    <nexusabsolu:nexus_wall> * 2,
    [[<nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>],
     [<nexusabsolu:wall_dust>, <minecraft:iron_nugget>, <nexusabsolu:wall_dust>],
     [<nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>]]);

// === CONDENSEUR RECIPE ===
recipes.addShaped("nexus_condenseur_craft",
    <nexusabsolu:condenseur>,
    [[<minecraft:iron_ingot>, <nexusabsolu:wall_dust>, <minecraft:iron_ingot>],
     [<nexusabsolu:wall_dust>, <minecraft:redstone>, <nexusabsolu:wall_dust>],
     [<minecraft:iron_ingot>, <nexusabsolu:wall_dust>, <minecraft:iron_ingot>]]);

// === ATELIER RECIPE ===
recipes.addShaped("nexus_atelier_craft",
    <nexusabsolu:atelier_voss>,
    [[<ore:plankWood>, <ore:plankWood>, <ore:plankWood>],
     [<nexusabsolu:wall_dust>, null, <nexusabsolu:wall_dust>],
     [<ore:cobblestone>, <ore:cobblestone>, <ore:cobblestone>]]);

print("Age0_Scavenging.zs loaded!");
