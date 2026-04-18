package com.example.blocks;

import com.Harbinger.Spore.Core.Sblocks;
import com.example.entity.block.BlockEntityRegistry;
import com.example.entity.block.CDUInputConnectorBlockEntity;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        if(level.isClientSide())  {
            super.neighborChanged(thisBlockState, level, thisBlockPos, changedBlock, changedBlockPos, p_60514_);
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
                log.info("CDUInputConnectorBlock.neighborChanged: CDU has been removed nearby");
                if(!connectorBlockEntity.isConnectedToCDU(changedBlockPos)) return;
                connectorBlockEntity.disconnectFromCDU(changedBlockPos);
            }


            connectorBlockEntity.connectToCDU(changedBlockPos);
            return;
        }

        if(connectorBlockEntity.isConnectedToCDU()) return;
        if(changedToBlock != Sblocks.CDU.get()) return;
        connectorBlockEntity.connectToCDU(changedBlockPos);
    }
}
