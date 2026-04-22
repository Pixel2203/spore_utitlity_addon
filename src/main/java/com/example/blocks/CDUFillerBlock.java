package com.example.blocks;

import com.Harbinger.Spore.Core.Sblocks;
import com.example.entity.block.BlockEntityRegistry;
import com.example.entity.block.CDUFillerBlockEntity;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class CDUFillerBlock extends Block implements EntityBlock {
    private static final Logger log = LoggerFactory.getLogger(CDUFillerBlock.class);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CDUFillerBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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

        CDUFillerBlockEntity connectorBlockEntity = (CDUFillerBlockEntity) level.getBlockEntity(thisBlockPos);

        if(Objects.isNull(connectorBlockEntity)) {
            log.error("CDUFillerBlock.neighborChanged: ConnectorBlockEntity is null");
            return;
        }

        BlockState changedToBlock = level.getBlockState(changedBlockPos);
        boolean wasCDU = changedBlock == Sblocks.CDU.get();
        boolean hasCDUBeenRemoved = wasCDU && !changedToBlock.is(Sblocks.CDU.get());
        boolean hasCDUBeenPlaced  = !wasCDU && changedToBlock.is(Sblocks.CDU.get());

        if(hasCDUBeenPlaced) {
            onCDUPlacement(changedBlockPos, thisBlockPos, thisBlockState, connectorBlockEntity);
            return;
        }
        if(hasCDUBeenRemoved) {
            onCDURemoval(changedBlockPos, connectorBlockEntity);
        }

    }

    private void onCDUPlacement(BlockPos placementPos, BlockPos fillerPos, BlockState fillerState,  CDUFillerBlockEntity fillerBlockEntity) {
        if(canConnectToFiller(placementPos, fillerPos, fillerState, fillerBlockEntity)) {
            log.debug("CDUFillerBlock.neighborChanged: CDU has been connected to filler");
            fillerBlockEntity.connectToCDU(placementPos);
        }
    }

    private void onCDURemoval(BlockPos removalPos, CDUFillerBlockEntity fillerBlockEntity) {
        if(fillerBlockEntity.isConnectedToCDU(removalPos)) {
            log.debug("CDUFillerBlock.neighborChanged: Connected CDU has been removed");
            fillerBlockEntity.disconnectFromCDU(removalPos);
        }
    }

    private boolean canConnectToFiller(BlockPos cduPos, BlockPos fillerPos, BlockState fillerState, CDUFillerBlockEntity fillerBlockEntity) {
        Direction facing = fillerState.getValue(CDUFillerBlock.FACING);
        Direction right = facing.getClockWise();        // 90° im Uhrzeigersinn
        Direction left = facing.getCounterClockWise();  // 90° gegen den Uhrzeigersinn
        boolean isRight = fillerPos.relative(right).equals(cduPos);
        boolean isLeft = fillerPos.relative(left).equals(cduPos);

        return (isLeft || isRight) && !fillerBlockEntity.isConnectedToCDU(cduPos);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState newState, boolean p_60519_) {

        if(level.isClientSide() || newState.is(blockState.getBlock())) {
            super.onRemove(blockState, level, blockPos, newState, p_60519_);
            return;
        };

        CDUFillerBlockEntity blockEntity = (CDUFillerBlockEntity)level.getBlockEntity(blockPos);
        if(Objects.isNull(blockEntity)) {
            log.error("CDUFillerBlock.onRemove: CDUFillerBlockEntity is null although it should not be");
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

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult preprocessedResult = super.use(blockState, level, blockPos, player, hand, hitResult);
        if(level.isClientSide() || hand == InteractionHand.OFF_HAND) return preprocessedResult;
        CDUFillerBlockEntity blockEntity = (CDUFillerBlockEntity)level.getBlockEntity(blockPos);
        NetworkHooks.openScreen((ServerPlayer) player, blockEntity, blockPos);

        return InteractionResult.SUCCESS;
    }
}
