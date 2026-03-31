# MODPACK_RECIPES_SKILL.md — Patterns de recettes expert modpacks
> Reference pour designer des recettes style Enigmatica 2 Expert / Compact Claustrophobia.
> Lire AVANT de creer/modifier des recettes CraftTweaker.

---

## 1. REGLES VISUELLES (Table de Craft 3x3)

### Symetrie obligatoire
Toujours symetrique gauche-droite (miroir vertical).
```
[X] [Y] [X]     BIEN (symetrique)
[Z] [C] [Z]
[X] [Y] [X]

[X] [Y] [Z]     MAL (asymetrique)
[Z] [C] [X]
[Y] [X] [Z]
```

### Position = Role
```
HAUT    = Fonction (ce que la machine FAIT)
CENTRE  = Composant cle / coeur / frame
BAS     = Base / puissance / sortie
COTES   = Structure / enveloppe / protection
COINS   = Renfort / decoration / materiau secondaire
```

### Exemples E2E
```
Pulverizer:
  [piston]  [grinder]  [piston]     <- fonction: ecraser
  [flint]   [frame]    [flint]      <- coeur: machine frame + flint (materiau dur)
  [gear]    [coil]     [gear]       <- base: engrenage cuivre + bobine redstone

Igneous Extruder:
  [null]    [glass]    [null]       <- fonction: voir a travers
  [invar]   [frame64]  [invar]      <- coeur: frame + invar (resistant chaleur)
  [gearCu]  [IE_block] [gearCu]    <- base: engrenage + composant IE

Nullifier:
  [tin]  [tin]   [tin]              <- full enveloppe = protection complete
  [tin]  [trash] [tin]              <- coeur: poubelle
  [tin]  [tin]   [tin]              <- (pattern "boite")
```

### Formes classiques
```
OUTIL (pioche, epee):        MACHINE:                   TUYAU/CONDUIT:
[tete] [tete] [null]         [fonc]  [fonc]  [fonc]     [isol] [cond] [isol]
[null] [bind] [null]         [struct][coeur] [struct]    [isol] [ener] [isol]
[null] [manch][null]         [base]  [power] [base]     [isol] [cond] [isol]

ENGRENAGE (croix):           BLOC STOCKAGE:             UPGRADE:
[null] [dent] [null]         [coin]  [cote]  [coin]     [mat]  [comp] [mat]
[dent] [axe]  [dent]         [cote]  [coeur] [cote]     [comp] [base] [comp]
[null] [dent] [null]         [coin]  [fonc]  [coin]     [mat]  [comp] [mat]
```

---

## 2. REGLES DE COMPLEXITE (E2E)

### Chaque craft important force 2-3 mods
```
BIEN:
  Pulverizer = Pistons (vanilla) + Grinder (AA) + Flint plate (Tinkers) + Frame (Thermal) + Gear (Thermal)
  → Force: vanilla + Actually Additions + Tinkers Construct + Thermal

MAL:
  Pulverizer = 4 iron + 1 redstone + 3 cobble
  → Juste vanilla, zero inter-mod
```

### Items intermediaires NE SE CRAFTENT PAS sur la table
Les composants cles (coils, gears, frames) doivent passer par des MACHINES:
```
E2E:
  Redstone Reception Coil = Atomic Reconstructor (AA) OU Mekanism Infuser
  Machine Frame = Forestry Thermionic Fabricator (4 mods dedans)
  Steel = IE Blast Furnace (pas craft table)

Pour Nexus Age 0:
  Bronze = Alloy Kiln IE (pas craft shapeless)
  Invar = Alloy Kiln IE (pas craft shapeless)
  Compose Gear = craft table OK (c'est notre item custom central)
```

### Progression tiered (chaque tier necessite le precedent)
```
E2E Cell Frames:
  Basic → Hardened (needs Basic + Empowerer) → Reinforced → Signalum → Resonant

CC Compact Machines:
  Tiny → smash → craft Small → smash → craft Normal → ...

Nexus Absolu Compose:
  Compose A (drop pioche) → Gear A → Block A → energie
  (futurs: Compose B-E pour les ages suivants)
```

---

## 3. REGLES DE GATING (CC + E2E)

### Gate par RECETTE, pas par Stage
CC ne bloque que 2 items via ItemStages (chorus fruit, ender pearl).
Tout le reste est gate par les INGREDIENTS des recettes:
- Pas de diamant dans ton inventaire = pas de diamond mesh = pas de drops rares
- Pas de bronze = pas de machine = pas d'alliages avances

### Gate par DISPONIBILITE des materiaux
En Compact Machine, le gating est NATUREL:
- Pas de minerai = pioche + sieve = seule source de metal
- Pas de Nether = pas de blaze rod, glowstone, soul sand
- Pas de mob = pas de drops mob (sauf si on les spawn)

### Gate par MACHINE
Forcer une machine intermediaire allonge la chaine:
```
CC: iron dust + HOP graphite + redstone → blend → four → modularium
    (5 ingredients + 1 machine = long)

Nexus: copper + tin → Alloy Kiln → bronze → Gear → machines
    (grind grits + kiln + craft = long)
```

---

## 4. PATTERNS SPECIFIQUES

### Fluxducts / Conduits (transport)
```
E2E Leadstone Fluxduct x12:
  [alloy] [alloy] [alloy]
  [cable]  [glass] [cable]
  [alloy] [alloy] [alloy]
  → 3 materiaux: alloy basic (EnderIO) + XNet cable + glass

Nexus Fluxduct x4:
  [dust]  [nugget] [dust]
  [dust]  [redst]  [dust]
  [dust]  [nugget] [dust]
  → isolation dust + conducteur nugget + energie redstone
```

### Dynamos / Generateurs
```
E2E: pas de changement majeur sur les dynamos
CC: Enervation Dynamo nerfee (redstone 500 au lieu de default)

Nexus Stirling Dynamo:
  [null]   [gear]    [null]    <- conversion mecanique
  [cobble] [redst]   [cobble]  <- boitier
  [bronze] [redst]   [bronze]  <- base alliage
```

### Coffres / Stockage
```
E2E: Storage Crate = chest + treated wood + wood casing
CC: pas de changement special

Nexus Silver Chest:
  [bronze] [iron]   [bronze]   <- renfort
  [iron]   [chest]  [iron]     <- coeur = coffre vanilla
  [bronze] [string] [bronze]   <- mecanisme fermeture
```

### Machines Thermal (pattern standard)
```
TOUTES les machines Thermal E2E suivent:
  [fonctionnel] [fonctionnel] [fonctionnel]    <- ce que la machine fait
  [materiau]    [FRAME]       [materiau]       <- toujours un frame au centre
  [gear]        [COIL]        [gear]           <- toujours gear + coil en bas

Frame = thermalexpansion:frame (le composant le plus cher)
Coil = thermalfoundation:material:513 (Redstone Reception Coil)
Gear = ore:gearCopper / ore:gearInvar / ore:gearTin selon tier
```

---

## 5. ANTI-PATTERNS (ne PAS faire)

### Recettes "soupe" (ingredients random)
```
MAL:
  [diamond] [redstone] [iron]
  [gold]    [emerald]  [cobble]
  [string]  [coal]     [stick]
  → Aucune logique visuelle, juste "mets des trucs rares"
```

### Wall dust partout
```
MAL:
  [dust] [dust] [dust]
  [dust] [iron] [dust]
  [dust] [dust] [dust]
  → Wall dust = gratuit, ce n'est PAS un ingredient de valeur
  → Utiliser dust SEULEMENT comme isolation/remplissage (cotes/coins)
```

### Memes ingredients que vanilla mais reorganises
```
MAL:
  Four vanilla = 8 cobble en cercle
  "Custom" four = 8 cobble en forme differente
  → C'est pas un custom craft, c'est juste reorganise

BIEN:
  Custom four = 7 cobble + flint (allumeur) + clay (isolation)
  → Ajoute du sens: le flint allume le feu, la clay isole la chaleur
```

### Recettes trop simples pour des items importants
```
MAL: Condenseur = 4 iron + 1 redstone (trop facile)
BIEN: Condenseur = 2 bronze + 4 invar + 2 redstone + 1 compose
      (force: Alloy Kiln + 3 metaux + pioche renf grind)
```

---

## 6. CHECKLIST AVANT DE CREER UNE RECETTE

1. [ ] La recette est-elle SYMETRIQUE gauche-droite ?
2. [ ] Le CENTRE contient-il le composant le plus important ?
3. [ ] Le HAUT represente-t-il la fonction de l'item ?
4. [ ] Le BAS represente-t-il la base/puissance ?
5. [ ] La recette force-t-elle AU MOINS 2 sources de materiaux differentes ?
6. [ ] Le wall_dust n'est-il utilise QUE pour l'isolation (pas comme ingredient principal) ?
7. [ ] L'item necessaire le plus rare est-il accessible a ce stade du jeu ?
8. [ ] La recette a-t-elle un SENS visuel (on "voit" ce que l'item fait) ?
9. [ ] Pas de boucle (l'item A necessite B qui necessite A) ?

---

## 7. MATERIAUX PAR VALEUR (Age 0)

### Gratuit (mains nues)
wall_dust, sticks, planks

### Facile (pioche fragmentee)
cobblestone_fragment, flint, clay

### Moyen (sieve ou pioche renforcee)
iron_nugget, coal, redstone, grits

### Cher (transformation)
iron/copper/tin/nickel lingots (4 grits → raw → four)
string (4 wall_dust)
cobblestone (2 fragments)
gravel (hammer cobble)

### Tres cher (machine + grind)
bronze (Alloy Kiln: 3 copper + 1 tin)
invar (Alloy Kiln: 2 iron + 1 nickel)
compose_a (5.5% pioche renforcee)

### Endgame Age 0
compose_gear_a (4 compose A + 1 bronze)
compose_block_a (8 compose A + 1 gear)

---

*Reference: Enigmatica 2 Expert + Compact Claustrophobia scripts*
*Derniere mise a jour: Session 6*
