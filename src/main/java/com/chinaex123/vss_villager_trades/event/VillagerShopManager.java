package com.chinaex123.vss_villager_trades.event;

import com.chinaex123.vss_villager_trades.api.VSSVillagerCurrency;
import com.chinaex123.vss_villager_trades.client.menu.VillagerTradesMenu;
import com.chinaex123.vss_villager_trades.network.VillagerOffersPacket;
import com.chinaex123.vss_villager_trades.util.ViScriptShopUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

public final class VillagerShopManager {

    private VillagerShopManager() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!(event.getTarget() instanceof AbstractVillager villager)) return;

        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);

        openVillagerTrades(serverPlayer, villager);
    }

    public static void openVillagerTrades(ServerPlayer player, AbstractVillager villager) {
        if (villager instanceof Villager v && v.shouldRestock()) {
            v.restock();
        }

        MerchantOffers offers = getOffersFromVillager(villager);
        int balance = ViScriptShopUtil.getMoney(player);
        Component title = getVillagerTitle(villager);

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new VillagerTradesMenu(id, inv, p, villager, offers, balance),
                title
        ));

        PacketDistributor.sendToPlayer(player,
                new VillagerOffersPacket(new ArrayList<>(offers), balance));
    }

    public static MerchantOffers getOffersFromVillager(AbstractVillager villager) {
        if (villager == null) return new MerchantOffers();

        return villager.getOffers();
    }

    public static boolean isSellOffer(MerchantOffer offer) {
        return VSSVillagerCurrency.isSellOffer(offer);
    }

    /** 买：玩家花多少 VSS */
    public static int emeraldCostToVSS(MerchantOffer offer) {
        return VSSVillagerCurrency.buyCostInVss(offer);
    }

    /** 卖：玩家得多少 VSS */
    public static int emeraldRewardFromSell(MerchantOffer offer) {
        return VSSVillagerCurrency.sellRewardInVss(offer);
    }

    /** 非绿宝石部分的代价 */
    public static ItemStack getNonEmeraldCost(MerchantOffer offer) {
        return VSSVillagerCurrency.getNonCurrencyCost(offer);
    }

    public static boolean removeFromPlayer(ServerPlayer player, ItemStack target) {
        if (target.isEmpty()) return true;

        int need = target.getCount();
        int have = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, target)) {
                have += s.getCount();
            }
        }
        if (have < need) return false;

        int remaining = need;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, target)) {
                int take = Math.min(s.getCount(), remaining);
                s.shrink(take);
                remaining -= take;
            }
        }
        return true;
    }

    private static Component getVillagerTitle(AbstractVillager villager) {
        if (villager instanceof Villager v) {
            VillagerProfession profession = v.getVillagerData().getProfession();
            if (profession == VillagerProfession.NONE) {
                return Component.translatable("entity.minecraft.villager");
            }
            return Component.translatable("entity.minecraft.villager." + profession.name());
        }
        if (villager instanceof WanderingTrader) {
            return Component.translatable("entity.minecraft.wandering_trader");
        }
        return villager.getDisplayName();
    }
}