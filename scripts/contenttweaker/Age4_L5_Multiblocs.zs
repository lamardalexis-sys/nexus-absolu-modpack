// ============================================================================
// Nexus Absolu - Age4_L5_Multiblocs.zs
// ============================================================================
// Block controllers L5 Nucleaire.
// Reference design : docs/age4-cartouche-manifold/lines/L5-nucleaire.md
//
// 2 multiblocs L5 :
//   1. gamma_forge (3x3x3) - L5.7 forge composes gamma1 + gamma2
//   2. lit_chamber (3x3x3) - L5.8 chambre Lithium-Tritium gamma3
//
// Note : centrifugation cascade et breeder reactor utilisent NuclearCraft natif.
// Mycelium activation L5.9 implementee via KubeJS (deja en TEMP dans L8).
// ============================================================================

#priority 8000
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;


// 1. MB-GAMMA-FORGE (composes gamma1 + gamma2)
val gamma_forge_controller = VanillaFactory.createBlock("gamma_forge_controller",
    <blockmaterial:iron>);
gamma_forge_controller.setBlockHardness(8.0);
gamma_forge_controller.setBlockResistance(30.0);
gamma_forge_controller.toolClass = "pickaxe";
gamma_forge_controller.toolLevel = 3;
gamma_forge_controller.lightValue = 10;  // brille (radioactif visuel)
gamma_forge_controller.register();


// 2. MB-LIT-CHAMBER (Tritiure de Lithium gamma3)
val lit_chamber_controller = VanillaFactory.createBlock("lit_chamber_controller",
    <blockmaterial:iron>);
lit_chamber_controller.setBlockHardness(7.0);
lit_chamber_controller.setBlockResistance(25.0);
lit_chamber_controller.toolClass = "pickaxe";
lit_chamber_controller.toolLevel = 3;
lit_chamber_controller.lightValue = 8;
lit_chamber_controller.register();


print("[Nexus Absolu] Age4_L5_Multiblocs.zs loaded -- 2 controllers L5");
