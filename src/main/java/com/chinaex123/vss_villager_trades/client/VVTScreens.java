package com.chinaex123.vss_villager_trades.client;

import com.chinaex123.vss_villager_trades.VSSVillagerTrades;
import com.chinaex123.vss_villager_trades.client.gui.VillagerTradesScreen;
import com.chinaex123.vss_villager_trades.init.VVTMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = VSSVillagerTrades.MODID, value = Dist.CLIENT)
public class VVTScreens {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(VVTMenuTypes.VILLAGER_TRADES_MENU.get(), VillagerTradesScreen::new);
    }
}
