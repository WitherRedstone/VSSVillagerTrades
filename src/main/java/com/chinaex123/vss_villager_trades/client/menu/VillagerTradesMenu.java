package com.chinaex123.vss_villager_trades.client.menu;

import com.chinaex123.vss_villager_trades.event.VillagerShopManager;
import com.chinaex123.vss_villager_trades.init.VVTMenuTypes;
import com.chinaex123.vss_villager_trades.network.VSSBalancePacket;
import com.chinaex123.vss_villager_trades.network.VillagerOffersPacket;
import com.chinaex123.vss_villager_trades.utils.ViScriptShopUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VillagerTradesMenu extends AbstractContainerMenu {

    public static final int OUTPUT_SLOT_START = 0;
    public static final int OUTPUT_SLOT_COUNT = 27;
    public static final int PLAYER_SLOT_START = OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT;

    private static final String NBT_OUTPUT = "vss_output";

    private AbstractVillager villager;

    private MerchantOffers serverOffers;

    private List<MerchantOffer> offers = Collections.emptyList();

    private int vssBalance;

    private final SimpleContainer outputContainer;

    public VillagerTradesMenu(int containerId, Inventory playerInventory, Player player,
                              AbstractVillager villager,
                              MerchantOffers serverOffers,
                              int balance) {
        super(VVTMenuTypes.VILLAGER_TRADES_MENU.get(), containerId);
        this.villager = villager;
        this.serverOffers = serverOffers;
        this.offers = (serverOffers == null)
                ? Collections.emptyList()
                : new ArrayList<>(serverOffers);
        this.vssBalance = balance;

        this.outputContainer = createOutputContainer();

        if (villager != null) {
            CompoundTag data = villager.getPersistentData();
            if (data.contains(NBT_OUTPUT)) {
                ContainerHelper.loadAllItems(
                        data.getCompound(NBT_OUTPUT),
                        outputContainer.getItems(),
                        villager.registryAccess()
                );
            }
        }

        addSlots(playerInventory);
    }

    public VillagerTradesMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        super(VVTMenuTypes.VILLAGER_TRADES_MENU.get(), containerId);
        this.outputContainer = createOutputContainer();
        addSlots(playerInventory);
    }

    private SimpleContainer createOutputContainer() {
        return new SimpleContainer(OUTPUT_SLOT_COUNT) {
            @Override
            public void setChanged() {
                super.setChanged();
                saveOutputToVillager();
            }
        };
    }

    private void saveOutputToVillager() {
        if (villager == null) return;
        CompoundTag data = villager.getPersistentData();
        CompoundTag outputTag = new CompoundTag();
        ContainerHelper.saveAllItems(
                outputTag,
                outputContainer.getItems(),
                villager.registryAccess()
        );
        data.put(NBT_OUTPUT, outputTag);
    }

    private void addSlots(Inventory playerInventory) {
        int outX = 108, outY = 18, slotSize = 18;
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                this.addSlot(new SlotOutput(this.outputContainer,
                        r * 9 + c, outX + c * slotSize, outY + r * slotSize));

        int invX = 108, invY = 84;
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                this.addSlot(new Slot(playerInventory, r * 9 + c + 9,
                        invX + c * slotSize, invY + r * slotSize));

        int hotY = 142;
        for (int c = 0; c < 9; c++)
            this.addSlot(new Slot(playerInventory, c, invX + c * slotSize, hotY));
    }

    public List<MerchantOffer> getOffers() {
        return offers;
    }

    public int getVssBalance() {
        return vssBalance;
    }

    public void setVssBalance(int b) {
        this.vssBalance = b;
    }

    public void setOffers(List<MerchantOffer> offers) {
        this.offers = (offers == null) ? Collections.emptyList() : new ArrayList<>(offers);
    }

    public boolean executeTrade(int offerIndex, int count, Player player) {
        if (player.level().isClientSide) return false;
        if (!(player instanceof ServerPlayer sp)) return false;
        if (serverOffers == null) return false;
        if (offerIndex < 0 || offerIndex >= serverOffers.size()) return false;

        MerchantOffer realOffer = serverOffers.get(offerIndex);
        if (realOffer.isOutOfStock()) return false;

        boolean didAny = false;
        for (int n = 0; n < count; n++) {
            if (realOffer.isOutOfStock()) break;

            if (VillagerShopManager.isSellOffer(realOffer)) {
                // 卖：物品 → VSS
                ItemStack need = VillagerShopManager.getNonEmeraldCost(realOffer);
                if (need.isEmpty()) break;
                if (!VillagerShopManager.removeFromPlayer(sp, need)) break;

                int reward = VillagerShopManager.emeraldRewardFromSell(realOffer);
                ViScriptShopUtil.addMoney(sp, reward);
                this.vssBalance += reward;
            } else {
                // 买：VSS → 物品
                int vssCost = VillagerShopManager.emeraldCostToVSS(realOffer);
                if (vssCost < 0) break;
                if (this.vssBalance < vssCost) break;

                ItemStack extraCost = VillagerShopManager.getNonEmeraldCost(realOffer);
                if (!extraCost.isEmpty()) {
                    if (!VillagerShopManager.removeFromPlayer(sp, extraCost)) break;
                }

                ViScriptShopUtil.removeMoney(sp, vssCost);
                this.vssBalance -= vssCost;
                placeResultInOutput(realOffer.getResult().copy());
            }

            realOffer.increaseUses();
            didAny = true;
        }

        if (!didAny) return false;

        this.offers = new ArrayList<>(serverOffers);
        PacketDistributor.sendToPlayer(sp, new VillagerOffersPacket(this.offers, this.vssBalance));
        this.broadcastChanges();
        sendBalanceToClient(sp);
        return true;
    }

    private void sendBalanceToClient(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new VSSBalancePacket(this.vssBalance));
    }

    private void placeResultInOutput(ItemStack result) {
        if (result.isEmpty()) return;

        for (int i = 0; i < OUTPUT_SLOT_COUNT; i++) {
            ItemStack ex = outputContainer.getItem(i);
            if (!ex.isEmpty()
                    && ItemStack.isSameItemSameComponents(ex, result)
                    && ex.getCount() < ex.getMaxStackSize()) {
                int add = Math.min(result.getCount(), ex.getMaxStackSize() - ex.getCount());
                ex.grow(add);
                result.shrink(add);
                outputContainer.setChanged();
                if (result.isEmpty()) return;
            }
        }
        for (int i = 0; i < OUTPUT_SLOT_COUNT; i++) {
            if (outputContainer.getItem(i).isEmpty()) {
                outputContainer.setItem(i, result);
                return;
            }
        }
    }

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

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public static class SlotOutput extends Slot {
        public SlotOutput(SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }
    }
}