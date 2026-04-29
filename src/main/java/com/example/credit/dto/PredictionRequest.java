package com.example.credit.dto;

public class PredictionRequest {
    private Integer age;
    private Double income;
    private Integer creditScore;
    private Double loanAmount;
    private String employmentStatus;
    private Double creditUtilization;
    private Integer existingLoans;
    private String paymentHistory;
    
    // New fields for real dataset
    private String gender;
    private String education;
    private Double debt;
    private Integer loanTerm;
    private Integer numCreditCards;
    private String residenceType;
    private String maritalStatus;

    // Getters and Setters
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
}
