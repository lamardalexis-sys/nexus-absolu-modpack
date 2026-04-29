package com.nexusabsolu.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet server → client : synchronise les timestamps des phases Manifoldine.
 *
 * Le client maintient son propre etat dans ManifoldClientState (variables
 * statiques). A chaque injection le serveur envoie un packet avec les 3
 * timestamps cles (phase2_at, active_until, fatigue_until) en deltas par
 * rapport au tick world courant — comme ca le client peut calculer la phase
 * en faisant juste world.getTotalWorldTime() - sync_tick.
 *
 * Pour annuler/reset les effets cote client (quand cooldown termine ou si
 * le serveur veut force stop), envoyer un packet avec tous les deltas a 0.
 */
public class PacketManifoldPhase implements IMessage {

    private long syncTick;       // tick world au moment ou le serveur envoie
    private int phase2Delta;     // ticks restants jusqu'a phase 2 (negatif)
    private int activeDelta;     // ticks restants jusqu'a la fin des potions
    private int fatigueDelta;    // ticks restants jusqu'a la fin de la fatigue

    public PacketManifoldPhase() {}

    public PacketManifoldPhase(long syncTick, int phase2Delta,
                                int activeDelta, int fatigueDelta) {
        this.syncTick = syncTick;
        this.phase2Delta = phase2Delta;
        this.activeDelta = activeDelta;
        this.fatigueDelta = fatigueDelta;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        syncTick = buf.readLong();
        phase2Delta = buf.readInt();
        activeDelta = buf.readInt();
        fatigueDelta = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(syncTick);
        buf.writeInt(phase2Delta);
        buf.writeInt(activeDelta);
        buf.writeInt(fatigueDelta);
    }

    public static class Handler implements IMessageHandler<PacketManifoldPhase, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketManifoldPhase msg, MessageContext ctx) {
            // Run sur thread main client
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                com.nexusabsolu.mod.client.ManifoldClientState.update(
                    msg.syncTick, msg.phase2Delta, msg.activeDelta, msg.fatigueDelta);
            });
            return null;
        }
    }
}
