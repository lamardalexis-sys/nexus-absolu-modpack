package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.events.ManifoldEffectHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Charge et applique le shader post-process Mandelbulb pendant l'injection.
 *
 * Architecture (apres code review v2) :
 *
 *   - Hook sur ClientTickEvent.Phase.END (1 fois par tick, propre,
 *     pas RenderGameOverlayEvent qui peut etre appele plusieurs fois)
 *   - Field shaderGroup recupere UNE FOIS au premier appel (cache),
 *     pas a chaque frame
 *   - ObfuscationReflectionHelper plutot que reflection raw → resolution
 *     auto deobf/srg, code plus robuste
 *   - Garde-fou : si le shader echoue a charger, on n'essaie pas en boucle
 *   - Cleanup : si player passe a null (deconnexion), on desactive le shader
 *
 * Pieges connus MC 1.12.2 :
 *   - OptiFine remplace EntityRenderer → loadShader() peut etre no-op.
 *     Workaround : tester sans OptiFine, ou desactiver "Fast Render".
 *   - Pas d'ApiPublique pour set des uniforms custom — on passe par
 *     ShaderUniform.set(float) via reflection (3 invocations/frame, OK perf).
 *   - field_148031_d / field_147707_d sont les noms SRG en MCP mappings.
 */
@SideOnly(Side.CLIENT)
public class ManifoldShaderHandler {

    // === SRG field names pour reflection robuste ===
    // EntityRenderer.shaderGroup
    private static final String SRG_SHADER_GROUP = "field_147707_d";
    // ShaderGroup.listShaders
    private static final String SRG_SHADER_LIST = "field_148031_d";

    private static final ResourceLocation SHADER_PATH =
        new ResourceLocation("nexusabsolu", "shaders/post/manifold.json");

    // === State ===
    private boolean shaderActive = false;
    private boolean shaderFailed = false;        // si echec, on retry pas
    private float intensityCurrent = 0.0f;       // fade in/out smooth
    private long startTime = 0L;

    // === Method cache pour reflection (compute UNE fois, reutilise) ===
    private Method shaderUniformSetFloat = null;
    private boolean reflectionInitialized = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        // 1 fois par tick (END phase), pas plusieurs comme RenderGameOverlayEvent
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        // Cleanup si plus de player (deconnexion / quit world)
        if (player == null || player.world == null) {
            if (shaderActive) deactivateShader(mc);
            return;
        }

        // On a deja essaye et ca a casse → on n'essaie plus
        if (shaderFailed) return;

        long now = player.world.getTotalWorldTime();
        int stage = ManifoldClientState.getCurrentStage(now);

        // Shader actif uniquement pendant stages 4 (hyperspace) et 5 (peak)
        // Le shader Mandelbulb est lourd, on l'utilise pas en stages 1-3
        boolean shouldBeActive = (stage == ManifoldEffectHandler.STAGE_4_HYPERSPACE
                                || stage == ManifoldEffectHandler.STAGE_5_PEAK);

        // Smooth fade — converge vers 1.0 ou 0.0
        float targetIntensity = shouldBeActive ? 1.0f : 0.0f;
        intensityCurrent += (targetIntensity - intensityCurrent) * 0.05f;

        // Activation : seulement si on n'est PAS deja actif (evite reload spam)
        if (shouldBeActive && !shaderActive) {
            activateShader(mc);
        }
        // Desactivation : quand fade complet (intensity ~ 0)
        else if (!shouldBeActive && shaderActive && intensityCurrent < 0.01f) {
            deactivateShader(mc);
        }

        // Injection des uniforms si shader actif
        if (shaderActive) {
            injectUniforms(mc, stage);
        }
    }

    private void activateShader(Minecraft mc) {
        if (mc.entityRenderer == null) return;
        try {
            mc.entityRenderer.loadShader(SHADER_PATH);
            shaderActive = true;
            startTime = System.currentTimeMillis();
            System.out.println("[Manifold] Shader active");
        } catch (Throwable t) {
            // Throwable, pas Exception, parce que les erreurs GLSL peuvent
            // remonter en Error (pas Exception)
            System.err.println("[Manifold] Echec chargement shader: " + t.getMessage());
            t.printStackTrace();
            shaderActive = false;
            shaderFailed = true;  // on n'essaie plus
        }
    }

    private void deactivateShader(Minecraft mc) {
        if (mc.entityRenderer == null) return;
        try {
            mc.entityRenderer.stopUseShader();
            shaderActive = false;
            System.out.println("[Manifold] Shader desactive");
        } catch (Throwable t) {
            System.err.println("[Manifold] Erreur desactivation: " + t.getMessage());
            // On force shaderActive=false meme si erreur sinon on reste bloque
            shaderActive = false;
        }
    }

    /**
     * Injecte les uniforms Time / Intensity / Phase chaque frame.
     */
    private void injectUniforms(Minecraft mc, int stage) {
        ShaderGroup group = getShaderGroup(mc);
        if (group == null) return;

        List<Shader> shaders = getShaderList(group);
        if (shaders == null) return;

        if (!reflectionInitialized) initReflection();

        float time = (System.currentTimeMillis() - startTime) / 1000.0f;
        // Phase uniform : 1.0 uniquement au PEAK pour effet plus intense
        float phaseValue = (stage == ManifoldEffectHandler.STAGE_5_PEAK) ? 1.0f : 0.0f;

        for (Shader shader : shaders) {
            setUniform(shader, "Time", time);
            setUniform(shader, "Intensity", intensityCurrent);
            setUniform(shader, "Phase", phaseValue);
        }
    }

    private void initReflection() {
        try {
            shaderUniformSetFloat = ShaderUniform.class.getMethod("set", float.class);
            reflectionInitialized = true;
        } catch (NoSuchMethodException e) {
            System.err.println("[Manifold] ShaderUniform.set(float) introuvable");
            shaderFailed = true;
        }
    }

    private void setUniform(Shader shader, String name, float value) {
        if (shaderUniformSetFloat == null) return;
        try {
            ShaderUniform u = shader.getShaderManager().getShaderUniform(name);
            if (u != null) {
                shaderUniformSetFloat.invoke(u, value);
            }
        } catch (Throwable t) {
            // Silent fail — sinon spam console chaque frame
        }
    }

    /**
     * Recupere le champ shaderGroup d'EntityRenderer via ObfuscationReflectionHelper
     * (gere auto deobf/srg). Renvoie null si echec.
     */
    private ShaderGroup getShaderGroup(Minecraft mc) {
        try {
            return ObfuscationReflectionHelper.getPrivateValue(
                EntityRenderer.class,
                mc.entityRenderer,
                SRG_SHADER_GROUP, "shaderGroup");
        } catch (Throwable t) {
            // Fallback : reflection raw
            try {
                Field f;
                try {
                    f = EntityRenderer.class.getDeclaredField("shaderGroup");
                } catch (NoSuchFieldException e) {
                    f = EntityRenderer.class.getDeclaredField(SRG_SHADER_GROUP);
                }
                f.setAccessible(true);
                return (ShaderGroup) f.get(mc.entityRenderer);
            } catch (Throwable t2) {
                return null;
            }
        }
    }

    /**
     * Recupere la liste des Shaders du ShaderGroup.
     */
    @SuppressWarnings("unchecked")
    private List<Shader> getShaderList(ShaderGroup group) {
        try {
            return ObfuscationReflectionHelper.getPrivateValue(
                ShaderGroup.class, group, SRG_SHADER_LIST, "listShaders");
        } catch (Throwable t) {
            try {
                Field f;
                try {
                    f = ShaderGroup.class.getDeclaredField("listShaders");
                } catch (NoSuchFieldException e) {
                    f = ShaderGroup.class.getDeclaredField(SRG_SHADER_LIST);
                }
                f.setAccessible(true);
                return (List<Shader>) f.get(group);
            } catch (Throwable t2) {
                return null;
            }
        }
    }
}
