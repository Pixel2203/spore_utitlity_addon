package com.example.afu;

import com.example.entity.block.AFUBlockEntity;
import com.example.entity.block.BlockEntityRegistry;
import com.example.errors.BlockLimitExceededException;
import com.example.examplemod.Config;
import com.example.sound.SoundRegistry;
import com.example.util.ITickableBlockEntity;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Slf4j
public abstract class AFUBaseEntity extends BlockEntity implements ITickableBlockEntity {

    protected final AFUContext context;
    protected final RoomScanner scanner;


    public AFUBaseEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.AFUBlockEntity.get(), blockPos, blockState);

        ContainerData containerData = new ContainerData() {
            @Override
            public int get(int id) {
                if(id != 0) throw new IndexOutOfBoundsException("Provided an id that is not covered by ContainerData");
                return AFUBaseEntity.this.context.isActive() ? 1 : 0;
            }

            @Override
            public void set(int id, int value) {
                if(id != 0) throw new IndexOutOfBoundsException("Provided an id that is not covered by ContainerData");
                if(value != 0 && value != 1) throw new IndexOutOfBoundsException("Provided an id that is not covered by ContainerData");
                AFUBaseEntity.this.context.setActive(value == 1);
            }

            @Override
            public int getCount() {
                return 1;
            }
        };

        this.context = new AFUContext(Config.AFU_RETRY_INTERVAL.get(), Config.AFU_INVALIDATION_INTERVAL.get(),containerData);
        this.scanner = new RoomScanner(Config.AFU_BLOCK_LIMIT.get());
    }


    public boolean isActive() {
        return context.isActive();
    }

    public void breach(BlockPos breachedPos) {
        log.debug("AFUBlockEntity.breach({})", breachedPos);
        Level level = getLevel();
        if(level.isClientSide()) return;
        if(!context.isSealed()) return;
        int currentlySealedBlocks = this.context.getSealedBlocks().size();
        int allowedToSealAdditionally = this.scanner.maxBlocksLimit() - currentlySealedBlocks;
        // perform incremental scan
        ScanResult scanResult = null;
        try {
            scanResult = this.scanner.incrementalScan((ServerLevel) level, breachedPos);
            boolean canSeal = scanResult.sealedBlocks().size() <= allowedToSealAdditionally;
            if(canSeal) {
                AFUManager.registerAFU(this, context.getSealedBlocks(), context.getReplacedAirBlocks(), (ServerLevel) level);
                return;
            }
        } catch (BlockLimitExceededException e) {
            log.info("AFUBlockEntity.breach: Incremental Seal failed at {}", breachedPos);
        }
        this.unseal((ServerLevel) level);
    }


    public void seal(@NotNull ServerLevel level) {
        if(context.isSealed() && !this.context.getSealedBlocks().isEmpty() && !this.context.getReplacedAirBlocks().isEmpty()) {
            log.warn(("AFUBlockEntity.seal: Unable to seal as AFU is already sealed"));
            return;
        }
        try {
            ScanResult result = this.scanner.scan(level, getBlockPos());
            context.getSealedBlocks().addAll(result.sealedBlocks());

            AFUManager.registerAFU(this, context.getSealedBlocks(), context.getReplacedAirBlocks(), level);
            log.debug("Blocks to supply: {}", context.getSealedBlocks().size());
            context.setSealed(true);
            this.setChanged();
        }catch (BlockLimitExceededException e) {
            log.error(e.getMessage());
        }
    }

    public void unseal(ServerLevel level) {
        if(!context.isSealed()) {
            log.warn(("AFUBlockEntity.unseal: Unable to unseal as AFU is already unsealed"));
            return;
        }
        Set<BlockPos> sealedBlocks = context.getSealedBlocks();
        Set<BlockPos> replacedAirBlocks = context.getReplacedAirBlocks();
        if(sealedBlocks.isEmpty()) {
            log.warn("AFUBlockEntity.unseal: sealedBlocks is empty, cannot unseal");
            return;
        }
        if(replacedAirBlocks.isEmpty()) {
            log.warn("AFUBlockEntity.unseal: replacedAirBlocks is empty,  cannot unseal");
            return;
        }
        AFUManager.unregisterAFU(level, sealedBlocks, replacedAirBlocks);

        sealedBlocks.clear();
        replacedAirBlocks.clear();
        context.setSealed(false);
        this.setChanged();
    }


    @Override
    public void tick(ServerLevel level) {

        if(context.isActive()) {
            level.playSound(null, getBlockPos(), SoundRegistry.AFU_IDLE_SOUND.get(), SoundSource.NEUTRAL);
        }

        if(context.isSealed()) {
            context.setInvalidationTicker(context.getInvalidationTicker() + 1);
            if(context.getInvalidationTicker() % context.getAutoInvalidationInterval() == 0) {
                context.setInvalidationTicker(0);
                try {
                    scanner.scan(level, getBlockPos());
                } catch (BlockLimitExceededException e) {
                    this.unseal(level);
                }
            }
            return;
        }

        context.increaseTicker();
        if(context.getTicker() >= context.getAutoRetryInterval()) {
            this.seal(level);
            context.setTicker(0);
        }
    }
}
