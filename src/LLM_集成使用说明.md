# AI Rebate Spread 推荐系统 - LLM 集成使用说明

## 系统概述

本系统已成功升级为基于大语言模型（LLM）的智能推荐系统，支持多种LLM服务提供商，包括OpenAI GPT、本地模型（Ollama）以及传统算法回退机制。

## 主要特性

### 🤖 多LLM服务支持
- **OpenAI GPT**: 支持GPT-4等最新模型
- **本地模型**: 支持Ollama等本地部署的开源模型
- **回退机制**: 当LLM服务不可用时自动切换到传统算法

### 🎯 智能推荐能力
- **上下文理解**: 基于客户历史、市场环境、风险等级进行综合分析
- **专业推理**: 运用金融风险管理和量化分析理论
- **动态调整**: 根据市场波动和客户特征提供个性化推荐

### 🔧 配置灵活性
- **多场景提示词**: 标准、保守、优惠、高波动等不同场景模板
- **参数可调**: 温度、最大令牌数、超时时间等可配置
- **缓存机制**: 减少重复调用，提高响应速度

## 配置说明

### 1. 基础配置 (application.yml)

```yaml
ai:
  llm:
    # 服务提供商选择: openai, local, fallback
    provider: fallback
    # 是否启用LLM推荐
    enabled: true
    # 回退策略启用
    fallback-enabled: true
    
    # OpenAI配置
    openai:
      api-key: ${OPENAI_API_KEY:your-openai-api-key}
      model: gpt-4
      base-url: https://api.openai.com/v1
      temperature: 0.3
      max-tokens: 1000
      timeout: 30000
    
    # 本地模型配置
    local:
      base-url: http://localhost:11434
      model: llama3
      temperature: 0.3
      timeout: 30000
```

### 2. 环境变量配置

```bash
# OpenAI API密钥
export OPENAI_API_KEY="your-actual-openai-api-key"

# 或在启动时指定
java -jar app.jar --ai.llm.openai.api-key=your-api-key
```

## 使用方式

### 1. 标准推荐接口

```bash
# POST /api/rebate-ai/recommendations/recommend
curl -X POST http://localhost:8080/api/rebate-ai/recommendations/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "customerCode": "CUST001",
    "currency": "USD",
    "queryDate": "2024-01-15",
    "daysRange": 30
  }'
```

**响应示例**:
```json
{
  "customerCode": "CUST001",
  "customerName": "示例客户",
  "currency": "USD",
  "recommendationDate": "2024-01-15",
  "recommendedSpread": 0.0250,
  "confidenceScore": 0.85,
  "recommendationReason": "基于LLM分析：市场波动率适中，客户信用良好...",
  "usedLLM": true,
  "llmProvider": "OpenAI",
  "llmModel": "GPT-4",
  "riskAssessmentDetail": "客户风险等级适中，建议采用标准定价策略",
  "marketAnalysisDetail": "美元市场流动性充足，波动性在正常范围内",
  "keyFactors": ["客户信用等级", "市场流动性", "历史交易表现"],
  "status": "SUCCESS"
}
```

### 2. LLM服务状态检查

```bash
# GET /api/rebate-ai/recommendations/llm/status
curl http://localhost:8080/api/rebate-ai/recommendations/llm/status
```

**响应示例**:
```json
{
  "llmEnabled": true,
  "availableServices": 2,
  "services": [
    {
      "provider": "Fallback Service",
      "modelName": "Traditional Algorithm",
      "available": true
    },
    {
      "provider": "OpenAI",
      "modelName": "gpt-4",
      "available": false
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

### 3. LLM推荐测试

```bash
# POST /api/rebate-ai/recommendations/llm/test
curl -X POST "http://localhost:8080/api/rebate-ai/recommendations/llm/test?customerCode=TEST001&currency=USD"
```

## 部署方案

### 方案一: 使用OpenAI服务（推荐）

1. **获取API密钥**: 在OpenAI官网申请API密钥
2. **配置密钥**: 设置环境变量或配置文件
3. **测试连接**: 使用测试接口验证服务可用性

```yaml
ai:
  llm:
    provider: openai
    enabled: true
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4
```

### 方案二: 使用本地模型（成本优化）

1. **安装Ollama**: 
```bash
# macOS/Linux
curl -fsSL https://ollama.ai/install.sh | sh

# Windows
# 下载并安装Ollama客户端
```

2. **下载模型**:
```bash
ollama pull llama3
ollama pull mistral
```

3. **启动服务**:
```bash
ollama serve
```

4. **配置应用**:
```yaml
ai:
  llm:
    provider: local
    enabled: true
    local:
      base-url: http://localhost:11434
      model: llama3
```

### 方案三: 回退模式（稳定性优先）

```yaml
ai:
  llm:
    provider: fallback
    enabled: true
    fallback-enabled: true
```

## 性能优化建议

### 1. 缓存策略
- 启用推荐结果缓存，避免重复计算
- 设置合理的缓存过期时间（1-24小时）

### 2. 并发控制
- 设置合理的LLM服务超时时间
- 实现请求队列和限流机制

### 3. 监控告警
- 监控LLM服务响应时间和成功率
- 设置回退机制触发告警

## 故障排除

### 常见问题

1. **OpenAI API调用失败**
   - 检查API密钥是否正确
   - 验证网络连接和防火墙设置
   - 确认API配额是否充足

2. **本地模型响应慢**
   - 检查模型是否已下载
   - 验证Ollama服务是否正常运行
   - 考虑使用更轻量的模型

3. **推荐结果不合理**
   - 检查历史数据质量
   - 调整提示词模板
   - 验证业务约束条件

### 日志分析

```bash
# 查看LLM相关日志
grep "LLM\|OpenAI\|Ollama" application.log

# 查看推荐生成日志
grep "generateRecommendation" application.log
```

## API文档

系统集成了Swagger文档，启动应用后访问：
- **Swagger UI**: http://localhost:8080/api/rebate-ai/swagger-ui.html
- **API文档**: http://localhost:8080/api/rebate-ai/v3/api-docs

## 安全考虑

1. **API密钥保护**: 使用环境变量或密钥管理服务
2. **网络安全**: 配置防火墙规则，限制外部访问
3. **数据隐私**: 确保客户数据在传输过程中的安全性
4. **审计日志**: 记录所有推荐请求和响应

## 升级路径

当前系统支持渐进式升级：
1. **阶段一**: 启用回退模式，确保系统稳定
2. **阶段二**: 配置本地模型，测试LLM功能
3. **阶段三**: 接入OpenAI等云服务，提升推荐质量
4. **阶段四**: 根据业务需求定制化提示词和算法

---

📧 如需技术支持，请联系开发团队。
