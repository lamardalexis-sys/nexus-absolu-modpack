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
        try {
            classWSDM = Class.forName(
                "org.dave.compactmachines3.world.WorldSavedDataMachines");
            classStructTools = Class.forName(
                "org.dave.compactmachines3.world.tools.StructureTools");
            classTEMachine = Class.forName(
                "org.dave.compactmachines3.tile.TileEntityMachine");

            methodGetInstance = classWSDM.getMethod("getInstance");
            methodAddPos = classWSDM.getMethod("addMachinePosition",
                int.class, BlockPos.class, int.class);
            methodGetRoomPos = classWSDM.getMethod("getMachineRoomPosition",
                int.class);

            // StructureTools.getIdForPos has multiple possible signatures
            // across CM3 versions - try common ones
            Method idMethod = null;
            for (Method m : classStructTools.getMethods()) {
                if (m.getName().equals("getIdForPos")) {
                    idMethod = m;
                    break;
                }
            }
            methodGetIdForPos = idMethod;

            fieldMachineId = classTEMachine.getField("id");
            fieldMachineId.setAccessible(true);

            // roomPos is private, access via declared fields
            Field rp = null;
            for (Field f : classTEMachine.getDeclaredFields()) {
                if (f.getName().equals("roomPos")) {
                    rp = f;
                    break;
                }
            }
            if (rp != null) {
                rp.setAccessible(true);
                fieldRoomPos = rp;
            }

            initSuccess = (methodGetIdForPos != null && fieldRoomPos != null);

            if (initSuccess) {
                FMLLog.log.info("[CM3Bridge] Successfully initialized. "
                    + "Portal Voss teleport can relink CM rooms.");
            } else {
                FMLLog.log.warn("[CM3Bridge] Partial init - missing "
                    + "getIdForPos=" + (methodGetIdForPos != null)
                    + " roomPos=" + (fieldRoomPos != null));
            }
        } catch (ClassNotFoundException e) {
            FMLLog.log.warn("[CM3Bridge] Compact Machines 3 classes not found. "
                + "CM room relinking will be disabled.");
            initSuccess = false;
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            FMLLog.log.warn("[CM3Bridge] CM3 API signature mismatch: "
                + e.getMessage() + ". CM room relinking will be disabled.");
            initSuccess = false;
        }
    }

    /**
     * Look up the CM room id that contains the given world position.
     * Returns -1 if the position is not inside any CM room, or if CM3 is
     * unavailable.
     */
    public static int getIdForPos(World world, BlockPos pos) {
        if (!isAvailable()) return -1;
        try {
            // Try (World, BlockPos) signature first, then (BlockPos)
            Object result;
            if (methodGetIdForPos.getParameterCount() == 2) {
                result = methodGetIdForPos.invoke(null, world, pos);
            } else {
                result = methodGetIdForPos.invoke(null, pos);
            }
            if (result instanceof Integer) {
                return (Integer) result;
            }
        } catch (Exception e) {
            FMLLog.log.warn("[CM3Bridge] getIdForPos failed: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Get the room position (north-west corner in DIM144) for the given
     * room id. Returns null if the room doesn't exist or CM3 is unavailable.
     */
    public static BlockPos getRoomPosition(int roomId) {
        if (!isAvailable()) return null;
        try {
            Object wsdm = methodGetInstance.invoke(null);
            if (wsdm == null) return null;
            Object result = methodGetRoomPos.invoke(wsdm, roomId);
            if (result instanceof BlockPos) {
                return (BlockPos) result;
            }
        } catch (Exception e) {
            FMLLog.log.warn("[CM3Bridge] getRoomPosition failed: " + e.getMessage());
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
        try {
            Object wsdm = methodGetInstance.invoke(null);
            if (wsdm == null) return false;
            methodAddPos.invoke(wsdm, roomId, pos, dimension);
            return true;
        } catch (Exception e) {
            FMLLog.log.warn("[CM3Bridge] addMachinePosition failed: " + e.getMessage());
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
        Block machineBlock = getMachineBlock();
        if (machineBlock == null || machineBlock == net.minecraft.init.Blocks.AIR) {
            FMLLog.log.warn("[CM3Bridge] compactmachines3:machine block not found");
            return false;
        }

        // Look up the room's DIM144 coordinates before placing
        BlockPos roomPos = getRoomPosition(roomId);
        if (roomPos == null) {
            FMLLog.log.warn("[CM3Bridge] Room " + roomId + " has no roomPos");
            return false;
        }

        // Place the block with the appropriate meta for the size
        IBlockState state = machineBlock.getStateFromMeta(size);
        world.setBlockState(pos, state, 3);

        // Grab the fresh TileEntity and link it to the existing room
        TileEntity te = world.getTileEntity(pos);
        if (te == null) {
            FMLLog.log.warn("[CM3Bridge] TileEntity not created at " + pos);
            return false;
        }

        boolean idOk = setMachineId(te, roomId);
        boolean roomOk = setRoomPos(te, roomPos);
        boolean posOk = addMachinePosition(roomId, pos, world.provider.getDimension());

        te.markDirty();
        world.notifyBlockUpdate(pos, state, state, 3);

        if (idOk && roomOk && posOk) {
            FMLLog.log.info("[CM3Bridge] Linked CM block at " + pos
                + " (dim " + world.provider.getDimension() + ") to room " + roomId);
            return true;
        } else {
            FMLLog.log.warn("[CM3Bridge] Partial link failure: id=" + idOk
                + " roomPos=" + roomOk + " pos=" + posOk);
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
