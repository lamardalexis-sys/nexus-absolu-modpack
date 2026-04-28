package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.NexusAbsoluMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.InputStream;

/**
 * Force le chargement des traductions du mod dans LanguageMap.
 *
 * Pour une raison non identifiee (bug Forge ou conflit avec un autre
 * mod du pack), Forge ne charge pas correctement les fichiers
 * lang/en_us.lang et lang/fr_fr.lang du mod, meme lorsqu'ils sont
 * presents dans le jar. Resultat : tous les items et blocs du mod
 * affichent leur cle brute (item.nexusabsolu.X.name).
 *
 * Solution bulldozer : on utilise la methode publique
 * LanguageMap.inject(InputStream) pour forcer l'injection de notre
 * lang file directement depuis le jar du mod. Cette methode parse
 * le format Java Properties standard et inject dans la map interne
 * de LanguageMap.
 *
 * Le listener se relance a chaque rechargement de ressources
 * (F3+T, changement de langue, etc.).
 */
@SideOnly(Side.CLIENT)
public class NexusLangInjector implements IResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        injectAll();
    }

    public static void injectAll() {
        // 1. Toujours charger en_us comme fallback
        injectFromJar("en_us");

        // 2. Charger la langue courante par-dessus si differente
        try {
            String currentCode = Minecraft.getMinecraft()
                    .getLanguageManager().getCurrentLanguage().getLanguageCode();
            if (currentCode != null && !"en_us".equals(currentCode)) {
                injectFromJar(currentCode);
            }
        } catch (Exception e) {
            NexusAbsoluMod.LOGGER.warn("[NexusLang] Cannot detect current locale, en_us only", e);
        }
    }

    private static void injectFromJar(String langCode) {
        String path = "/assets/nexusabsolu/lang/" + langCode + ".lang";
        InputStream is = NexusLangInjector.class.getResourceAsStream(path);
        if (is == null) {
            NexusAbsoluMod.LOGGER.warn("[NexusLang] Lang file not found in jar: " + path);
            return;
        }
        try {
            // LanguageMap.inject est public + static, parse le format Java Properties
            // et inject dans la map interne.
            LanguageMap.inject(is);
            NexusAbsoluMod.LOGGER.info("[NexusLang] Injected " + langCode + ".lang from mod jar");
        } catch (Exception e) {
            NexusAbsoluMod.LOGGER.error("[NexusLang] Inject failed for " + path, e);
        } finally {
            try { is.close(); } catch (Exception ignored) {}
        }
    }
}
