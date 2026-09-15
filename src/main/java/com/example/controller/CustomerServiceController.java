package com.example.controller;

import com.example.service.CustomerServiceAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liushug
 * @description 客服控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cs")
public class CustomerServiceController {

    private final CustomerServiceAgent agent;

    @RequestMapping("/chat")
    public String chat(String message) {
        return agent.chat(message);
    }


}
