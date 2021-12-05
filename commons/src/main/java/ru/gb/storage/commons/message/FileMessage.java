package ru.gb.storage.commons.message;

public class FileMessage extends Message {
    private byte[] content;
    private long startPosition;
    private String fileName;

    public boolean isEndOfFile() {
        return endOfFile;
    }

    public void setEndOfFile(boolean endOfFile) {
        this.endOfFile = endOfFile;
    }

    private boolean endOfFile = false;

    public FileMessage() {
        //
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "FileMessage{" +
                "fileName='" + fileName + "'" +
                ", startPosition=" + startPosition +
                ", endOfFile=" + endOfFile +
                '}';
    }
}
