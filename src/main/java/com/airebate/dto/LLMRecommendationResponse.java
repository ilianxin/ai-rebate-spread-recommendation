package com.airebate.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * LLM推荐响应DTO
 */
public class LLMRecommendationResponse {
    
    private BigDecimal recommendedSpread;
    private BigDecimal confidenceScore;
    private String reasoning;
    private String riskAssessment;
    private String marketAnalysis;
    private List<String> keyFactors;
    private Map<String, Object> additionalInsights;
    private LocalDateTime generatedAt;
    private String modelUsed;
    private boolean success;
    private String errorMessage;
    
    // 可选的替代推荐
    private List<AlternativeRecommendation> alternatives;

    // 构造函数
    public LLMRecommendationResponse() {
        this.generatedAt = LocalDateTime.now();
        this.success = true;
    }

    // 静态工厂方法
    public static LLMRecommendationResponse success(BigDecimal recommendedSpread, String reasoning) {
        LLMRecommendationResponse response = new LLMRecommendationResponse();
        response.setRecommendedSpread(recommendedSpread);
        response.setReasoning(reasoning);
        return response;
    }

    public static LLMRecommendationResponse error(String errorMessage) {
        LLMRecommendationResponse response = new LLMRecommendationResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    // 内部类：替代推荐
    public static class AlternativeRecommendation {
        private BigDecimal spread;
        private String scenario;
        private BigDecimal probability;

        public AlternativeRecommendation() {}

        public AlternativeRecommendation(BigDecimal spread, String scenario, BigDecimal probability) {
            this.spread = spread;
            this.scenario = scenario;
            this.probability = probability;
        }

        // Getters and Setters
        public BigDecimal getSpread() {
            return spread;
        }

        public void setSpread(BigDecimal spread) {
            this.spread = spread;
        }

        public String getScenario() {
            return scenario;
        }

        public void setScenario(String scenario) {
            this.scenario = scenario;
        }

        public BigDecimal getProbability() {
            return probability;
        }

        public void setProbability(BigDecimal probability) {
            this.probability = probability;
        }
    }

    // Getters and Setters
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

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public String getRiskAssessment() {
        return riskAssessment;
    }

    public void setRiskAssessment(String riskAssessment) {
        this.riskAssessment = riskAssessment;
    }

    public String getMarketAnalysis() {
        return marketAnalysis;
    }

    public void setMarketAnalysis(String marketAnalysis) {
        this.marketAnalysis = marketAnalysis;
    }

    public List<String> getKeyFactors() {
        return keyFactors;
    }

    public void setKeyFactors(List<String> keyFactors) {
        this.keyFactors = keyFactors;
    }

    public Map<String, Object> getAdditionalInsights() {
        return additionalInsights;
    }

    public void setAdditionalInsights(Map<String, Object> additionalInsights) {
        this.additionalInsights = additionalInsights;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<AlternativeRecommendation> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(List<AlternativeRecommendation> alternatives) {
        this.alternatives = alternatives;
    }
}
