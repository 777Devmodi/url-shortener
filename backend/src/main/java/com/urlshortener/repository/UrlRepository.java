package com.urlshortener.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.urlshortener.entity.Url;

public interface UrlRepository extends JpaRepository<Url,UUID> {

    Optional<Url> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
} 
