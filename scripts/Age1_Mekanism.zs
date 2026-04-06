// ==================================================
// Age1_Mekanism.zs — Mekanism machine recipe overrides
// ==================================================

// === Pressurized Reaction Chamber ===
recipes.remove(<mekanism:machineblock2:10>);
recipes.addShaped("nexus_prc", <mekanism:machineblock2:10>,
    [[<enderio:item_alloy_ingot:6>, <thermalexpansion:frame>, <enderio:item_alloy_ingot:6>],
     [<mekanism:controlcircuit:1>, <mekanism:machineblock:0>, <mekanism:controlcircuit:1>],
     [<mekanism:gastank>, <mekanism:basicblock:9>, <mekanism:gastank>]]);

// === Dynamic Tank ===
recipes.remove(<mekanism:basicblock:9>);
recipes.addShaped("nexus_dynamic_tank", <mekanism:basicblock:9>,
    [[null, <enderio:item_alloy_ingot:6>, null],
     [<enderio:item_alloy_ingot:6>, <minecraft:bucket>, <enderio:item_alloy_ingot:6>],
     [null, <enderio:item_alloy_ingot:6>, null]]);

// === Enrichment Chamber ===
recipes.remove(<mekanism:machineblock:0>);
recipes.addShaped("nexus_enrichment", <mekanism:machineblock:0>,
    [[<nexusabsolu:invarium_ingot>, <mekanism:controlcircuit:0>, <nexusabsolu:invarium_ingot>],
     [<actuallyadditions:item_crystal:5>, <thermalexpansion:frame>, <actuallyadditions:item_crystal:5>],
     [<nexusabsolu:invarium_ingot>, <mekanism:controlcircuit:0>, <nexusabsolu:invarium_ingot>]]);

// === Crusher ===
recipes.remove(<mekanism:machineblock:3>);
recipes.addShaped("nexus_crusher", <mekanism:machineblock:3>,
    [[<minecraft:redstone>, <mekanism:controlcircuit:1>, <minecraft:redstone>],
     [<minecraft:lava_bucket>, <thermalexpansion:frame>, <minecraft:lava_bucket>],
     [<minecraft:redstone>, <mekanism:controlcircuit:1>, <minecraft:redstone>]]);

print("[Nexus Absolu] Age1_Mekanism.zs loaded");

// === Fluid Input Hatch (Condenseur T2 upgrade) ===
recipes.addShaped("nexus_fluid_input", <nexusabsolu:fluid_input>,
    [[<ore:ingotInvar>, <nexusabsolu:nexus_wall>, <ore:ingotInvar>],
     [<nexusabsolu:nexus_wall>, <minecraft:bucket>, <nexusabsolu:nexus_wall>],
     [<ore:ingotInvar>, <nexusabsolu:nexus_wall>, <ore:ingotInvar>]]);
