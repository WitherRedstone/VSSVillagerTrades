package com.chinaex123.vss_villager_trades.client.gui;

import com.chinaex123.vss_villager_trades.VSSVillagerTrades;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 自定义刷新交易按钮，使用自定义材质渲染。
 * <p>
 * 两张 16×16 材质：
 * - refresh.png       激活可用状态（未锁交易）
 * - refresh_click.png 禁用状态（已锁交易）
 */
public class RefreshButton extends Button {

    /** 正常可用状态材质 */
    private static final ResourceLocation NORMAL = VSSVillagerTrades.id("textures/gui/refresh.png");
    /** 悬停/点击状态材质 */
    private static final ResourceLocation HOVERED = VSSVillagerTrades.id("textures/gui/refresh_click.png");
    /** 禁用不可用状态材质 */
    private static final ResourceLocation UNAVAILABLE = VSSVillagerTrades.id("textures/gui/refresh_unavailable.png");

    public RefreshButton(int x, int y, int width, int height, OnPress onPress) {
        super(Button.builder(Component.empty(), onPress).bounds(x, y, width, height));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture;
        if (!this.active) {
            texture = UNAVAILABLE;
        } else if (this.isHovered()) {
            texture = HOVERED;
        } else {
            texture = NORMAL;
        }

        guiGraphics.blit(texture,
                this.getX(), this.getY(),
                0, 0,
                this.width, this.height,
                16, 16);
    }
}