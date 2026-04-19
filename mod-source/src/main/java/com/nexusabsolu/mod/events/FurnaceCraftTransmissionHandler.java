package com.nexusabsolu.mod.events;

import com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Event handler qui transmet le flag "enhanced" d'un Furnace Nexus vers son
 * ItemBlock lors du craft d'un furnace de tier superieur.
 *
 * Pattern:
 *   Player crafte un Gold Furnace avec un Iron Furnace Enhanced en ingredient
 *   -> le Gold Furnace resultant est automatiquement Enhanced (herite le kit)
 *
 * Detection :
 *   1. Au craft, check si l'output est un ItemBlock dont le bloc est BlockFurnaceNexus
 *   2. Parcourir tous les slots de la matrice de craft : si AU MOINS UN est
 *      aussi un furnace Nexus avec BlockEntityTag.enhanced=true,
 *      on tag l'output avec BlockEntityTag.enhanced=true.
 *
 * Le flag BlockEntityTag est automatiquement lu par Minecraft au placement
 * du bloc (via TileEntity.readFromNBT indirectement).
 */
public class FurnaceCraftTransmissionHandler {

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack output = event.crafting;
        if (output.isEmpty()) return;
        if (!isFurnaceNexusItem(output)) return;

        IInventory craftMatrix = event.craftMatrix;
        if (craftMatrix == null) return;

        // Check si un des ingredients est un furnace enhanced
        boolean hasEnhancedInput = false;
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack ingr = craftMatrix.getStackInSlot(i);
            if (ingr.isEmpty()) continue;
            if (!isFurnaceNexusItem(ingr)) continue;
            if (isStackEnhanced(ingr)) {
                hasEnhancedInput = true;
                break;
            }
        }

        if (hasEnhancedInput) {
            // Tag l'output avec BlockEntityTag.enhanced = true
            setStackEnhanced(output, true);
        }
    }

    /** True si l'ItemStack est un ItemBlock d'un BlockFurnaceNexus. */
    private static boolean isFurnaceNexusItem(ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof ItemBlock)) return false;
        return ((ItemBlock) item).getBlock() instanceof BlockFurnaceNexus;
    }

    /** Lit BlockEntityTag.enhanced sur un ItemStack. */
    private static boolean isStackEnhanced(ItemStack stack) {
        if (!stack.hasTagCompound()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey("BlockEntityTag")) return false;
        return tag.getCompoundTag("BlockEntityTag").getBoolean("enhanced");
    }

    /** Ecrit BlockEntityTag.enhanced sur un ItemStack (cree les tags si absent). */
    private static void setStackEnhanced(ItemStack stack, boolean enhanced) {
        NBTTagCompound itemTag = stack.hasTagCompound()
            ? stack.getTagCompound() : new NBTTagCompound();
        NBTTagCompound beTag = itemTag.hasKey("BlockEntityTag")
            ? itemTag.getCompoundTag("BlockEntityTag") : new NBTTagCompound();
        beTag.setBoolean("enhanced", enhanced);
        itemTag.setTag("BlockEntityTag", beTag);
        stack.setTagCompound(itemTag);
    }
}
