package com.nexusabsolu.mod.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;

/**
 * /voss_goto &lt;roomId&gt;
 *
 * Tp le joueur dans la Compact Machine room avec l'id donne, dans DIM 144.
 * Position de spawn: (1024*roomId + 4, 41, 4) - centre approximatif.
 *
 * Permet de retrouver une room "orpheline" : par exemple apres fusion
 * matryoshka via Condenseur, les anciennes rooms (3x3, 5x5, 7x7) existent
 * toujours physiquement mais aucun bloc CM ne pointe vers elles. Cette
 * commande permet d'y acceder par leur id et de recuperer le stuff laisse
 * dedans.
 *
 * Usage typique :
 *   /voss_goto 0   -&gt; tp dans la room #0 (premiere CM creee, generalement la 3x3)
 *   /voss_goto 1   -&gt; tp dans la room #1 (deuxieme CM creee)
 *   /voss_goto 2   -&gt; tp dans la room #2 (etc.)
 */
public class CommandVossGoto extends CommandBase {

    @Override public String getName() { return "voss_goto"; }

    @Override public String getUsage(ICommandSender sender) {
        return "/voss_goto <roomId>";
    }

    @Override public int getRequiredPermissionLevel() { return 2; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.sendMessage(red("Cette commande doit etre executee par un joueur."));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(red("Usage: " + getUsage(sender)));
            sender.sendMessage(gray("roomId = id de la room CM (entier >= 0)"));
            sender.sendMessage(gray("Exemple: /voss_goto 1"));
            return;
        }

        int roomId;
        try {
            roomId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(red("roomId invalide: " + args[0] + " (doit etre un entier)"));
            return;
        }
        if (roomId < 0) {
            sender.sendMessage(red("roomId doit etre >= 0"));
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) sender;

        // Position cible : NW corner de la room + offset pour spawner
        // au-dessus du sol et eviter le mur.
        final double targetX = 1024.0 * roomId + 4.5;
        final double targetY = 41.0;
        final double targetZ = 4.5;

        WorldServer dim144 = server.getWorld(144);
        if (dim144 == null) {
            sender.sendMessage(red("DIM 144 non chargee. Reessaie apres avoir entre dans une CM."));
            return;
        }

        sender.sendMessage(green("TP vers room #" + roomId
            + " (" + (int) targetX + ", " + (int) targetY + ", " + (int) targetZ + ")"));

        if (player.dimension != 144) {
            // Cross-dimensional teleport
            player.changeDimension(144, new ITeleporter() {
                @Override
                public void placeEntity(net.minecraft.world.World w,
                                        net.minecraft.entity.Entity entity,
                                        float yaw) {
                    entity.setLocationAndAngles(targetX, targetY, targetZ, yaw, 0);
                }
            });
        } else {
            // Deja en DIM 144, tp interne
            player.connection.setPlayerLocation(
                targetX, targetY, targetZ,
                player.rotationYaw, player.rotationPitch);
        }
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
