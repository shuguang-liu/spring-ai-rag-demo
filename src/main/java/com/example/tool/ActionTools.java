package com.example.tool;

import com.example.agent.data.MockDataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * @author liushug
 * @date 2026/8/17 11:18
 * @description Agent 的手，用来执行退款、转人工的等操作
 */
@Component
@RequiredArgsConstructor
public class ActionTools {
    private final MockDataStore dataStore;

    @Tool(description = "为用户发起退款申请。调用前必须先确认：1.订单存在 2.订单状态允许退款（已发货或已签收可退，待付款不需要退款，已退款不能重复退）")
    public String applyRefund(
            @ToolParam(description = "订单号") String orderId,
            @ToolParam(description = "退款原因") String reason) {
        // 校验订单状态
        MockDataStore.OrderInfo order = dataStore.getOrder(orderId);
        if (order == null) {
            return "退款失败：未找到订单" + orderId;
        }
        if ("待付款".equals(order.status())) {
            return "退款失败：订单" + orderId + "状态为待付款，无需退款，建议取消订单";
        }
        if ("已退款".equals(order.status())) {
            return "退款失败：订单" + orderId + "已经退款，不能重复申请";
        }

        // 执行退款
        MockDataStore.RefundRecord record = dataStore.addRefund(orderId, reason);
        return String.format(
                "【退款成功】退款单号：%s | 订单：%s（%s）| 退款金额：%s元 | 原因：%s | 预计3个工作日到账",
                record.refundId(), orderId, order.productName(),
                record.amount(), reason);
    }

    @Tool(description = "将用户转接人工客服。当用户明确要求转人工，或问题超出AI处理能力时使用")
    public String transferToHuman(
            @ToolParam(description = "用户ID") String userId,
            @ToolParam(description = "转接原因摘要") String reason) {
        int queueNum = (int) (Math.random() * 5) + 1;
        return String.format(
                "【转接人工】已为您转接人工客服 | 用户：%s | 当前排队：%d人 | 原因：%s | 请等待客服接入，不要关闭对话",
                userId, queueNum, reason);
    }

}
