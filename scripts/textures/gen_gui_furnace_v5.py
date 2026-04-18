"""
gui_furnace.png v5 - layout final :
 - Tout visible en permanence (pas d'onglets)
 - Upgrades en CARRE 2x2 a droite du GUI
 - Petit onglet * a x=xSize-2 (comme Convertisseur) pour Config I/O

Layout 176x166 :
 - Cadre violet sombre Nexus
 - Zone machine (y=4..78) :
   * INPUT slot (55, 16)
   * FUEL slot (55, 52) 
   * Flamme tube (79, 37)
   * Progress zone (94, 38) avec silhouette fleche
   * OUTPUT_LARGE (116, 30) 26x26 double-cadre
   * RF bar verticale (122, 15, 6x58)
   * 4 Upgrades 2x2 : (130, 16) (148, 16) (130, 34) (148, 34) - cadres violets
 - Separateur y=80
 - Zone inventaire (y=83..166)

Onglet config externe (dans la texture a x=176, y=0, 15x17) :
 - Petit bout qui depasse a droite du GUI avec asterisque
"""
from PIL import Image, ImageDraw
import os

OUT = "mod-source/src/main/resources/assets/nexusabsolu/textures/gui"
os.makedirs(OUT, exist_ok=True)

BG_MAIN = (26, 13, 46, 255)
FRAME_OUT = (10, 5, 20, 255)
FRAME_MID = (58, 30, 100, 255)
FRAME_LIGHT = (107, 63, 160, 255)
SLOT_INSIDE = (16, 8, 32, 255)
TUBE_INSIDE = (8, 4, 16, 255)
SEPARATOR = (74, 42, 110, 255)

W, H = 176, 166
canvas = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
d = ImageDraw.Draw(canvas)


def draw_slot(x, y, size=18):
    d.rectangle([x, y, x + size - 1, y + size - 1], outline=FRAME_OUT)
    d.rectangle([x + 1, y + 1, x + size - 2, y + size - 2], fill=SLOT_INSIDE)
    d.rectangle([x, y, x + size - 1, y], fill=FRAME_LIGHT)
    d.rectangle([x, y, x, y + size - 1], fill=FRAME_LIGHT)


def draw_output_slot(x, y):
    d.rectangle([x, y, x + 25, y + 25], fill=FRAME_LIGHT)
    d.rectangle([x + 1, y + 1, x + 24, y + 24], fill=FRAME_OUT)
    d.rectangle([x + 3, y + 3, x + 22, y + 22], fill=SLOT_INSIDE)


def draw_tube_vertical(x, y, w, h):
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=FRAME_OUT)
    d.rectangle([x + 1, y + 1, x + w - 2, y + h - 2], fill=TUBE_INSIDE)


def draw_progress_zone(x, y, w, h):
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=TUBE_INSIDE)
    d.rectangle([x, y, x + w - 1, y], fill=FRAME_OUT)
    d.rectangle([x, y + h - 1, x + w - 1, y + h - 1], fill=FRAME_OUT)


def draw_inv_slot(x, y):
    d.rectangle([x, y, x + 17, y + 17], fill=FRAME_OUT)
    d.rectangle([x + 1, y + 1, x + 16, y + 16], fill=SLOT_INSIDE)
    d.rectangle([x, y, x + 17, y], fill=FRAME_MID)
    d.rectangle([x, y, x, y + 17], fill=FRAME_MID)


# === FOND PRINCIPAL ===
d.rectangle([0, 0, W - 1, H - 1], fill=BG_MAIN)
d.rectangle([0, 0, W - 1, 0], fill=FRAME_LIGHT)
d.rectangle([0, 0, 0, H - 1], fill=FRAME_LIGHT)
d.rectangle([0, H - 1, W - 1, H - 1], fill=FRAME_OUT)
d.rectangle([W - 1, 0, W - 1, H - 1], fill=FRAME_OUT)

# Zone machine
d.rectangle([4, 4, W - 5, 78], fill=FRAME_MID)
d.rectangle([5, 5, W - 6, 77], fill=BG_MAIN)

# === SLOTS MACHINE GAUCHE ===
draw_slot(55, 16)   # INPUT
draw_slot(55, 52)   # FUEL
draw_tube_vertical(79, 37, 12, 13)
draw_progress_zone(94, 38, 20, 10)

# Fleche silhouette
for ix in range(8):
    d.point((96 + ix, 42), fill=SEPARATOR)
    d.point((96 + ix, 43), fill=SEPARATOR)
    d.point((96 + ix, 44), fill=SEPARATOR)
arrow_tip = [(104, 40), (105, 40), (104, 41), (105, 41), (106, 41),
             (104, 45), (105, 45), (104, 46), (105, 46), (106, 46)]
for px, py in arrow_tip:
    d.point((px, py), fill=SEPARATOR)
d.point((107, 42), fill=SEPARATOR)
d.point((107, 43), fill=SEPARATOR)
d.point((107, 44), fill=SEPARATOR)

# OUTPUT_LARGE au centre-droit
draw_output_slot(116, 30)  # 26x26 -> occupe x=116-141, y=30-55

# === RF BAR VERTICALE (a droite du OUTPUT, avant les upgrades) ===
# OUTPUT finit a x=141. On met RF a x=144.
draw_tube_vertical(144, 16, 6, 56)

# === 4 UPGRADES EN CARRE 2x2 (a l'extreme droite) ===
# Position : (152, 16) (152+18, 16) (152, 34) (152+18, 34) ... wait c'est trop large
# Recalc : W=176, bord droit = 170. Disponible: x=152 a x=170 = 18px = 1 slot seul
# On a besoin de 36px pour 2x2. Donc faut repositionner.
# Nouvelle idee : RF bar + upgrades dans un layout different
# OPTION: Mettre la RF bar A GAUCHE, entre OUTPUT et les upgrades 2x2
# Actually mieux : Upgrades 2x2 a (134, 16) (152, 16) (134, 34) (152, 34)
#                  et RF bar horizontale en bas a (134, 56, 36, 8)

# CLEAR : efface ce qu'on vient de dessiner pour OUTPUT et RF bar, on repense
# Refaire : juste effacer en remplissant avec BG_MAIN
d.rectangle([110, 4, W - 5, 76], fill=FRAME_MID)
d.rectangle([111, 5, W - 6, 75], fill=BG_MAIN)

# OUTPUT_LARGE (plus a gauche pour libere la place)
draw_output_slot(94, 30)  # 26x26 -> x=94-119, y=30-55

# 4 UPGRADES 2x2 a droite
draw_slot(126, 16)   # Upgrade 0 : RF
draw_slot(144, 16)   # Upgrade 1 : IO
draw_slot(126, 34)   # Upgrade 2 : Speed
draw_slot(144, 34)   # Upgrade 3 : Eff

# Barre RF horizontale sous les upgrades
# (126, 58, 36, 8) = de (126-161) x (58-65)
d.rectangle([126, 58, 161, 65], fill=FRAME_OUT)
d.rectangle([127, 59, 160, 64], fill=TUBE_INSIDE)

# Re-dessine IN/FUEL/flamme/progress (ils ont pas ete ecrases, mais je verifie les positions)
# Input etait a (55,16), FUEL (55,52), flamme (79,37), progress (94,38)
# OUTPUT etait a (116,30) maintenant deplace a (94,30) mais progress arrow est a (94,38)
# CONFLIT : output_large (94-119) chevauche progress (94-114) !
# Je decale progress + progress arrow: nouvelle position progress = (72, 38, 20, 10)
# Et je decale flamme aussi

# Efface la zone interne machine et re-dessine proprement
d.rectangle([5, 5, W - 6, 77], fill=BG_MAIN)

# INPUT (55, 16)
draw_slot(55, 16)
# FUEL (55, 52)
draw_slot(55, 52)

# Flamme (entre IN et FUEL) - garde (79, 37, 12, 13)  - pas conflit
draw_tube_vertical(79, 37, 12, 13)

# PROGRESS zone maintenant de (72, 38, 20, 10) -- en-dessous du flamme, decale a gauche
# Actually on peut laisser la fleche avant le OUTPUT
# OUTPUT commence a x=94. Progress peut aller de x=72 a x=92 (20 wide)
draw_progress_zone(72, 38, 20, 10)
# Fleche silhouette dans la nouvelle progress zone (72-92, 38-48)
for ix in range(8):
    d.point((74 + ix, 42), fill=SEPARATOR)
    d.point((74 + ix, 43), fill=SEPARATOR)
    d.point((74 + ix, 44), fill=SEPARATOR)
arrow_tip = [(82, 40), (83, 40), (82, 41), (83, 41), (84, 41),
             (82, 45), (83, 45), (82, 46), (83, 46), (84, 46)]
for px, py in arrow_tip:
    d.point((px, py), fill=SEPARATOR)
d.point((85, 42), fill=SEPARATOR)
d.point((85, 43), fill=SEPARATOR)
d.point((85, 44), fill=SEPARATOR)

# Flamme doit etre a gauche de progress. Actuellement elle est a (79, 37)
# mais progress est a (72, 38) qui chevauche. 
# Je deplace flamme a (66, 37)
# Efface le dessin actuel de la flamme et re-dessine
# (par-contre la, j'ai refait le fond machine, donc la flamme a (79,37) est déjà dessinée
# et il faut l'effacer)
# NVM, plus simple : on met flamme a (66, 37) et on laisse (72, 38) progress zone
# La flamme sera entre INPUT (55,16) + FUEL (55,52) a gauche, et progress/output a droite.

# Efface la flamme actuelle (a 79,37) en la remplissant avec BG
d.rectangle([79, 37, 90, 49], fill=BG_MAIN)

# Nouvelle position flamme : juste a droite de INPUT/FUEL, avant progress
# INPUT va de 55 a 72 (18 large), FUEL pareil
# Flamme peut aller de 74 a 85. Mais progress est a 72-91 !
# Conflit. Il faut soit bouger progress plus a droite, soit virer la flamme verticale.

# SIMPLIFICATION : pas de flamme verticale separee. L'indicateur flamme sera juste
# a l'interieur du slot FUEL (une mini-icone dessinee en foreground par le Java).
# Du coup on a plus de place : progress a (74, 38, 20, 10)

# Re-efface machine et re-dessine proprement
d.rectangle([5, 5, W - 6, 77], fill=BG_MAIN)
# INPUT
draw_slot(55, 16)
# FUEL
draw_slot(55, 52)
# Progress (74-94, 38-48)
draw_progress_zone(74, 38, 20, 10)
# Fleche silhouette
for ix in range(8):
    d.point((76 + ix, 42), fill=SEPARATOR)
    d.point((76 + ix, 43), fill=SEPARATOR)
    d.point((76 + ix, 44), fill=SEPARATOR)
arrow_tip = [(84, 40), (85, 40), (84, 41), (85, 41), (86, 41),
             (84, 45), (85, 45), (84, 46), (85, 46), (86, 46)]
for px, py in arrow_tip:
    d.point((px, py), fill=SEPARATOR)
d.point((87, 42), fill=SEPARATOR)
d.point((87, 43), fill=SEPARATOR)
d.point((87, 44), fill=SEPARATOR)

# OUTPUT_LARGE (96, 30, 26x26)
draw_output_slot(96, 30)

# 4 Upgrades 2x2 a (126, 16) (144, 16) (126, 34) (144, 34)
draw_slot(126, 16)
draw_slot(144, 16)
draw_slot(126, 34)
draw_slot(144, 34)

# Barre RF horizontale sous les upgrades (126, 58, 36x7)
d.rectangle([126, 58, 161, 64], fill=FRAME_OUT)
d.rectangle([127, 59, 160, 63], fill=TUBE_INSIDE)

# === SEPARATEUR machine/inventaire ===
for ix in range(6, W - 6):
    d.point((ix, 80), fill=SEPARATOR)
    d.point((ix, 81), fill=FRAME_OUT)

# === ZONE INVENTAIRE ===
d.rectangle([4, 83, W - 5, H - 5], fill=FRAME_MID)
d.rectangle([5, 84, W - 6, H - 6], fill=BG_MAIN)

for row in range(3):
    for col in range(9):
        draw_inv_slot(7 + col * 18, 93 + row * 18)
for col in range(9):
    draw_inv_slot(7 + col * 18, 151)

# === ONGLET CONFIG CLIQUABLE (a x>=W, depasse a droite) ===
# Sprite a (176, 0) dans le PNG, 15x17 de taille
# Sera blitte par le code Java a x=xSize-2, y=18
tab_x = 176
tab_y = 0
# Corps de l'onglet (violet sombre)
d.rectangle([tab_x, tab_y, tab_x + 14, tab_y + 16], fill=FRAME_MID)
# Bord haut-droite clair
d.rectangle([tab_x, tab_y, tab_x + 14, tab_y], fill=FRAME_LIGHT)
d.rectangle([tab_x + 14, tab_y, tab_x + 14, tab_y + 16], fill=FRAME_OUT)
# Bord bas sombre
d.rectangle([tab_x, tab_y + 16, tab_x + 14, tab_y + 16], fill=FRAME_OUT)
# Astérisque au centre (icone config)
# Croix + diagonales
star_cx = tab_x + 7
star_cy = tab_y + 8
# Branches
for dy in [-3, -2, -1, 0, 1, 2, 3]:
    d.point((star_cx, star_cy + dy), fill=FRAME_LIGHT)
for dx in [-3, -2, -1, 0, 1, 2, 3]:
    d.point((star_cx + dx, star_cy), fill=FRAME_LIGHT)
# Diagonales
for d_offset in [-2, -1, 1, 2]:
    d.point((star_cx + d_offset, star_cy + d_offset), fill=FRAME_LIGHT)
    d.point((star_cx + d_offset, star_cy - d_offset), fill=FRAME_LIGHT)

canvas.save(f"{OUT}/gui_furnace.png")
print(f"[OK] gui_furnace.png v5 ({W}x{H}, upgrades 2x2, config tab right)")
