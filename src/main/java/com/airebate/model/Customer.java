package com.airebate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户实体类
 */
@Entity
@Table(name = "customers")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "客户代码不能为空")
    @Size(max = 50, message = "客户代码长度不能超过50字符")
    @Column(name = "customer_code", unique = true, nullable = false)
    private String customerCode;
    
    @NotBlank(message = "客户名称不能为空")
    @Size(max = 200, message = "客户名称长度不能超过200字符")
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "primary_currency", nullable = false)
    private Currency primaryCurrency;
    
    @Column(name = "risk_level")
    private Double riskLevel = 1.0; // 风险等级，默认为1.0
    
    @Column(name = "trading_volume")
    private Double tradingVolume = 0.0; // 交易量
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BillingResult> billingResults;

    // 构造函数
    public Customer() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Customer(String customerCode, String customerName, Currency primaryCurrency) {
        this();
        this.customerCode = customerCode;
        this.customerName = customerName;
        this.primaryCurrency = primaryCurrency;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Currency getPrimaryCurrency() {
        return primaryCurrency;
    }

    public void setPrimaryCurrency(Currency primaryCurrency) {
        this.primaryCurrency = primaryCurrency;
    }

    public Double getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(Double riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Double getTradingVolume() {
        return tradingVolume;
    }

    public void setTradingVolume(Double tradingVolume) {
        this.tradingVolume = tradingVolume;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<BillingResult> getBillingResults() {
        return billingResults;
    }

    public void setBillingResults(List<BillingResult> billingResults) {
        this.billingResults = billingResults;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
