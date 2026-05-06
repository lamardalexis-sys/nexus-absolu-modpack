// ============================================================================
// Nexus Absolu - Age4_L6_Multiblocs.zs
// ============================================================================
// Block controllers L6 Acides-Ammoniaque (HUB CENTRAL).
// Reference design : docs/age4-cartouche-manifold/lines/L6-acides-ammoniaque.md
//
// 3 multiblocs L6 :
//   1. haber_reactor (5x5x5)  - L6.1 NH3 pivot ⭐
//   2. ostwald_tower (3x4x3)  - L6.2 HNO3
//   3. contact_tower (3x4x3)  - L6.3 H2SO4 + Claus
//
// Note : evaporator existant pour NaOH concentre, bioreacteur pour aqua_regia/HCl.
// ============================================================================

#priority 8000
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;


// 1. MB-HABER (HUB CENTRAL NH3)
val haber_reactor_controller = VanillaFactory.createBlock("haber_reactor_controller",
    <blockmaterial:iron>);
haber_reactor_controller.setBlockHardness(10.0);
haber_reactor_controller.setBlockResistance(40.0);
haber_reactor_controller.toolClass = "pickaxe";
haber_reactor_controller.toolLevel = 3;
haber_reactor_controller.lightValue = 12;
haber_reactor_controller.register();


// 2. MB-OSTWALD (HNO3)
val ostwald_tower_controller = VanillaFactory.createBlock("ostwald_tower_controller",
    <blockmaterial:iron>);
ostwald_tower_controller.setBlockHardness(7.0);
ostwald_tower_controller.setBlockResistance(22.0);
ostwald_tower_controller.toolClass = "pickaxe";
ostwald_tower_controller.toolLevel = 3;
ostwald_tower_controller.lightValue = 8;
ostwald_tower_controller.register();


// 3. MB-CONTACT (H2SO4 + Claus)
val contact_tower_controller = VanillaFactory.createBlock("contact_tower_controller",
    <blockmaterial:iron>);
contact_tower_controller.setBlockHardness(7.0);
contact_tower_controller.setBlockResistance(22.0);
contact_tower_controller.toolClass = "pickaxe";
contact_tower_controller.toolLevel = 3;
contact_tower_controller.lightValue = 6;
contact_tower_controller.register();


print("[Nexus Absolu] Age4_L6_Multiblocs.zs loaded -- 3 controllers L6");
