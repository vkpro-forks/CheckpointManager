package ru.ac.checkpointmanager.configuration.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.lang.NonNull;
import ru.ac.checkpointmanager.exception.ExceptionUtils;

@Slf4j
public class RedisCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
        log.warn(ExceptionUtils.CACHING_FAILED, exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, @NonNull Cache cache, @NonNull Object key, Object value) {
        log.warn(ExceptionUtils.CACHING_FAILED, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
        log.warn(ExceptionUtils.CACHING_FAILED, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, @NonNull Cache cache) {
        log.warn(ExceptionUtils.CACHING_FAILED, exception.getMessage());
    }

}
