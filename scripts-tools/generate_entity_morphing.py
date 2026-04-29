#!/usr/bin/env python3
"""
Genere 16 frames de morphing entite Salviadroid pour le PEAK Stage 5.

V2 (Etape 3 visuel ultime) : MORPHING progressif sur 4 phases.

Phases :
  Frames 0-3  : iris cyan/magenta qui grossit (zoom progressif depuis pupille)
  Frames 4-7  : crack effect -- lignes blanches qui rayonnent + contours visage
                qui apparaissent en transparence
  Frames 8-11 : 3 visages superposes avec offsets lateraux et transparences
                (chakra-effect, separation chromatique)
  Frames 12-15: entite Salviadroid complete qui respire (corps + ailes
                + lotus + visage final)

Resolution 1024x1024 avec supersampling 2x + downscale Lanczos.

Output : assets/nexusabsolu/textures/gui/manifold/entity_0..15.png

Usage en jeu (cf. ManifoldOverlayHandler.renderEntity) :
  - 30 premieres secondes du PEAK (progress 0.5..0.5625) -> joue les 16 frames
    une fois en sequence
  - 60 secondes suivantes (progress 0.5625..0.6875) -> boucle sur frames 12-15
"""
import os
import math
from PIL import Image, ImageDraw

SIZE = 1024
SS = 2
DRAW_SIZE = SIZE * SS
DC = DRAW_SIZE // 2  # draw center

# Palette Salviadroid neon
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


# ============================================================
# Helpers : creer une image vierge + downscale a la fin
# ============================================================

def new_canvas():
    return Image.new("RGBA", (DRAW_SIZE, DRAW_SIZE), (0, 0, 0, 0))


def finalize(img):
    """Downscale supersample -> SIZE x SIZE avec Lanczos."""
    return img.resize((SIZE, SIZE), Image.LANCZOS)


# ============================================================
# Phase 1 : iris qui grossit (frames 0-3)
# ============================================================

def draw_iris_gradient(draw, cx, cy, radius, c_outer, c_inner):
    """Iris avec degrade radial concentrique (couches discretes)."""
    n_layers = 30
    for i in range(n_layers):
        f = i / n_layers
        r = int(radius * (1.0 - f))
        # Lerp couleur outer -> inner
        rr = int(c_outer[0] * (1 - f) + c_inner[0] * f)
        gg = int(c_outer[1] * (1 - f) + c_inner[1] * f)
        bb = int(c_outer[2] * (1 - f) + c_inner[2] * f)
        draw.ellipse(
            [(cx - r, cy - r), (cx + r, cy + r)],
            fill=(rr, gg, bb, 255)
        )


def make_iris_frame(frame_idx):
    """Frames 0-3 : iris qui grossit progressivement."""
    img = new_canvas()
    draw = ImageDraw.Draw(img, "RGBA")

    # Frame 0 = petit (radius = 80*SS), frame 3 = grand (radius = 360*SS)
    progress = frame_idx / 3.0  # 0..1 sur 4 frames
    iris_radius = int((80 + progress * 280) * SS)

    # Halo radial doux autour
    halo_radius = int(iris_radius * 1.5)
    n_halo = 20
    for i in range(n_halo, 0, -1):
        r = int(halo_radius * (i / n_halo))
        alpha = int(60 * (1 - i / n_halo))
        draw.ellipse(
            [(DC - r, DC - r), (DC + r, DC + r)],
            fill=(180, 50, 255, alpha)
        )

    # Iris : degrade cyan exterieur -> magenta interieur
    draw_iris_gradient(draw, DC, DC, iris_radius, PAL["cyan"], PAL["magenta"])

    # Anneau outline magenta
    draw.ellipse(
        [(DC - iris_radius, DC - iris_radius),
         (DC + iris_radius, DC + iris_radius)],
        outline=PAL["magenta"] + (255,), width=4 * SS
    )

    # Pupille noire au centre (proportionnelle)
    pupil_r = int(iris_radius * 0.35)
    draw.ellipse(
        [(DC - pupil_r, DC - pupil_r), (DC + pupil_r, DC + pupil_r)],
        fill=PAL["noir"] + (255,)
    )

    # Glint blanc dans la pupille (donne du caractere)
    glint_r = max(int(pupil_r * 0.25), 3 * SS)
    glint_off = int(pupil_r * 0.3)
    draw.ellipse(
        [(DC - glint_off - glint_r, DC - glint_off - glint_r),
         (DC - glint_off + glint_r, DC - glint_off + glint_r)],
        fill=(255, 255, 255, 230)
    )

    return finalize(img)


# ============================================================
# Phase 2 : crack effect (frames 4-7)
# ============================================================

def make_crack_frame(frame_idx):
    """Frames 4-7 : oeil se fissure + contours visage apparaissent.

    L'iris est encore present mais commence a etre 'cassé' par des lignes
    blanches rayonnantes. Le contour du visage final apparait progressivement.
    """
    img = new_canvas()
    draw = ImageDraw.Draw(img, "RGBA")

    # Local frame index 0..3
    local = frame_idx - 4
    progress = local / 3.0  # 0..1 sur les 4 frames de cette phase

    # 1. Iris (de moins en moins net : alpha decroit)
    iris_radius = int(360 * SS)
    iris_alpha = int(255 - progress * 100)  # 255 -> 155
    # On fait l'iris en RGBA puis on l'ajoute composé
    iris_img = Image.new("RGBA", (DRAW_SIZE, DRAW_SIZE), (0, 0, 0, 0))
    iris_draw = ImageDraw.Draw(iris_img, "RGBA")
    draw_iris_gradient(iris_draw, DC, DC, iris_radius,
                        PAL["cyan"], PAL["magenta"])
    # Reduction d'alpha global de l'iris
    iris_alpha_layer = Image.new("L", (DRAW_SIZE, DRAW_SIZE), iris_alpha)
    iris_img.putalpha(iris_alpha_layer)
    img = Image.alpha_composite(img, iris_img)
    draw = ImageDraw.Draw(img, "RGBA")

    pupil_r = int(iris_radius * 0.35)
    draw.ellipse(
        [(DC - pupil_r, DC - pupil_r), (DC + pupil_r, DC + pupil_r)],
        fill=PAL["noir"] + (255,)
    )

    # 2. Crack lines : rayonnent depuis le centre vers l'exterieur
    #    Plus le frame avance, plus elles sont longues et nombreuses
    n_cracks = 8 + local * 4  # 8, 12, 16, 20
    for i in range(n_cracks):
        a = (i / n_cracks) * 2 * math.pi + progress * 0.3
        # Origine : juste apres la pupille
        ox = DC + math.cos(a) * pupil_r * 1.1
        oy = DC + math.sin(a) * pupil_r * 1.1
        # Longueur croit avec progress + variation alea
        length_base = iris_radius * 0.4 + iris_radius * 0.6 * progress
        # Petit jitter selon i (pas vraiment alea mais reproductible)
        jitter = (i * 17 % 100) / 100.0  # 0..1
        length = length_base * (0.7 + jitter * 0.6)
        ex = DC + math.cos(a) * (pupil_r * 1.1 + length)
        ey = DC + math.sin(a) * (pupil_r * 1.1 + length)
        # Trait blanc avec largeur croissante au centre
        draw.line([(ox, oy), (ex, ey)],
                  fill=(255, 255, 255, 230),
                  width=int((3 + progress * 4) * SS))
        # Branchements lateraux (sub-cracks) plus l'animation avance
        if local >= 2:
            mid_x = (ox + ex) / 2
            mid_y = (oy + ey) / 2
            # 2 sub-branches a 30 deg
            for sign in [-1, 1]:
                sub_a = a + sign * 0.4
                sub_len = length * 0.3
                sx = mid_x + math.cos(sub_a) * sub_len
                sy = mid_y + math.sin(sub_a) * sub_len
                draw.line([(mid_x, mid_y), (sx, sy)],
                          fill=(255, 255, 255, 200),
                          width=int(2 * SS))

    # 3. Contour visage qui apparait progressivement (frames 6-7 surtout)
    #    Outline simple en alpha croissant
    if local >= 2:
        face_alpha = int(progress * 200)
        face_w = int(140 * SS)
        face_h = int(180 * SS)
        face_y = DC - int(40 * SS)
        # Tete ovale outline
        draw.ellipse(
            [(DC - face_w, face_y - face_h),
             (DC + face_w, face_y + face_h)],
            outline=PAL["or"] + (face_alpha,),
            width=int(3 * SS)
        )

    return finalize(img)


# ============================================================
# Phase 3 : 3 visages superposes (frames 8-11)
# ============================================================

def draw_simple_face(draw, cx, cy, scale, color_face, color_outline, alpha=255):
    """Visage simple ovale + 2 yeux + bouche, scaled."""
    fw = int(140 * SS * scale)
    fh = int(180 * SS * scale)
    # Tete
    draw.ellipse(
        [(cx - fw, cy - fh), (cx + fw, cy + fh)],
        fill=color_face + (alpha,)
    )
    # Outline
    draw.ellipse(
        [(cx - fw, cy - fh), (cx + fw, cy + fh)],
        outline=color_outline + (alpha,),
        width=int(3 * SS)
    )
    # Yeux (2 ovales noirs)
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
        # Glint blanc
        glint = max(int(eye_r * 0.3), 2 * SS)
        draw.ellipse(
            [(ex - glint, ey - glint),
             (ex + glint // 2, ey + glint // 2)],
            fill=(255, 255, 255, min(alpha + 30, 255))
        )
    # Bouche (ligne courbe -- arc)
    mouth_w = int(fw * 0.5)
    mouth_y = cy + int(fh * 0.3)
    draw.arc(
        [(cx - mouth_w, mouth_y - int(fh * 0.1)),
         (cx + mouth_w, mouth_y + int(fh * 0.15))],
        start=0, end=180,
        fill=PAL["rouge"] + (alpha,),
        width=int(3 * SS)
    )
    # 3eme oeil bindi rouge sur le front
    bindi_r = int(fw * 0.06)
    bindi_y = cy - int(fh * 0.5)
    draw.ellipse(
        [(cx - bindi_r, bindi_y - bindi_r),
         (cx + bindi_r, bindi_y + bindi_r)],
        fill=PAL["rouge"] + (alpha,)
    )


def make_three_faces_frame(frame_idx):
    """Frames 8-11 : 3 visages superposes avec separation chromatique."""
    img = new_canvas()

    # Local frame 0..3
    local = frame_idx - 8
    progress = local / 3.0

    # Offset lateral qui croit puis decroit (effet pulsation)
    # frame 0 : offset modere
    # frame 1-2 : offset max
    # frame 3 : offset reduit (preparation a la fusion)
    offset_curve = math.sin(progress * math.pi)  # 0->1->0
    base_offset = int(60 * SS * (0.5 + offset_curve * 1.0))

    # Visage gauche (decale + tinte cyan)
    layer_left = new_canvas()
    draw_left = ImageDraw.Draw(layer_left, "RGBA")
    draw_simple_face(
        draw_left, DC - base_offset, DC,
        scale=1.0,
        color_face=PAL["cyan"], color_outline=PAL["magenta"],
        alpha=180
    )

    # Visage droit (decale + tinte magenta)
    layer_right = new_canvas()
    draw_right = ImageDraw.Draw(layer_right, "RGBA")
    draw_simple_face(
        draw_right, DC + base_offset, DC,
        scale=1.0,
        color_face=PAL["magenta"], color_outline=PAL["cyan"],
        alpha=180
    )

    # Visage central (centre + tinte or)
    layer_center = new_canvas()
    draw_center = ImageDraw.Draw(layer_center, "RGBA")
    draw_simple_face(
        draw_center, DC, DC,
        scale=1.05,
        color_face=PAL["or"], color_outline=PAL["magenta"],
        alpha=220
    )

    # Composite : back to front
    img = Image.alpha_composite(img, layer_left)
    img = Image.alpha_composite(img, layer_right)
    img = Image.alpha_composite(img, layer_center)

    # Halo derriere les 3 (cyan diffus)
    draw = ImageDraw.Draw(img, "RGBA")
    halo_r = int(280 * SS)
    n_halo = 25
    for i in range(n_halo, 0, -1):
        r = int(halo_r * (i / n_halo))
        alpha = int(40 * (1 - i / n_halo))
        draw.ellipse(
            [(DC - r, DC - r), (DC + r, DC + r)],
            outline=(0, 255, 230, alpha),
            width=int(2 * SS)
        )

    return finalize(img)


# ============================================================
# Phase 4 : entite Salviadroid finale (frames 12-15)
# ============================================================

def draw_wing(draw, side, breathe_factor):
    """Aile gauche/droite avec spirales de plumes."""
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

    # "yeux de paon" sur l'aile
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
    """Visage central en meditation avec dot bindi."""
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

    # Halo / aureole
    halo_r = int((140 + breathe_factor * 12) * SS)
    for r in range(halo_r, halo_r - int(8 * SS), -1):
        draw.ellipse(
            [(DC - r, face_y - r), (DC + r, face_y + r)],
            outline=PAL["cyan"] + (160,), width=1
        )

    # Yeux fermes (arcs)
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

    # Bindi rouge sur front
    bindi_r = int(8 * SS)
    bindi_y = face_y - int(50 * SS)
    draw.ellipse(
        [(DC - bindi_r, bindi_y - bindi_r),
         (DC + bindi_r, bindi_y + bindi_r)],
        fill=PAL["rouge"] + (255,)
    )

    # Bouche
    draw.line(
        [(DC - int(15 * SS), face_y + int(50 * SS)),
         (DC + int(15 * SS), face_y + int(50 * SS))],
        fill=PAL["rouge"] + (255,), width=int(3 * SS)
    )


def draw_body_namaste(draw):
    """Corps + mains en namaste."""
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
    """Lotus avec oeil cyclope au bas."""
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

    # Petales du lotus
    for i in range(7):
        a = math.pi + (i / 6) * math.pi
        sx = DC + math.cos(a) * 200 * SS
        sy = lotus_y + math.sin(a) * 60 * SS
        ex = DC + math.cos(a) * 260 * SS
        ey = lotus_y + math.sin(a) * 100 * SS
        color = list(PAL.values())[i % len(PAL)]
        draw.line([(sx, sy), (ex, ey)], fill=color + (255,), width=int(4 * SS))

    # Oeil cyclope (third eye)
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
    """Fond grille de losanges (zone centrale evidee)."""
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


def make_salviadroid_frame(frame_idx):
    """Frames 12-15 : entite finale qui respire (cycle sin sur 4 frames)."""
    img = new_canvas()
    draw = ImageDraw.Draw(img, "RGBA")

    local = frame_idx - 12
    breathe = math.sin(local / 4.0 * 2 * math.pi)
    breathe_factor = (breathe + 1) / 2  # 0..1

    draw_background_grid(draw)
    draw_wing(draw, side=-1, breathe_factor=breathe_factor)
    draw_wing(draw, side=+1, breathe_factor=breathe_factor)
    draw_lotus_base(draw, breathe_factor)
    draw_body_namaste(draw)
    draw_face_meditative(draw, breathe_factor)

    return finalize(img)


# ============================================================
# Dispatch : 16 frames
# ============================================================

def make_frame(idx):
    if idx < 4:
        return make_iris_frame(idx)
    elif idx < 8:
        return make_crack_frame(idx)
    elif idx < 12:
        return make_three_faces_frame(idx)
    else:
        return make_salviadroid_frame(idx)


print(f"Generation de 16 frames d'entite morphing {SIZE}x{SIZE} (SS={SS}x)")
print(f"Output : {OUT_DIR}")
print()

for i in range(16):
    img = make_frame(i)
    out_path = os.path.join(OUT_DIR, f"entity_{i}.png")
    img.save(out_path, "PNG", optimize=True)
    phase = ["IRIS", "CRACK", "3FACES", "SALVIADROID"][i // 4]
    print(f"  [{i:2d}/16] {out_path}  phase={phase}")

print()
print("16 frames generees.")
