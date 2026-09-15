package com.example.tool;

import com.example.agent.data.MockDataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author liushug
 * @date 2026/8/17 11:04
 * @description 物流查询工具  Agent 的腿_用来追踪包裹位置
 */
@Component
@RequiredArgsConstructor
public class LogisticsTools {

    private final MockDataStore dataStore;

    @Tool(description = "根据物流单号查询物流轨迹，返回每个节点的状态、位置和更新时间。需要先通过订单查询获取物流单号")
    public String queryLogistics(
            @ToolParam(description = "物流单号，如SF100001") String trackingNo) {
        List<MockDataStore.LogisticsNode> nodes = dataStore.getLogistics(trackingNo);
        if (nodes.isEmpty()) {
            return "未找到物流信息：" + trackingNo + "，可能尚未发货或单号有误";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("【物流轨迹】单号：").append(trackingNo).append("\n");
        for (MockDataStore.LogisticsNode node : nodes) {
            sb.append(String.format("  [%s] %s - %s\n",
                    node.time(), node.status(), node.location()));
        }
        // 分析最新状态
        MockDataStore.LogisticsNode latest = nodes.get(nodes.size() - 1);
        sb.append("【当前状态】").append(latest.status())
                .append(" - ").append(latest.location());
        return sb.toString();
    }
}
