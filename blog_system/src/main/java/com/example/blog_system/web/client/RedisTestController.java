package com.example.blog_system.web.client;

import com.example.blog_system.model.ResponseData.ArticleResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/redis")
public class RedisTestController {

    private static final Logger logger = LoggerFactory.getLogger(RedisTestController.class);

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 增强的Redis连接测试，支持无Redis环境
     */
    @GetMapping("/status")
    public ArticleResponseData testRedisConnection() {
        try {
            // 测试Redis连接是否可用
            String testKey = "blog:test:connection";
            String testValue = "Redis连接测试 - " + System.currentTimeMillis();

            // 尝试写入Redis
            redisTemplate.opsForValue().set(testKey, testValue, 60, TimeUnit.SECONDS);

            // 从Redis读取
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);

            boolean isConnected = testValue.equals(retrievedValue);

            Map<String, Object> result = new HashMap<>();
            result.put("connected", isConnected);
            result.put("testKey", testKey);
            result.put("testValue", testValue);
            result.put("retrievedValue", retrievedValue);
            result.put("timestamp", System.currentTimeMillis());
            result.put("message", "Redis连接正常");

            logger.info("Redis连接测试成功");
            return ArticleResponseData.ok(result);

        } catch (Exception e) {
            logger.warn("Redis连接不可用: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("connected", false);
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            result.put("message", "Redis服务未启动，应用将在无缓存模式下运行");
            result.put("suggestion", "请启动Redis服务或检查连接配置");

            return ArticleResponseData.ok(result); // 仍然返回成功，但connected为false
        }
    }

    /**
     * 增强的用户数据缓存测试
     */
    @PostMapping("/user/cache")
    public ArticleResponseData cacheUserData(@RequestParam String username,
                                             @RequestParam String email) {
        try {
            String userKey = "blog:user:" + username;

            Map<String, Object> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("email", email);
            userData.put("cachedAt", System.currentTimeMillis());
            userData.put("type", "user_profile");

            // 尝试缓存用户数据
            redisTemplate.opsForValue().set(userKey, userData, 10, TimeUnit.MINUTES);

            // 验证缓存
            Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(userKey);

            Map<String, Object> result = new HashMap<>();
            result.put("userKey", userKey);
            result.put("originalData", userData);
            result.put("cachedData", cachedData);
            result.put("cacheSuccess", cachedData != null);
            result.put("message", cachedData != null ? "缓存成功" : "缓存失败");

            logger.info("用户数据缓存测试完成，用户名: {}", username);
            return ArticleResponseData.ok(result);

        } catch (Exception e) {
            logger.warn("用户数据缓存测试失败（Redis不可用）: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("cacheSuccess", false);
            result.put("error", e.getMessage());
            result.put("message", "Redis服务不可用，无法执行缓存操作");
            result.put("suggestion", "请启动Redis服务");

            return ArticleResponseData.ok(result);
        }
    }

    /**
     * 增强的获取缓存数据接口
     */
    @GetMapping("/data/{key}")
    public ArticleResponseData getCachedData(@PathVariable String key) {
        try {
            Object cachedData = redisTemplate.opsForValue().get(key);

            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("data", cachedData);
            result.put("exists", cachedData != null);
            result.put("dataType", cachedData != null ? cachedData.getClass().getSimpleName() : "null");
            result.put("message", cachedData != null ? "数据获取成功" : "键不存在");

            return ArticleResponseData.ok(result);

        } catch (Exception e) {
            logger.warn("获取缓存数据失败: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("exists", false);
            result.put("error", e.getMessage());
            result.put("message", "Redis服务不可用");

            return ArticleResponseData.ok(result);
        }
    }

    /**
     * 简单的Redis状态检查
     */
    @GetMapping("/simple-status")
    public ArticleResponseData simpleStatus() {
        try {
            // 简单的ping测试
            redisTemplate.opsForValue().get("test");

            Map<String, Object> result = new HashMap<>();
            result.put("status", "connected");
            result.put("message", "Redis服务运行正常");
            result.put("timestamp", System.currentTimeMillis());

            return ArticleResponseData.ok(result);

        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "disconnected");
            result.put("message", "Redis服务未运行: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            result.put("suggestion", "请安装并启动Redis服务，或继续在无缓存模式下测试其他功能");

            return ArticleResponseData.ok(result);
        }
    }
}