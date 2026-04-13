package com.nexusabsolu.mod.commands;

import com.nexusabsolu.mod.compat.CM3Bridge;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * /nexus_psd
 *
 * Repare la chaine matryoshka des Compact Machines pour les saves
 * existantes ou la Cle de Liberte a deja casse l'exit target PSD
 * (avant le fix v1.0.175).
 *
 * Workflow:
 *   1. /nexus_psd mark
 *      -> dans la salle PARENTE, en regardant le bloc CM enfant.
 *         Memorise les coords du bloc CM enfant (target).
 *
 *   2. PSD-entrer dans la salle enfant (cassee)
 *
 *   3. /nexus_psd apply
 *      -> dans la salle ENFANT (cassee).
 *         Reecrit l'exit target de cette salle vers les coords memorisees.
 *
 * Apres apply, PSD-exit ramenera dans la salle parente.
 *
 * Repete pour chaque maillon casse de la chaine matryoshka:
 *   9x9 -> 7x7, puis 7x7 -> 5x5, puis 5x5 -> 3x3.
 */
public class CommandRepairPSD extends CommandBase {

    private static final Map<UUID, MarkData> marks = new HashMap<>();

    private static class MarkData {
        BlockPos parentBlockPos;
        int parentDim;
        MarkData(BlockPos pos, int dim) { this.parentBlockPos = pos; this.parentDim = dim; }
    }

    @Override public String getName() { return "nexus_psd"; }

    @Override public String getUsage(ICommandSender sender) {
        return "/nexus_psd <mark|apply|status>";
    }

    @Override public int getRequiredPermissionLevel() { return 2; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.sendMessage(red("Cette commande doit etre executee par un joueur."));
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) sender;

        if (args.length < 1) {
            sender.sendMessage(red("Usage: " + getUsage(sender)));
            sendHelp(sender);
            return;
        }

        String sub = args[0].toLowerCase();
        if ("mark".equals(sub)) {
            doMark(player);
        } else if ("apply".equals(sub)) {
            doApply(player);
        } else if ("status".equals(sub)) {
            doStatus(player);
        } else {
            sender.sendMessage(red("Sous-commande inconnue: " + sub));
            sendHelp(sender);
        }
    }

    private void doMark(EntityPlayerMP player) {
        Vec3d eyes = player.getPositionEyes(1.0F);
        Vec3d look = player.getLook(1.0F);
        Vec3d end = eyes.addVector(look.x * 8.0, look.y * 8.0, look.z * 8.0);
        RayTraceResult ray = player.world.rayTraceBlocks(eyes, end, false, false, true);

        if (ray == null || ray.typeOfHit != RayTraceResult.Type.BLOCK) {
            player.sendMessage(red("Aucun bloc vise. Regarde le bloc CM parent et reessaie."));
            return;
        }

        BlockPos targetPos = ray.getBlockPos();
        net.minecraft.block.Block block = player.world.getBlockState(targetPos).getBlock();
        String regName = block.getRegistryName() != null
            ? block.getRegistryName().toString() : "?";

        if (!"compactmachines3:machine".equals(regName)) {
            player.sendMessage(red("Le bloc vise n'est pas une Compact Machine: " + regName));
            player.sendMessage(gray("Vise un bloc CM dans la salle parente."));
            return;
        }

        marks.put(player.getUniqueID(),
            new MarkData(targetPos, player.dimension));
        player.sendMessage(green("Marque: " + targetPos + " (DIM " + player.dimension + ")"));
        player.sendMessage(gray("Maintenant: rentre dans la salle enfant cassee, puis /nexus_psd apply"));
    }

    private void doApply(EntityPlayerMP player) {
        MarkData mark = marks.get(player.getUniqueID());
        if (mark == null) {
            player.sendMessage(red("Aucune marque. Fais d'abord /nexus_psd mark dans la salle parente."));
            return;
        }

        int roomId = CM3Bridge.getIdForPos(player.world, player.getPosition());
        if (roomId < 0) {
            player.sendMessage(red("Impossible d'identifier la salle CM courante. Reason: "
                + CM3Bridge.getLastFailureReason()));
            player.sendMessage(gray("Es-tu dans une CM (DIM 144)? DIM actuelle: " + player.dimension));
            return;
        }

        player.sendMessage(gray("Salle enfant: roomId=" + roomId));
        player.sendMessage(gray("Reecriture exit -> " + mark.parentBlockPos
            + " (DIM " + mark.parentDim + ")"));

        boolean ok = CM3Bridge.addMachinePosition(roomId, mark.parentBlockPos, mark.parentDim);

        if (ok) {
            player.sendMessage(green("OK. PSD-exit ramenera maintenant vers "
                + mark.parentBlockPos + " (DIM " + mark.parentDim + ")."));
            player.sendMessage(gray("Sors via PSD pour verifier. Reutilise mark/apply pour le maillon suivant."));
            marks.remove(player.getUniqueID());
        } else {
            player.sendMessage(red("Echec. Reason: " + CM3Bridge.getLastFailureReason()));
        }
    }

    private void doStatus(EntityPlayerMP player) {
        player.sendMessage(gray("DIM actuelle: " + player.dimension
            + " | pos: " + player.getPosition()));
        if (player.dimension == 144) {
            int roomId = CM3Bridge.getIdForPos(player.world, player.getPosition());
            player.sendMessage(gray("roomId courante: " + roomId));
        }
        MarkData mark = marks.get(player.getUniqueID());
        if (mark != null) {
            player.sendMessage(gray("Marque active: " + mark.parentBlockPos
                + " (DIM " + mark.parentDim + ")"));
        } else {
            player.sendMessage(gray("Aucune marque active."));
        }
    }

    private void sendHelp(ICommandSender sender) {
        sender.sendMessage(gray("Workflow matryoshka:"));
        sender.sendMessage(gray(" 1. Salle parente, regarde le bloc CM enfant -> /nexus_psd mark"));
        sender.sendMessage(gray(" 2. Rentre dans la salle enfant cassee"));
        sender.sendMessage(gray(" 3. /nexus_psd apply -> exit reecrit"));
        sender.sendMessage(gray(" 4. PSD-exit pour verifier"));
        sender.sendMessage(gray(" Repete pour chaque maillon casse."));
    }

    private TextComponentString red(String s) {
        TextComponentString t = new TextComponentString(s);
        t.getStyle().setColor(TextFormatting.RED);
        return t;
    }
    private TextComponentString green(String s) {
        TextComponentString t = new TextComponentString(s);
        t.getStyle().setColor(TextFormatting.GREEN);
        return t;
    }
    private TextComponentString gray(String s) {
        TextComponentString t = new TextComponentString(s);
        t.getStyle().setColor(TextFormatting.GRAY);
        return t;
    }
}
