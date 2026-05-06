// ============================================================================
// Nexus Absolu - Age4_L3_Multiblocs.zs
// ============================================================================
// Block controllers L3 Electrolyse-Cryo (cryo deja existant).
// Reference design : docs/age4-cartouche-manifold/lines/L3-electrolyse-cryo.md
//
// 3 nouveaux multiblocs (cryo_distillateur deja en place) :
//   1. ck_cell (5x3x3)         - L3.B Castner-Kellner brine -> Na/Cl/NaOH/H2
//   2. electric_furnace (5x4x5) - L3.D Four electrique HT pour Phosphore
//   3. fluorite_cell (3x3x3)    - L3.C.fluorite electrolyse F2
// ============================================================================

#priority 8000
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;


// 1. MB-CK Cellule Castner-Kellner
val ck_cell_controller = VanillaFactory.createBlock("ck_cell_controller",
    <blockmaterial:iron>);
ck_cell_controller.setBlockHardness(7.0);
ck_cell_controller.setBlockResistance(20.0);
ck_cell_controller.toolClass = "pickaxe";
ck_cell_controller.toolLevel = 3;
ck_cell_controller.lightValue = 6;
ck_cell_controller.register();


// 2. MB-FOUR-ELEC Four Electrique HT (1500 deg C)
val electric_furnace_controller = VanillaFactory.createBlock("electric_furnace_controller",
    <blockmaterial:iron>);
electric_furnace_controller.setBlockHardness(8.0);
electric_furnace_controller.setBlockResistance(25.0);
electric_furnace_controller.toolClass = "pickaxe";
electric_furnace_controller.toolLevel = 3;
electric_furnace_controller.lightValue = 14;  // tres chaud, brille
electric_furnace_controller.register();


// 3. MB-FLUORITE Cellule electrolyse Fluorite
val fluorite_cell_controller = VanillaFactory.createBlock("fluorite_cell_controller",
    <blockmaterial:iron>);
fluorite_cell_controller.setBlockHardness(6.0);
fluorite_cell_controller.setBlockResistance(18.0);
fluorite_cell_controller.toolClass = "pickaxe";
fluorite_cell_controller.toolLevel = 3;
fluorite_cell_controller.lightValue = 5;
fluorite_cell_controller.register();


print("[Nexus Absolu] Age4_L3_Multiblocs.zs loaded -- 3 controllers L3");
