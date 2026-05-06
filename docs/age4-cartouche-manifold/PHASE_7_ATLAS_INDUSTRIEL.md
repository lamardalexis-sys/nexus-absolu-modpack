# Phase 7 — Atlas Industriel (Patchouli Vol V)

> Date : Mai 2026
> Statut : ✅ **COMPLET**

---

## 🎯 Résumé

Création du **Carnet Voss Vol V — Atlas Industriel**, le guide step-by-step pour construire les 25 multiblocs de l'Age 4. Patchouli book existant (`voss_codex`) étendu avec 1 nouvelle catégorie + 11 entries (35 pages au total) en FR et EN.

---

## 📚 Structure du Vol V (`vol_v` category)

| # | Entry | Pages | Couverture |
|---|---|---|---|
| 1 | `intro_guide` | 4 | Comment lire les layouts, hatches obligatoires, notation casings |
| 2 | `ligne_l1_petrochimie` | 2 | MB-DESA (3×3×3) + MB-HDS (3×8×3) |
| 3 | `ligne_l2_hydro` | 2 | MB-OSMOSE + MB-TRITIUM (besoin reactor NC adjacent) |
| 4 | `ligne_l3_electrolyse` | 3 | MB-CK + MB-FOUR-ELEC (5×4×5 HT 1500°C) + MB-FLUORITE |
| 5 | `ligne_l4_pyrometallurgie` | 3 | MB-HALL + MB-KROLL (4×5×4 étroit) + MB-AQUA-REGIA |
| 6 | `ligne_l5_nucleaire` | 2 | MB-GAMMA-FORGE + MB-LIT-CHAMBER |
| 7 | `ligne_l6_acides` | 3 | MB-HABER (5×5×5 HUB) + MB-OSTWALD + MB-CONTACT |
| 8 | `ligne_l7_organique` | 3 | MB-CRACKER + MB-CUMENE + MB-AROMATIC ⭐ + MB-FERMENTER |
| 9 | `ligne_l8_botanique` | 3 | MB-SOXHLET + MB-CYCLO ⭐⭐⭐ + MB-EVAPORATOR + MB-ALAMBIC + MB-MANA-ENCHANTER |
| 10 | `machines_finales_m1_m2` | 3 | M1 Mélangeur Cryogénique (5×4×5) + M2 Bio-Réacteur |
| 11 | `ordre_montage_optimal` | 7 | Stratégie 5 étapes pour construire dans l'ordre des dépendances |

---

## 🗺️ Stratégie de construction (entry `ordre_montage_optimal`)

L'entry la plus longue (7 pages) propose un ordre optimal qui minimise les blocages :

### Étape 1 — Bases (L2 + L3) — 4 multiblocs
1. Cryo-Distillateur (N₂/O₂/Ar pour tout)
2. MB-OSMOSE (Tridistilled water)
3. MB-CK Castner-Kellner (Na/Cl/NaOH)
4. Setup Mekanism Electrolytic Separator (heavywater)

### Étape 2 — Hub Acides (L6) — 3 multiblocs
5. MB-HABER (NH₃ pivot ⭐)
6. MB-OSTWALD (HNO₃)
7. MB-CONTACT (Claus + H₂SO₄)

### Étape 3 — Métaux + Pétrochimie — 6 multiblocs (parallèle)
8-13. MB-DESA, MB-HDS, MB-HALL, MB-KROLL, MB-AQUA-REGIA, MB-FOUR-ELEC, MB-FLUORITE

### Étape 4 — Organique + Nucléaire — 7 multiblocs (parallèle)
14-20. MB-CRACKER, MB-CUMENE, MB-AROMATIC, MB-FERMENTER, MB-TRITIUM, MB-GAMMA-FORGE, MB-LIT-CHAMBER + setup Reactor NuclearCraft

### Étape 5 — Manifoldine + Final — 5 multiblocs
21-26. MB-EVAPORATOR, MB-SOXHLET, MB-CYCLISATEUR, MB-ALAMBIC, MB-MANA-ENCHANTER, M1 Mélangeur Cryogénique + M2 Bio-Réacteur (déjà en place)

---

## 🎨 Style narratif

Chaque entry utilise le format Patchouli avec codes :
- `$(b)bold$(/b)` pour souligner les noms de machines
- `$(d)magenta$(/d)` pour les outputs critiques
- `$(c)red$(/c)` pour les avertissements
- `$(o)italic$(/o)` pour les commentaires de Voss
- `$(li)bullet$(/li)` pour les listes

Tonalité : technique précis (layouts) + pédagogique (explications) + Voss (commentaires italiques) + dark humor (warnings).

---

## ✅ Bilan global Patchouli `voss_codex` après Phase 7

| Catégorie | Entries | Description |
|---|---|---|
| `portail` (legacy) | 1 | Plans du Portail dimensionnel (existant avant) |
| `vol_iv` | 8 | Lore + 5 théorèmes + cartouche (Phase 6) |
| `vol_v` | 11 | **Atlas Industriel** -- guide construction (Phase 7) |
| **TOTAL** | **20 entries** | **~80 pages de lore + tutoriels** |

Le book est maintenant **un compagnon complet** pour l'Age 4 :
- **Vol IV** = pourquoi tu fais ça (lore narratif)
- **Vol V** = comment tu fais ça (guide pratique)

---

## 🔗 Synergies avec autres phases

- Synergie **Phase 5 BQ** : les quêtes BQ peuvent référencer ce book via `task_advancement` ou tooltip `Patchouli`
- Synergie **Phase 6 textures** : les icons des entries utilisent les nouvelles textures (cartouche_manifold, etc.)
- Synergie **Phase 4 Java** : l'entry `epilogue` du Vol IV décrit la téléportation Age 5 implémentée par `ManifoldTeleporter.java`

---

## 🚀 Extensions futures possibles

- **Patchouli `multiblock` type** : créer fichiers JSON dans `multiblocks/` pour render 3D in-game (rotation, layer-by-layer). Demande réécrire 25 layouts au format Patchouli (différent du format MM JSON).
- **Spotlight** sur items custom (`type: "patchouli:spotlight"`) avec recipe complète
- **Cross-references** entre entries (`type: "patchouli:link"`) pour navigation fluide entre L1→L6→L8 etc.
- **Recipe pages** intégrées (`type: "patchouli:crafting"`) pour les shaped recipes Phase 4
