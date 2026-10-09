package com.chinaex123.vss_villager_trades.mixin;

import com.chinaex123.vss_villager_trades.event.VillagerShopManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 村民交互 Mixin。
 * <p>
 * 接管普通村民与流浪商人的 mobInteract 方法，
 * 在满足条件时改为打开本模组的自定义交易菜单。
 * 婴儿、无交易项的无职业或傻子村民会表现为摇头拒绝；
 * 而有交易项（如已锁定交易）的无职业或傻子村民仍可打开菜单。
 */
@Mixin({Villager.class, WanderingTrader.class})
public class VillagerInteractMixin {

    /**
     * 在实体交互头部注入，替换为自定义交易菜单。
     * <p>
     * 仅响应主手且未处于次要使用状态的交互：
     * 实体需存活、未睡觉且未在交易中。
     * 客户端直接返回成功；服务端进行进一步状态校验：
     * 婴儿会摇头拒绝；无职业或傻子村民仅在交易项为空时摇头拒绝，
     * 有交易项时仍可打开；其余无交易项的情形也摇头拒绝；
     * 校验通过后打开自定义村民交易菜单。
     *
     * @param player 交互的玩家
     * @param hand   交互手
     * @param cir    回调信息，用于设置返回值
     */
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void vvt$openCustomTradeGUI(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (hand != InteractionHand.MAIN_HAND) return;
        if (player.isSecondaryUseActive()) return;

        AbstractVillager self = (AbstractVillager) (Object) this;

        // 存活、没在睡觉、没在交易
        if (!self.isAlive() || self.isSleeping() || self.isTrading()) return;

        // 客户端直接返回
        if (player.level().isClientSide) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        // Villager 状态检查
        if (self instanceof Villager v) {
            // 婴儿 → 摇头
            if (v.isBaby()) {
                ((VillagerAccessorMixin) v).invokeSetUnhappy();
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }
            // 无职业 → 摇头（但如果交易被锁 offers 不为空，说明是有锁的村民，不摇头）
            VillagerProfession prof = v.getVillagerData().getProfession();
            if ((prof == VillagerProfession.NONE || prof == VillagerProfession.NITWIT)
                    && self.getOffers().isEmpty()) {
                ((VillagerAccessorMixin) v).invokeSetUnhappy();
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }
        }

        // 没可交易项 → 摇头
        if (self.getOffers().isEmpty()) {
            if (self instanceof Villager v) {
                ((VillagerAccessorMixin) v).invokeSetUnhappy();
            }
            cir.setReturnValue(InteractionResult.CONSUME);
            return;
        }

        // 打开自定义交易菜单
        VillagerShopManager.openVillagerTrades((ServerPlayer) player, self);
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}