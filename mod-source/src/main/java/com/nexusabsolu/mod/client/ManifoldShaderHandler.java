package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.events.ManifoldEffectHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Charge et applique le shader post-process Mandelbulb pendant l'injection.
 *
 * Architecture MC 1.12.2 :
 * - EntityRenderer a un champ 'shaderGroup' qui peut etre configure pour
 *   appliquer un effet post-process plein ecran.
 * - On utilise reflection pour le set/unset (le champ est private/protected).
 * - On injecte les uniforms Time/Intensity/Phase chaque frame via
 *   ShaderUniform.set().
 *
 * Attention :
 *   - OptiFine peut intercepter le rendu et empecher le shader Forge native
 *     de s'appliquer. Si OptiFine est present, le shader peut ne rien faire.
 *   - Performance : Mandelbulb est lourd. Si lag → reduire RAYMARCH_STEPS
 *     dans manifold.fsh ou desactiver le shader.
 *   - Le shader fait du raymarching FULL SCREEN, donc charge GPU significative.
 */
@SideOnly(Side.CLIENT)
public class ManifoldShaderHandler {

    private static final ResourceLocation SHADER_PATH =
        new ResourceLocation("nexusabsolu", "shaders/post/manifold.json");

    private boolean shaderActive = false;
    private float intensityCurrent = 0.0f;  // pour fade in/out smooth
    private long startTime = 0;

    @SubscribeEvent
    public void onRenderTick(RenderGameOverlayEvent.Pre event) {
        // Run only once per frame
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;

        long now = player.world.getTotalWorldTime();
        int phase = ManifoldClientState.getCurrentPhase(now);

        boolean shouldBeActive = (phase == ManifoldEffectHandler.PHASE_ACTIVE
                                || phase == ManifoldEffectHandler.PHASE_NEGATIVE);

        // Fade in/out smooth (pas de coupure brutale)
        float targetIntensity = shouldBeActive ? 1.0f : 0.0f;
        intensityCurrent += (targetIntensity - intensityCurrent) * 0.05f;

        // Activation / desactivation du shader
        if (shouldBeActive && !shaderActive) {
            activateShader(mc);
        } else if (!shouldBeActive && shaderActive && intensityCurrent < 0.01f) {
            deactivateShader(mc);
        }

        // Injection des uniforms si shader actif
        if (shaderActive && mc.entityRenderer != null) {
            injectUniforms(mc, phase);
        }
    }

    private void activateShader(Minecraft mc) {
        if (mc.entityRenderer == null) return;
        try {
            // loadShader(ResourceLocation) est public en MC 1.12.2
            mc.entityRenderer.loadShader(SHADER_PATH);
            shaderActive = true;
            startTime = System.currentTimeMillis();
            System.out.println("[Manifold] Shader active");
        } catch (Exception e) {
            System.err.println("[Manifold] Echec chargement shader : " + e.getMessage());
            e.printStackTrace();
            shaderActive = false;
        }
    }

    private void deactivateShader(Minecraft mc) {
        if (mc.entityRenderer == null) return;
        try {
            mc.entityRenderer.stopUseShader();
            shaderActive = false;
            System.out.println("[Manifold] Shader desactive");
        } catch (Exception e) {
            System.err.println("[Manifold] Erreur desactivation : " + e.getMessage());
        }
    }

    /**
     * Injecte les uniforms Time / Intensity / Phase dans le shader.
     *
     * MC 1.12.2 ShaderGroup ne fournit pas d'API publique pour setter les
     * uniforms custom. On passe par reflection sur la liste des Shaders
     * du group, puis on chope leurs ShaderUniform par nom.
     */
    private void injectUniforms(Minecraft mc, int phase) {
        try {
            ShaderGroup group = getShaderGroup(mc);
            if (group == null) return;

            List<Shader> shaders = getShaderList(group);
            if (shaders == null) return;

            float time = (System.currentTimeMillis() - startTime) / 1000.0f;
            float phaseValue = (phase == ManifoldEffectHandler.PHASE_NEGATIVE) ? 1.0f : 0.0f;

            for (Shader shader : shaders) {
                setUniform(shader, "Time", time);
                setUniform(shader, "Intensity", intensityCurrent);
                setUniform(shader, "Phase", phaseValue);
            }
        } catch (Exception e) {
            // Silent fail — sinon on spam la console chaque frame
        }
    }

    private void setUniform(Shader shader, String name, float value) {
        ShaderUniform u = shader.getShaderManager().getShaderUniform(name);
        if (u != null) {
            u.set(value);
        }
    }

    /** Reflection : recupere shaderGroup depuis EntityRenderer. */
    private ShaderGroup getShaderGroup(Minecraft mc) throws Exception {
        // En 1.12.2 deobf : champ "shaderGroup", obf : "field_147707_d" ou similaire
        Field field;
        try {
            field = mc.entityRenderer.getClass().getDeclaredField("shaderGroup");
        } catch (NoSuchFieldException e) {
            // Tente le nom obfusque (Forge SRG : field_147707_d)
            try {
                field = mc.entityRenderer.getClass().getDeclaredField("field_147707_d");
            } catch (NoSuchFieldException e2) {
                return null;
            }
        }
        field.setAccessible(true);
        return (ShaderGroup) field.get(mc.entityRenderer);
    }

    /** Reflection : recupere la liste interne de Shaders du group. */
    @SuppressWarnings("unchecked")
    private List<Shader> getShaderList(ShaderGroup group) throws Exception {
        Field field;
        try {
            field = ShaderGroup.class.getDeclaredField("listShaders");
        } catch (NoSuchFieldException e) {
            try {
                // SRG name pour 1.12.2
                field = ShaderGroup.class.getDeclaredField("field_148031_d");
            } catch (NoSuchFieldException e2) {
                return null;
            }
        }
        field.setAccessible(true);
        return (List<Shader>) field.get(group);
    }
}
