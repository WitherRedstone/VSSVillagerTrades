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

public class VillagerTradesMenu extends AbstractContainerMenu {

    public static final int OUTPUT_SLOT_START = 0;
    public static final int OUTPUT_SLOT_COUNT = TradeOutputContainer.SIZE;
    public static final int PLAYER_SLOT_START = OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT;

    private final AbstractVillager villager;
    private final MerchantOffers serverOffers;
    private final TradeOutputContainer outputContainer;

    private List<MerchantOffer> offers = Collections.emptyList();
    private int vssBalance;

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

    public VillagerTradesMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        super(VVTMenuTypes.VILLAGER_TRADES_MENU.get(), containerId);
        this.villager = null;
        this.serverOffers = null;
        this.outputContainer = new TradeOutputContainer(null);

        VillagerSlotLayout.addSlots(outputContainer, playerInventory, this::addSlot);
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
}