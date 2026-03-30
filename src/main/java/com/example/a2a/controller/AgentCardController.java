package com.example.a2a.controller;

import com.example.a2a.dto.AgentCard;
import com.example.a2a.dto.AgentCard.Provider;
import com.example.a2a.dto.AgentCard.Skill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Agent 卡片控制器
 * 提供 A2A 标准的 AgentCard 元数据端点
 */
@RestController
public class AgentCardController {

    /**
     * Agent 名称
     */
    @Value("${spring.ai.alibaba.a2a.server.card.name:data_analysis_agent}")
    private String agentName;

    /**
     * Agent 描述
     */
    @Value("${spring.ai.alibaba.a2a.server.card.description:专门用于数据分析和统计计算的智能体}")
    private String agentDescription;

    /**
     * 版本号
     */
    @Value("${spring.ai.alibaba.a2a.server.version:1.0.0}")
    private String version;

    /**
     * 提供者名称
     */
    @Value("${spring.ai.alibaba.a2a.server.card.provider.name:Spring AI Alibaba Demo}")
    private String providerName;

    /**
     * 组织名称
     */
    @Value("${spring.ai.alibaba.a2a.server.card.provider.organization:Example Organization}")
    private String providerOrganization;

    /**
     * 服务器端口
     */
    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 获取 AgentCard 元数据
     * 符合 A2A 协议的标准端点
     * @return AgentCard 信息
     */
    @GetMapping("/.well-known/agent.json")
    public AgentCard getAgentCard() {
        AgentCard card = new AgentCard();
        card.setName(agentName);
        card.setDescription(agentDescription);
        card.setVersion(version);
        card.setEndpoint("http://localhost:" + serverPort + "/a2a/message");
        
        Provider provider = new Provider(providerName, providerOrganization);
        card.setProvider(provider);
        
        card.setCapabilities(Arrays.asList("text-generation", "data-analysis"));
        
        card.setSkills(Arrays.asList(
                new Skill("data_analysis", "数据分析技能"),
                new Skill("statistical_calculation", "统计计算技能")
        ));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("framework", "Spring AI Alibaba");
        metadata.put("protocol", "A2A");
        metadata.put("supported_formats", Arrays.asList("text", "json"));
        card.setMetadata(metadata);
        
        return card;
    }
}
