package ru.gb.storage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.Constant;
import ru.gb.storage.commons.message.CommandMessage;
import ru.gb.storage.commons.message.FileListMessage;
import ru.gb.storage.commons.message.FileMessage;
import ru.gb.storage.commons.message.Message;
import ru.gb.storage.commons.utils.CommandUtils;
import ru.gb.storage.commons.utils.FileUtils;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    private final Client fc;

    public ClientHandler(Client fc) {
        this.fc = fc;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client channel Active");
        fc.setCtx(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        System.out.println("Client incoming message :" + msg);
        if (msg instanceof CommandMessage) {
            var commandMessage = (CommandMessage) msg;
            switch (CommandUtils.getOrder(commandMessage)) {
                case Constant.TEXT:
                    System.out.println("Server:" + CommandUtils.getArgument(commandMessage));
                    break;
                case Constant.ECHO:
                    System.out.println("Server echo:" + CommandUtils.getArgument(commandMessage));
                    break;
                case Constant.BYE:
                    fc.stop();
                    break;
                default:
                    System.out.println("Server answer:" + CommandUtils.getArgument(commandMessage));
            }
        } else if (msg instanceof FileMessage) {
            FileUtils.recvFile((FileMessage) msg, fc.getRecvPath());
        } else if (msg instanceof FileListMessage) {
            var fileList = ((FileListMessage) msg).getFileList();
            FileUtils.sout(fileList);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

