package com.campusbrain.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/campusbrain?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}")
    private String mysqlUrl;

    @Value("${spring.datasource.username:root}")
    private String mysqlUsername;

    @Value("${spring.datasource.password:root}")
    private String mysqlPassword;

    @Value("${campusbrain.datasource.force-h2:false}")
    private boolean forceH2;

    @Bean
    @Primary
    public DataSource dataSource() {
        if (!forceH2 && isMySqlReachable("localhost", 3306, 1200)) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                log.info("Connecting to MySQL at localhost:3306 for CampusBrain database...");
                HikariConfig config = new HikariConfig();
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                config.setJdbcUrl(mysqlUrl);
                config.setUsername(mysqlUsername);
                config.setPassword(mysqlPassword);
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(3000);
                HikariDataSource ds = new HikariDataSource(config);
                // Test connection
                ds.getConnection().close();
                log.info("Successfully connected to MySQL database: campusbrain");
                return ds;
            } catch (ClassNotFoundException e) {
                log.info("MySQL JDBC driver not loaded on classpath. Falling back to persistent storage.");
            } catch (Exception e) {
                log.warn("MySQL connection test failed ({}). Switching to persistent storage.", e.getMessage());
            }
        } else {
            log.info("Using persistent file-based database for CampusBrain.");
        }

        try {
            Files.createDirectories(Paths.get("./data"));
        } catch (Exception ignored) {}

        HikariConfig h2Config = new HikariConfig();
        h2Config.setDriverClassName("org.h2.Driver");
        h2Config.setJdbcUrl("jdbc:h2:file:./data/campusbrain;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false");
        h2Config.setUsername("sa");
        h2Config.setPassword("");
        h2Config.setMaximumPoolSize(10);
        return new HikariDataSource(h2Config);
    }

    private boolean isMySqlReachable(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
