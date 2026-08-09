package com.nexusabsolu.mod.machines;

import com.google.gson.JsonObject;

/**
 * Une machine du moteur Nexus : son identite, son bloc controleur, son pattern.
 *
 * Immuable. Chargee depuis assets/nexusabsolu/machines/<id>.json, qui est la
 * source de verite unique : le moteur Java et les scripts Python du wiki et du
 * Carnet Voss lisent le meme fichier. Aucune donnee de structure ne doit etre
 * dupliquee en dur dans le code.
 */
public final class MachineDefinition {

    private final String id;
    private final String archetype;
    private final String nameFr;
    private final String nameEn;
    private final String color;
    private final String controllerBlockId;
    private final MachinePattern pattern;
    private final MachineLegend legend;

    private MachineDefinition(String id, String archetype, String nameFr, String nameEn,
                              String color, String controllerBlockId,
                              MachinePattern pattern, MachineLegend legend) {
        this.id = id;
        this.archetype = archetype;
        this.nameFr = nameFr;
        this.nameEn = nameEn;
        this.color = color;
        this.controllerBlockId = controllerBlockId;
        this.pattern = pattern;
        this.legend = legend;
    }

    public String getId() { return id; }
    public String getArchetype() { return archetype; }
    public String getNameFr() { return nameFr; }
    public String getNameEn() { return nameEn; }
    /** Couleur RRGGBB sans dieze, pour le wiki et les effets. */
    public String getColor() { return color; }
    public String getControllerBlockId() { return controllerBlockId; }
    public MachinePattern getPattern() { return pattern; }
    public MachineLegend getLegend() { return legend; }

    /**
     * Les identifiants de blocs acceptes pour un symbole donne.
     * Le symbole du controleur est resolu ici, pas dans la legende : le bloc
     * attendu depend de la machine.
     */
    public boolean accepts(char symbol, String blockId) {
        MachineLegend.Symbol s = legend.get(symbol);
        if (s == null) return false;
        if (s.isController()) return controllerBlockId.equals(blockId);
        return s.accepts(blockId);
    }

    public static MachineDefinition fromJson(JsonObject root, MachineLegend legend, String where)
            throws MachineFormatException {

        String id = MachineLegend.readString(root, "id", where);
        String archetype = root.has("archetype") ? root.get("archetype").getAsString() : "custom";
        String nameFr = MachineLegend.readString(root, "name_fr", where);
        String nameEn = MachineLegend.readString(root, "name_en", where);
        String color = root.has("color") ? root.get("color").getAsString() : "FFFFFF";
        String controller = MachineLegend.readString(root, "controller", where);

        if (controller.indexOf(':') < 0) {
            throw new MachineFormatException(where + " : 'controller' doit etre un identifiant"
                + " complet du type nexusabsolu:xxx_controller, recu '" + controller + "'");
        }
        if (!root.has("layers") || !root.get("layers").isJsonArray()) {
            throw new MachineFormatException(where + " : cle 'layers' absente ou pas un tableau");
        }

        MachinePattern pattern =
            MachinePattern.fromLayers(root.getAsJsonArray("layers"), legend, where);

        return new MachineDefinition(id, archetype, nameFr, nameEn, color,
                                     controller, pattern, legend);
    }
}
