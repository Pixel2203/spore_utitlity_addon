package com.example.creativeTabs;

import com.example.blocks.BlockRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class TabFactory {
    public static final CreativeModeTab MainTab =
            CreativeModeTab.builder()
                    .icon(() -> new ItemStack(BlockRegistry.CDUFiller.get()))
                    .title(Component.translatable("creativetab.main_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(BlockRegistry.CDUFiller.get());
                        output.accept(BlockRegistry.AFU.get());
                    }).build();
}
