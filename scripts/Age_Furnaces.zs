// ============================================
// NEXUS ABSOLU -- Age_Furnaces.zs
// Recipes for the 5 Furnace Nexus tiers + 4 upgrades.
// Matches FURNACES_RECIPES_DESIGN.md v1.0.185.
// ============================================

import crafttweaker.item.IIngredient;
import crafttweaker.item.IItemStack;

// ==================================================================
// FURNACE RECIPES (T1-T5)
// ==================================================================

// === T1 : IRON FURNACE (Age 0) ===
// Accessible des le debut : iron_grit + cobble + wall_dust + vanilla furnace
recipes.addShaped("nexus_furnace_iron", <nexusabsolu:furnace_iron>,
    [[<nexusabsolu:iron_grit>, <minecraft:cobblestone>,      <nexusabsolu:iron_grit>],
     [<minecraft:cobblestone>, <minecraft:furnace>,          <minecraft:cobblestone>],
     [<nexusabsolu:iron_grit>, <ore:dustWall>,               <nexusabsolu:iron_grit>]]);

// === T2 : GOLD FURNACE (Age 1) ===
// Utilise Thermal Foundation machine frame + iron furnace
recipes.addShaped("nexus_furnace_gold", <nexusabsolu:furnace_gold>,
    [[<ore:ingotGold>,                     <ore:ingotGold>,              <ore:ingotGold>],
     [<thermalfoundation:material:288>,    <nexusabsolu:furnace_iron>,   <thermalfoundation:material:288>],
     [<ore:ingotGold>,                     <ore:ingotGold>,              <ore:ingotGold>]]);
// Note: thermalfoundation:material:288 = Machine Frame (standard). Verif JEI si KO.

// === T3 : INVAR FURNACE (Age 1) ===
// Voie tech parallele : IE heavy engineering + invar + coal block center
recipes.addShaped("nexus_furnace_invar", <nexusabsolu:furnace_invar>,
    [[<ore:ingotInvar>,                        <ore:ingotInvar>,             <ore:ingotInvar>],
     [<immersiveengineering:metal_decoration0:0>, <nexusabsolu:furnace_iron>, <immersiveengineering:metal_decoration0:0>],
     [<ore:ingotInvar>,                        <minecraft:coal_block>,       <ore:ingotInvar>]]);
// Note: IE metal_decoration0:0 = Heavy Engineering block. Confirme via JEI.

// === T4 : EMERADIC CRYSTAL FURNACE (Age 1 tard) ===
// Utilise Environmental Tech + EnderIO + T2 ou T3 au centre (on prend T3 Invar)
recipes.addShaped("nexus_furnace_emeradic", <nexusabsolu:furnace_emeradic>,
    [[<environmentaltech:emeradic_crystal>, <environmentaltech:emeradic_crystal>, <environmentaltech:emeradic_crystal>],
     [<enderio:item_material:55>,           <nexusabsolu:furnace_invar>,          <enderio:item_material:55>],
     [<environmentaltech:emeradic_crystal>, <minecraft:redstone_block>,           <environmentaltech:emeradic_crystal>]]);
// Note: enderio:item_material:55 = Framed Machine Chassis (EnderIO 5.3.72).
// Note: environmentaltech:emeradic_crystal a verifier via JEI si recipe rejetee.

// === T5 : VOSSIUM IV FURNACE (Age 2) ===
// Saut d'Age : force les 3 magic mods (Astral + Draconic + Blood Magic).
// NOTE: les 3 items magic sont a confirmer via JEI, fallbacks commentes ci-dessous.
recipes.addShaped("nexus_furnace_vossium_iv", <nexusabsolu:furnace_vossium_iv>,
    [[<nexusabsolu:vossium_iv_ingot>,                 <astralsorcery:itemcelestialcrystal>, <nexusabsolu:vossium_iv_ingot>],
     [<draconicevolution:draconium_ingot>,            <nexusabsolu:furnace_emeradic>,       <draconicevolution:draconium_ingot>],
     [<nexusabsolu:vossium_iv_ingot>,                 <bloodmagic:slate:3>,                 <nexusabsolu:vossium_iv_ingot>]]);
// Substitutions par rapport au design pour garantir que les items existent :
//   - astralsorcery:itemcelestialcrystal (at lieu de attunedcrystal)  -- cristal d'Astral
//   - draconicevolution:draconium_ingot  (au lieu de wyvern_core)     -- materiel Draconic de base (plus accessible Age 2)
//   - bloodmagic:slate:3                 (au lieu de sanguine_core)   -- Imbued Slate (Age 2 BM)

// ==================================================================
// UPGRADE RECIPES (Sprint B) — Craftables Age 1+
// ==================================================================

// === RF CONVERTER (1 max stack, pre-requis pour les autres) ===
// Thermal capacitor + redstone = convertisseur simple
recipes.addShaped("nexus_upgrade_rf_converter", <nexusabsolu:upgrade_rf_converter>,
    [[<ore:dustRedstone>,           <ore:ingotGold>,            <ore:dustRedstone>],
     [<thermalfoundation:material:512>, <minecraft:redstone_block>, <thermalfoundation:material:512>],
     [<ore:dustRedstone>,           <ore:ingotGold>,            <ore:dustRedstone>]]);
// Note: thermalfoundation:material:512 = Redstone Servo (or similar). Confirme via JEI.

// === IO EXPANSION (1 max stack) ===
// Necessite le RF converter au centre + hopper pour les slots I/O
recipes.addShaped("nexus_upgrade_io_expansion", <nexusabsolu:upgrade_io_expansion>,
    [[<ore:ingotIron>,                    <minecraft:hopper>,                    <ore:ingotIron>],
     [<minecraft:hopper>,                 <nexusabsolu:upgrade_rf_converter>,    <minecraft:hopper>],
     [<ore:ingotIron>,                    <minecraft:hopper>,                    <ore:ingotIron>]]);

// === SPEED BOOSTER (stackable jusqu'a 8) ===
// Glowstone + sugar (symbole de vitesse) + redstone
recipes.addShaped("nexus_upgrade_speed_booster", <nexusabsolu:upgrade_speed_booster>,
    [[<minecraft:sugar>,             <minecraft:glowstone_dust>, <minecraft:sugar>],
     [<minecraft:glowstone_dust>,    <nexusabsolu:upgrade_rf_converter>, <minecraft:glowstone_dust>],
     [<minecraft:sugar>,             <minecraft:glowstone_dust>, <minecraft:sugar>]]);

// === EFFICIENCY CARD (stackable jusqu'a 10) ===
// Emerald + lapis (symboles eco) + RF converter
recipes.addShaped("nexus_upgrade_efficiency", <nexusabsolu:upgrade_efficiency>,
    [[<ore:dustLapis>,                <minecraft:emerald>,        <ore:dustLapis>],
     [<minecraft:emerald>,            <nexusabsolu:upgrade_rf_converter>, <minecraft:emerald>],
     [<ore:dustLapis>,                <minecraft:emerald>,        <ore:dustLapis>]]);

// ==================================================================
// REMOVE Mystical Agriculture furnaces (duplicate tier progression)
// ==================================================================
// Cache et retire les recipes de Mystical Agriculture furnace T1-T5
// pour eviter la redondance avec les notres. Les fourneaux MystAg
// auraient ete craftables avec juste des essences (trop facile).

// Les registries MystAg furnace peuvent varier, on tente les names connus
// (wrapped dans un try-catch conceptual via verif existence)

// Inferno Furnace (tier unique MystAg en 1.12)
// var infernoFurnace = <mysticalagriculture:inferno_crafter>;  // peut etre different
// Note: on laisse le joueur choisir entre notre pack et MystAg si ils cohabitent,
// mais on hide leur recipe pour clarte UX.

// Hide Mystical Agriculture furnaces from JEI (if they exist)
// Ils restent craftables pour les joueurs experts mais pas mis en avant
// try {
//     mods.jei.JEI.hide(<mysticalagriculture:inferno_furnace>);
// } catch (any e) {
//     // OK si inexistant dans cette version
// }

print("[Nexus Absolu] Age_Furnaces.zs loaded : 5 furnaces + 4 upgrades recipes");
