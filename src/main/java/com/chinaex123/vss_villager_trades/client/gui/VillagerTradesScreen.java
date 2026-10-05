package com.chinaex123.vss_villager_trades.client.gui;

import com.chinaex123.vss_villager_trades.VSSVillagerTrades;
import com.chinaex123.vss_villager_trades.client.menu.VillagerTradesMenu;
import com.chinaex123.vss_villager_trades.event.VillagerShopManager;
import com.chinaex123.vss_villager_trades.network.TradeRequestPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class VillagerTradesScreen extends AbstractContainerScreen<VillagerTradesMenu> {

    public static final ResourceLocation BACKGROUND = VSSVillagerTrades.id("textures/gui/villager_trades.png");

    private static final int GUI_WIDTH  = 276;
    private static final int GUI_HEIGHT = 166;

    private static final int LEFT_AREA_X = 5;
    private static final int LEFT_AREA_Y = 18;
    private static final int TRADE_ITEM_HEIGHT = 20;
    private static final int TRADE_ITEM_W = 88;
    private static final int OFFER_COUNT = 7;

    private static final int SCROLLER_X = 94;
    private static final int SCROLLER_Y = 18;
    private static final int SCROLLER_W = 6;
    private static final int SCROLLER_H = 139;

    private static final int TEX_WIDTH  = 512;
    private static final int TEX_HEIGHT = 256;

    private final Button[] offerButtons = new Button[OFFER_COUNT];
    private int scrollOffset = 0;
    private boolean scrolling = false;

    public VillagerTradesScreen(VillagerTradesMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth  = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelX = 111;
        this.inventoryLabelY = this.imageHeight - 91;
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos + LEFT_AREA_X;
        int y = this.topPos  + LEFT_AREA_Y;

        for (int row = 0; row < OFFER_COUNT; row++) {
            final int r = row;
            offerButtons[row] = Button.builder(Component.empty(), (btn) -> trade(r))
                    .bounds(x, y, TRADE_ITEM_W, TRADE_ITEM_HEIGHT)
                    .build();
            offerButtons[row].active = false;
            this.addRenderableWidget(offerButtons[row]);
            y += TRADE_ITEM_HEIGHT;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND,
                this.leftPos, this.topPos,
                0, 0,
                this.imageWidth, this.imageHeight,
                TEX_WIDTH, TEX_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x0 = 16, x1 = 83;
        int titleWidth = this.font.width(this.title);
        int titleX = x0 + (x1 - x0 - titleWidth) / 2;
        int titleY = 6;

        guiGraphics.drawString(this.font, this.title, titleX, titleY, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

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

        for (int row = 0; row < OFFER_COUNT; row++) {
            int index = scrollOffset + row;
            Button button = offerButtons[row];

            boolean inRange = index < offers.size();
            button.active = inRange;
            button.visible = inRange;

            if (!inRange) continue;

            MerchantOffer offer = offers.get(index);
            int x = button.getX();
            int y = button.getY();
            boolean out = offer.isOutOfStock();

            if (VillagerShopManager.isSellOffer(offer)) {
                // 卖：物品 → VSS
                ItemStack need = VillagerShopManager.getNonEmeraldCost(offer);
                if (!need.isEmpty()) {
                    g.renderItem(need, x + 4, y + 2);
                    g.renderItemDecorations(this.font, need, x + 4, y + 2);
                }

                g.drawString(this.font, "→", x + 50, y + 6, 0xFFCCCCCC, false);

                int reward = VillagerShopManager.emeraldRewardFromSell(offer);
                // 货币位置
                g.drawString(this.font, priceText(reward), x + 63, y + 6, 0xFF55FF55, false);
            } else {
                // 买：VSS → 物品
                ItemStack need = VillagerShopManager.getNonEmeraldCost(offer);
                if (!need.isEmpty()) {
                    g.renderItem(need, x + 4, y + 2);
                    g.renderItemDecorations(this.font, need, x + 4, y + 2);
                }

                int cost = VillagerShopManager.emeraldCostToVSS(offer);
                if (cost >= 0) {
                    // 货币位置
                    g.drawString(this.font, priceText(cost), x + 22, y + 6, 0xFFFFAA00, false);
                } else {
                    g.drawString(this.font, "?", x + 24, y + 6, 0xFF888888, false);
                }

                g.drawString(this.font, "→", x + 50, y + 6, 0xFFCCCCCC, false);

                g.renderItem(offer.getResult(), x + 62, y + 2);
                g.renderItemDecorations(this.font, offer.getResult(), x + 62, y + 2);
            }

            if (out) {
                g.fill(x, y, x + TRADE_ITEM_W, y + TRADE_ITEM_HEIGHT, 0x80000000);
            }
        }
    }

    private void renderScroller(GuiGraphics g) {
        List<MerchantOffer> offers = menu.getOffers();
        int total = offers.size();
        if (total <= OFFER_COUNT) return;

        int maxScroll = total - OFFER_COUNT;
        int scrollerH = Math.max(27, (int) ((float) OFFER_COUNT / total * SCROLLER_H));
        int maxScrollPx = SCROLLER_H - scrollerH;
        int scrollerY = SCROLLER_Y + (int) ((float) scrollOffset / maxScroll * maxScrollPx);

        int x = this.leftPos + SCROLLER_X;
        int y = this.topPos  + scrollerY;

        g.fill(x, y, x + SCROLLER_W, y + scrollerH, 0xFF000000);
        g.fill(x + 1, y + 1, x + SCROLLER_W - 1, y + scrollerH - 1, 0xFF8B8B8B);
        g.fill(x + 1, y + 1, x + SCROLLER_W - 1, y + 2, 0xFFC6C6C6);
    }

    private void renderVssBalance(GuiGraphics g) {
        int left = this.leftPos;
        int top  = this.topPos;

        int balance = menu.getVssBalance();
        Component text = Component.literal("◎ ")
                .append(formatNumber(balance));
        int textW   = this.font.width(text);

        int regionX = 146;
        int regionY = 7;
        int regionW = 85;

        int textX = left + regionX + (regionW - textW) / 2;
        int textY = top + regionY;

        g.drawString(this.font, text, textX, textY, 0xFF000000, false);
    }

    // ================== Tooltip ==================

    private void renderOfferTooltip(GuiGraphics g, int mouseX, int mouseY) {
        List<MerchantOffer> offers = menu.getOffers();

        for (int row = 0; row < OFFER_COUNT; row++) {
            int index = scrollOffset + row;
            Button button = offerButtons[row];

            if (!button.isHovered() || index >= offers.size()) continue;

            MerchantOffer offer = offers.get(index);
            List<Component> tips = new ArrayList<>();

            if (VillagerShopManager.isSellOffer(offer)) {
                // 卖：物品 → VSS
                ItemStack need = VillagerShopManager.getNonEmeraldCost(offer);
                tips.add(Component.translatable(
                        "tooltip.vss_villager_trades.sell",
                        need.getHoverName(),
                        need.getCount()
                ));
                tips.add(Component.translatable(
                        "tooltip.vss_villager_trades.sell_reward",
                        priceText(VillagerShopManager.emeraldRewardFromSell(offer))
                ));
            } else {
                // 买：VSS → 物品
                tips.add(offer.getResult().getHoverName());
                ItemStack need = VillagerShopManager.getNonEmeraldCost(offer);
                if (!need.isEmpty()) {
                    tips.add(Component.translatable(
                            "tooltip.vss_villager_trades.need",
                            need.getHoverName(),
                            need.getCount()
                    ));
                }
                int vss = VillagerShopManager.emeraldCostToVSS(offer);
                if (vss >= 0) {
                    tips.add(Component.translatable(
                            "tooltip.vss_villager_trades.buy_price",
                            priceText(vss)
                    ));
                } else {
                    tips.add(Component.translatable(
                            "tooltip.vss_villager_trades.no_price"
                    ));
                }
            }

            if (offer.isOutOfStock()) {
                tips.add(Component.translatable(
                        "tooltip.vss_villager_trades.out_of_stock"
                ).withStyle(ChatFormatting.RED));
            } else {
                tips.add(Component.translatable(
                        "tooltip.vss_villager_trades.uses",
                        (offer.getMaxUses() - offer.getUses()),
                        offer.getMaxUses()
                ).withStyle(ChatFormatting.GRAY));
            }

            tips.add(Component.literal(""));
            tips.add(Component.translatable(
                    "tooltip.vss_villager_trades.click_to_trade"
            ).withStyle(ChatFormatting.YELLOW));

            g.renderComponentTooltip(this.font, tips, mouseX, mouseY);
            return;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double scrollX, double scrollY) {
        List<MerchantOffer> offers = menu.getOffers();
        int total = offers.size();

        if (total > OFFER_COUNT) {
            scrollOffset = Mth.clamp(
                    (int) (scrollOffset - scrollY),
                    0, total - OFFER_COUNT);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int relX = (int) mouseX - this.leftPos;
        int relY = (int) mouseY - this.topPos;

        List<MerchantOffer> offers = menu.getOffers();
        int total = offers.size();

        if (total > OFFER_COUNT
                && relX >= SCROLLER_X && relX < SCROLLER_X + SCROLLER_W
                && relY >= SCROLLER_Y && relY < SCROLLER_Y + SCROLLER_H) {
            scrolling = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {
        if (scrolling && button == 0) {
            List<MerchantOffer> offers = menu.getOffers();
            int maxScroll = offers.size() - OFFER_COUNT;
            int scrollerH = Math.max(27, (int) ((float) OFFER_COUNT / offers.size() * SCROLLER_H));
            int maxScrollPx = SCROLLER_H - scrollerH;

            if (maxScrollPx > 0) {
                int relY = (int) mouseY - this.topPos;
                float ratio = (float) (relY - SCROLLER_Y - scrollerH / 2) / maxScrollPx;
                scrollOffset = Mth.clamp(
                        Math.round(ratio * maxScroll), 0, maxScroll);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (scrolling && button == 0) {
            scrolling = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void trade(int row) {
        int index = scrollOffset + row;
        if (index < 0 || index >= menu.getOffers().size()) return;

        MerchantOffer offer = menu.getOffers().get(index);
        int remaining = offer.getMaxUses() - offer.getUses();

        boolean shift = hasShiftDown();
        int count = shift ? Math.min(remaining, 64) : 1;

        PacketDistributor.sendToServer(new TradeRequestPacket(index, count));
    }

    private Component priceText(int amount) {
        return Component.translatable("tooltip.vss_villager_trades.symbol.currency")
                .append(formatNumber(amount));
    }

    private static String formatNumber(long value) {
        long abs = Math.abs(value);
        String sign = value < 0 ? "-" : "";

        if (abs >= 1_000_000_000_000L) {
            return sign + trimZero(abs / 1_000_000_000_000.0) + "T";
        }
        if (abs >= 1_000_000_000L) {
            return sign + trimZero(abs / 1_000_000_000.0) + "B";
        }
        if (abs >= 1_000_000L) {
            return sign + trimZero(abs / 1_000_000.0) + "M";
        }
        if (abs >= 1_000L) {
            return sign + trimZero(abs / 1_000.0) + "K";
        }
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