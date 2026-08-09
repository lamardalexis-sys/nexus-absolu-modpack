package com.nexusabsolu.mod.machines;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pattern d'un multibloc, en coordonnees LOCALES relatives au controleur.
 *
 * Convention de lecture du JSON, identique pour toutes les machines :
 *   - "layers" est ordonne du bas vers le haut, y croissant ;
 *   - dans une couche, rows[0] est la rangee a z = -1, rows[1] a z = 0, etc. ;
 *   - dans une rangee, le caractere d'indice 0 est a x = -1, le suivant a
 *     x = 0, etc. ;
 *   - le symbole de role "controller" definit l'origine (0,0,0) et doit
 *     apparaitre exactement une fois.
 *
 * La position du controleur n'est PAS stockee dans la liste des entrees : le
 * moteur ne la valide pas, il valide ce qu'il y a autour. C'est ce qui manquait
 * a Modular Machinery, qui ecrasait cette position avec son propre bloc.
 */
public final class MachinePattern {

    /** Une position du pattern et le symbole attendu. */
    public static final class Entry {
        public final int x;
        public final int y;
        public final int z;
        public final char symbol;

        Entry(int x, int y, int z, char symbol) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.symbol = symbol;
        }
    }

    /** Plafond de la charte de tailles : 3 x 5 x 3, jamais plus. */
    public static final int MAX_WIDTH = 3;
    public static final int MAX_HEIGHT = 5;
    public static final int MAX_DEPTH = 3;

    private final List<Entry> entries;
    private final int minX, maxX, minY, maxY, minZ, maxZ;

    private MachinePattern(List<Entry> entries,
                           int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.entries = Collections.unmodifiableList(entries);
        this.minX = minX; this.maxX = maxX;
        this.minY = minY; this.maxY = maxY;
        this.minZ = minZ; this.maxZ = maxZ;
    }

    /** Les positions a valider, controleur exclu. */
    public List<Entry> getEntries() { return entries; }

    /** Nombre de blocs a poser, controleur exclu. */
    public int getBlockCount() { return entries.size(); }

    public int getMinX() { return minX; }
    public int getMaxX() { return maxX; }
    public int getMinY() { return minY; }
    public int getMaxY() { return maxY; }
    public int getMinZ() { return minZ; }
    public int getMaxZ() { return maxZ; }

    public int getWidth()  { return maxX - minX + 1; }
    public int getHeight() { return maxY - minY + 1; }
    public int getDepth()  { return maxZ - minZ + 1; }

    /** Ex. "3x3x3". */
    public String getSizeLabel() {
        return getWidth() + "x" + getHeight() + "x" + getDepth();
    }

    /**
     * Construit le pattern depuis le tableau "layers" d'une definition.
     *
     * @param legend utilisee uniquement pour reconnaitre le symbole du
     *               controleur et refuser les symboles inconnus.
     * @param where  prefixe des messages d'erreur (nom du fichier).
     */
    public static MachinePattern fromLayers(JsonArray layers, MachineLegend legend, String where)
            throws MachineFormatException {

        if (layers == null || layers.size() == 0) {
            throw new MachineFormatException(where + " : 'layers' absent ou vide");
        }

        List<Entry> raw = new ArrayList<Entry>();
        boolean controllerFound = false;
        int cx = 0, cy = 0, cz = 0;
        int expectedRows = -1;
        int expectedCols = -1;
        int previousY = Integer.MIN_VALUE;

        for (int li = 0; li < layers.size(); li++) {
            JsonElement le = layers.get(li);
            if (!le.isJsonObject()) {
                throw new MachineFormatException(where + " : layers[" + li + "] n'est pas un objet");
            }
            JsonObject layer = le.getAsJsonObject();

            if (!layer.has("y")) {
                throw new MachineFormatException(where + " : layers[" + li + "] sans cle 'y'");
            }
            int y = layer.get("y").getAsInt();
            if (y <= previousY) {
                throw new MachineFormatException(where + " : layers[" + li + "] a y=" + y
                    + ", les couches doivent etre ordonnees du bas vers le haut, y strictement croissant");
            }
            previousY = y;

            if (!layer.has("rows") || !layer.get("rows").isJsonArray()) {
                throw new MachineFormatException(
                    where + " : layers[" + li + "] sans tableau 'rows'");
            }
            JsonArray rows = layer.getAsJsonArray("rows");

            if (expectedRows < 0) {
                expectedRows = rows.size();
            } else if (rows.size() != expectedRows) {
                throw new MachineFormatException(where + " : layers[" + li + "] a "
                    + rows.size() + " rangees, les couches precedentes en ont " + expectedRows
                    + ". Le pattern doit etre un pave regulier.");
            }

            for (int zi = 0; zi < rows.size(); zi++) {
                String row = rows.get(zi).getAsString();

                if (expectedCols < 0) {
                    expectedCols = row.length();
                } else if (row.length() != expectedCols) {
                    throw new MachineFormatException(where + " : layers[" + li + "].rows[" + zi
                        + "] fait " + row.length() + " caracteres, attendu " + expectedCols);
                }

                for (int xi = 0; xi < row.length(); xi++) {
                    char sym = row.charAt(xi);
                    MachineLegend.Symbol s = legend.get(sym);
                    if (s == null) {
                        throw new MachineFormatException(where + " : symbole '" + sym
                            + "' en layers[" + li + "].rows[" + zi + "][" + xi
                            + "] absent de legend.json");
                    }
                    if (s.isController()) {
                        if (controllerFound) {
                            throw new MachineFormatException(where
                                + " : plusieurs symboles de controleur dans le pattern,"
                                + " il en faut exactement un");
                        }
                        controllerFound = true;
                        cx = xi; cy = y; cz = zi;
                        continue;
                    }
                    raw.add(new Entry(xi, y, zi, sym));
                }
            }
        }

        if (!controllerFound) {
            throw new MachineFormatException(where
                + " : aucun symbole de role 'controller' dans le pattern");
        }
        if (raw.isEmpty()) {
            throw new MachineFormatException(where + " : pattern reduit au seul controleur");
        }

        // Recentrage sur le controleur : toutes les coordonnees deviennent
        // relatives a lui.
        List<Entry> centered = new ArrayList<Entry>(raw.size());
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (int i = 0; i < raw.size(); i++) {
            Entry e = raw.get(i);
            int x = e.x - cx;
            int y = e.y - cy;
            int z = e.z - cz;
            centered.add(new Entry(x, y, z, e.symbol));
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }
        // Le controleur fait partie du volume meme s'il n'est pas valide.
        if (0 < minX) minX = 0;
        if (0 > maxX) maxX = 0;
        if (0 < minY) minY = 0;
        if (0 > maxY) maxY = 0;
        if (0 < minZ) minZ = 0;
        if (0 > maxZ) maxZ = 0;

        MachinePattern pattern = new MachinePattern(centered, minX, maxX, minY, maxY, minZ, maxZ);

        if (pattern.getWidth() > MAX_WIDTH || pattern.getHeight() > MAX_HEIGHT
                || pattern.getDepth() > MAX_DEPTH) {
            throw new MachineFormatException(where + " : pattern " + pattern.getSizeLabel()
                + " hors charte. Plafond " + MAX_WIDTH + "x" + MAX_HEIGHT + "x" + MAX_DEPTH + ".");
        }

        return pattern;
    }
}
