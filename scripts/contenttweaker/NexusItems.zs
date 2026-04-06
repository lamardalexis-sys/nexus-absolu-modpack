#priority 9000
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;
import mods.contenttweaker.Item;

// ============================================
// NEXUS ABSOLU — Custom Items (ContentTweaker)
// Uses the Java mod creative tab (nexusabsolu)
// ============================================

// Helper function — uses JAVA creative tab (no separate CT tab)
function buildItem(name as string) {
    val item = mods.contenttweaker.VanillaFactory.createItem(name);
    item.setCreativeTab(<creativetab:nexusabsolu>);
    item.register();
}

// ============================================
// 1. GRITS — SUPPRIMES ICI
//    Maintenant dans le mod Java (ModItems.java)
//    wall_dust, iron_grit, copper_grit, etc.
// ============================================

buildItem("steel_stick");  // Reste en CT — pas encore migre

// ============================================
// 2. CLES D'EXPANSION — Gate la progression
//    SUPPRIMEES ICI — maintenant dans le mod Java (ModItems.java)
// ============================================

// ============================================
// 3. COMPOSANTS INTER-MODS
//    SUPPRIMES ICI — maintenant dans le mod Java (ModItems.java)
// ============================================

// ============================================
// 3b. LINGOTS VOSS — Fil rouge du modpack
//     SUPPRIMES ICI — maintenant dans le mod Java (ModItems.java)
//     invarium_ingot, vossium_ingot, vossium_ii/iii/iv_ingot
// ============================================

// ============================================
// 4. FRAGMENTS DE PROGRESSION
//    SUPPRIMES ICI — maintenant dans le mod Java (ModItems.java)
//    Sauf compose_x77, coeur_de_donnees, noyau_fissile,
//    fragment_espace_temps, codex_transcendant, prisme_transcendance
//    qui n'existent PAS encore dans le mod Java
// ============================================

buildItem("compose_x77");           // Âge 3 — Mekanism + Thermal + Astral
buildItem("coeur_de_donnees");      // Âge 4 — AE2
buildItem("noyau_fissile");         // Âge 5 — NuclearCraft
buildItem("fragment_espace_temps"); // Âge 6 — Galacticraft
buildItem("codex_transcendant");    // Âge 7 — Blood Magic + Draconic
buildItem("prisme_transcendance");  // Âge 8 — Draconic + Avaritia

// ============================================
// 5. LE NEXUS ABSOLU
//    SUPPRIME ICI — maintenant dans le mod Java (ModItems.java)
// ============================================
