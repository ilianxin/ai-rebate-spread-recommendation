package com.airebate.repository;

import com.airebate.model.BillingResult;
import com.airebate.model.Currency;
import com.airebate.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 账单结果数据访问层
 */
@Repository
public interface BillingResultRepository extends JpaRepository<BillingResult, Long> {
    
    /**
     * 根据客户和货币查找账单结果
     */
    List<BillingResult> findByCustomerAndCurrency(Customer customer, Currency currency);
    
    /**
     * 根据客户、货币和日期范围查找账单结果
     */
    @Query("SELECT br FROM BillingResult br WHERE br.customer = :customer " +
           "AND br.currency = :currency " +
           "AND br.billingDate BETWEEN :startDate AND :endDate " +
           "ORDER BY br.billingDate DESC")
    List<BillingResult> findByCustomerAndCurrencyAndDateRange(
        @Param("customer") Customer customer,
        @Param("currency") Currency currency,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * 根据客户和日期范围查找所有账单结果
     */
    @Query("SELECT br FROM BillingResult br WHERE br.customer = :customer " +
           "AND br.billingDate BETWEEN :startDate AND :endDate " +
           "ORDER BY br.billingDate DESC")
    List<BillingResult> findByCustomerAndDateRange(
        @Param("customer") Customer customer,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * 获取指定客户和货币的最新账单结果
     */
    @Query("SELECT br FROM BillingResult br WHERE br.customer = :customer " +
           "AND br.currency = :currency " +
           "ORDER BY br.billingDate DESC")
    List<BillingResult> findLatestByCustomerAndCurrency(
        @Param("customer") Customer customer,
        @Param("currency") Currency currency
    );
    
    /**
     * 统计客户在指定货币的交易数量
     */
    @Query("SELECT COUNT(br) FROM BillingResult br WHERE br.customer = :customer " +
           "AND br.currency = :currency")
    Long countByCustomerAndCurrency(
        @Param("customer") Customer customer,
        @Param("currency") Currency currency
    );
}
