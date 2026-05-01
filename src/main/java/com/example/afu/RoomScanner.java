package com.example.afu;

import com.example.errors.BlockLimitExceededException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public record RoomScanner(int maxBlocksLimit) {

    public ScanResult scan(ServerLevel level, BlockPos startPos) throws BlockLimitExceededException {
        // 1. Hier speichern wir alle Positionen, die wir schon besucht haben
        Set<BlockPos> sealed = new HashSet<>();

        // 2. Hier kommen die Blöcke rein, die noch geprüft werden müssen
        Queue<BlockPos> queue = new LinkedList<>();

        // Wir starten beim Block direkt am Air Purifier
        queue.add(startPos);

        BlockState startedState = level.getBlockState(startPos);
        if(canFlow(level, startedState, startPos)) {
            sealed.add(startPos);
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            // Prüfe alle 6 Richtungen
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);

                // Nur weitermachen, wenn wir dort noch nicht waren UND es Luft ist
                BlockState neighborState = level.getBlockState(neighbor);
                if (!canFlow(level, neighborState, neighbor)) continue;
                if (sealed.contains(neighbor)) continue;

                sealed.add(neighbor);
                queue.add(neighbor);

                // Sicherheitsstopp
                if (sealed.size() > this.maxBlocksLimit) {
                    throw new BlockLimitExceededException("Room to seal is not allowed to exceed " + this.maxBlocksLimit + " blocks!");
                }

            }
        }
        return new ScanResult(sealed);
    }

    public ScanResult incrementalScan(ServerLevel level, BlockPos startPos) throws BlockLimitExceededException {
        // 1. Hier speichern wir alle Positionen, die wir schon besucht haben
        Set<BlockPos> sealed = new HashSet<>();
        // 2. Hier kommen die Blöcke rein, die noch geprüft werden müssen
        Queue<BlockPos> queue = new LinkedList<>();

        // Wir starten beim Block direkt am Air Purifier
        queue.add(startPos);
        //visited.add(startPos);

        BlockState startedState = level.getBlockState(startPos);
        if(canFlow(level, startedState, startPos)) {
            sealed.add(startPos);
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            // Prüfe alle 6 Richtungen
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);

                // Nur weitermachen, wenn wir dort noch nicht waren UND es Luft ist
                BlockState neighborState = level.getBlockState(neighbor);
                if (!canFlow(level, neighborState, neighbor)) continue;
                if (sealed.contains(neighbor) || AFUManager.isSealed(neighbor)) continue;

                sealed.add(neighbor);
                queue.add(neighbor);

                // Sicherheitsstopp
                if (sealed.size() > this.maxBlocksLimit) {
                    throw new BlockLimitExceededException("Room to seal is not allowed to exceed " + this.maxBlocksLimit + " blocks!");
                }

            }
        }
        return new ScanResult(sealed);
    }



    public static boolean canFlow(Level level, BlockState state, BlockPos pos) {
        return state.is(Blocks.AIR) || state.canBeReplaced() || !state.isCollisionShapeFullBlock(level, pos);
    }


}
