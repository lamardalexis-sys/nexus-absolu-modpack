package com.nexusabsolu.mod.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Cartouche Usee — casing vide d'une Cartouche Manifold injectee.
 *
 * Recyclable : peut etre re-utilisee dans le pipeline d'encartouchage final
 * (sterilisation autoclave + Argon + scellage) pour faire une nouvelle
 * Cartouche Manifold. Economie de Titane/Iridium.
 *
 * Pas d'effet au right-click — c'est juste un consommable du pipeline.
 */
public class ItemCartoucheUsed extends ItemBase {

    public ItemCartoucheUsed() {
        super("cartouche_used");
        setMaxStackSize(16);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world,
                                List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC
            + "Casing vide. Encore tiede.");
        tooltip.add("");
        tooltip.add(TextFormatting.YELLOW + "Recyclable :");
        tooltip.add(TextFormatting.GRAY + "Sterilisation + Argon + scellage");
        tooltip.add(TextFormatting.GRAY + "= nouvelle Cartouche Manifold");
    }
}
