# 🎮 Plan de test critique — Validation Age 4 (post-session)

> **Pourquoi ce document** : 71 commits ajoutés en 8 jours sans aucun test in-game. Ce plan structure la validation en **4 niveaux** de gravité décroissante. Suis-le dans l'ordre, arrête-toi au premier blocage trouvé et reviens vers Claude pour fix immédiat.

---

## ⏱️ Estimation temps total

- **Niveau 1 (CRITIQUE)** : 5 min — confirme que rien n'est cassé
- **Niveau 2 (CŒUR)** : 15 min — valide les multiblocs principaux
- **Niveau 3 (PIPELINE)** : 30 min — fait tourner la chaîne
- **Niveau 4 (END-TO-END)** : 30 min — injection + Age 5

**Si tu n'as que 30 min → fais Niveau 1 + 2 + un peu de 3.**

---

## 🚨 NIVEAU 1 : SANITY CHECK CRITIQUE (5 min)

Si UN seul de ces tests échoue, **arrête tout** et signale-moi le bug. Tout le reste dépend de ces fondations.

### 1.1 Build Java compile

```bash
cd "/c/Users/lamar/curseforge/minecraft/Instances/Nexus Absolu"
git pull
bash mod-source/build.sh
```

**Attendu** : `=== BUILD SUCCESSFUL ===` à la fin.

**Si échec** :
- Si erreur sur `ManifoldTeleporter.java` → mes 151 lignes ont un bug, copie le message d'erreur
- Si erreur sur `ManifoldEffectHandler.java` ligne 239 → mon hook est mal greffé
- Si erreur sur `ItemCartoucheManifold.java` ligne 106 → mon hook startup est mal greffé
- Si erreur d'encodage → ajouter `-encoding UTF-8` dans javac

### 1.2 Game boot sans crash

Lance le jeu via CurseForge launcher. **Attendu** : tu arrives à l'écran principal Minecraft sans crash.

**Si crash** :
- Vérifier `logs/latest.log` pour stack trace
- Crashes typiques :
  - `NoClassDefFoundError: ManifoldTeleporter` → build.sh n'a pas inclus le nouveau fichier
  - `JsonSyntaxException` → un Patchouli ou recipe JSON est cassé
  - `NullPointerException` au load → un controller multibloc référence un block_id inexistant

### 1.3 World loadable

Charge un monde existant ou crée-en un nouveau. **Attendu** : tu spawn dans le monde, pas de crash, pas de erreur "missing block" massive.

**Si crash au load** :
- DIM 145 (nouvelle dimension Age 5) crée le crash → vérifier `config/justenoughdimensions/dimensions.json`

---

## ⚙️ NIVEAU 2 : VALIDATION DU CŒUR (15 min)

### 2.1 JEI affiche les nouveaux items

Ouvre l'inventaire créatif et cherche :
- `cartouche_manifold` (1 item, le Saint Graal de l'Age 4)
- `cartouche_chargee` (intermédiaire pre-armement)
- `cristal_manifoldine` (Phase 5 manifoldine)
- `compose_alpha` à `compose_delta` (6 composés M1)
- `casing_titane_iridium`
- `tryptamide_m_capsule`

**Attendu** : tous présents avec leurs textures HD que j'ai générées (fioles colorées, hexagonal pourpre, anneau, etc.)

**Si textures roses/blanches** : la texture n'est pas trouvée → check le path `assets/nexusabsolu/textures/items/`

### 2.2 Ouvrir le Carnet Voss

Donne-toi en créatif `nexusabsolu:carnet_voss_v4` (ou Patchouli book voss_codex). Ouvre-le.

**Attendu** : 3 catégories visibles :
- "Le Portail Voss" (legacy)
- "Carnet Voss Vol. IV" (lore Phase 6)
- "Carnet Voss Vol. V — Atlas Industriel" (guide Phase 7)

**Si crash sur ouverture** : un JSON Patchouli est cassé → check `logs/latest.log`.

### 2.3 Test rendu 3D Patchouli (Phase 8 critique)

Ouvre **Vol V** → "L6 Acides + Ammoniaque" → page 4 (multiblock MB-HABER).

**Attendu** : tu vois **un rendu 3D du multibloc HABER** que tu peux faire tourner avec la souris. En maintenant Shift, tu vois couche par couche.

**Si page vide ou crash** : le multiblock_id `nexusabsolu:haber_reactor` est mal référencé OU les 25 fichiers `multiblocks/*.json` ne sont pas chargés.

**Test additionnel** : essaie page 5 (OSTWALD) et page 6 (CONTACT) pour confirmer que les autres marchent.

### 2.4 Construire un multibloc test (le plus simple)

Choisis **MB-AROMATIC** (3×3×3 standard, le plus simple). En créatif :

- Pose le **controller** `aromatic_reactor_controller` au sol
- Suis le layout du book pour placer 26 casings autour

Active la machine via clic-droit sur le controller. **Attendu** : il dit "Multibloc reconnu" ou similaire (selon Modular Machinery).

**Si "structure invalide"** : le layout MM JSON ne correspond pas à ce que je t'ai fait construire — je dois corriger soit le book soit le JSON.

---

## 🏭 NIVEAU 3 : PIPELINE PARTIEL (30 min)

### 3.1 Recipe MM fonctionne

Dans le MB-AROMATIC reconnu, mets les inputs (via Item Bus + Fluid Hatch) :
- 1 dust naphta + 1000mB NH3 + 1 catalyseur Pt-Al2O3
- Branche RF (Energy Input) avec une source RF infinie créatif (RFTools Powercell etc.)

**Attendu** : la machine consomme énergie + inputs et produit de l'**indole_dust** (recette `indole_synthesis`).

**Si bloqué** : 
- "missing requirement" → un input ne match pas le recipe JSON
- "no output slot" → l'item bus output manque
- Pas de progression : RF/t insuffisant ou item bus mal placé

### 3.2 Recipe ZS (CraftTweaker) marche

Test une recette ZS simple : **Bronze + Invar → Invarium** (Alloy Kiln, recette de base).

**Attendu** : la recette apparaît en JEI et fonctionne in-game.

**Si pas en JEI** : un script ZS a une erreur de parsing → check `crafttweaker.log`.

### 3.3 Le craft shaped final marche

Crafte un **`cartouche_chargee`** en table de craft 3×3 (recette shaped Phase 4) :

```
α  Tryp  β
γ2 Cas  γ3
δ  Mtx  γ1
```

(α/β/γ/δ = composés ; Tryp = tryptamide_m_capsule ; Cas = casing_titane_iridium ; Mtx = matrix_pigmentary)

**Attendu** : `cartouche_chargee` apparaît dans le slot output.

**Si pas crafté** : la recette JSON dans `config/modularmachinery/recipes/cartouche_chargee_assembly.json` ou similaire a un problème.

---

## 🎯 NIVEAU 4 : END-TO-END (30 min)

### 4.1 Construire le Bio-Réacteur (M2)

Le Bio-Réacteur (5×5×3 = 83 blocs) existe déjà avant cette session. **Attendu** : tu peux le construire et il marche.

### 4.2 Recette `bio_solution_epsilon_synthesis`

Mets en input du Bio-Réacteur :
- compose_alpha + compose_beta + compose_gamma3 + compose_delta + 500mB argon

**Attendu** : production de `solution_epsilon` (1000mB).

### 4.3 Recette `bio_cartouche_manifold_armement` ⭐

Mets en input :
- 1× cartouche_chargee + 1000mB solution_epsilon + 4000mB liquid_starlight + 500mB argon

**Attendu** : après 30s à 100k RF/t (60M RF total), tu obtiens **1× `cartouche_manifold`** ⭐

**Si bloqué** : la recette `bio_cartouche_manifold_armement.json` a un problème.

### 4.4 Injection ⭐⭐⭐

**LE TEST FINAL** :

1. Place-toi en sécurité (eau, mur, tu vas être paralysé pendant 1 min de crash)
2. Right-click avec la `cartouche_manifold` en main
3. **Attendu** : trip 8 minutes en 9 stages (Onset → Saturation → Geometric → Hyperspace → PEAK → retour → Crash)
4. **À la fin du crash (8:00)** : **téléportation automatique vers DIM 145 (Age 5)**

**Si pas de téléportation à la fin** :
- Vérifier que `ManifoldTeleporter.onTripEnd()` est bien appelé (logs)
- Vérifier que NBT_AGE5_PENDING est bien set par `markFirstInjection()`
- Vérifier que DIM 145 charge correctement

**Si téléportation crash** :
- DIM 145 mal configurée (JED dimensions.json)
- `PlayerList.transferPlayerToDimension` plante (pas de `WorldServer` chargé pour DIM 145)

### 4.5 Test du retour à l'overworld

Une fois en Age 5, fais `/tp 0` ou un portail. **Attendu** : tu peux revenir à DIM 0 (overworld original) sans souci.

**Test bonus** : right-click une 2ème cartouche_manifold. **Attendu** : trip normal SANS téléportation (la 1ère fois est marquée NBT_AGE5_UNLOCKED).

---

## 📋 Si tu trouves un bug

1. **Note précisément** :
   - À quel niveau (1.1, 2.3, 4.4...)
   - Ce que tu attendais
   - Ce que tu as eu (message d'erreur / comportement)
   - La stack trace complète si crash
2. **Reviens me voir** avec ces infos
3. Je fixe immédiatement

## ✅ Si tout passe

Tu as un **Age 4 100% validé en jeu**. Tu peux :
- Publier sur CurseForge
- Annoncer la beta à des testeurs
- Continuer Phase 10+ sans dette technique

---

## 🎲 Cas d'usage rapide (juste valider que rien n'est cassé)

Si tu veux JUSTE valider que les 71 commits ne cassent rien (sans tester l'Age 4 en profondeur), fais :
1. Niveau 1.1 (build) ✅
2. Niveau 1.2 (boot) ✅  
3. Niveau 1.3 (world load) ✅
4. Niveau 2.1 (JEI items présents) ✅

**Ça prend 5-10 min. Si ces 4 passent, le travail de cette session ne casse rien d'existant.**

— *Bonne chance ! Je suis là pour fix au moindre problème.*
