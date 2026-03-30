package com.nexusabsolu.mod.items;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemCompose extends Item implements IHasModel {

    private final String tier;
    private final int rfPerTick;

    public ItemCompose(String name, String tier, int rfPerTick) {
        this.tier = tier;
        this.rfPerTick = rfPerTick;
        setUnlocalizedName(Reference.MOD_ID + "." + name);
        setRegistryName(Reference.MOD_ID, name);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setMaxStackSize(64);
        ModItems.ITEMS.add(this);
    }

    public String getTier() { return tier; }
    public int getRfPerTick() { return rfPerTick; }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add("\u00a77Compose Energetique - Tier " + tier);
        tooltip.add("\u00a7dBase: " + rfPerTick + " RF/t par bloc");
        tooltip.add("\u00a7o\u00a789x = 1 Bloc de Compose " + tier);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
