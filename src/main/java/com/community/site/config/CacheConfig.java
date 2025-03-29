package com.community.site.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

/**
 * 캐시 설정을 위한 클래스
 * application.properties에서 Caffeine 캐시 설정이 로드됩니다.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    
    /**
     * 애플리케이션 시작 시 캐시 초기화 정보를 로깅
     */
    @EventListener(ApplicationStartedEvent.class)
    public void logCacheInitialization() {
        logger.info("캐시 시스템이 초기화되었습니다. (Caffeine)");
        logger.info("설정된 캐시: posts, users (application.properties 참조)");
    }
} 