package com.nexusabsolu.mod.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class CommandNexusMsg extends CommandBase {

    private static final List<DelayedMsg> QUEUE = new ArrayList<>();
    private static boolean registered = false;

    public CommandNexusMsg() {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(this);
            registered = true;
        }
    }

    @Override public String getName() { return "nexusmsg"; }
    @Override public String getUsage(ICommandSender sender) { return "/nexusmsg <player>"; }
    @Override public int getRequiredPermissionLevel() { return 2; }
    @Override public boolean isUsernameIndex(String[] args, int index) { return index == 0; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        EntityPlayerMP player = null;
        if (args.length > 0) {
            try {
                player = getPlayer(server, sender, args[0]);
            } catch (Exception e) {
                player = null;
            }
        }
        if (player == null && sender instanceof EntityPlayerMP) {
            player = (EntityPlayerMP) sender;
        }
        if (player == null) return;

        UUID id = player.getUniqueID();
        int tick = 0;

        // Ligne 1: delai 1s
        tick += 20;
        addMsg(id, tick, text("[TRANSMISSION INTERCEPTEE]", TextFormatting.RED, true, false));

        // Ligne 2: delai 2s
        tick += 40;
        ITextComponent src = text("Source: Dr. E. Vo--", TextFormatting.GRAY, false, false);
        src.appendSibling(text("$$$", TextFormatting.DARK_RED, false, false, true));
        addMsg(id, tick, src);

        // Ligne 3: delai 2.5s
        tick += 50;
        addMsg(id, tick, text("", TextFormatting.GRAY, false, false));

        // Ligne 4: delai 2s
        tick += 40;
        addMsg(id, tick, text("  ...ne restez pas ici...", TextFormatting.GRAY, false, true));

        // Ligne 5: delai 2.5s
        tick += 50;
        ITextComponent murs = text("  les murs ne sont pas des m", TextFormatting.GRAY, false, true);
        murs.appendSibling(text("&@#!", TextFormatting.DARK_RED, false, false, true));
        addMsg(id, tick, murs);

        // Ligne 6: delai 3s
        tick += 60;
        addMsg(id, tick, text("  j'ai fait une erreur.", TextFormatting.GRAY, false, true));

        // Ligne 7: delai 2s
        tick += 40;
        ITextComponent ouvrir = text("  je n'aurais pas du ouvrir le ", TextFormatting.GRAY, false, true);
        ouvrir.appendSibling(text("#*!$%", TextFormatting.DARK_RED, false, false, true));
        addMsg(id, tick, ouvrir);

        // Ligne 8: delai 3s
        tick += 60;
        addMsg(id, tick, text("", TextFormatting.GRAY, false, false));

        // Ligne 9: delai 2s
        tick += 40;
        addMsg(id, tick, text("  frappez les murs. prenez tout.", TextFormatting.GOLD, false, false));

        // Ligne 10: delai 2s
        tick += 40;
        addMsg(id, tick, text("  sortez.", TextFormatting.RED, true, false));

        // Ligne 11: delai 3s
        tick += 60;
        addMsg(id, tick, text("", TextFormatting.GRAY, false, false));

        // Ligne 12: delai 2s
        tick += 40;
        addMsg(id, tick, text("[FIN DE TRANSMISSION - SIGNAL PERDU]", TextFormatting.DARK_GRAY, false, false));
    }

    private void addMsg(UUID playerId, int delay, ITextComponent msg) {
        synchronized (QUEUE) {
            QUEUE.add(new DelayedMsg(playerId, delay, msg));
        }
    }

    private TextComponentString text(String s, TextFormatting color, boolean bold, boolean italic) {
        return text(s, color, bold, italic, false);
    }

    private TextComponentString text(String s, TextFormatting color, boolean bold, boolean italic, boolean obfuscated) {
        TextComponentString t = new TextComponentString(s);
        Style style = new Style().setColor(color).setBold(bold).setItalic(italic).setObfuscated(obfuscated);
        t.setStyle(style);
        return t;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        synchronized (QUEUE) {
            Iterator<DelayedMsg> it = QUEUE.iterator();
            while (it.hasNext()) {
                DelayedMsg msg = it.next();
                msg.ticksLeft--;
                if (msg.ticksLeft <= 0) {
                    EntityPlayerMP player = net.minecraftforge.fml.common.FMLCommonHandler.instance()
                        .getMinecraftServerInstance().getPlayerList().getPlayerByUUID(msg.playerId);
                    if (player != null) {
                        player.sendMessage(msg.message);
                    }
                    it.remove();
                }
            }
        }
    }

    private static class DelayedMsg {
        UUID playerId;
        int ticksLeft;
        ITextComponent message;

        DelayedMsg(UUID id, int ticks, ITextComponent msg) {
            this.playerId = id;
            this.ticksLeft = ticks;
            this.message = msg;
        }
    }
}
