package com.example.entity.block;

import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AdjustableCDUBlockEntity extends CDUBlockEntity implements ITickableBlockEntity {
    public AdjustableCDUBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void tick() {

    }
}
