package com.example.afu;

import net.minecraft.core.BlockPos;

import java.util.Set;

public record ScanResult(Set<BlockPos> sealedBlocks, Set<BlockPos> cleanedAirBlocks) {
}
