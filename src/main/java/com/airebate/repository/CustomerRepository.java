package com.airebate.repository;

import com.airebate.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 客户数据访问层
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * 根据客户代码查找客户
     */
    Optional<Customer> findByCustomerCode(String customerCode);
    
    /**
     * 检查客户代码是否存在
     */
    boolean existsByCustomerCode(String customerCode);
    
    /**
     * 根据客户代码查找客户及其账单结果
     */
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.billingResults WHERE c.customerCode = :customerCode")
    Optional<Customer> findByCustomerCodeWithBillingResults(@Param("customerCode") String customerCode);
}
