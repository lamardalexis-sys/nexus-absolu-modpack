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
// v1.0.283 : Elementium -> Ludicrite Ingot (coherence nominale avec nom du four)
// v1.0.284 (Alexis) : 2 modifications supplementaires :
//   - TOP-CENTER : Gaia Spirit (manaresource:5) -> Supremium Ingot (mysticalagriculture:crafting:37)
//   - BOTTOM-CENTER : Draconic Core -> Gaia Spirit (descend a la base)
//   Supremium = tier 5 Mystical Agriculture (au-dessus du Superium T4 du Dark Astral).
//   Progression MA coherente entre T6 et T7 : Superium -> Supremium.
//   Force 3 mods : Botania + Big Reactors + Mystical Agriculture + Nexus (plus Draconic).
recipes.addShaped("nexus_furnace_gaia_ludicrite", <nexusabsolu:furnace_gaia_ludicrite>,
    [[<botania:manaresource:4>,                 <mysticalagriculture:crafting:37>,        <botania:manaresource:4>],
     [<botania:manaresource:8>,                 <nexusabsolu:furnace_dark_astral>,        <botania:manaresource:8>],
     [<bigreactors:ingotludicrite>,             <botania:manaresource:5>,                 <bigreactors:ingotludicrite>]]);

// === T8 : PALLANUTRO FURNACE (Age 5, nativeRF=true) ===
// v1.0.283 : refonte endgame cross-mod (chaos_shard -> palladium, wyvern_core ->
//            neutronium, draconic_core -> awakened_core).
// v1.0.284 (Alexis) : TOP-CENTER awakened_core -> Insanium Ingot (mysticalagradditions:insanium:2).
//   Resout le doublon Awakened Core (v1.0.283 en avait 2, celle du bas etait deja la
//   avant refonte). Progression Mystical Agriculture finale :
//     T6 Dark Astral   : Superium   (MA T4, crafting:36)
//     T7 Gaia          : Supremium  (MA T5, crafting:37)
//     T8 Pallanutro    : Insanium   (MAgradditions T6, insanium:2) <- le tier ULTIME
//   Force 4 mods endgame : Extra Planets + Avaritia + Mystical Agradditions + Draconic + Nexus.
//   Progression de difficulte : Insanium requiert Tier 6 Crafting Seed + 8 Supremium Essence.
recipes.addShaped("nexus_furnace_pallanutro", <nexusabsolu:furnace_pallanutro>,
    [[<extraplanets:tier5_items:5>,             <mysticalagradditions:insanium:2>,        <extraplanets:tier5_items:5>],
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
// Tier II : 5 slots in + 5 slots out
//   Requiert tier I au centre + compose C + hoppers
//   v1.0.294 (Alexis) : Compose A -> Compose C. Renforce le gate de progression
//   (Compose C est Age 1 mid-tier, pas trivialement accessible).
recipes.addShaped("nexus_upgrade_io_expansion_2", <nexusabsolu:upgrade_io_expansion_2>,
    [[<nexusabsolu:compose_c>,   <minecraft:hopper>,                       <nexusabsolu:compose_c>],
     [<minecraft:hopper>,        <nexusabsolu:upgrade_io_expansion_1>,     <minecraft:hopper>],
     [<nexusabsolu:compose_c>,   <minecraft:hopper>,                       <nexusabsolu:compose_c>]]);

// Tier III : 7 slots in + 7 slots out
//   v1.0.295 (Alexis) : renforce le gate de progression.
//     Compose B (Age 1 basic)  -> Compose E (Age 2 endgame, le plus haut tier des composes)
//     Vossium II (Age 1 alloy) -> Vossium IV (Age 2 alloy haut tier)
//   Le Tier III devient un vrai gate cross-Age (Age 2 materials requis).
recipes.addShaped("nexus_upgrade_io_expansion_3", <nexusabsolu:upgrade_io_expansion_3>,
    [[<nexusabsolu:compose_e>,        <nexusabsolu:vossium_iv_ingot>,           <nexusabsolu:compose_e>],
     [<nexusabsolu:vossium_iv_ingot>, <nexusabsolu:upgrade_io_expansion_2>,     <nexusabsolu:vossium_iv_ingot>],
     [<nexusabsolu:compose_e>,        <nexusabsolu:vossium_iv_ingot>,           <nexusabsolu:compose_e>]]);

// Tier IV : 9 slots in + 9 slots out
//   v1.0.296 (Alexis) : refonte endgame cross-mod 5 mods forces.
//     A = Tough Alloy   (nuclearcraft:alloy:1)
//     B = Manasteel Ingot (botania:manaresource:0)
//     F = Quantum Storage Unit (quantumstorage:quantum_storage_unit)
//     G = Expansion III (tier precedent, nexusabsolu)
//     H = Tuosss Ingot (nexusabsolu:tuosss_ingot, minerai overworld Age 2+)
//     N = Resonating Gem (astralsorcery:itemcraftingcomponent:4)
//   Layout :
//     A B A   = Tough Alloy | Manasteel     | Tough Alloy
//     F G H   = Quantum     | Expansion III | Tuosss
//     N B N   = Resonating  | Manasteel     | Resonating
//   Force 5 mods : NuclearCraft + Botania + QuantumStorage + Astral Sorcery + Nexus Absolu.
//   Vrai gate endgame Age 3+, requiert maitrise cross-mod complete.
//
//   ATTENTION (Alexis a confirmer) : l'ID quantumstorage:quantum_storage_unit
//   est DEDUIT du unlocalized name 'tile.quantumstorage.quantum_storage_unit'
//   fourni par Alexis. Le mod QuantumStorage n'apparait pas dans
//   minecraftinstance.json ni dans les configs. Si l'ID est faux, JEI affichera
//   la recette avec un slot vide. Simple a corriger (1 str_replace).
recipes.addShaped("nexus_upgrade_io_expansion_4", <nexusabsolu:upgrade_io_expansion_4>,
    [[<nuclearcraft:alloy:1>,                  <botania:manaresource:0>,                <nuclearcraft:alloy:1>],
     [<quantumstorage:quantum_storage_unit>,   <nexusabsolu:upgrade_io_expansion_3>,    <nexusabsolu:tuosss_ingot>],
     [<astralsorcery:itemcraftingcomponent:4>, <botania:manaresource:0>,                <astralsorcery:itemcraftingcomponent:4>]]);

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
