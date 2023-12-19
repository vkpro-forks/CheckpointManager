package ru.ac.checkpointmanager.service.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {

    private final CacheManager cacheManager;

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
}
