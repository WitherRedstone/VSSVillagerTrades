package com.chinaex123.vss_villager_trades.client.gui;

import net.minecraft.util.Mth;

public class TradeScrollController {

    private int scrollOffset = 0;
    private boolean dragging = false;

    public int offset() { return scrollOffset; }

    public void onScroll(int total, double delta) {
        int max = TradeListLayout.maxScroll(total);
        if (max <= 0) return;
        scrollOffset = Mth.clamp((int) (scrollOffset - delta), 0, max);
    }

    public boolean hitScroller(int total, int relX, int relY) {
        if (total <= TradeListLayout.OFFER_COUNT) return false;
        return relX >= TradeListLayout.SCROLLER_X && relX < TradeListLayout.SCROLLER_X + TradeListLayout.SCROLLER_W
                && relY >= TradeListLayout.SCROLLER_Y && relY < TradeListLayout.SCROLLER_Y + TradeListLayout.SCROLLER_H;
    }

    public void beginDrag() { dragging = true; }

    public boolean isDragging() { return dragging; }

    public void onDrag(int total, int relY) {
        int max = TradeListLayout.maxScroll(total);
        if (max <= 0) return;

        var geom = TradeListLayout.scrollerGeom(total, scrollOffset);
        int maxScrollPx = TradeListLayout.SCROLLER_H - geom.height();
        if (maxScrollPx <= 0) return;

        float ratio = (float) (relY - TradeListLayout.SCROLLER_Y - geom.height() / 2) / maxScrollPx;
        scrollOffset = Mth.clamp(Math.round(ratio * max), 0, max);
    }

    public void endDrag() { dragging = false; }
}