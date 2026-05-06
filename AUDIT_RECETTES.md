# AUDIT_RECETTES.md — Nexus Absolu

> Audit systématique des recettes CraftTweaker et identification des items orphelins.
> Date : 2026-05-06
> Sandbox audit : `/home/claude/nexus/`
> Méthode : analyse Python tous les `scripts/*.zs` + cross-référence Java mod (`mod-source/src/main/java`) + configs Modular Machinery + ContentTweaker.

---

## 1. STATISTIQUES GLOBALES

- **Total fichiers `.zs`** : 20
- **Total lignes** : 2 682
- **Items custom NA référencés** : 101
- **Items custom NA en output (créés par recette)** : 88 + 9 (furnaces/machines) = 97
- **Items custom NA en input ET output** : 88 (cohérents)
- **Items "input-only" identifiés** : 13

---

## 2. ITEMS "INPUT-ONLY" — ANALYSE DÉTAILLÉE

### 2.1 — FAUX POSITIFS (légitimement obtenus par drops/sieve)

Source : `mod-source/src/main/java/com/nexusabsolu/mod/scavenging/ScavengeEventHandler.java`

| Item | Source | OK ? |
|---|---|---|
| `cobblestone_fragment` | Pioche Fragmentée 30% drop | ✅ |
| `iron_grit` | Pioche Renforcée 12%, Pioche Ferrée 40% | ✅ |
| `copper_grit` | Pioche Renforcée 13%, Pioche Cuivrée 35% | ✅ |
| `tin_grit` | Pioche Renforcée 10%, Pioche Cuivrée 30% | ✅ |
| `nickel_grit` | Pioche Renforcée 5%, Pioche Cuivrée 25% | ✅ |
| `silver_grit` | Pioche Ferrée 25% | ✅ |
| `lead_grit` | Pioche Ferrée 25% | ✅ |
| `gold_grit` | Pioche Précieuse 45% | ✅ |
| `osmium_grit` | Pioche Précieuse 40% | ✅ |
| `compose_a` | Pioche Vossium 60%, Pioche Renforcée Age 2 10% | ✅ |
| `compose_b` | Pioche Steelium 15% | ✅ |

**11 items/13 sont correctement obtenables.**

### 2.2 — VRAIS ORPHELINS (NON-OBTENABLES)

#### 🔴 Problème 1 : `compose_e`
- **Tier endgame Age 2** (le plus haut tier des composes)
- **Utilisé en input** :
  - `Age0_Scavenging.zs:233` → recette `compose_block_e` (9× compose_e → bloc)
  - `Age_Furnaces.zs:196` → recette `upgrade_io_expansion_3` (4× compose_e + vossium IV)
- **Source** : **AUCUNE** (pas de drop, pas de recette)
- **Impact** : Tier upgrade III impossible → progression endgame Age 2 cassée

#### 🔴 Problème 2 : `signalhee_ingot`
- **Composant intermédiaire** pour produire `compose_c`
- **Utilisé en input** :
  - `Age1_Signalhee.zs:13/21/29` → input des 3 Smelters (TE/IE/EnderIO) pour compose_c
  - `Age1_Portal.zs:33` → composant du portail Voss
- **Source attendue** (commentaire ligne 6) : 
  - `Signalum + Diarrhee + Bio-E -> [KRDA125] -> Signalhee`
- **Source réelle** : **AUCUNE** (machine KRDA125 jamais implémentée)
- **Impact** : compose_c impossible → toute la chaîne Age 1 vossium III/IV cassée

#### 🔴 Problème 3 : `diarrhee_liquide` (fluid)
- **Fluide custom** utilisé pour la clé 9x9
- **Utilisé en input** :
  - `Age1_Signalhee.zs:41` → 1000mB pour craft `compact_key_9x9`
- **Source attendue** (commentaire ligne 5) :
  - `Food + Eau + Bio-E -> [Diarh33] -> Diarrhee Liquide`
- **Source réelle** : **AUCUNE** (machine Diarh33 jamais implémentée)
- **Impact** : Clé 9x9 impossible → progression Age 1 → Age 2 bloquée

---

## 3. BUG CONNU (déjà commenté)

### `<gas:argon>` dans Age4_Recipes.zs

- **Lignes 44-52** : recette PRC Mekanism utilisant `<gas:argon>` (gaz inexistant)
- **Status** : **DÉJÀ COMMENTÉE** (`//` au début de chaque ligne)
- **Fallback ligne 65-69** : recette shaped utilisant `bucket{FluidName:"argon"}` → fonctionnel
- **Verdict** : ✅ pas de bug actif

---

## 4. RECOMMANDATIONS

### 🔴 PRIORITÉ HAUTE — Implémenter les 3 sources manquantes

#### Option A : Créer les machines KRDA125 + Diarh33 (Modular Machinery)

Comme déjà fait pour Cryo-Distillateur et Bio-Réacteur, créer 2 nouvelles machines MM avec recettes :

```json
// config/modularmachinery/machinery/diarh33.json
{
  "registryname": "diarh33",
  "parts": [...],
  "name": "Diarh33"
}
// + recipe : Food + Eau + Bio-E -> 1000mB diarrhee_liquide
```

```json
// config/modularmachinery/machinery/krda125.json
// + recipe : Signalum + Diarrhee + Bio-E -> 1 signalhee_ingot
```

#### Option B : Recettes ZS simples (rapide, prototype)

Pour débloquer la progression rapidement en attendant les MM machines :

```zs
// Age1_Signalhee.zs (à ajouter)
recipes.addShaped("nexus_signalhee_temp",
    <nexusabsolu:signalhee_ingot>,
    [[<thermalfoundation:material:135>, ..., ...],   // signalum
     [...]]);
```

#### Option C : Source pour compose_e

Compose_e est tier ENDGAME Age 2. Sources possibles :
- Drop d'un mob Age 2 (avec EntityLootTables)
- Recette dans Age2_*.zs (n'existe pas encore)
- Output d'une nouvelle machine Age 2 (par ex. raffineur de composes)

### 🟡 PRIORITÉ MOYENNE — Ajouter validation continue

Le script audit Python utilisé peut être ajouté à `scripts-tools/` pour :
- Vérifier la cohérence input/output à chaque commit
- Détecter les nouveaux orphelins automatiquement
- Documenter les sources non-recipe (drops, sieve, Java mod)

---

## 5. PROCHAINES ÉTAPES

- [ ] Vérifier les **items multi-mods en input** non déclarés dans le pack (mods retirés ?)
- [ ] Vérifier les recettes **ContentTweaker** (`scripts/contenttweaker/`)
- [ ] Vérifier la **chaîne progression complète** Age 0 → 1 → 2 → 3 → 4
- [ ] Décider Option A vs B pour les 3 orphelins (validation Alexis requise)

---

*Audit Phase 1 — Items NA orphelins identifiés.*
