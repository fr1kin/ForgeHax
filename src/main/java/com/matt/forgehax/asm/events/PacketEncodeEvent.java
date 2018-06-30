package com.matt.forgehax.asm.events;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created by Babbaj on 3/28/2018.
 *
 * {@link net.minecraft.network.NettyPacketEncoder#encode(ChannelHandlerContext, Packet, ByteBuf)}
 *
 * This event is fired when the packet data is written to a PacketBuffer by the packet's writePacketData function.
 *
 * This event should only be used if you want to mess with the data that is sent to the server, otherwise use
 * {@link PacketEvent.Outgoing}
 *
 * Cancel to prevent the packet's encode function from being called.
 */
@Cancelable
public class PacketEncodeEvent extends Event {
    private final Packet<?> packet;
    private final PacketBuffer packetBuffer;

    public PacketEncodeEvent(Packet<?> packetIn, PacketBuffer buffer) {
        this.packet = packetIn;
        this.packetBuffer = buffer;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    public PacketBuffer getPacketBuffer() {
        return this.packetBuffer;
    }
}
