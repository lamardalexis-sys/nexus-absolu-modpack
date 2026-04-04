package com.nexusabsolu.mod.tiles;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Mekanism-style side configuration for machines.
 * 4 I/O types x 6 faces, with eject and auto-pull per type.
 */
public class SideConfig {

    public static final int TYPE_FOOD   = 0;
    public static final int TYPE_WATER  = 1;
    public static final int TYPE_ENERGY = 2;
    public static final int TYPE_OUTPUT = 3;
    public static final int NUM_TYPES = 4;
    public static final int NUM_FACES = 6;

    // faceConfig[type][face] = active?
    private boolean[][] faceConfig = new boolean[NUM_TYPES][NUM_FACES];
    private boolean[] eject = new boolean[NUM_TYPES];
    private boolean[] autoPull = new boolean[NUM_TYPES];

    public boolean isFaceActive(int type, int face) {
        if (type < 0 || type >= NUM_TYPES || face < 0 || face >= NUM_FACES) return false;
        return faceConfig[type][face];
    }

    public void toggleFace(int type, int face) {
        if (type < 0 || type >= NUM_TYPES || face < 0 || face >= NUM_FACES) return;
        faceConfig[type][face] = !faceConfig[type][face];
    }

    public void setFace(int type, int face, boolean active) {
        if (type < 0 || type >= NUM_TYPES || face < 0 || face >= NUM_FACES) return;
        faceConfig[type][face] = active;
    }

    public boolean isEject(int type) {
        return type >= 0 && type < NUM_TYPES && eject[type];
    }

    public void toggleEject(int type) {
        if (type >= 0 && type < NUM_TYPES) eject[type] = !eject[type];
    }

    public boolean isAutoPull(int type) {
        return type >= 0 && type < NUM_TYPES && autoPull[type];
    }

    public void toggleAutoPull(int type) {
        if (type >= 0 && type < NUM_TYPES) autoPull[type] = !autoPull[type];
    }

    /** Pack one type's 6 face booleans into 6 bits of an int. */
    public int getFaceBits(int type) {
        if (type < 0 || type >= NUM_TYPES) return 0;
        int bits = 0;
        for (int f = 0; f < NUM_FACES; f++) {
            if (faceConfig[type][f]) bits |= (1 << f);
        }
        return bits;
    }

    /** Unpack 6 bits into one type's face config. */
    public void setFaceBits(int type, int bits) {
        if (type < 0 || type >= NUM_TYPES) return;
        for (int f = 0; f < NUM_FACES; f++) {
            faceConfig[type][f] = (bits & (1 << f)) != 0;
        }
    }

    /** Pack eject flags into 4 bits. */
    public int getEjectBits() {
        int bits = 0;
        for (int t = 0; t < NUM_TYPES; t++) {
            if (eject[t]) bits |= (1 << t);
        }
        return bits;
    }

    public void setEjectBits(int bits) {
        for (int t = 0; t < NUM_TYPES; t++) {
            eject[t] = (bits & (1 << t)) != 0;
        }
    }

    /** Pack auto-pull flags into 4 bits. */
    public int getAutoPullBits() {
        int bits = 0;
        for (int t = 0; t < NUM_TYPES; t++) {
            if (autoPull[t]) bits |= (1 << t);
        }
        return bits;
    }

    public void setAutoPullBits(int bits) {
        for (int t = 0; t < NUM_TYPES; t++) {
            autoPull[t] = (bits & (1 << t)) != 0;
        }
    }

    /** Check if any type is active on a given face. */
    public boolean hasAnyType(int face) {
        for (int t = 0; t < NUM_TYPES; t++) {
            if (faceConfig[t][face]) return true;
        }
        return false;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        for (int t = 0; t < NUM_TYPES; t++) {
            compound.setInteger("SC_F" + t, getFaceBits(t));
        }
        compound.setInteger("SC_Eject", getEjectBits());
        compound.setInteger("SC_Pull", getAutoPullBits());
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        for (int t = 0; t < NUM_TYPES; t++) {
            setFaceBits(t, compound.getInteger("SC_F" + t));
        }
        setEjectBits(compound.getInteger("SC_Eject"));
        setAutoPullBits(compound.getInteger("SC_Pull"));
    }

    /** Set sensible defaults: energy=all faces, food=top, water=top, output=bottom. */
    public void setDefaults() {
        // Energy: all faces accept
        for (int f = 0; f < NUM_FACES; f++) {
            faceConfig[TYPE_ENERGY][f] = true;
        }
        // Food: top (UP=1)
        faceConfig[TYPE_FOOD][1] = true;
        // Water: top (UP=1)
        faceConfig[TYPE_WATER][1] = true;
        // Output: bottom (DOWN=0)
        faceConfig[TYPE_OUTPUT][0] = true;
    }
}
