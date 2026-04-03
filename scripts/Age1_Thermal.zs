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

// =============================================
// AUTO SCAVENGER — mine les murs CM automatiquement
// Necessite pioche + compose gear + iron
// =============================================

// Auto Scavenger: bras mecanique qui frappe les murs
//   [iron]     [pioche]   [iron]      <- tete de minage
//   [redstone] [gear]     [redstone]  <- coeur: compose gear + transfert
//   [iron]     [redstone] [iron]      <- base + alimentation RF
recipes.addShaped("nexus_auto_scavenger",
    <nexusabsolu:auto_scavenger>,
    [[<minecraft:iron_ingot>, <nexusabsolu:pioche_renforcee>, <minecraft:iron_ingot>],
     [<minecraft:redstone>, <nexusabsolu:compose_gear_a>, <minecraft:redstone>],
     [<minecraft:iron_ingot>, <minecraft:redstone>, <minecraft:iron_ingot>]]);

// Fluiduct Opaque (fluid pipe) — custom recipe
// Pour transporter l'eau vers la Steam Dynamo
//   [clay]   [copper]  [clay]    <- isolation + conducteur
//   [clay]   [glass]   [clay]    <- fluide visible
//   [clay]   [copper]  [clay]    <- isolation + conducteur
recipes.remove(<thermaldynamics:duct_16>);
recipes.addShaped("nexus_fluiduct_opaque",
    <thermaldynamics:duct_16> * 4,
    [[<minecraft:clay_ball>, <ore:ingotCopper>, <minecraft:clay_ball>],
     [<minecraft:clay_ball>, <ore:blockGlass>, <minecraft:clay_ball>],
     [<minecraft:clay_ball>, <ore:ingotCopper>, <minecraft:clay_ball>]]);


// === ATOMIC RECONSTRUCTOR — necessite Machine Frame (force Thermal) ===
//   [iron]     [redstone] [iron]      <- energie
//   [redstone] [frame]    [redstone]  <- coeur: Machine Frame Thermal
//   [cobble]   [iron]     [cobble]    <- base
recipes.remove(<actuallyadditions:block_atomic_reconstructor>);
recipes.addShaped("nexus_atomic_reconstructor",
    <actuallyadditions:block_atomic_reconstructor>,
    [[<minecraft:iron_ingot>, <minecraft:redstone>, <minecraft:iron_ingot>],
     [<minecraft:redstone>, <thermalexpansion:frame>, <minecraft:redstone>],
     [<ore:cobblestone>, <minecraft:iron_ingot>, <ore:cobblestone>]]);


// === PIOCHES SPECIALISEES (Age 1) ===
// Chaque pioche cible des grits specifiques
// Pattern pioche standard: 3 materiaux + cristal AA au centre + 2 sticks
// Force le joueur a avoir l'Atomic Reconstructor (Q70) avant
// Cristaux dispo en CM: Enori (iron), Void (coal), Restonia (redstone)

// Pioche Cuivree: copper + tin + nickel grits (1500 uses)
//   [copper] [Enori Crystal] [copper]   <- cristal blanc (iron)
//   [null]   [stick]         [null]
//   [null]   [stick]         [null]
recipes.addShaped("nexus_pioche_cuivree",
    <nexusabsolu:pioche_cuivree>,
    [[<ore:ingotCopper>, <actuallyadditions:item_crystal:5>, <ore:ingotCopper>],
     [null, <minecraft:stick>, null],
     [null, <minecraft:stick>, null]]);

// Pioche Ferree: iron + lead + silver grits (1500 uses)
//   [iron]  [Void Crystal] [iron]       <- cristal noir (coal)
//   [null]  [stick]        [null]
//   [null]  [stick]        [null]
recipes.addShaped("nexus_pioche_ferree",
    <nexusabsolu:pioche_ferree>,
    [[<minecraft:iron_ingot>, <actuallyadditions:item_crystal:3>, <minecraft:iron_ingot>],
     [null, <minecraft:stick>, null],
     [null, <minecraft:stick>, null]]);

// Pioche Precieuse: gold + osmium grits (1500 uses)
//   [gold]  [Restonia Crystal] [gold]   <- cristal rouge (redstone)
//   [null]  [stick]            [null]
//   [null]  [stick]            [null]
recipes.addShaped("nexus_pioche_precieuse",
    <nexusabsolu:pioche_precieuse>,
    [[<minecraft:gold_ingot>, <actuallyadditions:item_crystal:0>, <minecraft:gold_ingot>],
     [null, <minecraft:stick>, null],
     [null, <minecraft:stick>, null]]);

// Pioche Vossium: compose_a only (2500 uses, 70% drop rate!)
// Cristal Dimensionnel: Compose A -> Diamatine Crystal via Atomic Reconstructor
//   [vossium]  [Diamatine Crystal] [vossium]  <- cristal bleu (compose_a custom)
//   [null]     [stick]             [null]
//   [null]     [stick]             [null]
recipes.addShaped("nexus_pioche_vossium",
    <nexusabsolu:pioche_vossium>,
    [[<contenttweaker:vossium_ingot>, <actuallyadditions:item_crystal:2>, <contenttweaker:vossium_ingot>],
     [null, <minecraft:stick>, null],
     [null, <minecraft:stick>, null]]);

// Recette custom: Compose A -> Diamatine Crystal via Atomic Reconstructor
// Le Compose A resonne avec la dimension et cristallise en Diamatine
mods.actuallyadditions.AtomicReconstructor.addRecipe(<actuallyadditions:item_crystal:2>, <nexusabsolu:compose_a>, 5000);

