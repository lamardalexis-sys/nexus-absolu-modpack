// =============================================
// AGE 0 — Scavenging & Early Resources
// Nexus Absolu
// Recettes basees sur les patterns E2E:
//   - Symetrie gauche-droite
//   - Centre = composant cle
//   - Haut = fonction, Bas = base/power
//   - Cotes = structure
// =============================================

// === WALL DUST CONVERSIONS ===
// 4x wall_dust -> 1 string (fibres des murs)
recipes.addShapeless("nexus_walldust_to_string",
    <minecraft:string>,
    [<nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>, <nexusabsolu:wall_dust>]);

// GRAVEL: Ex Nihilo hammer cobble -> gravel -> sieve
// 4x cobblestone_fragment -> 1 cobblestone
recipes.addShapeless("nexus_fragment_to_cobble",
    <minecraft:cobblestone>,
    [<nexusabsolu:cobblestone_fragment>, <nexusabsolu:cobblestone_fragment>, <nexusabsolu:cobblestone_fragment>, <nexusabsolu:cobblestone_fragment>]);

// === GRITS: ONLY from Pioche Renforcee ===
// 4 grits = 1 raw grit -> furnace -> lingot

recipes.addShapeless("nexus_raw_iron_grit",
    <nexusabsolu:raw_iron_grit>,
    [<nexusabsolu:iron_grit>, <nexusabsolu:iron_grit>, <nexusabsolu:iron_grit>, <nexusabsolu:iron_grit>]);
recipes.addShapeless("nexus_raw_copper_grit",
    <nexusabsolu:raw_copper_grit>,
    [<nexusabsolu:copper_grit>, <nexusabsolu:copper_grit>, <nexusabsolu:copper_grit>, <nexusabsolu:copper_grit>]);
recipes.addShapeless("nexus_raw_tin_grit",
    <nexusabsolu:raw_tin_grit>,
    [<nexusabsolu:tin_grit>, <nexusabsolu:tin_grit>, <nexusabsolu:tin_grit>, <nexusabsolu:tin_grit>]);
recipes.addShapeless("nexus_raw_gold_grit",
    <nexusabsolu:raw_gold_grit>,
    [<nexusabsolu:gold_grit>, <nexusabsolu:gold_grit>, <nexusabsolu:gold_grit>, <nexusabsolu:gold_grit>]);
recipes.addShapeless("nexus_raw_silver_grit",
    <nexusabsolu:raw_silver_grit>,
    [<nexusabsolu:silver_grit>, <nexusabsolu:silver_grit>, <nexusabsolu:silver_grit>, <nexusabsolu:silver_grit>]);
recipes.addShapeless("nexus_raw_lead_grit",
    <nexusabsolu:raw_lead_grit>,
    [<nexusabsolu:lead_grit>, <nexusabsolu:lead_grit>, <nexusabsolu:lead_grit>, <nexusabsolu:lead_grit>]);
recipes.addShapeless("nexus_raw_nickel_grit",
    <nexusabsolu:raw_nickel_grit>,
    [<nexusabsolu:nickel_grit>, <nexusabsolu:nickel_grit>, <nexusabsolu:nickel_grit>, <nexusabsolu:nickel_grit>]);
recipes.addShapeless("nexus_raw_osmium_grit",
    <nexusabsolu:raw_osmium_grit>,
    [<nexusabsolu:osmium_grit>, <nexusabsolu:osmium_grit>, <nexusabsolu:osmium_grit>, <nexusabsolu:osmium_grit>]);

// === RAW GRITS -> FURNACE -> INGOTS ===
furnace.addRecipe(<minecraft:iron_ingot>, <nexusabsolu:raw_iron_grit>);
furnace.addRecipe(<thermalfoundation:material:128>, <nexusabsolu:raw_copper_grit>);
furnace.addRecipe(<thermalfoundation:material:129>, <nexusabsolu:raw_tin_grit>);
furnace.addRecipe(<thermalfoundation:material:130>, <nexusabsolu:raw_silver_grit>);
furnace.addRecipe(<thermalfoundation:material:133>, <nexusabsolu:raw_nickel_grit>);
furnace.addRecipe(<thermalfoundation:material:131>, <nexusabsolu:raw_lead_grit>);
furnace.addRecipe(<minecraft:gold_ingot>, <nexusabsolu:raw_gold_grit>);
furnace.addRecipe(<mekanism:ingot:1>, <nexusabsolu:raw_osmium_grit>);

// === BLOCK VANILLA NUGGETS -> INGOT ===
recipes.removeByRecipeName("minecraft:iron_ingot_from_nuggets");
recipes.removeByRecipeName("minecraft:gold_ingot_from_nuggets");

// === IRON INGOT -> 8 NUGGETS (not 9) ===
recipes.removeByRecipeName("minecraft:iron_nugget");
recipes.addShapeless("nexus_iron_to_nuggets",
    <minecraft:iron_nugget> * 8,
    [<minecraft:iron_ingot>]);

// === BRONZE: Alloy Kiln (IE) seulement ===
// Pas de craft shapeless — uniquement via Alloy Kiln + charbon
// Copper + Tin = Bronze
mods.immersiveengineering.AlloySmelter.addRecipe(<thermalfoundation:material:163> * 4, <ore:ingotCopper> * 3, <ore:ingotTin>, 400);
// Iron + Nickel = Invar
mods.immersiveengineering.AlloySmelter.addRecipe(<thermalfoundation:material:162> * 3, <ore:ingotIron> * 2, <ore:ingotNickel>, 400);

// =============================================
// TOOLS — forme classique outil (tete/binding/manche)
// =============================================

// Pioche Fragmentee: tete bois + pointe cobble (symetrique)
//   [plank]    [cobble]   [plank]    <- tete: bois + pointe roche
//   [null]     [stick]    [null]     <- manche
//   [null]     [stick]    [null]
// NOTE: cobble = 4 fragments, donc progression naturelle
recipes.remove(<nexusabsolu:pioche_fragmentee>);
recipes.addShaped("nexus_pioche_frag",
    <nexusabsolu:pioche_fragmentee>,
    [[<ore:plankWood>, <ore:cobblestone>, <ore:plankWood>],
     [null, <minecraft:stick>, null],
     [null, <minecraft:stick>, null]]);

// Pioche Renforcee: tete metal + flint + binding string
//   [nugget] [flint]  [nugget]    <- tete: metal edges + flint pointe
//   [null]   [string] [null]      <- binding
//   [null]   [stick]  [null]      <- manche
recipes.remove(<nexusabsolu:pioche_renforcee>);
recipes.addShaped("nexus_pioche_renf",
    <nexusabsolu:pioche_renforcee>,
    [[<minecraft:iron_nugget>, <minecraft:flint>, <minecraft:iron_nugget>],
     [null, <minecraft:string>, null],
     [null, <minecraft:stick>, null]]);

// =============================================
// BLOCS & MACHINES — pattern E2E (symetrique, core au centre)
// =============================================

// Atelier du Dr. Voss: table de travail avancee
//   [plank]  [string] [plank]    <- surface de travail + ficelle
//   [cobble] [plank]  [cobble]   <- structure + plateau bois central
//   [stick]  [cobble] [stick]    <- pieds + base solide
recipes.remove(<nexusabsolu:atelier_voss>);
recipes.addShaped("nexus_atelier_craft",
    <nexusabsolu:atelier_voss>,
    [[<ore:plankWood>, <minecraft:string>, <ore:plankWood>],
     [<ore:cobblestone>, <ore:plankWood>, <ore:cobblestone>],
     [<minecraft:stick>, <ore:cobblestone>, <minecraft:stick>]]);

// Four vanilla override: boite de pierre + flint allumeur + clay isolation
//   [cobble] [cobble] [cobble]   <- murs
//   [cobble] [flint]  [cobble]   <- allumeur au centre
//   [cobble] [clay]   [cobble]   <- base isolante
recipes.remove(<minecraft:furnace>);
recipes.addShaped("nexus_furnace",
    <minecraft:furnace>,
    [[<ore:cobblestone>, <ore:cobblestone>, <ore:cobblestone>],
     [<ore:cobblestone>, <minecraft:flint>, <ore:cobblestone>],
     [<ore:cobblestone>, <minecraft:clay_ball>, <ore:cobblestone>]]);

// Nexus Wall x2: mur dimensionnel renforce
//   [cobble] [clay]   [cobble]   <- mur solide + mortier
//   [clay]   [nugget] [clay]     <- armature fer + mortier
//   [cobble] [flint]  [cobble]   <- base renforcee + silex
recipes.remove(<nexusabsolu:nexus_wall>);
recipes.addShaped("nexus_wall_craft",
    <nexusabsolu:nexus_wall> * 2,
    [[<ore:cobblestone>, <minecraft:clay_ball>, <ore:cobblestone>],
     [<minecraft:clay_ball>, <minecraft:iron_nugget>, <minecraft:clay_ball>],
     [<ore:cobblestone>, <minecraft:flint>, <ore:cobblestone>]]);

// Machine Frame: composant central de toutes les machines (pattern E2E)
//   [iron]  [glass] [iron]     <- structure metallique
//   [glass] [gear]  [glass]    <- coeur: engrenage compose + vision
//   [iron]  [glass] [iron]     <- structure metallique
recipes.remove(<thermalexpansion:frame>);
recipes.addShaped("nexus_machine_frame",
    <thermalexpansion:frame>,
    [[<minecraft:iron_ingot>, <ore:blockGlass>, <minecraft:iron_ingot>],
     [<ore:blockGlass>, <nexusabsolu:compose_gear_a>, <ore:blockGlass>],
     [<minecraft:iron_ingot>, <ore:blockGlass>, <minecraft:iron_ingot>]]);

// Condenseur Dimensionnel: fusionne deux CMs
//   [bronze] [compose]  [bronze]      <- contacts energetiques
//   [invar]  [frame]    [invar]       <- coeur: machine frame + structure invar
//   [invar]  [red_bloc] [invar]       <- base: redstone bloc (9 redstone!)
recipes.remove(<nexusabsolu:condenseur>);
recipes.addShaped("nexus_condenseur_craft",
    <nexusabsolu:condenseur>,
    [[<ore:ingotBronze>, <nexusabsolu:compose_a>, <ore:ingotBronze>],
     [<ore:ingotInvar>, <thermalexpansion:frame>, <ore:ingotInvar>],
     [<ore:ingotInvar>, <minecraft:redstone_block>, <ore:ingotInvar>]]);

// Convertisseur du Dr. Voss: transforme compose en RF
//   [iron]     [compose] [iron]       <- compose en haut (source energie)
//   [redstone] [frame]   [redstone]   <- coeur: machine frame + transfert
//   [iron]     [redstone][iron]       <- structure + sortie RF
recipes.remove(<nexusabsolu:convertisseur_voss>);
recipes.addShaped("nexus_convertisseur_craft",
    <nexusabsolu:convertisseur_voss>,
    [[<minecraft:iron_ingot>, <nexusabsolu:compose_a>, <minecraft:iron_ingot>],
     [<minecraft:redstone>, <thermalexpansion:frame>, <minecraft:redstone>],
     [<minecraft:iron_ingot>, <minecraft:redstone>, <minecraft:iron_ingot>]]);

// Engrenage de Compose A: croix + axe bronze
//   [null]     [compose] [null]      <- dent haut
//   [compose]  [bronze]  [compose]   <- axe bronze + dents laterales
//   [null]     [compose] [null]      <- dent bas
recipes.remove(<nexusabsolu:compose_gear_a>);
recipes.addShaped("nexus_compose_gear_a",
    <nexusabsolu:compose_gear_a>,
    [[null, <nexusabsolu:compose_a>, null],
     [<nexusabsolu:compose_a>, <ore:ingotBronze>, <nexusabsolu:compose_a>],
     [null, <nexusabsolu:compose_a>, null]]);

// Compose Block A: 8 compose + gear au centre
recipes.remove(<nexusabsolu:compose_block_a>);
recipes.addShaped("nexus_compose_block_a",
    <nexusabsolu:compose_block_a>,
    [[<nexusabsolu:compose_a>, <nexusabsolu:compose_a>, <nexusabsolu:compose_a>],
     [<nexusabsolu:compose_a>, <nexusabsolu:compose_gear_a>, <nexusabsolu:compose_a>],
     [<nexusabsolu:compose_a>, <nexusabsolu:compose_a>, <nexusabsolu:compose_a>]]);

// Compose Blocks B-E: 9x compose (pas de gear)
recipes.remove(<nexusabsolu:compose_block_b>);
recipes.addShaped("nexus_compose_block_b",
    <nexusabsolu:compose_block_b>,
    [[<nexusabsolu:compose_b>, <nexusabsolu:compose_b>, <nexusabsolu:compose_b>],
     [<nexusabsolu:compose_b>, <nexusabsolu:compose_b>, <nexusabsolu:compose_b>],
     [<nexusabsolu:compose_b>, <nexusabsolu:compose_b>, <nexusabsolu:compose_b>]]);
recipes.remove(<nexusabsolu:compose_block_c>);
recipes.addShaped("nexus_compose_block_c",
    <nexusabsolu:compose_block_c>,
    [[<nexusabsolu:compose_c>, <nexusabsolu:compose_c>, <nexusabsolu:compose_c>],
     [<nexusabsolu:compose_c>, <nexusabsolu:compose_c>, <nexusabsolu:compose_c>],
     [<nexusabsolu:compose_c>, <nexusabsolu:compose_c>, <nexusabsolu:compose_c>]]);
recipes.remove(<nexusabsolu:compose_block_d>);
recipes.addShaped("nexus_compose_block_d",
    <nexusabsolu:compose_block_d>,
    [[<nexusabsolu:compose_d>, <nexusabsolu:compose_d>, <nexusabsolu:compose_d>],
     [<nexusabsolu:compose_d>, <nexusabsolu:compose_d>, <nexusabsolu:compose_d>],
     [<nexusabsolu:compose_d>, <nexusabsolu:compose_d>, <nexusabsolu:compose_d>]]);
recipes.remove(<nexusabsolu:compose_block_e>);
recipes.addShaped("nexus_compose_block_e",
    <nexusabsolu:compose_block_e>,
    [[<nexusabsolu:compose_e>, <nexusabsolu:compose_e>, <nexusabsolu:compose_e>],
     [<nexusabsolu:compose_e>, <nexusabsolu:compose_e>, <nexusabsolu:compose_e>],
     [<nexusabsolu:compose_e>, <nexusabsolu:compose_e>, <nexusabsolu:compose_e>]]);

// =============================================
// ENERGIE — Stirling Dynamo (gatee derriere bronze)
// =============================================

// Stirling Dynamo: gear mecanique + boitier + base bronze
//   [null]   [gear]     [null]      <- conversion mecanique
//   [cobble] [redstone] [cobble]    <- boitier + transfert
//   [bronze] [redstone] [bronze]    <- base bronze + sortie
recipes.remove(<thermalexpansion:dynamo:0>);
recipes.addShaped("nexus_stirling_dynamo",
    <thermalexpansion:dynamo:0>,
    [[null, <nexusabsolu:compose_gear_a>, null],
     [<ore:cobblestone>, <minecraft:redstone>, <ore:cobblestone>],
     [<ore:ingotBronze>, <minecraft:redstone>, <ore:ingotBronze>]]);

// =============================================
// TRANSPORT ENERGIE — Fluxduct Leadstone
// =============================================

// Fluxduct Leadstone x4: cable isole (clay+string = vraie isolation)
//   [clay]   [nugget] [clay]     <- isolation argile + conducteur
//   [string] [redst]  [string]   <- fibre conductrice + energie
//   [clay]   [nugget] [clay]     <- isolation argile + conducteur
recipes.remove(<thermaldynamics:duct_0>);
recipes.addShaped("nexus_fluxduct_leadstone",
    <thermaldynamics:duct_0> * 4,
    [[<minecraft:clay_ball>, <minecraft:iron_nugget>, <minecraft:clay_ball>],
     [<minecraft:string>, <minecraft:redstone>, <minecraft:string>],
     [<minecraft:clay_ball>, <minecraft:iron_nugget>, <minecraft:clay_ball>]]);

// =============================================
// STOCKAGE — Silver Chest custom
// =============================================

// Silver Chest: coffre renforce
//   [bronze] [iron]   [bronze]    <- couvercle renforce
//   [iron]   [chest]  [iron]      <- structure + coffre vanilla
//   [bronze] [string] [bronze]    <- base + mecanisme fermeture
recipes.remove(<ironchest:iron_chest:4>);
recipes.addShaped("nexus_silver_chest",
    <ironchest:iron_chest:4>,
    [[<ore:ingotBronze>, <minecraft:iron_ingot>, <ore:ingotBronze>],
     [<minecraft:iron_ingot>, <minecraft:chest>, <minecraft:iron_ingot>],
     [<ore:ingotBronze>, <minecraft:string>, <ore:ingotBronze>]]);

print("Age0_Scavenging.zs loaded!");
print("Age0_Energy.zs loaded!");

// === REMOVE VANILLA MACHINE FRAME (stone+sandstone recipe) ===
// On garde seulement la recette avec les metaux
recipes.removeByRecipeName("thermalexpansion:machine_frame");
