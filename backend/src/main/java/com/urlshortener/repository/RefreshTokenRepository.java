package com.urlshortener.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.urlshortener.entity.RefreshToken;
import com.urlshortener.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,UUID>{
    
    Optional<RefreshToken> findByToken(String token);
    
    void deleteByUser(User user);
}
