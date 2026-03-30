package com.nexusabsolu.mod.tiles;

import net.minecraftforge.energy.EnergyStorage;

public class InternalEnergyStorage extends EnergyStorage {

    public InternalEnergyStorage(int capacity, int maxReceive) {
        super(capacity, maxReceive, 0);
    }

    public InternalEnergyStorage(int capacity, int maxReceive, int energy) {
        super(capacity, maxReceive, 0, energy);
    }

    public InternalEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    // Internal drain - bypasses maxExtract check
    public void drainInternal(int amount) {
        this.energy = Math.max(0, this.energy - amount);
    }

    // Internal generation - bypasses maxReceive check
    public void generateInternal(int amount) {
        this.energy = Math.min(this.capacity, this.energy + amount);
    }

    public int getEnergy() {
        return this.energy;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(this.capacity, energy));
    }
}
