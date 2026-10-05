package com.chinaex123.vss_villager_trades.util;

/**
 * 数字格式化工具类。
 * <p>
 * 将较大的数值按千、百万、十亿、万亿等量级缩写为带单位后缀的字符串，
 * 并保留一位小数（整数部分不显示小数）。
 */
public final class NumberFormatter {

    /**
     * 私有构造函数，防止实例化。
     * <p>
     * 该类只提供静态方法，不需要实例。
     */
    private NumberFormatter() {
    }

    /**
     * 将数值格式化为带单位的缩写字符串。
     * <p>
     * 依据绝对值大小选择单位：万亿（T）、十亿（B）、百万（M）、千（K），
     * 小于一千时直接返回原值；负值会保留负号。
     *
     * @param value 待格式化的数值
     * @return 格式化后的字符串
     */
    public static String format(long value) {
        long abs = Math.abs(value);
        String sign = value < 0 ? "-" : "";

        if (abs >= 1_000_000_000_000L) return sign + trimZero(abs / 1_000_000_000_000.0) + "T";
        if (abs >= 1_000_000_000L) return sign + trimZero(abs / 1_000_000_000.0) + "B";
        if (abs >= 1_000_000L) return sign + trimZero(abs / 1_000_000.0) + "M";
        if (abs >= 1_000L) return sign + trimZero(abs / 1_000.0) + "K";
        return sign + abs;
    }

    /**
     * 去除数值末尾多余的零。
     * <p>
     * 先将数值向下取整到一位小数，若为整数则返回整数形式，
     * 否则返回保留一位小数的字符串。
     *
     * @param v 待处理数值
     * @return 处理后的字符串
     */
    private static String trimZero(double v) {
        double rounded = Math.floor(v * 10) / 10.0;
        if (rounded == (long) rounded) {
            return String.valueOf((long) rounded);
        }
        return String.format(java.util.Locale.ROOT, "%.1f", rounded);
    }
}