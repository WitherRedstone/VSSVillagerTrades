package com.chinaex123.vss_villager_trades.client.gui;

/**
 * 村民交易界面布局常量与几何计算工具。
 * <p>
 * 定义界面尺寸、交易列表区域、滚动条区域与纹理尺寸等布局常量，
 * 并提供滚动条几何计算与最大滚动偏移计算。
 */
public final class TradeListLayout {

    /**
     * 私有构造函数，防止实例化。
     * <p>
     * 该类只提供静态常量与方法，不需要实例。
     */
    private TradeListLayout() {}

    /** 界面宽度 */
    public static final int GUI_WIDTH  = 276;
    /** 界面高度 */
    public static final int GUI_HEIGHT = 166;

    /** 交易列表区域起始 X 坐标 */
    public static final int LEFT_AREA_X = 5;
    /** 交易列表区域起始 Y 坐标 */
    public static final int LEFT_AREA_Y = 18;
    /** 单个交易条目高度 */
    public static final int TRADE_ITEM_HEIGHT = 20;
    /** 单个交易条目宽度 */
    public static final int TRADE_ITEM_W = 88;
    /** 每页可见的交易条目数量 */
    public static final int OFFER_COUNT = 7;

    /** 滚动条起始 X 坐标 */
    public static final int SCROLLER_X = 94;
    /** 滚动条起始 Y 坐标 */
    public static final int SCROLLER_Y = 18;
    /** 滚动条宽度 */
    public static final int SCROLLER_W = 6;
    /** 滚动条总高度 */
    public static final int SCROLLER_H = 139;

    /** 背景纹理宽度 */
    public static final int TEX_WIDTH  = 512;
    /** 背景纹理高度 */
    public static final int TEX_HEIGHT = 256;

    /**
     * 滚动条几何信息。
     *
     * @param height 滚动条滑块高度
     * @param y      滚动条滑块起始 Y 坐标
     */
    public record ScrollerGeom(int height, int y) {}

    /**
     * 计算滚动条滑块的几何信息。
     * <p>
     * 交易总数不超过可见行数时，滑块铺满整个滚动条；
     * 否则依据总条目数与当前偏移计算滑块高度与位置：
     * 高度按可见行数占比缩放且不小于 27，位置按偏移在可滚动范围内的比例换算。
     *
     * @param total        总条目数
     * @param scrollOffset 当前滚动偏移量
     * @return 滚动条滑块几何信息
     */
    public static ScrollerGeom scrollerGeom(int total, int scrollOffset) {
        if (total <= OFFER_COUNT) return new ScrollerGeom(SCROLLER_H, SCROLLER_Y);

        int maxScroll = total - OFFER_COUNT;
        int h = Math.max(27, (int) ((float) OFFER_COUNT / total * SCROLLER_H));
        int maxScrollPx = SCROLLER_H - h;
        int y = SCROLLER_Y + (int) ((float) scrollOffset / maxScroll * maxScrollPx);
        return new ScrollerGeom(h, y);
    }

    /**
     * 计算最大滚动偏移量。
     * <p>
     * 为总条目数减去可见行数，最小为 0。
     *
     * @param total 总条目数
     * @return 最大滚动偏移量
     */
    public static int maxScroll(int total) {
        return Math.max(0, total - OFFER_COUNT);
    }
}