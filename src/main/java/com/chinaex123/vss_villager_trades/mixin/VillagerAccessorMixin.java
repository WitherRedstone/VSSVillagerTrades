package com.chinaex123.vss_villager_trades.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 村民访问器 Mixin。
 * <p>
 * 通过 Mixin 的 Invoker 与 Accessor 机制暴露村民的私有方法与字段，
 * 供本模组在需要时调用村民的补货更新、设置不满状态，
 * 以及访问村民数据同步字段。
 */
@Mixin(Villager.class)
public interface VillagerAccessorMixin {

    /**
     * 调用村民的 setUnhappy 方法，将村民标记为不满状态。
     */
    @Invoker("setUnhappy")
    void invokeSetUnhappy();

    /**
     * 调用村民的 updateTrades 方法，触发交易更新。
     */
    @Invoker("updateTrades")
    void invokeUpdateTrades();

    /**
     * 获取村民数据同步字段。
     *
     * @return 村民数据的实体数据访问器
     */
    @Accessor("DATA_VILLAGER_DATA")
    static EntityDataAccessor<VillagerData> getVss$DataVillagerData() {
        throw new UnsupportedOperationException();
    }
}