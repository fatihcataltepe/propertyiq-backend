package com.propertyiq.gateway.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.ECKey;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.PublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwksKeyProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwksKeyProvider.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Value("${supabase.url:https://your-project.supabase.co}")
    private String supabaseUrl;

    private final WebClient webClient;
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private volatile Instant cacheExpiresAt = Instant.MIN;

    public JwksKeyProvider(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostConstruct
    public void init() {
        logger.info("JwksKeyProvider initialized with Supabase URL: {}", supabaseUrl);
    }

    public Mono<PublicKey> getKey(String kid) {
        if (isCacheValid() && keyCache.containsKey(kid)) {
            logger.debug("Returning cached key for kid: {}", kid);
            return Mono.just(keyCache.get(kid));
        }

        return refreshKeys()
                .flatMap(keys -> {
                    PublicKey key = keys.get(kid);
                    if (key == null) {
                        logger.warn("Key not found for kid: {} after refresh", kid);
                        return Mono.error(new IllegalArgumentException("Unknown key ID: " + kid));
                    }
                    return Mono.just(key);
                });
    }

    private boolean isCacheValid() {
        return Instant.now().isBefore(cacheExpiresAt);
    }

    private Mono<Map<String, PublicKey>> refreshKeys() {
        String jwksUrl = supabaseUrl + "/auth/v1/.well-known/jwks.json";
        logger.debug("Fetching JWKS from: {}", jwksUrl);

        return webClient.get()
                .uri(jwksUrl)
                .retrieve()
                .bodyToMono(String.class)
                .publishOn(Schedulers.boundedElastic())
                .map(this::parseJwks)
                .doOnSuccess(keys -> {
                    keyCache.clear();
                    keyCache.putAll(keys);
                    cacheExpiresAt = Instant.now().plus(CACHE_TTL);
                    logger.info("JWKS cache refreshed with {} keys, expires at: {}", keys.size(), cacheExpiresAt);
                })
                .doOnError(e -> logger.error("Failed to fetch JWKS: {}", e.getMessage()));
    }

    private Map<String, PublicKey> parseJwks(String jwksJson) {
        Map<String, PublicKey> keys = new ConcurrentHashMap<>();
        try {
            JWKSet jwkSet = JWKSet.parse(jwksJson);
            for (JWK jwk : jwkSet.getKeys()) {
                String kid = jwk.getKeyID();
                if (kid == null) {
                    logger.warn("JWK without kid found, skipping");
                    continue;
                }
                try {
                    PublicKey publicKey = extractPublicKey(jwk);
                    if (publicKey != null) {
                        keys.put(kid, publicKey);
                        logger.debug("Loaded key with kid: {}, algorithm: {}", kid, jwk.getAlgorithm());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to extract public key for kid {}: {}", kid, e.getMessage());
                }
            }
        } catch (ParseException e) {
            logger.error("Failed to parse JWKS JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to parse JWKS", e);
        }
        return keys;
    }

    private PublicKey extractPublicKey(JWK jwk) throws Exception {
        if (jwk instanceof RSAKey rsaKey) {
            return rsaKey.toRSAPublicKey();
        } else if (jwk instanceof ECKey ecKey) {
            return ecKey.toECPublicKey();
        } else {
            logger.warn("Unsupported key type: {}", jwk.getKeyType());
            return null;
        }
    }

    public String getSupabaseUrl() {
        return supabaseUrl;
    }
}
