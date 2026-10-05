package com.chinaex123.vss_villager_trades.client.gui;

import com.chinaex123.vss_villager_trades.VSSVillagerTrades;
import com.chinaex123.vss_villager_trades.client.menu.VillagerTradesMenu;
import com.chinaex123.vss_villager_trades.network.TradeRequestPacket;
import com.chinaex123.vss_villager_trades.util.NumberFormatter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class VillagerTradesScreen extends AbstractContainerScreen<VillagerTradesMenu> {
    public static final ResourceLocation BACKGROUND = VSSVillagerTrades.id("textures/gui/villager_trades.png");

    private final Button[] offerButtons = new Button[TradeListLayout.OFFER_COUNT];
    private final TradeScrollController scrollController = new TradeScrollController();

    public VillagerTradesScreen(VillagerTradesMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth  = TradeListLayout.GUI_WIDTH;
        this.imageHeight = TradeListLayout.GUI_HEIGHT;
        this.inventoryLabelX = 111;
        this.inventoryLabelY = this.imageHeight - 91;
    }

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
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND,
                this.leftPos, this.topPos,
                0, 0,
                this.imageWidth, this.imageHeight,
                TradeListLayout.TEX_WIDTH, TradeListLayout.TEX_HEIGHT);
    }

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

    // ================== 主渲染 ==================

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderOffers(guiGraphics);
        renderScroller(guiGraphics);
        renderVssBalance(guiGraphics);
        renderOfferTooltip(guiGraphics, mouseX, mouseY);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

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

            if (offer.isOutOfStock()) {
                g.fill(x, y,
                        x + TradeListLayout.TRADE_ITEM_W,
                        y + TradeListLayout.TRADE_ITEM_HEIGHT,
                        0x80000000);
            }
        }
    }

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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int total = menu.getOffers().size();
        if (total > TradeListLayout.OFFER_COUNT) {
            scrollController.onScroll(total, scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

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

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {
        if (scrollController.isDragging() && button == 0) {
            scrollController.onDrag(menu.getOffers().size(), (int) mouseY - this.topPos);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (scrollController.isDragging() && button == 0) {
            scrollController.endDrag();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // ================== 交易发包 ==================

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
}