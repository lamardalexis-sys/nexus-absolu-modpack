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
        INSTANCE.registerMessage(PacketScavengerSpeed.Handler.class,
            PacketScavengerSpeed.class, packetId++, Side.SERVER);
        // v1.0.325 (Age 4) — sync phases Manifoldine server → client
        INSTANCE.registerMessage(PacketManifoldPhase.Handler.class,
            PacketManifoldPhase.class, packetId++, Side.CLIENT);
        // v1.0.329 — Localisateur Dimensionnel (S->C: ouvre GUI, C->S: TP request)
        INSTANCE.registerMessage(PacketOpenLocalisateur.Handler.class,
            PacketOpenLocalisateur.class, packetId++, Side.CLIENT);
        INSTANCE.registerMessage(PacketTeleportToMachine.Handler.class,
            PacketTeleportToMachine.class, packetId++, Side.SERVER);
        // v1.0.341 -- Debug commande /nexus_test_sound
        INSTANCE.registerMessage(
            com.nexusabsolu.mod.commands.CommandNexusTestSound.TestSoundPacket.Handler.class,
            com.nexusabsolu.mod.commands.CommandNexusTestSound.TestSoundPacket.class,
            packetId++, Side.CLIENT);
        // v1.0.362 -- Sprint 2 etape 2 : sync PlayerMemorySnapshot pour flashbacks NDE
        INSTANCE.registerMessage(PacketSyncMemorySnapshot.Handler.class,
            PacketSyncMemorySnapshot.class, packetId++, Side.CLIENT);
    }
}
