package com.example.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Configuration;

/**
 * @author liushug
 * @date 2026/9/15 15:35
 * @description MCP配置类
 */
@Configuration
public class McpClientConfig {
    public ChatClient chatClient(ChatModel chatModel, ToolCallbackProvider mcpTools){
        return ChatClient.builder(chatModel).defaultSystem("你是一个智能客服助手，可以调用工具查询订单和物流信息。")
                .defaultTools(mcpTools.getToolCallbacks()).build();
    }
}
