package com.example.a2a.dto;

/**
 * Agent 请求 DTO
 * 用于封装调用智能体的请求参数
 */
public class AgentRequest {
    /**
     * Agent 名称
     */
    private String agentName;
    /**
     * 用户消息
     */
    private String message;
    /**
     * 会话 ID
     */
    private String sessionId;
    /**
     * 是否使用流式输出
     */
    private boolean stream;

    public AgentRequest() {}

    public AgentRequest(String agentName, String message) {
        this.agentName = agentName;
        this.message = message;
    }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public boolean isStream() { return stream; }
    public void setStream(boolean stream) { this.stream = stream; }
}
