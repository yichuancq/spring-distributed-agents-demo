package com.example.a2a.controller;

import com.example.a2a.dto.AgentWithMemoryRequest;
import com.example.a2a.dto.AgentResponse;
import com.example.a2a.dto.MemoryRequest;
import com.example.a2a.dto.MemoryResponse;
import com.example.a2a.service.A2AAgentService;
import com.example.a2a.service.MemoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 记忆管理控制器
 * 提供长期记忆、短期记忆、用户偏好管理的 API 接口
 */
@RestController
@RequestMapping("/api/memory")
public class MemoryController {

    private static final Logger logger = LoggerFactory.getLogger(MemoryController.class);

    private final MemoryService memoryService;
    private final A2AAgentService agentService;

    @Autowired
    public MemoryController(MemoryService memoryService, A2AAgentService agentService) {
        this.memoryService = memoryService;
        this.agentService = agentService;
    }

    /**
     * 保存记忆
     * POST /api/memory/save
     */
    @PostMapping("/save")
    public ResponseEntity<MemoryResponse> saveMemory(@Valid @RequestBody MemoryRequest request) {
        logger.info("保存记忆请求: userId={}, context={}, key={}", 
                request.getUserId(), request.getContext(), request.getKey());
        
        MemoryResponse response = memoryService.saveMemory(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取记忆
     * GET /api/memory/get?userId=xxx&context=xxx&key=xxx
     */
    @GetMapping("/get")
    public ResponseEntity<MemoryResponse> getMemory(
            @RequestParam String userId,
            @RequestParam String context,
            @RequestParam String key) {
        logger.info("获取记忆请求: userId={}, context={}, key={}", userId, context, key);
        
        Optional<MemoryResponse> memoryOpt = memoryService.getMemory(userId, context, key);
        if (memoryOpt.isPresent()) {
            return ResponseEntity.ok(memoryOpt.get());
        }
        return ResponseEntity.ok(MemoryResponse.builder()
                .success(false)
                .message("未找到记忆")
                .build());
    }

    /**
     * 搜索记忆
     * POST /api/memory/search
     */
    @PostMapping("/search")
    public ResponseEntity<List<MemoryResponse>> searchMemories(
            @RequestParam String userId,
            @RequestParam String context,
            @RequestBody(required = false) Map<String, String> filters) {
        logger.info("搜索记忆请求: userId={}, context={}, filters={}", userId, context, filters);
        
        if (filters == null) {
            filters = new HashMap<>();
        }
        
        List<MemoryResponse> results = memoryService.searchMemories(userId, context, filters);
        return ResponseEntity.ok(results);
    }

    /**
     * 调用 Agent（带记忆功能）
     * POST /api/memory/agent/invoke
     */
    @PostMapping("/agent/invoke")
    public ResponseEntity<AgentResponse> invokeAgentWithMemory(
            @Valid @RequestBody AgentWithMemoryRequest request) {
        logger.info("调用 Agent (带记忆): agentName={}, userId={}, sessionId={}", 
                request.getAgentName(), request.getUserId(), request.getSessionId());
        
        Optional<String> responseOpt = agentService.invokeLocalAgentWithMemory(
                request.getAgentName(),
                request.getMessage(),
                request.getUserId(),
                request.getSessionId()
        );
        
        if (responseOpt.isPresent()) {
            return ResponseEntity.ok(AgentResponse.builder()
                    .success(true)
                    .message("调用成功")
                    .content(responseOpt.get())
                    .agentName(request.getAgentName())
                    .build());
        }
        
        return ResponseEntity.ok(AgentResponse.builder()
                .success(false)
                .message("调用失败或 Agent 不存在")
                .build());
    }

    /**
     * 设置用户偏好
     * POST /api/memory/preferences/{userId}
     */
    @PostMapping("/preferences/{userId}")
    public ResponseEntity<Map<String, Object>> setUserPreferences(
            @PathVariable String userId,
            @RequestBody List<String> preferences) {
        logger.info("设置用户偏好: userId={}, preferences={}", userId, preferences);
        
        boolean success = agentService.setUserPreferences(userId, preferences);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "设置成功" : "设置失败");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户偏好
     * GET /api/memory/preferences/{userId}
     */
    @GetMapping("/preferences/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPreferences(@PathVariable String userId) {
        logger.info("获取用户偏好: userId={}", userId);
        
        List<String> preferences = agentService.getUserPreferences(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("userId", userId);
        result.put("preferences", preferences);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 清除会话记忆
     * DELETE /api/memory/session/{sessionId}
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> clearSessionMemory(@PathVariable String sessionId) {
        logger.info("清除会话记忆: sessionId={}", sessionId);
        
        agentService.clearSessionMemory(sessionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "会话记忆已清除");
        result.put("sessionId", sessionId);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取对话历史
     * GET /api/memory/session/{sessionId}/history?maxMessages=10
     */
    @GetMapping("/session/{sessionId}/history")
    public ResponseEntity<Map<String, Object>> getConversationHistory(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "10") int maxMessages) {
        logger.info("获取对话历史: sessionId={}, maxMessages={}", sessionId, maxMessages);
        
        List<Map<String, String>> history = memoryService.getConversationHistory(sessionId, maxMessages);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("sessionId", sessionId);
        result.put("history", history);
        result.put("count", history.size());
        
        return ResponseEntity.ok(result);
    }
}
