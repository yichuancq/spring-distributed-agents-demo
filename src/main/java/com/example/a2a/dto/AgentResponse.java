package com.example.a2a.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent 响应 DTO
 * 用于封装智能体调用的响应结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * Agent 名称
     */
    private String agentName;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应内容
     */
    private String content;

    /**
     * 执行结果（兼容旧字段）
     */
    private String result;

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 响应时间戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 错误信息
     */
    private String error;

    /**
     * 创建成功响应
     * @param agentName Agent 名称
     * @param result 执行结果
     * @return AgentResponse 实例
     */
    public static AgentResponse success(String agentName, String result) {
        return AgentResponse.builder()
                .success(true)
                .agentName(agentName)
                .result(result)
                .content(result)
                .message("调用成功")
                .build();
    }

    /**
     * 创建成功响应（带会话 ID）
     * @param agentName Agent 名称
     * @param result 执行结果
     * @param sessionId 会话 ID
     * @return AgentResponse 实例
     */
    public static AgentResponse success(String agentName, String result, String sessionId) {
        return AgentResponse.builder()
                .success(true)
                .agentName(agentName)
                .result(result)
                .content(result)
                .sessionId(sessionId)
                .message("调用成功")
                .build();
    }

    /**
     * 创建失败响应
     * @param agentName Agent 名称
     * @param error 错误信息
     * @return AgentResponse 实例
     */
    public static AgentResponse failure(String agentName, String error) {
        return AgentResponse.builder()
                .success(false)
                .agentName(agentName)
                .error(error)
                .message(error)
                .build();
    }
}
