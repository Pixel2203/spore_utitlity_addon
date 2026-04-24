package com.example.datagen.loot;

import com.example.blocks.BlockRegistry;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.dropSelf(BlockRegistry.CDUFiller.get());
        this.dropNothing(BlockRegistry.CleanedAir.get());
        this.dropSelf(BlockRegistry.AFU.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return BlockRegistry.getRegisteredBlocks().stream().map(RegistryObject::get)::iterator;
    }
    private void dropNothing(Block block){
        this.dropOther(block, Items.AIR);
    }
}
