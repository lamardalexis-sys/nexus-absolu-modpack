#!/usr/bin/env python3
"""
v3 : 28 frames de morphing entite ULTRA FLUIDE pour le PEAK Stage 5.

Design : vraie video morphing image-par-image, pas de saut de phase.
Les phases CRACK et FACES utilisent des progressions parametriques 0..1
pour chaque frame -> les fissures se developpent vraiment, les visages
apparaissent vraiment progressivement.

Repartition (28 frames) :
  Frames 0-3   : IRIS         -- iris cyan/magenta qui grossit (4 frames)
  Frames 4-11  : CRACK        -- fissures de 0% a 100% (8 frames)
  Frames 12-19 : FACES        -- 3 visages apparaissent puis fusionnent (8 frames)
  Frames 20-23 : TRANSITION   -- fusion 3-visages -> Salviadroid (4 frames)
  Frames 24-27 : SALVIADROID  -- entite finale qui respire (4 frames loop)

Avec crossfade entre frames adjacentes (deja en place v1.0.336), donne une
animation continue de ~30s : 28 frames * ~21 ticks/frame ~ 30 sec @ 84 BPM.

Resolution 1024x1024 supersampling 2x Lanczos.

Output : assets/nexusabsolu/textures/gui/manifold/entity_0..27.png
"""
import os
import math
from PIL import Image, ImageDraw

SIZE = 1024
SS = 2
DRAW_SIZE = SIZE * SS
DC = DRAW_SIZE // 2

PAL = {
    "magenta": (255, 0, 200),
    "cyan": (0, 255, 230),
    "lime": (140, 255, 0),
    "or": (255, 220, 0),
    "violet": (180, 50, 255),
    "rose": (255, 50, 150),
    "orange": (255, 100, 0),
    "vert": (0, 220, 100),
    "rouge": (255, 30, 30),
    "blanc": (255, 240, 200),
    "noir": (5, 0, 15),
}

OUT_DIR = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui/manifold"
os.makedirs(OUT_DIR, exist_ok=True)


def new_canvas():
    return Image.new("RGBA", (DRAW_SIZE, DRAW_SIZE), (0, 0, 0, 0))


def finalize(img):
    return img.resize((SIZE, SIZE), Image.LANCZOS)


def lerp(a, b, t):
    return a + (b - a) * t


def lerp_color(c1, c2, t):
    return (
        int(lerp(c1[0], c2[0], t)),
        int(lerp(c1[1], c2[1], t)),
        int(lerp(c1[2], c2[2], t))
    )


# ============================================================
# IRIS qui grossit (frames 0-3)
# ============================================================

def draw_iris_gradient(draw, cx, cy, radius, c_outer, c_inner):
    n_layers = 30
    for i in range(n_layers):
        f = i / n_layers
        r = int(radius * (1.0 - f))
        rr = int(c_outer[0] * (1 - f) + c_inner[0] * f)
        gg = int(c_outer[1] * (1 - f) + c_inner[1] * f)
        bb = int(c_outer[2] * (1 - f) + c_inner[2] * f)
        draw.ellipse(
            [(cx - r, cy - r), (cx + r, cy + r)],
            fill=(rr, gg, bb, 255)
        )


def make_iris_frame(local_idx, n_local=4):
    """local_idx = 0..n_local-1. Iris grossit progressivement."""
    img = new_canvas()
    draw = ImageDraw.Draw(img, "RGBA")

    progress = local_idx / max(1, n_local - 1)  # 0..1
    iris_radius = int((80 + progress * 280) * SS)

    # Halo doux
    halo_radius = int(iris_radius * 1.5)
    n_halo = 20
    for i in range(n_halo, 0, -1):
        r = int(halo_radius * (i / n_halo))
        alpha = int(60 * (1 - i / n_halo))
        draw.ellipse(
            [(DC - r, DC - r), (DC + r, DC + r)],
            fill=(180, 50, 255, alpha)
        )

    draw_iris_gradient(draw, DC, DC, iris_radius, PAL["cyan"], PAL["magenta"])

    draw.ellipse(
        [(DC - iris_radius, DC - iris_radius),
         (DC + iris_radius, DC + iris_radius)],
        outline=PAL["magenta"] + (255,), width=4 * SS
    )

    pupil_r = int(iris_radius * 0.35)
    draw.ellipse(
        [(DC - pupil_r, DC - pupil_r), (DC + pupil_r, DC + pupil_r)],
        fill=PAL["noir"] + (255,)
    )

    glint_r = max(int(pupil_r * 0.25), 3 * SS)
    glint_off = int(pupil_r * 0.3)
    draw.ellipse(
        [(DC - glint_off - glint_r, DC - glint_off - glint_r),
         (DC - glint_off + glint_r, DC - glint_off + glint_r)],
        fill=(255, 255, 255, 230)
    )

    return finalize(img)


# ============================================================
# CRACK 8 frames (4-11) -- fissures de 0% a 100%
# ============================================================

def make_crack_frame(local_idx, n_local=8):
    """local_idx = 0..n_local-1. Fissures se developpent progressivement.

    Progression par frame :
    - Iris alpha : 255 -> 130 (s'estompe doucement)
    - Cracks : nombre + longueur + epaisseur + sub-cracks croient
    - Contour visage : alpha 0 -> 200 dans la 2eme moitie
    """
    img = new_canvas()
    draw = ImageDraw.Draw(img, "RGBA")

    progress = local_idx / max(1, n_local - 1)  # 0..1 sur la phase

    # 1. Iris en arriere-plan, alpha decroit progressivement
    iris_radius = int(360 * SS)
    iris_alpha = int(255 - progress * 125)
    iris_img = Image.new("RGBA", (DRAW_SIZE, DRAW_SIZE), (0, 0, 0, 0))
    iris_draw = ImageDraw.Draw(iris_img, "RGBA")
    draw_iris_gradient(iris_draw, DC, DC, iris_radius,
                        PAL["cyan"], PAL["magenta"])
    iris_alpha_layer = Image.new("L", (DRAW_SIZE, DRAW_SIZE), iris_alpha)
    iris_img.putalpha(iris_alpha_layer)
    img = Image.alpha_composite(img, iris_img)
    draw = ImageDraw.Draw(img, "RGBA")

    pupil_r = int(iris_radius * 0.35)
    draw.ellipse(
        [(DC - pupil_r, DC - pupil_r), (DC + pupil_r, DC + pupil_r)],
        fill=PAL["noir"] + (255,)
    )

    # 2. Crack lines : nombre et longueur croissent
    n_cracks = int(4 + progress * 22)  # 4 a 26 cracks
    base_alpha = int(180 + progress * 75)  # 180 a 255

    for i in range(n_cracks):
        a = (i / n_cracks) * 2 * math.pi + progress * 0.5
        ox = DC + math.cos(a) * pupil_r * 1.1
        oy = DC + math.sin(a) * pupil_r * 1.1
        # Longueur varie : commence courte, finit longue
        length_base = iris_radius * (0.2 + progress * 0.85)
        jitter = (i * 17 % 100) / 100.0
        length = length_base * (0.7 + jitter * 0.6)
        ex = DC + math.cos(a) * (pupil_r * 1.1 + length)
        ey = DC + math.sin(a) * (pupil_r * 1.1 + length)
        # Largeur croit avec progress
        thick = int((1.5 + progress * 5.0) * SS)
        draw.line([(ox, oy), (ex, ey)],
                  fill=(255, 255, 255, base_alpha),
                  width=thick)

        # Sub-cracks : seulement dans la 2eme moitie
        if progress > 0.3:
            sub_alpha = int(200 * (progress - 0.3) / 0.7)
            mid_x = (ox + ex) / 2
            mid_y = (oy + ey) / 2
            for sign in [-1, 1]:
                sub_a = a + sign * (0.3 + progress * 0.2)
                sub_len = length * 0.3
                sx = mid_x + math.cos(sub_a) * sub_len
                sy = mid_y + math.sin(sub_a) * sub_len
                draw.line([(mid_x, mid_y), (sx, sy)],
                          fill=(255, 255, 255, sub_alpha),
                          width=int(2 * SS))

                # Sub-sub-cracks dans la toute fin de phase
                if progress > 0.7:
                    sss_alpha = int(150 * (progress - 0.7) / 0.3)
                    for sub_sign in [-1, 1]:
                        sss_a = sub_a + sub_sign * 0.2
                        sss_len = sub_len * 0.5
                        sssx = sx + math.cos(sss_a) * sss_len
                        sssy = sy + math.sin(sss_a) * sss_len
                        draw.line([(sx, sy), (sssx, sssy)],
                                  fill=(255, 255, 255, sss_alpha),
                                  width=int(1 * SS))

    # 3. Contour visage : apparait dans la 2eme moitie
    if progress > 0.4:
        face_alpha = int((progress - 0.4) / 0.6 * 220)
        face_w = int(140 * SS)
        face_h = int(180 * SS)
        face_y = DC - int(40 * SS)
        draw.ellipse(
            [(DC - face_w, face_y - face_h),
             (DC + face_w, face_y + face_h)],
            outline=PAL["or"] + (face_alpha,),
            width=int(3 * SS)
        )
        # Contour interieur (double ligne) dans le tiers final
        if progress > 0.7:
            inner_alpha = int((progress - 0.7) / 0.3 * 180)
            ifw = int(face_w * 0.92)
            ifh = int(face_h * 0.92)
            draw.ellipse(
                [(DC - ifw, face_y - ifh),
                 (DC + ifw, face_y + ifh)],
                outline=PAL["magenta"] + (inner_alpha,),
                width=int(2 * SS)
            )

    return finalize(img)


# ============================================================
# FACES 8 frames (12-19) -- 3 visages apparaissent puis fusionnent
# ============================================================

def draw_simple_face(draw, cx, cy, scale, color_face, color_outline, alpha=255):
    fw = int(140 * SS * scale)
    fh = int(180 * SS * scale)
    draw.ellipse(
        [(cx - fw, cy - fh), (cx + fw, cy + fh)],
        fill=color_face + (alpha,)
    )
    draw.ellipse(
        [(cx - fw, cy - fh), (cx + fw, cy + fh)],
        outline=color_outline + (alpha,), width=int(3 * SS)
    )
    eye_off_x = int(fw * 0.4)
    eye_off_y = int(fh * 0.15)
    eye_r = int(fw * 0.15)
    for sign in [-1, 1]:
        ex = cx + sign * eye_off_x
        ey = cy - eye_off_y
        draw.ellipse(
            [(ex - eye_r, ey - eye_r * 0.7),
             (ex + eye_r, ey + eye_r * 0.7)],
            fill=PAL["noir"] + (alpha,)
        )
        glint = max(int(eye_r * 0.3), 2 * SS)
        draw.ellipse(
            [(ex - glint, ey - glint),
             (ex + glint // 2, ey + glint // 2)],
            fill=(255, 255, 255, min(alpha + 30, 255))
        )
    mouth_w = int(fw * 0.5)
    mouth_y = cy + int(fh * 0.3)
    draw.arc(
        [(cx - mouth_w, mouth_y - int(fh * 0.1)),
         (cx + mouth_w, mouth_y + int(fh * 0.15))],
        start=0, end=180,
        fill=PAL["rouge"] + (alpha,), width=int(3 * SS)
    )
    bindi_r = int(fw * 0.06)
    bindi_y = cy - int(fh * 0.5)
    draw.ellipse(
        [(cx - bindi_r, bindi_y - bindi_r),
         (cx + bindi_r, bindi_y + bindi_r)],
        fill=PAL["rouge"] + (alpha,)
    )


def make_faces_frame(local_idx, n_local=8):
    """local_idx = 0..n_local-1. 3 visages apparaissent (alpha croit, ecart
    croit) puis fusionnent (alpha lateraux decroit, ecart se ferme)."""
    img = new_canvas()

    progress = local_idx / max(1, n_local - 1)  # 0..1

    # Cycle : 0..0.6 = apparition + ecart max, 0.6..1.0 = fusion
    if progress < 0.6:
        # Phase apparition
        appear = progress / 0.6  # 0..1 d'apparition
        base_offset = int(60 * SS * (0.2 + appear * 1.5))  # ecart augmente
        side_alpha = int(100 + appear * 100)  # 100 a 200
        center_alpha = int(150 + appear * 75)  # 150 a 225
    else:
        # Phase fusion
        fuse = (progress - 0.6) / 0.4  # 0..1 de fusion
        base_offset = int(60 * SS * (1.7 - fuse * 1.5))  # ecart se ferme
        side_alpha = int(200 - fuse * 100)  # 200 a 100
        center_alpha = int(225 + fuse * 30)  # 225 a 255

    # Pulsation legere d'oscillation laterale (effet vivant)
    pulse = math.sin(progress * math.pi * 3) * 0.1
    base_offset = int(base_offset * (1 + pulse))

    # Visage gauche (cyan)
    layer_left = new_canvas()
    draw_simple_face(
        ImageDraw.Draw(layer_left, "RGBA"),
        DC - base_offset, DC, scale=1.0,
        color_face=PAL["cyan"], color_outline=PAL["magenta"],
        alpha=side_alpha
    )

    # Visage droit (magenta)
    layer_right = new_canvas()
    draw_simple_face(
        ImageDraw.Draw(layer_right, "RGBA"),
        DC + base_offset, DC, scale=1.0,
        color_face=PAL["magenta"], color_outline=PAL["cyan"],
        alpha=side_alpha
    )

    # Visage central (or) - scale qui pulse legerement vers la fin
    center_scale = 1.05 + (0.05 if progress > 0.6 else 0)
    layer_center = new_canvas()
    draw_simple_face(
        ImageDraw.Draw(layer_center, "RGBA"),
        DC, DC, scale=center_scale,
        color_face=PAL["or"], color_outline=PAL["magenta"],
        alpha=center_alpha
    )

    img = Image.alpha_composite(img, layer_left)
    img = Image.alpha_composite(img, layer_right)
    img = Image.alpha_composite(img, layer_center)

    # Halo cyan en arriere-plan (apparait puis disparait)
    halo_intensity = math.sin(progress * math.pi)  # 0 -> 1 -> 0
    if halo_intensity > 0.05:
        draw = ImageDraw.Draw(img, "RGBA")
        halo_r = int(280 * SS)
        n_halo = 25
        for i in range(n_halo, 0, -1):
            r = int(halo_r * (i / n_halo))
            alpha = int(40 * (1 - i / n_halo) * halo_intensity)
            draw.ellipse(
                [(DC - r, DC - r), (DC + r, DC + r)],
                outline=(0, 255, 230, alpha),
                width=int(2 * SS)
            )

    return finalize(img)


# ============================================================
# TRANSITION 4 frames (20-23) -- fusion 3-visages -> Salviadroid
# ============================================================

def make_transition_frame(local_idx, n_local=4):
    """local_idx = 0..n_local-1. Le visage central de l'or se transforme en
    Salviadroid : ailes apparaissent, lotus en bas, halo s'amplifie."""
    img = new_canvas()
    draw = ImageDraw.Draw(img, "RGBA")

    progress = local_idx / max(1, n_local - 1)  # 0..1

    # Visage central (qui se reduit pour devenir le visage Salviadroid)
    face_y = lerp(DC, DC - int(200 * SS), progress)
    face_scale = lerp(1.05, 0.65, progress)  # se reduit
    face_w = int(140 * SS * face_scale)
    face_h = int(180 * SS * face_scale)
    draw.ellipse(
        [(DC - face_w, face_y - face_h), (DC + face_w, face_y + face_h)],
        fill=PAL["or"] + (255,)
    )
    draw.ellipse(
        [(DC - face_w, face_y - face_h), (DC + face_w, face_y + face_h)],
        outline=PAL["magenta"] + (255,), width=int(3 * SS)
    )

    # Yeux du visage (closed -> meditation)
    eye_progress = progress  # 0 = yeux ouverts, 1 = yeux fermes
    eye_off = int(40 * SS * face_scale)
    eye_w = int(20 * SS * face_scale)
    eye_h = int(10 * SS * face_scale)
    for sign in [-1, 1]:
        ex = DC + sign * eye_off
        # Mix entre oeil ouvert et arc ferme
        if eye_progress < 0.5:
            # Ouvert : ovale noir
            r = int(eye_w * 0.7)
            draw.ellipse(
                [(ex - r, face_y - r * 0.7), (ex + r, face_y + r * 0.7)],
                fill=PAL["noir"] + (255,)
            )
        else:
            # Ferme : arc
            draw.arc(
                [(ex - eye_w, face_y - eye_h),
                 (ex + eye_w, face_y + eye_h)],
                start=0, end=180,
                fill=PAL["noir"] + (255,), width=int(3 * SS)
            )

    # Bindi
    bindi_r = int(8 * SS * face_scale)
    bindi_y = face_y - int(50 * SS * face_scale)
    draw.ellipse(
        [(DC - bindi_r, bindi_y - bindi_r),
         (DC + bindi_r, bindi_y + bindi_r)],
        fill=PAL["rouge"] + (255,)
    )

    # AILES qui apparaissent progressivement
    if progress > 0.0:
        wing_alpha = int(progress * 255)
        for side in [-1, 1]:
            base_x = DC + side * int(200 * SS)
            base_y = DC - int(60 * SS)
            tip_x = DC + side * int(400 * SS) * progress + side * int(200 * SS) * (1 - progress)
            tip_y = DC - int(160 * SS) * progress

            pts = [
                (base_x, base_y),
                (tip_x, tip_y - int(160 * SS) * progress),
                (tip_x + side * int(60 * SS) * progress, tip_y),
                (base_x + side * int(40 * SS), base_y + int(160 * SS))
            ]
            wing_layer = new_canvas()
            wing_draw = ImageDraw.Draw(wing_layer, "RGBA")
            wing_draw.polygon(pts, fill=PAL["violet"] + (255,))
            wing_draw.line(pts + [pts[0]],
                           fill=PAL["magenta"] + (255,), width=int(3 * SS))

            # Yeux de paon (apparaissent dans la 2eme moitie)
            if progress > 0.5:
                peacock_alpha = int((progress - 0.5) / 0.5 * 255)
                n_eyes = int((progress - 0.5) / 0.5 * 8)
                for i in range(n_eyes):
                    ff = i / max(1, n_eyes)
                    ex = base_x + (tip_x - base_x) * (0.25 + ff * 0.65)
                    ey = base_y + (tip_y - base_y) * (0.25 + ff * 0.65)
                    r_ext = int(16 * SS)
                    r_int = int(10 * SS)
                    r_pup = int(4 * SS)
                    wing_draw.ellipse(
                        [(ex - r_ext, ey - r_ext), (ex + r_ext, ey + r_ext)],
                        fill=PAL["lime"] + (peacock_alpha,))
                    wing_draw.ellipse(
                        [(ex - r_int, ey - r_int), (ex + r_int, ey + r_int)],
                        fill=PAL["cyan"] + (peacock_alpha,))
                    wing_draw.ellipse(
                        [(ex - r_pup, ey - r_pup), (ex + r_pup, ey + r_pup)],
                        fill=PAL["noir"] + (peacock_alpha,))

            # Apply wing alpha
            wing_alpha_layer = Image.new("L", (DRAW_SIZE, DRAW_SIZE), wing_alpha)
            r, g, b, a = wing_layer.split()
            new_a = Image.eval(a, lambda v: int(v * wing_alpha / 255))
            wing_layer = Image.merge("RGBA", (r, g, b, new_a))

            img = Image.alpha_composite(img, wing_layer)

    # CORPS Salviadroid (triangulaire) qui apparait
    if progress > 0.3:
        body_alpha = int((progress - 0.3) / 0.7 * 255)
        body_y = DC + int(80 * SS)
        body_layer = new_canvas()
        body_draw = ImageDraw.Draw(body_layer, "RGBA")
        body_draw.polygon([
            (DC, body_y - int(120 * SS)),
            (DC - int(100 * SS), body_y + int(120 * SS)),
            (DC + int(100 * SS), body_y + int(120 * SS))
        ], fill=PAL["cyan"] + (body_alpha,),
           outline=PAL["magenta"] + (body_alpha,))

        # Mains namaste
        hand_y = body_y - int(40 * SS)
        for sign in [-1, 1]:
            body_draw.polygon([
                (DC + sign * int(10 * SS), hand_y),
                (DC + sign * int(50 * SS), hand_y + int(60 * SS)),
                (DC + sign * int(10 * SS), hand_y + int(60 * SS))
            ], fill=PAL["or"] + (body_alpha,),
               outline=PAL["magenta"] + (body_alpha,))

        img = Image.alpha_composite(img, body_layer)

    # LOTUS du bas (apparait dans le dernier tiers)
    if progress > 0.5:
        lotus_alpha_factor = (progress - 0.5) / 0.5
        lotus_alpha = int(lotus_alpha_factor * 255)
        lotus_layer = new_canvas()
        lotus_draw = ImageDraw.Draw(lotus_layer, "RGBA")
        lotus_y = DC + int(360 * SS)
        # Dome scale
        dome_scale = lotus_alpha_factor
        lotus_draw.ellipse(
            [(DC - int(260 * SS * dome_scale), lotus_y - int(80 * SS * dome_scale)),
             (DC + int(260 * SS * dome_scale), lotus_y + int(160 * SS * dome_scale))],
            fill=PAL["violet"] + (lotus_alpha,)
        )
        if dome_scale > 0.5:
            lotus_draw.ellipse(
                [(DC - int(260 * SS * dome_scale), lotus_y - int(80 * SS * dome_scale)),
                 (DC + int(260 * SS * dome_scale), lotus_y + int(160 * SS * dome_scale))],
                outline=PAL["orange"] + (lotus_alpha,), width=int(3 * SS)
            )

        img = Image.alpha_composite(img, lotus_layer)

    return finalize(img)


# ============================================================
# SALVIADROID 4 frames (24-27) -- entite finale qui respire
# ============================================================

def draw_wing(draw, side, breathe_factor):
    base_x = DC + side * int(200 * SS)
    base_y = DC - int(60 * SS)
    tip_x = DC + side * int(400 * SS)
    tip_y = DC - int(160 * SS) + int(breathe_factor * 30 * SS)

    pts = [
        (base_x, base_y),
        (tip_x, tip_y - int(160 * SS)),
        (tip_x + side * int(60 * SS), tip_y),
        (base_x + side * int(40 * SS), base_y + int(160 * SS))
    ]
    draw.polygon(pts, fill=PAL["violet"] + (255,))
    pts_loop = pts + [pts[0]]
    for i in range(len(pts_loop) - 1):
        draw.line([pts_loop[i], pts_loop[i + 1]],
                  fill=PAL["magenta"] + (255,), width=int(3 * SS))

    for i in range(8):
        ff = i / 8
        ex = base_x + (tip_x - base_x) * (0.25 + ff * 0.65)
        ey = base_y + (tip_y - base_y) * (0.25 + ff * 0.65)
        r_ext = int(16 * SS)
        r_int = int(10 * SS)
        r_pup = int(4 * SS)
        draw.ellipse([(ex - r_ext, ey - r_ext), (ex + r_ext, ey + r_ext)],
                     fill=PAL["lime"] + (255,))
        draw.ellipse([(ex - r_int, ey - r_int), (ex + r_int, ey + r_int)],
                     fill=PAL["cyan"] + (255,))
        draw.ellipse([(ex - r_pup, ey - r_pup), (ex + r_pup, ey + r_pup)],
                     fill=PAL["noir"] + (255,))


def draw_face_meditative(draw, breathe_factor):
    face_y = DC - int(200 * SS)
    fw = int(90 * SS)
    fh = int(100 * SS)
    draw.ellipse(
        [(DC - fw, face_y - fh), (DC + fw, face_y + fh)],
        fill=PAL["or"] + (255,)
    )
    draw.ellipse(
        [(DC - fw, face_y - fh), (DC + fw, face_y + fh)],
        outline=PAL["magenta"] + (255,), width=int(3 * SS)
    )
    halo_r = int((140 + breathe_factor * 12) * SS)
    for r in range(halo_r, halo_r - int(8 * SS), -1):
        draw.ellipse(
            [(DC - r, face_y - r), (DC + r, face_y + r)],
            outline=PAL["cyan"] + (160,), width=1
        )
    eye_off = int(40 * SS)
    eye_w = int(20 * SS)
    eye_h = int(10 * SS)
    for sign in [-1, 1]:
        ex = DC + sign * eye_off
        draw.arc(
            [(ex - eye_w, face_y - eye_h),
             (ex + eye_w, face_y + eye_h)],
            start=0, end=180,
            fill=PAL["noir"] + (255,), width=int(3 * SS)
        )
    bindi_r = int(8 * SS)
    bindi_y = face_y - int(50 * SS)
    draw.ellipse(
        [(DC - bindi_r, bindi_y - bindi_r),
         (DC + bindi_r, bindi_y + bindi_r)],
        fill=PAL["rouge"] + (255,)
    )
    draw.line(
        [(DC - int(15 * SS), face_y + int(50 * SS)),
         (DC + int(15 * SS), face_y + int(50 * SS))],
        fill=PAL["rouge"] + (255,), width=int(3 * SS)
    )


def draw_body_namaste(draw):
    body_y = DC + int(80 * SS)
    draw.polygon([
        (DC, body_y - int(120 * SS)),
        (DC - int(100 * SS), body_y + int(120 * SS)),
        (DC + int(100 * SS), body_y + int(120 * SS))
    ], fill=PAL["cyan"] + (255,), outline=PAL["magenta"] + (255,))
    hand_y = body_y - int(40 * SS)
    for sign in [-1, 1]:
        draw.polygon([
            (DC + sign * int(10 * SS), hand_y),
            (DC + sign * int(50 * SS), hand_y + int(60 * SS)),
            (DC + sign * int(10 * SS), hand_y + int(60 * SS))
        ], fill=PAL["or"] + (255,), outline=PAL["magenta"] + (255,))


def draw_lotus_base(draw, breathe_factor):
    lotus_y = DC + int(360 * SS)
    draw.ellipse(
        [(DC - int(260 * SS), lotus_y - int(80 * SS)),
         (DC + int(260 * SS), lotus_y + int(160 * SS))],
        fill=PAL["violet"] + (255,)
    )
    draw.ellipse(
        [(DC - int(260 * SS), lotus_y - int(80 * SS)),
         (DC + int(260 * SS), lotus_y + int(160 * SS))],
        outline=PAL["orange"] + (255,), width=int(3 * SS)
    )
    for i in range(7):
        a = math.pi + (i / 6) * math.pi
        sx = DC + math.cos(a) * 200 * SS
        sy = lotus_y + math.sin(a) * 60 * SS
        ex = DC + math.cos(a) * 260 * SS
        ey = lotus_y + math.sin(a) * 100 * SS
        color = list(PAL.values())[i % len(PAL)]
        draw.line([(sx, sy), (ex, ey)], fill=color + (255,), width=int(4 * SS))

    eye_y = lotus_y - int(10 * SS)
    eye_pulse = 1.0 + breathe_factor * 0.18
    eye_w = int(60 * SS * eye_pulse)
    eye_h = int(40 * SS * eye_pulse)
    draw.ellipse(
        [(DC - eye_w, eye_y - eye_h), (DC + eye_w, eye_y + eye_h)],
        fill=PAL["blanc"] + (255,)
    )
    draw.ellipse(
        [(DC - eye_w + int(10 * SS), eye_y - eye_h + int(6 * SS)),
         (DC + eye_w - int(10 * SS), eye_y + eye_h - int(6 * SS))],
        fill=PAL["vert"] + (255,)
    )
    pup_r = int(12 * SS)
    draw.ellipse(
        [(DC - pup_r, eye_y - pup_r), (DC + pup_r, eye_y + pup_r)],
        fill=PAL["noir"] + (255,)
    )
    glint_r = int(4 * SS)
    glint_off = int(8 * SS)
    draw.ellipse(
        [(DC - glint_off - glint_r, eye_y - glint_off - glint_r),
         (DC - glint_off + glint_r, eye_y - glint_off + glint_r)],
        fill=PAL["blanc"] + (255,)
    )


def draw_background_grid(draw):
    spacing = int(60 * SS)
    for x in range(0, DRAW_SIZE, spacing):
        for y in range(0, DRAW_SIZE, spacing):
            dx = x - DC
            dy = y - DC
            dist = math.sqrt(dx * dx + dy * dy)
            if dist < 160 * SS:
                continue
            color = list(PAL.values())[(x + y) % len(PAL)]
            s = int(10 * SS)
            draw.polygon([
                (x, y - s), (x + s, y), (x, y + s), (x - s, y)
            ], fill=color + (180,))


def make_salviadroid_frame(local_idx, n_local=4):
    img = new_canvas()
    draw = ImageDraw.Draw(img, "RGBA")

    # Cycle complete sin sur 4 frames
    breathe = math.sin(local_idx / n_local * 2 * math.pi)
    breathe_factor = (breathe + 1) / 2

    draw_background_grid(draw)
    draw_wing(draw, side=-1, breathe_factor=breathe_factor)
    draw_wing(draw, side=+1, breathe_factor=breathe_factor)
    draw_lotus_base(draw, breathe_factor)
    draw_body_namaste(draw)
    draw_face_meditative(draw, breathe_factor)

    return finalize(img)


# ============================================================
# Dispatch global
# ============================================================

def make_frame(idx):
    if idx < 4:
        return make_iris_frame(idx, 4)
    elif idx < 12:
        return make_crack_frame(idx - 4, 8)
    elif idx < 20:
        return make_faces_frame(idx - 12, 8)
    elif idx < 24:
        return make_transition_frame(idx - 20, 4)
    else:
        return make_salviadroid_frame(idx - 24, 4)


N_FRAMES = 28

print(f"Generation de {N_FRAMES} frames de morphing entite {SIZE}x{SIZE} (SS={SS}x)")
print(f"Output : {OUT_DIR}")
print()

for i in range(N_FRAMES):
    img = make_frame(i)
    out_path = os.path.join(OUT_DIR, f"entity_{i}.png")
    img.save(out_path, "PNG", optimize=True)
    if i < 4:
        phase = "IRIS"
    elif i < 12:
        phase = "CRACK"
    elif i < 20:
        phase = "FACES"
    elif i < 24:
        phase = "TRANSITION"
    else:
        phase = "SALVIADROID"
    print(f"  [{i:2d}/{N_FRAMES}] {out_path}  phase={phase}")

print()
print(f"{N_FRAMES} frames generees.")
