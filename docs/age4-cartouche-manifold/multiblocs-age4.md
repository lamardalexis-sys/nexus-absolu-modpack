# Multiblocs Age 4 — Schémas de construction

> Référence pour construire les 2 multiblocs Modular Machinery de l'Age 4.
> Specs validées avec Alexis. JSON dans `config/modularmachinery/machinery/`.

---

## 🏭 CRYO-DISTILLATEUR ATMOSPHERIQUE

**Specs** : socle 7×5 sur 2 couches + tour 3×3 hauteur 11. Réaliste = colonne de distillation atmosphérique avec 2 gros compresseurs.

**Total** : 152 blocs, 21 m de haut.

### Légende
- `C` = Casing (n'importe quel type — modularmachinery:blockcasing ou hatch)
- `D` = Casing décoratif uniquement (modularmachinery:blockcasing)
- `★` = **Controller** (cryo_distillateur block custom)
- `P` = Compresseur (`minecraft:iron_block` — 4 unités, 2 paires de gros)
- `V` = **Fluid Input Hatch** (modularmachinery:blockfluidinputhatch) — entrée d'air
- `O` = **Fluid Output Hatch** (modularmachinery:blockfluidoutputhatch) — sortie Argon/N2/O2
- `G` = Verre (minecraft:glass) — visualisation colonne
- `.` = Vide

---

### Layer Y=-1 (sol bas, 7×5 plein)

```
       X→   -3  -2  -1   0   1   2   3
  Z=-2     [C] [C] [C] [C] [C] [C] [C]
  Z=-1     [C] [C] [C] [C] [C] [C] [C]
  Z= 0     [C] [C] [C] [C] [C] [C] [C]
  Z= 1     [C] [C] [C] [C] [C] [C] [C]
  Z= 2     [C] [C] [C] [C] [C] [C] [C]
```

**35 blocs casing.**

---

### Layer Y=0 (socle technique, 7×5 avec compresseurs + ventilations)

```
       X→   -3  -2  -1   0   1   2   3
  Z=-2     [C] [C] [C] [C] [C] [C] [C]
  Z=-1     [C] [P] [C] [V] [C] [P] [C]
  Z= 0     [C] [C] [V] [★] [V] [C] [C]
  Z= 1     [C] [P] [C] [V] [C] [P] [C]
  Z= 2     [C] [C] [C] [C] [C] [C] [C]
```

**34 blocs (controller compte séparément).**
- 4 compresseurs `iron_block` aux positions miroir (style industriel)
- 4 ventilations Fluid Input pour l'air ambiant (4 directions cardinales depuis le centre)

---

### Layers Y=1, 3, 5, 7 (niveaux pleins de la tour 3×3)

```
       X→   -1   0   1
  Z=-1     [C] [C] [C]
  Z= 0     [C]  .  [C]    ← centre creux (chambre fractionnement)
  Z= 1     [C] [C] [C]
```

**8 blocs × 4 niveaux = 32 blocs.**

---

### Layers Y=2, 4, 6, 8 (niveaux verre — visualisation)

```
       X→   -1   0   1
  Z=-1     [C] [G] [C]
  Z= 0     [G]  .  [G]    ← verre sur les 4 cotes du milieu
  Z= 1     [C] [G] [C]
```

**8 blocs × 4 niveaux = 32 blocs.**

---

### Layer Y=9 (sortie fluides au sommet de la tour)

```
       X→   -1   0   1
  Z=-1     [C] [C] [C]
  Z= 0     [C] [O] [C]    ← O = Fluid Output Hatch (Argon/N2/O2 sortent ici)
  Z= 1     [C] [C] [C]
```

**9 blocs.**

---

### Layer Y=10 (sommet décoratif)

```
       X→   -1   0   1
  Z=-1     [D] [D] [D]
  Z= 0     [D] [D] [D]
  Z= 1     [D] [D] [D]
```

**9 blocs casing décoratif.**

---

### Vue 3D simplifiée (coupe latérale, X:-1 à 1)

```
   Y=10  D D D    ← couvercle
   Y= 9  C O C    ← sortie Argon/N2/O2
   Y= 8  C G C    ← verre
   Y= 7  C C C
   Y= 6  C G C
   Y= 5  C C C
   Y= 4  C G C
   Y= 3  C C C
   Y= 2  C G C
   Y= 1  C C C    ← base de la tour
   Y= 0  V ★ V    ← socle (controller + 4 ventilations)
   Y=-1  C C C    ← sol bas
         |   |
         x=-1 x=1
```

---

### Comportement attendu

L'air ambiant entre par les 4 **Fluid Input Hatches** (les "ventilations" autour du controller). Les 4 compresseurs `iron_block` font partie de la structure (visuels — réalisme industriel).

L'air est compressé puis monte dans la colonne 3×3, où il subit la **distillation cryogénique** :
- N2 condense vers le bas (-196°C)
- O2 condense ensuite (-183°C)
- Argon reste au sommet (-186°C, mais purifié là-haut)

La sortie `Fluid Output Hatch` au sommet (Y=9) éjecte **séparément** N2, O2, Ar selon la recette en cours.

⚠️ **Recettes à définir** dans `config/modularmachinery/recipes/cryo_distillateur/` (prochaine étape).

---

## 🧬 BIO-RÉACTEUR MANIFOLD

**Specs** : compact 5×5×3 (low-profile, 1 étage utilisable).

**Total** : 83 blocs.

### Légende
- `C` = Casing (any type)
- `D` = Casing décoratif
- `★` = **Controller** (bioreacteur block custom)
- `Vi` = Fluid Input Hatch
- `Vo` = Fluid Output Hatch
- `Ii` = Item Input Bus
- `Io` = Item Output Bus
- `E` = Energy Input Hatch
- `G` = Verre
- `.` = Vide (chambre cyclisation, espace pour le joueur)

---

### Layer Y=-1 (sol, 5×5 plein)

```
       X→   -2  -1   0   1   2
  Z=-2     [C] [C] [C] [C] [C]
  Z=-1     [C] [C] [C] [C] [C]
  Z= 0     [C] [C] [C] [C] [C]
  Z= 1     [C] [C] [C] [C] [C]
  Z= 2     [C] [C] [C] [C] [C]
```

**25 blocs.**

---

### Layer Y=0 (mur d'accueil, anneau 5×5 avec controller au centre + 4 I/O)

```
       X→   -2  -1   0   1   2
  Z=-2     [C] [C] [Ii][C] [C]    ← Ii = Item Input (sud)
  Z=-1     [C]  .   .   .  [C]
  Z= 0     [Vi] .  [★]  .  [Vo]   ← Vi gauche, ★ controller, Vo droite
  Z= 1     [C]  .   .   .  [C]
  Z= 2     [C] [C] [Io][C] [C]    ← Io = Item Output (nord)
```

**17 blocs (anneau de 16 + controller compte séparément).**
- Intérieur 3×3 vide = chambre de cyclisation où le joueur entre

---

### Layer Y=1 (mur supérieur, anneau 5×5 avec verre sur 4 côtés)

```
       X→   -2  -1   0   1   2
  Z=-2     [E]  .  [G]  .  [C]    ← E = Energy Input (coin)
  Z=-1      .   .   .   .   .
  Z= 0     [G]  .   .   .  [G]    ← verre 4 cotes (visualisation magique)
  Z= 1      .   .   .   .   .
  Z= 2     [C]  .  [G]  .  [C]
```

**16 blocs (anneau, intérieur vide).**

---

### Layer Y=2 (toit, 5×5 plein décoratif)

```
       X→   -2  -1   0   1   2
  Z=-2     [D] [D] [D] [D] [D]
  Z=-1     [D] [D] [D] [D] [D]
  Z= 0     [D] [D] [D] [D] [D]
  Z= 1     [D] [D] [D] [D] [D]
  Z= 2     [D] [D] [D] [D] [D]
```

**25 blocs casing décoratif.**

---

### Vue 3D coupe latérale (X:-2 à 2, à Z=0)

```
   Y=2   D D D D D    ← toit
   Y=1   G . . . G    ← verre cotes
   Y=0   Vi . ★ . Vo  ← controller + I/O fluides
   Y=-1  C C C C C    ← sol
         |       |
         x=-2    x=2
```

---

### Comportement attendu

Le joueur **entre dans la chambre 3×3 vide** (intérieur du multibloc). Une fois tous les composants placés dans les Item/Fluid Input Hatches :
- Item Input (Z=-2) : Cristal Manifoldine, Mycelium Active, Carbone Au, etc.
- Fluid Input (X=-2) : Solution alpha, Solution beta, Eau Lourde, Liquid Starlight
- Energy Input (coin haut Y=1) : alimentation RF (50M+ recommandé)

Le controller déclenche la **cyclisation finale** :
- Sortie Fluid Output (X=+2) : Solution Epsilon
- Sortie Item Output (Z=+2) : Cartouche Manifold ARMÉE

⚠️ **Recettes à définir** dans `config/modularmachinery/recipes/bioreacteur/` (prochaine étape).

---

## 🛠️ TODO Recettes

Les multiblocs sont **fonctionnels structurellement** mais sans recette pour l'instant :

| Multibloc | Recettes à créer |
|---|---|
| Cryo-Distillateur | air → Argon, air → N2, air → O2 (3 recettes minimum) |
| Bio-Réacteur | composants → Solution Epsilon, Solution Epsilon → Cartouche Manifold |

Format des recettes : `config/modularmachinery/recipes/<machine_registryname>/<recipe_name>.json` avec `inputs`, `outputs`, `time`, `energy`. Voir `recipes/iron_centrifuge/` pour exemple.

---

## 📝 Note pour les quêtes BQ

Les quêtes Q4 (Cryo-Distillateur) et Q71 (Bio-Réacteur) utilisent actuellement `task_crafting` sur les blocs *controller* custom de ContentTweaker (`cryo_distillateur_controller`, `bioreacteur_controller`).

**Important** : Le block ContentTweaker custom n'est **PAS** la même chose que le multibloc Modular Machinery. Il y a 2 options :

**Option A** : Le block CT custom **remplace** le controller MM. Cela demande de re-pointer le JSON Modular Machinery sur `contenttweaker:cryo_distillateur_controller` au lieu d'un controller MM par défaut.

**Option B** : Le block CT custom est juste une **brique dans la structure** du multibloc, et le controller reste un block MM par défaut. Le joueur doit alors crafter LE controller MM (probablement via une recette CraftTweaker custom) ET les briques CT custom autour.

Recommandation : **Option A** — c'est plus immersif (le block custom Nexus avec belle texture biseautée EnderIO style EST le controller). À implémenter via le champ `controller` du JSON MM. Voir https://github.com/HellFirePvP/ModularMachinery/wiki .

À traiter dans le commit suivant.
