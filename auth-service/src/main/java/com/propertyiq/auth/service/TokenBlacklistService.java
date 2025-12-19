package com.propertyiq.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String jti, long ttlSeconds) {
        if (jti == null || jti.isEmpty()) {
            return;
        }
        String key = BLACKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isEmpty()) {
            return false;
        }
        String key = BLACKLIST_PREFIX + jti;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
