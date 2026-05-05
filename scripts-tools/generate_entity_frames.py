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
    """Aureole d'yeux flottants autour (biblical angel) - DEPRECATED, garde pour compat."""
    n_eyes = int(n_max * intensity)
    random.seed(2024)
    for _ in range(n_eyes):
        a = random.uniform(0, 2 * math.pi)
        r = random.uniform(DRAW * 0.30, DRAW * 0.45)
        x = int(CX + math.cos(a) * r)
        y = int(CY + math.sin(a) * r)
        er = random.randint(int(DRAW*0.018), int(DRAW*0.038))
        er = int(er * (1 + pulse * 0.2))
        look_to_center = math.atan2(CY - y, CX - x)
        iris_color = random.choice(['#7B0000', '#5B1010', '#A00020'])
        draw_eye(img, x, y, er, openness=1.0,
                 look_a=look_to_center, look_strength=0.5,
                 iris_color_hex=iris_color)


def draw_powerful_entity(img, intensity=1.0, pulse=0.0, formation=1.0,
                          breath=0.0, head_sway=0.0, star_twinkle=0.0):
    """
    Entité humanoide cyclope biblical angel POUR DE VRAI - VIVANTE.
    
    formation : 0 -> 1 progression de la formation
    pulse : oscillation 0 -> 1 pour aura/halo etoile
    breath : -1 -> 1, respiration (corps/tete pulsent en taille)
    head_sway : -1 -> 1, oscillation laterale tete/epaules
    star_twinkle : -1 -> 1, scintillement etoile (taille + rotation)
    """
    d = ImageDraw.Draw(img, 'RGBA')
    er = DRAW * 0.13
    
    # Coefficients de respiration (subtils : ±5%)
    breath_factor = 1.0 + breath * 0.05
    sway_offset = head_sway * er * 0.05  # oscillation horizontale +- 5% de er
    
    body_color = (50, 18, 40)
    body_outline = (10, 0, 15)
    aura_gold = (220, 170, 30)
    star_gold = (255, 215, 50)
    eye_red = '#7B0000'
    
    head_t = min(1.0, formation * 4.0)
    body_t = min(1.0, max(0, formation - 0.20) * 4.0)
    arms_t = min(1.0, max(0, formation - 0.40) * 4.0)
    star_t = min(1.0, max(0, formation - 0.60) * 4.0)
    aura_t = min(1.0, max(0, formation - 0.80) * 5.0)
    
    if head_t <= 0:
        return
    
    # 1. AURA DIVINE (background)
    if aura_t > 0:
        aura = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
        ad = ImageDraw.Draw(aura, 'RGBA')
        aura_r = int(DRAW * 0.42 * (1 + pulse * 0.10))
        for i in range(15):
            t = i / 15
            r = int(aura_r * (1 - t * 0.7))
            alpha = int(70 * (1 - t) * intensity * aura_t)
            ad.ellipse([CX-r, CY-r, CX+r, CY+r],
                       fill=aura_gold + (alpha,))
        n_rays = 16
        for i in range(n_rays):
            a = i * 2 * math.pi / n_rays + pulse * 0.2
            x_in = CX + math.cos(a) * DRAW * 0.20
            y_in = CY + math.sin(a) * DRAW * 0.20
            x_out = CX + math.cos(a) * DRAW * 0.48
            y_out = CY + math.sin(a) * DRAW * 0.48
            alpha = int(180 * intensity * aura_t)
            ad.line([(x_in, y_in), (x_out, y_out)],
                    fill=aura_gold + (alpha,), width=3*SS)
        aura = aura.filter(ImageFilter.GaussianBlur(radius=15))
        img.alpha_composite(aura)
        d = ImageDraw.Draw(img, 'RGBA')
    
    # 2. CORPS trapeze (avec respiration)
    if body_t > 0:
        torso_top_y = int(CY + er * 1.5)
        torso_bot_y = int(CY + er * 3.0 * body_t * breath_factor)
        torso_top_w = int(er * 1.6 * breath_factor)
        torso_bot_w = int(er * 1.2 * breath_factor)
        torso_pts = [
            (CX - torso_top_w + sway_offset, torso_top_y),
            (CX + torso_top_w + sway_offset, torso_top_y),
            (CX + torso_bot_w + sway_offset * 0.5, torso_bot_y),
            (CX - torso_bot_w + sway_offset * 0.5, torso_bot_y),
        ]
        alpha = int(230 * intensity * body_t)
        d.polygon(torso_pts, fill=body_color + (alpha,),
                  outline=body_outline + (255,))
        # 4 yeux poitrine
        n_chest_eyes = int(4 * body_t)
        for i in range(n_chest_eyes):
            ex_offset = (i - (n_chest_eyes-1)/2) * er * 0.45
            ex = int(CX + ex_offset + sway_offset)
            ey = int(torso_top_y + er * 0.4)
            erad = int(er * 0.16)
            draw_eye(img, ex, ey, erad, openness=1.0,
                     look_a=0.5, look_strength=0.3,
                     iris_color_hex=eye_red)
        d = ImageDraw.Draw(img, 'RGBA')
    
    # 3. TETE CONIQUE (avec respiration + sway)
    if head_t > 0:
        head_base_y = int(CY + er * 1.4)
        head_top_y = int(CY - er * 2.2 * head_t * breath_factor)
        head_w = int(er * 1.5 * breath_factor)
        head_pts = [
            (CX - head_w + sway_offset, head_base_y),
            (CX + head_w + sway_offset, head_base_y),
            (CX + sway_offset * 1.5, head_top_y),  # pointe plus deviee
        ]
        alpha = int(230 * intensity * head_t)
        d.polygon(head_pts, fill=body_color + (alpha,),
                  outline=body_outline + (255,))
    
    # 4. BRAS (avec sway)
    right_hand_x = right_hand_y = right_hand_r = None
    if arms_t > 0:
        torso_top_y = int(CY + er * 1.5)
        torso_top_w = int(er * 1.6 * breath_factor)
        for side in [-1, 1]:
            shoulder_x = CX + side * torso_top_w + int(sway_offset)
            shoulder_y = torso_top_y
            elbow_x = shoulder_x + side * int(er * 1.0 * arms_t)
            elbow_y = shoulder_y + int(er * 1.2 * arms_t)
            hand_x = elbow_x + side * int(er * 0.5 * arms_t)
            hand_y = elbow_y + int(er * 1.0 * arms_t)
            arm_w = int(er * 0.30)
            d.polygon([
                (shoulder_x - arm_w, shoulder_y),
                (shoulder_x + arm_w, shoulder_y),
                (elbow_x + arm_w * side, elbow_y + arm_w),
                (elbow_x - arm_w * side, elbow_y - arm_w),
            ], fill=body_color + (int(230 * arms_t),),
               outline=body_outline + (255,))
            d.polygon([
                (elbow_x + arm_w * side, elbow_y + arm_w),
                (elbow_x - arm_w * side, elbow_y - arm_w),
                (hand_x - arm_w * 0.7, hand_y - arm_w * 0.7),
                (hand_x + arm_w * 0.7, hand_y + arm_w * 0.7),
            ], fill=body_color + (int(230 * arms_t),),
               outline=body_outline + (255,))
            hand_r = int(er * 0.28)
            d.ellipse([hand_x - hand_r, hand_y - hand_r,
                       hand_x + hand_r, hand_y + hand_r],
                      fill=body_color + (int(230 * arms_t),),
                      outline=body_outline + (255,))
            if side > 0:
                right_hand_x = hand_x
                right_hand_y = hand_y
                right_hand_r = hand_r
    
    # 5. ETOILE D'OR avec scintillement (taille + rotation)
    if star_t > 0 and right_hand_x is not None:
        sx = right_hand_x
        sy = right_hand_y
        # Scintillement : taille varie ±15%, rotation continue
        twinkle_scale = 1.0 + star_twinkle * 0.15
        sr = int(right_hand_r * 1.6 * star_t * twinkle_scale)
        # Halo dore
        halo = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
        hd = ImageDraw.Draw(halo, 'RGBA')
        halo_r = int(sr * 2.5 * (1 + pulse * 0.3))
        for i in range(10):
            t = i / 10
            r = int(halo_r * (1 - t * 0.7))
            alpha = int(180 * (1 - t) * star_t * intensity)
            hd.ellipse([sx-r, sy-r, sx+r, sy+r],
                       fill=star_gold + (alpha,))
        halo = halo.filter(ImageFilter.GaussianBlur(radius=15))
        img.alpha_composite(halo)
        d = ImageDraw.Draw(img, 'RGBA')
        # Etoile : rotation basée sur star_twinkle
        star_rot = star_twinkle * math.pi / 6  # ±30 deg
        star_pts = []
        for i in range(10):
            a = -math.pi/2 + i * math.pi / 5 + star_rot
            r = sr if i % 2 == 0 else sr * 0.4
            star_pts.append((sx + math.cos(a) * r, sy + math.sin(a) * r))
        d.polygon(star_pts, fill=star_gold + (int(255 * star_t),),
                  outline=(255, 255, 200, int(255 * star_t)))


# ============================================================
# Main frame generation
# ============================================================

def generate_frame(frame_idx):
    """Genere une frame complete a partir de l'index. TOUT EST VIVANT.
    
    Animations basees sur frame_idx :
    - Oeil qui regarde dans differentes directions (look_a varie en sin)
    - Pupille qui se dilate/contracte (battement organique)
    - Clignements occasionnels (frames 25, 38, 53)
    - Entite qui respire (corps/tete pulsent en taille)
    - Tete qui oscille legerement (head sway)
    - Etoile d'or qui scintille (taille + rotation continue)
    - Aura qui pulse (deja en place)
    """
    phase, progress = get_phase_and_progress(frame_idx)
    
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    
    # === ANIMATIONS GLOBALES (basees sur frame_idx pour mouvement vivant) ===
    t = frame_idx * 1.0  # phase de temps
    
    # Mouvement de l'oeil : balaye dans differentes directions au fil du temps
    # look_a oscille entre -pi et pi en sin lent
    eye_look_a = math.sin(t * 0.18) * math.pi  # cycle complet ~35 frames
    eye_look_strength = 0.10 + abs(math.sin(t * 0.24)) * 0.20  # 0.10 -> 0.30
    
    # Pupille pulse organique (battement a frequence intermediaire)
    pupil_breath = 1.0 + math.sin(t * 0.45) * 0.08
    
    # Clignements : tres rapides (1 frame) sur frames specifiques
    # Eviter en phase IRIS (oeil deja en train de s'ouvrir)
    blink_frames = {25, 38, 53}  # 3 clignements bien espaces
    is_blinking = frame_idx in blink_frames
    
    # Animations entite (toutes en sin pour boucler proprement)
    entity_breath = math.sin(t * 0.30)         # respiration -1 -> 1
    entity_head_sway = math.sin(t * 0.22)      # oscillation tete -1 -> 1
    entity_star_twinkle = math.sin(t * 0.55)   # scintillement etoile -1 -> 1
    
    # === Parametres oeil selon la phase ===
    eye_openness = 1.0
    eye_pupil_dilation = 1.0
    eye_weeping = False
    eye_iris = '#3F6B4F'
    
    if phase == 'iris':
        eye_openness = progress  # ouverture progressive
        # Pas de mouvement encore (oeil pas encore ouvert)
        eye_look_a = 0.4
        eye_look_strength = 0.15
    elif phase == 'crack':
        eye_openness = 0.1 if is_blinking else 1.0
        eye_pupil_dilation = (1.0 + progress * 0.3) * pupil_breath
    elif phase == 'faces':
        eye_openness = 0.1 if is_blinking else 1.0
        eye_pupil_dilation = (1.3 + progress * 0.2) * pupil_breath
    elif phase == 'metamorph':
        eye_openness = 0.1 if is_blinking else 1.0
        eye_pupil_dilation = 1.5 * pupil_breath
        green = (63, 107, 79)
        red = (107, 30, 30)
        c = lerp_color(green, red, progress)
        eye_iris = '#{:02X}{:02X}{:02X}'.format(*c)
    elif phase == 'entity_loop':
        eye_iris = '#7B0000'
        # Pupille pulse plus marque pendant entity_loop
        eye_pupil_dilation = (1.5 + math.sin(progress * 2 * math.pi) * 0.2) * pupil_breath
        eye_weeping = True
        eye_openness = 0.1 if is_blinking else 1.0
    
    # === Transformation autour ===
    if phase == 'iris':
        pass
    elif phase == 'crack':
        # Cracks bougent : seed varie subtilement avec frame
        draw_cracks(img, intensity=progress, seed=99 + (frame_idx // 4))
    elif phase == 'faces':
        draw_cracks(img, intensity=1.0, seed=99 + (frame_idx // 4))
        draw_faces_around(img, intensity=progress)
    elif phase == 'metamorph':
        draw_cracks(img, intensity=1.0, seed=99 + (frame_idx // 4))
        draw_faces_around(img, intensity=max(0, 1.0 - progress * 1.5))
        draw_tentacles(img, intensity=max(0, 1.0 - progress * 1.5))
        draw_powerful_entity(img, intensity=1.0, pulse=0.0,
                             formation=progress,
                             breath=entity_breath, head_sway=entity_head_sway,
                             star_twinkle=entity_star_twinkle)
    elif phase == 'entity_loop':
        draw_cracks(img, intensity=1.0, seed=99 + (frame_idx // 4))
        pulse = (math.sin(progress * 2 * math.pi) + 1) / 2
        draw_powerful_entity(img, intensity=1.0, pulse=pulse,
                             formation=1.0,
                             breath=entity_breath, head_sway=entity_head_sway,
                             star_twinkle=entity_star_twinkle)
    
    # === Oeil principal (par-dessus tout, point d'ancrage VIVANT) ===
    eye_size = int(DRAW * 0.13)
    draw_eye(img, CX, CY, eye_size,
             openness=eye_openness,
             look_a=eye_look_a, look_strength=eye_look_strength,
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
