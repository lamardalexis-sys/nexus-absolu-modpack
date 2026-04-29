package com.nexusabsolu.mod.items;

import com.nexusabsolu.mod.events.ManifoldEffectHandler;
import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Cartouche Manifold — l'item phare de l'Age 4.
 *
 * Right-click → declenche la sequence d'injection complete via
 * ManifoldEffectHandler.startInjection() :
 *
 *   PHASE 1 (0-4 min)  : potions cranked + overlay violet/cyan + particules chaos
 *   PHASE 2 (4-5 min)  : overlay NEGATIF (le trip vrille) + particules vrillees
 *   CRASH (5 min)      : potions retirees, fatigue 1 min appliquee
 *   PHASE 3 (5-6 min)  : Slowness II + Mining Fatigue II + Weakness
 *
 * Cooldown total : 5 minutes (anti-overdose).
 *
 * Apres usage : transformee en CartoucheUsed (casing recyclable).
 */
public class ItemCartoucheManifold extends ItemBase {

    public ItemCartoucheManifold() {
        super("cartouche_manifold");
        setMaxStackSize(1);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;  // glint permanent
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world,
                                List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.LIGHT_PURPLE + "" + TextFormatting.ITALIC
            + "Le serum est vivant. Il pulse.");
        tooltip.add(TextFormatting.DARK_GRAY + "\"Cinq theoremes. Une seule injection.\"");
        tooltip.add("");
        tooltip.add(TextFormatting.YELLOW + "Clic droit pour t'injecter.");
        tooltip.add(TextFormatting.GRAY + "Phase 1 : "
            + TextFormatting.LIGHT_PURPLE + "4 minutes "
            + TextFormatting.GRAY + "(puissance absolue)");
        tooltip.add(TextFormatting.GRAY + "Phase 2 : "
            + TextFormatting.DARK_PURPLE + "1 minute "
            + TextFormatting.GRAY + "(le trip vrille)");
        tooltip.add(TextFormatting.GRAY + "Crash : "
            + TextFormatting.RED + "1 minute "
            + TextFormatting.GRAY + "(fatigue forcee)");
        tooltip.add("");
        tooltip.add(TextFormatting.DARK_RED + "Cooldown : 5 minutes. Pas de re-injection.");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player,
                                                     EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (!(player instanceof EntityPlayerMP)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        EntityPlayerMP mp = (EntityPlayerMP) player;

        // Cooldown check (covers la fatigue ET l'anti-overdose, le cooldown
        // est plus long que la duree totale donc on bloque pendant tout le cycle)
        long now = world.getTotalWorldTime();
        long cooldownUntil = ManifoldEffectHandler.getCooldownUntil(player);
        if (now < cooldownUntil) {
            long remaining = (cooldownUntil - now) / 20;
            int phase = ManifoldEffectHandler.getCurrentPhase(player);
            String reason = phase == ManifoldEffectHandler.PHASE_FATIGUE
                ? "Tu te remets a peine de la derniere injection."
                : "Le serum coule deja dans tes veines. Pas deux fois.";
            player.sendMessage(new TextComponentString(
                TextFormatting.DARK_RED + reason + " "
                + TextFormatting.GRAY + "(" + formatTime(remaining) + ")"));
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        // === INJECTION ===
        playInjectionSounds(world, player);
        sendInjectionMessages(player);
        ManifoldEffectHandler.startInjection(mp);

        // Consomme la cartouche, donne la cartouche usee
        stack.shrink(1);
        ItemStack used = new ItemStack(ModItems.CARTOUCHE_USED, 1);
        if (!player.inventory.addItemStackToInventory(used)) {
            player.dropItem(used, false);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private void playInjectionSounds(World world, EntityPlayer player) {
        world.playSound(null, player.posX, player.posY, player.posZ,
            SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.5F, 0.5F);
        world.playSound(null, player.posX, player.posY, player.posZ,
            SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 2.0F);
    }

    private void sendInjectionMessages(EntityPlayer player) {
        player.sendMessage(new TextComponentString(
            TextFormatting.LIGHT_PURPLE + "" + TextFormatting.BOLD
            + "============================================"));
        player.sendMessage(new TextComponentString(
            TextFormatting.LIGHT_PURPLE + "" + TextFormatting.BOLD
            + "        [ M A N I F O L D I N E ]"));
        player.sendMessage(new TextComponentString(
            TextFormatting.LIGHT_PURPLE + "" + TextFormatting.BOLD
            + "============================================"));
        player.sendMessage(new TextComponentString(
            TextFormatting.GRAY + "" + TextFormatting.ITALIC
            + "\"Tu te souviens de quelque chose."));
        player.sendMessage(new TextComponentString(
            TextFormatting.GRAY + "" + TextFormatting.ITALIC
            + " Quelque chose qui n'etait pas la avant.\""));
        player.sendMessage(new TextComponentString(
            TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC
            + "- Voss, Carnet Vol. IV"));
        player.sendMessage(new TextComponentString(""));
        player.sendMessage(new TextComponentString(
            TextFormatting.GREEN + "Cinq theoremes fusionnent dans ton sang."));
        player.sendMessage(new TextComponentString(
            TextFormatting.GRAY + "Sois prudent. Le retour sera dur."));
    }

    private static String formatTime(long seconds) {
        long m = seconds / 60;
        long s = seconds % 60;
        if (m > 0) return m + "m " + s + "s restantes";
        return s + "s restantes";
    }
}
