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

public class ItemFragment extends ItemBase {
    private final String loreText;
    private final String vossQuote;
    private final TextFormatting color;
    private final EnumParticleTypes particleType;

    public ItemFragment(String name, String loreText, String vossQuote,
                       TextFormatting color, EnumParticleTypes particleType) {
        super(name);
        this.loreText = loreText;
        this.vossQuote = vossQuote;
        this.color = color;
        this.particleType = particleType;
        setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true; // Enchantment glint — all fragments shimmer
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(color + loreText);
        tooltip.add("");
        tooltip.add(TextFormatting.ITALIC + "" + TextFormatting.DARK_GRAY + "\"" + vossQuote + "\"");
        tooltip.add(TextFormatting.DARK_GRAY + "  — Dr. E. Voss");
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
        // Emit particles when held
        if (world.isRemote && isHeld && particleType != null && world.rand.nextInt(5) == 0) {
            double x = entity.posX + (world.rand.nextDouble() - 0.5) * 1.5;
            double y = entity.posY + 1.0 + world.rand.nextDouble() * 0.5;
            double z = entity.posZ + (world.rand.nextDouble() - 0.5) * 1.5;
            world.spawnParticle(particleType, x, y, z, 0, 0.05, 0);
        }
    }
}
