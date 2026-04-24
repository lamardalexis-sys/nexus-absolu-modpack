package com.nexusabsolu.mod.archives.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Contract for any block that can be part of a Voss Archives Network.
 *
 * <p>Every block that participates in a network (cables, drives, terminals,
 * IO ports, craft interfaces, etc.) implements this interface via its
 * TileEntity. The Controller uses this contract to scan, track, and tick
 * all nodes in the network.
 *
 * <p><b>Design inspiration</b> : RS {@code INetworkNode} (simplified for our
 * smaller scope - no security, no async crafting manager, no wireless).
 *
 * <p><b>Lifecycle</b> :
 * <ul>
 *   <li>Block placed -&gt; {@link #onConnected(INexusNetwork)} called when
 *       adjacent cable/controller discovers this node</li>
 *   <li>Block update -&gt; Controller may re-scan, {@link #onDisconnected()}
 *       called if node is no longer reachable</li>
 *   <li>Block broken -&gt; {@link #onDisconnected()} called before removal</li>
 *   <li>Chunk unloaded -&gt; {@link #onDisconnected()} called defensively
 *       (prevents "ghost node" bug observed in RS issue #3468)</li>
 * </ul>
 *
 * <p><b>RF consumption</b> : each node reports its passive RF drain per tick
 * via {@link #getEnergyUsage()}. The Controller sums these and extracts from
 * its own energy storage each tick. If the controller can't afford the total,
 * the network enters "low power" mode (all nodes inactive).
 *
 * @since v1.0.301 (Archives Voss Sprint 0)
 */
public interface INexusNetworkNode {

    /**
     * @return the World this node lives in (same as TileEntity.getWorld()).
     *   May be null during very early init; callers should handle gracefully.
     */
    World getNodeWorld();

    /**
     * @return the block position of this node.
     */
    BlockPos getNodePos();

    /**
     * Called by the network when this node is successfully attached.
     * Use to cache the network reference, update visuals, etc.
     *
     * @param network the network this node joined, never null
     */
    void onConnected(INexusNetwork network);

    /**
     * Called when this node is removed from the network, whether by block
     * break, chunk unload, or network invalidation.
     *
     * <p>Implementations MUST clear their cached network reference here to
     * prevent memory leaks and "ghost node" behavior.
     */
    void onDisconnected();

    /**
     * @return the network this node is currently part of, or null if not
     *   connected to any network (between onDisconnected and onConnected).
     */
    INexusNetwork getNetwork();

    /**
     * @return RF drained per tick by this node when active. Used by Controller
     *   to compute total network power draw. Return 0 for passive nodes (cables).
     */
    int getEnergyUsage();

    /**
     * @return true if this node is in "low power" mode (network can't afford
     *   its energy usage). Useful for rendering/GUI feedback.
     */
    default boolean isLowPower() {
        INexusNetwork net = getNetwork();
        return net != null && !net.canRun();
    }
}
