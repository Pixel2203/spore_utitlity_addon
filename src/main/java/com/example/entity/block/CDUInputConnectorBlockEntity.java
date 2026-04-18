package com.example.entity.block;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Objects;

public class CDUInputConnectorBlockEntity extends BlockEntity implements ITickableBlockEntity {

    private static final Logger log = LoggerFactory.getLogger(CDUInputConnectorBlockEntity.class);

    @Nullable
    private CDUBlockEntity connectedCDU;
    @Nullable
    private BlockPos connectedCDUPos;

    public CDUInputConnectorBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntityRegistry.CduInputConnectorBlockEntity.get(), p_155229_, p_155230_);
    }

    @Override
    public void tick() {
         if(connectedCDU == null) return;

    }

    public void connectToCDU(BlockPos blockPos) {
        if(Objects.isNull(level)) {
            log.error("CDUInputConnectorBlockEntity.connectToCDU: level is null");
            return;
        }
        if(!level.getBlockState(blockPos).is(Sblocks.CDU.get())) {
            log.error("CDUInputConnectorBlockEntity.connectToCDU: blockstate is not of type cdu");
            return;
        }
        CDUBlockEntity cduBlockEntity = (CDUBlockEntity) level.getBlockEntity(blockPos);
        this.connectedCDU = cduBlockEntity;
        this.connectedCDUPos = blockPos;
        log.debug("CDUInputConnectorBlockEntity.connectToCDU: connected to cdu at {}", this.connectedCDUPos);
    }

    public void disconnectFromCDU(BlockPos blockPos) {
        if(Objects.isNull(level)) {
            log.error("CDUInputConnectorBlockEntity.disconnectFromCDU: level is null");
            return;
        }
        if(!this.connectedCDUPos.equals(blockPos)) {
            log.warn("CDUInputConnectorBlockEntity.disconnectFromCDU: disconnect from CDU that was not connected!");
            return;
        }
        log.debug("CDUInputConnectorBlockEntity.connectToCDU: disconnected from cdu at {}", this.connectedCDUPos);
        this.connectedCDUPos = null;
        this.connectedCDU = null;

    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(Objects.isNull(level)) return;
        if(level.isClientSide()) return;

        CDUBlockEntity foundCDU = this.getNearbyCDU(level, getBlockPos());
        if(Objects.isNull(foundCDU)) return;

        log.debug("CDUInputConnectorBlock.onPlace: found CDU nearby at {}" , foundCDU.getBlockPos());
        this.connectToCDU(foundCDU.getBlockPos());

    }

    public boolean isConnectedToCDU() {
        return Objects.nonNull(connectedCDU);
    }

    public boolean isConnectedToCDU(BlockPos blockPos) {
        return Objects.nonNull(connectedCDU) && connectedCDUPos.equals(blockPos);
    }



    @javax.annotation.Nullable
    private CDUBlockEntity getNearbyCDU(Level level, BlockPos blockPos) {
        return java.util.Arrays.stream(net.minecraft.core.Direction.values())
                .map(blockPos::relative)
                .map(blockPos1 -> this.checkForCDU(level, blockPos1))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @javax.annotation.Nullable
    private CDUBlockEntity checkForCDU(Level level, BlockPos blockPos) {
        if(Objects.isNull(level)) return null;
        BlockState blockState = level.getBlockState(blockPos);
        if(!blockState.is(Sblocks.CDU.get())) return null;
        return (CDUBlockEntity) level.getBlockEntity(blockPos);
    }
}
