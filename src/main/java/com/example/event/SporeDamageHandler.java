package com.example.event;

import com.example.afu.AFUManager;
import com.example.examplemod.SporeUtility;
import com.google.common.eventbus.Subscribe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SporeUtility.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SporeDamageHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if(player.isCreative() || player.isSpectator()) return;
        if(player.level().isClientSide()) return;

        if(player.tickCount % 20 == 0){
            BlockPos head = BlockPos.containing(player.getEyePosition());
            boolean isSealed = AFUManager.isSealed(head);
            if(isSealed) return;
            float maxHealth = player.getMaxHealth();
            player.hurt(player.damageSources().drown(), (float) (maxHealth * 0.1));
        }
    }


}
