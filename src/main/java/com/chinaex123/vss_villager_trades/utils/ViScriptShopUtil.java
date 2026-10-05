package com.chinaex123.vss_villager_trades.utils;

import com.viscriptshop.util.ViScriptShopServerUtil;
import net.minecraft.server.level.ServerPlayer;

/**
 * VSS 货币工具类。
 * <p>
 * 对 ViScriptShop 的服务端货币接口进行封装，提供查询、增加、扣除与设置余额的静态方法，
 * 便于其他代码以统一入口操作玩家货币。
 */
public final class ViScriptShopUtil {

    /**
     * 私有构造函数，防止实例化。
     * <p>
     * 该类只提供静态方法，不需要实例。
     */
    private ViScriptShopUtil() {
    }

    /**
     * 获取玩家 VSS 余额。
     *
     * @param player 服务器玩家
     * @return 玩家当前余额
     */
    public static int getMoney(ServerPlayer player) {
        return (int) ViScriptShopServerUtil.getMoney(player);
    }

    /**
     * 增加玩家 VSS 货币。
     *
     * @param player 服务器玩家
     * @param amount 增加的金额
     */
    public static void addMoney(ServerPlayer player, int amount) {
        ViScriptShopServerUtil.addMoney(player, amount);
    }

    /**
     * 扣除玩家 VSS 货币。
     *
     * @param player 服务器玩家
     * @param amount 扣除的金额
     */
    public static void removeMoney(ServerPlayer player, int amount) {
        ViScriptShopServerUtil.removeMoney(player, amount);
    }

    /**
     * 设置玩家 VSS 余额。
     *
     * @param player 服务器玩家
     * @param amount 要设置的余额
     */
    public static void setMoney(ServerPlayer player, int amount) {
        ViScriptShopServerUtil.setMoney(player, amount);
    }
}