package com.nexusabsolu.mod.network;

import com.nexusabsolu.mod.Reference;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NexusPacketHandler {

    public static SimpleNetworkWrapper INSTANCE;
    private static int packetId = 0;

    public static void init() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);
        INSTANCE.registerMessage(PacketMiningHeartbeat.Handler.class,
            PacketMiningHeartbeat.class, packetId++, Side.SERVER);
    }
}
