package ru.gb.storage.commons.utils;

import io.netty.channel.ChannelHandlerContext;
import ru.gb.storage.commons.message.FileMessage;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtils {
    private FileUtils(){}
    public static void sendFile(ChannelHandlerContext ctx, String path) {
        try (RandomAccessFile f = new RandomAccessFile(path, "r")) {
            final FileMessage fmsg = new FileMessage();
            byte[] content = new byte[(int) f.length()];
            f.read(content);
            fmsg.setContent(content);
            ctx.writeAndFlush(fmsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void recvFile(FileMessage fileMessage) {
        try (RandomAccessFile file = new RandomAccessFile("1", "rw")) {
            file.write(fileMessage.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
