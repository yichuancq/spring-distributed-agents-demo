package com.example.a2a.service;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.a2a.dto.AgentCard;
import com.example.a2a.dto.AgentCard.Provider;
import com.example.a2a.dto.AgentCard.Skill;
import com.example.a2a.dto.MemoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 服务类
 * 提供智能体的调用、注册、发现等核心功能
 */
@Service
public class A2AAgentService {

    private static final Logger logger = LoggerFactory.getLogger(A2AAgentService.class);

    /**
     * 聊天模型
     */
    private final ChatModel chatModel;
    /**
     * Nacos 命名服务
     */
    private final NamingService namingService;
    /**
     * 记忆服务
     */
    private final MemoryService memoryService;

    /**
     * Nacos 分组名称
     */
    @Value("${spring.ai.alibaba.a2a.registry.nacos.group:A2A_AGENTS}")
    private String nacosGroup;

    /**
     * 服务器端口
     */
    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 应用名称
     */
    @Value("${spring.application.name:a2a-agent-server}")
    private String applicationName;

    /**
     * 本地 Agent 映射表
     */
    private final Map<String, AgentInfo> localAgents = new ConcurrentHashMap<>();

    /**
     * 构造函数，初始化本地 Agent
     * @param chatModel 聊天模型
     * @param namingService Nacos 命名服务
     * @param memoryService 记忆服务
     */
    @Autowired
    public A2AAgentService(ChatModel chatModel, NamingService namingService, MemoryService memoryService) {
        this.chatModel = chatModel;
        this.namingService = namingService;
        this.memoryService = memoryService;
        
        // 初始化数据分析 Agent
        localAgents.put("data_analysis_agent", new AgentInfo(
                "data_analysis_agent",
                "专门用于数据分析和统计计算的本地智能体",
                "你是一个专业的数据分析专家，擅长处理各类数据统计和分析任务。\n" +
                "\n" +
                "你的职责包括：\n" +
                "1. 理解用户的数据分析需求\n" +
                "2. 提供准确的统计计算结果\n" +
                "3. 给出专业的分析建议\n" +
                "4. 生成清晰的数据报告\n" +
                "\n" +
                "在回答时，请确保：\n" +
                "- 数据分析逻辑清晰\n" +
                "- 结果准确可靠\n" +
                "- 建议具有实际参考价值"
        ));
        
        // 初始化报告生成 Agent
        localAgents.put("report_generator_agent", new AgentInfo(
                "report_generator_agent",
                "专门用于生成业务报告的智能体",
                "你是一个专业的报告生成专家，擅长将数据和分析结果转化为专业的业务报告。\n" +
                "\n" +
                "你的职责包括：\n" +
                "1. 整理和组织分析结果\n" +
                "2. 生成结构化的报告内容\n" +
                "3. 提供清晰的图表描述\n" +
                "4. 给出可执行的行动建议"
        ));
    }

    /**
     * 调用本地 Agent
     * @param agentName Agent 名称
     * @param message 用户消息
     * @return 响应结果
     */
    public Optional<String> invokeLocalAgent(String agentName, String message) {
        logger.info("调用本地 Agent: {}, 消息: {}", agentName, message);
        
        AgentInfo agentInfo = localAgents.get(agentName);
        if (agentInfo == null) {
            logger.warn("未找到本地 Agent: {}", agentName);
            return Optional.empty();
        }

        try {
            String fullPrompt = agentInfo.instruction() + "\n\n用户问题：" + message;
            String response = chatModel.call(fullPrompt);
            
            if (response != null && !response.isBlank()) {
                return Optional.of(response);
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("调用 Agent 失败: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 调用远程 Agent（预留接口）
     * @param agentName Agent 名称
     * @param message 用户消息
     * @return 响应结果
     */
    public Optional<String> invokeRemoteAgent(String agentName, String message) {
        logger.info("调用远程 Agent: {}, 消息: {}", agentName, message);
        
        try {
            String serviceName = "a2a:" + agentName;
            List<Instance> instances = namingService.getAllInstances(serviceName, nacosGroup);
            
            if (instances.isEmpty()) {
                logger.warn("未找到远程 Agent: {}", agentName);
                return Optional.empty();
            }
            
            Instance instance = instances.get(0);
            String endpoint = "http://" + instance.getIp() + ":" + instance.getPort() + "/a2a/message";
            
            logger.info("远程 Agent 端点: {}", endpoint);
            
            return Optional.empty();
        } catch (Exception e) {
            logger.error("调用远程 Agent 失败: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 注册 Agent 到 Nacos
     * @param agentName Agent 名称
     * @param description Agent 描述
     */
    public void registerAgent(String agentName, String description) {
        try {
            String serviceName = "a2a:" + agentName;
            Instance instance = new Instance();
            instance.setIp(getLocalIp());
            instance.setPort(Integer.parseInt(serverPort));
            instance.setServiceName(serviceName);
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("agent.name", agentName);
            metadata.put("agent.description", description);
            metadata.put("agent.endpoint", "/a2a/message");
            metadata.put("application.name", applicationName);
            instance.setMetadata(metadata);
            
            namingService.registerInstance(serviceName, nacosGroup, instance);
            logger.info("成功注册 Agent 到 Nacos: {}", serviceName);
        } catch (Exception e) {
            logger.error("注册 Agent 到 Nacos 失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从 Nacos 发现所有 Agent
     * @return Agent 卡片列表
     */
    public List<AgentCard> discoverAgents() {
        List<AgentCard> agents = new ArrayList<>();
        
        try {
            List<String> serviceNames = namingService.getServicesOfServer(1, 100).getData();
            
            for (String serviceName : serviceNames) {
                if (serviceName.startsWith("a2a:")) {
                    List<Instance> instances = namingService.getAllInstances(serviceName, nacosGroup);
                    for (Instance instance : instances) {
                        AgentCard card = convertToAgentCard(instance);
                        agents.add(card);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("发现 Agent 失败: {}", e.getMessage(), e);
        }
        
        return agents;
    }

    /**
     * 获取指定 Agent 的卡片信息
     * @param agentName Agent 名称
     * @return Agent 卡片
     */
    public Optional<AgentCard> getAgentCard(String agentName) {
        try {
            String serviceName = "a2a:" + agentName;
            List<Instance> instances = namingService.getAllInstances(serviceName, nacosGroup);
            
            if (!instances.isEmpty()) {
                return Optional.of(convertToAgentCard(instances.get(0)));
            }
        } catch (Exception e) {
            logger.error("获取 AgentCard 失败: {}", e.getMessage(), e);
        }
        
        if (localAgents.containsKey(agentName)) {
            return Optional.of(createLocalAgentCard(agentName));
        }
        
        return Optional.empty();
    }

    /**
     * 将 Nacos 实例转换为 AgentCard
     * @param instance Nacos 实例
     * @return AgentCard
     */
    private AgentCard convertToAgentCard(Instance instance) {
        AgentCard card = new AgentCard();
        Map<String, String> metadata = instance.getMetadata();
        
        card.setName(metadata.getOrDefault("agent.name", "unknown"));
        card.setDescription(metadata.getOrDefault("agent.description", ""));
        card.setEndpoint("http://" + instance.getIp() + ":" + instance.getPort() + 
                        metadata.getOrDefault("agent.endpoint", "/a2a/message"));
        
        Provider provider = new Provider();
        provider.setName(metadata.getOrDefault("application.name", "unknown"));
        provider.setOrganization(metadata.getOrDefault("organization", ""));
        card.setProvider(provider);
        
        return card;
    }

    /**
     * 创建本地 Agent 的卡片信息
     * @param agentName Agent 名称
     * @return AgentCard
     */
    private AgentCard createLocalAgentCard(String agentName) {
        AgentCard card = new AgentCard();
        card.setName(agentName);
        card.setVersion("1.0.0");
        card.setEndpoint("http://localhost:" + serverPort + "/a2a/message");
        
        AgentInfo agentInfo = localAgents.get(agentName);
        if (agentInfo != null) {
            card.setDescription(agentInfo.description());
            
            if ("data_analysis_agent".equals(agentName)) {
                card.setCapabilities(Arrays.asList("text-generation", "data-analysis"));
                card.setSkills(Arrays.asList(new Skill("data_analysis", "数据分析技能")));
            } else if ("report_generator_agent".equals(agentName)) {
                card.setCapabilities(Arrays.asList("text-generation", "report-generation"));
                card.setSkills(Arrays.asList(new Skill("report_generation", "报告生成技能")));
            }
        }
        
        Provider provider = new Provider("Spring AI Alibaba Demo", "Example Organization");
        card.setProvider(provider);
        
        return card;
    }

    /**
     * 获取所有本地 Agent 的名称
     * @return Agent 名称列表
     */
    public List<String> getLocalAgentNames() {
        return new ArrayList<>(localAgents.keySet());
    }

    /**
     * 获取本地 IP 地址
     * @return IP 地址
     */
    private String getLocalIp() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
    
    /**
     * Agent 信息类
     */
    private static class AgentInfo {
        private final String name;
        private final String description;
        private final String instruction;

        public AgentInfo(String name, String description, String instruction) {
            this.name = name;
            this.description = description;
            this.instruction = instruction;
        }

        public String name() {
            return name;
        }

        public String description() {
            return description;
        }

        public String instruction() {
            return instruction;
        }
    }

    /**
     * 调用本地 Agent（带记忆功能）
     * @param agentName Agent 名称
     * @param message 用户消息
     * @param userId 用户 ID（用于长期记忆）
     * @param sessionId 会话 ID（用于短期记忆）
     * @return 响应结果
     */
    public Optional<String> invokeLocalAgentWithMemory(String agentName, String message, 
            String userId, String sessionId) {
        logger.info("调用本地 Agent (带记忆): {}, 用户: {}, 会话: {}, 消息: {}", 
                agentName, userId, sessionId, message);
        
        AgentInfo agentInfo = localAgents.get(agentName);
        if (agentInfo == null) {
            logger.warn("未找到本地 Agent: {}", agentName);
            return Optional.empty();
        }

        try {
            StringBuilder fullPrompt = new StringBuilder(agentInfo.instruction());
            
            String userPreferences = memoryService.getUserPreferencesPrompt(userId);
            if (!userPreferences.isBlank()) {
                fullPrompt.append(userPreferences);
            }
            
            String conversationHistory = memoryService.getConversationHistoryPrompt(sessionId, 10);
            if (!conversationHistory.isBlank()) {
                fullPrompt.append(conversationHistory);
            }
            
            fullPrompt.append("\n\n当前用户问题：").append(message);
            
            String response = chatModel.call(fullPrompt.toString());
            
            if (response != null && !response.isBlank()) {
                memoryService.saveConversationHistory(sessionId, "user", message);
                memoryService.saveConversationHistory(sessionId, "assistant", response);
                
                return Optional.of(response);
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("调用 Agent (带记忆) 失败: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 设置用户偏好
     * @param userId 用户 ID
     * @param preferences 偏好列表
     * @return 是否成功
     */
    public boolean setUserPreferences(String userId, List<String> preferences) {
        logger.info("设置用户偏好: userId={}, preferences={}", userId, preferences);
        var result = memoryService.learnUserPreferences(userId, preferences);
        return result.isSuccess();
    }

    /**
     * 获取用户偏好
     * @param userId 用户 ID
     * @return 偏好列表
     */
    public List<String> getUserPreferences(String userId) {
        Optional<MemoryResponse> memoryOpt = memoryService.getMemory(userId, "preferences", "user_profile");
        if (memoryOpt.isPresent() && memoryOpt.get().getData() != null) {
            Object rules = memoryOpt.get().getData().get("rules");
            if (rules instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> list = (List<String>) rules;
                return list;
            }
        }
        return new ArrayList<>();
    }

    /**
     * 清除会话记忆
     * @param sessionId 会话 ID
     */
    public void clearSessionMemory(String sessionId) {
        memoryService.clearSessionMemory(sessionId);
    }
}
