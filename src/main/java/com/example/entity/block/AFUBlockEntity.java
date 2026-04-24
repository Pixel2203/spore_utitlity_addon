package com.example.entity.block;

import com.example.afu.AFUManager;
import com.example.blocks.BlockRegistry;
import com.example.errors.BlockLimitExceededException;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.util.tuples.Pair;

import java.util.*;

public class AFUBlockEntity extends BlockEntity implements ITickableBlockEntity {

    private static final Logger log = LoggerFactory.getLogger(AFUBlockEntity.class);
    private boolean isSealed = false;
    private int ticker;
    private final int autoRetry = 20 * 60;
    private final int maxRoomLimit = 1000;

    @Nullable
    private Set<BlockPos> sealedBlocks;
    @Nullable
    private Set<BlockPos> replacedAirBlocks;


    public AFUBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntityRegistry.AFUBlockEntity.get(), p_155229_, p_155230_);
    }

    public void breach() {
        Level level = getLevel();
        if(level.isClientSide()) return;
        this.unseal((ServerLevel) level);
        this.seal((ServerLevel) level);
    }


    public void seal(@NotNull ServerLevel level) {
        if(isSealed) {
            log.warn(("AFUBlockEntity.seal: Unable to seal as AFU is already sealed"));
            return;
        }
        try {
            Pair<Set<BlockPos>, Set<BlockPos>> result = this.spread(level, getBlockPos(), maxRoomLimit);
            this.replacedAirBlocks = result.getA();
            this.sealedBlocks = result.getB();
            AFUManager.registerAFU(this, sealedBlocks, replacedAirBlocks, level);

            log.debug("Blocks to supply: {}", sealedBlocks.size());
        }catch (BlockLimitExceededException e) {
            log.error(e.getMessage());
        }
    }

    public void unseal(ServerLevel level) {
        if(!isSealed) {
            log.warn(("AFUBlockEntity.unseal: Unable to unseal as AFU is already unsealed"));
            return;
        }
        AFUManager.unregisterAFU(level, this);
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

        while (!queue.isEmpty() ) {
            BlockPos current = queue.poll();
            // Prüfe alle 6 Richtungen
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);

                // Nur weitermachen, wenn wir dort noch nicht waren UND es Luft ist
                BlockState neighborState = level.getBlockState(neighbor);
                if(!canFlow(level, neighborState, neighbor)) continue;
                if(sealed.contains(neighbor)) continue;

                if(neighborState.is(Blocks.AIR)) {
                    toBeReplaced.add(neighbor);
                }

                sealed.add(neighbor);
                queue.add(neighbor);

                // Sicherheitsstopp
                if (sealed.size() > maxBlocks) {
                    throw new BlockLimitExceededException("Room to seal is not allowed to exceed " + maxBlocks + " blocks!");
                }

            }
        }
        return new Pair<>(toBeReplaced, sealed);
    }

    private boolean canFlow(Level level, BlockState state, BlockPos pos) {
        return state.is(Blocks.AIR) || state.canBeReplaced() || !state.isCollisionShapeFullBlock(level, pos);
    }


    @Override
    public void tick(ServerLevel level) {
        if(isSealed) return;
        ticker++;
        if(ticker >= autoRetry) {
            this.seal(level);
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

    @Override
    public void onLoad() {
        super.onLoad();

        Level level = getLevel();
        if(level.isClientSide()) return;
        if(!isSealed) {
            this.seal((ServerLevel) level);
        }


    }

    @Override
    public void onChunkUnloaded() {
        if(this.isSealed) {
            this.unseal((ServerLevel) level);
        }
        super.onChunkUnloaded();
    }
}
