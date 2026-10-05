package com.chinaex123.vss_villager_trades.api;

import com.chinaex123.vss_villager_trades.config.VVTConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VSSVillagerCurrency {

    /** 货币物品 → 每单位多少 VSS */
    private static final Map<Item, Integer> currencies = new HashMap<>();

    private static boolean initialized = false;

    private VSSVillagerCurrency() {}

    public static synchronized void initialize() {
        if (initialized) return;
        initialized = true;

        if (loadFromConfig()) return;

        currencies.clear();
        currencies.put(Items.EMERALD, 5);
    }

    private static boolean loadFromConfig() {
        try {
            List<String> entries = VVTConfig.getCurrencyItems();

            Map<Item, Integer> loaded = new HashMap<>();
            for (String entry : entries) {
                int comma = entry.lastIndexOf(',');
                if (comma <= 0 || comma == entry.length() - 1) continue;

                String idStr = entry.substring(0, comma).trim();
                String priceStr = entry.substring(comma + 1).trim();

                ResourceLocation id = ResourceLocation.tryParse(idStr);
                if (id == null) continue;

                Item item = BuiltInRegistries.ITEM.get(id);
                if (item == Items.AIR) continue;

                int price;
                try {
                    price = Integer.parseInt(priceStr);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (price <= 0) continue;

                loaded.put(item, price);
            }

            if (loaded.isEmpty()) return false;

            currencies.clear();
            currencies.putAll(loaded);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ================== 判断 ==================

    public static boolean isCurrency(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return currencies.containsKey(stack.getItem());
    }

    public static boolean isSellOffer(MerchantOffer offer) {
        return isCurrency(offer.getResult());
    }

    public static boolean isBuyOffer(MerchantOffer offer) {
        return !isSellOffer(offer);
    }

    // ================== 换算 ==================

    /** 拿某物品的单价 */
    public static int getUnitPrice(Item item) {
        return currencies.getOrDefault(item, 1);
    }

    /** 买：玩家要花多少 VSS */
    public static int buyCostInVss(MerchantOffer offer) {
        int total = 0;
        boolean found = false;

        if (isCurrency(offer.getCostA())) {
            total += offer.getCostA().getCount() * getUnitPrice(offer.getCostA().getItem());
            found = true;
        }
        if (isCurrency(offer.getCostB())) {
            total += offer.getCostB().getCount() * getUnitPrice(offer.getCostB().getItem());
            found = true;
        }

        if (!found) return -1;
        return Math.max(1, total);
    }

    /** 卖：玩家能得多少 VSS */
    public static int sellRewardInVss(MerchantOffer offer) {
        if (!isCurrency(offer.getResult())) return 0;
        int total = offer.getResult().getCount()
                * getUnitPrice(offer.getResult().getItem());
        return Math.max(1, total);
    }

    /** 非货币部分的额外代价 */
    public static ItemStack getNonCurrencyCost(MerchantOffer offer) {
        if (!offer.getCostB().isEmpty() && !isCurrency(offer.getCostB())) {
            return offer.getCostB().copy();
        }
        if (!isCurrency(offer.getCostA())) {
            return offer.getCostA().copy();
        }
        return ItemStack.EMPTY;
    }

    public static void setCurrency(Item item) {
        currencies.clear();
        if (item != null) currencies.put(item, 1);
    }

    public static Item getCurrency() {
        return currencies.isEmpty() ? Items.EMERALD : currencies.keySet().iterator().next();
    }
}