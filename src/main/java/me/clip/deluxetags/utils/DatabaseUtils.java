package me.clip.deluxetags.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {
    private static DatabaseUtils INSTANCE;
    private DataSource dataSource;

    private DatabaseUtils() {
    }

    public static void initConfig(String jdbcURL, String username, String password) throws ClassNotFoundException {
        DatabaseUtils.INSTANCE = new DatabaseUtils();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcURL);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts",true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit",2048);
        config.setLeakDetectionThreshold(2000);
        DatabaseUtils.INSTANCE.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    public static DatabaseUtils getINSTANCE() {
        return INSTANCE;
    }
}
