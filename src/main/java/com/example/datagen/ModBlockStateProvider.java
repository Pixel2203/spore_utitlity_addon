package com.example.datagen;

import com.example.blocks.BlockRegistry;
import com.example.examplemod.SporeUtility;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;


public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, SporeUtility.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile cduFillerModel = models().cube(BlockRegistry.CDUFiller.getId().getPath(),
                modLoc("block/cdu_filler/filler-bottom"),
                modLoc("block/cdu_filler/filler-top"),
                modLoc("block/cdu_filler/filler-front"),
                modLoc("block/cdu_filler/filler-front"),
                modLoc("block/cdu_filler/filler-side"),
                modLoc("block/cdu_filler/filler-side")
        ).texture("particle", modLoc("block/cdu_filler/filler-bottom"));
        horizontalBlock(BlockRegistry.CDUFiller.get(),  cduFillerModel);
        simpleBlockItem(BlockRegistry.CDUFiller.get(), cduFillerModel);

        blockWithItem(BlockRegistry.AFU);
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject){
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

}
