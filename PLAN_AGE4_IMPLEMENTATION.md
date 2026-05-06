# PLAN_AGE4_IMPLEMENTATION.md

> Roadmap d'implémentation complète Age 4 : Cartouche Manifold.
> Source du design : `docs/age4-cartouche-manifold/` (5400 lignes, 8 lignes L1-L8).
> Date début : 2026-05-06.

---

## 🎯 Objectif final

Le joueur doit pouvoir crafter la **Cartouche Manifold** (`nexusabsolu:cartouche_manifold`) en passant par les 8 lignes parallèles, ~50 recettes MM, 30 éléments + 16 champis Botania → 10 composés intermédiaires → 2 outputs → encartouchage final.

---

## 📊 État actuel (avant cette session)

| Composant | État | Détails |
|---|---|---|
| Item Cartouche + trip 8 min | ✅ FAIT | Java mod, Centinela music, 9 stages, visuels, shaders |
| `Age4_Manifold_Content.zs` | ✅ FAIT | ~50 fluides + ~70 items custom CT |
| Multiblocs MM existants | ⚠️ PARTIEL | bioreacteur, cryo_distillateur, alloy_furnace, iron_centrifuge, power_transformer (5/14) |
| Recettes MM existantes | ⚠️ PARTIEL | air_intake, air_to_argon/n2/o2, solution_epsilon, cartouche_manifold_armement (5/~50) |
| **Recettes ZS L1-L8** | ❌ ABSENT | rien implémenté |
| Cinématique Age 4→5 | ❌ ABSENT | dimension Age 5 non créée |
| Quêtes BQ Phases 2-6 | ❌ ABSENT | Phase 0+1 OK (16/80) |

---

## 🛠 Multiblocs MM nécessaires (manquants)

D'après les fichiers `lines/L*.md` :

### Lignes principales (1 multibloc majeur par ligne)

| ID | Multibloc | Ligne | Layout | Status |
|---|---|---|---|---|
| MB-DESA | Vacuum Chamber (désaération) | L1 | TBD | ❌ |
| MB-HDS | Hydrodésulfuration | L1 | TBD | ❌ |
| MB-HYDROCRACK | Hydrocrackeur | L1 | TBD | ❌ |
| MB-OSMOSE | Osmose Inverse | L2 | TBD | ❌ |
| MB-DISTILL | Distillation Tridistillée | L2 | TBD | ❌ |
| MB-CRYO-ATM | Cryo-Distillation Atm | L3 | ✅ existe (cryo_distillateur) | ⚠️ recettes manquantes |
| MB-ELECTRO-CK | Castner-Kellner | L3 | TBD | ❌ |
| MB-HALL | Hall-Héroult (Al) | L4 | TBD | ❌ |
| MB-KROLL | Kroll (Ti) | L4 | TBD | ❌ |
| MB-AQUA-REGIA | Eau Régale (Au) | L4 | TBD | ❌ |
| MB-FISSION | Fission Reactor (NC bridge) | L5 | external | ❌ |
| MB-HABER | Haber-Bosch (NH₃) | L6 | TBD | ❌ |
| MB-OSTWALD | Ostwald (HNO₃) | L6 | TBD | ❌ |
| MB-CONTACT | Contact (H₂SO₄) | L6 | TBD | ❌ |
| MB-CUMENE | Cumène 4 phases | L7 | TBD | ❌ |
| MB-TRYPTAMIDE | Tryptamide-M synthesis | L7 | TBD | ❌ |
| MB-ALCHEMY | Alchemy Chamber (extraction acide) | L8 | TBD | ❌ |
| MB-ALAMBIC | Alambic Manaïque (5x4x5) | L8 | TBD | ❌ |
| MB-SOXHLET | Soxhlet Extractor (3x5x3) | L8 | TBD | ❌ |
| MB-CYCLO | Cyclisateur Stellaire (5x6x5) ⭐⭐⭐ | L8 | TBD | ❌ |
| MB-EVAPORATOR | Évaporateur (cristallisation) | L8/L6 | TBD | ❌ |
| MB-MANA-ENCHANTER | Mana Enchanter (3x3x3) | L8 | TBD | ❌ |
| MB-MELANGEUR | M1 Mélangeur Cryogénique | Final | TBD | ❌ |

**Total** : ~22 multiblocs custom à créer. Le **bioreacteur** existant sert pour M2.

---

## 🎬 Plan d'attaque (ordre d'implémentation)

### 🟢 PHASE 1 — Pilote : débloquer le `cristal_manifoldine` (L8.C)

**Objectif** : prouver le pattern + débloquer immédiatement le KEY ITEM.

#### 1.1 Recettes ZS L8.C (chaîne Manifoldine) — **MODE TEMPORAIRE**

Créer `scripts/Age4_L8_Manifoldine.zs` avec recettes shaped/Mekanism (en attendant les multiblocs custom). Permet immédiatement :
- 8x Spores Activées via furnace
- Manifoldine Extract Purified via Mekanism PRC ou shapeless
- Manifoldine Brute via Astral Sorcery Lightwell + recette shaped (proxy nuit)
- Cristal Manifoldine via évaporation (recette shaped)

#### 1.2 Test : valider que le craft cartouche_manifold fonctionne maintenant

→ Si oui, on a une **Cartouche craftable end-to-end** (rough).

### 🟡 PHASE 2 — Multiblocs L8 (versions propres)

Remplacer les recettes temporaires par de vrais multiblocs MM custom :

#### 2.1 MB-SOXHLET (3x5x3)
- JSON dans `config/modularmachinery/machinery/soxhlet_extractor.json`
- Block controller via ContentTweaker `createBlock`
- Recettes JSON dans `config/modularmachinery/recipes/`

#### 2.2 MB-CYCLO Cyclisateur Stellaire (5x6x5) ⭐⭐⭐
- Avec requirements : nuit + ciel ouvert + dimension overworld
- Effet visuel particulier (starlight beam la nuit)

#### 2.3 MB-EVAPORATOR (cristallisation)

#### 2.4 MB-ALAMBIC Manaïque (5x4x5)
- Intégration Botania mana via ManaPool checking (KubeJS event)

#### 2.5 MB-MANA-ENCHANTER (3x3x3)

### 🟠 PHASE 3 — Lignes L1-L7 (~50 recettes ZS)

Une session par ligne (méthodologie superpowers : design + small task + verify + commit) :

- [ ] **L1 Pétrochimie** : `scripts/Age4_L1_Petrochimie.zs` (HDS, Hydrocrack, Solvant α)
- [ ] **L2 Hydro-eau** : `scripts/Age4_L2_HydroEau.zs` (osmose, distill, tritium)
- [ ] **L3 Électrolyse-cryo** : `scripts/Age4_L3_ElectrolyseCryo.zs` (Castner-Kellner, électrolyse)
  - Cryo-distillation déjà branchée, juste compléter
- [ ] **L4 Pyrométallurgie** : `scripts/Age4_L4_Pyrometallurgie.zs` (Hall, Kroll, Aqua Regia, H₃PO₄)
- [ ] **L5 Nucléaire** : `scripts/Age4_L5_Nucleaire.zs` (γ1/γ2/γ3, Mycélium Activation)
- [ ] **L6 Acides-ammoniaque** : `scripts/Age4_L6_Acides.zs` (Haber-Bosch, Ostwald, Contact)
- [ ] **L7 Organique-acétone** : `scripts/Age4_L7_Organique.zs` (Cumène 4 phases, Tryptamide-M)

Les 9-15 multiblocs custom de ces lignes seront créés au fur et à mesure (JSON MM + Block ContentTweaker).

### 🟣 PHASE 4 — Craft final + cinématique Age 5

#### 4.1 M1 Mélangeur Cryogénique
- Multibloc qui mixe les 10 composés α/β/γ/δ/ε en Solution Pré-Manifold

#### 4.2 M2 Bio-Réacteur Manifold étendu
- Solution + Essence Chromatique + Manifoldine → Sérum Manifold

#### 4.3 Encartouchage final
- Sérum + Casing Titane + Argon → Cartouche Manifold

#### 4.4 Cinématique Age 5
- Dimension Age 5 (registry, dimension type)
- Fin du trip 8min : téléport joueur vers dimension Age 5
- "Réveil" lore : effet de simulation qui s'effondre

### 🔵 PHASE 5 — Quêtes BQ Phases 2-6 (64 quêtes)

Documentées dans `docs/age4-cartouche-manifold/quetes-phase2.md` à `quetes-phase6.md` (~2400 lignes).
Génération via `scripts-tools/generate_age4_quests.py` (existe).

### 🟤 PHASE 6 — Polish

- Carnet Voss Vol IV (Patchouli book)
- Textures 70 items custom (placeholder violet/noir actuellement)
- Sons ambient supplémentaires stages 1-4

---

## ✅ Suivi (à mettre à jour à chaque commit)

### Phase 1 — Pilote
- [ ] 1.1 Recettes L8.C temporaires
- [ ] 1.2 Test cristal_manifoldine craftable
- [ ] 1.3 Test cartouche_manifold craftable end-to-end

### Phase 2 — Multiblocs L8
- [ ] 2.1 MB-SOXHLET
- [ ] 2.2 MB-CYCLO
- [ ] 2.3 MB-EVAPORATOR
- [ ] 2.4 MB-ALAMBIC
- [ ] 2.5 MB-MANA-ENCHANTER

### Phase 3 — Lignes
- [ ] 3.1 L1 Pétrochimie
- [ ] 3.2 L2 Hydro-eau
- [ ] 3.3 L3 Électrolyse-cryo
- [ ] 3.4 L4 Pyrométallurgie
- [ ] 3.5 L5 Nucléaire
- [ ] 3.6 L6 Acides-ammoniaque
- [ ] 3.7 L7 Organique-acétone

### Phase 4 — Final
- [ ] 4.1 M1 Mélangeur Cryogénique
- [ ] 4.2 M2 Bio-Réacteur étendu
- [ ] 4.3 Encartouchage
- [ ] 4.4 Dimension Age 5 + cinématique

### Phase 5 — Quêtes
- [ ] 5.1 Phase 2 BQ
- [ ] 5.2 Phase 3 BQ
- [ ] 5.3 Phase 4 BQ
- [ ] 5.4 Phase 5 BQ
- [ ] 5.5 Phase 6 BQ

### Phase 6 — Polish
- [ ] 6.1 Carnet Voss Vol IV
- [ ] 6.2 Textures items
- [ ] 6.3 Sons ambient

---

## 🔧 Méthodologie de travail

À chaque commit, suivre **superpowers** :
1. Design avant code (lire le `.md` de la ligne ciblée)
2. Plan détaillé avec exact files
3. Small tasks (2-5 min)
4. Verify before commit (syntax + ASCII-safe + tests)
5. Code review systématique

Conventions :
- Fichiers ZS : `scripts/Age4_LX_*.zs`
- Fichiers MM JSON : `config/modularmachinery/machinery/*.json` + `config/modularmachinery/recipes/*.json`
- Blocks CT : dans `scripts/Age4_Manifold_Content.zs` ou nouveau `Age4_Multiblocs.zs`
- Pas d'accent dans le code (ASCII-safe Java)
- Méthode commit : 1 ligne logique = 1 commit

---

*Plan vivant - mis à jour à chaque étape complétée.*
