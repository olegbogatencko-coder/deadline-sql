package ru.netology.data;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseUtils {
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/app";
    private static final String DB_USER = "app";
    private static final String DB_PASS = "pass";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static String getAuthCode(String login) {
        String sql = "SELECT code FROM auth_codes ac JOIN users u ON ac.user_id = u.id WHERE u.login = ? ORDER BY ac.created DESC LIMIT 1";
        try (Connection conn = getConnection()) {
            QueryRunner runner = new QueryRunner();
            String code = null;
            // Ждем, пока приложение сгенерирует код и запишет его в БД
            for (int i = 0; i < 10; i++) {
                code = runner.query(conn, sql, new ScalarHandler<>(), login);
                if (code != null) {
                    return code;
                }
                Thread.sleep(1000); // ждем 1 секунду и пробуем снова
            }
            throw new RuntimeException("Код так и не появился в БД за 10 секунд!");
        } catch (Exception e) {
            throw new RuntimeException("Не удалось получить код из БД", e);
        }
    }

    public static void cleanDatabase() {
        try (Connection conn = getConnection()) {
            QueryRunner runner = new QueryRunner();
            // Очищаем все таблицы
            runner.update(conn, "DELETE FROM card_transactions");
            runner.update(conn, "DELETE FROM auth_codes");
            runner.update(conn, "DELETE FROM cards");
            runner.update(conn, "DELETE FROM users");

            // Восстанавливаем демо-пользователя vasya (пароль qwerty123)
            // Хэш взят из логов запуска SUT
            String hash = "$2a$10$pQRYtROy3sUfAp/C2/dBJustCBdnmU5H7iOB7y0YAvRmcBmzP6oz.";
            runner.update(conn, "INSERT INTO users(id, login, password, status) VALUES (?, ?, ?, ?)",
                    UUID.randomUUID().toString(), "vasya", hash, "active");

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось очистить БД", e);
        }
    }
}