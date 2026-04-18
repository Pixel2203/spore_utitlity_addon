package com.example.entity.block;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import com.Harbinger.Spore.Sblocks.CDUBlock;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

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

    @Nullable
    private CDUBlockEntity getConnectedCDU() {
        return java.util.Arrays.stream(net.minecraft.core.Direction.values())
                .map(worldPosition::relative)
                .map(this::checkForCDU)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    private CDUBlockEntity checkForCDU(BlockPos blockPos) {
        if(Objects.isNull(level)) return null;
        BlockState blockState = this.level.getBlockState(blockPos);
        if(!blockState.is(Sblocks.CDU.get())) return null;
        return (CDUBlockEntity) level.getBlockEntity(blockPos);
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
        this.connectedCDUPos = null;
        this.connectedCDU = null;
    }

    public boolean isConnectedToCDU() {
        return Objects.nonNull(connectedCDU);
    }

    public boolean isConnectedToCDU(BlockPos blockPos) {
        return Objects.nonNull(connectedCDU) && connectedCDUPos.equals(blockPos);
    }
}
