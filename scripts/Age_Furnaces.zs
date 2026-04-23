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
// v1.0.276 : nouveau design Alexis. Progression inter-mod (Galacticraft + TE + Nexus) :
//   IMI    I = invarium ingot (nexusabsolu)
//   CGC    M = machine frame (thermalexpansion:frame)
//   IVI    C = compressed steel (galacticraft, ore:compressedSteel)
//          G = gold furnace (tier precedent, nexusabsolu)
//          V = vossium ingot (nexusabsolu, = Invarium + Compose A via Alloy Smelter)
// Layout : top = invarium + machine frame (fonction chaleur amelioree),
//          centre = compressed steel encadrant le gold_furnace (coeur),
//          bas = invarium + vossium (anchor + gating composite).
// Force 3 mods : Galacticraft + Thermal Expansion + Nexus Absolu.
// Gating : le Vossium ingot requiert Compose A => progression tiered fine.
recipes.addShaped("nexus_furnace_invarium", <nexusabsolu:furnace_invarium>,
    [[<nexusabsolu:invarium_ingot>, <thermalexpansion:frame>,     <nexusabsolu:invarium_ingot>],
     [<ore:compressedSteel>,         <nexusabsolu:furnace_gold>,   <ore:compressedSteel>],
     [<nexusabsolu:invarium_ingot>, <nexusabsolu:vossium_ingot>,  <nexusabsolu:invarium_ingot>]]);

// === T4 : EMERADIC CRYSTAL FURNACE (Age 1 tard) ===
// FIX : emeradic_crystal vient d'Actually Additions, pas d'Environmental Tech.
// actuallyadditions:item_crystal:4 = Emeradic Crystal (vert, obtenu via Empowerer).
// FIX : enderio:item_material:55 n'existait pas. Industrial Machine Chassis = :1
recipes.addShaped("nexus_furnace_emeradic", <nexusabsolu:furnace_emeradic>,
    [[<actuallyadditions:item_crystal:4>, <actuallyadditions:item_crystal:4>, <actuallyadditions:item_crystal:4>],
     [<enderio:item_material:1>,          <nexusabsolu:furnace_invarium>,     <enderio:item_material:1>],
     [<actuallyadditions:item_crystal:4>, <minecraft:redstone_block>,         <actuallyadditions:item_crystal:4>]]);

// === T5 : VOSSIUM IV FURNACE (Age 2) ===
// v1.0.278 : nouveau design Alexis — thematisation Age 2 renforcee :
//   Intermedium Essence (Mystical Agriculture, crafting:2) REMPLACE Celestial Crystal
//   Tuosss Ingot (nexusabsolu, Age 2 overworld mining) REMPLACE Draconium Ingot
// Raison : Astral Sorcery + Draconic etaient hors-theme pour Age 2 intermediate.
// Mystical Agriculture + minerai Tuosss sont les vrais items Age 2 :
//   - Intermedium = tier 3 MA (force farming setup)
//   - Tuosss = minerai overworld custom Age 2+ (gate avant Digital Miner Mekanism)
//
// Layout :
//   vossium_iv | intermedium_essence    | vossium_iv     TOP: magic Age 2 (MA farming)
//   tuosss     | EMERADIC_FURNACE       | tuosss         CENTRE: tier precedent + metal custom Age 2
//   vossium_iv | apprentice_blood_orb   | vossium_iv     BASE: blood magic tier 2
//
// Force 3 mods : Mystical Agriculture + Blood Magic + Nexus Absolu.
recipes.addShaped("nexus_furnace_vossium_iv", <nexusabsolu:furnace_vossium_iv>,
    [[<nexusabsolu:vossium_iv_ingot>,  <mysticalagriculture:crafting:2>,                                <nexusabsolu:vossium_iv_ingot>],
     [<nexusabsolu:tuosss_ingot>,      <nexusabsolu:furnace_emeradic>,                                  <nexusabsolu:tuosss_ingot>],
     [<nexusabsolu:vossium_iv_ingot>,  <bloodmagic:blood_orb>.withTag({orb: "bloodmagic:apprentice"}),  <nexusabsolu:vossium_iv_ingot>]]);

// === T6 : DARK ASTRAL FURNACE (Age 3) ===
// Progression : Vossium IV + constellations Astral Sorcery + gate cross-mod.
// v1.0.283 (Alexis) : remplacements pour mix inter-mod plus riche :
//   Starmetal Ingot (astralsorcery:itemcraftingcomponent:1) -> Dark Soularium Ingot
//                                                              (simplyjetpacks:metaitemmods:12)
//   Stardust        (astralsorcery:itemcraftingcomponent:2) -> Superium Ingot
//                                                              (mysticalagriculture:crafting:36)
// Gate : force 4 mods sur la recette (Astral Sorcery conserve le centre celeste,
//        Simply Jetpacks apporte son materiau endgame, Mystical Agriculture son
//        ingot tier 4, Draconic le socle bas, Nexus au centre).
recipes.addShaped("nexus_furnace_dark_astral", <nexusabsolu:furnace_dark_astral>,
    [[<astralsorcery:itemcelestialcrystal>,     <mysticalagriculture:crafting:36>,        <astralsorcery:itemcelestialcrystal>],
     [<simplyjetpacks:metaitemmods:12>,         <nexusabsolu:furnace_vossium_iv>,         <simplyjetpacks:metaitemmods:12>],
     [<astralsorcery:itemcelestialcrystal>,     <draconicevolution:draconium_block>,      <astralsorcery:itemcelestialcrystal>]]);

// === T7 : GAIA LUDICRITE FURNACE (Age 4, nativeRF=true) ===
// IDs Botania verifies (dump items.csv) :
//   manaresource:0 = Manasteel, :4 = Terrasteel, :5 = Gaia Spirit
//   manaresource:7 = Elementium, :8 = Pixie Dust
// v1.0.273 : fix IDs Botania (avant :22 et :14 etaient inventes)
// v1.0.283 (Alexis) : Elementium (manaresource:7) -> Ludicrite Ingot (bigreactors:ingotludicrite)
//   Coherence thematique : c'est un 'Gaia LUDICRITE Furnace', donc inclure le vrai
//   Ludicrite Ingot de Big Reactors a la base a du sens nominal.
//   Force 3 mods : Botania (terrasteel/gaia_spirit/pixie_dust) + Big Reactors (ludicrite)
//   + Draconic (core) + Nexus Absolu (centre).
recipes.addShaped("nexus_furnace_gaia_ludicrite", <nexusabsolu:furnace_gaia_ludicrite>,
    [[<botania:manaresource:4>,                 <botania:manaresource:5>,                 <botania:manaresource:4>],
     [<botania:manaresource:8>,                 <nexusabsolu:furnace_dark_astral>,        <botania:manaresource:8>],
     [<bigreactors:ingotludicrite>,             <draconicevolution:draconic_core>,        <bigreactors:ingotludicrite>]]);

// === T8 : PALLANUTRO FURNACE (Age 5, nativeRF=true) ===
// v1.0.283 (Alexis) : refonte complete de la recette pour endgame multi-mod :
//   Chaos Shard (draconicevolution:chaos_shard)   -> Palladium Ingot (extraplanets:tier5_items:5)
//   Wyvern Core (draconicevolution:wyvern_core)   -> Neutronium Ingot (avaritia:resource:4)
//   Draconic Core (draconicevolution:draconic_core) -> Awakened Core (draconicevolution:awakened_core)
// Awakened Core conserve en bas-centre aussi. Donc 2 awakened cores total dans
// la recette (haut + bas). Alexis confirmed implicite (pas mentionne de changer
// celle du bas), je l'execute tel quel.
// Force 3 mods endgame : Extra Planets (palladium tier 5 planetes Jupiter+) +
// Avaritia (neutronium, ingot ultime cosmique) + Draconic (2x awakened core) +
// Nexus Absolu (centre).
//
// Gate : ce craft demande maintenant acces Extra Planets tier 5 (voyage Jupiter)
// ET Avaritia neutronium compressor (8 min cobble -> neutronium). C'est un vrai
// endgame multi-mod.
recipes.addShaped("nexus_furnace_pallanutro", <nexusabsolu:furnace_pallanutro>,
    [[<extraplanets:tier5_items:5>,             <draconicevolution:awakened_core>,        <extraplanets:tier5_items:5>],
     [<avaritia:resource:4>,                    <nexusabsolu:furnace_gaia_ludicrite>,     <avaritia:resource:4>],
     [<extraplanets:tier5_items:5>,             <draconicevolution:awakened_core>,        <extraplanets:tier5_items:5>]]);

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
