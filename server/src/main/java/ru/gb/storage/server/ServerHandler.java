package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.storage.commons.Constant;
import ru.gb.storage.commons.message.CommandMessage;
import ru.gb.storage.commons.message.FileMessage;
import ru.gb.storage.commons.message.Message;
import ru.gb.storage.commons.message.ResultMessage;
import ru.gb.storage.commons.utils.FileUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger log = LogManager.getLogger(ServerHandler.class);
    private final Server server;
    private boolean loggedIn=false;
    private final String[] anonymousArray = { Constant.REGISTER,Constant.LOGIN,Constant.ECHO};
    private final Set<String> anonymusSet = new HashSet<>(Arrays.asList(anonymousArray));
    public ServerHandler(Server server) {
        this.server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("ServerHandler channel active");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        log.info(msg);
        if (msg instanceof FileMessage)
            server.getExecutor().execute(() -> FileUtils.recvFile((FileMessage) msg, Server.getConfig().getRoot()));
        else if (msg instanceof CommandMessage) {
            var commandMessage = (CommandMessage) msg;
            var words = commandMessage.getArguments();
            ResultMessage result;
            String order =commandMessage.getOrder();
            if (!(loggedIn || anonymusSet.contains(order))) {
                result = new ResultMessage(false,"Команда "+ commandMessage.getCommand()+" требует выполнения регистрации или входа на сервер " );
                ctx.writeAndFlush(result);
                return;
            }
            switch (order) {
                case Constant.ECHO:
                    var echo = new CommandMessage(commandMessage.getCommand());
                    ctx.writeAndFlush(echo);
                    break;
                case Constant.BYE:
                    // Close the current channel
                    ctx.channel().close();
                    break;
                case Constant.REGISTER:
                    result = Server.getUser().register(words);
                    ctx.writeAndFlush(result);
                    loggedIn=result.isOk();
                    break;
                case Constant.LOGIN:
                     result = Server.getUser().checkPassword(words);
                    ctx.writeAndFlush(result);
                    loggedIn = result.isOk();
                    break;
                case Constant.GET_FILELIST:
                    server.getExecutor().execute(() -> FileUtils.sendFileList(ctx, Server.getConfig().getRoot() + commandMessage.getArgument()));
                    break;
                case Constant.GET:
                    server.getExecutor().execute(() -> {
                        var path=Server.getConfig().getRoot() + commandMessage.getArgument();
                        FileUtils.sendFile(ctx, path);
                        log.info(path);
                    });
                    break;
                case Constant.SD:
                    // Close the current channel
                    ctx.channel().close();
                    // Then close the parent channel (the one attached to the bind)
                    ctx.channel().parent().close();
                    break;
                case Constant.UNREGISTER:
                    result=Server.getUser().unregister(words);
                    ctx.writeAndFlush(result);
                    loggedIn=!result.isOk();
                    break;
                case Constant.CHANGE_PASSWORD:
                    ctx.writeAndFlush(Server.getUser().changePassword(words));
                    break;
                case Constant.LOGOUT:
                    loggedIn = false;
                    break;
                default:
                    result = new ResultMessage(false, "Wrong command - " + commandMessage.getCommand());
                    ctx.writeAndFlush(result);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Server Handler", cause);
        ctx.close();
    }
}


