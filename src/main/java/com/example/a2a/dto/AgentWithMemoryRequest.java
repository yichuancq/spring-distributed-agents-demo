package com.example.a2a.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 带记忆的 Agent 调用请求
 */
@Data
public class AgentWithMemoryRequest {

    /**
     * Agent 名称
     */
    @NotBlank(message = "Agent 名称不能为空")
    private String agentName;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息不能为空")
    private String message;

    /**
     * 用户 ID（用于记忆关联）
     */
    @NotBlank(message = "用户 ID 不能为空")
    private String userId;

    /**
     * 会话 ID（用于短期记忆）
     */
    private String sessionId;
}
