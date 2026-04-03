package com.nexusabsolu.mod.network;

import com.nexusabsolu.mod.tiles.TileAutoScavenger;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketScavengerSpeed implements IMessage {

    private BlockPos pos;
    private boolean increase; // true = +1, false = -1

    public PacketScavengerSpeed() {}

    public PacketScavengerSpeed(BlockPos pos, boolean increase) {
        this.pos = pos;
        this.increase = increase;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        increase = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeBoolean(increase);
    }

    public static class Handler implements IMessageHandler<PacketScavengerSpeed, IMessage> {
        @Override
        public IMessage onMessage(PacketScavengerSpeed msg, MessageContext ctx) {
            WorldServer world = ctx.getServerHandler().player.getServerWorld();
            world.addScheduledTask(() -> {
                TileEntity te = world.getTileEntity(msg.pos);
                if (te instanceof TileAutoScavenger) {
                    TileAutoScavenger scav = (TileAutoScavenger) te;
                    if (msg.increase) {
                        scav.increaseSpeed();
                    } else {
                        scav.decreaseSpeed();
                    }
                }
            });
            return null;
        }
    }
}
