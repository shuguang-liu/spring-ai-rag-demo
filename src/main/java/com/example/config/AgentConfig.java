package com.example.config;

import com.example.tool.ActionTools;
import com.example.tool.LogisticsTools;
import com.example.tool.OrderTools;
import com.example.tool.UserTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liushug
 * @description
 */
@Configuration
public class AgentConfig {

    /**
     * 智能客服Agent
     *
     * ChatMemory 由 Spring AI 自动配置，直接注入即可
     * 默认使用 InMemoryChatMemoryRepository + MessageWindowChatMemory（窗口大小20条）
     *
     * 智能客服Agent
     *
     * 核心配置三要素：
     * 1. System Prompt → Agent的身份和行为规则
     * 2. Tools → Agent的能力边界
     * 3. Memory → Agent的记忆能力
     */
    @Bean
    public ChatClient customerServiceAgent_V2(ChatModel chatModel,
                                           ChatMemory chatMemory,
                                           OrderTools orderTools,
                                           LogisticsTools logisticsTools,
                                           UserTools userTools,
                                           ActionTools actionTools) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        你是"智购商城"的AI客服助手，名叫小智。你的职责是帮助客户解决订单、物流、退款等问题。
                        
                        ## 你的能力
                        1. 查询订单详情和订单列表
                        2. 查询物流轨迹
                        3. 查询用户信息
                        4. 发起退款申请
                        5. 转接人工客服
                        
                        ## 工作流程
                        1. 理解用户意图，判断需要哪些信息
                        2. 如果缺少关键信息（如订单号、用户ID），主动询问用户
                        3. 调用工具获取数据，基于真实数据回答，绝不编造
                        4. 如果用户要求操作（退款、转人工），先确认条件再执行
                        5. 回答要简洁友好，像真人客服一样自然
                        
                        ## 规则
                        - 不要编造订单号、物流单号等信息，必须通过工具查询
                        - 退款前必须确认订单状态是否允许退款
                        - 如果用户情绪激动或问题复杂，主动提出转人工
                        - 每次回答控制在200字以内
                        """)
                .defaultTools(orderTools, logisticsTools, userTools, actionTools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

}
