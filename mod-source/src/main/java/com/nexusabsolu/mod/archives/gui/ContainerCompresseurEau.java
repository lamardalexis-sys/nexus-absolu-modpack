package com.nexusabsolu.mod.archives.gui;

import com.nexusabsolu.mod.archives.tiles.TileCompresseurEau;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;

/**
 * Container du Compresseur d'Eau Voss.
 *
 * <p>Pas de slots items (c'est une machine a fluides). Juste la sync des
 * fields vers le client pour affichage GUI : progress, energy, input fluid
 * amount, output fluid amount.
 *
 * <p>Pour simplifier, on passe tout via 4 fields int (pas de split long comme
 * pour les furnaces, les valeurs restent petites : capacity 20k RF,
 * tank 4k mB, progress 0..100).
 *
 * @since v1.0.302 (Archives Voss Sprint 1)
 */
public class ContainerCompresseurEau extends Container {

    public static final int FIELD_PROGRESS = 0;
    public static final int FIELD_ENERGY = 1;
    public static final int FIELD_INPUT_AMOUNT = 2;
    public static final int FIELD_OUTPUT_AMOUNT = 3;

    private final TileCompresseurEau tile;

    // Cache pour detectAndSendChanges (envoyer uniquement sur delta)
    private int lastProgress = -1;
    private int lastEnergy = -1;
    private int lastInputAmount = -1;
    private int lastOutputAmount = -1;

    public ContainerCompresseurEau(InventoryPlayer playerInv, TileCompresseurEau tile) {
        this.tile = tile;
        // Inventaire joueur standard (pas de slots machine pour cette machine fluide)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new net.minecraft.inventory.Slot(
                    playerInv, col + row * 9 + 9,
                    8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new net.minecraft.inventory.Slot(
                playerInv, col, 8 + col * 18, 142));
        }
    }

    public TileCompresseurEau getTile() { return tile; }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        // Pas de shift-click handling particulier (pas de slots machine)
        return ItemStack.EMPTY;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int progress = tile.getProgress();
        int energy = tile.getEnergyStored();
        int inputAmt = tile.getInputTank().getFluidAmount();
        int outputAmt = tile.getOutputTank().getFluidAmount();
        for (IContainerListener listener : listeners) {
            if (progress != lastProgress) listener.sendWindowProperty(this, FIELD_PROGRESS, progress);
            if (energy != lastEnergy) listener.sendWindowProperty(this, FIELD_ENERGY, energy);
            if (inputAmt != lastInputAmount) listener.sendWindowProperty(this, FIELD_INPUT_AMOUNT, inputAmt);
            if (outputAmt != lastOutputAmount) listener.sendWindowProperty(this, FIELD_OUTPUT_AMOUNT, outputAmt);
        }
        lastProgress = progress;
        lastEnergy = energy;
        lastInputAmount = inputAmt;
        lastOutputAmount = outputAmt;
    }

    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case FIELD_PROGRESS: tile.setProgressClient(data); break;
            case FIELD_ENERGY: tile.setEnergyClient(data); break;
            case FIELD_INPUT_AMOUNT:
                // Hack simple : on recree un FluidStack pour display. Le fluide
                // reel est en NBT cote serveur, mais on sait que input peut etre
                // eau_voss_chaude OU water. Pour le GUI, on utilise eau_voss_chaude
                // comme display par defaut si amount > 0 (texture bleue ternie).
                if (data > 0) {
                    net.minecraftforge.fluids.FluidStack fs = new net.minecraftforge.fluids.FluidStack(
                        com.nexusabsolu.mod.proxy.CommonProxy.EAU_VOSS_CHAUDE, data);
                    tile.getInputTank().setFluid(fs);
                } else {
                    tile.getInputTank().setFluid(null);
                }
                break;
            case FIELD_OUTPUT_AMOUNT:
                if (data > 0) {
                    net.minecraftforge.fluids.FluidStack fs = new net.minecraftforge.fluids.FluidStack(
                        com.nexusabsolu.mod.proxy.CommonProxy.EAU_VOSS_FROIDE, data);
                    tile.getOutputTank().setFluid(fs);
                } else {
                    tile.getOutputTank().setFluid(null);
                }
                break;
        }
    }
}
