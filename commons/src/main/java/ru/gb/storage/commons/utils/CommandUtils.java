package ru.gb.storage.commons.utils;

import ru.gb.storage.commons.message.CommandMessage;

public class CommandUtils {
    private CommandUtils() {

    }

    public static String getArgument(CommandMessage command) {
        int pos = command.getCommand().indexOf(" ");
        if (pos >= 0)
            return command.getCommand().substring(pos).strip();
        else
            return "";
    }

    public static String getOrder(CommandMessage command) {
        int pos = command.getCommand().indexOf(" ");
        if (pos > 0) {
            return command.getCommand().substring(0, pos).strip();
        }
        return command.getCommand();
    }

}
