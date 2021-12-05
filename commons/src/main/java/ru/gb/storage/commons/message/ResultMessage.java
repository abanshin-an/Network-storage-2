package ru.gb.storage.commons.message;

public class ResultMessage extends Message {
    private boolean ok;
    private String comment;

    public ResultMessage() {
    }

    public ResultMessage(boolean ok, String comment) {
        this.ok = ok;
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    @Override
    public String toString() {
        return "ResultMessage{" +
                "result=" + ok +
                ", comment='" + comment + '\'' +
                '}';
    }
}
