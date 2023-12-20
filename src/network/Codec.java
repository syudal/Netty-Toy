package network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.Arrays;
import java.util.List;

public final class Codec {
    public static class PacketDecoder extends ReplayingDecoder<Void> {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            int length = in.readInt();

            byte[] src = new byte[length];
            in.readBytes(src);

            Packet packet = new Packet();
            packet.setDataLen(length);
            packet.rawAppendBuffer(Unpooled.wrappedBuffer(src), length);
            out.add(packet);
        }
    }

    public static class PacketEncoder extends MessageToByteEncoder<byte[]> {

        @Override
        protected void encode(ChannelHandlerContext ctx, byte[] message, ByteBuf out) throws Exception {

            out.writeInt(message.length);
            out.writeBytes(Arrays.copyOf(message, message.length));
        }
    }
}