package com.example.items;

import com.example.examplemod.SporeUtility;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ItemRegistry {
    private ItemRegistry() {
        /* This utility class should not be instantiated */
    }


    private static final DeferredRegister<Item> registered_items = DeferredRegister.create(ForgeRegistries.ITEMS, SporeUtility.MODID);

    public static void registerItems(IEventBus modBus) {
        registered_items.register(modBus);
    }

    public static RegistryObject<Item> registerItem(String name, Supplier<Item> item){
        return registered_items.register(name, item);
    }
}
