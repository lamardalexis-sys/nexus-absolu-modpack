package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.manifold.PlayerMemorySnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Renderer des flashbacks NDE : affiche des references a ce que le joueur
 * a vecu dans sa save (items collectes, mobs tues, biomes visites) en
 * surimpression pendant la phase NDE du Cartouche Manifold.
 *
 * "Vie qui defile devant les yeux" - effet classique des NDE.
 *
 * INTEGRATION dans ManifoldOverlayHandler :
 * <pre>
 *   // Dans onRenderOverlay, pendant la NDE (progress 0.625 -> 0.99) :
 *   if (ManifoldClientMemory.hasSnapshot()) {
 *       ManifoldFlashbackRenderer.render(
 *           mc, w, h, now, progress,
 *           ManifoldClientMemory.getSnapshot());
 *   }
 * </pre>
 *
 * COMPORTEMENT VISUEL :
 *   - Pendant la NDE : 50-100 items defilent a l'ecran (icones + fade)
 *   - Mouvement aleatoire mais smooth (Perlin-like, deterministe via seed)
 *   - Alpha proportionnel a la progression NDE (fade in 0->0.7, fade out 0.85->1.0)
 *   - Tailles variees (0.5x a 2x) pour effet de profondeur
 *   - Texte (noms mobs/biomes) en surimpression sur certaines icones
 *
 * @since 1.0.363 (Sprint 2 etape 2 phase D)
 */
@SideOnly(Side.CLIENT)
public class ManifoldFlashbackRenderer {

    private static final int N_FLASHBACK_ITEMS = 40;  // items affiches simultanement
    private static final long TRIP_SEED = 0x4E455855L;  // "NEXU" en hex - pseudo-random deterministe

    /** Cache des items resolus depuis le snapshot (resolution couteuse, on cache). */
    private static List<ItemStack> cachedItems = null;
    private static long cachedSnapshotHash = 0L;

    /**
     * Rendu principal : a appeler pendant la NDE depuis le ManifoldOverlayHandler.
     *
     * @param mc Minecraft
     * @param w largeur ecran
     * @param h hauteur ecran
     * @param now tick courant
     * @param tripProgress progression du trip (0.0 -> 1.0)
     * @param snapshot le snapshot a afficher (non-null requis)
     */
    public static void render(Minecraft mc, int w, int h, long now,
                              float tripProgress, PlayerMemorySnapshot snapshot) {
        if (snapshot == null) return;
        // Active uniquement pendant la NDE
        if (tripProgress < 0.625f || tripProgress > 0.99f) return;

        // Compute alpha : fade in 0.625-0.70, plateau 0.70-0.85, fade out 0.85-0.99
        float alpha;
        if (tripProgress < 0.70f) {
            float t = (tripProgress - 0.625f) / (0.70f - 0.625f);
            alpha = t * t * (3f - 2f * t);  // smoothstep
        } else if (tripProgress < 0.85f) {
            alpha = 1.0f;
        } else {
            float t = (tripProgress - 0.85f) / (0.99f - 0.85f);
            alpha = 1.0f - (t * t * (3f - 2f * t));
        }
        if (alpha < 0.02f) return;

        // Resolution items (cache si snapshot inchange)
        long snapHash = snapshot.getTotalEntries();
        if (cachedItems == null || cachedSnapshotHash != snapHash) {
            cachedItems = resolveItemsFromSnapshot(snapshot);
            cachedSnapshotHash = snapHash;
        }

        if (cachedItems.isEmpty()) {
            // Pas d'items utilisables : essayer le texte des mobs/biomes
            renderTextFlashbacks(mc, w, h, now, snapshot, alpha);
            return;
        }

        // Render items en flottement
        renderItemFlashbacks(mc, w, h, now, alpha);

        // Texte en plus (mobs/biomes sur certaines positions)
        renderTextFlashbacks(mc, w, h, now, snapshot, alpha * 0.7f);
    }

    /**
     * Resoud les ItemStack depuis les map names du snapshot.
     * Filtre uniquement les items existants dans le registry vanilla/mods loaded.
     */
    private static List<ItemStack> resolveItemsFromSnapshot(PlayerMemorySnapshot snapshot) {
        List<ItemStack> items = new ArrayList<>();
        // Items collected en priorite, puis crafted
        for (String key : snapshot.getItemsCollected().keySet()) {
            ItemStack stack = resolveItem(key);
            if (stack != null && !stack.isEmpty()) items.add(stack);
        }
        for (String key : snapshot.getItemsCrafted().keySet()) {
            ItemStack stack = resolveItem(key);
            if (stack != null && !stack.isEmpty()) items.add(stack);
        }
        return items;
    }

    /**
     * Convertit un nom statId (genre "minecraft.cobblestone") en ItemStack.
     */
    private static ItemStack resolveItem(String statKey) {
        if (statKey == null || statKey.isEmpty()) return ItemStack.EMPTY;
        // Format StatList : "minecraft.dirt" -> "minecraft:dirt"
        String resourceName;
        int firstDot = statKey.indexOf('.');
        if (firstDot > 0) {
            resourceName = statKey.substring(0, firstDot) + ":"
                + statKey.substring(firstDot + 1).replace('.', '_');
        } else {
            resourceName = statKey;
        }
        try {
            ResourceLocation loc = new ResourceLocation(resourceName);
            Item item = Item.REGISTRY.getObject(loc);
            if (item == null) return ItemStack.EMPTY;
            return new ItemStack(item);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Rendu des items flottants.
     */
    private static void renderItemFlashbacks(Minecraft mc, int w, int h, long now, float alpha) {
        if (cachedItems.isEmpty()) return;

        RenderItem renderItem = mc.getRenderItem();
        Random rng = new Random(TRIP_SEED);

        // Activer alpha + lumineux pour les icones
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        RenderHelper.enableGUIStandardItemLighting();

        for (int i = 0; i < N_FLASHBACK_ITEMS; i++) {
            // Position deterministe (seed = i + tick block)
            float xt = rng.nextFloat();
            float yt = rng.nextFloat();
            float scale = 0.5f + rng.nextFloat() * 1.5f;
            float speed = 0.3f + rng.nextFloat() * 0.7f;

            // Mouvement vertical lent (defile vers le bas)
            float yOffset = ((now * speed * 0.2f) % (h + 80f)) - 40f;
            float x = xt * w;
            float y = yt * h + yOffset;
            // Wrap-around
            while (y > h + 16) y -= (h + 32);

            // Item depuis le cache
            ItemStack stack = cachedItems.get(i % cachedItems.size());
            if (stack.isEmpty()) continue;

            // Render avec scale
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(scale, scale, 1.0f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
            try {
                renderItem.renderItemAndEffectIntoGUI(stack, 0, 0);
            } catch (Exception e) {
                // Some items might fail to render (mod issues), skip
            }
            GlStateManager.popMatrix();
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /**
     * Rendu du texte (mobs / biomes / dimensions visites) en surimpression.
     */
    private static void renderTextFlashbacks(Minecraft mc, int w, int h, long now,
                                              PlayerMemorySnapshot snapshot, float alpha) {
        FontRenderer fr = mc.fontRenderer;
        if (fr == null) return;

        Random rng = new Random(TRIP_SEED + 1L);
        int baseColor = 0x00FFFFFF;
        int alphaInt = Math.min(255, (int) (alpha * 200f));

        // Texte mobs (max 5)
        int idx = 0;
        for (String mobKey : snapshot.getMobsKilled().keySet()) {
            if (idx >= 5) break;
            float x = rng.nextFloat() * (w - 100f);
            float y = rng.nextFloat() * h;
            float yOffset = ((now * 0.5f) % (h + 60f)) - 30f;
            float finalY = (y + yOffset) % h;

            int color = (alphaInt << 24) | baseColor;
            String txt = "Tue: " + simplifyKey(mobKey);
            fr.drawString(txt, (int) x, (int) finalY, color);
            idx++;
        }

        // Texte biomes (max 5)
        int idx2 = 0;
        for (String biome : snapshot.getBiomesVisited()) {
            if (idx2 >= 5) break;
            float x = rng.nextFloat() * (w - 100f);
            float y = rng.nextFloat() * h;
            float yOffset = ((now * 0.4f + 100f) % (h + 60f)) - 30f;
            float finalY = (y + yOffset) % h;

            int color = (alphaInt << 24) | 0x00B0FF00;
            String txt = "Visite: " + simplifyKey(biome);
            fr.drawString(txt, (int) x, (int) finalY, color);
            idx2++;
        }
    }

    /** "minecraft:cobblestone" -> "cobblestone". */
    private static String simplifyKey(String key) {
        if (key == null) return "?";
        int colon = key.lastIndexOf(':');
        if (colon < 0) colon = key.lastIndexOf('_');
        return colon >= 0 ? key.substring(colon + 1) : key;
    }

    /** Force le re-cache (utile en dev). */
    public static void invalidateCache() {
        cachedItems = null;
        cachedSnapshotHash = 0L;
    }
}
