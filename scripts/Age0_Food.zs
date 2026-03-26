// ============================================
// NEXUS ABSOLU -- Age0_Food.zs
// Bonsai Trees, Pam's HarvestCraft, Spice of Life, Crop Dusting
// Nourriture et farming dans la compact machine
// ============================================

// ==========================================
// BONSAI TREES -- Source de bois en compact
// Le joueur recoit un Oak Sapling dans le coffre de depart
// Bonsai Pot + Sapling = arbre auto dans 1 bloc
// ==========================================

// Le Bonsai Pot est craftable par defaut avec des planks
// Pas de modification necessaire

// ==========================================
// PAM'S HARVESTCRAFT -- Nourriture variee
// Le joueur plante des graines sur de la terre
// La variete alimentaire donne des coeurs (Spice of Life Carrot)
// ==========================================

// Les jardins Pam's (gardens) spawnnent sur la dirt
// En compact machine, le joueur fait de la dirt via Ex Nihilo barrel
// Puis place la dirt, clic droit pour scavenge -> graines

// ==========================================
// SPICE OF LIFE CARROT EDITION
// Manger des aliments differents = plus de coeurs
// Max 50 coeurs (config a faire)
// ==========================================

// La progression de coeurs :
//  5 aliments differents  ->  1 coeur bonus
// 10 aliments differents  ->  2 coeurs bonus
// 15 aliments differents  ->  3 coeurs bonus
// etc.
// C'est gere dans config/spiceoflife

// ==========================================
// CROP DUSTING -- Poop = composant central
// Les animaux (chickens, cows) font du poop
// Le poop est un composant pour :
//   - La Cle 5x5 (premier craft qui utilise poop)
//   - Le Super Fertilizer (accelere les crops)
//   - L'Organic Catalyst (composant du Fragment Organique)
// ==========================================

// Le poop spawn quand le joueur s'accroupit longtemps
// OU quand des animaux sont proches
// Pas de modification necessaire -- le mod le fait tout seul

// ==========================================
// SOUL SHARDS -- Mob farming en compact
// Le joueur tue des mobs pour remplir un Soul Shard
// 50 kills = tier 1 -> Soul Cage = spawner automatique
// ==========================================

// Soul Shard est OK par defaut
// Le joueur a besoin de Soul Sand pour le Soul Cage
// Soul Sand : normalement Nether only
// SOLUTION : Ex Nihilo sieve dust avec diamond mesh -> Soul Sand

// Ajouter Soul Sand au tamis (dust + diamond mesh)
// NOTE: Ceci est gere dans la config Ex Nihilo JSON
// Si pas possible en JSON, on le fait ici :

// mods.exnihilocreatio.Sieve.addStringMeshRecipe(<minecraft:soul_sand>, <exnihilocreatio:block_dust>, 0.05);
// ^ Decommenter si Ex Nihilo config ne le fait pas

// ==========================================
// STORAGE DRAWERS -- Organisation en compact
// CRUCIAL : chaque bloc compte dans une petite salle
// Storage Drawers = meilleur ratio stockage/bloc
// ==========================================

// Storage Drawers est OK par defaut
// Le joueur craft des Drawers en bois -> stockage compact
// Le Compacting Drawer auto-compress (nuggets -> ingots -> blocks)
// Le Drawer Controller donne un acces centralise

// Pas de modification necessaire

print("[Nexus Absolu] Age0_Food.zs loaded");
