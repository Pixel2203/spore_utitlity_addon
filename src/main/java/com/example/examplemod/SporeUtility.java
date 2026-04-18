package com.example.examplemod;

import com.example.blocks.BlockRegistry;
import com.example.entity.block.BlockEntityRegistry;
import com.example.items.ItemRegistry;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SporeUtility.MODID)
public class SporeUtility
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "sporeutility";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2

    public SporeUtility(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        BlockRegistry.registerBlocks(modEventBus);
        ItemRegistry.registerItems(modEventBus);
        BlockEntityRegistry.registerBlockEntityTypes(modEventBus);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

}
