# -*- coding: utf-8 -*-
"""(famille, couleur de base, couleur d'accent) pour les 64 items.
Couleurs choisies d'apres l'aspect reel du compose."""

P = {
# --- POUDRES -------------------------------------------------------------
"sulfur_pure":            ("dust",(228,204, 64),(255,240,140)),  # soufre jaune vif
"phosphorus_white":       ("dust",(240,236,214),(255,255,255)),  # phosphore blanc cireux
"boron_pure":             ("dust",( 74, 66, 62),(140,128,120)),  # bore brun-noir
"beryllium_pure":         ("dust",(178,186,178),(230,238,230)),  # gris metal mat
"uranyl_dust":            ("dust",(214,206, 74),(250,246,150)),  # yellowcake
"fluorite_dust":          ("dust",(150,110,200),(200,170,240)),  # fluorine violette
"indole_dust":            ("dust",(224,208,176),(250,240,215)),  # organique creme
"iron_chloride_dust":     ("dust",(168, 88, 44),(220,140, 84)),  # rouille
"iron_dust_pure":         ("dust",(126,132,140),(186,192,200)),
"magnesium_chloride":     ("dust",(232,232,236),(255,255,255)),
"magnesium_fluoride":     ("dust",(224,228,234),(255,255,255)),
"calcium_phosphate":      ("dust",(230,226,208),(255,252,238)),  # os
"alumina":                ("dust",(238,238,240),(255,255,255)),
"aluminum_pure":          ("dust",(196,202,210),(240,246,252)),
"titanium_pure":          ("dust",(158,160,166),(214,218,224)),
"slag_silicate":          ("dust",( 88, 84, 80),(140,134,128)),  # scorie
"b2o3_dust":              ("dust",(228,234,238),(255,255,255)),  # vitreux
"bef2_dust":              ("dust",(220,228,224),(252,255,252)),
"delta1_glucose_phosphate":("dust",(226,196,132),(252,232,180)), # ambre pale
"tensioactif_phosphate":  ("dust",(176,206,222),(224,244,255)),
# --- LINGOTS -------------------------------------------------------------
"gold_pure_99":           ("ingot",(226,182, 52),(255,232,140)),
"iridium_pure_99":        ("ingot",(224,228,234),(255,255,255)),
"platinum_pure_99":       ("ingot",(212,216,222),(250,252,255)),
"osmium_pure_99":         ("ingot",(140,156,184),(200,214,238)),  # reflet bleute
"lithium6_ingot":         ("ingot",(198,190,196),(246,232,240)),
"magnesium_ingot":        ("ingot",(196,198,196),(244,246,244)),
"sodium_ingot":           ("ingot",(184,184,178),(232,232,226)),
"plutonium239_ingot":     ("ingot",( 96,106, 92),(150,220,140)),  # gris + lueur verte
"thorium_ingot":          ("ingot",( 84, 88, 90),(140,146,150)),
"titanium_sponge":        ("ingot",(150,150,152),(206,206,208)),
# --- CAPSULES (coque metal + contenu colore) -----------------------------
"gamma1_uf6_capsule":     ("capsule",( 96,196,110),(180,255,190)),
"gamma2_pube_borate_capsule":("capsule",(224,124, 52),(255,190,120)),
"gamma3_lit_capsule":     ("capsule",( 88,208,224),(180,250,255)),
"fluorine_capsule":       ("capsule",(206,224,120),(240,255,180)),
"helium4_capsule":        ("capsule",(196,220,244),(246,252,255)),
"p2o5_capsule":           ("capsule",(238,236,228),(255,255,255)),
"mana_bound_capsule":     ("capsule",( 82,168,232),(170,226,255)),
"tryptamide_m_capsule":   ("capsule",(158, 92,204),(214,166,248)),
"air_canister":           ("capsule",(170,206,226),(226,244,255)),
# --- CATALYSEURS ---------------------------------------------------------
"pd_c_catalyst":          ("catalyst",( 46, 46, 50),(196,202,210)),  # charbon + Pd
"pt_re_catalyst":         ("catalyst",(190,196,204),(240,244,250)),
"pt_al2o3_catalyst":      ("catalyst",(214,218,222),(252,254,255)),
"cu_zn_catalyst":         ("catalyst",(184,110, 62),(238,168,110)),
"cu_zno_catalyst":        ("catalyst",(176,116, 78),(232,172,124)),
"fe_k2o_catalyst":        ("catalyst",(140, 84, 56),(206,146,104)),
"v2o5_catalyst":          ("catalyst",(226,146, 48),(255,200,120)),  # orange vanadium
"acid_catalyst":          ("catalyst",(118,178, 96),(180,232,150)),
"zeolite_pellet":         ("catalyst",(226,218,196),(252,248,230)),
"pellets_como":           ("catalyst",( 76, 92,118),(140,164,196)),  # cobalt-molybdene
"platinum_gauze":         ("gauze",  (206,212,220),(250,252,255)),
# --- CRISTAUX ------------------------------------------------------------
"cristal_chromatic_raw":  ("crystal",(150,120,200),(255,220,255)),
"epsilon_manifoldine_active":("crystal",(176, 79,216),(236,180,255)),
# --- BLOCS ---------------------------------------------------------------
"cryolithe_block":        ("block",(226,228,232),(255,255,255)),
"graphite_block":         ("block",( 58, 58, 62),(120,120,128)),
# --- SPECIFIQUES ---------------------------------------------------------
"beta1_cobalt_phthalocyanine":("dust",( 32, 62,158),( 90,140,242)), # bleu phtalo
"beta2_iridium_hexafluoride":("capsule",(214,212,142),(250,248,196)),
"delta2_neural_silver":   ("dust",(198,206,214),(140,244,252)),      # argent + cyan
"casing_cartouche_empty": ("casing",(150,156,166),(212,220,232)),
"casing_cartouche_sterile":("casing",(198,206,216),(248,252,255)),
"casing_titane_iridium":  ("casing",(176,184,196),(238,244,252)),
"cryotheum_used":         ("dust",(132,158,176),(190,220,238)),      # bleu delave
"mercury_pool":           ("pool",(188,192,198),(246,250,255)),
"resin_charge":           ("catalyst",(206,150, 72),(248,204,132)),  # resine ambree
"spores_active":          ("spores",(158, 96,206),(226,180,252)),
}
