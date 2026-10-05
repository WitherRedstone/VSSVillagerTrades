package com.chinaex123.vss_villager_trades.client.gui;

import com.chinaex123.vss_villager_trades.event.VillagerShopManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public final class TradeOfferRenderer {

    private TradeOfferRenderer() {}

    public static void renderRow(GuiGraphics g, net.minecraft.client.gui.Font font, MerchantOffer offer, int x, int y) {
        if (VillagerShopManager.isSellOffer(offer)) {
            renderSell(g, font, offer, x, y);
        } else {
            renderBuy(g, font, offer, x, y);
        }
    }

    private static void renderSell(GuiGraphics g, net.minecraft.client.gui.Font font, MerchantOffer offer, int x, int y) {
        ItemStack need = VillagerShopManager.getNonEmeraldCost(offer);
        if (!need.isEmpty()) {
            g.renderItem(need, x + 4, y + 2);
            g.renderItemDecorations(font, need, x + 4, y + 2);
        }
        g.drawString(font, "→", x + 50, y + 6, 0xFFCCCCCC, false);

        int reward = VillagerShopManager.emeraldRewardFromSell(offer);
        g.drawString(font, TradeTooltipRenderer.priceText(reward), x + 63, y + 6, 0xFF55FF55, false);
    }

    private static void renderBuy(GuiGraphics g, net.minecraft.client.gui.Font font, MerchantOffer offer, int x, int y) {
        ItemStack need = VillagerShopManager.getNonEmeraldCost(offer);
        if (!need.isEmpty()) {
            g.renderItem(need, x + 4, y + 2);
            g.renderItemDecorations(font, need, x + 4, y + 2);
        }

        int cost = VillagerShopManager.emeraldCostToVSS(offer);
        if (cost >= 0) {
            g.drawString(font, TradeTooltipRenderer.priceText(cost), x + 22, y + 6, 0xFFFFAA00, false);
        } else {
            g.drawString(font, "?", x + 24, y + 6, 0xFF888888, false);
        }

        g.drawString(font, "→", x + 50, y + 6, 0xFFCCCCCC, false);
        g.renderItem(offer.getResult(), x + 62, y + 2);
        g.renderItemDecorations(font, offer.getResult(), x + 62, y + 2);
    }
}