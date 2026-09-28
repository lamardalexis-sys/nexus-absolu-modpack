# -*- coding: utf-8 -*-
"""Genere la ligne de quetes Coding de Nexus Absolu dans DefaultQuests.json.

30 quetes, IDs 3000-3029, du "je n'ai jamais programme" a "j'automatise mon
usine". Voir CODING_DESIGN.md pour le pourquoi de chaque palier.

  Palier 0  La logique, sans code       RFTools (blocs de logique)
  Palier 1  Programmer avec des blocs   RFTools Control
  Palier 2  Ecrire du code              ComputerCraft 1.80 (Lua 5.1)
  Palier 3  Les composants              OpenComputers 1.8.9 (OpenOS, Lua 5.3)
  Finale    Le Rapport de Production    OBLIGATOIRE pour l'Age 3 (voir plus bas)
            L'Architecte numerique

Oui et non : la ligne est un tutoriel parallele, a son rythme. Une seule quete
est obligatoire, "Le Rapport de Production" (3028) : "VERS L'AGE 3" (156)
l'exige a la place de l'ancienne "Premiere Turtle" (150). Elle ne depend que du
Void Ore Miner T1 (quete 123 de l'Age 2) : un joueur qui sait deja programmer
n'a pas a faire le tutoriel.

Faits verifies dans les sources (rapports dans CODING_DESIGN.md) :
- ComputerCraft 1.80pr1 (dan200) : ids a meta (peripheral:0..5, cable:1),
  pas de noms CC:Tweaked. Un ordinateur ou une turtle etiquetes changent de
  damage : on exige Damage 32767 (joker BetterQuesting).
- OpenComputers 1.8.9a : items regroupes (component, card, storage, upgrade)
  avec meta. L'EEPROM porte son label dans tag.oc:data.oc:label.
- RFTools Control 2.0.2 : la carte programme garde sa grille en NBT
  (grid:[{id:"opcode",...}]). BetterQuesting, en partialMatch, accepte une
  liste qui CONTIENT les elements demandes : on peut donc exiger qu'un
  programme utilise tel opcode.
- BetterQuesting 3.5.329 : proprietes en minuscules ; une cle absente vaut 0 ;
  un prerequis inexistant bloque la quete pour toujours.

Idempotent : retire puis reecrit les quetes de la ligne, relancable a volonte.

    python3 scripts/gen/gen_quests_coding.py
"""
import collections
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DQ = os.path.join(ROOT, 'config', 'betterquesting', 'DefaultQuests.json')

URL = 'https://raw.githubusercontent.com/lamardalexis-sys/nexus-absolu-modpack/main/programmes/'

CODING_LINE_ID = 3
AGE2_LINE_NAME = 'Age 2'
OLD_CODING = [150, 157, 158, 159, 160, 161, 162]   # ancienne ligne, remplacee
GATE = 3028            # Le Rapport de Production
VERS_AGE3 = 156        # exige GATE a la place de 150
VOID_MINER_T1 = 123    # seul prerequis de GATE
AGE2_START = 97        # "Tu es sorti" : ouvre la ligne
GATE_POS_AGE2 = (-60, 1010)


# ------------------------------------------------------------ construction

def item(iid, meta=0, count=1, tag=None):
    d = collections.OrderedDict()
    d['id:8'] = iid
    d['Count:3'] = count
    d['Damage:2'] = meta
    d['OreDict:8'] = ''
    if tag is not None:
        d['tag:10'] = tag
    return d


def retrieval(*items, nbt=False):
    """Detection sans consommation. nbt=True : le NBT du joueur doit CONTENIR
    celui demande (sous-ensemble, listes comprises)."""
    return {'taskID:8': 'bq_standard:retrieval',
            'requiredItems:9': items,
            'consume:1': 0, 'autoConsume:1': 0, 'groupDetect:1': 0,
            'ignoreNBT:1': 0 if nbt else 1, 'partialMatch:1': 1}


def checkbox():
    return {'taskID:8': 'bq_standard:checkbox'}


def program_card(*opcodes):
    """Carte programme RFTools Control dont la grille utilise ces opcodes."""
    grid = collections.OrderedDict()
    for i, op in enumerate(opcodes):
        grid['%d:10' % i] = {'id:8': op}
    return item('rftoolscontrol:program_card', 0, 1, {'grid:9': grid})


def eeprom(label):
    return item('opencomputers:storage', 0, 1, {'oc:data:10': {'oc:label:8': label}})


def printout(title):
    # 32767 : page simple, liasse ou livre imprime, tant que le titre y est.
    return item('computercraft:printout', 32767, 1, {'title:8': title})


XP = lambda n: item('minecraft:experience_bottle', 0, n)


# ------------------------------------------------------------ couleurs par palier

C0, C1, C2, C3, CF = '§f', '§b', '§a', '§e', '§6'
CODE = '§a'      # une ligne de code
NOTE = '§7'      # texte normal
VOSS = '§7§o'    # la voix de Voss
OBJ = '\n\n§e§lObjectif : §r§7'


def desc(voss, lecon, objectif):
    """Voix de Voss, puis la lecon, puis l'objectif, comme les quetes de l'Age 3."""
    return VOSS + voss + '§r\n\n' + lecon + OBJ + objectif


def code(*lignes):
    return '\n'.join(CODE + l + '§r' for l in lignes)


# ------------------------------------------------------------ les 30 quetes

Q = []


def quest(qid, pos, name, icon, text, tasks, rewards, prereqs, main=False, size=24,
          tasklogic='AND'):
    Q.append(dict(id=qid, pos=pos, name=name, icon=icon, desc=text, tasks=tasks,
                  rewards=rewards, prereqs=prereqs, main=main, size=size,
                  tasklogic=tasklogic))


# ===== Palier 0 : la logique, sans code (RFTools) ============================
Y0 = 0

quest(3000, (0, Y0), C0 + '§l1. Une entree, une sortie', item('rftools:digit_block'),
      desc("Tout ce que j'ai construit ici tient dans une phrase : quelque chose entre, "
           "quelque chose sort. Le reste n'est que de la patience.",
           NOTE + "Un programme, c'est une §lentree§r§7 transformee en §lsortie§r§7.\n"
           "Le Digit Block affiche la force du signal redstone qu'il recoit, de 0 a 15.\n\n"
           "§b§lExercice :§r§7 un levier, un Wire Block, un Digit Block. Actionne le "
           "levier : 15 s'affiche. Attention au sens : le Wire n'emet que d'un cote, et "
           "le Digit ne lit que son entree. Remplace ensuite le Wire par quelques "
           "poussieres de redstone : le nombre baisse d'un point par poussiere.",
           "1 Digit Block (RFTools)"),
      [retrieval(item('rftools:digit_block'))],
      [item('rftools:wire_block', 0, 4), XP(4)], [AGE2_START])

quest(3001, (60, Y0), C0 + '§l2. Si... alors', item('rftools:invchecker_block'),
      desc("Une machine qui agit toujours est un outil. Une machine qui agit seulement "
           "quand il le faut commence a ressembler a une pensee.",
           NOTE + "La §lcondition§r§7 est l'idee la plus importante de la programmation : "
           "§lSI§r§7 quelque chose est vrai, §lALORS§r§7 on agit.\n"
           "L'Inventory Checker surveille §lune case§r§7 d'un inventaire et emet un "
           "signal quand elle contient au moins N items.\n\n"
           "§b§lExercice :§r§7 un coffre derriere le checker, case 0, seuil 32. Une "
           "lampe s'allume quand le coffre est assez plein.",
           "1 Inventory Checker"),
      [retrieval(item('rftools:invchecker_block'))],
      [item('minecraft:comparator', 0, 2), XP(4)], [3000])

quest(3002, (120, Y0), C0 + '§l3. Le temps qui passe', item('rftools:timer_block'),
      desc("Mes premieres experiences n'echouaient pas faute d'energie. Elles echouaient "
           "parce qu'elles arrivaient trop tot.",
           NOTE + "Un §levenement§r§7, c'est un moment qui declenche une action. Le Timer "
           "envoie une impulsion toutes les N ticks (20 ticks = 1 seconde).\n\n"
           "§b§lExercice :§r§7 un Timer regle sur 100 ticks fait tirer un dropper toutes "
           "les 5 secondes. Active l'option qui met le timer en pause quand il recoit du "
           "redstone, et arrete-le avec un levier.",
           "1 Timer"),
      [retrieval(item('rftools:timer_block'))],
      [item('minecraft:clock'), XP(4)], [3001])

quest(3003, (180, Y0), C0 + '§l4. Se souvenir', item('rftools:counter_block'),
      desc("Le Condenseur ne se souvient de rien. C'est ce qui le rend fiable. C'est "
           "aussi ce qui le rend bete.",
           NOTE + "Une §lvariable§r§7 est une valeur qu'on garde en memoire et qui change. "
           "Le Counter compte les impulsions qu'il recoit, et emet un signal quand il "
           "atteint son seuil, puis repart de zero.\n\n"
           "§b§lExercice :§r§7 branche le Timer de la quete precedente sur un Counter "
           "regle a 10. Il emet un signal toutes les 10 impulsions. Pour voir la valeur, "
           "un Screen, un Screen Controller et un Counter Module.",
           "1 Counter"),
      [retrieval(item('rftools:counter_block'))],
      [item('rftools:counter_module'), XP(4)], [3002])

quest(3004, (240, Y0), C0 + '§l5. Recommencer', item('rftools:sequencer_block'),
      desc("Tout ce qui compte dans ce laboratoire a ete fait cent fois. Seules les "
           "erreurs ont ete faites une seule fois.",
           NOTE + "Une §lboucle§r§7 repete une suite d'instructions. Le Sequencer joue un "
           "motif de 1 a 64 cases, allume ou eteint, en boucle.\n\n"
           "§b§lExercice :§r§7 un motif de 8 cases en mode §lLoop1§r§7 pilote une rangee "
           "de lampes : un chenillard. Passe en mode §lStep§r§7 : chaque appui sur un "
           "bouton avance d'une case.",
           "1 Sequencer"),
      [retrieval(item('rftools:sequencer_block'))],
      [item('minecraft:redstone', 0, 32), XP(4)], [3003])

quest(3005, (300, Y0), C0 + '§l6. ET, OU, NON', item('rftools:logic_block'),
      desc("J'ai ecrit NEXUS.lua avec trois mots. Tout le reste en decoule.",
           NOTE + "Le §lET§r§7 : les deux entrees doivent etre vraies. Le §lOU§r§7 : une "
           "seule suffit. Le §lNON§r§7 : on inverse.\n"
           "La §lLogic Gate§r§7 lit trois entrees (gauche, arriere, droite) et te laisse "
           "choisir la sortie pour chacune des 8 combinaisons : une table de verite.\n\n"
           "§b§lExercice :§r§7 deux leviers, une lampe qui ne s'allume que si les deux "
           "sont actifs. Puis si l'un OU l'autre. Tu sais maintenant tout ce qu'un "
           "processeur sait faire, en plus lent.",
           "1 Logic Gate"),
      [retrieval(item('rftools:logic_block'))],
      [item('rftools:machine_frame'), XP(6)], [3004], main=True, size=32)

# ===== Palier 1 : programmer avec des blocs (RFTools Control) ===============
Y1 = 100

quest(3006, (0, Y1), C1 + '§l7. Le processeur', item('rftoolscontrol:processor'),
      desc("Il vient un moment ou les blocs ne suffisent plus. On veut dire a la machine "
           "une suite de choses, dans l'ordre.",
           NOTE + "RFTools Control fait programmer §lsans taper de code§r§7 : on pose des "
           "instructions (opcodes) sur une grille et on les relie par des fleches.\n"
           "- le §lProgrammer§r§7 edite une Program Card ;\n"
           "- le §lProcessor§r§7 execute jusqu'a 6 cartes. Il lui faut du RF et au "
           "moins un §lCPU Core§r§7 (le B500 fait 1 instruction par tick).",
           "1 Processor, 1 Programmer, 1 Program Card, 1 CPU Core B500"),
      [retrieval(item('rftoolscontrol:processor'), item('rftoolscontrol:programmer'),
                 item('rftoolscontrol:program_card'), item('rftoolscontrol:cpu_core_500'))],
      [item('rftoolscontrol:program_card', 0, 4), XP(6)], [3005])

quest(3007, (60, Y1), C1 + '§l8. Premier programme', item('rftoolscontrol:program_card'),
      desc("Le premier programme de ma vie faisait clignoter une lampe. Je l'ai regardee "
           "une heure. Je n'ai jamais compris pourquoi c'etait si satisfaisant.",
           NOTE + "Tout programme commence par un §levenement§r§7, sinon il ne tourne "
           "jamais.\n\n"
           "§b§lConstruis :§r§7\n"
           "- §lev_timer§r§7 (toutes les 20 ticks)\n"
           "- -> §ldo_rs§r§7 (cote de la lampe, niveau 15)\n"
           "- -> §ldo_delay§r§7 (10 ticks)\n"
           "- -> §ldo_rs§r§7 (niveau 0)\n\n"
           "Pour relier deux opcodes, double-clique sur le bord de l'un : il devient "
           "vert. Clique sur §lSave§r§7 : sans ca, la carte reste vide. Mets-la dans le "
           "Processor, alimente-le. La lampe clignote.\n\n"
           "§eGarde la carte programmee dans ton inventaire pour valider.",
           "une Program Card qui utilise ev_timer et do_rs"),
      [retrieval(program_card('ev_timer', 'do_rs'), nbt=True)],
      [item('rftoolscontrol:ram_chip'), XP(6)], [3006])

quest(3008, (120, Y1), C1 + '§l9. Retenir un nombre', item('rftoolscontrol:ram_chip'),
      desc("Compter est facile. Se souvenir de ce qu'on a compte, c'est le debut de "
           "toute science.",
           NOTE + "Sans §lRAM Chip§r§7, le processeur n'a §laucune§r§7 variable. Chaque "
           "puce en ajoute 8. Il faut ensuite §lallouer§r§7 des variables a la carte "
           "(boutons au-dessus des emplacements de cartes).\n\n"
           "§b§lConstruis :§r§7\n"
           "- §lev_timer§r§7 (20 ticks)\n"
           "- -> §leval_countinv§r§7 (le coffre voisin) : compte ses items\n"
           "- -> §ldo_setvar§r§7 (variable 0) : range le resultat\n\n"
           "La liste de gauche du Processor montre les variables. Dans sa console, "
           "§lwatch set 0§r§7 affiche chaque changement de la variable 0.",
           "une Program Card qui utilise eval_countinv et do_setvar"),
      [retrieval(program_card('eval_countinv', 'do_setvar'), nbt=True)],
      [item('rftoolscontrol:ram_chip', 0, 2), XP(6)], [3007])

quest(3009, (180, Y1), C1 + '§l10. Decider', item('minecraft:redstone_lamp'),
      desc("La machine ne choisit pas. Elle compare deux nombres et suit une fleche. "
           "Nous faisons pareil, avec plus d'aplomb.",
           NOTE + "Les opcodes §ltest_...§r§7 ont §ldeux sorties§r§7 : §averte§7 si c'est "
           "vrai, §crouge§7 sinon.\n\n"
           "§b§lConstruis :§r§7\n"
           "- §lev_timer§r§7 -> §leval_countinv§r§7 (le coffre)\n"
           "- -> §ltest_gt_number§r§7 : v1 = fonction §llast_int§r§7, v2 = 31\n"
           "- sortie §averte§7 -> §ldo_rs§r§7 15 ; sortie §crouge§7 -> §ldo_rs§r§7 0\n\n"
           "C'est l'Inventory Checker de la quete 2, mais sur tout le coffre et plus une "
           "seule case.",
           "une Program Card qui utilise eval_countinv, test_gt_number et do_rs"),
      [retrieval(program_card('eval_countinv', 'test_gt_number', 'do_rs'), nbt=True)],
      [item('minecraft:quartz', 0, 16), XP(6)], [3008])

quest(3010, (240, Y1), C1 + '§l11. Deplacer', item('minecraft:chest'),
      desc("Un laboratoire propre est un laboratoire ou les choses vont a leur place "
           "sans qu'on les porte.",
           NOTE + "Le processeur ne transfere jamais directement d'un coffre a un autre : "
           "il §lprend§r§7 dans une case interne, puis §lrepose§r§7. Alloue des cases "
           "internes a la carte, comme pour les variables.\n\n"
           "§b§lConstruis :§r§7 un programme qui vide le coffre A dans le coffre B tant "
           "que A contient plus de 32 items :\n"
           "- §lev_timer§r§7 (20 ticks) : sans evenement, rien ne tourne\n"
           "- -> §leval_countinv§r§7 (A) -> §ltest_gt_number§r§7 (32)\n"
           "- §averte§7 -> §ldo_fetchitems§r§7 (A vers la case interne 0)\n"
           "- -> §ldo_pushitems§r§7 (case 0 vers B)",
           "une Program Card qui utilise do_fetchitems et do_pushitems"),
      [retrieval(program_card('do_fetchitems', 'do_pushitems'), nbt=True)],
      [item('rftoolscontrol:variable_module'), XP(6)], [3009])

quest(3011, (300, Y1), C1 + '§l12. Combien par minute ?', item('rftoolscontrol:variable_module'),
      desc("Le Void Miner produit. Mais combien ? Personne ne gere ce qu'il ne mesure "
           "pas. Moi non plus.",
           NOTE + "Un §ldebit§r§7, c'est une difference divisee par un temps.\n\n"
           "§b§lConstruis :§r§7\n"
           "- §lev_timer§r§7 1200 ticks (une minute)\n"
           "- -> §leval_countinv§r§7 -> §ldo_setvar§r§7 1 (compte actuel)\n"
           "- -> §ldo_subtract_numbers§r§7 : var 1 moins var 0 -> §ldo_setvar§r§7 2\n"
           "- -> §leval_number§r§7 (var 1) -> §ldo_setvar§r§7 0 (le nouveau precedent)\n\n"
           "La variable 2 donne les items arrives en une minute. Affiche-la sur un "
           "Screen RFTools avec le §lVariable Module§r§7.\n\n"
           "§eTu tiens la moitie du Rapport de Production.",
           "une Program Card qui utilise ev_timer, eval_countinv et do_subtract_numbers"),
      [retrieval(program_card('ev_timer', 'eval_countinv', 'do_subtract_numbers'), nbt=True)],
      [item('rftools:screen'), item('rftools:screen_controller'), XP(8)], [3010],
      main=True, size=32)

# ===== Palier 2 : ecrire du code (ComputerCraft 1.80, Lua 5.1) ===============
Y2 = 200
Y2b = 260

quest(3012, (0, Y2), C2 + '§l13. Le premier ordinateur', item('computercraft:computer'),
      desc("Les blocs m'ont appris a penser. Le texte m'a appris a aller vite.",
           NOTE + "Un ordinateur ComputerCraft se programme en §lLua§r§7, en tapant du "
           "texte. Clic droit dessus, puis tape §llua§r§7 : chaque ligne s'execute tout "
           "de suite.\n\n"
           + code('2 + 2', 'print("Bonjour, Voss")', 'x = 7', 'print(x * 6)', 'exit()') +
           "\n\n" + NOTE + "Un ordinateur avance (en or) affiche les couleurs et gere la souris.",
           "1 ordinateur ComputerCraft (normal ou avance)"),
      [retrieval(item('computercraft:computer', 32767))],
      [item('minecraft:stone', 0, 16), item('minecraft:glass_pane', 0, 4), XP(6)], [3011])

quest(3013, (60, Y2), C2 + '§l14. Un programme qui reste', item('minecraft:writable_book'),
      desc("Une idee qu'on n'ecrit pas n'a jamais existe. J'en ai perdu des centaines "
           "avant de comprendre.",
           NOTE + "§ledit bonjour§r§7 ouvre un fichier. Ecris dedans :\n\n"
           + code('print("Bonjour, je suis un programme")') +
           "\n\n" + NOTE + "§lCtrl§r§7, puis §lSave§r§7, puis §lExit§r§7. Tape "
           "§lbonjour§r§7 : ton programme tourne. §lls§r§7 liste les fichiers.\n\n"
           "Donne un nom a l'ordinateur avec §llabel set Atelier§r§7 : casse, il garde "
           "ses programmes.",
           "ecrire, sauver et lancer un programme"),
      [checkbox()],
      [XP(6)], [3012])

quest(3014, (120, Y2), C2 + '§l15. Variables et boucles', item('minecraft:repeater'),
      desc("J'ai compte les jours de mon enfermement sur un mur. Le jour quatre cent "
           "douze, j'ai ecrit une boucle.",
           NOTE + "Une variable garde une valeur. Une boucle §lfor§r§7 la fait varier :\n\n"
           + code('for i = 10, 1, -1 do', '  print(i)', '  sleep(1)', 'end',
                  'print("Decollage")') +
           "\n\n" + NOTE + "§b§lExercice :§r§7 ecris §lcompte§r§7, un programme qui "
           "affiche les nombres pairs de 2 a 20. Indice : le troisieme nombre du "
           "§lfor§r§7 est le pas.",
           "le programme compte fonctionne"),
      [checkbox()],
      [XP(6)], [3013])

quest(3015, (180, Y2), C2 + '§l16. Conditions et redstone', item('minecraft:lever'),
      desc("Un ordinateur qui ne touche pas le monde n'est qu'une calculatrice couteuse.",
           NOTE + "L'API §lredstone§r§7 lit et ecrit du redstone sur les 6 cotes "
           "(§ltop bottom left right front back§r§7) :\n\n"
           + code('if redstone.getInput("left") then',
                  '  redstone.setOutput("right", true)',
                  'else',
                  '  redstone.setOutput("right", false)',
                  'end') +
           "\n\n" + NOTE + "§b§lExercice :§r§7 un levier a gauche, une lampe a droite. "
           "La lampe suit le levier. Puis : elle s'allume seulement si le levier est "
           "§leteint§r§7 (§lnot§r§7).",
           "la lampe obeit a ton programme"),
      [checkbox()],
      [item('minecraft:redstone_lamp', 0, 2), XP(6)], [3014])

quest(3016, (240, Y2), C2 + '§l17. Attendre un evenement', item('minecraft:clock'),
      desc("Un bon programme ne tourne pas en rond. Il dort, et on le reveille.",
           NOTE + "§los.pullEvent()§r§7 attend qu'il se passe quelque chose : un timer, "
           "un changement de redstone, une touche, un message.\n\n"
           + code('local t = os.startTimer(1)',
                  'local evt, param = os.pullEvent()',
                  'if evt == "timer" and param == t then ... end') +
           "\n\n" + NOTE + "Recupere le phare, un programme complet qui clignote et "
           "s'arrete au levier :\n"
           + code('wget ' + URL + 'cc/phare.lua phare') +
           "\n" + NOTE + "Lis-le avec §ledit phare§r§7 avant de le lancer.",
           "faire tourner le phare, et comprendre chaque ligne"),
      [checkbox()],
      [XP(6)], [3015])

quest(3017, (300, Y2), C2 + '§l18. Le moniteur', item('computercraft:peripheral', 2),
      desc("Afficher, c'est deja expliquer. Mes carnets n'ont jamais eu de lecteur. Mes "
           "moniteurs, si.",
           NOTE + "Un §lperipherique§r§7 est un bloc colle a l'ordinateur. On le "
           "recupere avec §lperipheral.wrap(cote)§r§7 :\n\n"
           + code('local m = peripheral.wrap("top")',
                  'm.clear()',
                  'm.setTextScale(2)',
                  'm.setCursorPos(1, 1)',
                  'm.write("Usine Voss : OK")') +
           "\n\n" + NOTE + "Plusieurs moniteurs colles forment un seul grand ecran. "
           "§lmonitor top bonjour§r§7 lance ton programme sur le moniteur.",
           "1 moniteur"),
      [retrieval(item('computercraft:peripheral', 2))],
      [item('computercraft:peripheral', 2, 3), XP(6)], [3016])

quest(3018, (300, Y2b), C2 + '§l19. La tortue', item('computercraft:turtle'),
      desc("J'ai donne des jambes a mes programmes. Ils se sont mis a creuser. Je n'aurais "
           "pas du etre surpris.",
           NOTE + "Une §lturtle§r§7 est un ordinateur qui bouge. Donne-lui une pioche en "
           "diamant a l'etabli : elle creuse.\n\n"
           + code('turtle.forward()', 'turtle.dig()', 'turtle.turnLeft()',
                  'turtle.placeDown()') +
           "\n\n" + NOTE + "Elle a besoin de carburant : charbon dans une case, puis "
           "§lrefuel§r§7. §lturtle.getFuelLevel()§r§7 dit ce qui reste.",
           "1 turtle, normale ou avancee"),
      # Selon l'outil, le cote ou la couleur, CC 1.80 range une turtle sous trois
      # items differents (ItemTurtleLegacy, ItemTurtleNormal, ItemTurtleAdvanced).
      [retrieval(item('computercraft:turtle', 32767)),
       retrieval(item('computercraft:turtle_expanded', 32767)),
       retrieval(item('computercraft:turtle_advanced', 32767))],
      [item('minecraft:coal', 0, 32), XP(6)], [3017], tasklogic='OR')

quest(3019, (240, Y2b), C2 + '§l20. La tortue mineuse', item('minecraft:iron_pickaxe'),
      desc("La mine a toujours ete le premier metier des machines. Elle ne se plaint pas "
           "de l'obscurite.",
           NOTE + "Un vrai programme : la turtle creuse un tunnel de N blocs, gere le "
           "gravier qui retombe et la bedrock, verifie son carburant, puis revient.\n\n"
           + code('wget ' + URL + 'cc/tunnel.lua tunnel', 'tunnel 20') +
           "\n\n" + NOTE + "§b§lExercice :§r§7 modifie-le pour qu'il pose une torche "
           "tous les 8 blocs (l'operateur §l%§r§7). Le sol sous la turtle est plein : "
           "fais demi-tour et pose-la derriere toi avec §lturtle.place()§r§7.",
           "un tunnel de 20 blocs creuse par ta turtle"),
      [checkbox()],
      [item('minecraft:torch', 0, 32), XP(8)], [3018])

quest(3020, (180, Y2b), C2 + '§l21. Le reseau', item('computercraft:peripheral', 1),
      desc("Deux machines qui se parlent font plus que deux machines. C'est la seule "
           "arithmetique qui m'ait jamais console.",
           NOTE + "Un §lmodem sans fil§r§7 et l'API §lrednet§r§7 envoient des messages "
           "entre ordinateurs (64 blocs au sol, bien plus en altitude) :\n\n"
           + code('rednet.open("right")', 'rednet.broadcast("Bonjour")',
                  'local id, msg = rednet.receive()') +
           "\n\n" + NOTE + "Programme complet :\n"
           + code('wget ' + URL + 'cc/radio.lua radio') +
           "\n" + NOTE + "Un ordinateur fait §lradio ecoute§r§7, l'autre §lradio envoie "
           "salut§r§7.",
           "2 modems sans fil"),
      [retrieval(item('computercraft:peripheral', 1, 2))],
      [item('minecraft:ender_pearl', 0, 4), XP(8)], [3019])

quest(3021, (120, Y2b), C2 + '§l22. Imprimer', item('computercraft:peripheral', 3),
      desc("Un ecran s'eteint. Une page, on la garde. J'ai garde toutes les miennes, "
           "meme celles que j'aurais du bruler.",
           NOTE + "L'imprimante a besoin de §lpapier§r§7 et d'§lencre§r§7 (un colorant). "
           "Le titre d'une page se donne dans le programme :\n\n"
           + code('local p = peripheral.wrap("left")',
                  'p.newPage()',
                  'p.setPageTitle("Premiere page")',
                  'p.write("Bonjour, Voss")',
                  'p.endPage()') +
           "\n\n" + NOTE + "Recupere la page dans l'imprimante. §lTitre exact§r§7, "
           "majuscule comprise.",
           'une page imprimee titree "Premiere page"'),
      [retrieval(printout('Premiere page'), nbt=True)],
      [item('minecraft:paper', 0, 32), item('minecraft:dye', 0, 8), XP(8)], [3020],
      main=True, size=32)

# ===== Palier 3 : les composants (OpenComputers 1.8.9, OpenOS, Lua 5.3) =====
Y3 = 340

quest(3022, (0, Y3), C3 + '§l23. Assembler une machine', item('opencomputers:case1'),
      desc("ComputerCraft te donne une machine. OpenComputers te donne les pieces. Je "
           "prefere les pieces : on sait ce qu'on possede.",
           NOTE + "Un ordinateur OpenComputers se monte : §lboitier§r§7, §lprocesseur§r§7, "
           "§lmemoire§r§7, §lcarte graphique§r§7, puis un §lecran§r§7 colle et un "
           "§lclavier§r§7 pose dessus. Il lui faut du §lRF§r§7.\n\n"
           "§8Attention : le pack donne 8 pepites par lingot, la carte graphique consomme "
           "une barrette de RAM, et le clavier demande 37 boutons.",
           "boitier T1, CPU T1, 2 RAM T1, carte graphique T1, ecran T1, clavier"),
      [retrieval(item('opencomputers:case1'), item('opencomputers:component', 0),
                 item('opencomputers:component', 6, 2), item('opencomputers:card', 1),
                 item('opencomputers:screen1'), item('opencomputers:keyboard'))],
      [item('opencomputers:material', 7, 8), XP(8)], [3021])

quest(3023, (60, Y3), C3 + '§l24. Un systeme', item('opencomputers:storage', 1),
      desc("Une machine sans systeme est un presse-papier. Tres cher.",
           NOTE + "Au demarrage, l'ordinateur lit son §lEEPROM§r§7 (le BIOS), puis "
           "cherche un disque. Il te faut :\n"
           "- l'EEPROM §lLua BIOS§r§7 (EEPROM + manuel) ;\n"
           "- la disquette §lOpenOS§r§7 (disquette + manuel) dans un §llecteur de "
           "disquettes§r§7 colle au boitier ;\n"
           "- un §ldisque dur§r§7 dans le boitier.\n\n"
           "Allume, tape §linstall§r§7, choisis le disque dur, laisse redemarrer, retire "
           "la disquette. Ensuite : §lls§r§7, §ledit§r§7, §llua§r§7, §lman§r§7.",
           "EEPROM Lua BIOS, disquette OpenOS, lecteur de disquettes, disque dur T1"),
      [retrieval(eeprom('EEPROM (Lua BIOS)'),
                 item('opencomputers:storage', 1, 1,
                      {'oc:lootFactory:8': 'opencomputers:openos'}),
                 item('opencomputers:diskdrive'), item('opencomputers:storage', 2),
                 nbt=True)],
      [item('minecraft:book', 0, 2), XP(8)], [3022])

quest(3024, (120, Y3), C3 + '§l25. Tout est composant', item('opencomputers:card', 4),
      desc("Chaque piece que j'ajoute a une adresse. La machine les connait toutes par "
           "leur nom. Je n'ai jamais su le faire avec mes assistants.",
           NOTE + "Dans OpenComputers, chaque piece est un §lcomposant§r§7. §lcomponents§r§7 "
           "les liste. En Lua :\n\n"
           + code('local component = require("component")',
                  'local sides = require("sides")',
                  'local rs = component.redstone',
                  'rs.setOutput(sides.back, 15)') +
           "\n\n" + NOTE + "Pour une carte redstone dans le boitier, les cotes sont "
           "§lrelatifs au boitier§r§7 : §lback§r§7 = derriere, §lfront§r§7 = devant.",
           "1 carte redstone T1, et une lampe allumee par code"),
      [retrieval(item('opencomputers:card', 4)), checkbox()],
      [XP(8)], [3023])

quest(3025, (180, Y3), C3 + '§l26. Le transposer', item('opencomputers:transposer'),
      desc("Toute usine finit par etre un probleme de logistique. La mienne aussi.",
           NOTE + "Le §ltransposer§r§7 lit les coffres qu'il touche et deplace les items "
           "entre eux, sur ordre de l'ordinateur. Ses cotes sont §labsolus§r§7 "
           "(§lup down north south east west§r§7), et les cases commencent a §l1§r§7.\n\n"
           + code('local tp = component.transposer',
                  'local pile = tp.getStackInSlot(sides.up, 1)',
                  'tp.transferItem(sides.up, sides.down, 64, 1)') +
           "\n\n" + NOTE + "§b§lExercice :§r§7 un trieur, les minerais d'un cote, le "
           "reste de l'autre. Avec une carte internet :\n"
           + code('wget ' + URL + 'oc/trieur.lua trieur.lua') +
           "\n" + NOTE + "La carte internet est de niveau 2 : elle ne rentre pas dans un "
           "boitier T1, il faut un §lboitier T2§r§7. Sinon, recopie le programme avec "
           "§ledit§r§7.",
           "1 transposer"),
      [retrieval(item('opencomputers:transposer'))],
      [item('opencomputers:card', 8), item('opencomputers:case2'), XP(8)], [3024])

quest(3026, (240, Y3), C3 + '§l27. Le robot', item('opencomputers:robot'),
      desc("J'ai assemble mon premier robot un mardi. Le mercredi, il avait creuse "
           "jusqu'a la bedrock. Je lui avais oublie une condition d'arret.",
           NOTE + "Un robot se construit dans l'§lassembleur§r§7 (alimente) : un boitier, "
           "un CPU, de la RAM, l'§lEEPROM Lua BIOS§r§7, un disque dur ou OpenOS est "
           "installe, un ecran, un clavier, une carte graphique et des "
           "§lameliorations§r§7. Sans §lInventory Upgrade§r§7, il n'a aucune case.\n\n"
           + code('local robot = require("robot")',
                  'robot.forward()', 'robot.swing()', 'robot.select(1)', 'robot.place()') +
           "\n\n" + NOTE + "L'outil se met dans son emplacement d'outil : il n'existe "
           "pas d'amelioration outil.",
           "1 robot"),
      [retrieval(item('opencomputers:robot'))],
      [item('opencomputers:upgrade', 17), XP(10)], [3025])

quest(3027, (300, Y3), C3 + '§l28. Graver une EEPROM', item('opencomputers:storage', 0),
      desc("Les programmes que je voulais garder, je les ai graves. Le papier brule. Le "
           "silicium, moins.",
           NOTE + "Une §lEEPROM§r§7 garde un programme de 4 Ko. Les drones et les "
           "microcontroleurs n'ont que ca : pas d'OpenOS.\n\n"
           + code('flash monprog.lua "Premier Flash"') +
           "\n\n" + NOTE + "§lflash§r§7 te demande d'inserer l'EEPROM a graver : retire "
           "le BIOS, mets une EEPROM vierge, confirme, puis remets le BIOS. OpenOS "
           "continue de tourner pendant l'echange.\n\n"
           "§cN'utilise pas -q§r§7 : il graverait le BIOS en place sans demander.",
           'une EEPROM etiquetee "Premier Flash"'),
      [retrieval(eeprom('Premier Flash'), nbt=True)],
      [item('opencomputers:storage', 0, 2), XP(10)], [3026], main=True, size=32)

# ===== Finale ===============================================================
YF = 430

quest(GATE, (120, YF), CF + '§l★ Le Rapport de Production', item('computercraft:printout'),
      desc("Avant de vous laisser voir l'etage suivant, je veux un chiffre. Pas une "
           "impression, pas un espoir : combien de minerais votre Void Miner tire du "
           "neant, par minute. Ecrit par une machine, pas par vous.",
           "§6§lCette quete ouvre l'Age 3.§r§7 Le reste de la ligne Coding est un "
           "tutoriel : si tu sais deja programmer, fais seulement celle-ci.\n\n"
           "§b§lMontage ComputerCraft :§r§7 une turtle devant le coffre de sortie du Void "
           "Miner T1, un coffre sous elle, une imprimante a sa gauche.\n"
           + code('wget ' + URL + 'cc/rapport.lua rapport', 'rapport 5') +
           "\n\n§b§lMontage OpenComputers :§r§7 un transposer entre le coffre du Void Miner "
           "(au-dessus) et un coffre de stockage (en dessous). §lwget§r§7 demande une "
           "carte internet, donc un boitier T2 ; sinon recopie avec §ledit§r§7.\n"
           + code('wget ' + URL + 'oc/rapport.lua rapport.lua', 'rapport 5 up down',
                  'flash /home/rapport.txt "Rapport de Production"') +
           "\n\n" + NOTE + "Ou ecris le tien : tu as tout appris. Le debit theorique "
           "d'un Void Miner T1 sans modificateur est de 3 minerais par minute.\n\n"
           "§8La quete ne verifie que le titre. Le chiffre, lui, est sur l'honneur : "
           "Voss le relira.",
           'une page imprimee OU une EEPROM, titree "Rapport de Production"'),
      [retrieval(printout('Rapport de Production'), nbt=True),
       retrieval(eeprom('Rapport de Production'), nbt=True)],
      [item('environmentaltech:modifier_speed', 0, 2), XP(16)],
      [VOID_MINER_T1], main=True, size=40, tasklogic='OR')

quest(3029, (240, YF), CF + "§l★ L'Architecte numerique", item('opencomputers:screen2'),
      desc("NEXUS.lua commence par trois lignes. Vous savez maintenant ecrire la "
           "quatrieme.",
           NOTE + "Construis un §ltableau de bord§r§7 qui lit ton usine en direct, sur un "
           "ecran T2 en couleurs (la carte graphique T2 demande un boitier T2). Colle un "
           "§ladaptateur§r§7 au bloc a lire :\n"
           "- §lAE2§r§7 : §lcomponent.me_controller.getItemsInNetwork()§r§7\n"
           "- §lExtreme Reactors§r§7 (Computer Port) : §lcomponent.br_reactor§r§7, "
           "§lgetEnergyStored()§r§7, §lsetAllControlRodLevels(n)§r§7\n"
           "- tout bloc a energie RF : §lcomponent.energy_device§r§7\n\n"
           "§b§lPour aller plus loin :§r§7 drones (EEPROM seule), le programmeur de "
           "drones de PneumaticCraft, la regulation du reacteur de Draconic Evolution.",
           "carte graphique T2, ecran T2, et un tableau de bord qui tourne"),
      [retrieval(item('opencomputers:card', 2), item('opencomputers:screen2')), checkbox()],
      [item('opencomputers:component', 1), XP(16)], [3027, GATE], main=True, size=32)


# ------------------------------------------------------------ assemblage BQ

def build_quest(q):
    props = collections.OrderedDict([
        ('issilent:1', 0),
        ('snd_complete:8', 'minecraft:entity.player.levelup'),
        ('lockedprogress:1', 0),
        ('tasklogic:8', q['tasklogic']),
        ('repeattime:3', -1),
        ('visibility:8', 'ALWAYS'),
        ('simultaneous:1', 0),
        ('globalshare:1', 0),
        ('questlogic:8', 'AND'),
        ('snd_update:8', 'minecraft:entity.player.levelup'),
        ('autoclaim:1', 0),
        ('ismain:1', 1 if q['main'] else 0),
        ('repeat_relative:1', 1),
        ('icon:10', q['icon']),
        ('name:8', q['name']),
        ('desc:8', q['desc']),
    ])
    tasks = collections.OrderedDict()
    for i, t in enumerate(q['tasks']):
        t = dict(t)
        if 'requiredItems:9' in t:
            t['requiredItems:9'] = collections.OrderedDict(
                ('%d:10' % j, it) for j, it in enumerate(t['requiredItems:9']))
        tasks['%d:10' % i] = collections.OrderedDict([('index:3', i)] + list(t.items()))
    rewards = collections.OrderedDict()
    if q['rewards']:
        rewards['0:10'] = collections.OrderedDict([
            ('rewardID:8', 'bq_standard:item'), ('index:3', 0),
            ('rewards:9', collections.OrderedDict(
                ('%d:10' % j, it) for j, it in enumerate(q['rewards'])))])
    return collections.OrderedDict([
        ('questID:3', q['id']),
        ('preRequisites:11', q['prereqs']),
        ('properties:10', {'betterquesting:10': props}),
        ('tasks:9', tasks),
        ('rewards:9', rewards),
    ])


LINE_DESC = ("§7§oPage parallele : apprendre a programmer, sans rien savoir au depart.§r\n\n"
             "§f§lPalier 0§r§7  la logique, sans code (RFTools)\n"
             "§b§lPalier 1§r§7  programmer avec des blocs (RFTools Control)\n"
             "§a§lPalier 2§r§7  ecrire du code (ComputerCraft, Lua)\n"
             "§e§lPalier 3§r§7  les composants (OpenComputers, OpenOS)\n\n"
             "§7A ton rythme. §6Une seule quete est obligatoire :§r§7 §6Le Rapport de "
             "Production§r§7, qui ouvre l'Age 3. Si tu sais deja coder, va directement "
             "la faire.")


def reindex(entries):
    return collections.OrderedDict(('%d:10' % i, e) for i, e in enumerate(entries))


def main():
    with open(DQ, encoding='utf-8') as f:
        raw = f.read()
    data = json.loads(raw, object_pairs_hook=collections.OrderedDict)

    new_ids = {q['id'] for q in Q}
    assert len(new_ids) == len(Q) == 30, 'il faut 30 quetes aux ids distincts'
    assert new_ids == set(range(3000, 3030))
    drop = new_ids | set(OLD_CODING)

    # 1. Base de quetes : on retire l'ancienne ligne et nos ids, on ajoute les neuves.
    db = [v for v in data['questDatabase:9'].values() if v['questID:3'] not in drop]
    db += [build_quest(q) for q in Q]
    data['questDatabase:9'] = reindex(db)
    byid = {v['questID:3']: v for v in db}

    # 2. "VERS L'AGE 3" exige le Rapport de Production a la place de la Premiere Turtle.
    pre = [p for p in byid[VERS_AGE3]['preRequisites:11'] if p not in OLD_CODING]
    if GATE not in pre:
        pre.append(GATE)
    byid[VERS_AGE3]['preRequisites:11'] = pre

    # 3. Ligne Coding : entrees et proprietes.
    lines = list(data['questLines:9'].values())
    coding = [l for l in lines if l.get('lineID:3') == CODING_LINE_ID]
    assert len(coding) == 1, 'ligne Coding introuvable (lineID 3)'
    coding = coding[0]
    p = coding['properties:10']['betterquesting:10']
    p['name:8'] = '§l§aCoding — du levier au programme'
    p['desc:8'] = LINE_DESC
    p['icon:10'] = item('computercraft:computer')
    coding['quests:9'] = reindex([
        collections.OrderedDict([('id:3', q['id']), ('x:3', q['pos'][0]), ('y:3', q['pos'][1]),
                                 ('sizeX:3', q['size']), ('sizeY:3', q['size'])])
        for q in Q])

    # 4. La quete verrou apparait aussi dans l'onglet de l'Age 2, pres de VERS L'AGE 3.
    age2 = [l for l in lines if AGE2_LINE_NAME in l['properties:10']['betterquesting:10']['name:8']]
    assert len(age2) == 1
    entries = [e for e in age2[0]['quests:9'].values() if e['id:3'] not in drop]
    entries.append(collections.OrderedDict([
        ('id:3', GATE), ('x:3', GATE_POS_AGE2[0]), ('y:3', GATE_POS_AGE2[1]),
        ('sizeX:3', 32), ('sizeY:3', 32)]))
    age2[0]['quests:9'] = reindex(entries)

    # 5. Aucune autre ligne ne doit encore citer les anciennes quetes.
    for l in lines:
        for e in l['quests:9'].values():
            assert e['id:3'] in byid, 'ligne %s : quete %s absente' % (
                l['properties:10']['betterquesting:10']['name:8'], e['id:3'])

    out = json.dumps(data, ensure_ascii=False, indent=2)
    if raw.endswith('\n'):
        out += '\n'
    if out != raw:
        with open(DQ, 'w', encoding='utf-8') as f:
            f.write(out)
        print('DefaultQuests.json ecrit : %d quetes, ligne Coding = 30' % len(db))
    else:
        print('DefaultQuests.json deja a jour')
    return 0


if __name__ == '__main__':
    sys.exit(main())
