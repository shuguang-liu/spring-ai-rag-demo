package com.example.config;

/**
 * @author liushug
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
