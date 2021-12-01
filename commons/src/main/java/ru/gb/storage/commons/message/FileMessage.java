package ru.gb.storage.commons.message;

public class FileMessage extends Message {
    byte[] content;

    public FileMessage() {
        //
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "FileMessage{}";
    }
}
