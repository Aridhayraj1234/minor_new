package com.example.credit.dto;

public class PredictionResponse {
    private String riskLevel;
    private Double probabilityValue;
    private String explanation;
    private String approvalStatus;

    public PredictionResponse(String riskLevel, Double probabilityValue, String explanation, String approvalStatus) {
        this.riskLevel = riskLevel;
        this.probabilityValue = probabilityValue;
        this.explanation = explanation;
        this.approvalStatus = approvalStatus;
    }

    public String getRiskLevel() { return riskLevel; }
    public Double getProbabilityValue() { return probabilityValue; }
    public String getExplanation() { return explanation; }
    public String getApprovalStatus() { return approvalStatus; }
}
