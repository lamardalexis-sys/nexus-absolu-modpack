package com.nexusabsolu.mod.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Ampoule de Nuit Stellaire -- vide, elle ne se remplit qu'a certaines
 * conditions d'environnement.
 *
 * POURQUOI CET ITEM EXISTE
 * ------------------------
 * Le design de la Cyclisation Stellaire (L8.C.3) imposait quatre conditions :
 * Overworld, y >= 60, ciel degage, nuit (13000-23000 ticks). Elles etaient
 * ecrites dans cyclo_manifoldine_cyclization.json sous forme de requirements
 * "dimension", "position", "weather" et "time".
 *
 * Ces types n'existent pas dans Modular Machinery 1.11.1, qui n'enregistre
 * que item, fluid, energy, gas et duration. La recette entiere levait une
 * JsonParseException et etait silencieusement ecartee au chargement -- donc
 * la manifoldine_brute n'etait produite par rien et la ligne L8 etait coupee.
 *
 * Le gating est donc deporte ici : les conditions sont verifiees au moment
 * de remplir l'ampoule, et le Cyclisateur se contente de consommer une
 * ampoule pleine. Meme contrainte pour le joueur, mais exprimee dans un type
 * que MM comprend.
 *
 * A SUPPRIMER si le pack migre vers Modular Machinery Community Edition et
 * que CE fournit de vrais requirements d'environnement.
 */
public class ItemAmpouleNuitStellaire extends ItemBase {

    private static final int NIGHT_START = 13000;
    private static final int NIGHT_END   = 23000;
    private static final int MIN_HEIGHT  = 60;

    public ItemAmpouleNuitStellaire() {
        super("ampoule_nuit_stellaire_vide");
        setMaxStackSize(16);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);

        if (world.isRemote) {
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, held);
        }

        BlockPos pos = player.getPosition();

        if (world.provider.getDimension() != 0) {
            deny(player, "Le ciel d'ici n'est pas le bon.");
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, held);
        }
        if (pos.getY() < MIN_HEIGHT) {
            deny(player, "Trop bas. Il faut au moins " + MIN_HEIGHT + " metres.");
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, held);
        }
        if (!world.canSeeSky(pos)) {
            deny(player, "Quelque chose bloque le ciel.");
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, held);
        }
        if (world.isRaining() || world.isThundering()) {
            deny(player, "Les nuages font ecran.");
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, held);
        }
        long t = world.getWorldTime() % 24000L;
        if (t < NIGHT_START || t > NIGHT_END) {
            deny(player, "Pas encore. Attendez la nuit, la vraie.");
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, held);
        }

        held.shrink(1);
        ItemStack filled = new ItemStack(
            com.nexusabsolu.mod.init.ModItems.AMPOULE_NUIT_STELLAIRE, 1);
        if (!player.inventory.addItemStackToInventory(filled)) {
            player.dropItem(filled, false);
        }
        world.playSound(null, pos, SoundEvents.BLOCK_GLASS_PLACE,
            SoundCategory.PLAYERS, 0.7F, 1.6F);
        player.sendMessage(new TextComponentString(
            TextFormatting.AQUA + "L'ampoule se remplit d'une lumiere froide."));
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, held);
    }

    private void deny(EntityPlayer player, String why) {
        player.sendMessage(new TextComponentString(TextFormatting.GRAY + why));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world,
                               List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + "Clic droit sous le ciel nocturne.");
        tooltip.add(TextFormatting.DARK_GRAY + "Overworld, y >= 60, ciel degage,");
        tooltip.add(TextFormatting.DARK_GRAY + "temps clair, entre 13000 et 23000.");
    }
}
