-- phare.lua  (ComputerCraft 1.80)
-- Lecon "Evenements" : fait clignoter une lampe a DROITE de l'ordinateur,
-- et s'arrete quand un levier a GAUCHE est active.
--
-- Usage : phare [periode_en_secondes]

local periode = tonumber(({ ... })[1]) or 1
local allume = false

print("Phare en marche. Active le levier a gauche pour l'arreter.")
local minuteur = os.startTimer(periode)

while true do
  local evenement, param = os.pullEvent()
  if evenement == "timer" and param == minuteur then
    allume = not allume
    redstone.setOutput("right", allume)
    minuteur = os.startTimer(periode)
  elseif evenement == "redstone" and redstone.getInput("left") then
    redstone.setOutput("right", false)
    print("Levier active : phare arrete.")
    break
  end
end
