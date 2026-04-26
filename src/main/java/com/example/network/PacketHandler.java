package com.example.network;

import com.example.examplemod.SporeUtility;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    public static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel CHANNEL;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void registerPackets() {
        CHANNEL.messageBuilder(AFUTogglePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                        .decoder(AFUTogglePacket::decode)
                        .encoder(AFUTogglePacket::encode)
                        .consumerMainThread(AFUTogglePacket::handle)
                        .add();

    }

    public static void registerChannel() {
        CHANNEL = NetworkRegistry.ChannelBuilder.named(
                        ResourceLocation.fromNamespaceAndPath(SporeUtility.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        registerPackets();
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }
    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message );
    }


}
