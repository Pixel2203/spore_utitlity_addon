package com.example.event;

import com.example.afu.AFUManager;
import com.example.afu.RoomScanner;
import com.example.entity.block.AFUBlockEntity;
import com.example.examplemod.SporeUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SporeUtility.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AFUBreachHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        checkAndBreach((Level) event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        checkAndBreach((Level) event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        // Iterate a copy in case another mod mutates the list
        for (BlockPos pos : event.getAffectedBlocks()) {
            checkAndBreach(level, pos);
        }
    }

    private static void checkAndBreach(Level level, BlockPos changedPos) {
        AFUBlockEntity afu = AFUManager.getOwner(changedPos);
        if (afu == null) afu = AFUManager.getSealedOwner(changedPos);

        if (afu == null) {
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = changedPos.relative(dir);
                afu = AFUManager.getOwner(neighbor);
                if (afu == null) afu = AFUManager.getSealedOwner(neighbor);
                if (afu != null) break;
            }
        }

        if (afu != null) {
            afu.breach(changedPos);
        }
    }
}