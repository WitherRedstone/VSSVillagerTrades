package com.chinaex123.vss_villager_trades.network;

import com.chinaex123.vss_villager_trades.client.menu.VillagerTradesMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 模组网络管理类。
 * <p>
 * 负责注册本模组的网络数据包及其处理逻辑，包括：
 * 服务端到客户端的余额同步与村民交易列表同步，
 * 以及客户端到服务端的交易请求。
 */
public final class VVTNetwork {

    /** 网络协议版本号 */
    private static final String PROTOCOL_VERSION = "1.0";

    /**
     * 私有构造函数，防止实例化。
     * <p>
     * 该类只提供静态方法，不需要实例。
     */
    private VVTNetwork() {}

    /**
     * 初始化网络模块。
     * <p>
     * 在模组事件总线上监听负载处理器注册事件。
     *
     * @param modEventBus 模组事件总线
     */
    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(VVTNetwork::onRegisterPayloadHandlers);
    }

    /**
     * 注册网络数据包处理器。
     * <p>
     * 以协议版本创建注册器，并分别注册服务端到客户端、客户端到服务端的数据包。
     *
     * @param event 负载处理器注册事件
     */
    private static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL_VERSION);

        // ========== 服务端 -> 客户端 ==========
        registrar.playToClient(
                VSSBalancePacket.TYPE,
                VSSBalancePacket.STREAM_CODEC,
                VVTNetwork::handleVssBalanceSync
        );
        registrar.playToClient(
                VillagerOffersPacket.TYPE,
                VillagerOffersPacket.STREAM_CODEC,
                VVTNetwork::handleVillagerOffersSync
        );

        // ========== 客户端 -> 服务端 ==========
        registrar.playToServer(
                TradeRequestPacket.TYPE,
                TradeRequestPacket.STREAM_CODEC,
                VVTNetwork::handleTradeRequest
        );
        registrar.playToServer(
                RefreshTradesPacket.TYPE,
                RefreshTradesPacket.STREAM_CODEC,
                VVTNetwork::handleRefreshTrades
        );
    }

    /**
     * 处理 VSS 余额同步。
     * <p>
     * 若当前打开的菜单为村民交易菜单，则将收到的余额写入该菜单。
     *
     * @param packet  余额同步数据包
     * @param context 上下文
     */
    private static void handleVssBalanceSync(VSSBalancePacket packet, IPayloadContext context) {
        AbstractContainerMenu menu = Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.containerMenu
                : null;
        if (menu instanceof VillagerTradesMenu vtm) {
            vtm.setVssBalance(packet.balance());
        }
    }

    /**
     * 处理村民交易列表同步。
     * <p>
     * 在主线程中将收到的交易列表与余额写入村民交易菜单。
     *
     * @param packet  交易列表同步数据包
     * @param context 上下文
     */
    private static void handleVillagerOffersSync(VillagerOffersPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbstractContainerMenu menu = Minecraft.getInstance().player != null
                    ? Minecraft.getInstance().player.containerMenu
                    : null;
            if (menu instanceof VillagerTradesMenu vtm) {
                vtm.setOffers(packet.offers());
                vtm.setVssBalance(packet.balance());
            }
        });
    }

    /**
     * 处理交易请求。
     * <p>
     * 在主线程中校验发送者当前菜单，若为村民交易菜单则执行对应序号的交易。
     *
     * @param packet  交易请求数据包
     * @param context 上下文
     */
    private static void handleTradeRequest(TradeRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player sender = context.player();
            AbstractContainerMenu menu = sender.containerMenu;
            if (menu instanceof VillagerTradesMenu vtm) {
                vtm.executeTrade(packet.offerIndex(), packet.count(), sender);
            }
        });
    }

    /**
     * 处理刷新交易请求。
     * <p>
     * 仅在主线程中执行，调用菜单的刷新方法。
     *
     * @param packet  刷新请求数据包
     * @param context 上下文
     */
    private static void handleRefreshTrades(RefreshTradesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player sender = context.player();
            AbstractContainerMenu menu = sender.containerMenu;
            if (menu instanceof VillagerTradesMenu vtm) {
                vtm.refreshTrades(sender);
            }
        });
    }
}