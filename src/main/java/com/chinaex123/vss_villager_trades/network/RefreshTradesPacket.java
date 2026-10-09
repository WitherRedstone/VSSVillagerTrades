package com.chinaex123.vss_villager_trades.network;

import com.chinaex123.vss_villager_trades.VSSVillagerTrades;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 刷新交易请求数据包（客户端→服务端）。
 * <p>
 * 客户端在玩家点击"刷新交易"按钮后发送此数据包，
 * 服务端判断村民是否已锁交易，未锁则重建交易列表并同步回客户端。
 */
public record RefreshTradesPacket() implements CustomPacketPayload {

    /** 网络包类型标识 */
    public static final CustomPacketPayload.Type<RefreshTradesPacket> TYPE =
            new CustomPacketPayload.Type<>(VSSVillagerTrades.id("refresh_trades"));

    /** 空编解码器，没有字段 */
    public static final StreamCodec<ByteBuf, RefreshTradesPacket> STREAM_CODEC =
            StreamCodec.unit(new RefreshTradesPacket());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}