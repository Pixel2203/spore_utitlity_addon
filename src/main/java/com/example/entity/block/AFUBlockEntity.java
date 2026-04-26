package com.example.entity.block;

import com.example.afu.AFUContext;
import com.example.afu.AFUManager;
import com.example.afu.RoomScanner;
import com.example.afu.ScanResult;
import com.example.blocks.BlockRegistry;
import com.example.errors.BlockLimitExceededException;
import com.example.examplemod.Config;
import com.example.menu.AFUMenu;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.util.tuples.Pair;

import java.util.*;

public class AFUBlockEntity extends BlockEntity implements ITickableBlockEntity, MenuProvider, Container {

    private static final Logger log = LoggerFactory.getLogger(AFUBlockEntity.class);


    private final AFUContext context;
    private final RoomScanner scanner;

    public AFUBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.AFUBlockEntity.get(), blockPos, blockState);
        this.context = new AFUContext(Config.AFU_RETRY_INTERVAL.get());
        this.scanner = new RoomScanner(Config.AFU_RETRY_INTERVAL.get());
    }

    public boolean isActive() {
        return context.isActive();
    }

    public void activate() {

    }

    public void deactivate() {

    }

    public void breach(BlockPos breachedPos) {
        Level level = getLevel();
        if(level.isClientSide()) return;
        this.unseal((ServerLevel) level);
        this.seal((ServerLevel) level);
    }


    public void seal(@NotNull ServerLevel level) {
        if(context.isSealed() && !this.context.getSealedBlocks().isEmpty() && !this.context.getReplacedAirBlocks().isEmpty()) {
            log.warn(("AFUBlockEntity.seal: Unable to seal as AFU is already sealed"));
            return;
        }
        try {
            ScanResult result = this.scanner.scan(level, getBlockPos());
            context.getReplacedAirBlocks().addAll(result.cleanedAirBlocks());
            context.getSealedBlocks().addAll(result.sealedBlocks());

            AFUManager.registerAFU(this, context.getSealedBlocks(), context.getReplacedAirBlocks(), level);

            log.debug("Blocks to supply: {}", context.getSealedBlocks().size());
            context.setSealed(true);
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
    }






    @Override
    public void tick(ServerLevel level) {
        if(context.isSealed()) return;
        context.increaseTicker();
        if(context.getTicker() >= context.getAutoRetryInterval()) {
            this.seal(level);
            context.setTicker(0);
        }
    }

    @Override
    public void load(@NotNull CompoundTag compoundTag) {
        super.load(compoundTag);
        this.context.loadFromNbt(compoundTag);

    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        this.context.saveToNbt(compoundTag);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        Level level = getLevel();
        if(level.isClientSide()) return;
        if(context.isSealed()) {
            this.seal((ServerLevel) level);
        }
    }

    /**
     * Well well well, this is a fince piece of shit, you see the setSealed(true) after the unseal and wonder why ?
     * Yeah well lets just put it simple, onChunkUnloaded will be called multiple times, so now you would be asking yourself, WAIT WHAT?
     * Lets put it this way, saveAdditional --> onChunkUnloaded --> unseal --> AFUMANAGER --> setBlock --> Loads Chunks --> onLoad() --> saveAdditional --> onChunkUnloaded --> unseal --> returns because sealedBlocks is empty
     */
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if(context.isSealed()) {
            this.unseal((ServerLevel) level);
            this.context.setSealed(true);
        }

    }


    @Override
    public Component getDisplayName() {
        return Component.translatable("menus.afu");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        return new AFUMenu(p_39954_, p_39955_, this);
    }

    private final ItemStackHandler inventory = new ItemStackHandler(1);
    private LazyOptional<IItemHandler> lazyOptional = LazyOptional.empty();

    @Override
    public int getContainerSize() {
        return inventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        return inventory.getStackInSlot(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return inventory.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(Player p_18946_) {
        return true;
    }

    @Override
    public void clearContent() {
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return this.lazyOptional.cast();
        }
        return super.getCapability(cap);
    }
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.lazyOptional.invalidate();

    }
}
