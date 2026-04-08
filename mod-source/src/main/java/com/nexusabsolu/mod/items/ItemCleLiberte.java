package com.nexusabsolu.mod.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Cle de Liberte - inactive.
 *
 * Given to the player as an Age 1 reward. Holds no power on its own.
 * To activate: hold it while right-clicking the Ecran de Controle of
 * a complete and fully-charged Portail Voss. The structure drains its
 * energy and fluid, and the key transforms into a Cle de Liberte Activee
 * (see ItemCleLiberteActivee).
 */
public class ItemCleLiberte extends ItemBase {

    public ItemCleLiberte() {
        super("cle_liberte");
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world,
                                List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + "Une cle ancienne, froide au toucher.");
        tooltip.add(TextFormatting.DARK_GRAY + "Elle attend quelque chose.");
        tooltip.add("");
        tooltip.add(TextFormatting.DARK_PURPLE + "Active-la sur un Portail Voss charge.");
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return false;
    }
}
