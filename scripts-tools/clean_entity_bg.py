"""
v1.0.352 : Rend transparent le fond noir des frames entity_0.png a entity_239.png.

Probleme : les frames entity de l'autre conversation Claude ont un fond noir
(pas transparent) -> en jeu on voit un rectangle noir tourne au PEAK.

Solution : pour chaque pixel near-black (R+G+B < 60), on met alpha = 0.
Les etoiles (pixels blancs/clairs) et l'entite (pixels colores) sont preserves.

Usage :
  python scripts-tools/clean_entity_bg.py

Tourne en ~2-5 minutes selon la taille des frames.
"""

import os
import sys
import time

try:
    from PIL import Image
    import numpy as np
except ImportError:
    print("ERREUR : pip install Pillow numpy")
    sys.exit(1)

ENTITY_DIR = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui/manifold"

# Seuil de detection 'noir' (pixel sombre = transparent)
# Plus haut = plus de pixels deviennent transparents (plus aggressif)
# Plus bas  = seuls les pixels vraiment noirs deviennent transparents (plus safe)
DARK_THRESHOLD = 60  # 0..255, somme R+G+B max pour etre considere 'noir'

# Plage de frames a traiter : 0 a 239 (frames de l'autre Claude)
# 240-299 = mes frames NDE peinture, qui ont deja un bon alpha, NE PAS TOUCHER
FRAME_START = 0
FRAME_END = 239


def clean_frame(path):
    """Rend transparents les pixels sombres d'une frame."""
    img = Image.open(path).convert("RGBA")
    arr = np.array(img)

    r = arr[..., 0].astype(np.int32)
    g = arr[..., 1].astype(np.int32)
    b = arr[..., 2].astype(np.int32)
    a = arr[..., 3]

    # Mask : pixel "sombre" = somme RGB < threshold
    sum_rgb = r + g + b
    is_dark = sum_rgb < DARK_THRESHOLD

    # Pour les pixels sombres : alpha = 0
    # Pour les autres : on garde l'alpha existant
    # IMPORTANT : on conserve les etoiles (pixels clairs) et l'entite
    new_alpha = np.where(is_dark, 0, a).astype(np.uint8)
    arr[..., 3] = new_alpha

    # Stats
    n_dark = int(is_dark.sum())
    n_total = is_dark.size
    pct_transparent = 100.0 * n_dark / n_total

    Image.fromarray(arr).save(path, "PNG", optimize=True)
    return pct_transparent


def main():
    if not os.path.isdir(ENTITY_DIR):
        print(f"ERREUR : dossier introuvable : {ENTITY_DIR}")
        print("Lance ce script depuis la racine du repo Nexus Absolu.")
        sys.exit(1)

    print(f"=== Cleaning fond noir des frames entity {FRAME_START}-{FRAME_END} ===")
    print(f"Dossier : {ENTITY_DIR}")
    print(f"Seuil dark : R+G+B < {DARK_THRESHOLD}")
    print(f"NB : frames 240-299 (NDE) ne sont PAS modifiees.\n")

    t0 = time.time()
    n_done = 0
    n_skipped = 0
    pct_avg = 0.0

    for i in range(FRAME_START, FRAME_END + 1):
        path = os.path.join(ENTITY_DIR, f"entity_{i}.png")
        if not os.path.isfile(path):
            n_skipped += 1
            continue
        try:
            pct = clean_frame(path)
            pct_avg += pct
            n_done += 1
            if i % 20 == 0:
                print(f"  entity_{i}.png : {pct:.1f}% pixels rendus transparents")
        except Exception as e:
            print(f"  ERREUR entity_{i}.png : {e}")
            n_skipped += 1

    elapsed = time.time() - t0
    if n_done > 0:
        pct_avg /= n_done

    print(f"\n=== TERMINE ===")
    print(f"Frames traitees : {n_done}")
    print(f"Frames skipped  : {n_skipped}")
    print(f"Pixel transparent moyen : {pct_avg:.1f}%")
    print(f"Temps : {elapsed:.1f}s")
    print(f"\nMaintenant : bash mod-source/build.sh pour rebuild le mod.")


if __name__ == "__main__":
    main()
