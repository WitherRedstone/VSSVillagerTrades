package com.chinaex123.vss_villager_trades.client.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;

public class TradeOutputContainer extends SimpleContainer {

    public static final int SIZE = 27;
    private static final String NBT_OUTPUT = "vss_output";

    private final AbstractVillager villager;

    public TradeOutputContainer(AbstractVillager villager) {
        super(SIZE);
        this.villager = villager;
        loadFromVillager();
    }

    private void loadFromVillager() {
        if (villager == null) return;
        CompoundTag data = villager.getPersistentData();
        if (!data.contains(NBT_OUTPUT)) return;

        ContainerHelper.loadAllItems(
                data.getCompound(NBT_OUTPUT),
                this.getItems(),
                villager.registryAccess()
        );
    }

    @Override
    public void setChanged() {
        super.setChanged();
        saveToVillager();
    }

    private void saveToVillager() {
        if (villager == null) return;
        CompoundTag data = villager.getPersistentData();
        CompoundTag outputTag = new CompoundTag();
        ContainerHelper.saveAllItems(
                outputTag,
                this.getItems(),
                villager.registryAccess()
        );
        data.put(NBT_OUTPUT, outputTag);
    }

    public void placeResult(ItemStack result) {
        if (result.isEmpty()) return;

        // 尝试堆叠
        for (int i = 0; i < SIZE; i++) {
            ItemStack ex = getItem(i);
            if (!ex.isEmpty()
                    && ItemStack.isSameItemSameComponents(ex, result)
                    && ex.getCount() < ex.getMaxStackSize()) {
                int add = Math.min(result.getCount(), ex.getMaxStackSize() - ex.getCount());
                ex.grow(add);
                result.shrink(add);
                setChanged();
                if (result.isEmpty()) return;
            }
        }

        // 找空格
        for (int i = 0; i < SIZE; i++) {
            if (getItem(i).isEmpty()) {
                setItem(i, result);
                return;
            }
        }
    }
}