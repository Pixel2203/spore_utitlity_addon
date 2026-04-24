package com.example.blocks;

import com.example.afu.AFUManager;
import com.example.entity.block.AFUBlockEntity;
import com.example.entity.block.CDUFillerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class CleanedAirBlock extends Block {
    private static final Logger log = LoggerFactory.getLogger(CleanedAirBlock.class);

    public CleanedAirBlock() {
        super(Properties.of().noCollission().noOcclusion().air().replaceable().noLootTable().pushReaction(PushReaction.DESTROY));
    }

    @Override
    public void neighborChanged(BlockState thisBlockState, Level level, BlockPos thisBlockPos, Block changedBlock, BlockPos changedBlockPos, boolean p_60514_) {
        super.neighborChanged(thisBlockState, level, thisBlockPos, changedBlock, changedBlockPos, p_60514_);
        if(level.isClientSide())  {
            return;
        }
        if(changedBlock == BlockRegistry.CleanedAir.get()) return; // Ohne Bedenken
        BlockState changedToBlock = level.getBlockState(changedBlockPos);

        if(changedToBlock.is(Blocks.AIR)) {
            AFUBlockEntity afu = AFUManager.getOwner(thisBlockPos);
            if(Objects.isNull(afu)) {
                log.error("CleanedAirBlock.neighborChanged: Unable to find owning AFU for block {}, removing block", thisBlockPos);
                level.setBlock(thisBlockPos, Blocks.AIR.defaultBlockState(),3);
                return;
            }
            afu.breach(changedBlockPos);
        }



    }
}
