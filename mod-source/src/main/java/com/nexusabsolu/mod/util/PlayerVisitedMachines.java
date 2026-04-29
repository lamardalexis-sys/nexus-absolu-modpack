package com.nexusabsolu.mod.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;

/**
 * Track des Compact Machines visitees par un joueur.
 *
 * Les ids sont stockes dans le tag PERSISTED du joueur, donc ils survivent
 * a la mort du joueur (Forge persiste ce tag automatiquement entre les
 * respawns).
 *
 * Format NBT (sous EntityPlayer.PERSISTED_NBT_TAG):
 *   "nexusabsolu:visited_machines" -> NBTTagIntArray (ordre = ordre de visite)
 *
 * V1: on stocke uniquement les ids. La taille de salle n'est pas stockee
 * (pas trivial a recuperer en DIM 144 sans le bloc CM source). La GUI
 * affiche "Salle #ID" -- l'ordre d'apparition correspond a l'ordre de
 * visite donc le joueur retrouve facilement sa progression (5x5 -> 7x7 -> 9x9).
 */
public final class PlayerVisitedMachines {

    public static final String NBT_KEY = "nexusabsolu:visited_machines";

    private PlayerVisitedMachines() {}

    /** Retourne le sous-compound PERSISTED, en le creant si absent. */
    private static NBTTagCompound getPersistedTag(EntityPlayer player) {
        NBTTagCompound entityData = player.getEntityData();
        NBTTagCompound persisted;
        if (entityData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            persisted = entityData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        } else {
            persisted = new NBTTagCompound();
            entityData.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        }
        return persisted;
    }

    /** Retourne les ids visites par ce joueur, dans l'ordre de visite.
     *  Tableau vide si aucune visite. */
    public static int[] getVisited(EntityPlayer player) {
        NBTTagCompound persisted = getPersistedTag(player);
        if (!persisted.hasKey(NBT_KEY, 11)) { // 11 = NBTTagIntArray
            return new int[0];
        }
        return persisted.getIntArray(NBT_KEY).clone();
    }

    /** Retourne true si l'id est deja dans la liste visitee. */
    public static boolean hasVisited(EntityPlayer player, int id) {
        int[] arr = getVisited(player);
        for (int v : arr) {
            if (v == id) return true;
        }
        return false;
    }

    /** Ajoute un id a la liste s'il n'y est pas deja. Retourne true si
     *  l'id a ete ajoute, false s'il etait deja present. */
    public static boolean addVisited(EntityPlayer player, int id) {
        if (id < 0) return false;
        int[] current = getVisited(player);
        for (int v : current) {
            if (v == id) return false;
        }
        int[] next = new int[current.length + 1];
        System.arraycopy(current, 0, next, 0, current.length);
        next[current.length] = id;

        NBTTagCompound persisted = getPersistedTag(player);
        persisted.setTag(NBT_KEY, new NBTTagIntArray(next));
        return true;
    }
}
