package com.chinaex123.vss_villager_trades.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class VVTConfig {
    public static final ModConfigSpec.BooleanValue INFINITE_TRADES;
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<List<?>> CURRENCY_ITEMS;

    static {
        BUILDER.comment("通用配置").push("Common Config");

        INFINITE_TRADES = BUILDER
                .comment("是否启用无限交易")
                        .define("infiniteTrades", false);
        CURRENCY_ITEMS = BUILDER
                .comment(
                        "村民交易使用的货币物品列表",
                        "格式: \"物品ID, 价格\"",
                        "例如: [\"minecraft:emerald, 10\", \"minecraft:diamond, 200\"]",
                        "表示 1 绿宝石 = 10 VSS, 1 钻石 = 200 VSS"
                )
                .comment(
                        "List of currency items used in villager trades",
                        "Format: \"itemID, price\"",
                        "Example: [\"minecraft:emerald, 10\", \"minecraft:diamond, 200\"]",
                        "Means 1 emerald = 10 VSS, 1 diamond = 200 VSS"
                )
                .defineList("currencyItems",
                        List.of(
                                "minecraft:emerald, 5"
                        ), obj -> obj instanceof String s && isValidEntry(s));

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private static boolean isValidEntry(String s) {
        int comma = s.lastIndexOf(',');
        if (comma <= 0 || comma == s.length() - 1) return false;
        String idStr = s.substring(0, comma).trim();
        String priceStr = s.substring(comma + 1).trim();
        if (ResourceLocation.tryParse(idStr) == null) return false;
        try {
            return Integer.parseInt(priceStr) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static List<String> getCurrencyItems() {
        return CURRENCY_ITEMS.get().stream()
                .map(obj -> (String) obj)
                .toList();
    }
}