package com.urlshortener.service;

import java.time.Instant;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urlshortener.dto.AuthResponse;
import com.urlshortener.dto.LoginRequest;
import com.urlshortener.dto.RefreshTokenRequest;
import com.urlshortener.entity.RefreshToken;
import com.urlshortener.entity.User;
import com.urlshortener.repository.RefreshTokenRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder  passwordEncoder;

    @Transactional
    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(()->  new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // access Token
        String accessToken =  jwtUtil.generateJwtToken(request.getEmail());
        String refreshTokenStr = jwtUtil.generateRefreshToken(user.getEmail());

        // Save refresh token to DB 
         RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(Instant.now().plusMillis(
                        jwtUtil.getRefreshTokenExpirationMs()))   // you’ll need a getter
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpirationMs() / 1000) // in seconds
                .build();
    } 


    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request){
        String requestToken = request.getRefreshToken();

        // Validate Jwt signature and expiration 
        if (!jwtUtil.validateRefreshToken(requestToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        // Fetch stored Token
        RefreshToken storedToken = refreshTokenRepository.findByToken(requestToken)
        .orElseThrow(()-> new BadCredentialsException("Refresh token not found"));

        // Check if revoked or expired 
        if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token revoked or expired");
        }

        // extract email from token 
        String email = jwtUtil.extractEmailFromRefreshToken(requestToken);
        
        // Rotation: revoke old token , issue new one 
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);


        String newAccessToken = jwtUtil.generateJwtToken(email);
        String newRefreshTokenStr =  jwtUtil.generateRefreshToken(email);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(storedToken.getUser())
                .token(newRefreshTokenStr)
                .expiresAt(Instant.now().plusMillis(
                        jwtUtil.getRefreshTokenExpirationMs()))
                .build();
        
                refreshTokenRepository.save(newRefreshToken);
        
                return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpirationMs() / 1000)
                .build();
       }

       @Transactional
       public void logout(String refreshTokenStr){
        refreshTokenRepository.findByToken(refreshTokenStr)
        .ifPresent(token -> {
             token.setRevoked(true);
             refreshTokenRepository.save(token);
        }
        );
       }
}
