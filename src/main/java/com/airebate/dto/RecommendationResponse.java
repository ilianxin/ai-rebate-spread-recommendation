package com.airebate.dto;

import com.airebate.model.Currency;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Rebate Spread推荐响应DTO
 */
public class RecommendationResponse {
    
    private String customerCode;
    private String customerName;
    private Currency currency;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recommendationDate;
    
    private BigDecimal recommendedSpread;
    private BigDecimal confidenceScore;
    private BigDecimal riskAdjustment;
    private BigDecimal volatilityFactor;
    private BigDecimal volumeFactor;
    private BigDecimal historicalPerformance;
    private String recommendationReason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validUntil;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;
    
    private String status; // SUCCESS, WARNING, ERROR
    private String message;
    
    // LLM相关字段
    private String llmProvider;
    private String llmModel;
    private String riskAssessmentDetail;
    private String marketAnalysisDetail;
    private String[] keyFactors;
    private boolean usedLLM;

    // 构造函数
    public RecommendationResponse() {
        this.generatedAt = LocalDateTime.now();
        this.status = "SUCCESS";
    }

    // 静态工厂方法
    public static RecommendationResponse success(String customerCode, String customerName, 
                                               Currency currency, LocalDate recommendationDate,
                                               BigDecimal recommendedSpread) {
        RecommendationResponse response = new RecommendationResponse();
        response.setCustomerCode(customerCode);
        response.setCustomerName(customerName);
        response.setCurrency(currency);
        response.setRecommendationDate(recommendationDate);
        response.setRecommendedSpread(recommendedSpread);
        return response;
    }

    public static RecommendationResponse error(String message) {
        RecommendationResponse response = new RecommendationResponse();
        response.setStatus("ERROR");
        response.setMessage(message);
        return response;
    }

    public static RecommendationResponse warning(String message) {
        RecommendationResponse response = new RecommendationResponse();
        response.setStatus("WARNING");
        response.setMessage(message);
        return response;
    }

    // Getters and Setters
    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public LocalDate getRecommendationDate() {
        return recommendationDate;
    }

    public void setRecommendationDate(LocalDate recommendationDate) {
        this.recommendationDate = recommendationDate;
    }

    public BigDecimal getRecommendedSpread() {
        return recommendedSpread;
    }

    public void setRecommendedSpread(BigDecimal recommendedSpread) {
        this.recommendedSpread = recommendedSpread;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public BigDecimal getRiskAdjustment() {
        return riskAdjustment;
    }

    public void setRiskAdjustment(BigDecimal riskAdjustment) {
        this.riskAdjustment = riskAdjustment;
    }

    public BigDecimal getVolatilityFactor() {
        return volatilityFactor;
    }

    public void setVolatilityFactor(BigDecimal volatilityFactor) {
        this.volatilityFactor = volatilityFactor;
    }

    public BigDecimal getVolumeFactor() {
        return volumeFactor;
    }

    public void setVolumeFactor(BigDecimal volumeFactor) {
        this.volumeFactor = volumeFactor;
    }

    public BigDecimal getHistoricalPerformance() {
        return historicalPerformance;
    }

    public void setHistoricalPerformance(BigDecimal historicalPerformance) {
        this.historicalPerformance = historicalPerformance;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLlmProvider() {
        return llmProvider;
    }

    public void setLlmProvider(String llmProvider) {
        this.llmProvider = llmProvider;
    }

    public String getLlmModel() {
        return llmModel;
    }

    public void setLlmModel(String llmModel) {
        this.llmModel = llmModel;
    }

    public String getRiskAssessmentDetail() {
        return riskAssessmentDetail;
    }

    public void setRiskAssessmentDetail(String riskAssessmentDetail) {
        this.riskAssessmentDetail = riskAssessmentDetail;
    }

    public String getMarketAnalysisDetail() {
        return marketAnalysisDetail;
    }

    public void setMarketAnalysisDetail(String marketAnalysisDetail) {
        this.marketAnalysisDetail = marketAnalysisDetail;
    }

    public String[] getKeyFactors() {
        return keyFactors;
    }

    public void setKeyFactors(String[] keyFactors) {
        this.keyFactors = keyFactors;
    }

    public boolean isUsedLLM() {
        return usedLLM;
    }

    public void setUsedLLM(boolean usedLLM) {
        this.usedLLM = usedLLM;
    }
}
