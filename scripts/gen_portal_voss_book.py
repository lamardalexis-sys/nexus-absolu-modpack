#!/usr/bin/env python3
"""
Generate the JSON snippet for a 'Plans du Portail Voss' written_book
that goes as a reward in Q146 of DefaultQuests.json.

The written_book NBT format uses 'pages' as a List<String>, where each
string is itself a JSON text component (like {"text":"..."}).

Output: a properly-escaped JSON snippet ready to paste into BQ rewards.
"""

import json

# Each page is plain text. Use real newlines (we'll convert to \n in JSON).
# Use plain ASCII; the § color codes work in 1.12 written_books.
# Approx 14 lines per page max for readability.

PAGES = [
    # Page 1 - Title
    """§l§5Plans du Portail§r
    
Traces par le
Dr. Elias Voss

Ces plans decrivent
les 9 couches du
Portail dimensionnel.

Construis-les dans
ta Compact Machine
9x9.""",

    # Page 2 - Legend
    """§lLegende :§r

w = Nexus Wall
W = Nexus Wall T2
3 = Vossium III
4 = Vossium IV
E = Energy Input
F = Fluid Input
D = Compose D
~ = Lave
X = Ecran Controle""",

    # Page 3 - Layer y=-5 (base 7x7)
    """§lCouche -5§r
(la base, 7x7)

W W W E W W W
W W 3 3 3 W W
W 3 W 3 W 3 W
W 3 3 4 3 3 F
W 3 W 3 W 3 W
W W 3 3 3 W W
W W W W W W W

E et F opposes.""",

    # Page 4 - Layer y=-4 (lava + Compose D)
    """§lCouche -4§r
(le bassin)

D w w w D
w ~ ~ ~ w
w ~ 4 ~ w
w ~ ~ ~ w
D w w w D

8 lave autour du
2eme bloc Vossium IV
au centre.""",

    # Page 5 - Layer y=-3
    """§lCouche -3§r
(plateforme)

w w w
w W w
w w w

3x3 de murs avec
un Wall T2 au centre
juste au-dessus du
Vossium IV.""",

    # Page 6 - Pillars y=-2 to y=0
    """§lCouches -2 a 0§r
(les piliers)

A chaque niveau,
2 piliers Wall T2.

Position : a gauche
et a droite du
centre, de chaque
cote du futur
portail.

L'§lECRAN§r est place
au centre, niveau 0.""",

    # Page 7 - Bridge + horns
    """§lCouche +1§r
(le pont)

W w w w W

§lCouches +2 et +3§r
(les cornes)

w w . . . w w
. . . . . . .
w . . . . . w

Branches externes
au-dessus du pont.""",

    # Page 8 - Activation
    """§l§6Activation§r

§e1.§r Branche un
Energy Input au F
arriere et charge
§61,000,000 RF§r.

§e2.§r Branche un
Fluid Input et
remplis-le avec
§d10 buckets§r de
Diarrhee Liquide.""",

    # Page 9 - Final / Voss farewell
    """§e3.§r Tiens la
§dCle de Liberte§r
en main.

§e4.§r Clique droit
sur l'§lECRAN§r.

§r§o
Si tu lis ceci,
c'est que tu as
reussi la ou j'ai
echoue.

— E. Voss§r""",
]


def to_text_component(page):
    """Convert plain page text to a JSON text component string."""
    # In Minecraft, a written_book page is itself a JSON-encoded
    # text component. The simplest form is {"text":"..."}.
    obj = {"text": page}
    return json.dumps(obj, ensure_ascii=False)


def main():
    print("=== Pages as JSON text components ===\n")
    pages_json = [to_text_component(p) for p in PAGES]
    for i, p in enumerate(pages_json):
        print(f"Page {i+1} length={len(p)} chars:")
        print(f"  {p[:100]}{'...' if len(p) > 100 else ''}")
        print()

    # Now build the full BQ tag:10 structure.
    # For BetterQuesting, the NBT-typed format uses "pages:9" as a TagList
    # whose entries are TAG_String. The format is just an array of strings.
    print("\n=== Full BQ snippet for written_book reward ===\n")
    snippet = {
        "id:8": "minecraft:written_book",
        "Count:3": 1,
        "Damage:2": 0,
        "OreDict:8": "",
        "tag:10": {
            "title:8": "Plans du Portail Voss",
            "author:8": "Dr. E. Voss",
            "pages:9": pages_json,
            "resolved:1": 1,
            "generation:3": 0
        }
    }

    # Pretty-print with the indent matching DefaultQuests.json (2 spaces)
    print(json.dumps(snippet, indent=2, ensure_ascii=False))

    # Also verify total size
    full = json.dumps(snippet, ensure_ascii=False)
    print(f"\n\nTotal snippet size: {len(full)} bytes")
    print(f"Total pages: {len(PAGES)}")

    # Save to file for clean copy-paste
    with open("/tmp/portal_voss_book_snippet.json", "w", encoding="utf-8") as f:
        f.write(json.dumps(snippet, indent=14, ensure_ascii=False))
    print("\nWrote /tmp/portal_voss_book_snippet.json")


if __name__ == "__main__":
    main()
