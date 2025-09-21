package com.airebate.service;

import com.airebate.dto.LLMRecommendationRequest;
import com.airebate.dto.LLMRecommendationResponse;

/**
 * LLM (大语言模型) 服务接口
 * 支持多种LLM提供商的集成
 */
public interface LLMService {
    
    /**
     * 生成AI推荐
     * @param request LLM推荐请求
     * @return LLM推荐响应
     */
    LLMRecommendationResponse generateRecommendation(LLMRecommendationRequest request);
    
    /**
     * 检查LLM服务可用性
     * @return 是否可用
     */
    boolean isAvailable();
    
    /**
     * 获取模型名称
     * @return 模型名称
     */
    String getModelName();
    
    /**
     * 获取服务提供商
     * @return 提供商名称
     */
    String getProvider();
}
