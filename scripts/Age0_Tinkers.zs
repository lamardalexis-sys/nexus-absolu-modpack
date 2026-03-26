// ============================================
// NEXUS ABSOLU -- Age0_Tinkers.zs
// Tinkers Construct tweaks pour le early game compact
// ============================================

// ==========================================
// SMELTERY COST -- On ne modifie pas
// La Smeltery necessite :
//   - Seared Bricks (gravel + clay, cuit au four)
//   - Seared Tank (seared bricks)
//   - Controller (seared bricks)
//   - Faucet, Casting Table/Basin
// C'est un gate NATUREL -- le joueur a besoin de clay + gravel
// Clay vient du scavenging (pioche bois, rare)
// Gravel vient du gravel dust (4 dust -> 1 gravel)
// ==========================================

// ==========================================
// TOOL STATION et PART BUILDER -- OK par defaut
// Le joueur les craft en bois et les utilise dans la 5x5
// ==========================================

// ==========================================
// TINKERS TOOLS -- Progression naturelle
// Bois -> Pierre -> Fer -> Bronze -> Acier
// Les tools Tinkers remplacent les outils vanilla
// (les vanilla iron+ sont bloques dans Age0_Blocking.zs)
// ==========================================

// On ajoute un alliage custom dans la Smeltery : Vossium
// Vossium Ingot se fait dans la Smeltery : Iron + Redstone + Lapis
// C'est un materiau mid-tier entre Bronze et Acier
// DISPONIBLE : Age 1 (salle 9x9, Smeltery + lingots)

// NOTE : les alliages Tinkers sont dans le JSON config, pas CraftTweaker
// On gerera ca dans les configs Tinkers

print("[Nexus Absolu] Age0_Tinkers.zs loaded");
