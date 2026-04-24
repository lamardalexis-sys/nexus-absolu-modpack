package com.nexusabsolu.mod.archives.api;

import java.util.Collection;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Contract for a Voss Archives Network - the logical network tied to a
 * Controller block in a valid Archives Voss multiblock.
 *
 * <p>A network owns one Controller and N nodes (cables, drives, terminals,
 * IO ports, craft interfaces). The Controller is the "brain" : it holds the
 * energy storage, orchestrates item movement, and owns the network identity.
 *
 * <p><b>Design inspiration</b> : RS {@code INetwork} (significantly simplified
 * - no crafting manager interface here, no security, no fluid support at MVP).
 *
 * <p><b>Single-controller invariant</b> : if a scan discovers two controllers
 * in the same connected component, BOTH controllers enter "conflict" state
 * (structureFormed=false) and log a warning. One of the two must be removed
 * by the player.
 *
 * @since v1.0.301 (Archives Voss Sprint 0)
 */
public interface INexusNetwork {

    /**
     * @return a unique identifier for this network (derived from controller
     *   BlockPos + dimension + world seed, stable across reloads).
     */
    java.util.UUID getId();

    /**
     * @return the World containing the controller.
     */
    World getWorld();

    /**
     * @return position of the controller block (the center of the multiblock
     *   upper layer).
     */
    BlockPos getControllerPos();

    /**
     * @return all nodes currently part of this network. Never null, may be
     *   empty if only the controller exists. The returned collection is
     *   unmodifiable; to add/remove, use internal network methods.
     */
    Collection<INexusNetworkNode> getNodes();

    /**
     * @return the total RF currently stored in the controller's internal
     *   energy storage.
     */
    int getEnergyStored();

    /**
     * @return max RF capacity of the controller's internal energy storage.
     */
    int getMaxEnergyStored();

    /**
     * @return the sum of {@link INexusNetworkNode#getEnergyUsage()} over all
     *   connected nodes. Controller will try to drain this amount each tick.
     */
    int getTotalEnergyUsage();

    /**
     * @return true if the controller can currently afford the total energy
     *   usage. When false, the network enters "low power" mode and nodes
     *   should stop their activity until energy is restored.
     */
    boolean canRun();

    /**
     * Called each server tick by the controller. Updates energy accounting,
     * propagates state to nodes if needed.
     */
    void tick();

    /**
     * Adds a node to the network. Called when a cable scan discovers a new
     * node. Idempotent : re-adding an already-present node is a no-op.
     *
     * @param node the node to add, must be non-null
     */
    void addNode(INexusNetworkNode node);

    /**
     * Removes a node from the network. Called when a node is broken, its
     * chunk unloaded, or it becomes unreachable. Idempotent.
     *
     * @param node the node to remove
     */
    void removeNode(INexusNetworkNode node);

    /**
     * Forces a complete rescan of the network from the controller. Expensive,
     * should only be called on structural changes (new cable placed, cable
     * removed). Uses BFS with chunk-load safety (nodes in unloaded chunks
     * are skipped, preventing the RS #3468 bug).
     */
    void rescan();
}
