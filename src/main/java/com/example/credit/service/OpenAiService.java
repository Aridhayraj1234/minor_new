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
        sb.append("📋 AI RISK ANALYSIS REPORT\n");
        sb.append("==========================\n\n");
        
        sb.append("📊 VERDICT: ");
        if (prediction.equalsIgnoreCase("LOW")) {
            sb.append("✅ RECOMMEND APPROVAL\n");
        } else if (prediction.equalsIgnoreCase("MEDIUM")) {
            sb.append("⚠️ RECOMMEND MANUAL REVIEW\n");
        } else {
            sb.append("❌ RECOMMEND REJECTION\n");
        }
        
        sb.append("🔢 Risk Score: ").append(String.format("%.1f", score * 100)).append("%\n\n");

        if (!strengths.isEmpty()) {
            sb.append("📈 PROS (Strengths):\n");
            for (String s : strengths) sb.append("- ").append(s).append("\n");
            sb.append("\n");
        }
        
        if (!riskFactors.isEmpty()) {
            sb.append("📉 CONS (Risk Factors):\n");
            for (String r : riskFactors) sb.append("- ").append(r).append("\n");
            sb.append("\n");
        }
        
        if (!recommendations.isEmpty()) {
            sb.append("💡 ADVISORY / RECOMMENDATIONS:\n");
            for (String rec : recommendations) sb.append("- ").append(rec).append("\n");
        } else {
            sb.append("💡 ADVISORY: Profile is stable. Standard proceeding is recommended.");
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
        String prompt = "You are a professional financial risk analyst.\n\n" +
                "User Data: " + userData + "\n" +
                "Risk Level: " + prediction + "\n" +
                "Risk Probability: " + score + "\n\n" +
                "Generate a structured report with the following sections:\n" +
                "1. 📊 VERDICT: (Recommend Approval/Review/Rejection)\n" +
                "2. 📈 PROS: (List 2-3 key strengths)\n" +
                "3. 📉 CONS: (List 2-3 key risk factors)\n" +
                "4. 💡 ADVISORY: (Final professional advice)\n" +
                "Use emojis and professional language.";

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", "You are a expert financial analyst. Provide structured, clear reports."),
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

    public String chat(String message) {
        if (apiKey == null || apiKey.isBlank() || "your_default_key_here".equals(apiKey)) {
            return simulateChat(message);
        }
        
        String url = "https://api.openai.com/v1/chat/completions";
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", "You are a helpful assistant for a Credit Risk Prediction system. Help users with system navigation, credit score tips, or general banking inquiries."),
                Map.of("role", "user", "content", message)
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
                    Map<String, Object> responseMessage = (Map<String, Object>) choices.get(0).get("message");
                    return (String) responseMessage.get("content");
                }
            }
        } catch (Exception e) {
            System.err.println("OpenAI Chat Error: " + e.getMessage());
        }
        return simulateChat(message);
    }

    private String simulateChat(String message) {
        String msg = message.toLowerCase();
        
        if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey")) 
            return "Hello! I'm your Credit Risk Assistant. How can I help you navigate the system or understand credit risk today?";
        
        if (msg.contains("credit score") || msg.contains("score")) 
            return "A credit score is a numerical expression of a person's creditworthiness. Higher scores (above 700) generally lead to better loan terms and lower interest rates.";
            
        if (msg.contains("risk") || msg.contains("level")) 
            return "We categorize risk into LOW, MEDIUM, and HIGH. LOW risk usually means a high probability of approval, while HIGH risk indicates significant concerns that may lead to rejection.";
            
        if (msg.contains("admin") || msg.contains("dashboard") || msg.contains("manage")) 
            return "The Admin Dashboard allows you to monitor all registered users, review the complete history of credit applications, and adjust user roles.";
            
        if (msg.contains("loan") || msg.contains("amount") || msg.contains("debt")) 
            return "Loan amounts and existing debt are critical factors in our ML model. A high debt-to-income ratio is one of the most common reasons for a 'HIGH' risk classification.";
            
        if (msg.contains("history") || msg.contains("entries"))
            return "You can view the full history of all predictions in the 'Credit Entry History' section. Use the search bar there to find specific users or transactions.";

        if (msg.contains("thank") || msg.contains("thanks"))
            return "You're very welcome! Let me know if you have any other questions.";

        if (msg.contains("help") || msg.contains("how"))
            return "I can help you understand how our risk scores are calculated, explain the different risk levels, or help you find information in the dashboard. What would you like to know?";

        String[] defaults = {
            "That's a great question! While I'm in simulation mode, I can tell you that our system uses 13 different financial factors to predict credit risk accurately.",
            "I'm not quite sure about that specific query, but I can assist with questions about credit scores, risk factors, or using the admin panel.",
            "Interesting! Did you know that keeping your credit utilization below 30% is one of the best ways to maintain a healthy credit score?",
            "I'm here to help! Try asking about 'risk levels', 'credit scores', or how to 'manage users'."
        };
        int index = Math.abs(message.hashCode()) % defaults.length;
        return defaults[index];
    }
}
