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
    private boolean wasSealed = false;
    @Setter
    private boolean isSealed = false;
    @Setter
    private int ticker = 0;

    private Set<BlockPos> sealedBlocks = new HashSet<>();
    private Set<BlockPos> replacedAirBlocks = new HashSet<>();


    public void increaseTicker() {
        ticker++;
    }

    public void loadFromNbt(CompoundTag compoundTag) {
        this.wasSealed = compoundTag.getBoolean("wasSealed");
        this.ticker = compoundTag.getInt("ticker");
    }

    public void saveToNbt(CompoundTag compoundTag) {
        compoundTag.putBoolean("wasSealed", isSealed);
        compoundTag.putInt("ticker", ticker);


    }



}
