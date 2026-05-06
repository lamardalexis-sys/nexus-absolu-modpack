// ============================================================================
// Nexus Absolu — Age1_Machines.zs
//
// Recettes des machines Diarh33 (machine_humaine) et KRDA125 (machine_krda).
// Ajoute aussi la source de compose_e (tier endgame Age 2).
//
// CONTEXTE :
//   Audit recettes (AUDIT_RECETTES.md) a identifie que ces 2 machines existent
//   en Java mod (TileMachineHumaine, TileMachineKRDA, GUIs) mais n'avaient
//   AUCUNE recette de craft. Resultat : progression Age 1 -> Age 2 cassee
//   car impossible d'obtenir diarrhee_liquide ni signalhee_ingot.
//
// PATTERN E2E (cf MODPACK_RECIPES_SKILL.md) :
//   - Symetrie gauche-droite obligatoire
//   - Top    = fonction (ce que la machine fait)
//   - Center = composant cle / coeur
//   - Bottom = base / power
//   - 2-3 mods forces par craft
//
// PROGRESSION :
//   1. machine_humaine (Age 1, post-RF basique) -> diarrhee_liquide
//   2. machine_krda (Age 1, post-Signalum + machine_humaine) -> signalhee_ingot
//   3. compose_e (Age 2 endgame) -> via Smelter + composes inferieurs
// ============================================================================


// ============================================================================
// 1. machine_humaine (Diarh33) -- Age 1
// ============================================================================
//
// Concept narratif : machine biologique de fermentation qui digere de la
// matiere organique pour produire un fluide de diarrhee. Voss l'a mise au
// point apres ses experiences sur la chaine alimentaire dans la salle 7x7.
//
// Pattern :
//   [hopper]       [hopper]              [hopper]         <- top: 3 hoppers (entree alimentation)
//   [frame_TE]     [dispenser]           [frame_TE]       <- center: structure TE + dispenser (sortie fluide)
//   [ingotCopper]  [ingotDarkSteel]      [ingotCopper]    <- base: copper + dark steel (resistance corrosion)
//
// Mods forces : vanilla (hopper, dispenser) + Thermal Expansion (frame) + EnderIO (dark steel)

recipes.addShaped("nexus_machine_humaine",
    <nexusabsolu:machine_humaine>,
    [[<minecraft:hopper>,             <minecraft:hopper>,           <minecraft:hopper>],
     [<thermalexpansion:frame>,       <minecraft:dispenser>,        <thermalexpansion:frame>],
     [<ore:ingotCopper>,              <ore:ingotDarkSteel>,         <ore:ingotCopper>]]);


// ============================================================================
// 2. machine_krda (KRDA125) -- Age 1
// ============================================================================
//
// Concept narratif : machine industrielle de raffinement utilisant la
// chimie unique de la diarrhee combinee a des metaux conducteurs Signalum.
// Voss a decouvert que cette combinaison cree des alliages dimensionnels
// (signalhee).
//
// Pattern :
//   [ingotSignalum]   [frame_TE]                 [ingotSignalum]   <- top: signalum + frame (raffinement RF)
//   [ingotSignalum]   [machine_humaine]          [ingotSignalum]   <- center: machine_humaine au centre (chaining + signalum bobines)
//   [ingotInvar]      [gearCopper]               [ingotInvar]      <- base: invar + copper gear (mecanisme)
//
// Mods forces : Thermal Foundation (signalum, invar, copper gear) + Thermal Expansion (frame) + Nexus Absolu (machine_humaine)
// Pre-requis : machine_humaine deja crafte (chaining = progression naturelle)

recipes.addShaped("nexus_machine_krda",
    <nexusabsolu:machine_krda>,
    [[<ore:ingotSignalum>,            <thermalexpansion:frame>,     <ore:ingotSignalum>],
     [<ore:ingotSignalum>,            <nexusabsolu:machine_humaine>, <ore:ingotSignalum>],
     [<ore:ingotInvar>,               <ore:gearCopper>,             <ore:ingotInvar>]]);


// ============================================================================
// 3. compose_e (Age 2 endgame) -- via Induction Smelter
// ============================================================================
//
// Concept narratif : compose_e est la version "transcendee" du compose_d
// realisee en injectant 4 unites de compose_a (energie pure). C'est le
// dernier tier des composes, utilise uniquement pour les upgrades endgame.
//
// Pre-requis : compose_d (existant via raffinement vossium) + 4 compose_a (drops Pioche Vossium).
//
// Recette Induction Smelter (Thermal Expansion) :
//   1x compose_d + 4x compose_a -> 1x compose_e
//   Cout : 20000 RF (smelting industriel haute energie)

mods.thermalexpansion.InductionSmelter.addRecipe(
    <nexusabsolu:compose_e>,                 // output
    <nexusabsolu:compose_d>,                 // primary input
    <nexusabsolu:compose_a> * 4,             // secondary input (energie de fusion)
    20000                                     // RF cost
);

// Fallback EnderIO Alloy Smelter pour compatibilite si TE absent
mods.enderio.AlloySmelter.addRecipe(
    <nexusabsolu:compose_e>,
    [<nexusabsolu:compose_d>, <nexusabsolu:compose_a> * 4],
    20000
);


print("[Nexus Absolu] Age1_Machines.zs loaded -- 2 machines + compose_e recipes added");
