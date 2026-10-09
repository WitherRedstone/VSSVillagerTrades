package com.chinaex123.vss_villager_trades.mixin;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * AbstractVillager 访问器 Mixin。
 * <p>
 * offers 字段定义在 AbstractVillager 而非 Villager 中，
 * 因此需要单独一个接口来 Accessor。
 */
@Mixin(AbstractVillager.class)
public interface AbstractVillagerAccessorMixin {

    /** 直接读取 offers 字段。 */
    @Accessor("offers")
    MerchantOffers getVss$Offers();

    /** 直接写入 offers 字段。 */
    @Accessor("offers")
    void setVss$Offers(MerchantOffers offers);
}