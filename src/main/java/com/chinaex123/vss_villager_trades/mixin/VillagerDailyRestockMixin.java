package com.chinaex123.vss_villager_trades.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 村民每日补货 Mixin。
 * <p>
 * 在村民的服务端 AI 刻末尾检查是否跨入新的一天：
 * 若已跨天且存在已售罄的交易，则触发一次补货。
 * 通过持久化数据记录上次检查的游戏日，避免重复触发。
 */
@Mixin(Villager.class)
public abstract class VillagerDailyRestockMixin {

    /** 持久化数据中记录上次检查游戏日的键名 */
    @Unique
    private static final String VSS_LAST_CHECK_DAY = "vss_last_check_day";

    /**
     * 在村民服务端 AI 刻末尾注入，处理每日补货。
     * <p>
     * 仅在服务端生效，且每 20 刻检查一次：
     * 首次检查时仅记录当前游戏日，不执行补货；
     * 之后若当前游戏日晚于上次记录且存在已售罄交易，则触发补货并更新记录。
     *
     * @param ci 回调信息
     */
    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void vss$dailyRestock(CallbackInfo ci) {
        Villager self = (Villager) (Object) this;

        if (self.level().isClientSide) return;
        if (self.tickCount % 20 != 0) return;

        long currentDay = self.level().getDayTime() / 24000L;

        CompoundTag data = self.getPersistentData();
        long lastDay = data.getLong(VSS_LAST_CHECK_DAY);

        // 首次记录，不补货
        if (lastDay == 0L) {
            data.putLong(VSS_LAST_CHECK_DAY, currentDay);
            return;
        }

        if (currentDay <= lastDay) return;

        data.putLong(VSS_LAST_CHECK_DAY, currentDay);

        if (vss$hasAnyOfferUsedUp(self)) {
            self.restock();
        }
    }

    /**
     * 判断村民是否存在已售罄的交易。
     *
     * @param villager 目标村民
     * @return 存在已售罄交易返回 true
     */
    @Unique
    private static boolean vss$hasAnyOfferUsedUp(Villager villager) {
        for (MerchantOffer offer : villager.getOffers()) {
            if (offer.isOutOfStock()) {
                return true;
            }
        }
        return false;
    }
}