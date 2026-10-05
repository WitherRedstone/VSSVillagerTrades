package com.chinaex123.vss_villager_trades.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 村民交易更新 Mixin。
 * <p>
 * 替换原版村民交易更新逻辑，为村民一次性添加其职业在
 * 1 至 5 级全部等级下的交易项，并取消原方法执行。
 * 单个交易项构建失败时忽略该条目，不影响其余交易。
 */
@Mixin(Villager.class)
public abstract class VillagerUpdateTradesMixin {

    /**
     * 在交易更新头部注入，添加全部等级的交易项。
     * <p>
     * 依据村民职业获取交易表；职业无对应交易表时放行原逻辑。
     * 依次遍历 1 至 5 级，逐条构建交易项并加入村民的交易列表，
     * 最后取消原方法执行。
     *
     * @param ci 回调信息，用于取消原方法执行
     */
    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void vss$updateTradesAllLevels(CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        VillagerData data = self.getVillagerData();

        Int2ObjectMap<VillagerTrades.ItemListing[]> trades =
                VillagerTrades.TRADES.get(data.getProfession());
        if (trades == null || trades.isEmpty()) return;

        MerchantOffers offers = self.getOffers();

        for (int level = 1; level <= 5; level++) {
            VillagerTrades.ItemListing[] listings = trades.get(level);
            if (listings == null) continue;
            for (VillagerTrades.ItemListing listing : listings) {
                try {
                    MerchantOffer offer = listing.getOffer(self, self.getRandom());
                    if (offer != null) offers.add(offer);
                } catch (Exception ignored) {}
            }
        }

        ci.cancel();   // 取消原方法
    }
}