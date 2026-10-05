package com.chinaex123.vss_villager_trades.client.menu;

import com.chinaex123.vss_villager_trades.network.VSSBalancePacket;
import com.chinaex123.vss_villager_trades.network.VillagerOffersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public final class MenuNetworkSync {
    private MenuNetworkSync() {}

    public static void sendOffersAndBalance(ServerPlayer player, List<MerchantOffer> offers, int balance) {
        PacketDistributor.sendToPlayer(player, new VillagerOffersPacket(offers, balance));
        PacketDistributor.sendToPlayer(player, new VSSBalancePacket(balance));
    }

    public static void sendBalance(ServerPlayer player, int balance) {
        PacketDistributor.sendToPlayer(player, new VSSBalancePacket(balance));
    }
}