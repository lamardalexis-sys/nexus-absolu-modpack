package com.nexusabsolu.mod.machines;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Legende partagee par toutes les definitions de machines.
 *
 * Un symbole de pattern (un caractere) porte trois choses : un role
 * fonctionnel, un libelle affichable, et la liste des identifiants de blocs
 * qui l'acceptent. La legende est chargee une fois depuis
 * assets/nexusabsolu/machines/legend.json et partagee par toutes les
 * MachineDefinition.
 *
 * Le symbole du controleur (role "controller") a volontairement une liste de
 * blocs vide : le bloc attendu depend de la machine, il est fourni par
 * MachineDefinition.getControllerBlockId().
 */
public final class MachineLegend {

    public static final String ROLE_CONTROLLER = "controller";
    public static final String ROLE_CASING = "casing";
    public static final String ROLE_ANY = "any";
    public static final String ROLE_PORT_ITEM = "port_item";
    public static final String ROLE_PORT_FLUID = "port_fluid";
    public static final String ROLE_PORT_ENERGY = "port_energy";

    /** Un symbole de pattern et ce qu'il accepte. */
    public static final class Symbol {

        private final char key;
        private final String role;
        private final String nameFr;
        private final String nameEn;
        private final Set<String> blocks;

        Symbol(char key, String role, String nameFr, String nameEn, Set<String> blocks) {
            this.key = key;
            this.role = role;
            this.nameFr = nameFr;
            this.nameEn = nameEn;
            this.blocks = Collections.unmodifiableSet(blocks);
        }

        public char getKey() { return key; }
        public String getRole() { return role; }
        public String getNameFr() { return nameFr; }
        public String getNameEn() { return nameEn; }
        public Set<String> getBlocks() { return blocks; }

        public boolean isController() { return ROLE_CONTROLLER.equals(role); }

        /** True si l'identifiant de bloc donne satisfait ce symbole. */
        public boolean accepts(String blockId) {
            return blockId != null && blocks.contains(blockId);
        }
    }

    private final Map<Character, Symbol> symbols;

    private MachineLegend(Map<Character, Symbol> symbols) {
        this.symbols = Collections.unmodifiableMap(symbols);
    }

    public Symbol get(char key) { return symbols.get(Character.valueOf(key)); }

    public boolean has(char key) { return symbols.containsKey(Character.valueOf(key)); }

    public Map<Character, Symbol> all() { return symbols; }

    /**
     * Construit la legende depuis le JSON.
     *
     * @throws MachineFormatException si un symbole est mal forme. On echoue
     *         fort et tot : une legende partiellement chargee produirait des
     *         structures qui refusent de se former sans dire pourquoi.
     */
    public static MachineLegend fromJson(JsonObject root) throws MachineFormatException {
        if (!root.has("symbols") || !root.get("symbols").isJsonObject()) {
            throw new MachineFormatException("legend.json : cle 'symbols' absente ou pas un objet");
        }
        JsonObject syms = root.getAsJsonObject("symbols");
        Map<Character, Symbol> out = new LinkedHashMap<Character, Symbol>();

        for (Map.Entry<String, JsonElement> e : syms.entrySet()) {
            String key = e.getKey();
            if (key.length() != 1) {
                throw new MachineFormatException(
                    "legend.json : le symbole '" + key + "' doit faire exactement un caractere");
            }
            if (!e.getValue().isJsonObject()) {
                throw new MachineFormatException(
                    "legend.json : le symbole '" + key + "' n'est pas un objet");
            }
            JsonObject o = e.getValue().getAsJsonObject();

            String role = readString(o, "role", "legend.json/" + key);
            String nameFr = o.has("name_fr") ? o.get("name_fr").getAsString() : role;
            String nameEn = o.has("name_en") ? o.get("name_en").getAsString() : role;

            Set<String> blocks = new HashSet<String>();
            if (o.has("blocks")) {
                if (!o.get("blocks").isJsonArray()) {
                    throw new MachineFormatException(
                        "legend.json/" + key + " : 'blocks' doit etre un tableau");
                }
                JsonArray arr = o.getAsJsonArray("blocks");
                for (int i = 0; i < arr.size(); i++) {
                    blocks.add(arr.get(i).getAsString());
                }
            }

            if (blocks.isEmpty() && !ROLE_CONTROLLER.equals(role)) {
                throw new MachineFormatException(
                    "legend.json/" + key + " : liste 'blocks' vide pour le role '" + role
                    + "'. Seul le role 'controller' a le droit d'etre vide.");
            }

            out.put(Character.valueOf(key.charAt(0)),
                    new Symbol(key.charAt(0), role, nameFr, nameEn, blocks));
        }

        if (out.isEmpty()) {
            throw new MachineFormatException("legend.json : aucun symbole declare");
        }
        return new MachineLegend(out);
    }

    static String readString(JsonObject o, String key, String where)
            throws MachineFormatException {
        if (!o.has(key) || o.get(key).isJsonNull()) {
            throw new MachineFormatException(where + " : cle '" + key + "' absente");
        }
        return o.get(key).getAsString();
    }
}
