package ru.ac.checkpointmanager.config;

import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PostgresTestContainersConfiguration {

    @Container
    @ServiceConnection(type = JdbcConnectionDetails.class)
    private static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest").withDatabaseName("chpmanDB");

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.liquibase.enabled", () -> true);
        registry.add("spring.liquibase.label-filter", () -> "!demo-data");
    }

}
