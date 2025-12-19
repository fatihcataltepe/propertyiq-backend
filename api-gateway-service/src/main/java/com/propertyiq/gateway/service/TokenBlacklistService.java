package com.propertyiq.gateway.service;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final ReactiveStringRedisTemplate redisTemplate;

    public TokenBlacklistService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> isBlacklisted(String jti) {
        if (jti == null || jti.isEmpty()) {
            return Mono.just(false);
        }
        String key = BLACKLIST_PREFIX + jti;
        return redisTemplate.hasKey(key);
    }
}
