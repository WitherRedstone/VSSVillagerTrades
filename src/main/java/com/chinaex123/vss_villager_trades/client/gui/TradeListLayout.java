package com.chinaex123.vss_villager_trades.client.gui;

public final class TradeListLayout {
    private TradeListLayout() {}

    public static final int GUI_WIDTH  = 276;
    public static final int GUI_HEIGHT = 166;

    public static final int LEFT_AREA_X = 5;
    public static final int LEFT_AREA_Y = 18;
    public static final int TRADE_ITEM_HEIGHT = 20;
    public static final int TRADE_ITEM_W = 88;
    public static final int OFFER_COUNT = 7;

    public static final int SCROLLER_X = 94;
    public static final int SCROLLER_Y = 18;
    public static final int SCROLLER_W = 6;
    public static final int SCROLLER_H = 139;

    public static final int TEX_WIDTH  = 512;
    public static final int TEX_HEIGHT = 256;

    public record ScrollerGeom(int height, int y) {}

    public static ScrollerGeom scrollerGeom(int total, int scrollOffset) {
        if (total <= OFFER_COUNT) return new ScrollerGeom(SCROLLER_H, SCROLLER_Y);

        int maxScroll = total - OFFER_COUNT;
        int h = Math.max(27, (int) ((float) OFFER_COUNT / total * SCROLLER_H));
        int maxScrollPx = SCROLLER_H - h;
        int y = SCROLLER_Y + (int) ((float) scrollOffset / maxScroll * maxScrollPx);
        return new ScrollerGeom(h, y);
    }

    public static int maxScroll(int total) {
        return Math.max(0, total - OFFER_COUNT);
    }
}