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
// 2. MB-OSTWALD (HNO3)
// 3. MB-CONTACT (H2SO4 + Claus)
print("[Nexus Absolu] Age4_L6_Multiblocs.zs loaded -- 3 controllers L6");
