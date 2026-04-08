package com.nexusabsolu.mod.compat;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reflection-based bridge to Compact Machines 3 internals.
 *
 * This class lets us manipulate CM3 room bindings without having CM3
 * on our compile classpath. All methods return null or false on failure
 * so callers can gracefully degrade if CM3 is absent or its API changed.
 *
 * Core flow for "moving" a CM room's physical entrance from one block
 * to another:
 *   int roomId = getIdForPos(world, playerPos);
 *   BlockPos roomPos = getRoomPosition(roomId);
 *   Block machine = getMachineBlock();
 *   world.setBlockState(newPos, machine.getStateFromMeta(LARGE));
 *   TileEntity newTE = world.getTileEntity(newPos);
 *   setMachineId(newTE, roomId);
 *   setRoomPos(newTE, roomPos);
 *   addMachinePosition(roomId, newPos, newDimension);
 *
 * After this, the PSD will direct the player to newPos when exiting the
 * room, instead of the original block position.
 *
 * See REFERENCE_CM3_IE.md sections 1.2, 1.3, 1.7 for API details.
 */
public class CM3Bridge {

    // Cached reflection handles (null until first successful lookup)
    private static Class<?> classWSDM;           // WorldSavedDataMachines
    private static Class<?> classStructTools;    // StructureTools
    private static Class<?> classTEMachine;      // TileEntityMachine

    private static Method methodGetInstance;     // WSDM.getInstance()
    private static Method methodAddPos;          // WSDM.addMachinePosition(int, BlockPos, int)
    private static Method methodGetRoomPos;      // WSDM.getMachineRoomPosition(int)
    private static Method methodGetIdForPos;     // StructureTools.getIdForPos(World, BlockPos) OR (BlockPos)

    private static Field fieldMachineId;         // TEMachine.id
    private static Field fieldRoomPos;           // TEMachine.roomPos

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
        FMLLog.log.info("[CM3Bridge] === Initializing CM3 reflection bridge ===");
        try {
            FMLLog.log.info("[CM3Bridge] Looking up WorldSavedDataMachines class...");
            classWSDM = Class.forName(
                "org.dave.compactmachines3.world.WorldSavedDataMachines");
            FMLLog.log.info("[CM3Bridge]   -> found: " + classWSDM.getName());

            FMLLog.log.info("[CM3Bridge] Looking up StructureTools class...");
            classStructTools = Class.forName(
                "org.dave.compactmachines3.world.tools.StructureTools");
            FMLLog.log.info("[CM3Bridge]   -> found: " + classStructTools.getName());

            FMLLog.log.info("[CM3Bridge] Looking up TileEntityMachine class...");
            classTEMachine = Class.forName(
                "org.dave.compactmachines3.tile.TileEntityMachine");
            FMLLog.log.info("[CM3Bridge]   -> found: " + classTEMachine.getName());

            FMLLog.log.info("[CM3Bridge] Looking up WSDM.getInstance()...");
            // Try multiple signatures for getInstance
            for (Method m : classWSDM.getMethods()) {
                if (m.getName().equals("getInstance")) {
                    methodGetInstance = m;
                    FMLLog.log.info("[CM3Bridge]   -> found: " + m
                        + " (params=" + m.getParameterCount() + ")");
                    break;
                }
            }
            if (methodGetInstance == null) {
                lastFailureReason = "WorldSavedDataMachines.getInstance() introuvable";
                FMLLog.log.warn("[CM3Bridge] FAIL: " + lastFailureReason);
                initSuccess = false;
                return;
            }

            FMLLog.log.info("[CM3Bridge] Looking up WSDM.addMachinePosition(...)...");
            for (Method m : classWSDM.getMethods()) {
                if (m.getName().equals("addMachinePosition")) {
                    methodAddPos = m;
                    FMLLog.log.info("[CM3Bridge]   -> found: " + m);
                    break;
                }
            }

            FMLLog.log.info("[CM3Bridge] Looking up WSDM.getMachineRoomPosition(...)...");
            for (Method m : classWSDM.getMethods()) {
                if (m.getName().equals("getMachineRoomPosition")) {
                    methodGetRoomPos = m;
                    FMLLog.log.info("[CM3Bridge]   -> found: " + m);
                    break;
                }
            }

            FMLLog.log.info("[CM3Bridge] Looking up StructureTools.getIdForPos(...)...");
            for (Method m : classStructTools.getMethods()) {
                if (m.getName().equals("getIdForPos")) {
                    methodGetIdForPos = m;
                    FMLLog.log.info("[CM3Bridge]   -> found: " + m
                        + " (params=" + m.getParameterCount() + ")");
                    break;
                }
            }

            FMLLog.log.info("[CM3Bridge] Looking up TileEntityMachine fields...");
            for (Field f : classTEMachine.getDeclaredFields()) {
                FMLLog.log.info("[CM3Bridge]   field: " + f.getName() + " type=" + f.getType().getSimpleName());
                if (f.getName().equals("id")) {
                    f.setAccessible(true);
                    fieldMachineId = f;
                }
                if (f.getName().equals("roomPos")) {
                    f.setAccessible(true);
                    fieldRoomPos = f;
                }
            }
            // Also check inherited public fields
            if (fieldMachineId == null) {
                for (Field f : classTEMachine.getFields()) {
                    if (f.getName().equals("id")) {
                        f.setAccessible(true);
                        fieldMachineId = f;
                        break;
                    }
                }
            }

            FMLLog.log.info("[CM3Bridge] Summary: wsdm=" + (classWSDM != null)
                + " tools=" + (classStructTools != null)
                + " te=" + (classTEMachine != null)
                + " getInstance=" + (methodGetInstance != null)
                + " addPos=" + (methodAddPos != null)
                + " getRoomPos=" + (methodGetRoomPos != null)
                + " getIdForPos=" + (methodGetIdForPos != null)
                + " fId=" + (fieldMachineId != null)
                + " fRoomPos=" + (fieldRoomPos != null));

            // Minimum requirements
            if (methodGetIdForPos == null) {
                lastFailureReason = "StructureTools.getIdForPos() introuvable";
            } else if (methodAddPos == null) {
                lastFailureReason = "WSDM.addMachinePosition() introuvable";
            } else if (fieldMachineId == null) {
                lastFailureReason = "TileEntityMachine.id field introuvable";
            } else if (fieldRoomPos == null) {
                lastFailureReason = "TileEntityMachine.roomPos field introuvable";
            } else {
                initSuccess = true;
                lastFailureReason = null;
                FMLLog.log.info("[CM3Bridge] === INITIALIZED SUCCESSFULLY ===");
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
     * Returns -1 if the position is not inside any CM room, or if CM3 is
     * unavailable.
     */
    public static int getIdForPos(World world, BlockPos pos) {
        if (!isAvailable()) {
            FMLLog.log.info("[CM3Bridge] getIdForPos: bridge unavailable (reason: " + lastFailureReason + ")");
            return -1;
        }
        try {
            int paramCount = methodGetIdForPos.getParameterCount();
            Class<?>[] paramTypes = methodGetIdForPos.getParameterTypes();
            FMLLog.log.info("[CM3Bridge] getIdForPos: calling " + methodGetIdForPos
                + " with pos=" + pos + " worldDim=" + world.provider.getDimension());

            Object result;
            if (paramCount == 2) {
                // Likely (World, BlockPos) or (BlockPos, World)
                if (paramTypes[0].isAssignableFrom(World.class)) {
                    result = methodGetIdForPos.invoke(null, world, pos);
                } else {
                    result = methodGetIdForPos.invoke(null, pos, world);
                }
            } else if (paramCount == 1) {
                // Just (BlockPos)
                result = methodGetIdForPos.invoke(null, pos);
            } else {
                lastFailureReason = "getIdForPos a " + paramCount + " parametres (attendu 1 ou 2)";
                FMLLog.log.warn("[CM3Bridge] " + lastFailureReason);
                return -1;
            }
            FMLLog.log.info("[CM3Bridge] getIdForPos: returned " + result);
            if (result instanceof Integer) {
                return (Integer) result;
            }
            lastFailureReason = "getIdForPos a retourne " + (result == null ? "null" : result.getClass().getSimpleName());
        } catch (Throwable t) {
            lastFailureReason = "getIdForPos exception: " + t.getClass().getSimpleName() + " " + t.getMessage();
            FMLLog.log.warn("[CM3Bridge] " + lastFailureReason, t);
        }
        return -1;
    }

    /** Invoke getInstance(), trying with/without a World parameter. */
    private static Object invokeGetInstance() {
        try {
            int paramCount = methodGetInstance.getParameterCount();
            if (paramCount == 0) {
                return methodGetInstance.invoke(null);
            } else if (paramCount == 1) {
                // Probably needs a World - use overworld
                net.minecraft.server.MinecraftServer server =
                    net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
                if (server == null) return null;
                World overworld = server.getWorld(0);
                return methodGetInstance.invoke(null, overworld);
            }
        } catch (Throwable t) {
            lastFailureReason = "getInstance exception: " + t.getClass().getSimpleName() + " " + t.getMessage();
            FMLLog.log.warn("[CM3Bridge] " + lastFailureReason, t);
        }
        return null;
    }

    /**
     * Get the room position (north-west corner in DIM144) for the given
     * room id. Returns null if the room doesn't exist or CM3 is unavailable.
     */
    public static BlockPos getRoomPosition(int roomId) {
        if (!isAvailable()) return null;
        if (methodGetRoomPos == null) {
            lastFailureReason = "getMachineRoomPosition indisponible";
            return null;
        }
        try {
            Object wsdm = invokeGetInstance();
            if (wsdm == null) {
                lastFailureReason = "WSDM.getInstance() a retourne null";
                FMLLog.log.warn("[CM3Bridge] " + lastFailureReason);
                return null;
            }
            Object result = methodGetRoomPos.invoke(wsdm, roomId);
            FMLLog.log.info("[CM3Bridge] getRoomPosition(" + roomId + ") = " + result);
            if (result instanceof BlockPos) {
                return (BlockPos) result;
            }
            lastFailureReason = "getMachineRoomPosition a retourne " + (result == null ? "null" : result.getClass().getSimpleName());
        } catch (Throwable t) {
            lastFailureReason = "getRoomPosition exception: " + t.getClass().getSimpleName() + " " + t.getMessage();
            FMLLog.log.warn("[CM3Bridge] " + lastFailureReason, t);
        }
        return null;
    }

    /**
     * Update the map {roomId -> (dimension, blockPos)} so that the PSD
     * will direct the player to the new position when exiting the room.
     * Returns true on success.
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
                lastFailureReason = "WSDM.getInstance() a retourne null (addMachinePosition)";
                return false;
            }
            methodAddPos.invoke(wsdm, roomId, pos, dimension);
            FMLLog.log.info("[CM3Bridge] addMachinePosition(" + roomId + ", " + pos + ", dim=" + dimension + ") OK");
            return true;
        } catch (Throwable t) {
            lastFailureReason = "addMachinePosition exception: " + t.getClass().getSimpleName() + " " + t.getMessage();
            FMLLog.log.warn("[CM3Bridge] " + lastFailureReason, t);
            return false;
        }
    }

    /**
     * Set the id field of a TileEntityMachine via reflection.
     * Call this on a freshly placed CM block to link it to an existing room.
     */
    public static boolean setMachineId(TileEntity te, int id) {
        if (!isAvailable() || te == null) return false;
        if (!classTEMachine.isInstance(te)) return false;
        try {
            fieldMachineId.setInt(te, id);
            return true;
        } catch (IllegalAccessException e) {
            FMLLog.log.warn("[CM3Bridge] setMachineId failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Set the private roomPos field of a TileEntityMachine via reflection.
     */
    public static boolean setRoomPos(TileEntity te, BlockPos roomPos) {
        if (!isAvailable() || te == null) return false;
        if (!classTEMachine.isInstance(te)) return false;
        try {
            fieldRoomPos.set(te, roomPos);
            return true;
        } catch (IllegalAccessException e) {
            FMLLog.log.warn("[CM3Bridge] setRoomPos failed: " + e.getMessage());
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
        FMLLog.log.info("[CM3Bridge] placeLinkedMachine: roomId=" + roomId
            + " pos=" + pos + " dim=" + world.provider.getDimension() + " size=" + size);

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

        // Look up the room's DIM144 coordinates before placing
        BlockPos roomPos = getRoomPosition(roomId);
        if (roomPos == null) {
            lastFailureReason = "Room " + roomId + " n'a pas de roomPos dans WSDM (reason=" + lastFailureReason + ")";
            FMLLog.log.warn("[CM3Bridge] " + lastFailureReason);
            return false;
        }
        FMLLog.log.info("[CM3Bridge]   roomPos for id=" + roomId + ": " + roomPos);

        // Place the block with the appropriate meta for the size
        IBlockState state = machineBlock.getStateFromMeta(size);
        FMLLog.log.info("[CM3Bridge]   placing " + machineBlock.getRegistryName()
            + " meta=" + size + " at " + pos);
        world.setBlockState(pos, state, 3);

        // Grab the fresh TileEntity and link it to the existing room
        TileEntity te = world.getTileEntity(pos);
        FMLLog.log.info("[CM3Bridge]   fresh TE: " + (te == null ? "null" : te.getClass().getName()));
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

        boolean idOk = setMachineId(te, roomId);
        boolean roomOk = setRoomPos(te, roomPos);
        boolean posOk = addMachinePosition(roomId, pos, world.provider.getDimension());
        FMLLog.log.info("[CM3Bridge]   link results: id=" + idOk + " roomPos=" + roomOk + " addPos=" + posOk);

        te.markDirty();
        world.notifyBlockUpdate(pos, state, state, 3);

        if (idOk && roomOk && posOk) {
            FMLLog.log.info("[CM3Bridge] SUCCESS: Linked CM block at " + pos
                + " (dim " + world.provider.getDimension() + ") to room " + roomId);
            lastFailureReason = null;
            return true;
        } else {
            lastFailureReason = "Link partiel: id=" + idOk + " roomPos=" + roomOk + " addPos=" + posOk;
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
