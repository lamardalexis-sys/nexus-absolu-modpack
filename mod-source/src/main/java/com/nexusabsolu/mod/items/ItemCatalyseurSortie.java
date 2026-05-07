package com.nexusabsolu.mod.items;

import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.util.PortailSortieValidator;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
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
 * Catalyseur de Sortie - the consumable portal igniter for Age 2 Phase 1->2.
 *
 * Right-click on any Obsidienne Voss block belonging to a valid 6x5 frame
 * (interior 4x3) -> the interior is filled with Portail Sortie Actif blocks
 * and the catalyser is consumed.
 *
 * If the clicked block is not Obsidienne Voss, or no valid frame can be
 * found, the player gets a feedback message and the item is NOT consumed.
 *
 * Sprint 3 - v1.0.350+
 * (Recipe : 9x9 Extended Crafting with TF boss trophies, Sprint 4)
 */
public class ItemCatalyseurSortie extends ItemBase {

    public ItemCatalyseurSortie() {
        super("catalyseur_sortie");
        setMaxStackSize(1);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;  // Enchanted glint
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world,
                                List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.LIGHT_PURPLE
            + "Concentre des essences de TF et du Vossium endgame.");
        tooltip.add(TextFormatting.DARK_GRAY + "\"La foret te laisse partir. Voss aussi.\"");
        tooltip.add("");
        tooltip.add(TextFormatting.YELLOW
            + "Clic droit sur un cadre Obsidienne Voss 4x4 pour allumer.");
        tooltip.add(TextFormatting.DARK_GRAY + "Usage unique.");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                       EnumHand hand, EnumFacing facing,
                                       float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            // Client-side: just signal SUCCESS so the server also runs.
            return EnumActionResult.SUCCESS;
        }

        ItemStack stack = player.getHeldItem(hand);

        // 1. The clicked block must be an Obsidienne Voss
        Block clicked = world.getBlockState(pos).getBlock();
        if (clicked != ModBlocks.OBSIDIENNE_VOSS) {
            player.sendMessage(new TextComponentString(
                TextFormatting.RED
                + "[Catalyseur] Doit etre utilise sur une Obsidienne Voss."));
            return EnumActionResult.FAIL;
        }

        // 2. Find a valid frame anchored to this block (any orientation)
        PortailSortieValidator.FrameResult result =
            PortailSortieValidator.findAnyFrame(world, pos);

        if (result == null) {
            player.sendMessage(new TextComponentString(
                TextFormatting.RED
                + "[Catalyseur] Cadre invalide. Verifie la structure 4x4 (interieur 2x2 vide)."));
            return EnumActionResult.FAIL;
        }

        // 3. Fill the interior with active portal blocks
        PortailSortieValidator.fillInterior(world, result.bottomLeft, result.axis);

        // 4. Effects + consume item
        world.playSound(null, pos,
            SoundEvents.BLOCK_PORTAL_TRIGGER,
            SoundCategory.BLOCKS, 1.0F, 0.7F);
        world.playSound(null, pos,
            SoundEvents.ENTITY_LIGHTNING_THUNDER,
            SoundCategory.WEATHER, 0.5F, 1.4F);

        player.sendMessage(new TextComponentString(
            TextFormatting.LIGHT_PURPLE + "" + TextFormatting.BOLD
            + "Le portail s'ouvre. Voss observe."));

        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }

        return EnumActionResult.SUCCESS;
    }
}
