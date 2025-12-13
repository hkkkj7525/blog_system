// TestController.java
package com.example.blog_system.web.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class TestController {

    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "博客系统API运行正常");
        result.put("status", "success");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}