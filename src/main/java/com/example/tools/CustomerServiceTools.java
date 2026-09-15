package com.example.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * @author liushug
 * @date 2026/9/15 14:57
 * @description 定义 LLM 调用的工具
 */
@Component
public class CustomerServiceTools {

    @Tool(description = "根据订单号查询订单状态，返回订单金额、状态、下单时间")
    public String queryOrder(@ToolParam(description = "订单号， 格式 ORD+8位数字") String orderNo){
        // 模拟数据， 实际需要查数据库
        if(!orderNo.startsWith("ORD")){
            return "订单号格式错误";
        }
        return String.format("【订单详情】订单号：%s | 金额：200元 | 状态：已下单 | 下单时间：今天", orderNo);
    }

    @Tool(description = "根据订单号查询物流轨迹，返回最新物流节点和时间")
    public String queryLogistics(@ToolParam(description = "订单号") String orderNo){
        return String.format("订单 %s 物流：已到达北京分拣中心，预计明日送达", orderNo);
    }

    @Tool(description = "创建售后工单，用于退款、投诉等场景。写操作，需用户确认后调用")
    public String createTicket(
            @ToolParam(description = "订单号") String orderNo,
            @ToolParam(description = "工单类型：refund-退款, complaint-投诉") String type,
            @ToolParam(description = "问题描述") String description) {
        return String.format("工单已创建：订单 %s，类型 %s，问题：%s", orderNo, type, description);
    }

}
