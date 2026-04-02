package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.network.NexusPacketHandler;
import com.nexusabsolu.mod.network.PacketMiningHeartbeat;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientMiningHandler {

    private int tickCounter = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) return;

        // Don't send heartbeat if a GUI is open
        if (mc.currentScreen != null) return;

        // Throttle: send every 3 ticks (150ms) to reduce network
        tickCounter++;
        if (tickCounter % 3 != 0) return;

        // Check if left mouse button is held
        if (!mc.gameSettings.keyBindAttack.isKeyDown()) return;

        // Use the already-computed objectMouseOver (updated each frame)
        RayTraceResult ray = mc.objectMouseOver;
        if (ray == null || ray.typeOfHit != RayTraceResult.Type.BLOCK) return;

        Block block = mc.world.getBlockState(ray.getBlockPos()).getBlock();
        if (block.getRegistryName() == null) return;
        if (!block.getRegistryName().toString().equals("compactmachines3:wall")) return;

        // Player is holding left-click on a CM wall -> send heartbeat
        NexusPacketHandler.INSTANCE.sendToServer(new PacketMiningHeartbeat());
    }
}
