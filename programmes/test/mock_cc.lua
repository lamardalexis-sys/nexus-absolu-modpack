-- Bouchons ComputerCraft 1.80 pour tester programmes/cc/*.lua sous LuaJ 2.0.3.
-- Simule : un coffre devant la turtle alimente par un Void Miner, un coffre
-- dessous, une imprimante a gauche, le temps qui passe avec sleep().

local T = { temps = 0, sortie = {}, pages = {}, dessous = 0, ecran = {} }
_G.TEST = T

-- Production simulee : 7,5 minerais / minute repartis sur 3 minerais.
local production = {
  { name = "minecraft:iron_ore", damage = 0, parMin = 4.5 },
  { name = "thermalfoundation:ore", damage = 2, parMin = 2.0 },
  { name = "minecraft:gold_ore", damage = 0, parMin = 1.0 },
}
local cumul = {}
local function produire(dt)
  for i, p in ipairs(production) do
    cumul[i] = (cumul[i] or 0) + p.parMin * dt / 60
    local n = math.floor(cumul[i])
    if n > 0 then
      cumul[i] = cumul[i] - n
      table.insert(T.sortie, { name = p.name, damage = p.damage, count = n })
    end
  end
end
-- 12 items attendaient deja dans le coffre avant la mesure.
table.insert(T.sortie, { name = "minecraft:coal_ore", damage = 0, count = 12 })

local slot1 = nil
_G.turtle = {
  select = function(n) assert(n == 1) return true end,
  suck = function()
    if slot1 then return false end
    local pile = table.remove(T.sortie, 1)
    if not pile then return false end
    slot1 = pile
    return true
  end,
  getItemDetail = function(n)
    assert(n == 1)
    if not slot1 then return nil end
    return { name = slot1.name, damage = slot1.damage, count = slot1.count }
  end,
  dropDown = function()
    if not slot1 then return false end
    T.dessous = T.dessous + slot1.count
    slot1 = nil
    return true
  end,
}

_G.sleep = function(s) T.temps = T.temps + s; produire(s) end

local curX, curY = 1, 1
_G.term = {
  setCursorPos = function(x, y) curX, curY = x, y end,
  getCursorPos = function() return curX, curY end,
  clearLine = function() end,
  write = function(s) T.ecran[#T.ecran + 1] = s end,
}
local vraiPrint = print
_G.print = function(...) T.ecran[#T.ecran + 1] = table.concat({ ... }, " "); vraiPrint(...) end

local page
local imprimante = {
  newPage = function() page = { titre = nil, lignes = {} }; return true end,
  setPageTitle = function(t) page.titre = t end,
  getPageSize = function() return 25, 21 end,
  setCursorPos = function(x, y) page.y = y end,
  write = function(s)
    assert(#s <= 25, "ligne trop longue pour la page : " .. s)
    page.lignes[page.y] = s
  end,
  endPage = function() table.insert(T.pages, page); return true end,
}
_G.peripheral = {
  wrap = function(cote) if cote == "left" then return imprimante end end,
  getType = function(cote) if cote == "left" then return "printer" end end,
}
