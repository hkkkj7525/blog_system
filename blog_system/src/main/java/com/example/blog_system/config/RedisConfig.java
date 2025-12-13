package com.example.blog_system.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * 自定义Redis配置类，进行序列化以及RedisTemplate设置
 * 添加Redis连接失败的降级处理
 */
@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
public class RedisConfig extends CachingConfigurerSupport {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    // 缓存有效期常量
    private static final Duration CACHE_TTL = Duration.ofDays(1); // 统一改为1天

    /**
     * 创建JSON序列化器
     */
    private Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();

        // 设置序列化配置
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 使用新的方法激活默认类型，替代已废弃的enableDefaultTyping
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        serializer.setObjectMapper(objectMapper);
        return serializer;
    }

    /**
     * 定制Redis API模板RedisTemplate
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();

        try {
            template.setConnectionFactory(redisConnectionFactory);

            // 分别设置key和value的序列化器
            RedisSerializer<String> stringSerializer = new StringRedisSerializer();
            Jackson2JsonRedisSerializer<Object> jsonSerializer = jackson2JsonRedisSerializer();

            // 设置key的序列化器
            template.setKeySerializer(stringSerializer);
            template.setHashKeySerializer(stringSerializer);

            // 设置value的序列化器
            template.setValueSerializer(jsonSerializer);
            template.setHashValueSerializer(jsonSerializer);

            template.afterPropertiesSet();
            logger.info("RedisTemplate配置初始化成功");

        } catch (Exception e) {
            logger.error("RedisTemplate配置初始化失败，应用将在无Redis模式下运行", e);
            // 在连接失败时，可以返回一个基础的RedisTemplate，但要注意后续使用可能会抛出异常
        }

        return template;
    }

    /**
     * 定制Redis缓存管理器RedisCacheManager
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        try {
            // 创建序列化器
            RedisSerializer<String> stringSerializer = new StringRedisSerializer();
            Jackson2JsonRedisSerializer<Object> jsonSerializer = jackson2JsonRedisSerializer();

            // 配置缓存序列化和有效期
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(CACHE_TTL) // 统一使用1天
                    .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                    .disableCachingNullValues(); // 不缓存空值

            RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory)
                    .cacheDefaults(config)
                    .transactionAware() // 支持事务
                    .build();

            logger.info("RedisCacheManager配置初始化成功");
            return cacheManager;

        } catch (Exception e) {
            logger.error("RedisCacheManager初始化失败", e);
            // 返回null可能导致缓存相关功能不可用
            return null;
        }
    }
}