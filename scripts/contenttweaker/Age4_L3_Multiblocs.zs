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
// 2. MB-FOUR-ELEC Four Electrique HT (1500 deg C)
// 3. MB-FLUORITE Cellule electrolyse Fluorite
print("[Nexus Absolu] Age4_L3_Multiblocs.zs loaded -- 3 controllers L3");
