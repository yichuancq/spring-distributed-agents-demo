package com.example.a2a.controller;

import com.example.a2a.dto.AgentCard;
import com.example.a2a.dto.AgentListResponse;
import com.example.a2a.dto.AgentRequest;
import com.example.a2a.dto.AgentResponse;
import com.example.a2a.service.A2AAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

/**
 * Agent 管理控制器
 * 提供 Agent 的管理和调用接口
 */
@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    /**
     * Agent 服务
     */
    private final A2AAgentService a2AAgentService;

    /**
     * 构造函数
     *
     * @param a2AAgentService Agent 服务
     */
    @Autowired
    public AgentController(A2AAgentService a2AAgentService) {
        this.a2AAgentService = a2AAgentService;
    }

    /**
     * 获取所有 Agent 列表
     *
     * @return Agent 列表响应
     */
    @GetMapping
    public ResponseEntity<AgentListResponse> listAgents() {
        List<AgentCard> agents = a2AAgentService.discoverAgents();

        List<String> localAgents = a2AAgentService.getLocalAgentNames();
        for (String agentName : localAgents) {
            Optional<AgentCard> localCard = a2AAgentService.getAgentCard(agentName);
            if (localCard.isPresent() && agents.stream().noneMatch(a -> a.getName().equals(agentName))) {
                agents.add(localCard.get());
            }
        }

        return ResponseEntity.ok(new AgentListResponse(agents.size(), agents));
    }

    /**
     * 获取指定 Agent 的卡片信息
     *
     * @param agentName Agent 名称
     * @return Agent 卡片
     */
    @GetMapping("/{agentName}")
    public ResponseEntity<AgentCard> getAgentCard(@PathVariable String agentName) {
        Optional<AgentCard> card = a2AAgentService.getAgentCard(agentName);
        return card.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 调用指定 Agent
     *
     * @param agentName Agent 名称
     * @param request   请求参数
     * @return 响应结果
     */
    @PostMapping("/{agentName}/invoke")
    public ResponseEntity<AgentResponse> invokeAgent(@PathVariable String agentName, @RequestBody AgentRequest request) {

        String message = request.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AgentResponse.failure(agentName, "消息不能为空"));
        }

        Optional<String> result = a2AAgentService.invokeLocalAgent(agentName, message);

        if (result.isPresent()) {
            return ResponseEntity.ok(AgentResponse.success(agentName, result.get(), request.getSessionId()));
        }

        result = a2AAgentService.invokeRemoteAgent(agentName, message);

        if (result.isPresent()) {
            return ResponseEntity.ok(AgentResponse.success(agentName, result.get(), request.getSessionId()));
        }

        return ResponseEntity.ok(AgentResponse.failure(agentName, "未找到 Agent: " + agentName));
    }

    /**
     * 流式调用指定 Agent（SSE）
     *
     * @param agentName Agent 名称
     * @param request   请求参数
     * @return 流式响应
     */
    @PostMapping(value = "/{agentName}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamInvokeAgent(@PathVariable String agentName, @RequestBody AgentRequest request) {

        String message = request.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return Flux.just("data: {\"error\": \"消息不能为空\"}\n\n");
        }

        Optional<String> result = a2AAgentService.invokeLocalAgent(agentName, message);

        if (result.isPresent()) {
            return Flux.just("data: {\"agent\": \"" + agentName + "\", \"status\": \"processing\"}\n\n", "data: {\"agent\": \"" + agentName + "\", \"result\": \"" + escapeJson(result.get()) + "\"}\n\n", "data: {\"agent\": \"" + agentName + "\", \"status\": \"completed\"}\n\n");
        }

        return Flux.just("data: {\"error\": \"未找到 Agent: " + agentName + "\"}\n\n");
    }

    /**
     * 获取本地 Agent 列表
     *
     * @return 本地 Agent 列表响应
     */
    @GetMapping("/local")
    public ResponseEntity<AgentListResponse> listLocalAgents() {
        List<String> localAgentNames = a2AAgentService.getLocalAgentNames();
        List<AgentCard> localAgents = localAgentNames.stream().map(name -> a2AAgentService.getAgentCard(name)).filter(Optional::isPresent).map(Optional::get).toList();

        return ResponseEntity.ok(new AgentListResponse(localAgents.size(), localAgents));
    }

    /**
     * 手动注册 Agent
     *
     * @param agentName   Agent 名称
     * @param description Agent 描述
     * @return 响应结果
     */
    @PostMapping("/{agentName}/register")
    public ResponseEntity<AgentResponse> registerAgent(@PathVariable String agentName, @RequestParam(required = false, defaultValue = "") String description) {

        a2AAgentService.registerAgent(agentName, description);
        return ResponseEntity.ok(AgentResponse.success(agentName, "Agent 注册成功: " + agentName));
    }

    /**
     * 转义 JSON 字符串中的特殊字符
     *
     * @param text 原始文本
     * @return 转义后的文本
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
