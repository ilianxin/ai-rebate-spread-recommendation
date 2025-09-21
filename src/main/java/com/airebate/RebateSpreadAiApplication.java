package com.airebate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Rebate Spread AI 微服务主启动类
 * 用于基于跨客户不同货币动态账单结果生成rebate spread数值推荐
 */
@SpringBootApplication
public class RebateSpreadAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RebateSpreadAiApplication.class, args);
    }
}
