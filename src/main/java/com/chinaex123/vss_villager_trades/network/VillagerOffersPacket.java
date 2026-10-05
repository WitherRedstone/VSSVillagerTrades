package com.chinaex123.vss_villager_trades.network;

import com.chinaex123.vss_villager_trades.VSSVillagerTrades;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;

/**
 * 村民交易列表同步数据包（服务端→客户端）。
 * <p>
 * 服务端在村民交易数据发生变化时发送此数据包，
 * 客户端收到后将交易列表与玩家余额一并更新到当前打开的村民交易菜单中。
 *
 * @param offers  村民的交易列表
 * @param balance 玩家当前 VSS 余额
 */
public record VillagerOffersPacket(List<MerchantOffer> offers, int balance)
        implements CustomPacketPayload {

    /** 网络包类型标识 */
    public static final CustomPacketPayload.Type<VillagerOffersPacket> TYPE =
            new CustomPacketPayload.Type<>(VSSVillagerTrades.id("villager_offers"));

    /** 网络包编解码器，按字段顺序组合交易列表与余额 */
    public static final StreamCodec<RegistryFriendlyByteBuf, VillagerOffersPacket> STREAM_CODEC =
            StreamCodec.composite(
                    MerchantOffer.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    VillagerOffersPacket::offers,
                    ByteBufCodecs.VAR_INT,
                    VillagerOffersPacket::balance,
                    VillagerOffersPacket::new
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