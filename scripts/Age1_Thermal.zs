// ============================================
// NEXUS ABSOLU -- Age1_Thermal.zs
// Thermal Expansion inter-mod recipes (salle 9x9+)
// Chaque machine Thermal necessite un composant d'un autre mod
// ============================================

// === MACHINE FRAME -- vanilla OK (Tin + Iron + Glass) ===
// Pas modifie -- c'est la base de tout Thermal

// === PULVERIZER -- necessite composant IE + EnderIO ===
recipes.remove(<thermalexpansion:machine:1>);
recipes.addShaped("nexus_pulverizer", <thermalexpansion:machine:1>,
    [[null, <immersiveengineering:material:1>, null],
     [<enderio:item_material:11>, <thermalexpansion:frame>, <enderio:item_material:11>],
     [<ore:ingotBronze>, <minecraft:piston>, <ore:ingotBronze>]]);

// === INDUCTION SMELTER -- necessite EnderIO component ===
recipes.remove(<thermalexpansion:machine:3>);
recipes.addShaped("nexus_induction_smelter", <thermalexpansion:machine:3>,
    [[null, <enderio:item_material:0>, null],
     [<ore:gearInvar>, <thermalexpansion:frame>, <ore:gearInvar>],
     [<ore:ingotInvar>, <minecraft:bucket>, <ore:ingotInvar>]]);

// === REDSTONE FURNACE -- gear copper remplace par Inf Bimetal Gear ===
recipes.remove(<thermalexpansion:machine:0>);
recipes.addShaped("nexus_redstone_furnace", <thermalexpansion:machine:0>,
    [[null, <minecraft:redstone>, null],
     [<enderio:item_material:11>, <thermalexpansion:frame>, <enderio:item_material:11>],
     [<ore:ingotBronze>, <minecraft:furnace>, <ore:ingotBronze>]]);

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
//   [clay]   [bronze]  [clay]    <- isolation + conducteur
//   [clay]   [glass]   [clay]    <- fluide visible
//   [clay]   [bronze]  [clay]    <- isolation + conducteur
recipes.remove(<thermaldynamics:duct_16>);
recipes.addShaped("nexus_fluiduct_opaque",
    <thermaldynamics:duct_16> * 4,
    [[<minecraft:clay_ball>, <ore:ingotBronze>, <minecraft:clay_ball>],
     [<minecraft:clay_ball>, <ore:blockGlass>, <minecraft:clay_ball>],
     [<minecraft:clay_ball>, <ore:ingotBronze>, <minecraft:clay_ball>]]);


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


// === REINFORCED UPGRADE KIT — simplifie pour CM ===
recipes.remove(<thermalfoundation:upgrade:1>);
recipes.addShaped("nexus_reinforced_upgrade",
    <thermalfoundation:upgrade:1>,
    [[null, <ore:ingotElectrum>, null],
     [<ore:ingotElectrum>, <ore:gearBronze>, <ore:ingotElectrum>],
     [<ore:blockGlass>, <ore:ingotElectrum>, <ore:blockGlass>]]);


// === STEEL STICK — 2 steel = 4 steel sticks ===
recipes.addShaped("nexus_steel_stick",
    <contenttweaker:steel_stick> * 4,
    [[<thermalfoundation:material:160>],
     [<thermalfoundation:material:160>]]);

// === PIOCHE STEELIUM — post-steel pioche ===
//   [vossium]  [steel]       [vossium]
//   [null]     [steel_stick] [null]
//   [null]     [steel_stick] [null]
recipes.addShaped("nexus_pioche_steelium",
    <nexusabsolu:pioche_steelium>,
    [[<contenttweaker:vossium_ingot>, <thermalfoundation:material:160>, <contenttweaker:vossium_ingot>],
     [null, <immersiveengineering:material:2>, null],
     [null, <immersiveengineering:material:2>, null]]);

// === OBSIDIAN FRAGMENT — 4 fragments = 1 obsidian ===
recipes.addShaped("nexus_obsidian_from_fragments",
    <minecraft:obsidian>,
    [[<nexusabsolu:obsidian_fragment>, <nexusabsolu:obsidian_fragment>],
     [<nexusabsolu:obsidian_fragment>, <nexusabsolu:obsidian_fragment>]]);

// === JEI DESCRIPTION — Pioche Steelium ===
mods.jei.JEI.addDescription(<nexusabsolu:pioche_steelium>,
    "PIOCHE STEELIUM",
    "",
    "L'acier renforce le Vossium. Les murs cedent leurs secrets les plus rares.",
    "",
    "Drops:",
    "- Compose B (15%)",
    "- Fragments d'Obsidian (20%) — 4 fragments = 1 Obsidian",
    "- Diamond (15%)",
    "- Emerald (10%)",
    "- Wall Dust (40%)",
    "",
    "Durabilite: 3000"
);

mods.jei.JEI.addDescription(<nexusabsolu:obsidian_fragment>,
    "FRAGMENT D'OBSIDIAN",
    "",
    "Un eclat d'obsidian extrait des murs dimensionnels.",
    "Combine 4 fragments (2x2) pour obtenir 1 Obsidian.",
    "",
    "Source: Pioche Steelium (20%)"
);


// === JEI DESCRIPTION — Atelier du Dr. Voss ===
mods.jei.JEI.addDescription(<nexusabsolu:atelier_voss>,
    "ATELIER DU DR. VOSS",
    "",
    "Table de travail unique du Dr. Voss.",
    "Place 2 ingredients dans les slots de gauche.",
    "",
    "Recettes:",
    "- 2 Planks + 1 Stick = 1 Pioche Fragmentee",
    "- 2 Iron Nuggets + 1 Wall Dust = 1 Pioche Renforcee",
    "- 1 Bloc de Fer + 1 Redstone = 4 CM Walls",
    "- 4 CM Walls + 1 Hopper = 2 Tunnels"
);

mods.jei.JEI.addDescription(<compactmachines3:wallbreakable>,
    "COMPACT MACHINE WALL",
    "",
    "Mur de Compact Machine (cassable).",
    "",
    "Craft: Atelier du Dr. Voss",
    "1 Bloc de Fer + 1 Redstone = 4 Walls"
);

mods.jei.JEI.addDescription(<compactmachines3:tunneltool>,
    "COMPACT MACHINE TUNNEL",
    "",
    "Transfere items, fluides et energie",
    "entre l'interieur et l'exterieur d'une CM.",
    "",
    "Craft: Atelier du Dr. Voss",
    "4 CM Walls + 1 Hopper = 2 Tunnels"
);


// === VOSSIUM-II — Vossium + Compose B dans l'Alloy Kiln ===
// Les canaux-A du Vossium redistribuent l'energie du Compose B
// uniformement dans le reseau cristallin. Sans eux, le metal craque.
mods.immersiveengineering.AlloySmelter.addRecipe(
    <contenttweaker:vossium_ii_ingot>,
    <contenttweaker:vossium_ingot>,
    <nexusabsolu:compose_b>,
    400);

// Vossium-II dans l'EnderIO Alloy Smelter
mods.enderio.AlloySmelter.addRecipe(<contenttweaker:vossium_ii_ingot>, [<contenttweaker:vossium_ingot>, <nexusabsolu:compose_b>], 6000);

// Vossium-II dans le Thermal Induction Smelter
mods.thermalexpansion.InductionSmelter.addRecipe(<contenttweaker:vossium_ii_ingot>, <contenttweaker:vossium_ingot>, <nexusabsolu:compose_b>, 6000);

// === ENGRENAGE DE COMPOSE B ===
// Croix de Compose B + coeur Vossium-II
recipes.addShaped("nexus_compose_gear_b",
    <nexusabsolu:compose_gear_b>,
    [[null, <nexusabsolu:compose_b>, null],
     [<nexusabsolu:compose_b>, <contenttweaker:vossium_ii_ingot>, <nexusabsolu:compose_b>],
     [null, <nexusabsolu:compose_b>, null]]);

// === CONDENSEUR T2 (master) ===
// Le coeur du multibloc 3x3x3 — 4 mods requis
recipes.addShaped("nexus_condenseur_t2",
    <nexusabsolu:condenseur_t2>,
    [[<contenttweaker:vossium_ii_ingot>, <actuallyadditions:item_crystal:2>, <contenttweaker:vossium_ii_ingot>],
     [<ore:ingotSteel>, <thermalexpansion:frame>, <ore:ingotSteel>],
     [<contenttweaker:vossium_ii_ingot>, <nexusabsolu:compose_gear_b>, <contenttweaker:vossium_ii_ingot>]]);

// === ITEM INPUT (receptacle items) ===
recipes.addShaped("nexus_item_input",
    <nexusabsolu:item_input>,
    [[<ore:plateInvar>, <minecraft:hopper>, <ore:plateInvar>],
     [<contenttweaker:vossium_ingot>, <thermalexpansion:frame>, <contenttweaker:vossium_ingot>],
     [<ore:plateInvar>, <ore:gearCopper>, <ore:plateInvar>]]);

// === ITEM OUTPUT (sortie items) ===
recipes.addShaped("nexus_item_output",
    <nexusabsolu:item_output>,
    [[<ore:plateInvar>, <minecraft:piston>, <ore:plateInvar>],
     [<contenttweaker:vossium_ingot>, <thermalexpansion:frame>, <contenttweaker:vossium_ingot>],
     [<ore:plateInvar>, <ore:gearCopper>, <ore:plateInvar>]]);

// === ENERGY INPUT (port RF) ===
recipes.addShaped("nexus_energy_input",
    <nexusabsolu:energy_input>,
    [[<contenttweaker:vossium_ingot>, <thermaldynamics:duct_0>, <contenttweaker:vossium_ingot>],
     [<ore:plateInvar>, <thermalexpansion:cell>, <ore:plateInvar>],
     [<contenttweaker:vossium_ingot>, <minecraft:redstone_block>, <contenttweaker:vossium_ingot>]]);

// === BLOC VOSSIUM-II (9 lingots) ===
recipes.addShaped("nexus_vossium_ii_block",
    <nexusabsolu:vossium_ii_block>,
    [[<contenttweaker:vossium_ii_ingot>, <contenttweaker:vossium_ii_ingot>, <contenttweaker:vossium_ii_ingot>],
     [<contenttweaker:vossium_ii_ingot>, <contenttweaker:vossium_ii_ingot>, <contenttweaker:vossium_ii_ingot>],
     [<contenttweaker:vossium_ii_ingot>, <contenttweaker:vossium_ii_ingot>, <contenttweaker:vossium_ii_ingot>]]);

print("[Nexus Absolu] Vossium-II recipes loaded");

// ============================================
// COMPOSE C — Synthese Thermique
// Induction Smelter: Compose B + Signalum -> Compose C
// ============================================
mods.thermalexpansion.InductionSmelter.addRecipe(<nexusabsolu:compose_c>, <nexusabsolu:compose_b>, <thermalfoundation:material:165>, 8000);

// ============================================
// COMPOSE D — Resonance Dimensionnelle (multi-etapes)
// Etape 1: EnderIO Alloy Smelter: Compose C + Vibrant Alloy -> Compose D brut
// Etape 2: AA Atomic Reconstructor: Compose D brut -> Compose D
// ============================================
mods.enderio.AlloySmelter.addRecipe(<nexusabsolu:compose_d_raw>, [<nexusabsolu:compose_c>, <enderio:item_alloy_ingot:2>], 10000);
mods.actuallyadditions.AtomicReconstructor.addRecipe(<nexusabsolu:compose_d>, <nexusabsolu:compose_d_raw>, 15000);

// ============================================
// VOSSIUM-III (Vossium-II + Compose C) — 3 smelters
// ============================================
mods.immersiveengineering.AlloySmelter.addRecipe(<contenttweaker:vossium_iii_ingot>, <contenttweaker:vossium_ii_ingot>, <nexusabsolu:compose_c>, 500);
mods.enderio.AlloySmelter.addRecipe(<contenttweaker:vossium_iii_ingot>, [<contenttweaker:vossium_ii_ingot>, <nexusabsolu:compose_c>], 8000);
mods.thermalexpansion.InductionSmelter.addRecipe(<contenttweaker:vossium_iii_ingot>, <contenttweaker:vossium_ii_ingot>, <nexusabsolu:compose_c>, 8000);

// ============================================
// VOSSIUM-IV (Vossium-III + Compose D) — 3 smelters
// ============================================
mods.immersiveengineering.AlloySmelter.addRecipe(<contenttweaker:vossium_iv_ingot>, <contenttweaker:vossium_iii_ingot>, <nexusabsolu:compose_d>, 600);
mods.enderio.AlloySmelter.addRecipe(<contenttweaker:vossium_iv_ingot>, [<contenttweaker:vossium_iii_ingot>, <nexusabsolu:compose_d>], 12000);
mods.thermalexpansion.InductionSmelter.addRecipe(<contenttweaker:vossium_iv_ingot>, <contenttweaker:vossium_iii_ingot>, <nexusabsolu:compose_d>, 12000);

// ============================================
// ENGRENAGES C et D
// ============================================
recipes.addShaped("nexus_compose_gear_c",
    <nexusabsolu:compose_gear_c>,
    [[null, <nexusabsolu:compose_c>, null],
     [<nexusabsolu:compose_c>, <contenttweaker:vossium_iii_ingot>, <nexusabsolu:compose_c>],
     [null, <nexusabsolu:compose_c>, null]]);

recipes.addShaped("nexus_compose_gear_d",
    <nexusabsolu:compose_gear_d>,
    [[null, <nexusabsolu:compose_d>, null],
     [<nexusabsolu:compose_d>, <contenttweaker:vossium_iv_ingot>, <nexusabsolu:compose_d>],
     [null, <nexusabsolu:compose_d>, null]]);

// ============================================
// BLOCS VOSSIUM III et IV (9 lingots)
// ============================================
recipes.addShaped("nexus_vossium_iii_block",
    <nexusabsolu:vossium_iii_block>,
    [[<contenttweaker:vossium_iii_ingot>, <contenttweaker:vossium_iii_ingot>, <contenttweaker:vossium_iii_ingot>],
     [<contenttweaker:vossium_iii_ingot>, <contenttweaker:vossium_iii_ingot>, <contenttweaker:vossium_iii_ingot>],
     [<contenttweaker:vossium_iii_ingot>, <contenttweaker:vossium_iii_ingot>, <contenttweaker:vossium_iii_ingot>]]);

recipes.addShaped("nexus_vossium_iv_block",
    <nexusabsolu:vossium_iv_block>,
    [[<contenttweaker:vossium_iv_ingot>, <contenttweaker:vossium_iv_ingot>, <contenttweaker:vossium_iv_ingot>],
     [<contenttweaker:vossium_iv_ingot>, <contenttweaker:vossium_iv_ingot>, <contenttweaker:vossium_iv_ingot>],
     [<contenttweaker:vossium_iv_ingot>, <contenttweaker:vossium_iv_ingot>, <contenttweaker:vossium_iv_ingot>]]);
