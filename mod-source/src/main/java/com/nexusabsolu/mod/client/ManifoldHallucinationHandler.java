package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.events.ManifoldEffectHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Hallucinations visuelles cote CLIENT uniquement : pendant l'injection,
 * environ 1 mob sur 3 est rendu comme un bloc aleatoire qui flotte a sa place.
 *
 * IMPORTANT :
 *  - Le mob existe TOUJOURS cote serveur (hitbox, IA, degats normaux)
 *  - C'est UNIQUEMENT le rendu visuel qui change pour le joueur injecte
 *  - Les autres joueurs sur le serveur voient les mobs normalement
 *  - Le bloc affiche est STABLE par UUID (le meme mob = le meme bloc pendant
 *    toute la duree de l'injection) — sinon ca flickerait entre 2 frames
 *
 * Conformement au lore Manifoldine + DMT : "tu vois ce que tu penses dans le reel".
 * Le mob est toujours la (donc il peut te tuer), mais ton cerveau le voit comme
 * autre chose. C'est la dissociation entre realite physique et perception.
 */
@SideOnly(Side.CLIENT)
public class ManifoldHallucinationHandler {

    /**
     * Palette de blocs "DMT-friendly" : couleurs vives, materiaux qui sortent
     * de l'ordinaire. Les mobs deviennent ces blocs aleatoirement.
     */
    private static final IBlockState[] HALLUCINATION_BLOCKS = {
        Blocks.REDSTONE_BLOCK.getDefaultState(),
        Blocks.LAPIS_BLOCK.getDefaultState(),
        Blocks.EMERALD_BLOCK.getDefaultState(),
        Blocks.GOLD_BLOCK.getDefaultState(),
        Blocks.DIAMOND_BLOCK.getDefaultState(),
        Blocks.PURPUR_BLOCK.getDefaultState(),
        Blocks.GLASS.getDefaultState(),
        Blocks.GLOWSTONE.getDefaultState(),
        Blocks.SEA_LANTERN.getDefaultState(),
        Blocks.PRISMARINE.getDefaultState(),
        Blocks.MAGMA.getDefaultState(),
        Blocks.OBSIDIAN.getDefaultState()
    };

    /** Probabilite (0.0-1.0) qu'un mob donne soit halluciné. */
    private static final double HALLUCINATION_RATE = 0.33;

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre<?> event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer self = mc.player;
        if (self == null) return;

        // Phase check : actif uniquement pendant injection
        long now = self.world.getTotalWorldTime();
        // Hallucinations actives pendant les stages 3, 4, 5 (pas l'onset/saturation)
        int stage = ManifoldClientState.getCurrentStage(now);
        if (stage != ManifoldEffectHandler.STAGE_3_GEOMETRIC
            && stage != ManifoldEffectHandler.STAGE_4_HYPERSPACE
            && stage != ManifoldEffectHandler.STAGE_5_PEAK) {
            return;
        }

        Entity entity = event.getEntity();

        // Pas d'hallucination sur les joueurs (sinon en multi tu vois plus
        // tes potes — ca casse la coop)
        if (entity instanceof EntityPlayer) return;

        // Hash stable du UUID de l'entite → determine si halluciné + quel bloc
        long uuidHash = entity.getUniqueID().getMostSignificantBits()
                      ^ entity.getUniqueID().getLeastSignificantBits();
        // Le hash module sur 100 te donne un nombre stable [0,99]
        int rollHash = (int)(Math.abs(uuidHash) % 100);
        if (rollHash >= (int)(HALLUCINATION_RATE * 100)) {
            return;  // Ce mob est dans le 67% non halluciné
        }

        // Choix du bloc — autre hash dimension du UUID
        int blockIdx = (int)(Math.abs(uuidHash >> 8) % HALLUCINATION_BLOCKS.length);
        IBlockState blockState = HALLUCINATION_BLOCKS[blockIdx];

        // === Render le bloc a la place du mob ===
        // Annule le rendu normal du mob
        event.setCanceled(true);

        double x = event.getX();
        double y = event.getY();
        double z = event.getZ();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        // Rotation lente du bloc (effet "flottant" hallucinatoire)
        float rotation = (now % 360L) * 2.0F;
        GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);

        // Centre le bloc sur la position du mob (offset -0.5 pour que le
        // centre du bloc soit exactement ou serait le centre du mob)
        // On scale aussi un peu plus gros que le mob d'origine pour effet "presence"
        float scale = 1.2F;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-0.5F, 0.0F, -0.5F);

        // Rendering du bloc (utilise le brightness 1.0 = full bright pour
        // que le bloc soit visible meme la nuit / dans les grottes)
        try {
            mc.getBlockRendererDispatcher().renderBlockBrightness(blockState, 1.0F);
        } catch (Exception e) {
            // Si erreur de rendering pour ce bloc particulier (mod block etc),
            // on annule l'annulation pour que le mob revienne au rendu normal
            event.setCanceled(false);
        }

        GlStateManager.popMatrix();
    }
}
