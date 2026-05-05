package com.example.pokemon.config;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(CacheProperties cacheProperties) {
        return builder -> {
            ObjectMapper cacheObjectMapper = new ObjectMapper();
            cacheObjectMapper.registerModule(new JavaTimeModule());
            cacheObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            GenericJackson2JsonRedisSerializer valueSerializer =
                    new GenericJackson2JsonRedisSerializer(cacheObjectMapper);

            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                    .serializeValuesWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));

            CacheProperties.Redis redis = cacheProperties.getRedis();
            Duration ttl = redis.getTimeToLive();
            if (ttl != null) {
                config = config.entryTtl(ttl);
            } else {
                config = config.entryTtl(Duration.ofMinutes(10));
            }

            if (!redis.isCacheNullValues()) {
                config = config.disableCachingNullValues();
            }

            if (StringUtils.hasText(redis.getKeyPrefix())) {
                config = config.prefixCacheNameWith(redis.getKeyPrefix());
            }
            if (!redis.isUseKeyPrefix()) {
                config = config.disableKeyPrefix();
            }

            builder.cacheDefaults(config);
        };
    }
}
