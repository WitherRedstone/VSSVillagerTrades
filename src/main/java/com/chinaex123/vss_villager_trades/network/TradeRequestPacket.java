package com.chinaex123.vss_villager_trades.network;

import com.chinaex123.vss_villager_trades.VSSVillagerTrades;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 交易请求数据包（客户端→服务端）。
 * <p>
 * 客户端在玩家点击某个交易项后发送此数据包，
 * 携带交易在列表中的索引与请求次数，供服务端在村民交易菜单中执行对应交易。
 *
 * @param offerIndex 交易在列表中的索引
 * @param count      请求执行的交易次数
 */
public record TradeRequestPacket(int offerIndex, int count) implements CustomPacketPayload {

    /** 网络包类型标识 */
    public static final CustomPacketPayload.Type<TradeRequestPacket> TYPE =
            new CustomPacketPayload.Type<>(VSSVillagerTrades.id("trade_request"));

    /** 网络包编解码器，按字段顺序组合交易索引与交易次数 */
    public static final StreamCodec<ByteBuf, TradeRequestPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TradeRequestPacket::offerIndex,
            ByteBufCodecs.VAR_INT, TradeRequestPacket::count,
            TradeRequestPacket::new
    );

    /**
     * 获取网络包类型。
     *
     * @return 网络包类型标识
     */
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}