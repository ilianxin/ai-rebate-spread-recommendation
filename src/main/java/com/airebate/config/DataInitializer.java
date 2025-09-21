package com.airebate.config;

import com.airebate.model.BillingResult;
import com.airebate.model.Currency;
import com.airebate.model.Customer;
import com.airebate.repository.BillingResultRepository;
import com.airebate.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据初始化组件
 * 在应用启动时创建一些示例数据用于演示和测试
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private BillingResultRepository billingResultRepository;

    @Override
    public void run(String... args) throws Exception {
        if (customerRepository.count() == 0) {
            logger.info("开始初始化示例数据...");
            initializeSampleData();
            logger.info("示例数据初始化完成");
        } else {
            logger.info("数据库中已存在数据，跳过初始化");
        }
    }

    private void initializeSampleData() {
        // 创建示例客户
        List<Customer> customers = createSampleCustomers();
        
        // 为每个客户创建账单数据
        for (Customer customer : customers) {
            createBillingDataForCustomer(customer);
        }
        
        logger.info("创建了 {} 个客户和相应的账单数据", customers.size());
    }

    private List<Customer> createSampleCustomers() {
        List<Customer> customers = new ArrayList<>();
        
        // 客户1：美国银行
        Customer customer1 = new Customer("US_BANK_001", "美国第一银行", Currency.USD);
        customer1.setRiskLevel(0.8);
        customer1.setTradingVolume(150000.0);
        customers.add(customerRepository.save(customer1));
        
        // 客户2：欧洲投资银行
        Customer customer2 = new Customer("EU_INVEST_002", "欧洲投资银行", Currency.EUR);
        customer2.setRiskLevel(0.9);
        customer2.setTradingVolume(200000.0);
        customers.add(customerRepository.save(customer2));
        
        // 客户3：日本证券公司
        Customer customer3 = new Customer("JP_SEC_003", "日本证券株式会社", Currency.JPY);
        customer3.setRiskLevel(1.2);
        customer3.setTradingVolume(80000.0);
        customers.add(customerRepository.save(customer3));
        
        // 客户4：英国金融集团
        Customer customer4 = new Customer("UK_FIN_004", "英国金融集团", Currency.GBP);
        customer4.setRiskLevel(1.0);
        customer4.setTradingVolume(120000.0);
        customers.add(customerRepository.save(customer4));
        
        // 客户5：中国国际银行
        Customer customer5 = new Customer("CN_INTL_005", "中国国际银行", Currency.CNY);
        customer5.setRiskLevel(1.1);
        customer5.setTradingVolume(300000.0);
        customers.add(customerRepository.save(customer5));
        
        return customers;
    }

    private void createBillingDataForCustomer(Customer customer) {
        LocalDate startDate = LocalDate.now().minusDays(60); // 创建60天的历史数据
        
        for (int day = 0; day < 60; day++) {
            LocalDate billingDate = startDate.plusDays(day);
            
            // 每天创建1-3条记录
            int recordsPerDay = 1 + (int)(Math.random() * 3);
            
            for (int i = 0; i < recordsPerDay; i++) {
                BillingResult billingResult = createSampleBillingResult(customer, billingDate);
                billingResultRepository.save(billingResult);
            }
        }
        
        // 为客户创建其他货币的数据
        createCrossCurrencyData(customer);
    }

    private BillingResult createSampleBillingResult(Customer customer, LocalDate billingDate) {
        BillingResult billingResult = new BillingResult();
        billingResult.setCustomer(customer);
        billingResult.setCurrency(customer.getPrimaryCurrency());
        billingResult.setBillingDate(billingDate);
        
        // 基于货币生成相应规模的交易金额
        BigDecimal baseAmount = getBaseAmountForCurrency(customer.getPrimaryCurrency());
        double randomFactor = 0.5 + Math.random(); // 0.5-1.5倍随机因子
        billingResult.setTransactionAmount(baseAmount.multiply(BigDecimal.valueOf(randomFactor)));
        
        // 交易量
        billingResult.setTransactionVolume((int)(50 + Math.random() * 500));
        
        // 市场波动率 (0.01-0.15)
        billingResult.setMarketVolatility(BigDecimal.valueOf(0.01 + Math.random() * 0.14));
        
        // 流动性评分 (1-10)
        billingResult.setLiquidityScore(BigDecimal.valueOf(1 + Math.random() * 9));
        
        // 利润率 (0.01-0.08)
        billingResult.setProfitMargin(BigDecimal.valueOf(0.01 + Math.random() * 0.07));
        
        return billingResult;
    }

    private void createCrossCurrencyData(Customer customer) {
        Currency[] otherCurrencies = getOtherCurrencies(customer.getPrimaryCurrency());
        
        for (Currency currency : otherCurrencies) {
            // 为其他货币创建较少的历史数据
            int recordCount = 5 + (int)(Math.random() * 15); // 5-20条记录
            LocalDate startDate = LocalDate.now().minusDays(30);
            
            for (int i = 0; i < recordCount; i++) {
                LocalDate billingDate = startDate.plusDays((int)(Math.random() * 30));
                
                BillingResult billingResult = new BillingResult();
                billingResult.setCustomer(customer);
                billingResult.setCurrency(currency);
                billingResult.setBillingDate(billingDate);
                
                BigDecimal baseAmount = getBaseAmountForCurrency(currency);
                double randomFactor = 0.3 + Math.random() * 0.7; // 跨货币交易通常较小
                billingResult.setTransactionAmount(baseAmount.multiply(BigDecimal.valueOf(randomFactor)));
                
                billingResult.setTransactionVolume((int)(20 + Math.random() * 200));
                billingResult.setMarketVolatility(BigDecimal.valueOf(0.02 + Math.random() * 0.18));
                billingResult.setLiquidityScore(BigDecimal.valueOf(2 + Math.random() * 8));
                billingResult.setProfitMargin(BigDecimal.valueOf(0.015 + Math.random() * 0.065));
                
                billingResultRepository.save(billingResult);
            }
        }
    }

    private BigDecimal getBaseAmountForCurrency(Currency currency) {
        return switch (currency) {
            case USD, EUR, GBP -> BigDecimal.valueOf(10000);
            case JPY -> BigDecimal.valueOf(1000000); // 日元面值较大
            case CNY -> BigDecimal.valueOf(50000);
            case CAD, AUD -> BigDecimal.valueOf(8000);
            case CHF -> BigDecimal.valueOf(7000);
            case HKD -> BigDecimal.valueOf(60000);
            case SGD -> BigDecimal.valueOf(12000);
        };
    }

    private Currency[] getOtherCurrencies(Currency primaryCurrency) {
        List<Currency> others = new ArrayList<>();
        for (Currency currency : Currency.values()) {
            if (currency != primaryCurrency) {
                others.add(currency);
            }
        }
        // 随机选择2-3种其他货币
        int count = 2 + (int)(Math.random() * 2);
        Currency[] selected = new Currency[Math.min(count, others.size())];
        for (int i = 0; i < selected.length; i++) {
            int index = (int)(Math.random() * others.size());
            selected[i] = others.remove(index);
        }
        return selected;
    }
}
