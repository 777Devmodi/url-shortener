package com.urlshortener.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urlshortener.entity.Url;
import com.urlshortener.entity.User;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Encoder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlRepository urlRepository;
    private static final SecureRandom RANDOM = new SecureRandom();
    
    @Transactional
    public Url shorten(String originalUrl , User user, Long ttlMinutes){
        // 1. Generate a unique short code 
        String shortCode = generateUniqueShortCode();

        // 2. Build Url entity

        Url url  = Url.builder().originalUrl(originalUrl)
                    .shortCode(shortCode)
                    .user(user)
                    .clickCount(0)
                    .isActive(true)
                    .createdAt(Instant.now())
                    .expiresAt(ttlMinutes != null ? Instant.now().plusMillis(ttlMinutes*60) : null)
                    .build();
        return urlRepository.save(url);
    }

    @Transactional(readOnly = true)
    public Optional<Url> getByShortCode(String shortCode){
        return urlRepository.findByShortCode(shortCode);
    }

    @Transactional
    public Optional<Url> incrementClickAndGet(String shortCode){
        Optional<Url>  optUrl = urlRepository.findByShortCode(shortCode);
        optUrl.ifPresent(url -> {
            url.setClickCount(url.getClickCount() + 1);
            urlRepository.save(url);
        }
    );
        return optUrl;
    }

    private String generateUniqueShortCode(){
        String shortCode;
        do {
            long randomNumber =  Math.abs(RANDOM.nextLong());
            shortCode = Base62Encoder.encoder(randomNumber);
        } while (urlRepository.existsByShortCode(shortCode));
        return shortCode;
    }
    
}
