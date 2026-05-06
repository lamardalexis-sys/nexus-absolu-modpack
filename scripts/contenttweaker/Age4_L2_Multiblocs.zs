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

val osmose_inverse_controller = VanillaFactory.createBlock("osmose_inverse_controller",
    <blockmaterial:iron>);
osmose_inverse_controller.setBlockHardness(5.0);
osmose_inverse_controller.setBlockResistance(15.0);
osmose_inverse_controller.toolClass = "pickaxe";
osmose_inverse_controller.toolLevel = 2;
osmose_inverse_controller.lightValue = 4;
osmose_inverse_controller.register();


// ============================================================================
// 2. MB-TRITIUM (Tritium Breeder) - L2.5
// ============================================================================
// 1x lithium6_ingot + 100mB heavywater -> 10mB tritium + 1x helium4_capsule
// 10s, 5000 RF/t (shielding)

val tritium_breeder_controller = VanillaFactory.createBlock("tritium_breeder_controller",
    <blockmaterial:iron>);
tritium_breeder_controller.setBlockHardness(8.0);
tritium_breeder_controller.setBlockResistance(30.0);
tritium_breeder_controller.toolClass = "pickaxe";
tritium_breeder_controller.toolLevel = 3;
tritium_breeder_controller.lightValue = 8;
tritium_breeder_controller.register();


print("[Nexus Absolu] Age4_L2_Multiblocs.zs loaded -- 2 controllers L2");
