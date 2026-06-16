package com.urlshortener.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.urlshortener.service.RateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {
    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Determine identifier : authenticated user email or IP address
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();

        String identifier;
        if (auth != null && auth.isAuthenticated() && "anonymousUser".equals(auth.getPrincipal())) {
            identifier = (String) auth.getPrincipal(); // email
        }else{
            identifier = request.getRemoteAddr(); // fallback IP
        }

        if (!rateLimiterService.isAllowed(identifier)) {
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded. Try Again Later");
            return;
        }
        filterChain.doFilter(request, response);
        
    }

}
