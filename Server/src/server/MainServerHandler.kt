package server

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.group.ChannelGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import network.Packet

@Sharable
class MainServerHandler : ChannelInboundHandlerAdapter() {
    private val channelGroup: ChannelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)

    override fun channelActive(ctx: ChannelHandlerContext) {
        println("[Connect] " + ctx.channel().remoteAddress())
        channelGroup.add(ctx.channel())
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        println("[DisConnect] " + ctx.channel().remoteAddress())
        channelGroup.remove(ctx.channel())
        ctx.channel().close()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        val packet: Packet = msg as Packet
        val sendPacket = Packet(packet.dataLen)

        packet.copyTo(sendPacket)
        println("[Recv] " + ctx.channel().remoteAddress() + " " + packet)

        when (val header: Int = packet.decodeShort().toInt()) {
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
        for (channel in channelGroup) {
            channelSend(channel, packet)
        }
    }

    private fun channelSend(channel: Channel, packet: Packet) {
        println("[Send] " + channel.remoteAddress() + " " + packet)
        channel.writeAndFlush(packet.toArray())
    }
}