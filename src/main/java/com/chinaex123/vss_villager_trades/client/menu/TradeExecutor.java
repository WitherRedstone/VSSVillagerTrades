package com.chinaex123.vss_villager_trades.client.menu;

import com.chinaex123.vss_villager_trades.event.VillagerShopManager;
import com.chinaex123.vss_villager_trades.util.ViScriptShopUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * 交易执行器。
 * <p>
 * 在服务端按序号与次数执行村民交易：
 * 出售交易消耗玩家物品并增加 VSS货币 余额，
 * 购买交易扣除 VSS货币 余额与额外物品并将结果放入输出容器。
 * 交易数量受库存限制，无法继续时提前结束。
 */
public final class TradeExecutor {

    /**
     * 私有构造函数，防止实例化。
     * <p>
     * 该类只提供静态方法，不需要实例。
     */
    private TradeExecutor() {}

    /**
     * 交易执行结果。
     *
     * @param didAny     是否成功执行了至少一次交易
     * @param newBalance 执行后的 VSS货币 余额
     */
    public record Result(boolean didAny, int newBalance) {}

    /**
     * 执行交易。
     * <p>
     * 校验交易列表与序号有效性、交易是否已售罄，
     * 随后按次数循环执行交易：根据交易类型分别走出售或购买流程，
     * 任一次失败即提前结束；每次成功后增加交易使用次数。
     *
     * @param serverOffers 服务端交易列表
     * @param offerIndex   交易在列表中的索引
     * @param count        请求执行的交易次数
     * @param player       执行交易的玩家
     * @param balance      执行前的 VSS货币 余额
     * @param output       交易输出容器
     * @return 交易执行结果
     */
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

    /**
     * 执行出售交易。
     * <p>
     * 需要玩家持有对应的非货币代价物品，扣除后按配置增加 VSS货币 余额。
     *
     * @param offer   交易项
     * @param player  执行交易的玩家
     * @param balance 当前 VSS货币 余额
     * @return 新的 VSS货币 余额，失败返回 null
     */
    private static Integer trySell(MerchantOffer offer, ServerPlayer player, int balance) {
        ItemStack need = VillagerShopManager.getNonEmeraldCost(offer);
        if (need.isEmpty()) return null;
        if (!VillagerShopManager.removeFromPlayer(player, need)) return null;

        int reward = VillagerShopManager.emeraldRewardFromSell(offer);
        ViScriptShopUtil.addMoney(player, reward);
        return balance + reward;
    }

    /**
     * 执行购买交易。
     * <p>
     * 需要余额足够且玩家持有额外代价物品，
     * 扣除后按配置扣减 VSS货币 并将交易结果放入输出容器。
     *
     * @param offer   交易项
     * @param player  执行交易的玩家
     * @param balance 当前 VSS货币 余额
     * @param output  交易输出容器
     * @return 新的 VSS货币 余额，失败返回 null
     */
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