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
val thermal_cracker_controller = VanillaFactory.createBlock("thermal_cracker_controller",
    <blockmaterial:iron>);
thermal_cracker_controller.setBlockHardness(7.0);
thermal_cracker_controller.setBlockResistance(20.0);
thermal_cracker_controller.toolClass = "pickaxe";
thermal_cracker_controller.toolLevel = 3;
thermal_cracker_controller.lightValue = 12;  // chaud
thermal_cracker_controller.register();


// 2. MB-CUMENE (3x4x3 oxydation + clivage)
val cumene_reactor_controller = VanillaFactory.createBlock("cumene_reactor_controller",
    <blockmaterial:iron>);
cumene_reactor_controller.setBlockHardness(6.0);
cumene_reactor_controller.setBlockResistance(18.0);
cumene_reactor_controller.toolClass = "pickaxe";
cumene_reactor_controller.toolLevel = 3;
cumene_reactor_controller.lightValue = 8;
cumene_reactor_controller.register();


// 3. MB-AROMATIC (3x3x3 indole + tryptamide ⭐)
val aromatic_reactor_controller = VanillaFactory.createBlock("aromatic_reactor_controller",
    <blockmaterial:iron>);
aromatic_reactor_controller.setBlockHardness(6.0);
aromatic_reactor_controller.setBlockResistance(20.0);
aromatic_reactor_controller.toolClass = "pickaxe";
aromatic_reactor_controller.toolLevel = 3;
aromatic_reactor_controller.lightValue = 10;  // brille (Tryptamide actif)
aromatic_reactor_controller.register();


// 4. MB-FERMENTER (3x3x3 ethanol)
val fermenter_controller = VanillaFactory.createBlock("fermenter_controller",
    <blockmaterial:iron>);
fermenter_controller.setBlockHardness(4.0);
fermenter_controller.setBlockResistance(10.0);
fermenter_controller.toolClass = "pickaxe";
fermenter_controller.toolLevel = 2;
fermenter_controller.lightValue = 3;
fermenter_controller.register();


print("[Nexus Absolu] Age4_L7_Multiblocs.zs loaded -- 4 controllers L7");
