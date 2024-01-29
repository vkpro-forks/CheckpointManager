package ru.ac.checkpointmanager.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

/**
 * Конфигурационный класс для запуска одного контейнера для всех тестов (ускоряет тестирование)
 * <p>
 * Не требует применения {@link org.testcontainers.junit.jupiter.Testcontainers } и
 * {@link org.testcontainers.junit.jupiter.Container} аннотаций, которые управляют запуском и остановкой контейнеров автоматически.
 * Этот класс запускает одновременно два контейнера: Postgres и Redis, которые будут переиспользоваться для всех тестов
 */
public class PostgresAndRedisTestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("cbotDB");

    static RedisContainer redisContainer = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));

    static {
        Startables.deepStart(postgresContainer, redisContainer).join();
    }

    //initialize environment before dynamic property source applied
    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        TestPropertyValues.of(
                        "spring.liquibase.enabled=true",
                        "spring.liquibase.label-filter=!demo-data",
                        "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
                        "spring.datasource.username=" + postgresContainer.getUsername(),
                        "spring.datasource.password=" + postgresContainer.getPassword(),
                        "spring.data.redis.host=" + redisContainer.getHost(),
                        "spring.data.redis.port=" + redisContainer.getMappedPort(6379).toString()
                )
                .applyTo(ctx.getEnvironment());
    }

}
