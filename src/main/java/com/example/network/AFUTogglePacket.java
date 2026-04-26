package com.example.network;

import com.example.entity.block.AFUBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AFUTogglePacket {
    private final BlockPos pos;

    public AFUTogglePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(AFUTogglePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static AFUTogglePacket decode(FriendlyByteBuf buf) {
        return new AFUTogglePacket(buf.readBlockPos());
    }

    public static void handle(AFUTogglePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ServerLevel level = player.serverLevel();
            BlockEntity be = level.getBlockEntity(msg.pos);
            if (be instanceof AFUBlockEntity afu) {
                afu.toggleActive();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}