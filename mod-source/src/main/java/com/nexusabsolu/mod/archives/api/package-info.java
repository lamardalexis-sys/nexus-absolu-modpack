/**
 * Public API for the Voss Archives storage system.
 *
 * <p>This package contains only interfaces and constants. Implementations
 * live in {@code com.nexusabsolu.mod.archives.apiimpl}. Blocks, TileEntities,
 * items, GUIs, etc. live in their respective sibling packages.
 *
 * <p><b>Architecture overview</b> (inspired by Refined Storage, simplified) :
 *
 * <pre>
 *   ┌─────────────────────────────────────────────┐
 *   │         Archives Voss Multiblock            │
 *   │                                             │
 *   │  Controller (INexusNetwork impl) ─┐         │
 *   │                                   │         │
 *   │  Frame blocks                     ▼         │
 *   │  Thermal core blocks       ┌──────────────┐ │
 *   │  Input port / Output port  │ Node list    │ │
 *   │                            └──────────────┘ │
 *   └─────────────────────────────────────────────┘
 *                          ▲
 *                          │ (cables)
 *                          ▼
 *   ┌────────────┐  ┌────────────┐  ┌────────────┐
 *   │   Drive    │  │  Terminal  │  │ Interface  │  ...
 *   │ 10 disques │  │  Stockage  │  │   Craft    │
 *   └────────────┘  └────────────┘  └────────────┘
 *       (all implement INexusNetworkNode)
 * </pre>
 *
 * <p><b>Entry points</b> :
 * <ul>
 *   <li>{@link com.nexusabsolu.mod.archives.api.INexusNetwork} — the network
 *       itself (one per controller)</li>
 *   <li>{@link com.nexusabsolu.mod.archives.api.INexusNetworkNode} — any
 *       block that participates in a network</li>
 * </ul>
 *
 * @since v1.0.301 (Archives Voss Sprint 0)
 */
package com.nexusabsolu.mod.archives.api;
