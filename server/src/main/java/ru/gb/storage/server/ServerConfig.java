package ru.gb.storage.server;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerConfig {
    private int port = 8989;
    private String root = "server_";
    private String users = "server/user1.db";
    private String connection = "";
    private String driverName = "";

    private ServerConfig() {

    }

    public static ServerConfig init(String[] args) {
        String configFile;
        ServerConfig config = null;
        if (args.length == 1)
            configFile = args[0];
        else
            configFile = "server/server.yaml";
        Yaml yaml = new Yaml();
        Path p = Paths.get(configFile);
        try (InputStream in = Files.newInputStream(p)) {
            config = yaml.loadAs(in, ServerConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static void main(String[] args) {
        ServerConfig config = ServerConfig.init(args);
        if (config != null)
            System.out.println(config);
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        StringBuilder sb = new StringBuilder(root);
        if ((root.length() == 0) || root.charAt(root.length() - 1) != File.separatorChar) {
            sb.append(File.separatorChar);
        }
        this.root = sb.toString();
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "port=" + port +
                ", root='" + root + '\'' +
                ", users='" + users + '\'' +
                '}';
    }
}
