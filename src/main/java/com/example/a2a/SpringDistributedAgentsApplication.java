package com.example.a2a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用启动类
 * 分布式智能体（A2A Agent）系统入口
 */
@SpringBootApplication
public class SpringDistributedAgentsApplication {

    /**
     * 应用主入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringDistributedAgentsApplication.class, args);
    }
}
