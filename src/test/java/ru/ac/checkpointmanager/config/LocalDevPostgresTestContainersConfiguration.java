package ru.ac.checkpointmanager.config;

import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class LocalDevPostgresTestContainersConfiguration {

    @Bean
    @ServiceConnection(type = JdbcConnectionDetails.class)
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:latest").withDatabaseName("chpmanDB");
    }

}
