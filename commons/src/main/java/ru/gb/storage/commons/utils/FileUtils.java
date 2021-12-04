package ru.gb.storage.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.channel.ChannelHandlerContext;
import ru.gb.storage.commons.Constant;
import ru.gb.storage.commons.message.CommandMessage;
import ru.gb.storage.commons.message.FileListElement;
import ru.gb.storage.commons.message.FileListMessage;
import ru.gb.storage.commons.message.FileMessage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.stream.Stream;

public class FileUtils {
    static ObjectMapper objectMapper;

    static {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private FileUtils() {
    }

    public static void sendFile(ChannelHandlerContext ctx, String path) {
        var file = new File(path);
        if (!file.exists() || !file.isFile()) {
            ctx.writeAndFlush(new CommandMessage("Файл " + path + " не найден"));
        } else
            try (var f = new RandomAccessFile(file, "r")) {
                var fileLength = f.length();
                var endOfFile = false;
                do {
                    var position = f.getFilePointer();
                    var availableBytes = fileLength - position;
                    var bytes = new byte[(int) Math.min(availableBytes, Constant.BUFFER_SIZE)];
                    f.read(bytes);
                    endOfFile = f.getFilePointer() >= fileLength;
                    var fileMessage = new FileMessage();
                    fileMessage.setContent(bytes);
                    fileMessage.setFileName(file.getName());
                    fileMessage.setStartPosition(position);
                    fileMessage.setEndOfFile(endOfFile);
                    if (ctx != null) {
                        System.out.println("server send " + path + " position " + position);
                        ctx.writeAndFlush(fileMessage);
                    }
                } while (!endOfFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void recvFile(FileMessage fileMessage, String path) {
        var f = new File(path + fileMessage.getFileName());
        System.out.println("receive part " + f.getAbsoluteFile() + " position " + fileMessage.getStartPosition());
        try (RandomAccessFile file = new RandomAccessFile(f, "rw")) {
            file.seek(fileMessage.getStartPosition());
            file.write(fileMessage.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fileMessage.isEndOfFile()) {
            System.out.println("receive complete " + fileMessage.getFileName());
        }

    }

    public static void sendFileList(ChannelHandlerContext ctx, String path) {
        FileListElement[] list = getFileList(path);
        String s = "send filelist " + list.length + " elements";
        System.out.println(s);
        var fileListMessage = new FileListMessage(list);
        if (ctx != null) {
            ctx.writeAndFlush(fileListMessage);
        }
    }

    public static FileListElement[] getFileList(String path) {
        if (path.length() == 0)
            path = ".";
        File[] list = new File(path).listFiles();
        FileListElement[] fileElements;
        if ((list!=null) && (list.length > 0)) {
            fileElements = Stream.of(list)
                    .map(FileListElement::new)
                    .toArray(FileListElement[]::new);
        } else {
            fileElements = new FileListElement[]{};
        }
        return fileElements;
    }

    public static void sout(FileListElement[] list) {
        if (list == null)
            return;
        for (FileListElement i : list) {
            System.out.println(i.toString());
        }
    }

}