package com.airebate.controller;

import com.airebate.model.BillingResult;
import com.airebate.model.Currency;
import com.airebate.model.Customer;
import com.airebate.repository.BillingResultRepository;
import com.airebate.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 数据管理控制器
 * 用于管理客户和账单数据，支持测试和演示
 */
@RestController
@RequestMapping("/data")
@Tag(name = "数据管理", description = "客户和账单数据的增删改查接口")
public class DataManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(DataManagementController.class);
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private BillingResultRepository billingResultRepository;

    // ====== 客户管理 ======
    
    @Operation(summary = "创建客户", description = "添加新客户到系统")
    @PostMapping("/customers")
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        try {
            if (customerRepository.existsByCustomerCode(customer.getCustomerCode())) {
                return ResponseEntity.badRequest().build();
            }
            
            Customer savedCustomer = customerRepository.save(customer);
            logger.info("创建客户成功: {}", savedCustomer.getCustomerCode());
            return ResponseEntity.ok(savedCustomer);
        } catch (Exception e) {
            logger.error("创建客户失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "获取所有客户", description = "查询系统中的所有客户")
    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "根据代码查询客户", description = "通过客户代码查询特定客户")
    @GetMapping("/customers/{customerCode}")
    public ResponseEntity<Customer> getCustomerByCode(
            @Parameter(description = "客户代码", required = true)
            @PathVariable String customerCode) {
        
        Optional<Customer> customer = customerRepository.findByCustomerCode(customerCode);
        return customer.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "更新客户信息", description = "修改现有客户的信息")
    @PutMapping("/customers/{customerCode}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable String customerCode,
            @Valid @RequestBody Customer customerUpdate) {
        
        Optional<Customer> existingCustomer = customerRepository.findByCustomerCode(customerCode);
        if (existingCustomer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Customer customer = existingCustomer.get();
        customer.setCustomerName(customerUpdate.getCustomerName());
        customer.setPrimaryCurrency(customerUpdate.getPrimaryCurrency());
        customer.setRiskLevel(customerUpdate.getRiskLevel());
        customer.setTradingVolume(customerUpdate.getTradingVolume());
        
        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(savedCustomer);
    }

    // ====== 账单数据管理 ======
    
    @Operation(summary = "添加账单记录", description = "为客户添加新的账单数据")
    @PostMapping("/billing-results")
    public ResponseEntity<BillingResult> createBillingResult(@Valid @RequestBody BillingResult billingResult) {
        try {
            BillingResult savedResult = billingResultRepository.save(billingResult);
            logger.info("创建账单记录成功，ID: {}", savedResult.getId());
            return ResponseEntity.ok(savedResult);
        } catch (Exception e) {
            logger.error("创建账单记录失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "批量添加账单记录", description = "批量导入客户账单数据")
    @PostMapping("/billing-results/batch")
    public ResponseEntity<List<BillingResult>> createBillingResultsBatch(
            @Valid @RequestBody List<BillingResult> billingResults) {
        try {
            List<BillingResult> savedResults = billingResultRepository.saveAll(billingResults);
            logger.info("批量创建账单记录成功，数量: {}", savedResults.size());
            return ResponseEntity.ok(savedResults);
        } catch (Exception e) {
            logger.error("批量创建账单记录失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "查询客户账单记录", description = "获取指定客户的账单数据")
    @GetMapping("/billing-results/customer/{customerCode}")
    public ResponseEntity<List<BillingResult>> getBillingResultsByCustomer(
            @PathVariable String customerCode,
            @RequestParam(required = false) Currency currency,
            @RequestParam(required = false) Integer limit) {
        
        Optional<Customer> customerOpt = customerRepository.findByCustomerCode(customerCode);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Customer customer = customerOpt.get();
        List<BillingResult> results;
        
        if (currency != null) {
            results = billingResultRepository.findByCustomerAndCurrency(customer, currency);
        } else {
            results = customer.getBillingResults();
        }
        
        if (limit != null && limit > 0) {
            results = results.stream().limit(limit).toList();
        }
        
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "快速创建测试客户", description = "创建预设的测试客户，用于演示")
    @PostMapping("/customers/demo")
    public ResponseEntity<Customer> createDemoCustomer(
            @RequestParam String customerCode,
            @RequestParam String customerName,
            @RequestParam Currency primaryCurrency) {
        
        if (customerRepository.existsByCustomerCode(customerCode)) {
            return ResponseEntity.badRequest().build();
        }
        
        Customer customer = new Customer(customerCode, customerName, primaryCurrency);
        customer.setRiskLevel(1.0 + Math.random()); // 随机风险等级
        customer.setTradingVolume(Math.random() * 100000); // 随机交易量
        
        Customer savedCustomer = customerRepository.save(customer);
        
        // 创建一些示例账单数据
        createSampleBillingData(savedCustomer, primaryCurrency);
        
        return ResponseEntity.ok(savedCustomer);
    }

    @Operation(summary = "为客户生成示例数据", description = "为指定客户生成测试用的账单数据")
    @PostMapping("/billing-results/generate/{customerCode}")
    public ResponseEntity<String> generateSampleData(
            @PathVariable String customerCode,
            @RequestParam Currency currency,
            @RequestParam(defaultValue = "30") int days) {
        
        Optional<Customer> customerOpt = customerRepository.findByCustomerCode(customerCode);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Customer customer = customerOpt.get();
        int createdCount = createSampleBillingData(customer, currency, days);
        
        return ResponseEntity.ok(String.format("为客户 %s 生成了 %d 条 %s 货币的示例账单数据", 
                                              customerCode, createdCount, currency));
    }

    // ====== 辅助方法 ======
    
    private void createSampleBillingData(Customer customer, Currency currency) {
        createSampleBillingData(customer, currency, 30);
    }
    
    private int createSampleBillingData(Customer customer, Currency currency, int days) {
        int createdCount = 0;
        LocalDate startDate = LocalDate.now().minusDays(days);
        
        for (int i = 0; i < days; i++) {
            LocalDate billingDate = startDate.plusDays(i);
            
            // 随机生成1-3条记录每天
            int recordsPerDay = 1 + (int)(Math.random() * 3);
            
            for (int j = 0; j < recordsPerDay; j++) {
                BillingResult billingResult = new BillingResult();
                billingResult.setCustomer(customer);
                billingResult.setCurrency(currency);
                billingResult.setBillingDate(billingDate);
                
                // 随机数据
                billingResult.setTransactionAmount(BigDecimal.valueOf(1000 + Math.random() * 50000));
                billingResult.setTransactionVolume((int)(10 + Math.random() * 1000));
                billingResult.setMarketVolatility(BigDecimal.valueOf(0.01 + Math.random() * 0.1));
                billingResult.setLiquidityScore(BigDecimal.valueOf(1 + Math.random() * 9));
                billingResult.setProfitMargin(BigDecimal.valueOf(0.01 + Math.random() * 0.1));
                
                billingResultRepository.save(billingResult);
                createdCount++;
            }
        }
        
        logger.info("为客户 {} 生成了 {} 条示例数据", customer.getCustomerCode(), createdCount);
        return createdCount;
    }
}
