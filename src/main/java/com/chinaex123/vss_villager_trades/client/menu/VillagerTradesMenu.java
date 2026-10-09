package com.chinaex123.vss_villager_trades.client.menu;

import com.chinaex123.vss_villager_trades.init.VVTMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 村民交易菜单。
 * <p>
 * 包含输出槽位与玩家物品栏槽位，持有服务端交易列表与玩家 VSS货币 余额。
 * 支持在服务端执行交易并根据结果同步交易列表与余额到客户端。
 */
public class VillagerTradesMenu extends AbstractContainerMenu {

    /** 输出槽位起始索引 */
    public static final int OUTPUT_SLOT_START = 0;
    /** 输出槽位数量 */
    public static final int OUTPUT_SLOT_COUNT = TradeOutputContainer.SIZE;
    /** 玩家槽位起始索引 */
    public static final int PLAYER_SLOT_START = OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT;

    /** 关联的村民，客户端构造时为 null */
    private final AbstractVillager villager;
    /** 服务端交易列表，客户端构造时为 null */
    private final MerchantOffers serverOffers;
    /** 交易输出容器 */
    private final TradeOutputContainer outputContainer;

    /** 当前展示的交易列表 */
    private List<MerchantOffer> offers = Collections.emptyList();
    /** 当前展示的 VSS货币 余额 */
    private int vssBalance;

    /**
     * 构造函数（服务端创建）。
     * <p>
     * 以村民交易列表初始化展示用列表，创建输出容器并添加各类槽位。
     *
     * @param containerId     菜单 ID
     * @param playerInventory 玩家物品栏
     * @param player          打开菜单的玩家
     * @param villager        关联的村民
     * @param serverOffers    服务端交易列表
     * @param balance         玩家当前 VSS货币 余额
     */
    public VillagerTradesMenu(int containerId, Inventory playerInventory, Player player, AbstractVillager villager, MerchantOffers serverOffers, int balance) {
        super(VVTMenuTypes.VILLAGER_TRADES_MENU.get(), containerId);
        this.villager = villager;
        this.serverOffers = serverOffers;
        this.offers = (serverOffers == null)
                ? Collections.emptyList()
                : new ArrayList<>(serverOffers);
        this.vssBalance = balance;
        this.outputContainer = new TradeOutputContainer(villager);

        VillagerSlotLayout.addSlots(outputContainer, playerInventory, this::addSlot);
    }

    /**
     * 构造函数（通过网络缓冲区创建）。
     * <p>
     * 用于客户端接收服务端菜单数据时创建菜单实例，
     * 此时村民与服务端交易列表均为 null，仅创建空输出容器并添加槽位。
     *
     * @param containerId     菜单 ID
     * @param playerInventory 玩家物品栏
     * @param buf             网络缓冲区
     */
    public VillagerTradesMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        super(VVTMenuTypes.VILLAGER_TRADES_MENU.get(), containerId);
        this.villager = null;
        this.serverOffers = null;
        this.outputContainer = new TradeOutputContainer(null);

        VillagerSlotLayout.addSlots(outputContainer, playerInventory, this::addSlot);
    }

    /**
     * 获取当前展示的交易列表。
     *
     * @return 交易列表
     */
    public List<MerchantOffer> getOffers() {
        return offers;
    }

    /**
     * 获取当前展示的 VSS货币 余额。
     *
     * @return VSS货币 余额
     */
    public int getVssBalance() {
        return vssBalance;
    }

    /**
     * 设置当前展示的 VSS货币 余额。
     *
     * @param b 新的余额
     */
    public void setVssBalance(int b) {
        this.vssBalance = b;
    }

    /**
     * 设置当前展示的交易列表。
     * <p>
     * 传入 null 时置为空列表。
     *
     * @param offers 新的交易列表
     */
    public void setOffers(List<MerchantOffer> offers) {
        this.offers = (offers == null) ? Collections.emptyList() : new ArrayList<>(offers);
    }

    /**
     * 执行交易。
     * <p>
     * 仅在服务端执行：委托交易执行器按序号与次数处理交易，
     * 若产生了实际交易则更新余额与交易列表，
     * 并将结果同步到客户端、刷新菜单。
     *
     * @param offerIndex 交易在列表中的索引
     * @param count      请求执行的交易次数
     * @param player     执行交易的玩家
     */
    public void executeTrade(int offerIndex, int count, Player player) {
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer sp)) return;

        TradeExecutor.Result result = TradeExecutor.execute(
                serverOffers, offerIndex, count, sp, vssBalance, outputContainer);

        if (!result.didAny()) return;

        this.vssBalance = result.newBalance();
        this.offers = new ArrayList<>(serverOffers);

        MenuNetworkSync.sendOffersAndBalance(sp, this.offers, this.vssBalance);
        this.broadcastChanges();
    }

    /**
     * 快速移动物品。
     * <p>
     * 仅允许将输出槽位中的物品移动到玩家物品栏，
     * 反向移动则直接返回空物品堆。
     *
     * @param player 执行操作的玩家
     * @param index  被点击的槽位索引
     * @return 移动后的物品堆栈（空表示移动失败）
     */
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index < OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT) {
                if (!this.moveItemStackTo(stackInSlot, PLAYER_SLOT_START, PLAYER_SLOT_START + 36, true))
                    return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }

    /**
     * 检查玩家是否仍然可以访问此菜单。
     * <p>
     * 检查村民是否仍然存活且距离玩家在允许范围内。
     *
     * @param player 要检查的玩家
     * @return 村民存活且距离足够远时返回 true
     */
    @Override
    public boolean stillValid(@NotNull Player player) {
        return villager != null && villager.isAlive()
                && player.distanceToSqr(villager) <= 64.0;
    }
}