package com.airebate.dto;

import com.airebate.model.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * LLM推荐请求DTO
 */
public class LLMRecommendationRequest {
    
    private String customerCode;
    private String customerName;
    private Currency currency;
    private LocalDate recommendationDate;
    
    // 历史数据摘要
    private BigDecimal avgTransactionVolume;
    private BigDecimal avgTransactionAmount;
    private BigDecimal avgProfitMargin;
    private BigDecimal avgLiquidityScore;
    private BigDecimal marketVolatility;
    private Double customerRiskLevel;
    private Double customerTradingVolume;
    
    // 市场数据
    private Map<String, Object> marketContext;
    
    // 业务规则
    private BigDecimal minSpread;
    private BigDecimal maxSpread;
    private BigDecimal defaultSpread;
    
    // 当前市场状况描述
    private String marketCondition;
    
    // 客户特征描述
    private String customerProfile;

    // 构造函数
    public LLMRecommendationRequest() {}

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

    public BigDecimal getAvgTransactionVolume() {
        return avgTransactionVolume;
    }

    public void setAvgTransactionVolume(BigDecimal avgTransactionVolume) {
        this.avgTransactionVolume = avgTransactionVolume;
    }

    public BigDecimal getAvgTransactionAmount() {
        return avgTransactionAmount;
    }

    public void setAvgTransactionAmount(BigDecimal avgTransactionAmount) {
        this.avgTransactionAmount = avgTransactionAmount;
    }

    public BigDecimal getAvgProfitMargin() {
        return avgProfitMargin;
    }

    public void setAvgProfitMargin(BigDecimal avgProfitMargin) {
        this.avgProfitMargin = avgProfitMargin;
    }

    public BigDecimal getAvgLiquidityScore() {
        return avgLiquidityScore;
    }

    public void setAvgLiquidityScore(BigDecimal avgLiquidityScore) {
        this.avgLiquidityScore = avgLiquidityScore;
    }

    public BigDecimal getMarketVolatility() {
        return marketVolatility;
    }

    public void setMarketVolatility(BigDecimal marketVolatility) {
        this.marketVolatility = marketVolatility;
    }

    public Double getCustomerRiskLevel() {
        return customerRiskLevel;
    }

    public void setCustomerRiskLevel(Double customerRiskLevel) {
        this.customerRiskLevel = customerRiskLevel;
    }

    public Double getCustomerTradingVolume() {
        return customerTradingVolume;
    }

    public void setCustomerTradingVolume(Double customerTradingVolume) {
        this.customerTradingVolume = customerTradingVolume;
    }

    public Map<String, Object> getMarketContext() {
        return marketContext;
    }

    public void setMarketContext(Map<String, Object> marketContext) {
        this.marketContext = marketContext;
    }

    public BigDecimal getMinSpread() {
        return minSpread;
    }

    public void setMinSpread(BigDecimal minSpread) {
        this.minSpread = minSpread;
    }

    public BigDecimal getMaxSpread() {
        return maxSpread;
    }

    public void setMaxSpread(BigDecimal maxSpread) {
        this.maxSpread = maxSpread;
    }

    public BigDecimal getDefaultSpread() {
        return defaultSpread;
    }

    public void setDefaultSpread(BigDecimal defaultSpread) {
        this.defaultSpread = defaultSpread;
    }

    public String getMarketCondition() {
        return marketCondition;
    }

    public void setMarketCondition(String marketCondition) {
        this.marketCondition = marketCondition;
    }

    public String getCustomerProfile() {
        return customerProfile;
    }

    public void setCustomerProfile(String customerProfile) {
        this.customerProfile = customerProfile;
    }
}
