// ============================================
// NEXUS ABSOLU — Age0_Scavenging.zs
// Nexus Wall recipes, grits, fragments
// ============================================

// NOTE: Le scavenging des murs (drops quand on casse un Nexus Wall)
// est geree par le mod Java (ScavengeEventHandler.java)
// Ce script gere les RECETTES de conversion des drops

// ==========================================
// NEXUS WALL — Renewable crafting
// ==========================================

// 4 wall dust → 2 nexus walls (boucle infinie)
recipes.addShaped("nexus_wall_craft", <nexusabsolu:nexus_wall> * 2,
    [[<nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>],
     [<nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>]]);

// ==========================================
// GRITS → NUGGETS (furnace, inefficace)
// 1 grit = 1 nugget, il faut 9 grits pour 1 lingot
// C'est LENT et INTENTIONNEL — le tamis Ex Nihilo est mieux
// ==========================================

furnace.addRecipe(<minecraft:iron_nugget>, <nexusabsolu:iron_grit>);
furnace.addRecipe(<thermalfoundation:material:192>, <nexusabsolu:copper_grit>);   // copper nugget
furnace.addRecipe(<thermalfoundation:material:193>, <nexusabsolu:tin_grit>);      // tin nugget
furnace.addRecipe(<thermalfoundation:material:194>, <nexusabsolu:silver_grit>);   // silver nugget
furnace.addRecipe(<thermalfoundation:material:195>, <nexusabsolu:lead_grit>);     // lead nugget
furnace.addRecipe(<thermalfoundation:material:197>, <nexusabsolu:nickel_grit>);   // nickel nugget
furnace.addRecipe(<minecraft:gold_nugget>, <nexusabsolu:gold_grit>);
furnace.addRecipe(<mekanism:nugget:1>, <nexusabsolu:osmium_grit>);               // osmium nugget

// ==========================================
// FRAGMENTS → Items complets
// ==========================================

// 4 cobblestone fragments → 1 cobblestone
recipes.addShaped("cobble_from_fragments", <minecraft:cobblestone>,
    [[<nexusabsolu:cobblestone_fragment>, <nexusabsolu:cobblestone_fragment>],
     [<nexusabsolu:cobblestone_fragment>, <nexusabsolu:cobblestone_fragment>]]);

// 4 diamond fragments → 1 diamond
recipes.addShaped("diamond_from_fragments", <minecraft:diamond>,
    [[<nexusabsolu:diamond_fragment>, <nexusabsolu:diamond_fragment>],
     [<nexusabsolu:diamond_fragment>, <nexusabsolu:diamond_fragment>]]);

// 4 emerald fragments → 1 emerald
recipes.addShaped("emerald_from_fragments", <minecraft:emerald>,
    [[<nexusabsolu:emerald_fragment>, <nexusabsolu:emerald_fragment>],
     [<nexusabsolu:emerald_fragment>, <nexusabsolu:emerald_fragment>]]);

// 8 ender pearl fragments → 1 ender pearl (plus rare)
recipes.addShaped("ender_pearl_from_fragments", <minecraft:ender_pearl>,
    [[<nexusabsolu:ender_pearl_fragment>, <nexusabsolu:ender_pearl_fragment>, <nexusabsolu:ender_pearl_fragment>],
     [<nexusabsolu:ender_pearl_fragment>, null, <nexusabsolu:ender_pearl_fragment>],
     [<nexusabsolu:ender_pearl_fragment>, <nexusabsolu:ender_pearl_fragment>, <nexusabsolu:ender_pearl_fragment>]]);

// 4 obsidian fragments → 1 obsidian
recipes.addShaped("obsidian_from_fragments", <minecraft:obsidian>,
    [[<nexusabsolu:obsidian_fragment>, <nexusabsolu:obsidian_fragment>],
     [<nexusabsolu:obsidian_fragment>, <nexusabsolu:obsidian_fragment>]]);

// ==========================================
// SUPER FERTILIZER (poop + bonemeal)
// ==========================================

recipes.addShapeless("super_fertilizer", <nexusabsolu:super_fertilizer> * 4,
    [<cropdusting:poop>, <minecraft:dye:15>, <minecraft:dye:15>]);

print("[Nexus Absolu] Age0_Scavenging.zs loaded");
