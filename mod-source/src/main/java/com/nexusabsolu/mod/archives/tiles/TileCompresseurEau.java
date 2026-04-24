package com.nexusabsolu.mod.archives.tiles;

import com.nexusabsolu.mod.proxy.CommonProxy;
import com.nexusabsolu.mod.tiles.InternalEnergyStorage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;

/**
 * TileEntity du Compresseur d'Eau Voss.
 *
 * <p>Cycle de fonctionnement :
 * <ol>
 *   <li>Verifie si inputTank contient >=200 mB (eau_voss_chaude OU water vanilla)</li>
 *   <li>Verifie si outputTank a au moins 150 mB d'espace libre</li>
 *   <li>Verifie si energy stocke >=5000 RF (cost total d'un cycle)</li>
 *   <li>Incremente progress de 50 RF/tick draine (100 ticks total = 5s)</li>
 *   <li>A progress=100 : drain 200 mB input, ajoute 150 mB eau_voss_froide output,
 *       50 mB perdus (force appoint regulier)</li>
 * </ol>
 *
 * <p>Capacite tanks : 4000 mB chacun (4 buckets). RF capacity : 20000.
 *
 * @since v1.0.302 (Archives Voss Sprint 1)
 */
public class TileCompresseurEau extends TileEntity implements ITickable {

    // Constantes balance
    public static final int RF_CAPACITY = 20_000;
    public static final int RF_PER_TICK = 50;
    public static final int TANK_CAPACITY = 4_000;  // 4 buckets par tank
    public static final int INPUT_PER_CYCLE = 200;  // mB
    public static final int OUTPUT_PER_CYCLE = 150; // mB (perte de 50 mB)
    public static final int CYCLE_TICKS = 100;       // 5 secondes

    // Energy storage (reutilise InternalEnergyStorage)
    // maxReceive = INT_MAX comme les fours (pas de throttle artificiel)
    private final InternalEnergyStorage energy = new InternalEnergyStorage(RF_CAPACITY, Integer.MAX_VALUE);

    /**
     * Input tank. Accepte 2 fluides :
     *   - eau_voss_chaude (cycle ferme)
     *   - water vanilla (appoint pour compenser les 50 mB perdus)
     */
    private final FluidTank inputTank = new FluidTank(TANK_CAPACITY) {
        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            if (fluid == null) return false;
            String name = fluid.getFluid().getName();
            return name.equals("eau_voss_chaude") || name.equals("water");
        }
        @Override
        public boolean canDrainFluidType(FluidStack fluid) {
            return false;  // pas d'extraction externe depuis input
        }
    };

    /**
     * Output tank. Ne contient que de l'eau_voss_froide.
     */
    private final FluidTank outputTank = new FluidTank(TANK_CAPACITY) {
        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return false;  // output ne peut etre rempli qu'en interne
        }
    };

    /** Progress du cycle en cours, 0..CYCLE_TICKS. 0 = idle. */
    private int progress = 0;

    @Override
    public void update() {
        if (world == null || world.isRemote) return;

        boolean wasRunning = progress > 0;
        tryRunCycle();
        boolean nowRunning = progress > 0;

        // v1.0.302 : notif client sur changement d'etat actif (GUI progress bar)
        if (wasRunning != nowRunning) {
            markDirty();
        }
    }

    /**
     * Logique principale : tente d'avancer le cycle d'1 tick.
     */
    private void tryRunCycle() {
        // Conditions pour run :
        if (inputTank.getFluidAmount() < INPUT_PER_CYCLE) { progress = 0; return; }
        if (outputTank.getCapacity() - outputTank.getFluidAmount() < OUTPUT_PER_CYCLE) { progress = 0; return; }
        if (energy.getEnergyStored() < RF_PER_TICK) { progress = 0; return; }

        // Drain energie pour ce tick
        energy.drainInternal(RF_PER_TICK);
        progress++;

        // Cycle complet ?
        if (progress >= CYCLE_TICKS) {
            // Drain input (melange eau_chaude + water, peu importe la proportion)
            inputTank.drainInternal(INPUT_PER_CYCLE, true);
            // Fill output avec eau_voss_froide
            FluidStack result = new FluidStack(CommonProxy.EAU_VOSS_FROIDE, OUTPUT_PER_CYCLE);
            outputTank.fillInternal(result, true);
            // Reset progress
            progress = 0;
            markDirty();
        }
    }

    // =========================================================================
    // Getters publics (pour GUI + Container)
    // =========================================================================

    public int getProgress() { return progress; }
    public int getMaxProgress() { return CYCLE_TICKS; }
    public int getEnergyStored() { return energy.getEnergyStored(); }
    public int getMaxEnergy() { return energy.getMaxEnergyStored(); }
    public FluidTank getInputTank() { return inputTank; }
    public FluidTank getOutputTank() { return outputTank; }

    // Setters client (pour sync)
    public void setProgressClient(int v) { this.progress = v; }
    public void setEnergyClient(int v) { this.energy.setEnergy(v); }

    // =========================================================================
    // Capability exposure
    // =========================================================================

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing side) {
        if (capability == CapabilityEnergy.ENERGY) return true;
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return true;
        return super.hasCapability(capability, side);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityEnergy.ENERGY) {
            return (T) energy;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            // Combine les 2 tanks : les pipes exterieures voient un IFluidHandler
            // avec 2 tanks (input + output). Les mods peuvent fill input et drain output.
            IFluidHandler combined = new FluidHandlerConcatenate(inputTank, outputTank);
            return (T) combined;
        }
        return super.getCapability(capability, side);
    }

    // =========================================================================
    // NBT serialization
    // =========================================================================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("progress", progress);
        nbt.setInteger("energy", energy.getEnergyStored());
        NBTTagCompound inputNBT = new NBTTagCompound();
        inputTank.writeToNBT(inputNBT);
        nbt.setTag("inputTank", inputNBT);
        NBTTagCompound outputNBT = new NBTTagCompound();
        outputTank.writeToNBT(outputNBT);
        nbt.setTag("outputTank", outputNBT);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.progress = nbt.getInteger("progress");
        this.energy.setEnergy(nbt.getInteger("energy"));
        if (nbt.hasKey("inputTank")) inputTank.readFromNBT(nbt.getCompoundTag("inputTank"));
        if (nbt.hasKey("outputTank")) outputTank.readFromNBT(nbt.getCompoundTag("outputTank"));
    }

    @Override
    public boolean shouldRefresh(net.minecraft.world.World world,
                                  net.minecraft.util.math.BlockPos pos,
                                  net.minecraft.block.state.IBlockState oldState,
                                  net.minecraft.block.state.IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
