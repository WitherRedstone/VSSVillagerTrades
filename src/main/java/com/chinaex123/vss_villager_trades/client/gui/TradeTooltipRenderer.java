package com.chinaex123.vss_villager_trades.client.gui;

import com.chinaex123.vss_villager_trades.event.VillagerShopManager;
import com.chinaex123.vss_villager_trades.util.NumberFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.ArrayList;
import java.util.List;

public final class TradeTooltipRenderer {

    private TradeTooltipRenderer() {}

    public static List<Component> build(MerchantOffer offer) {
        List<Component> tips = new ArrayList<>();

        if (VillagerShopManager.isSellOffer(offer)) {
            buildSellTips(offer, tips);
        } else {
            buildBuyTips(offer, tips);
        }

        appendStockLine(offer, tips);

        tips.add(Component.literal(""));
        tips.add(Component.translatable("tooltip.vss_villager_trades.click_to_trade")
                .withStyle(ChatFormatting.YELLOW));
        return tips;
    }

    private static void buildSellTips(MerchantOffer offer, List<Component> tips) {
        ItemStack need = VillagerShopManager.getNonEmeraldCost(offer);
        tips.add(Component.translatable("tooltip.vss_villager_trades.sell",
                need.getHoverName(), need.getCount()));
        tips.add(Component.translatable("tooltip.vss_villager_trades.sell_reward",
                priceText(VillagerShopManager.emeraldRewardFromSell(offer))));
    }

    private static void buildBuyTips(MerchantOffer offer, List<Component> tips) {
        tips.add(offer.getResult().getHoverName());

        ItemStack need = VillagerShopManager.getNonEmeraldCost(offer);
        if (!need.isEmpty()) {
            tips.add(Component.translatable("tooltip.vss_villager_trades.need",
                    need.getHoverName(), need.getCount()));
        }

        int vss = VillagerShopManager.emeraldCostToVSS(offer);
        if (vss >= 0) {
            tips.add(Component.translatable("tooltip.vss_villager_trades.buy_price",
                    priceText(vss)));
        } else {
            tips.add(Component.translatable("tooltip.vss_villager_trades.no_price"));
        }
    }

    private static void appendStockLine(MerchantOffer offer, List<Component> tips) {
        if (offer.isOutOfStock()) {
            tips.add(Component.translatable("tooltip.vss_villager_trades.out_of_stock")
                    .withStyle(ChatFormatting.RED));
        } else {
            tips.add(Component.translatable("tooltip.vss_villager_trades.uses", offer.getMaxUses() - offer.getUses(), offer.getMaxUses())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public static Component priceText(int amount) {
        return Component.translatable("tooltip.vss_villager_trades.symbol.currency")
                .append(NumberFormatter.format(amount));
    }
}