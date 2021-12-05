package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandMessage extends Message {

    @JsonProperty("command")
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

    public String getArgument() {
        int pos = command.indexOf(" ");
        if (pos >= 0)
            return command.substring(pos).strip();
        else
            return "";
    }

    public String getOrder() {
        int pos = command.indexOf(" ");
        if (pos > 0) {
            return command.substring(0, pos).strip();
        }
        return command;
    }

    public String[] getArguments() {
        return command.split("\\s+");
    }

    @Override
    public String toString() {
        return "CommandMessage{" +
                "command=" + command +
                '}';
    }
}
