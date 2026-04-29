package com.example.credit.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_applications")
public class CreditApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ML Model fields
    private Integer age;
    private Double income;
    @Column(name = "credit_score")
    private Integer creditScore;
    @Column(name = "loan_amount")
    private Double loanAmount;
    @Column(name = "employment_status")
    private String employmentStatus;
    @Column(name = "credit_utilization")
    private Double creditUtilization;
    @Column(name = "existing_loans")
    private Integer existingLoans;
    @Column(name = "payment_history")
    private String paymentHistory;

    // New fields for real dataset
    private String gender;
    private String education;
    private Double debt;
    @Column(name = "loan_term")
    private Integer loanTerm;
    @Column(name = "num_credit_cards")
    private Integer numCreditCards;
    @Column(name = "residence_type")
    private String residenceType;
    @Column(name = "marital_status")
    private String maritalStatus;

    // Results
    @Column(name = "prediction_result")
    private String predictionResult; // LOW, MEDIUM, HIGH

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "llm_explanation", columnDefinition = "TEXT")
    private String llmExplanation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Double getIncome() { return income; }
    public void setIncome(Double income) { this.income = income; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public Double getLoanAmount() { return loanAmount; }
    public void setLoanAmount(Double loanAmount) { this.loanAmount = loanAmount; }
    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
    public Double getCreditUtilization() { return creditUtilization; }
    public void setCreditUtilization(Double creditUtilization) { this.creditUtilization = creditUtilization; }
    public Integer getExistingLoans() { return existingLoans; }
    public void setExistingLoans(Integer existingLoans) { this.existingLoans = existingLoans; }
    public String getPaymentHistory() { return paymentHistory; }
    public void setPaymentHistory(String paymentHistory) { this.paymentHistory = paymentHistory; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public Double getDebt() { return debt; }
    public void setDebt(Double debt) { this.debt = debt; }
    public Integer getLoanTerm() { return loanTerm; }
    public void setLoanTerm(Integer loanTerm) { this.loanTerm = loanTerm; }
    public Integer getNumCreditCards() { return numCreditCards; }
    public void setNumCreditCards(Integer numCreditCards) { this.numCreditCards = numCreditCards; }
    public String getResidenceType() { return residenceType; }
    public void setResidenceType(String residenceType) { this.residenceType = residenceType; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

    public String getPredictionResult() { return predictionResult; }
    public void setPredictionResult(String predictionResult) { this.predictionResult = predictionResult; }
    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }
    public String getLlmExplanation() { return llmExplanation; }
    public void setLlmExplanation(String llmExplanation) { this.llmExplanation = llmExplanation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
