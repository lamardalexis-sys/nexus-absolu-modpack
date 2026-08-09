package com.nexusabsolu.mod.machines;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Valide un pattern autour d'un controleur, dans les quatre orientations.
 *
 * Le controleur reste le controleur : cette classe ne pose et ne remplace
 * aucun bloc. C'est la difference de fond avec Modular Machinery 1.11.1, dont
 * checkStructure() ecrasait la position du controleur par son propre bloc.
 *
 * En cas d'echec, le validateur renvoie les ecarts de la rotation la plus
 * proche du but, pour que le joueur sache quel bloc manque et ou.
 */
public final class StructureValidator {

    /**
     * Rotations de 90 degres autour de l'axe Y.
     * dx = a*x + b*z ; dz = c*x + d*z, avec {a, b, c, d}.
     */
    private static final int[][] ROTATIONS = {
        {  1,  0,  0,  1 },   // 0   : local tel quel
        {  0, -1,  1,  0 },   // 90  : horaire
        { -1,  0,  0, -1 },   // 180
        {  0,  1, -1,  0 },   // 270
    };

    public static final int ROTATION_COUNT = 4;

    private StructureValidator() { }

    /** Un bloc du pattern qui ne correspond pas. */
    public static final class Mismatch {
        public final BlockPos pos;
        public final char expected;
        public final String expectedLabel;
        public final String found;

        Mismatch(BlockPos pos, char expected, String expectedLabel, String found) {
            this.pos = pos;
            this.expected = expected;
            this.expectedLabel = expectedLabel;
            this.found = found;
        }
    }

    /** Resultat d'une validation. */
    public static final class Result {
        private final boolean formed;
        private final int rotation;
        private final Map<String, List<BlockPos>> portsByRole;
        private final List<Mismatch> mismatches;
        private final int missingCount;

        Result(boolean formed, int rotation,
               Map<String, List<BlockPos>> portsByRole,
               List<Mismatch> mismatches, int missingCount) {
            this.formed = formed;
            this.rotation = rotation;
            this.portsByRole = Collections.unmodifiableMap(portsByRole);
            this.mismatches = Collections.unmodifiableList(mismatches);
            this.missingCount = missingCount;
        }

        public boolean isFormed() { return formed; }

        /** Rotation retenue, ou la plus proche du but en cas d'echec. */
        public int getRotation() { return rotation; }

        /** Positions des ports trouves, groupees par role de la legende. */
        public Map<String, List<BlockPos>> getPortsByRole() { return portsByRole; }

        public List<BlockPos> getPorts(String role) {
            List<BlockPos> l = portsByRole.get(role);
            return l == null ? Collections.<BlockPos>emptyList() : l;
        }

        /** Ecarts de la rotation la plus proche. Vide si la structure est formee. */
        public List<Mismatch> getMismatches() { return mismatches; }

        /** Nombre total de blocs manquants ou incorrects. */
        public int getMissingCount() { return missingCount; }
    }

    /**
     * Essaie les quatre rotations et renvoie la premiere qui passe.
     *
     * @param world      monde, cote serveur de preference
     * @param controller position du bloc controleur
     * @param def        definition de la machine
     */
    public static Result validate(World world, BlockPos controller, MachineDefinition def) {
        if (world == null || controller == null || def == null) {
            return new Result(false, 0,
                new LinkedHashMap<String, List<BlockPos>>(),
                new ArrayList<Mismatch>(), Integer.MAX_VALUE);
        }

        List<Mismatch> bestMismatches = null;
        int bestRotation = 0;

        for (int r = 0; r < ROTATION_COUNT; r++) {
            List<Mismatch> mismatches = new ArrayList<Mismatch>();
            Map<String, List<BlockPos>> ports = new LinkedHashMap<String, List<BlockPos>>();

            for (MachinePattern.Entry e : def.getPattern().getEntries()) {
                BlockPos p = worldPos(controller, e.x, e.y, e.z, r);
                String found = blockIdAt(world, p);

                if (def.accepts(e.symbol, found)) {
                    MachineLegend.Symbol sym = def.getLegend().get(e.symbol);
                    String role = sym.getRole();
                    List<BlockPos> l = ports.get(role);
                    if (l == null) {
                        l = new ArrayList<BlockPos>();
                        ports.put(role, l);
                    }
                    l.add(p);
                } else {
                    MachineLegend.Symbol sym = def.getLegend().get(e.symbol);
                    String label = sym == null ? String.valueOf(e.symbol) : sym.getNameFr();
                    mismatches.add(new Mismatch(p, e.symbol, label, found));
                }
            }

            if (mismatches.isEmpty()) {
                return new Result(true, r, ports, mismatches, 0);
            }
            if (bestMismatches == null || mismatches.size() < bestMismatches.size()) {
                bestMismatches = mismatches;
                bestRotation = r;
            }
        }

        return new Result(false, bestRotation,
            new LinkedHashMap<String, List<BlockPos>>(),
            bestMismatches, bestMismatches.size());
    }

    /** Position monde d'une coordonnee locale, pour une rotation donnee. */
    public static BlockPos worldPos(BlockPos controller, int x, int y, int z, int rotation) {
        int[] m = ROTATIONS[rotation & 3];
        int dx = x * m[0] + z * m[1];
        int dz = x * m[2] + z * m[3];
        return controller.add(dx, y, dz);
    }

    /** Identifiant du bloc a cette position, jamais null. */
    public static String blockIdAt(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        ResourceLocation rl = block.getRegistryName();
        return rl == null ? "?" : rl.toString();
    }
}
