package com.chinaex123.vss_villager_trades.mixin;

import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Villager.class)
public interface VillagerAccessorMixin {

    @Invoker("setUnhappy")
    void invokeSetUnhappy();
}