package com.chinaex123.vss_villager_trades.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * 村民核心逻辑 Mixin。
 * <p>
 * 集中处理与村民交易相关的多项改写：
 * 交易锁定（职业变更后保留已使用交易）、按确定性规则生成各等级交易、
 * 客户端显示被锁定的职业数据，以及每日补货。
 */
@Mixin(Villager.class)
public abstract class VillagerMixin {

    /** 每个等级挑选的交易数量 */
    @Unique
    private static final int TRADES_PER_LEVEL = 2;

    /** 持久化数据中记录上次检查游戏日的键名 */
    @Unique
    private static final String VSS_LAST_CHECK_DAY = "vss_last_check_day";

    /** 同步到客户端的被锁定职业名称 */
    @Unique
    private static final EntityDataAccessor<String> VSS_LOCKED_PROF =
            SynchedEntityData.defineId(Villager.class, EntityDataSerializers.STRING);

    /** 在职业变更过程中临时保存被锁定的交易列表 */
    @Unique
    private final ThreadLocal<MerchantOffers> vss$lockedOffers = new ThreadLocal<>();

    /**
     * 注册同步数据字段。
     * <p>
     * 追加注册用于存储被锁定职业名称的字符串字段。
     *
     * @param builder 同步数据构建器
     * @param ci      回调信息
     */
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void vss$registerEntityData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(VSS_LOCKED_PROF, "");
    }

    /**
     * 客户端读取村民数据时应用被锁定的职业。
     * <p>
     * 仅在客户端生效：若同步的被锁定职业名称非空，
     * 且当前职业为无职业或傻子，则以被锁定职业替换返回值，
     * 使客户端显示正确的职业外观。
     *
     * @param cir 回调信息，用于读取与设置返回值
     */
    @Inject(method = "getVillagerData", at = @At("RETURN"), cancellable = true)
    private void vss$overrideVillagerDataForClient(CallbackInfoReturnable<VillagerData> cir) {
        Villager self = (Villager) (Object) this;
        if (!self.level().isClientSide) return;

        String lockedProfName = self.getEntityData().get(VSS_LOCKED_PROF);
        if (lockedProfName == null || lockedProfName.isEmpty()) return;

        VillagerData original = cir.getReturnValue();
        if (original.getProfession() == VillagerProfession.NONE
                || original.getProfession() == VillagerProfession.NITWIT) {
            ResourceLocation profId = ResourceLocation.parse(lockedProfName);
            VillagerProfession lockedProf = BuiltInRegistries.VILLAGER_PROFESSION.get(profId);
            if (lockedProf != null) {
                VillagerData overridden = new VillagerData(original.getType(), lockedProf, original.getLevel());
                cir.setReturnValue(overridden);
            }
        }
    }

    /**
     * 在村民数据变更前记录需要锁定的交易。
     * <p>
     * 职业发生变化且原交易列表中存在已使用的交易时，
     * 将被锁定的交易暂存到线程本地变量，并同步旧职业名称到客户端；
     * 否则不做处理。
     *
     * @param newData 新的村民数据
     * @param ci      回调信息
     */
    @Inject(method = "setVillagerData", at = @At("HEAD"))
    private void vss$lockVillagerDataHead(VillagerData newData, CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        VillagerData oldData = self.getVillagerData();

        if (oldData.getProfession().equals(newData.getProfession())) return;

        MerchantOffers offers = ((AbstractVillagerAccessorMixin) self).getVss$Offers();

        if (offers == null || offers.isEmpty()) return;

        for (MerchantOffer o : offers) {
            if (o.getUses() > 0) {
                vss$lockedOffers.set(offers);
                VillagerProfession oldProf = oldData.getProfession();
                if (oldProf != VillagerProfession.NONE && oldProf != VillagerProfession.NITWIT) {
                    ResourceLocation profId = BuiltInRegistries.VILLAGER_PROFESSION.getKey(oldProf);
                    if (profId != null) {
                        self.getEntityData().set(VSS_LOCKED_PROF, profId.toString());
                    }
                }
                return;
            }
        }
    }

    /**
     * 在村民数据变更后恢复被锁定的交易。
     * <p>
     * 若线程本地变量中存在被锁定的交易列表，
     * 则将其重新写回村民的交易列表并清理暂存。
     *
     * @param newData 新的村民数据
     * @param ci      回调信息
     */
    @Inject(method = "setVillagerData", at = @At("RETURN"))
    private void vss$lockVillagerDataReturn(VillagerData newData, CallbackInfo ci) {
        MerchantOffers lockedOffers = vss$lockedOffers.get();
        if (lockedOffers == null) return;

        Villager self = (Villager) (Object) this;
        ((AbstractVillagerAccessorMixin) self).setVss$Offers(lockedOffers);
        vss$lockedOffers.remove();
    }

    /**
     * 在交易更新头部注入，按规则重建交易列表。
     * <p>
     * 若现有交易中存在已使用的交易，则视为已锁定，直接取消原逻辑；
     * 职业为无职业或傻子、或职业无对应交易表时同样取消。
     * 否则清空交易列表，并兼容实验性交易重平衡特性后，
     * 对 1 至 5 级分别随机挑选固定数量的交易项加入列表。
     * 最后取消原方法执行。
     *
     * @param ci 回调信息，用于取消原方法执行
     */
    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void vss$updateTradesAllLevels(CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        VillagerData data = self.getVillagerData();
        VillagerProfession currentProf = data.getProfession();
        int currentLevel = data.getLevel();

        MerchantOffers existing = ((AbstractVillagerAccessorMixin) self).getVss$Offers();
        // 已存在使用过的交易视为已锁定，不再重建
        if (existing != null && !existing.isEmpty()) {
            for (MerchantOffer o : existing) {
                if (o.getUses() > 0) {
                    ci.cancel();
                    return;
                }
            }
        }

        Int2ObjectMap<VillagerTrades.ItemListing[]> trades;
        if (self.level().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)) {
            Int2ObjectMap<VillagerTrades.ItemListing[]> experimental =
                    VillagerTrades.EXPERIMENTAL_TRADES.get(currentProf);
            trades = experimental != null
                    ? experimental
                    : VillagerTrades.TRADES.get(currentProf);
        } else {
            trades = VillagerTrades.TRADES.get(currentProf);
        }

        MerchantOffers offers = ((AbstractVillagerAccessorMixin) self).getVss$Offers();
        if (offers == null) {
            offers = new MerchantOffers();
            ((AbstractVillagerAccessorMixin) self).setVss$Offers(offers);
        }
        offers.clear();

        // 无职业、傻子或没有交易表时不生成交易
        if (currentProf == VillagerProfession.NONE || currentProf == VillagerProfession.NITWIT) {
            ci.cancel();
            return;
        }

        if (trades == null || trades.isEmpty()) {
            ci.cancel();
            return;
        }

        RandomSource random = self.getRandom();

        for (int level = 1; level <= 5; level++) {
            VillagerTrades.ItemListing[] listings = trades.get(level);
            if (listings == null || listings.length == 0) continue;

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