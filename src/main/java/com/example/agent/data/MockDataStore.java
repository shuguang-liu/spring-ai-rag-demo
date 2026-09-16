package com.example.agent.data;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liushug
 * @description 模拟业务数据，实际这里连接真实数据库
 */
@Component
public class MockDataStore {
    // ===== 订单数据 =====
    private final Map<String, OrderInfo> orders = new LinkedHashMap<>();
    // ===== 用户数据 =====
    private final Map<String, UserInfo> users = new LinkedHashMap<>();
    // ===== 物流数据 =====
    private final Map<String, List<LogisticsNode>> logistics = new LinkedHashMap<>();
    // ===== 退款记录 =====
    private final Map<String, RefundRecord> refunds = new LinkedHashMap<>();

    public MockDataStore() {
        // 初始化用户
        users.put("1001", new UserInfo("1001", "张三", "138****1234", "VIP"));
        users.put("1002", new UserInfo("1002", "李四", "139****5678", "普通"));
        users.put("1003", new UserInfo("1003", "王五", "137****9012", "SVIP"));

        // 初始化订单
        orders.put("ORD001", new OrderInfo("ORD001", "1001", "iPhone 16 Pro",
                "已发货", new BigDecimal("7999"),
                LocalDateTime.now().minusDays(3), "SF100001"));
        orders.put("ORD002", new OrderInfo("ORD002", "1001", "AirPods Pro 3",
                "已签收", new BigDecimal("1899"),
                LocalDateTime.now().minusDays(10), "SF100002"));
        orders.put("ORD003", new OrderInfo("ORD003", "1002", "MacBook Air M4",
                "待付款", new BigDecimal("9999"),
                LocalDateTime.now().minusHours(2), null));
        orders.put("ORD004", new OrderInfo("ORD004", "1001", "iPad Air",
                "已退款", new BigDecimal("4799"),
                LocalDateTime.now().minusDays(15), "SF100004"));
        orders.put("ORD005", new OrderInfo("ORD005", "1003", "Apple Watch Ultra 3",
                "已发货", new BigDecimal("6499"),
                LocalDateTime.now().minusDays(1), "SF100005"));

        // 初始化物流
        logistics.put("SF100001", List.of(
                new LogisticsNode("已揽收", "深圳转运中心", LocalDateTime.now().minusDays(3)),
                new LogisticsNode("运输中", "武汉转运中心", LocalDateTime.now().minusDays(2)),
                new LogisticsNode("派送中", "乌鲁木齐营业部", LocalDateTime.now().minusDays(1))
        ));
        logistics.put("SF100002", List.of(
                new LogisticsNode("已揽收", "深圳转运中心", LocalDateTime.now().minusDays(10)),
                new LogisticsNode("运输中", "西安转运中心", LocalDateTime.now().minusDays(8)),
                new LogisticsNode("派送中", "乌鲁木齐营业部", LocalDateTime.now().minusDays(7)),
                new LogisticsNode("已签收", "乌鲁木齐营业部", LocalDateTime.now().minusDays(6))
        ));
        logistics.put("SF100004", List.of(
                new LogisticsNode("已揽收", "上海转运中心", LocalDateTime.now().minusDays(15)),
                new LogisticsNode("已签收", "乌鲁木齐营业部", LocalDateTime.now().minusDays(12))
        ));
        logistics.put("SF100005", List.of(
                new LogisticsNode("已揽收", "深圳转运中心", LocalDateTime.now().minusDays(1)),
                new LogisticsNode("运输中", "郑州转运中心", LocalDateTime.now().minusHours(12))
        ));
    }

    // ===== 查询方法 =====

    public OrderInfo getOrder(String orderId) {
        return orders.get(orderId);
    }

    public List<OrderInfo> getOrdersByUserId(String userId) {
        return orders.values().stream()
                .filter(o -> o.userId().equals(userId))
                .toList();
    }

    public UserInfo getUser(String userId) {
        return users.get(userId);
    }

    public List<LogisticsNode> getLogistics(String trackingNo) {
        return logistics.getOrDefault(trackingNo, List.of());
    }

    public RefundRecord addRefund(String orderId, String reason) {
        String refundId = "RF" + System.currentTimeMillis();
        OrderInfo order = orders.get(orderId);
        RefundRecord record = new RefundRecord(refundId, orderId, reason,
                "处理中", order != null ? order.price() : BigDecimal.ZERO,
                LocalDateTime.now());
        refunds.put(refundId, record);
        return record;
    }

    public RefundRecord getRefund(String refundId) {
        return refunds.get(refundId);
    }

    // ===== 数据模型 =====

    public record OrderInfo(String orderId, String userId, String productName,
                            String status, BigDecimal price,
                            LocalDateTime createTime, String trackingNo) {}

    public record UserInfo(String userId, String name, String phone, String level) {}

    public record LogisticsNode(String status, String location, LocalDateTime time) {}

    public record RefundRecord(String refundId, String orderId, String reason,
                               String status, BigDecimal amount, LocalDateTime createTime) {}
}
