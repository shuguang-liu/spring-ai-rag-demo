package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liushug
 * @description TODO
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final JdbcTemplate jdbcTemplate;

    public StatsController(@Qualifier("authJdbcTemplate")JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/orders")
    public String orderStats() {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders", Integer.class);
        Integer completed = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE status = 'completed'", Integer.class);
        Integer pending = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE status = 'pending'", Integer.class);

        return String.format(
                "本月订单统计：总数 %d，已完成 %d，待处理 %d",
                total, completed, pending);
    }

}
