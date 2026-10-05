package com.chinaex123.vss_villager_trades.client.gui;

import com.chinaex123.vss_villager_trades.util.TradeRules;
import com.chinaex123.vss_villager_trades.event.VillagerShopManager;
import com.chinaex123.vss_villager_trades.util.NumberFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.ArrayList;
import java.util.List;

/**
 * 交易提示渲染工具。
 * <p>
 * 依据交易类型构建悬停提示信息：
 * 出售交易显示所需物品与可得货币，购买交易显示产出物品、所需物品与价格；
 * 均会附加库存信息与操作提示。
 */
public final class TradeTooltipRenderer {

    /**
     * 私有构造函数，防止实例化。
     */
    private TradeTooltipRenderer() {}

    /**
     * 构建交易的提示信息列表。
     *
     * @param offer 交易项
     * @return 提示信息组件列表
     */
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

    /**
     * 构建出售交易的提示内容。
     * <p>
     * 显示玩家需提供的物品（含附魔）及其数量，以及出售可获得的货币数量。
     *
     * @param offer 交易项
     * @param tips  提示信息收集列表
     */
    private static void buildSellTips(MerchantOffer offer, List<Component> tips) {
        ItemStack need = VillagerShopManager.getNonEmeraldCost(offer);
        tips.add(Component.translatable("tooltip.vss_villager_trades.sell",
                nameWithEnchantments(need), need.getCount()));
        tips.add(Component.translatable("tooltip.vss_villager_trades.sell_reward",
                priceText(VillagerShopManager.emeraldRewardFromSell(offer))));
    }

    /**
     * 构建购买交易的提示内容。
     * <p>
     * 显示产出物品（含附魔）、所需的额外代价物品（含附魔，若有）与购买价格；
     * 价格无效时显示无价格提示。
     *
     * @param offer 交易项
     * @param tips  提示信息收集列表
     */
    private static void buildBuyTips(MerchantOffer offer, List<Component> tips) {
        ItemStack result = offer.getResult();
        tips.add(nameWithEnchantments(result));

        ItemStack need = VillagerShopManager.getNonEmeraldCost(offer);
        if (!need.isEmpty()) {
            tips.add(Component.translatable("tooltip.vss_villager_trades.need",
                    nameWithEnchantments(need), need.getCount()));
        }

        int vss = VillagerShopManager.emeraldCostToVSS(offer);
        if (vss >= 0) {
            tips.add(Component.translatable("tooltip.vss_villager_trades.buy_price",
                    priceText(vss)));
        } else {
            tips.add(Component.translatable("tooltip.vss_villager_trades.no_price"));
        }
    }

    /**
     * 附加库存信息行。
     * <p>
     * 无限交易模组加载时显示无限提示；
     * 交易售罄时以红色显示售罄提示，
     * 否则以灰色显示剩余次数与总次数。
     *
     * @param offer 交易项
     * @param tips  提示信息收集列表
     */
    private static void appendStockLine(MerchantOffer offer, List<Component> tips) {
        if (TradeRules.isEnabled()) {
            tips.add(Component.translatable("tooltip.vss_villager_trades.infinite")
                    .withStyle(ChatFormatting.GREEN));
            return;
        }

        if (offer.isOutOfStock()) {
            tips.add(Component.translatable("tooltip.vss_villager_trades.out_of_stock")
                    .withStyle(ChatFormatting.RED));
        } else {
            tips.add(Component.translatable("tooltip.vss_villager_trades.uses",
                            offer.getMaxUses() - offer.getUses(), offer.getMaxUses())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    /**
     * 返回物品名 + 附魔后缀的组件。
     * <p>
     * 有附魔时返回"物品名[附魔1, 附魔2]"格式，附魔部分用浅紫色显示；
     * 无附魔时只返回物品名。
     *
     * @param stack 物品
     * @return 显示名组件
     */
    private static Component nameWithEnchantments(ItemStack stack) {
        Component base = stack.getHoverName().copy();

        ItemEnchantments ench = getEnchantments(stack);
        if (ench == null || ench.isEmpty()) return base;

        // 拼 [附魔1, 附魔2]
        Component enchPart = Component.literal("[")
                .withStyle(ChatFormatting.LIGHT_PURPLE);

        boolean first = true;
        for (var entry : ench.entrySet()) {
            if (!first) {
                enchPart = enchPart.copy()
                        .append(Component.literal(", ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            enchPart = enchPart.copy()
                    .append(Enchantment.getFullname(entry.getKey(), entry.getIntValue())
                            .copy()
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
            first = false;
        }

        enchPart = enchPart.copy()
                .append(Component.literal("]")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));

        return base.copy().append(enchPart);
    }

    /**
     * 拿物品的附魔列表。
     * <p>
     * 附魔书用 STORED_ENCHANTMENTS，普通附魔物品用 ENCHANTMENTS。
     *
     * @param stack 物品
     * @return 附魔列表，没有附魔时返回 null
     */
    private static ItemEnchantments getEnchantments(ItemStack stack) {
        if (stack.isEmpty()) return null;

        ItemEnchantments ench = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (ench != null && !ench.isEmpty()) return ench;

        ench = stack.get(DataComponents.ENCHANTMENTS);
        if (ench != null && !ench.isEmpty()) return ench;

        return null;
    }

    /**
     * 构建货币价格文本。
     *
     * @param amount 货币数量
     * @return 价格文本组件
     */
    public static Component priceText(int amount) {
        return Component.translatable("tooltip.vss_villager_trades.symbol.currency")
                .append(NumberFormatter.format(amount));
    }
}