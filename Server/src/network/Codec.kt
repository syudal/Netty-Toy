package network

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.ReplayingDecoder


class Codec {
    class PacketDecoder : ReplayingDecoder<Void?>() {
        override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
            val length = `in`.readInt()

            val src = ByteArray(length)
            `in`.readBytes(src)

            val packet = Packet(length)
            packet.dataLen = length
            packet.rawAppendBuffer(Unpooled.wrappedBuffer(src), length)
            out.add(packet)
        }
    }

    class PacketEncoder : MessageToByteEncoder<ByteArray>() {
        override fun encode(ctx: ChannelHandlerContext, message: ByteArray, out: ByteBuf) {
            out.writeInt(message.size)
            out.writeBytes(message.copyOf(message.size))
        }
    }
}