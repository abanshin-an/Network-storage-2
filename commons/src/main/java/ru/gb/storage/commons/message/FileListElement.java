package ru.gb.storage.commons.message;

import ru.gb.storage.commons.Constant;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FileListElement {
    private String name;
    private String size;
    private LocalDateTime datetime;

    public FileListElement() {
    }

    public FileListElement(String name, String size, LocalDateTime datetime) {
        this.name = name;
        this.size = size;
        this.datetime = datetime;
    }

    public FileListElement(File file) {
        name = file.getName();
        if (file.isDirectory()) {
            size = Constant.DIR;
        } else {
            size = String.valueOf(file.length());
        }
        datetime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault()
        );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "FileListElement{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", datetime=" + datetime +
                '}';
    }
}
