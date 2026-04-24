package com.example.blocks;

import com.example.entity.block.AFUBlockEntity;
import com.example.entity.block.BlockEntityRegistry;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AFUBlock extends Block implements EntityBlock {
    private static final Logger log = LoggerFactory.getLogger(AFUBlock.class);

    public AFUBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK));
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState1, boolean p_60570_) {
        super.onPlace(blockState, level, blockPos, blockState1, p_60570_);
        if(level.isClientSide()) return;

        AFUBlockEntity entity = (AFUBlockEntity) level.getBlockEntity(blockPos);
        if(Objects.isNull(entity)) {
            log.error("AFUBlock.onPlace: AFUBlockEntity is null");
            return;
        }

        entity.seal((ServerLevel) level);
    }

    @Override
    public void onRemove(BlockState p_60515_, Level level, BlockPos blockPos, BlockState p_60518_, boolean p_60519_) {

        if(level.isClientSide()) {
            super.onRemove(p_60515_, level, blockPos, p_60518_, p_60519_);
            return;
        };
        AFUBlockEntity entity = (AFUBlockEntity) level.getBlockEntity(blockPos);
        if(Objects.isNull(entity)) {
            log.error("AFUBlock.onRemove: AFUBlockEntity is null");
            return;
        }
        entity.unseal((ServerLevel) level);
        super.onRemove(p_60515_, level, blockPos, p_60518_, p_60519_);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.AFUBlockEntity.get().create(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_) {
        return ITickableBlockEntity.getTickerHelper(p_153212_);
    }
}
