# BRIEF DE TRANSFERT — Session "Visuel Ultime"

> Document écrit en fin de session 2026-04-29 par Claude pour Claude.
> À lire avant tout autre fichier dans une nouvelle session.

## Contexte

Le joueur (Alexis, FR, dev) a un build qui tourne avec le système Cartouche
Manifold à 9 stages BPM-sync 84 BPM (Centinela G minor). Il veut maintenant
pousser l'expérience visuelle au maximum. Cette session est **dédiée à ça
uniquement**, pas de polish autre, pas de quêtes, pas de recettes.

## ✅ Décisions prises avant cette session (validées)

### 1. Musique
**Fade-in sur Stage 1 + boost volume au PEAK.**

Implémentation :
- Au start injection : jouer `ModSounds.MANIFOLD_CENTINELA` à volume 0.0
- Dans `ManifoldEffectHandler.onPlayerTick()` : interpoler le volume selon le progress
  - Stage 1 (0:00 → 0:30) : 0.0 → 0.4 (fade in)
  - Stage 2-4 (0:30 → 4:00) : 0.4 (constant, ambient)
  - Stage 5 PEAK (4:00 → 5:30) : 0.4 → 0.8 (boost volume)
  - Retour 4'/3'/2'/1' (5:30 → 8:00) : 0.8 → 0.0 (fade out)
- Problème technique : `world.playSound()` ne permet pas de modifier le volume
  d'un son en cours. Faut soit utiliser `Minecraft.getMinecraft().getSoundHandler()`
  côté client avec un `ITickableSound` custom, soit re-jouer le son à intervalles
  avec différents volumes (cracra). PRÉFÉRER ITickableSound côté client + sync
  packet pour déclencher.

### 2. Détails / animations / formes
**LE PLUS DE DÉTAIL POSSIBLE.**

Plan complet :
- **16 nouvelles textures mandala** (vs 4 actuelles), chacune en 1024x1024
  (vs 512x512), beaucoup plus de couches géométriques
- **16 frames morphing entité** : œil → visage → visages multiples → entité finale
  - Frames 0-3 : œil unique grossit (gradient iris cyan/magenta)
  - Frames 4-7 : œil se fissure et devient visage (apparition des contours)
  - Frames 8-11 : visage se duplique en 3 visages superposés (transparences)
  - Frames 12-15 : les 3 visages fusionnent en l'entité Salviadroid finale
  - Animation jouée pendant les 30 premières secondes du Stage 5 PEAK
  - Après les 30s, boucle sur frames 12-15 (entité finale qui respire)
- **Plus de couches sur l'overlay** :
  - Layer cosmic dust (particules de fond constellation)
  - Layer text glyphs (glyphes Voss qui scrollent en strates)
  - Layer waveform (synchronisé sur la musique : équivalent visuel basse/medium/high)

### 3. Voyage / espace infini
**Tunnel + parallax + shaders qui déforment le tout.**

Implémentation :
- **3 couches tunnel parallax** (vs 1 actuelle) :
  - Couche fond : tunnel scale 0.3x, rotation lente
  - Couche médiane : tunnel scale 0.6x, rotation moyenne
  - Couche avant : tunnel scale 1.2x, rotation rapide + zoom in fort
- **Acceleration progressive** : au début Stage 4 le zoom est lent, à la fin
  rapide → effet "on accélère dans le temps"
- **Shader chromatic aberration** : décale R/G/B de quelques pixels selon distance
  au centre = effet "réalité qui se replie"
- **Shader wobble** : déformation sinusoïdale 2D → "le temps tremble"
- Optimiser : ces shaders ajoutés au shader Mandelbulb existant (manifold.fsh),
  pas un nouveau pipeline

## 📋 Roadmap pour cette nouvelle session

### Étape 1 — Musique fade in/out (1h estimé)
- Créer `ManifoldMusicTickableSound` (client-side) implementing `ITickableSound`
- Volume calculé chaque tick selon progress du trip
- Déclencher via packet server → client au start injection
- Stop quand progress >= 1.0

### Étape 2 — 16 mandalas haute résolution (1.5h estimé)
- Réécrire `generate_mandala_textures.py` :
  - Output 1024x1024 (vs 512)
  - 16 frames (vs 4)
  - Plus de couches (sub-mandalas niveau 3, geometric arc, lotus deep)
  - Sub-pixel anti-aliasing via supersampling 2x puis downscale
- Update `ManifoldOverlayHandler` pour cycle sur 16 frames

### Étape 3 — Morphing entité 16 frames (2h estimé)
- Nouveau script `generate_entity_morphing.py` :
  - Frames 0-3 : variantes iris (zoom progressif depuis pupille)
  - Frames 4-7 : crack effect (lignes blanches qui apparaissent)
  - Frames 8-11 : 3 visages avec offsets latéraux + transparences
  - Frames 12-15 : entité Salviadroid avec breathing
- Update overlay : afficher la frame correcte selon time dans Stage 5

### Étape 4 — 3 tunnels parallax (45min)
- Modifier `renderZoomTunnel()` pour render 3 fois avec scales/rotations différentes
- Acceleration progressive : multiplier zoom_speed par (1 + progress_in_stage4 * 3)

### Étape 5 — Shaders chromatic aberration + wobble (1h)
- Ajouter dans `manifold.fsh` :
  ```glsl
  vec2 wobble_uv = uv + vec2(sin(time*2.0+uv.y*8.0), cos(time*1.7+uv.x*8.0)) * 0.02;
  vec3 col_r = sample(wobble_uv + vec2(0.005, 0));
  vec3 col_g = sample(wobble_uv);
  vec3 col_b = sample(wobble_uv - vec2(0.005, 0));
  ```
- Activer ces effets uniquement Stage 4 + 5

### Étape 6 — Couches additionnelles overlay (1h)
- Cosmic dust : particles statiques avec twinkle
- Text glyphs : scroll de runes Voss latérales
- Waveform reactive : 3 barres bass/mid/high (utiliser BPM sync à défaut de FFT)

**TEMPS TOTAL ESTIMÉ : 7h** — c'est une grosse session, prévoir possible split.

## 🔧 État du code actuel (à NE PAS casser)

### Fichiers principaux à connaître
```
mod-source/src/main/java/com/nexusabsolu/mod/
├── events/ManifoldEffectHandler.java       (server, 9 stages, NBT, music trigger)
├── client/ManifoldClientState.java         (smoothstep, getLayerIntensities, BPM helpers)
├── client/ManifoldOverlayHandler.java      (5 layers : onset/plasma/mandala/tunnel/entity)
├── client/ManifoldHallucinationHandler.java (mobs → blocs stages 3-5)
├── client/ManifoldShaderHandler.java       (Mandelbulb post-process stages 4-5)
├── network/PacketManifoldPhase.java        (sync server → client : startTick + totalTicks)
├── items/ItemCartoucheManifold.java        (right-click → start)
├── items/ItemCartoucheUsed.java            (casing recyclable)
└── init/ModSounds.java                     (MANIFOLD_CENTINELA registered)

mod-source/src/main/resources/assets/nexusabsolu/
├── shaders/post/manifold.json              (pipeline 2 passes manifold + blit)
├── shaders/program/manifold.{vsh,fsh,json} (Mandelbulb raymarching)
├── shaders/program/blit.{fsh,json}         (passthrough copy)
├── sounds/manifold/centinela.ogg           (5.7 MB)
├── sounds.json                             (manifold.centinela registered, stream=true)
└── textures/gui/manifold/
    ├── mandala_{1,2,3,4}.png    (à étendre à 16 frames)
    ├── tunnel_tile.png          (seamless)
    ├── entity_{0,1,2,3}.png     (à étendre à 16 frames morphing)
    └── entity.png               (vieille version, ignorer)
```

### Convention build
```bash
git pull
bash mod-source/build.sh
# Lance le jeu dans /modpack/
```

### Pièges connus (DOCUMENTÉS DANS LE CODE)
1. KubeJS 1.12.2 abandonné, on utilise tout en Java mod-source
2. ContentTweaker namespace = `contenttweaker:` (PAS `nexusabsolu:`)
3. Sound `EVOCATION_ILLAGER_CAST_SPELL` n'existe pas en 1.12.2 → utiliser `ENDERMEN_TELEPORT`
4. `RenderGameOverlayEvent.Post` avec `ElementType.ALL` pas appelé → utiliser `HOTBAR`
5. `world.spawnParticle(t, x,y,z, 1, 0, 0, 0, 0)` ambigu → forcer `0.0` au lieu de `0`
6. ShaderUniform.set(float) overload → reflection method cache pour bypass Matrix4f
7. ObfuscationReflectionHelper avec `EntityRenderer.class` direct (pas `getClass()` wildcard)
8. OptiFine peut bloquer le shader Forge native — tester sans OptiFine d'abord

## 🎯 Ce que la nouvelle session NE DOIT PAS faire

- ❌ Toucher aux quêtes BQ (Phases 2-6 pending mais HORS SCOPE)
- ❌ Toucher aux recettes ZS multiblocs
- ❌ Créer la dimension Age 5
- ❌ Modifier `ItemCartoucheManifold` ou `ItemCartoucheUsed` (sauf signal start)
- ❌ Casser le système 9 stages (architecture validée par playtest)

## 📌 Workflow git

```bash
# Au début
cd /home/claude/nexus-absolu-modpack
git pull
git remote set-url origin "https://lamardalexis-sys:${PAT_FROM_USER}@github.com/lamardalexis-sys/nexus-absolu-modpack.git"

# À chaque commit
git add -A
git commit -m "feat(age4-visual): ..."
git pull --rebase origin main
git push origin HEAD

# À la fin de session
git remote set-url origin "https://github.com/lamardalexis-sys/nexus-absolu-modpack.git"
# Demander à l'user de révoquer le PAT GitHub
```

## ⚠️ Règles strictes session

- Le PAT GitHub colle dans le chat est **CRAMÉ**, demande à Alexis de le révoquer
  après chaque session push
- Toujours `git remote set-url` propre à la fin (sans token)
- Tester chaque chunk en isolation avant d'enchaîner (build + lance le jeu)
- Si compile fail : ne JAMAIS push tant que pas fixé localement

## 🎬 Première action prochaine session

```
1. Lis ce fichier (BRIEF.md) en entier
2. Lis docs/age4-cartouche-manifold/99-progress-log.md
3. Demande à Alexis de confirmer la priorité Étape 1-6 ci-dessus
4. Code Étape 1 d'abord (musique fade) — c'est le plus simple, valide qu'on
   a un workflow propre puis enchaîne avec les étapes lourdes
```

---

*Document créé fin session 2026-04-29 par Claude.*
*Trip 8 minutes BPM-sync à venir : voyage infini de temps unleashed.*
