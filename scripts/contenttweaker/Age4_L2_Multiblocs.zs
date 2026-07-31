// ============================================================================
// Nexus Absolu - Age4_L2_Multiblocs.zs
// ============================================================================
// Block controllers L2 Hydro-Eau.
// Reference design : docs/age4-cartouche-manifold/lines/L2-hydro-eau.md
//
// 2 multiblocs L2 :
//   1. osmose_inverse (3x3x3) - L2.3 osmose inverse
//   2. tritium_breeder (3x3x3) - L2.5 reproduction tritium (besoin reactor NC adjacent)
//
// Note : MB-FILTER (filtration ionique) substitue par evaporator existant
// (recette ion_filtration utilise evaporator).
// ============================================================================

#priority 8000
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;


// ============================================================================
// 1. MB-OSMOSE (Osmose Inverse) - L2.3
// ============================================================================
// 1000mB bidistilled_water -> 1000mB tridistilled_water
// 10s, 3000 RF/t (pression haute)

// ============================================================================
// 2. MB-TRITIUM (Tritium Breeder) - L2.5
// ============================================================================
// 1x lithium6_ingot + 100mB heavywater -> 10mB tritium + 1x helium4_capsule
// 10s, 5000 RF/t (shielding)

print("[Nexus Absolu] Age4_L2_Multiblocs.zs loaded -- 2 controllers L2");
