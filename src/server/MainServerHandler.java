/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import network.Packet;

/**
 * Handler implementation for the echo server.
 */
@Sharable
public class MainServerHandler extends ChannelInboundHandlerAdapter {
    static final server.Channel channel = server.Channel.getInstance();
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[Connect] " + ctx.channel().remoteAddress());
        channel.getBroadCastChannel().add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[DisConnect] " + ctx.channel().remoteAddress());
        channel.getBroadCastChannel().remove(ctx.channel());
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Packet packet = (Packet) msg;
        Packet sendPacket = new Packet(packet.getDataLen());

        packet.copyTo(sendPacket);
        System.out.println("[Recv] " + ctx.channel().remoteAddress() + " " + packet);

        short header = packet.decodeShort();

        switch (header){
            case 0:
                broadcastChannelSend(sendPacket);
                break;

            default:
                channelSend(ctx.channel(), sendPacket);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        channel.getBroadCastChannel().remove(ctx.channel());
        ctx.close();
    }

    private void broadcastChannelSend(Packet packet){
        for (Channel c: channel.getBroadCastChannel()) {
            channelSend(c, packet);
        }
    }

    private void channelSend(Channel channel, Packet packet){
        System.out.println("[Send] " + channel.remoteAddress() + " " + packet);
        channel.writeAndFlush(packet.toArray());
    }
}