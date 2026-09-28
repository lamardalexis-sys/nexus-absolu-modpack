-- Bouchons OpenComputers 1.8.9 / OpenOS pour tester programmes/oc/*.lua en Lua 5.3.
-- Simule un transposer entre le coffre de sortie d'un Void Miner (up) et un
-- coffre de stockage (down), et le temps qui passe avec os.sleep().

local T = { temps = 0, dessous = 0, fichiers = {} }
TEST = T

local production = {
  { name = "minecraft:iron_ore", damage = 0.0, parMin = 4.5 },
  { name = "thermalfoundation:ore", damage = 2.0, parMin = 2.0 },
  { name = "minecraft:gold_ore", damage = 0.0, parMin = 1.0 },
}
-- Coffre de 27 cases ; 12 charbons attendaient avant la mesure.
local coffre = {}
coffre[1] = { name = "minecraft:coal_ore", damage = 0.0, size = 12 }
local cumul = {}
local function produire(dt)
  for i, p in ipairs(production) do
    cumul[i] = (cumul[i] or 0) + p.parMin * dt / 60
    local n = math.floor(cumul[i])
    if n > 0 then
      cumul[i] = cumul[i] - n
      for s = 1, 27 do
        if coffre[s] == nil then
          coffre[s] = { name = p.name, damage = p.damage, size = n }
          break
        end
      end
    end
  end
end

local sides = { bottom = 0, down = 0, top = 1, up = 1, back = 2, north = 2,
                front = 3, south = 3, right = 4, west = 4, left = 5, east = 5 }
for k, v in pairs({ down = 0, up = 1, north = 2, south = 3, west = 4, east = 5 }) do
  sides[v] = k
end

local transposer = {
  getInventorySize = function(cote)
    if cote == sides.up or cote == sides.down then return 27 end
    return nil, "no inventory"
  end,
  getStackInSlot = function(cote, slot)
    assert(slot >= 1 and slot <= 27, "slot hors bornes")
    if cote == sides.up then
      local p = coffre[slot]
      if p then return { name = p.name, damage = p.damage, size = p.size, label = p.name } end
    end
    return nil
  end,
  transferItem = function(src, dst, count, slot)
    assert(src == sides.up and dst == sides.down)
    local p = coffre[slot]
    if not p then return 0 end
    local n = math.min(count or 64, p.size, 64)
    p.size = p.size - n
    if p.size == 0 then coffre[slot] = nil end
    T.dessous = T.dessous + n
    return n
  end,
}

local modules = {
  component = {
    isAvailable = function(nom) return nom == "transposer" end,
    transposer = transposer,
  },
  shell = { parse = function(...) return { ... }, {} end },
  sides = sides,
}
local vraiRequire = require
require = function(nom) return modules[nom] or vraiRequire(nom) end

os.sleep = function(s) T.temps = T.temps + s; produire(s) end

local vraiOpen = io.open
io.open = function(chemin, mode)
  if chemin:sub(1, 6) == "/home/" then
    local contenu = {}
    return {
      write = function(self, s) contenu[#contenu + 1] = s end,
      close = function() T.fichiers[chemin] = table.concat(contenu) end,
    }
  end
  return vraiOpen(chemin, mode)
end
