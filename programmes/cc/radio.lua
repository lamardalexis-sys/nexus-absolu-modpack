-- radio.lua  (ComputerCraft 1.80, modem sans fil)
-- Lecon "Le reseau" : un ordinateur ecoute, l'autre envoie.
--
-- Sur l'ordinateur qui ecoute :   radio ecoute
-- Sur l'ordinateur qui envoie :   radio envoie Bonjour depuis la base
--
-- Le modem sans fil peut etre sur n'importe quel cote.

local args = { ... }

-- Trouve le premier modem et ouvre rednet dessus.
local cote
for _, c in ipairs(rs.getSides()) do
  if peripheral.getType(c) == "modem" then
    cote = c
    break
  end
end
if not cote then
  print("Aucun modem : pose un modem sans fil sur l'ordinateur.")
  return
end
rednet.open(cote)

if args[1] == "ecoute" then
  print("Mon numero : " .. os.getComputerID() .. ". J'ecoute (Ctrl+T pour arreter).")
  while true do
    local expediteur, message = rednet.receive()
    print("[" .. expediteur .. "] " .. tostring(message))
  end
elseif args[1] == "envoie" and args[2] then
  local message = table.concat(args, " ", 2)
  rednet.broadcast(message)
  print("Envoye a tous : " .. message)
else
  print("Usage : radio ecoute   |   radio envoie <message>")
end
