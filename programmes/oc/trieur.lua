-- trieur.lua  (OpenComputers 1.8.9, OpenOS)
-- Lecon "Le transposer" : trie un coffre. Les minerais (oredict "ore...")
-- vont d'un cote, tout le reste de l'autre.
--
-- Montage : un transposer touche trois coffres.
-- Usage : trieur [source] [minerais] [reste]      (defaut : up north south)

local component = require("component")
local shell = require("shell")
local sides = require("sides")

local args = shell.parse(...)
local SOURCE = sides[args[1] or "up"]
local MINERAIS = sides[args[2] or "north"]
local RESTE = sides[args[3] or "south"]

local tp = component.transposer

local function estMinerai(pile)
  for _, nom in ipairs(pile.oreNames or {}) do
    if nom:sub(1, 3) == "ore" then
      return true
    end
  end
  return false
end

local tries = 0
for slot = 1, tp.getInventorySize(SOURCE) do
  local pile = tp.getStackInSlot(SOURCE, slot)
  if pile then
    local cible = estMinerai(pile) and MINERAIS or RESTE
    tries = tries + (tp.transferItem(SOURCE, cible, pile.size, slot) or 0)
  end
end
print(tries .. " items tries.")
