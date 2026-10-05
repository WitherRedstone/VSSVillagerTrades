//package com.chinaex123.vss_villager_trades.mixin;
//
//import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
//import net.minecraft.world.entity.npc.VillagerTrades;
//import net.minecraft.world.entity.npc.WanderingTrader;
//import net.minecraft.world.item.trading.MerchantOffer;
//import net.minecraft.world.item.trading.MerchantOffers;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
///**
// * 流浪商人交易更新 Mixin。
// * <p>
// * 替换原版流浪商人交易更新逻辑，一次性添加流浪商人交易表中的全部交易项，
// * 并取消原方法执行。单个交易项构建失败时忽略该条目，不影响其余交易。
// */
//@Mixin(WanderingTrader.class)
//public abstract class WanderingTraderUpdateTradesMixin {
//
//    /**
//     * 在交易更新头部注入，添加全部流浪商人交易项。
//     * <p>
//     * 遍历流浪商人交易表中的所有条目，逐条构建交易项并加入交易列表，
//     * 最后取消原方法执行。
//     *
//     * @param ci 回调信息，用于取消原方法执行
//     */
//    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
//    private void vss$updateTradesAll(CallbackInfo ci) {
//        WanderingTrader self = (WanderingTrader) (Object) this;
//        MerchantOffers offers = self.getOffers();
//
//        Int2ObjectMap<VillagerTrades.ItemListing[]> trades =
//                VillagerTrades.WANDERING_TRADER_TRADES;
//        for (VillagerTrades.ItemListing[] listings : trades.values()) {
//            for (VillagerTrades.ItemListing listing : listings) {
//                try {
//                    MerchantOffer offer = listing.getOffer(self, self.getRandom());
//                    if (offer != null) offers.add(offer);
//                } catch (Exception ignored) {}
//            }
//        }
//
//        ci.cancel();
//    }
//}