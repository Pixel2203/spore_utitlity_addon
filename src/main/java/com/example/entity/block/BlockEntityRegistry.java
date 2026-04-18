package com.example.entity.block;

import com.example.blocks.BlockRegistry;
import com.example.examplemod.SporeUtility;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.example.blocks.BlockRegistry.CduInputConnectorBlock;

public class BlockEntityRegistry {

    private static final DeferredRegister<BlockEntityType<?>> registered_blockentities = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SporeUtility.MODID);

    public static final RegistryObject<BlockEntityType<CDUInputConnectorBlockEntity>> CduInputConnectorBlockEntity = registered_blockentities.register("cdu_input_connector", () -> Factory.CDUInputConnectorBlockEntity);

    public static void registerBlockEntityTypes(IEventBus eventBus){
        registered_blockentities.register(eventBus);
    }


    private static class Factory {
        public static BlockEntityType<CDUInputConnectorBlockEntity> CDUInputConnectorBlockEntity = BlockEntityType.Builder.of(CDUInputConnectorBlockEntity::new, CduInputConnectorBlock.get()).build(null);

    }
}
