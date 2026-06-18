package com.urlshortener.config;

import java.io.IOException;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CorrelationIdFilter  extends OncePerRequestFilter{
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Using Existing Header
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }


        // PUT in MDC so all logs during this request include it 
        MDC.put(MDC_KEY, correlationId);

        // Add it to response headers so client can trace
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // Clean up MDC after request to avoid memory leaks
            MDC.remove(MDC_KEY);
        }
    }

}
