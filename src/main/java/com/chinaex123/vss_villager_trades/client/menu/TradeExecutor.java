package com.chinaex123.vss_villager_trades.client.menu;

import com.chinaex123.vss_villager_trades.event.VillagerShopManager;
import com.chinaex123.vss_villager_trades.util.ViScriptShopUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public final class TradeExecutor {

    private TradeExecutor() {}

    public record Result(boolean didAny, int newBalance) {}

    public static Result execute(MerchantOffers serverOffers, int offerIndex, int count, ServerPlayer player, int balance, TradeOutputContainer output) {

        if (serverOffers == null) return new Result(false, balance);
        if (offerIndex < 0 || offerIndex >= serverOffers.size()) return new Result(false, balance);

        MerchantOffer offer = serverOffers.get(offerIndex);
        if (offer.isOutOfStock()) return new Result(false, balance);

        boolean didAny = false;
        int currentBalance = balance;

        for (int n = 0; n < count; n++) {
            if (offer.isOutOfStock()) break;

            if (VillagerShopManager.isSellOffer(offer)) {
                Integer newBal = trySell(offer, player, currentBalance);
                if (newBal == null) break;
                currentBalance = newBal;
            } else {
                Integer newBal = tryBuy(offer, player, currentBalance, output);
                if (newBal == null) break;
                currentBalance = newBal;
            }

            offer.increaseUses();
            didAny = true;
        }

        return new Result(didAny, currentBalance);
    }

    /** 卖：物品 → VSS。返回新余额，失败返回 null。 */
    private static Integer trySell(MerchantOffer offer, ServerPlayer player, int balance) {
        ItemStack need = VillagerShopManager.getNonEmeraldCost(offer);
        if (need.isEmpty()) return null;
        if (!VillagerShopManager.removeFromPlayer(player, need)) return null;

        int reward = VillagerShopManager.emeraldRewardFromSell(offer);
        ViScriptShopUtil.addMoney(player, reward);
        return balance + reward;
    }

    /** 买：VSS → 物品。返回新余额，失败返回 null。 */
    private static Integer tryBuy(MerchantOffer offer, ServerPlayer player,
                                  int balance, TradeOutputContainer output) {
        int vssCost = VillagerShopManager.emeraldCostToVSS(offer);
        if (vssCost < 0) return null;
        if (balance < vssCost) return null;

        ItemStack extraCost = VillagerShopManager.getNonEmeraldCost(offer);
        if (!extraCost.isEmpty() && !VillagerShopManager.removeFromPlayer(player, extraCost)) {
            return null;
        }

        ViScriptShopUtil.removeMoney(player, vssCost);
        output.placeResult(offer.getResult().copy());
        return balance - vssCost;
    }
}