package com.nexusabsolu.mod.machines;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nexusabsolu.mod.NexusAbsoluMod;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Charge et conserve les definitions de machines.
 *
 * Les definitions vivent dans les resources du mod, pas dans config/ : elles
 * font partie du mod, elles sont versionnees avec lui, et le joueur n'a pas a
 * les editer. Un jar ne permettant pas de lister un dossier de facon fiable,
 * l'enumeration passe par index.json.
 *
 * Le chargement se fait une fois au preInit. Une machine mal formee est
 * refusee avec un message explicite dans le log, les autres continuent de se
 * charger : une faute de frappe dans un fichier ne doit pas couper les 29
 * autres machines.
 */
public final class MachineRegistry {

    private static final String BASE = "/assets/nexusabsolu/machines/";
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static Map<String, MachineDefinition> byId =
        Collections.emptyMap();
    private static Map<String, MachineDefinition> byController =
        Collections.emptyMap();
    private static MachineLegend legend;
    private static boolean loaded = false;

    private MachineRegistry() { }

    public static boolean isLoaded() { return loaded; }
    public static MachineLegend getLegend() { return legend; }

    public static MachineDefinition get(String id) { return byId.get(id); }

    /** Definition associee a un bloc controleur, ou null. */
    public static MachineDefinition getByController(String blockId) {
        return byController.get(blockId);
    }

    public static Map<String, MachineDefinition> all() { return byId; }

    public static int size() { return byId.size(); }

    /** Appele une fois au preInit. Idempotent. */
    public static void load() {
        if (loaded) return;
        loaded = true;

        Map<String, MachineDefinition> ids = new LinkedHashMap<String, MachineDefinition>();
        Map<String, MachineDefinition> ctrls = new LinkedHashMap<String, MachineDefinition>();

        try {
            JsonObject legendJson = readJson("legend.json");
            legend = MachineLegend.fromJson(legendJson);
        } catch (Exception e) {
            NexusAbsoluMod.LOGGER.error(
                "Moteur machines : legend.json illisible, aucune machine ne sera chargee", e);
            byId = Collections.emptyMap();
            byController = Collections.emptyMap();
            return;
        }

        JsonArray list;
        try {
            JsonObject index = readJson("index.json");
            if (!index.has("machines") || !index.get("machines").isJsonArray()) {
                throw new MachineFormatException("index.json : cle 'machines' absente");
            }
            list = index.getAsJsonArray("machines");
        } catch (Exception e) {
            NexusAbsoluMod.LOGGER.error(
                "Moteur machines : index.json illisible, aucune machine ne sera chargee", e);
            byId = Collections.emptyMap();
            byController = Collections.emptyMap();
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            String id = list.get(i).getAsString();
            String file = id + ".json";
            try {
                JsonObject root = readJson(file);
                MachineDefinition def = MachineDefinition.fromJson(root, legend, file);

                if (!def.getId().equals(id)) {
                    throw new MachineFormatException(file + " : 'id' vaut '" + def.getId()
                        + "' mais index.json le declare sous '" + id + "'");
                }
                if (ids.containsKey(def.getId())) {
                    throw new MachineFormatException(file + " : id '" + def.getId()
                        + "' deja utilise");
                }
                MachineDefinition clash = ctrls.get(def.getControllerBlockId());
                if (clash != null) {
                    throw new MachineFormatException(file + " : le controleur '"
                        + def.getControllerBlockId() + "' est deja pris par la machine '"
                        + clash.getId() + "'");
                }

                ids.put(def.getId(), def);
                ctrls.put(def.getControllerBlockId(), def);

                NexusAbsoluMod.LOGGER.info("Moteur machines : " + def.getId() + " ("
                    + def.getPattern().getSizeLabel() + ", "
                    + def.getPattern().getBlockCount() + " blocs) -> "
                    + def.getControllerBlockId());

            } catch (Exception e) {
                NexusAbsoluMod.LOGGER.error("Moteur machines : " + file + " refuse -- "
                    + e.getMessage());
            }
        }

        byId = Collections.unmodifiableMap(ids);
        byController = Collections.unmodifiableMap(ctrls);
        NexusAbsoluMod.LOGGER.info("Moteur machines : " + byId.size() + " machine(s) chargee(s)");
    }

    private static JsonObject readJson(String name) throws IOException, MachineFormatException {
        InputStream in = MachineRegistry.class.getResourceAsStream(BASE + name);
        if (in == null) {
            throw new MachineFormatException("ressource introuvable : " + BASE + name);
        }
        Reader reader = null;
        try {
            reader = new InputStreamReader(in, UTF8);
            return new JsonParser().parse(reader).getAsJsonObject();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ignored) { }
            }
            try { in.close(); } catch (IOException ignored) { }
        }
    }
}
