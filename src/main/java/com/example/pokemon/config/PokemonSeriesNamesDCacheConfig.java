package com.example.pokemon.config;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;

@Configuration
public class PokemonSeriesNamesDCacheConfig {

    public static final String CACHE_NAME = "pokemonSeriesNamesD";

    /**
     * Per-cache TTL and serialization aligned with {@link CacheConfig} defaults, without modifying that class.
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public RedisCacheManagerBuilderCustomizer pokemonSeriesNamesDCacheCustomizer(CacheProperties cacheProperties) {
        return builder -> {
            ObjectMapper cacheObjectMapper = new ObjectMapper();
            cacheObjectMapper.registerModule(new JavaTimeModule());
            cacheObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            PolymorphicTypeValidator ptv =
                    BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build();
            cacheObjectMapper.activateDefaultTypingAsProperty(
                    ptv, ObjectMapper.DefaultTyping.NON_FINAL, "@class");
            GenericJackson2JsonRedisSerializer valueSerializer =
                    new GenericJackson2JsonRedisSerializer(cacheObjectMapper);

            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                    .serializeValuesWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                    .entryTtl(Duration.ofMinutes(5));

            CacheProperties.Redis redis = cacheProperties.getRedis();
            if (!redis.isCacheNullValues()) {
                config = config.disableCachingNullValues();
            }
            if (StringUtils.hasText(redis.getKeyPrefix())) {
                config = config.prefixCacheNameWith(redis.getKeyPrefix());
            }
            if (!redis.isUseKeyPrefix()) {
                config = config.disableKeyPrefix();
            }

            builder.withCacheConfiguration(CACHE_NAME, config);
        };
    }
}
