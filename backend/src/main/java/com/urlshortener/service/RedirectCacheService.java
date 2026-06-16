package com.urlshortener.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedirectCacheService {
    private final StringRedisTemplate redisTemplate;
    private static final String CACHE_PREFIX = "redirect:";
    private static final Duration TTL = Duration.ofMinutes(30);

    public void cacheUrl(String shortCode, String originalUrl){
        String key = CACHE_PREFIX+shortCode;
        redisTemplate.opsForValue().set(key,originalUrl,TTL);
    }

    public String getOriginalValue(String shortCode){
         String key = CACHE_PREFIX + shortCode;
         return redisTemplate.opsForValue().get(key);
    }

    public void evict(String shortCode){
        String key = CACHE_PREFIX+shortCode;
        redisTemplate.delete(key);
    }
}
