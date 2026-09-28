-- 8 blocs de pierre, un gravier qui retombe une fois au bloc 3, puis un mur (bedrock) au bloc 6.
local pos, dir, fuel, gravier, log = 0, 1, 0, true, {}
local charbon = 4
local function devant() return pos + dir end
turtle = {
  getFuelLevel = function() return fuel end,
  select = function() end,
  refuel = function() fuel = fuel + charbon * 80; charbon = 0; return true end,
  detect = function() return dir == 1 and devant() <= 8 and not (log[devant()]) end,
  dig = function()
    if devant() == 6 then return false end         -- bedrock
    if devant() == 3 and gravier then gravier = false; return true end -- retombe
    log[devant()] = true; return true end,
  forward = function()
    if dir == 1 and devant() <= 8 and not log[devant()] then return false end
    if fuel <= 0 then return false end
    pos = pos + dir; fuel = fuel - 1; return true end,
  detectUp = function() return true end, digUp = function() return true end,
  turnLeft = function() dir = -dir * 1; if dir == 0 then dir = 1 end end,
}
-- deux turnLeft = demi-tour : on modelise un demi-tour tous les deux appels
local n = 0
turtle.turnLeft = function() n = n + 1; if n % 2 == 0 then dir = -dir end end
local vraiArgs = { "10" }
local f = assert(loadfile("cc/tunnel.lua"))
f(unpack(vraiArgs))
assert(pos == 0, "la turtle doit revenir au depart, pos=" .. pos)
print("OK tunnel.lua : bloquee par la bedrock, gravier gere, revenue au depart")
