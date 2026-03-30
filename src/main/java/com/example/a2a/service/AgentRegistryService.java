package com.example.a2a.service;

import com.alibaba.nacos.api.naming.NamingService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Agent 注册服务类
 * 负责应用启动时自动注册本地 Agent 到 Nacos
 */
@Service
public class AgentRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(AgentRegistryService.class);

    /**
     * Agent 服务
     */
    private final A2AAgentService a2AAgentService;
    /**
     * Nacos 命名服务
     */
    private final NamingService namingService;

    /**
     * 是否启用注册
     */
    @Value("${spring.ai.alibaba.a2a.registry.enabled:true}")
    private boolean registryEnabled;

    /**
     * 构造函数
     * @param a2AAgentService Agent 服务
     * @param namingService Nacos 命名服务
     */
    @Autowired
    public AgentRegistryService(A2AAgentService a2AAgentService, NamingService namingService) {
        this.a2AAgentService = a2AAgentService;
        this.namingService = namingService;
    }

    /**
     * 应用启动后自动注册 Agent
     */
    @PostConstruct
    public void init() {
        if (!registryEnabled) {
            logger.info("Agent 注册已禁用");
            return;
        }

        logger.info("开始注册本地 Agent 到 Nacos...");
        
        a2AAgentService.registerAgent("data_analysis_agent", 
                "专门用于数据分析和统计计算的智能体");
        
        a2AAgentService.registerAgent("report_generator_agent", 
                "专门用于生成业务报告的智能体");
        
        logger.info("Agent 注册完成");
    }
}
