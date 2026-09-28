-- tunnel.lua  (ComputerCraft 1.80, turtle minière)
-- Lecon "La tortue mineuse" : creuse un tunnel de 1 x 2, puis revient.
-- Mets du charbon dans la case 16 : la turtle s'en sert comme carburant.
--
-- Usage : tunnel <longueur>

local longueur = tonumber(({ ... })[1])
if not longueur or longueur < 1 then
  print("Usage : tunnel <longueur>")
  return
end

local function carburant(besoin)
  if turtle.getFuelLevel() == "unlimited" then return true end
  if turtle.getFuelLevel() >= besoin then return true end
  turtle.select(16)
  turtle.refuel()
  turtle.select(1)
  return turtle.getFuelLevel() >= besoin
end

-- Aller + retour : il faut 2 x longueur de carburant.
if not carburant(longueur * 2) then
  print("Pas assez de carburant : mets du charbon en case 16.")
  return
end

local avance = 0
for i = 1, longueur do
  -- Le gravier peut retomber : on creuse tant que quelque chose bloque.
  -- Si dig() echoue (bedrock, bloc protege), inutile d'insister.
  while turtle.detect() do
    if not turtle.dig() then
      break
    end
  end
  if not turtle.forward() then
    print("Bloque apres " .. avance .. " blocs.")
    break
  end
  avance = avance + 1
  if turtle.detectUp() then
    turtle.digUp()
  end
end

print("Tunnel de " .. avance .. " blocs. Retour.")
turtle.turnLeft()
turtle.turnLeft()
for i = 1, avance do
  -- Du gravier a pu retomber derriere : on le recreuse.
  while not turtle.forward() do
    if not turtle.dig() then
      print("Retour bloque, il reste " .. (avance - i + 1) .. " blocs.")
      return
    end
  end
end
turtle.turnLeft()
turtle.turnLeft()
