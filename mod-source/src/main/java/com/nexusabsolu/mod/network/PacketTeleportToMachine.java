package com.nexusabsolu.mod.network;

import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.util.PlayerVisitedMachines;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet client -> server : le joueur a clique sur une ligne de la GUI
 * Localisateur Dimensionnel et demande a etre teleporte dans la CM
 * correspondante.
 *
 * Validations cote serveur (anti-exploit) :
 *   1. Le joueur a un Localisateur Dimensionnel dans son inventaire
 *      (sinon il a triche : pas l'item = pas le droit d'utiliser ce packet).
 *   2. La machineId demandee est dans la liste visitee du joueur
 *      (impossible de TP dans une CM jamais visitee).
 *   3. La machineId est >= 0.
 *
 * TP : utilise la formule legacy CM3 (1024*id + 4.5, 41, 4.5) qui est la
 * meme que dans CM3Bridge.getIdForPos. Si le joueur n'est pas en DIM 144,
 * on cross-dimensional teleport via ITeleporter custom. Sinon on utilise
 * setPlayerLocation pour un TP intra-dim plus leger.
 */
public class PacketTeleportToMachine implements IMessage {

    private int machineId;

    public PacketTeleportToMachine() {
        this.machineId = -1;
    }

    public PacketTeleportToMachine(int machineId) {
        this.machineId = machineId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        this.machineId = pb.readVarInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeVarInt(machineId);
    }

    public static class Handler implements IMessageHandler<PacketTeleportToMachine, IMessage> {
        @Override
        public IMessage onMessage(final PacketTeleportToMachine msg, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            // Toutes les operations world doivent etre planifiees sur le main
            // thread serveur.
            player.getServerWorld().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    handleTeleport(player, msg.machineId);
                }
            });
            return null;
        }

        private static void handleTeleport(EntityPlayerMP player, int machineId) {
            // Validation 1 : id valide
            if (machineId < 0) {
                FMLLog.log.warn("[Localisateur] Refus TP pour " + player.getName()
                    + " : machineId invalide (" + machineId + ")");
                return;
            }

            // Validation 2 : le joueur a un Localisateur Dimensionnel
            if (!hasLocalisateur(player)) {
                FMLLog.log.warn("[Localisateur] Refus TP pour " + player.getName()
                    + " : pas de Localisateur dans l'inventaire (machineId="
                    + machineId + ")");
                return;
            }

            // Validation 3 : la machine est dans la liste visitee
            if (!PlayerVisitedMachines.hasVisited(player, machineId)) {
                FMLLog.log.warn("[Localisateur] Refus TP pour " + player.getName()
                    + " : machineId " + machineId + " non visitee");
                return;
            }

            // OK : effectue le TP
            performTeleport(player, machineId);
        }

        private static boolean hasLocalisateur(EntityPlayerMP player) {
            // Recherche dans toutes les slots de l'inventaire (main + armor + offhand)
            for (ItemStack stack : player.inventory.mainInventory) {
                if (!stack.isEmpty() && stack.getItem() == ModItems.LOCALISATEUR_DIMENSIONNEL) {
                    return true;
                }
            }
            for (ItemStack stack : player.inventory.offHandInventory) {
                if (!stack.isEmpty() && stack.getItem() == ModItems.LOCALISATEUR_DIMENSIONNEL) {
                    return true;
                }
            }
            return false;
        }

        private static void performTeleport(EntityPlayerMP player, int roomId) {
            // Position cible : meme formule que CommandVossGoto (legacy CM3
            // coords scheme : floorDiv(x, 1024) -> roomId).
            final double targetX = 1024.0 * roomId + 4.5;
            final double targetY = 41.0;
            final double targetZ = 4.5;

            FMLLog.log.info("[Localisateur] " + player.getName()
                + " TP -> room #" + roomId + " (" + (int) targetX + ", "
                + (int) targetY + ", " + (int) targetZ + ")");

            if (player.dimension != 144) {
                // Cross-dimensional teleport via ITeleporter
                player.changeDimension(144, new ITeleporter() {
                    @Override
                    public void placeEntity(World w, Entity entity, float yaw) {
                        entity.setLocationAndAngles(targetX, targetY, targetZ, yaw, 0);
                    }
                });
            } else {
                // Intra-DIM 144 : TP leger via setPlayerLocation
                player.connection.setPlayerLocation(targetX, targetY, targetZ,
                    player.rotationYaw, player.rotationPitch);
            }

            // SFX enderman teleport sur la position d'arrivee
            player.world.playSound(null, targetX, targetY, targetZ,
                SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS,
                0.7F, 1.0F);
        }
    }
}
