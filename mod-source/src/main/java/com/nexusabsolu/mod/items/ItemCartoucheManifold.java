package com.nexusabsolu.mod.items;

import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
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
 * Serum neurochimique injectable qui fusionne les 5 theoremes de Voss
 * et fait sortir le joueur de la simulation.
 *
 * V1 : right-click → applique les potions vanilla cranked (3 minutes).
 *      Apres usage : transformee en CartoucheUsed (casing recyclable),
 *      cooldown 5 minutes pour eviter overdose.
 *
 * V2 (TODO) : Lifesteal absolu, aura toxique, particules custom.
 * V3 (TODO) : Lightning Chain, Bullet Time, Hard Reset.
 *
 * Stack size = 1, pas de glint (le glint vient des effets actifs).
 */
public class ItemCartoucheManifold extends ItemBase {

    private static final int DURATION_TICKS = 3600;   // 3 minutes
    private static final int COOLDOWN_TICKS = 6000;   // 5 minutes
    private static final String NBT_COOLDOWN = "manifold_cooldown_until";

    public ItemCartoucheManifold() {
        super("cartouche_manifold");
        setMaxStackSize(1);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;  // glint permanent — c'est une cartouche speciale
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
        tooltip.add(TextFormatting.GRAY + "Duree : " + TextFormatting.WHITE + "3 minutes");
        tooltip.add(TextFormatting.GRAY + "Cooldown : " + TextFormatting.WHITE + "5 minutes");
        tooltip.add(TextFormatting.DARK_RED + "Overdose = mort instantanee.");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player,
                                                     EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        // Client side : succes pour synchroniser
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        if (!(player instanceof EntityPlayerMP)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        // Cooldown check
        long now = world.getTotalWorldTime();
        long cooldownUntil = getCooldownUntil(player);
        if (now < cooldownUntil) {
            long remaining = (cooldownUntil - now) / 20;  // en secondes
            player.sendMessage(new TextComponentString(
                TextFormatting.DARK_RED + "Ton corps n'a pas encore metabolise la derniere injection. "
                + TextFormatting.GRAY + "(" + remaining + "s restantes)"));
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        // INJECTION
        injectCartouche((EntityPlayerMP) player, world);

        // Set cooldown
        setCooldownUntil(player, now + COOLDOWN_TICKS);

        // Consomme la cartouche, donne la cartouche usee
        stack.shrink(1);
        ItemStack used = new ItemStack(ModItems.CARTOUCHE_USED, 1);
        if (!player.inventory.addItemStackToInventory(used)) {
            player.dropItem(used, false);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private void injectCartouche(EntityPlayerMP player, World world) {
        // ---- SONS ----
        // ENDERMEN_TELEPORT colle au lore : warp dimensionnel = sortie simulation
        world.playSound(null, player.posX, player.posY, player.posZ,
            SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.5F, 0.5F);
        world.playSound(null, player.posX, player.posY, player.posZ,
            SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 2.0F);

        // ---- POTIONS VANILLA CRANKED ----
        // duration en ticks, amplifier 0=I, 1=II, 2=III, 3=IV, 4=V
        addEffect(player, MobEffects.STRENGTH, 4);          // X — vanilla cap is 5 (V), but we go epic
        addEffect(player, MobEffects.SPEED, 2);             // III
        addEffect(player, MobEffects.REGENERATION, 3);      // IV
        addEffect(player, MobEffects.RESISTANCE, 3);        // IV
        addEffect(player, MobEffects.HASTE, 4);             // V
        addEffect(player, MobEffects.JUMP_BOOST, 2);        // III
        addEffect(player, MobEffects.NIGHT_VISION, 0);
        addEffect(player, MobEffects.WATER_BREATHING, 0);
        addEffect(player, MobEffects.FIRE_RESISTANCE, 0);
        addEffect(player, MobEffects.ABSORPTION, 4);        // V (bonus 10 hearts)

        // ---- MESSAGES ----
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
            TextFormatting.GRAY + "Duree : "
            + TextFormatting.WHITE + "3 minutes."));
    }

    private static void addEffect(EntityLivingBase entity, net.minecraft.potion.Potion potion, int amplifier) {
        // showParticles=false pour pas spammer l'ecran
        // showIcon=true pour que l'effet apparaisse dans l'inventaire
        entity.addPotionEffect(new PotionEffect(potion, DURATION_TICKS, amplifier, false, true));
    }

    // ---- NBT helpers : cooldown stocke sur le joueur via tag global ----
    private static long getCooldownUntil(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        NBTTagCompound persisted = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        return persisted.getLong(NBT_COOLDOWN);
    }

    private static void setCooldownUntil(EntityPlayer player, long until) {
        NBTTagCompound data = player.getEntityData();
        NBTTagCompound persisted;
        if (data.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            persisted = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        } else {
            persisted = new NBTTagCompound();
            data.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        }
        persisted.setLong(NBT_COOLDOWN, until);
    }
}
