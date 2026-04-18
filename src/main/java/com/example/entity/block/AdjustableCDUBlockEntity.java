package com.example.entity.block;

import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdjustableCDUBlockEntity extends BlockEntity implements ITickableBlockEntity {
    private static final Logger log = LoggerFactory.getLogger(AdjustableCDUBlockEntity.class);
    public AdjustableCDUBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.AdjustableCDUBlockEntity.get(),pos, state);
    }

    @Override
    public void tick() {
        log.info("Hello From ticker");
    }



}
