// ==================================================
// Age2_Overworld.zs — Contenu Overworld (Age 2+)
//
// Introduit le minerai Tutuosss (Overworld uniquement)
// comme gate obligatoire pour le Digital Miner Mekanism.
//
// Chaine:
//   tutuosss_ore (minage manuel Overworld) -> tuosss_row (drop)
//   tuosss_row (furnace) -> tuosss_ingot
//   tuosss_ingot + mats Age 1 endgame -> Digital Miner
//
// Objectif: forcer le joueur a sortir des Compact Machines
// et miner manuellement avant d'automatiser via Digital Miner.
// ==================================================

// === SMELTING: tuosss_row -> tuosss_ingot (4 XP) ===
furnace.addRecipe(<nexusabsolu:tuosss_ingot>, <nexusabsolu:tuosss_row>, 1.0);

// Induction Smelter (Thermal) - 2x output avec sable comme secondaire
mods.thermalexpansion.InductionSmelter.addRecipe(
    <nexusabsolu:tuosss_ingot> * 2,
    <nexusabsolu:tuosss_row>,
    <minecraft:sand>,
    1200
);

// Alloy Smelter (IE) - 1x output
mods.immersiveengineering.AlloySmelter.addRecipe(
    <nexusabsolu:tuosss_ingot>,
    <nexusabsolu:tuosss_row>,
    <minecraft:sand>,
    300
);

// === DIGITAL MINER MEKANISM - RECETTE COMPLEXIFIEE ===
// Meta 4 = Digital Miner dans mekanism:machineblock
// Gate dur: tuosss_ingot (Overworld) + Vossium IV + Compose D + core Mekanism
recipes.remove(<mekanism:machineblock:4>);
recipes.addShaped("nexus_digital_miner",
    <mekanism:machineblock:4>,
    [[<ore:ingotTuosss>,        <mekanism:atomicalloy>,     <ore:ingotTuosss>],
     [<nexusabsolu:vossium_iv_ingot>, <mekanism:teleportationcore>, <nexusabsolu:vossium_iv_ingot>],
     [<ore:ingotTuosss>,        <nexusabsolu:compose_d>,    <ore:ingotTuosss>]]);

print("[Nexus Absolu] Age2_Overworld.zs loaded");
