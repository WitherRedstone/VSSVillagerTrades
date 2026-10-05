package com.chinaex123.vss_villager_trades.client.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;

/**
 * 交易输出容器。
 * <p>
 * 用于存放村民交易产生的结果物品，共 27 格。
 * 容器内容持久化到村民的持久化数据中，
 * 并提供按堆叠优先、空槽次之的结果存放方法。
 */
public class TradeOutputContainer extends SimpleContainer {

    /** 容器槽位数量 */
    public static final int SIZE = 27;
    /** 村民持久化数据中输出容器的键名 */
    private static final String NBT_OUTPUT = "vss_output";

    /** 关联的村民，可能为 null */
    private final AbstractVillager villager;

    /**
     * 构造交易输出容器。
     * <p>
     * 初始化 27 格容器并尝试从村民持久化数据中加载已有内容。
     *
     * @param villager 关联的村民，可为 null
     */
    public TradeOutputContainer(AbstractVillager villager) {
        super(SIZE);
        this.villager = villager;
        loadFromVillager();
    }

    /**
     * 从村民持久化数据加载容器内容。
     * <p>
     * 村民为空或不存在输出数据时直接返回。
     */
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

    /**
     * 容器内容变化时同步保存到村民持久化数据。
     */
    @Override
    public void setChanged() {
        super.setChanged();
        saveToVillager();
    }

    /**
     * 将容器内容保存到村民持久化数据。
     * <p>
     * 村民为空时不执行任何操作。
     */
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

    /**
     * 将结果物品放入容器。
     * <p>
     * 优先尝试堆叠到已有的同类物品上，放不下时再寻找空槽位存放；
     * 两种情况均无法容纳时剩余部分不再处理。
     *
     * @param result 待放入的结果物品
     */
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