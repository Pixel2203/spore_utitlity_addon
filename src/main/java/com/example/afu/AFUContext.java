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

    private Set<BlockPos> sealedBlocks = new HashSet<>();
    private Set<BlockPos> replacedAirBlocks = new HashSet<>();


    public void increaseTicker() {
        ticker++;
    }

    public void loadFromNbt(CompoundTag compoundTag) {
        this.isSealed = compoundTag.getBoolean("isSealed");
        this.ticker = compoundTag.getInt("ticker");

        this.sealedBlocks.clear();
        long[] sealedArray = compoundTag.getLongArray("sealedBlocks");
        for (long l : sealedArray) {
            this.sealedBlocks.add(BlockPos.of(l));
        }

        // Replaced Air Blocks laden
        this.replacedAirBlocks.clear();
        long[] replacedArray = compoundTag.getLongArray("replacedAirBlocks");
        for (long l : replacedArray) {
            this.replacedAirBlocks.add(BlockPos.of(l));
        }
    }

    public void saveToNbt(CompoundTag compoundTag) {
        compoundTag.putBoolean("isSealed", isSealed);
        compoundTag.putInt("ticker", ticker);

        long[] sealedArray = this.sealedBlocks.stream()
                .mapToLong(BlockPos::asLong)
                .toArray();
        compoundTag.putLongArray("sealedBlocks", sealedArray);

        long[] replacedArray = this.replacedAirBlocks.stream()
                .mapToLong(BlockPos::asLong)
                .toArray();
        compoundTag.putLongArray("replacedAirBlocks", replacedArray);
    }



}
