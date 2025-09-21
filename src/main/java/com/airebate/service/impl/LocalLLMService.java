package com.airebate.service.impl;

import com.airebate.dto.LLMRecommendationRequest;
import com.airebate.dto.LLMRecommendationResponse;
import com.airebate.service.LLMService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地大模型服务实现（支持Ollama、LocalAI等）
 */
@Service
@ConditionalOnProperty(name = "ai.llm.provider", havingValue = "local")
public class LocalLLMService implements LLMService {

    private static final Logger logger = LoggerFactory.getLogger(LocalLLMService.class);

    @Value("${ai.llm.local.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${ai.llm.local.model:llama3}")
    private String model;

    @Value("${ai.llm.local.timeout:30000}")
    private int timeout;

    @Value("${ai.llm.local.temperature:0.3}")
    private double temperature;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public LLMRecommendationResponse generateRecommendation(LLMRecommendationRequest request) {
        try {
            logger.info("正在为客户 {} 使用本地模型 {} 生成推荐", request.getCustomerCode(), model);

            String prompt = buildPrompt(request);
            String response = callLocalLLM(prompt);
            
            return parseResponse(response, request);

        } catch (Exception e) {
            logger.error("本地LLM服务调用失败", e);
            return LLMRecommendationResponse.error("本地LLM服务调用失败: " + e.getMessage());
        }
    }

    private String buildPrompt(LLMRecommendationRequest request) {
        return String.format("""
            你是一位专业的金融量化分析师，专门负责外汇交易的rebate spread定价。
            
            # 任务
            为以下客户提供精确的rebate spread推荐建议。
            
            # 客户信息
            客户代码: %s
            客户名称: %s
            交易货币: %s
            推荐日期: %s
            
            # 历史数据分析
            平均交易量: %s
            平均交易金额: %s
            平均利润率: %s%%
            平均流动性评分: %s/10
            市场波动率: %s
            客户风险等级: %s
            客户总交易量: %s
            
            # 市场环境
            当前市场状况: %s
            客户风险画像: %s
            
            # 约束条件
            最小允许spread: %s
            最大允许spread: %s
            基准spread: %s
            
            # 要求
            请基于上述信息，运用金融量化模型和风险管理原理，提供：
            
            1. 精确的spread推荐值（必须在 %s 到 %s 之间）
            2. 推荐置信度（0到1之间的小数）
            3. 详细的定价逻辑和依据
            4. 客户风险评估
            5. 市场趋势分析
            6. 主要影响因素
            
            请严格按照以下JSON格式回复：
            ```json
            {
                "recommendedSpread": 数值,
                "confidenceScore": 小数值,
                "reasoning": "详细的推荐理由和计算逻辑",
                "riskAssessment": "风险评估分析",
                "marketAnalysis": "市场趋势和环境分析",
                "keyFactors": ["主要因素1", "主要因素2", "主要因素3"]
            }
            ```
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
            request.getMarketCondition() != null ? request.getMarketCondition() : "正常市场条件",
            request.getCustomerProfile() != null ? request.getCustomerProfile() : "标准企业客户",
            request.getMinSpread(),
            request.getMaxSpread(),
            request.getDefaultSpread(),
            request.getMinSpread(),
            request.getMaxSpread()
        );
    }

    private String callLocalLLM(String prompt) throws Exception {
        String url = baseUrl + "/api/generate";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        
        // 添加参数控制
        Map<String, Object> options = new HashMap<>();
        options.put("temperature", temperature);
        options.put("top_p", 0.9);
        options.put("top_k", 40);
        requestBody.put("options", options);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.get("response").asText();
        } else {
            throw new RuntimeException("本地LLM API调用失败: " + response.getStatusCode());
        }
    }

    private LLMRecommendationResponse parseResponse(String apiResponse, LLMRecommendationRequest request) {
        try {
            // 提取JSON部分
            String jsonStr = extractJsonFromResponse(apiResponse);
            JsonNode jsonResponse = objectMapper.readTree(jsonStr);

            LLMRecommendationResponse response = new LLMRecommendationResponse();
            
            // 解析推荐spread
            BigDecimal recommendedSpread = new BigDecimal(jsonResponse.get("recommendedSpread").asText());
            
            // 确保在约束范围内
            if (recommendedSpread.compareTo(request.getMinSpread()) < 0) {
                logger.warn("推荐spread {} 低于最小值 {}，调整为最小值", recommendedSpread, request.getMinSpread());
                recommendedSpread = request.getMinSpread();
            }
            if (recommendedSpread.compareTo(request.getMaxSpread()) > 0) {
                logger.warn("推荐spread {} 高于最大值 {}，调整为最大值", recommendedSpread, request.getMaxSpread());
                recommendedSpread = request.getMaxSpread();
            }
            
            response.setRecommendedSpread(recommendedSpread);
            response.setConfidenceScore(new BigDecimal(jsonResponse.get("confidenceScore").asText()));
            response.setReasoning(jsonResponse.get("reasoning").asText());
            response.setRiskAssessment(jsonResponse.get("riskAssessment").asText());
            response.setMarketAnalysis(jsonResponse.get("marketAnalysis").asText());
            response.setModelUsed(model);

            // 解析关键因素
            if (jsonResponse.has("keyFactors")) {
                List<String> keyFactors = new ArrayList<>();
                jsonResponse.get("keyFactors").forEach(factor -> keyFactors.add(factor.asText()));
                response.setKeyFactors(keyFactors);
            }

            logger.info("成功解析本地LLM响应，推荐spread: {}, 置信度: {}", 
                       recommendedSpread, response.getConfidenceScore());

            return response;

        } catch (Exception e) {
            logger.error("解析本地LLM响应失败: {}", e.getMessage());
            logger.debug("原始响应: {}", apiResponse);
            
            // 返回回退响应
            LLMRecommendationResponse fallbackResponse = new LLMRecommendationResponse();
            fallbackResponse.setRecommendedSpread(request.getDefaultSpread());
            fallbackResponse.setConfidenceScore(BigDecimal.valueOf(0.3));
            fallbackResponse.setReasoning("本地LLM响应解析失败，使用默认推荐策略。错误: " + e.getMessage());
            fallbackResponse.setRiskAssessment("由于解析错误，采用保守的风险评估");
            fallbackResponse.setMarketAnalysis("无法完成市场分析，建议人工复核");
            fallbackResponse.setModelUsed(model + " (解析失败)");
            
            return fallbackResponse;
        }
    }

    private String extractJsonFromResponse(String response) {
        // 首先尝试找到```json块
        Pattern codeBlockPattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```", Pattern.CASE_INSENSITIVE);
        Matcher codeBlockMatcher = codeBlockPattern.matcher(response);
        
        if (codeBlockMatcher.find()) {
            return codeBlockMatcher.group(1).trim();
        }
        
        // 然后尝试找到任何JSON对象
        Pattern jsonPattern = Pattern.compile("\\{[^{}]*(?:\\{[^{}]*\\}[^{}]*)*\\}", Pattern.DOTALL);
        Matcher jsonMatcher = jsonPattern.matcher(response);
        
        if (jsonMatcher.find()) {
            return jsonMatcher.group();
        }
        
        // 如果都没找到，尝试简单的键值对提取
        logger.warn("无法提取JSON，尝试简单解析");
        return createFallbackJson(response);
    }

    private String createFallbackJson(String response) {
        // 简单的回退策略：创建一个基本的JSON响应
        return String.format("""
            {
                "recommendedSpread": "%s",
                "confidenceScore": "0.5",
                "reasoning": "LLM响应格式异常，采用默认策略",
                "riskAssessment": "中等风险",
                "marketAnalysis": "标准市场分析",
                "keyFactors": ["数据质量", "市场稳定性", "客户历史表现"]
            }
            """, "0.1");
    }

    @Override
    public boolean isAvailable() {
        try {
            String url = baseUrl + "/api/tags";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.error("检查本地LLM服务可用性失败", e);
            return false;
        }
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public String getProvider() {
        return "Local LLM (Ollama)";
    }
}
