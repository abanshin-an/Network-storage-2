package ru.gb.storage.commons.message;

public class ResultMessage extends Message {
    boolean result;
    String comment;

    public ResultMessage(boolean result, String comment) {
        this.result = result;
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ResultMessage{" +
                "result=" + result +
                ", comment='" + comment + '\'' +
                '}';
    }
}
