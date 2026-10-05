package com.chinaex123.vss_villager_trades;

import com.chinaex123.vss_villager_trades.api.VSSVillagerCurrency;
import com.chinaex123.vss_villager_trades.config.VVTConfig;
import com.chinaex123.vss_villager_trades.event.VillagerShopManager;
import com.chinaex123.vss_villager_trades.init.VVTMenuTypes;
import com.chinaex123.vss_villager_trades.network.VVTNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(VSSVillagerTrades.MODID)
public class VSSVillagerTrades {
    public static final String MODID = "vss_villager_trades";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VSSVillagerTrades(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, VVTConfig.SPEC);
        modEventBus.register(this);

        VVTMenuTypes.register(modEventBus);
        NeoForge.EVENT_BUS.register(VillagerShopManager.class);
        VVTNetwork.init(modEventBus);
    }

    @SubscribeEvent
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        VSSVillagerCurrency.initialize();
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}