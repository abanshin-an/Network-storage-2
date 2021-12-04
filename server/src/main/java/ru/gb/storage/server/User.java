package ru.gb.storage.server;

import ru.gb.storage.commons.message.ResultMessage;

import java.sql.*;

public class User {
    private static final String SERVER_USERS = "server_users";
    private static final String CANT_UNREGISTER = "Не удалось удалить информацию о пользователе ";
    private static final String CANT_REGISTER = "Не удалось добавить информацию о пользователе ";
    private static final String CANT_UPDATE = "Не удалось обновить информацию о пользователе ";
    private static Connection connection = null;
    private static ServerConfig config = null;

    public static void main(String[] args) {
        try {
            config = ServerConfig.init(args);
            init();
            var user = new User();
            var r = user.register("a2n", "qwe");
            System.out.println("register a2n " + r);
            r = user.register("a2n", "qwe");
            System.out.println("register a2n (duplicate ) " + r);
            r = user.unregister("a2n");
            System.out.println("unregister a2n " + r);
            r = user.register("a2n (register again)", "qwe");
            System.out.println("register a2n " + r);
            r = user.checkPassword("a2n", "xcv");
            System.out.println("checkPassword a2n (wrong password) " + r);
            r = user.checkPassword("a2n", "qwe");
            System.out.println("checkPassword a2n  " + r);
            r = user.changePassword("a2n", "qwe", "asd");
            System.out.println("changePassword a2n  " + r);
        } finally {
            close();
        }
    }

    private static void init() {
        try {
            Class.forName(config.getDriverName());
        } catch (ClassNotFoundException e) {
            System.out.println("Can't get class. No driver found");
            e.printStackTrace();
            return;
        }
        try {
            connection = DriverManager.getConnection(config.getConnection());
        } catch (SQLException e) {
            System.out.println("Can't get connection. Incorrect URL");
            e.printStackTrace();
            return;
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("create table if not exists " + SERVER_USERS + " (user_name varchar(64),password varchar(64))");
        } catch (SQLException e) {
            System.out.println("Can't create user table");
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            System.out.println("Can't close connection");
            e.printStackTrace();
        }
    }

    public ResultMessage register(String userName, String password) {
        String sql = "INSERT INTO " + SERVER_USERS + " (user_name, password) Values (?, ?)";
        ResultMessage result = new ResultMessage(true, "");
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, password);
            int rows = preparedStatement.executeUpdate();
            result.setResult(rows == 1);
            result.setComment(CANT_REGISTER + userName);
            System.out.println("" + rows + " rows added");
        } catch (SQLException e) {
            result.setResult(false);
            result.setComment(CANT_REGISTER + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public ResultMessage unregister(String userName) {
        String sql = "DELETE FROM " + SERVER_USERS + " WHERE user_name= ?";
        ResultMessage result = new ResultMessage(true, "");
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userName);
            int rows = preparedStatement.executeUpdate();
            result.setResult(rows == 1);
            result.setComment(CANT_UNREGISTER + userName);
            System.out.println("" + rows + " rows removed");
        } catch (SQLException e) {
            result.setResult(false);
            result.setComment(CANT_UNREGISTER + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public ResultMessage checkPassword(String userName, String password) {
        String sql = "SELECT password from " + SERVER_USERS + " WHERE user_name= ?";
        ResultMessage result = new ResultMessage(true, "");
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userName);
            ResultSet resultSet = preparedStatement.executeQuery();
            String passwordDb = "";
            if (resultSet.next())
                passwordDb = resultSet.getString("password");
            result.setResult(!passwordDb.equals(password));
            result.setComment("Пароль неверен " + userName);
        } catch (SQLException e) {
            result.setResult(false);
            result.setComment(CANT_UPDATE + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public ResultMessage changePassword(String userName, String oldPassword, String newPassword) {
        String sql = "UPDATE " + SERVER_USERS + " SET password= ? WHERE user_name= ? and password = ?";
        ResultMessage result = new ResultMessage(true, "");
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, newPassword);
            preparedStatement.setString(2, userName);
            preparedStatement.setString(3, oldPassword);
            int rows = preparedStatement.executeUpdate();
            result.setResult(rows == 1);
            result.setComment(CANT_UPDATE + userName);
            System.out.println("" + rows + " rows added");
            System.out.println("" + rows + " rows updated");
        } catch (SQLException e) {
            result.setResult(false);
            result.setComment(CANT_UPDATE + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}