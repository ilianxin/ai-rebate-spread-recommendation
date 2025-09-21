# AI Rebate Spread 推荐系统

基于大语言模型（LLM）的智能外汇rebate spread推荐系统，为银行和金融机构提供精准、专业的动态定价建议。

## 🌟 项目特色

- **🤖 LLM智能推荐**: 集成OpenAI GPT、本地模型等多种大语言模型
- **🎯 专业金融分析**: 基于量化金融理论和风险管理原理
- **⚡ 多层回退机制**: 确保99.9%系统可用性
- **🔧 灵活配置**: 支持多种部署方案和配置选项
- **📊 实时监控**: 提供完整的API监控和状态检查
- **🛡️ 企业级安全**: 数据加密、权限控制、审计日志

## 🏗️ 系统架构

```
┌─────────────────────┐    ┌──────────────────────┐    ┌─────────────────────┐
│   API Controller    │────│   Service Layer      │────│   Repository Layer  │
└─────────────────────┘    └──────────────────────┘    └─────────────────────┘
                                       │
                           ┌──────────────────────┐
                           │   LLM Service Layer  │
                           └──────────────────────┘
                                       │
        ┌──────────────────┬──────────────────┬──────────────────┐
        │                  │                  │                  │
┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│  OpenAI GPT   │  │  Local Model  │  │   Fallback    │  │   Cache       │
│   Service     │  │   (Ollama)    │  │   Service     │  │   Layer       │
└───────────────┘  └───────────────┘  └───────────────┘  └───────────────┘
```

## 🚀 快速开始

### 环境要求

- **Java**: 17+
- **Spring Boot**: 3.x
- **数据库**: H2 (默认) / MySQL / PostgreSQL
- **内存**: 最小2GB，推荐4GB+

### 1. 克隆项目

```bash
git clone <repository-url>
cd ai-rebate-spread
```

### 2. 配置应用

#### 选择部署方案

**方案A: OpenAI模式 (推荐)**
```yaml
# application.yml
ai:
  llm:
    provider: openai
    enabled: true
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4
```

```bash
export OPENAI_API_KEY="your-openai-api-key"
```

**方案B: 本地模型模式**
```bash
# 安装Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# 下载模型
ollama pull llama3

# 启动服务
ollama serve
```

```yaml
# application.yml
ai:
  llm:
    provider: local
    local:
      base-url: http://localhost:11434
      model: llama3
```

**方案C: 回退模式 (开箱即用)**
```yaml
# application.yml
ai:
  llm:
    provider: fallback
    enabled: true
```

### 3. 启动应用

```bash
# 开发模式
./mvnw spring-boot:run

# 生产模式
./mvnw clean package
java -jar target/rebate-spread-ai-1.0.jar
```

### 4. 验证部署

```bash
# 健康检查
curl http://localhost:8080/api/rebate-ai/recommendations/health

# LLM服务状态
curl http://localhost:8080/api/rebate-ai/recommendations/llm/status

# 测试推荐
curl -X POST http://localhost:8080/api/rebate-ai/recommendations/llm/test
```

## 📖 API 接口文档

### 基础信息

- **Base URL**: `http://localhost:8080/api/rebate-ai`
- **Content-Type**: `application/json`
- **文档地址**: http://localhost:8080/api/rebate-ai/swagger-ui.html

### 核心接口

#### 1. 获取智能推荐

**接口**: `POST /recommendations/recommend`

**请求参数**:
```json
{
  "customerCode": "CUST001",
  "currency": "USD",
  "queryDate": "2024-01-15",
  "daysRange": 30
}
```

**响应示例**:
```json
{
  "customerCode": "CUST001",
  "customerName": "优质企业客户",
  "currency": "USD",
  "recommendationDate": "2024-01-15",
  "recommendedSpread": 0.0245,
  "confidenceScore": 0.92,
  "recommendationReason": "基于GPT-4分析：客户信用等级优秀，市场流动性充足，美元波动率处于低位，建议采用有竞争力的定价策略",
  "riskAssessmentDetail": "客户风险等级较低，历史违约概率小于0.1%，建议维持当前风险敞口",
  "marketAnalysisDetail": "美元兑主要货币对波动率下降，美联储政策预期稳定，流动性环境良好",
  "keyFactors": ["客户信用等级AAA", "市场流动性充足", "政策环境稳定", "历史表现优秀"],
  "usedLLM": true,
  "llmProvider": "OpenAI",
  "llmModel": "gpt-4",
  "validUntil": "2024-01-16T10:30:00",
  "generatedAt": "2024-01-15T10:30:00",
  "status": "SUCCESS"
}
```

#### 2. 批量查询历史推荐

**接口**: `GET /recommendations/history/{customerCode}`

**查询参数**:
- `limit`: 返回记录数量 (默认10)
- `currency`: 货币过滤 (可选)

**示例**:
```bash
curl "http://localhost:8080/api/rebate-ai/recommendations/history/CUST001?limit=20&currency=USD"
```

#### 3. 货币对推荐

**接口**: `GET /recommendations/currency/{currency}`

**查询参数**:
- `date`: 查询日期 (格式: yyyy-MM-dd)
- `limit`: 返回记录数量

**示例**:
```bash
curl "http://localhost:8080/api/rebate-ai/recommendations/currency/USD?date=2024-01-15&limit=10"
```

### 管理接口

#### 4. LLM服务状态检查

**接口**: `GET /recommendations/llm/status`

**响应示例**:
```json
{
  "llmEnabled": true,
  "availableServices": 2,
  "services": [
    {
      "provider": "OpenAI",
      "modelName": "gpt-4",
      "available": true
    },
    {
      "provider": "Fallback Service",
      "modelName": "Traditional Algorithm",
      "available": true
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

#### 5. LLM推荐测试

**接口**: `POST /recommendations/llm/test`

**查询参数**:
- `customerCode`: 测试客户代码 (默认: TEST001)
- `currency`: 测试货币 (默认: USD)

**示例**:
```bash
curl -X POST "http://localhost:8080/api/rebate-ai/recommendations/llm/test?customerCode=DEMO&currency=EUR"
```

#### 6. 系统健康检查

**接口**: `GET /recommendations/health`

**响应**: 
```json
{
  "status": "UP",
  "details": {
    "database": "UP",
    "llmService": "UP",
    "timestamp": "2024-01-15T10:30:00"
  }
}
```

#### 7. 清理过期推荐

**接口**: `POST /recommendations/cleanup`

用于手动触发清理7天前的过期推荐记录。

### 数据管理接口

#### 8. 客户管理

- `GET /data/customers` - 获取客户列表
- `POST /data/customers` - 创建客户
- `PUT /data/customers/{id}` - 更新客户信息
- `DELETE /data/customers/{id}` - 删除客户

#### 9. 交易数据管理

- `GET /data/billing-results` - 获取交易数据
- `POST /data/billing-results` - 添加交易记录
- `POST /data/billing-results/batch` - 批量导入

## 🔧 配置说明

### 核心配置项

```yaml
# LLM服务配置
ai:
  llm:
    provider: openai           # 服务提供商: openai, local, fallback
    enabled: true              # 是否启用LLM
    fallback-enabled: true     # 是否启用回退机制
    
    # OpenAI配置
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4
      temperature: 0.3
      max-tokens: 1000
      timeout: 30000
    
    # 本地模型配置
    local:
      base-url: http://localhost:11434
      model: llama3
      temperature: 0.3
      timeout: 30000

# 推荐算法配置
ai:
  rebate:
    default-spread-range: 0.1
    min-spread: 0.01
    max-spread: 0.5
    volatility-weight: 0.3
    volume-weight: 0.4
    history-weight: 0.3
```

### 环境变量

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `OPENAI_API_KEY` | OpenAI API密钥 | `sk-...` |
| `DATABASE_URL` | 数据库连接 | `jdbc:mysql://...` |
| `SPRING_PROFILES_ACTIVE` | 运行环境 | `prod` |

## 🛠️ 开发指南

### 项目结构

```
src/main/java/com/airebate/
├── config/                 # 配置类
│   ├── DataInitializer.java
│   ├── SchedulingConfig.java
│   └── SwaggerConfig.java
├── controller/             # REST控制器
│   ├── DataManagementController.java
│   └── RebateRecommendationController.java
├── dto/                    # 数据传输对象
│   ├── RecommendationRequest.java
│   ├── RecommendationResponse.java
│   ├── LLMRecommendationRequest.java
│   └── LLMRecommendationResponse.java
├── model/                  # 实体模型
│   ├── Customer.java
│   ├── BillingResult.java
│   ├── Currency.java
│   └── RebateSpreadRecommendation.java
├── repository/             # 数据访问层
│   ├── CustomerRepository.java
│   ├── BillingResultRepository.java
│   └── RebateSpreadRecommendationRepository.java
├── service/                # 业务逻辑层
│   ├── AIRecommendationEngine.java
│   ├── RebateSpreadService.java
│   ├── LLMService.java
│   ├── LLMServiceManager.java
│   ├── PromptTemplateService.java
│   └── impl/
│       ├── OpenAILLMService.java
│       ├── LocalLLMService.java
│       └── FallbackLLMService.java
└── exception/              # 异常处理
    └── GlobalExceptionHandler.java
```

### 扩展LLM服务

1. **实现LLMService接口**:
```java
@Service
@ConditionalOnProperty(name = "ai.llm.provider", havingValue = "custom")
public class CustomLLMService implements LLMService {
    @Override
    public LLMRecommendationResponse generateRecommendation(LLMRecommendationRequest request) {
        // 实现自定义LLM调用逻辑
    }
}
```

2. **添加配置支持**:
```yaml
ai:
  llm:
    provider: custom
    custom:
      api-endpoint: https://api.custom.com
      api-key: your-key
```

### 自定义提示词模板

修改 `PromptTemplateService` 中的模板方法：

```java
public String generateCustomPrompt(LLMRecommendationRequest request) {
    return """
        你是专业的金融分析师...
        客户信息: %s
        请提供推荐...
        """.formatted(request.getCustomerCode());
}
```

## 📊 监控与运维

### 关键指标

- **响应时间**: API平均响应时间 < 2s
- **成功率**: 推荐生成成功率 > 99%
- **LLM可用率**: LLM服务可用率 > 95%
- **缓存命中率**: 推荐缓存命中率 > 80%

### 日志分析

```bash
# 查看推荐生成日志
grep "generateRecommendation" logs/application.log

# 查看LLM调用日志
grep "LLM\|OpenAI\|Ollama" logs/application.log

# 查看错误日志
grep "ERROR" logs/application.log
```

### 性能优化

1. **数据库优化**: 添加适当的索引
2. **缓存策略**: 启用Redis缓存
3. **连接池**: 调整数据库连接池大小
4. **JVM调优**: 设置合适的堆内存大小

## 🔒 安全配置

### API安全

```yaml
# 启用安全配置
security:
  enabled: true
  api-key: ${API_SECRET_KEY}
  rate-limit: 100  # 每分钟请求限制
```

### 数据保护

- 敏感数据加密存储
- API访问令牌验证
- 请求频率限制
- 审计日志记录

## 🐛 故障排除

### 常见问题

**Q: OpenAI API调用失败**
```bash
# 检查API密钥
echo $OPENAI_API_KEY

# 测试网络连接
curl -H "Authorization: Bearer $OPENAI_API_KEY" https://api.openai.com/v1/models
```

**Q: 本地模型响应慢**
```bash
# 检查Ollama状态
ollama list

# 重启Ollama服务
ollama serve
```

**Q: 数据库连接失败**
```bash
# 检查数据库状态
curl http://localhost:8080/api/rebate-ai/recommendations/health
```

### 支持联系

- 📧 **技术支持**: tech-support@company.com
- 📞 **紧急热线**: +86-xxx-xxxx-xxxx
- 💬 **在线文档**: [详细文档链接]

## 📝 更新日志

### v2.0.0 (2024-01-15)
- ✨ 集成LLM智能推荐系统
- ✨ 支持OpenAI GPT和本地模型
- ✨ 新增多场景提示词模板
- ✨ 增强API接口和监控功能
- 🐛 修复缓存机制问题

### v1.0.0 (2023-12-01)
- 🎉 初始版本发布
- ✨ 基础推荐算法实现
- ✨ REST API接口
- ✨ 数据管理功能

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

---

**🚀 立即开始使用AI驱动的智能推荐系统，提升您的金融业务决策效率！**
