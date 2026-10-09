package com.chinaex123.vss_villager_trades.client.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.function.Consumer;

/**
 * 村民交易菜单槽位布局工具。
 * <p>
 * 定义输出区域、玩家背包与快捷栏的坐标常量，
 * 并提供一次性添加全部槽位的静态方法。
 */
public final class VillagerSlotLayout {

    /**
     * 私有构造函数，防止实例化。
     * <p>
     * 该类只提供静态常量与方法，不需要实例。
     */
    private VillagerSlotLayout() {}

    /** 输出区域起始 X 坐标 */
    public static final int OUTPUT_X = 108;
    /** 输出区域起始 Y 坐标 */
    public static final int OUTPUT_Y = 19;
    /** 单个槽位的边长 */
    public static final int SLOT_SIZE = 18;

    /** 玩家背包起始 X 坐标 */
    public static final int INV_X = 108;
    /** 玩家背包起始 Y 坐标 */
    public static final int INV_Y = 84;
    /** 快捷栏 Y 坐标 */
    public static final int HOTBAR_Y = 142;

    /**
     * 向菜单中添加全部槽位。
     * <p>
     * 依次添加 3×9 的输出槽位、3×9 的玩家背包槽位与 9 格快捷栏槽位，
     * 通过传入的槽位添加器完成注册。
     *
     * @param output          交易输出容器
     * @param playerInventory 玩家物品栏
     * @param slotAdder       槽位添加器
     */
    public static void addSlots(TradeOutputContainer output, Inventory playerInventory, Consumer<Slot> slotAdder) {

        // 输出 3×9
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                slotAdder.accept(new SlotOutput(output,
                        r * 9 + c,
                        OUTPUT_X + c * SLOT_SIZE,
                        OUTPUT_Y + r * SLOT_SIZE));
            }
        }

        // 玩家背包 3×9
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                slotAdder.accept(new Slot(playerInventory,
                        r * 9 + c + 9,
                        INV_X + c * SLOT_SIZE,
                        INV_Y + r * SLOT_SIZE));
            }
        }

        // 快捷栏
        for (int c = 0; c < 9; c++) {
            slotAdder.accept(new Slot(playerInventory, c,
                    INV_X + c * SLOT_SIZE,
                    HOTBAR_Y));
        }
    }
}