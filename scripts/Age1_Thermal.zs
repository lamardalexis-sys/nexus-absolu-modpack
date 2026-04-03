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


// === TOOLTIPS PIOCHES — visibles au survol ===
<nexusabsolu:pioche_fragmentee>.addTooltip("\u00A77Mine les murs des Compact Machines");
<nexusabsolu:pioche_renforcee>.addTooltip("\u00A77Mine les murs des Compact Machines");
<nexusabsolu:pioche_cuivree>.addTooltip("\u00A77Specialisee \u00A76metaux de base");
<nexusabsolu:pioche_ferree>.addTooltip("\u00A77Specialisee \u00A78metaux lourds");
<nexusabsolu:pioche_precieuse>.addTooltip("\u00A77Specialisee \u00A76metaux precieux");
<nexusabsolu:pioche_vossium>.addTooltip("\u00A75Resonne avec la dimension");

// === JEI DESCRIPTIONS — visibles avec U ===
mods.jei.JEI.addDescription(<nexusabsolu:pioche_fragmentee>,
    "PIOCHE FRAGMENTEE",
    "",
    "Frappe les murs des Compact Machines pour extraire des ressources basiques.",
    "",
    "Drops:",
    "- Wall Dust (garanti)",
    "- Cobblestone Fragment (30%)",
    "- Flint (20%)",
    "- Clay (15%)",
    "",
    "Durabilite: bois"
);

mods.jei.JEI.addDescription(<nexusabsolu:pioche_renforcee>,
    "PIOCHE RENFORCEE",
    "",
    "Version amelioree. Extrait des grits metalliques des murs.",
    "",
    "Drops:",
    "- Iron Grit (12%)",
    "- Copper Grit (13%)",
    "- Tin Grit (10%)",
    "- Charbon (10%)",
    "- Redstone (8%)",
    "- Nickel Grit (5%)",
    "- Compose A (10%)",
    "- Wall Dust (32%)",
    "",
    "Durabilite: fer"
);

mods.jei.JEI.addDescription(<nexusabsolu:pioche_cuivree>,
    "PIOCHE CUIVREE",
    "",
    "Specialisee dans les metaux de base.",
    "Necessite un Cristal Enori (Atomic Reconstructor).",
    "",
    "Drops:",
    "- Copper Grit (35%)",
    "- Tin Grit (30%)",
    "- Nickel Grit (25%)",
    "- Wall Dust (10%)",
    "",
    "Durabilite: 1500"
);

mods.jei.JEI.addDescription(<nexusabsolu:pioche_ferree>,
    "PIOCHE FERREE",
    "",
    "Specialisee dans les metaux lourds.",
    "Necessite un Cristal Void (Atomic Reconstructor).",
    "",
    "Drops:",
    "- Iron Grit (40%)",
    "- Lead Grit (25%)",
    "- Silver Grit (25%)",
    "- Wall Dust (10%)",
    "",
    "Durabilite: 1500"
);

mods.jei.JEI.addDescription(<nexusabsolu:pioche_precieuse>,
    "PIOCHE PRECIEUSE",
    "",
    "Specialisee dans les metaux rares.",
    "Necessite un Cristal Restonia (Atomic Reconstructor).",
    "",
    "Drops:",
    "- Gold Grit (45%)",
    "- Osmium Grit (40%)",
    "- Wall Dust (15%)",
    "",
    "Durabilite: 1500"
);

mods.jei.JEI.addDescription(<nexusabsolu:pioche_vossium>,
    "PIOCHE VOSSIUM",
    "",
    "Le Vossium resonne avec la dimension.",
    "Necessite un Cristal Diamatine (Compose A + Atomic Reconstructor).",
    "",
    "Drops:",
    "- Compose A (60%)",
    "- Grains of Infinity (10%)",
    "- Wall Dust (30%)",
    "",
    "Durabilite: 2500",
    "",
    "\"Le Vossium vibre quand il touche les murs.",
    "L'energie cristallisee se libere.\"",
    "- Dr. E. Voss"
);

