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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrlEnv;

    @Value("${spring.datasource.url:#{null}}")
    private String springDatasourceUrl;

    @Value("${spring.datasource.username:root}")
    private String defaultUsername;

    @Value("${spring.datasource.password:root}")
    private String defaultPassword;

    @Value("${campusbrain.datasource.force-h2:false}")
    private boolean forceH2;

    @Bean
    @Primary
    public DataSource dataSource() {
        if (!forceH2) {
            String targetDbUrl = databaseUrlEnv != null && !databaseUrlEnv.trim().isEmpty() 
                    ? databaseUrlEnv.trim() 
                    : (springDatasourceUrl != null && !springDatasourceUrl.trim().isEmpty() ? springDatasourceUrl.trim() : null);

            // 1. Cloud PostgreSQL (Neon / Supabase / Render Postgres)
            if (targetDbUrl != null && (targetDbUrl.startsWith("postgres://") || targetDbUrl.startsWith("postgresql://") || targetDbUrl.startsWith("jdbc:postgresql://"))) {
                try {
                    log.info("Configuring Cloud PostgreSQL datasource for permanent storage...");
                    HikariConfig config = new HikariConfig();
                    config.setDriverClassName("org.postgresql.Driver");

                    if (targetDbUrl.startsWith("jdbc:postgresql://")) {
                        config.setJdbcUrl(targetDbUrl);
                        if (defaultUsername != null && !defaultUsername.equals("root")) config.setUsername(defaultUsername);
                        if (defaultPassword != null && !defaultPassword.equals("root")) config.setPassword(defaultPassword);
                    } else {
                        // Parse postgres://user:pass@host:port/dbname
                        URI uri = new URI(targetDbUrl);
                        String userInfo = uri.getUserInfo();
                        if (userInfo != null && userInfo.contains(":")) {
                            String[] parts = userInfo.split(":", 2);
                            config.setUsername(parts[0]);
                            config.setPassword(parts[1]);
                        }
                        String host = uri.getHost();
                        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                        String path = uri.getPath(); // "/neondb"
                        String query = uri.getQuery();
                        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                        if (query != null && !query.isEmpty()) {
                            jdbcUrl += "?" + query;
                        } else {
                            jdbcUrl += "?sslmode=require";
                        }
                        config.setJdbcUrl(jdbcUrl);
                    }

                    config.setMaximumPoolSize(10);
                    config.setMinimumIdle(2);
                    config.setConnectionTimeout(15000);

                    HikariDataSource ds = new HikariDataSource(config);
                    // Verify connection
                    ds.getConnection().close();
                    log.info("Successfully connected to Cloud PostgreSQL database! Academic materials are permanent.");
                    return ds;
                } catch (Exception e) {
                    log.error("Failed to connect to Cloud PostgreSQL ({}). Falling back to local storage.", e.getMessage());
                }
            }

            // 2. MySQL (Remote or Localhost:3306)
            if (targetDbUrl != null && targetDbUrl.startsWith("jdbc:mysql://")) {
                try {
                    log.info("Attempting to connect to MySQL database...");
                    HikariConfig config = new HikariConfig();
                    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    config.setJdbcUrl(targetDbUrl);
                    config.setUsername(defaultUsername);
                    config.setPassword(defaultPassword);
                    config.setMaximumPoolSize(10);
                    config.setMinimumIdle(2);
                    config.setConnectionTimeout(5000);
                    HikariDataSource ds = new HikariDataSource(config);
                    ds.getConnection().close();
                    log.info("Successfully connected to MySQL database: campusbrain");
                    return ds;
                } catch (Exception e) {
                    log.warn("MySQL connection failed: {}. Falling back to local storage.", e.getMessage());
                }
            } else if (isMySqlReachable("localhost", 3306, 1200)) {
                try {
                    HikariConfig config = new HikariConfig();
                    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    config.setJdbcUrl("jdbc:mysql://localhost:3306/campusbrain?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                    config.setUsername(defaultUsername);
                    config.setPassword(defaultPassword);
                    config.setMaximumPoolSize(10);
                    config.setMinimumIdle(2);
                    config.setConnectionTimeout(3000);
                    HikariDataSource ds = new HikariDataSource(config);
                    ds.getConnection().close();
                    log.info("Successfully connected to local MySQL database: campusbrain");
                    return ds;
                } catch (Exception e) {
                    log.warn("Local MySQL connection failed ({}). Switching to local file database.", e.getMessage());
                }
            }
        }

        // 3. Fallback: Local file-based H2 database
        log.info("Using local persistent file-based database for CampusBrain.");
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
