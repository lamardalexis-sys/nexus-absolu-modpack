// ============================================
// NEXUS ABSOLU -- Age_Furnaces.zs
// Recipes pour les 5 Furnace Nexus + 4 upgrades.
// IDs verifies le 2026-04-18 apres feedback Alexis : emeradic_crystal
// n'est PAS dans Environmental Tech mais dans Actually Additions.
// Tous les IDs douteux ont ete remplaces par OreDict ou items vanilla.
// ============================================

import crafttweaker.item.IIngredient;
import crafttweaker.item.IItemStack;

// ==================================================================
// FURNACE RECIPES (T1-T5)
// ==================================================================

// === T1 : IRON FURNACE (Age 0) ===
// Items utilises : tous confirmes existants
recipes.addShaped("nexus_furnace_iron", <nexusabsolu:furnace_iron>,
    [[<nexusabsolu:iron_grit>, <minecraft:cobblestone>,      <nexusabsolu:iron_grit>],
     [<minecraft:cobblestone>, <minecraft:furnace>,          <minecraft:cobblestone>],
     [<nexusabsolu:iron_grit>, <ore:dustWall>,               <nexusabsolu:iron_grit>]]);

// === T2 : GOLD FURNACE (Age 1) ===
// FIX : thermalfoundation:material:288 etait Steel Gear, pas Machine Frame.
// Le vrai Machine Frame est thermalexpansion:frame (confirme via grep scripts).
recipes.addShaped("nexus_furnace_gold", <nexusabsolu:furnace_gold>,
    [[<ore:ingotGold>,              <ore:ingotGold>,              <ore:ingotGold>],
     [<thermalexpansion:frame>,     <nexusabsolu:furnace_iron>,   <thermalexpansion:frame>],
     [<ore:ingotGold>,              <ore:ingotGold>,              <ore:ingotGold>]]);

// === T3 : INVAR FURNACE (Age 1, parallele au Gold) ===
// FIX : immersiveengineering:metal_decoration0:0 etait Copper Coil Block, pas Heavy Engineering.
// Remplace par un item OreDict + steel ingot pour voie tech fiable.
recipes.addShaped("nexus_furnace_invar", <nexusabsolu:furnace_invar>,
    [[<ore:ingotInvar>,             <ore:ingotInvar>,             <ore:ingotInvar>],
     [<ore:ingotSteel>,             <nexusabsolu:furnace_iron>,   <ore:ingotSteel>],
     [<ore:ingotInvar>,             <minecraft:coal_block>,       <ore:ingotInvar>]]);

// === T4 : EMERADIC CRYSTAL FURNACE (Age 1 tard) ===
// FIX : emeradic_crystal vient d'Actually Additions, pas d'Environmental Tech.
// actuallyadditions:item_crystal:4 = Emeradic Crystal (vert, obtenu via Empowerer).
// FIX : enderio:item_material:55 n'existait pas. Industrial Machine Chassis = :1
recipes.addShaped("nexus_furnace_emeradic", <nexusabsolu:furnace_emeradic>,
    [[<actuallyadditions:item_crystal:4>, <actuallyadditions:item_crystal:4>, <actuallyadditions:item_crystal:4>],
     [<enderio:item_material:1>,          <nexusabsolu:furnace_invar>,        <enderio:item_material:1>],
     [<actuallyadditions:item_crystal:4>, <minecraft:redstone_block>,         <actuallyadditions:item_crystal:4>]]);

// === T5 : VOSSIUM IV FURNACE (Age 2) ===
// astralsorcery:itemcelestialcrystal confirme dans dump (existe bien).
// draconicevolution:draconium_ingot confirme dans dump.
// bloodmagic:slate pas confirme dans le dump visible => remplace par OreDict / item plus commun.
// Pour rester dans l'esprit 'magic mod' Age 2 avec items garantis, on prend :
//   - astralsorcery:itemcelestialcrystal (confirmer une derniere fois via JEI in-game)
//   - draconicevolution:draconium_ingot
//   - bloodmagic:blood_orb:0 (Weak Blood Orb, confirme dans dump)
recipes.addShaped("nexus_furnace_vossium_iv", <nexusabsolu:furnace_vossium_iv>,
    [[<nexusabsolu:vossium_iv_ingot>,       <astralsorcery:itemcelestialcrystal>, <nexusabsolu:vossium_iv_ingot>],
     [<draconicevolution:draconium_ingot>,  <nexusabsolu:furnace_emeradic>,       <draconicevolution:draconium_ingot>],
     [<nexusabsolu:vossium_iv_ingot>,       <bloodmagic:blood_orb:0>,             <nexusabsolu:vossium_iv_ingot>]]);

// ==================================================================
// UPGRADE RECIPES
// ==================================================================

// === RF CONVERTER (prerequis pour les autres) ===
// thermalfoundation:material:512 = Redstone Servo (confirme dans dump)
recipes.addShaped("nexus_upgrade_rf_converter", <nexusabsolu:upgrade_rf_converter>,
    [[<ore:dustRedstone>,                   <ore:ingotGold>,            <ore:dustRedstone>],
     [<thermalfoundation:material:512>,     <minecraft:redstone_block>, <thermalfoundation:material:512>],
     [<ore:dustRedstone>,                   <ore:ingotGold>,            <ore:dustRedstone>]]);

// === IO EXPANSION ===
recipes.addShaped("nexus_upgrade_io_expansion", <nexusabsolu:upgrade_io_expansion>,
    [[<ore:ingotIron>,        <minecraft:hopper>,                    <ore:ingotIron>],
     [<minecraft:hopper>,     <nexusabsolu:upgrade_rf_converter>,    <minecraft:hopper>],
     [<ore:ingotIron>,        <minecraft:hopper>,                    <ore:ingotIron>]]);

// === SPEED BOOSTER ===
recipes.addShaped("nexus_upgrade_speed_booster", <nexusabsolu:upgrade_speed_booster>,
    [[<minecraft:sugar>,          <minecraft:glowstone_dust>,            <minecraft:sugar>],
     [<minecraft:glowstone_dust>, <nexusabsolu:upgrade_rf_converter>,    <minecraft:glowstone_dust>],
     [<minecraft:sugar>,          <minecraft:glowstone_dust>,            <minecraft:sugar>]]);

// === EFFICIENCY CARD ===
recipes.addShaped("nexus_upgrade_efficiency", <nexusabsolu:upgrade_efficiency>,
    [[<ore:dustLapis>,       <minecraft:emerald>,                   <ore:dustLapis>],
     [<minecraft:emerald>,   <nexusabsolu:upgrade_rf_converter>,    <minecraft:emerald>],
     [<ore:dustLapis>,       <minecraft:emerald>,                   <ore:dustLapis>]]);

print("[Nexus Absolu] Age_Furnaces.zs charge : 5 furnaces + 4 upgrades");
