package com.urlshortener.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urlshortener.dto.SignUpRequest;
import com.urlshortener.entity.User;
import com.urlshortener.exception.UserAlreadyExistsException;
import com.urlshortener.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;    

    @Transactional
    public User signup (SignUpRequest request){
        // checking if the email exist or not 
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email" + request.getEmail() + "already exists.");
        }

        // build user with hashed password
        User user = User.builder().email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .build();

        return userRepository.save(user);

    }

}
