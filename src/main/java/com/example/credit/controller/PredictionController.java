package com.example.credit.controller;

import com.example.credit.dto.PredictionRequest;
import com.example.credit.dto.PredictionResponse;
import com.example.credit.entity.CreditApplication;
import com.example.credit.entity.User;
import com.example.credit.repository.CreditApplicationRepository;
import com.example.credit.repository.UserRepository;
import com.example.credit.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PredictionController {

    @Autowired
    private CreditApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OpenAiService openAiService;

    @Value("${ml.api.base-url:http://localhost:5000}")
    private String mlApiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestBody PredictionRequest payload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        // 1. Call Flask ML model
        String flaskUrl = mlApiBaseUrl + "/predict";
        Map<String, Object> flaskRequest = new HashMap<>();
        flaskRequest.put("age", payload.getAge());
        flaskRequest.put("income", payload.getIncome());
        flaskRequest.put("credit_score", payload.getCreditScore());
        flaskRequest.put("loan_amount", payload.getLoanAmount());
        flaskRequest.put("employment_status", payload.getEmploymentStatus());
        flaskRequest.put("payment_history", payload.getPaymentHistory());
        
        // New fields
        flaskRequest.put("gender", payload.getGender());
        flaskRequest.put("education", payload.getEducation());
        flaskRequest.put("debt", payload.getDebt());
        flaskRequest.put("loan_term", payload.getLoanTerm());
        flaskRequest.put("num_credit_cards", payload.getNumCreditCards());
        flaskRequest.put("residence_type", payload.getResidenceType());
        flaskRequest.put("marital_status", payload.getMaritalStatus());

        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(flaskUrl, flaskRequest, Map.class);
        } catch (Exception e) {
            return ResponseEntity.status(503).body("ML Model Service unavailable. Ensure Flask is on " + flaskUrl);
        }

        Map<String, Object> body = response.getBody();
        if (body == null || body.get("probability") == null) {
            return ResponseEntity.status(500).body("ML model returned incomplete response.");
        }
        Double probabilityValue = ((Number) body.get("probability")).doubleValue();

        // ML model: probability = probability of being creditworthy (1 = approved)
        double riskProbability = 1.0 - probabilityValue;

        // 2. Determine Risk Level
        String riskLevel;
        String approvalStatus;
        if (probabilityValue >= 0.70) {
            riskLevel = "LOW";
            approvalStatus = "APPROVED";
        } else if (probabilityValue >= 0.40) {
            riskLevel = "MEDIUM";
            approvalStatus = "REVIEW";
        } else {
            riskLevel = "HIGH";
            approvalStatus = "DECLINED";
        }

        // 3. Call OpenAI for Explanation
        String userData = String.format("Income: %s, Employment: %s, Age: %s, Credit Score: %s, Loan Amount: %s, Debt: %s, Education: %s",
                payload.getIncome(), payload.getEmploymentStatus(), payload.getAge(),
                payload.getCreditScore(), payload.getLoanAmount(), payload.getDebt(), payload.getEducation());

        String explanation = openAiService.generateExplanation(userData, riskLevel, riskProbability);

        // 4. Save to Database
        CreditApplication application = new CreditApplication();
        application.setUser(user);
        application.setAge(payload.getAge());
        application.setIncome(payload.getIncome());
        application.setCreditScore(payload.getCreditScore());
        application.setLoanAmount(payload.getLoanAmount());
        application.setEmploymentStatus(payload.getEmploymentStatus());
        application.setPaymentHistory(payload.getPaymentHistory());
        
        // New fields
        application.setGender(payload.getGender());
        application.setEducation(payload.getEducation());
        application.setDebt(payload.getDebt());
        application.setLoanTerm(payload.getLoanTerm());
        application.setNumCreditCards(payload.getNumCreditCards());
        application.setResidenceType(payload.getResidenceType());
        application.setMaritalStatus(payload.getMaritalStatus());
        
        application.setPredictionResult(riskLevel);
        application.setRiskScore(riskProbability);
        application.setLlmExplanation(explanation);

        applicationRepository.save(application);

        // 5. Return response
        PredictionResponse predictionResponse = new PredictionResponse(riskLevel, riskProbability, explanation, approvalStatus);
        return ResponseEntity.ok(predictionResponse);
    }
}
