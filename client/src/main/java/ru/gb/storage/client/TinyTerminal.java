package ru.gb.storage.client;

import ru.gb.storage.commons.Constant;
import ru.gb.storage.commons.message.CommandMessage;
import ru.gb.storage.commons.utils.CommandUtils;
import ru.gb.storage.commons.utils.FileUtils;

import java.nio.file.Path;
import java.util.Scanner;

public class TinyTerminal {
    private static ClientConfig config = null;

    public static void main(String[] args) {
        config = ClientConfig.init(args);
        run();
    }

    public static void run() {
        try {
            Client client = new Client(config);
            new Thread(client).start();
            System.out.println("TinyTerminal started");
            System.out.println(config.toString());
            client.setRecvPath(config.getRoot());
            while (!client.isFinished()) {
                System.out.print("=>");
                Scanner scanner = new Scanner(System.in);
                var s = scanner.nextLine();
                s = s.trim();
                if (parseInput(client, s)) continue;
                var message = new CommandMessage(s);
                if (CommandUtils.getOrder(message).equals(Constant.BYE))
                    client.stop();
                client.getCtx().writeAndFlush(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("TinyTerminal exit");
    }

    private static boolean parseInput(Client client, String s) {
        if (s.length() == 0) {
            return true;
        }
        String[] words = s.split("\\b");
        switch (words[0]) {
            case Constant.CD:
                client.setRecvPath(s.substring(2).trim());
                return true;
            case Constant.LS:
                var path = Path.of(client.getRecvPath());
                var list = FileUtils.getFileList(path.toAbsolutePath().toString());
                System.out.println("\n" + path + ":");
                FileUtils.sout(list);
                return true;
            case Constant.PUT:
                FileUtils.sendFile(client.getCtx(), s.substring(4).trim());
                return true;
        }
        return false;
    }
}
