// ============================================
// NEXUS ABSOLU -- Age0_ExNihilo.zs
// Configuration Ex Nihilo Creatio pour le gameplay compact
// Le tamis est la voie PRINCIPALE de progression
// Les grits des murs sont le COMPLEMENT
// ============================================

// ==========================================
// SIEVE DROPS -- Override gravel drops
// En compact: on veut SEULEMENT coal, flint, iron nugget
// Pas de pieces/chunks (trop de raccourcis)
// ==========================================

// Sieve drops are configured via JSON in config/exnihilocreatio/SieveRegistry.json
// (enableJSONLoading=true in ExNihiloCreatio.cfg)
// Gravel drops: coal, flint, iron nuggets SEULEMENT

// ==========================================
// CRUCIBLE -- Source de lave en compact
// CRITIQUE : le joueur n'a pas acces a la lave naturellement
// Le Crucible Ex Nihilo chauffe par en dessous (torche/lave)
// ==========================================

// Le Crucible vanilla Ex Nihilo fait :
// Cobblestone -> 250mb Lava (sur source de chaleur)
// Netherrack -> 1000mb Lava (plus efficace)
// On ne modifie pas -- c'est bien comme ca

// Le joueur place une torche sous le Crucible
// Met du cobblestone dedans -> lave lente
// Cette lave alimente la Smeltery Tinkers (7x7)

// ==========================================
// BARRELS -- Composting en compact
// Le barrel transforme les dechets organiques en dirt
// CRUCIAL car le joueur n'a pas de dirt infini
// ==========================================

// Les recipes de compost sont gerees par Ex Nihilo config
// On s'assure que les items Pam's HarvestCraft fonctionnent
// Default : feuilles, saplings, graines -> compost -> dirt

// ==========================================
// MESH UPGRADES -- Progression du tamis
// ==========================================

// String Mesh (default) -- basique
// Flint Mesh -- minerais communs
// Iron Mesh -- minerais moddes (copper, tin, silver, etc.)
// Diamond Mesh -- materiaux rares (osmium, certus quartz)

// Les meshs sont deja dans Ex Nihilo, pas besoin de modifier
// SAUF : on veut que le Diamond Mesh necessite des composants Tinkers

recipes.remove(<exnihilocreatio:item_mesh:4>); // diamond mesh
recipes.addShaped("nexus_diamond_mesh", <exnihilocreatio:item_mesh:4>,
    [[<minecraft:diamond>, <minecraft:string>, <minecraft:diamond>],
     [<minecraft:string>, <ore:ingotBronze>, <minecraft:string>],
     [<minecraft:diamond>, <minecraft:string>, <minecraft:diamond>]]);
// Necessite Bronze = Smeltery = 7x7 minimum

// ==========================================
// HAMMER -- Ex Nihilo hammer pour casser les blocs
// Cobble -> Gravel -> Sand -> Dust
// ==========================================

// Les hammers Ex Nihilo sont OK par defaut
// Le joueur les craft pour transformer cobble -> gravel -> sand -> dust
// Chaque etape donne acces a de meilleurs tamis

// ==========================================
// CROOK -- Pour les feuilles / saplings
// ==========================================

// Le Crook Ex Nihilo est OK par defaut
// Utile pour les Bonsai Trees -> plus de saplings

// ==========================================
// DOLLS -- Spawn de mobs via Ex Nihilo
// ==========================================

// Les dolls Ex Nihilo (Blaze, Enderman, Shulker, Guardian)
// sont OK mais on les gate un peu

// La Blaze Doll necessite du Nether material -> pas avant Age 3
// L'Enderman Doll OK car les Ender Pearls sont tres rares en compact

print("[Nexus Absolu] Age0_ExNihilo.zs loaded");
