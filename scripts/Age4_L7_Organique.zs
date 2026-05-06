// ============================================================================
// Nexus Absolu - Age4_L7_Organique.zs
// ============================================================================
// Recettes ZS pour la ligne L7 Organique-Acetone :
//   - 6 catalyseurs (pt_re, cu_zn, pt_al2o3, pd_c, cu_zno, acid)
//   - Syngas (CO + H2 mix)
//
// Reference design : docs/age4-cartouche-manifold/lines/L7-organique-acetone.md
// ============================================================================


// ============================================================================
// L7.A.1 - Pt-Re Catalyst (Reformage Naphtha -> Benzene)
// ============================================================================
recipes.addShaped("nexus_pt_re_catalyst",
    <contenttweaker:pt_re_catalyst> * 1,
    [[<ore:nuggetPlatinum>, <ore:dustRhenium>, <ore:nuggetPlatinum>],
     [<ore:dustRhenium>, <ore:platePlatinum>, <ore:dustRhenium>],
     [<ore:nuggetPlatinum>, <ore:dustRhenium>, <ore:nuggetPlatinum>]]);

// Fallback : si dustRhenium indispo, utiliser dustIridium (similar properties)
recipes.addShaped("nexus_pt_re_catalyst_fallback",
    <contenttweaker:pt_re_catalyst> * 1,
    [[<ore:nuggetPlatinum>, <ore:dustIridium>, <ore:nuggetPlatinum>],
     [<ore:dustIridium>, <contenttweaker:platinum_pure_99>, <ore:dustIridium>],
     [<ore:nuggetPlatinum>, <ore:dustIridium>, <ore:nuggetPlatinum>]]);


// ============================================================================
// L7.B - Cu-Zn Catalyst (IPA dehydrogenation -> Acetone)
// ============================================================================
recipes.addShaped("nexus_cu_zn_catalyst",
    <contenttweaker:cu_zn_catalyst> * 2,
    [[<ore:dustCopper>, <ore:dustZinc>, <ore:dustCopper>],
     [<ore:dustZinc>, <ore:ingotCopper>, <ore:dustZinc>],
     [<ore:dustCopper>, <ore:dustZinc>, <ore:dustCopper>]]);


// ============================================================================
// L7.C - Pt-Al2O3 Catalyst (Indole synthesis)
// ============================================================================
recipes.addShaped("nexus_pt_al2o3_catalyst",
    <contenttweaker:pt_al2o3_catalyst> * 1,
    [[<ore:nuggetPlatinum>, <contenttweaker:alumina>, <ore:nuggetPlatinum>],
     [<contenttweaker:alumina>, <ore:platePlatinum>, <contenttweaker:alumina>],
     [<ore:nuggetPlatinum>, <contenttweaker:alumina>, <ore:nuggetPlatinum>]]);


// ============================================================================
// L7.D - Pd-C Catalyst (Tryptamide-M synthesis ⭐)
// ============================================================================
// Palladium sur carbone (charcoal compresse)
recipes.addShaped("nexus_pd_c_catalyst",
    <contenttweaker:pd_c_catalyst> * 1,
    [[<ore:nuggetPalladium>, <minecraft:coal:1>, <ore:nuggetPalladium>],
     [<minecraft:coal:1>, <ore:dustPalladium>, <minecraft:coal:1>],
     [<ore:nuggetPalladium>, <minecraft:coal:1>, <ore:nuggetPalladium>]]);

// Fallback : si Palladium indispo, utiliser Platinum (chimie similaire group 10)
recipes.addShaped("nexus_pd_c_catalyst_fallback",
    <contenttweaker:pd_c_catalyst> * 1,
    [[<ore:nuggetPlatinum>, <minecraft:coal:1>, <ore:nuggetPlatinum>],
     [<minecraft:coal:1>, <contenttweaker:graphite_block>, <minecraft:coal:1>],
     [<ore:nuggetPlatinum>, <minecraft:coal:1>, <ore:nuggetPlatinum>]]);


// ============================================================================
// L7.E.1 - Cu-ZnO Catalyst (Methanol synthesis from Syngas)
// ============================================================================
recipes.addShaped("nexus_cu_zno_catalyst",
    <contenttweaker:cu_zno_catalyst> * 2,
    [[<ore:dustCopper>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <ore:dustCopper>],
     [<ore:dustZinc>, <ore:ingotCopper>, <ore:dustZinc>],
     [<ore:dustCopper>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <ore:dustCopper>]]);


// ============================================================================
// L7.B - Acid Catalyst (IPA synthesis - solid acid generic)
// ============================================================================
// Catalyseur acide solide (silice + H2SO4 supporte)
recipes.addShaped("nexus_acid_catalyst",
    <contenttweaker:acid_catalyst> * 4,
    [[<ore:dustSilicon>, <minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000}), <ore:dustSilicon>],
     [<minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000}), <ore:dustSilicon>, <minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000})],
     [<ore:dustSilicon>, <minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000}), <ore:dustSilicon>]]);


// ============================================================================
// L7.X - Syngas (CO + H2 mix) - input methanol synthesis
// ============================================================================
// Source : steam reforming methane (gaz naturel) - PCC ou MM custom
// TEMP : recette shaped en attendant vrai multibloc steam reformer
// 1 bucket methane + 1 bucket water -> 2 buckets syngas (proxy)

recipes.addShapeless("nexus_syngas_proxy",
    <minecraft:bucket>.withTag({FluidName: "syngas", Amount: 1000}) * 2,
    [<minecraft:bucket>.withTag({FluidName: "methane", Amount: 1000}),
     <minecraft:bucket>.withTag({FluidName: "tridistilled_water", Amount: 1000})]);


print("[Nexus Absolu] Age4_L7_Organique.zs loaded -- 9 recettes L7");
