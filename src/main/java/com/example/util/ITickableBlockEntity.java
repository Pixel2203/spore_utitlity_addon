package com.example.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface ITickableBlockEntity {
    void tick(ServerLevel level);
    static <T extends BlockEntity>BlockEntityTicker<T> getTickerHelper(Level level){
        return level.isClientSide() ? null : (level0,pos,state,blockEntity) -> ((ITickableBlockEntity)blockEntity).tick((ServerLevel) level);
    }
}
