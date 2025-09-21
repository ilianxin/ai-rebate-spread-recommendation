package com.airebate.config;

import com.airebate.service.RebateSpreadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulingConfig.class);
    
    @Autowired
    private RebateSpreadService rebateSpreadService;

    /**
     * 每天凌晨2点清理过期推荐
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredRecommendations() {
        logger.info("开始执行定时清理过期推荐任务");
        try {
            rebateSpreadService.cleanupExpiredRecommendations();
            logger.info("定时清理过期推荐任务完成");
        } catch (Exception e) {
            logger.error("定时清理过期推荐任务失败", e);
        }
    }

    /**
     * 每小时打印系统统计信息
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void logSystemStats() {
        try {
            String stats = rebateSpreadService.getSystemStats();
            logger.info("系统统计: {}", stats);
        } catch (Exception e) {
            logger.warn("获取系统统计信息失败", e);
        }
    }
}
