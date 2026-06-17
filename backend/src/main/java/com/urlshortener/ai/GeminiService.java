package com.urlshortener.ai;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

     /**
     * Asynchronously analyse a URL using Gemini.
     * This method does not block the caller; it runs on a separate thread.
     */
    @Async
    public CompletableFuture<UrlAnalysis> analyzeUrl(String url){

        try {
         String prompt = String.format("""
                Analyze this URL: "%s"
                Respond ONLY with a valid JSON object (no additional text) containing:
                - "category": a short category like "Technology", "News", "Shopping", "Social", "Education", etc.
                - "isPhishing": true if the URL looks suspicious or malicious, false otherwise
                - "confidence": a number between 0.0 and 1.0
                """, url);
            // build request

            Map<String,Object> requestBody = Map.of(
                "contents" , new Object[]{
                    Map.of("parts", new Object[]{
                        Map.of("text", prompt)
                    })
                }
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
             HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
             String fullUrl = apiUrl + "?key=" + apiKey;
             ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

             // Parse Gemini’s JSON response
             String responseBody = response.getBody();
             JsonNode root = objectMapper.readTree(responseBody);
             JsonNode candidates = root.get("candidates");
             if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                JsonNode parts = content.get("parts");
                if (parts != null && parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).get("text").asText();
                    // The text should be the JSON we requested
                    JsonNode analysisNode = objectMapper.readTree(text);
                    UrlAnalysis analysis = new UrlAnalysis();
                    analysis.setCategory(analysisNode.get("category").asText());
                    analysis.setPhishing(analysisNode.get("isPhishing").asBoolean());
                    analysis.setConfidence(analysisNode.get("confidence").asDouble());
                    return CompletableFuture.completedFuture(analysis);
                }
             } 
               throw new RuntimeException("Unable to parse Gemini response");
            }catch (Exception e) {
            log.error("Gemini analysis failed for URL: {}", url, e);
            // Return a default safe analysis
            UrlAnalysis fallback = new UrlAnalysis("Uncategorized", false, 0.0);
            return CompletableFuture.completedFuture(fallback);
        }
    }
}
