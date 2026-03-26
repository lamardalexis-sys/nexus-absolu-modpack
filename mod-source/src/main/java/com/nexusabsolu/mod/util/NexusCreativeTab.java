package com.nexusabsolu.mod.util;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NexusCreativeTab extends CreativeTabs {
    public NexusCreativeTab() {
        super(Reference.MOD_ID);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack createIcon() {
        return new ItemStack(ModItems.NEXUS_ABSOLU);
    }
}
