package com.example.a2a.dto;

import java.util.List;

/**
 * Agent 列表响应 DTO
 * 用于返回多个智能体的信息
 */
public class AgentListResponse {
    /**
     * Agent 数量
     */
    private int count;
    /**
     * Agent 卡片列表
     */
    private List<AgentCard> agents;

    public AgentListResponse() {}

    public AgentListResponse(int count, List<AgentCard> agents) {
        this.count = count;
        this.agents = agents;
    }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public List<AgentCard> getAgents() { return agents; }
    public void setAgents(List<AgentCard> agents) { this.agents = agents; }
}
