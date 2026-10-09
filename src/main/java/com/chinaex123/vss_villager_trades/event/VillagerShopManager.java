package com.chinaex123.vss_villager_trades.event;

import com.chinaex123.vss_villager_trades.api.VSSVillagerCurrency;
import com.chinaex123.vss_villager_trades.client.menu.VillagerTradesMenu;
import com.chinaex123.vss_villager_trades.network.VillagerOffersPacket;
import com.chinaex123.vss_villager_trades.util.ViScriptShopUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

/**
 * 村民商店管理器。
 * <p>
 * 接管玩家与村民的右键交互，改为打开自定义的村民交易菜单，
 * 并在打开时同步交易列表与玩家 VSS货币 余额。
 * 同时提供交易代价计算与物品扣除等辅助方法。
 */
public final class VillagerShopManager {

    /**
     * 私有构造函数，防止实例化。
     * <p>
     * 该类只提供静态方法，不需要实例。
     */
    private VillagerShopManager() {}

    /**
     * 打开村民交易菜单。
     * <p>
     * 若为普通村民且可补货则先执行补货；随后读取交易列表与玩家余额，
     * 以村民名称作为标题打开菜单，并将交易列表与余额同步到客户端。
     *
     * @param player   服务器玩家
     * @param villager 目标村民
     */
    public static void openVillagerTrades(ServerPlayer player, AbstractVillager villager) {
        if (villager instanceof Villager v) {
            if (v.shouldRestock()) {
                v.restock();
            }
        }

        MerchantOffers offers = getOffersFromVillager(villager);
        int balance = ViScriptShopUtil.getMoney(player);
        Component title = getVillagerTitle(villager);

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new VillagerTradesMenu(id, inv, p, villager, offers, balance),
                title
        ));

        PacketDistributor.sendToPlayer(player,
                new VillagerOffersPacket(new ArrayList<>(offers), balance));
    }

    /**
     * 获取村民的交易列表。
     * <p>
     * 村民为空时返回空交易列表。
     *
     * @param villager 目标村民
     * @return 村民的交易列表
     */
    public static MerchantOffers getOffersFromVillager(AbstractVillager villager) {
        if (villager == null) return new MerchantOffers();

        return villager.getOffers();
    }

    /**
     * 判断该交易是否为出售交易。
     *
     * @param offer 交易项
     * @return 是出售交易返回 true
     */
    public static boolean isSellOffer(MerchantOffer offer) {
        return VSSVillagerCurrency.isSellOffer(offer);
    }

    /**
     * 计算购买交易所需的 VSS货币 数量。
     *
     * @param offer 交易项
     * @return 购买所需 VSS货币 数量
     */
    public static int emeraldCostToVSS(MerchantOffer offer) {
        return VSSVillagerCurrency.buyCostInVss(offer);
    }

    /**
     * 计算出售交易可获得的 VSS货币 数量。
     *
     * @param offer 交易项
     * @return 出售可得 VSS货币 数量
     */
    public static int emeraldRewardFromSell(MerchantOffer offer) {
        return VSSVillagerCurrency.sellRewardInVss(offer);
    }

    /**
     * 获取交易中非绿宝石部分的代价物品。
     *
     * @param offer 交易项
     * @return 非货币部分的代价物品堆
     */
    public static ItemStack getNonEmeraldCost(MerchantOffer offer) {
        return VSSVillagerCurrency.getNonCurrencyCost(offer);
    }

    /**
     * 从玩家物品栏中扣除指定物品。
     * <p>
     * 先统计玩家是否拥有足够数量的目标物品，不足时返回 false；
     * 足够时从各槽位依次扣除，扣除完成返回 true。
     *
     * @param player 服务器玩家
     * @param target 待扣除的物品堆
     * @return 扣除成功返回 true
     */
    public static boolean removeFromPlayer(ServerPlayer player, ItemStack target) {
        if (target.isEmpty()) return true;

        int need = target.getCount();
        int have = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, target)) {
                have += s.getCount();
            }
        }
        if (have < need) return false;

        int remaining = need;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, target)) {
                int take = Math.min(s.getCount(), remaining);
                s.shrink(take);
                remaining -= take;
            }
        }
        return true;
    }

    /**
     * 获取村民界面的显示标题。
     * <p>
     * 普通村民按其职业返回对应名称（无职业时返回通用村民名称），
     * 流浪商人返回流浪商人名称，其他情况返回实体的显示名称。
     *
     * @param villager 目标村民
     * @return 村民界面的标题组件
     */
    private static Component getVillagerTitle(AbstractVillager villager) {
        if (villager instanceof Villager v) {
            VillagerProfession profession = v.getVillagerData().getProfession();
            if (profession == VillagerProfession.NONE) {
                return Component.translatable("entity.minecraft.villager");
            }
            return Component.translatable("entity.minecraft.villager." + profession.name());
        }
        if (villager instanceof WanderingTrader) {
            return Component.translatable("entity.minecraft.wandering_trader");
        }
        return villager.getDisplayName();
    }
}