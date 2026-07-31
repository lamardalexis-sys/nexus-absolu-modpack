// ============================================================================
// Nexus Absolu - Age4_L7_Multiblocs.zs
// ============================================================================
// Block controllers L7 Organique-Acetone (DERNIERE LIGNE).
// Reference design : docs/age4-cartouche-manifold/lines/L7-organique-acetone.md
//
// 4 multiblocs L7 :
//   1. thermal_cracker (3x5x3)  - L7.A.2 cracking gaz naturel + L7.E methanol/IPA
//   2. cumene_reactor (3x4x3)   - L7.A.3+4 synthese + clivage cumene
//   3. aromatic_reactor (3x3x3) - L7.C+D indole + tryptamide-M ⭐
//   4. fermenter (3x3x3)        - L7.E ethanol fermentation
// ============================================================================

#priority 8000
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;


// 1. MB-CRACKER (3x5x3 thermal cracking)
// 2. MB-CUMENE (3x4x3 oxydation + clivage)
// 3. MB-AROMATIC (3x3x3 indole + tryptamide ⭐)
// 4. MB-FERMENTER (3x3x3 ethanol)
print("[Nexus Absolu] Age4_L7_Multiblocs.zs loaded -- 4 controllers L7");
