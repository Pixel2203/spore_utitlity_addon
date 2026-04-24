package com.example.entity.block;

import com.Harbinger.Spore.Core.Sblocks;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import com.example.blocks.CDUFillerBlock;
import com.example.menu.CDUFillerMenu;
import com.example.util.ITickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CDUFillerBlockEntity extends BlockEntity implements ITickableBlockEntity, Container, MenuProvider {

    private static final Logger log = LoggerFactory.getLogger(CDUFillerBlockEntity.class);


    private final List<CDUBlockEntity> connectedCDUs;

    public CDUFillerBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(BlockEntityRegistry.CduInputConnectorBlockEntity.get(), p_155229_, p_155230_);
        this.connectedCDUs = new ArrayList<>();
    }

    @Override
    public void tick(ServerLevel level) {
         if(connectedCDUs.isEmpty()) return;
         if(!hasIceCanister()) return;

         for(CDUBlockEntity connectedCDU : connectedCDUs) {

             int cduFuel = connectedCDU.getFuel();
             if(cduFuel > 0) continue;
             refuelCDU(connectedCDU);

             if(!hasIceCanister()) break;
         }

    }

    private void refuelCDU(CDUBlockEntity connectedCDU) {
        ItemStack extraced = inventory.extractItem(0, 1, false);
        if(!extraced.is(Sitems.ICE_CANISTER.get())) {
            log.warn("CDUFillerBlockEntity.refuelCDU: Extracted Item was not of type IceCanister, found: {}" , extraced);
            return;
        }
        connectedCDU.setFuel(connectedCDU.maxFuel);
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
            log.error("CDUFillerBlockEntity.connectToCDU: level is null");
            return;
        }
        if(!level.getBlockState(blockPos).is(Sblocks.CDU.get())) {
            log.error("CDUFillerBlockEntity.connectToCDU: blockstate is not of type cdu");
            return;
        }
        if(isConnectedToCDU(blockPos)) {
            log.error("CDUFillerBlockEntity.connectToCDU: already connected to cdu");
            return;
        }

        CDUBlockEntity cduBlockEntity = (CDUBlockEntity) level.getBlockEntity(blockPos);
        this.connectedCDUs.add(cduBlockEntity);
        log.debug("CDUFillerBlockEntity.connectToCDU: connected to cdu at {}", cduBlockEntity.getBlockPos());
    }

    public void disconnectFromCDU(BlockPos blockPos) {
        if(Objects.isNull(level)) {
            log.error("CDUFillerBlockEntity.disconnectFromCDU: level is null");
            return;
        }
        if(!isConnectedToCDU(blockPos)) {
            log.error("CDUFillerBlockEntity.disconnectFromCDU: disconnect from CDU that was not connected!");
            return;
        }
        List<CDUBlockEntity> cdus = this.connectedCDUs.stream()
                .filter(entity -> entity.getBlockPos().equals(blockPos))
                .toList();

        if(cdus.size() > 1) {
            log.warn("CDUFillerBlockEntity.disconnectFromCDU: Found more than one CDU at {}", blockPos);
        }

        this.connectedCDUs.remove(cdus.get(0));
        log.debug("CDUFillerBlockEntity.connectToCDU: disconnected from cdu at {}", blockPos);

    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyOptional = LazyOptional.of(() -> inventory);
        if(Objects.isNull(level)) return;
        if(level.isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel) level;
        if(isConnectedToCDU()) return;
        List<BlockPos> foundCDUs = this.getNearbyCDUs(serverLevel, getBlockPos());
        if(foundCDUs.isEmpty()) return;

        for(BlockPos cduPos : foundCDUs) {
            if(isConnectedToCDU(cduPos)) return;
            connectToCDU(cduPos);
            log.debug("CDUFillerBlockEntity.onPlace: found CDU nearby at {}" , cduPos);
        }
    }

    public boolean isConnectedToCDU() {
        return !this.connectedCDUs.isEmpty();
    }

    public boolean isConnectedToCDU(BlockPos blockPos) {
        return this.connectedCDUs.stream().anyMatch(cdu -> cdu.getBlockPos().equals(blockPos));
    }


    /**
     * @param level ServerLevel
     * @param blockPos BlockPos
     * @return Returns an Immutable List of CDUBlockEntity
     */
    private List<BlockPos> getNearbyCDUs(ServerLevel level, BlockPos blockPos) {
        Direction facing = getBlockState().getValue(CDUFillerBlock.FACING);
        Direction right = facing.getClockWise();
        Direction left = facing.getCounterClockWise();

        BlockPos rightBlockPos = blockPos.relative(right);
        BlockPos leftBlockPos = blockPos.relative(left);

        List<CDUBlockEntity> nearbyCDUs = new ArrayList<>();

        CDUBlockEntity leftEntity = checkForCDU(level, leftBlockPos);
        CDUBlockEntity rightEntity = checkForCDU(level, rightBlockPos);
        nearbyCDUs.add(leftEntity);
        nearbyCDUs.add(rightEntity);

        return nearbyCDUs.stream()
                .filter(Objects::nonNull)
                .map(BlockEntity::getBlockPos)
                .toList();
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

    @Override
    public Component getDisplayName() {
        return Component.literal("cdu_filler");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CDUFillerMenu(containerId, inventory, this);
    }
}
