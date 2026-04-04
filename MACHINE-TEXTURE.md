# MACHINE-TEXTURE.md — Style Nexus Absolu pour les textures de machines
> Lire AVANT de creer une texture de bloc machine. Ce document est LA reference.

---

## 1. STYLE GENERAL

Les machines Nexus Absolu utilisent le style **EnderIO biseaute** :
- Cadre 3 couches (bordure → bevel → panneau)
- Centre creuse (le panneau est encastre visuellement)
- Couleurs sombres violet/gris avec accents colores
- Animations subtiles (4-6 frames, interpolation)

```
Vue en coupe d'un pixel row :

  [BORD][BEV3][BEV2][  centre creuse  ][BEV2][BEV1][BORD]
   ↑      ↑     ↑         ↑              ↑     ↑     ↑
  noir   clair  mid     panneau        mid  sombre  noir
                        (contenu)
```

---

## 2. PALETTE EXACTE (valeurs RGB)

### Cadre (identique sur TOUTES les machines)
```
BORD  = (10, 10, 16)    — bordure exterieure, 1px tout le tour
BEV3  = (58, 58, 72)    — bevel clair (haut + gauche, row 1 et col 1)
BEV2  = (46, 46, 56)    — bevel moyen (row 2-3 et col 2-3)
BEV1  = (24, 24, 34)    — bevel sombre (bas + droite, row 14 et col 14)
```

### Centre (change par machine)
```
PANEL_BG     = (12, 12, 18)    — fond du panneau creuse (quasi noir)
PURPLE_DARK  = (60, 20, 80)    — accent violet (barres, cadres internes)
PURPLE_ACCENT = (90, 32, 128)  — violet plus clair (details, symboles)
PURPLE_BRIGHT = (122, 56, 176) — violet vif (highlight, ecran actif)
CYAN_ACCENT  = (0, 180, 180)   — cyan (indicateur, signature Voss)
```

### Accents par fonction
```
INPUT_GREEN   = (30, 120, 48)  solide / (50, 200, 80) brillant
OUTPUT_ORANGE = (140, 88, 24)  solide / (220, 140, 40) brillant
ENERGY_RED    = (200, 50, 50)  solide / (255, 80, 80)  brillant
GOLD_DOT      = (180, 140, 40) — indicateur status
```

---

## 3. STRUCTURE 16x16 PIXEL PAR PIXEL

### Grille de reference
```
     0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
  0: B  B  B  B  B  B  B  B  B  B  B  B  B  B  B  B
  1: B  3  2  3  3  3  3  3  3  3  3  3  3  2  1  B
  2: B  3  3  2  2  2  2  2  2  2  2  2  2  3  1  B
  3: B  3  2  .  .  .  .  .  .  .  .  .  .  2  1  B
  4: B  3  2  .  .  .  .  .  .  .  .  .  .  2  1  B
  5: B  3  2  .  .  .  .  .  .  .  .  .  .  2  1  B
  6: B  3  2  .  .  .  .  .  .  .  .  .  .  2  1  B
  7: B  3  2  .  .  .  .  .  .  .  .  .  .  2  1  B
  8: B  3  2  .  .  .  .  .  .  .  .  .  .  2  1  B
  9: B  3  2  .  .  .  .  .  .  .  .  .  .  2  1  B
 10: B  3  2  .  .  .  .  .  .  .  .  .  .  2  1  B
 11: B  3  2  .  .  .  .  .  .  .  .  .  .  2  1  B
 12: B  3  2  2  2  2  2  2  2  2  2  2  2  2  1  B
 13: B  2  3  2  2  2  2  2  2  2  2  2  2  3  1  B
 14: B  1  1  1  1  1  1  1  1  1  1  1  1  2  1  B
 15: B  B  B  B  B  B  B  B  B  B  B  B  B  B  B  B

B = BORD (10,10,16)
1 = BEV1 (24,24,34) — ombre (bas-droite)
2 = BEV2 (46,46,56) — transition
3 = BEV3 (58,58,72) — lumiere (haut-gauche)
. = zone libre pour le contenu (10x10 pixels, de 3,3 a 12,12)
```

### Bevel 3D — explication
```
Lumiere vient du HAUT-GAUCHE :
  - Row 1, Col 1 = BEV3 (clair, face a la lumiere)
  - Row 14, Col 14 = BEV1 (sombre, dans l'ombre)
  - Le bevel cree l'illusion que le panneau est ENCASTRE dans le cadre
```

---

## 4. CONTENU DU PANNEAU — par type de machine

### Machine de traitement (Pulverizer, Smelter, etc.)
```
Zone 3,3 → 12,12 :
  - Row 3-4 : barre violette horizontale en haut
  - Row 5-10 : symbole de la machine (engrenages, flamme, etc.)
  - Row 11 : barre violette + cyan dot (indicateur Voss)
  - Fond : PANEL_BG avec noise ±2
```

### Machine avec face directionnelle (Auto-Scavenger, Condenseur)
```
Front :  symbole anime (engrenages, ecran)
Side :   panneaux avec vents (lignes horizontales alternees)
Top :    vents + accent violet au centre
```

### Port I/O (Item Input, Item Output, Energy Input)
```
Structure :
  - Barres violettes haut + bas (row 5 et 10)
  - Fleche coloree au centre (verte=IN, orange=OUT)
  - Pour Energy : eclair rouge au centre

Fleche IN (gauche a droite) :
  Row 6-8, col 5-8 = couleur solide (shaft)
  Row 7, col 9 = couleur brillante (pointe)

Fleche OUT (identique mais inversee) :
  Row 6-8, col 7-10 = couleur solide
  Row 7, col 6 = couleur brillante (pointe)
```

---

## 5. ANIMATION (mcmeta)

### Quand animer
- Machines qui consomment du RF : OUI (4-6 frames)
- Blocs statiques (murs, blocs metal) : NON ou subtil (3-4 frames)
- Ports I/O : OUI (4 frames, la fleche pulse)
- Ecrans : OUI (4 frames, scanline ou pulse)

### Parametres mcmeta standard
```json
{"animation": {"interpolate": true, "frametime": 4}}
```
- `interpolate: true` = transitions fluides entre frames
- `frametime: 4` = chaque frame dure 4 ticks (200ms)
- Pour les machines rapides : `frametime: 3`
- Pour les blocs de metal : `frametime: 5`

### Quoi animer entre les frames
```
Frame 0 : etat de base
Frame 1 : accent pulse ON (couleur +30 brightness)
Frame 2 : accent pulse MAX (couleur +50 brightness)
Frame 3 : accent pulse OFF (retour base)

OU pour les scanlines :
Frame 0 : scanline en haut
Frame 1 : scanline au milieu
Frame 2 : scanline en bas
Frame 3 : scanline disparait
```

---

## 6. CODE PYTHON — Template pour creer une machine

```python
from PIL import Image
import random

def create_machine_texture(name, num_frames=4, center_func=None):
    """Cree une texture de machine Nexus Absolu 16x16"""
    
    # Palette
    BORD = (10, 10, 16)
    BEV1 = (24, 24, 34)
    BEV2 = (46, 46, 56)
    BEV3 = (58, 58, 72)
    PANEL = (12, 12, 18)
    
    frames = []
    for f_idx in range(num_frames):
        img = Image.new('RGBA', (16, 16), (*PANEL, 255))
        px = img.load()
        random.seed(42)  # Meme noise pour chaque frame
        
        # Noise sur le fond
        for y in range(16):
            for x in range(16):
                n = random.randint(-2, 2)
                px[x, y] = (PANEL[0]+n, PANEL[1]+n, PANEL[2]+n+1, 255)
        
        # Bordure exterieure (1px)
        for i in range(16):
            px[i, 0]  = (*BORD, 255)
            px[i, 15] = (*BORD, 255)
            px[0, i]  = (*BORD, 255)
            px[15, i] = (*BORD, 255)
        
        # Bevel 3 couches
        for i in range(1, 15):
            px[i,  1] = (*BEV3, 255)  # haut = clair
            px[1,  i] = (*BEV3, 255)  # gauche = clair
            px[i, 14] = (*BEV1, 255)  # bas = sombre
            px[14, i] = (*BEV1, 255)  # droite = sombre
        for i in range(2, 14):
            px[i,  2] = (*BEV2, 255)
            px[2,  i] = (*BEV2, 255)
            px[i, 13] = (*BEV2, 255)
            px[13, i] = (*BEV2, 255)
        
        # Coins du bevel
        px[1, 1]   = (*BEV3, 255)
        px[14, 14] = (*BEV1, 255)
        px[1, 14]  = (*BEV2, 255)
        px[14, 1]  = (*BEV2, 255)
        
        # Contenu custom
        if center_func:
            center_func(px, f_idx, num_frames)
        
        frames.append(img)
    
    # Sauvegarder
    if num_frames == 1:
        frames[0].save(f'{name}.png')
    else:
        total_h = 16 * num_frames
        combined = Image.new('RGBA', (16, total_h))
        for i, fr in enumerate(frames):
            combined.paste(fr, (0, i * 16))
        combined.save(f'{name}.png')
        
        import json
        with open(f'{name}.png.mcmeta', 'w') as f:
            json.dump({"animation": {"interpolate": True, "frametime": 4}}, f)
```

### Exemple : creer un bloc "Analyseur"
```python
def analyseur_content(px, frame_idx, num_frames):
    PURPLE = (60, 20, 80)
    CYAN = (0, 180, 180)
    
    # Barres horizontales violettes
    for x in range(3, 13):
        px[x, 5] = (*PURPLE, 255)
        px[x, 10] = (*PURPLE, 255)
    
    # Symbole loupe au centre
    for dx, dy in [(6,7),(7,6),(8,6),(9,7),(9,8),(8,9),(7,9),(6,8)]:
        px[dx, dy] = (*PURPLE, 255)
    # Manche
    px[10, 9] = (*PURPLE, 255)
    px[11, 10] = (*PURPLE, 255)
    
    # Cyan dot (indicateur Voss) — pulse
    import math
    bright = 0.5 + 0.5 * math.sin(frame_idx / num_frames * 2 * math.pi)
    if bright > 0.7:
        px[11, 11] = (*CYAN, 255)
    
    # Gold status dot
    px[12, 4] = (180, 140, 40, 255)

create_machine_texture('analyseur', num_frames=4, center_func=analyseur_content)
```

---

## 7. TEXTURES MULTI-FACE (machines directionnelles)

### Fichiers necessaires
```
machine_front.png  — face active (ecran, symbole)
machine_side.png   — face laterale (panneaux, vents)
machine_top.png    — face superieure (vents, accent)
```
Si la face arriere est differente, ajouter `machine_back.png`.

### Blockstate JSON
```json
{
  "variants": {
    "facing=north": {"model": "nexusabsolu:machine", "y": 0},
    "facing=south": {"model": "nexusabsolu:machine", "y": 180},
    "facing=west":  {"model": "nexusabsolu:machine", "y": 270},
    "facing=east":  {"model": "nexusabsolu:machine", "y": 90}
  }
}
```

### Vents (faces laterales et top)
```
Pattern standard pour les vents :
  Row 4 : (6, 2, 12)  — fente sombre
  Row 5 : (16, 8, 24) — ombre sous la fente
  Row 7 : (6, 2, 12)
  Row 8 : (16, 8, 24)
  (repeter tous les 3 pixels)
```

---

## 8. TEXTURES SHELL 48x48 (multiblocs connectes)

Pour les multiblocs 3x3x3 (comme le Condenseur T2), on utilise des
textures 48x48 rendues par TESR sur le bounding box du multibloc.

### Structure d'une face 48x48
```
  Rows 0-15  : section du haut (1er bloc)
  Rows 16-31 : section du milieu (2eme bloc)
  Rows 32-47 : section du bas (3eme bloc)
  
  Separateurs aux joints de blocs :
    Row 15-16 : ligne sombre (joint visible)
    Row 31-32 : ligne sombre (joint visible)
```

### Ports dans le shell
Les ports (Input, Output, Energy) occupent seulement 16x16 dans
la zone du bloc correspondant, PAS toute la face 48x48.
```
  Face gauche : port IN vert  en section milieu (16-31)
  Face droite : port OUT orange en section milieu (16-31)  
  Face arriere : port RF rouge en section bas (32-47)
```

### Rendu TESR
```java
// IMPORTANT — etats GL pour shell opaque :
GlStateManager.enableDepth();
GlStateManager.depthFunc(GL11.GL_LEQUAL);
GlStateManager.depthMask(true);
GlStateManager.disableBlend();    // PAS de blend = opaque
GlStateManager.disableAlpha();    // PAS d'alpha test
GlStateManager.disableCull();     // visible des 2 cotes
GlStateManager.disableLighting();
GlStateManager.enableTexture2D();

// Offset pour eviter z-fighting avec les blocs solides dessous
double o = 0.02;   // devant les blocs
double e = 0.02;   // extension aux coins (overlap)
```

---

## 9. CHECKLIST AVANT DE CREER UNE TEXTURE

1. [ ] Format : 16x16 (blocs) ou 32x32 (items) ou 48x48 (shell)
2. [ ] Bordure BORD (10,10,16) sur les 4 cotes
3. [ ] Bevel 3 couches : BEV3 haut-gauche, BEV1 bas-droite
4. [ ] Fond du panneau : PANEL_BG (12,12,18) avec noise ±2
5. [ ] Symbole au centre identifiable (pas trop de detail)
6. [ ] Barres violettes horizontales pour structurer
7. [ ] Cyan dot si c'est une machine Voss
8. [ ] Gold dot pour les indicateurs de status
9. [ ] Animation : 4 frames si RF, 1 frame si statique
10. [ ] mcmeta avec interpolate:true et frametime:4

---

## 10. NE PAS FAIRE

- Ne PAS utiliser de couleurs vives en dehors des accents
- Ne PAS remplir tout l'espace — laisser du PANEL_BG visible
- Ne PAS mettre plus de 2 couleurs d'accent par face
- Ne PAS animer plus de 6 frames (memoire GPU)
- Ne PAS oublier le bevel (c'est ce qui fait le look Nexus)
- Ne PAS faire de textures semi-transparentes pour les machines
  (alpha = 255 partout, sauf si c'est du verre)

---

*Derniere mise a jour : Session Textures T2 — Analyse des textures existantes*
*Reference : auto_scavenger, atelier_voss, condenseur_t2, item_input/output, energy_input*
