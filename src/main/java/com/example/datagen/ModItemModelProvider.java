package com.example.datagen;

import com.example.examplemod.SporeUtility;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SporeUtility.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {




    }
    private ItemModelBuilder simpleItem(RegistryObject<Item> item){
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(SporeUtility.MODID, "item/" + item.getId().getPath()));
    }
    private ItemModelBuilder simpleItemFromTexture(RegistryObject<Item> item, String texture){
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(SporeUtility.MODID, "item/" + texture));
    }


    private ItemModelBuilder simpleTextureForPredicate(String state, String texture){
        return withExistingParent(state,
                ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(SporeUtility.MODID, "item/" + texture));
    }
}
