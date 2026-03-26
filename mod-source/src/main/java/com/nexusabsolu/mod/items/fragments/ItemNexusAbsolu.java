package com.nexusabsolu.mod.items.fragments;

import com.nexusabsolu.mod.items.ItemBase;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemNexusAbsolu extends ItemBase {
    public ItemNexusAbsolu() {
        super("nexus_absolu");
        setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.LIGHT_PURPLE + "" + TextFormatting.BOLD + "Le Nexus Absolu");
        tooltip.add("");
        tooltip.add(TextFormatting.DARK_PURPLE + "Neuf verites. Un seul point de convergence.");
        tooltip.add("");
        tooltip.add(TextFormatting.ITALIC + "" + TextFormatting.GRAY + "\"Tu as trouve le Nexus.");
        tooltip.add(TextFormatting.ITALIC + "" + TextFormatting.GRAY + " Ou le Nexus t'a trouve.\"");
        tooltip.add(TextFormatting.DARK_GRAY + "  — Dr. Elias Nathaniel Voss");
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
        if (!world.isRemote) return;
        if (!isHeld) return;

        // Massive particle effect when held — portal + enchant + end rod
        for (int i = 0; i < 3; i++) {
            double angle = world.rand.nextDouble() * Math.PI * 2;
            double radius = 0.8 + world.rand.nextDouble() * 0.5;
            double x = entity.posX + Math.cos(angle) * radius;
            double y = entity.posY + 0.5 + world.rand.nextDouble() * 1.5;
            double z = entity.posZ + Math.sin(angle) * radius;
            world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z,
                (world.rand.nextDouble() - 0.5) * 0.2,
                world.rand.nextDouble() * 0.2,
                (world.rand.nextDouble() - 0.5) * 0.2);
        }

        if (world.rand.nextInt(3) == 0) {
            double x = entity.posX + (world.rand.nextDouble() - 0.5) * 2;
            double y = entity.posY + 1 + world.rand.nextDouble();
            double z = entity.posZ + (world.rand.nextDouble() - 0.5) * 2;
            world.spawnParticle(EnumParticleTypes.END_ROD, x, y, z, 0, 0.1, 0);
        }
    }
}
