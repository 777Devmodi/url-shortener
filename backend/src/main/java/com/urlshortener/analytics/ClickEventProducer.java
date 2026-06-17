package com.urlshortener.analytics;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClickEventProducer {
    private final StringRedisTemplate redisTemplate;

    @Value("${app.redis.stream.click-events}")
    private String streamKey;

    public void publishClickEvent(String shortCode, String ip, String userAgent,String referer){
        Map<String, String> eventData = Map.of(
            "shortCode",shortCode,
            "ip",ip!=null ? ip:"unknown",
            "userAgent",userAgent != null ? userAgent : "unknown",
            "referer",referer != null ? referer : "unknown",
            "timestamp",Instant.now().toString()
        );

        MapRecord<String, String, String> record = StreamRecords.newRecord()
        .ofMap(eventData)
        .withStreamKey(streamKey);
        redisTemplate.opsForStream().add(record);
    }
}
