package com.nexusabsolu.mod.items;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPioche extends ItemPickaxe implements IHasModel {

    private final int dustMultiplier;

    public ItemPioche(String name, ToolMaterial material, int dustMultiplier) {
        super(material);
        this.dustMultiplier = dustMultiplier;
        setUnlocalizedName(Reference.MOD_ID + "." + name);
        setRegistryName(Reference.MOD_ID, name);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        ModItems.ITEMS.add(this);
    }

    public int getDustMultiplier() { return dustMultiplier; }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
