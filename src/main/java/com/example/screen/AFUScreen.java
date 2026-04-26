package com.example.screen;

import com.example.examplemod.SporeUtility;
import com.example.menu.AFUMenu;
import com.example.menu.CDUFillerMenu;
import com.example.menu.ModMenuTypes;
import com.example.network.AFUTogglePacket;
import com.example.network.PacketHandler;
import com.example.screen.components.ToggleImageButton;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@Slf4j
public class AFUScreen extends AbstractContainerScreen<AFUMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(SporeUtility.MODID,"textures/gui/afu-menu.png");
    private ToggleImageButton powerButton;

    public AFUScreen(AFUMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE,x,y, 0, 0, imageWidth, imageHeight);
    }



    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 1000000;
        this.titleLabelY = 1000000;

        this.powerButton = new ToggleImageButton(
                this.leftPos + 11,
                this.topPos + 16,
                17,
                18,
                176,
                0,
                18,
                TEXTURE,
                (button) -> {
                    PacketHandler.sendToServer( new AFUTogglePacket(this.menu.getBlockEntity().getBlockPos()));
                }
        );
        this.addRenderableWidget(this.powerButton);
    }

    @Override
    public void render(GuiGraphics p_283479_, int p_283661_, int p_281248_, float p_281886_) {
        renderBackground(p_283479_);
        super.render(p_283479_, p_283661_, p_281248_, p_281886_);
        renderTooltip(p_283479_, p_283661_, p_281248_);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.powerButton != null) {
            this.powerButton.setToggled(this.menu.isActive());
        }
    }


}
