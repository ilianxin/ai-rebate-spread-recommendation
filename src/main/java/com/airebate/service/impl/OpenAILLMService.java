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
 * OpenAI GPT服务实现
 */
@Service
@ConditionalOnProperty(name = "ai.llm.provider", havingValue = "openai")
public class OpenAILLMService implements LLMService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAILLMService.class);

    @Value("${ai.llm.openai.api-key}")
    private String apiKey;

    @Value("${ai.llm.openai.model:gpt-4}")
    private String model;

    @Value("${ai.llm.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${ai.llm.openai.temperature:0.3}")
    private double temperature;

    @Value("${ai.llm.openai.max-tokens:1000}")
    private int maxTokens;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public LLMRecommendationResponse generateRecommendation(LLMRecommendationRequest request) {
        try {
            logger.info("正在为客户 {} 使用OpenAI GPT生成推荐", request.getCustomerCode());

            String prompt = buildPrompt(request);
            String response = callOpenAI(prompt);
            
            return parseResponse(response, request);

        } catch (Exception e) {
            logger.error("OpenAI API调用失败", e);
            return LLMRecommendationResponse.error("LLM服务调用失败: " + e.getMessage());
        }
    }

    private String buildPrompt(LLMRecommendationRequest request) {
        return String.format("""
            你是一位资深的外汇交易和风险管理专家，专门为银行提供rebate spread定价建议。
            
            请基于以下信息为客户提供rebate spread推荐：
            
            ## 客户信息
            - 客户代码: %s
            - 客户名称: %s
            - 货币对: %s
            - 推荐日期: %s
            
            ## 历史交易数据摘要
            - 平均交易量: %s
            - 平均交易金额: %s
            - 平均利润率: %s
            - 平均流动性评分: %s
            - 市场波动率: %s
            - 客户风险等级: %s
            - 客户交易量: %s
            
            ## 市场状况
            %s
            
            ## 客户特征
            %s
            
            ## 业务约束
            - 最小spread: %s
            - 最大spread: %s
            - 默认spread: %s
            
            请提供：
            1. 推荐的spread值（必须在最小和最大范围内）
            2. 置信度评分（0-1之间）
            3. 详细的推荐理由
            4. 风险评估
            5. 市场分析
            6. 关键影响因素（列表形式）
            
            请以JSON格式回复，确保数值准确且在约束范围内：
            {
                "recommendedSpread": 数值,
                "confidenceScore": 数值,
                "reasoning": "详细推荐理由",
                "riskAssessment": "风险评估",
                "marketAnalysis": "市场分析",
                "keyFactors": ["因素1", "因素2", "因素3"]
            }
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
            request.getDefaultSpread()
        );
    }

    private String callOpenAI(String prompt) throws Exception {
        String url = baseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.get("choices").get(0).get("message").get("content").asText();
        } else {
            throw new RuntimeException("OpenAI API调用失败: " + response.getStatusCode());
        }
    }

    private LLMRecommendationResponse parseResponse(String apiResponse, LLMRecommendationRequest request) {
        try {
            // 尝试解析JSON响应
            String jsonStr = extractJsonFromResponse(apiResponse);
            JsonNode jsonResponse = objectMapper.readTree(jsonStr);

            LLMRecommendationResponse response = new LLMRecommendationResponse();
            
            // 解析推荐spread
            BigDecimal recommendedSpread = new BigDecimal(jsonResponse.get("recommendedSpread").asText());
            
            // 确保在约束范围内
            if (recommendedSpread.compareTo(request.getMinSpread()) < 0) {
                recommendedSpread = request.getMinSpread();
            }
            if (recommendedSpread.compareTo(request.getMaxSpread()) > 0) {
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

            return response;

        } catch (Exception e) {
            logger.error("解析LLM响应失败", e);
            
            // 返回简化的响应
            LLMRecommendationResponse fallbackResponse = new LLMRecommendationResponse();
            fallbackResponse.setRecommendedSpread(request.getDefaultSpread());
            fallbackResponse.setConfidenceScore(BigDecimal.valueOf(0.5));
            fallbackResponse.setReasoning("LLM响应解析失败，使用默认推荐值。原始响应: " + apiResponse);
            fallbackResponse.setModelUsed(model);
            
            return fallbackResponse;
        }
    }

    private String extractJsonFromResponse(String response) {
        // 尝试提取JSON部分
        Pattern jsonPattern = Pattern.compile("\\{[^{}]*(?:\\{[^{}]*\\}[^{}]*)*\\}", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(response);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        // 如果没有找到JSON，返回原始响应
        return response;
    }

    @Override
    public boolean isAvailable() {
        try {
            // 简单的健康检查
            return apiKey != null && !apiKey.trim().isEmpty();
        } catch (Exception e) {
            logger.error("检查OpenAI服务可用性失败", e);
            return false;
        }
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public String getProvider() {
        return "OpenAI";
    }
}
