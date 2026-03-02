package com.todo.analytics.config;

import com.todo.common.security.SecretService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
    @Value("${SPRING_DATASOURCE_URL}")
    private String jdbcUrl;

    @Value("${SPRING_DATASOURCE_USERNAME}")
    private String username;

    @Value("${SPRING_DATASOURCE_PASSWORD}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(100);
        config.setMinimumIdle(10);
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(5000);
        config.addDataSourceProperty("prepareThreshold", "0");
        config.addDataSourceProperty("preparedStatementCacheQueries", "0");

        return new HikariDataSource(config);
    }
}
