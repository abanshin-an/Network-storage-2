package ru.gb.storage.commons;

public class Constant {
    public static final String ECHO = "echo";
    public static final String GET = "get";
    public static final String PUT = "put";
    public static final String CD = "cd";
    public static final String LS = "ls";
    public static final String GET_FILELIST = "rls";
    public static final String REGISTER = "reg";
    public static final String UNREGISTER = "unreg";
    public static final String LOGIN = "login";
    public static final String LOGOUT = "logout";
    public static final String CHANGE_PASSWORD = "chpass";

    public static final String CONNECT = "connect";
    public static final String TEXT = "text";
    public static final String BYE = "bye";
    public static final String SD = "sd!"; // shutdown
    public static final String DIR = "<DIR>";
    public static final int BUFFER_SIZE = 64 * 1024;
    public static final int FRAME_SIZE = 1024 * 1024;

    private Constant() {
    }
}
