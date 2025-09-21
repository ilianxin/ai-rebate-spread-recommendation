package com.airebate.controller;

import com.airebate.dto.RecommendationRequest;
import com.airebate.dto.RecommendationResponse;
import com.airebate.model.Currency;
import com.airebate.model.RebateSpreadRecommendation;
import com.airebate.service.LLMServiceManager;
import com.airebate.service.RebateSpreadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rebate Spread推荐API控制器
 * 提供基于不同货币和日期的动态推荐接口
 */
@RestController
@RequestMapping("/recommendations")
@Tag(name = "Rebate Spread推荐", description = "基于AI的动态rebate spread推荐服务")
public class RebateRecommendationController {
    
    private static final Logger logger = LoggerFactory.getLogger(RebateRecommendationController.class);
    
    @Autowired
    private RebateSpreadService rebateSpreadService;
    
    @Autowired
    private LLMServiceManager llmServiceManager;

    @Operation(summary = "获取智能推荐", description = "根据客户代码、货币类型和查询日期获取AI生成的rebate spread推荐")
    @PostMapping("/recommend")
    public ResponseEntity<RecommendationResponse> getRecommendation(
            @Valid @RequestBody RecommendationRequest request) {
        
        logger.info("收到推荐请求: {}", request.getCustomerCode());
        
        RecommendationResponse response = rebateSpreadService.getRecommendation(request);
        
        if ("ERROR".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        } else if ("WARNING".equals(response.getStatus())) {
            return ResponseEntity.ok().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "获取推荐（GET方式）", description = "通过GET参数获取推荐，便于简单调用")
    @GetMapping("/recommend")
    public ResponseEntity<RecommendationResponse> getRecommendationByParams(
            @Parameter(description = "客户代码", required = true)
            @RequestParam String customerCode,
            
            @Parameter(description = "货币类型", required = true)
            @RequestParam Currency currency,
            
            @Parameter(description = "查询日期 (格式: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate queryDate,
            
            @Parameter(description = "历史数据天数范围，默认30天")
            @RequestParam(defaultValue = "30") Integer daysRange) {
        
        RecommendationRequest request = new RecommendationRequest(customerCode, currency, queryDate);
        request.setDaysRange(daysRange);
        
        return getRecommendation(request);
    }

    @Operation(summary = "获取客户推荐历史", description = "查看指定客户的历史推荐记录")
    @GetMapping("/history/{customerCode}")
    public ResponseEntity<List<RebateSpreadRecommendation>> getRecommendationHistory(
            @Parameter(description = "客户代码", required = true)
            @PathVariable String customerCode,
            
            @Parameter(description = "返回记录数量限制，默认10条")
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<RebateSpreadRecommendation> history = 
                rebateSpreadService.getCustomerRecommendationHistory(customerCode, limit);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "支持的货币列表", description = "获取系统支持的所有货币类型")
    @GetMapping("/currencies")
    public ResponseEntity<Currency[]> getSupportedCurrencies() {
        return ResponseEntity.ok(Currency.values());
    }

    @Operation(summary = "系统健康检查", description = "检查服务状态和基本统计信息")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            String stats = rebateSpreadService.getSystemStats();
            return ResponseEntity.ok("服务正常运行 - " + stats);
        } catch (Exception e) {
            logger.error("健康检查失败", e);
            return ResponseEntity.status(500).body("服务异常: " + e.getMessage());
        }
    }

    @Operation(summary = "清理过期推荐", description = "手动触发清理过期的推荐记录")
    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupExpiredRecommendations() {
        try {
            rebateSpreadService.cleanupExpiredRecommendations();
            return ResponseEntity.ok("过期推荐清理完成");
        } catch (Exception e) {
            logger.error("清理过期推荐失败", e);
            return ResponseEntity.status(500).body("清理失败: " + e.getMessage());
        }
    }

    @Operation(summary = "LLM服务状态", description = "检查LLM服务的可用性和状态")
    @GetMapping("/llm/status")
    public ResponseEntity<?> getLLMStatus() {
        try {
            boolean hasAvailableService = llmServiceManager.hasAvailableService();
            List<LLMServiceManager.ServiceStatus> serviceStatuses = llmServiceManager.getServiceStatus();
            
            Map<String, Object> status = new HashMap<>();
            status.put("llmEnabled", hasAvailableService);
            status.put("availableServices", serviceStatuses.size());
            status.put("services", serviceStatuses);
            status.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("获取LLM状态失败", e);
            return ResponseEntity.status(500).body("状态检查失败: " + e.getMessage());
        }
    }

    @Operation(summary = "LLM推荐测试", description = "测试LLM推荐功能")
    @PostMapping("/llm/test")
    public ResponseEntity<?> testLLMRecommendation(
            @RequestParam(defaultValue = "TEST001") String customerCode,
            @RequestParam(defaultValue = "USD") Currency currency) {
        try {
            // 创建测试请求
            RecommendationRequest testRequest = new RecommendationRequest();
            testRequest.setCustomerCode(customerCode);
            testRequest.setCurrency(currency);
            testRequest.setQueryDate(LocalDate.now());
            
            logger.info("执行LLM推荐测试，客户: {}, 货币: {}", customerCode, currency);
            
            RecommendationResponse response = rebateSpreadService.getRecommendation(testRequest);
            
            // 添加测试标识
            Map<String, Object> testResult = new HashMap<>();
            testResult.put("testMode", true);
            testResult.put("recommendation", response);
            testResult.put("llmUsed", response.isUsedLLM());
            testResult.put("provider", response.getLlmProvider());
            testResult.put("model", response.getLlmModel());
            
            return ResponseEntity.ok(testResult);
        } catch (Exception e) {
            logger.error("LLM推荐测试失败", e);
            return ResponseEntity.status(500).body("测试失败: " + e.getMessage());
        }
    }
}
