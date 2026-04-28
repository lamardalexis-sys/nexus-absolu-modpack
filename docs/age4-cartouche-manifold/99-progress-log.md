# 99 — Progress Log

> **À LIRE EN PREMIER** dans toute session future Claude sur ce projet.

---

## 📌 Architecture des âges (RÉFÉRENCE — vraie numérotation)

⚠️ Le `SKILL.md` racine du repo est **incohérent** : il oublie que l'Âge 0 existe et tous ses "Âge X" sont décalés de +1. Tous les fichiers de ce vault utilisent la VRAIE numérotation :

- Âges 0-1 : Compact Machines (simulation 1ère couche)
- **Fin Âge 1** = sortie CM vers Overworld
- Âges 2-3-4 : Overworld mais simulation 2ème couche
- **Fin Âge 4** = Cartouche Manifold = sortie de simulation → vrai monde
- Âges 5-9 : réalité

---

## ✅ COMPLETED

### Vault Obsidian — `docs/age4-cartouche-manifold/`
- `README.md` — MOC index
- `SKILL.md` — skill projet (patterns superpowers appliqués)
- `00-vision.md` — philosophie et ton
- `01-lore-integration.md` — 5 théorèmes Voss + Carnet Vol IV
- `02-pipeline-overview.md` — vue d'ensemble + matrice inter-dépendances
- `03-progression-quetes.md` — structure 80 quêtes en 6 phases
- `99-progress-log.md` — CE FICHIER
- `lines/L1-petrochimie.md` — HDS, Hydrocrack, Solvant α
- `lines/L2-hydro-eau.md` — filtration ionique, osmose, tritium
- `lines/L3-electrolyse-cryo.md` — Cryo-Distillation Atmosphérique, Castner-Kellner
- `lines/L4-pyrometallurgie.md` — Hall-Héroult, Kroll, Aqua Regia, H₃PO₄
- `lines/L5-nucleaire.md` — γ1/γ2/γ3, Mycélium Activation
- `lines/L6-acides-ammoniaque.md` — Haber-Bosch hub, Ostwald, Contact
- `lines/L7-organique-acetone.md` — Cumène 4 phases, Tryptamide-M
- `lines/L8-botanique-manifoldine.md` — Alambic, Soxhlet, Cyclisateur Stellaire

### Scripts code prêts à coller
- `scripts/Age4_Manifold_Content.zs` — ContentTweaker, ~50 fluides + ~70 items custom
- `kubejs/server_scripts/age4_manifold_effects.js` — 4 effets signature
- `scripts-tools/generate_age4_quests.py` — générateur Python BQ JSON
- `quests-source/age4.json` — 16 quêtes Phase 0 (3) + Phase 1 (13), IDs 4000-4015

### Mémoire & Skills
- `memory_user_edits #13` — entry à jour (vault, scripts, architecture)
- `superpowers` cloné dans `/home/claude/superpowers/` (best practices)
- Patterns appliqués : writing-plans, verification-before-completion, DRY, YAGNI

---

## ⏳ PENDING (sessions futures)

### Quêtes BQ — Phases 2-6 (~64 quêtes restantes)
- Phase 2 Métaux (15q) : L4 Hall-Héroult, Kroll, Aqua Regia, H₃PO₄
- Phase 3 Chimie Sèche (15q) : L6 Haber-Bosch, Ostwald, Contact, HCl
- Phase 4 Feu Nucléaire (10q) : L5 UF₆, breeder, γ2, γ3, Mycélium
- Phase 5 Vivant et Étoile (15q) : L8 16 champis, Alambic, Cyclisateur
- Phase 6 Convergence (10q) : composés finaux + Q-FINAL Injection
- → étendre `scripts-tools/generate_age4_quests.py` en suivant le pattern

### JSON Modular Machinery (9 multiblocs)
À créer dans `config/modularmachinery/machinery/` :
- MB-CRYO-ATM, MB-CK, MB-HALL, MB-KROLL, MB-HDS
- MB-ALAMBIC, MB-CYCLO, MB-BIOREACTOR, MB-MIXER-CRYO

### Mod source Java
À créer dans `mod-source/src/main/java/com/nexusabsolu/mod/` :
- `items/ItemCartoucheManifold.java` — injection custom si KubeJS limite
- `particles/ManifoldineParticleFactory.java` — particules glow custom
- `events/ManifoldDeathHandler.java` — Hard Reset si KubeJS échoue
- `cinematics/Age4ExitCinematic.java` — sortie simulation Phase 6 finale

### Patchouli Book — Carnet Voss Vol IV
- `assets/nexusabsolu/patchouli_books/carnet_voss_v4/` : book.json + 5 chapitres
- Préface déjà rédigée dans `01-lore-integration.md`

### Cleanup
- SKILL.md principal du repo : corriger numérotation des âges (oublie Âge 0)

### Tests en jeu
- KubeJS 1.12.2 events compatibility — valider Lifesteal/Lightning/Bullet Time/Hard Reset
- BQ JSON age4.json — charger et vérifier affichage + validation
- ContentTweaker `nexusabsolu:` namespace — vérifier cohérence avec autres âges

---

## ⚠️ ACTIONS UTILISATEUR (Alexis)

1. **Commit/push manuel** depuis sa machine — Claude n'a PAS pushé (PAT exposé en chat session précédente).
2. **Régénérer le PAT GitHub** : token exposé `github_pat_11BZJKYXA0...VL5jrUJ1FT1rI2DYBFIXF37Lb4XlA` → révoquer + régénérer dans Settings > Developer.
3. **Tester en jeu** :
   - Charger ZS : `/ct fluids` + `/ct items` doivent lister les nouveaux registres
   - Charger KubeJS : `/give @p nexusabsolu:cartouche_manifold` puis right-click
   - Charger BQ : copier `age4.json` dans `config/betterquesting/` ou `/bq_admin import`
4. **Reporter en chat** ce qui marche/casse → on itère.

---

## 📂 Refs techniques

- `/home/claude/superpowers/` — best practices clonés
- HellFirePvP/ModularMachinery wiki — design multiblocs
- docs.blamejared.com/1.12/en/Mods/ModularMachinery/ — recettes CrT
- docs.blamejared.com/1.12/en/Mods/ContentTweaker/ — fluides/items custom
- github.com/Funwayguy/BetterQuesting/wiki — BQ format

---

## 📝 Historique sessions

### Session N (compactée)
- Vault initial (6 fichiers MOC) + scripts + entry mémoire #13
- Pas persisté sur repo (ephemeral /home/claude/)

### Session actuelle
- Repo re-cloné, vault élargi à 16+ fichiers
- 8 fichiers `lines/L1-L8.md` (détail technique chaque ligne)
- ContentTweaker complet (~50 fluides + ~70 items)
- KubeJS effets signature (4 effets)
- Générateur Python + age4.json (16 quêtes Phase 0+1)
- SKILL.md projet (patterns superpowers)
- memory_user_edits #13 mis à jour
