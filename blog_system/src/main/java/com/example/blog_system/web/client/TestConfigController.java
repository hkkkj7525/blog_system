package com.example.blog_system.web.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestConfigController {

    /**
     * 测试配置是否正常
     */
    @GetMapping("/config")
    public Map<String, Object> testConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Spring Security配置正常");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 测试数据库连接
     */
    @GetMapping("/database")
    public Map<String, Object> testDatabase() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "数据库连接正常");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}