package com.example.datagen;

import com.example.blocks.BlockRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BlockRegistry.CDUFiller.get())
                .define('H', Items.HOPPER)
                .define('S', Items.STONE)
                .define('C', Items.CHEST)
                .pattern("SSS")
                .pattern("HCH")
                .pattern("SSS")
                .unlockedBy("has_hopper", has(Items.HOPPER))
                .save(pWriter);
    }
}
