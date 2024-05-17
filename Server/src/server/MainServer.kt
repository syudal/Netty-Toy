package server
import database.Database
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import network.Codec

object MainServer {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Load the initial instance of the Database
        Database.getInstance().load();

        // Configure the server.
        val bossGroup: EventLoopGroup = NioEventLoopGroup(1)
        val workerGroup: EventLoopGroup = NioEventLoopGroup()
        val serverHandler = MainServerHandler()
        try {
            val b = ServerBootstrap()
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .handler(LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.SO_BACKLOG, 100) // 동접 인원
                .childOption(ChannelOption.TCP_NODELAY, true) // Nagle Algorithm 비활성화 여부
                .childHandler(object : ChannelInitializer<SocketChannel>() {

                    @Throws(Exception::class)
                    public override fun initChannel(ch: SocketChannel) {
                        val p = ch.pipeline()
                        p.addLast(Codec.PacketDecoder())
                        p.addLast(Codec.PacketEncoder())
                        p.addLast(serverHandler)
                    }
                })

            // Start the server.
            val f = b.bind(Setting().serverPort).sync()

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync()
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}