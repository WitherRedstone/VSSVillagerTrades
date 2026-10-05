package com.chinaex123.vss_villager_trades.util;

import com.chinaex123.vss_villager_trades.config.VVTConfig;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * 交易规则工具。
 * <p>
 * 依据配置决定村民交易是否受库存限制：
 * 启用无限交易时忽略库存状态，且交易成功后不增加使用次数；
 * 未启用时按原版逻辑判断库存并在交易成功后累加使用次数。
 */
public final class TradeRules {

    /**
     * 私有构造函数，防止实例化。
     * <p>
     * 该类只提供静态方法，不需要实例。
     */
    private TradeRules() {}

    /**
     * 判断是否启用无限交易。
     *
     * @return 启用返回 true
     */
    public static boolean isEnabled() {
        return VVTConfig.INFINITE_TRADES.get();
    }

    /**
     * 判断指定交易当前是否可执行。
     * <p>
     * 启用无限交易时始终可执行；
     * 否则要求交易未售罄。
     *
     * @param offer 交易项
     * @return 可执行返回 true
     */
    public static boolean isSoldOut(MerchantOffer offer) {
        if (isEnabled()) return false;
        return offer.isOutOfStock();
    }

    /**
     * 交易成功后的处理。
     * <p>
     * 启用无限交易时不累加使用次数；
     * 否则按原版逻辑增加一次使用次数。
     *
     * @param offer 交易项
     */
    public static void onTradeSuccess(MerchantOffer offer) {
        if (isEnabled()) return;
        offer.increaseUses();
    }
}