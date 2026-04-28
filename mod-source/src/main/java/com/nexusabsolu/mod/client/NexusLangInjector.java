package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.NexusAbsoluMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Force le chargement des traductions du mod dans LanguageMap.
 *
 * Pour une raison non identifiée (probablement bug Forge ou conflit
 * avec un autre mod du pack), Forge ne charge pas correctement
 * les fichiers lang/en_us.lang et lang/fr_fr.lang du mod, meme
 * lorsqu'ils sont presents dans le jar.
 * Resultat : tous les items et blocs du mod affichent leur cle
 * brute (item.nexusabsolu.X.name) au lieu du nom traduit.
 *
 * Ce listener bulldozer :
 * 1. Lit le lang file approprie directement depuis le jar
 * 2. Inject toutes les cles dans LanguageMap.languageList via reflection
 * 3. Se relance a chaque rechargement de ressources (F3+T, changement
 *    de langue, etc.)
 */
@SideOnly(Side.CLIENT)
public class NexusLangInjector implements IResourceManagerReloadListener {

    private static final String[] LANG_FALLBACK = {"en_us"};

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        injectAll();
    }

    public static void injectAll() {
        try {
            Map<String, String> langMap = getLanguageListField();
            if (langMap == null) {
                NexusAbsoluMod.LOGGER.error("[NexusLang] Cannot access LanguageMap.languageList");
                return;
            }

            String currentCode = "en_us";
            try {
                currentCode = Minecraft.getMinecraft()
                        .getLanguageManager().getCurrentLanguage().getLanguageCode();
            } catch (Exception ignored) {}

            int total = 0;
            // Charger d'abord en_us comme fallback, puis langue courante par dessus
            for (String code : LANG_FALLBACK) {
                total += loadLangInto(code, langMap);
            }
            if (!isFallback(currentCode)) {
                total += loadLangInto(currentCode, langMap);
            }

            NexusAbsoluMod.LOGGER.info("[NexusLang] Injected " + total
                    + " translation entries (locale: " + currentCode + ")");
        } catch (Exception e) {
            NexusAbsoluMod.LOGGER.error("[NexusLang] Injection failed", e);
        }
    }

    private static boolean isFallback(String code) {
        for (String f : LANG_FALLBACK) {
            if (f.equals(code)) return true;
        }
        return false;
    }

    private static int loadLangInto(String langCode, Map<String, String> target) {
        String path = "/assets/nexusabsolu/lang/" + langCode + ".lang";
        InputStream is = NexusLangInjector.class.getResourceAsStream(path);
        if (is == null) {
            NexusAbsoluMod.LOGGER.warn("[NexusLang] Lang file not found: " + path);
            return 0;
        }
        int count = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String s = line.trim();
                if (s.isEmpty() || s.startsWith("#") || s.startsWith("!")) continue;
                int eq = s.indexOf('=');
                if (eq <= 0) continue;
                String k = s.substring(0, eq).trim();
                String v = s.substring(eq + 1).trim();
                target.put(k, v);
                count++;
            }
        } catch (Exception e) {
            NexusAbsoluMod.LOGGER.error("[NexusLang] Read error " + path, e);
        }
        return count;
    }

    /**
     * Recupere la map interne de LanguageMap.
     * En 1.12.2 le champ s'appelle "languageList" (MCP) ou "field_135032_a" (SRG).
     */
    @SuppressWarnings("unchecked")
    private static Map<String, String> getLanguageListField() {
        LanguageMap lm = LanguageMap.getInstance();
        // Tenter MCP puis SRG
        String[] names = {"languageList", "field_135032_a"};
        for (String name : names) {
            try {
                Field f = LanguageMap.class.getDeclaredField(name);
                f.setAccessible(true);
                Object val = f.get(lm);
                if (val instanceof Map) {
                    return (Map<String, String>) val;
                }
            } catch (NoSuchFieldException ignored) {
            } catch (Exception e) {
                NexusAbsoluMod.LOGGER.error("[NexusLang] Field access " + name, e);
            }
        }
        // Fallback : iterer tous les Map declares
        try {
            for (Field f : LanguageMap.class.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    Object val = f.get(lm);
                    if (val instanceof Map) {
                        return (Map<String, String>) val;
                    }
                }
            }
        } catch (Exception e) {
            NexusAbsoluMod.LOGGER.error("[NexusLang] Field iteration", e);
        }
        return null;
    }
}
