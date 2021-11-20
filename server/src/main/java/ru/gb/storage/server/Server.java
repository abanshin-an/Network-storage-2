package ru.gb.storage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;


public class Server {
    private static final int PORT = 9000;
    public static void main(String[] args) throws Exception {
        Server s = new Server();
        s.run();
    }

    public void run() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(512, 0, 2, 0, 2),
                                    new LengthFieldPrepender(2),
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new SimpleChannelInboundHandler<String>() {
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx)  {
                                            System.out.println("channel Active");
                                        }

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, String s)  {
                                            var m ="Server echo :" + s;
                                            System.out.println(m);
                                            ctx.writeAndFlush(m);
                                        }

                                    }
                            );
                        }
                    });
            serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128);
            Channel channel = serverBootstrap.bind(PORT).sync().channel();
            System.out.println("Server started");
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }


}
