package com.chinaex123.vss_villager_trades.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 村民交易更新 Mixin。
 * <p>
 * 替换原版村民交易更新逻辑，为村民添加其职业在 1 至 5 级下的交易项：
 * 每级依据村民 UUID 派生的确定性种子随机挑选固定数量的交易，
 * 并兼容实验性交易重平衡特性。最后取消原方法执行。
 */
@Mixin(Villager.class)
public abstract class VillagerUpdateTradesMixin {

    /** 每个等级挑选的交易数量 */
    @Unique
    private static final int TRADES_PER_LEVEL = 2;

    /**
     * 在交易更新头部注入，为各等级添加随机挑选的交易项。
     * <p>
     * 依据是否启用交易重平衡特性选择交易表；
     * 职业无对应交易表时放行原逻辑。
     * 以村民 UUID 派生出确定性种子，对 1 至 5 级分别创建独立随机源，
     * 每级从候选交易中随机挑选固定数量构建交易项并加入村民交易列表，
     * 单个交易项构建失败时忽略该条目。最后取消原方法执行。
     *
     * @param ci 回调信息，用于取消原方法执行
     */
    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void vss$updateTradesAllLevels(CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        VillagerData data = self.getVillagerData();

        // 实验性交易重平衡支持
        Int2ObjectMap<VillagerTrades.ItemListing[]> trades;
        if (self.level().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)) {
            Int2ObjectMap<VillagerTrades.ItemListing[]> experimental =
                    VillagerTrades.EXPERIMENTAL_TRADES.get(data.getProfession());
            trades = experimental != null
                    ? experimental
                    : VillagerTrades.TRADES.get(data.getProfession());
        } else {
            trades = VillagerTrades.TRADES.get(data.getProfession());
        }

        if (trades == null || trades.isEmpty()) return;

        MerchantOffers offers = self.getOffers();

        // 每个村民的确定性种子
        long villagerSeed = self.getUUID().getMostSignificantBits()
                ^ self.getUUID().getLeastSignificantBits();

        for (int level = 1; level <= 5; level++) {
            VillagerTrades.ItemListing[] listings = trades.get(level);
            if (listings == null || listings.length == 0) continue;

            // 每级独立种子
            RandomSource random = RandomSource.create(villagerSeed + level);

            List<VillagerTrades.ItemListing> candidates = new ArrayList<>(List.of(listings));
            int picked = 0;
            while (picked < TRADES_PER_LEVEL && !candidates.isEmpty()) {
                VillagerTrades.ItemListing listing =
                        candidates.remove(random.nextInt(candidates.size()));
                try {
                    MerchantOffer offer = listing.getOffer(self, random);
                    if (offer != null) {
                        offers.add(offer);
                        picked++;
                    }
                } catch (Exception ignored) {}
            }
        }

        ci.cancel();
    }
}