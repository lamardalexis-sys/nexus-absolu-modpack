# -*- coding: utf-8 -*-
"""Remplace les codes de couleur illisibles du Carnet Voss.

Patchouli rend les pages sur un fond parchemin clair. Les couleurs vives
heritees des codes Minecraft y passent mal :
  $(b) = bleu clair  -> quasi invisible
  $(d) = rose        -> quasi invisible
  $(c) = ?           -> idem
  $(6) = or          -> idem

Remplacement par des codes a fort contraste sur fond clair :
  $(l)  gras            pour la mise en evidence structurelle (etapes, noms)
  $(#a00) rouge sombre  pour les valeurs et quantites
  $(#248) bleu sombre   pour les noms de blocs et machines
"""
import glob,re

REPL = [
    # $(b)...$(/b) : mise en evidence -> gras
    (re.compile(r'\$\(b\)(.*?)\$\(/b\)', re.S), r'$(l)\1$(/l)'),
    # $(d)...$(/d) : items importants -> bleu sombre
    (re.compile(r'\$\(d\)(.*?)\$\(/d\)', re.S), r'$(#248)\1$()'),
    # $(c)...$(/c)
    (re.compile(r'\$\(c\)(.*?)\$\(/c\)', re.S), r'$(#a00)\1$()'),
    # $(6)...$(/6) : valeurs chiffrees -> rouge sombre
    (re.compile(r'\$\(6\)(.*?)\$\(/6\)', re.S), r'$(#a00)\1$()'),
]
# balises orphelines restantes
ORPHAN = [(re.compile(r'\$\(b\)'), '$(l)'), (re.compile(r'\$\(/b\)'), '$(/l)'),
          (re.compile(r'\$\(d\)'), '$(#248)'), (re.compile(r'\$\(/d\)'), '$()'),
          (re.compile(r'\$\(c\)'), '$(#a00)'), (re.compile(r'\$\(/c\)'), '$()'),
          (re.compile(r'\$\(6\)'), '$(#a00)'), (re.compile(r'\$\(/6\)'), '$()')]

n=0; files=0
for p in sorted(glob.glob('mod-source/src/main/resources/assets/nexusabsolu/patchouli_books/**/*.json',recursive=True)):
    t=open(p,encoding='utf-8').read(); orig=t
    for rx,to in REPL:
        t,k=rx.subn(to,t); n+=k
    for rx,to in ORPHAN:
        t,k=rx.subn(to,t); n+=k
    if t!=orig:
        open(p,'w',encoding='utf-8').write(t); files+=1
print("%d remplacements dans %d fichiers"%(n,files))
