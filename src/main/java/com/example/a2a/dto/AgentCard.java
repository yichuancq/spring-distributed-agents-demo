package com.example.a2a.dto;

import java.util.List;
import java.util.Map;

/**
 * Agent 卡片信息 DTO
 * 用于描述智能体的基本信息、能力和技能
 */
public class AgentCard {
    /**
     * Agent 名称
     */
    private String name;
    /**
     * Agent 描述
     */
    private String description;
    /**
     * 版本号
     */
    private String version;
    /**
     * 提供者信息
     */
    private Provider provider;
    /**
     * 能力列表
     */
    private List<String> capabilities;
    /**
     * 技能列表
     */
    private List<Skill> skills;
    /**
     * 调用端点地址
     */
    private String endpoint;
    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    public AgentCard() {}

    public AgentCard(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 提供者信息内部类
     */
    public static class Provider {
        /**
         * 提供者名称
         */
        private String name;
        /**
         * 组织名称
         */
        private String organization;

        public Provider() {}

        public Provider(String name, String organization) {
            this.name = name;
            this.organization = organization;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getOrganization() { return organization; }
        public void setOrganization(String organization) { this.organization = organization; }
    }

    /**
     * 技能信息内部类
     */
    public static class Skill {
        /**
         * 技能名称
         */
        private String name;
        /**
         * 技能描述
         */
        private String description;

        public Skill() {}

        public Skill(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }
    public List<String> getCapabilities() { return capabilities; }
    public void setCapabilities(List<String> capabilities) { this.capabilities = capabilities; }
    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) { this.skills = skills; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
