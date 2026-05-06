"""
v1.0.361 : Rend transparent le fond noir des mandala_*.png

Probleme : les PNG mandala ont un fond NOIR (RGB 0,0,0, alpha 255).
Quand le code Java dessine le mandala via drawRotatedTexture sur un quad
rotationne de 4800x4800 pixels, TOUT le carre du PNG est noir opaque
(sauf le motif central) -> on voit des 'carres noirs' aux coins de l'ecran.

Solution : pour chaque pixel R+G+B < 60, alpha = 0.
Le motif central (couleurs vives) est preserve, le fond devient transparent.

Usage :
  python scripts-tools/clean_mandala_bg.py

Tourne en ~1 minute selon le nombre de mandala_*.png.
"""

import os
import sys
import time
import glob

try:
    from PIL import Image
    import numpy as np
except ImportError:
    print("ERREUR : pip install Pillow numpy")
    sys.exit(1)

MANDALA_DIR = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui/manifold"
DARK_THRESHOLD = 60  # R+G+B < 60 -> transparent


def clean_frame(path):
    img = Image.open(path).convert("RGBA")
    arr = np.array(img)
    
    r = arr[..., 0].astype(np.int32)
    g = arr[..., 1].astype(np.int32)
    b = arr[..., 2].astype(np.int32)
    a = arr[..., 3]
    
    sum_rgb = r + g + b
    is_dark = sum_rgb < DARK_THRESHOLD
    
    new_alpha = np.where(is_dark, 0, a).astype(np.uint8)
    arr[..., 3] = new_alpha
    
    n_dark = int(is_dark.sum())
    n_total = is_dark.size
    pct_transparent = 100.0 * n_dark / n_total
    
    Image.fromarray(arr).save(path, "PNG", optimize=True)
    return pct_transparent


def main():
    if not os.path.isdir(MANDALA_DIR):
        print(f"ERREUR : dossier introuvable : {MANDALA_DIR}")
        sys.exit(1)
    
    pattern = os.path.join(MANDALA_DIR, "mandala_*.png")
    files = sorted(glob.glob(pattern))
    
    if not files:
        print(f"Aucun mandala_*.png trouve dans {MANDALA_DIR}")
        sys.exit(0)
    
    print(f"=== Cleaning fond noir des {len(files)} mandala_*.png ===")
    print(f"Seuil dark : R+G+B < {DARK_THRESHOLD}\n")
    
    t0 = time.time()
    n_done = 0
    pct_avg = 0.0
    
    for path in files:
        try:
            pct = clean_frame(path)
            pct_avg += pct
            n_done += 1
            name = os.path.basename(path)
            print(f"  {name}: {pct:.1f}% pixels transparents")
        except Exception as e:
            print(f"  ERREUR {os.path.basename(path)}: {e}")
    
    elapsed = time.time() - t0
    if n_done > 0:
        pct_avg /= n_done
    
    print(f"\n=== TERMINE ===")
    print(f"Frames traitees : {n_done}/{len(files)}")
    print(f"Pixel transparent moyen : {pct_avg:.1f}%")
    print(f"Temps : {elapsed:.1f}s")
    print(f"\nMaintenant : bash mod-source/build.sh pour rebuild le mod.")


if __name__ == "__main__":
    main()
