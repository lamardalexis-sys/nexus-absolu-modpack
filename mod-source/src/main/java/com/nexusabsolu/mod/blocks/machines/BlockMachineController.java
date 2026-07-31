package com.nexusabsolu.mod.blocks.machines;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Bloc de facade des multiblocs de l'Age 4.
 *
 * Migre depuis ContentTweaker (Age4_L*_Multiblocs.zs) vers le mod, pour que
 * tout le contenu machine appartienne a Nexus Absolu : nom traduit, texture
 * versionnee avec le mod, onglet creatif.
 *
 * ATTENTION -- ce bloc n'est PAS le controleur fonctionnel de Modular
 * Machinery. Aucun fichier de config/modularmachinery/machinery/ ne declare
 * de binding vers un bloc custom : MM forme ses structures autour de son
 * propre blockcontroller. Ces blocs servent de facade et de repere visuel.
 * Les rendre reellement fonctionnels demanderait d'etendre l'API de MM.
 */
public class BlockMachineController extends Block implements IHasModel {

    public BlockMachineController(String name, float hardness, float resistance,
                                  int toolLevel, int lightValue) {
        super(Material.IRON);
        setUnlocalizedName(Reference.MOD_ID + "." + name);
        setRegistryName(Reference.MOD_ID, name);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHardness(hardness);
        setResistance(resistance);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", toolLevel);
        setLightLevel(lightValue / 15.0F);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
