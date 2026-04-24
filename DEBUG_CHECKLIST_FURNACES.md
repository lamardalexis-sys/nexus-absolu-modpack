# Checklist Debug Fours Voss — v1.0.297

**Objectif** : valider que toutes les features récentes fonctionnent avant
de démarrer les Archives Voss.

Coche les cases au fur et à mesure de ton test. Si un test échoue, note
le comportement observé dans le bloc "Bugs trouvés".

---

## ✅ Test 1 — Alignement visuel des slots (v1.0.279, v1.0.280)

**Tier 0 (Iron Furnace)** :
- [ ] Pose un Iron Furnace, ouvre le GUI
- [ ] Les items mis dans les slots Input/Output/Fuel sont **pixel-perfect**
      centrés dans leurs cadres
- [ ] La progress arrow remplit **exactement** le cadre dessiné (pas de
      débordement violet à droite, pas de queue sombre coupée)

**Tiers I à IV (carte IO installée)** :
- [ ] Ajoute Extension I/O I dans un Gold Furnace → GUI montre 3 slots input
- [ ] Les items dans les slots sont parfaitement alignés avec les cadres
- [ ] Fais pareil avec Extension II (5 slots), III (7 slots), IV (9 slots)
- [ ] Progress arrow alignée sur toutes les versions

---

## ✅ Test 2 — Auto-sort ON vs OFF (v1.0.279, v1.0.286)

- [ ] Pose un Gold Furnace + Extension I/O I, mode auto-sort **ON**
- [ ] Mets du minerai dans les 3 inputs → tous cuisent **en parallèle**
- [ ] Bascule auto-sort **OFF** → seul le slot 0 cuit, les autres attendent
- [ ] Retire l'item du slot 0 → le slot 1 prend le relais tout seul
- [ ] Check que le tooltip RF affiche "sequentiel" en OFF, "auto-sort" en ON

---

## ✅ Test 3 — Multiplier conso auto-sort (v1.0.286)

- [ ] Dark Astral + Extension I/O IV + 0 booster + RF mode + auto-sort ON
- [ ] Tooltip RF affiche :
  - Conso/paire : 300 RF/t (base Dark Astral)
  - Conso max : **1800 RF/t** (= 300 × 6)
  - Rouge (auto-sort ON)
- [ ] Désactive auto-sort → Conso max devient **grise** (indicative)
- [ ] Autonomie affiche "Autonomie (auto-sort)" quand ON, "Autonomie (sequentiel)" quand OFF

---

## ✅ Test 4 — Speed multiplier dynamique (v1.0.289)

- [ ] Pose un Pallanutro vide, pas de booster → affichage sur GUI : **x56.0**
- [ ] Ajoute 4 Speed Boosters → affichage passe à ~**x200** (plafonné ou proche)
- [ ] Retire 3 boosters (garde 1) → affichage redescend dynamiquement
- [ ] Teste sur Dark Astral :
  - 0 booster : x6.0
  - 4 boosters : ~x14
  - 8 boosters : ~x22

---

## ✅ Test 5 — Multi-items par tick sur Pallanutro/Infinite (v1.0.290, v1.0.291)

**Pallanutro** :
- [ ] Pose Pallanutro + Extension IV + 8 Speed Boosters + auto-sort ON
- [ ] Alimente avec RF infinie (Draconic Energy Core / Creative Energy)
- [ ] Remplis tous les inputs avec 1 stack de minerai smeltable chacun
- [ ] Chronomètre combien de temps met 1 stack complet à cuire
- [ ] Attendu : ~**1 seconde** pour cuire un stack de 64 (environ 64 items/sec)

**Infinite** (si le T9 existe — sinon skip) :
- [ ] Si Infinite Furnace existe : mêmes conditions, devrait cuire un stack en **~0.5 seconde**
- [ ] **IMPORTANT** : Infinite doit être **~1.8× plus rapide** que Pallanutro

---

## ✅ Test 6 — Max Receive RF illimité (v1.0.282)

- [ ] Pose un Pallanutro
- [ ] Connecte un câble Mekanism Ultimate (102k RF/t max) directement
- [ ] Vérifie que l'énergie se remplit en **< 1 seconde** (pas 67 minutes)
- [ ] Même test avec Infinite (si existe) : remplissage quasi-instantané
- [ ] Teste avec Flux Networks : idem

---

## ✅ Test 7 — Config IO panel : layout + labels (v1.0.288, v1.0.292, v1.0.293)

**Sur un Four Voss** :
- [ ] Ouvre Config IO
- [ ] Layout visuel :
  ```
           [H]
    [Ga] [Av] [Dr]
    [Ar] [Ba]
  ```
- [ ] Hover chaque bouton : tooltip affiche "Haut/Bas/Gauche/Droite/Avant/Arrière"
- [ ] Clic gauche cycle None → In → Out → Both
- [ ] Shift+clic toggle Fuel IN (logique historique préservée)

**Sur Convertisseur / Machine Humaine / KRDA** :
- [ ] Ouvre chaque GUI, vérifie que le layout est **identique** au four
- [ ] Même position, même labels, même comportement

---

## ✅ Test 8 — Blocage Fourneaux Mystical Agriculture (v1.0.297)

- [ ] Crafte un **Inferium Furnace** (MA normal)
- [ ] Pose-le
- [ ] Clic droit dessus → **rien ne s'ouvre**
- [ ] Actionbar affiche en rouge : "Désactivé dans Nexus Absolu - utilisez les Fourneaux Voss"
- [ ] Attends 2 secondes, re-clic → message réapparaît (throttle 1.5s passed)
- [ ] Spam-clique → message ne réapparaît pas plus d'1× par 1.5s
- [ ] **Important** : tu peux toujours casser le bloc (clic gauche)
- [ ] Répète pour les 5 autres : Prudentium, Intermedium, Superium, Supremium, Ultimate

---

## ✅ Test 9 — Nouvelles recettes (v1.0.275 à v1.0.296)

**Fourneaux (T1 à T8)** :
- [ ] Recette Iron Furnace = iron/cobble/furnace vanilla + redstone ✓ (v1.0.275)
- [ ] Recette Gold Furnace = gold + Kiln Brick IE + Gold Gear TF + Iron Furnace ✓
- [ ] Recette Invarium Furnace = compressed steel + machine frame + vossium ingot ✓
- [ ] Recette Vossium IV = Intermedium Essence + Tuosss + Apprentice Blood Orb + Emeradic Furnace ✓
- [ ] Recette Dark Astral = Dark Soularium + Superium + celestial crystal + draconium block ✓
- [ ] Recette Gaia Ludicrite = Supremium + terrasteel + pixie_dust + gaia_spirit + ludicrite ✓
- [ ] Recette Pallanutro = Palladium + Insanium + Neutronium + Awakened Core ✓

**Upgrades IO Expansion** :
- [ ] Expansion I : recette inchangée depuis v1.0.246
- [ ] Expansion II : utilise **Composé C** (pas A) ✓ (v1.0.294)
- [ ] Expansion III : Composé E + Vossium IV ✓ (v1.0.295)
- [ ] Expansion IV : Tough Alloy + Manasteel + QuantumStorage + Tuosss + Resonating Gem ✓ (v1.0.296)

**Test JEI** : toutes les recettes s'affichent **sans slot vide/air**.

---

## ✅ Test 10 — Stress test production longue durée

**Le test qui révèle les bugs insidieux** :

- [ ] Pose 1× Pallanutro + 8 boosters + 1× Carte Efficience
- [ ] Extension IV + auto-sort ON
- [ ] 9 ItemInputs extérieurs alimentent continuellement (chunks loadés, items infinis via Creative ou farms)
- [ ] 9 ItemOutputs déposent dans des coffres
- [ ] Laisse tourner **30 minutes** minimum
- [ ] Check :
  - [ ] Aucun crash
  - [ ] Aucun item perdu (coffres reçoivent bien tous les smelts)
  - [ ] RF reste stable (câble suffit pour l'input continu)
  - [ ] Pas de fuite mémoire visible (F3 MB usage stable)
  - [ ] Sauvegarde → reload le monde → le four reprend où il en était

---

## Bugs trouvés (à remplir pendant tes tests)

| # Test | Comportement observé | Attendu | Notes |
|---|---|---|---|
| | | | |
| | | | |
| | | | |

---

## Priorité pour fixer

Si tu trouves des bugs :

**Priorité HAUTE** (doit être fixé avant Archives Voss) :
- Crashes
- Items perdus / dupliqués
- Save/load broken
- GUI totalement cassé

**Priorité MOYENNE** (à planifier) :
- Bugs visuels mineurs (pixels off, couleurs)
- Tooltips incorrects
- Performance (lag > 1s latence)

**Priorité BASSE** (acceptable) :
- Texte mal formaté mais lisible
- Détails cosmétiques

---

## Feu vert Archives Voss

Quand tous les tests ci-dessus passent :
- [ ] ✅ 10/10 tests OK
- [ ] ✅ Aucun bug priorité HAUTE
- [ ] ✅ Backup du monde test fait
- [ ] ✅ Alexis confirme "GO Archives Voss"

→ On crée la branche `feature/archives-voss` et on démarre Sprint 1.
