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

### Session "Visuel Ultime" 2026-04-29 -- Etape 2 (16 mandalas haute resolution)
- ✅ `scripts-tools/generate_mandala_textures.py` (REWRITE) -- v2 :
  - 1024x1024 (vs 512) avec supersampling 2x puis downscale Lanczos (sub-pixel anti-aliasing)
  - 16 frames (vs 4) -- 4 groupes de 4 (petales fines/moyennes/denses/hybride lotus profond)
  - Couches additionnelles : sub-mandalas niveau 3 RECURSIF, geometric arcs partiels (effet brise), lotus deep multi-couches superposees
  - Geometrie sacree multi-densite (hexagones, octogones, dodecagones a 16 cotes imbriques)
- ✅ 16 PNG generes dans `assets/nexusabsolu/textures/gui/manifold/mandala_1..16.png` (~15 MB total)
- ✅ `client/ManifoldOverlayHandler.java` (MODIFY) -- `MANDALA_TEX[]` etendu de 4 a 16 entrees. Logique de rendu inchangee (utilise `% MANDALA_TEX.length` partout, donc transparent au changement). Cycle complet 16 frames * 100 ticks = 80s, ~6 cycles sur le trip de 8 min.
- ✅ Bump version 1.0.329 -> 1.0.330.
- ⏳ A tester en jeu : verifier que les 16 frames se chargent correctement et que le crossfade entre frames est lisse.

### Session "Visuel Ultime" 2026-04-29 -- Etape 1 (Musique fade in/out)
- ✅ `client/ManifoldMusicTickableSound.java` (NEW) — `MovingSound` qui implemente `ITickableSound` (via heritage). `update()` recalcule le volume chaque tick selon `ManifoldClientState.getTripProgress()`. Attenuation NONE (musique "dans la tete").
- ✅ Courbe de volume conforme au brief :
  - Stage 1 (0:00 → 0:30) : 0.0 → 0.4 (fade-in lineaire)
  - Stages 2-4 (0:30 → 4:00) : 0.4 (ambient constant)
  - Stage 5 PEAK (4:00 → 5:30) : 0.4 → 0.8 (boost lineaire)
  - Retour 4'/3'/2'/1' (5:30 → 8:00) : 0.8 → 0.0 (fade-out lineaire)
- ✅ `network/PacketManifoldPhase.java` (MODIFY) — le `Handler.onMessage()` cote client demarre maintenant `ManifoldMusicTickableSound` apres avoir update `ManifoldClientState`. Conditions : `startTick != 0 && totalTicks > 0 && MANIFOLD_CENTINELA != null`.
- ✅ `events/ManifoldEffectHandler.java` (MODIFY) — `playPeakMusic()` supprimee (la musique demarre maintenant au Stage 1, pas au PEAK). Remplacee par `announcePeakArrival()` qui n'envoie que le message narratif "Quelque chose s'approche...". NBT_MUSIC_PLAYED renomme en NBT_PEAK_ANNOUNCED. Whispers villageois inchanges (orthogonaux).
- ✅ Bump version 1.0.328 → 1.0.329.
- ⏳ A tester en jeu : verifier la courbe de volume sur 8 min (sans OptiFine d'abord).

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

### Mod source Java (avancée massive sessions Avril 2026)
- ✅ `ItemCartoucheManifold` + `ItemCartoucheUsed` (mod source, pas CT)
- ✅ Right-click → trip 8 min en **9 stages progressifs** :
   1. Onset (0:30) → 2. Saturation (1:30) → 3. Geometric (2:30) → 4. Hyperspace (4:00)
   → 5. **PEAK** (5:30) → 4'/3'/2'/1' (retour mirror) → Crash + Fatigue 1 min
- ✅ Architecture stages avec smoothstep (intensités lerp continues, pas de coupures)
- ✅ **BPM sync à 84 BPM** (Centinela G minor) — toutes pulsations basées sur `BEAT_TICKS = 14.29 ticks`
- ✅ Musique custom **Centinela** intégrée (assets/sounds/manifold/centinela.ogg, joue au PEAK)
- ✅ **Whispers villageois** lointains au PEAK (entity.villager.ambient pitch 0.4-0.7, dist 10-15)
- ✅ **Entité Salviadroid-style** animée (4 frames, visage divin + ailes + namaste + œil cyclope)
- ✅ Shader Mandelbulb raymarching (post-process, désactivé sans OptiFine, à tester)
- ✅ Hallucinations entités (mobs deviennent blocs aléatoires stages 3-5)
- ✅ Overlay 5 layers progressifs (onset tint / plasma / mandala / tunnel / entité + letterbox PEAK)
- ✅ Indicator debug haut-gauche (nom du stage + % progression)
- ✅ Cooldown 10 min anti-overdose

### Scripts code prêts à coller
- `scripts/Age4_Manifold_Content.zs` — ContentTweaker, ~50 fluides + ~70 items custom
- `scripts-tools/generate_age4_quests.py` — générateur Python BQ JSON
- `scripts-tools/generate_mandala_textures.py` — 4 mandalas 512×512
- `scripts-tools/generate_tunnel_textures.py` — tunnel seamless tile + entity v1
- `scripts-tools/generate_entity_textures.py` — 4 frames entity Salviadroid animée
- `quests-source/age4.json` — 16 quêtes Phase 0+1, IDs 4000-4015

### Mémoire & Skills
- `memory_user_edits #13` — entry à jour
- `superpowers` cloné dans `/home/claude/superpowers/` (best practices)

---

## ⏳ PENDING — fin Age 4 + transition Age 5

> **Ordre de priorité** (le joueur ne peut pas finir l'Age 4 sans le 🟢)

### 🟢 CRITIQUE — sans ça la Cartouche n'existe pas
1. **Recettes ZenScript Modular Machinery** pour les 9 multiblocs (HDS, Cryo-Atm, Castner-Kellner, Hall, Kroll, Haber, Cumène, Alambic, Cyclisateur) — fichier `Age4_Recipes.zs` à créer
2. **JSON Modular Machinery** pour les 9 multiblocs custom dans `config/modularmachinery/machinery/`
3. **Craft final de la Cartouche** : pipeline M1 (Mélangeur Cryogénique) → M2 (Bio-Réacteur) → encartouchage Ti+Argon
4. **Cinématique fin Âge 4** : à la fin du trip 8 min, téléporter joueur vers dimension Age 5 ("vrai monde")

### 🟣 CRITIQUE — transition vers Age 5
11. **Créer dimension Age 5** (registry + dimension type + spawn rules — c'est le "vrai monde" hors simulation)
12. **Mécanique téléportation** depuis fin du trip Cartouche vers Age 5
13. **Lore transition** : effet de "réveil" — le joueur réalise qu'il était en simulation

### 🟡 IMPORTANT — sans ça le joueur galère
5. **Quêtes BQ Phases 2-6** (~64 quêtes restantes pour age4.json)
6. **Carnet Voss Vol IV Patchouli book** (préface OK, 5 chapitres à écrire)
7. **Textures pour les ~70 items custom** ContentTweaker (placeholder violet/noir actuellement)

### 🔵 POLISH
8. Test in-game shader Mandelbulb (savoir si OptiFine bloque)
9. Améliorer textures mandala (densité/saturation)
10. Sons ambient supplémentaires pour stages 1-4

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
