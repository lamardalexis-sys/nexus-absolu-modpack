package com.nexusabsolu.mod.events;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Event handler qui desactive l'interaction avec les 6 Essence Furnaces de
 * Mystical Agriculture (Inferium -> Ultimate).
 *
 * Contexte (v1.0.297, demande Alexis) :
 *   L'Ultimate Furnace est utilise comme ingredient de craft de l'Infinite
 *   Furnace Nexus (T9). Pour debloquer l'Ultimate, il faut crafter toute la
 *   chaine Inferium -> Prudentium -> Intermedium -> Superium -> Supremium ->
 *   Ultimate. Mais Alexis veut que ces 6 furnaces MA ne servent PAS comme
 *   alternatives aux Furnaces Voss Nexus - ils existent uniquement comme
 *   etapes de craft obligatoires.
 *
 * Solution implementee :
 *   - Les 6 items restent craftables normalement (progression MA visible)
 *   - Ils peuvent etre poses (bloc physique, visuel) et casses
 *   - MAIS le clic-droit dessus est CANCEL par cet handler
 *   - Un message apparait dans l'actionbar : "Desactive dans Nexus Absolu"
 *
 * Impact :
 *   - Les joueurs voient les fours MA comme 'etapes tremplin' vers l'Infinite
 *   - Pas d'alternative parallele qui ferait concurrence aux Fourneaux Voss
 *   - Zero impact sur autres mods ou features MA (seeds, essences, etc.)
 *
 * Events utilises :
 *   - PlayerInteractEvent.RightClickBlock (main et offhand couvertes)
 *   - Canceled en setting setResult(Result.DENY) + setCanceled(true)
 *
 * Note :
 *   Le design cancel-silencieux preserve le jeu si un autre mod essaie
 *   d'ouvrir le GUI via API (pas juste via clic-droit direct).
 */
public class MAFurnaceBlockerHandler {

    /**
     * Liste des 6 blocs MA a bloquer. Utilise ResourceLocation plutot que
     * Block directement pour eviter un hard-depend sur MA (si MA absent du
     * pack, les ResourceLocation restent valides mais ne matchent rien =
     * pas de crash).
     */
    private static final Set<ResourceLocation> BLOCKED_FURNACES = new HashSet<>(Arrays.asList(
        new ResourceLocation("mysticalagriculture", "inferium_furnace"),
        new ResourceLocation("mysticalagriculture", "prudentium_furnace"),
        new ResourceLocation("mysticalagriculture", "intermedium_furnace"),
        new ResourceLocation("mysticalagriculture", "superium_furnace"),
        new ResourceLocation("mysticalagriculture", "supremium_furnace"),
        new ResourceLocation("mysticalagriculture", "ultimate_furnace")
    ));

    /**
     * Cooldown pour ne pas spammer le message quand le joueur clique
     * rapidement. Cle : player UUID, value : timestamp du dernier message.
     */
    private static final java.util.Map<java.util.UUID, Long> LAST_MSG_TIME = new java.util.HashMap<>();
    private static final long MSG_COOLDOWN_MS = 1500L;

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) return;  // server-side seulement, suffit pour cancel

        Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
        if (block == null) return;

        ResourceLocation id = block.getRegistryName();
        if (id == null || !BLOCKED_FURNACES.contains(id)) return;

        // Cancel l'interaction
        event.setUseBlock(Result.DENY);
        event.setUseItem(Result.DENY);
        event.setCanceled(true);

        // Message actionbar (throttle par player UUID)
        if (event.getEntityPlayer() != null) {
            java.util.UUID pid = event.getEntityPlayer().getUniqueID();
            long now = System.currentTimeMillis();
            Long last = LAST_MSG_TIME.get(pid);
            if (last == null || (now - last) > MSG_COOLDOWN_MS) {
                LAST_MSG_TIME.put(pid, now);
                String msg = TextFormatting.RED + "Desactive dans Nexus Absolu"
                    + TextFormatting.GRAY + " - utilisez les Fourneaux Voss";
                event.getEntityPlayer().sendStatusMessage(
                    new TextComponentString(msg), true);  // true = actionbar
            }
        }
    }
}
