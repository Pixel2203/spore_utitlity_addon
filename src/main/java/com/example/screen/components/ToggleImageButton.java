package com.example.screen.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ToggleImageButton extends AbstractWidget {
    @FunctionalInterface
    public interface OnPress {
        void onPress(ToggleImageButton button);
    }

    private final ResourceLocation texture;
    private final int xTexStart;
    private final int yTexStart;
    private final int yDiffTex;
    private final OnPress onPress;
    private boolean toggled = false;

    public ToggleImageButton(int x, int y, int width, int height,
                             int xTexStart, int yTexStart, int yDiffTex,
                             ResourceLocation texture, OnPress onPress) {
        super(x, y, width, height, Component.empty());
        this.texture = texture;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffTex = yDiffTex;
        this.onPress = onPress;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.onPress.onPress(this);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int v = this.yTexStart;
        if (this.toggled) {
            v += this.yDiffTex;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.texture);
        guiGraphics.blit(this.texture, this.getX(), this.getY(), this.xTexStart, v, this.width, this.height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public boolean isToggled() {
        return toggled;
    }
}