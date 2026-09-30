# -*- coding: utf-8 -*-
"""Simulation de la mise en page d'une page de texte Patchouli 1.12.2.

Source unique partagee par split_patchouli_pages.py (decoupage) et
check/check_patchouli_overflow.py (controle). Portage ligne a ligne de :

  client/book/gui/GuiBook.java          PAGE_WIDTH 116, PAGE_HEIGHT 156, ligne 9
  client/book/page/PageText.java        texte a y = 22 (page 0), 12 (titre), -4
  client/book/page/PageMultiblock.java  texte a y = 115
  client/book/text/BookTextParser.java  commandes $(...), macros par defaut
  client/book/text/TextLayouter.java    retour a la ligne
  common/book/Book.java                 DEFAULT_MACROS

branche 1.12.2-final de github.com/VazkiiMods/Patchouli.

Patchouli ne pagine pas et ne tronque pas : il dessine, et ce qui depasse
la hauteur de la page sort du parchemin.

Largeurs : la police que le livre utilise, via book_font(). Sans
"use_blocky_font" dans book.json, Patchouli ecrit en police UNICODE
(font_widths.json) ; avec, en police normale (font_widths_ascii.json, repli
unicode hors de la table de FontRenderer). Voir extract_font_widths.py.
Si un fichier manque, on s'arrete : mesurer avec la mauvaise police
sous-estimerait certaines lignes et surestimerait d'autres.

Coupure de ligne : TextLayouter coupe avec java.text.BreakIterator. Ses
regles (tirets, ponctuation, chiffres) ne se reimitent pas fidelement en
Python, donc on interroge le vrai : BreakPoints.java, compile au premier
appel et lance une fois par execution. Il faut un JDK dans le PATH (ou
NEXUS_JAVA_HOME). Le jeu tourne sous Java 8 : sous un autre Java les
regles peuvent differer, un avertissement le signale.

Le reste est un portage exact. Ecarts assumes, sans effet sur le Carnet :
DEFAULT_MACROS est applique dans l'ordre du dict (Java : ordre du HashMap).
"""
import atexit
import base64
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
BOOK_JSON = os.path.join(os.path.dirname(os.path.dirname(HERE)), 'mod-source', 'src', 'main',
                         'resources', 'assets', 'nexusabsolu', 'patchouli_books',
                         'voss_codex', 'book.json')

PAGE_WIDTH = 116
PAGE_HEIGHT = 156
LINE = 9

TOP_FIRST = 22        # page d'indice 0 d'une entree : nom de l'entree + separateur
TOP_TITLED = 12       # page de texte avec titre
TOP_UNTITLED = -4     # page de texte sans titre
TOP_MULTIBLOCK = 115  # texte sous le rendu 3D

DEFAULT_MACROS = {'$(list': '$(li', '/$': '$()', '<br>': '$(br)',
                  '$(item)': '$(#b0b)', '$(thing)': '$(#490)'}


class LayoutError(Exception):
    pass


# ---------------------------------------------------------------- police

class Font(object):
    """FontRenderer 1.12.2 en mode unicode."""

    def __init__(self, path=None):
        path = path or os.path.join(HERE, 'font_widths.json')
        if not os.path.exists(path):
            raise LayoutError(
                "%s absent. Lancer une fois :\n"
                "  python3 scripts/gen/extract_font_widths.py <minecraft-1.12.2.jar>"
                % os.path.relpath(path))
        with open(path, encoding='utf-8') as f:
            data = json.load(f)
        self.widths = data['widths']
        self.sha1 = data.get('sha1', '?')

    def char_width(self, c):
        o = ord(c)
        if o == 160:
            return 4
        if o == 167:                 # U+00A7, debut de code de format
            return -1
        if c == ' ':
            return 4
        if o >= len(self.widths):
            raise LayoutError('pas de largeur connue pour U+%04X (%r)' % (o, c))
        return int(self.widths[o])

    def string_width(self, text):
        """FontRenderer.getStringWidth : codes de format, +1 par glyphe en gras."""
        width = 0
        bold = False
        j = 0
        n = len(text)
        while j < n:
            c = text[j]
            k = self.char_width(c)
            if k < 0 and j < n - 1:
                j += 1
                c = text[j]
                if c in 'lL':
                    bold = True
                elif c in 'rR':
                    bold = False
                k = 0
            width += k
            if bold and k > 0:
                width += 1
            j += 1
        return width


class BlockyFont(Font):
    """FontRenderer 1.12.2 hors mode unicode : ascii.png pour les caracteres
    de sa table CHARSET, police unicode pour les autres (getCharWidth)."""

    def __init__(self, path=None, ascii_path=None):
        Font.__init__(self, path)
        ascii_path = ascii_path or os.path.join(HERE, 'font_widths_ascii.json')
        if not os.path.exists(ascii_path):
            raise LayoutError(
                "%s absent. Lancer une fois :\n"
                "  python3 scripts/gen/extract_font_widths.py --ascii <minecraft-1.12.2.jar>"
                % os.path.relpath(ascii_path))
        with open(ascii_path, encoding='utf-8') as f:
            data = json.load(f)
        self.ascii = data['widths']
        self.sha1 = 'ascii ' + data.get('sha1', '?')

    def char_width(self, c):
        if c != '\u00a7' and c in self.ascii:
            return self.ascii[c]
        return Font.char_width(self, c)


def book_font(book_json=None):
    """La police du texte des pages, selon "use_blocky_font" de book.json."""
    with open(book_json or BOOK_JSON, encoding='utf-8') as f:
        blocky = json.load(f).get('use_blocky_font', False)
    return BlockyFont() if blocky else Font()


# ---------------------------------------------------------------- analyse

class Span(object):
    __slots__ = ('text', 'codes', 'line_breaks', 'spacing_left',
                 'spacing_right', 'bold')

    def __init__(self, state, text):
        self.text = text
        self.codes = state.codes
        self.line_breaks = state.line_breaks
        self.spacing_left = state.spacing_left
        self.spacing_right = state.spacing_right
        self.bold = '§l' in state.codes
        state.line_breaks = 0
        state.spacing_left = 0
        state.spacing_right = 0


class State(object):
    def __init__(self):
        self.codes = ''
        self.line_breaks = 0
        self.spacing_left = 0
        self.spacing_right = 0
        self.is_external_link = False
        self.ending_external = False


_COMMANDS_BREAK = {'br': 1, 'br2': 2, '2br': 2, 'p': 2}
_COMMANDS_CODES = {'k': '§k', 'obf': '§k', 'l': '§l', 'bold': '§l',
                   'm': '§m', 'strike': '§m', 'o': '§o',
                   'italic': '§o', 'italics': '§o'}
_COMMANDS_NOOP = ('/t', 'nocolor')
_COMMANDS_RESET = ('', 'reset', 'clear')


def _process_command(font, state, cmd):
    state.ending_external = False
    result = ''
    if len(cmd) == 1 and cmd in '0123456789abcdef':
        return ''
    if cmd.startswith('#') and len(cmd) in (4, 7):
        return ''
    if re.fullmatch(r'li[0-9]?', cmd):
        c = cmd[2] if len(cmd) > 2 else '1'
        dist = int(c)
        state.line_breaks = 1
        state.spacing_left = dist * 4
        state.spacing_right = font.string_width(' ')
        return '§0' + ('◦' if dist % 2 == 0 else '•')
    colon = cmd.find(':')
    if colon > 0:
        function, parameter = cmd[:colon], cmd[colon + 1:]
        if function == 'l':
            state.is_external_link = bool(re.match(r'^https?:', parameter))
        elif function in ('tooltip', 't'):
            pass
        elif function == 'k':
            raise LayoutError('$(k:...) affiche une touche : largeur inconnue hors jeu')
        else:
            result = '[MISSING FUNCTION: %s]' % function
    elif cmd == '/l':
        state.ending_external = state.is_external_link
        state.is_external_link = False
    elif cmd in _COMMANDS_BREAK:
        state.line_breaks = _COMMANDS_BREAK[cmd]
    elif cmd in _COMMANDS_CODES:
        state.codes = _COMMANDS_CODES[cmd]
    elif cmd in _COMMANDS_RESET:
        state.ending_external = state.is_external_link
        state.codes = ''
        state.is_external_link = False
    elif cmd == 'playername':
        raise LayoutError('$(playername) : largeur inconnue hors jeu')
    # _COMMANDS_NOOP et commandes inconnues : rien
    if state.ending_external:
        result += '§7↪'
    return result


def parse(font, text):
    """BookTextParser.parse + processCommands : texte -> liste de Span."""
    if text is None:
        text = '[ERROR]'
    for key, value in DEFAULT_MACROS.items():
        text = text.replace(key, value)
    state = State()
    spans = []
    start = 0
    i = 0
    n = len(text)
    while i < n:
        if text[i] == '$' and i + 1 < n and text[i + 1] == '(':
            if i > start:
                spans.append(Span(state, text[start:i]))
            start = i
            while i < n and text[i] != ')':
                i += 1
            if i == n:
                codes, state.codes = state.codes, ''     # Span.error : codes ""
                spans.append(Span(state, '[ERROR: UNFINISHED COMMAND]'))
                state.codes = codes
                break
            processed = _process_command(font, state, text[start + 2:i])
            if processed:
                spans.append(Span(state, processed))
            start = i + 1
        i += 1
    spans.append(Span(state, text[start:]))
    return spans


# ---------------------------------------------------------------- coupure

def _java_whitespace(c):
    """Character.isWhitespace : blancs hors espaces insecables, plus 0x1C-0x1F."""
    return c in ' \t\n\x0b\x0c\r\x1c\x1d\x1e\x1f' or (
        c.isspace() and c not in '   \x85')


class _JavaBreaks(object):
    """Pont vers BreakPoints.java, un seul processus par execution."""

    def __init__(self):
        self.proc = None
        self.cache = {}
        self.java_version = None

    def _tool(self, name):
        home = os.environ.get('NEXUS_JAVA_HOME') or os.environ.get('JAVA_HOME')
        if home:
            for cand in (name, name + '.exe'):
                p = os.path.join(home, 'bin', cand)
                if os.path.exists(p):
                    return p
        p = shutil.which(name)
        if not p:
            raise LayoutError(
                "%s introuvable. La coupure de ligne de Patchouli vient de "
                "java.text.BreakIterator : installer un JDK (8 de preference, celui "
                "du jeu) ou pointer NEXUS_JAVA_HOME dessus." % name)
        return p

    def _start(self):
        java = self._tool('java')
        javac = self._tool('javac')
        src = os.path.join(HERE, 'BreakPoints.java')
        with open(src, 'rb') as f:
            digest = hashlib.sha1(f.read() + javac.encode('utf-8')).hexdigest()[:12]
        out = os.path.join(tempfile.gettempdir(), 'nexus_breakpoints_' + digest)
        if not os.path.exists(os.path.join(out, 'BreakPoints.class')):
            os.makedirs(out, exist_ok=True)
            r = subprocess.run([javac, '-encoding', 'ASCII', '-source', '8', '-target', '8',
                                '-d', out, src], capture_output=True, text=True)
            if r.returncode != 0:
                r = subprocess.run([javac, '-encoding', 'ASCII', '-d', out, src],
                                   capture_output=True, text=True)
            if r.returncode != 0:
                raise LayoutError('compilation de BreakPoints.java :\n' + r.stderr)
        v = subprocess.run([java, '-version'], capture_output=True, text=True).stderr
        m = re.search(r'version "([^"]+)"', v)
        self.java_version = m.group(1) if m else '?'
        if not self.java_version.startswith('1.8'):
            sys.stderr.write('  attention : coupures calculees avec Java %s, Minecraft 1.12.2 '
                             'tourne sous Java 8 (NEXUS_JAVA_HOME pour choisir)\n'
                             % self.java_version)
        self.proc = subprocess.Popen([java, '-cp', out, 'BreakPoints'],
                                     stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                     stderr=subprocess.DEVNULL)
        atexit.register(self.close)

    def close(self):
        if self.proc and self.proc.poll() is None:
            self.proc.stdin.close()
            self.proc.wait()

    def points(self, text):
        if text in self.cache:
            return self.cache[text]
        if any(ord(c) > 0xFFFF for c in text):
            raise LayoutError('caractere hors du plan de base : indices Java et Python divergent')
        if self.proc is None:
            self._start()
        self.proc.stdin.write(base64.b64encode(text.encode('utf-8')) + b'\n')
        self.proc.stdin.flush()
        line = self.proc.stdout.readline()
        if not line:
            raise LayoutError('BreakPoints.java ne repond plus')
        pts = [int(x) for x in line.decode('utf-8').split()]
        self.cache[text] = pts
        return pts


_BREAKER = _JavaBreaks()


def java_version():
    """Version du Java qui a fourni les coupures (None si pas encore lance)."""
    return _BREAKER.java_version


class _Breaks(object):
    def __init__(self, text):
        self.points = _BREAKER.points(text)

    def preceding(self, offset):
        """BreakIterator.preceding : derniere coupure strictement avant offset."""
        best = 0
        for p in self.points:
            if p < offset:
                best = p
            else:
                break
        return best


# ---------------------------------------------------------------- mise en page

class _Tail(object):
    __slots__ = ('span', 'start', 'width', 'length')

    def __init__(self, font, span, start):
        self.span = span
        self.start = start
        self.width = (font.string_width(span.codes + span.text[start:])
                      + span.spacing_left + span.spacing_right)
        self.length = len(span.text) - start


class Layout(object):
    """TextLayouter. words : liste (y, x, texte, gras)."""

    def __init__(self, font, top, width=PAGE_WIDTH):
        self.font = font
        self.width = width
        self.y = top
        self.top = top
        self.words = []
        self.pending = []
        self.line_start = 0
        self.width_so_far = 0
        self._guard = 0

    def run(self, spans):
        paragraph = []
        for span in spans:
            if span.line_breaks > 0:
                self._paragraph(paragraph)
                self.width_so_far = 0
                self.y += span.line_breaks * LINE
                paragraph = []
            paragraph.append(span)
        if paragraph:
            self._paragraph(paragraph)
        return self

    def _paragraph(self, paragraph):
        breaks = _Breaks(''.join(s.text for s in paragraph))
        self.line_start = 0
        for span in paragraph:
            self._append(breaks, span)
        self._flush()

    def _append(self, breaks, span):
        last = _Tail(self.font, span, 0)
        self.width_so_far += last.width
        self.pending.append(last)
        while self.width_so_far > self.width:
            self._guard += 1
            if self._guard > 10000:
                raise LayoutError('boucle de mise en page (le jeu gelerait aussi)')
            self._break_line(breaks)
            self.width_so_far = sum(t.width for t in self.pending)

    def _break_line(self, breaks):
        width = 0
        offset = 0
        for t in self.pending:
            width += t.width
            offset += t.length
        last = self.pending[-1]
        width -= last.width
        offset -= last.length
        chars = last.span.text
        for i in range(last.start, len(chars)):
            width += self.font.char_width(chars[i])
            if last.span.bold:
                width += 1
            if width > self.width:
                overflow = self.line_start + offset + i - last.start
                brk = overflow + 1
                if not _java_whitespace(chars[i]):
                    brk = breaks.preceding(brk)
                if brk <= self.line_start:
                    brk = overflow - 1
                self._break_at(brk)
                return
        self._flush()
        self.y += LINE

    def _word(self, t, x, length):
        self.words.append((self.y, x + t.span.spacing_left,
                           t.span.text[t.start:t.start + length], t.span.bold))

    def _flush(self):
        x = 0
        for t in self.pending:
            self._word(t, x, t.length)
            x += t.width
        self.pending = []

    def _break_at(self, text_offset):
        offset = self.line_start
        x = 0
        index = 0
        while index < len(self.pending):
            t = self.pending[index]
            if offset + t.length < text_offset:
                self._word(t, x, t.length)
                offset += t.length
                x += t.width
            else:
                self._word(t, x, text_offset - offset)
                self.pending[index] = _Tail(self.font, t.span, t.start + text_offset - offset)
                break
            index += 1
        del self.pending[:index]
        self.line_start = text_offset
        self.y += LINE

    # -- lecture du resultat

    def visible(self):
        """Mots qui dessinent quelque chose (hors blancs et codes seuls)."""
        return [w for w in self.words
                if re.sub('§.', '', w[2]).strip()]

    def bottom(self):
        """y du haut de la derniere ligne dessinee, ou None si rien."""
        v = self.visible()
        return max(w[0] for w in v) if v else None

    def lines_used(self):
        b = self.bottom()
        return 0 if b is None else (b - self.top) // LINE + 1

    def lines(self):
        """Texte de chaque ligne occupee, de haut en bas (pour le diagnostic)."""
        rows = {}
        for y, x, text, _ in self.words:
            rows.setdefault(y, []).append((x, re.sub('§.', '', text)))
        return [(y, ''.join(t for _, t in sorted(rows[y]))) for y in sorted(rows)]


# ---------------------------------------------------------------- API

def budget(top):
    """Nombre de lignes qui tiennent entierement sur la page."""
    return (PAGE_HEIGHT - LINE - top) // LINE + 1


def page_top(page, index):
    """y de depart du texte d'une page, ou None si le type n'a pas de texte suivi."""
    kind = page.get('type')
    if kind == 'text':
        if index == 0:
            return TOP_FIRST
        return TOP_TITLED if page.get('title') else TOP_UNTITLED
    if kind == 'multiblock':
        return TOP_MULTIBLOCK
    return None


def layout(font, text, top):
    return Layout(font, top).run(parse(font, text))


def overflow(font, text, top):
    """Nombre de lignes qui sortent de la page (0 si tout tient)."""
    return max(0, layout(font, text, top).lines_used() - budget(top))
