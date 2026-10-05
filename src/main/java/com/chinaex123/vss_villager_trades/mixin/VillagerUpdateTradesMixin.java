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

@Mixin(Villager.class)
public abstract class VillagerUpdateTradesMixin {

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void vss$updateTradesAllLevels(CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        VillagerData data = self.getVillagerData();

        Int2ObjectMap<VillagerTrades.ItemListing[]> trades =
                VillagerTrades.TRADES.get(data.getProfession());
        if (trades == null || trades.isEmpty()) return;

        MerchantOffers offers = self.getOffers();

        // ★ 遍历 1~5 级，全部加进去
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