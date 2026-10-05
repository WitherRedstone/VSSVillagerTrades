package com.chinaex123.vss_villager_trades.network;

import com.chinaex123.vss_villager_trades.VSSVillagerTrades;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TradeRequestPacket(int offerIndex, int count) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TradeRequestPacket> TYPE =
            new CustomPacketPayload.Type<>(VSSVillagerTrades.id("trade_request"));

    public static final StreamCodec<ByteBuf, TradeRequestPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TradeRequestPacket::offerIndex,
            ByteBufCodecs.VAR_INT, TradeRequestPacket::count,
            TradeRequestPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}