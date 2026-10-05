package com.chinaex123.vss_villager_trades.client.gui;

import com.chinaex123.vss_villager_trades.event.VillagerShopManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * 交易条目渲染工具。
 * <p>
 * 依据交易类型在指定位置绘制交易行内容：
 * 出售交易绘制所需物品、箭头与可得货币，购买交易绘制所需物品、价格、箭头与产出物品。
 */
public final class TradeOfferRenderer {

    /**
     * 私有构造函数，防止实例化。
     * <p>
     * 该类只提供静态方法，不需要实例。
     */
    private TradeOfferRenderer() {}

    /**
     * 绘制单行交易条目。
     * <p>
     * 依据交易是否为出售交易分别调用对应的绘制逻辑。
     *
     * @param g     图形上下文
     * @param font  字体
     * @param offer 交易项
     * @param x     绘制起始 X 坐标
     * @param y     绘制起始 Y 坐标
     */
    public static void renderRow(GuiGraphics g, net.minecraft.client.gui.Font font, MerchantOffer offer, int x, int y) {
        if (VillagerShopManager.isSellOffer(offer)) {
            renderSell(g, font, offer, x, y);
        } else {
            renderBuy(g, font, offer, x, y);
        }
    }

    /**
     * 绘制出售交易行。
     * <p>
     * 依次绘制所需物品及其数量装饰、箭头与可得货币数量。
     *
     * @param g     图形上下文
     * @param font  字体
     * @param offer 交易项
     * @param x     绘制起始 X 坐标
     * @param y     绘制起始 Y 坐标
     */
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

    /**
     * 绘制购买交易行。
     * <p>
     * 依次绘制所需物品及其数量装饰、价格（无效时显示问号）、箭头与产出物品及其数量装饰。
     *
     * @param g     图形上下文
     * @param font  字体
     * @param offer 交易项
     * @param x     绘制起始 X 坐标
     * @param y     绘制起始 Y 坐标
     */
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