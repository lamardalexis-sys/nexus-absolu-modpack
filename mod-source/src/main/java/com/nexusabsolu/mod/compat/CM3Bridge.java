package com.nexusabsolu.mod.compat;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Reflection-based bridge to Compact Machines 3 internals.
 *
 * This class lets us manipulate CM3 room bindings without having CM3
 * on our compile classpath. All methods return null or false on failure
 * so callers can gracefully degrade if CM3 is absent or its API changed.
 *
 * NOTE: This version of CM3 uses the LEGACY COORDS SCHEME:
 * - TileEntityMachine has a 'coords' int field (room id) instead of id+roomPos
 * - Room positions are computed: NW corner = (1024*coords, 40, 0) in DIM 144
 * - addMachinePosition has 4 params: (int, BlockPos, int, EnumMachineSize)
 * - getIdForPos / getMachineRoomPosition methods don't exist - we compute them
 *
 * Core flow for "moving" a CM room's physical entrance to a new block:
 *   int roomId = getIdForPos(world, playerPos);     // floorDiv(x, 1024) in DIM 144
 *   Block machine = getMachineBlock();
 *   world.setBlockState(newPos, machine.getStateFromMeta(SIZE_LARGE));
 *   TileEntity newTE = world.getTileEntity(newPos);
 *   setMachineRoomId(newTE, roomId);                // writes coords + initialized
 *   addMachinePosition(roomId, newPos, newDim);     // updates WSDM.machinePositions
 *
 * After this, the PSD will direct the player to newPos when exiting the
 * room, instead of the original block position.
 *
 * See REFERENCE_CM3_IE.md and logs/latest.log for discovery history.
 */
public class CM3Bridge {

    // Cached reflection handles (null until first successful lookup)
    private static Class<?> classWSDM;           // WorldSavedDataMachines
    private static Class<?> classTEMachine;      // TileEntityMachine
    private static Class<?> classEnumSize;       // EnumMachineSize

    private static Field fieldWSDMInstance;      // public static WSDM.INSTANCE
    private static Method methodGetInstance;     // WSDM.getInstance() (rare fallback)
    private static Method methodAddPos;          // WSDM.addMachinePosition(...)

    private static Field fieldCoords;            // TEMachine.coords (legacy room id)
    private static Field fieldInitialized;       // TEMachine.initialized
    private static Object enumSizeLarge;         // EnumMachineSize.LARGE constant

    private static boolean initAttempted = false;
    private static boolean initSuccess = false;
    private static String lastFailureReason = null;

    /** Returns the last diagnostic message (why init failed, or why the
     *  last operation failed). Null if everything is fine. */
    public static String getLastFailureReason() {
        if (!initAttempted) init();
        return lastFailureReason;
    }

    /**
     * Returns true if the CM3 bridge has been successfully initialized,
     * i.e. all required classes and methods are available.
     */
    public static boolean isAvailable() {
        if (!initAttempted) init();
        return initSuccess;
    }

    /** Lazy initialization of all reflection handles. */
    private static void init() {
        initAttempted = true;
        FMLLog.log.info("[CM3Bridge] === Initializing CM3 reflection bridge (legacy coords scheme) ===");
        try {
            FMLLog.log.info("[CM3Bridge] Looking up WorldSavedDataMachines class...");
            classWSDM = Class.forName(
                "org.dave.compactmachines3.world.WorldSavedDataMachines");
            FMLLog.log.info("[CM3Bridge]   -> found: " + classWSDM.getName());

            FMLLog.log.info("[CM3Bridge] Looking up TileEntityMachine class...");
            classTEMachine = Class.forName(
                "org.dave.compactmachines3.tile.TileEntityMachine");
            FMLLog.log.info("[CM3Bridge]   -> found: " + classTEMachine.getName());

            FMLLog.log.info("[CM3Bridge] Looking up EnumMachineSize class...");
            classEnumSize = Class.forName(
                "org.dave.compactmachines3.reference.EnumMachineSize");
            Object[] constants = classEnumSize.getEnumConstants();
            FMLLog.log.info("[CM3Bridge]   -> found enum with " + constants.length + " values");
            for (int i = 0; i < constants.length; i++) {
                FMLLog.log.info("[CM3Bridge]     [" + i + "] = " + constants[i]);
            }
            // LARGE = 9x9 interior, ordinal 3
            if (constants.length > 3) {
                enumSizeLarge = constants[3];
                FMLLog.log.info("[CM3Bridge]   using [3] = " + enumSizeLarge + " as LARGE");
            }

            // --- WSDM instance access: static INSTANCE field ---
            FMLLog.log.info("[CM3Bridge] Looking for WSDM static INSTANCE field...");
            for (Field f : classWSDM.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (Modifier.isStatic(mod)
                    && classWSDM.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    fieldWSDMInstance = f;
                    FMLLog.log.info("[CM3Bridge]   -> USING static field: " + f.getName());
                    break;
                }
            }
            // Fallback getInstance() if INSTANCE field absent
            if (fieldWSDMInstance == null) {
                for (Method m : classWSDM.getMethods()) {
                    if (m.getName().equals("getInstance")
                        && Modifier.isStatic(m.getModifiers())) {
                        methodGetInstance = m;
                        FMLLog.log.info("[CM3Bridge]   fallback getInstance: " + m);
                        break;
                    }
                }
            }

            // --- addMachinePosition: handle both 3-arg and 4-arg variants ---
            FMLLog.log.info("[CM3Bridge] Looking up WSDM.addMachinePosition(...)...");
            Method bestAddPos = null;
            for (Method m : classWSDM.getMethods()) {
                if (m.getName().equals("addMachinePosition")) {
                    FMLLog.log.info("[CM3Bridge]   candidate: " + m
                        + " (params=" + m.getParameterCount() + ")");
                    // Prefer the 4-arg variant (with EnumMachineSize) if found
                    if (m.getParameterCount() == 4) {
                        bestAddPos = m;
                    } else if (bestAddPos == null) {
                        bestAddPos = m;
                    }
                }
            }
            methodAddPos = bestAddPos;
            if (methodAddPos != null) {
                FMLLog.log.info("[CM3Bridge]   -> using: " + methodAddPos);
            }

            // --- TileEntityMachine fields: coords (legacy id) + initialized ---
            FMLLog.log.info("[CM3Bridge] Looking up TileEntityMachine fields...");
            for (Field f : classTEMachine.getDeclaredFields()) {
                FMLLog.log.info("[CM3Bridge]   field: " + f.getName()
                    + " type=" + f.getType().getSimpleName());
                if (f.getName().equals("coords")) {
                    f.setAccessible(true);
                    fieldCoords = f;
                }
                if (f.getName().equals("initialized")) {
                    f.setAccessible(true);
                    fieldInitialized = f;
                }
            }

            FMLLog.log.info("[CM3Bridge] Summary: wsdm=" + (classWSDM != null)
                + " te=" + (classTEMachine != null)
                + " enumSize=" + (enumSizeLarge != null)
                + " INSTANCE=" + (fieldWSDMInstance != null)
                + " addPos=" + (methodAddPos != null)
                + " coords=" + (fieldCoords != null)
                + " initialized=" + (fieldInitialized != null));

            // Minimum requirements
            if (fieldWSDMInstance == null && methodGetInstance == null) {
                lastFailureReason = "WSDM.INSTANCE introuvable";
            } else if (methodAddPos == null) {
                lastFailureReason = "WSDM.addMachinePosition introuvable";
            } else if (fieldCoords == null) {
                lastFailureReason = "TileEntityMachine.coords field introuvable";
            } else if (enumSizeLarge == null) {
                lastFailureReason = "EnumMachineSize.LARGE introuvable";
            } else {
                initSuccess = true;
                lastFailureReason = null;
                FMLLog.log.info("[CM3Bridge] === INITIALIZED SUCCESSFULLY (legacy scheme) ===");
                return;
            }
            FMLLog.log.warn("[CM3Bridge] FAIL: " + lastFailureReason);

        } catch (ClassNotFoundException e) {
            lastFailureReason = "Compact Machines 3 absent: " + e.getMessage();
            FMLLog.log.warn("[CM3Bridge] FAIL: " + lastFailureReason);
            initSuccess = false;
        } catch (Throwable t) {
            lastFailureReason = "Erreur init: " + t.getClass().getSimpleName() + " " + t.getMessage();
            FMLLog.log.warn("[CM3Bridge] FAIL: " + lastFailureReason, t);
            initSuccess = false;
        }
    }

    /**
     * Look up the CM room id that contains the given world position.
     *
     * In CM3's legacy coords scheme, rooms are spaced 1024 blocks apart
     * on the X axis in DIM 144, with room N's NW corner at (1024*N, 40, 0).
     * Max room size is 13 blocks, so we can safely identify the containing
     * room via integer floor-division of the X coordinate.
     *
     * Returns -1 if the position is not in DIM 144.
     */
    public static int getIdForPos(World world, BlockPos pos) {
        if (!isAvailable()) {
            FMLLog.log.info("[CM3Bridge] getIdForPos: bridge unavailable (reason: " + lastFailureReason + ")");
            return -1;
        }
        int dim = world.provider.getDimension();
        if (dim != 144) {
            FMLLog.log.info("[CM3Bridge] getIdForPos: not in DIM 144 (dim=" + dim + ")");
            return -1;
        }
        int id = Math.floorDiv(pos.getX(), 1024);
        FMLLog.log.info("[CM3Bridge] getIdForPos: computed id=" + id + " from x=" + pos.getX());
        return id;
    }

    /** Get the WSDM singleton instance. Prefers the static INSTANCE field,
     *  falls back to getInstance() method, and as last resort force-loads
     *  DIM 144 to trigger constructor population of INSTANCE. */
    private static Object invokeGetInstance() {
        try {
            // Primary: read the static INSTANCE field
            if (fieldWSDMInstance != null) {
                Object inst = fieldWSDMInstance.get(null);
                if (inst != null) {
                    return inst;
                }
                FMLLog.log.info("[CM3Bridge] INSTANCE field is null - force-loading DIM 144");
                // Force-load DIM 144 so its WorldSavedData constructor runs
                net.minecraft.server.MinecraftServer server =
                    net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
                if (server != null) {
                    try {
                        net.minecraftforge.common.DimensionManager.initDimension(144);
                    } catch (Throwable t) {
                        FMLLog.log.warn("[CM3Bridge] initDimension(144) threw: " + t.getMessage());
                    }
                    // Also try touching the dim144 world's map storage
                    World dim144 = server.getWorld(144);
                    if (dim144 != null) {
                        dim144.getMapStorage();  // ensures storage is loaded
                    }
                }
                inst = fieldWSDMInstance.get(null);
                if (inst != null) {
                    FMLLog.log.info("[CM3Bridge] INSTANCE populated after force-load");
                    return inst;
                }
                FMLLog.log.warn("[CM3Bridge] INSTANCE still null after force-load");
            }

            // Fallback: call getInstance() if it exists
            if (methodGetInstance != null) {
                int paramCount = methodGetInstance.getParameterCount();
                if (paramCount == 0) {
                    return methodGetInstance.invoke(null);
                } else if (paramCount == 1) {
                    net.minecraft.server.MinecraftServer server =
                        net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
                    if (server == null) return null;
                    World overworld = server.getWorld(0);
                    return methodGetInstance.invoke(null, overworld);
                }
            }
        } catch (Throwable t) {
            lastFailureReason = "invokeGetInstance exception: " + t.getClass().getSimpleName() + " " + t.getMessage();
            FMLLog.log.warn("[CM3Bridge] " + lastFailureReason, t);
        }
        return null;
    }

    /**
     * Get the room position (NW corner in DIM 144) for the given room id.
     * Computed directly from the legacy coords scheme: (1024*id, 40, 0).
     */
    public static BlockPos getRoomPosition(int roomId) {
        return new BlockPos(1024 * roomId, 40, 0);
    }

    /**
     * Update the map {roomId -> (dimension, blockPos, size)} so that the
     * PSD will direct the player to the new position when exiting the room.
     * Handles both 3-arg and 4-arg addMachinePosition signatures.
     */
    public static boolean addMachinePosition(int roomId, BlockPos pos, int dimension) {
        if (!isAvailable()) return false;
        if (methodAddPos == null) {
            lastFailureReason = "addMachinePosition indisponible";
            return false;
        }
        try {
            Object wsdm = invokeGetInstance();
            if (wsdm == null) {
                lastFailureReason = "WSDM instance introuvable";
                FMLLog.log.warn("[CM3Bridge] " + lastFailureReason);
                return false;
            }
            int paramCount = methodAddPos.getParameterCount();
            if (paramCount == 4) {
                // (int id, BlockPos pos, int dim, EnumMachineSize size)
                if (enumSizeLarge == null) {
                    lastFailureReason = "EnumMachineSize.LARGE absent - addMachinePosition impossible";
                    return false;
                }
                methodAddPos.invoke(wsdm, roomId, pos, dimension, enumSizeLarge);
                FMLLog.log.info("[CM3Bridge] addMachinePosition(4-arg)(" + roomId
                    + ", " + pos + ", dim=" + dimension + ", LARGE) OK");
            } else if (paramCount == 3) {
                methodAddPos.invoke(wsdm, roomId, pos, dimension);
                FMLLog.log.info("[CM3Bridge] addMachinePosition(3-arg)(" + roomId
                    + ", " + pos + ", dim=" + dimension + ") OK");
            } else {
                lastFailureReason = "addMachinePosition a " + paramCount + " params (attendu 3 ou 4)";
                return false;
            }
            return true;
        } catch (Throwable t) {
            lastFailureReason = "addMachinePosition exception: " + t.getClass().getSimpleName() + " " + t.getMessage();
            FMLLog.log.warn("[CM3Bridge] " + lastFailureReason, t);
            return false;
        }
    }

    /**
     * Link a freshly placed TileEntityMachine to an existing room by
     * setting its 'coords' field (legacy scheme) and marking it initialized
     * so CM3 doesn't try to generate a new room on first interact.
     */
    public static boolean setMachineRoomId(TileEntity te, int roomId) {
        if (!isAvailable() || te == null) return false;
        if (!classTEMachine.isInstance(te)) return false;
        try {
            if (fieldCoords != null) {
                fieldCoords.setInt(te, roomId);
                FMLLog.log.info("[CM3Bridge] set coords=" + roomId + " on TE");
            }
            if (fieldInitialized != null) {
                fieldInitialized.setBoolean(te, true);
                FMLLog.log.info("[CM3Bridge] set initialized=true on TE");
            }
            return true;
        } catch (IllegalAccessException e) {
            lastFailureReason = "setMachineRoomId failed: " + e.getMessage();
            FMLLog.log.warn("[CM3Bridge] " + lastFailureReason);
            return false;
        }
    }

    /**
     * Get the compactmachines3:machine Block instance.
     * Used for placing a new CM block in the world.
     */
    public static Block getMachineBlock() {
        return Block.getBlockFromName("compactmachines3:machine");
    }

    /**
     * Place a new CM block at the given position, linked to an existing
     * room. The block will point to the same interior as the original CM.
     *
     * @param world     the world to place in
     * @param pos       block position
     * @param size      machine size meta (0=tiny, 1=small, 2=normal, 3=large,
     *                                      4=giant, 5=maximum)
     * @param roomId    existing room id to link to
     * @return true on success
     */
    public static boolean placeLinkedMachine(World world, BlockPos pos,
                                              int size, int roomId) {
        return placeLinkedMachine(world, pos, size, roomId, true);
    }

    /**
     * Variante avec contrôle sur la mise à jour de l'exit target PSD.
     *
     * @param updateExitTarget si true (defaut), appelle WSDM.addMachinePosition
     *   pour rediriger la sortie PSD de la salle vers ce nouveau bloc.
     *   Si false, le nouveau bloc fonctionne comme entrée mais la salle
     *   garde son exit target original (utile pour matryoshka: une fois
     *   echappé en overworld via Cle de Liberte, on ne veut PAS écraser
     *   le retour vers la CM parente).
     */
    public static boolean placeLinkedMachine(World world, BlockPos pos,
                                              int size, int roomId,
                                              boolean updateExitTarget) {
        FMLLog.log.info("[CM3Bridge] placeLinkedMachine: roomId=" + roomId
            + " pos=" + pos + " dim=" + world.provider.getDimension()
            + " size=" + size + " updateExitTarget=" + updateExitTarget);

        if (!isAvailable()) {
            FMLLog.log.warn("[CM3Bridge] placeLinkedMachine: bridge unavailable");
            return false;
        }

        Block machineBlock = getMachineBlock();
        FMLLog.log.info("[CM3Bridge]   machineBlock lookup: "
            + (machineBlock == null ? "null" : machineBlock.getRegistryName()));
        if (machineBlock == null || machineBlock == net.minecraft.init.Blocks.AIR) {
            lastFailureReason = "compactmachines3:machine block introuvable";
            FMLLog.log.warn("[CM3Bridge] " + lastFailureReason);
            return false;
        }

        // In the legacy scheme, the room position is computed, not stored.
        BlockPos roomPos = getRoomPosition(roomId);
        FMLLog.log.info("[CM3Bridge]   computed roomPos for id=" + roomId + ": " + roomPos);

        // Place the block with the appropriate meta for the size
        IBlockState state = machineBlock.getStateFromMeta(size);
        FMLLog.log.info("[CM3Bridge]   placing " + machineBlock.getRegistryName()
            + " meta=" + size + " at " + pos);
        world.setBlockState(pos, state, 3);

        // Grab the fresh TileEntity and link it to the existing room
        TileEntity te = world.getTileEntity(pos);
        FMLLog.log.info("[CM3Bridge]   fresh TE: "
            + (te == null ? "null" : te.getClass().getName()));
        if (te == null) {
            lastFailureReason = "TileEntity non cree apres setBlockState";
            FMLLog.log.warn("[CM3Bridge] " + lastFailureReason);
            return false;
        }
        if (!classTEMachine.isInstance(te)) {
            lastFailureReason = "TileEntity n'est pas une TileEntityMachine: " + te.getClass().getName();
            FMLLog.log.warn("[CM3Bridge] " + lastFailureReason);
            return false;
        }

        // Set the TE's coords (legacy id) and mark it initialized so CM3
        // doesn't generate a new room on first interact
        boolean teOk = setMachineRoomId(te, roomId);

        // Update the WSDM map to rebind the PSD exit target — only if requested.
        // For matryoshka preservation (Cle de Liberte escape), we want to KEEP
        // the original parent CM as the exit target, so we skip this call.
        boolean mapOk;
        if (updateExitTarget) {
            mapOk = addMachinePosition(roomId, pos, world.provider.getDimension());
            FMLLog.log.info("[CM3Bridge]   link results: teCoords=" + teOk + " wsdmMap=" + mapOk);
        } else {
            mapOk = true; // skipped intentionally — original exit target preserved
            FMLLog.log.info("[CM3Bridge]   link results: teCoords=" + teOk
                + " wsdmMap=SKIPPED (matryoshka preservation)");
        }

        te.markDirty();
        world.notifyBlockUpdate(pos, state, state, 3);

        if (teOk && mapOk) {
            FMLLog.log.info("[CM3Bridge] SUCCESS: Linked CM block at " + pos
                + " (dim " + world.provider.getDimension() + ") to room " + roomId);
            lastFailureReason = null;
            return true;
        } else {
            lastFailureReason = "Link partiel: teCoords=" + teOk + " wsdmMap=" + mapOk;
            FMLLog.log.warn("[CM3Bridge] " + lastFailureReason);
            return false;
        }
    }

    // Machine size constants (mirror EnumMachineSize)
    public static final int SIZE_TINY    = 0;  // 3x3 interior
    public static final int SIZE_SMALL   = 1;  // 5x5
    public static final int SIZE_NORMAL  = 2;  // 7x7
    public static final int SIZE_LARGE   = 3;  // 9x9
    public static final int SIZE_GIANT   = 4;  // 11x11
    public static final int SIZE_MAXIMUM = 5;  // 13x13
}
