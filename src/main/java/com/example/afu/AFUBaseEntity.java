package com.example.afu;

import com.example.entity.block.BlockEntityRegistry;
import com.example.errors.BlockLimitExceededException;
import com.example.examplemod.Config;
import com.example.items.ItemRegistry;
import com.example.sound.SoundRegistry;
import com.example.util.ITickableBlockEntity;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

@Slf4j
public abstract class AFUBaseEntity extends BlockEntity implements ITickableBlockEntity {

    protected final AFUContext context;
    protected final RoomScanner scanner;


    protected AFUBaseEntity(BlockPos blockPos, BlockState blockState) {
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
        log.debug("AFUBlockEntity.breach BreachedPos: {})", breachedPos);
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
            if(Objects.isNull(getActiveFilter())) return;
            int filterInterval = this.calculateFilterUseInterval(result.sealedBlocks().size());
            context.setFilterInterval(filterInterval);
            context.getSealedBlocks().addAll(result.sealedBlocks());
            AFUManager.registerAFU(this, context.getSealedBlocks(), context.getReplacedAirBlocks(), level);
            context.setSealed(true);
            this.setChanged();


            log.debug("Blocks to supply: {}", context.getSealedBlocks().size());
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
        if(sealedBlocks.isEmpty()) {
            log.warn("AFUBlockEntity.unseal: sealedBlocks is empty, cannot unseal");
            return;
        }
        AFUManager.unregisterAFU(level, sealedBlocks);
        context.setFilterInterval(0);
        sealedBlocks.clear();
        context.setSealed(false);
        this.setChanged();
    }



    @Override
    public void tick(ServerLevel level) {
        if(!isActive()) return;

        if(context.isSealed()) {
            onSealedTick(level);
        }else {
            onUnsealedTick(level);
        }
    }

    private void onSealedTick(ServerLevel level) {
        progressInvalidation(level);
        context.setFilterTicker(context.getFilterTicker() + 1);
        if(context.getFilterTicker() >= context.getFilterInterval()) {
            context.setFilterTicker(0);
            onFilterTick(level);
        }
        return;
    }

    private void onUnsealedTick(ServerLevel level) {
        progressInvalidation(level);

        context.increaseTicker();

        if(context.getTicker() >= context.getAutoRetryInterval()) {
            this.seal(level);
            context.setTicker(0);
        }
    }

    private void progressInvalidation(ServerLevel level) {
        context.setInvalidationTicker(context.getInvalidationTicker() + 1);
        if(context.getInvalidationTicker() % context.getAutoInvalidationInterval() == 0) {
            context.setInvalidationTicker(0);
            try {
                scanner.scan(level, getBlockPos());
            } catch (BlockLimitExceededException e) {
                this.unseal(level);
            }
        }
    }

    private void onFilterTick(ServerLevel level) {
        var filter = getActiveFilter();
        if(Objects.isNull(filter)) {
            log.debug("AFUBlockEntity.onFilterTick: Filter is null, has been removed or broke");
            this.unseal(level);
            return;
        }
        int oldDamageValue = filter.getDamageValue();

        ItemStack updatedFiler = filter.copy();
        updatedFiler.setDamageValue(oldDamageValue + 1);
        if(oldDamageValue >= filter.getMaxDamage()) updatedFiler = ItemStack.EMPTY;
        context.getInventory().setStackInSlot(0, updatedFiler);
        setChanged();
    }

    @Nullable
    private ItemStack getActiveFilter() {
        ItemStack filter = this.context.getInventory().getStackInSlot(0);
        if(!filter.is(ItemRegistry.Filter.get())) {
            log.error(("AFUBlockEntity.filter: Item is not in Filter"));
            return null;
        }
        return filter.copy();
    }

    private int calculateFilterUseInterval(int roomSize) {
        return Config.AFU_BLOCK_LIMIT.get() / roomSize;
    }

}
