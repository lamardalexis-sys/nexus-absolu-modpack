package com.nexusabsolu.mod.util;

import com.nexusabsolu.mod.blocks.BlockPortailSortieActif;
import com.nexusabsolu.mod.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * PortailSortieValidator - frame detection for the Age 2 Phase 1->2 portal.
 *
 * Frame shape: 4 wide x 4 tall (interior 2 wide x 2 tall).
 *
 *   X X X X       <- top edge (4)
 *   X . . X       <- interior row
 *   X . . X       <- interior row
 *   X X X X       <- bottom edge (4)
 *
 *   X = Obsidienne Voss frame block (12 total)
 *   . = Air (becomes Portail Sortie Actif when ignited - 4 blocks)
 *
 * Two orientations supported: AXIS=X (frame extends along world X axis)
 * and AXIS=Z (frame extends along world Z axis). Detection is brute-force
 * across all (dx, dy) offsets in [0, WIDTH) x [0, HEIGHT) which is at most
 * 16 tests per axis = 32 total. Cheap and bulletproof.
 *
 * Sprint 3 - v1.0.352+
 */
public class PortailSortieValidator {

    public static final int WIDTH = 4;
    public static final int HEIGHT = 4;

    /**
     * Try to find a valid frame anchored such that {@code clickedPos} is
     * one of its frame blocks. Returns the bottom-left corner if found,
     * null otherwise.
     */
    public static BlockPos findFrame(World world, BlockPos clickedPos,
                                      EnumFacing.Axis axis) {
        for (int dy = 0; dy < HEIGHT; dy++) {
            for (int dx = 0; dx < WIDTH; dx++) {
                BlockPos candidate = offsetAlongAxis(clickedPos, -dx, -dy, axis);
                if (isValidFrame(world, candidate, axis)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Try both orientations. Returns a result describing which axis matched
     * and the bottom-left, or null if no valid frame.
     */
    public static FrameResult findAnyFrame(World world, BlockPos clickedPos) {
        for (EnumFacing.Axis axis : new EnumFacing.Axis[]{EnumFacing.Axis.X, EnumFacing.Axis.Z}) {
            BlockPos bl = findFrame(world, clickedPos, axis);
            if (bl != null) {
                return new FrameResult(bl, axis);
            }
        }
        return null;
    }

    /** Verify the 18 frame blocks + 12 interior positions. */
    private static boolean isValidFrame(World world, BlockPos bottomLeft,
                                         EnumFacing.Axis axis) {
        Block frameBlock = ModBlocks.OBSIDIENNE_VOSS;
        for (int dy = 0; dy < HEIGHT; dy++) {
            for (int dx = 0; dx < WIDTH; dx++) {
                BlockPos pos = offsetAlongAxis(bottomLeft, dx, dy, axis);
                Block atPos = world.getBlockState(pos).getBlock();

                boolean isFramePos = (dy == 0 || dy == HEIGHT - 1
                                       || dx == 0 || dx == WIDTH - 1);
                if (isFramePos) {
                    if (atPos != frameBlock) return false;
                } else {
                    // Interior must be air or already-active portal (re-ignition idempotent)
                    if (atPos != Blocks.AIR && atPos != ModBlocks.PORTAIL_SORTIE_ACTIF) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** Fill the 2x2 interior with active portal blocks. */
    public static void fillInterior(World world, BlockPos bottomLeft,
                                     EnumFacing.Axis axis) {
        IBlockState portalState = ModBlocks.PORTAIL_SORTIE_ACTIF.getDefaultState()
            .withProperty(BlockPortailSortieActif.AXIS, axis);
        for (int dy = 1; dy < HEIGHT - 1; dy++) {
            for (int dx = 1; dx < WIDTH - 1; dx++) {
                BlockPos pos = offsetAlongAxis(bottomLeft, dx, dy, axis);
                world.setBlockState(pos, portalState, 3);
            }
        }
    }

    /**
     * Helper: offset a BlockPos by (dx, dy) along the given axis. Y is always
     * vertical. dx is along X if axis=X, else along Z.
     */
    private static BlockPos offsetAlongAxis(BlockPos base, int dx, int dy,
                                             EnumFacing.Axis axis) {
        if (axis == EnumFacing.Axis.X) {
            return base.add(dx, dy, 0);
        } else {
            return base.add(0, dy, dx);
        }
    }

    /** Result of frame detection: bottom-left corner + axis. */
    public static class FrameResult {
        public final BlockPos bottomLeft;
        public final EnumFacing.Axis axis;

        public FrameResult(BlockPos bottomLeft, EnumFacing.Axis axis) {
            this.bottomLeft = bottomLeft;
            this.axis = axis;
        }
    }
}
