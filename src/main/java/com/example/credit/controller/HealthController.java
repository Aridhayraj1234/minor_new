package com.example.credit.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {

    @Value("${ml.api.base-url:http://localhost:5000}")
    private String mlApiBaseUrl;

    @Value("${openai.api.key}")
    private String openAiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/health")
    public ResponseEntity<?> getHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("backend", "UP");

        // Check ML Service
        try {
            ResponseEntity<Map> mlResponse = restTemplate.getForEntity(mlApiBaseUrl + "/health", Map.class);
            status.put("ml_service", mlResponse.getBody());
        } catch (Exception e) {
            status.put("ml_service", "DOWN (Connection Refused)");
        }

        // Check LLM Configuration
        if (openAiKey == null || openAiKey.isBlank() || "your_default_key_here".equals(openAiKey)) {
            status.put("llm_model", "SIMULATED (No API Key)");
        } else {
            status.put("llm_model", "ENABLED");
        }

        return ResponseEntity.ok(status);
    }
}
