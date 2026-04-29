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

    @SubscribeEvent
    public static void onSoundRegister(RegistryEvent.Register<SoundEvent> event) {
        STOMACH_GURGLE = register(event, "machine.stomach_gurgle");
        DIARRHEE_EXPLOSIVE = register(event, "machine.diarrhee_explosive");
        MANIFOLD_CENTINELA = register(event, "manifold.centinela");
    }

    private static SoundEvent register(RegistryEvent.Register<SoundEvent> event,
                                        String name) {
        ResourceLocation loc = new ResourceLocation(Reference.MOD_ID, name);
        SoundEvent sound = new SoundEvent(loc).setRegistryName(loc);
        event.getRegistry().register(sound);
        return sound;
    }
}
