package com.example.examplemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = SporeUtility.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();


    public static final ForgeConfigSpec.IntValue AFU_BLOCK_LIMIT = BUILDER
            .comment("The number of blocks that can be covered by an AFU")
            .comment("Default: 250")
            .comment("Max: 3000")
            .defineInRange("afu_limit", 250, 0, 3000);

    public static final ForgeConfigSpec.IntValue AFU_RETRY_INTERVAL = BUILDER
            .comment("The interval (in ticks) in which the afu will try to seal a room if its not sealed a room")
            .comment("Default: " + 120 * 20)
            .comment("Max: " + Integer.MAX_VALUE)
            .defineInRange("afu_retry_interval", 120 * 20, 0, Integer.MAX_VALUE);


    static final ForgeConfigSpec SPEC = BUILDER.build();

}
