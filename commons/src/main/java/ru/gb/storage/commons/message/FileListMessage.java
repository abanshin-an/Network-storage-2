package ru.gb.storage.commons.message;

public class FileListMessage extends Message {
    private FileListElement[] fileList;

    public FileListMessage() {
    }

    public FileListMessage(FileListElement[] s) {
        fileList = s;
    }

    public FileListElement[] getFileList() {
        return fileList;
    }

    public void setFileList(FileListElement[] list) {
        fileList = list;
    }
}
