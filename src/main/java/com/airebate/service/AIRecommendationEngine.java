package com.airebate.service;

import com.airebate.dto.LLMRecommendationRequest;
import com.airebate.dto.LLMRecommendationResponse;
import com.airebate.model.BillingResult;
import com.airebate.model.Currency;
import com.airebate.model.Customer;
import com.airebate.model.RebateSpreadRecommendation;
import com.airebate.service.LLMServiceManager;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI推荐引擎核心服务
 * 基于历史数据和市场因素生成智能的rebate spread推荐
 */
@Service
public class AIRecommendationEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(AIRecommendationEngine.class);
    
    @Autowired
    private LLMServiceManager llmServiceManager;
    
    @Value("${ai.rebate.default-spread-range:0.1}")
    private double defaultSpreadRange;
    
    @Value("${ai.rebate.min-spread:0.01}")
    private double minSpread;
    
    @Value("${ai.rebate.max-spread:0.5}")
    private double maxSpread;
    
    @Value("${ai.rebate.learning-rate:0.01}")
    private double learningRate;
    
    @Value("${ai.rebate.volatility-weight:0.3}")
    private double volatilityWeight;
    
    @Value("${ai.rebate.volume-weight:0.4}")
    private double volumeWeight;
    
    @Value("${ai.rebate.history-weight:0.3}")
    private double historyWeight;
    
    @Value("${ai.llm.enabled:true}")
    private boolean llmEnabled;
    
    @Value("${ai.llm.fallback-enabled:true}")
    private boolean fallbackEnabled;

    /**
     * 生成智能推荐（新版本，支持LLM）
     */
    public RebateSpreadRecommendation generateRecommendation(Customer customer, Currency currency, 
                                                           LocalDate recommendationDate, 
                                                           List<BillingResult> historicalData) {
        logger.info("为客户 {} 生成 {} 货币在 {} 的推荐", 
                   customer.getCustomerCode(), currency, recommendationDate);
        
        // 创建推荐对象
        RebateSpreadRecommendation recommendation = new RebateSpreadRecommendation(
            customer, currency, recommendationDate, BigDecimal.ZERO);
        
        try {
            // 优先尝试使用LLM推荐
            if (llmEnabled && llmServiceManager.hasAvailableService()) {
                LLMRecommendationResponse llmResponse = generateLLMRecommendation(
                    customer, currency, recommendationDate, historicalData);
                
                if (llmResponse.isSuccess()) {
                    logger.info("成功使用LLM生成推荐");
                    populateRecommendationFromLLM(recommendation, llmResponse);
                    return recommendation;
                } else {
                    logger.warn("LLM推荐失败: {}", llmResponse.getErrorMessage());
                }
            }
            
            // 如果LLM不可用或失败，使用传统算法
            if (fallbackEnabled) {
                logger.info("使用传统算法生成推荐");
                generateTraditionalRecommendation(recommendation, customer, currency, historicalData);
            } else {
                throw new RuntimeException("LLM服务不可用且传统算法回退已禁用");
            }
            
        } catch (Exception e) {
            logger.error("生成推荐时发生错误", e);
            // 使用默认值
            recommendation.setRecommendedSpread(BigDecimal.valueOf(defaultSpreadRange));
            recommendation.setConfidenceScore(BigDecimal.valueOf(0.5));
            recommendation.setRecommendationReason("使用默认推荐值，原因：" + e.getMessage());
        }
        
        recommendation.setValidUntil(LocalDateTime.now().plusHours(24));
        return recommendation;
    }

    /**
     * 使用LLM生成推荐
     */
    private LLMRecommendationResponse generateLLMRecommendation(Customer customer, Currency currency,
                                                              LocalDate recommendationDate,
                                                              List<BillingResult> historicalData) {
        // 准备LLM请求数据
        LLMRecommendationRequest llmRequest = new LLMRecommendationRequest();
        llmRequest.setCustomerCode(customer.getCustomerCode());
        llmRequest.setCustomerName(customer.getCustomerName());
        llmRequest.setCurrency(currency);
        llmRequest.setRecommendationDate(recommendationDate);
        
        // 计算历史数据摘要
        populateHistoricalDataSummary(llmRequest, historicalData);
        
        // 设置客户信息
        llmRequest.setCustomerRiskLevel(customer.getRiskLevel());
        llmRequest.setCustomerTradingVolume(customer.getTradingVolume());
        
        // 设置业务约束
        llmRequest.setMinSpread(BigDecimal.valueOf(minSpread));
        llmRequest.setMaxSpread(BigDecimal.valueOf(maxSpread));
        llmRequest.setDefaultSpread(BigDecimal.valueOf(defaultSpreadRange));
        
        // 生成市场状况和客户特征描述
        llmRequest.setMarketCondition(generateMarketConditionDescription(historicalData, currency));
        llmRequest.setCustomerProfile(generateCustomerProfileDescription(customer));
        
        // 调用LLM服务
        return llmServiceManager.generateRecommendation(llmRequest);
    }

    /**
     * 填充历史数据摘要
     */
    private void populateHistoricalDataSummary(LLMRecommendationRequest request, List<BillingResult> historicalData) {
        if (historicalData.isEmpty()) {
            return;
        }
        
        // 计算平均值
        double avgVolume = historicalData.stream()
            .filter(data -> data.getTransactionVolume() != null)
            .mapToDouble(data -> data.getTransactionVolume().doubleValue())
            .average().orElse(0.0);
        
        double avgAmount = historicalData.stream()
            .filter(data -> data.getTransactionAmount() != null)
            .mapToDouble(data -> data.getTransactionAmount().doubleValue())
            .average().orElse(0.0);
        
        double avgProfitMargin = historicalData.stream()
            .filter(data -> data.getProfitMargin() != null)
            .mapToDouble(data -> data.getProfitMargin().doubleValue())
            .average().orElse(0.05);
        
        double avgLiquidity = historicalData.stream()
            .filter(data -> data.getLiquidityScore() != null)
            .mapToDouble(data -> data.getLiquidityScore().doubleValue())
            .average().orElse(5.0);
        
        double avgVolatility = historicalData.stream()
            .filter(data -> data.getMarketVolatility() != null)
            .mapToDouble(data -> data.getMarketVolatility().doubleValue())
            .average().orElse(0.5);
        
        request.setAvgTransactionVolume(BigDecimal.valueOf(avgVolume));
        request.setAvgTransactionAmount(BigDecimal.valueOf(avgAmount));
        request.setAvgProfitMargin(BigDecimal.valueOf(avgProfitMargin));
        request.setAvgLiquidityScore(BigDecimal.valueOf(avgLiquidity));
        request.setMarketVolatility(BigDecimal.valueOf(avgVolatility));
    }

    /**
     * 生成市场状况描述
     */
    private String generateMarketConditionDescription(List<BillingResult> historicalData, Currency currency) {
        StringBuilder description = new StringBuilder();
        
        if (!historicalData.isEmpty()) {
            double avgVolatility = historicalData.stream()
                .filter(data -> data.getMarketVolatility() != null)
                .mapToDouble(data -> data.getMarketVolatility().doubleValue())
                .average().orElse(0.5);
            
            if (avgVolatility > 0.7) {
                description.append("市场波动剧烈，");
            } else if (avgVolatility < 0.3) {
                description.append("市场相对稳定，");
            } else {
                description.append("市场波动正常，");
            }
        }
        
        // 基于货币类型的市场描述
        description.append(getCurrencyMarketCondition(currency));
        
        return description.toString();
    }

    /**
     * 生成客户特征描述
     */
    private String generateCustomerProfileDescription(Customer customer) {
        StringBuilder profile = new StringBuilder();
        
        if (customer.getRiskLevel() != null) {
            if (customer.getRiskLevel() > 1.5) {
                profile.append("高风险客户，");
            } else if (customer.getRiskLevel() < 0.8) {
                profile.append("低风险优质客户，");
            } else {
                profile.append("标准风险客户，");
            }
        }
        
        if (customer.getTradingVolume() != null) {
            if (customer.getTradingVolume() > 50000) {
                profile.append("高频交易，交易量大");
            } else if (customer.getTradingVolume() > 10000) {
                profile.append("中等交易频率");
            } else {
                profile.append("交易频率较低");
            }
        }
        
        return profile.toString();
    }

    /**
     * 获取货币市场状况
     */
    private String getCurrencyMarketCondition(Currency currency) {
        return switch (currency) {
            case USD -> "美元市场流动性充足，作为全球储备货币具有稳定性";
            case EUR -> "欧元区经济政策影响较大，需关注欧央行动向";
            case JPY -> "日元避险需求强劲，利率政策敏感";
            case GBP -> "英镑政治经济因素影响明显，波动性相对较高";
            case CNY -> "人民币国际化进程加快，汇率管控相对严格";
            default -> "该货币流动性一般，需要额外关注地缘政治风险";
        };
    }

    /**
     * 从LLM响应填充推荐数据
     */
    private void populateRecommendationFromLLM(RebateSpreadRecommendation recommendation, 
                                             LLMRecommendationResponse llmResponse) {
        recommendation.setRecommendedSpread(llmResponse.getRecommendedSpread());
        recommendation.setConfidenceScore(llmResponse.getConfidenceScore());
        recommendation.setRecommendationReason(llmResponse.getReasoning());
        
        // 从LLM响应中解析传统字段（用于兼容性）
        recommendation.setVolatilityFactor(BigDecimal.valueOf(0.5)); // 默认值
        recommendation.setVolumeFactor(BigDecimal.valueOf(0.5));     // 默认值
        recommendation.setHistoricalPerformance(BigDecimal.valueOf(0.5)); // 默认值
        recommendation.setRiskAdjustment(BigDecimal.valueOf(1.0));   // 默认值
        
        logger.info("从LLM响应填充推荐数据完成");
    }

    /**
     * 使用传统算法生成推荐
     */
    private void generateTraditionalRecommendation(RebateSpreadRecommendation recommendation,
                                                 Customer customer, Currency currency,
                                                 List<BillingResult> historicalData) {
        // 计算各种因子
        BigDecimal volatilityFactor = calculateVolatilityFactor(historicalData);
        BigDecimal volumeFactor = calculateVolumeFactor(historicalData);
        BigDecimal historicalPerformanceFactor = calculateHistoricalPerformanceFactor(historicalData);
        BigDecimal riskAdjustment = calculateRiskAdjustment(customer, currency);
        
        // 计算基础推荐spread
        BigDecimal baseSpread = calculateBaseSpread(volatilityFactor, volumeFactor, 
                                                   historicalPerformanceFactor);
        
        // 应用风险调整
        BigDecimal adjustedSpread = applyRiskAdjustment(baseSpread, riskAdjustment);
        
        // 确保在合理范围内
        BigDecimal finalSpread = constrainSpread(adjustedSpread);
        
        // 计算置信度
        BigDecimal confidence = calculateConfidenceScore(historicalData, volatilityFactor);
        
        // 填充推荐数据
        recommendation.setRecommendedSpread(finalSpread);
        recommendation.setConfidenceScore(confidence);
        recommendation.setVolatilityFactor(volatilityFactor);
        recommendation.setVolumeFactor(volumeFactor);
        recommendation.setHistoricalPerformance(historicalPerformanceFactor);
        recommendation.setRiskAdjustment(riskAdjustment);
        recommendation.setRecommendationReason(generateRecommendationReason(
            volatilityFactor, volumeFactor, historicalPerformanceFactor, riskAdjustment));
        
        logger.info("传统算法生成推荐完成: spread={}, confidence={}", finalSpread, confidence);
    }

    /**
     * 计算波动率因子
     */
    private BigDecimal calculateVolatilityFactor(List<BillingResult> historicalData) {
        if (historicalData.isEmpty()) {
            return BigDecimal.valueOf(0.5); // 默认中等波动率
        }
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        
        for (BillingResult result : historicalData) {
            if (result.getMarketVolatility() != null) {
                stats.addValue(result.getMarketVolatility().doubleValue());
            } else if (result.getTransactionAmount() != null) {
                // 如果没有市场波动率，使用交易金额的变化作为替代
                stats.addValue(result.getTransactionAmount().doubleValue());
            }
        }
        
        if (stats.getN() == 0) {
            return BigDecimal.valueOf(0.5);
        }
        
        double volatility = stats.getStandardDeviation() / (stats.getMean() + 1);
        
        // 标准化到0-1范围
        double normalizedVolatility = Math.min(1.0, Math.max(0.0, volatility));
        
        return BigDecimal.valueOf(normalizedVolatility).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 计算交易量因子
     */
    private BigDecimal calculateVolumeFactor(List<BillingResult> historicalData) {
        if (historicalData.isEmpty()) {
            return BigDecimal.valueOf(0.5);
        }
        
        double totalVolume = historicalData.stream()
            .mapToDouble(result -> result.getTransactionVolume() != null ? 
                        result.getTransactionVolume().doubleValue() : 0.0)
            .sum();
        
        double avgVolume = totalVolume / historicalData.size();
        
        // 标准化交易量因子 (高交易量 = 低spread)
        double volumeFactor = 1.0 - Math.min(1.0, avgVolume / 10000.0); // 假设10000为高交易量阈值
        
        return BigDecimal.valueOf(volumeFactor).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 计算历史表现因子
     */
    private BigDecimal calculateHistoricalPerformanceFactor(List<BillingResult> historicalData) {
        if (historicalData.isEmpty()) {
            return BigDecimal.valueOf(0.5);
        }
        
        double avgProfitMargin = historicalData.stream()
            .filter(result -> result.getProfitMargin() != null)
            .mapToDouble(result -> result.getProfitMargin().doubleValue())
            .average()
            .orElse(0.05); // 默认5%利润率
        
        double avgLiquidityScore = historicalData.stream()
            .filter(result -> result.getLiquidityScore() != null)
            .mapToDouble(result -> result.getLiquidityScore().doubleValue())
            .average()
            .orElse(5.0); // 默认中等流动性评分
        
        // 综合历史表现 (高利润率和高流动性 = 可以设置较高spread)
        double performanceFactor = (avgProfitMargin * 10 + avgLiquidityScore / 10) / 2;
        performanceFactor = Math.min(1.0, Math.max(0.0, performanceFactor));
        
        return BigDecimal.valueOf(performanceFactor).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 计算风险调整因子
     */
    private BigDecimal calculateRiskAdjustment(Customer customer, Currency currency) {
        double riskLevel = customer.getRiskLevel() != null ? customer.getRiskLevel() : 1.0;
        double tradingVolume = customer.getTradingVolume() != null ? customer.getTradingVolume() : 0.0;
        
        // 货币风险权重
        double currencyRisk = getCurrencyRiskWeight(currency);
        
        // 综合风险调整
        double riskAdjustment = (riskLevel * 0.4 + currencyRisk * 0.6) * (1 + tradingVolume / 100000);
        riskAdjustment = Math.min(2.0, Math.max(0.5, riskAdjustment)); // 限制在0.5-2.0范围
        
        return BigDecimal.valueOf(riskAdjustment).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 获取货币风险权重
     */
    private double getCurrencyRiskWeight(Currency currency) {
        return switch (currency) {
            case USD, EUR -> 0.8; // 低风险
            case GBP, JPY, CHF -> 0.9; // 中低风险
            case CNY, CAD, AUD -> 1.0; // 中等风险
            case HKD, SGD -> 1.1; // 中高风险
            default -> 1.2; // 高风险
        };
    }

    /**
     * 计算基础spread
     */
    private BigDecimal calculateBaseSpread(BigDecimal volatilityFactor, BigDecimal volumeFactor, 
                                         BigDecimal historicalPerformanceFactor) {
        double baseSpread = defaultSpreadRange;
        
        // 加权计算
        double adjustedSpread = baseSpread * (
            volatilityFactor.doubleValue() * volatilityWeight +
            volumeFactor.doubleValue() * volumeWeight +
            historicalPerformanceFactor.doubleValue() * historyWeight
        );
        
        return BigDecimal.valueOf(adjustedSpread).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 应用风险调整
     */
    private BigDecimal applyRiskAdjustment(BigDecimal baseSpread, BigDecimal riskAdjustment) {
        return baseSpread.multiply(riskAdjustment).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 约束spread在合理范围内
     */
    private BigDecimal constrainSpread(BigDecimal spread) {
        double value = spread.doubleValue();
        value = Math.max(minSpread, Math.min(maxSpread, value));
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 计算置信度评分
     */
    private BigDecimal calculateConfidenceScore(List<BillingResult> historicalData, BigDecimal volatilityFactor) {
        double dataQuality = Math.min(1.0, historicalData.size() / 30.0); // 30天数据为满分
        double volatilityPenalty = 1.0 - volatilityFactor.doubleValue() * 0.3; // 高波动率降低置信度
        
        double confidence = dataQuality * volatilityPenalty;
        confidence = Math.max(0.1, Math.min(1.0, confidence));
        
        return BigDecimal.valueOf(confidence).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 生成推荐理由
     */
    private String generateRecommendationReason(BigDecimal volatilityFactor, BigDecimal volumeFactor,
                                               BigDecimal historicalPerformanceFactor, BigDecimal riskAdjustment) {
        StringBuilder reason = new StringBuilder();
        
        reason.append("基于AI分析：");
        
        if (volatilityFactor.doubleValue() > 0.7) {
            reason.append("市场波动率较高，");
        } else if (volatilityFactor.doubleValue() < 0.3) {
            reason.append("市场波动率较低，");
        }
        
        if (volumeFactor.doubleValue() > 0.7) {
            reason.append("交易量偏低，");
        } else if (volumeFactor.doubleValue() < 0.3) {
            reason.append("交易量较高，");
        }
        
        if (historicalPerformanceFactor.doubleValue() > 0.7) {
            reason.append("历史表现良好，");
        } else if (historicalPerformanceFactor.doubleValue() < 0.3) {
            reason.append("历史表现一般，");
        }
        
        if (riskAdjustment.doubleValue() > 1.2) {
            reason.append("考虑到较高风险因子。");
        } else if (riskAdjustment.doubleValue() < 0.8) {
            reason.append("考虑到较低风险因子。");
        } else {
            reason.append("综合各项因子平衡考虑。");
        }
        
        return reason.toString();
    }
}
