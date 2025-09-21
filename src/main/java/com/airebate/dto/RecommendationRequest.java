package com.airebate.dto;

import com.airebate.model.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Rebate Spread推荐请求DTO
 */
public class RecommendationRequest {
    
    @NotBlank(message = "客户代码不能为空")
    private String customerCode;
    
    @NotNull(message = "货币类型不能为空")
    private Currency currency;
    
    @NotNull(message = "查询日期不能为空")
    private LocalDate queryDate;
    
    private Integer daysRange = 30; // 历史数据天数范围，默认30天

    // 构造函数
    public RecommendationRequest() {}

    public RecommendationRequest(String customerCode, Currency currency, LocalDate queryDate) {
        this.customerCode = customerCode;
        this.currency = currency;
        this.queryDate = queryDate;
    }

    // Getters and Setters
    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public LocalDate getQueryDate() {
        return queryDate;
    }

    public void setQueryDate(LocalDate queryDate) {
        this.queryDate = queryDate;
    }

    public Integer getDaysRange() {
        return daysRange;
    }

    public void setDaysRange(Integer daysRange) {
        this.daysRange = daysRange;
    }
}
