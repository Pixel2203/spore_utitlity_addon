package com.mixins;

import com.Harbinger.Spore.SBlockEntities.CDUBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CDUBlockEntity.class)
public class CDURedstoneMixin {

    private static final Logger log = LoggerFactory.getLogger(CDURedstoneMixin.class);

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void addCDUTurnsOfflineWhenHavingRedstoneSignal(Level level, BlockPos blockPos, BlockState blockState, CDUBlockEntity e, CallbackInfo ci) {
        boolean hasRedstone = level.hasNeighborSignal(blockPos);
        if(hasRedstone) {
            log.info("CDURedstoneMixin | Redstone detected");
            ci.cancel();
        }

    }

    @Inject(method = "serverTick", at = @At("HEAD"), remap = false)
    private static void addCDUFuelsWhenHavingHopperNearby(Level level, BlockPos blockPos, BlockState blockState, CDUBlockEntity e, CallbackInfo ci) {
        // TODO document why this method is empty
    }
}
