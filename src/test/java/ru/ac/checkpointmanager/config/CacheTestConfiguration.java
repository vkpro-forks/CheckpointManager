package ru.ac.checkpointmanager.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;

@TestConfiguration
public class CacheTestConfiguration {

    @Bean
    @Primary
    RedisCacheManager redisCacheManager() {
        RedisCacheManager cacheManager = Mockito.mock(RedisCacheManager.class);
        Cache mockCache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(mockCache);
        return cacheManager;
    }

}
