package ru.gb.storage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import ru.gb.storage.commons.Constant;
import ru.gb.storage.commons.handler.JSONDecoder;
import ru.gb.storage.commons.handler.JSONEncoder;

import java.io.File;

public class Client implements Runnable {
    private final ClientConfig config;
    private String recvPath = "";
    private volatile boolean finished = false;
    private ChannelHandlerContext ctx;

    public Client(ClientConfig config) {
        this.config = config;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public boolean isFinished() {
        return finished;
    }

    public void stop() {
// once having an event in your handler (EchoServerHandler)
// Close the current channel
        ctx.channel().close();
// Then close the parent channel (the one attached to the bind)
        ctx.channel().parent().close();
    }

    public String getRecvPath() {
        return recvPath;
    }

    public void setRecvPath(String recvPath) {
        StringBuilder sb = new StringBuilder(recvPath);
        if ((recvPath.length() == 0) || recvPath.charAt(recvPath.length() - 1) != File.separatorChar) {
            sb.append(File.separatorChar);
        }
        this.recvPath = sb.toString();
    }

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) {
                            System.out.println("channelActive");
                        }

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) {
                            System.out.println("channelInactive");
                            finished = true;
                        }

                        @Override
                        public void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(Constant.FRAME_SIZE, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new JSONDecoder(),
                                    new JSONEncoder(),
                                    new ClientHandler(Client.this)
                            );
                        }
                    });
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = bootstrap.connect(config.getHost(), config.getPort()).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();

            }
        }
    }

}
