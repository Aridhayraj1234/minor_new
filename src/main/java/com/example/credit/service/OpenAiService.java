package com.example.credit.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {
    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateExplanation(String userData, String prediction, Double score) {
        if (apiKey == null || apiKey.isBlank() || "your_default_key_here".equals(apiKey)) {
            return generateSmartSimulation(userData, prediction, score);
        }
        return callOpenAi(userData, prediction, score);
    }

    private String generateSmartSimulation(String userData, String prediction, Double score) {
        Map<String, String> fields = parseUserData(userData);

        double income = parseDouble(fields.get("Income"), 0);
        int creditScore = parseInt(fields.get("Credit Score"), 0);
        double loanAmount = parseDouble(fields.get("Loan Amount"), 0);
        double debt = parseDouble(fields.get("Debt"), 0);
        String education = fields.getOrDefault("Education", "Bachelor");
        String employment = fields.getOrDefault("Employment", "UNKNOWN");
        int age = parseInt(fields.get("Age"), 30);

        List<String> riskFactors = new ArrayList<>();
        List<String> strengths = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // Analyze Credit Score
        if (creditScore >= 750) {
            strengths.add("excellent credit score (" + creditScore + ")");
        } else if (creditScore >= 670) {
            strengths.add("good credit score (" + creditScore + ")");
        } else if (creditScore >= 580) {
            riskFactors.add("below-average credit score (" + creditScore + ")");
            recommendations.add("improve credit score by paying all bills on time");
        } else {
            riskFactors.add("poor credit score (" + creditScore + ")");
            recommendations.add("urgently rebuild credit by disputing errors");
        }

        // Analyze Debt-to-Income
        if (income > 0) {
            double dti = debt / income;
            if (dti > 0.4) {
                riskFactors.add("high debt-to-income ratio (" + String.format("%.0f", dti * 100) + "%)");
                recommendations.add("reduce outstanding debt before taking new loans");
            } else if (dti < 0.2) {
                strengths.add("low debt-to-income ratio");
            }
        }

        // Analyze Education
        if ("PhD".equalsIgnoreCase(education) || "Master".equalsIgnoreCase(education)) {
            strengths.add("high level of education (" + education + ")");
        }

        // Analyze Employment
        if ("Unemployed".equalsIgnoreCase(employment)) {
            riskFactors.add("currently unemployed");
            recommendations.add("secure stable employment");
        } else {
            strengths.add("stable employment");
        }

        // Analyze Income vs Loan Amount
        if (income > 0 && loanAmount > 0) {
            double ratio = loanAmount / income;
            if (ratio > 2.0) {
                riskFactors.add("loan amount is twice the annual income");
                recommendations.add("consider a smaller loan amount");
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📊 Risk Score: ").append(String.format("%.1f", score * 100)).append("% | Level: ").append(prediction).append("\n\n");

        if (!strengths.isEmpty()) {
            sb.append("✅ Strengths: ").append(String.join(", ", strengths)).append(".\n\n");
        }
        if (!riskFactors.isEmpty()) {
            sb.append("⚠️ Risk Factors: ").append(String.join("; ", riskFactors)).append(".\n\n");
        }
        if (!recommendations.isEmpty()) {
            sb.append("💡 Recommendations: (1) ").append(String.join(". (2) ", recommendations)).append(".");
        } else {
            sb.append("💡 Recommendation: Strong profile. Standard approval process may proceed.");
        }

        return sb.toString();
    }

    private Map<String, String> parseUserData(String userData) {
        Map<String, String> result = new HashMap<>();
        String[] parts = userData.split(",\\s*");
        for (String part : parts) {
            int colonIdx = part.indexOf(":");
            if (colonIdx != -1) {
                String key = part.substring(0, colonIdx).trim();
                String value = part.substring(colonIdx + 1).trim();
                result.put(key, value);
            }
        }
        return result;
    }

    private double parseDouble(String val, double def) {
        if (val == null) return def;
        try { return Double.parseDouble(val.trim()); } catch (Exception e) { return def; }
    }

    private int parseInt(String val, int def) {
        if (val == null) return def;
        try { return (int) Double.parseDouble(val.trim()); } catch (Exception e) { return def; }
    }

    private String callOpenAi(String userData, String prediction, Double score) {
        String url = "https://api.openai.com/v1/chat/completions";
        String prompt = "You are a financial risk analyst.\n\nUser Data: " + userData +
                "\nRisk Level: " + prediction + "\nRisk Probability: " + score +
                "\n\nProvide a professional 3-4 sentence analysis with actionable advice.";

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", "You are a financial risk analyst. Be concise and professional."),
                Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            System.err.println("OpenAI API Error: " + e.getMessage());
        }
        return generateSmartSimulation(userData, prediction, score);
    }
}
