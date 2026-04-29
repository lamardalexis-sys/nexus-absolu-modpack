package com.nexusabsolu.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet server → client : synchronise le start tick d'une injection Manifold.
 *
 * Architecture refondue : au lieu d'envoyer 3 timestamps de phase, on envoie
 * juste le tick world du debut + la duree totale. Le client calcule lui-meme
 * le progress [0..1] et le stage actuel a chaque frame avec la meme logique
 * que ManifoldEffectHandler.getCurrentStage().
 *
 * Avantage : pas besoin de re-sync quand on change de stage, le client le
 * detecte tout seul. Aussi : si le serveur change la duration des stages,
 * le client se met a jour sans changer le packet.
 */
public class PacketManifoldPhase implements IMessage {

    private long syncTick;     // tick world au moment de l'envoi
    private long startTick;    // tick world du debut du trip
    private int totalTicks;    // duree totale (TOTAL_DURATION) — 0 = annulation

    public PacketManifoldPhase() {}

    public PacketManifoldPhase(long syncTick, long startTick, int totalTicks) {
        this.syncTick = syncTick;
        this.startTick = startTick;
        this.totalTicks = totalTicks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        syncTick = buf.readLong();
        startTick = buf.readLong();
        totalTicks = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(syncTick);
        buf.writeLong(startTick);
        buf.writeInt(totalTicks);
    }

    public static class Handler implements IMessageHandler<PacketManifoldPhase, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketManifoldPhase msg, MessageContext ctx) {
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                com.nexusabsolu.mod.client.ManifoldClientState.update(
                    msg.syncTick, msg.startTick, msg.totalTicks);

                // v1.0.329 (Etape 1 visuel ultime) -- demarrer la musique avec
                // ITickableSound pour fade in/out. Conditions : start reel
                // (pas annulation) + ModSounds initialise.
                // v1.0.335 : log de debug pour confirmer qu'on arrive bien ici
                // cote client (utile si la musique ne demarre pas).
                if (msg.startTick != 0L && msg.totalTicks > 0
                        && com.nexusabsolu.mod.init.ModSounds.MANIFOLD_CENTINELA != null) {
                    System.out.println("[Manifold] Demarrage musique Centinela (startTick="
                        + msg.startTick + ", totalTicks=" + msg.totalTicks + ")");
                    net.minecraft.client.Minecraft.getMinecraft().getSoundHandler().playSound(
                        new com.nexusabsolu.mod.client.ManifoldMusicTickableSound(
                            com.nexusabsolu.mod.init.ModSounds.MANIFOLD_CENTINELA));
                } else {
                    System.out.println("[Manifold] Pas de musique demarree : startTick="
                        + msg.startTick + " totalTicks=" + msg.totalTicks
                        + " sound=" + (com.nexusabsolu.mod.init.ModSounds.MANIFOLD_CENTINELA != null));
                }
            });
            return null;
        }
    }
}
