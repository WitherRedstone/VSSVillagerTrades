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
//@Mixin(WanderingTrader.class)
//public abstract class WanderingTraderUpdateTradesMixin {
//
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