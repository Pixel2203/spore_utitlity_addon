package com.example.sound;

import com.example.examplemod.SporeUtility;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SporeUtility.MODID);

    public static final RegistryObject<SoundEvent> AFU_IDLE_SOUND = registerSoundEvent("afu-idle");

    private static RegistryObject<SoundEvent> registerSoundEvent(String sound) {
        return SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SporeUtility.MODID, sound)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

}
