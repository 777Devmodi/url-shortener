package com.urlshortener.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    
    private final StringRedisTemplate redisTemplate;

    @Value("${app.rate-limit.capacity}")
    private int capacity;

    @Value("${app.rate-limit.refill-rate}")
    private double refillRate;  // token per second

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    // Lua script for atomic check-and-consume
    private static final String LUA_SCRIPT = """
        local key = KEYS[1]
        local capacity = tonumber(ARGV[1])
        local refill_rate = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])

            local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
            local tokens = tonumber(bucket[1])
            local last_refill = tonumber(bucket[2])

            if tokens == nil then
                -- first request: initialize bucket full
                tokens = capacity
                last_refill = now
            else
                -- refill tokens based on elapsed time
                local elapsed_ms = now - last_refill
                local tokens_to_add = (elapsed_ms / 1000) * refill_rate
                tokens = math.min(capacity, tokens + tokens_to_add)
            end

            if tokens >= 1 then
                tokens = tokens - 1
                redis.call('HMSET', key, 'tokens', tokens, 'last_refill', now)
                redis.call('EXPIRE', key, 3600)  -- cleanup unused keys after 1 hour
                return 1
            else
                return 0
            end
            """;

        public boolean isAllowed(String identifier){
            String key = RATE_LIMIT_PREFIX+identifier;
            Instant now = Instant.now();

            DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA_SCRIPT,Long.class);
            List<String> keys = Collections.singletonList(key);
            Long result = redisTemplate.execute(
                script,
                keys,
                String.valueOf(capacity),
                String.valueOf(refillRate),
                String.valueOf(now.toEpochMilli())
            );
            return result != null && result == 1L;
        }
}
