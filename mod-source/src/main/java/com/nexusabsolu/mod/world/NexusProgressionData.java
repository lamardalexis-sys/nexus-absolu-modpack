package com.nexusabsolu.mod.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

/**
 * Progression persistante a l'echelle du monde.
 *
 * Stocke des flags globaux declenchant des changements de gameplay
 * apres certains jalons (ex: passage de l'Age 1 a l'Age 2).
 *
 * Declencheurs:
 *  - age2Reached: set a true la premiere fois qu'un joueur passe
 *    de la dimension Compact Machines (144) vers l'Overworld (0).
 *    Declenche le gating des drops auto-scavenger (plus de drops
 *    "banals" type iron/copper/coal etc., seuls restent les drops
 *    CM-exclusifs Compose A, B, Grains of Infinity).
 *
 * Flag monde-global: une fois pose, s'applique a tous les joueurs.
 * Pose pour toujours (pas de rollback).
 */
public class NexusProgressionData extends WorldSavedData {

    private static final String NAME = "nexus_progression";
    private boolean age2Reached = false;

    public NexusProgressionData() {
        super(NAME);
    }

    public NexusProgressionData(String name) {
        super(name);
    }

    /** Charge (ou cree) l'instance depuis le world storage de l'overworld. */
    public static NexusProgressionData get(World world) {
        // Always use the overworld's storage - data is world-scoped, not dimension-scoped
        MapStorage storage = world.getMinecraftServer()
            .getWorld(0).getPerWorldStorage();
        NexusProgressionData data = (NexusProgressionData)
            storage.getOrLoadData(NexusProgressionData.class, NAME);
        if (data == null) {
            data = new NexusProgressionData();
            storage.setData(NAME, data);
        }
        return data;
    }

    /** Helper statique: le joueur a-t-il atteint l'Age 2 ? */
    public static boolean isAge2Reached(World world) {
        if (world == null || world.isRemote) return false;
        if (world.getMinecraftServer() == null) return false;
        return get(world).age2Reached;
    }

    /** Marque l'Age 2 comme atteint. Persistant, non reversible. */
    public static void markAge2Reached(World world) {
        if (world == null || world.isRemote) return;
        if (world.getMinecraftServer() == null) return;
        NexusProgressionData data = get(world);
        if (!data.age2Reached) {
            data.age2Reached = true;
            data.markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.age2Reached = nbt.getBoolean("age2Reached");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("age2Reached", this.age2Reached);
        return nbt;
    }
}
