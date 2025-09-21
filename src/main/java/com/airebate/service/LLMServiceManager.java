package com.airebate.service;

import com.airebate.dto.LLMRecommendationRequest;
import com.airebate.dto.LLMRecommendationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * LLM服务管理器
 * 负责管理多个LLM服务实例，提供服务选择和故障转移功能
 */
@Service
public class LLMServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(LLMServiceManager.class);

    @Value("${ai.llm.enabled:true}")
    private boolean llmEnabled;

    @Value("${ai.llm.fallback-enabled:true}")
    private boolean fallbackEnabled;

    private final List<LLMService> llmServices;

    public LLMServiceManager(List<LLMService> llmServices) {
        this.llmServices = llmServices;
        logger.info("初始化LLM服务管理器，发现 {} 个服务实例", llmServices.size());
        
        // 记录所有可用的服务
        for (LLMService service : llmServices) {
            logger.info("注册LLM服务: {} - {}", service.getProvider(), service.getModelName());
        }
    }

    /**
     * 生成推荐
     * 优先使用配置的主要服务，失败时自动切换到其他可用服务
     */
    public LLMRecommendationResponse generateRecommendation(LLMRecommendationRequest request) {
        if (!llmEnabled) {
            logger.info("LLM服务已禁用，跳过LLM推荐");
            return LLMRecommendationResponse.error("LLM服务已禁用");
        }

        // 尝试使用主要服务
        Optional<LLMService> primaryService = getPrimaryService();
        if (primaryService.isPresent()) {
            try {
                LLMRecommendationResponse response = primaryService.get().generateRecommendation(request);
                if (response.isSuccess()) {
                    logger.info("主要LLM服务 {} 成功生成推荐", primaryService.get().getProvider());
                    return response;
                }
                logger.warn("主要LLM服务返回失败响应: {}", response.getErrorMessage());
            } catch (Exception e) {
                logger.error("主要LLM服务调用失败", e);
            }
        }

        // 如果主要服务失败，尝试其他可用服务
        if (fallbackEnabled) {
            for (LLMService service : llmServices) {
                if (primaryService.isPresent() && service.equals(primaryService.get())) {
                    continue; // 跳过已经尝试过的主要服务
                }

                if (service.isAvailable()) {
                    try {
                        logger.info("尝试使用备用LLM服务: {}", service.getProvider());
                        LLMRecommendationResponse response = service.generateRecommendation(request);
                        if (response.isSuccess()) {
                            logger.info("备用LLM服务 {} 成功生成推荐", service.getProvider());
                            return response;
                        }
                    } catch (Exception e) {
                        logger.error("备用LLM服务 {} 调用失败", service.getProvider(), e);
                    }
                }
            }
        }

        // 所有服务都失败
        logger.error("所有LLM服务都不可用");
        return LLMRecommendationResponse.error("所有LLM服务都不可用");
    }

    /**
     * 获取主要服务（第一个可用的服务）
     */
    private Optional<LLMService> getPrimaryService() {
        return llmServices.stream()
                .filter(LLMService::isAvailable)
                .findFirst();
    }

    /**
     * 检查是否有可用的LLM服务
     */
    public boolean hasAvailableService() {
        return llmEnabled && llmServices.stream().anyMatch(LLMService::isAvailable);
    }

    /**
     * 获取所有可用服务的状态信息
     */
    public List<ServiceStatus> getServiceStatus() {
        return llmServices.stream()
                .map(service -> new ServiceStatus(
                        service.getProvider(),
                        service.getModelName(),
                        service.isAvailable()
                ))
                .toList();
    }

    /**
     * 服务状态内部类
     */
    public static class ServiceStatus {
        private final String provider;
        private final String modelName;
        private final boolean available;

        public ServiceStatus(String provider, String modelName, boolean available) {
            this.provider = provider;
            this.modelName = modelName;
            this.available = available;
        }

        public String getProvider() {
            return provider;
        }

        public String getModelName() {
            return modelName;
        }

        public boolean isAvailable() {
            return available;
        }

        @Override
        public String toString() {
            return String.format("%s (%s) - %s", provider, modelName, available ? "可用" : "不可用");
        }
    }
}
