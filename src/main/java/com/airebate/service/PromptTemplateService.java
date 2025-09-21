package com.airebate.service;

import com.airebate.dto.LLMRecommendationRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM提示词模板管理服务
 * 负责管理和生成不同场景下的提示词模板
 */
@Service
public class PromptTemplateService {

    /**
     * 生成标准推荐提示词
     */
    public String generateStandardPrompt(LLMRecommendationRequest request) {
        return String.format("""
            你是一位资深的金融风险管理和外汇交易专家，拥有20年的银行业从业经验。请为以下客户提供专业的rebate spread定价建议。
            
            ## 任务说明
            基于客户历史交易数据、市场环境和风险评估，为客户推荐最优的rebate spread值。
            
            ## 客户基本信息
            - 客户代码: %s
            - 客户名称: %s
            - 交易货币: %s
            - 推荐日期: %s
            
            ## 历史交易分析
            - 平均交易量: %s
            - 平均交易金额: %s
            - 平均利润率: %s%%
            - 平均流动性评分: %s (1-10分)
            - 市场波动率: %s
            - 客户风险等级: %s
            - 客户总交易量: %s
            
            ## 市场环境分析
            当前市场状况: %s
            
            ## 客户风险画像
            客户特征: %s
            
            ## 定价约束条件
            - 最小允许spread: %s
            - 最大允许spread: %s
            - 基准spread: %s
            
            ## 分析要求
            请运用金融量化方法、风险管理理论和市场经验，综合考虑以下因素：
            1. 客户信用风险和交易历史表现
            2. 市场流动性和波动性风险
            3. 货币对特性和地缘政治风险
            4. 竞争环境和客户关系价值
            5. 监管要求和合规考虑
            
            ## 输出格式要求
            请严格按照以下JSON格式提供分析结果：
            
            ```json
            {
                "recommendedSpread": 数值 (必须在 %s 到 %s 之间),
                "confidenceScore": 小数值 (0到1之间),
                "reasoning": "详细的推荐理由，包括定价逻辑、风险评估和市场分析",
                "riskAssessment": "客户风险评估和风险缓释建议",
                "marketAnalysis": "市场环境分析和趋势判断",
                "keyFactors": ["主要影响因素1", "主要影响因素2", "主要影响因素3"]
            }
            ```
            
            ## 注意事项
            1. 推荐的spread值必须在约束范围内
            2. 置信度评分要基于数据质量和市场确定性
            3. 推荐理由要具体、专业、可执行
            4. 风险评估要全面且具有前瞻性
            """,
            request.getCustomerCode(),
            request.getCustomerName(),
            request.getCurrency(),
            request.getRecommendationDate(),
            request.getAvgTransactionVolume(),
            request.getAvgTransactionAmount(),
            request.getAvgProfitMargin(),
            request.getAvgLiquidityScore(),
            request.getMarketVolatility(),
            request.getCustomerRiskLevel(),
            request.getCustomerTradingVolume(),
            request.getMarketCondition() != null ? request.getMarketCondition() : "标准市场条件",
            request.getCustomerProfile() != null ? request.getCustomerProfile() : "标准客户",
            request.getMinSpread(),
            request.getMaxSpread(),
            request.getDefaultSpread(),
            request.getMinSpread(),
            request.getMaxSpread()
        );
    }

    /**
     * 生成保守型推荐提示词（用于高风险客户）
     */
    public String generateConservativePrompt(LLMRecommendationRequest request) {
        return String.format("""
            你是一位风险管理专家，请为高风险客户提供保守的rebate spread定价建议。
            
            ## 风险控制要求
            对于风险等级较高的客户，需要采用保守的定价策略，优先考虑风险控制而非盈利最大化。
            
            ## 客户信息
            客户代码: %s，风险等级: %s（高风险）
            交易货币: %s，市场波动率: %s（需要额外关注）
            
            ## 保守定价原则
            1. 适当提高spread以对冲风险
            2. 考虑流动性风险补偿
            3. 设置更严格的风险限额
            4. 加强持续监控要求
            
            请在约束范围 %s - %s 内推荐一个偏保守的spread值，并说明风险控制措施。
            
            输出JSON格式，重点关注风险控制。
            """,
            request.getCustomerCode(),
            request.getCustomerRiskLevel(),
            request.getCurrency(),
            request.getMarketVolatility(),
            request.getMinSpread(),
            request.getMaxSpread()
        );
    }

    /**
     * 生成优惠型推荐提示词（用于优质客户）
     */
    public String generatePreferentialPrompt(LLMRecommendationRequest request) {
        return String.format("""
            你是一位客户关系管理专家，请为优质客户提供有竞争力的rebate spread定价建议。
            
            ## 客户价值评估
            客户 %s 是我们的优质客户，风险等级: %s（低风险），交易量: %s（活跃）
            
            ## 优惠定价策略
            1. 适当降低spread以维护客户关系
            2. 考虑长期价值和客户忠诚度
            3. 保持竞争优势
            4. 平衡盈利性和客户满意度
            
            ## 市场竞争考虑
            当前市场环境: %s
            需要考虑同业竞争和客户期望
            
            请在约束范围 %s - %s 内推荐一个有竞争力的spread值。
            
            输出JSON格式，强调客户价值和关系维护。
            """,
            request.getCustomerCode(),
            request.getCustomerRiskLevel(),
            request.getCustomerTradingVolume(),
            request.getMarketCondition(),
            request.getMinSpread(),
            request.getMaxSpread()
        );
    }

    /**
     * 生成市场波动型提示词（用于高波动市场环境）
     */
    public String generateVolatilityPrompt(LLMRecommendationRequest request) {
        return String.format("""
            你是一位市场风险专家，当前市场环境波动较大，请提供适应性的rebate spread定价建议。
            
            ## 市场波动情况
            当前市场波动率: %s（高波动）
            货币对: %s，市场状况: %s
            
            ## 波动环境定价策略
            1. 增加波动性风险补偿
            2. 缩短定价有效期
            3. 设置动态调整机制
            4. 加强实时监控
            
            ## 客户影响评估
            客户 %s 的历史表现: 利润率 %s，流动性评分 %s
            在波动环境下的适应能力评估
            
            请推荐适合高波动环境的spread值（范围: %s - %s），并说明应对措施。
            
            输出JSON格式，重点关注波动性管理。
            """,
            request.getMarketVolatility(),
            request.getCurrency(),
            request.getMarketCondition(),
            request.getCustomerCode(),
            request.getAvgProfitMargin(),
            request.getAvgLiquidityScore(),
            request.getMinSpread(),
            request.getMaxSpread()
        );
    }

    /**
     * 根据场景选择合适的提示词模板
     */
    public String generatePromptByScenario(LLMRecommendationRequest request) {
        // 分析场景特征
        boolean isHighRisk = request.getCustomerRiskLevel() != null && request.getCustomerRiskLevel() > 1.5;
        boolean isLowRisk = request.getCustomerRiskLevel() != null && request.getCustomerRiskLevel() < 0.8;
        boolean isHighVolatility = request.getMarketVolatility() != null && 
                                 request.getMarketVolatility().doubleValue() > 0.7;
        boolean isHighVolume = request.getCustomerTradingVolume() != null && 
                             request.getCustomerTradingVolume() > 50000;

        // 场景判断和模板选择
        if (isHighVolatility) {
            return generateVolatilityPrompt(request);
        } else if (isHighRisk) {
            return generateConservativePrompt(request);
        } else if (isLowRisk && isHighVolume) {
            return generatePreferentialPrompt(request);
        } else {
            return generateStandardPrompt(request);
        }
    }

    /**
     * 获取提示词模板变量
     */
    public Map<String, Object> getTemplateVariables(LLMRecommendationRequest request) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerCode", request.getCustomerCode());
        variables.put("customerName", request.getCustomerName());
        variables.put("currency", request.getCurrency());
        variables.put("recommendationDate", request.getRecommendationDate());
        variables.put("avgTransactionVolume", request.getAvgTransactionVolume());
        variables.put("avgTransactionAmount", request.getAvgTransactionAmount());
        variables.put("avgProfitMargin", request.getAvgProfitMargin());
        variables.put("avgLiquidityScore", request.getAvgLiquidityScore());
        variables.put("marketVolatility", request.getMarketVolatility());
        variables.put("customerRiskLevel", request.getCustomerRiskLevel());
        variables.put("customerTradingVolume", request.getCustomerTradingVolume());
        variables.put("marketCondition", request.getMarketCondition());
        variables.put("customerProfile", request.getCustomerProfile());
        variables.put("minSpread", request.getMinSpread());
        variables.put("maxSpread", request.getMaxSpread());
        variables.put("defaultSpread", request.getDefaultSpread());
        
        return variables;
    }
}
