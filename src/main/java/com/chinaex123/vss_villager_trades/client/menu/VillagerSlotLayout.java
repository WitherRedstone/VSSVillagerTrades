package com.chinaex123.vss_villager_trades.client.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.function.Consumer;

public final class VillagerSlotLayout {
    private VillagerSlotLayout() {}

    public static final int OUTPUT_X = 108;
    public static final int OUTPUT_Y = 18;
    public static final int SLOT_SIZE = 18;

    public static final int INV_X = 108;
    public static final int INV_Y = 84;
    public static final int HOTBAR_Y = 142;

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