package com.example.tool;

import com.example.agent.data.MockDataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * @author liushug
 * @date 2026/8/17 11:16
 * @description Agent 的记忆--用来标识用户身份
 */
@Component
@RequiredArgsConstructor
public class UserTools {

    private final MockDataStore dataStore;

    @Tool(description = "根据用户ID查询用户基本信息，包括姓名、手机号、会员等级")
    public String queryUserInfo(
            @ToolParam(description = "用户ID，如1001") String userId) {
        MockDataStore.UserInfo user = dataStore.getUser(userId);
        if (user == null) {
            return "未找到用户：" + userId + "，请确认用户ID是否正确";
        }
        return String.format("【用户信息】用户ID：%s | 姓名：%s | 手机：%s | 会员等级：%s",
                user.userId(), user.name(), user.phone(), user.level());
    }


}
