# 分布式智能体系统（Spring AI Alibaba）

基于 Spring AI Alibaba 的 A2A（Agent to Agent）分布式智能体系统，支持记忆管理、用户偏好学习和多智能体协作。

## 🎯 功能特性

- **分布式智能体**：基于 A2A 协议的智能体注册与发现
- **记忆管理**：短期记忆（对话历史）和长期记忆（用户偏好）
- **前端界面**：现代化 Web 界面，支持对话、历史记录和偏好管理
- **RESTful API**：完整的 REST 接口，支持智能体调用和记忆操作
- **多模式运行**：支持本地模式和分布式模式

## 🛠 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.3.6 | 应用框架 |
| Spring AI Alibaba | 1.0.0-M6.1 | A2A 协议实现 |
| Nacos | 2.3.2 | 服务注册与发现 |
| Alibaba DashScope | - | 大模型 API |
| Maven | 3.6+ | 项目构建 |
| HTML5 + CSS3 + JavaScript | - | 前端界面 |

## 🚀 快速开始

### 1. 环境要求

- **JDK 17+**
- **Maven 3.6+**
- **阿里云 DashScope API Key**（获取方式：[阿里云 DashScope 控制台](https://dashscope.console.aliyun.com/)）

### 2. 配置 API Key

**方式一：环境变量**
```bash
export DASHSCOPE_API_KEY=sk-xxxxxxxxxxxxxxxx
```

**方式二：修改配置文件**
```yaml
# src/main/resources/application.yml
spring:
  ai:
    dashscope:
      api-key: sk-xxxxxxxxxxxxxxxx
```

### 3. 启动应用

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 或指定 API Key
mvn spring-boot:run -DDASHSCOPE_API_KEY=sk-xxxxxxxxxxxxxxxx
```

### 4. 访问前端界面

```
浏览器打开: http://localhost:8080
```

## ⚙️ 配置说明

### 核心配置（application.yml）

```yaml
spring:
  application:
    name: a2a-agent-server
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY:your-api-key-here}
      chat:
        options:
          model: qwen-plus
    alibaba:
      a2a:
        server:
          enabled: true
          version: 1.0.0
          card:
            name: data_analysis_agent
            description: 专门用于数据分析和统计计算的智能体
        registry:
          enabled: false  # 本地模式：false，分布式模式：true
          nacos:
            server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
            namespace: ${NACOS_NAMESPACE:public}
            group: A2A_AGENTS
            username: ${NACOS_USERNAME:nacos}
            password: ${NACOS_PASSWORD:nacos}
```

## 📡 API 接口说明

### 智能体管理

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 获取 Agent 列表 | GET | `/api/v1/agents` | 获取所有可用的智能体 |
| 获取本地 Agent | GET | `/api/v1/agents/local` | 获取本地智能体列表 |
| 获取 Agent 详情 | GET | `/api/v1/agents/{agentName}` | 获取指定智能体的卡片信息 |
| 调用智能体 | POST | `/api/v1/agents/{agentName}/invoke` | 调用指定智能体 |
| 流式调用 | POST | `/api/v1/agents/{agentName}/stream` | 流式调用智能体（SSE） |
| 注册智能体 | POST | `/api/v1/agents/{agentName}/register` | 手动注册智能体 |

### 记忆管理

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 保存记忆 | POST | `/api/memory/save` | 保存记忆数据 |
| 获取记忆 | GET | `/api/memory/get` | 获取指定记忆 |
| 搜索记忆 | POST | `/api/memory/search` | 搜索记忆 |
| 调用智能体（带记忆） | POST | `/api/memory/agent/invoke` | 带记忆功能的智能体调用 |
| 设置用户偏好 | POST | `/api/memory/preferences/{userId}` | 设置用户长期偏好 |
| 获取用户偏好 | GET | `/api/memory/preferences/{userId}` | 获取用户偏好 |
| 清除会话记忆 | DELETE | `/api/memory/session/{sessionId}` | 清除会话历史 |
| 获取对话历史 | GET | `/api/memory/session/{sessionId}/history` | 获取会话历史记录 |

### 调用示例

**1. 带记忆调用智能体**
```bash
curl -X POST http://localhost:8080/api/memory/agent/invoke \
  -H "Content-Type: application/json" \
  -d '{
    "agentName": "data_analysis_agent",
    "message": "帮我分析一下销售数据",
    "userId": "user001",
    "sessionId": "session_test_001"
  }'
```

**2. 设置用户偏好**
```bash
curl -X POST http://localhost:8080/api/memory/preferences/user001 \
  -H "Content-Type: application/json" \
  -d '["用户喜欢简短直接的回答", "用户只说中文", "用户关注数据分析"]'
```

**3. 获取对话历史**
```bash
curl -X GET "http://localhost:8080/api/memory/session/session_test_001/history?maxMessages=10"
```

## 🎨 前端界面

### 功能模块

1. **对话**：带记忆功能的智能体对话界面
2. **用户偏好**：设置和管理用户长期偏好
3. **对话历史**：查看和管理会话历史
4. **Agent 列表**：查看所有可用的智能体

### 界面预览

```
┌─────────────────────────────────────────────────────┐
│  🤖 分布式智能体系统                                │
│  基于 Spring AI Alibaba 的 A2A Agent 平台          │
├─────────────────────────────────────────────────────┤
│ [对话] [用户偏好] [对话历史] [Agent 列表]           │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌───────────────────────────────────────────────┐ │
│  │  Agent: [data_analysis_agent ▼]               │ │
│  │  用户: [user001]  会话: [session_default]     │ │
│  │  [清除会话]                                     │ │
│  └───────────────────────────────────────────────┘ │
│                                                     │
│  ┌───────────────────────────────────────────────┐ │
│  │ 💬 欢迎使用智能体系统！                       │ │
│  │ 👤 你的问题                                    │ │
│  │ 🤖 Agent 的回答                                │ │
│  └───────────────────────────────────────────────┘ │
│                                                     │
│  [输入消息...]                          [发送]      │
│                                                     │
└─────────────────────────────────────────────────────┘
```

## 🔄 运行模式

| 模式 | Nacos | 功能 | 适用场景 |
|------|-------|------|----------|
| **本地模式** | ❌ 不需要 | 仅使用本地 Agent | 开发调试、单机部署 |
| **分布式模式** | ✅ 需要 | Agent 注册与发现、跨服务调用 | 生产环境、多 Agent 协作 |

### 启用分布式模式

1. **启动 Nacos 服务器**
   ```bash
   # 下载 Nacos
   # https://github.com/alibaba/nacos/releases
   
   # Windows 启动（单机模式）
   startup.cmd -m standalone
   ```

2. **修改配置**
   ```yaml
   # src/main/resources/application.yml
   spring:
     ai:
       alibaba:
         a2a:
           registry:
             enabled: true  # 启用 Nacos
   ```

3. **重启应用**

## ⚠️ 注意事项

1. **API Key 安全**：不要将 API Key 硬编码到代码中，使用环境变量或配置文件管理
2. **Nacos 依赖**：分布式模式需要 Nacos 服务器，本地模式可以禁用
3. **内存管理**：会话记忆默认存储在内存中，重启后会丢失。生产环境建议使用持久化存储
4. **大模型调用**：调用大模型会产生 API 费用，请合理使用
5. **Java 版本**：项目要求 Java 17+，请确保环境配置正确

## 📁 项目结构

```
spring-distributed-agents-demo/
├── src/
│   ├── main/
│   │   ├── java/com/example/a2a/
│   │   │   ├── config/       # 配置类
│   │   │   ├── controller/   # 控制器
│   │   │   ├── dto/          # 数据传输对象
│   │   │   ├── service/      # 服务层
│   │   │   └── SpringDistributedAgentsApplication.java  # 应用入口
│   │   └── resources/
│   │       ├── application.yml  # 配置文件
│   │       └── static/      # 静态资源
│   │           ├── index.html    # 前端首页
│   │           ├── css/     # 样式文件
│   │           └── js/      # JavaScript 文件
│   └── test/                # 测试代码
├── pom.xml                  # Maven 配置
└── README.md                # 项目说明
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 MIT 许可证。
