package com.example.afu;

import com.example.blocks.BlockRegistry;
import com.example.entity.block.AFUBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AFUManager {
    // Speichert: Welche Luft-Position gehört zu welcher AFU?
    // Map<LuftPos, AFUPos>
    private static final Map<BlockPos, AFUBlockEntity> AIR_MAP = new HashMap<>();
    private static final Map<BlockPos, AFUBlockEntity> SEALED_MAP = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(AFUManager.class);

    public static void addSealedBlock(BlockPos pos, AFUBlockEntity blockEntity) {
        if(SEALED_MAP.containsKey(pos)) {
            log.warn("Block {} already sealed", pos);
            return;
        }
        SEALED_MAP.put(pos, blockEntity);
    }

    public static void removeSealedBlock(BlockPos pos) {
        if(!SEALED_MAP.containsKey(pos)) {
            log.warn("Block {} was not sealed", pos);
            return;
        }
        SEALED_MAP.remove(pos);
    }

    public static boolean isSealed(BlockPos pos) {
        return SEALED_MAP.containsKey(pos);
    }

    public static void unregisterAFU(ServerLevel level, AFUBlockEntity afu) {
        // Entfernt alle Einträge, die zu dieser AFU gehören
        Set<BlockPos> foundAirBlocks = AIR_MAP.entrySet().stream().filter(blockPosAFUBlockEntityEntry -> blockPosAFUBlockEntityEntry.getValue() == afu)
                        .map(Map.Entry::getKey).collect(Collectors.toSet());
        foundAirBlocks.forEach(AIR_MAP::remove);

        // Entfernt alle Sealed Zustände
        var sealedBlocks = SEALED_MAP.entrySet()
                .stream()
                .filter(blockPosAFUBlockEntityEntry ->  blockPosAFUBlockEntityEntry.getValue() == afu)
                .map(Map.Entry::getKey).collect(Collectors.toSet());

        sealedBlocks.forEach(AFUManager::removeSealedBlock);


        foundAirBlocks.forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if(state.is(BlockRegistry.CleanedAir.get())) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        });
    }

    public static void registerAFU(AFUBlockEntity afu, Set<BlockPos> sealedBlocks, Set<BlockPos> airBlocks, ServerLevel level) {
        sealedBlocks.forEach(pos -> AFUManager.addSealedBlock(pos, afu));
        airBlocks.forEach(pos -> AIR_MAP.put(pos, afu));

        airBlocks.forEach((pos) -> {
            level.setBlock(pos, BlockRegistry.CleanedAir.get().defaultBlockState(), 2);
        });
    }

    public static AFUBlockEntity getOwner(BlockPos airPos) {
        return AIR_MAP.get(airPos);
    }
}