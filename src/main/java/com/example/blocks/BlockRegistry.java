package com.example.blocks;

import com.example.examplemod.SporeUtility;
import com.example.items.ItemRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.function.Supplier;

public class BlockRegistry {

    private static final DeferredRegister<Block> registered_blocks = DeferredRegister.create(ForgeRegistries.BLOCKS, SporeUtility.MODID);
    public static final RegistryObject<Block> AdjustableCDU = registerBlockWithItem("adjustable_cdu", () -> Factory.ADJUSTABLE_CDU);






    public static void registerBlocks(IEventBus modBus) {
        registered_blocks.register(modBus);
    }

    public static Collection<RegistryObject<Block>> getRegisteredBlocks(){
        return registered_blocks.getEntries();
    }


    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> block){
        RegistryObject<T> toReturn = (RegistryObject<T>) registerBlock(name, block);
        ItemRegistry.registerItem(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return toReturn;
    }
    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> block, Item.Properties properties){
        RegistryObject<T> toReturn = (RegistryObject<T>) registerBlock(name,block);

        ItemRegistry.registerItem(name, () -> new BlockItem(block.get(), properties));
        return toReturn;
    }
    private static <T extends Block> RegistryObject<Block> registerBlock(String name, Supplier<T> block){
        return registered_blocks.register(name, block);
    }

    private static class Factory {
        public static final Block ADJUSTABLE_CDU = new AdjustableCDUBlock();
    }
}
