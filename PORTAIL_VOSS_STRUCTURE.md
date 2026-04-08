########################################################################
# PORTAIL VOSS - Structure attendue par TilePortalVoss.checkStructure()
########################################################################

LEGENDE:
  w  w = Nexus Wall (normal)
  W  W = Nexus Wall T2 (violet anime)
  3  3 = Bloc de Vossium III
  4  4 = Bloc de Vossium IV
  E  E = Energy Input (bloc dedie)
  L  L = Fluid Input (bloc dedie)
  D  D = Bloc de Compose D
  ~  ~ = Lava (vanilla)
  X  X = Ecran de Controle (MASTER, le bloc que tu cliques)
  .  (air / pas de bloc requis)

ORIENTATION:
  L'Ecran de Controle (X) est le bloc MASTER a y=0 (niveau de reference).
  x = axe est-ouest (negatif = ouest)
  z = axe nord-sud (negatif = nord)
  y = hauteur (0 = niveau de l'ecran, negatif = en dessous)
  La structure supporte les 4 rotations cardinales automatiquement.


=== LAYER y=-5  (BASE 7x7 - Fondation de vossium III/IV avec Energy/Fluid hatches) ===

         -4  -3  -2  -1  +0  +1  +2  +3  +4 
      +------------------------------------+
  z=-4 |  .   .   .   .   .   .   .   .   .  |
  z=-3 |  .   W   W   W   E   W   W   W   .  |
  z=-2 |  .   W   W   3   3   3   W   W   .  |
  z=-1 |  .   W   3   W   3   W   3   W   .  |
  z=+0 |  .   W   3   3   4   3   3   L   .  |
  z=+1 |  .   W   3   W   3   W   3   W   .  |
  z=+2 |  .   W   W   3   3   3   W   W   .  |
  z=+3 |  .   W   W   W   W   W   W   W   .  |
  z=+4 |  .   .   .   .   .   .   .   .   .  |
      +------------------------------------+

=== LAYER y=-4  (BASSIN - Piscine de lave autour du cube V4 + coins Compose D) ===

         -3  -2  -1  +0  +1  +2  +3 
      +----------------------------+
  z=-3 |  .   .   .   .   .   .   .  |
  z=-2 |  .   D   w   w   w   D   .  |
  z=-1 |  .   w   ~   ~   ~   w   .  |
  z=+0 |  .   w   ~   4   ~   w   .  |
  z=+1 |  .   w   ~   ~   ~   w   .  |
  z=+2 |  .   D   w   w   w   D   .  |
  z=+3 |  .   .   .   .   .   .   .  |
      +----------------------------+

=== LAYER y=-3  (COLONNE - Plateforme 3x3 en walls avec coeur W2) ===

         -2  -1  +0  +1  +2 
      +--------------------+
  z=-2 |  .   .   .   .   .  |
  z=-1 |  .   w   w   w   .  |
  z=+0 |  .   w   W   w   .  |
  z=+1 |  .   w   w   w   .  |
  z=+2 |  .   .   .   .   .  |
      +--------------------+

=== LAYER y=-2  (PILIERS BAS - 2 piliers W2 de chaque cote du futur portail) ===

         -2  -1  +0  +1  +2 
      +--------------------+
  z=-1 |  .   .   .   .   .  |
  z=+0 |  .   W   .   W   .  |
  z=+1 |  .   .   .   .   .  |
      +--------------------+

=== LAYER y=-1  (PILIERS MILIEU - 2 piliers W2 (zone du portail entre les 2)) ===

         -2  -1  +0  +1  +2 
      +--------------------+
  z=-1 |  .   .   .   .   .  |
  z=+0 |  .   W   .   W   .  |
  z=+1 |  .   .   .   .   .  |
      +--------------------+

=== LAYER y=+0  (ECRAN DE CONTROLE + PILIERS - master au milieu, piliers W2 a cote) ===

         -2  -1  +0  +1  +2 
      +--------------------+
  z=-1 |  .   .   .   .   .  |
  z=+0 |  .   W   X   W   .  |
  z=+1 |  .   .   .   .   .  |
      +--------------------+

=== LAYER y=+1  (PONT HORIZONTAL - passerelle de walls avec extremites W2) ===

         -3  -2  -1  +0  +1  +2  +3 
      +----------------------------+
  z=-1 |  .   .   .   .   .   .   .  |
  z=+0 |  .   W   w   w   w   W   .  |
  z=+1 |  .   .   .   .   .   .   .  |
      +----------------------------+

=== LAYER y=+2  (CORNES EXTERIEURES - 2 walls de chaque cote, 1 bloc de plus qu'en dessous) ===

         -4  -3  -2  -1  +0  +1  +2  +3  +4 
      +------------------------------------+
  z=-1 |  .   .   .   .   .   .   .   .   .  |
  z=+0 |  .   w   w   .   .   .   w   w   .  |
  z=+1 |  .   .   .   .   .   .   .   .   .  |
      +------------------------------------+

=== LAYER y=+3  (POINTES DES CORNES - 2 walls isoles aux extremites) ===

         -4  -3  -2  -1  +0  +1  +2  +3  +4 
      +------------------------------------+
  z=-1 |  .   .   .   .   .   .   .   .   .  |
  z=+0 |  .   w   .   .   .   .   .   w   .  |
  z=+1 |  .   .   .   .   .   .   .   .   .  |
      +------------------------------------+


########################################################################
# MATERIAUX NECESSAIRES (hors Ecran de Controle, pose en dernier)
########################################################################
   29 x  Nexus Wall (normal)
   39 x  Nexus Wall T2 (violet anime)
   16 x  Bloc de Vossium III
    2 x  Bloc de Vossium IV
    1 x  Energy Input (bloc dedie)
    1 x  Fluid Input (bloc dedie)
    4 x  Bloc de Compose D
    8 x  Lava (vanilla)
  ---
  101 blocs au total (incluant l'Ecran de Controle)
