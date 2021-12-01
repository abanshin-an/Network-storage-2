package ru.gb.storage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import ru.gb.storage.commons.Constant;
import ru.gb.storage.commons.handler.JSONDecoder;
import ru.gb.storage.commons.handler.JSONEncoder;
import ru.gb.storage.commons.message.CommandMessage;
import ru.gb.storage.commons.message.Message;
import ru.gb.storage.commons.utils.CommandUtils;
import ru.gb.storage.commons.utils.FileUtils;

import static ru.gb.storage.commons.Constant.STORAGE_ROOT;

public class Server {
    private static final int PORT = 9000;
    NioEventLoopGroup bossGroup = null;
    NioEventLoopGroup workGroup = null;
    ChannelFuture channelFuture = null;

    public static void main(String[] args) throws Exception {
        Server s = new Server();
        s.run();
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
                            new LengthFieldBasedFrameDecoder(64 * 1024, 0, 2, 0, 2),
                            new LengthFieldPrepender(2),
                            new JSONDecoder(),
                            new JSONEncoder(),
                            new SimpleChannelInboundHandler<Message>() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    System.out.println("channel Active");
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, Message msg)  {
                                    System.out.println(msg);
                                    if (msg instanceof CommandMessage) {
                                        var commandMessage = (CommandMessage) msg;
                                        switch (CommandUtils.getOrder(commandMessage)) {
                                            case Constant.ECHO:
                                                var echo = new CommandMessage(commandMessage.getCommand());
                                                ctx.writeAndFlush(echo);
                                                break;
                                            case Constant.GET:
                                                FileUtils.sendFile(ctx, Constant.getProperty(STORAGE_ROOT) + CommandUtils.getArgument(commandMessage));
                                                break;
                                            case Constant.BYE:
                                                // Close the current channel
                                                ctx.channel().close();
                                                break;
                                            case Constant.SD:
                                                // Close the current channel
                                                ctx.channel().close();
                                                // Then close the parent channel (the one attached to the bind)
                                                ctx.channel().parent().close();
                                                stop();
                                                break;
                                            default:
                                                var wrongCommand = new CommandMessage("Wrong command - " + commandMessage.getCommand());
                                                ctx.writeAndFlush(wrongCommand);
                                        }
                                    }
                                }

                            }
                    );
                }
            });
            serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128);
            channelFuture = serverBootstrap.bind(PORT).sync();
            System.out.println("Server started");
            System.out.println("Storage root:" + Constant.getProperty(STORAGE_ROOT));
            channelFuture.channel().closeFuture().sync();   // close port
        } finally {
            bossGroup.shutdownGracefully().sync();
            workGroup.shutdownGracefully().sync();
        }
    }

    public void stop()  {
        // shutdown EventLoopGroup
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}
