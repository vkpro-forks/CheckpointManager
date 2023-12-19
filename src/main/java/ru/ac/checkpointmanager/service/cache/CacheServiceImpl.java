package ru.ac.checkpointmanager.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@Slf4j
public class CacheServiceImpl implements CacheService {

    private final CacheManager cacheManager;

    private int hourForLogInScheduledCheck;

    @Autowired
    public CacheServiceImpl(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Очищает все кэши, управляемые CacheManager.
     */
    @Override
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Scheduled(cron = "0 * * * * ?")
    public void clearCacheOfVerifiedTokens() {
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() != hourForLogInScheduledCheck) {
            hourForLogInScheduledCheck = now.getHour();
            log.debug("Scheduled {} continues to work", MethodLog.getMethodName());
        }
        Stream.of("registration", "email")
                .map(cacheManager::getCache)
                .filter(Objects::nonNull)
                .forEach(Cache::clear);
    }
}
