package com.chinaex123.vss_villager_trades.client.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotOutput extends Slot {
    public SlotOutput(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }
}