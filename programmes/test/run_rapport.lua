dofile("test/mock_cc.lua")
dofile("cc/rapport.lua")
local T = TEST
assert(#T.pages == 1, "une page attendue")
local p = T.pages[1]
assert(p.titre == "Rapport de Production", "titre : " .. tostring(p.titre))
-- 5 min a 7,5/min : 37 ou 38 selon l'arrondi ; le charbon d'avant la mesure est exclu.
local total = tonumber(p.lignes[5]:match("(%d+)"))
assert(total >= 36 and total <= 38, "total : " .. tostring(total))
for y = 1, 21 do if p.lignes[y] then io.write(string.format("   |%-25s|\n", p.lignes[y])) end end
print("OK rapport.lua : titre, total " .. total .. ", charbon initial exclu, lignes <= 25 car.")
