package ru.ac.checkpointmanager.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class LocalDevRedisTestContainersConfiguration {

    @Bean
    @ServiceConnection(name = "redis", type = RedisConnectionDetails.class)
    public RedisContainer redisContainer() {
        return new RedisContainer(
                RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));
    }

}
