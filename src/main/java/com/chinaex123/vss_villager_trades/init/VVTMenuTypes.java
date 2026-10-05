package com.chinaex123.vss_villager_trades.init;

import com.chinaex123.vss_villager_trades.VSSVillagerTrades;
import com.chinaex123.vss_villager_trades.client.menu.VillagerTradesMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class VVTMenuTypes {

    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, VSSVillagerTrades.MODID);

    /** 村民交易菜单类型 */
    public static final Supplier<MenuType<VillagerTradesMenu>> VILLAGER_TRADES_MENU =
            MENU_TYPES.register("villager_trades",
                    () -> IMenuTypeExtension.create(VillagerTradesMenu::new));

    private VVTMenuTypes() {}

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}