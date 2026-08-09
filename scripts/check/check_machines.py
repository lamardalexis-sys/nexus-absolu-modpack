#!/usr/bin/env python3
"""
Controle de coherence du moteur de machines Nexus.

Verif 2 du protocole : croiser les donnees entre elles par script, jamais a
l'oeil. Ce script ne modifie rien, il lit et il rapporte.

Ce qu'il verifie :

  1. legend.json  -- chaque symbole a un role et une liste de blocs non vide
                     (seul le role 'controller' a le droit d'etre vide).
  2. index.json   -- il liste exactement les fichiers de machines presents.
  3. chaque machine -- id coherent avec le nom de fichier et l'index,
                     pattern rectangulaire, exactement un controleur, tout
                     symbole present dans la legende, taille dans la charte.
  4. blocs        -- chaque bloc nexusabsolu: cite par la legende ou par une
                     definition existe vraiment dans le mod (ModBlocks +
                     blockstate + modele + texture + traductions FR et EN).
  5. bascule MM   -- le pattern reproduit fidelement le machinery JSON de
                     Modular Machinery qu'il remplace, tant que MM est la.
  6. encodage     -- aucun caractere non-ASCII dans les .java du moteur.

Usage :
    python3 scripts/check/check_machines.py
Code de sortie : 0 si tout passe, 1 sinon.
"""

import io
import json
import os
import re
import sys

# Toujours resoudre depuis l'emplacement du script, jamais depuis le cwd :
# un chemin relatif en dur fait lire un fichier et en modifier un autre.
HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))

RES = os.path.join(ROOT, "mod-source", "src", "main", "resources",
                   "assets", "nexusabsolu")
MACHINES = os.path.join(RES, "machines")
JAVA = os.path.join(ROOT, "mod-source", "src", "main", "java",
                    "com", "nexusabsolu", "mod")
MM_MACHINERY = os.path.join(ROOT, "config", "modularmachinery", "machinery")

MAX_W, MAX_H, MAX_D = 3, 5, 3

# Correspondance symbole Nexus -> variable d'elements Modular Machinery,
# utilisee pour le controle croise pendant la bascule.
MM_ELEMENTS = {
    "D": "casings_decorative",
    "A": "casings_all",
    "I": "casings_item",
    "F": "casings_fluid",
    "E": "casings_energy",
}

errors = []
warnings = []
checks = [0]


def fail(msg):
    errors.append(msg)


def warn(msg):
    warnings.append(msg)


def ok(msg):
    checks[0] += 1


def load_json(path):
    with io.open(path, encoding="utf-8") as fh:
        return json.load(fh)


# ---------------------------------------------------------------- legende

def check_legend():
    path = os.path.join(MACHINES, "legend.json")
    if not os.path.isfile(path):
        fail("legend.json absent de %s" % MACHINES)
        return None
    try:
        data = load_json(path)
    except ValueError as e:
        fail("legend.json illisible : %s" % e)
        return None

    syms = data.get("symbols")
    if not isinstance(syms, dict) or not syms:
        fail("legend.json : 'symbols' absent ou vide")
        return None

    controllers = 0
    for key, entry in syms.items():
        if len(key) != 1:
            fail("legend.json : symbole '%s' fait %d caracteres, il en faut 1"
                 % (key, len(key)))
            continue
        role = entry.get("role")
        if not role:
            fail("legend.json/%s : 'role' absent" % key)
            continue
        blocks = entry.get("blocks", [])
        if role == "controller":
            controllers += 1
            if blocks:
                fail("legend.json/%s : role 'controller' avec une liste 'blocks'"
                     " non vide ; le bloc attendu depend de la machine" % key)
        elif not blocks:
            fail("legend.json/%s : liste 'blocks' vide pour le role '%s'"
                 % (key, role))
        for b in blocks:
            if ":" not in b:
                fail("legend.json/%s : '%s' n'est pas un identifiant complet"
                     % (key, b))
        ok("symbole %s" % key)

    if controllers != 1:
        fail("legend.json : %d symbole(s) de role 'controller', il en faut"
             " exactement un" % controllers)
    return syms


# ------------------------------------------------------------------ index

def check_index():
    path = os.path.join(MACHINES, "index.json")
    if not os.path.isfile(path):
        fail("index.json absent de %s" % MACHINES)
        return []
    try:
        listed = load_json(path).get("machines", [])
    except ValueError as e:
        fail("index.json illisible : %s" % e)
        return []

    present = sorted(
        f[:-5] for f in os.listdir(MACHINES)
        if f.endswith(".json") and f not in ("index.json", "legend.json"))

    missing = [m for m in present if m not in listed]
    ghost = [m for m in listed if m not in present]
    for m in missing:
        fail("%s.json existe mais n'est pas declare dans index.json :"
             " le moteur ne le chargera jamais" % m)
    for m in ghost:
        fail("index.json declare '%s' mais %s.json n'existe pas" % (m, m))
    if not missing and not ghost:
        ok("index complet (%d machine(s))" % len(listed))
    return listed


# -------------------------------------------------------------- machines

def parse_pattern(data, name, syms):
    """Renvoie {(x,y,z): symbole} recentre sur le controleur, ou None."""
    layers = data.get("layers")
    if not isinstance(layers, list) or not layers:
        fail("%s : 'layers' absent ou vide" % name)
        return None

    ctrl_syms = [k for k, v in syms.items() if v.get("role") == "controller"]
    ctrl_sym = ctrl_syms[0] if ctrl_syms else "C"

    raw = {}
    ctrl = None
    rows_n = cols_n = None
    prev_y = None

    for li, layer in enumerate(layers):
        y = layer.get("y")
        if y is None:
            fail("%s : layers[%d] sans 'y'" % (name, li))
            return None
        if prev_y is not None and y <= prev_y:
            fail("%s : layers[%d] a y=%s, les couches doivent etre ordonnees"
                 " du bas vers le haut" % (name, li, y))
            return None
        prev_y = y

        rows = layer.get("rows")
        if not isinstance(rows, list):
            fail("%s : layers[%d] sans 'rows'" % (name, li))
            return None
        if rows_n is None:
            rows_n = len(rows)
        elif len(rows) != rows_n:
            fail("%s : layers[%d] a %d rangees, attendu %d"
                 % (name, li, len(rows), rows_n))
            return None

        for zi, row in enumerate(rows):
            if cols_n is None:
                cols_n = len(row)
            elif len(row) != cols_n:
                fail("%s : layers[%d].rows[%d] fait %d caracteres, attendu %d"
                     % (name, li, zi, len(row), cols_n))
                return None
            for xi, sym in enumerate(row):
                if sym not in syms:
                    fail("%s : symbole '%s' en layers[%d].rows[%d][%d] absent"
                         " de legend.json" % (name, sym, li, zi, xi))
                    return None
                if sym == ctrl_sym:
                    if ctrl is not None:
                        fail("%s : plusieurs controleurs dans le pattern" % name)
                        return None
                    ctrl = (xi, y, zi)
                raw[(xi, y, zi)] = sym

    if ctrl is None:
        fail("%s : aucun symbole de controleur dans le pattern" % name)
        return None

    cx, cy, cz = ctrl
    return dict(((x - cx, y - cy, z - cz), s) for (x, y, z), s in raw.items())


def check_machine(mid, syms, listed):
    name = "%s.json" % mid
    path = os.path.join(MACHINES, name)
    try:
        data = load_json(path)
    except ValueError as e:
        fail("%s illisible : %s" % (name, e))
        return

    if data.get("id") != mid:
        fail("%s : 'id' vaut '%s', attendu '%s' (nom de fichier)"
             % (name, data.get("id"), mid))

    for key in ("name_fr", "name_en", "controller"):
        if not data.get(key):
            fail("%s : cle '%s' absente" % (name, key))

    controller = data.get("controller", "")
    if controller and ":" not in controller:
        fail("%s : 'controller' = '%s' sans namespace" % (name, controller))

    color = data.get("color", "")
    if color and not re.match(r"^[0-9A-Fa-f]{6}$", color):
        fail("%s : 'color' = '%s', attendu six chiffres hexadecimaux"
             % (name, color))

    cells = parse_pattern(data, name, syms)
    if cells is None:
        return

    xs = [c[0] for c in cells]
    ys = [c[1] for c in cells]
    zs = [c[2] for c in cells]
    w, h, d = max(xs) - min(xs) + 1, max(ys) - min(ys) + 1, max(zs) - min(zs) + 1
    if w > MAX_W or h > MAX_H or d > MAX_D:
        fail("%s : pattern %dx%dx%d hors charte (plafond %dx%dx%d)"
             % (name, w, h, d, MAX_W, MAX_H, MAX_D))
    else:
        ok("%s pattern %dx%dx%d" % (mid, w, h, d))

    check_controller_assets(mid, controller)
    cross_check_mm(mid, cells, syms)


# ------------------------------------------------------------- assets bloc

def registered_blocks():
    """Identifiants nexusabsolu: enregistres, lus dans ModBlocks.java."""
    path = os.path.join(JAVA, "init", "ModBlocks.java")
    if not os.path.isfile(path):
        warn("ModBlocks.java introuvable, controle des blocs saute")
        return None
    src = io.open(path, encoding="utf-8", errors="replace").read()
    ids = set(re.findall(r'"([a-z0-9_]+)"', src))
    # Les blocs sans argument de nom declarent leur registryName dans leur
    # propre classe : on les ramasse en balayant les sources de blocs.
    for base in ("blocks", os.path.join("machines", "block")):
        d = os.path.join(JAVA, base)
        for root, _dirs, files in os.walk(d):
            for f in files:
                if not f.endswith(".java"):
                    continue
                s = io.open(os.path.join(root, f),
                            encoding="utf-8", errors="replace").read()
                ids.update(re.findall(r'setRegistryName\(\s*Reference\.MOD_ID\s*,\s*"([a-z0-9_]+)"', s))
                ids.update(re.findall(r'String\s+name\s*=\s*"([a-z0-9_]+)"', s))
    return ids


def lang_keys(fname):
    path = os.path.join(RES, "lang", fname)
    if not os.path.isfile(path):
        return set()
    keys = set()
    for line in io.open(path, encoding="utf-8", errors="replace"):
        if "=" in line and not line.strip().startswith("#"):
            keys.add(line.split("=", 1)[0].strip())
    return keys


REGISTERED = None
FR_KEYS = None
EN_KEYS = None


def check_block_assets(block_id, origin):
    """Verifie qu'un bloc nexusabsolu: existe vraiment, assets compris."""
    ns, _, short = block_id.partition(":")
    if ns != "nexusabsolu":
        return
    if REGISTERED is not None and short not in REGISTERED:
        fail("%s : bloc '%s' introuvable dans les sources du mod"
             % (origin, block_id))
        return
    missing = []
    if not os.path.isfile(os.path.join(RES, "blockstates", short + ".json")):
        missing.append("blockstates/%s.json" % short)
    if not os.path.isfile(os.path.join(RES, "models", "block", short + ".json")):
        missing.append("models/block/%s.json" % short)
    if not os.path.isfile(os.path.join(RES, "models", "item", short + ".json")):
        missing.append("models/item/%s.json" % short)
    # Deux conventions de cle coexistent dans le mod, selon que
    # setUnlocalizedName prefixe ou non par le MOD_ID. Les deux sont valides.
    keys = ("tile.nexusabsolu.%s.name" % short, "tile.%s.name" % short)
    if not any(k in FR_KEYS for k in keys):
        missing.append("fr_fr.lang:%s" % keys[0])
    if not any(k in EN_KEYS for k in keys):
        missing.append("en_us.lang:%s" % keys[0])
    if missing:
        fail("%s : bloc '%s' incomplet -- %s"
             % (origin, block_id, ", ".join(missing)))
    else:
        ok("bloc %s" % block_id)


def check_texture_of_model(short, origin):
    """Le modele de bloc pointe vers une texture qui existe."""
    path = os.path.join(RES, "models", "block", short + ".json")
    if not os.path.isfile(path):
        return
    try:
        model = load_json(path)
    except ValueError as e:
        fail("%s : models/block/%s.json illisible : %s" % (origin, short, e))
        return
    for _slot, tex in (model.get("textures") or {}).items():
        if not tex.startswith("nexusabsolu:"):
            continue
        rel = tex.split(":", 1)[1]
        png = os.path.join(RES, "textures", rel + ".png")
        if not os.path.isfile(png):
            fail("%s : texture '%s' declaree par models/block/%s.json"
                 " mais absente" % (origin, tex, short))
        else:
            ok("texture %s" % tex)


def check_controller_assets(mid, controller):
    check_block_assets(controller, "%s.json" % mid)
    if controller.startswith("nexusabsolu:"):
        check_texture_of_model(controller.split(":", 1)[1], "%s.json" % mid)


# ------------------------------------------------------- controle croise MM

def cross_check_mm(mid, cells, syms):
    """Le pattern Nexus reproduit-il celui de Modular Machinery ?"""
    path = os.path.join(MM_MACHINERY, "%s.json" % mid)
    if not os.path.isfile(path):
        warn("%s : pas de machinery/%s.json cote MM, controle croise saute"
             % (mid, mid))
        return
    try:
        mm = load_json(path)
    except ValueError as e:
        fail("machinery/%s.json illisible : %s" % (mid, e))
        return

    mm_cells = {}
    for p in mm.get("parts", []):
        mm_cells[(p["x"], p["y"], p["z"])] = p.get("elements", "casings_all")

    nexus_cells = dict(
        (pos, MM_ELEMENTS.get(sym))
        for pos, sym in cells.items()
        if syms.get(sym, {}).get("role") != "controller")

    diffs = []
    for pos in sorted(set(list(mm_cells.keys()) + list(nexus_cells.keys()))):
        a = mm_cells.get(pos)
        b = nexus_cells.get(pos)
        if a != b:
            diffs.append("%s : MM=%s Nexus=%s" % (str(pos), a, b))
    if diffs:
        fail("%s : le pattern diverge de machinery/%s.json (%d ecart(s))\n    %s"
             % (mid, mid, len(diffs), "\n    ".join(diffs[:8])))
    else:
        ok("%s identique au pattern MM (%d blocs)" % (mid, len(mm_cells)))


# ------------------------------------------------------------- encodage

def check_ascii():
    root = os.path.join(JAVA, "machines")
    if not os.path.isdir(root):
        return
    for dirpath, _dirs, files in os.walk(root):
        for f in files:
            if not f.endswith(".java"):
                continue
            p = os.path.join(dirpath, f)
            raw = open(p, "rb").read()
            bad = [i for i, b in enumerate(bytearray(raw)) if b > 127]
            if bad:
                line = raw[:bad[0]].count(b"\n") + 1
                fail("%s : caractere non-ASCII ligne %d ; javac Windows est en"
                     " Cp1252, tout .java doit rester en ASCII pur"
                     % (os.path.relpath(p, ROOT), line))
            else:
                ok("ascii %s" % f)


# ------------------------------------------------------------------ main

def main():
    global REGISTERED, FR_KEYS, EN_KEYS

    print("Controle du moteur de machines -- %s" % ROOT)
    print("")

    REGISTERED = registered_blocks()
    FR_KEYS = lang_keys("fr_fr.lang")
    EN_KEYS = lang_keys("en_us.lang")

    syms = check_legend()
    if syms is None:
        report()
        return 1

    # Tous les blocs cites par la legende doivent exister cote Nexus.
    for key, entry in syms.items():
        for b in entry.get("blocks", []):
            check_block_assets(b, "legend.json/%s" % key)
            if b.startswith("nexusabsolu:"):
                check_texture_of_model(b.split(":", 1)[1], "legend.json/%s" % key)

    listed = check_index()
    for mid in listed:
        check_machine(mid, syms, listed)

    check_ascii()
    return report()


def report():
    print("")
    for w in warnings:
        print("  ATTENTION  %s" % w)
    for e in errors:
        print("  ERREUR     %s" % e)
    print("")
    print("%d controle(s) passe(s), %d avertissement(s), %d erreur(s)"
          % (checks[0], len(warnings), len(errors)))
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
