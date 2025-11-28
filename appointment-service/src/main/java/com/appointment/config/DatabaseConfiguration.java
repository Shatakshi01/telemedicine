package com.appointment.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class DatabaseConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        DataSource dataSource = dataSourceProperties()
                .initializeDataSourceBuilder()
                .build();

        // Ensure schema exists and set search_path
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                // Create schema if it doesn't exist
                statement.execute("CREATE SCHEMA IF NOT EXISTS appointment");
                // Set search path to prioritize appointment schema
                statement.execute("SET search_path TO appointment, public");
                System.out.println("Successfully set search_path to appointment, public");
            }
        } catch (SQLException e) {
            // Log the error but don't fail startup - this might be due to permissions
            System.err.println("Warning: Could not set search_path: " + e.getMessage());
        }

        return dataSource;
    }
}
