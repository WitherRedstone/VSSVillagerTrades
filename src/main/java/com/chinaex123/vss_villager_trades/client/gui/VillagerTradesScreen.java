package com.chinaex123.vss_villager_trades.client.gui;

import com.chinaex123.vss_villager_trades.VSSVillagerTrades;
import com.chinaex123.vss_villager_trades.client.menu.VillagerTradesMenu;
import com.chinaex123.vss_villager_trades.network.RefreshTradesPacket;
import com.chinaex123.vss_villager_trades.network.TradeRequestPacket;
import com.chinaex123.vss_villager_trades.util.NumberFormatter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * 村民交易界面。
 * <p>
 * 绘制交易背景、交易条目与滚动条，并在顶部显示玩家 VSS 余额。
 * 支持鼠标滚轮与拖动滚动条翻动交易列表，
 * 点击交易条目或按住 Shift 点击可发送对应次数的交易请求。
 */
@OnlyIn(Dist.CLIENT)
public class VillagerTradesScreen extends AbstractContainerScreen<VillagerTradesMenu> {

    /** 界面背景纹理位置 */
    public static final ResourceLocation BACKGROUND = VSSVillagerTrades.id("textures/gui/villager_trades.png");

    /** 交易条目按钮数组 */
    private final Button[] offerButtons = new Button[TradeListLayout.OFFER_COUNT];
    /** 刷新交易按钮 */
    private RefreshButton refreshButton;
    /** 滚动控制器 */
    private final TradeScrollController scrollController = new TradeScrollController();

    /**
     * 构造村民交易界面。
     *
     * @param menu      村民交易菜单
     * @param inventory 玩家物品栏
     * @param title     界面标题
     */
    public VillagerTradesScreen(VillagerTradesMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth  = TradeListLayout.GUI_WIDTH;
        this.imageHeight = TradeListLayout.GUI_HEIGHT;
        this.inventoryLabelX = 111;
        this.inventoryLabelY = this.imageHeight - 91;
    }

    /**
     * 初始化界面组件。
     * <p>
     * 在左侧区域按行创建交易条目按钮，初始均为禁用状态，
     * 由渲染阶段按实际交易数量启用。
     */
    @Override
    protected void init() {
        super.init();

        int x = this.leftPos + TradeListLayout.LEFT_AREA_X;
        int y = this.topPos  + TradeListLayout.LEFT_AREA_Y;

        for (int row = 0; row < TradeListLayout.OFFER_COUNT; row++) {
            final int r = row;
            offerButtons[row] = Button.builder(Component.empty(), (btn) -> trade(r))
                    .bounds(x, y, TradeListLayout.TRADE_ITEM_W, TradeListLayout.TRADE_ITEM_HEIGHT)
                    .build();
            offerButtons[row].active = false;
            this.addRenderableWidget(offerButtons[row]);
            y += TradeListLayout.TRADE_ITEM_HEIGHT;
        }

        // 刷新交易按钮
        int btnX = this.leftPos + 108;
        int btnY = this.topPos + 2;
        refreshButton = new RefreshButton(btnX, btnY, 16, 16, (btn) -> sendRefresh());
        refreshButton.active = false;
        this.addRenderableWidget(refreshButton);
    }

    /**
     * 渲染界面背景。
     * <p>
     * 按界面尺寸将背景纹理绘制到居中位置。
     *
     * @param guiGraphics 图形上下文
     * @param partialTick 部分 tick 插值
     * @param mouseX      鼠标 X 坐标
     * @param mouseY      鼠标 Y 坐标
     */
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND,
                this.leftPos, this.topPos,
                0, 0,
                this.imageWidth, this.imageHeight,
                TradeListLayout.TEX_WIDTH, TradeListLayout.TEX_HEIGHT);
    }

    /**
     * 渲染界面标签。
     * <p>
     * 在顶部居中绘制标题，并在底部绘制玩家物品栏标题。
     *
     * @param guiGraphics 图形上下文
     * @param mouseX      鼠标 X 坐标
     * @param mouseY      鼠标 Y 坐标
     */
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x0 = 16, x1 = 83;
        int titleWidth = this.font.width(this.title);
        int titleX = x0 + (x1 - x0 - titleWidth) / 2;
        int titleY = 6;

        guiGraphics.drawString(this.font, this.title, titleX, titleY, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    /**
     * 主渲染方法。
     * <p>
     * 依次渲染背景、基类内容、交易条目、滚动条、VSS 余额与交易提示，
     * 最后渲染鼠标悬停提示。
     *
     * @param guiGraphics 图形上下文
     * @param mouseX      鼠标 X 坐标
     * @param mouseY      鼠标 Y 坐标
     * @param partialTick 部分 tick 插值
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderOffers(guiGraphics);
        renderScroller(guiGraphics);
        renderVssBalance(guiGraphics);
        renderOfferTooltip(guiGraphics, mouseX, mouseY);
        updateRefreshButton();

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * 渲染可见的交易条目。
     * <p>
     * 依据滚动偏移计算每行对应的交易索引，
     * 在范围内时启用按钮并绘制交易行，售罄的交易额外叠加半透明蒙层。
     *
     * @param g 图形上下文
     */
    private void renderOffers(GuiGraphics g) {
        List<MerchantOffer> offers = menu.getOffers();

        for (int row = 0; row < TradeListLayout.OFFER_COUNT; row++) {
            int index = scrollController.offset() + row;
            Button button = offerButtons[row];

            boolean inRange = index < offers.size();
            button.active = inRange;
            button.visible = inRange;
            if (!inRange) continue;

            MerchantOffer offer = offers.get(index);
            int x = button.getX();
            int y = button.getY();

            TradeOfferRenderer.renderRow(g, this.font, offer, x, y);

            // 售罄时叠加半透明蒙层
            if (offer.isOutOfStock()) {
                g.fill(x, y,
                        x + TradeListLayout.TRADE_ITEM_W,
                        y + TradeListLayout.TRADE_ITEM_HEIGHT,
                        0x80000000);
            }
        }
    }

    /**
     * 渲染滚动条。
     * <p>
     * 交易总数不超过可见行数时不绘制；
     * 否则依据当前偏移计算滑块位置与高度，并分层绘制滚动条与滑块。
     *
     * @param g 图形上下文
     */
    private void renderScroller(GuiGraphics g) {
        int total = menu.getOffers().size();
        if (total <= TradeListLayout.OFFER_COUNT) return;

        var geom = TradeListLayout.scrollerGeom(total, scrollController.offset());
        int x = this.leftPos + TradeListLayout.SCROLLER_X;
        int y = this.topPos  + geom.y();
        int h = geom.height();

        g.fill(x, y, x + TradeListLayout.SCROLLER_W, y + h, 0xFF000000);
        g.fill(x + 1, y + 1, x + TradeListLayout.SCROLLER_W - 1, y + h - 1, 0xFF8B8B8B);
        g.fill(x + 1, y + 1, x + TradeListLayout.SCROLLER_W - 1, y + 2, 0xFFC6C6C6);
    }

    /**
     * 渲染玩家 VSS 余额。
     * <p>
     * 在指定区域水平居中显示货币符号与格式化后的余额。
     *
     * @param g 图形上下文
     */
    private void renderVssBalance(GuiGraphics g) {
        int balance = menu.getVssBalance();
        Component text = Component.literal("◎ ")
                .append(NumberFormatter.format(balance));
        int textW = this.font.width(text);

        int regionX = 146, regionY = 7, regionW = 85;
        int textX = this.leftPos + regionX + (regionW - textW) / 2;
        int textY = this.topPos + regionY;

        g.drawString(this.font, text, textX, textY, 0xFF000000, false);
    }

    /**
     * 渲染交易条目的悬停提示。
     * <p>
     * 找到首个悬停且有效的交易条目后，构建并显示其提示信息，随后返回。
     *
     * @param g      图形上下文
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     */
    private void renderOfferTooltip(GuiGraphics g, int mouseX, int mouseY) {
        List<MerchantOffer> offers = menu.getOffers();

        for (int row = 0; row < TradeListLayout.OFFER_COUNT; row++) {
            int index = scrollController.offset() + row;
            Button button = offerButtons[row];

            if (!button.isHovered() || index >= offers.size()) continue;

            List<Component> tips = TradeTooltipRenderer.build(offers.get(index));
            g.renderComponentTooltip(this.font, tips, mouseX, mouseY);
            return;
        }
    }

    /**
     * 处理鼠标滚轮事件。
     * <p>
     * 交易总数超过可见行数时交由滚动控制器处理并消费该事件，
     * 否则调用父类处理。
     *
     * @param mouseX  鼠标 X 坐标
     * @param mouseY  鼠标 Y 坐标
     * @param scrollX 水平滚动量
     * @param scrollY 垂直滚动量
     * @return 事件是否被处理
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int total = menu.getOffers().size();
        if (total > TradeListLayout.OFFER_COUNT) {
            scrollController.onScroll(total, scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /**
     * 处理鼠标点击事件。
     * <p>
     * 若点击位置命中滚动条滑块，则开始拖动并消费该事件，
     * 否则调用父类处理。
     *
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @param button 鼠标按键
     * @return 事件是否被处理
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int relX = (int) mouseX - this.leftPos;
        int relY = (int) mouseY - this.topPos;

        if (scrollController.hitScroller(menu.getOffers().size(), relX, relY)) {
            scrollController.beginDrag();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * 处理鼠标拖动事件。
     * <p>
     * 处于拖动状态且为左键时，交由滚动控制器按鼠标位置更新偏移并消费该事件，
     * 否则调用父类处理。
     *
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @param button 鼠标按键
     * @param dragX  水平拖动量
     * @param dragY  垂直拖动量
     * @return 事件是否被处理
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {
        if (scrollController.isDragging() && button == 0) {
            scrollController.onDrag(menu.getOffers().size(), (int) mouseY - this.topPos);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    /**
     * 处理鼠标释放事件。
     * <p>
     * 处于拖动状态且为左键时结束拖动并消费该事件，
     * 否则调用父类处理。
     *
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @param button 鼠标按键
     * @return 事件是否被处理
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (scrollController.isDragging() && button == 0) {
            scrollController.endDrag();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * 发送交易请求。
     * <p>
     * 依据滚动偏移计算点击行对应的交易索引并校验范围，
     * 普通点击请求一次交易，按住 Shift 时请求不超过剩余库存与 64 次的交易。
     *
     * @param row 点击的行号
     */
    private void trade(int row) {
        int index = scrollController.offset() + row;
        List<MerchantOffer> offers = menu.getOffers();
        if (index < 0 || index >= offers.size()) return;

        MerchantOffer offer = offers.get(index);
        int remaining = offer.getMaxUses() - offer.getUses();

        boolean shift = hasShiftDown();
        int count = shift ? Math.min(remaining, 64) : 1;

        PacketDistributor.sendToServer(new TradeRequestPacket(index, count));
    }

    /**
     * 每帧更新刷新按钮状态：
     * 未锁交易 → 激活可点击；已锁 → 禁用变灰。
     */
    private void updateRefreshButton() {
        refreshButton.active = !isTradesLocked();
    }

    /**
     * 发送刷新交易请求到服务端。
     */
    private void sendRefresh() {
        PacketDistributor.sendToServer(new RefreshTradesPacket());
    }

    /**
     * 判断交易列表是否已锁（任一交易项已使用过）。
     * 用于控制刷新按钮的启用状态。
     */
    private boolean isTradesLocked() {
        for (MerchantOffer offer : menu.getOffers()) {
            if (offer.getUses() > 0) return true;
        }
        return false;
    }
}