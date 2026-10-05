package com.chinaex123.vss_villager_trades.mixin;

import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 村民访问器 Mixin。
 * <p>
 * 通过 Mixin 的 Invoker 机制暴露村民的私有方法，
 * 供本模组在需要时将村民标记为不满状态。
 */
@Mixin(Villager.class)
public interface VillagerAccessorMixin {

    /**
     * 调用村民的 setUnhappy 方法，将村民标记为不满状态。
     */
    @Invoker("setUnhappy")
    void invokeSetUnhappy();
}