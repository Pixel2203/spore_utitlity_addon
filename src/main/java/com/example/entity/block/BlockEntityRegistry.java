package com.example.entity.block;

import com.example.examplemod.SporeUtility;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.example.blocks.BlockRegistry.AFU;
import static com.example.blocks.BlockRegistry.CDUFiller;

public class BlockEntityRegistry {

    private static final DeferredRegister<BlockEntityType<?>> registered_blockentities = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SporeUtility.MODID);

    public static final RegistryObject<BlockEntityType<CDUFillerBlockEntity>> CduInputConnectorBlockEntity = registered_blockentities.register("cdu_filler_entity", () -> Factory.CDUFillerBlockEntity);
    public static final RegistryObject<BlockEntityType<AFUBlockEntity>> AFUBlockEntity = registered_blockentities.register("afu_entity", () -> Factory.AFUBlockEntity);

    public static void registerBlockEntityTypes(IEventBus eventBus){
        registered_blockentities.register(eventBus);
    }


    private static class Factory {
        public static BlockEntityType<CDUFillerBlockEntity> CDUFillerBlockEntity = BlockEntityType.Builder.of(CDUFillerBlockEntity::new, CDUFiller.get()).build(null);
        public static BlockEntityType<AFUBlockEntity> AFUBlockEntity = BlockEntityType.Builder.of(AFUBlockEntity::new, AFU.get()).build(null);

    }
}
