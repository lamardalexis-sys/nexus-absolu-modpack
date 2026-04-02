# CRAFT_REFERENCE.md — Systeme de Craft Nexus Absolu
> Base sur l'analyse de ~20K lignes de scripts SevTech Ages + Enigmatica 2 Expert.
> Ce document est LA reference pour creer des recettes. Le lire AVANT de coder.

---

## 1. ARCHITECTURE DES SCRIPTS

### Structure des fichiers
```
scripts/
  Globals.zs              #priority 3000 - variables globales, raccourcis metaux
  NexusCraft.zs           #priority 2000 - framework de craft (inspire E2E)
  Age0_Scavenging.zs      - recettes Age 0
  Age0_Keys.zs            - cles + catalyseurs
  Age0_Blocking.zs        - items bloques
  Age0_ExNihilo.zs        - Ex Nihilo overrides
  Age0_Food.zs            - nourriture
  Age1_Thermal.zs         - Thermal inter-mod
  Age1_EnderIO.zs         - EnderIO inter-mod
  Age1_IE.zs              - Immersive Engineering
  Age1_Invarium.zs        - crafts Invarium/Vossium
  Age2_Magic.zs           - Botania/Astral/Blood Magic
  Age3_Mekanism.zs        - Mekanism gating
  Age3_PneumaticCraft.zs  - PneumaticCraft gating
  contenttweaker/
    NexusItems.zs          #loader contenttweaker - items custom
```

### Ordre de chargement
```
#priority 4000  - ContentTweaker (items/blocs custom)
#priority 3000  - Globals.zs (variables, raccourcis)
#priority 2000  - NexusCraft.zs (framework)
#priority 1000  - Scripts de recettes (par defaut)
```

---

## 2. GLOBALS.ZS — Raccourcis centraux

### Pattern SevTech : metals.X.Y
SevTech utilise un systeme MaterialSystem de ContentTweaker.
Pour Nexus Absolu, on utilise des variables simples dans Globals.zs :

```zenscript
#priority 3000

// === METAUX THERMAL ===
global ironIngot     as IItemStack = <minecraft:iron_ingot>;
global goldIngot     as IItemStack = <minecraft:gold_ingot>;
global copperIngot   as IItemStack = <thermalfoundation:material:128>;
global tinIngot      as IItemStack = <thermalfoundation:material:129>;
global silverIngot   as IItemStack = <thermalfoundation:material:130>;
global leadIngot     as IItemStack = <thermalfoundation:material:131>;
global nickelIngot   as IItemStack = <thermalfoundation:material:133>;
global steelIngot    as IItemStack = <thermalfoundation:material:160>;
global electrumIngot as IItemStack = <thermalfoundation:material:161>;
global invarIngot    as IItemStack = <thermalfoundation:material:162>;
global bronzeIngot   as IItemStack = <thermalfoundation:material:163>;
global signalumIngot as IItemStack = <thermalfoundation:material:165>;
global enderiumIngot as IItemStack = <thermalfoundation:material:167>;

// === GEARS ===
global ironGear      as IItemStack = <thermalfoundation:material:24>;
global copperGear    as IItemStack = <thermalfoundation:material:256>;
global invarGear     as IItemStack = <thermalfoundation:material:290>;
global bronzeGear    as IItemStack = <thermalfoundation:material:291>;
global signalumGear  as IItemStack = <thermalfoundation:material:293>;

// === PLATES ===
global ironPlate     as IItemStack = <thermalfoundation:material:32>;
global copperPlate   as IItemStack = <thermalfoundation:material:320>;
global steelPlate    as IItemStack = <thermalfoundation:material:352>;

// === THERMAL FRAMES ===
global machineFrame  as IItemStack = <thermalexpansion:frame>;

// === ENDERIO ===
global electricalSteel  as IItemStack = <enderio:item_alloy_ingot:0>;
global darkSteel        as IItemStack = <enderio:item_alloy_ingot:6>;
global redstoneAlloy    as IItemStack = <enderio:item_alloy_ingot:3>;
global simpleChassis     as IItemStack = <enderio:item_material:0>;

// === IE ===
global treatedWood      as IItemStack = <immersiveengineering:treated_wood>;
global steelRod         as IItemStack = <immersiveengineering:material:2>;
global wireCoilCopper   as IItemStack = <immersiveengineering:wirecoil:0>;
global ieHeater         as IItemStack = <immersiveengineering:metal_device1:1>;

// === NEXUS ABSOLU CUSTOM ===
global wallDust       as IItemStack = <nexusabsolu:wall_dust>;
global ironGrit       as IItemStack = <nexusabsolu:iron_grit>;
global copperGrit     as IItemStack = <nexusabsolu:copper_grit>;
global tinGrit        as IItemStack = <nexusabsolu:tin_grit>;
global composeA       as IItemStack = <nexusabsolu:compose_a>;

// === CONTENTTWEAKER CUSTOM ===
global invariumIngot  as IItemStack = <contenttweaker:invarium_ingot>;
global vossiumIngot   as IItemStack = <contenttweaker:vossium_ingot>;
global vossiumII      as IItemStack = <contenttweaker:vossium_ii_ingot>;
```

---

## 3. NEXUSCRAFT.ZS — Framework de craft simplifie

### Inspire de E2E (Krutoy242) mais simplifie pour notre usage

```zenscript
#priority 2000

import crafttweaker.item.IItemStack;
import crafttweaker.item.IIngredient;

// ============================================================
//  FONCTION PRINCIPALE : nexus.remake()
//  Supprime l'ancienne recette + ajoute la nouvelle
//  Pattern E2E "remove-then-replace"
// ============================================================

// Shaped remake (le plus courant)
function remake(output as IItemStack, grid as IIngredient[][]) {
    recipes.remove(output);
    recipes.addShaped(output, grid);
}

// Shaped remake avec nom custom
function remakeNamed(name as string, output as IItemStack, grid as IIngredient[][]) {
    recipes.remove(output);
    recipes.addShaped(name, output, grid);
}

// Shapeless remake
function remakeShapeless(output as IItemStack, inputs as IIngredient[]) {
    recipes.remove(output);
    recipes.addShapeless(output, inputs);
}

// Mirrored remake (symetrique gauche-droite automatique)
function remakeMirrored(name as string, output as IItemStack, grid as IIngredient[][]) {
    recipes.remove(output);
    recipes.addShapedMirrored(name, output, grid);
}

// ============================================================
//  FONCTIONS UTILITAIRES
// ============================================================

// Supprimer une recette + la cacher de JEI
function purge(item as IItemStack) {
    mods.jei.JEI.removeAndHide(item);
}

// Supprimer uniquement la recette (garder visible dans JEI)
function removeOnly(item as IItemStack) {
    recipes.remove(item);
}

// Ajouter un tooltip en jeu + description JEI
function describe(item as IItemStack, text as string) {
    item.addTooltip(text);
    mods.jei.JEI.addDescription(item, text);
}

// Compact : 9 items -> 1 bloc et inverse
function compactRecipe(item as IIngredient, block as IItemStack) {
    recipes.addShapeless(block, [item,item,item,item,item,item,item,item,item]);
    recipes.addShapeless(item.items[0] * 9, [block]);
}
```

### Utilisation dans les scripts de recettes

```zenscript
// Au lieu de :
recipes.remove(<thermalexpansion:machine:1>);
recipes.addShaped("Pulverizer", <thermalexpansion:machine:1>, [
    [null, <actuallyadditions:block_grinder>, null],
    [<ore:ingotInvar>, machineFrame, <ore:ingotInvar>],
    [copperGear, wireCoilCopper, copperGear]
]);

// On ecrit :
remake(<thermalexpansion:machine:1>, [
    [null, <actuallyadditions:block_grinder>, null],
    [invarIngot, machineFrame, invarIngot],
    [copperGear, wireCoilCopper, copperGear]
]);
```

---

## 4. PATTERNS DE RECETTES — CE QU'ON A APPRIS

### 4a. Pattern SevTech : Static Recipe Maps
Utilise quand on a BEAUCOUP de recettes pour un mod.
```zenscript
static shapedRecipes as IIngredient[][][][IItemStack] = {
    <output:item> : [[
        [A, B, A],
        [C, D, C],
        [A, B, A]
    ]]
};

static removeRecipes as IItemStack[] = [
    <output:item>
];

// Dans init() :
for item, recipeLists in shapedRecipes {
    for recipeList in recipeLists {
        recipes.addShaped(item, recipeList);
    }
}
for item in removeRecipes { recipes.remove(item); }
```

### 4b. Pattern E2E : Pretty Grid (pour reference, pas implemente)
```zenscript
craft.remake(<minecraft:piston> * 2, ["pretty",
    "# # #",
    "@ I @",
    "@ R @"], {
    "@": <ore:cobblestone>,
    "#": <ore:plankWood>,
    "R": <ore:dustRedstone>,
    "I": <ore:plateIron>,
});
```
Elegant mais trop complexe pour notre taille (~200 recettes vs 2000+ pour E2E).

### 4c. Pattern Nexus Absolu : remake() direct
Notre approche simplifiee — le meilleur compromis lisibilite/simplicite :
```zenscript
// Pulverizer : force IE + Tinkers
remake(<thermalexpansion:machine:1>, [
    [null, <actuallyadditions:block_grinder>, null],
    [invarIngot, machineFrame, invarIngot],
    [copperGear, wireCoilCopper, copperGear]
]);
```

---

## 5. REGLES DE DESIGN DES RECETTES

### Regle d'or : 2-3 mods par craft
Chaque recette importante DOIT forcer des composants de 2-3 mods differents.
Le joueur ne doit JAMAIS pouvoir progresser avec un seul mod.

### Position = Role (grille 3x3)
```
HAUT    = Fonction (ce que la machine FAIT)
CENTRE  = Coeur / frame / composant cle
BAS     = Base / puissance / alimentation
COTES   = Structure / enveloppe
COINS   = Renfort / materiau secondaire
```

### Symetrie obligatoire
Toujours symetrique gauche-droite. Utiliser addShapedMirrored si un seul cote suffit.

### Progression des materiaux par age
```
Age 0 : wall_dust, grits, cobble, bois, fer basique
Age 1 : bronze, invar, cuivre, Invarium, Vossium, acier
Age 2 : electrum, signalum, manasteel, starmetal, slates
Age 3 : enderium, osmium, dark steel, compressed iron
Age 4 : certus quartz, processeurs AE2, circuits Mekanism
Age 5 : uranium, thorium, draconium, nuclearcraft
```

### Anti-exploits (inspires de E2E/SevTech)
- Bloquer la fonte de redstone/glowstone dans la Smeltery (empeche raccourci EnderIO)
- Supprimer les outils vanilla (forcer Tinkers)
- Supprimer les recettes de base de chaque mod AVANT d'ajouter les notres
- Bloquer les items d'ages futurs via ItemStages/RecipeStages

---

## 6. API REFERENCE — RECETTES PAR MOD

### CraftTweaker (vanilla)
```zenscript
// Shaped
recipes.addShaped("nom", <output>, [[A,B,C],[D,E,F],[G,H,I]]);
recipes.addShapedMirrored("nom", <output>, [[A,B,C],[D,E,F]]);

// Shapeless
recipes.addShapeless("nom", <output>, [A, B, C]);

// Remove
recipes.remove(<item>);
recipes.removeByRecipeName("mod:recipe_name");

// Furnace
furnace.addRecipe(<output>, <input>);
furnace.remove(<output>);
```

### Thermal Expansion
```zenscript
// Pulverizer (broyage)
mods.thermalexpansion.Pulverizer.addRecipe(<output>, <input>, energy);
mods.thermalexpansion.Pulverizer.addRecipe(<output>, <input>, energy, <secondary>, chance);

// Induction Smelter (alliages)
mods.thermalexpansion.InductionSmelter.addRecipe(<output>, <input1>, <input2>, energy);

// Sawmill
mods.thermalexpansion.Sawmill.addRecipe(<output>, <input>, energy);
mods.thermalexpansion.Sawmill.addRecipe(<output>, <input>, energy, <secondary>, chance);

// Fluid Transposer (fill)
mods.thermalexpansion.Transposer.addFillRecipe(<output>, <input>, <liquid:nom> * amount, energy);

// Dynamos (fuel)
mods.thermalexpansion.SteamDynamo.addFuel(<item>, energy);
mods.thermalexpansion.CompressionDynamo.addFuel(<liquid:nom>, energy);
mods.thermalexpansion.NumisticDynamo.addGemFuel(<item>, energy);

// Crucible (item -> fluid)
mods.thermalexpansion.Crucible.addRecipe(<liquid:nom> * amount, <input>, energy);

// Centrifuge
mods.thermalexpansion.Centrifuge.addRecipe([<output> % chance], <input>, <liquid>, energy);
```

### EnderIO
```zenscript
// Alloy Smelter
mods.enderio.AlloySmelter.addRecipe(<output>, [<in1>, <in2>, <in3>], energy);

// SAG Mill
mods.enderio.SagMill.addRecipe([<out1> % chance, <out2> % chance], <input>, "NONE", energy);

// Soul Binder
mods.enderio.SoulBinder.addRecipe(<output>, <input>, ["minecraft:zombie"], xp, energy);
```

### Immersive Engineering
```zenscript
// Crusher (broyage)
mods.immersiveengineering.Crusher.addRecipe(<output>, <input>, energy);
mods.immersiveengineering.Crusher.addRecipe(<output>, <input>, energy, <secondary>, chance);

// Arc Furnace (alliages)
mods.immersiveengineering.ArcFurnace.addRecipe(<output>, <input>, <slag>, time, energy, [<additives>]);

// Metal Press
mods.immersiveengineering.MetalPress.addRecipe(<output>, <input>, <mold>, energy);

// Alloy Kiln
mods.immersiveengineering.AlloySmelter.addRecipe(<output>, <input1>, <input2>, time);

// Blueprint
mods.immersiveengineering.Blueprint.addRecipe("category", <output>, [<inputs>]);
```

### Tinkers Construct
```zenscript
// Smeltery alliage
mods.tconstruct.Alloy.addRecipe(<liquid:output> * amount, [<liquid:in1> * a, <liquid:in2> * b]);
mods.tconstruct.Alloy.removeRecipe(<liquid:output>);

// Melting (fondre un item)
mods.tconstruct.Melting.addRecipe(<liquid:output> * amount, <input>);
mods.tconstruct.Melting.removeRecipe(<liquid:output>, <input>);

// Casting (couler dans un moule)
mods.tconstruct.Casting.addTableRecipe(<output>, <cast>, <liquid:input>, amount, consumeCast, time);
mods.tconstruct.Casting.addBasinRecipe(<output>, <cast>, <liquid:input>, amount);
```

### Mekanism
```zenscript
// Metallurgic Infuser
mods.mekanism.infuser.addRecipe("TYPE", amount, <input>, <output>);
// Types: REDSTONE, DIAMOND, OBSIDIAN, TIN, CARBON, FUNGI, BIO

// Enrichment Chamber
mods.mekanism.enrichment.addRecipe(<input>, <output>);

// Crusher
mods.mekanism.crusher.addRecipe(<input>, <output>);

// Purification Chamber
mods.mekanism.purification.addRecipe(<input>, <gas:oxygen>, <output>);

// Chemical Injection Chamber
mods.mekanism.chemical.injection.addRecipe(<input>, <gas:type>, <output>);
```

### Botania
```zenscript
// Petal Apothecary
mods.botania.Apothecary.addRecipe(<output>, [<petals>]);

// Mana Infusion
mods.botania.ManaInfusion.addInfusion(<output>, <input>, mana);
mods.botania.ManaInfusion.addAlchemy(<output>, <input>, mana);

// Elven Trade
mods.botania.ElvenTrade.addRecipe([<outputs>], [<inputs>]);

// Runic Altar
mods.botania.RuneAltar.addRecipe(<output>, [<inputs>], mana);
```

### Astral Sorcery
```zenscript
// Starlight Crafting (Luminous Table)
mods.astralsorcery.Altar.addDiscoveryAltarRecipe("nom", <output>, starlightRequired, craftTickTime, [grid]);

// Constellation Altar
mods.astralsorcery.Altar.addConstellationAltarRecipe("nom", <output>, starlight, time, [grid]);

// Lightwell
mods.astralsorcery.Lightwell.addLiquefaction(<input>, <liquid:output>, multiplier, shatter);

// Grindstone
mods.astralsorcery.Grindstone.addRecipe(<input>, <output>);
```

### Blood Magic
```zenscript
// Blood Altar
mods.bloodmagic.BloodAltar.addRecipe(<output>, <input>, tier, lpDrained, consumeRate, drainRate);

// Alchemy Table
mods.bloodmagic.AlchemyTable.addRecipe(<output>, [<inputs>], lpDrained, time, tierRequired);

// Tartaric Forge
mods.bloodmagic.TartaricForge.addRecipe(<output>, [<inputs>], minimumSouls, soulDrain);
```

### Extended Crafting
```zenscript
// Table Crafting (3x3 a 9x9)
mods.extendedcrafting.TableCrafting.addShaped(<output>, [[grid 5x5 ou 7x7 ou 9x9]]);
mods.extendedcrafting.TableCrafting.addShapeless(<output>, [ingredients]);

// Combination Crafting (pedestal + RF)
mods.extendedcrafting.CombinationCrafting.addRecipe(<output>, rfCost, rfPerTick, <center>, [<pedestals>]);

// Ender Crafting
mods.extendedcrafting.EnderCrafting.addShaped(<output>, [[grid]], seconds);
```

### Ex Nihilo Creatio
```zenscript
// Sieve
mods.exnihilocreatio.Sieve.addStringMeshRecipe(<input>, <output>, chance);
mods.exnihilocreatio.Sieve.addFlintMeshRecipe(<input>, <output>, chance);
mods.exnihilocreatio.Sieve.addIronMeshRecipe(<input>, <output>, chance);
mods.exnihilocreatio.Sieve.addDiamondMeshRecipe(<input>, <output>, chance);

// Hammer
mods.exnihilocreatio.Hammer.addRecipe(<input>, <output>, chance, fortuneChance, [forLevels]);

// Compost
mods.exnihilocreatio.Compost.addRecipe(<input>, amount, <output>, color);

// Crucible (heat)
mods.exnihilocreatio.Crucible.addRecipe(<input>, <liquid:output> * amount);
```

### JEI
```zenscript
import mods.jei.JEI;

// Cacher un item de JEI
JEI.removeAndHide(<item>);

// Cacher sans supprimer la recette
JEI.hide(<item>);

// Ajouter une description
JEI.addDescription(<item>, "Texte explicatif");
JEI.addDescription(<item>, ["Ligne 1", "Ligne 2"]);
```

### GameStages / ItemStages / RecipeStages
```zenscript
// Bloquer un item tant que le stage n'est pas deblocke
mods.ItemStages.addItemStage("age_1", <item>);

// Bloquer une recette
mods.recipestages.Recipes.setRecipeStage("age_1", <output>);
mods.recipestages.Recipes.setRecipeStage("age_1", "recipe_name");

// Debloquer un stage pour un joueur (via commande ou advancement)
// /gamestage add @p age_1
```

---

## 7. TEMPLATES DE RECETTES PAR AGE

### Template Age 0 (Compact Machine 3x3)
```zenscript
// Pattern : items basiques, pas de RF, crafts manuels
remake(<nexusabsolu:condenseur>, [
    [ironGrit, wallDust, ironGrit],
    [wallDust, <minecraft:redstone>, wallDust],
    [ironGrit, wallDust, ironGrit]
]);
```

### Template Age 1 (Compact Machine 5x5-7x7, premier RF)
```zenscript
// Pattern : bronze/invar + composants IE/Thermal
remake(<thermalexpansion:machine:1>, [
    [null, wireCoilCopper, null],
    [invarIngot, machineFrame, invarIngot],
    [copperGear, ieHeater, copperGear]
]);
```

### Template Age 3+ (Overworld, inter-mod agressif)
```zenscript
// Pattern : 3+ mods forces, materiaux avances
remake(<mekanism:machineblock:0>, [
    [steelPlate, <mekanism:controlcircuit:0>, steelPlate],
    [<ore:circuitBasic>, <mekanism:basicblock:8>, <ore:circuitBasic>],
    [steelPlate, ieHeater, steelPlate]
]);
```

---

## 8. CHECKLIST AVANT DE CREER UNE RECETTE

- [ ] La recette force-t-elle 2-3 mods differents ?
- [ ] Est-elle symetrique gauche-droite ?
- [ ] Le centre est-il le composant cle ?
- [ ] Le haut represente-t-il la fonction ?
- [ ] L'ancienne recette vanilla est-elle supprimee ?
- [ ] Les materiaux correspondent-ils a l'age actuel ?
- [ ] Un tooltip/description JEI guide-t-il le joueur ?
- [ ] La recette ne cree-t-elle pas de raccourci (anti-exploit) ?

---

## 9. ANTI-EXPLOITS A IMPLEMENTER

### Inspires de E2E
```zenscript
// Bloquer fonte redstone/glowstone dans Smeltery (empeche alliages EnderIO gratis)
for item in <ore:dustRedstone>.items {
    mods.tconstruct.Melting.removeRecipe(<liquid:redstone>, item);
}
for item in <ore:blockRedstone>.items {
    mods.tconstruct.Melting.removeRecipe(<liquid:redstone>, item);
}

// Bloquer duplication bronze via Tinkers
mods.tconstruct.Melting.removeRecipe(<liquid:bronze>, <ic2:pipe>);
```

### Inspires de SevTech
```zenscript
// Supprimer les outils vanilla (forcer pioche custom / Tinkers)
purge(<minecraft:wooden_pickaxe>);
purge(<minecraft:stone_pickaxe>);
purge(<minecraft:iron_pickaxe>);
purge(<minecraft:diamond_pickaxe>);
purge(<minecraft:golden_pickaxe>);

// Bloquer acces Nether/End en Age 0
purge(<minecraft:flint_and_steel>);  // pas de portail Nether
```

---

## 10. TOOLTIPS PATTERN (inspire E2E)

```zenscript
// Tooltips informatifs pour guider le joueur
describe(machineFrame, "Base de toutes les machines Thermal. Necessite Invar + Verre.");
describe(<nexusabsolu:condenseur>, "Fusionne 2 Compact Machines en une plus grande. Necessite du RF.");
describe(invariumIngot, "Alliage du Dr. Voss. Bronze + Invar dans l'Alloy Kiln.");
describe(vossiumIngot, "Invarium perfectionne avec du Compose A. Evolue avec chaque Compose.");
describe(composeA, "Energie dimensionnelle cristallisee. Drop rare du scavenging.");

// Pattern E2E : descriptions JEI pour les items non-craftables
mods.jei.JEI.addDescription(<compactmachines3:wall>,
    "Ne peut pas etre mine normalement.",
    "Utilisez vos poings ou une Pioche Custom pour extraire des ressources.",
    "Pioche Fragmentee: wall_dust + fragments",
    "Pioche Renforcee: grits + compose");
```

---

*Derniere mise a jour : Session 10 — Analyse SevTech + E2E (~20K lignes)*
