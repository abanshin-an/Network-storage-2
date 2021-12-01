package ru.gb.storage.commons.message;

public class CommandMessage extends Message {
    private String command;

    public CommandMessage() {

    }

    public CommandMessage(String command) {
        this.command = command.strip();
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command.strip();
    }

    @Override
    public String toString() {
        return "CommandMessage{" +
                "command=" + command +
                '}';
    }
}
