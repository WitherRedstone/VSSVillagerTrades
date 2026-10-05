package com.chinaex123.vss_villager_trades.mixin;

import com.chinaex123.vss_villager_trades.event.VillagerShopManager;
import com.chinaex123.vss_villager_trades.util.ViScriptShopUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.ShowTradesToPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 村民交易展示 Mixin。
 * <p>
 * 接管村民向玩家展示可交易物品的行为：
 * 当交互玩家拥有 VSS 余额时，改用手持物品的方式展示村民的交易内容，
 * 并取消原版展示逻辑；无余额或无可用物品时放行原版逻辑。
 */
@Mixin(ShowTradesToPlayer.class)
public abstract class ShowVillagerTradesMixin {

    /** 用于展示的物品列表 */
    @Shadow
    @Final
    private List<ItemStack> displayItems;

    /** 玩家当前手持的物品堆 */
    @Shadow
    private ItemStack playerItemStack;

    /** 循环计数器 */
    @Shadow
    private int cycleCounter;

    /** 当前展示索引 */
    @Shadow
    private int displayIndex;

    /** 剩余展示时间 */
    @Shadow
    private int lookTime;

    /**
     * 获取村民当前的注视目标。
     *
     * @param villager 村民
     * @return 注视目标实体
     */
    @Shadow
    private LivingEntity lookAtTarget(Villager villager) {
        throw new AssertionError();
    }

    /**
     * 以手持物品的形式展示指定物品。
     *
     * @param villager 村民
     * @param item     要展示的物品
     */
    @Shadow
    private static void displayAsHeldItem(Villager villager, ItemStack item) {
        throw new AssertionError();
    }

    /**
     * 清除村民当前手持的展示物品。
     *
     * @param villager 村民
     */
    @Shadow
    private static void clearHeldItem(Villager villager) {
        throw new AssertionError();
    }

    /**
     * 在展示交易逻辑头部注入，改为展示 VSS 相关交易内容。
     * <p>
     * 仅在注视目标为玩家且该玩家拥有 VSS 余额时生效：
     * 以村民的可展示物品替换展示列表，手持展示首个物品并设置展示时长，
     * 随后取消原方法执行；否则放行原版逻辑。
     *
     * @param level    服务端世界
     * @param owner    村民
     * @param gameTime 当前游戏时间
     * @param ci       回调信息，用于取消原方法执行
     */
    @Inject(
            method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void vss$showTrades(ServerLevel level, Villager owner, long gameTime, CallbackInfo ci) {
        LivingEntity target = this.lookAtTarget(owner);
        if (!(target instanceof ServerPlayer player)) return;
        if (ViScriptShopUtil.getMoney(player) <= 0) return;

        // 重新计算显示列表
        List<ItemStack> display = vss$getDisplayItems(owner);
        if (display.isEmpty()) return;

        // 和当前列表比较，不一样才重置
        if (!vss$sameList(this.displayItems, display)) {
            this.displayItems.clear();
            this.displayItems.addAll(display);
            this.cycleCounter = 0;
            this.displayIndex = 0;
            displayAsHeldItem(owner, display.get(0));
        }

        this.lookTime = 900;

        // ★ 手动跑原版的循环逻辑
        if (this.displayItems.size() >= 2 && ++this.cycleCounter >= 40) {
            ++this.displayIndex;
            this.cycleCounter = 0;
            if (this.displayIndex > this.displayItems.size() - 1) {
                this.displayIndex = 0;
            }
            displayAsHeldItem(owner, this.displayItems.get(this.displayIndex));
        }

        ci.cancel();
    }

    @Unique
    private static boolean vss$sameList(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!ItemStack.isSameItemSameComponents(a.get(i), b.get(i))) return false;
        }
        return true;
    }

    /**
     * 构建村民的可展示物品列表。
     * <p>
     * 遍历村民交易列表：出售交易取其非货币代价物品，购买交易取产出物品；
     * 跳过空物品，每个物品仅取一个，最多收集 5 个。
     *
     * @param villager 村民
     * @return 可展示的物品列表
     */
    @Unique
    private static List<ItemStack> vss$getDisplayItems(Villager villager) {
        List<ItemStack> result = new ArrayList<>();
        for (MerchantOffer offer : villager.getOffers()) {
            ItemStack display;
            if (VillagerShopManager.isSellOffer(offer)) {
                display = VillagerShopManager.getNonEmeraldCost(offer);
            } else {
                display = offer.getResult();
            }
            if (!display.isEmpty()) {
                result.add(display.copyWithCount(1));
            }
            if (result.size() >= 5) break;
        }
        return result;
    }
}