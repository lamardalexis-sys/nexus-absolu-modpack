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
 * Grabber Voss — "Sac du Sujet 46".
 *
 * Age 2 intro item, rewarded at qid 100. This first Sprint 1 version is a lore
 * placeholder: single-stack Java item with narrative tooltip, registered in
 * the mod's creative tab. A full 36-slot custom inventory GUI (see
 * quests-source/SPRINT1_TECHNICAL_PLAN.md section 4) is explicitly deferred to
 * Sprint 1.5 to keep the first pass reviewable and compilable on the first try.
 */
public class ItemGrabberVoss extends ItemBase {

    public ItemGrabberVoss(String name) {
        super(name);
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + "Sac du Sujet 46");
        tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC
            + "\"Il y a plus dedans qu'il n'y parait.\"");
        tooltip.add(TextFormatting.DARK_PURPLE + "Inventaire etendu (a venir Sprint 1.5)");
    }
}
