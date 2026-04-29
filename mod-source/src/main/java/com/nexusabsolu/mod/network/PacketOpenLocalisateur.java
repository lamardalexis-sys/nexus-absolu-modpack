package com.nexusabsolu.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet server -> client : ouvre la GUI Localisateur Dimensionnel cote
 * client avec la liste des Compact Machines visitees par le joueur.
 *
 * Le serveur envoie ce packet en reponse au clic-droit sur l'item
 * Localisateur Dimensionnel. Le client ouvre la GuiLocalisateurDimensionnel
 * avec ces ids.
 */
public class PacketOpenLocalisateur implements IMessage {

    private int[] machineIds;

    public PacketOpenLocalisateur() {
        this.machineIds = new int[0];
    }

    public PacketOpenLocalisateur(int[] machineIds) {
        this.machineIds = machineIds != null ? machineIds : new int[0];
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        int len = pb.readVarInt();
        // Garde-fou anti-DoS : limite raisonnable, un joueur n'aura jamais
        // 10000 CMs distinctes en pratique.
        if (len < 0 || len > 10000) {
            this.machineIds = new int[0];
            return;
        }
        this.machineIds = new int[len];
        for (int i = 0; i < len; i++) {
            this.machineIds[i] = pb.readVarInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeVarInt(machineIds.length);
        for (int id : machineIds) {
            pb.writeVarInt(id);
        }
    }

    public static class Handler implements IMessageHandler<PacketOpenLocalisateur, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(final PacketOpenLocalisateur msg, MessageContext ctx) {
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(
                        new com.nexusabsolu.mod.gui.GuiLocalisateurDimensionnel(msg.machineIds));
                }
            });
            return null;
        }
    }
}
