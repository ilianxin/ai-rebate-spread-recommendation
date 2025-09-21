package com.airebate.repository;

import com.airebate.model.Currency;
import com.airebate.model.Customer;
import com.airebate.model.RebateSpreadRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Rebate Spread推荐数据访问层
 */
@Repository
public interface RebateSpreadRecommendationRepository extends JpaRepository<RebateSpreadRecommendation, Long> {
    
    /**
     * 查找有效的推荐记录
     */
    @Query("SELECT r FROM RebateSpreadRecommendation r WHERE r.customer = :customer " +
           "AND r.currency = :currency " +
           "AND r.recommendationDate = :date " +
           "AND r.validUntil > :now " +
           "ORDER BY r.createdAt DESC")
    Optional<RebateSpreadRecommendation> findValidRecommendation(
        @Param("customer") Customer customer,
        @Param("currency") Currency currency,
        @Param("date") LocalDate date,
        @Param("now") LocalDateTime now
    );
    
    /**
     * 根据客户和货币查找最新推荐
     */
    @Query("SELECT r FROM RebateSpreadRecommendation r WHERE r.customer = :customer " +
           "AND r.currency = :currency " +
           "ORDER BY r.createdAt DESC")
    List<RebateSpreadRecommendation> findLatestByCustomerAndCurrency(
        @Param("customer") Customer customer,
        @Param("currency") Currency currency
    );
    
    /**
     * 根据客户查找指定日期范围内的推荐
     */
    @Query("SELECT r FROM RebateSpreadRecommendation r WHERE r.customer = :customer " +
           "AND r.recommendationDate BETWEEN :startDate AND :endDate " +
           "ORDER BY r.recommendationDate DESC")
    List<RebateSpreadRecommendation> findByCustomerAndDateRange(
        @Param("customer") Customer customer,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * 清理过期的推荐记录
     */
    @Query("DELETE FROM RebateSpreadRecommendation r WHERE r.validUntil < :expiredTime")
    void deleteExpiredRecommendations(@Param("expiredTime") LocalDateTime expiredTime);
    
    /**
     * 统计客户的推荐历史数量
     */
    @Query("SELECT COUNT(r) FROM RebateSpreadRecommendation r WHERE r.customer = :customer")
    Long countByCustomer(@Param("customer") Customer customer);
}
