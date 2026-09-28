-- rapport.lua  (OpenComputers 1.8.9, OpenOS, Lua 5.3)
-- Mesure la production d'un Void Ore Miner avec un transposer, puis ecrit le
-- Rapport de Production dans /home/rapport.txt, pret a etre grave sur EEPROM.
--
-- Montage : un transposer relie a l'ordinateur (colle ou par cable), qui touche
--   - le coffre de sortie du Void Miner  (par defaut : au-dessus, "up")
--   - un coffre de stockage              (par defaut : en dessous, "down")
--
-- Usage : rapport [minutes] [cote_source] [cote_destination]
--         rapport 5 up down
-- Puis :  flash /home/rapport.txt "Rapport de Production"
--
-- Nexus Absolu -- ligne Coding, quete "Le Rapport de Production".

local component = require("component")
local shell = require("shell")
local sides = require("sides")

local args = shell.parse(...)
local DUREE = tonumber(args[1]) or 5          -- minutes
local SOURCE = sides[args[2] or "up"]
local DEST = sides[args[3] or "down"]
local PAS = 5                                 -- secondes entre deux vidages
local FICHIER = "/home/rapport.txt"

if not component.isAvailable("transposer") then
  io.stderr:write("Aucun transposer : il doit toucher le boitier ou un cable.\n")
  return 1
end
if SOURCE == nil or DEST == nil then
  io.stderr:write("Cote inconnu. Cotes : up down north south east west.\n")
  return 1
end
local tp = component.transposer

-- Verifie qu'il y a bien un inventaire de chaque cote.
for _, cote in ipairs({ SOURCE, DEST }) do
  if not tp.getInventorySize(cote) then
    io.stderr:write("Pas d'inventaire du cote " .. sides[cote] .. ".\n")
    return 1
  end
end

local compte = {}   -- ["nom:damage"] = nombre
local total = 0

-- Vide la source dans la destination. transferItem renvoie le nombre
-- d'items vraiment deplaces : c'est lui qu'on compte.
local function vider(compter)
  for slot = 1, tp.getInventorySize(SOURCE) do
    local pile = tp.getStackInSlot(SOURCE, slot)
    if pile then
      local deplaces = tp.transferItem(SOURCE, DEST, pile.size, slot) or 0
      if deplaces < pile.size then
        error("Le coffre de destination est plein.", 0)
      end
      if compter then
        local cle = pile.name .. ":" .. math.floor(pile.damage)
        compte[cle] = (compte[cle] or 0) + deplaces
        total = total + deplaces
      end
    end
  end
end

print("Vidage initial du coffre...")
vider(false)

print("Mesure pendant " .. DUREE .. " minute(s). Ctrl+Alt+C pour arreter.")
local tours = math.floor(DUREE * 60 / PAS)
for i = 1, tours do
  os.sleep(PAS)
  vider(true)
  io.write(string.format("\r%d/%d  recus : %d   ", i, tours, total))
end
print("")

local liste = {}
for nom, n in pairs(compte) do liste[#liste + 1] = { nom = nom, n = n } end
table.sort(liste, function(a, b) return a.n > b.n end)

local lignes = {
  "RAPPORT DE PRODUCTION -- Void Ore Miner",
  string.format("Duree : %g min", DUREE),
  string.format("Total : %d minerais", total),
  string.format("Debit : %.1f minerais / minute", total / DUREE),
  "",
}
for _, e in ipairs(liste) do
  lignes[#lignes + 1] = string.format("%6d  %s", e.n, e.nom)
end

local texte = table.concat(lignes, "\n") .. "\n"
print(texte)

-- Une EEPROM contient 4 Ko : le rapport doit tenir dedans.
if #texte > 4096 then
  texte = texte:sub(1, 4096)
end
local f = assert(io.open(FICHIER, "w"))
f:write(texte)
f:close()

print("Rapport ecrit dans " .. FICHIER .. ".")
print("Pour le graver :  flash " .. FICHIER .. ' "Rapport de Production"')
print("flash te demandera d'inserer une EEPROM vierge a la place du BIOS,")
print("puis de remettre le BIOS. OpenOS continue de tourner pendant l'echange.")
