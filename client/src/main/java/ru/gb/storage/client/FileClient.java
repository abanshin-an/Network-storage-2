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
import ru.gb.storage.commons.message.CommandMessage;
import ru.gb.storage.commons.message.FileMessage;
import ru.gb.storage.commons.message.Message;
import ru.gb.storage.commons.utils.CommandUtils;
import ru.gb.storage.commons.utils.FileUtils;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;


public class FileClient {
    private static final int PORT = 9000;
    private static final String HOST = "localhost";
    private static final AtomicBoolean stop = new AtomicBoolean(false);

    public static void main(String[] args) throws Exception {
        FileClient ec = new FileClient();
        ec.run();
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        public void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(64*1024, 0, 2, 0, 2),
                                    new LengthFieldPrepender(2),
                                    new JSONDecoder(),
                                    new JSONEncoder(),
                                    new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
                                            System.out.println("Client incoming message :" + msg);
                                            if (msg instanceof CommandMessage) {
                                                var commandMessage = (CommandMessage) msg;
                                                switch (CommandUtils.getOrder(commandMessage)) {
                                                    case Constant.ECHO:
                                                        System.out.println("Server echo:"+CommandUtils.getArgument(commandMessage));
                                                        break;
                                                    case Constant.GET:
                                                        break;
                                                    case Constant.BYE:
                                                        stop.set(true);
                                                        break;
                                                    default:
                                                        System.out.println("Server answer:"+CommandUtils.getArgument(commandMessage));
                                                }
                                            } else if (msg instanceof FileMessage) {
                                                var fileMessage = (FileMessage) msg;
                                                FileUtils.recvFile(fileMessage);
                                            }
                                        }
                                    }
                            );
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            Channel channel = bootstrap.connect(HOST, PORT).sync().channel();
            Scanner scanner = new Scanner(System.in);
            while (!stop.get()) {
                String s = scanner.nextLine();
                var message = new CommandMessage(s);
                if (CommandUtils.getOrder(message).equals(Constant.BYE))
                    stop.set(true);
                channel.writeAndFlush(message);
            }
        } finally {
            group.shutdownGracefully().sync();
        }

    }

}
