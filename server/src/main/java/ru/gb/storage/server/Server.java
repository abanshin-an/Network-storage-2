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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.storage.commons.Constant;
import ru.gb.storage.commons.handler.JSONDecoder;
import ru.gb.storage.commons.handler.JSONEncoder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger log = LogManager.getLogger(Server.class);
    private static Executor executor = null;
    private NioEventLoopGroup bossGroup = null;
    private NioEventLoopGroup workGroup = null;
    private static ServerConfig config = null;
    private static final UserDB user=new UserDB();

    public static ServerConfig getConfig() {
        return config;
    }

    public static UserDB getUser() {
        return user;
    }

    public static void main(String[] args) {
        config = ServerConfig.init(args);
        UserDB.init(config,false);
        executor = Executors.newCachedThreadPool();
        try {
            Server s = new Server();
            s.run();
        } catch (Exception e) {
            log.error(e);
            Thread.currentThread().interrupt();
        } finally {
            UserDB.close();
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
                            new ServerHandler(Server.this)
                    );
                }
            });
            serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128);
            ChannelFuture channelFuture = serverBootstrap.bind(config.getPort()).sync();
            log.info("Server started {} ",config);
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

    public Executor getExecutor() {
        return executor;
    }
}
