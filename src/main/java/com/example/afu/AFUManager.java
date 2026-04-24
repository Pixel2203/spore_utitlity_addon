package com.example.afu;

import com.example.entity.block.AFUBlockEntity;
import net.minecraft.core.BlockPos;
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



    public static void registerAir(BlockPos air, AFUBlockEntity afu) {

    }

    public static Set<BlockPos> unregisterAFU(AFUBlockEntity afu) {
        // Entfernt alle Einträge, die zu dieser AFU gehören
        Set<BlockPos> foundAirBlocks = AIR_MAP.entrySet().stream().filter(blockPosAFUBlockEntityEntry -> blockPosAFUBlockEntityEntry.getValue() == afu)
                        .map(Map.Entry::getKey).collect(Collectors.toSet());
        foundAirBlocks.forEach(AFUManager::clearAirAt);

        // Entfernt alle Sealed Zustände
        var sealedBlocks = SEALED_MAP.entrySet()
                .stream()
                .filter(blockPosAFUBlockEntityEntry ->  blockPosAFUBlockEntityEntry.getValue() == afu)
                .map(Map.Entry::getKey).collect(Collectors.toSet());

        sealedBlocks.forEach(AFUManager::removeSealedBlock);

        return foundAirBlocks;
    }

    public static void registerAFU(AFUBlockEntity afu, Set<BlockPos> sealedBlocks, Set<BlockPos> airBlocks) {
        sealedBlocks.forEach(pos -> AFUManager.addSealedBlock(pos, afu));
        airBlocks.forEach(pos -> AIR_MAP.put(pos, afu));

    }

    public static AFUBlockEntity getOwner(BlockPos airPos) {
        return AIR_MAP.get(airPos);
    }

    public static void clearAirAt(BlockPos pos) {
        AIR_MAP.remove(pos);
    }
}