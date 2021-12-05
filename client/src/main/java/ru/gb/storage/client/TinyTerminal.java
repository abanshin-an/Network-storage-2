package ru.gb.storage.client;

import ru.gb.storage.commons.Constant;
import ru.gb.storage.commons.message.CommandMessage;
import ru.gb.storage.commons.utils.FileUtils;
import ru.gb.storage.commons.utils.PasswordUtils;

import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TinyTerminal {
    private static ClientConfig config = null;

    public static void main(String[] args) {
        config = ClientConfig.init(args);
        run();
    }

    public static void run() {
        try {
            Client client = new Client(config);
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(client::run);
            System.out.println("TinyTerminal started");
            System.out.println(config.toString());
            client.setRecvPath(config.getRoot());
            while (!client.isFinished()) {
                System.out.print("\n=>");
                Scanner scanner = new Scanner(System.in);
                var s = scanner.nextLine();
                s = s.trim();
                var message = new CommandMessage(s);
                if (parseInput(client, message)) continue;
                if (message.getOrder().equals(Constant.BYE))
                    client.stop();
                if (client.getCtx() == null) {
                    System.out.println("Сервер отключился. Жду подключения. Чтобы выйти нажмите Enter любую клавишу");
                    scanner.nextLine();
                }
                if (client.getCtx() != null)
                    client.getCtx().writeAndFlush(message);
                else
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        System.out.println("TinyTerminal exit");
    }

    private static boolean parseInput(Client client, CommandMessage message) {
        String s = message.getCommand();
        if (s.length() == 0) {
            return true;
        }
        String[] words = message.getArguments();
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
            case Constant.REGISTER:
            case Constant.UNREGISTER:
            case Constant.LOGIN:
                if (words.length == 3) {
                    if (words[0].equals(Constant.REGISTER)) {
                        words[2] = PasswordUtils.hashPassword(words[2]);
                        message.setCommand(String.join(" ", words));
                    }
                    return false;
                }
                System.out.println(words[0] + " <username> <password>");
                return true;
            case Constant.CHANGE_PASSWORD:
                if (words.length == 4)
                    return false;
                System.out.println(words[0] + " <username> <old-password> <new-password> ");
                return true;
            case Constant.LOGOUT:
                if (words.length == 1)
                    return false;
                System.out.println(words[0]);
                return true;
            default:
        }
        return false;
    }
}
