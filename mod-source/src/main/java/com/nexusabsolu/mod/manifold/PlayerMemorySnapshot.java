package com.nexusabsolu.mod.manifold;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Snapshot de la "memoire" d'un joueur : ses statistiques de jeu sous une
 * forme compacte pour les flashbacks NDE du Cartouche Manifold (Sprint 2 etape 2).
 *
 * Capture :
 *   - Top N mobs tues (par type)
 *   - Top N items craftes ou collectes
 *   - Liste des biomes visites (ResourceLocation)
 *   - Liste des dimensions visitees
 *   - Distance totale parcourue (cm)
 *   - Temps de jeu total (ticks)
 *
 * Le snapshot est cree CTR-COTE-SERVEUR a partir de StatisticsManagerServer
 * du joueur, puis serialise en NBT pour transmission au client (paquet) ou
 * sauvegarde dans WorldSavedData.
 *
 * Pendant la NDE, le client utilise le snapshot pour afficher des references
 * defilantes : icones d'items, noms de mobs/biomes, etc.
 *
 * Usage typique cote serveur :
 * <pre>
 *   PlayerMemorySnapshot snap = PlayerMemorySnapshot.captureFromPlayer(player);
 *   NBTTagCompound nbt = new NBTTagCompound();
 *   snap.writeToNBT(nbt);
 *   // Envoyer nbt via paquet ou sauver dans WorldSavedData
 * </pre>
 *
 * @since 1.0.361 (Sprint 2 etape 2 - NDE flashbacks)
 */
public class PlayerMemorySnapshot {

    /** Nombre max d'entrees gardees pour chaque categorie (top N). */
    public static final int MAX_MOBS_KILLED = 30;
    public static final int MAX_ITEMS_COLLECTED = 50;
    public static final int MAX_BIOMES_VISITED = 20;

    /** Map type mob -> nombre tues. Trie decroissant avant serialisation. */
    private final Map<String, Integer> mobsKilled = new HashMap<>();

    /** Map item -> nombre crafte/collecte. */
    private final Map<String, Integer> itemsCrafted = new HashMap<>();
    private final Map<String, Integer> itemsCollected = new HashMap<>();

    /** Liste de biomes traverses (ResourceLocation strings). */
    private final List<String> biomesVisited = new ArrayList<>();

    /** Liste de dimensions visitees (id integer en string). */
    private final List<String> dimensionsVisited = new ArrayList<>();

    /** Distance parcouru total (cm comme dans StatList). */
    private long totalDistanceCm = 0L;

    /** Temps de jeu total en ticks. */
    private long playTimeTicks = 0L;

    /** Nom du joueur capture (pour debug et display). */
    private String playerName = "";

    public PlayerMemorySnapshot() {}

    public Map<String, Integer> getMobsKilled() {
        return Collections.unmodifiableMap(mobsKilled);
    }

    public Map<String, Integer> getItemsCrafted() {
        return Collections.unmodifiableMap(itemsCrafted);
    }

    public Map<String, Integer> getItemsCollected() {
        return Collections.unmodifiableMap(itemsCollected);
    }

    public List<String> getBiomesVisited() {
        return Collections.unmodifiableList(biomesVisited);
    }

    public List<String> getDimensionsVisited() {
        return Collections.unmodifiableList(dimensionsVisited);
    }

    public long getTotalDistanceCm() { return totalDistanceCm; }
    public long getPlayTimeTicks()   { return playTimeTicks; }
    public String getPlayerName()    { return playerName; }

    /**
     * Capture un snapshot des statistiques du joueur depuis le serveur.
     * Cette methode DOIT etre appelee cote serveur (acces a StatisticsManagerServer).
     *
     * @param player joueur cible (doit etre EntityPlayerMP)
     * @return snapshot non-null (potentiellement vide si player n'a pas de stats)
     */
    public static PlayerMemorySnapshot captureFromPlayer(EntityPlayer player) {
        PlayerMemorySnapshot snap = new PlayerMemorySnapshot();
        if (player == null) return snap;

        snap.playerName = player.getName();

        // Acces aux stats : seulement disponible cote serveur
        if (!(player instanceof EntityPlayerMP)) {
            return snap;
        }
        EntityPlayerMP mp = (EntityPlayerMP) player;
        StatisticsManagerServer stats = mp.getStatFile();
        if (stats == null) return snap;

        // === Mobs killed ===
        // StatList.MOB_KILLS_COUNT est l'indice global
        // Pour les types specifiques, on parcourt StatList.ENTITY_KILLED_STATS
        // Note : les ENTITY_KILLED_STATS ne sont pas exposes simplement en 1.12.2.
        // On utilise le total mob_kills comme proxy pour l'instant.
        int totalMobsKilled = stats.readStat(StatList.MOB_KILLS);
        if (totalMobsKilled > 0) {
            snap.mobsKilled.put("nexusabsolu.total_mobs", totalMobsKilled);
        }

        // === Items crafted / collected ===
        // Pour chaque item, on regarde StatList.getCraftStats(item) et
        // StatList.getObjectUseStats(item). En 1.12.2 c'est rate-limite par
        // l'absence d'API directe. On itere sur tous les items vanilla connus
        // et on garde ceux dont le count > 0.
        for (StatBase stat : StatList.ALL_STATS) {
            String name = stat.statId;
            // Filtre : on s'interesse aux stats "stat.craftItem.*" et "stat.useItem.*"
            int value = stats.readStat(stat);
            if (value <= 0) continue;
            if (name.startsWith("stat.craftItem.")) {
                String item = name.substring("stat.craftItem.".length());
                snap.itemsCrafted.put(item, value);
            } else if (name.startsWith("stat.useItem.")) {
                String item = name.substring("stat.useItem.".length());
                snap.itemsCollected.put(item, value);
            }
        }
        snap.trimToTopN();

        // === Distance totale ===
        // Sommation des differents StatList de distance (walked, sprinted, etc.)
        long distance = 0L;
        distance += stats.readStat(StatList.WALK_ONE_CM);
        distance += stats.readStat(StatList.SPRINT_ONE_CM);
        distance += stats.readStat(StatList.SWIM_ONE_CM);
        distance += stats.readStat(StatList.FLY_ONE_CM);
        distance += stats.readStat(StatList.HORSE_ONE_CM);
        distance += stats.readStat(StatList.BOAT_ONE_CM);
        snap.totalDistanceCm = distance;

        // === Temps de jeu ===
        snap.playTimeTicks = stats.readStat(StatList.PLAY_ONE_MINUTE);

        // === Biomes / dimensions ===
        // Pas accessibles via StatisticsManagerServer en 1.12.2 par defaut.
        // Sera complete via un BiomeTracker custom Sprint 2.5.

        return snap;
    }

    /**
     * Garde uniquement les top N entrees par valeur decroissante.
     */
    private void trimToTopN() {
        trimMap(mobsKilled, MAX_MOBS_KILLED);
        trimMap(itemsCrafted, MAX_ITEMS_COLLECTED);
        trimMap(itemsCollected, MAX_ITEMS_COLLECTED);
    }

    private static void trimMap(Map<String, Integer> map, int maxSize) {
        if (map.size() <= maxSize) return;
        // Trier par valeur decroissante
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        map.clear();
        for (int i = 0; i < maxSize && i < entries.size(); i++) {
            Map.Entry<String, Integer> e = entries.get(i);
            map.put(e.getKey(), e.getValue());
        }
    }

    /**
     * Manuelle add : pour les biomes/dimensions trackes par un autre systeme.
     */
    public void addBiomeVisited(Biome biome) {
        if (biome == null) return;
        ResourceLocation loc = biome.getRegistryName();
        if (loc == null) return;
        String s = loc.toString();
        if (!biomesVisited.contains(s) && biomesVisited.size() < MAX_BIOMES_VISITED) {
            biomesVisited.add(s);
        }
    }

    public void addDimensionVisited(int dimId) {
        String s = String.valueOf(dimId);
        if (!dimensionsVisited.contains(s)) {
            dimensionsVisited.add(s);
        }
    }

    // === Serialisation NBT ===

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("playerName", playerName);
        nbt.setLong("totalDistanceCm", totalDistanceCm);
        nbt.setLong("playTimeTicks", playTimeTicks);

        nbt.setTag("mobsKilled", mapToNBT(mobsKilled));
        nbt.setTag("itemsCrafted", mapToNBT(itemsCrafted));
        nbt.setTag("itemsCollected", mapToNBT(itemsCollected));

        NBTTagList biomes = new NBTTagList();
        for (String b : biomesVisited) {
            NBTTagCompound t = new NBTTagCompound();
            t.setString("v", b);
            biomes.appendTag(t);
        }
        nbt.setTag("biomesVisited", biomes);

        NBTTagList dims = new NBTTagList();
        for (String d : dimensionsVisited) {
            NBTTagCompound t = new NBTTagCompound();
            t.setString("v", d);
            dims.appendTag(t);
        }
        nbt.setTag("dimensionsVisited", dims);

        return nbt;
    }

    public static PlayerMemorySnapshot readFromNBT(NBTTagCompound nbt) {
        PlayerMemorySnapshot s = new PlayerMemorySnapshot();
        if (nbt == null) return s;

        s.playerName = nbt.getString("playerName");
        s.totalDistanceCm = nbt.getLong("totalDistanceCm");
        s.playTimeTicks = nbt.getLong("playTimeTicks");

        nbtToMap(nbt.getCompoundTag("mobsKilled"), s.mobsKilled);
        nbtToMap(nbt.getCompoundTag("itemsCrafted"), s.itemsCrafted);
        nbtToMap(nbt.getCompoundTag("itemsCollected"), s.itemsCollected);

        NBTTagList biomes = nbt.getTagList("biomesVisited", 10);
        for (int i = 0; i < biomes.tagCount(); i++) {
            s.biomesVisited.add(biomes.getCompoundTagAt(i).getString("v"));
        }
        NBTTagList dims = nbt.getTagList("dimensionsVisited", 10);
        for (int i = 0; i < dims.tagCount(); i++) {
            s.dimensionsVisited.add(dims.getCompoundTagAt(i).getString("v"));
        }

        return s;
    }

    private static NBTTagCompound mapToNBT(Map<String, Integer> map) {
        NBTTagCompound nbt = new NBTTagCompound();
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            // Sanitize key for NBT (remove characters not allowed)
            String k = e.getKey().replace(':', '_').replace('.', '_');
            nbt.setInteger(k, e.getValue());
        }
        return nbt;
    }

    private static void nbtToMap(NBTTagCompound nbt, Map<String, Integer> map) {
        if (nbt == null) return;
        for (String k : nbt.getKeySet()) {
            map.put(k, nbt.getInteger(k));
        }
    }

    /**
     * @return total nombre d'entrees pour debug (mobs + items).
     */
    public int getTotalEntries() {
        return mobsKilled.size() + itemsCrafted.size()
            + itemsCollected.size() + biomesVisited.size()
            + dimensionsVisited.size();
    }
}
