package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.manifold.PlayerMemorySnapshot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Storage cote client du PlayerMemorySnapshot recu via PacketSyncMemorySnapshot.
 *
 * Stocke le snapshot le plus recent envoye par le serveur. Le ManifoldOverlayHandler
 * lit ce snapshot pendant la NDE pour afficher les flashbacks (icones items + noms
 * mobs/biomes en surimpression defilante).
 *
 * Le snapshot peut etre null si le client n'a jamais recu de paquet de sync
 * (par exemple : pas encore lance le trip Cartouche). Dans ce cas le rendu
 * des flashbacks est skippe et la NDE reste en mode "generique".
 *
 * @since 1.0.362 (Sprint 2 etape 2 phase B)
 */
@SideOnly(Side.CLIENT)
public class ManifoldClientMemory {

    private static PlayerMemorySnapshot snapshot = null;
    private static long snapshotReceivedTick = 0L;

    /** Set par le handler du PacketSyncMemorySnapshot. */
    public static void setSnapshot(PlayerMemorySnapshot snap) {
        snapshot = snap;
        snapshotReceivedTick = System.currentTimeMillis();
    }

    /** @return le snapshot le plus recent ou null si jamais recu. */
    public static PlayerMemorySnapshot getSnapshot() {
        return snapshot;
    }

    /** @return true si on a un snapshot utilisable. */
    public static boolean hasSnapshot() {
        return snapshot != null;
    }

    /** Vide le snapshot (par exemple a la fin du trip pour libere memoire). */
    public static void clear() {
        snapshot = null;
        snapshotReceivedTick = 0L;
    }

    /** @return ticks ecoules depuis la reception du snapshot (pour debug). */
    public static long getAgeMillis() {
        if (snapshotReceivedTick == 0L) return -1L;
        return System.currentTimeMillis() - snapshotReceivedTick;
    }
}
