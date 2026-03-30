package com.example.a2a.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 记忆响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 命名空间
     */
    private List<String> namespace;

    /**
     * 记忆键
     */
    private String key;

    /**
     * 记忆数据
     */
    private Map<String, Object> data;
}
