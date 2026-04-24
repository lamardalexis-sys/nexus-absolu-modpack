package com.nexusabsolu.mod.archives.tiles;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.archives.blocks.BlockArchiveFrame;
import com.nexusabsolu.mod.archives.blocks.BlockArchiveThermalCore;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

/**
 * TileEntity of the Archives Voss Controller.
 *
 * <p>Responsibilities :
 * <ul>
 *   <li>Detect and validate the 8-block multiblock structure around itself</li>
 *   <li>Re-validate on neighbor change (block placed/broken nearby)</li>
 *   <li>Hold the network reference (Sprint 2+)</li>
 *   <li>Own the RF energy storage (Sprint 2+)</li>
 *   <li>Own the fluid tanks eau_voss_froide IN / eau_voss_chaude OUT (Sprint 2+)</li>
 * </ul>
 *
 * <p><b>Structure du multiblock</b> (Controller place au centre de la couche 2) :
 *
 * <pre>
 *   Couche 2 (y+1, au-dessus du sol) :   FRAME  CONTROLLER  FRAME
 *   Couche 1 (y+0, au niveau du sol) :   IN     THERMAL  THERMAL  THERMAL  OUT
 *                                        ^                                  ^
 *                                        ItemInput (bloc existant)          ItemOutput (bloc existant)
 * </pre>
 *
 * <p>Soit vue de cote (section transversale) :
 *
 * <pre>
 *           [  F ] [CTRL] [  F ]         <- y+1
 *   [IN] [TC] [TC] [TC] [OUT]            <- y+0
 * </pre>
 *
 * <p>Le Controller a donc :
 * <ul>
 *   <li>1 FRAME a gauche (west) meme niveau</li>
 *   <li>1 FRAME a droite (east) meme niveau</li>
 *   <li>1 THERMAL_CORE directement en-dessous (y-1, centre)</li>
 *   <li>1 THERMAL_CORE a (y-1, west)</li>
 *   <li>1 THERMAL_CORE a (y-1, east)</li>
 *   <li>1 ItemInput a (y-1, west-west) = 2 blocs a gauche du thermal center</li>
 *   <li>1 ItemOutput a (y-1, east-east) = 2 blocs a droite</li>
 * </ul>
 *
 * <p>L'orientation (axe Ouest-Est VS Nord-Sud) est determinee au 1er placement :
 * on cherche quelle orientation valide la structure.
 *
 * <p><b>Scan frequency</b> : re-scan uniquement sur neighborChanged events
 * (optimisation critique - voir pieges doc ARCHIVES_VOSS_RESEARCH_SYNTHESIS).
 *
 * @since v1.0.302 (Archives Voss Sprint 1)
 */
public class TileArchiveController extends TileEntity implements ITickable {

    // Registry names des blocs qu'on recherche dans le scan
    // (on compare via ResourceLocation plutot que Class pour flexibilite future)
    private static final String REG_FRAME = Reference.MOD_ID + ":archive_frame";
    private static final String REG_THERMAL = Reference.MOD_ID + ":archive_thermal_core";
    // ItemInput et ItemOutput : blocs deja codes dans le pack (reutilisation)
    private static final String REG_ITEM_INPUT = Reference.MOD_ID + ":item_input";
    private static final String REG_ITEM_OUTPUT = Reference.MOD_ID + ":item_output";

    /** True si la structure 8-blocs est valide autour du controller. */
    private boolean structureFormed = false;

    /**
     * Orientation du multiblock (axe ouest-est OR nord-sud).
     * null quand structure non valide.
     */
    private Axis axis = null;

    /**
     * Counter pour limiter les rescan. Scanne une fois au load, puis
     * sur neighborChanged events uniquement (pas a chaque tick).
     */
    private boolean needsRescan = true;

    public enum Axis {
        WEST_EAST,  // Frames gauche/droite sur axe X, IN a l'ouest, OUT a l'est
        NORTH_SOUTH; // Frames nord/sud sur axe Z, IN au nord, OUT au sud
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) return;

        // Rescan paresseux : une fois au load puis sur demande
        if (needsRescan) {
            needsRescan = false;
            rescanStructure();
        }
    }

    /**
     * Appele par le Block.onNeighborChange pour signaler qu'un bloc
     * voisin a ete pose/casse. Delegue a update() au prochain tick.
     */
    public void requestRescan() {
        needsRescan = true;
    }

    /**
     * Execute le scan des 8 blocs attendus autour du controller.
     * Teste les 2 orientations possibles (W-E puis N-S) et retient la premiere
     * qui match. Si aucune match, structureFormed = false.
     */
    private void rescanStructure() {
        boolean wasFormed = structureFormed;

        // Teste axe West-East
        if (checkStructure(Axis.WEST_EAST)) {
            structureFormed = true;
            axis = Axis.WEST_EAST;
        }
        // Teste axe North-South
        else if (checkStructure(Axis.NORTH_SOUTH)) {
            structureFormed = true;
            axis = Axis.NORTH_SOUTH;
        }
        // Aucune orientation valide
        else {
            structureFormed = false;
            axis = null;
        }

        // Sauvegarde NBT si l'etat a change
        if (wasFormed != structureFormed) {
            markDirty();
            // Notifier le client pour update visuel (renderer enhanced quand form)
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    /**
     * Verifie si la structure correspond a une orientation donnee.
     *
     * @param axis WEST_EAST : frames a l'ouest+est, IN a l'ouest, OUT a l'est
     *             NORTH_SOUTH : frames au nord+sud, IN au nord, OUT au sud
     * @return true si tous les 8 blocs sont a leur place
     */
    private boolean checkStructure(Axis axis) {
        int dx1, dz1, dx2, dz2;  // offsets pour les 2 directions opposees
        if (axis == Axis.WEST_EAST) {
            dx1 = -1; dz1 = 0;  // ouest
            dx2 = 1; dz2 = 0;   // est
        } else {
            dx1 = 0; dz1 = -1;  // nord
            dx2 = 0; dz2 = 1;   // sud
        }

        // Couche 2 (y du controller) :
        //   pos+dir1 = frame,  pos+dir2 = frame
        if (!matchesBlock(pos.add(dx1, 0, dz1), REG_FRAME)) return false;
        if (!matchesBlock(pos.add(dx2, 0, dz2), REG_FRAME)) return false;

        // Couche 1 (y-1) :
        //   pos+(0,-1,0) = thermal_core (centre)
        //   pos+dir1+(-1y) = thermal_core (gauche du centre couche 1)
        //   pos+dir2+(-1y) = thermal_core (droite du centre couche 1)
        if (!matchesBlock(pos.add(0, -1, 0), REG_THERMAL)) return false;
        if (!matchesBlock(pos.add(dx1, -1, dz1), REG_THERMAL)) return false;
        if (!matchesBlock(pos.add(dx2, -1, dz2), REG_THERMAL)) return false;

        // Couche 1 extremites :
        //   pos+(2*dir1)+(-1y) = ItemInput
        //   pos+(2*dir2)+(-1y) = ItemOutput
        if (!matchesBlock(pos.add(2 * dx1, -1, 2 * dz1), REG_ITEM_INPUT)) return false;
        if (!matchesBlock(pos.add(2 * dx2, -1, 2 * dz2), REG_ITEM_OUTPUT)) return false;

        // Les 8 blocs sont la. Structure valide.
        return true;
    }

    /**
     * Helper : verifie que le bloc a la position donnee match le registry name.
     * Retourne false si le bloc est absent (air) ou different.
     */
    private boolean matchesBlock(BlockPos target, String expectedRegistry) {
        if (!world.isBlockLoaded(target)) return false;  // safety contre chunk unloaded
        Block block = world.getBlockState(target).getBlock();
        if (block == null) return false;
        net.minecraft.util.ResourceLocation reg = block.getRegistryName();
        if (reg == null) return false;
        return reg.toString().equals(expectedRegistry);
    }

    // =========================================================================
    // Getters publics
    // =========================================================================

    public boolean isStructureFormed() { return structureFormed; }
    public Axis getStructureAxis() { return axis; }

    /**
     * Appele par BlockArchiveController.breakBlock avant que le bloc disparaisse.
     * Sprint 2+ : deconnectera les nodes du reseau.
     */
    public void onControllerBroken() {
        // Sprint 2 : detach all network nodes, cleanup
        structureFormed = false;
        axis = null;
    }

    // =========================================================================
    // NBT serialization
    // =========================================================================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("structureFormed", structureFormed);
        if (axis != null) nbt.setString("axis", axis.name());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.structureFormed = nbt.getBoolean("structureFormed");
        if (nbt.hasKey("axis")) {
            try {
                this.axis = Axis.valueOf(nbt.getString("axis"));
            } catch (IllegalArgumentException e) {
                this.axis = null;
            }
        }
        // Re-scan au prochain tick pour valider l'etat sauvegarde
        this.needsRescan = true;
    }

    /**
     * v1.0.302 : shouldRefresh override pour eviter que le TE soit recree
     * quand le blockstate change (sinon on perdrait structureFormed et axis).
     * Voir ARCHIVES_VOSS_RESEARCH_SYNTHESIS.md piege #1.
     */
    @Override
    public boolean shouldRefresh(net.minecraft.world.World world,
                                  net.minecraft.util.math.BlockPos pos,
                                  net.minecraft.block.state.IBlockState oldState,
                                  net.minecraft.block.state.IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    /**
     * v1.0.307 : bounding box elargie pour le TESR.
     * Sans ca, quand le bloc Controller est hors de l'ecran, le TESR ne
     * tick plus et la shell disparait, meme si d'autres blocs du multiblock
     * sont encore visibles.
     * Retourne une AABB couvrant les 8 blocs du multiblock (5x2x1 autour du
     * Controller pour toute orientation).
     */
    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox() {
        // Couvre 5 blocs dans chaque direction horizontale (par safety : on ne
        // sait pas l'orientation cote tick rendering), 2 blocs vertical.
        net.minecraft.util.math.BlockPos p = getPos();
        return new net.minecraft.util.math.AxisAlignedBB(
            p.getX() - 3, p.getY() - 2, p.getZ() - 3,
            p.getX() + 4, p.getY() + 2, p.getZ() + 4
        );
    }
}
