package com.example.a2a.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Nacos 配置类
 * 用于配置 Nacos 服务发现和注册中心
 */
@Configuration
public class NacosConfig {

    private static final Logger logger = LoggerFactory.getLogger(NacosConfig.class);

    /**
     * Nacos 服务器地址
     */
    @Value("${spring.ai.alibaba.a2a.registry.nacos.server-addr:localhost:8848}")
    private String serverAddr;

    /**
     * Nacos 命名空间
     */
    @Value("${spring.ai.alibaba.a2a.registry.nacos.namespace:public}")
    private String namespace;

    /**
     * Nacos 用户名
     */
    @Value("${spring.ai.alibaba.a2a.registry.nacos.username:nacos}")
    private String username;

    /**
     * Nacos 密码
     */
    @Value("${spring.ai.alibaba.a2a.registry.nacos.password:nacos}")
    private String password;

    /**
     * 是否启用 Nacos
     */
    @Value("${spring.ai.alibaba.a2a.registry.enabled:true}")
    private boolean registryEnabled;

    /**
     * 创建 Nacos NamingService Bean
     * 用于服务的注册与发现
     * @return NamingService 实例
     * @throws Exception 创建服务时可能抛出的异常
     */
    @Bean
    public NamingService namingService() throws Exception {
        if (!registryEnabled) {
            logger.info("Nacos 注册已禁用，使用本地模式");
            return NacosFactory.createNamingService(new Properties());
        }

        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", serverAddr);
            properties.setProperty("namespace", namespace);
            properties.setProperty("username", username);
            properties.setProperty("password", password);
            
            logger.info("正在连接 Nacos 服务器: {}", serverAddr);
            return NacosFactory.createNamingService(properties);
        } catch (Exception e) {
            logger.warn("连接 Nacos 失败: {}, 将使用本地模式运行", e.getMessage());
            return NacosFactory.createNamingService(new Properties());
        }
    }
}
