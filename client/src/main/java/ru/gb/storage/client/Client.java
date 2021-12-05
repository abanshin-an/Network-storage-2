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
import java.util.Timer;
import java.util.TimerTask;

public class Client {
    private final ClientConfig config;
    private final Bootstrap bootstrap = new Bootstrap();
    private final Timer timer = new Timer();
    private String recvPath = "";
    private volatile boolean finished = false;
    private ChannelHandlerContext ctx;
    private Channel channel;

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

    public void stop() throws InterruptedException {
        finished = true;
        if (ctx != null) {
            ctx.channel().close().sync();
        }
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

    public void run() {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
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
            scheduleConnect(10);
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


    private void doConnect() {
        try {
            ChannelFuture f = bootstrap.connect(config.getHost(), config.getPort());
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {//if is not successful, reconnect
                        future.channel().close();
                        bootstrap.connect(config.getHost(), config.getPort()).addListener(this);
                    } else {//good, the connection is ok
                        channel = future.channel();
//add a listener to detect the connection lost
                        addCloseDetectListener(channel);
                        connectionEstablished();
                    }
                }

                private void addCloseDetectListener(Channel channel) {
//if the channel connection is lost, the ChannelFutureListener.operationComplete() will be called
                    channel.closeFuture().addListener((ChannelFutureListener) future -> {
                        connectionLost();
                        scheduleConnect(5);
                    });
                }
            });
        } catch (Exception ex) {
            scheduleConnect(1000);
        }
    }

    public void connectionLost() {
        System.out.println("connectionLost()");
    }

    public void connectionEstablished() {
        System.out.println("connectionLost()");
    }

    private void scheduleConnect(long millis) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                doConnect();
            }
        }, millis);
    }

}
