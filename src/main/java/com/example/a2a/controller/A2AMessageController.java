package com.example.a2a.controller;

import com.example.a2a.dto.AgentRequest;
import com.example.a2a.dto.AgentResponse;
import com.example.a2a.service.A2AAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * A2A 消息控制器
 * 提供符合 A2A 协议的消息调用端点
 */
@RestController
@RequestMapping("/a2a")
public class A2AMessageController {

    /**
     * Agent 服务
     */
    private final A2AAgentService a2AAgentService;

    /**
     * 构造函数
     * @param a2AAgentService Agent 服务
     */
    @Autowired
    public A2AMessageController(A2AAgentService a2AAgentService) {
        this.a2AAgentService = a2AAgentService;
    }

    /**
     * 处理 A2A 消息调用
     * @param agentName Agent 名称（从请求头获取）
     * @param request 请求参数
     * @return 响应结果
     */
    @PostMapping("/message")
    public ResponseEntity<AgentResponse> handleMessage(
            @RequestHeader(value = "X-Agent-Name", required = false, defaultValue = "data_analysis_agent") String agentName,
            @RequestBody AgentRequest request) {
        
        String message = request.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AgentResponse.failure(agentName, "消息不能为空"));
        }

        String targetAgent = request.getAgentName() != null ? request.getAgentName() : agentName;
        
        Optional<String> result = a2AAgentService.invokeLocalAgent(targetAgent, message);
        
        if (result.isPresent()) {
            return ResponseEntity.ok(AgentResponse.success(targetAgent, result.get(), request.getSessionId()));
        }
        
        return ResponseEntity.ok(AgentResponse.failure(targetAgent, 
                "未找到 Agent: " + targetAgent));
    }

    /**
     * 处理指定 Agent 的消息调用
     * @param agentName Agent 名称
     * @param request 请求参数
     * @return 响应结果
     */
    @PostMapping("/message/{agentName}")
    public ResponseEntity<AgentResponse> handleMessageForAgent(
            @PathVariable String agentName,
            @RequestBody AgentRequest request) {
        
        String message = request.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(AgentResponse.failure(agentName, "消息不能为空"));
        }

        Optional<String> result = a2AAgentService.invokeLocalAgent(agentName, message);
        
        if (result.isPresent()) {
            return ResponseEntity.ok(AgentResponse.success(agentName, result.get(), request.getSessionId()));
        }
        
        return ResponseEntity.ok(AgentResponse.failure(agentName, 
                "未找到 Agent: " + agentName));
    }
}
