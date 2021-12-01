package ru.gb.storage.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Constant {
    public static final String ECHO = "echo";
    public static final String GET = "get";
//    public static final String PUT = "put";
    public static final String BYE = "bye";
    public static final String SD = "sd!"; // shutdown
    public static final String STORAGE_ROOT = "storage-root";
    private static final Properties PROPS;

    static {
        String rootPath = new File(".").getAbsolutePath();
        String appConfigPath = rootPath.substring(0, rootPath.length() - 1) + "app.properties";

        PROPS = new Properties();
        try {
            PROPS.load(new FileInputStream(appConfigPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Constant() {

    }

    public static String getProperty(String s) {
        return PROPS.getProperty(s);
    }
}
