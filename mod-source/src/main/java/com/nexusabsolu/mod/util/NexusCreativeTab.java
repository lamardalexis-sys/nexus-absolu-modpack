package com.nexusabsolu.mod.util;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class NexusCreativeTab extends CreativeTabs {
    public NexusCreativeTab() {
        super(Reference.MOD_ID);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(ModItems.NEXUS_ABSOLU);
    }
}
