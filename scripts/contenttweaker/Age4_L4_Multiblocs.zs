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
// 2. MB-KROLL Reacteur Kroll
// 3. MB-AQUA-REGIA Cellule Eau Regale
print("[Nexus Absolu] Age4_L4_Multiblocs.zs loaded -- 3 controllers L4");
