package com.example.blocks;

import com.example.entity.block.BlockEntityRegistry;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
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

import static com.Harbinger.Spore.Sblocks.CDUBlock.LIT;

public class AdjustableCDUBlock extends Block implements EntityBlock {

    public AdjustableCDUBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.STONE).strength(1.0F));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityRegistry.AdjustableCDUBlockEntity.get().create(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_) {
        return ITickableBlockEntity.getTickerHelper(p_153212_);
    }

    public static boolean isCDUUsable(BlockPos pos, Level level) {
        if (level != null && !level.isClientSide) {
            BlockState state = level.getBlockState(pos);
            if (!state.getBlock().equals(BlockRegistry.AdjustableCDU.get())) {
                return true;
            } else {
                return !(Boolean)state.getValue(LIT);
            }
        } else {
            return false;
        }
    }


}
