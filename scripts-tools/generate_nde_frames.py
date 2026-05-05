#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Genere les 60 frames de la Near Death Experience (NDE) du Cartouche Manifold.

Cette phase intervient APRES les 240 frames d'entity (Stage 5 PEAK), comme
6eme phase finale one-shot avant le retour a la realite.

Distribution 60 frames NDE :
  Decorporation (10) : 240-249 - l'entite s'eloigne dans la nebuleuse cosmique
  Hyperspace    (20) : 250-269 - tunnel kaleidoscopique DMT en mouvement
  Crystal Palace(10) : 270-279 - Hall of Masters, geometrie sacree
  Past Lives    (10) : 280-289 - silhouettes humaines en spirale temporelle
  Blackout      (10) : 290-299 - tout devient noir progressivement

Effets painterly ULTRA appliques sur chaque frame :
  - Distortion organique (warping noise-based, casse les contours geometriques)
  - Color bleeding (multi-pass blur, effet aquarelle)
  - Brush strokes (1000 ovales colores sampled depuis l'image)
  - Cosmic noise overlay (nebuleuse coloree multi-octave)
  - Chromatic aberration (decalage RGB, effet 'vibration retina DMT')
  - Grain final (texture peinture/film)

Ces effets ne sont PAS appliques aux 240 frames d'entity precedentes -
elles gardent leur style geometrique propre. Les 60 frames NDE marquent
une rupture stylistique deliberee : le passage du visuel 'entite' au
visuel 'experience mystique' et 'mort imminente'.

Source images des frames NDE : entity_240.png a entity_299.png
"""
import os
import math
import random
import sys
from PIL import Image, ImageDraw, ImageFilter
import numpy as np

# Reuse base utilities from existing generator
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from generate_entity_frames import (
    draw_eye, draw_powerful_entity, hex_to_rgb, lerp_color,
    SIZE, OUT_DIR
)

# Override DRAW : pas de supersampling pour les NDE (les effets de distortion
# + brush strokes + grain compensent l'absence d'antialiasing 2x).
# Reduit la memoire necessaire de 4x : 2048x2048 RGBA (16 MB) -> 1024x1024 (4 MB).
DRAW = SIZE  # 1024 (au lieu de 2048)
CX = CY = DRAW // 2
SS = 1  # super-sampling factor (etait 2 dans generate_entity_frames)

# Ranges des sous-phases NDE
NDE_RANGES = {
    'decorporation': (240, 249),  # 10 frames
    'hyperspace':    (250, 269),  # 20 frames
    'crystal_palace':(270, 279),  # 10 frames
    'past_lives':    (280, 289),  # 10 frames
    'blackout':      (290, 299),  # 10 frames
}

NDE_PALETTE = {
    'gold':         (255, 215, 100),
    'gold_d':       (200, 160, 50),
    'gold_l':       (255, 235, 180),
    'white':        (255, 255, 255),
    'cream':        (255, 245, 220),
    'magenta':      (255, 0, 200),
    'magenta_d':    (180, 0, 140),
    'cyan':         (0, 240, 255),
    'cyan_d':       (0, 160, 200),
    'violet':       (160, 30, 255),
    'violet_d':     (100, 10, 180),
    'green_acid':   (100, 255, 80),
    'green_d':      (50, 180, 40),
    'rose':         (255, 100, 200),
    'rose_d':       (200, 60, 150),
    'black':        (5, 0, 10),
    'blue_deep':    (40, 30, 100),
    'blue_l':       (100, 130, 255),
    'orange':       (255, 140, 60),
}


# ============================================================
# Painterly ULTRA effects
# ============================================================

def make_noise_field(size, scale=20, octaves=3, seed=42):
    """Multi-octave random noise field (poor-man's Perlin)."""
    np.random.seed(seed)
    final = np.zeros((size, size), dtype=np.float32)
    for octave in range(octaves):
        oct_scale = scale * (2 ** octave)
        low_size = max(2, size // oct_scale)
        low_noise = np.random.randn(low_size, low_size).astype(np.float32)
        low_img = Image.fromarray((low_noise * 50 + 128).clip(0, 255).astype(np.uint8))
        upsized = low_img.resize((size, size), Image.LANCZOS)
        upsized = upsized.filter(ImageFilter.GaussianBlur(radius=oct_scale//2))
        arr = np.array(upsized, dtype=np.float32)
        final += arr / (2 ** octave)
    final = (final - final.min()) / (final.max() - final.min())
    return final


def cosmic_noise_overlay(img):
    """Nebuleuse cosmique coloree appliquee en overlay."""
    arr = np.array(img.convert('RGBA'))
    h, w = arr.shape[:2]
    noise_low = make_noise_field(h, scale=40, octaves=4, seed=42)
    noise_high = make_noise_field(h, scale=8, octaves=2, seed=99)
    combined = (noise_low * 0.7 + noise_high * 0.3).clip(0, 1)
    
    color_overlay = np.zeros((h, w, 3), dtype=np.float32)
    mask_dark = combined < 0.3
    mask_med = (combined >= 0.3) & (combined < 0.6)
    mask_bright = (combined >= 0.6) & (combined < 0.85)
    mask_very_bright = combined >= 0.85
    
    t_dark = combined / 0.3
    color_overlay[..., 0] = np.where(mask_dark, 20 + t_dark * 40, color_overlay[..., 0])
    color_overlay[..., 1] = np.where(mask_dark, 10 + t_dark * 20, color_overlay[..., 1])
    color_overlay[..., 2] = np.where(mask_dark, 50 + t_dark * 100, color_overlay[..., 2])
    
    t_med = (combined - 0.3) / 0.3
    color_overlay[..., 0] = np.where(mask_med, 60 + t_med * 200, color_overlay[..., 0])
    color_overlay[..., 1] = np.where(mask_med, 30 + t_med * 50, color_overlay[..., 1])
    color_overlay[..., 2] = np.where(mask_med, 150 + t_med * 80, color_overlay[..., 2])
    
    t_br = (combined - 0.6) / 0.25
    color_overlay[..., 0] = np.where(mask_bright, 255 - t_br * 150, color_overlay[..., 0])
    color_overlay[..., 1] = np.where(mask_bright, 80 + t_br * 175, color_overlay[..., 1])
    color_overlay[..., 2] = np.where(mask_bright, 230 + t_br * 25, color_overlay[..., 2])
    
    t_vbr = (combined - 0.85) / 0.15
    color_overlay[..., 0] = np.where(mask_very_bright, 105 + t_vbr * 150, color_overlay[..., 0])
    color_overlay[..., 1] = np.where(mask_very_bright, 255, color_overlay[..., 1])
    color_overlay[..., 2] = np.where(mask_very_bright, 255, color_overlay[..., 2])
    
    color_overlay = color_overlay.clip(0, 255).astype(np.uint8)
    alpha = (combined * 100).clip(0, 100).astype(np.uint8)
    overlay_rgba = np.dstack([color_overlay, alpha])
    overlay_img = Image.fromarray(overlay_rgba, 'RGBA')
    if img.mode != 'RGBA':
        img = img.convert('RGBA')
    img.alpha_composite(overlay_img)
    return img


def chromatic_aberration(img, offset=5):
    """Decalage R/G/B = effet vibration retina DMT."""
    arr = np.array(img.convert('RGBA'))
    r = arr[..., 0]; g = arr[..., 1]; b = arr[..., 2]; a = arr[..., 3]
    r_shifted = np.roll(np.roll(r, offset, axis=1), offset // 2, axis=0)
    b_shifted = np.roll(np.roll(b, -offset, axis=1), -offset // 2, axis=0)
    result = np.stack([r_shifted, g, b_shifted, a], axis=-1)
    return Image.fromarray(result, 'RGBA')


def apply_distortion(img, strength=10):
    """Warping organique noise-based."""
    arr = np.array(img)
    h, w = arr.shape[:2]
    noise_x = make_noise_field(h, scale=30, octaves=3, seed=11) * 2 - 1
    noise_y = make_noise_field(h, scale=30, octaves=3, seed=22) * 2 - 1
    yy, xx = np.meshgrid(np.arange(h), np.arange(w), indexing='ij')
    new_xx = (xx + noise_x * strength).astype(np.int32).clip(0, w-1)
    new_yy = (yy + noise_y * strength).astype(np.int32).clip(0, h-1)
    if arr.ndim == 3:
        result = arr[new_yy, new_xx]
    else:
        result = arr[new_yy, new_xx]
    return Image.fromarray(result)


def apply_color_bleeding(img, blur_radius=3):
    """Multi-pass blur effet aquarelle."""
    versions = [img]
    for r in [blur_radius, blur_radius * 2, blur_radius * 4, blur_radius * 6]:
        versions.append(img.filter(ImageFilter.GaussianBlur(radius=r)))
    result = versions[0].copy()
    blur_med = Image.blend(result, versions[1], 0.35)
    blur_large = Image.blend(blur_med, versions[2], 0.20)
    blur_huge = Image.blend(blur_large, versions[3], 0.10)
    return blur_huge


def apply_grain(img, intensity=12):
    """Grain final."""
    arr = np.array(img, dtype=np.int16)
    h, w = arr.shape[:2]
    np.random.seed(7)
    noise = np.random.randint(-intensity, intensity, size=(h, w), dtype=np.int16)
    if arr.ndim == 3:
        for c in range(3):
            arr[..., c] = (arr[..., c] + noise).clip(0, 255)
    return Image.fromarray(arr.astype(np.uint8))


def apply_brush_strokes(img, n_strokes=1000):
    """1000 ovales colores sampled = effet coups de pinceau."""
    arr = np.array(img)
    h, w = arr.shape[:2]
    overlay = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay, 'RGBA')
    random.seed(99)
    for _ in range(n_strokes):
        x = random.randint(0, w-1)
        y = random.randint(0, h-1)
        if arr.ndim == 3 and arr.shape[2] >= 3:
            r, g, b = arr[y, x, 0], arr[y, x, 1], arr[y, x, 2]
        else:
            continue
        size_x = random.randint(2, 10) * SS
        size_y = random.randint(2, 6) * SS
        alpha = random.randint(50, 130)
        od.ellipse([x - size_x, y - size_y, x + size_x, y + size_y],
                   fill=(int(r), int(g), int(b), alpha))
    if img.mode != 'RGBA':
        img = img.convert('RGBA')
    img.alpha_composite(overlay)
    return img


def apply_painterly_ULTRA(img):
    """Pipeline complet : tous les effets dans l'ordre."""
    img = apply_distortion(img, strength=10)
    img = apply_color_bleeding(img, blur_radius=3)
    img = apply_brush_strokes(img, n_strokes=1000)
    img = cosmic_noise_overlay(img)
    img = chromatic_aberration(img, offset=5)
    img = apply_grain(img, intensity=12)
    return img


# ============================================================
# Sub-phase drawing (geometric base, before painterly)
# ============================================================

def draw_decorporation(img, progress):
    """L'entite s'eloigne dans la nebuleuse cosmique.
    progress 0->1 : entite normale -> entite reduite au loin."""
    d = ImageDraw.Draw(img, 'RGBA')
    
    # Background : nebuleuse cosmique
    nebula = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    nd = ImageDraw.Draw(nebula, 'RGBA')
    random.seed(99)
    for _ in range(8):
        cx_n = random.randint(0, DRAW)
        cy_n = random.randint(0, DRAW)
        radius_n = random.randint(int(DRAW*0.15), int(DRAW*0.30))
        color = random.choice([NDE_PALETTE['violet_d'], NDE_PALETTE['blue_deep'],
                               NDE_PALETTE['magenta_d'], NDE_PALETTE['cyan_d']])
        for i in range(15):
            t = i / 15
            r = int(radius_n * (1 - t))
            alpha = int(60 * (1 - t))
            nd.ellipse([cx_n-r, cy_n-r, cx_n+r, cy_n+r], fill=color + (alpha,))
    nebula = nebula.filter(ImageFilter.GaussianBlur(radius=40))
    img.alpha_composite(nebula)
    d = ImageDraw.Draw(img, 'RGBA')
    
    # 200 etoiles
    random.seed(42)
    for _ in range(200):
        x = random.randint(0, DRAW)
        y = random.randint(0, DRAW)
        sz = random.choice([1, 1, 2, 2, 3]) if random.random() < 0.85 else random.randint(3, 5)
        color = random.choice([NDE_PALETTE['white']] * 6 + [NDE_PALETTE['gold_l'],
                              NDE_PALETTE['blue_l'], NDE_PALETTE['rose']])
        a = random.randint(150, 255)
        d.ellipse([x-sz, y-sz, x+sz, y+sz], fill=color + (a,))
        if sz >= 3:
            for hr in range(sz+2, sz+6):
                d.ellipse([x-hr, y-hr, x+hr, y+hr], outline=color + (40,), width=1)
    
    # Galaxies
    random.seed(123)
    for _ in range(3):
        gx = random.randint(int(DRAW*0.15), int(DRAW*0.85))
        gy = random.randint(int(DRAW*0.15), int(DRAW*0.85))
        if abs(gx - CX) < DRAW*0.20 and abs(gy - CY) < DRAW*0.20:
            continue
        rotation = random.uniform(0, 2*math.pi)
        gal_color = random.choice([NDE_PALETTE['gold_l'], NDE_PALETTE['rose'],
                                    NDE_PALETTE['cyan']])
        for j in range(80):
            t = j / 80
            angle = t * 4 * math.pi + rotation
            radius = t * DRAW * 0.06
            sx = int(gx + math.cos(angle) * radius)
            sy = int(gy + math.sin(angle) * radius)
            sz = max(1, int(2 * (1 - t)))
            d.ellipse([sx-sz, sy-sz, sx+sz, sy+sz], fill=gal_color + (180,))
    
    # Mini entite au centre (taille decroissante avec progress)
    sub_size = max(int(DRAW * 0.30), int(DRAW * (1.0 - progress * 0.7)))
    sub_img = Image.new('RGBA', (sub_size, sub_size), (0, 0, 0, 0))
    sub_cx = sub_cy = sub_size // 2
    sub_eye_radius = int(sub_size * 0.13)
    
    halo_intensity = max(0.3, 1.0 - progress * 0.5)
    halo = Image.new('RGBA', (sub_size, sub_size), (0, 0, 0, 0))
    hd = ImageDraw.Draw(halo, 'RGBA')
    halo_r = int(sub_size * 0.40 * halo_intensity)
    for i in range(15):
        t = i / 15
        r = int(halo_r * (1 - t * 0.7))
        alpha = int(80 * (1 - t) * halo_intensity)
        hd.ellipse([sub_cx-r, sub_cy-r, sub_cx+r, sub_cy+r],
                   fill=NDE_PALETTE['gold'] + (alpha,))
    halo = halo.filter(ImageFilter.GaussianBlur(radius=10))
    sub_img.alpha_composite(halo)
    sd = ImageDraw.Draw(sub_img, 'RGBA')
    
    # Entite simplifiee
    er_sub = sub_size * 0.13
    head_pts = [
        (sub_cx - int(er_sub * 1.5), int(sub_cy + er_sub * 1.4)),
        (sub_cx + int(er_sub * 1.5), int(sub_cy + er_sub * 1.4)),
        (sub_cx, int(sub_cy - er_sub * 2.2)),
    ]
    sd.polygon(head_pts, fill=(50, 18, 40, int(220 * halo_intensity)),
               outline=(10, 0, 15, 255))
    sd.polygon([
        (sub_cx - int(er_sub * 1.6), int(sub_cy + er_sub * 1.5)),
        (sub_cx + int(er_sub * 1.6), int(sub_cy + er_sub * 1.5)),
        (sub_cx + int(er_sub * 1.2), int(sub_cy + er_sub * 3.0)),
        (sub_cx - int(er_sub * 1.2), int(sub_cy + er_sub * 3.0)),
    ], fill=(50, 18, 40, int(220 * halo_intensity)),
       outline=(10, 0, 15, 255))
    
    draw_eye(sub_img, sub_cx, sub_cy, sub_eye_radius,
             openness=1.0, look_a=0, look_strength=0,
             iris_color_hex='#7B0000', pupil_dilation=2.0, weeping=True)
    
    paste_x = (DRAW - sub_size) // 2
    paste_y = (DRAW - sub_size) // 2
    img.alpha_composite(sub_img, (paste_x, paste_y))


def draw_hyperspace(img, progress):
    """Tunnel kaleidoscopique DMT en mouvement."""
    d = ImageDraw.Draw(img, 'RGBA')
    palette = [NDE_PALETTE['magenta'], NDE_PALETTE['cyan'], NDE_PALETTE['gold'],
               NDE_PALETTE['violet'], NDE_PALETTE['green_acid'], NDE_PALETTE['rose'],
               NDE_PALETTE['orange']]
    
    n_rings = 60
    max_r = int(DRAW * 0.55)
    for i in range(n_rings):
        ring_offset = (progress * n_rings + i) % n_rings
        r = int(max_r * (1 - ring_offset / n_rings))
        if r < 5: continue
        color = palette[i % len(palette)]
        alpha = int(180 * (1 - ring_offset / n_rings) ** 0.5)
        for offset in [-2, 0, 2]:
            d.ellipse([CX-r-offset, CY-r-offset, CX+r+offset, CY+r+offset],
                      outline=color + (alpha,), width=1*SS)
    
    n_rays = 48
    for i in range(n_rays):
        a = i * 2 * math.pi / n_rays + progress * math.pi * 0.2
        ray_color = palette[i % len(palette)]
        x_in = CX + math.cos(a) * DRAW * 0.05
        y_in = CY + math.sin(a) * DRAW * 0.05
        x_out = CX + math.cos(a) * DRAW * 0.55
        y_out = CY + math.sin(a) * DRAW * 0.55
        alpha = int(150 + progress * 80)
        d.line([(x_in, y_in), (x_out, y_out)],
               fill=ray_color + (alpha,), width=1*SS)
        for sub_r_factor in [0.20, 0.35, 0.50]:
            mid_r = DRAW * sub_r_factor
            mx = CX + math.cos(a) * mid_r
            my = CY + math.sin(a) * mid_r
            perp_a = a + math.pi/2
            sub_len = DRAW * 0.025
            d.line([(mx - math.cos(perp_a) * sub_len, my - math.sin(perp_a) * sub_len),
                    (mx + math.cos(perp_a) * sub_len, my + math.sin(perp_a) * sub_len)],
                   fill=ray_color + (200,), width=1*SS)
    
    mandala_radii = [0.06, 0.10, 0.15, 0.22, 0.30]
    petal_counts = [6, 8, 12, 16, 24]
    for level, (rf, np_) in enumerate(zip(mandala_radii, petal_counts)):
        outer_r = int(DRAW * rf)
        inner_r = int(outer_r * 0.5)
        for i in range(np_):
            a = i * 2 * math.pi / np_ + progress * math.pi * (1 + level * 0.3)
            a_next = (i + 1) * 2 * math.pi / np_ + progress * math.pi * (1 + level * 0.3)
            mid = (a + a_next) / 2
            tip_in = (CX + math.cos(mid) * inner_r, CY + math.sin(mid) * inner_r)
            tip_out = (CX + math.cos(mid) * outer_r, CY + math.sin(mid) * outer_r)
            side1 = (CX + math.cos(a) * (inner_r + outer_r) / 2,
                     CY + math.sin(a) * (inner_r + outer_r) / 2)
            side2 = (CX + math.cos(a_next) * (inner_r + outer_r) / 2,
                     CY + math.sin(a_next) * (inner_r + outer_r) / 2)
            color = palette[(i + level) % len(palette)]
            alpha = 200 - level * 20
            d.polygon([tip_in, side1, tip_out, side2],
                      fill=color + (alpha,), outline=NDE_PALETTE['white'] + (100,))
    
    # Glow central
    glow = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow, 'RGBA')
    for r_factor, alpha in [(0.10, 80), (0.06, 150), (0.03, 230), (0.015, 255)]:
        r = int(DRAW * r_factor)
        gd.ellipse([CX-r, CY-r, CX+r, CY+r], fill=NDE_PALETTE['white'] + (alpha,))
    glow = glow.filter(ImageFilter.GaussianBlur(radius=8))
    img.alpha_composite(glow)
    d = ImageDraw.Draw(img, 'RGBA')
    
    # Particules
    random.seed(int(progress * 100) + 1)
    for _ in range(80):
        a = random.uniform(0, 2 * math.pi)
        r = random.uniform(0, DRAW * 0.50)
        x = int(CX + math.cos(a) * r)
        y = int(CY + math.sin(a) * r)
        sz = random.choice([1, 1, 2, 2, 3])
        color = random.choice(palette + [NDE_PALETTE['white']])
        alpha = random.randint(180, 255)
        d.ellipse([x-sz, y-sz, x+sz, y+sz], fill=color + (alpha,))


def draw_crystal_palace(img, progress):
    """Hall of Masters - structure geometrique sacree."""
    d = ImageDraw.Draw(img, 'RGBA')
    
    halo = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    hd = ImageDraw.Draw(halo, 'RGBA')
    for i in range(25):
        t = i / 25
        r = int(DRAW * 0.55 * (1 - t * 0.5))
        alpha = int(150 * (1 - t * 0.7))
        hd.ellipse([CX-r, CY-r, CX+r, CY+r], fill=NDE_PALETTE['gold'] + (alpha,))
    halo = halo.filter(ImageFilter.GaussianBlur(radius=40))
    img.alpha_composite(halo)
    d = ImageDraw.Draw(img, 'RGBA')
    
    # Hexadecagone exterieur
    n_sides_outer = 16
    pts_outer = [(CX + math.cos(i * 2*math.pi/n_sides_outer - math.pi/2) * DRAW * 0.45,
                  CY + math.sin(i * 2*math.pi/n_sides_outer - math.pi/2) * DRAW * 0.45)
                 for i in range(n_sides_outer)]
    d.polygon(pts_outer, outline=NDE_PALETTE['gold'] + (255,), width=4*SS)
    
    pts_mid = [(CX + math.cos(i * math.pi/4 - math.pi/8) * DRAW * 0.32,
                CY + math.sin(i * math.pi/4 - math.pi/8) * DRAW * 0.32) for i in range(8)]
    d.polygon(pts_mid, outline=NDE_PALETTE['gold_l'] + (255,), width=3*SS)
    
    # 16 piliers
    for i in range(n_sides_outer):
        a = i * 2*math.pi/n_sides_outer - math.pi/2
        x_in = CX + math.cos(a) * DRAW * 0.10
        y_in = CY + math.sin(a) * DRAW * 0.10
        x_out = CX + math.cos(a) * DRAW * 0.45
        y_out = CY + math.sin(a) * DRAW * 0.45
        d.line([(x_in, y_in), (x_out, y_out)], fill=NDE_PALETTE['cream'] + (200,), width=2*SS)
        d.ellipse([x_out - DRAW*0.012, y_out - DRAW*0.012,
                   x_out + DRAW*0.012, y_out + DRAW*0.012],
                  fill=NDE_PALETTE['gold_l'] + (220,),
                  outline=NDE_PALETTE['white'] + (255,))
    
    # Etoile a 16 branches
    n_star_pts = 32
    star_pts = []
    for i in range(n_star_pts):
        a = i * 2 * math.pi / n_star_pts - math.pi/2
        r = DRAW * (0.18 if i % 2 == 0 else 0.10)
        star_pts.append((CX + math.cos(a) * r, CY + math.sin(a) * r))
    d.polygon(star_pts, fill=NDE_PALETTE['gold_l'] + (180,),
              outline=NDE_PALETTE['white'] + (255,))
    
    # Hexagramme
    hex_pts1 = [(CX + math.cos(i * math.pi/3 - math.pi/2) * DRAW * 0.13,
                 CY + math.sin(i * math.pi/3 - math.pi/2) * DRAW * 0.13) for i in range(3)]
    hex_pts2 = [(CX + math.cos(i * math.pi/3 - math.pi/2 + math.pi/3) * DRAW * 0.13,
                 CY + math.sin(i * math.pi/3 - math.pi/2 + math.pi/3) * DRAW * 0.13) for i in range(3)]
    d.polygon(hex_pts1, outline=NDE_PALETTE['gold'] + (255,), width=3*SS)
    d.polygon(hex_pts2, outline=NDE_PALETTE['gold'] + (255,), width=3*SS)
    
    # 16 Masters
    n_masters = 16
    for i in range(n_masters):
        a = i * 2 * math.pi / n_masters - math.pi / 2
        mx = int(CX + math.cos(a) * DRAW * 0.38)
        my = int(CY + math.sin(a) * DRAW * 0.38)
        master_halo = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
        mhd = ImageDraw.Draw(master_halo, 'RGBA')
        for rf, al in [(0.040, 80), (0.025, 150), (0.015, 220)]:
            r = int(DRAW * rf)
            mhd.ellipse([mx-r, my-r, mx+r, my+r], fill=NDE_PALETTE['gold_l'] + (al,))
        master_halo = master_halo.filter(ImageFilter.GaussianBlur(radius=10))
        img.alpha_composite(master_halo)
        d = ImageDraw.Draw(img, 'RGBA')
        head_r = int(DRAW * 0.014)
        d.ellipse([mx - head_r, my - DRAW*0.040,
                   mx + head_r, my - DRAW*0.040 + 2*head_r],
                  fill=NDE_PALETTE['white'] + (255,),
                  outline=NDE_PALETTE['gold'] + (255,))
        body_top = my - DRAW*0.040 + 2*head_r
        d.polygon([
            (mx - DRAW*0.010, body_top),
            (mx + DRAW*0.010, body_top),
            (mx + DRAW*0.020, my + DRAW*0.020),
            (mx - DRAW*0.020, my + DRAW*0.020),
        ], fill=NDE_PALETTE['white'] + (220,), outline=NDE_PALETTE['gold'] + (255,))
    
    # Lumiere centrale
    center_glow = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    cgd = ImageDraw.Draw(center_glow, 'RGBA')
    for r_factor, alpha in [(0.06, 100), (0.04, 200), (0.02, 255)]:
        r = int(DRAW * r_factor)
        cgd.ellipse([CX-r, CY-r, CX+r, CY+r], fill=NDE_PALETTE['white'] + (alpha,))
    center_glow = center_glow.filter(ImageFilter.GaussianBlur(radius=6))
    img.alpha_composite(center_glow)


def draw_past_lives(img, progress):
    """12 silhouettes humaines en spirale temporelle."""
    d = ImageDraw.Draw(img, 'RGBA')
    
    # Background violet
    bg = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    bgd = ImageDraw.Draw(bg, 'RGBA')
    for i in range(20):
        t = i / 20
        r = int(DRAW * 0.55 * (1 - t * 0.5))
        alpha = int(80 * (1 - t))
        bgd.ellipse([CX-r, CY-r, CX+r, CY+r], fill=NDE_PALETTE['violet_d'] + (alpha,))
    bg = bg.filter(ImageFilter.GaussianBlur(radius=50))
    img.alpha_composite(bg)
    d = ImageDraw.Draw(img, 'RGBA')
    
    # Etoiles
    random.seed(42)
    for _ in range(80):
        x = random.randint(0, DRAW)
        y = random.randint(0, DRAW)
        sz = random.choice([1, 1, 2])
        d.ellipse([x-sz, y-sz, x+sz, y+sz],
                  fill=NDE_PALETTE['white'] + (random.randint(120, 200),))
    
    # 12 silhouettes en spirale
    eras = [
        (0.05, NDE_PALETTE['orange'], 'crouch'),
        (0.13, NDE_PALETTE['orange'], 'standing'),
        (0.21, NDE_PALETTE['gold'], 'standing'),
        (0.29, NDE_PALETTE['gold'], 'arms_up'),
        (0.37, NDE_PALETTE['rose'], 'standing'),
        (0.45, NDE_PALETTE['rose'], 'fight'),
        (0.53, NDE_PALETTE['cyan'], 'standing'),
        (0.61, NDE_PALETTE['cyan'], 'walking'),
        (0.69, NDE_PALETTE['green_acid'], 'arms_up'),
        (0.77, NDE_PALETTE['violet'], 'standing'),
        (0.85, NDE_PALETTE['magenta'], 'arms_up'),
        (0.93, NDE_PALETTE['white'], 'arms_up'),
    ]
    
    for ratio, color, pose in eras:
        angle = ratio * 4 * math.pi - math.pi/2
        radius = DRAW * 0.10 + ratio * DRAW * 0.30
        sx = int(CX + math.cos(angle) * radius)
        sy = int(CY + math.sin(angle) * radius)
        sz = int(DRAW * 0.06 * (1 - ratio * 0.4))
        
        # Halo
        halo_l = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
        hld = ImageDraw.Draw(halo_l, 'RGBA')
        halo_r = int(sz * 0.45)
        for j in range(8):
            t = j / 8
            r = int(halo_r * (1 - t * 0.6))
            alpha = int(120 * (1 - t))
            hld.ellipse([sx-r, sy-r, sx+r, sy+r], fill=color + (alpha,))
        halo_l = halo_l.filter(ImageFilter.GaussianBlur(radius=8))
        img.alpha_composite(halo_l)
        d = ImageDraw.Draw(img, 'RGBA')
        
        # Tete
        head_r = int(sz * 0.14)
        head_y = sy - int(sz * 0.45)
        d.ellipse([sx - head_r, head_y - head_r,
                   sx + head_r, head_y + head_r],
                  fill=NDE_PALETTE['white'] + (220,),
                  outline=color + (255,), width=2*SS)
        
        # Corps selon pose
        body_top = head_y + head_r
        if pose == 'arms_up':
            d.polygon([
                (sx - int(sz*0.10), body_top),
                (sx + int(sz*0.10), body_top),
                (sx + int(sz*0.15), sy + int(sz*0.40)),
                (sx - int(sz*0.15), sy + int(sz*0.40)),
            ], fill=NDE_PALETTE['white'] + (200,), outline=color + (255,))
            d.line([(sx - int(sz*0.10), body_top),
                    (sx - int(sz*0.20), head_y - int(sz*0.20))],
                   fill=NDE_PALETTE['white'] + (200,), width=3*SS)
            d.line([(sx + int(sz*0.10), body_top),
                    (sx + int(sz*0.20), head_y - int(sz*0.20))],
                   fill=NDE_PALETTE['white'] + (200,), width=3*SS)
        else:
            d.polygon([
                (sx - int(sz*0.10), body_top),
                (sx + int(sz*0.10), body_top),
                (sx + int(sz*0.15), sy + int(sz*0.40)),
                (sx - int(sz*0.15), sy + int(sz*0.40)),
            ], fill=NDE_PALETTE['white'] + (200,), outline=color + (255,))
    
    # Lumiere centrale
    center_glow = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    cgd = ImageDraw.Draw(center_glow, 'RGBA')
    for r_factor, alpha in [(0.10, 50), (0.06, 120), (0.03, 200), (0.015, 255)]:
        r = int(DRAW * r_factor)
        cgd.ellipse([CX-r, CY-r, CX+r, CY+r], fill=NDE_PALETTE['white'] + (alpha,))
    center_glow = center_glow.filter(ImageFilter.GaussianBlur(radius=12))
    img.alpha_composite(center_glow)


def draw_blackout(img, progress):
    """Tout devient noir progressivement (boite qui se referme).
    progress 0 -> 1 : visible -> noir total."""
    # On dessine d'abord un fond cosmique faiblissant
    d = ImageDraw.Draw(img, 'RGBA')
    
    # Etoiles qui s'eteignent (moins nombreuses + plus faibles avec progress)
    n_stars = int(50 * (1 - progress))
    random.seed(42)
    for _ in range(n_stars):
        x = random.randint(0, DRAW)
        y = random.randint(0, DRAW)
        sz = random.choice([1, 1, 2])
        alpha = int(random.randint(120, 200) * (1 - progress * 0.8))
        d.ellipse([x-sz, y-sz, x+sz, y+sz],
                  fill=NDE_PALETTE['white'] + (alpha,))
    
    # Halo central qui se reduit puis disparait
    halo_intensity = 1.0 - progress
    if halo_intensity > 0:
        halo = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
        hd = ImageDraw.Draw(halo, 'RGBA')
        halo_r = int(DRAW * 0.25 * halo_intensity)
        for i in range(15):
            t = i / 15
            r = int(halo_r * (1 - t * 0.7))
            if r < 1: break
            alpha = int(150 * (1 - t) * halo_intensity)
            hd.ellipse([CX-r, CY-r, CX+r, CY+r],
                       fill=NDE_PALETTE['gold_l'] + (alpha,))
        halo = halo.filter(ImageFilter.GaussianBlur(radius=20))
        img.alpha_composite(halo)
    
    # Overlay noir progressif
    overlay = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, int(255 * progress)))
    img.alpha_composite(overlay)


# ============================================================
# Main frame generation
# ============================================================

def get_phase_progress(frame_idx):
    """Return (phase_name, progress 0->1)."""
    for phase, (start, end) in NDE_RANGES.items():
        if start <= frame_idx <= end:
            n = max(1, end - start)
            progress = (frame_idx - start) / n
            return phase, progress
    return None, 0.0


def generate_nde_frame(frame_idx):
    """Genere une frame NDE (240..299) avec effets ULTRA.
    Travaille direct a SIZE x SIZE pour economiser memoire."""
    phase, progress = get_phase_progress(frame_idx)
    if phase is None:
        return None
    
    # 1. Dessine la base geometrique direct a SIZE (pas de supersampling)
    img = Image.new('RGBA', (DRAW, DRAW), (0, 0, 0, 0))
    
    if phase == 'decorporation':
        draw_decorporation(img, progress)
    elif phase == 'hyperspace':
        draw_hyperspace(img, progress)
    elif phase == 'crystal_palace':
        draw_crystal_palace(img, progress)
    elif phase == 'past_lives':
        draw_past_lives(img, progress)
    elif phase == 'blackout':
        draw_blackout(img, progress)
    
    # 2. Apply ULTRA painterly effects (sauf blackout final = juste grain)
    if phase == 'blackout' and progress > 0.7:
        img_final = apply_grain(img, intensity=8)
    else:
        img_final = apply_painterly_ULTRA(img)
    
    return img_final


# ============================================================
# MAIN
# ============================================================

if __name__ == "__main__":
    import time
    import gc
    print(f"=== Generation 60 frames NDE (240..299) avec effets ULTRA ===\n")
    
    total_start = time.time()
    for frame_idx in range(240, 300):
        phase, progress = get_phase_progress(frame_idx)
        t0 = time.time()
        img = generate_nde_frame(frame_idx)
        out_path = os.path.join(OUT_DIR, f"entity_{frame_idx}.png")
        # Optimisation : convertit en mode P (palette 256 couleurs) pour reduire taille
        # Garde RGBA si on veut la transparence, mais NDE n'en a pas besoin
        img_rgb = img.convert('RGB')  # Strip alpha (pas necessaire pour NDE)
        # Quantize a 256 couleurs avec dithering = -70% de taille
        img_quant = img_rgb.quantize(colors=256, method=Image.MEDIANCUT, dither=Image.FLOYDSTEINBERG)
        img_quant.save(out_path, "PNG", optimize=True)
        size_kb = os.path.getsize(out_path) / 1024
        elapsed = time.time() - t0
        print(f"  entity_{frame_idx:03d}.png  [{phase:<14s} {progress:.2f}]  "
              f"{size_kb:.0f} KB  ({elapsed:.1f}s)", flush=True)
        # Force garbage collection pour eviter accumulation memoire
        del img, img_rgb, img_quant
        gc.collect()
    
    total_time = time.time() - total_start
    print(f"\n=== TERMINE - 60 frames NDE generees en {total_time:.0f}s ===")
