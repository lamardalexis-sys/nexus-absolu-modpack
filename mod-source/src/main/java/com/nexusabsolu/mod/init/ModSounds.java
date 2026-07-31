package com.nexusabsolu.mod.init;

import com.nexusabsolu.mod.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModSounds {

    public static SoundEvent STOMACH_GURGLE;
    public static SoundEvent DIARRHEE_EXPLOSIVE;
    public static SoundEvent MANIFOLD_CENTINELA;

    // Sons ambient des multiblocs (Phase 6.C). Ils reutilisent des sons
    // vanilla via "type": "event" dans sounds.json -- aucun .ogg a fournir.
    // Le volume et le pitch declares dans sounds.json ne sont pas garantis
    // pour les entrees de type "event" : passer les valeurs voulues a
    // World.playSound() au moment de la lecture.
    public static SoundEvent MULTIBLOCK_HUMMING;
    public static SoundEvent MULTIBLOCK_COMPLETE;
    public static SoundEvent HABER_PRESSURE;
    public static SoundEvent CYCLISATEUR_STELLAIRE;
    public static SoundEvent KROLL_ARGON;

    @SubscribeEvent
    public static void onSoundRegister(RegistryEvent.Register<SoundEvent> event) {
        STOMACH_GURGLE = register(event, "machine.stomach_gurgle");
        DIARRHEE_EXPLOSIVE = register(event, "machine.diarrhee_explosive");
        MANIFOLD_CENTINELA = register(event, "manifold.centinela");

        MULTIBLOCK_HUMMING = register(event, "machine.multiblock_humming");
        MULTIBLOCK_COMPLETE = register(event, "machine.multiblock_complete");
        HABER_PRESSURE = register(event, "machine.haber_pressure");
        CYCLISATEUR_STELLAIRE = register(event, "machine.cyclisateur_stellaire");
        KROLL_ARGON = register(event, "machine.kroll_argon");
    }

    private static SoundEvent register(RegistryEvent.Register<SoundEvent> event,
                                        String name) {
        ResourceLocation loc = new ResourceLocation(Reference.MOD_ID, name);
        SoundEvent sound = new SoundEvent(loc).setRegistryName(loc);
        event.getRegistry().register(sound);
        return sound;
    }
}
