package com.chinaex123.vss_villager_trades.client.menu;

import com.chinaex123.vss_villager_trades.network.VSSBalancePacket;
import com.chinaex123.vss_villager_trades.network.VillagerOffersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * 菜单网络同步工具。
 * <p>
 * 负责将服务端的交易列表与玩家 VSS 余额同步到指定客户端，
 * 供村民交易菜单刷新显示。
 */
public final class MenuNetworkSync {

    /**
     * 私有构造函数，防止实例化。
     * <p>
     * 该类只提供静态方法，不需要实例。
     */
    private MenuNetworkSync() {}

    /**
     * 同步交易列表与余额到客户端。
     * <p>
     * 先发送交易列表与余额的组合数据包，再单独发送一次余额数据包。
     *
     * @param player  目标玩家
     * @param offers  交易列表
     * @param balance VSS 余额
     */
    public static void sendOffersAndBalance(ServerPlayer player, List<MerchantOffer> offers, int balance) {
        PacketDistributor.sendToPlayer(player, new VillagerOffersPacket(offers, balance));
        PacketDistributor.sendToPlayer(player, new VSSBalancePacket(balance));
    }

    /**
     * 同步余额到客户端。
     *
     * @param player  目标玩家
     * @param balance VSS 余额
     */
    public static void sendBalance(ServerPlayer player, int balance) {
        PacketDistributor.sendToPlayer(player, new VSSBalancePacket(balance));
    }
}