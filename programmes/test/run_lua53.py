"""Execute un programme OpenOS sous le vrai Lua 5.3 (lupa) avec les bouchons OC."""
import sys
from lupa import lua53
lua = lua53.LuaRuntime()
lua.execute(open('test/mock_oc.lua', encoding='utf-8').read())
prog = open(sys.argv[1], encoding='utf-8').read()
fn = lua.execute('return function(src, ...) return load(src, "=prog")(...) end')
fn(prog, *sys.argv[2:])
T = lua.globals().TEST
txt = T.fichiers['/home/rapport.txt']
assert txt, 'rapport.txt non ecrit'
print('--- /home/rapport.txt ---'); print(txt)
import re
total = int(re.search(r'Total : (\d+)', txt).group(1))
assert 35 <= total <= 38, total
assert 'coal_ore' not in txt, 'le charbon initial ne doit pas etre compte'
assert len(txt.encode()) <= 4096
print('OK oc/rapport.lua : total', total, ', charbon initial exclu, rapport < 4 Ko')
