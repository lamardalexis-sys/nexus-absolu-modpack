#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
process_trip.py - Pipeline complet de post-processing pour les 3000 frames
                  du Cartouche Manifold (Nexus Absolu modpack).

Place ce script a cote de out_frames/ (dossier contenant 0000.png a 2999.png).
Optionnel : si prompts_config.py est aussi a cote, les scenes seront detectees auto.

USAGE
-----
  python process_trip.py audit              # check integrite, taille, scenes
  python process_trip.py contact            # planches contact (1 PNG par scene)
  python process_trip.py preview            # MP4 preview (6 fps + 30 fps)
  python process_trip.py resize             # 1024 -> 512 + 128 couleurs
  python process_trip.py install [PATH]     # copie vers assets du mod
  python process_trip.py java               # genere le code Java ResourceLocation
  python process_trip.py all                # tout d'un coup (sauf install)

OPTIONS
-------
  --size N         taille cible pour resize (defaut: 512)
  --colors N       palette pour quantize (defaut: 128)
  --src DIR        dossier source (defaut: out_frames)
  --workers N      threads pour resize (defaut: 8)
  --fps N          override fps preview (defaut: 6 pour playback realiste)
  --skip N         contact sheet : prend 1 frame sur N (defaut: auto)
"""

import argparse
import os
import shutil
import subprocess
import sys
from concurrent.futures import ProcessPoolExecutor, as_completed
from pathlib import Path

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("ERREUR : pip install Pillow")
    sys.exit(1)

# ============================================================
# Configuration
# ============================================================
SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_SRC = SCRIPT_DIR / "out_frames"
WORK_DIR = SCRIPT_DIR / "processed"
TOTAL_FRAMES = 3000


# ============================================================
# Stages du trip (synchro avec ManifoldEffectHandler.java)
# 9 stages sur 8 min, ratio progress 0..1
# ============================================================
TRIP_STAGES = [
    ("stage1_onset",        0.0000, 0.0625),  # 0:00 - 0:30
    ("stage2_saturation",   0.0625, 0.1875),  # 0:30 - 1:30
    ("stage3_geometric",    0.1875, 0.3125),  # 1:30 - 2:30
    ("stage4_hyperspace",   0.3125, 0.5000),  # 2:30 - 4:00
    ("stage5_peak",         0.5000, 0.6875),  # 4:00 - 5:30
    ("stage4r_hyperspace",  0.6875, 0.8125),  # 5:30 - 6:30
    ("stage3r_geometric",   0.8125, 0.8750),  # 6:30 - 7:00
    ("stage2r_saturation",  0.8750, 0.9375),  # 7:00 - 7:30
    ("stage1r_onset",       0.9375, 1.0000),  # 7:30 - 8:00
]


def detect_scenes(src_dir):
    """
    Detecte les scenes dans cet ordre de priorite :
    1. prompts_config.py SCENES (le decoupage 'content' qu'on a genere)
    2. TRIP_STAGES (les 9 stages du Java ManifoldEffectHandler)
    3. Fallback : 8 chunks egaux

    Retour : liste de dicts { name, start, end, count }
    """
    # Priorite 1 : prompts_config.py
    sys.path.insert(0, str(SCRIPT_DIR))
    try:
        import prompts_config
        scenes = []
        idx = 0
        for s in prompts_config.SCENES:
            scenes.append({
                "name": s["name"],
                "start": idx,
                "end": idx + s["count"] - 1,
                "count": s["count"],
                "source": "prompts_config",
            })
            idx += s["count"]
        if scenes:
            return scenes
    except Exception:
        pass

    # Priorite 2 : 9 stages du trip Java
    scenes = []
    for name, start_ratio, end_ratio in TRIP_STAGES:
        start = int(start_ratio * TOTAL_FRAMES)
        end = int(end_ratio * TOTAL_FRAMES) - 1
        scenes.append({
            "name": name,
            "start": start,
            "end": end,
            "count": end - start + 1,
            "source": "trip_stages",
        })
    return scenes


# ============================================================
# AUDIT
# ============================================================
def cmd_audit(args):
    src = Path(args.src)
    if not src.exists():
        print(f"ERREUR : dossier source introuvable: {src}")
        sys.exit(1)

    print(f"=== AUDIT : {src} ===\n")

    files = sorted(src.glob("*.png"))
    print(f"Fichiers PNG trouves : {len(files)}")

    if len(files) != TOTAL_FRAMES:
        print(f"ATTENTION : attendu {TOTAL_FRAMES}, trouve {len(files)}")

    # Detecte les trous dans la numerotation
    expected = set(f"{i:04d}.png" for i in range(TOTAL_FRAMES))
    found = set(f.name for f in files)
    missing = sorted(expected - found)
    extra = sorted(found - expected)

    if missing:
        print(f"\nFRAMES MANQUANTES ({len(missing)}) :")
        for m in missing[:10]:
            print(f"  {m}")
        if len(missing) > 10:
            print(f"  ... et {len(missing) - 10} autres")
    else:
        print("OK : aucune frame manquante.")

    if extra:
        print(f"\nFichiers en trop ({len(extra)}) :")
        for e in extra[:10]:
            print(f"  {e}")

    # Stats taille
    sizes = [f.stat().st_size for f in files]
    if sizes:
        total_mb = sum(sizes) / 1024 / 1024
        avg_kb = sum(sizes) / len(sizes) / 1024
        min_kb = min(sizes) / 1024
        max_kb = max(sizes) / 1024
        print(f"\nTaille totale : {total_mb:.1f} MB")
        print(f"Moyenne       : {avg_kb:.0f} KB / frame")
        print(f"Min / Max     : {min_kb:.0f} KB / {max_kb:.0f} KB")

        # Frames suspectes (trop petites = probable echec/corruption)
        suspects = [f for f, s in zip(files, sizes) if s < 10240]  # < 10 KB
        if suspects:
            print(f"\nFrames SUSPECTES (< 10 KB, possibles erreurs) : {len(suspects)}")
            for s in suspects[:5]:
                print(f"  {s.name} ({s.stat().st_size / 1024:.1f} KB)")

    # Verif rapide d'integrite sur 5 frames echantillon
    print("\nVerification d'integrite (5 echantillons) :")
    sample_indices = [0, TOTAL_FRAMES // 4, TOTAL_FRAMES // 2,
                      3 * TOTAL_FRAMES // 4, TOTAL_FRAMES - 1]
    for i in sample_indices:
        path = src / f"{i:04d}.png"
        if path.exists():
            try:
                with Image.open(path) as img:
                    img.verify()
                print(f"  {path.name} : OK ({img.size[0]}x{img.size[1]})")
            except Exception as e:
                print(f"  {path.name} : CORROMPUE - {e}")

    # Scenes
    scenes = detect_scenes(src)
    src_label = scenes[0].get("source", "unknown") if scenes else "none"
    print(f"\n=== SCENES DETECTEES : {len(scenes)} (source: {src_label}) ===")
    for s in scenes:
        print(f"  [{s['start']:04d}-{s['end']:04d}] {s['name']:22s} ({s['count']} frames)")


# ============================================================
# CONTACT SHEETS
# ============================================================
def cmd_contact(args):
    src = Path(args.src)
    out_dir = WORK_DIR / "contact_sheets"
    out_dir.mkdir(parents=True, exist_ok=True)

    scenes = detect_scenes(src)
    print(f"=== CONTACT SHEETS ({len(scenes)} scenes) ===\n")

    THUMB = 192   # taille des miniatures
    PAD = 6
    COLS = 8     # 8 colonnes
    LABEL_H = 24

    for s in scenes:
        count = s["count"]
        # On prend max 32 echantillons reguliers
        n_thumbs = min(32, count)
        if n_thumbs <= 0:
            continue
        step = max(1, count // n_thumbs)
        indices = [s["start"] + i * step for i in range(n_thumbs) if s["start"] + i * step <= s["end"]]

        rows = (len(indices) + COLS - 1) // COLS
        W = COLS * (THUMB + PAD) + PAD
        H = LABEL_H + rows * (THUMB + PAD) + PAD
        sheet = Image.new("RGB", (W, H), (15, 15, 20))
        draw = ImageDraw.Draw(sheet)

        # Titre
        title = f"{s['name']}  [{s['start']:04d}-{s['end']:04d}]  ({count} frames)"
        draw.text((PAD, 4), title, fill=(220, 200, 255))

        for i, idx in enumerate(indices):
            path = src / f"{idx:04d}.png"
            if not path.exists():
                continue
            img = Image.open(path).convert("RGB")
            img.thumbnail((THUMB, THUMB), Image.LANCZOS)
            col = i % COLS
            row = i // COLS
            x = PAD + col * (THUMB + PAD)
            y = LABEL_H + PAD + row * (THUMB + PAD)
            # Centrage si pas carre
            ox = (THUMB - img.width) // 2
            oy = (THUMB - img.height) // 2
            sheet.paste(img, (x + ox, y + oy))
            # Petit numero de frame en bas a gauche
            draw.text((x + 2, y + THUMB - 12), f"{idx}", fill=(180, 180, 180))

        out_path = out_dir / f"{s['name']}.png"
        sheet.save(out_path, "PNG", optimize=True)
        print(f"  {out_path.name} ({rows} rows, {len(indices)} thumbs)")

    print(f"\nOK, regarde {out_dir}/ pour valider visuellement chaque scene.")


# ============================================================
# PREVIEW MP4 (ffmpeg)
# ============================================================
def cmd_preview(args):
    src = Path(args.src)
    out_dir = WORK_DIR
    out_dir.mkdir(parents=True, exist_ok=True)

    if shutil.which("ffmpeg") is None:
        print("ERREUR : ffmpeg introuvable dans le PATH.")
        sys.exit(1)

    # Pattern d'entree
    pattern = str(src / "%04d.png")

    # 1. Preview lente (playback realiste 6 fps -> 8 min)
    fps_slow = args.fps if args.fps else 6
    out_slow = out_dir / f"preview_{fps_slow}fps.mp4"
    print(f"=== PREVIEW {fps_slow} fps -> {out_slow.name} ===")
    cmd = [
        "ffmpeg", "-y",
        "-framerate", str(fps_slow),
        "-i", pattern,
        "-c:v", "libx264",
        "-pix_fmt", "yuv420p",
        "-crf", "20",
        "-vf", "scale=trunc(iw/2)*2:trunc(ih/2)*2",
        str(out_slow),
    ]
    subprocess.run(cmd, check=True)

    # 2. Preview rapide (30 fps -> 1m40 pour scrub rapide)
    out_fast = out_dir / "preview_30fps.mp4"
    print(f"\n=== PREVIEW 30 fps -> {out_fast.name} ===")
    cmd = [
        "ffmpeg", "-y",
        "-framerate", "30",
        "-i", pattern,
        "-c:v", "libx264",
        "-pix_fmt", "yuv420p",
        "-crf", "20",
        "-vf", "scale=trunc(iw/2)*2:trunc(ih/2)*2",
        str(out_fast),
    ]
    subprocess.run(cmd, check=True)

    # Tailles
    print(f"\nGeneres dans {out_dir}/ :")
    for f in [out_slow, out_fast]:
        if f.exists():
            print(f"  {f.name} ({f.stat().st_size / 1024 / 1024:.1f} MB)")


# ============================================================
# RESIZE + REQUANTIZE
# ============================================================
def _resize_one(args_tuple):
    src_path, dst_path, size, colors = args_tuple
    try:
        img = Image.open(src_path).convert("RGB")
        # Resize Lanczos = haute qualite
        img = img.resize((size, size), Image.LANCZOS)
        # Requantize avec dithering Floyd-Steinberg
        img = img.quantize(colors=colors, method=Image.MEDIANCUT,
                           dither=Image.FLOYDSTEINBERG)
        img.save(dst_path, "PNG", optimize=True)
        return ("ok", dst_path.name, dst_path.stat().st_size)
    except Exception as e:
        return ("fail", src_path.name, str(e))


def cmd_resize(args):
    src = Path(args.src)
    out_dir = WORK_DIR / f"frames_{args.size}"
    out_dir.mkdir(parents=True, exist_ok=True)

    files = sorted(src.glob("*.png"))
    if not files:
        print(f"ERREUR : aucun PNG dans {src}")
        sys.exit(1)

    print(f"=== RESIZE : {len(files)} frames -> {args.size}x{args.size}, {args.colors} couleurs ===")
    print(f"Output : {out_dir}/")
    print(f"Workers : {args.workers}\n")

    tasks = [(f, out_dir / f.name, args.size, args.colors) for f in files]
    ok = fail = 0
    total_size = 0
    failed = []

    with ProcessPoolExecutor(max_workers=args.workers) as ex:
        futures = {ex.submit(_resize_one, t): t for t in tasks}
        for i, fut in enumerate(as_completed(futures), 1):
            status, name, info = fut.result()
            if status == "ok":
                ok += 1
                total_size += info
            else:
                fail += 1
                failed.append((name, info))
            if i % 100 == 0 or i == len(tasks):
                print(f"  [{i:4d}/{len(tasks)}] OK={ok} FAIL={fail} | "
                      f"~{total_size / max(ok, 1) / 1024:.0f} KB/frame")

    print(f"\n=== RESULTAT ===")
    print(f"OK   : {ok}")
    print(f"FAIL : {fail}")
    if failed:
        print("\nEchecs :")
        for n, e in failed[:5]:
            print(f"  {n}: {e}")
    if ok:
        total_mb = total_size / 1024 / 1024
        print(f"\nTaille totale : {total_mb:.1f} MB ({total_size / ok / 1024:.0f} KB/frame moyenne)")


# ============================================================
# INSTALL dans le mod
# ============================================================
def cmd_install(args):
    # Chemin par defaut : assets/nexusabsolu/textures/manifold/trip/
    target = args.path if args.path else None
    if not target:
        # Cherche un nexus-absolu-modpack a cote ou dans Desktop
        candidates = [
            SCRIPT_DIR.parent / "nexus-absolu-modpack" / "mod-source" / "src" / "main" / "resources" / "assets" / "nexusabsolu" / "textures" / "manifold" / "trip",
            Path.home() / "Desktop" / "nexus-absolu-modpack" / "mod-source" / "src" / "main" / "resources" / "assets" / "nexusabsolu" / "textures" / "manifold" / "trip",
        ]
        for c in candidates:
            if c.parent.parent.exists():  # Si manifold/ existe
                target = c
                break
        if not target:
            print("ERREUR : impossible de trouver le repo nexus-absolu-modpack.")
            print("Specifie le chemin manuel : python process_trip.py install <PATH>")
            sys.exit(1)

    target = Path(target)
    target.mkdir(parents=True, exist_ok=True)

    src = WORK_DIR / f"frames_{args.size}"
    if not src.exists():
        print(f"ERREUR : {src} n'existe pas. Lance d'abord 'resize'.")
        sys.exit(1)

    files = sorted(src.glob("*.png"))
    print(f"=== INSTALL : {len(files)} frames -> {target} ===")

    # Renomme en trip_NNNN.png pour eviter les collisions avec les 50 frames entity
    for i, f in enumerate(files):
        idx = int(f.stem)
        dst = target / f"trip_{idx:04d}.png"
        shutil.copy2(f, dst)
        if (i + 1) % 500 == 0:
            print(f"  [{i+1}/{len(files)}]")

    total_mb = sum(f.stat().st_size for f in target.glob("trip_*.png")) / 1024 / 1024
    print(f"\nOK : {len(files)} frames installees ({total_mb:.1f} MB)")


# ============================================================
# JAVA CODEGEN
# ============================================================
def cmd_java(args):
    out = WORK_DIR / "TripFrames.java.snippet"
    WORK_DIR.mkdir(parents=True, exist_ok=True)

    code = []
    code.append("// =============================================================")
    code.append("// TRIP FRAMES - 3000 frames de fond plein ecran pour le trip")
    code.append("// Genere par scripts-tools/process_trip.py")
    code.append("//")
    code.append("// A integrer dans ManifoldOverlayHandler en tant que NOUVELLE COUCHE 0")
    code.append("// (avant cosmic dust). Les frames sont synchronisees au tripProgress")
    code.append("// avec crossfade fluide entre frame N et N+1.")
    code.append("// =============================================================")
    code.append("")
    code.append("public static final int TRIP_FRAME_COUNT = 3000;")
    code.append("")
    code.append("private static final ResourceLocation[] TRIP_FRAMES = new ResourceLocation[TRIP_FRAME_COUNT];")
    code.append("static {")
    code.append("    for (int i = 0; i < TRIP_FRAME_COUNT; i++) {")
    code.append("        TRIP_FRAMES[i] = new ResourceLocation(")
    code.append("            \"nexusabsolu\",")
    code.append("            String.format(\"textures/manifold/trip/trip_%04d.png\", i)")
    code.append("        );")
    code.append("    }")
    code.append("}")
    code.append("")
    code.append("/**")
    code.append(" * Mappe le tripProgress [0..1] vers une frame index float.")
    code.append(" * Partie entiere = frame courante, partie fractionnaire = blend vers frame+1.")
    code.append(" * Synchronise avec les 9 stages de ManifoldEffectHandler.")
    code.append(" */")
    code.append("private float computeTripFrameFloat(long t) {")
    code.append("    float progress = ManifoldClientState.getTripProgress(t);")
    code.append("    if (progress <= 0f) return 0f;")
    code.append("    if (progress >= 1f) return TRIP_FRAME_COUNT - 1;")
    code.append("    return progress * (TRIP_FRAME_COUNT - 1);")
    code.append("}")
    code.append("")
    code.append("/**")
    code.append(" * Render couche trip en plein ecran avec crossfade entre frames.")
    code.append(" * Intensite globale modulable (suit la courbe d'intensite des stages).")
    code.append(" */")
    code.append("private void renderTripBackground(Minecraft mc, int w, int h, long t, float globalAlpha) {")
    code.append("    if (globalAlpha <= 0.01f) return;")
    code.append("")
    code.append("    float frameFloat = computeTripFrameFloat(t);")
    code.append("    int frameA = (int) frameFloat;")
    code.append("    int frameB = Math.min(frameA + 1, TRIP_FRAME_COUNT - 1);")
    code.append("    float blend = frameFloat - frameA;")
    code.append("")
    code.append("    GlStateManager.pushMatrix();")
    code.append("    GlStateManager.enableBlend();")
    code.append("    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);")
    code.append("    GlStateManager.disableAlpha();")
    code.append("    GlStateManager.disableLighting();")
    code.append("    GlStateManager.depthMask(false);")
    code.append("")
    code.append("    // Frame A (alpha = (1 - blend) * globalAlpha)")
    code.append("    mc.getTextureManager().bindTexture(TRIP_FRAMES[frameA]);")
    code.append("    GlStateManager.color(1f, 1f, 1f, (1f - blend) * globalAlpha);")
    code.append("    drawFullscreen(w, h);")
    code.append("")
    code.append("    // Frame B (alpha = blend * globalAlpha)")
    code.append("    mc.getTextureManager().bindTexture(TRIP_FRAMES[frameB]);")
    code.append("    GlStateManager.color(1f, 1f, 1f, blend * globalAlpha);")
    code.append("    drawFullscreen(w, h);")
    code.append("")
    code.append("    // Reset GL state")
    code.append("    GlStateManager.color(1f, 1f, 1f, 1f);")
    code.append("    GlStateManager.depthMask(true);")
    code.append("    GlStateManager.enableAlpha();")
    code.append("    GlStateManager.disableBlend();")
    code.append("    GlStateManager.popMatrix();")
    code.append("}")
    code.append("")
    code.append("private void drawFullscreen(int w, int h) {")
    code.append("    Tessellator tess = Tessellator.getInstance();")
    code.append("    BufferBuilder buf = tess.getBuffer();")
    code.append("    buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);")
    code.append("    buf.pos(0, h, 0).tex(0, 1).endVertex();")
    code.append("    buf.pos(w, h, 0).tex(1, 1).endVertex();")
    code.append("    buf.pos(w, 0, 0).tex(1, 0).endVertex();")
    code.append("    buf.pos(0, 0, 0).tex(0, 0).endVertex();")
    code.append("    tess.draw();")
    code.append("}")
    code.append("")
    code.append("// === Dans renderOverlay(), AVANT 'COUCHE 0 cosmic dust', ajouter : ===")
    code.append("// // COUCHE -1 : Trip background continu (3000 frames sur 8 min)")
    code.append("// // Intensite : monte au stage 2, max au PEAK, redescend en symetrie.")
    code.append("// float tripBgAlpha = computeTripBackgroundAlpha(progress);")
    code.append("// renderTripBackground(mc, w, h, now, tripBgAlpha);")
    code.append("")
    code.append("/**")
    code.append(" * Courbe d'intensite globale du trip background.")
    code.append(" * Suit la phenomenologie : monte progressivement, max au PEAK,")
    code.append(" * redescend symetriquement.")
    code.append(" */")
    code.append("private float computeTripBackgroundAlpha(float progress) {")
    code.append("    // Visible des le stage 2 onset (0.0625) jusqu'a la fin (1.0)")
    code.append("    if (progress < 0.0625f) {")
    code.append("        return progress / 0.0625f * 0.3f;  // fade-in douce 0..30%")
    code.append("    }")
    code.append("    if (progress < 0.5f) {")
    code.append("        // Stages 2-4 : montee 30% -> 70%")
    code.append("        float t = (progress - 0.0625f) / (0.5f - 0.0625f);")
    code.append("        return 0.3f + t * 0.4f;")
    code.append("    }")
    code.append("    if (progress < 0.6875f) {")
    code.append("        // PEAK : 70% -> 100% au milieu, 70% au sortir")
    code.append("        float t = (progress - 0.5f) / (0.6875f - 0.5f);")
    code.append("        float peak = (float) Math.sin(t * Math.PI);  // bell curve")
    code.append("        return 0.7f + peak * 0.3f;")
    code.append("    }")
    code.append("    // Retour : 70% -> 0%")
    code.append("    float t = (progress - 0.6875f) / (1.0f - 0.6875f);")
    code.append("    return 0.7f * (1f - t);")
    code.append("}")

    snippet = "\n".join(code)
    out.write_text(snippet, encoding="utf-8")
    print(f"=== JAVA SNIPPET genere ===\n")
    print(f"Fichier : {out}\n")
    print(snippet)
    print(f"\n--- A integrer dans ManifoldOverlayHandler.java ---")


# ============================================================
# ALL
# ============================================================
def cmd_all(args):
    print("\n##### PHASE 1 : AUDIT #####\n")
    cmd_audit(args)
    print("\n##### PHASE 2 : CONTACT SHEETS #####\n")
    cmd_contact(args)
    print("\n##### PHASE 3 : PREVIEW MP4 #####\n")
    cmd_preview(args)
    print("\n##### PHASE 4 : RESIZE #####\n")
    cmd_resize(args)
    print("\n##### PHASE 5 : JAVA CODEGEN #####\n")
    cmd_java(args)
    print("\n##### TERMINE #####")
    print(f"Resultats dans {WORK_DIR}/")
    print("Pour installer dans le mod : python process_trip.py install [PATH_DU_REPO]")


# ============================================================
# CLI
# ============================================================
def main():
    p = argparse.ArgumentParser(description="Pipeline post-processing Cartouche Manifold")
    p.add_argument("command", choices=[
        "audit", "contact", "preview", "resize", "install", "java", "all"
    ])
    p.add_argument("path", nargs="?", help="Argument optionnel (ex: chemin install)")
    p.add_argument("--src", default=str(DEFAULT_SRC), help="Dossier source (defaut: out_frames)")
    p.add_argument("--size", type=int, default=512, help="Taille cible resize (defaut: 512)")
    p.add_argument("--colors", type=int, default=128, help="Palette quantize (defaut: 128)")
    p.add_argument("--workers", type=int, default=8, help="Threads parallel (defaut: 8)")
    p.add_argument("--fps", type=int, default=0, help="FPS preview (defaut: 6)")
    p.add_argument("--skip", type=int, default=0, help="Contact: 1 frame sur N")

    args = p.parse_args()

    handlers = {
        "audit": cmd_audit,
        "contact": cmd_contact,
        "preview": cmd_preview,
        "resize": cmd_resize,
        "install": cmd_install,
        "java": cmd_java,
        "all": cmd_all,
    }
    handlers[args.command](args)


if __name__ == "__main__":
    main()
