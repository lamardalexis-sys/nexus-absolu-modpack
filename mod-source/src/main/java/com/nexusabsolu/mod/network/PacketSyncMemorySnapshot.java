package com.nexusabsolu.mod.network;

import com.nexusabsolu.mod.client.ManifoldClientMemory;
import com.nexusabsolu.mod.manifold.PlayerMemorySnapshot;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;

/**
 * Packet server -> client : envoie un PlayerMemorySnapshot serialise
 * au client pour qu'il puisse afficher les flashbacks pendant la NDE
 * du Cartouche Manifold (Sprint 2 etape 2 phase B).
 *
 * Usage cote serveur (a appeler quand le trip commence) :
 * <pre>
 *   PlayerMemorySnapshot snap = PlayerMemorySnapshot.captureFromPlayer(player);
 *   NexusPacketHandler.INSTANCE.sendTo(
 *       new PacketSyncMemorySnapshot(snap),
 *       (EntityPlayerMP) player);
 * </pre>
 *
 * Cote client : le handler stocke le snapshot dans ManifoldClientMemory,
 * que le ManifoldOverlayHandler peut consulter pendant le rendu.
 *
 * @since 1.0.362 (Sprint 2 etape 2 phase B)
 */
public class PacketSyncMemorySnapshot implements IMessage {

    private NBTTagCompound nbt;

    public PacketSyncMemorySnapshot() {
        this.nbt = new NBTTagCompound();
    }

    public PacketSyncMemorySnapshot(PlayerMemorySnapshot snapshot) {
        this.nbt = new NBTTagCompound();
        if (snapshot != null) {
            snapshot.writeToNBT(this.nbt);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        try {
            this.nbt = pb.readCompoundTag();
            if (this.nbt == null) {
                this.nbt = new NBTTagCompound();
            }
        } catch (Exception e) {
            this.nbt = new NBTTagCompound();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeCompoundTag(this.nbt);
    }

    public static class Handler implements IMessageHandler<PacketSyncMemorySnapshot, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketSyncMemorySnapshot msg, MessageContext ctx) {
            // Doit s'executer sur le main thread client pour eviter les races
            Minecraft.getMinecraft().addScheduledTask(() -> {
                PlayerMemorySnapshot snap = PlayerMemorySnapshot.readFromNBT(msg.nbt);
                ManifoldClientMemory.setSnapshot(snap);
            });
            return null;
        }
    }
}
