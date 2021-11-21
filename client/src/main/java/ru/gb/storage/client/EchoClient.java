package ru.gb.storage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;


public class EchoClient {
    private static final int PORT = 9000;
    private static final String HOST = "localhost";

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup(1 );
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        public void initChannel(NioSocketChannel ch){
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(512, 0, 2, 0, 2),
                                    new LengthFieldPrepender(2),
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new SimpleChannelInboundHandler<String>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx,String msg){
                                            System.out.println("Client incoming message :"+msg);
                                        }
                                    }
                            );
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE,true);

            Channel channel=bootstrap.connect(HOST,PORT).sync().channel();
            Scanner scanner = new Scanner(System.in);
            boolean stop = false;
            while (!stop) {
                String s=scanner.nextLine();
                channel.writeAndFlush(s);
                stop = s.equals("bye");
            }
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        EchoClient ec=new EchoClient();
        ec.run();
    }

}
