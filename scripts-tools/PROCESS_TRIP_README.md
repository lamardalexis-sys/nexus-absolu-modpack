# process_trip.py — Pipeline post-processing Cartouche Manifold

Pipeline complet pour traiter les 3000 frames generees via Replicate flux-schnell
et les integrer dans le mod (couche trip background plein ecran).

## Pre-requis

- Python 3.7+ avec **Pillow** : `pip install Pillow`
- **ffmpeg** dans le PATH (pour les previews MP4)
- Dossier `out_frames/` avec les 3000 PNG nommes `0000.png` a `2999.png`

## Usage typique

Place le script a cote du dossier `out_frames/` (par exemple sur `~/Desktop/image_trip/`).

```bash
# 1. Audit rapide (5s) : verifie integrite + detecte les 9 stages du trip
python process_trip.py audit

# 2. Contact sheets (1-2 min) : une planche par stage pour valider VISUELLEMENT
python process_trip.py contact
# -> regarde processed/contact_sheets/*.png

# 3. Preview MP4 (3-5 min) : video lisible
python process_trip.py preview
# -> processed/preview_6fps.mp4 (8 min, playback realiste)
# -> processed/preview_30fps.mp4 (1m40, scrub rapide)

# 4. Resize + quantize (5-10 min) : 1024 -> 512, 128 couleurs
python process_trip.py resize
# -> processed/frames_512/ (~80-150 MB au total)

# 5. Genere le snippet Java a coller dans ManifoldOverlayHandler
python process_trip.py java
# -> processed/TripFrames.java.snippet

# 6. Installe les frames dans le mod
python process_trip.py install
# Auto-detecte le repo. Sinon : python process_trip.py install <PATH>

# === OU : tout d'un coup (sauf install) ===
python process_trip.py all
```

## Detection des scenes

Le script utilise dans cet ordre de priorite :

1. **prompts_config.py** s'il est a cote (le decoupage 'content' utilise
   pour generer les prompts)
2. **9 stages du trip** synchronises avec `ManifoldEffectHandler.java` :
   - stage1_onset       (0:00 - 0:30)
   - stage2_saturation  (0:30 - 1:30)
   - stage3_geometric   (1:30 - 2:30)
   - stage4_hyperspace  (2:30 - 4:00)
   - stage5_peak        (4:00 - 5:30)
   - stage4r_hyperspace (5:30 - 6:30)
   - stage3r_geometric  (6:30 - 7:00)
   - stage2r_saturation (7:00 - 7:30)
   - stage1r_onset      (7:30 - 8:00)

## Options

```bash
--size N        # taille cible resize (defaut: 512)
--colors N      # palette quantize (defaut: 128)
--src DIR       # dossier source (defaut: out_frames)
--workers N     # threads parallel (defaut: 8)
--fps N         # FPS preview (defaut: 6 = playback realiste)
```

## Integration Java

Le `cmd_java` genere :

- `TRIP_FRAMES[3000]` : array de `ResourceLocation` charge en memoire
- `computeTripFrameFloat(t)` : mapping progress [0..1] -> frame float (avec crossfade)
- `renderTripBackground(...)` : rendu plein ecran avec blend entre frame N et N+1
- `computeTripBackgroundAlpha(progress)` : courbe d'intensite globale (fade-in,
  PEAK bell-curve, fade-out)

A integrer comme **COUCHE -1** dans `ManifoldOverlayHandler.renderOverlay()`,
avant le cosmic dust, pour servir de fond plein ecran continu.

## Performances attendues

| Etape       | Duree    | Sortie                  |
|-------------|----------|-------------------------|
| audit       | 5 s      | console                 |
| contact     | 1-2 min  | 9 PNG planches          |
| preview     | 3-5 min  | 2 MP4 (~50-150 MB)      |
| resize      | 5-10 min | 3000 PNG (~80-150 MB)   |
| install     | 30 s     | copie vers mod assets   |
| java        | < 1 s    | 1 snippet .java         |

## En cas de probleme

- **Frame manquante** : `audit` les liste. Tu peux relancer `batch_generator.py`
  qui skip les frames existantes (resume automatique).
- **Quality merdique** : regarde les contact sheets AVANT le resize. Si une
  scene specifique est ratee, on peut regenerer juste cette scene.
- **Trop lourd** : passe `--size 256 --colors 64` pour plus aggressif
  (~30-50 MB total au prix d'un peu de qualite).
