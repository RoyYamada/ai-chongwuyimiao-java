package com.example.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/db")
@Tag(name = "数据库", description = "数据库连接测试相关接口")
public class DbController {
    private final JdbcTemplate jdbcTemplate;

    public DbController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/ping")
    @Operation(summary = "数据库连接测试", description = "测试数据库连接是否正常")
    public String ping() {
        Integer v = jdbcTemplate.queryForObject("select 1", Integer.class);
        return "ok: " + v;
    }
}
