-- rapport.lua  (ComputerCraft 1.80, turtle)
-- Mesure la production d'un Void Ore Miner et imprime le Rapport de Production.
--
-- Montage :
--   - la turtle REGARDE le coffre de sortie du Void Miner (devant elle) ;
--   - un coffre de stockage SOUS la turtle ;
--   - une imprimante collee a GAUCHE de la turtle, avec du papier et de l'encre.
--
-- Usage : rapport [minutes]      (5 minutes par defaut)
--
-- Nexus Absolu -- ligne Coding, quete "Le Rapport de Production".

local args = { ... }
local DUREE = tonumber(args[1]) or 5      -- minutes de mesure
local PAS = 5                             -- secondes entre deux vidages
local COTE_IMPRIMANTE = "left"
local TITRE = "Rapport de Production"

local compte = {}   -- ["nom:damage"] = nombre d'items recus
local total = 0

-- Vide le coffre de devant dans le coffre du dessous.
-- Si "compter" est vrai, chaque item deplace est compte.
local function vider(compter)
  turtle.select(1)
  while turtle.suck() do
    local detail = turtle.getItemDetail(1)
    if detail and compter then
      local cle = detail.name .. ":" .. detail.damage
      compte[cle] = (compte[cle] or 0) + detail.count
      total = total + detail.count
    end
    if not turtle.dropDown() then
      error("Le coffre du dessous est plein.", 0)
    end
  end
end

-- Trie les minerais du plus produit au moins produit.
local function classement()
  local liste = {}
  for nom, n in pairs(compte) do
    liste[#liste + 1] = { nom = nom, n = n }
  end
  table.sort(liste, function(a, b) return a.n > b.n end)
  return liste
end

-- Coupe un texte a la largeur d'une page imprimee (25 caracteres).
local function court(texte, largeur)
  if #texte > largeur then
    return texte:sub(1, largeur)
  end
  return texte
end

-- 1. Ce qui attendait deja dans le coffre n'a pas ete produit pendant la mesure.
print("Vidage initial du coffre...")
vider(false)

-- 2. Mesure.
print("Mesure pendant " .. DUREE .. " minute(s).")
local tours = math.floor(DUREE * 60 / PAS)
for i = 1, tours do
  sleep(PAS)
  vider(true)
  term.setCursorPos(1, select(2, term.getCursorPos()))
  term.clearLine()
  term.write(string.format("%d/%d  recus : %d", i, tours, total))
end
print("")

-- 3. Resultat.
local parMinute = total / DUREE
local liste = classement()
print(string.format("Total : %d minerais en %s min", total, DUREE))
print(string.format("Debit : %.1f minerais / minute", parMinute))
for i = 1, math.min(5, #liste) do
  print(string.format("  %5d  %s", liste[i].n, liste[i].nom))
end

-- 4. Impression.
local imprimante = peripheral.wrap(COTE_IMPRIMANTE)
if not imprimante or peripheral.getType(COTE_IMPRIMANTE) ~= "printer" then
  print("Pas d'imprimante a gauche : rapport non imprime.")
  return
end
if not imprimante.newPage() then
  print("L'imprimante n'a plus de papier ou d'encre.")
  return
end
imprimante.setPageTitle(TITRE)
local largeur, hauteur = imprimante.getPageSize()
local ligne = 1
local function ecrire(texte)
  if ligne <= hauteur then
    imprimante.setCursorPos(1, ligne)
    imprimante.write(court(texte, largeur))
    ligne = ligne + 1
  end
end
ecrire("RAPPORT DE PRODUCTION")
ecrire("Void Ore Miner")
ecrire("")
ecrire(string.format("Duree : %s min", DUREE))
ecrire(string.format("Total : %d", total))
ecrire(string.format("Debit : %.1f / min", parMinute))
ecrire("")
for i = 1, #liste do
  local nom = liste[i].nom:gsub("^[^:]*:", "")
  ecrire(string.format("%4d %s", liste[i].n, nom))
end
imprimante.endPage()
print("Rapport imprime. Recupere la page dans l'imprimante.")
