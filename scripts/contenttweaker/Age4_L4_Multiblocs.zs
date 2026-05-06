// ============================================================================
// Nexus Absolu - Age4_L4_Multiblocs.zs
// ============================================================================
// Block controllers L4 Pyrometallurgie.
// Reference design : docs/age4-cartouche-manifold/lines/L4-pyrometallurgie.md
//
// 3 multiblocs L4 :
//   1. hall_heroult_cell (5x4x5)  - L4.B electrolyse Al 950 deg C
//   2. kroll_reactor (4x5x4)       - L4.C reduction Ti sous Argon
//   3. aqua_regia_cell (3x3x3)     - L4.D dissolution metaux precieux
// ============================================================================

#priority 8000
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;


// 1. MB-HALL Hall-Heroult Cell
val hall_heroult_cell_controller = VanillaFactory.createBlock("hall_heroult_cell_controller",
    <blockmaterial:iron>);
hall_heroult_cell_controller.setBlockHardness(8.0);
hall_heroult_cell_controller.setBlockResistance(25.0);
hall_heroult_cell_controller.toolClass = "pickaxe";
hall_heroult_cell_controller.toolLevel = 3;
hall_heroult_cell_controller.lightValue = 14;  // tres chaud
hall_heroult_cell_controller.register();


// 2. MB-KROLL Reacteur Kroll
val kroll_reactor_controller = VanillaFactory.createBlock("kroll_reactor_controller",
    <blockmaterial:iron>);
kroll_reactor_controller.setBlockHardness(7.0);
kroll_reactor_controller.setBlockResistance(22.0);
kroll_reactor_controller.toolClass = "pickaxe";
kroll_reactor_controller.toolLevel = 3;
kroll_reactor_controller.lightValue = 8;
kroll_reactor_controller.register();


// 3. MB-AQUA-REGIA Cellule Eau Regale
val aqua_regia_cell_controller = VanillaFactory.createBlock("aqua_regia_cell_controller",
    <blockmaterial:iron>);
aqua_regia_cell_controller.setBlockHardness(6.0);
aqua_regia_cell_controller.setBlockResistance(18.0);
aqua_regia_cell_controller.toolClass = "pickaxe";
aqua_regia_cell_controller.toolLevel = 3;
aqua_regia_cell_controller.lightValue = 5;
aqua_regia_cell_controller.register();


print("[Nexus Absolu] Age4_L4_Multiblocs.zs loaded -- 3 controllers L4");
