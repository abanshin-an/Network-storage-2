package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.Constant;
import ru.gb.storage.commons.message.CommandMessage;
import ru.gb.storage.commons.message.FileMessage;
import ru.gb.storage.commons.message.Message;
import ru.gb.storage.commons.utils.CommandUtils;
import ru.gb.storage.commons.utils.FileUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    private final Executor executor;
    private final ServerConfig config;

    public ServerHandler(ThreadPoolExecutor executor, ServerConfig config) {
        this.executor = executor;
        this.config = config;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("channel Active");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        System.out.println(msg);
        if (msg instanceof FileMessage) executor.execute(() -> FileUtils.recvFile((FileMessage) msg, config.getRoot()));
        else if (msg instanceof CommandMessage) {
            var commandMessage = (CommandMessage) msg;
            switch (CommandUtils.getOrder(commandMessage)) {
                case Constant.ECHO:
                    var echo = new CommandMessage(commandMessage.getCommand());
                    ctx.writeAndFlush(echo);
                    break;
                case Constant.GET_FILELIST:
                    executor.execute(() -> FileUtils.sendFileList(ctx, config.getRoot() + CommandUtils.getArgument(commandMessage)));
                    break;
                case Constant.GET:
                    executor.execute(() -> {
                        FileUtils.sendFile(ctx, config.getRoot() + CommandUtils.getArgument(commandMessage));
                        System.out.println("file " + config.getRoot() + CommandUtils.getArgument(commandMessage) + " sent");
                    });
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
                    break;
                default:
                    var wrongCommand = new CommandMessage("Wrong command - " + commandMessage.getCommand());
                    ctx.writeAndFlush(wrongCommand);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}


