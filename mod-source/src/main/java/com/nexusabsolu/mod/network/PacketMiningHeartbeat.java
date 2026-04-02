package com.nexusabsolu.mod.network;

import com.nexusabsolu.mod.scavenging.ScavengeEventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketMiningHeartbeat implements IMessage {

    public PacketMiningHeartbeat() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        // Empty packet - no data needed
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // Empty packet - no data needed
    }

    public static class Handler implements IMessageHandler<PacketMiningHeartbeat, IMessage> {
        @Override
        public IMessage onMessage(PacketMiningHeartbeat message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            // Schedule on main server thread for thread safety
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    ScavengeEventHandler.refreshHeartbeat(player.getUniqueID());
                }
            });
            return null;
        }
    }
}
