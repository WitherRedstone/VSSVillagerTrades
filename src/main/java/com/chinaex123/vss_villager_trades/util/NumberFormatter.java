package com.chinaex123.vss_villager_trades.util;

public final class NumberFormatter {
    private NumberFormatter() {}

    public static String format(long value) {
        long abs = Math.abs(value);
        String sign = value < 0 ? "-" : "";

        if (abs >= 1_000_000_000_000L) return sign + trimZero(abs / 1_000_000_000_000.0) + "T";
        if (abs >= 1_000_000_000L)     return sign + trimZero(abs / 1_000_000_000.0) + "B";
        if (abs >= 1_000_000L)         return sign + trimZero(abs / 1_000_000.0) + "M";
        if (abs >= 1_000L)             return sign + trimZero(abs / 1_000.0) + "K";
        return sign + abs;
    }

    private static String trimZero(double v) {
        double rounded = Math.floor(v * 10) / 10.0;
        if (rounded == (long) rounded) {
            return String.valueOf((long) rounded);
        }
        return String.format(java.util.Locale.ROOT, "%.1f", rounded);
    }
}