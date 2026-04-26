package com.example.afu;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
public class AFUContext {

    private final int autoRetryInterval;
    private final Set<BlockPos> sealedBlocks = new HashSet<>();
    private final Set<BlockPos> replacedAirBlocks = new HashSet<>();
    private final ItemStackHandler inventory = new ItemStackHandler(1);


    private boolean isSealed = false;
    private int ticker = 0;
    private boolean isActive = false;




    private LazyOptional<IItemHandler> lazyOptional = LazyOptional.empty();


    public void increaseTicker() {
        ticker++;
    }

    public void loadFromNbt(CompoundTag compoundTag) {
        this.isSealed = compoundTag.getBoolean("isSealed");
        this.ticker = compoundTag.getInt("ticker");
        this.isActive = compoundTag.getBoolean("isActive");
        this.inventory.deserializeNBT(compoundTag.getCompound("inventory"));
    }

    public void saveToNbt(CompoundTag compoundTag) {
        compoundTag.putBoolean("isSealed", isSealed);
        compoundTag.putInt("ticker", ticker);
        compoundTag.putBoolean("isActive", isActive);
        compoundTag.put("inventory", inventory.serializeNBT());


    }



}
