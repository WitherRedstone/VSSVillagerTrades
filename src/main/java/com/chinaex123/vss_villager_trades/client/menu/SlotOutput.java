package com.chinaex123.vss_villager_trades.client.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 输出槽位。
 * <p>
 * 用于展示交易输出容器中的结果物品，
 * 禁止玩家向其中放入物品。
 */
public class SlotOutput extends Slot {

    /**
     * 构造输出槽位。
     *
     * @param container 所属容器
     * @param index     槽位索引
     * @param x         槽位 X 坐标
     * @param y         槽位 Y 坐标
     */
    public SlotOutput(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    /**
     * 检查是否允许放入物品。
     *
     * @param stack 待放入的物品堆
     * @return 始终返回 false，禁止放入
     */
    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }
}