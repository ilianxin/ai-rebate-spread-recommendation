package com.airebate.service;

import com.airebate.dto.RecommendationRequest;
import com.airebate.dto.RecommendationResponse;
import com.airebate.model.BillingResult;
import com.airebate.model.Customer;
import com.airebate.model.RebateSpreadRecommendation;
import com.airebate.repository.BillingResultRepository;
import com.airebate.repository.CustomerRepository;
import com.airebate.repository.RebateSpreadRecommendationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Rebate Spread推荐服务
 * 整合AI引擎和数据访问，提供完整的推荐功能
 */
@Service
@Transactional
public class RebateSpreadService {
    
    private static final Logger logger = LoggerFactory.getLogger(RebateSpreadService.class);
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private BillingResultRepository billingResultRepository;
    
    @Autowired
    private RebateSpreadRecommendationRepository recommendationRepository;
    
    @Autowired
    private AIRecommendationEngine aiEngine;

    /**
     * 获取动态推荐
     */
    public RecommendationResponse getRecommendation(RecommendationRequest request) {
        logger.info("处理推荐请求: 客户={}, 货币={}, 日期={}", 
                   request.getCustomerCode(), request.getCurrency(), request.getQueryDate());
        
        try {
            // 查找客户
            Optional<Customer> customerOpt = customerRepository.findByCustomerCode(request.getCustomerCode());
            if (customerOpt.isEmpty()) {
                return RecommendationResponse.error("客户不存在: " + request.getCustomerCode());
            }
            
            Customer customer = customerOpt.get();
            
            // 检查是否有有效的缓存推荐
            Optional<RebateSpreadRecommendation> cachedRecommendation = 
                recommendationRepository.findValidRecommendation(
                    customer, request.getCurrency(), request.getQueryDate(), LocalDateTime.now());
            
            if (cachedRecommendation.isPresent()) {
                logger.info("使用缓存的推荐结果");
                return convertToResponse(cachedRecommendation.get());
            }
            
            // 获取历史数据
            List<BillingResult> historicalData = getHistoricalData(customer, request);
            
            if (historicalData.isEmpty()) {
                logger.warn("客户 {} 没有足够的历史数据", request.getCustomerCode());
                return RecommendationResponse.warning("客户历史数据不足，使用默认推荐策略");
            }
            
            // 生成新推荐
            RebateSpreadRecommendation recommendation = aiEngine.generateRecommendation(
                customer, request.getCurrency(), request.getQueryDate(), historicalData);
            
            // 保存推荐结果
            recommendation = recommendationRepository.save(recommendation);
            
            logger.info("成功生成新推荐，ID: {}", recommendation.getId());
            
            return convertToResponse(recommendation);
            
        } catch (Exception e) {
            logger.error("处理推荐请求时发生错误", e);
            return RecommendationResponse.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 获取历史数据
     */
    private List<BillingResult> getHistoricalData(Customer customer, RecommendationRequest request) {
        return billingResultRepository.findByCustomerAndCurrencyAndDateRange(
            customer,
            request.getCurrency(),
            request.getQueryDate().minusDays(request.getDaysRange()),
            request.getQueryDate()
        );
    }

    /**
     * 转换为响应DTO
     */
    private RecommendationResponse convertToResponse(RebateSpreadRecommendation recommendation) {
        RecommendationResponse response = RecommendationResponse.success(
            recommendation.getCustomer().getCustomerCode(),
            recommendation.getCustomer().getCustomerName(),
            recommendation.getCurrency(),
            recommendation.getRecommendationDate(),
            recommendation.getRecommendedSpread()
        );
        
        response.setConfidenceScore(recommendation.getConfidenceScore());
        response.setRiskAdjustment(recommendation.getRiskAdjustment());
        response.setVolatilityFactor(recommendation.getVolatilityFactor());
        response.setVolumeFactor(recommendation.getVolumeFactor());
        response.setHistoricalPerformance(recommendation.getHistoricalPerformance());
        response.setRecommendationReason(recommendation.getRecommendationReason());
        response.setValidUntil(recommendation.getValidUntil());
        
        // 检查推荐理由中是否包含LLM相关信息
        String reason = recommendation.getRecommendationReason();
        if (reason != null) {
            if (reason.contains("LLM") || reason.contains("GPT") || reason.contains("大模型") || 
                reason.contains("OpenAI") || reason.contains("本地模型")) {
                response.setUsedLLM(true);
                // 尝试从推荐理由中提取LLM信息
                if (reason.contains("OpenAI") || reason.contains("GPT")) {
                    response.setLlmProvider("OpenAI");
                    response.setLlmModel("GPT");
                } else if (reason.contains("本地") || reason.contains("Local") || reason.contains("Ollama")) {
                    response.setLlmProvider("Local LLM");
                    response.setLlmModel("Ollama");
                } else {
                    response.setLlmProvider("LLM Service");
                    response.setLlmModel("Unknown");
                }
            } else if (reason.contains("传统算法") || reason.contains("Traditional Algorithm") || 
                      reason.contains("基于AI分析")) {
                response.setUsedLLM(false);
                response.setLlmProvider("Traditional Service");
                response.setLlmModel("Mathematical Algorithm");
            }
        }
        
        return response;
    }

    /**
     * 获取客户历史推荐
     */
    @Transactional(readOnly = true)
    public List<RebateSpreadRecommendation> getCustomerRecommendationHistory(String customerCode, 
                                                                           int limit) {
        Optional<Customer> customerOpt = customerRepository.findByCustomerCode(customerCode);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("客户不存在: " + customerCode);
        }
        
        List<RebateSpreadRecommendation> recommendations = 
            recommendationRepository.findLatestByCustomerAndCurrency(
                customerOpt.get(), null);
        
        return recommendations.stream()
            .limit(limit)
            .toList();
    }

    /**
     * 清理过期推荐
     */
    @Transactional
    public void cleanupExpiredRecommendations() {
        logger.info("开始清理过期推荐");
        LocalDateTime expiredTime = LocalDateTime.now().minusDays(7); // 清理7天前的记录
        recommendationRepository.deleteExpiredRecommendations(expiredTime);
        logger.info("过期推荐清理完成");
    }

    /**
     * 获取系统统计信息
     */
    @Transactional(readOnly = true)
    public String getSystemStats() {
        long customerCount = customerRepository.count();
        long billingResultCount = billingResultRepository.count();
        long recommendationCount = recommendationRepository.count();
        
        return String.format("系统统计 - 客户数: %d, 账单记录数: %d, 推荐记录数: %d", 
                           customerCount, billingResultCount, recommendationCount);
    }
}
