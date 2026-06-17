package com.urlshortener.analytics;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.urlshortener.entity.ClickEvent;
import com.urlshortener.entity.Url;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.UrlRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClickEventConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private final ClickEventRepository clickEventRepository;
    private final UrlRepository urlRepository;

    @Value("${app.redis.stream.click-events}")
    private String streamKey;

    @Value("${app.redis.stream.consumer-group}")
    private String consumerGroup;

    @PostConstruct
    public void start() {
        container.receive(
                Consumer.from(consumerGroup, "instance-1"),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                this
        );
    }

    @Override
    @Transactional
    public void onMessage(MapRecord<String, String, String> message) {
        Map<String, String> data = message.getValue();
        String shortCode = data.get("shortCode");

        ClickEvent event = ClickEvent.builder()
                .url(null)
                .clickedAt(Instant.parse(data.get("timestamp")))
                .ipAddress(data.get("ip"))
                .userAgent(data.get("userAgent"))
                .referer(data.get("referer"))
                .build();

        Optional<Url> optUrl = urlRepository.findByShortCode(shortCode);
        if (optUrl.isPresent()) {
            Url url = optUrl.get();
            event.setUrl(url);
            clickEventRepository.save(event);

            url.setClickCount(url.getClickCount() + 1);
            urlRepository.save(url);

            log.debug("Processed click event for {}", shortCode);
        } else {
            log.warn("URL not found for shortCode: {}", shortCode);
        }
    }
}