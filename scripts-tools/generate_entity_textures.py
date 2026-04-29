#!/usr/bin/env python3
"""
Genere 4 frames de l'entite Salviadroid pour animation cyclique.

Structure :
- Figure centrale (visage avec marque dot rouge, type bindi)
- 2 ailes deployees laterales
- Mains en namaste
- Cercle/lotus du bas avec œil cyclope (third eye)
- Motifs geometriques en couches
- Couleurs neon ultra-saturees

4 frames pour animation : ailes qui battent + breathing du visage.

Output : assets/nexusabsolu/textures/gui/manifold/entity_X.png
"""
import os
import math
from PIL import Image, ImageDraw, ImageFilter

SIZE = 512
CENTER = SIZE // 2

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


def draw_wing(draw, side, breathe_factor):
    """Aile gauche/droite avec spirales de plumes.
       side = -1 (gauche) ou +1 (droite)
       breathe_factor : 0.0 → 1.0 (cycle de respiration)
    """
    # Position de base de l'aile
    base_x = CENTER + side * 100
    base_y = CENTER - 30
    tip_x = CENTER + side * 200
    tip_y = CENTER - 80 + int(breathe_factor * 15)  # legere oscillation

    # Polygone de l'aile (4 points)
    pts = [
        (base_x, base_y),
        (tip_x, tip_y - 80),
        (tip_x + side * 30, tip_y),
        (base_x + side * 20, base_y + 80)
    ]
    draw.polygon(pts, fill=PAL["violet"] + (255,))

    # Outline magenta
    pts_loop = pts + [pts[0]]
    for i in range(len(pts_loop) - 1):
        draw.line([pts_loop[i], pts_loop[i + 1]],
                  fill=PAL["magenta"] + (255,), width=3)

    # Plumes : courbes spiralees a l'interieur de l'aile
    feather_count = 8
    for f in range(feather_count):
        ff = f / feather_count
        # Origine plume
        ox = base_x + (tip_x - base_x) * ff
        oy = base_y + (tip_y - base_y) * ff
        # Courbe spiralee
        spiral_pts = []
        for s in range(20):
            sf = s / 20.0
            angle = sf * math.pi * 1.5 * side + breathe_factor * 0.3
            r = 30 * (1 - sf) + 5
            sx = ox + math.cos(angle) * r
            sy = oy + math.sin(angle) * r - sf * 20
            spiral_pts.append((sx, sy))
        for i in range(len(spiral_pts) - 1):
            color = [PAL["lime"], PAL["cyan"], PAL["or"]][f % 3]
            draw.line([spiral_pts[i], spiral_pts[i + 1]],
                      fill=color + (255,), width=2)

    # Points colores sur l'aile (effet "yeux de paon")
    for i in range(6):
        ff = i / 6
        ex = base_x + (tip_x - base_x) * (0.3 + ff * 0.6)
        ey = base_y + (tip_y - base_y) * (0.3 + ff * 0.6)
        # Cercle externe lime
        draw.ellipse([(ex - 8, ey - 8), (ex + 8, ey + 8)],
                     fill=PAL["lime"] + (255,))
        # Cercle interne cyan
        draw.ellipse([(ex - 5, ey - 5), (ex + 5, ey + 5)],
                     fill=PAL["cyan"] + (255,))
        # Pupille noire
        draw.ellipse([(ex - 2, ey - 2), (ex + 2, ey + 2)],
                     fill=PAL["noir"] + (255,))


def draw_face(draw, breathe_factor):
    """Visage central avec dot rouge bindi, yeux fermés, méditation."""
    face_y = CENTER - 100
    # Tete : ovale doré
    draw.ellipse([(CENTER - 45, face_y - 50), (CENTER + 45, face_y + 50)],
                 fill=PAL["or"] + (255,))
    # Outline magenta
    draw.ellipse([(CENTER - 45, face_y - 50), (CENTER + 45, face_y + 50)],
                 outline=PAL["magenta"] + (255,), width=3)

    # Halo derriere la tete (auréole)
    halo_radius = 70 + int(breathe_factor * 5)
    for r in range(halo_radius, halo_radius - 5, -1):
        # Cercles concentriques fins
        draw.ellipse([(CENTER - r, face_y - r), (CENTER + r, face_y + r)],
                     outline=PAL["cyan"] + (180,), width=1)

    # Yeux fermés (lignes courbes — méditation)
    draw.arc([(CENTER - 25, face_y - 12), (CENTER - 5, face_y + 8)],
             start=0, end=180, fill=PAL["noir"] + (255,), width=2)
    draw.arc([(CENTER + 5, face_y - 12), (CENTER + 25, face_y + 8)],
             start=0, end=180, fill=PAL["noir"] + (255,), width=2)

    # Bindi dot rouge / 3eme oeil sur le front
    draw.ellipse([(CENTER - 5, face_y - 25), (CENTER + 5, face_y - 15)],
                 fill=PAL["rouge"] + (255,))

    # Nez (triangle subtil)
    draw.polygon([
        (CENTER, face_y - 5),
        (CENTER - 4, face_y + 15),
        (CENTER + 4, face_y + 15)
    ], fill=PAL["orange"] + (200,))

    # Bouche
    draw.line([(CENTER - 8, face_y + 25), (CENTER + 8, face_y + 25)],
              fill=PAL["rouge"] + (255,), width=2)


def draw_body_namaste(draw):
    """Corps avec mains en namaste devant la poitrine."""
    body_y = CENTER + 40
    # Torse triangulaire
    draw.polygon([
        (CENTER, body_y - 60),
        (CENTER - 50, body_y + 60),
        (CENTER + 50, body_y + 60)
    ], fill=PAL["cyan"] + (255,), outline=PAL["magenta"] + (255,))

    # Mains en namaste (2 triangles inversés)
    hand_y = body_y - 20
    # Main gauche
    draw.polygon([
        (CENTER - 5, hand_y),
        (CENTER - 25, hand_y + 30),
        (CENTER - 5, hand_y + 30)
    ], fill=PAL["or"] + (255,), outline=PAL["magenta"] + (255,))
    # Main droite (mirror)
    draw.polygon([
        (CENTER + 5, hand_y),
        (CENTER + 25, hand_y + 30),
        (CENTER + 5, hand_y + 30)
    ], fill=PAL["or"] + (255,), outline=PAL["magenta"] + (255,))


def draw_lotus_base(draw, breathe_factor):
    """Lotus/dome au bas avec œil cyclope (third eye)."""
    lotus_y = CENTER + 180
    # Dome principal
    draw.ellipse([(CENTER - 130, lotus_y - 40), (CENTER + 130, lotus_y + 80)],
                 fill=PAL["violet"] + (255,))
    # Outline orange
    draw.ellipse([(CENTER - 130, lotus_y - 40), (CENTER + 130, lotus_y + 80)],
                 outline=PAL["orange"] + (255,), width=3)

    # Petale lotus (symbol bouddhiste) — triangles radiaux
    for i in range(7):
        a = math.pi + (i / 6) * math.pi  # demi-cercle haut
        sx = CENTER + math.cos(a) * 100
        sy = lotus_y + math.sin(a) * 30
        ex = CENTER + math.cos(a) * 130
        ey = lotus_y + math.sin(a) * 50
        color = list(PAL.values())[i % len(PAL)]
        draw.line([(sx, sy), (ex, ey)], fill=color + (255,), width=4)

    # Œil cyclope (third eye) au centre du dome
    eye_y = lotus_y - 5
    eye_pulse = 1.0 + breathe_factor * 0.15
    eye_w = int(30 * eye_pulse)
    eye_h = int(20 * eye_pulse)
    # Blanc de l'oeil
    draw.ellipse([(CENTER - eye_w, eye_y - eye_h),
                  (CENTER + eye_w, eye_y + eye_h)],
                 fill=PAL["blanc"] + (255,))
    # Iris vert
    draw.ellipse([(CENTER - eye_w + 5, eye_y - eye_h + 3),
                  (CENTER + eye_w - 5, eye_y + eye_h - 3)],
                 fill=PAL["vert"] + (255,))
    # Pupille
    draw.ellipse([(CENTER - 6, eye_y - 6), (CENTER + 6, eye_y + 6)],
                 fill=PAL["noir"] + (255,))
    # Glow
    draw.ellipse([(CENTER - 2, eye_y - 4), (CENTER + 2, eye_y - 2)],
                 fill=PAL["blanc"] + (255,))

    # Motifs geometriques sur le dome (lignes radiales)
    for i in range(12):
        a = math.pi + (i / 11) * math.pi
        x1 = CENTER + math.cos(a) * 60
        y1 = lotus_y + math.sin(a) * 20
        x2 = CENTER + math.cos(a) * 90
        y2 = lotus_y + math.sin(a) * 35
        color = [PAL["lime"], PAL["rose"], PAL["cyan"]][i % 3]
        draw.line([(x1, y1), (x2, y2)], fill=color + (255,), width=2)


def draw_background(draw):
    """Fond geometrique : grille de losanges/etoiles."""
    # Grille de petits losanges
    spacing = 30
    for x in range(0, SIZE, spacing):
        for y in range(0, SIZE, spacing):
            # Distance du centre
            dx = x - CENTER
            dy = y - CENTER
            dist = math.sqrt(dx*dx + dy*dy)
            if dist < 80:
                continue  # zone centrale = transparente
            color = list(PAL.values())[(x + y) % len(PAL)]
            # Petit losange
            s = 5
            draw.polygon([
                (x, y - s), (x + s, y), (x, y + s), (x - s, y)
            ], fill=color + (200,))


def make_entity_frame(frame_idx):
    """Genere une frame avec breathing animation."""
    # Cycle breathing : 0.0 → 1.0 → 0.0
    breathe = math.sin(frame_idx / 4.0 * 2 * math.pi)  # 4 frames = 1 cycle complet
    breathe_factor = (breathe + 1) / 2  # normalise [0,1]

    # Fond noir transparent
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img, "RGBA")

    # Layer 1 : background (grille de motifs)
    draw_background(draw)

    # Layer 2 : ailes (en arriere-plan)
    draw_wing(draw, side=-1, breathe_factor=breathe_factor)
    draw_wing(draw, side=+1, breathe_factor=breathe_factor)

    # Layer 3 : lotus base (en bas)
    draw_lotus_base(draw, breathe_factor)

    # Layer 4 : corps + namaste
    draw_body_namaste(draw)

    # Layer 5 : visage (en dernier, le plus en avant)
    draw_face(draw, breathe_factor)

    return img


for i in range(4):
    img = make_entity_frame(i)
    out_path = os.path.join(OUT_DIR, f"entity_{i}.png")
    img.save(out_path, "PNG")
    print(f"  Generated: {out_path}")

print(f"\n4 frames d'entite generees")
