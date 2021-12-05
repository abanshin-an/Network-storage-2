package ru.gb.storage.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.storage.commons.message.ResultMessage;
import ru.gb.storage.commons.utils.PasswordUtils;

import java.sql.*;

public class UserDB {
    private static final Logger log = LogManager.getLogger(UserDB.class);

    private static final String SERVER_USERS = "server_users";
    private static final String CANT_UNREGISTER = "Не удалось удалить информацию о пользователе ";
    private static final String UNREGISTER = "Удалена информация о пользователе ";
    private static final String CANT_REGISTER = "Не удалось добавить информацию о пользователе ";
    private static final String REGISTER = "Добавлена информация о пользователе ";
    private static final String CANT_UPDATE = "Не удалось обновить информацию о пользователе ";
    private static final String CANT_CHECK = "Не удалось проверить информацию о пользователе ";
    private static final String UPDATED = "Запись обновлена. Пользователь ";
    private static final String WRONG_CALL1 = "Неправильное обращение. Используйте %s <ИмяПользователя> <Пароль>";
    private static final String WRONG_CALL2 = "Неправильное обращение. Используйте %s <ИмяПользователя> <СтарыйПароль> <НовыйПароль>";
    private static Connection connection = null;
    private static ServerConfig config = null;

    public static void main(String[] args) {
        try {
            config = ServerConfig.init(args);
            init(config, true);
            var user = new UserDB();
            var pwHash = PasswordUtils.hashPassword("qwe");
            var r = user.register("a2n", pwHash);
            log.info("1 register a2n {}", r);
            r = user.checkPassword("a2n", "qwe");
            log.info("2 checkPassword a2n {} ", r);
            r = user.register("a2n", "qwe");
            log.info("3 register a2n (duplicate ) {} ", r);
            r = user.unregister("a2n", "qwe");
            log.info("4 unregister a2n {} ", r);
            r = user.register("a2n", pwHash);
            log.info("5 register a2n  (register again) {}", r);
            r = user.checkPassword("a2n", "xcv");
            log.info("6 checkPassword a2n (wrong password) {}", r);
            r = user.checkPassword("a2n", "qwe");
            log.info("7 checkPassword a2n {} ", r);
            r = user.changePassword("a2n", "qwe", "asd");
            log.info("8 changePassword a2n {} ", r);
            r = user.unregister("a2n", "asd");
            log.info("9 unregister a2n {} ", r);
        } finally {
            close();
        }
    }

    public static void init(ServerConfig config, boolean drop) {
        UserDB.config = config;
        try {
            connection = DriverManager.getConnection(config.getConnection());
        } catch (SQLException e) {
            log.info("Can't get connection. Incorrect URL");
            e.printStackTrace();
            return;
        }
        try (Statement stmt = connection.createStatement()) {
            if (drop)
                stmt.execute("DROP TABLE IF EXISTS " + SERVER_USERS);
            stmt.execute("create table if not exists " + SERVER_USERS + " (user_name varchar(64) unique not null,password varchar(64))");
        } catch (SQLException e) {
            log.info("Can't create user table");
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            log.info("Can't close connection ", e);
        }
    }

    public ResultMessage register(String[] words) {
        if (words.length != 3) {
            return new ResultMessage(false, getComment1(words));
        } else return register(words[1], words[2]);
    }

    private String getComment1(String[] words) {
        return String.format(WRONG_CALL1, words[0]);
    }

    private ResultMessage register(String userName, String pwHash) {
        ResultMessage result = new ResultMessage(true, "");
        if (!pwHash.startsWith("$2a$")) {
            result.setOk(false);
            result.setComment(CANT_REGISTER + userName + " хеш пароля неверен");
            return result;
        }
        String sql = "INSERT INTO " + SERVER_USERS + " (user_name, password) Values (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, pwHash);
            int rows = preparedStatement.executeUpdate();
            result.setOk(rows == 1);
            result.setComment(REGISTER + userName);
            log.info("{} rows added {} {}", rows, userName, pwHash);
        } catch (SQLException e) {
            result.setOk(false);
            result.setComment(CANT_REGISTER + userName);
            log.error("register", e);
        }
        return result;
    }

    public ResultMessage unregister(String[] words) {
        if (words.length != 3) {
            return new ResultMessage(false, getComment1(words));
        } else return unregister(words[1], words[2]);
    }

    private ResultMessage unregister(String userName, String password) {
        ResultMessage result = checkPassword(userName, password);
        if (!result.isOk()) {
            return result;
        }
        String sql = "DELETE FROM " + SERVER_USERS + " WHERE user_name= ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userName);
            int rows = preparedStatement.executeUpdate();
            result.setOk(rows == 1);
            result.setComment(UNREGISTER + userName);
            log.info("{} rows removed", rows);
        } catch (SQLException e) {
            result.setOk(false);
            result.setComment(CANT_UNREGISTER + e.getMessage());
            log.error("unregister", e);
        }
        return result;
    }

    public ResultMessage checkPassword(String[] words) {
        if (words.length != 3) {
            return new ResultMessage(false, getComment1(words));
        } else return checkPassword(words[1], words[2]);
    }

    private ResultMessage checkPassword(String userName, String password) {
        String sql = "SELECT password from " + SERVER_USERS + " WHERE user_name= ?";
        boolean r = false;
        ResultMessage result = new ResultMessage(false, CANT_CHECK + " " + userName);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userName);
            ResultSet resultSet = preparedStatement.executeQuery();
            String passwordDb;
            if (resultSet.next()) {
                passwordDb = resultSet.getString("password");
                r = PasswordUtils.checkPassword(password, passwordDb);
                log.info("checkPassword {}\n pw= {}\n db= {}", r, password, passwordDb);
            } else
                log.info("Запись user_name= {} не найдена", userName);
            result.setOk(r);
            if (r)
                result.setComment("Пароль ОК " + userName);
            else
                result.setComment("Пароль неверен " + userName);
        } catch (SQLException e) {
            result.setOk(false);
            result.setComment(CANT_CHECK + e.getMessage());
            log.error("checkPassword", e);
        }
        return result;
    }

    public ResultMessage changePassword(String[] words) {
        if (words.length != 4) {
            return new ResultMessage(false, String.format(WRONG_CALL2, words[0]));
        } else return changePassword(words[1], words[2], words[3]);
    }

    private ResultMessage changePassword(String userName, String oldPassword, String newPassword) {
        ResultMessage result = checkPassword(userName, oldPassword);
        if (!result.isOk()) {
            return result;
        }
        String sql = "UPDATE " + SERVER_USERS + " SET password= ? WHERE user_name= ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, PasswordUtils.hashPassword(newPassword));
            preparedStatement.setString(2, userName);
            int rows = preparedStatement.executeUpdate();
            var r = rows == 1;
            result.setOk(r);
            if (r)
                result.setComment(UPDATED + userName);
            else
                result.setComment(CANT_UPDATE + userName);
        } catch (SQLException e) {
            result.setOk(false);
            result.setComment(CANT_UPDATE + e.getMessage());
            log.error("changePassword", e);
        }
        return result;
    }
}