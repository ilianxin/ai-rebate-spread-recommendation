package com.airebate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单结果实体类
 * 记录客户的动态账单数据
 */
@Entity
@Table(name = "billing_results")
public class BillingResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;
    
    @NotNull(message = "账单日期不能为空")
    @Column(name = "billing_date", nullable = false)
    private LocalDate billingDate;
    
    @Positive(message = "交易金额必须为正数")
    @Column(name = "transaction_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal transactionAmount;
    
    @Positive(message = "交易量必须为正数")
    @Column(name = "transaction_volume", nullable = false)
    private Integer transactionVolume;
    
    @Column(name = "market_volatility", precision = 10, scale = 6)
    private BigDecimal marketVolatility; // 市场波动率
    
    @Column(name = "liquidity_score", precision = 5, scale = 2)
    private BigDecimal liquidityScore; // 流动性评分
    
    @Column(name = "profit_margin", precision = 8, scale = 4)
    private BigDecimal profitMargin; // 利润率
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 构造函数
    public BillingResult() {
        this.createdAt = LocalDateTime.now();
    }

    public BillingResult(Customer customer, Currency currency, LocalDate billingDate, 
                        BigDecimal transactionAmount, Integer transactionVolume) {
        this();
        this.customer = customer;
        this.currency = currency;
        this.billingDate = billingDate;
        this.transactionAmount = transactionAmount;
        this.transactionVolume = transactionVolume;
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

    public LocalDate getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(LocalDate billingDate) {
        this.billingDate = billingDate;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public Integer getTransactionVolume() {
        return transactionVolume;
    }

    public void setTransactionVolume(Integer transactionVolume) {
        this.transactionVolume = transactionVolume;
    }

    public BigDecimal getMarketVolatility() {
        return marketVolatility;
    }

    public void setMarketVolatility(BigDecimal marketVolatility) {
        this.marketVolatility = marketVolatility;
    }

    public BigDecimal getLiquidityScore() {
        return liquidityScore;
    }

    public void setLiquidityScore(BigDecimal liquidityScore) {
        this.liquidityScore = liquidityScore;
    }

    public BigDecimal getProfitMargin() {
        return profitMargin;
    }

    public void setProfitMargin(BigDecimal profitMargin) {
        this.profitMargin = profitMargin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
