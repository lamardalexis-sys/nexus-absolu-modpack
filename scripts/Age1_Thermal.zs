// ============================================
// NEXUS ABSOLU -- Age1_Thermal.zs
// Thermal Expansion inter-mod recipes (salle 9x9+)
// Chaque machine Thermal necessite un composant d'un autre mod
// ============================================

// === MACHINE FRAME -- vanilla OK (Tin + Iron + Glass) ===
// Pas modifie -- c'est la base de tout Thermal

// === PULVERIZER -- necessite composant IE ===
recipes.remove(<thermalexpansion:machine:1>);
recipes.addShaped("nexus_pulverizer", <thermalexpansion:machine:1>,
    [[null, <immersiveengineering:material:1>, null],
     [<ore:gearCopper>, <thermalexpansion:frame>, <ore:gearCopper>],
     [<ore:ingotCopper>, <minecraft:piston>, <ore:ingotCopper>]]);

// === INDUCTION SMELTER -- necessite EnderIO component ===
recipes.remove(<thermalexpansion:machine:3>);
recipes.addShaped("nexus_induction_smelter", <thermalexpansion:machine:3>,
    [[null, <enderio:item_material:0>, null],
     [<ore:gearInvar>, <thermalexpansion:frame>, <ore:gearInvar>],
     [<ore:ingotInvar>, <minecraft:bucket>, <ore:ingotInvar>]]);

// === REDSTONE FURNACE -- PAS modifie (premiere machine du joueur) ===
// Le joueur en a besoin tres tot, pas de gate

// === MAGMATIC DYNAMO -- necessite IE Heater ===
recipes.remove(<thermalexpansion:dynamo:1>);
recipes.addShaped("nexus_magmatic_dynamo", <thermalexpansion:dynamo:1>,
    [[null, <ore:ingotInvar>, null],
     [<ore:ingotInvar>, <immersiveengineering:metal_device1:1>, <ore:ingotInvar>],
     [<ore:gearCopper>, <minecraft:redstone_block>, <ore:gearCopper>]]);

// === STEAM DYNAMO -- PAS modifie (premiere source d'energie) ===

// === SAWMILL -- necessite IE sawblade ===
recipes.remove(<thermalexpansion:machine:5>);
recipes.addShaped("nexus_sawmill", <thermalexpansion:machine:5>,
    [[null, <immersiveengineering:material:1>, null],
     [<ore:gearTin>, <thermalexpansion:frame>, <ore:gearTin>],
     [<ore:ingotTin>, <ore:plankWood>, <ore:ingotTin>]]);

print("[Nexus Absolu] Age1_Thermal.zs loaded");
