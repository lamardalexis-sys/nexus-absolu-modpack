local sorties, file, t = {}, {}, 0
os.startTimer = function(p) t = t + 1; file[#file+1] = { "timer", t }; return t end
local levier = false
os.pullEvent = function()
  if #sorties >= 4 and not levier then levier = true; return "redstone" end
  return unpack(table.remove(file, 1))
end
redstone = { setOutput = function(c, v) sorties[#sorties+1] = tostring(v) end,
             getInput = function(c) return c == "left" and levier end }
assert(loadfile("cc/phare.lua"))("1")
assert(table.concat(sorties, ",") == "true,false,true,false,false", table.concat(sorties, ","))
print("OK phare.lua : clignote 4 fois puis s'eteint au levier")
