package com.nexusabsolu.mod.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGrit extends ItemBase {
    private final String metalName;

    public ItemGrit(String name, String metalName) {
        super(name);
        this.metalName = metalName;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + "Extrait des murs de la Compact Machine");
        tooltip.add(TextFormatting.DARK_GRAY + "Fond dans le Purificateur de Voss");
    }
}
