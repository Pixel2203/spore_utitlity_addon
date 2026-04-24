package com.example.blocks;

import com.example.entity.block.AFUBlockEntity;
import com.example.entity.block.BlockEntityRegistry;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
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
    public void onRemove(BlockState p_60515_, Level level, BlockPos blockPos, BlockState p_60518_, boolean p_60519_) {

        if(level.isClientSide()) {
            super.onRemove(p_60515_, level, blockPos, p_60518_, p_60519_);
            return;
        }
        AFUBlockEntity entity = (AFUBlockEntity) level.getBlockEntity(blockPos);
        if(Objects.isNull(entity)) {
            log.error("AFUBlock.onRemove: Unable to find responsible AFU");
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

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult p_60508_) {
        InteractionResult preprocessed = super.use(blockState, level, blockPos, player, hand, p_60508_);
        if(level.isClientSide()) return preprocessed;
        if(hand != InteractionHand.MAIN_HAND) return preprocessed;
        AFUBlockEntity afu = (AFUBlockEntity) level.getBlockEntity(blockPos);
        if(Objects.isNull(afu)) {
            log.error("AFUBlock.use: Unable to find afu blockentity");
            return InteractionResult.FAIL;
        }
        NetworkHooks.openScreen((ServerPlayer) player, afu, blockPos);
        return InteractionResult.SUCCESS;
    }

}
