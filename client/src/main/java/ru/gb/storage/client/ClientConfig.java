package ru.gb.storage.client;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientConfig {
    private int port = 8989;
    private String root = "client_";
    private String host = "127.0.0.1";

    public static ClientConfig init(String[] args) {
        ClientConfig config = null;
        String configFile;
        if (args.length == 1)
            configFile = args[0];
        else
            configFile = "client/client.yaml";
        Yaml yaml = new Yaml();
        Path p = Paths.get(configFile);
        try (InputStream in = Files.newInputStream(p)) {
            config = yaml.loadAs(in, ClientConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static void main(String[] args) {
        ClientConfig clientConfig = ClientConfig.init(args);
        if (clientConfig != null)
            System.out.println(clientConfig);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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

    @Override
    public String toString() {
        return "ClientConfig{" +
                "port=" + port +
                ", root='" + root + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}
