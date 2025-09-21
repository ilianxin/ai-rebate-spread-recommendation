package com.airebate.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Rebate Spread推荐结果实体类
 * 存储AI生成的推荐数据
 */
@Entity
@Table(name = "rebate_spread_recommendations")
public class RebateSpreadRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;
    
    @Column(name = "recommendation_date", nullable = false)
    private LocalDate recommendationDate;
    
    @Column(name = "recommended_spread", precision = 8, scale = 6, nullable = false)
    private BigDecimal recommendedSpread; // 推荐的spread值
    
    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore; // 置信度评分 (0-1)
    
    @Column(name = "risk_adjustment", precision = 6, scale = 4)
    private BigDecimal riskAdjustment; // 风险调整因子
    
    @Column(name = "volatility_factor", precision = 6, scale = 4)
    private BigDecimal volatilityFactor; // 波动率因子
    
    @Column(name = "volume_factor", precision = 6, scale = 4)
    private BigDecimal volumeFactor; // 交易量因子
    
    @Column(name = "historical_performance", precision = 6, scale = 4)
    private BigDecimal historicalPerformance; // 历史表现因子
    
    @Column(name = "recommendation_reason", length = 500)
    private String recommendationReason; // 推荐理由
    
    @Column(name = "valid_until")
    private LocalDateTime validUntil; // 推荐有效期
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 构造函数
    public RebateSpreadRecommendation() {
        this.createdAt = LocalDateTime.now();
        this.validUntil = LocalDateTime.now().plusDays(1); // 默认1天有效期
    }

    public RebateSpreadRecommendation(Customer customer, Currency currency, 
                                    LocalDate recommendationDate, BigDecimal recommendedSpread) {
        this();
        this.customer = customer;
        this.currency = currency;
        this.recommendationDate = recommendationDate;
        this.recommendedSpread = recommendedSpread;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
