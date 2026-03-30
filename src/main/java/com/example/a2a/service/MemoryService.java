package com.example.a2a.service;

import com.example.a2a.dto.MemoryRequest;
import com.example.a2a.dto.MemoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记忆管理服务
 * 提供长期记忆（用户偏好）和短期记忆（对话历史）的管理功能
 * 使用内存存储实现，生产环境建议使用数据库
 */
@Service
public class MemoryService {

    private static final Logger logger = LoggerFactory.getLogger(MemoryService.class);

    /**
     * 长期记忆存储（内存存储）
     * Key: namespace.toString() -> Key: memoryKey -> Value: memoryData
     */
    private final Map<String, Map<String, Map<String, Object>>> longTermMemory = new ConcurrentHashMap<>();

    /**
     * 短期对话历史存储（内存存储）
     */
    private final Map<String, List<Map<String, String>>> sessionHistory = new ConcurrentHashMap<>();

    /**
     * 保存记忆
     * @param request 记忆保存请求
     * @return 保存结果
     */
    public MemoryResponse saveMemory(MemoryRequest request) {
        try {
            List<String> namespace = Arrays.asList(request.getUserId(), request.getContext());
            String namespaceKey = String.join(":", namespace);

            Map<String, Object> memoryData = new HashMap<>();
            if (request.getData() != null) {
                memoryData.putAll(request.getData());
            }
            if (request.getRules() != null && !request.getRules().isEmpty()) {
                memoryData.put("rules", request.getRules());
            }

            longTermMemory.computeIfAbsent(namespaceKey, k -> new ConcurrentHashMap<>());
            longTermMemory.get(namespaceKey).put(request.getKey(), memoryData);

            logger.info("成功保存记忆: userId={}, context={}, key={}", 
                    request.getUserId(), request.getContext(), request.getKey());

            return MemoryResponse.builder()
                    .success(true)
                    .message("记忆保存成功")
                    .namespace(namespace)
                    .key(request.getKey())
                    .data(memoryData)
                    .build();
        } catch (Exception e) {
            logger.error("保存记忆失败: {}", e.getMessage(), e);
            return MemoryResponse.builder()
                    .success(false)
                    .message("保存失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取记忆
     * @param userId 用户 ID
     * @param context 应用上下文
     * @param key 记忆键
     * @return 记忆内容
     */
    public Optional<MemoryResponse> getMemory(String userId, String context, String key) {
        try {
            List<String> namespace = Arrays.asList(userId, context);
            String namespaceKey = String.join(":", namespace);

            Map<String, Map<String, Object>> namespaceMemories = longTermMemory.get(namespaceKey);
            if (namespaceMemories != null && namespaceMemories.containsKey(key)) {
                Map<String, Object> data = namespaceMemories.get(key);
                logger.info("获取记忆: userId={}, context={}, key={}", userId, context, key);

                return Optional.of(MemoryResponse.builder()
                        .success(true)
                        .message("获取成功")
                        .namespace(namespace)
                        .key(key)
                        .data(data)
                        .build());
            }

            return Optional.empty();
        } catch (Exception e) {
            logger.error("获取记忆失败: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 搜索记忆
     * @param userId 用户 ID
     * @param context 应用上下文
     * @param filters 过滤条件
     * @return 匹配的记忆列表
     */
    public List<MemoryResponse> searchMemories(String userId, String context, Map<String, String> filters) {
        List<MemoryResponse> results = new ArrayList<>();
        try {
            List<String> namespace = Arrays.asList(userId, context);
            String namespaceKey = String.join(":", namespace);

            Map<String, Map<String, Object>> namespaceMemories = longTermMemory.get(namespaceKey);
            if (namespaceMemories != null) {
                for (Map.Entry<String, Map<String, Object>> entry : namespaceMemories.entrySet()) {
                    boolean match = true;
                    if (filters != null && !filters.isEmpty()) {
                        for (Map.Entry<String, String> filter : filters.entrySet()) {
                            Object value = entry.getValue().get(filter.getKey());
                            if (value == null || !filter.getValue().equals(value.toString())) {
                                match = false;
                                break;
                            }
                        }
                    }
                    if (match) {
                        results.add(MemoryResponse.builder()
                                .success(true)
                                .namespace(namespace)
                                .key(entry.getKey())
                                .data(entry.getValue())
                                .build());
                    }
                }
            }

            logger.info("搜索记忆: userId={}, context={}, filters={}, 找到 {} 条结果", 
                    userId, context, filters, results.size());
        } catch (Exception e) {
            logger.error("搜索记忆失败: {}", e.getMessage(), e);
        }
        return results;
    }

    /**
     * 获取用户偏好（从长期记忆中）
     * @param userId 用户 ID
     * @return 用户偏好字符串，用于注入到 Agent 提示词中
     */
    public String getUserPreferencesPrompt(String userId) {
        StringBuilder sb = new StringBuilder();

        try {
            List<String> contexts = Arrays.asList("chitchat", "preferences", "general");
            
            for (String context : contexts) {
                Optional<MemoryResponse> memoryOpt = getMemory(userId, context, "user_profile");
                if (memoryOpt.isPresent()) {
                    MemoryResponse memory = memoryOpt.get();
                    if (memory.getData() != null && memory.getData().containsKey("rules")) {
                        @SuppressWarnings("unchecked")
                        List<String> rules = (List<String>) memory.getData().get("rules");
                        if (rules != null && !rules.isEmpty()) {
                            sb.append("\n用户偏好设置：\n");
                            for (String rule : rules) {
                                sb.append("- ").append(rule).append("\n");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("获取用户偏好失败: {}", e.getMessage());
        }

        return sb.toString();
    }

    /**
     * 保存对话历史（短期记忆）
     * @param sessionId 会话 ID
     * @param role 角色（user/assistant）
     * @param content 内容
     */
    public void saveConversationHistory(String sessionId, String role, String content) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        sessionHistory.computeIfAbsent(sessionId, k -> Collections.synchronizedList(new ArrayList<>()));

        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        message.put("timestamp", String.valueOf(System.currentTimeMillis()));

        sessionHistory.get(sessionId).add(message);

        logger.debug("保存对话历史: sessionId={}, role={}", sessionId, role);
    }

    /**
     * 获取对话历史（短期记忆）
     * @param sessionId 会话 ID
     * @param maxMessages 最大消息数
     * @return 对话历史列表
     */
    public List<Map<String, String>> getConversationHistory(String sessionId, int maxMessages) {
        if (sessionId == null || sessionId.isBlank()) {
            return new ArrayList<>();
        }

        List<Map<String, String>> history = sessionHistory.getOrDefault(sessionId, new ArrayList<>());
        
        if (history.size() > maxMessages) {
            return history.subList(history.size() - maxMessages, history.size());
        }
        return new ArrayList<>(history);
    }

    /**
     * 获取对话历史字符串，用于注入到 Agent 提示词中
     * @param sessionId 会话 ID
     * @param maxMessages 最大消息数
     * @return 对话历史字符串
     */
    public String getConversationHistoryPrompt(String sessionId, int maxMessages) {
        List<Map<String, String>> history = getConversationHistory(sessionId, maxMessages);
        if (history.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("\n对话历史：\n");
        for (Map<String, String> msg : history) {
            String role = "user".equals(msg.get("role")) ? "用户" : "助手";
            sb.append(role).append(": ").append(msg.get("content")).append("\n");
        }
        return sb.toString();
    }

    /**
     * 清除会话记忆
     * @param sessionId 会话 ID
     */
    public void clearSessionMemory(String sessionId) {
        sessionHistory.remove(sessionId);
        logger.info("清除会话记忆: sessionId={}", sessionId);
    }

    /**
     * 学习用户偏好（自动分析对话并保存）
     * @param userId 用户 ID
     * @param preferences 提取的偏好列表
     */
    public MemoryResponse learnUserPreferences(String userId, List<String> preferences) {
        MemoryRequest request = new MemoryRequest();
        request.setUserId(userId);
        request.setContext("preferences");
        request.setKey("user_profile");
        request.setRules(preferences);

        return saveMemory(request);
    }
}
