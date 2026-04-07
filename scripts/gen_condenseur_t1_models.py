#!/usr/bin/env python3
"""
Generate Condenseur T1 formed-block 3D MODELS (JSON).

Each block has a 16x16x16 base cube + 3D relief elements (IE-style):
buttons that pop out, rivets in bump, screen panel, valve, etc.

Coordinates: [0,0,0] = back-left-bottom, [16,16,16] = front-right-top
"""

import json
import os

OUT = "mod-source/src/main/resources/assets/nexusabsolu/models/block"

TEX = {
    "side":      "nexusabsolu:blocks/condenseur_formed_side",
    "front":     "nexusabsolu:blocks/condenseur_formed_front",
    "energy":    "nexusabsolu:blocks/condenseur_formed_energy",
    "top":       "nexusabsolu:blocks/condenseur_formed_top",
    "bottom":    "nexusabsolu:blocks/condenseur_formed_bottom",
    "glass":     "nexusabsolu:blocks/condenseur_formed_glass",
    "top_wall":  "nexusabsolu:blocks/condenseur_formed_top_wall_top",
}

# ========= HELPERS =========

def cube(x1, y1, z1, x2, y2, z2, faces):
    el = {"from": [x1, y1, z1], "to": [x2, y2, z2], "faces": {}}
    for fname, info in faces.items():
        f = {"uv": info.get("uv", [0, 0, 16, 16]), "texture": "#" + info["tex"]}
        if "cullface" in info:
            f["cullface"] = info["cullface"]
        el["faces"][fname] = f
    return el

def base_cube(north_tex, south_tex, east_tex, west_tex, up_tex, down_tex):
    return cube(0, 0, 0, 16, 16, 16, {
        "north": {"tex": north_tex, "cullface": "north"},
        "south": {"tex": south_tex, "cullface": "south"},
        "east":  {"tex": east_tex,  "cullface": "east"},
        "west":  {"tex": west_tex,  "cullface": "west"},
        "up":    {"tex": up_tex,    "cullface": "up"},
        "down":  {"tex": down_tex,  "cullface": "down"},
    })

# ========= MODEL 1: MASTER =========

def model_master():
    """
    Master block with 3D relief on north + west faces (the 'front' faces).
    Reliefs: VOSS-7 plate, CRT screen panel, 2 mechanical buttons.
    """
    elements = [
        base_cube("front", "side", "side", "front", "top", "bottom"),
    ]

    # NORTH face reliefs (z < 0 sticks out toward viewer)
    # VOSS-7 plate
    elements.append(cube(2, 11, -0.5, 14, 14, 0, {
        "north": {"tex": "front", "uv": [2, 2, 14, 5]},
    }))
    # CRT screen panel
    elements.append(cube(3.5, 5, -0.5, 12, 10, 0, {
        "north": {"tex": "front", "uv": [3.5, 6, 12, 11]},
    }))
    # Button left (pops out 1px)
    elements.append(cube(3, 2, -1, 4.5, 3.5, 0, {
        "north": {"tex": "front", "uv": [3, 12.5, 4.5, 14]},
        "south": {"tex": "front", "uv": [3, 12.5, 4.5, 14]},
        "east":  {"tex": "side",  "uv": [0, 12.5, 1, 14]},
        "west":  {"tex": "side",  "uv": [0, 12.5, 1, 14]},
        "up":    {"tex": "front", "uv": [3, 12.5, 4.5, 13]},
        "down":  {"tex": "front", "uv": [3, 13, 4.5, 14]},
    }))
    # Button right
    elements.append(cube(11, 2, -1, 12.5, 3.5, 0, {
        "north": {"tex": "front", "uv": [11, 12.5, 12.5, 14]},
        "south": {"tex": "front", "uv": [11, 12.5, 12.5, 14]},
        "east":  {"tex": "side",  "uv": [0, 12.5, 1, 14]},
        "west":  {"tex": "side",  "uv": [0, 12.5, 1, 14]},
        "up":    {"tex": "front", "uv": [11, 12.5, 12.5, 13]},
        "down":  {"tex": "front", "uv": [11, 13, 12.5, 14]},
    }))

    # WEST face reliefs (x < 0 sticks out toward viewer)
    elements.append(cube(-0.5, 11, 2, 0, 14, 14, {
        "west": {"tex": "front", "uv": [2, 2, 14, 5]},
    }))
    elements.append(cube(-0.5, 5, 4, 0, 10, 12.5, {
        "west": {"tex": "front", "uv": [3.5, 6, 12, 11]},
    }))
    elements.append(cube(-1, 2, 11.5, 0, 3.5, 13, {
        "west":  {"tex": "front", "uv": [11, 12.5, 12.5, 14]},
        "east":  {"tex": "front", "uv": [11, 12.5, 12.5, 14]},
        "north": {"tex": "side",  "uv": [0, 12.5, 1, 14]},
        "south": {"tex": "side",  "uv": [0, 12.5, 1, 14]},
        "up":    {"tex": "front", "uv": [11, 12.5, 12.5, 13]},
        "down":  {"tex": "front", "uv": [11, 13, 12.5, 14]},
    }))
    elements.append(cube(-1, 2, 3, 0, 3.5, 4.5, {
        "west":  {"tex": "front", "uv": [3, 12.5, 4.5, 14]},
        "east":  {"tex": "front", "uv": [3, 12.5, 4.5, 14]},
        "north": {"tex": "side",  "uv": [0, 12.5, 1, 14]},
        "south": {"tex": "side",  "uv": [0, 12.5, 1, 14]},
        "up":    {"tex": "front", "uv": [3, 12.5, 4.5, 13]},
        "down":  {"tex": "front", "uv": [3, 13, 4.5, 14]},
    }))

    return {
        "ambientocclusion": False,
        "textures": {
            "particle": TEX["front"],
            "front":  TEX["front"],
            "side":   TEX["side"],
            "top":    TEX["top"],
            "bottom": TEX["bottom"],
        },
        "elements": elements
    }

# ========= MODEL 2: BOTTOM WALL (positions 1, 2) =========

def model_bottom_wall():
    """
    Bottom-row nexus walls. Reliefs on all 4 vertical faces:
    - Plate '01' raised
    - LED dot raised
    - Cable strip raised
    - 4 corner rivets raised
    """
    elements = [
        base_cube("side", "side", "side", "side", "top", "bottom"),
    ]

    # Helper: add reliefs to one vertical face
    # face_dir: "n", "s", "e", "w"
    def add(face_dir):
        if face_dir == "n":
            # Plate "01" 
            elements.append(cube(8, 10.5, -0.5, 13.5, 13.5, 0, {
                "north": {"tex": "side", "uv": [8, 2.5, 13.5, 5.5]}
            }))
            # LED
            elements.append(cube(11, 3.5, -0.5, 12.5, 5, 0, {
                "north": {"tex": "side", "uv": [11, 11, 12.5, 12.5]}
            }))
            # Cable strip
            elements.append(cube(2.5, 3, -0.3, 4.5, 13.5, 0, {
                "north": {"tex": "side", "uv": [2.5, 2.5, 4.5, 13]}
            }))
            # 4 corner rivets
            for (rx, ry) in [(1, 1), (13, 1), (1, 13), (13, 13)]:
                elements.append(cube(rx, ry, -0.5, rx+1, ry+1, 0, {
                    "north": {"tex": "side", "uv": [rx, 15-ry, rx+1, 16-ry]}
                }))

        elif face_dir == "s":
            elements.append(cube(2.5, 10.5, 16, 8, 13.5, 16.5, {
                "south": {"tex": "side", "uv": [8, 2.5, 13.5, 5.5]}
            }))
            elements.append(cube(3.5, 3.5, 16, 5, 5, 16.5, {
                "south": {"tex": "side", "uv": [11, 11, 12.5, 12.5]}
            }))
            elements.append(cube(11.5, 3, 16, 13.5, 13.5, 16.3, {
                "south": {"tex": "side", "uv": [2.5, 2.5, 4.5, 13]}
            }))
            for (rx, ry) in [(2, 1), (14, 1), (2, 13), (14, 13)]:
                elements.append(cube(rx, ry, 16, rx+1, ry+1, 16.5, {
                    "south": {"tex": "side", "uv": [rx, 15-ry, rx+1, 16-ry]}
                }))

        elif face_dir == "e":
            elements.append(cube(16, 10.5, 8, 16.5, 13.5, 13.5, {
                "east": {"tex": "side", "uv": [8, 2.5, 13.5, 5.5]}
            }))
            elements.append(cube(16, 3.5, 11, 16.5, 5, 12.5, {
                "east": {"tex": "side", "uv": [11, 11, 12.5, 12.5]}
            }))
            elements.append(cube(16, 3, 2.5, 16.3, 13.5, 4.5, {
                "east": {"tex": "side", "uv": [2.5, 2.5, 4.5, 13]}
            }))
            for (rz, ry) in [(1, 1), (13, 1), (1, 13), (13, 13)]:
                elements.append(cube(16, ry, rz, 16.5, ry+1, rz+1, {
                    "east": {"tex": "side", "uv": [rz, 15-ry, rz+1, 16-ry]}
                }))

        elif face_dir == "w":
            elements.append(cube(-0.5, 10.5, 2.5, 0, 13.5, 8, {
                "west": {"tex": "side", "uv": [8, 2.5, 13.5, 5.5]}
            }))
            elements.append(cube(-0.5, 3.5, 11, 0, 5, 12.5, {
                "west": {"tex": "side", "uv": [11, 11, 12.5, 12.5]}
            }))
            elements.append(cube(-0.3, 3, 11.5, 0, 13.5, 13.5, {
                "west": {"tex": "side", "uv": [2.5, 2.5, 4.5, 13]}
            }))
            for (rz, ry) in [(2, 1), (14, 1), (2, 13), (14, 13)]:
                elements.append(cube(-0.5, ry, rz, 0, ry+1, rz+1, {
                    "west": {"tex": "side", "uv": [rz, 15-ry, rz+1, 16-ry]}
                }))

    for d in ("n", "s", "e", "w"):
        add(d)

    return {
        "ambientocclusion": False,
        "textures": {
            "particle": TEX["side"],
            "side":   TEX["side"],
            "top":    TEX["top"],
            "bottom": TEX["bottom"],
        },
        "elements": elements
    }

# ========= MODEL 3: ENERGY INPUT =========

def model_energy():
    """
    Energy input - keeps redstone block character.
    Reliefs: RF plate, central lightning panel raised, 3 horizontal grille bars raised more.
    """
    elements = [
        base_cube("energy", "energy", "energy", "energy", "top", "bottom"),
    ]

    def add(face_dir):
        if face_dir == "n":
            elements.append(cube(5.5, 11.5, -0.5, 10, 14, 0, {
                "north": {"tex": "energy", "uv": [5.5, 1.5, 10, 4]}
            }))
            elements.append(cube(5, 4, -0.5, 11, 11, 0, {
                "north": {"tex": "energy", "uv": [5, 5, 11, 12]}
            }))
            for gy in [10, 8, 6]:
                elements.append(cube(6, gy, -1, 10, gy+0.5, -0.5, {
                    "north": {"tex": "energy", "uv": [6, 16-gy-0.5, 10, 16-gy]}
                }))
        elif face_dir == "s":
            elements.append(cube(6, 11.5, 16, 10.5, 14, 16.5, {
                "south": {"tex": "energy", "uv": [5.5, 1.5, 10, 4]}
            }))
            elements.append(cube(5, 4, 16, 11, 11, 16.5, {
                "south": {"tex": "energy", "uv": [5, 5, 11, 12]}
            }))
            for gy in [10, 8, 6]:
                elements.append(cube(6, gy, 16.5, 10, gy+0.5, 17, {
                    "south": {"tex": "energy", "uv": [6, 16-gy-0.5, 10, 16-gy]}
                }))
        elif face_dir == "e":
            elements.append(cube(16, 11.5, 6, 16.5, 14, 10.5, {
                "east": {"tex": "energy", "uv": [5.5, 1.5, 10, 4]}
            }))
            elements.append(cube(16, 4, 5, 16.5, 11, 11, {
                "east": {"tex": "energy", "uv": [5, 5, 11, 12]}
            }))
            for gy in [10, 8, 6]:
                elements.append(cube(16.5, gy, 6, 17, gy+0.5, 10, {
                    "east": {"tex": "energy", "uv": [6, 16-gy-0.5, 10, 16-gy]}
                }))
        elif face_dir == "w":
            elements.append(cube(-0.5, 11.5, 5.5, 0, 14, 10, {
                "west": {"tex": "energy", "uv": [5.5, 1.5, 10, 4]}
            }))
            elements.append(cube(-0.5, 4, 5, 0, 11, 11, {
                "west": {"tex": "energy", "uv": [5, 5, 11, 12]}
            }))
            for gy in [10, 8, 6]:
                elements.append(cube(-1, gy, 6, -0.5, gy+0.5, 10, {
                    "west": {"tex": "energy", "uv": [6, 16-gy-0.5, 10, 16-gy]}
                }))

    for d in ("n", "s", "e", "w"):
        add(d)

    return {
        "ambientocclusion": False,
        "textures": {
            "particle": TEX["energy"],
            "energy": TEX["energy"],
            "top":    TEX["top"],
            "bottom": TEX["bottom"],
        },
        "elements": elements
    }

# ========= MODEL 4: TOP GLASS =========

def model_top_glass():
    """
    Glass with metal cross armature in relief on TOP face.
    """
    elements = [
        cube(0, 0, 0, 16, 16, 16, {
            "north": {"tex": "glass", "cullface": "north"},
            "south": {"tex": "glass", "cullface": "south"},
            "east":  {"tex": "glass", "cullface": "east"},
            "west":  {"tex": "glass", "cullface": "west"},
            "up":    {"tex": "glass", "cullface": "up"},
            "down":  {"tex": "glass", "cullface": "down"},
        }),
        # Beam X axis
        cube(0, 16, 7, 16, 17, 9, {
            "up":    {"tex": "glass", "uv": [0, 7, 16, 9]},
            "north": {"tex": "glass", "uv": [0, 7, 16, 8]},
            "south": {"tex": "glass", "uv": [0, 7, 16, 8]},
            "east":  {"tex": "glass", "uv": [7, 7, 9, 8]},
            "west":  {"tex": "glass", "uv": [7, 7, 9, 8]},
        }),
        # Beam Z axis
        cube(7, 16, 0, 9, 17, 16, {
            "up":    {"tex": "glass", "uv": [7, 0, 9, 16]},
            "north": {"tex": "glass", "uv": [7, 7, 9, 8]},
            "south": {"tex": "glass", "uv": [7, 7, 9, 8]},
            "east":  {"tex": "glass", "uv": [0, 7, 16, 8]},
            "west":  {"tex": "glass", "uv": [0, 7, 16, 8]},
        }),
        # Center boss
        cube(6, 17, 6, 10, 18.5, 10, {
            "up":    {"tex": "glass", "uv": [6, 6, 10, 10]},
            "north": {"tex": "glass", "uv": [6, 6, 10, 7.5]},
            "south": {"tex": "glass", "uv": [6, 6, 10, 7.5]},
            "east":  {"tex": "glass", "uv": [6, 6, 10, 7.5]},
            "west":  {"tex": "glass", "uv": [6, 6, 10, 7.5]},
        }),
    ]

    return {
        "ambientocclusion": False,
        "textures": {
            "particle": TEX["glass"],
            "glass": TEX["glass"],
        },
        "elements": elements
    }

# ========= MODEL 5: TOP WALL (with exhaust valve) =========

def model_top_wall():
    """
    Top wall with exhaust valve in relief on TOP face, plus 4 corner bolts.
    """
    elements = [
        base_cube("side", "side", "side", "side", "top_wall", "bottom"),
    ]

    # Valve outer collar (large flat disc)
    elements.append(cube(5, 16, 5, 11, 16.5, 11, {
        "up":    {"tex": "top_wall", "uv": [5, 5, 11, 11]},
        "north": {"tex": "top_wall", "uv": [5, 10, 11, 10.5]},
        "south": {"tex": "top_wall", "uv": [5, 10, 11, 10.5]},
        "east":  {"tex": "top_wall", "uv": [5, 10, 11, 10.5]},
        "west":  {"tex": "top_wall", "uv": [5, 10, 11, 10.5]},
    }))

    # Valve raised middle (chimney top)
    elements.append(cube(6, 16.5, 6, 10, 18, 10, {
        "up":    {"tex": "top_wall", "uv": [6, 6, 10, 10]},
        "north": {"tex": "top_wall", "uv": [6, 6, 10, 7.5]},
        "south": {"tex": "top_wall", "uv": [6, 6, 10, 7.5]},
        "east":  {"tex": "top_wall", "uv": [6, 6, 10, 7.5]},
        "west":  {"tex": "top_wall", "uv": [6, 6, 10, 7.5]},
    }))

    # 4 corner mounting bolts
    for (bx, bz) in [(4, 4), (11, 4), (4, 11), (11, 11)]:
        elements.append(cube(bx, 16, bz, bx+1.5, 16.5, bz+1.5, {
            "up":    {"tex": "top_wall", "uv": [bx, bz, bx+1.5, bz+1.5]},
            "north": {"tex": "top_wall", "uv": [bx, bz, bx+1.5, bz+0.5]},
            "south": {"tex": "top_wall", "uv": [bx, bz, bx+1.5, bz+0.5]},
            "east":  {"tex": "top_wall", "uv": [bx, bz, bx+1.5, bz+0.5]},
            "west":  {"tex": "top_wall", "uv": [bx, bz, bx+1.5, bz+0.5]},
        }))

    return {
        "ambientocclusion": False,
        "textures": {
            "particle": TEX["side"],
            "side":     TEX["side"],
            "top_wall": TEX["top_wall"],
            "bottom":   TEX["bottom"],
        },
        "elements": elements
    }

# ========= MAIN =========

def main():
    os.makedirs(OUT, exist_ok=True)

    files = {
        "condenseur_formed_master.json":     model_master(),
        "condenseur_formed_bottom.json":     model_bottom_wall(),
        "condenseur_formed_energy.json":     model_energy(),
        "condenseur_formed_top_glass.json":  model_top_glass(),
        "condenseur_formed_top_wall.json":   model_top_wall(),
    }

    for name, model in files.items():
        path = os.path.join(OUT, name)
        with open(path, "w") as f:
            json.dump(model, f, indent=2)
        n_elements = len(model.get("elements", []))
        print(f"  wrote {n_elements:3d} elements  {path}")

    print(f"\n{len(files)} 3D models generated.")

if __name__ == "__main__":
    main()
