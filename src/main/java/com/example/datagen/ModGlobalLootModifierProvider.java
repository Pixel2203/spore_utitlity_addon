package com.example.datagen;

import com.example.examplemod.SporeUtility;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output) {
        super(output, SporeUtility.MODID);
    }

    @Override
    protected void start() {
    }
}
