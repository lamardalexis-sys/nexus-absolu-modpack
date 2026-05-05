#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Genere les 60 frames d'entity morphing pour le Cartouche Manifold (Stage 5 PEAK).

Concept :
  - Un OEIL semi-realiste reste IDENTIQUE au centre durant les 60 frames
    (point d'ancrage psychique, l'observateur cosmique qui te fixe).
  - Autour de l'oeil, une transformation horror progressive en 5 phases :
    - IRIS        : 0-7   (8)  - oeil qui s'ouvre depuis ferme
    - CRACK       : 8-21  (14) - cracks rouge sang qui apparaissent (realite brisee)
    - FACES       : 22-35 (14) - visages organiques chair sombre se forment
    - METAMORPHOSE: 36-49 (14) - tentacules sortent + transformation entite
    - ENTITY_LOOP : 50-59 (10) - entite finale + aureole d'yeux flottants (loop)

Style : semi-realisme stylise pousse (pur Python PIL, pas de photos ni AI).
Limite : pas du photorealisme. Mais largement plus pousse que le wireframe.

Pour regenerer : python3 scripts-tools/generate_entity_frames.py
"""
import os
import math
import random
from PIL import Image, ImageDraw, ImageFilter

SIZE = 1024
SS = 2
DRAW = SIZE * SS
CX = CY = DRAW // 2
OUT_DIR = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui/manifold"

N_FRAMES = 60
PHASE_RANGES = {
    'iris':       (0, 7),    # 8 frames
    'crack':      (8, 21),   # 14 frames
    'faces':      (22, 35),  # 14 frames
    'metamorph':  (36, 49),  # 14 frames
    'entity_loop':(50, 59),  # 10 frames
}


# ============================================================
# Helpers
# ============================================================

def hex_to_rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))


def lerp_color(c1, c2, t):
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(3))


def get_phase_and_progress(frame_idx):
    """Retourne (phase_name, progress 0->1 dans la phase)."""
    for phase, (start, end) in PHASE_RANGES.items():
        if start <= frame_idx <= end:
            n = end - start
            progress = (frame_idx - start) / max(1, n)
            return phase, progress
    return 'entity_loop', 1.0


# ============================================================
# Eye drawing (avec parametre openness pour la phase IRIS)
# ============================================================

def draw_iris_detailed(img, cx, cy, radius, base_color):
    d = ImageDraw.Draw(img, 'RGBA')
    color_dark = tuple(max(0, c - 70) for c in base_color)
    color_light = tuple(min(255, c + 50) for c in base_color)
    color_outer = tuple(max(0, c - 100) for c in base_color)
    n_steps = 50
    for i in range(n_steps):
        t = i / n_steps
        r = int(radius * (1 - t * 0.95))
        if r < 1: break
        c = lerp_color(color_outer, color_light, t)
        d.ellipse([cx-r, cy-r, cx+r, cy+r], fill=c + (255,))
    n_strands = 100
    for i in range(n_strands):
        a = i * 2 * math.pi / n_strands + random.uniform(-0.02, 0.02)
        r_in = radius * 0.18
        r_out = radius * 0.92
        x1 = cx + math.cos(a) * r_in
        y1 = cy + math.sin(a) * r_in
        x2 = cx + math.cos(a) * r_out
        y2 = cy + math.sin(a) * r_out
        if i % 3 == 0:
            d.line([(x1, y1), (x2, y2)], fill=color_light + (100,), width=1*SS)
        elif i % 3 == 1:
            d.line([(x1, y1), (x2, y2)], fill=color_dark + (130,), width=1*SS)
    d.ellipse([cx-radius, cy-radius, cx+radius, cy+radius],
              outline=(15, 8, 5, 255), width=3*SS)


def draw_eye(img, cx, cy, radius, openness=1.0, look_a=0, look_strength=0.15,
             iris_color_hex='#3F6B4F', pupil_dilation=1.0, weeping=False):
    """
    Oeil semi-realiste, ouvert/ferme selon openness.
    pupil_dilation : 1.0 = pupille normale, 1.5 = dilatee, 0.5 = retractee.
    weeping : True ajoute des larmes noires qui coulent.
    """
    d = ImageDraw.Draw(img, 'RGBA')
    sclera_w = radius * 1.7
    sclera_h = radius * 1.0 * openness

    if sclera_h < 2:
        # Œil ferme : juste une ligne noire epaisse
        d.line([(cx-sclera_w, cy), (cx+sclera_w, cy)],
               fill=(20, 12, 8, 255), width=4*SS)
        return

    # 1. Sclera
    d.ellipse([cx-sclera_w, cy-sclera_h, cx+sclera_w, cy+sclera_h],
              fill=(248, 244, 238, 255))

    # 2. Ombre subtile haut
    shadow_layer = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    sh = ImageDraw.Draw(shadow_layer, 'RGBA')
    for i in range(15):
        t = i / 15
        h = int(sclera_h * (0.55 - t * 0.45))
        if h < 1: break
        alpha = int(45 * (1 - t))
        sh.ellipse([cx-sclera_w*0.95, cy-sclera_h*0.95,
                    cx+sclera_w*0.95, cy-sclera_h*0.95+h*2],
                   fill=(80, 50, 40, alpha))
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(radius=8))
    img.alpha_composite(shadow_layer)
    d = ImageDraw.Draw(img, 'RGBA')

    # 3. Veines rouges
    random.seed(42)
    for _ in range(4):
        sa = random.uniform(0, 2*math.pi)
        sx = cx + math.cos(sa) * sclera_w * 0.95
        sy = cy + math.sin(sa) * sclera_h * 0.95
        n_seg = random.randint(3, 5)
        prev_x, prev_y = sx, sy
        cur_a = sa + math.pi
        cur_r_x = sclera_w * 0.85
        cur_r_y = sclera_h * 0.85
        for _ in range(n_seg):
            cur_r_x *= 0.6
            cur_r_y *= 0.6
            cur_a += random.uniform(-0.4, 0.4)
            x = cx + math.cos(cur_a) * cur_r_x
            y = cy + math.sin(cur_a) * cur_r_y
            d.line([(prev_x, prev_y), (x, y)],
                   fill=(170, 40, 50, 150), width=1*SS)
            prev_x, prev_y = x, y

    # 4. Iris (visible si suffisamment ouvert)
    if openness > 0.3:
        iris_x = cx + math.cos(look_a) * radius * look_strength
        iris_y = cy + math.sin(look_a) * radius * look_strength * 0.5
        iris_color = hex_to_rgb(iris_color_hex)
        iris_r = int(min(radius * 0.6, sclera_h * 0.95))
        draw_iris_detailed(img, iris_x, iris_y, iris_r, iris_color)

        d = ImageDraw.Draw(img, 'RGBA')
        # 5. Pupille (avec dilatation parametrable)
        pupil_r = int(iris_r * 0.45 * pupil_dilation)
        for i in range(6):
            t = i / 6
            r = int(pupil_r * (1 - t * 0.3))
            if r < 1: break
            c = int(0 + t * 8)
            d.ellipse([iris_x-r, iris_y-r, iris_x+r, iris_y+r],
                      fill=(c, c, c, 255))

        # 6. Reflets
        halo = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
        hd = ImageDraw.Draw(halo, 'RGBA')
        hl_x = iris_x - iris_r * 0.30
        hl_y = iris_y - iris_r * 0.36
        hl_r = int(iris_r * 0.20)
        hd.ellipse([hl_x-hl_r*2, hl_y-hl_r*2, hl_x+hl_r*2, hl_y+hl_r*2],
                   fill=(255, 255, 255, 100))
        halo = halo.filter(ImageFilter.GaussianBlur(radius=8))
        img.alpha_composite(halo)
        d = ImageDraw.Draw(img, 'RGBA')
        d.ellipse([hl_x-hl_r, hl_y-hl_r, hl_x+hl_r, hl_y+hl_r],
                  fill=(255, 255, 255, 255))
        hl2_r = int(iris_r * 0.08)
        hl2_x = iris_x + iris_r * 0.27
        hl2_y = iris_y + iris_r * 0.30
        d.ellipse([hl2_x-hl2_r, hl2_y-hl2_r, hl2_x+hl2_r, hl2_y+hl2_r],
                  fill=(255, 255, 255, 220))

    # 7. Lignes paupieres
    n_pts = 50
    upper_pts = [(cx + math.cos(math.pi + t/n_pts * math.pi) * sclera_w,
                  cy + math.sin(math.pi + t/n_pts * math.pi) * sclera_h)
                 for t in range(n_pts + 1)]
    for i in range(len(upper_pts) - 1):
        d.line([upper_pts[i], upper_pts[i+1]],
               fill=(20, 12, 8, 255), width=4*SS)
    lower_pts = [(cx + math.cos(t/n_pts * math.pi) * sclera_w,
                  cy + math.sin(t/n_pts * math.pi) * sclera_h)
                 for t in range(n_pts + 1)]
    for i in range(len(lower_pts) - 1):
        d.line([lower_pts[i], lower_pts[i+1]],
               fill=(70, 45, 35, 200), width=2*SS)

    # 8. Cils (toujours visibles si oeil ouvert)
    n_lashes = 35
    for i in range(n_lashes):
        t = (i + 0.5) / n_lashes
        a = math.pi + t * math.pi
        x_base = cx + math.cos(a) * sclera_w * 0.99
        y_base = cy + math.sin(a) * sclera_h * 0.99
        cil_len = radius * 0.30 * (0.6 + math.sin(t * math.pi) * 0.5)
        cil_dir_a = a - math.pi/2.2 + (t - 0.5) * 0.5
        x_tip = x_base + math.cos(cil_dir_a) * cil_len
        y_tip = y_base + math.sin(cil_dir_a) * cil_len
        d.line([(x_base, y_base), (x_tip, y_tip)],
               fill=(15, 8, 5, 255), width=2*SS)
    n_lower = 18
    for i in range(n_lower):
        t = (i + 0.5) / n_lower
        a = t * math.pi
        x_base = cx + math.cos(a) * sclera_w * 0.99
        y_base = cy + math.sin(a) * sclera_h * 0.99
        cil_len = radius * 0.13 * (0.5 + math.sin(t * math.pi) * 0.4)
        cil_dir_a = a + math.pi/2.5 + (t - 0.5) * 0.3
        x_tip = x_base + math.cos(cil_dir_a) * cil_len
        y_tip = y_base + math.sin(cil_dir_a) * cil_len
        d.line([(x_base, y_base), (x_tip, y_tip)],
               fill=(20, 12, 8, 220), width=1*SS)

    # 9. Larmes noires (phase entity loop)
    if weeping:
        for side in [-1, 1]:
            tx = cx + side * sclera_w * 0.7
            ty_start = cy + sclera_h * 0.5
            # Chaine de gouttes qui descendent
            for i in range(4):
                ty = ty_start + i * radius * 0.18
                tr = int(radius * (0.05 - i * 0.008))
                if tr < 1: break
                d.ellipse([tx-tr, ty-tr, tx+tr, ty+tr],
                          fill=(0, 0, 0, 220))


# ============================================================
# Phase transformations (autour de l'oeil)
# ============================================================

def draw_cracks(img, intensity, seed=99):
    """Cracks rouge sang qui rayonnent du centre."""
    d = ImageDraw.Draw(img, 'RGBA')
    random.seed(seed)
    n_cracks = int(20 * intensity)
    for _ in range(n_cracks):
        base_a = random.uniform(0, 2 * math.pi)
        n_segments = random.randint(8, 15)
        start_r = random.uniform(DRAW * 0.10, DRAW * 0.20)
        prev_x = CX + math.cos(base_a) * start_r
        prev_y = CY + math.sin(base_a) * start_r
        for s in range(n_segments):
            t = (s + 1) / n_segments
            r = start_r + (DRAW * 0.5 - start_r) * t
            wobble = random.uniform(-0.4, 0.4)
            a = base_a + wobble
            x = CX + math.cos(a) * r
            y = CY + math.sin(a) * r
            alpha = int(180 * (1 - t * 0.6) * intensity)
            d.line([(prev_x, prev_y), (x, y)],
                   fill=(160, 30, 40, alpha), width=2*SS)
            prev_x, prev_y = x, y


def draw_faces_around(img, intensity):
    """Visages organiques chair sombre qui apparaissent autour."""
    d = ImageDraw.Draw(img, 'RGBA')
    n_faces = max(0, int(4 * intensity + 0.5))
    # Couleur chair sombre violette (plus visible que noir)
    face_color = (60, 35, 50)
    eye_color = (5, 0, 5)
    
    for i in range(n_faces):
        a = i * 2 * math.pi / 4 + math.pi / 4
        r = DRAW * 0.30
        fx = int(CX + math.cos(a) * r)
        fy = int(CY + math.sin(a) * r)
        # Forme blob organique (asymetrique)
        n_pts = 30
        pts = []
        random.seed(i * 10)
        for j in range(n_pts):
            aj = j * 2 * math.pi / n_pts
            rj = DRAW * 0.10 * (1 + random.uniform(-0.3, 0.3))
            pts.append((fx + math.cos(aj) * rj, fy + math.sin(aj) * rj))
        alpha = int(220 * intensity)
        d.polygon(pts, fill=face_color + (alpha,))
        # 2 yeux noirs vides (orbites caves)
        for side in [-1, 1]:
            ex = fx + side * DRAW * 0.025
            ey = fy - DRAW * 0.015
            er = int(DRAW * 0.012)
            d.ellipse([ex-er, ey-er, ex+er, ey+er],
                      fill=eye_color + (alpha,))
        # Bouche : trait noir
        d.line([(fx - DRAW*0.025, fy + DRAW*0.020),
                (fx + DRAW*0.025, fy + DRAW*0.020)],
               fill=eye_color + (alpha,), width=2*SS)


def draw_tentacles(img, intensity):
    """Tentacules sombres qui sortent du centre vers l'exterieur."""
    d = ImageDraw.Draw(img, 'RGBA')
    n_tentacles = int(12 * intensity)
    random.seed(7)
    for i in range(n_tentacles):
        base_a = i * 2 * math.pi / 12 + random.uniform(-0.2, 0.2)
        n_seg = 30
        prev_r = DRAW * 0.20
        prev_x = CX + math.cos(base_a) * prev_r
        prev_y = CY + math.sin(base_a) * prev_r
        for s in range(n_seg):
            t = (s + 1) / n_seg
            r = prev_r + (DRAW * 0.5 - prev_r) * t
            wobble = math.sin(t * math.pi * 3 + i) * 0.2
            a = base_a + wobble
            x = CX + math.cos(a) * r
            y = CY + math.sin(a) * r
            # Plus epais : 10 -> 1 px
            width = max(1, int((1 - t) * 10 * SS))
            alpha = int(220 * (1 - t * 0.4) * intensity)
            d.line([(prev_x, prev_y), (x, y)],
                   fill=(50, 15, 35, alpha), width=width)
            prev_x, prev_y = x, y


def draw_floating_eyes(img, intensity, n_max=30, pulse=0.0):
    """Aureole d'yeux flottants autour (biblical angel)."""
    n_eyes = int(n_max * intensity)
    random.seed(2024)
    for _ in range(n_eyes):
        a = random.uniform(0, 2 * math.pi)
        r = random.uniform(DRAW * 0.30, DRAW * 0.45)
        x = int(CX + math.cos(a) * r)
        y = int(CY + math.sin(a) * r)
        er = random.randint(int(DRAW*0.018), int(DRAW*0.038))
        # Variation taille avec pulse
        er = int(er * (1 + pulse * 0.2))
        # Tous regardent vers le centre
        look_to_center = math.atan2(CY - y, CX - x)
        # Iris rouge sang (horror)
        iris_color = random.choice(['#7B0000', '#5B1010', '#A00020'])
        draw_eye(img, x, y, er, openness=1.0,
                 look_a=look_to_center, look_strength=0.5,
                 iris_color_hex=iris_color)


# ============================================================
# Main frame generation
# ============================================================

def generate_frame(frame_idx):
    """Genere une frame complete a partir de l'index."""
    phase, progress = get_phase_and_progress(frame_idx)
    
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    
    # === Parametres oeil selon la phase ===
    eye_openness = 1.0
    eye_pupil_dilation = 1.0
    eye_weeping = False
    eye_iris = '#3F6B4F'  # vert noisette par defaut
    
    if phase == 'iris':
        # Oeil qui s'ouvre progressivement de 0 a 1
        eye_openness = progress
    elif phase == 'crack':
        # Pupille legerement dilatee (peur)
        eye_openness = 1.0
        eye_pupil_dilation = 1.0 + progress * 0.3
    elif phase == 'faces':
        # Pupille tres dilatee
        eye_pupil_dilation = 1.3 + progress * 0.2
    elif phase == 'metamorph':
        # Iris rougit progressivement (horror)
        eye_pupil_dilation = 1.5
        # Lerp vert vers rouge
        green = (63, 107, 79)
        red = (107, 30, 30)
        c = lerp_color(green, red, progress)
        eye_iris = '#{:02X}{:02X}{:02X}'.format(*c)
    elif phase == 'entity_loop':
        # Boucle finale : pulse pupille + larmes noires + iris rouge sang
        eye_iris = '#7B0000'
        eye_pupil_dilation = 1.5 + math.sin(progress * 2 * math.pi) * 0.2
        eye_weeping = True
    
    # === Transformation autour ===
    if phase == 'iris':
        # Phase 1 : juste l'oeil qui s'ouvre, rien autour
        pass
    elif phase == 'crack':
        # Cracks intensite 0 -> 1
        draw_cracks(img, intensity=progress)
    elif phase == 'faces':
        draw_cracks(img, intensity=1.0)
        draw_faces_around(img, intensity=progress)
    elif phase == 'metamorph':
        draw_cracks(img, intensity=1.0)
        draw_faces_around(img, intensity=1.0)
        draw_tentacles(img, intensity=progress)
    elif phase == 'entity_loop':
        draw_cracks(img, intensity=1.0)
        draw_faces_around(img, intensity=1.0)
        draw_tentacles(img, intensity=1.0)
        # Pulse intensity sur les yeux flottants
        pulse = math.sin(progress * 2 * math.pi)
        draw_floating_eyes(img, intensity=1.0, n_max=30, pulse=pulse)
    
    # === Oeil principal (par-dessus tout, point d'ancrage) ===
    draw_eye(img, CX, CY, int(DRAW * 0.18),
             openness=eye_openness,
             look_a=0.4, look_strength=0.15,
             iris_color_hex=eye_iris,
             pupil_dilation=eye_pupil_dilation,
             weeping=eye_weeping)
    
    return img.resize((SIZE, SIZE), Image.LANCZOS)


# ============================================================
# MAIN
# ============================================================

if __name__ == "__main__":
    os.makedirs(OUT_DIR, exist_ok=True)
    
    print(f"=== Generation {N_FRAMES} entity frames ===\n")
    
    # Supprime les anciennes 50 frames d'abord
    for i in range(50):
        old = os.path.join(OUT_DIR, f"entity_{i}.png")
        if os.path.exists(old):
            # On va les overwriter, juste affichage
            pass
    
    for frame_idx in range(N_FRAMES):
        phase, progress = get_phase_and_progress(frame_idx)
        img = generate_frame(frame_idx)
        out_path = os.path.join(OUT_DIR, f"entity_{frame_idx}.png")
        img.save(out_path, "PNG", optimize=True)
        size_kb = os.path.getsize(out_path) / 1024
        print(f"  entity_{frame_idx:02d}.png  [{phase:<12s} {progress:.2f}]  {size_kb:.0f} KB")
    
    print(f"\n=== TERMINE - {N_FRAMES} frames generees ===")
