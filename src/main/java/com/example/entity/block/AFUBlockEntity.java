package com.example.entity.block;

import com.example.afu.*;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
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

public class AFUBlockEntity extends AFUBaseEntity implements MenuProvider, Container {

    private static final Logger log = LoggerFactory.getLogger(AFUBlockEntity.class);


    public AFUBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);

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
        context.setLazyOptional(LazyOptional.of(context::getInventory)); ;
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
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AFUMenu(id, inv, this, context.getContainerData());
    }


    private boolean isSealValid() {
        try {
            scanner.scan((ServerLevel) getLevel(), getBlockPos());
            return true;
        }catch (BlockLimitExceededException e){
            return false;
        }
    }

    @Override
    public int getContainerSize() {
        return context.getInventory().getSlots();
    }

    @Override
    public boolean isEmpty() {
        return context.getInventory().getStackInSlot(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return context.getInventory().getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return context.getInventory().extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStackHandler inventory = context.getInventory();
        ItemStack stack = inventory.getStackInSlot(slot);
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        context.getInventory().setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(Player p_18946_) {
        return true;
    }

    @Override
    public void clearContent() {
        context.getInventory().setStackInSlot(0, ItemStack.EMPTY);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return context.getLazyOptional().cast();
        }
        return super.getCapability(cap);
    }
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.context.getLazyOptional().invalidate();

    }

    public void toggleActive() {
        ServerLevel level = (ServerLevel) getLevel();
        context.setActive(!context.isActive());
        if (context.isActive()) {
            seal(level);
        } else {
            unseal(level);
        }
        setChanged();
    }
}
