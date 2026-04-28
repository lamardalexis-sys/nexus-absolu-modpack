---
name: age4-cartouche-manifold
description: Use when working on Age 4 (L'Échappée / Cartouche Manifold) of the Nexus Absolu modpack. Triggered by mentions of "cartouche", "manifold", "age 4", "sortie de simulation", chimie pétrochimique-pharmaceutique-nucléaire, 8 lignes parallèles, 30 éléments chimiques, 16 champis Botania, Manifoldine, théorèmes de Voss IV/V.
---

# Age 4 — Cartouche Manifold (Project SKILL)

## Premier réflexe en arrivant dans une session sur ce projet

1. **TOUJOURS lire en premier** : `docs/age4-cartouche-manifold/99-progress-log.md` — c'est l'état réel du projet, le journal de session.
2. **Puis** : `docs/age4-cartouche-manifold/README.md` (MOC = Map of Content — tu vois tout d'un coup d'œil).
3. **Puis** seulement attaquer la tâche demandée.

## Architecture des âges (RÉELLE, pas celle du SKILL.md principal qui est faux)

- Âges 0-1 : Compact Machines (simulation 1ère couche)
- **Fin Âge 1** = sortie des CM vers Overworld
- Âges 2-3-4 : Overworld mais simulation 2ème couche
- **Fin Âge 4** = Cartouche Manifold = sortie de simulation → vrai monde
- Âges 5-9 : réalité

⚠️ Le `SKILL.md` racine du repo a un décalage de +1 (oublie Âge 0). Ne pas se fier à lui pour la numérotation.

## Principes de design Age 4

### Pipeline (8 lignes parallèles)
- L1 Pétrochimie / L2 Hydro-Eau / L3 Électrolyse+Cryo / L4 Pyrométallurgie
- L5 Nucléaire / L6 Acides+Ammoniaque / L7 Organique+Acétone / L8 Botanique+Manifoldine
- Hub central : **L6 (NH₃ pivot)** — consomme/produit le plus
- L2 et L3 alimentent presque tout (eau et gaz industriels)

### Convergence
- 30 éléments chimiques + 16 champignons Botania → 10 composés intermédiaires (α/β/γ/δ/ε)
- 2 multiblocs cascade : M1 Mélangeur Cryogénique → M2 Bio-Réacteur Manifold étendu
- Maturation stellaire intégrée dans M2 (sous le ciel nocturne, 4 min)

### 5 théorèmes de Voss respectés
- I Conservation : chimie réelle (Haber-Bosch, Hall-Héroult, Kroll, Cumène, Ostwald, Claus)
- II Organique : 16 champis → Essence Chromatique
- III Stellaire : Liquid Starlight + Cyclisateur
- IV Sanguine : composés δ bio-actifs + injection neurochimique
- V Brisure : Mycélium Activé sous flux neutronique → Manifoldine

## Conventions de code

### Fluides/items custom — ContentTweaker
- Préfixe registry : `nexusabsolu:` ou `contenttweaker:` (cohérence avec autres âges — vérifier dans `scripts/Age*_*.zs`)
- Fichier dédié : `scripts/Age4_Manifold.zs` (pattern existant)
- Pour chaque nouveau fluide :
  ```zenscript
  #loader contenttweaker
  import mods.contenttweaker.VanillaFactory;
  val ammoniaque = VanillaFactory.createFluid("ammoniaque", 0xC8E6C9);
  ammoniaque.density = 730;
  ammoniaque.viscosity = 250;
  ammoniaque.register();
  ```

### Recettes Modular Machinery — CraftTweaker
- Pattern :
  ```zenscript
  mods.modularmachinery.RecipeBuilder.newBuilder("recipe_id", "machine_name", duration_ticks, priority)
    .addItemInput(<item:nexusabsolu:xxx>)
    .addFluidInput(<liquid:nexusabsolu:yyy> * 1000)
    .addEnergyPerTickInput(rfPerTick)
    .addItemOutput(<item:nexusabsolu:zzz>)
    .build();
  ```

### Effets KubeJS
- Fichier : `kubejs/server_scripts/age4_manifold_effects.js`
- Player events : `onEvent('player.tick', e => {...})`
- Persist state via `player.persistentData` (NBT-backed)

### Quêtes BetterQuesting
- Format JSON avec types NBT-suffixés (`name:8` = string, `questID:3` = int)
- Pattern d'intro : voir `docs/age4-cartouche-manifold/03-progression-quetes.md` § "Pattern par quête"
- Layout serpentin (voir Q138-Q144 fin Âge 1 dans QUESTS_PLAN_UPDATED.md pour référence)

## Best practices héritées de superpowers

- **Bite-sized tasks** (2-5 min chacune)
- **Verification before completion** : tester chaque recette via `/give` + `/recipe` + assemble manuel avant de pousser
- **DRY** : un composant nommé une seule fois, référencé via wikilinks Obsidian
- **YAGNI** : on n'ajoute pas de mécaniques "au cas où" — chaque ingrédient a un rôle dans les 5 théorèmes ou est coupé

## Erreurs à NE JAMAIS refaire (specifique Age 4)

- ❌ Confondre Composé X-77 (Âge 3) avec Manifoldine (Âge 4)
- ❌ Utiliser Ergoline (vrai précurseur LSD/LSA) au lieu de la Manifoldine fictive
- ❌ Faire une recette sans inter-dépendance avec une autre ligne (chaque recette doit consommer au moins 1 produit d'une autre ligne)
- ❌ Pousser sur le repo Git si le PAT est exposé en chat → laisser Alexis commit
- ❌ Ajouter Forestry (mod exclu, pas rentable)
- ❌ Ne pas mettre à jour `99-progress-log.md` à la fin d'une session (on perd la mémoire)

## Quick reference — éléments à fabriquer

| Type | Nombre | Où documenté |
|------|--------|--------------|
| Fluides custom | ~30 | À créer dans `scripts/Age4_Manifold_Content.zs` |
| Items custom | ~10 (Cartouche Vide/Pleine, Cristaux Manifoldine, casing, etc.) | Idem |
| Multiblocs custom | 9 | `docs/age4-cartouche-manifold/multiblocs/` |
| Effets KubeJS | 4 (Bullet Time, Lifesteal, Lightning Chain, Hard Reset) | `kubejs/server_scripts/age4_manifold_effects.js` |
| Quêtes | ~80 en 6 phases | `quests-source/age4.json` (à créer) |

## Voir aussi (dans le vault)

- `README.md` — MOC entrypoint
- `00-vision.md` — philosophie et ton
- `01-lore-integration.md` — 5 théorèmes + Carnet Voss Vol IV
- `02-pipeline-overview.md` — vue d'ensemble industrielle + matrice inter-dépendances
- `03-progression-quetes.md` — structure 80 quêtes
- `lines/L1-L8.md` — détail technique par ligne
- `99-progress-log.md` — journal de session ⭐ LIRE EN PREMIER
