package com.example.network;

import com.example.examplemod.SporeUtility;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(SporeUtility.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public PacketHandler() {
        // In your mod constructor or common setup event:
        int id = 0;
        CHANNEL.registerMessage(id++, AFUTogglePacket.class,
                AFUTogglePacket::encode,
                AFUTogglePacket::decode,
                AFUTogglePacket::handle
        );
    }


}
