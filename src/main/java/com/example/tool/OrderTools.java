package com.example.tool;

import com.example.agent.data.MockDataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author liushug
 * @date 2026/8/17 10:55
 * @description 订单查询工具 Agent 的眼睛，用来查看订单信息
 */
@Component
@RequiredArgsConstructor
public class OrderTools {

    private final MockDataStore dataStore;

    @Tool(description = "根据订单号查询订单详情，返回商品名称、价格、订单状态、创建时间、物流单号等信息")
    public String queryOrder(@ToolParam(description = "订单号，格式如ORD001") String orderId){
        MockDataStore.OrderInfo order = dataStore.getOrder(orderId);
        if(order == null){
            return "未找到订单：" + orderId + "，请确认订单号是否正确";
        }
        return String.format("【订单详情】订单号：%s | 商品：%s | 价格：%s元 | 状态：%s | 下单时间：%s | 物流单号：%s",
                order.orderId(), order.productName(), order.price(),
                order.status(), order.createTime(),
                order.trackingNo() != null ? order.trackingNo() : "暂无（未发货）");

    }

    @Tool(description = "根据用户ID查询该用户的所有订单列表，返回每个订单的订单号、商品、价格、状态")
    public String queryUserOrders(
            @ToolParam(description = "用户ID，如1001") String userId) {
        List<MockDataStore.OrderInfo> orders = dataStore.getOrdersByUserId(userId);
        if (orders.isEmpty()) {
            return "用户" + userId + "暂无订单记录";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("用户").append(userId).append("共有").append(orders.size()).append("个订单：\n");
        for (MockDataStore.OrderInfo order : orders) {
            sb.append(String.format("  - %s | %s | %s元 | %s | %s\n",
                    order.orderId(), order.productName(), order.price(),
                    order.status(), order.createTime()));
        }
        return sb.toString();
    }


}
