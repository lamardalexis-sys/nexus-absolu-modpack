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
// v1.0.275 : nouveau design Alexis. Simple, accessible Age 0 :
//   ABA  A = iron ingot (obtenu via sieve/tinkers en CM)
//   BCB  B = cobblestone (vanilla)
//   ARA  C = furnace vanilla (coeur)
//        R = redstone dust (power base)
// Layout : top = metal (fonction chaleur), centre = furnace vanilla (coeur),
//          bas = iron + redstone (base power).
recipes.addShaped("nexus_furnace_iron", <nexusabsolu:furnace_iron>,
    [[<ore:ingotIron>,    <minecraft:cobblestone>, <ore:ingotIron>],
     [<minecraft:cobblestone>, <minecraft:furnace>, <minecraft:cobblestone>],
     [<ore:ingotIron>,    <ore:dustRedstone>,      <ore:ingotIron>]]);

// === T2 : GOLD FURNACE (Age 1) ===
// v1.0.275 : nouveau design Alexis. Progression inter-mod (IE + TF + Nexus) :
//   GKG  G = gold ingot (vanilla)
//   EIE  K = kiln brick (Immersive Engineering, stone_decoration:10)
//   GRG  E = Gold Gear (Thermal Foundation, material:25)
//        I = iron furnace (tier precedent, nexusabsolu)
//        R = redstone dust (assumed, convention du pack)
// Layout : top = gold frame + kiln brick (fonction chaleur amelioree),
//          centre = gears TF autour du iron_furnace (coeur + mecanisme),
//          bas = gold + redstone (base power).
// Force 3 mods : Immersive Engineering + Thermal Foundation + Nexus Absolu.
recipes.addShaped("nexus_furnace_gold", <nexusabsolu:furnace_gold>,
    [[<ore:ingotGold>,                    <immersiveengineering:stone_decoration:10>, <ore:ingotGold>],
     [<thermalfoundation:material:25>,    <nexusabsolu:furnace_iron>,                 <thermalfoundation:material:25>],
     [<ore:ingotGold>,                    <ore:dustRedstone>,                         <ore:ingotGold>]]);

// === T3 : INVARIUM FURNACE (Age 1, progression Gold -> Invarium) ===
// v1.0.268 : renomme de 'invar' a 'invarium'. Progression logique :
// Iron -> Gold -> Invarium -> Emeradic -> Vossium IV.
// Utilise le Gold Furnace au centre (au lieu de Iron) + Invarium ingots.
recipes.addShaped("nexus_furnace_invarium", <nexusabsolu:furnace_invarium>,
    [[<nexusabsolu:invarium_ingot>,  <nexusabsolu:invarium_ingot>,  <nexusabsolu:invarium_ingot>],
     [<ore:ingotSteel>,              <nexusabsolu:furnace_gold>,    <ore:ingotSteel>],
     [<nexusabsolu:invarium_ingot>,  <minecraft:coal_block>,        <nexusabsolu:invarium_ingot>]]);

// === T4 : EMERADIC CRYSTAL FURNACE (Age 1 tard) ===
// FIX : emeradic_crystal vient d'Actually Additions, pas d'Environmental Tech.
// actuallyadditions:item_crystal:4 = Emeradic Crystal (vert, obtenu via Empowerer).
// FIX : enderio:item_material:55 n'existait pas. Industrial Machine Chassis = :1
recipes.addShaped("nexus_furnace_emeradic", <nexusabsolu:furnace_emeradic>,
    [[<actuallyadditions:item_crystal:4>, <actuallyadditions:item_crystal:4>, <actuallyadditions:item_crystal:4>],
     [<enderio:item_material:1>,          <nexusabsolu:furnace_invarium>,     <enderio:item_material:1>],
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

// === T6 : DARK ASTRAL FURNACE (Age 3) ===
// Progression : Vossium IV + constellations Astral Sorcery.
// itemcraftingcomponent:1 = Starmetal Ingot (confirme dump)
// itemcraftingcomponent:2 = Stardust (confirme dump)
// itemcelestialcrystal:0 = Celestial Crystal
recipes.addShaped("nexus_furnace_dark_astral", <nexusabsolu:furnace_dark_astral>,
    [[<astralsorcery:itemcelestialcrystal>,     <astralsorcery:itemcraftingcomponent:2>,  <astralsorcery:itemcelestialcrystal>],
     [<astralsorcery:itemcraftingcomponent:1>,  <nexusabsolu:furnace_vossium_iv>,         <astralsorcery:itemcraftingcomponent:1>],
     [<astralsorcery:itemcelestialcrystal>,     <draconicevolution:draconium_block>,      <astralsorcery:itemcelestialcrystal>]]);

// === T7 : GAIA LUDICRITE FURNACE (Age 4, nativeRF=true) ===
// IDs Botania verifies (dump items.csv) :
//   manaresource:0 = Manasteel, :4 = Terrasteel, :5 = Gaia Spirit
//   manaresource:7 = Elementium, :8 = Pixie Dust
// v1.0.273 : fix IDs (avant :5=Gaia Spirit etait correct mais :22 et :14 etaient inventes)
recipes.addShaped("nexus_furnace_gaia_ludicrite", <nexusabsolu:furnace_gaia_ludicrite>,
    [[<botania:manaresource:4>,                 <botania:manaresource:5>,                 <botania:manaresource:4>],
     [<botania:manaresource:8>,                 <nexusabsolu:furnace_dark_astral>,        <botania:manaresource:8>],
     [<botania:manaresource:7>,                 <draconicevolution:draconic_core>,        <botania:manaresource:7>]]);

// === T8 : PALLANUTRO FURNACE (Age 5, nativeRF=true) ===
// IDs Draconic Evolution verifies (dump items.csv) :
//   chaos_shard = Chaos Shard, draconic_core / wyvern_core / awakened_core
recipes.addShaped("nexus_furnace_pallanutro", <nexusabsolu:furnace_pallanutro>,
    [[<draconicevolution:chaos_shard>,          <draconicevolution:draconic_core>,        <draconicevolution:chaos_shard>],
     [<draconicevolution:wyvern_core>,          <nexusabsolu:furnace_gaia_ludicrite>,     <draconicevolution:wyvern_core>],
     [<draconicevolution:chaos_shard>,          <draconicevolution:awakened_core>,        <draconicevolution:chaos_shard>]]);

// ==================================================================
// UPGRADE RECIPES
// ==================================================================

// === RF CONVERTER (prerequis pour les autres) ===
// thermalfoundation:material:512 = Redstone Servo (confirme dans dump)
recipes.addShaped("nexus_upgrade_rf_converter", <nexusabsolu:upgrade_rf_converter>,
    [[<ore:dustRedstone>,                   <ore:ingotGold>,            <ore:dustRedstone>],
     [<thermalfoundation:material:512>,     <minecraft:redstone_block>, <thermalfoundation:material:512>],
     [<ore:dustRedstone>,                   <ore:ingotGold>,            <ore:dustRedstone>]]);

// === IO EXPANSION (v1.0.247 : 4 tiers sequentiels) ===
// Chaque tier superieur requiert le precedent au centre.
// Plus on monte, plus les materiaux sont couteux (compose A -> E, vossium I -> IV).

// Tier I : 3 slots in + 3 slots out
//   Materiaux Age 0/1 : iron, hopper, RF converter
recipes.addShaped("nexus_upgrade_io_expansion_1", <nexusabsolu:upgrade_io_expansion_1>,
    [[<ore:ingotIron>,     <minecraft:hopper>,                    <ore:ingotIron>],
     [<minecraft:hopper>,  <nexusabsolu:upgrade_rf_converter>,    <minecraft:hopper>],
     [<ore:ingotIron>,     <minecraft:hopper>,                    <ore:ingotIron>]]);

// Tier II : 5 slots in + 5 slots out
//   Requiert tier I au centre + compose A + hoppers
recipes.addShaped("nexus_upgrade_io_expansion_2", <nexusabsolu:upgrade_io_expansion_2>,
    [[<nexusabsolu:compose_a>,   <minecraft:hopper>,                       <nexusabsolu:compose_a>],
     [<minecraft:hopper>,        <nexusabsolu:upgrade_io_expansion_1>,     <minecraft:hopper>],
     [<nexusabsolu:compose_a>,   <minecraft:hopper>,                       <nexusabsolu:compose_a>]]);

// Tier III : 7 slots in + 7 slots out
//   Requiert tier II au centre + compose B + vossium II ingots
recipes.addShaped("nexus_upgrade_io_expansion_3", <nexusabsolu:upgrade_io_expansion_3>,
    [[<nexusabsolu:compose_b>,        <nexusabsolu:vossium_ii_ingot>,           <nexusabsolu:compose_b>],
     [<nexusabsolu:vossium_ii_ingot>, <nexusabsolu:upgrade_io_expansion_2>,     <nexusabsolu:vossium_ii_ingot>],
     [<nexusabsolu:compose_b>,        <nexusabsolu:vossium_ii_ingot>,           <nexusabsolu:compose_b>]]);

// Tier IV : 9 slots in + 9 slots out
//   Requiert tier III au centre + compose C + vossium IV ingots (endgame)
recipes.addShaped("nexus_upgrade_io_expansion_4", <nexusabsolu:upgrade_io_expansion_4>,
    [[<nexusabsolu:compose_c>,        <nexusabsolu:vossium_iv_ingot>,           <nexusabsolu:compose_c>],
     [<nexusabsolu:vossium_iv_ingot>, <nexusabsolu:upgrade_io_expansion_3>,     <nexusabsolu:vossium_iv_ingot>],
     [<nexusabsolu:compose_c>,        <nexusabsolu:vossium_iv_ingot>,           <nexusabsolu:compose_c>]]);

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

// === UPGRADE KIT (v1.0.212) ===
// Tech-tier recipe : Invarium + Compose A + EnderIO Basic Component
// Shift+clic droit sur un Furnace Nexus pour le passer en mode Enhanced :
// debloque jauge RF + 4 slots upgrade.
recipes.addShaped("nexus_furnace_upgrade_kit", <nexusabsolu:furnace_upgrade_kit>,
    [[<nexusabsolu:invarium_ingot>, <nexusabsolu:compose_a>,       <nexusabsolu:invarium_ingot>],
     [<nexusabsolu:compose_a>,      <enderio:item_material:0>,     <nexusabsolu:compose_a>],
     [<nexusabsolu:invarium_ingot>, <nexusabsolu:compose_a>,       <nexusabsolu:invarium_ingot>]]);

print("[Nexus Absolu] Age_Furnaces.zs charge : 8 furnaces + 3 upgrades + 4 IO tiers + 1 kit");
