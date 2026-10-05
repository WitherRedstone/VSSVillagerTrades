package com.chinaex123.vss_villager_trades.network;

import com.chinaex123.vss_villager_trades.VSSVillagerTrades;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * VSS 余额同步数据包（服务端→客户端）。
 * <p>
 * 服务端在玩家余额发生变化时发送此数据包，
 * 客户端收到后将余额更新到当前打开的村民交易菜单中。
 *
 * @param balance 玩家当前 VSS 余额
 */
public record VSSBalancePacket(int balance) implements CustomPacketPayload {

    /** 网络包类型标识 */
    public static final CustomPacketPayload.Type<VSSBalancePacket> TYPE =
            new CustomPacketPayload.Type<>(VSSVillagerTrades.id("balance_sync"));

    /** 网络包编解码器，仅携带余额字段 */
    public static final StreamCodec<ByteBuf, VSSBalancePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            VSSBalancePacket::balance,
            VSSBalancePacket::new
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