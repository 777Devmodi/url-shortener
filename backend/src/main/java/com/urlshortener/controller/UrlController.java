package com.urlshortener.controller;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.entity.Url;
import com.urlshortener.entity.User;
import com.urlshortener.service.UrlService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;

    @PostMapping("/urls")
    public ResponseEntity<Url> shortenUrl(@Valid @RequestBody ShortenRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user  = null;
        if (auth != null && !auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            // In a real app, we would load user by email, but for now assume User entity is the principal.
            // Actually we set email as principal. We need a way to get the actual User entity. 
            // For now, we'll pass null for anonymous; later we can improve.
            // We'll create a helper to fetch User from email. 
        }
        Url url = urlService.shorten(request.getUrl(), user, request.getTtlMinutes());
        return ResponseEntity.status(HttpStatus.CREATED).body(url);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable @NotBlank String shortCode) {
        // Optional<Url> optUrl = urlService.incrementClickAndGet(shortCode);   
        // Url url = optUrl.orElseThrow(()->
        //  new ResponseStatusException(HttpStatus.NOT_FOUND,"Short URL not Found")); 

        // if (!url.isActive() || (url.getExpiresAt() != null && url.getExpiresAt().isBefore(Instant.now()))) {
        //     throw new ResponseStatusException(HttpStatus.GONE, "Short URL expired or disabled");
        // }
        //   HttpHeaders headers = new HttpHeaders();
        // headers.setLocation(URI.create(url.getOriginalUrl()));
        // return new ResponseEntity<>(headers,HttpStatus.FOUND);

          String originalUrl = urlService.resolveOriginalUrl(shortCode);
          HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(originalUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
      
    }
    
}
