package com.example.afu;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.HashSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class AFUContext {
    private final int autoRetryInterval;

    @Setter
    private boolean isSealed = false;
    @Setter
    private int ticker = 0;
    @Setter
    private boolean isActive = false;

    private Set<BlockPos> sealedBlocks = new HashSet<>();
    private Set<BlockPos> replacedAirBlocks = new HashSet<>();


    public void increaseTicker() {
        ticker++;
    }

    public void loadFromNbt(CompoundTag compoundTag) {
        this.isSealed = compoundTag.getBoolean("isSealed");
        this.ticker = compoundTag.getInt("ticker");
        this.isActive = compoundTag.getBoolean("isActive");
    }

    public void saveToNbt(CompoundTag compoundTag) {
        compoundTag.putBoolean("isSealed", isSealed);
        compoundTag.putInt("ticker", ticker);
        compoundTag.putBoolean("isActive", isActive);


    }



}
