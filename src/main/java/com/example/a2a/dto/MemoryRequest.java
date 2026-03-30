package com.example.a2a.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 记忆保存请求
 */
@Data
public class MemoryRequest {

    /**
     * 用户 ID（命名空间的一部分）
     */
    @NotBlank(message = "用户 ID 不能为空")
    private String userId;

    /**
     * 应用上下文（命名空间的一部分）
     */
    @NotBlank(message = "应用上下文不能为空")
    private String context;

    /**
     * 记忆键
     */
    @NotBlank(message = "记忆键不能为空")
    private String key;

    /**
     * 记忆数据（用户偏好等）
     */
    private Map<String, Object> data;

    /**
     * 规则列表
     */
    private List<String> rules;
}
