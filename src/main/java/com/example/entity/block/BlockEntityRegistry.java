package com.example.entity.block;

import com.example.blocks.BlockRegistry;
import com.example.examplemod.SporeUtility;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {

    private static final DeferredRegister<BlockEntityType<?>> registered_blockentities = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SporeUtility.MODID);

    public static final RegistryObject<BlockEntityType<AdjustableCDUBlockEntity>> AdjustableCDUBlockEntity = registered_blockentities.register("adjustable_cdu_entity", () -> Factory.AdjustableCDUBlockEntity);

    public static void registerBlockEntityTypes(IEventBus eventBus){
        registered_blockentities.register(eventBus);
    }


    private static class Factory {
        public static BlockEntityType<AdjustableCDUBlockEntity> AdjustableCDUBlockEntity = BlockEntityType.Builder.of(AdjustableCDUBlockEntity::new, BlockRegistry.AdjustableCDU.get()).build(null);

    }
}
