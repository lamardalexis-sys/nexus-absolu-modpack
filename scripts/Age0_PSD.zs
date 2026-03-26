// ============================================
// NEXUS ABSOLU -- Age0_PSD.zs
// Reskin du Personal Shrinking Device en "Dispositif du Dr. Voss"
// ============================================

// Rename the PSD via tooltip
<compactmachines3:psd>.addTooltip("\u00a7d\u00a7oCree par le Dr. E. Voss");
<compactmachines3:psd>.addTooltip("");
<compactmachines3:psd>.addTooltip("\u00a77Cet appareil vibre a une frequence");
<compactmachines3:psd>.addTooltip("\u00a77que la matiere ne comprend pas.");
<compactmachines3:psd>.addTooltip("\u00a78Ne le perdez pas.");

// Note: pour renommer l'item lui-meme il faut un resource pack
// ou modifier le lang file de compactmachines3

// Empecher le craft du PSD vanilla
// Le joueur recoit le sien dans le coffre de depart
recipes.remove(<compactmachines3:psd>);

// Recette de secours (au cas ou le joueur le perd)
recipes.addShaped("nexus_psd_backup", <compactmachines3:psd>,
    [[null, <nexusabsolu:wall_dust>, null],
     [<nexusabsolu:wall_dust>, <minecraft:ender_pearl>, <nexusabsolu:wall_dust>],
     [null, <ore:ingotIron>, null]]);

print("[Nexus Absolu] Age0_PSD.zs loaded");
