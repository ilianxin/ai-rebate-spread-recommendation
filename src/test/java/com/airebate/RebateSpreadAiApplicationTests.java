package com.airebate;

import com.airebate.dto.RecommendationRequest;
import com.airebate.dto.RecommendationResponse;
import com.airebate.model.Currency;
import com.airebate.service.RebateSpreadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RebateSpreadAiApplicationTests {

    @Autowired
    private RebateSpreadService rebateSpreadService;

    @Test
    void contextLoads() {
        // 测试Spring上下文是否正常加载
        assertNotNull(rebateSpreadService);
    }

    @Test
    void testRecommendationService() {
        // 测试推荐服务基本功能
        RecommendationRequest request = new RecommendationRequest();
        request.setCustomerCode("US_BANK_001");
        request.setCurrency(Currency.USD);
        request.setQueryDate(LocalDate.now());
        request.setDaysRange(30);

        RecommendationResponse response = rebateSpreadService.getRecommendation(request);
        
        assertNotNull(response);
        assertNotNull(response.getStatus());
        
        if ("SUCCESS".equals(response.getStatus())) {
            assertNotNull(response.getRecommendedSpread());
            assertTrue(response.getRecommendedSpread().doubleValue() > 0);
            assertTrue(response.getRecommendedSpread().doubleValue() < 1);
        }
    }

    @Test
    void testGetSystemStats() {
        // 测试系统统计功能
        String stats = rebateSpreadService.getSystemStats();
        assertNotNull(stats);
        assertTrue(stats.contains("客户数"));
        assertTrue(stats.contains("账单记录数"));
        assertTrue(stats.contains("推荐记录数"));
    }
}
