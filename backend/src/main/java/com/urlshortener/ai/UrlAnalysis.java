package com.urlshortener.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlAnalysis {
    private String category;       // e.g., "Technology", "Shopping"
    private boolean isPhishing;
    private double confidence;     // 0.0 - 1.0
}