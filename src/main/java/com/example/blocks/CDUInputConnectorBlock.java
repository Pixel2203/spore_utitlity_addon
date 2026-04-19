package com.example.blocks;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import com.example.entity.block.BlockEntityRegistry;
import com.example.entity.block.CDUInputConnectorBlockEntity;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
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

public class CDUInputConnectorBlock extends Block implements EntityBlock {
    private static final Logger log = LoggerFactory.getLogger(CDUInputConnectorBlock.class);

    public CDUInputConnectorBlock() {
        super(BlockBehaviour.Properties.of());
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.CduInputConnectorBlockEntity.get().create(blockPos, blockState);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
    }

    @Override
    public void neighborChanged(BlockState thisBlockState, Level level, BlockPos thisBlockPos, Block changedBlock, BlockPos changedBlockPos, boolean p_60514_) {
        super.neighborChanged(thisBlockState, level, thisBlockPos, changedBlock, changedBlockPos, p_60514_);
        if(level.isClientSide())  {
            return;
        }
        CDUInputConnectorBlockEntity connectorBlockEntity = (CDUInputConnectorBlockEntity) level.getBlockEntity(thisBlockPos);

        if(Objects.isNull(connectorBlockEntity)) {
            log.error("CDUInputConnectorBlock.neighborChanged: ConnectorBlockEntity is null");
            return;
        }

        Block changedToBlock = level.getBlockState(changedBlockPos).getBlock();

        if(changedBlock == Sblocks.CDU.get()) {
            if(changedToBlock != Sblocks.CDU.get()) {
                log.debug("CDUInputConnectorBlock.neighborChanged: CDU has been removed nearby");
                if(!connectorBlockEntity.isConnectedToCDU(changedBlockPos)) return;
                connectorBlockEntity.disconnectFromCDU(changedBlockPos);
                return;
            }


            connectorBlockEntity.connectToCDU(changedBlockPos);
            return;
        }

        if(connectorBlockEntity.isConnectedToCDU()) return;
        if(changedToBlock != Sblocks.CDU.get()) return;
        connectorBlockEntity.connectToCDU(changedBlockPos);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState newState, boolean p_60519_) {

        if(level.isClientSide() || newState.is(blockState.getBlock())) {
            super.onRemove(blockState, level, blockPos, newState, p_60519_);
            return;
        };

        CDUInputConnectorBlockEntity blockEntity = (CDUInputConnectorBlockEntity)level.getBlockEntity(blockPos);
        if(Objects.isNull(blockEntity)) {
            log.error("CDUInputConnectorBlock.onRemove: ConnectorBlockEntity is null altough it should not be");
            super.onRemove(blockState, level, blockPos, newState, p_60519_);
            return;
        }
        blockEntity.drops();
        super.onRemove(blockState, level, blockPos, newState, p_60519_);


    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_) {
        return ITickableBlockEntity.getTickerHelper(p_153212_);
    }
}
