package ru.gb.storage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import ru.gb.storage.commons.Constant;
import ru.gb.storage.commons.handler.JSONDecoder;
import ru.gb.storage.commons.handler.JSONEncoder;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
    private static ThreadPoolExecutor executor = null;
    private NioEventLoopGroup bossGroup = null;
    private NioEventLoopGroup workGroup = null;
    private static ServerConfig serverConfig = null;
    public static void main(String[] args) {
        serverConfig = ServerConfig.init(args);
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        try {
            Server s = new Server();
            s.run();
        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            try {
                executor.shutdown();
                executor.awaitTermination(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void run() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) {
                    ch.pipeline().addLast(
                            new LengthFieldBasedFrameDecoder(Constant.FRAME_SIZE, 0, 3, 0, 3),
                            new LengthFieldPrepender(3),
                            new JSONDecoder(),
                            new JSONEncoder(),
                            new ServerHandler(executor,serverConfig)
                    );
                }
            });
            serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128);
            ChannelFuture channelFuture = serverBootstrap.bind(serverConfig.getPort()).sync();
            System.out.println("Server started");
            System.out.println(serverConfig.toString());
            channelFuture.channel().closeFuture().sync();   // close port
        } finally {
            bossGroup.shutdownGracefully().sync();
            workGroup.shutdownGracefully().sync();
        }
    }

    public void stop() {
        // shutdown EventLoopGroup
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}
