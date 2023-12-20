package ru.ac.checkpointmanager.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация для интеграции с Redis.
 * <p>
 * Этот класс предоставляет конфигурации для работы с Redis,
 * включая настройку шаблона RedisTemplate и менеджера кэша RedisCacheManager.
 */
@Configuration
public class RedisConfig {

    /**
     * Создаёт и настраивает {@link RedisTemplate} для сериализации и десериализации объектов Redis.
     * <p>
     * Этот метод конфигурирует {@link RedisTemplate} с использованием {@link GenericJackson2JsonRedisSerializer}
     * для сериализации и десериализации объектов. Он также настраивает {@link ObjectMapper} с активацией
     * дефолтной типизации для корректной обработки полиморфных типов данных.
     *
     * @param connectionFactory Фабрика соединений с Redis.
     * @return сконфигурированный {@link RedisTemplate}.
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        ObjectMapper objectMapper = new ObjectMapper();
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class) // Разрешить сериализацию для базового типа Object и его подтипов
                .build();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        return template;
    }

    /**
     * Создаёт и конфигурирует {@link RedisCacheManager} для управления кэшем в Redis.
     * <p>
     * Этот метод настраивает {@link RedisCacheManager} с использованием {@link GenericJackson2JsonRedisSerializer}
     * для сериализации значений кэша.
     *
     * @param connectionFactory Фабрика соединений с Redis.
     * @return сконфигурированный {@link RedisCacheManager}.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        RedisCacheConfiguration tempCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> tempCache = new HashMap<>();
        tempCache.put("registration", tempCacheConfig);
        tempCache.put("email", tempCacheConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(tempCache)
                .build();
    }
}
