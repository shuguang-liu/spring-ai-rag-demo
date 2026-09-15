package com.example.config;

/**
 * @author liushug
 * @date 2026/9/15 1:22
 * @description SSE 状态枚举类
 */
public enum SseState {
    RETRIEVING,
    RERANKING,
    GENERATING,
    TOOL_CALLING,
    DONE,
    ERROR,
}
