package com.chinaex123.vss_villager_trades.client.gui;

import net.minecraft.util.Mth;

/**
 * 交易列表滚动控制器。
 * <p>
 * 维护交易列表的滚动偏移与拖动状态，
 * 支持滚轮滚动、命中检测以及拖动滚动条调整偏移。
 */
public class TradeScrollController {

    /** 当前滚动偏移量（以条目为单位） */
    private int scrollOffset = 0;
    /** 是否处于拖动状态 */
    private boolean dragging = false;

    /**
     * 获取当前滚动偏移量。
     *
     * @return 滚动偏移量
     */
    public int offset() { return scrollOffset; }

    /**
     * 处理滚轮滚动。
     * <p>
     * 依据总条目数计算最大偏移，若无需滚动则直接返回；
     * 否则按滚动方向调整偏移并钳制在有效范围内。
     *
     * @param total 总条目数
     * @param delta 滚动量
     */
    public void onScroll(int total, double delta) {
        int max = TradeListLayout.maxScroll(total);
        if (max <= 0) return;
        scrollOffset = Mth.clamp((int) (scrollOffset - delta), 0, max);
    }

    /**
     * 判断指定相对坐标是否命中滚动条区域。
     * <p>
     * 交易总数不超过可见行数时视为未命中；
     * 否则判断坐标是否落在滚动条矩形范围内。
     *
     * @param total 总条目数
     * @param relX  相对界面左上角的 X 坐标
     * @param relY  相对界面左上角的 Y 坐标
     * @return 命中返回 true
     */
    public boolean hitScroller(int total, int relX, int relY) {
        if (total <= TradeListLayout.OFFER_COUNT) return false;
        return relX >= TradeListLayout.SCROLLER_X && relX < TradeListLayout.SCROLLER_X + TradeListLayout.SCROLLER_W
                && relY >= TradeListLayout.SCROLLER_Y && relY < TradeListLayout.SCROLLER_Y + TradeListLayout.SCROLLER_H;
    }

    /**
     * 开始拖动滚动条。
     */
    public void beginDrag() { dragging = true; }

    /**
     * 是否处于拖动状态。
     *
     * @return 拖动中返回 true
     */
    public boolean isDragging() { return dragging; }

    /**
     * 处理拖动过程。
     * <p>
     * 依据拖动位置在可滚动像素范围内的比例，计算并钳制新的滚动偏移；
     * 无需滚动或可滚动像素不足时直接返回。
     *
     * @param total 总条目数
     * @param relY  相对界面左上角的 Y 坐标
     */
    public void onDrag(int total, int relY) {
        int max = TradeListLayout.maxScroll(total);
        if (max <= 0) return;

        var geom = TradeListLayout.scrollerGeom(total, scrollOffset);
        int maxScrollPx = TradeListLayout.SCROLLER_H - geom.height();
        if (maxScrollPx <= 0) return;

        // 以滑块中心为基准计算拖动比例
        float ratio = (float) (relY - TradeListLayout.SCROLLER_Y - geom.height() / 2) / maxScrollPx;
        scrollOffset = Mth.clamp(Math.round(ratio * max), 0, max);
    }

    /**
     * 结束拖动滚动条。
     */
    public void endDrag() { dragging = false; }
}