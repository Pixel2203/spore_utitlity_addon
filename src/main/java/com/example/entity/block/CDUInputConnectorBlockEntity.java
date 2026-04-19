package com.example.entity.block;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Objects;

public class CDUInputConnectorBlockEntity extends BlockEntity implements ITickableBlockEntity, Container {

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
         if(!hasIceCanister()) return;
         int cduFuel = connectedCDU.getFuel();
         if(cduFuel > 0) return;
         refuelCDU();
    }

    private void refuelCDU() {
        ItemStack extraced = inventory.extractItem(0, 1, false);
        if(!extraced.is(Sitems.ICE_CANISTER.get())) {
            log.warn("CDUInputConnectorBlockEntity.refuelCDU: Extracted Item was not of type IceCanister, found: {}" , extraced);
        }
        this.connectedCDU.setFuel(this.connectedCDU.maxFuel);
    }

    private boolean hasIceCanister() {
        return this.inventory.getStackInSlot(0).is(Sitems.ICE_CANISTER.get());
    }

    private final ItemStackHandler inventory = new ItemStackHandler(1);
    private LazyOptional<IItemHandler> lazyOptional = LazyOptional.empty();

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
    public void drops() {
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for(int i = 0; i < inventory.getSlots(); i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, container);
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
        log.debug("CDUInputConnectorBlockEntity.connectToCDU: connected to cdu at {}", this.connectedCDUPos);
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
        log.debug("CDUInputConnectorBlockEntity.connectToCDU: disconnected from cdu at {}", this.connectedCDUPos);
        this.connectedCDUPos = null;
        this.connectedCDU = null;

    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(Objects.isNull(level)) return;
        if(level.isClientSide()) return;
        if(isConnectedToCDU()) return;
        CDUBlockEntity foundCDU = this.getNearbyCDU(level, getBlockPos());
        if(Objects.isNull(foundCDU)) return;

        log.debug("CDUInputConnectorBlock.onPlace: found CDU nearby at {}" , foundCDU.getBlockPos());
        this.connectToCDU(foundCDU.getBlockPos());

    }

    public boolean isConnectedToCDU() {
        return Objects.nonNull(connectedCDU);
    }

    public boolean isConnectedToCDU(BlockPos blockPos) {
        return Objects.nonNull(connectedCDU) && connectedCDUPos.equals(blockPos);
    }



    @javax.annotation.Nullable
    private CDUBlockEntity getNearbyCDU(Level level, BlockPos blockPos) {
        return java.util.Arrays.stream(net.minecraft.core.Direction.values())
                .map(blockPos::relative)
                .map(blockPos1 -> this.checkForCDU(level, blockPos1))
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @javax.annotation.Nullable
    private CDUBlockEntity checkForCDU(Level level, BlockPos blockPos) {
        if(Objects.isNull(level)) return null;
        BlockState blockState = level.getBlockState(blockPos);
        if(!blockState.is(Sblocks.CDU.get())) return null;
        return (CDUBlockEntity) level.getBlockEntity(blockPos);
    }

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
    public int getMaxStackSize() {
        return Container.super.getMaxStackSize();
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
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(Sitems.ICE_CANISTER.get()) && Container.super.canPlaceItem(slot, stack);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.inventory.deserializeNBT(tag.getCompound("inventory"));
    }
}
