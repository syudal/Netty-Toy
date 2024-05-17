package server

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import network.Packet

@Sharable
class MainServerHandler : ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        println("[Connect] " + ctx.channel().remoteAddress())
    }


    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        println("[DisConnect] " + ctx.channel().remoteAddress())
        ctx.channel().close()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        val packet: Packet = msg as Packet
        val sendPacket = Packet(packet.dataLen)

        packet.copyTo(sendPacket)
        println("[Recv] " + ctx.channel().remoteAddress() + " " + packet)

        val header: Int = packet.decodeShort().toInt();

        when (header) {
            0 -> broadcastChannelSend(sendPacket)
            1 -> {
                val ret = Packet()
                var login = false

                val id: String = packet.decodeString()
                val pw: String = packet.decodeString()

                if (id == "admin" && pw == "admin") {
                    login = true
                }

                ret.encodeShort(header)
                ret.encodeBool(login)

                channelSend(ctx.channel(), ret)
            }

            else -> channelSend(ctx.channel(), sendPacket)
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        // Close the connection when an exception is raised.
        cause.printStackTrace()
        ctx.close()
    }

    private fun broadcastChannelSend(packet: Packet) {
    }

    private fun channelSend(channel: Channel, packet: Packet) {
        println("[Send] " + channel.remoteAddress() + " " + packet)
        channel.writeAndFlush(packet.toArray())
    }
}