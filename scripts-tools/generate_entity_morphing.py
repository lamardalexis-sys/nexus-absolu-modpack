#!/usr/bin/env python3
"""
v4 : 50 frames de morphing entite ULTRA fluide pour le PEAK Stage 5.

Inspiration entite finale : tableau Salviadroid classique -- humanoide vert
a tete triangulaire/cube, yeux noirs, corps articule, etoile doree a la
main droite, pose debout dans paysage psychedelique.

Repartition (50 frames) :
  Frames 0-5    (6)  : IRIS         -- iris cyan/magenta grossit
  Frames 6-17   (12) : CRACK        -- fissures de 0% a 100% (progression vraie)
  Frames 18-29  (12) : FACES        -- 3 visages apparaissent puis fusionnent
  Frames 30-41  (12) : METAMORPHOSE -- visage or -> entite humanoide verte
  Frames 42-49  (8)  : ENTITY LOOP  -- entite finale anime (etoile pulse, breathing)

Resolution 1024x1024 supersampling 2x Lanczos.

Output : assets/nexusabsolu/textures/gui/manifold/entity_0..49.png
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
    # Verts pour entite humanoide (inspire du tableau)
    "vert_corps": (60, 180, 100),       # corps reptilien vert
    "vert_corps_clair": (110, 220, 140),# highlights
    "vert_corps_sombre": (30, 110, 60), # ombres
    "vert_tete": (80, 200, 120),        # tete plus claire que corps
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
    return (int(lerp(c1[0], c2[0], t)),
            int(lerp(c1[1], c2[1], t)),
            int(lerp(c1[2], c2[2], t)))


# ============================================================
# IRIS qui grossit (frames 0-5, 6 frames)
# ============================================================

def draw_iris_gradient(draw, cx, cy, radius, c_outer, c_inner):
    n_layers = 30
    for i in range(n_layers):
        f = i / n_layers
        r = int(radius * (1.0 - f))
        rr = int(c_outer[0] * (1 - f) + c_inner[0] * f)
        gg = int(c_outer[1] * (1 - f) + c_inner[1] * f)
        bb = int(c_outer[2] * (1 - f) + c_inner[2] * f)
        draw.ellipse([(cx - r, cy - r), (cx + r, cy + r)], fill=(rr, gg, bb, 255))


def make_iris_frame(local_idx, n_local):
    img = new_canvas()
    draw = ImageDraw.Draw(img, "RGBA")
    progress = local_idx / max(1, n_local - 1)
    iris_radius = int((80 + progress * 280) * SS)

    # Halo violet doux
    halo_radius = int(iris_radius * 1.5)
    n_halo = 20
    for i in range(n_halo, 0, -1):
        r = int(halo_radius * (i / n_halo))
        alpha = int(60 * (1 - i / n_halo))
        draw.ellipse([(DC - r, DC - r), (DC + r, DC + r)],
                     fill=(180, 50, 255, alpha))

    draw_iris_gradient(draw, DC, DC, iris_radius, PAL["cyan"], PAL["magenta"])
    draw.ellipse([(DC - iris_radius, DC - iris_radius),
                  (DC + iris_radius, DC + iris_radius)],
                 outline=PAL["magenta"] + (255,), width=4 * SS)

    pupil_r = int(iris_radius * 0.35)
    draw.ellipse([(DC - pupil_r, DC - pupil_r),
                  (DC + pupil_r, DC + pupil_r)],
                 fill=PAL["noir"] + (255,))

    glint_r = max(int(pupil_r * 0.25), 3 * SS)
    glint_off = int(pupil_r * 0.3)
    draw.ellipse([(DC - glint_off - glint_r, DC - glint_off - glint_r),
                  (DC - glint_off + glint_r, DC - glint_off + glint_r)],
                 fill=(255, 255, 255, 230))
    return finalize(img)


# ============================================================
# CRACK 12 frames (6-17) : fissures progressent
# ============================================================

def make_crack_frame(local_idx, n_local):
    img = new_canvas()
    draw = ImageDraw.Draw(img, "RGBA")
    progress = local_idx / max(1, n_local - 1)

    # Iris arriere-plan, alpha decroit
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
    draw.ellipse([(DC - pupil_r, DC - pupil_r),
                  (DC + pupil_r, DC + pupil_r)],
                 fill=PAL["noir"] + (255,))

    n_cracks = int(4 + progress * 26)  # 4 a 30
    base_alpha = int(180 + progress * 75)
    for i in range(n_cracks):
        a = (i / n_cracks) * 2 * math.pi + progress * 0.5
        ox = DC + math.cos(a) * pupil_r * 1.1
        oy = DC + math.sin(a) * pupil_r * 1.1
        length_base = iris_radius * (0.2 + progress * 0.85)
        jitter = (i * 17 % 100) / 100.0
        length = length_base * (0.7 + jitter * 0.6)
        ex = DC + math.cos(a) * (pupil_r * 1.1 + length)
        ey = DC + math.sin(a) * (pupil_r * 1.1 + length)
        thick = int((1.5 + progress * 5.0) * SS)
        draw.line([(ox, oy), (ex, ey)],
                  fill=(255, 255, 255, base_alpha), width=thick)

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
                          fill=(255, 255, 255, sub_alpha), width=int(2 * SS))
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

    if progress > 0.4:
        face_alpha = int((progress - 0.4) / 0.6 * 220)
        face_w = int(140 * SS)
        face_h = int(180 * SS)
        face_y = DC - int(40 * SS)
        draw.ellipse([(DC - face_w, face_y - face_h),
                      (DC + face_w, face_y + face_h)],
                     outline=PAL["or"] + (face_alpha,),
                     width=int(3 * SS))
        if progress > 0.7:
            inner_alpha = int((progress - 0.7) / 0.3 * 180)
            ifw = int(face_w * 0.92)
            ifh = int(face_h * 0.92)
            draw.ellipse([(DC - ifw, face_y - ifh),
                          (DC + ifw, face_y + ifh)],
                         outline=PAL["magenta"] + (inner_alpha,),
                         width=int(2 * SS))
    return finalize(img)


# ============================================================
# FACES 12 frames (18-29) : 3 visages apparaissent puis fusionnent
# ============================================================

def draw_simple_face(draw, cx, cy, scale, color_face, color_outline, alpha=255):
    fw = int(140 * SS * scale)
    fh = int(180 * SS * scale)
    draw.ellipse([(cx - fw, cy - fh), (cx + fw, cy + fh)],
                 fill=color_face + (alpha,))
    draw.ellipse([(cx - fw, cy - fh), (cx + fw, cy + fh)],
                 outline=color_outline + (alpha,), width=int(3 * SS))
    eye_off_x = int(fw * 0.4)
    eye_off_y = int(fh * 0.15)
    eye_r = int(fw * 0.15)
    for sign in [-1, 1]:
        ex = cx + sign * eye_off_x
        ey = cy - eye_off_y
        draw.ellipse([(ex - eye_r, ey - eye_r * 0.7),
                      (ex + eye_r, ey + eye_r * 0.7)],
                     fill=PAL["noir"] + (alpha,))
        glint = max(int(eye_r * 0.3), 2 * SS)
        draw.ellipse([(ex - glint, ey - glint),
                      (ex + glint // 2, ey + glint // 2)],
                     fill=(255, 255, 255, min(alpha + 30, 255)))
    mouth_w = int(fw * 0.5)
    mouth_y = cy + int(fh * 0.3)
    draw.arc([(cx - mouth_w, mouth_y - int(fh * 0.1)),
              (cx + mouth_w, mouth_y + int(fh * 0.15))],
             start=0, end=180,
             fill=PAL["rouge"] + (alpha,), width=int(3 * SS))
    bindi_r = int(fw * 0.06)
    bindi_y = cy - int(fh * 0.5)
    draw.ellipse([(cx - bindi_r, bindi_y - bindi_r),
                  (cx + bindi_r, bindi_y + bindi_r)],
                 fill=PAL["rouge"] + (alpha,))


def make_faces_frame(local_idx, n_local):
    img = new_canvas()
    progress = local_idx / max(1, n_local - 1)

    if progress < 0.6:
        appear = progress / 0.6
        base_offset = int(60 * SS * (0.2 + appear * 1.5))
        side_alpha = int(100 + appear * 100)
        center_alpha = int(150 + appear * 75)
    else:
        fuse = (progress - 0.6) / 0.4
        base_offset = int(60 * SS * (1.7 - fuse * 1.5))
        side_alpha = int(200 - fuse * 100)
        center_alpha = int(225 + fuse * 30)

    pulse = math.sin(progress * math.pi * 3) * 0.1
    base_offset = int(base_offset * (1 + pulse))

    for cx_off, color_face, color_outline, alpha in [
        (-base_offset, PAL["cyan"], PAL["magenta"], side_alpha),
        ( base_offset, PAL["magenta"], PAL["cyan"], side_alpha),
    ]:
        layer = new_canvas()
        draw_simple_face(ImageDraw.Draw(layer, "RGBA"),
                          DC + cx_off, DC, scale=1.0,
                          color_face=color_face, color_outline=color_outline,
                          alpha=alpha)
        img = Image.alpha_composite(img, layer)

    center_scale = 1.05 + (0.05 if progress > 0.6 else 0)
    layer_center = new_canvas()
    draw_simple_face(ImageDraw.Draw(layer_center, "RGBA"),
                      DC, DC, scale=center_scale,
                      color_face=PAL["or"], color_outline=PAL["magenta"],
                      alpha=center_alpha)
    img = Image.alpha_composite(img, layer_center)

    halo_intensity = math.sin(progress * math.pi)
    if halo_intensity > 0.05:
        draw = ImageDraw.Draw(img, "RGBA")
        halo_r = int(280 * SS)
        n_halo = 25
        for i in range(n_halo, 0, -1):
            r = int(halo_r * (i / n_halo))
            alpha = int(40 * (1 - i / n_halo) * halo_intensity)
            draw.ellipse([(DC - r, DC - r), (DC + r, DC + r)],
                         outline=(0, 255, 230, alpha), width=int(2 * SS))
    return finalize(img)


# ============================================================
# METAMORPHOSE 12 frames (30-41) : visage or -> entite humanoide verte
#
# Etapes :
#   0.00..0.20 : couleur or -> vert (visage en place, change de teinte)
#   0.20..0.45 : tete devient triangulaire (forme s'angularise)
#   0.30..0.60 : corps emerge progressivement (rectangle vert qui apparait
#                sous la tete et grandit)
#   0.45..0.75 : bras se forment (de chaque cote du corps)
#   0.65..1.00 : etoile dore apparait dans la main droite
# ============================================================

def draw_humanoid(draw, progress_full, breathe=0.0):
    """Dessine l'entite humanoide verte selon progress 0..1 de formation.

    progress_full=0 -> rien
    progress_full=1 -> entite complete avec etoile

    breathe=0..1 : facteur de respiration pour subtle pulse.
    """
    # === TETE ===
    # Position tete : breath ne decale plus la tete (ca creait un decalage
    # entre le cou et le corps). Au lieu : on fera un subtle scale global.
    head_cy = int(DC - 280 * SS)
    head_size = int(120 * SS)

    # Forme : interpole entre cercle (round) et triangle (angular)
    angularity = min(1.0, progress_full * 2.5)  # devient triangle des 0.4

    if angularity < 0.15:
        # Cercle (visage classique des frames precedentes)
        draw.ellipse(
            [(DC - head_size, head_cy - head_size),
             (DC + head_size, head_cy + head_size)],
            fill=PAL["vert_tete"] + (255,)
        )
        draw.ellipse(
            [(DC - head_size, head_cy - head_size),
             (DC + head_size, head_cy + head_size)],
            outline=PAL["noir"] + (255,), width=int(3 * SS)
        )
    else:
        # Forme triangulaire/cube qui s'angularise
        # On dessine un polygone a 6 cotes qui devient progressivement plus
        # carre/triangulaire (avec coins plus marques)
        n_pts = 6
        pts = []
        for k in range(n_pts):
            a = -math.pi / 2 + (k / n_pts) * 2 * math.pi
            # Plus angularity est elevee, plus le rayon varie selon angle
            # (cree des coins droits)
            r_mod = 1.0 + angularity * 0.3 * math.cos(2 * a)
            r = head_size * r_mod
            pts.append((DC + math.cos(a) * r, head_cy + math.sin(a) * r))
        draw.polygon(pts, fill=PAL["vert_tete"] + (255,))
        # Outline
        pts_loop = pts + [pts[0]]
        for k in range(len(pts_loop) - 1):
            draw.line([pts_loop[k], pts_loop[k + 1]],
                      fill=PAL["noir"] + (255,), width=int(3 * SS))

    # YEUX (toujours noirs comme frames precedentes mais plus grands au fur et a mesure)
    eye_off_x = int(head_size * 0.4)
    eye_off_y = int(head_size * 0.05)
    eye_r = int(head_size * 0.18)
    for sign in [-1, 1]:
        ex = DC + sign * eye_off_x
        ey = head_cy - eye_off_y
        # Yeux vides noirs (look alien)
        draw.ellipse(
            [(ex - eye_r, ey - eye_r * 0.85),
             (ex + eye_r, ey + eye_r * 0.85)],
            fill=PAL["noir"] + (255,)
        )

    # Bouche ferme (ligne droite, look serieux)
    mouth_w = int(head_size * 0.3)
    mouth_y = head_cy + int(head_size * 0.45)
    draw.line(
        [(DC - mouth_w, mouth_y), (DC + mouth_w, mouth_y)],
        fill=PAL["noir"] + (255,), width=int(3 * SS)
    )

    # === COU + COL ===
    if progress_full > 0.20:
        neck_alpha = int(min(255, (progress_full - 0.20) / 0.10 * 255))
        neck_w = int(60 * SS)
        neck_h = int(60 * SS)
        neck_y_top = head_cy + head_size - int(20 * SS)
        draw.rectangle(
            [(DC - neck_w, neck_y_top),
             (DC + neck_w, neck_y_top + neck_h)],
            fill=PAL["vert_corps"] + (neck_alpha,)
        )

    # === TORSE ===
    if progress_full > 0.30:
        body_progress = min(1.0, (progress_full - 0.30) / 0.30)
        body_alpha = int(body_progress * 255)
        body_top_y = head_cy + head_size + int(40 * SS)
        body_bottom_y = body_top_y + int(280 * SS * body_progress)
        body_w = int(140 * SS)
        # Torse trapezoidal (plus large en haut, plus etroit en bas)
        torso_pts = [
            (DC - body_w, body_top_y),
            (DC + body_w, body_top_y),
            (DC + int(body_w * 0.85), body_bottom_y),
            (DC - int(body_w * 0.85), body_bottom_y)
        ]
        draw.polygon(torso_pts, fill=PAL["vert_corps"] + (body_alpha,))
        # Highlight bord gauche (lumiere venant de gauche)
        draw.line(
            [(DC - body_w, body_top_y),
             (DC - int(body_w * 0.85), body_bottom_y)],
            fill=PAL["vert_corps_clair"] + (body_alpha,),
            width=int(8 * SS)
        )
        # Outline general
        torso_loop = torso_pts + [torso_pts[0]]
        for k in range(len(torso_loop) - 1):
            draw.line([torso_loop[k], torso_loop[k + 1]],
                      fill=PAL["noir"] + (body_alpha,), width=int(3 * SS))
        # Ligne centrale verticale (musculature stylisee)
        draw.line(
            [(DC, body_top_y + int(20 * SS)),
             (DC, body_bottom_y - int(20 * SS))],
            fill=PAL["vert_corps_sombre"] + (body_alpha,),
            width=int(2 * SS)
        )

        # === BRAS ===
        if progress_full > 0.45:
            arm_progress = min(1.0, (progress_full - 0.45) / 0.30)
            arm_alpha = int(arm_progress * 255)
            arm_len = int(220 * SS * arm_progress)
            arm_w = int(40 * SS)
            shoulder_y = body_top_y + int(20 * SS)
            for sign in [-1, 1]:
                shoulder_x = DC + sign * body_w
                elbow_x = shoulder_x + sign * int(arm_len * 0.4)
                elbow_y = shoulder_y + int(arm_len * 0.5)
                hand_x = elbow_x + sign * int(arm_len * 0.2)
                hand_y = elbow_y + int(arm_len * 0.4)

                arm_draw_polygon_segment(
                    draw,
                    shoulder_x, shoulder_y, elbow_x, elbow_y,
                    arm_w, arm_alpha
                )
                arm_draw_polygon_segment(
                    draw,
                    elbow_x, elbow_y, hand_x, hand_y,
                    int(arm_w * 0.85), arm_alpha
                )
                # Main (cercle)
                hr = int(arm_w * 0.7)
                draw.ellipse(
                    [(hand_x - hr, hand_y - hr),
                     (hand_x + hr, hand_y + hr)],
                    fill=PAL["vert_corps"] + (arm_alpha,),
                    outline=PAL["noir"] + (arm_alpha,)
                )

                # ETOILE DOREE main droite
                if sign == 1 and progress_full > 0.65:
                    star_progress = min(1.0, (progress_full - 0.65) / 0.30)
                    star_size = int(110 * SS * star_progress)  # 60 -> 110 (plus gros)
                    star_alpha = int(star_progress * 255)
                    if star_size > 4:
                        # Position : un peu au-dessus et a droite de la main
                        sx = hand_x + int(50 * SS)
                        sy = hand_y - int(20 * SS)
                        draw_star(draw, sx, sy, star_size, star_alpha)

        # === JAMBES ===
        if progress_full > 0.55:
            leg_progress = min(1.0, (progress_full - 0.55) / 0.25)
            leg_alpha = int(leg_progress * 255)
            leg_len = int(180 * SS * leg_progress)
            leg_w = int(45 * SS)
            for sign in [-1, 1]:
                hip_x = DC + sign * int(50 * SS)
                hip_y = body_bottom_y
                foot_x = hip_x + sign * int(15 * SS)
                foot_y = hip_y + leg_len
                arm_draw_polygon_segment(
                    draw,
                    hip_x, hip_y, foot_x, foot_y,
                    leg_w, leg_alpha
                )
                fw = int(leg_w * 1.2)
                fh = int(leg_w * 0.5)
                draw.ellipse(
                    [(foot_x - fw, foot_y - fh),
                     (foot_x + fw + sign * int(20 * SS), foot_y + fh)],
                    fill=PAL["vert_corps_sombre"] + (leg_alpha,),
                    outline=PAL["noir"] + (leg_alpha,)
                )

    return None


def arm_draw_polygon_segment(draw, x1, y1, x2, y2, width, alpha):
    """Dessine un segment epais (capsule) entre 2 points. Stylise comme un
    rectangle oriente."""
    dx = x2 - x1
    dy = y2 - y1
    length = math.sqrt(dx * dx + dy * dy)
    if length < 0.5:
        return
    # Vecteur perpendiculaire normalise
    px = -dy / length * width / 2
    py = dx / length * width / 2
    pts = [
        (x1 - px, y1 - py),
        (x1 + px, y1 + py),
        (x2 + px, y2 + py),
        (x2 - px, y2 - py)
    ]
    draw.polygon(pts, fill=PAL["vert_corps"] + (alpha,))
    # Highlight
    draw.line([(x1 - px, y1 - py), (x2 - px, y2 - py)],
              fill=PAL["vert_corps_clair"] + (alpha,),
              width=int(3 * SS))
    # Outline
    pts_loop = pts + [pts[0]]
    for k in range(len(pts_loop) - 1):
        draw.line([pts_loop[k], pts_loop[k + 1]],
                  fill=PAL["noir"] + (alpha,), width=int(2 * SS))


def draw_star(draw, cx, cy, size, alpha):
    """Etoile a 5 pointes doree (style cherche/magique).
    v4 : plus contrastee avec outline epais + glow plus dense."""
    pts = []
    for k in range(10):
        a = -math.pi / 2 + (k / 10) * 2 * math.pi
        r = size if k % 2 == 0 else size * 0.4
        pts.append((cx + math.cos(a) * r, cy + math.sin(a) * r))
    # Glow autour de l'etoile (avant le fill pour etre derriere)
    glow_r = int(size * 1.3)
    n_glow = 8
    for i in range(n_glow, 0, -1):
        gr = int(glow_r * (i / n_glow))
        ga = int(50 * (1 - i / n_glow) * alpha / 255)
        draw.ellipse(
            [(cx - gr, cy - gr), (cx + gr, cy + gr)],
            fill=PAL["or"] + (ga,)
        )
    # Fill or
    draw.polygon(pts, fill=PAL["or"] + (alpha,))
    # Outline epais orange
    pts_loop = pts + [pts[0]]
    for k in range(len(pts_loop) - 1):
        draw.line([pts_loop[k], pts_loop[k + 1]],
                  fill=PAL["orange"] + (alpha,),
                  width=int(5 * SS))
    # Highlight petit blanc au centre (subtle)
    hi_r = int(size * 0.12)
    draw.ellipse(
        [(cx - hi_r, cy - hi_r), (cx + hi_r, cy + hi_r)],
        fill=(255, 255, 220, alpha)
    )


def make_metamorphose_frame(local_idx, n_local):
    """Visage or -> entite humanoide verte avec etoile."""
    img = new_canvas()
    draw = ImageDraw.Draw(img, "RGBA")

    progress = local_idx / max(1, n_local - 1)  # 0..1

    # === Phase 0..0.20 : visage or change de couleur vers vert ===
    # On dessine un visage avec interpolation de couleur
    if progress < 0.20:
        color_progress = progress / 0.20
        face_color = lerp_color(PAL["or"], PAL["vert_tete"], color_progress)
        outline_color = lerp_color(PAL["magenta"], PAL["noir"], color_progress)
        face_y = DC
        fw = int(140 * SS)
        fh = int(180 * SS)
        # Le visage descend legerement vers la position finale
        face_y = int(DC + lerp(0, -280, color_progress) * SS)
        # Forme : devient un peu plus angulaire au fur et a mesure
        draw.ellipse(
            [(DC - fw, face_y - fh), (DC + fw, face_y + fh)],
            fill=face_color + (255,)
        )
        draw.ellipse(
            [(DC - fw, face_y - fh), (DC + fw, face_y + fh)],
            outline=outline_color + (255,), width=int(3 * SS)
        )
        # Yeux qui passent de noir (fermes/ouverts) a noir vide alien
        eye_off = int(40 * SS)
        eye_w = int(20 * SS)
        eye_h = int(10 * SS) + int(color_progress * 25 * SS)  # s'agrandissent
        for sign in [-1, 1]:
            ex = DC + sign * eye_off
            draw.ellipse(
                [(ex - eye_w, face_y - eye_h),
                 (ex + eye_w, face_y + eye_h)],
                fill=PAL["noir"] + (255,)
            )
        # Bindi rouge disparaitre
        bindi_alpha = int((1.0 - color_progress) * 255)
        bindi_r = int(8 * SS)
        bindi_y = face_y - int(50 * SS)
        if bindi_alpha > 5:
            draw.ellipse(
                [(DC - bindi_r, bindi_y - bindi_r),
                 (DC + bindi_r, bindi_y + bindi_r)],
                fill=PAL["rouge"] + (bindi_alpha,)
            )
        # Bouche : passe d'un sourire a une ligne droite
        mouth_w = int(35 * SS)
        mouth_y = face_y + int(50 * SS)
        if color_progress < 0.5:
            # Sourire
            draw.arc(
                [(DC - mouth_w, mouth_y - int(15 * SS)),
                 (DC + mouth_w, mouth_y + int(15 * SS))],
                start=0, end=180,
                fill=PAL["rouge"] + (255,), width=int(3 * SS)
            )
        else:
            # Ligne droite
            draw.line(
                [(DC - mouth_w, mouth_y), (DC + mouth_w, mouth_y)],
                fill=PAL["noir"] + (255,), width=int(3 * SS)
            )
    else:
        # === Phase 0.20..1.0 : entite humanoide en cours de formation ===
        # On remappe le progress sur 0..1 pour cette phase
        formation_progress = (progress - 0.20) / 0.80
        draw_humanoid(draw, formation_progress, breathe=0.0)

    return finalize(img)


# ============================================================
# ENTITY LOOP 8 frames (42-49) : entite finale qui anime
# ============================================================

def draw_psychedelic_background(draw):
    """Petits motifs psychedeliques en arriere-plan (inspire du tableau)."""
    # Grille de petits diamants colores
    spacing = int(80 * SS)
    pal_list = [PAL["cyan"], PAL["magenta"], PAL["or"], PAL["violet"],
                PAL["lime"], PAL["rose"]]
    for x in range(0, DRAW_SIZE, spacing):
        for y in range(0, DRAW_SIZE, spacing):
            dx = x - DC
            dy = y - DC
            dist = math.sqrt(dx * dx + dy * dy)
            if dist < 200 * SS:
                continue  # zone centrale = entite
            color = pal_list[(x + y) % len(pal_list)]
            s = int(8 * SS)
            draw.polygon([
                (x, y - s), (x + s, y), (x, y + s), (x - s, y)
            ], fill=color + (200,))


def make_entity_loop_frame(local_idx, n_local):
    """Entite finale + breathing + etoile qui pulse."""
    img = new_canvas()
    draw = ImageDraw.Draw(img, "RGBA")

    # Fond psychedelique
    draw_psychedelic_background(draw)

    # Cycle complet sin sur n_local frames
    breathe = math.sin(local_idx / n_local * 2 * math.pi)
    breathe_factor = (breathe + 1) / 2  # 0..1

    # Dessine entite complete + breathing
    draw_humanoid(draw, 1.0, breathe=breathe_factor)

    # Etoile pulse plus fort (independent breathing)
    star_pulse = math.sin(local_idx / n_local * 2 * math.pi * 2)
    if star_pulse > 0:
        # Position de l'etoile : doit matcher exactement draw_humanoid
        head_cy = int(DC - 280 * SS)
        head_size = int(120 * SS)
        body_top_y = head_cy + head_size + int(40 * SS)
        body_w = int(140 * SS)
        shoulder_y = body_top_y + int(20 * SS)
        shoulder_x = DC + body_w
        arm_len = int(220 * SS)
        elbow_x = shoulder_x + int(arm_len * 0.4)
        elbow_y = shoulder_y + int(arm_len * 0.5)
        hand_x = elbow_x + int(arm_len * 0.2)
        hand_y = elbow_y + int(arm_len * 0.4)
        star_x = hand_x + int(50 * SS)
        star_y = hand_y - int(20 * SS)

        extra_glow = int(50 * SS * star_pulse)
        for i in range(8, 0, -1):
            gr = int(extra_glow * (i / 8))
            ga = int(60 * (1 - i / 8) * star_pulse)
            draw.ellipse(
                [(star_x - gr, star_y - gr), (star_x + gr, star_y + gr)],
                fill=PAL["or"] + (ga,)
            )

    return finalize(img)


# ============================================================
# Dispatch
# ============================================================

def make_frame(idx):
    if idx < 6:
        return make_iris_frame(idx, 6)
    elif idx < 18:
        return make_crack_frame(idx - 6, 12)
    elif idx < 30:
        return make_faces_frame(idx - 18, 12)
    elif idx < 42:
        return make_metamorphose_frame(idx - 30, 12)
    else:
        return make_entity_loop_frame(idx - 42, 8)


N_FRAMES = 50

print(f"Generation de {N_FRAMES} frames de morphing entite {SIZE}x{SIZE} (SS={SS}x)")
print(f"Output : {OUT_DIR}")
print()

for i in range(N_FRAMES):
    img = make_frame(i)
    out_path = os.path.join(OUT_DIR, f"entity_{i}.png")
    img.save(out_path, "PNG", optimize=True)
    if i < 6:
        phase = "IRIS"
    elif i < 18:
        phase = "CRACK"
    elif i < 30:
        phase = "FACES"
    elif i < 42:
        phase = "METAMORPHOSE"
    else:
        phase = "ENTITY_LOOP"
    print(f"  [{i:2d}/{N_FRAMES}] {out_path}  phase={phase}")

print()
print(f"{N_FRAMES} frames generees.")
