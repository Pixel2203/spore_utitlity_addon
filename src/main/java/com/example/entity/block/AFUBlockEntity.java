package com.example.entity.block;

import com.example.afu.AFUManager;
import com.example.blocks.BlockRegistry;
import com.example.errors.BlockLimitExceededException;
import com.example.examplemod.Config;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.util.tuples.Pair;

import java.util.*;

public class AFUBlockEntity extends BlockEntity implements ITickableBlockEntity {

    private static final Logger log = LoggerFactory.getLogger(AFUBlockEntity.class);

    private boolean isSealed = false;

    private int ticker;
    private final int autoRetry = 20 * 60;

    public AFUBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntityRegistry.AFUBlockEntity.get(), p_155229_, p_155230_);
    }

    public void breach() {
        this.unseal(getLevel(), getBlockPos());
        this.seal(getLevel(), getBlockPos());
    }


    public void seal(Level level, BlockPos blockPos) {
        if(Objects.isNull(level)) return;
        if(level.isClientSide) return;
        try {
            int limit = 1000;
            Pair<Set<BlockPos>, Set<BlockPos>> result = this.spread(level, blockPos, limit);
            Set<BlockPos> toBeReplaced = result.getA();
            Set<BlockPos> sealed = result.getB();
            AFUManager.registerAFU(this, sealed, toBeReplaced);

            toBeReplaced.forEach((pos) -> {
                level.setBlock(pos, BlockRegistry.CleanedAir.get().defaultBlockState(), 2);

            });
            log.debug("Blocks to supply: {}", sealed.size());
        }catch (BlockLimitExceededException e) {
            log.error(e.getMessage());
        }
    }

    public void unseal(Level level, BlockPos blockPos) {
        if(Objects.isNull(level)) return;
        if(level.isClientSide) return;
        Set<BlockPos> suppliedBlocks = AFUManager.unregisterAFU(this);

        suppliedBlocks.forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if(state.is(BlockRegistry.CleanedAir.get())) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        });

    }

    private Pair<Set<BlockPos>, Set<BlockPos>> spread(Level level, BlockPos startPos, int maxBlocks) throws BlockLimitExceededException {
        // 1. Hier speichern wir alle Positionen, die wir schon besucht haben
        Set<BlockPos> sealed = new HashSet<>();
        Set<BlockPos> toBeReplaced = new HashSet<>();
        // 2. Hier kommen die Blöcke rein, die noch geprüft werden müssen
        Queue<BlockPos> queue = new LinkedList<>();

        // Wir starten beim Block direkt am Air Purifier
        queue.add(startPos);
        //visited.add(startPos);

        int count = 0;

        while (!queue.isEmpty() ) {
            if(count >= maxBlocks) {
                throw new BlockLimitExceededException("Room to seal is not allowed to exceed " + maxBlocks + " blocks!");
            }
            BlockPos current = queue.poll();

            // Prüfe alle 6 Richtungen
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);

                // Nur weitermachen, wenn wir dort noch nicht waren UND es Luft ist
                BlockState neighborState = level.getBlockState(neighbor);
                if (!sealed.contains(neighbor) && canFlow(level, neighborState, neighbor)) {

                    // Block setzen

                    // Markieren und zur Schlange hinzufügen
                    if(neighborState.is(Blocks.AIR)) {
                        toBeReplaced.add(neighbor);
                    }
                    sealed.add(neighbor);
                    queue.add(neighbor);
                    count++;

                    // Sicherheitsstopp
                    if (count >= maxBlocks) break;
                }
            }
        }
        return new Pair<>(toBeReplaced, sealed);
    }

    private boolean canFlow(Level level, BlockState state, BlockPos pos) {
        return state.is(Blocks.AIR) || state.canBeReplaced() || !state.isCollisionShapeFullBlock(level, pos);
    }


    @Override
    public void tick() {
        if(isSealed) return;
        ticker++;
        if(ticker >= autoRetry) {
            this.seal(getLevel(), getBlockPos());
            ticker = 0;
        }
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.isSealed = compoundTag.getBoolean("isSealed");
        this.ticker = compoundTag.getInt("ticker");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.putBoolean("isSealed", isSealed);
        compoundTag.putInt("ticker", ticker);
    }
}
