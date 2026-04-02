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
    private final String dropType;

    public ItemPioche(String name, ToolMaterial material, int dustMultiplier) {
        this(name, material, dustMultiplier, "default");
    }

    public ItemPioche(String name, ToolMaterial material, int dustMultiplier, String dropType) {
        super(material);
        this.dustMultiplier = dustMultiplier;
        this.dropType = dropType;
        setUnlocalizedName(Reference.MOD_ID + "." + name);
        setRegistryName(Reference.MOD_ID, name);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setMaxDamage(material.getMaxUses());
        ModItems.ITEMS.add(this);
    }

    public int getDustMultiplier() { return dustMultiplier; }
    public String getDropType() { return dropType; }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
