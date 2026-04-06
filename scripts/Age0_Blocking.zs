// ============================================
// NEXUS ABSOLU -- Age0_Blocking.zs
// Bloque les recettes qui ne doivent PAS etre accessibles en Age 0
// Le joueur est dans une Compact Machine 3x3 -> 9x9
// Il n'a PAS de RF, PAS de machines, juste des grits et un four lent
// ============================================

import mods.jei.JEI.removeAndHide as rh;

// ==========================================
// VANILLA RECIPES -- Bloquer les raccourcis
// Le joueur ne doit PAS pouvoir bypass le scavenging
// ==========================================

// Bloquer le crafting bench -> craft depuis cobblestone fragments
// NON -- on le garde, le joueur en a besoin

// Bloquer les outils vanilla en fer+ (forcer Tinkers)
rh(<minecraft:iron_pickaxe>);
rh(<minecraft:iron_axe>);
rh(<minecraft:iron_shovel>);
rh(<minecraft:iron_sword>);
rh(<minecraft:iron_hoe>);
rh(<minecraft:diamond_pickaxe>);
rh(<minecraft:diamond_axe>);
rh(<minecraft:diamond_shovel>);
rh(<minecraft:diamond_sword>);
rh(<minecraft:diamond_hoe>);
rh(<minecraft:golden_pickaxe>);
rh(<minecraft:golden_axe>);
rh(<minecraft:golden_shovel>);
rh(<minecraft:golden_sword>);
rh(<minecraft:golden_hoe>);

// Bloquer les armures vanilla (forcer Construct's Armory ou Tinkers)
rh(<minecraft:iron_helmet>);
rh(<minecraft:iron_chestplate>);
rh(<minecraft:iron_leggings>);
rh(<minecraft:iron_boots>);
rh(<minecraft:diamond_helmet>);
rh(<minecraft:diamond_chestplate>);
rh(<minecraft:diamond_leggings>);
rh(<minecraft:diamond_boots>);
rh(<minecraft:golden_helmet>);
rh(<minecraft:golden_chestplate>);
rh(<minecraft:golden_leggings>);
rh(<minecraft:golden_boots>);

// On garde les outils bois et pierre -- necessaires pour le debut
// On garde le four vanilla -- mais les grits donnent des nuggets (lent)

// ==========================================
// BLOQUER LES MACHINES QUI ARRIVENT TROP TOT
// Ces items existent dans JEI mais le joueur ne doit pas
// pouvoir les crafter avant d'avoir les prerequis
// ==========================================

// Mekanism -- pas avant Age 3 (besoin d'osmium = overworld mining)
// On ne bloque PAS les recettes Mekanism ici, elles sont juste
// impossibles a faire car l'osmium n'est pas disponible en compact

// AE2 -- pas avant Age 3 (besoin de certus quartz = overworld mining)
// Meme logique -- auto-gate par les materiaux

// NuclearCraft -- pas avant Age 5
// Auto-gate par les materiaux

// ==========================================
// BLOQUER L'ENCHANTING TABLE
// Trop facile de s'enchanter en compact avec des mob spawners
// ==========================================

recipes.remove(<minecraft:enchanting_table>);
recipes.addShaped("nexus_enchanting_table", <minecraft:enchanting_table>,
    [[null, <minecraft:book>, null],
     [<minecraft:diamond>, <ore:ingotDarkSteel>, <minecraft:diamond>],
     [<minecraft:obsidian>, <minecraft:obsidian>, <minecraft:obsidian>]]);
// Necessite Dark Steel = EnderIO = Age 1 minimum

// ==========================================
// BLOQUER L'ANVIL -- trop puissant trop tot
// ==========================================

recipes.remove(<minecraft:anvil>);
recipes.addShaped("nexus_anvil", <minecraft:anvil>,
    [[<ore:blockSteel>, <ore:blockSteel>, <ore:blockSteel>],
     [null, <ore:ingotSteel>, null],
     [<ore:ingotSteel>, <ore:ingotSteel>, <ore:ingotSteel>]]);
// Necessite Steel = IE = Age 1 minimum

// ==========================================
// BLOQUER LE NETHER PORTAL (pas de Nether avant Age 3)
// Le Flint & Steel necessite de l'acier IE
// ==========================================

recipes.remove(<minecraft:flint_and_steel>);
recipes.addShaped("nexus_flint_and_steel", <minecraft:flint_and_steel>,
    [[null, <ore:ingotSteel>],
     [<minecraft:flint>, null]]);

// ==========================================
// BLOQUER LE BUCKET -- trop tot casse le early game
// Necessite du fer pur (pas des nuggets)
// ==========================================

// Le bucket vanilla reste craftable normalement avec 3 fer
// Mais le joueur n'a que des nuggets au debut
// Quand il a la smeltery (7x7) il peut faire des lingots -> bucket
// C'est un gate naturel, pas besoin de modifier

// ==========================================
// ENDER EYES -- Gate pour The End (Age 4)
// ==========================================

recipes.remove(<minecraft:ender_eye>);
recipes.addShaped("nexus_ender_eye", <minecraft:ender_eye>,
    [[null, <astralsorcery:itemcraftingcomponent:2>, null],
     [<ore:ingotPulsatingIron>, <minecraft:ender_pearl>, <ore:ingotPulsatingIron>],
     [null, <enderio:item_material:1>, null]]);
// Necessite Pulsating Iron (EnderIO) + Stardust (Astral) = Age 4

// ==========================================
// BLOQUER TOUTE PRODUCTION DE STEEL
// Seule source autorisee Ages 0-1 : EnderIO Alloy Smelter
// (Coal Coke + Iron + Wall Dust) dans Age1_EnderIO.zs
// Une recette plus rapide se debloquera en Age 2
// ==========================================

// --- IE Blast Furnace (Iron + Coal/Charcoal/Coke -> Steel) ---
// C'est le bypass principal : le Blast Brick est craftable en Age 1
// L'output peut etre IE steel OU TF steel selon l'unification oredict
mods.immersiveengineering.BlastFurnace.removeRecipe(<immersiveengineering:metal:8>);
mods.immersiveengineering.BlastFurnace.removeRecipe(<thermalfoundation:material:160>);

// --- IE Arc Furnace (peut aussi produire du steel) ---
mods.immersiveengineering.ArcFurnace.removeRecipe(<immersiveengineering:metal:8>);
mods.immersiveengineering.ArcFurnace.removeRecipe(<thermalfoundation:material:160>);

// --- IE Blast Furnace Fuel : retirer tous les fuels pour tuer la machine ---
mods.immersiveengineering.BlastFurnace.removeFuel(<minecraft:coal:0>);
mods.immersiveengineering.BlastFurnace.removeFuel(<minecraft:coal:1>);
mods.immersiveengineering.BlastFurnace.removeFuel(<immersiveengineering:material:6>);
mods.immersiveengineering.BlastFurnace.removeFuel(<minecraft:coal_block>);
mods.immersiveengineering.BlastFurnace.removeFuel(<immersiveengineering:stone_decoration:3>);

// TODO Age 3 : verifier Mekanism Metallurgic Infuser steel chain
// (Carbon + Iron -> Enriched Iron -> Steel) — gate naturel par osmium

print("[Nexus Absolu] Age0_Blocking.zs loaded");
