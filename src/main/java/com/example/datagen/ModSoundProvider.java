package com.example.datagen;

import com.example.examplemod.SporeUtility;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import net.minecraftforge.registries.RegistryObject;

public class ModSoundProvider extends SoundDefinitionsProvider {
    /**
     * Creates a new instance of this data provider.
     *
     * @param output The {@linkplain PackOutput} instance provided by the data generator.
     * @param helper The existing file helper provided by the event you are initializing this provider in.
     */
    public ModSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, SporeUtility.MODID, helper);
    }

    @Override
    public void registerSounds() {
        //this.registerSimpleSound(SoundRegistry.VIAL_FILL_SOUND);
    }

    /**
     * Erstellt einen einfachen Sound-Eintrag in der sounds.json.
     * Geht davon aus, dass die Sounddatei (.ogg) unter
     * assets/modid/sounds/<soundName>.ogg liegt.
     */
    private void registerSimpleSound(RegistryObject<SoundEvent> event) {
        // Starte den Sound-Eintrag (den Schlüssel in der JSON)

    }
}
