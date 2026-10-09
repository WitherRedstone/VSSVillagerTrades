package com.chinaex123.vss_villager_trades.mixin;

import com.chinaex123.vss_villager_trades.event.VillagerShopManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 村民开始交易 Mixin。
 * <p>
 * 接管村民的 startTrading 方法，取消原版交易界面，
 * 在满足条件时改为打开本模组的自定义村民交易菜单。
 * 婴儿、无职业、傻子或没有交易项的村民不打开菜单。
 */
@Mixin(Villager.class)
public class VillagerStartTradingMixin {

    /**
     * 在村民开始交易头部注入，替换为自定义交易菜单。
     * <p>
     * 先取消原方法执行，随后仅在服务端进行安全校验：
     * 玩家需为服务端玩家，村民非婴儿、职业非无职业或傻子，且存在交易项时，
     * 才打开自定义村民交易菜单；不满足条件时直接返回。
     *
     * @param player 交互的玩家
     * @param ci     回调信息，用于取消原方法执行
     */
    @Inject(method = "startTrading", at = @At("HEAD"), cancellable = true)
    private void vvt$replaceStartTrading(Player player, CallbackInfo ci) {
        // 取消原版 startTrading
        ci.cancel();

        Villager self = (Villager) (Object) this;

        // 客户端不处理。服务端的 openMenu 会同步到客户端自动打开 Screen。
        if (player.level().isClientSide) {
            return;
        }

        // 安全校验
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // 婴儿 → 不打开
        if (self.isBaby()) {
            return;
        }

        // 无职业或傻子 → 不打开
        VillagerProfession prof = self.getVillagerData().getProfession();
        if (prof == VillagerProfession.NONE || prof == VillagerProfession.NITWIT) {
            return;
        }

        // 没有交易项 → 不打开
        if (self.getOffers().isEmpty()) {
            return;
        }

        // 打开你的自定义交易菜单
        VillagerShopManager.openVillagerTrades(serverPlayer, self);
    }
}