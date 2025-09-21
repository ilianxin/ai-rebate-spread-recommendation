package com.airebate.service.impl;

import com.airebate.dto.LLMRecommendationRequest;
import com.airebate.dto.LLMRecommendationResponse;
import com.airebate.service.LLMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * 回退LLM服务实现
 * 当主要LLM服务不可用时，使用传统算法生成推荐
 */
@Service
@ConditionalOnProperty(name = "ai.llm.provider", havingValue = "fallback", matchIfMissing = true)
public class FallbackLLMService implements LLMService {

    private static final Logger logger = LoggerFactory.getLogger(FallbackLLMService.class);

    @Override
    public LLMRecommendationResponse generateRecommendation(LLMRecommendationRequest request) {
        logger.info("使用回退服务为客户 {} 生成推荐", request.getCustomerCode());

        try {
            // 使用简化的算法计算推荐
            BigDecimal recommendedSpread = calculateSpreadUsingTraditionalMethod(request);
            BigDecimal confidenceScore = calculateConfidenceScore(request);
            
            LLMRecommendationResponse response = new LLMRecommendationResponse();
            response.setRecommendedSpread(recommendedSpread);
            response.setConfidenceScore(confidenceScore);
            response.setReasoning(generateReasoning(request, recommendedSpread));
            response.setRiskAssessment(generateRiskAssessment(request));
            response.setMarketAnalysis(generateMarketAnalysis(request));
            response.setKeyFactors(Arrays.asList(
                "历史交易表现", 
                "市场波动率", 
                "客户风险等级", 
                "流动性状况"
            ));
            response.setModelUsed("Traditional Algorithm (Fallback)");

            logger.info("回退服务成功生成推荐: spread={}, confidence={}", 
                       recommendedSpread, confidenceScore);

            return response;

        } catch (Exception e) {
            logger.error("回退服务生成推荐失败", e);
            return LLMRecommendationResponse.error("回退服务生成推荐失败: " + e.getMessage());
        }
    }

    private BigDecimal calculateSpreadUsingTraditionalMethod(LLMRecommendationRequest request) {
        BigDecimal baseSpread = request.getDefaultSpread();
        
        // 波动率调整
        BigDecimal volatilityAdjustment = calculateVolatilityAdjustment(request);
        
        // 风险调整
        BigDecimal riskAdjustment = calculateRiskAdjustment(request);
        
        // 流动性调整
        BigDecimal liquidityAdjustment = calculateLiquidityAdjustment(request);
        
        // 综合调整
        BigDecimal adjustedSpread = baseSpread
            .multiply(BigDecimal.valueOf(1.0).add(volatilityAdjustment))
            .multiply(BigDecimal.valueOf(1.0).add(riskAdjustment))
            .multiply(BigDecimal.valueOf(1.0).add(liquidityAdjustment));
        
        // 确保在范围内
        if (adjustedSpread.compareTo(request.getMinSpread()) < 0) {
            adjustedSpread = request.getMinSpread();
        }
        if (adjustedSpread.compareTo(request.getMaxSpread()) > 0) {
            adjustedSpread = request.getMaxSpread();
        }
        
        return adjustedSpread.setScale(6, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVolatilityAdjustment(LLMRecommendationRequest request) {
        if (request.getMarketVolatility() == null) {
            return BigDecimal.ZERO;
        }
        
        // 高波动率增加spread
        double volatility = request.getMarketVolatility().doubleValue();
        double adjustment = (volatility - 0.5) * 0.2; // 假设0.5为中等波动率
        
        return BigDecimal.valueOf(adjustment).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRiskAdjustment(LLMRecommendationRequest request) {
        if (request.getCustomerRiskLevel() == null) {
            return BigDecimal.ZERO;
        }
        
        // 高风险客户增加spread
        double riskLevel = request.getCustomerRiskLevel();
        double adjustment = (riskLevel - 1.0) * 0.1; // 假设1.0为标准风险
        
        return BigDecimal.valueOf(adjustment).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLiquidityAdjustment(LLMRecommendationRequest request) {
        if (request.getAvgLiquidityScore() == null) {
            return BigDecimal.ZERO;
        }
        
        // 低流动性增加spread
        double liquidityScore = request.getAvgLiquidityScore().doubleValue();
        double adjustment = (5.0 - liquidityScore) * 0.02; // 假设5.0为中等流动性
        
        return BigDecimal.valueOf(adjustment).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateConfidenceScore(LLMRecommendationRequest request) {
        double confidence = 0.7; // 基础置信度
        
        // 数据质量影响置信度
        if (request.getAvgTransactionVolume() != null && 
            request.getAvgTransactionAmount() != null &&
            request.getMarketVolatility() != null) {
            confidence += 0.2; // 有完整数据
        }
        
        // 客户历史影响置信度
        if (request.getCustomerTradingVolume() != null && 
            request.getCustomerTradingVolume() > 10000) {
            confidence += 0.1; // 活跃客户
        }
        
        return BigDecimal.valueOf(Math.min(1.0, confidence)).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateReasoning(LLMRecommendationRequest request, BigDecimal recommendedSpread) {
        StringBuilder reasoning = new StringBuilder();
        reasoning.append("基于传统量化算法分析：");
        
        if (request.getMarketVolatility() != null) {
            double volatility = request.getMarketVolatility().doubleValue();
            if (volatility > 0.7) {
                reasoning.append("市场波动率较高(").append(volatility).append(")，适当增加spread以对冲风险；");
            } else if (volatility < 0.3) {
                reasoning.append("市场波动率较低(").append(volatility).append(")，可适度降低spread；");
            }
        }
        
        if (request.getCustomerRiskLevel() != null) {
            double riskLevel = request.getCustomerRiskLevel();
            if (riskLevel > 1.5) {
                reasoning.append("客户风险等级较高(").append(riskLevel).append(")，需要风险溢价；");
            }
        }
        
        if (request.getAvgLiquidityScore() != null) {
            double liquidity = request.getAvgLiquidityScore().doubleValue();
            if (liquidity < 3.0) {
                reasoning.append("流动性评分偏低(").append(liquidity).append(")，增加流动性风险补偿；");
            }
        }
        
        reasoning.append("最终推荐spread为").append(recommendedSpread).append("。");
        
        return reasoning.toString();
    }

    private String generateRiskAssessment(LLMRecommendationRequest request) {
        StringBuilder assessment = new StringBuilder();
        
        double riskLevel = request.getCustomerRiskLevel() != null ? request.getCustomerRiskLevel() : 1.0;
        double volatility = request.getMarketVolatility() != null ? request.getMarketVolatility().doubleValue() : 0.5;
        
        if (riskLevel > 1.5 || volatility > 0.7) {
            assessment.append("高风险：");
            if (riskLevel > 1.5) assessment.append("客户风险等级偏高；");
            if (volatility > 0.7) assessment.append("市场波动性较大；");
            assessment.append("建议采用保守定价策略。");
        } else if (riskLevel < 0.8 && volatility < 0.3) {
            assessment.append("低风险：客户信用良好，市场稳定，可考虑优惠定价。");
        } else {
            assessment.append("中等风险：客户和市场条件均在正常范围内，采用标准定价策略。");
        }
        
        return assessment.toString();
    }

    private String generateMarketAnalysis(LLMRecommendationRequest request) {
        StringBuilder analysis = new StringBuilder();
        
        analysis.append("市场分析：");
        
        if (request.getMarketVolatility() != null) {
            double volatility = request.getMarketVolatility().doubleValue();
            if (volatility > 0.6) {
                analysis.append("当前市场波动较大，建议密切关注市场动向；");
            } else {
                analysis.append("市场相对稳定，有利于精确定价；");
            }
        }
        
        // 基于货币类型的分析
        String currencyAnalysis = getCurrencyAnalysis(request.getCurrency().toString());
        analysis.append(currencyAnalysis);
        
        return analysis.toString();
    }

    private String getCurrencyAnalysis(String currency) {
        return switch (currency.toUpperCase()) {
            case "USD" -> "美元作为全球储备货币，流动性充足，风险相对较低。";
            case "EUR" -> "欧元区经济稳定，但需关注欧央行政策动向。";
            case "JPY" -> "日元具有避险属性，在市场不确定时期需求增加。";
            case "GBP" -> "英镑波动性相对较高，需关注英国政治经济动态。";
            case "CNY" -> "人民币国际化程度不断提升，但汇率管控较严。";
            default -> "该货币流动性一般，需要额外的流动性风险补偿。";
        };
    }

    @Override
    public boolean isAvailable() {
        return true; // 回退服务始终可用
    }

    @Override
    public String getModelName() {
        return "Traditional Algorithm";
    }

    @Override
    public String getProvider() {
        return "Fallback Service";
    }
}
