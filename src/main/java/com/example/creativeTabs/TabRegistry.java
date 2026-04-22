package com.example.creativeTabs;

import com.example.examplemod.SporeUtility;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;


public class TabRegistry {

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SporeUtility.MODID);

    private static final RegistryObject<CreativeModeTab> MainTab =
            CREATIVE_MODE_TABS.register("main_tab", () -> TabFactory.MainTab);

    public static void registerTabs(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
