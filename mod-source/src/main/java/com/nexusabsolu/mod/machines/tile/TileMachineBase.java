package com.nexusabsolu.mod.machines.tile;

import com.nexusabsolu.mod.machines.MachineDefinition;
import com.nexusabsolu.mod.machines.MachineRegistry;
import com.nexusabsolu.mod.machines.StructureValidator;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.List;

/**
 * Socle des controleurs de machines Nexus.
 *
 * Phase 1 du moteur : formation de structure uniquement. Ni recette, ni
 * energie, ni inventaire -- ils arrivent en phase 2. Cette classe sait
 * seulement dire, de facon fiable et lisible, si le multibloc est monte.
 *
 * Le bloc controleur n'est jamais remplace : c'est ce bloc-ci, avec cette
 * TileEntity-ci, du debut a la fin.
 */
public class TileMachineBase extends TileEntity implements ITickable {

    /** Intervalle de revalidation, en ticks. */
    private static final int CHECK_INTERVAL = 20;

    private boolean formed = false;
    private int rotation = -1;
    /** Nombre de blocs manquants a la derniere verification. Diagnostic seul. */
    private int missingCount = -1;

    private MachineDefinition definition;
    private boolean definitionResolved = false;

    /**
     * Definition associee au bloc pose ici, ou null si ce bloc n'est declare
     * dans aucun fichier de assets/nexusabsolu/machines/.
     */
    public MachineDefinition getDefinition() {
        if (!definitionResolved) {
            definitionResolved = true;
            if (world != null) {
                Block block = world.getBlockState(pos).getBlock();
                ResourceLocation rl = block.getRegistryName();
                if (rl != null) {
                    definition = MachineRegistry.getByController(rl.toString());
                }
            }
        }
        return definition;
    }

    public boolean isFormed() { return formed; }
    public int getRotation() { return rotation; }
    public int getMissingCount() { return missingCount; }

    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        if (world.getTotalWorldTime() % CHECK_INTERVAL != 0) return;

        MachineDefinition def = getDefinition();
        if (def == null) return;

        StructureValidator.Result result = StructureValidator.validate(world, pos, def);
        missingCount = result.isFormed() ? 0 : result.getMissingCount();

        if (result.isFormed() && !formed) {
            formed = true;
            rotation = result.getRotation();
            onFormed(def);
            markDirty();
            syncToClient();
        } else if (!result.isFormed() && formed) {
            formed = false;
            rotation = -1;
            onBroken(def);
            markDirty();
            syncToClient();
        }
    }

    /** Structure complete. Signal sonore, le reste viendra en phase 2. */
    protected void onFormed(MachineDefinition def) {
        world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL,
            SoundCategory.BLOCKS, 0.7F, 1.2F);
    }

    /** Structure cassee. */
    protected void onBroken(MachineDefinition def) {
        world.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK,
            SoundCategory.BLOCKS, 0.5F, 0.8F);
    }

    /**
     * Rapport lisible envoye au joueur qui clique droit sur le controleur.
     * C'est le seul retour utilisateur de la phase 1, et c'est aussi l'outil
     * de diagnostic : il dit quel bloc manque, ou, et ce qu'il y a a la place.
     */
    public void sendDiagnostic(EntityPlayer player) {
        if (world == null || world.isRemote) return;

        MachineDefinition def = getDefinition();
        if (def == null) {
            Block block = world.getBlockState(pos).getBlock();
            ResourceLocation rl = block.getRegistryName();
            player.sendMessage(new TextComponentString("\u00A7cAucune machine declaree pour "
                + (rl == null ? "ce bloc" : rl.toString())
                + " \u00A77(assets/nexusabsolu/machines/)"));
            return;
        }

        StructureValidator.Result result = StructureValidator.validate(world, pos, def);

        player.sendMessage(new TextComponentString("\u00A7b" + def.getNameFr()
            + " \u00A77-- " + def.getPattern().getSizeLabel() + ", "
            + def.getPattern().getBlockCount() + " blocs"));

        if (result.isFormed()) {
            player.sendMessage(new TextComponentString(
                "\u00A7aStructure formee\u00A77, orientation " + result.getRotation() * 90
                + " degres."));
            reportPorts(player, result);
            return;
        }

        List<StructureValidator.Mismatch> miss = result.getMismatches();
        player.sendMessage(new TextComponentString("\u00A7cStructure incomplete \u00A77-- "
            + miss.size() + " bloc(s) a corriger, orientation la plus proche : "
            + result.getRotation() * 90 + " degres."));

        int shown = 0;
        for (int i = 0; i < miss.size() && shown < 6; i++, shown++) {
            StructureValidator.Mismatch m = miss.get(i);
            player.sendMessage(new TextComponentString("\u00A77  "
                + m.pos.getX() + " " + m.pos.getY() + " " + m.pos.getZ()
                + " : attendu \u00A7f" + m.expectedLabel
                + "\u00A77, trouve \u00A7f" + shortName(m.found)));
        }
        if (miss.size() > shown) {
            player.sendMessage(new TextComponentString("\u00A78  ... et "
                + (miss.size() - shown) + " autre(s)."));
        }
    }

    private void reportPorts(EntityPlayer player, StructureValidator.Result result) {
        int items = result.getPorts("port_item").size();
        int fluids = result.getPorts("port_fluid").size();
        int energy = result.getPorts("port_energy").size();
        player.sendMessage(new TextComponentString("\u00A77Ports : \u00A7f" + items
            + "\u00A77 item, \u00A7f" + fluids + "\u00A77 fluide, \u00A7f" + energy
            + "\u00A77 energie."));
    }

    private static String shortName(String blockId) {
        if ("minecraft:air".equals(blockId)) return "rien";
        return blockId;
    }

    // -- Sync --

    private void syncToClient() {
        if (world != null && !world.isRemote) {
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    // -- NBT --

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("Formed", formed);
        compound.setInteger("Rotation", rotation);
        compound.setInteger("Missing", missingCount);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        formed = compound.getBoolean("Formed");
        rotation = compound.hasKey("Rotation") ? compound.getInteger("Rotation") : -1;
        missingCount = compound.hasKey("Missing") ? compound.getInteger("Missing") : -1;
    }

    /**
     * La TileEntity survit a un changement d'etat du meme bloc. Elle ne doit
     * disparaitre que si le bloc lui-meme change.
     */
    @Override
    public boolean shouldRefresh(net.minecraft.world.World world, BlockPos pos,
                                 net.minecraft.block.state.IBlockState oldState,
                                 net.minecraft.block.state.IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
