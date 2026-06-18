package com.urlshortener.config;
// To add custom health data, we can create a HealthIndicator bean

import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class CacheHealthIndicator implements HealthIndicator{
    private final RedisConnectionFactory connectionFactory;

    public CacheHealthIndicator(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public  Health health() {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            String pong = connection.ping();
            if ("PONG".equals(pong)) {
                return Health.up().withDetail("redis","reachable").build();
            }
        } catch (Exception e) {
            return Health.down().withDetail("redis", "unreachable").build();
        }
        return Health.unknown().build();
    }
}
